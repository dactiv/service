package com.github.dactiv.service.resource.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.ReflectionUtils;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.id.number.IntegerIdEntity;
import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.meta.IdValueMeta;
import com.github.dactiv.service.commons.service.domain.meta.TypeIdNameMeta;
import com.github.dactiv.service.commons.service.enumerate.AttachmentTypeEnum;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.commons.service.feign.MessageServiceFeignClient;
import com.github.dactiv.service.resource.config.AttachmentConfig;
import com.github.dactiv.service.resource.domain.entity.dictionary.DataDictionaryEntity;
import com.github.dactiv.service.resource.service.dictionary.DataDictionaryService;
import io.minio.CreateMultipartUploadResponse;
import io.minio.ListPartsResponse;
import io.minio.ObjectWriteResponse;
import io.minio.http.Method;
import io.minio.messages.Part;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 附件工具服务
 */
@Service
@RequiredArgsConstructor
public class AttachmentService implements InitializingBean {

    @Getter
    private final AttachmentConfig attachmentConfig;

    @Getter
    private final MinioTemplate minioTemplate;

    @Getter
    private final RedissonClient redissonClient;

    private final DataDictionaryService dataDictionaryService;

    private final MessageServiceFeignClient messageServiceFeignClient;

    /**
     * 转换目标对象和目标类的字段为 map
     *
     * @param target       目标对象
     * @param targetClass  目标类
     * @param ignoreFields 要忽略的字段名
     * @return map 对象
     */
    public Map<String, Object> convertFields(Object target, Class<?> targetClass, List<String> ignoreFields) {

        Map<String, Object> result = new LinkedHashMap<>();

        List<Field> fieldList = Arrays.asList(targetClass.getDeclaredFields());

        fieldList
                .stream()
                .filter(field -> !ignoreFields.contains(field.getName()))
                .forEach(field -> result.put(field.getName(), getFieldToValue(target, field)));

        if (Objects.nonNull(targetClass.getSuperclass())) {
            result.putAll(convertFields(target, targetClass.getSuperclass(), ignoreFields));
        }

        return result;
    }

    public IdValueMeta<String, String> getLinkUrl(FileObject fileObject) {
        String url = MessageFormat.format(
                attachmentConfig.getResult().getLinkUri(),
                fileObject.getBucketName(),
                fileObject.getObjectName()
        );

        return IdValueMeta.of(attachmentConfig.getResult().getLinkParamName(), url);
    }

    /**
     * 获取字段的 toString 值
     *
     * @param target 目标对象
     * @param field  字段
     * @return 值
     */
    private Object getFieldToValue(Object target, Field field) {
        Object value = ReflectionUtils.getFieldValue(target, field);

        if (Objects.isNull(value)) {
            return null;
        }

        if (StringUtils.startsWith(value.toString(), "\"") && (StringUtils.endsWith(value.toString(), "\""))) {
            value = StringUtils.unwrap(value.toString(), "\"");
        }

        if (ZonedDateTime.class.isAssignableFrom(value.getClass())) {
            ZonedDateTime zonedDateTime = Casts.cast(value);
            return Date.from(zonedDateTime.toInstant());
        }

        return value;
    }

    /**
     * 创建分片上传
     *
     * @param fileObject     文件对象
     * @param attachmentType 附件类型
     * @param contentType    内容类型
     * @param chunkSize      分片数量
     * @return 创建结果
     */
    public Map<String, Object> createMultipartUpload(FileObject fileObject, AttachmentTypeEnum attachmentType, String contentType, Integer chunkSize) throws Exception {
        Assert.isTrue(chunkSize > 0, "分片数量不能小于等于 0");
        CreateMultipartUploadResponse response = getMinioTemplate().createMultipartUpload(fileObject);
        String id = response.result().uploadId();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(IntegerIdEntity.ID_FIELD_NAME, id);

        TimeProperties expiresTime = attachmentConfig.getMultipartUploadCache().getExpiresTime();

        List<String> uploadUrls = new LinkedList<>();

        Map<String, String> param = new LinkedHashMap<>();
        param.put(MinioTemplate.UPLOAD_ID_PARAM_NAME, id);
        for (int i = 1; i <= chunkSize; i++) {
            param.put(MinioTemplate.PART_NUMBER_PARAM_NAME, String.valueOf(i));
            String uploadUrl = minioTemplate.getPresignedObjectUrl(fileObject, Method.PUT, expiresTime, param);
            uploadUrls.add(uploadUrl);
        }

        result.put(MinioTemplate.CHUNK_PARAM_NAME, uploadUrls);
        result.put(HttpHeaders.CONTENT_TYPE, contentType);
        result.put(TypeIdNameMeta.TYPE_FIELD_NAME, attachmentType.getValue());
        result.put(FileObject.MINIO_OBJECT_NAME, fileObject);

        String key = attachmentConfig.getMultipartUploadCache().getName(id);
        RBucket<Map<String, Object>> bucket = redissonClient.getBucket(key);

        bucket.setAsync(result, expiresTime.getValue(), expiresTime.getUnit());

        return result;

    }

