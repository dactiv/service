package com.github.dactiv.service.resource.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.framework.captcha.CaptchaProperties;
import com.github.dactiv.framework.captcha.CaptchaService;
import com.github.dactiv.framework.captcha.DelegateCaptchaService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.minio.FilenameObject;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.CodecUtils;
import com.github.dactiv.framework.minio.MinioTemplate;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.authentication.token.PrincipalAuthenticationToken;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.enumerate.AttachmentTypeEnum;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.resource.domain.body.SendAttachmentEmailRequestBody;
import com.github.dactiv.service.resource.resolver.AttachmentResolver;
import com.github.dactiv.service.resource.service.AttachmentService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 附件管理的控制器
 *
 * @author maurice.chen
 * @since 2022-02-16 01:48:39
 */
@Slf4j
@RestController
@RequestMapping("attachment")
@Plugin(
        name = "上传内容管理",
        id = "attachment",
        parent = "resource",
        icon = "icon-report-management",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    private final DelegateCaptchaService delegateCaptchaService;

    private final List<AttachmentResolver> attachmentResolvers;

    /**
     * 获取附件信息
     *
     * @param type     附件类型
     * @param filename 文件名称
     * @return 对象集合
     */
    @PostMapping("list")
    @PreAuthorize("isAuthenticated()")
    public List<Map<String, Object>> list(@RequestParam String type,
                                          @CurrentSecurityContext SecurityContext securityContext,
                                          String filename) throws Exception {
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class);
        Bucket bucket = Bucket.of(attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()));

        ListObjectsArgs.Builder builder = ListObjectsArgs
                .builder()
                .bucket(bucket.getBucketName())
                .includeUserMetadata(true)
                .recursive(true)
                .useApiVersion1(false);

        if (StringUtils.isNotBlank(filename)) {
            builder.prefix(filename);
        }

        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        Iterable<Result<Item>> results = attachmentService.getMinioTemplate().getMinioClient().listObjects(builder.build());

        List<Map<String, Object>> items = new LinkedList<>();
        for (Result<Item> result : results) {
            Item item = result.get();

            StatObjectResponse statObjectResponse = attachmentService.getMinioTemplate().statObject(FileObject.of(bucket.getBucketName(), item.objectName()), item.etag());
            if (attachmentService.isInaccessible(securityUserDetails, statObjectResponse.userMetadata())) {
                continue;
            }

            Map<String, Object> map = attachmentService.convertFields(statObjectResponse, statObjectResponse.getClass(), attachmentService.getAttachmentConfig().getResult().getStatObjectIgnoreFields());
            if (items.stream().anyMatch(i -> Objects.equals(i.get(FileObject.MINIO_ETAG), map.get(FileObject.MINIO_ETAG)))) {
                continue;
            }

            items.add(map);
        }

        return items;
    }

    /**
     * 删除文件
     *
     * @param fileObjects 文件对象集合
     * @return reset 结果集
     * @throws Exception 删除错误时抛出
     */
    @PostMapping("delete")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> delete(@RequestBody List<FileObject> fileObjects,
                                @CurrentSecurityContext SecurityContext securityContext,
                                @RequestParam Map<String, Object> appendParam) throws Exception {

        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        for (FileObject object : fileObjects) {

            AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(object.getBucketName(), AttachmentTypeEnum.class, true);
            if (Objects.nonNull(attachmentType)) {
                object.setBucketName(attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()));
            }

            if (StringUtils.endsWith(object.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {

                FileObject listFileObject = FileObject.of(StringUtils.removeStart(object.getBucketName(), attachmentService.getAttachmentConfig().getBucketPrefix()), object.getObjectName());

                List<Map<String, Object>> listFile = list(listFileObject.getBucketName(), securityContext, listFileObject.getObjectName());

                List<FileObject> fileObjectList = listFile
                        .stream()
                        .map(f -> FileObject.of(f.get(FileObject.MINIO_BUCKET_NAME).toString(), f.get(FileObject.MINIO_OBJECT_NAME).toString()))
                        .peek(f -> f.setBucketName(StringUtils.removeStart(f.getBucketName(), attachmentService.getAttachmentConfig().getBucketPrefix())))
                        .collect(Collectors.toList());

                RestResult<?> deleteResult = delete(fileObjectList, securityContext, appendParam);
                Assert.isTrue(deleteResult.isSuccess(), deleteResult.getMessage());
            } else {

                StatObjectResponse statObjectResponse = attachmentService.getMinioTemplate().statObject(object);
                if (attachmentService.isInaccessible(securityUserDetails, statObjectResponse.userMetadata())) {
                    continue;
                }

                List<AttachmentResolver> attachmentResolver = attachmentResolvers
                        .stream()
                        .filter(a -> a.isSupport(attachmentType))
                        .toList();

                for (AttachmentResolver resolver : attachmentResolver) {
                    RestResult<?> result = resolver.preDelete(object.getObjectName(), object, appendParam);
                    if (Objects.nonNull(result) && HttpStatus.OK.value() != result.getStatus()) {
                        return result;
                    }
                }

                attachmentService.getMinioTemplate().deleteObject(object);

                for (AttachmentResolver resolver : attachmentResolver) {
                    resolver.postDelete(object.getObjectName(), object, appendParam);
                }
            }

        }

        if (fileObjects.size() == 1) {
            return RestResult.of("删除 [" + fileObjects.getFirst().getObjectName() + "] 成功");
        } else {
            return RestResult.of("删除 " + fileObjects.size() + " 个文件成功");
        }

    }

    /**
     * 完成分片上传
     *
     * @param type       桶类型
     * @param objectName 文件名称
     * @param id         上传 id
     * @return rest 结果集
     */
    @PostMapping("completeMultipartUpload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> completeMultipartUpload(@PathVariable("type") String type,
                                                 @RequestParam String objectName,
                                                 @RequestParam String id,
                                                 @RequestParam Map<String, Object> appendParam) throws Exception {
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);
        String bucket = attachmentService.getAttachmentConfig().getBucketName(type);
        FileObject fileObject = FileObject.of(bucket, objectName);

        Map<String, Object> result = attachmentService.completeMultipartUpload(fileObject, id);

        this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .forEach(e -> e.completeMultipartUpload(fileObject, result, appendParam));

        return RestResult.ofSuccess("合并 [" + objectName + "] 文件成功", result);
    }

    /**
     * 创建文件上传
     *
     * @param type       桶类型
     * @param objectName 文件对象
     * @param chunkSize  片数量
     * @return rest 结果集
     */
    @PostMapping("createMultipartUpload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Map<String, Object>> createMultipartUpload(@PathVariable("type") String type,
                                                                 @RequestParam String objectName,
                                                                 @RequestParam String contentType,
                                                                 @RequestParam Integer chunkSize,
                                                                 @RequestParam Map<String, Object> appendParam) throws Exception {
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);
        String bucket = attachmentService.getAttachmentConfig().getBucketName(type);
        FileObject fileObject = FileObject.of(bucket, objectName);
        FilenameObject filenameObject = FilenameObject.of(fileObject);

        List<AttachmentResolver> attachmentResolvers = this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();

        for (AttachmentResolver resolver : attachmentResolvers) {
            RestResult<Map<String, Object>> execute = resolver.createMultipartUpload(filenameObject, appendParam);
            if (Objects.isNull(execute)) {
                continue;
            }
            if (!execute.isSuccess()) {
                return execute;
            }

            if (MapUtils.isNotEmpty(execute.getData())) {
                result.put(resolver.getKeyName(), execute.getData());
            }
        }

        Map<String, Object> data = attachmentService.createMultipartUpload(filenameObject, attachmentType, contentType, chunkSize);

        result.putAll(data);
        return RestResult.ofSuccess("创建 [" + filenameObject.getFilename() + "] 的分片信息成功", result);
    }


    /**
     * 验证码上传文件
     *
     * @param files   文件信息
     * @param request http 请求
     * @return reset 结果集
     * @throws Exception 上传文件错误时候抛出
     */
    @PostMapping("captchaUpload")
    public RestResult<Map<String, Object>> captchaUpload(@RequestParam List<MultipartFile> files, HttpServletRequest request) throws Exception {
        String captchaType = request.getParameter(CaptchaProperties.DEFAULT_CAPTCHA_TYPE_PARAM_NAME);
        Assert.hasText(captchaType, CaptchaProperties.DEFAULT_CAPTCHA_TYPE_PARAM_NAME + " 参数不能为空");

        RestResult<Map<String, Object>> result = delegateCaptchaService.verify(request);

        if (!result.isSuccess()) {
            return result;
        }

        CaptchaService captchaService = delegateCaptchaService.getCaptchaServiceByType(captchaType);
        captchaService.getBuildToken(request);
        String captchaValue = request.getParameter(captchaService.getCaptchaParamName());
        String tokenValue = request.getParameter(captchaService.getTokenParamName());

        Assert.hasText(captchaValue, captchaService.getCaptchaParamName() + " 参数不能为空");
        Map<String, Object> appendParam = Casts.castArrayValueMapToObjectValueMap(request.getParameterMap());
        String prefixValue = captchaType + AntPathMatcher.DEFAULT_PATH_SEPARATOR + tokenValue + AntPathMatcher.DEFAULT_PATH_SEPARATOR + captchaValue;
        appendParam.put(attachmentService.getAttachmentConfig().getUploadFilePrefixParamName(), prefixValue);

        Map<String, String> userMetadata = new LinkedHashMap<>();
        userMetadata.put(MinioTemplate.UPLOADER_ID, tokenValue);
        userMetadata.put(MinioTemplate.UPLOADER_TYPE, captchaValue);

        appendParam.put(MinioTemplate.USER_METADATA, userMetadata);
        for (MultipartFile file : files) {
            RestResult<Map<String, Object>> uploadResult = singleUpload(file, null, AttachmentTypeEnum.CAPTCHA_VERIFY_FILE.getValue(), appendParam);
            Assert.isTrue(uploadResult.getStatus() == HttpStatus.OK.value(), uploadResult.getMessage());
        }
        return RestResult.of("上传 " + files.size() + "个文件成功");
    }

    @GetMapping("getCaptchaObject")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<byte[]> getCaptchaObject(@CurrentSecurityContext SecurityContext securityContext,
                                                   @RequestParam String objectName,
                                                   Map<String, Object> appendParam) throws Exception {

        String bucket = attachmentService.getAttachmentConfig().getBucketName(AttachmentTypeEnum.CAPTCHA_VERIFY_FILE.getValue());
        FileObject fileObject = FileObject.of(bucket, objectName);
        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        StatObjectResponse statObjectResponse = attachmentService.getMinioTemplate().statObject(fileObject);

        if (attachmentService.isInaccessible(securityUserDetails, statObjectResponse.userMetadata())) {
            throw new InternalAuthenticationServiceException("您没有权限访问此资源");
        }

        String filename = objectName;
        if (statObjectResponse.userMetadata().containsKey(FilenameObject.MINIO_ORIGINAL_FILE_NAME)) {
            filename = statObjectResponse.userMetadata().get(FilenameObject.MINIO_ORIGINAL_FILE_NAME);
        }

        if (!appendParam.containsKey(FilenameObject.MINIO_ORIGINAL_FILE_NAME)) {
            appendParam.put(FilenameObject.MINIO_ORIGINAL_FILE_NAME, filename);
        }

        return getObjectResponseEntity(fileObject, appendParam);
    }

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @param type 桶类型
     * @return reset 结果集
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("singleUpload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Map<String, Object>> singleUpload(MultipartFile file,
                                                        @CurrentSecurityContext SecurityContext securityContext,
                                                        @PathVariable String type,
                                                        @RequestParam Map<String, Object> appendParam) throws Exception {
        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);

        List<AttachmentResolver> attachmentResolvers = this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .toList();

        String bucketName = Objects.isNull(attachmentType) ? type : attachmentService.getAttachmentConfig().getBucketName(type);
        FileObject fileObject = FileObject.of(bucketName, file.getOriginalFilename());
        FilenameObject filenameObject = FilenameObject.of(fileObject);
        Map<String, Object> result = new LinkedHashMap<>();

        SecurityUserDetails userDetails = null;
        if (securityContext != null) {
            userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        }

        Map<String, String> userMetadata = new LinkedHashMap<>();
        if (appendParam.containsKey(MinioTemplate.USER_METADATA)) {
            Map<String, String> paramUserMetadata = Casts.convertValue(appendParam.get(MinioTemplate.USER_METADATA), new TypeReference<>() {
            });
            userMetadata.putAll(paramUserMetadata);
        }

        for (AttachmentResolver resolver : attachmentResolvers) {
            RestResult<Map<String, Object>> execute = resolver.preUpload(file, filenameObject, userDetails, userMetadata, appendParam);
            if (Objects.isNull(execute)) {
                continue;
            }
            if (execute.isSuccess()) {
                return execute;
            } else if (MapUtils.isNotEmpty(execute.getData())) {
                result.put(resolver.getKeyName(), execute.getData());
            }
        }

        if (Objects.nonNull(userDetails)) {
            userMetadata.put(MinioTemplate.UPLOADER_ID, userDetails.getId().toString());
            userMetadata.put(MinioTemplate.UPLOADER_TYPE, userDetails.getType());
        }

        ObjectWriteResponse response = attachmentService.getMinioTemplate().putObject(
                filenameObject,
                file.getInputStream(),
                userMetadata
        );

        if (MapUtils.isEmpty(result)) {
            result = attachmentService.convertFields(response, response.getClass(), attachmentService.getAttachmentConfig().getResult().getUploadResultIgnoreFields());
            result.putAll(Casts.convertValue(attachmentService.getLinkUrl(filenameObject), Casts.MAP_TYPE_REFERENCE));
        } else {
            Map<String, Object> fields = attachmentService.convertFields(response, response.getClass(), attachmentService.getAttachmentConfig().getResult().getUploadResultIgnoreFields());
            fields.putAll(Casts.convertValue(attachmentService.getLinkUrl(filenameObject), Casts.MAP_TYPE_REFERENCE));
            result.put(attachmentService.getAttachmentConfig().getSourceField(), fields);
        }

        result.put(FilenameObject.MINIO_ORIGINAL_FILE_NAME, filenameObject.getFilename());
        result.put(HttpHeaders.CONTENT_TYPE, file.getContentType());

        for (AttachmentResolver resolver : attachmentResolvers) {
            resolver.postUpload(file, result, response, userDetails, userMetadata, appendParam);
        }

        return RestResult.ofSuccess("上传完成", result);
    }

    /**
     * 上传文件
     *
     * @param files 文件
     * @param type  桶类型
     * @return reset 结果集
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("upload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<List<Map<String, Object>>> upload(@RequestParam List<MultipartFile> files,
                                                        @CurrentSecurityContext SecurityContext securityContext,
                                                        @PathVariable String type,
                                                        @RequestParam Map<String, Object> appendParam) throws Exception {
        List<Map<String, Object>> list = new LinkedList<>();

        for (MultipartFile file : files) {
            RestResult<Map<String, Object>> result = singleUpload(file, securityContext, type, appendParam);

            if (!result.isSuccess()) {
                log.warn("上传文件 [" + file.getName() + "] 失败，原因为:" + result.getMessage());
                continue;
            }

            list.add(result.getData());
        }

        return RestResult.ofSuccess("上传完成", list);

    }

    /**
     * 查看对象信息
     *
     * @param type     桶类型
     * @param filename 文件名
     * @return 对象信息
     * @throws Exception 获取失败时抛出
     */
    @GetMapping("info/{type}/{filename}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Map<String, String>> info(@PathVariable("type") String type,
                                                @PathVariable("filename") String filename) throws Exception {

        FileObject fileObject = FileObject.of(
                attachmentService.getAttachmentConfig().getBucketName(type),
                filename
        );

        GetObjectResponse is = attachmentService.getMinioTemplate().getObject(fileObject);
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : is.headers().names()) {
            result.put(name, is.headers().get(name));
        }

        return RestResult.ofSuccess(result);
    }

    /**
     * 获取预签署 url
     *
     * @param type     桶类型
     * @param filename 文件名
     * @return rest 结果集
     * @throws Exception 获取失败时抛出
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("presignedUrl/{type}/{filename}")
    public RestResult<String> presignedUrl(@PathVariable("type") String type,
                                           @PathVariable("filename") String filename,
                                           @RequestParam String method,
                                           @RequestParam Map<String, Object> appendParam) throws Exception {

        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class);

        List<AttachmentResolver> attachmentResolvers = this.attachmentResolvers
                .stream()
                .filter(a -> a.isSupport(attachmentType))
                .toList();

        FileObject fileObject = FileObject.of(
                attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()),
                filename
        );

        String url = attachmentService.getMinioTemplate().getPresignedObjectUrl(fileObject, Method.valueOf(method));

        attachmentResolvers.forEach(a -> a.presignedUrl(fileObject, url, appendParam));

        return RestResult.ofSuccess(url);
    }

    /**
     * 查询预签署 url
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @param method     签署方法
     * @return rest 结果集
     * @throws Exception 查询错误时抛出
     */
    @GetMapping("queryPresignedUrl")
    public RestResult<String> queryPresignedUrl(@RequestParam String bucketName,
                                                @RequestParam String objectName,
                                                @RequestParam String method,
                                                @RequestParam Map<String, Object> appendParam) throws Exception {

        String remove = StringUtils.appendIfMissing(
                attachmentService.getAttachmentConfig().getBucketPrefix(),
                Casts.DOT
        );
        String name = StringUtils.removeStart(bucketName, remove);

        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(name, AttachmentTypeEnum.class, true);
        FileObject fileObject;
        List<AttachmentResolver> attachmentResolvers = new LinkedList<>();
        if (Objects.nonNull(attachmentType)) {
            fileObject = FileObject.of(
                    attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()),
                    objectName
            );
            attachmentResolvers = this.attachmentResolvers
                    .stream()
                    .filter(a -> a.isSupport(attachmentType))
                    .toList();
        } else {
            fileObject = FileObject.of(bucketName, objectName);
        }

        String url = attachmentService.getMinioTemplate().getPresignedObjectUrl(fileObject, Method.valueOf(method));
        attachmentResolvers.forEach(a -> a.presignedUrl(fileObject, url, appendParam));

        return RestResult.ofSuccess(url);
    }

    /**
     * 获取文件
     *
     * @param type     桶类型
     * @param filename 文件名
     * @return 文件流字节
     * @throws Exception 获取失败时抛出
     */
    @GetMapping("get/{type}/{filename}")
    public ResponseEntity<byte[]> get(@PathVariable("type") String type,
                                      @PathVariable("filename") String filename,
                                      @CurrentSecurityContext SecurityContext securityContext,
                                      @RequestParam Map<String, Object> appendParam) throws Exception {

        if (!SystemConstants.EXPORT_BUCKET.getBucketName().equals(type)) {
            AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(type, AttachmentTypeEnum.class, true);
            if (Objects.nonNull(attachmentType)) {
                type = attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue());
            }
        }
        FileObject fileObject = FileObject.of(type, filename);

        SecurityUserDetails userDetails = null;
        if (securityContext.getAuthentication() instanceof PrincipalAuthenticationToken) {
            userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        }

        return getObject(fileObject, userDetails, appendParam);
    }

    /**
     * 判断对象是否存在
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return true 存在，否则 false
     */
    @GetMapping("isObjectExist")
    public boolean isObjectExist(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName) {
        return attachmentService.getMinioTemplate().isObjectExist(FileObject.of(bucketName, objectName));
    }

    /**
     * 查询文件对象
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 字节流
     * @throws Exception 查询错误时抛出
     */
    @GetMapping("query")
    public Object query(@RequestParam String bucketName,
                        @RequestParam String objectName,
                        @RequestParam(required = false, defaultValue = "false") boolean base64,
                        @CurrentSecurityContext SecurityContext securityContext,
                        @RequestParam Map<String, Object> appendParam) throws Exception {

        SecurityUserDetails userDetails = null;
        if (securityContext.getAuthentication() instanceof PrincipalAuthenticationToken) {
            userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        }

        ResponseEntity<byte[]> response = getObject(FileObject.of(bucketName, objectName), userDetails, appendParam);

        if (!base64 || Objects.isNull(response.getBody())) {
            return response;
        } else {
            return RestResult.ofSuccess(Base64.encodeToString(response.getBody()));
        }
    }

    @PostMapping("getMultiObject")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<byte[]> getMultiObject(@RequestBody List<FilenameObject> list,
                                                 @CurrentSecurityContext SecurityContext securityContext) throws Exception {

        SecurityUserDetails userDetails = null;
        if (securityContext.getAuthentication() instanceof PrincipalAuthenticationToken) {
            userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
        }

        if (list.size() == 1) {
            return getObject(list.getFirst(), userDetails, new LinkedHashMap<>());
        }

        ByteArrayOutputStream responseData = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(responseData);

        for (int i = 0; i < list.size(); i++) {
            FilenameObject filenameObject = list.get(i);
            GetObjectResponse is = attachmentService.getMinioTemplate().getObject(filenameObject);
            String filename = filenameObject.getObjectName();
            if (StringUtils.isNotEmpty(filenameObject.getFilename())) {
                filename = filenameObject.getFilename();
            }
            ZipEntry zipEntry = new ZipEntry(filename + Casts.UNDERSCORE + (i + 1));
            zipOut.putNextEntry(zipEntry);

            byte[] data = IOUtils.toByteArray(is);
            is.close();

            IOUtils.write(data, zipOut);
        }

        IOUtils.close(zipOut, responseData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(
                SpringMvcUtils.DEFAULT_ATTACHMENT_NAME,
                URLEncoder.encode(attachmentService.getAttachmentConfig().getMultiFileTitle() + Casts.DOT + ResourceUtils.URL_PROTOCOL_ZIP, CodecUtils.DEFAULT_ENCODING)
        );

        return new ResponseEntity<>(responseData.toByteArray(), headers, HttpStatus.OK);
    }

    @PostMapping("sendEmail")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> sendEmail(@CurrentSecurityContext SecurityContext securityContext,
                                   @RequestBody SendAttachmentEmailRequestBody body) throws Exception {
        SecurityUserDetails securityUserDetails = Casts.cast(securityContext.getAuthentication().getDetails());

        for (FilenameObject filenameObject : body.getFiles()) {
            StatObjectResponse statObjectResponse = attachmentService.getMinioTemplate().statObject(filenameObject);
            if (!attachmentService.isInaccessible(securityUserDetails, statObjectResponse.userMetadata())) {
                throw new AccessDeniedException("当前存在不属于你可操作的文件信息。");
            }
        }

        List<String> emails = body.getToEmails();
        if (CollectionUtils.isEmpty(emails)) {
            SecurityUserDetails userDetails = Casts.cast(securityContext.getAuthentication().getDetails());
            String email = userDetails.getMeta().getOrDefault(SecurityUserDetailsConstants.EMAIL_KEY, StringUtils.EMPTY).toString();
            emails.add(email);
        }

        attachmentService.sendEmail(emails, body.getFiles());

        return RestResult.of("发送文件 " + body.getFiles().stream().map(FilenameObject::getFilename).toList() + " 到 " + emails + " 邮件成功");

    }

    /**
     * 获取文件对象
     *
     * @param fileObject 文件对象信息
     * @return 文件字节响应实体
     */
    public ResponseEntity<byte[]> getObject(FileObject fileObject,
                                            SecurityUserDetails securityUserDetails,
                                            Map<String, Object> appendParam) throws Exception {

        String remove = StringUtils.appendIfMissing(
                attachmentService.getAttachmentConfig().getBucketPrefix(),
                Casts.DOT
        );
        String name = StringUtils.removeStart(fileObject.getBucketName(), remove);

        AttachmentTypeEnum attachmentType = ValueEnumUtils.parse(name, AttachmentTypeEnum.class, true);
        List<AttachmentResolver> attachmentResolvers = new LinkedList<>();
        if (Objects.nonNull(attachmentType)) {
            this.attachmentResolvers
                    .stream()
                    .filter(a -> a.isSupport(attachmentType))
                    .forEach(attachmentResolvers::add);
        }

        attachmentResolvers.forEach(a -> a.preGetObject(fileObject, securityUserDetails, appendParam));

        ResponseEntity<byte[]> response = getObjectResponseEntity(fileObject, appendParam);

        attachmentResolvers.forEach(a -> a.postGetObject(fileObject, response, securityUserDetails, appendParam));

        return response;
    }

    private ResponseEntity<byte[]> getObjectResponseEntity(FileObject fileObject, Map<String, Object> appendParam) throws Exception {
        GetObjectResponse is = attachmentService.getMinioTemplate().getObject(fileObject);
        String contentType = is.headers().get(HttpHeaders.CONTENT_TYPE);
        HttpHeaders headers = new HttpHeaders();

        if (StringUtils.isNotEmpty(contentType)) {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            headers.setContentType(mediaType);
        }

        String objectName;
        if (StringUtils.contains(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {
            objectName = StringUtils.substringAfterLast(fileObject.getObjectName(), AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        } else {
            objectName = fileObject.getObjectName();
        }

        String filename = appendParam.getOrDefault(FilenameObject.MINIO_ORIGINAL_FILE_NAME, StringUtils.EMPTY).toString();

        if (StringUtils.isEmpty(filename) && fileObject instanceof FilenameObject) {
            FilenameObject filenameObject = Casts.cast(fileObject, FilenameObject.class);
            filename = filenameObject.getFilename();
        }

        if (StringUtils.isEmpty(filename)) {
            filename = objectName;
        }

        headers.setContentDispositionFormData(
                SpringMvcUtils.DEFAULT_ATTACHMENT_NAME,
                URLEncoder.encode(filename, CodecUtils.DEFAULT_ENCODING)
        );
        byte[] data = IOUtils.toByteArray(is);
        is.close();

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

}