    /**
     * 完成分片上传
     *
     * @param fileObject 文件对象
     * @param uploadId   上传 id
     */
    public Map<String, Object> completeMultipartUpload(FileObject fileObject, String uploadId) throws Exception {
        String key = attachmentConfig.getMultipartUploadCache().getName(uploadId);
        RBucket<Map<String, Object>> bucket = redissonClient.getBucket(key);
        Assert.isTrue(bucket.isExists(), "找不到 ID 为 [" + uploadId + "] 分片上传内容");
        Map<String, Object> map = bucket.get();
        List<String> chunkList = Casts.convertValue(map.get(MinioTemplate.CHUNK_PARAM_NAME), new TypeReference<>() {
        });

        Part[] parts = new Part[chunkList.size()];
        ListPartsResponse partResult = minioTemplate.listParts(fileObject, parts.length, uploadId);

        List<Part> partList = partResult.result().partList();
        Map<String, String> param = new LinkedHashMap<>();
        param.put(MinioTemplate.UPLOAD_ID_PARAM_NAME, uploadId);
        TimeProperties expirationTime = TimeProperties.of(1, TimeUnit.SECONDS);
        for (int i = 1; i <= partList.size(); i++) {
            parts[i - 1] = new Part(i, partList.get(i - 1).etag());
            param.put(MinioTemplate.PART_NUMBER_PARAM_NAME, String.valueOf(i));
            minioTemplate.getPresignedObjectUrl(fileObject, Method.PUT, expirationTime, param);
        }

        ObjectWriteResponse response = minioTemplate.completeMultipartUpload(fileObject, uploadId, parts);

        Map<String, Object> result = convertFields(response, response.getClass(), attachmentConfig.getResult().getUploadResultIgnoreFields());

        result.putAll(Casts.convertValue(getLinkUrl(fileObject), Casts.MAP_TYPE_REFERENCE));
        result.put(HttpHeaders.CONTENT_TYPE, map.get(HttpHeaders.CONTENT_TYPE));
        result.put(TypeIdNameMeta.TYPE_FIELD_NAME, map.get(TypeIdNameMeta.TYPE_FIELD_NAME));

        FileObject cacheFileObject = Casts.cast(map.get(FileObject.MINIO_OBJECT_NAME));

        if (FilenameObject.class.isAssignableFrom(cacheFileObject.getClass())) {
            FilenameObject filenameObject = Casts.cast(cacheFileObject);
            result.put(FilenameObject.MINIO_ORIGINAL_FILE_NAME, filenameObject.getFilename());
        }

        bucket.deleteAsync();

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (AttachmentTypeEnum type : AttachmentTypeEnum.values()) {
            minioTemplate.makeBucketIfNotExists(Bucket.of(attachmentConfig.getBucketName(type.getValue())));
        }

        minioTemplate.makeBucketIfNotExists(SystemConstants.EXPORT_BUCKET);
    }

    public void sendEmail(List<String> email, List<FilenameObject> files) {
        Assert.isTrue(CollectionUtils.isNotEmpty(email), "找不到可用的邮箱，请先输入邮箱在发送文件信息");
        Assert.isTrue(CollectionUtils.isNotEmpty(files), "找不到要发送的文件，请先选择文件在发送邮件");
        Map<String, Object> param = new LinkedHashMap<>();

        String title = files.size() > 1 ? LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + attachmentConfig.getMultiFileTitle() : files.iterator().next().getFilename();

        param.put(MessageServiceFeignClient.DEFAULT_MESSAGE_TYPE_KEY, MessageServiceFeignClient.DEFAULT_EMAIL_TYPE_VALUE);
        param.put(MessageServiceFeignClient.Constants.Email.TO_EMAILS_FIELD, email);
        param.put(MessageServiceFeignClient.DEFAULT_TITLE_KEY, title);
        param.put(TypeIdNameMeta.TYPE_FIELD_NAME, MessageTypeEnum.SYSTEM.toString());
        param.put(MessageServiceFeignClient.Constants.Email.ATTACHMENT_LIST_FIELD, files);

        DataDictionaryEntity dictionary = dataDictionaryService.getByCode(attachmentConfig.getSendEmailDictionaryCode());

        param.put(MessageServiceFeignClient.DEFAULT_CONTENT_KEY, MessageFormat.format(dictionary.getValue().toString(), title));
        RestResult<?> result = messageServiceFeignClient.send(param);
        Assert.isTrue(result.isSuccess(), result.getMessage());
    }
}
