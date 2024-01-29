package com.github.dactiv.service.commons.service.feign;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.spring.security.authentication.config.SpringSecurityProperties;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.service.commons.service.SystemConstants;
import com.github.dactiv.service.commons.service.domain.dto.ExportDataMeta;
import com.github.dactiv.service.commons.service.domain.meta.ResourceDictionaryMeta;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import lombok.NonNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源服务 feign 客户端
 *
 * @author maurice
 */
@FeignClient(value = SystemConstants.SYS_RESOURCE_NAME, configuration = FeignAuthenticationConfiguration.class)
public interface ResourceServiceFeignClient {

    String DEFAULT_IGNORE_INTERCEPTOR_PARAM_NAME = "ignoreInterceptor";

    String DEFAULT_VERIFY_SUCCESS_DELETE_PARAM_NAME = "verifySuccessDelete";

    String DEFAULT_VERIFY_TOKEN_EXIST_PARAM_NAME = "verifyTokenExist";

    String ROCKET_MERCHANT_CREATED_TAG = "merchantCreated";

    String ROCKET_MERCHANT_DELETED_TAG = "merchantDeleted";

    String ROCKET_MERCHANT_CREATED_BROADCASTING_DESTINATION = SystemConstants.RABBIT_MQ_EXCHANGE_NAME + CacheProperties.DEFAULT_SEPARATOR + ROCKET_MERCHANT_CREATED_TAG;

    String ROCKET_MERCHANT_DELETED_BROADCASTING_DESTINATION = SystemConstants.RABBIT_MQ_EXCHANGE_NAME + CacheProperties.DEFAULT_SEPARATOR + ROCKET_MERCHANT_DELETED_TAG;

    String ARGS_FIELD_NAME = "args";

    String ARGS_GENERATE_FIELD_NAME = "generate";

    String PHONE_NUMBER_PARAM_NAME = "phoneNumberParamName";

    String EMAIL_PARAM_NAME = "emailParamName";

    String SMS_CAPTCHA_PARAM_NAME = "_smsCaptcha";

    String EMAIL_CAPTCHA_PARAM_NAME = "_emailCaptcha";

    /**
     * 根据数名称获取数据字典集合
     *
     * @param name 字典名称
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionaries/{code}")
    List<ResourceDictionaryMeta> findDataDictionaries(@PathVariable("code") String name);

    /**
     * 根据字典类型查询数据字典
     *
     * @param typeId 字典类型 id
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionariesByTypeId")
    List<ResourceDictionaryMeta> findDataDictionariesByTypeId(@RequestParam("typeId") Integer typeId);

    /**
     * 根据字典类型查询数据字典
     *
     * @param typeId 字典类型 id
     * @return 数据字典集合
     */
    @GetMapping("findMdmDataDictionariesByTypeId")
    List<ResourceDictionaryMeta> findMdmDataDictionariesByTypeId(@RequestParam("typeId") Integer typeId);

    /**
     * 创建生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截类型
     * @param interceptType 拦截的 token 类型
     * @return 绑定 token
     */
    @PostMapping("captcha/createCaptchaIntercept")
    Map<String, Object> createCaptchaIntercept(@RequestParam("token") String token,
                                               @RequestParam("type") String type,
                                               @RequestParam("interceptType") String interceptType);

    /**
     * 获取商户
     *
     * @param id 商户 id
     * @return rest 结果
     */
    @GetMapping("merchant/get")
    Map<String, Object> getMerchant(@RequestParam("id") Integer id);

    /**
     * 创建验证码绑定 token
     *
     * @param type             验证码类型
     * @param deviceIdentified 唯一识别
     * @return 绑定 token
     */
    @GetMapping("captcha/generateToken")
    Map<String, Object> createCaptchaToken(@RequestParam("type") String type,
                                           @RequestParam("deviceIdentified") String deviceIdentified,
                                           @RequestParam Map<String, Object> appendParms);

    /**
     * 生成验证码
     *
     * @param param 验证码 token 信息
     * @return 生成结果
     */
    @GetMapping("captcha/generateCaptcha")
    Object generateCaptcha(@RequestParam Map<String, Object> param);

    /**
     * 校验验证码
     *
     * @param param 参数信息
     * @return rest 结果集
     */
    @PostMapping("captcha/verifyCaptcha")
    RestResult<Object> verifyCaptcha(@RequestParam Map<String, Object> param);

    /**
     * 删除验证码
     *
     * @param param 参数信息
     * @return rest 结果集
     */
    @PostMapping("captcha/deleteCaptcha")
    RestResult<Object> deleteCaptcha(@RequestParam Map<String, Object> param);

    /**
     * 获取资源字典
     *
     * @param code 字典代码
     * @return 资源字典
     */
    @GetMapping("getDataDictionary/{code}")
    ResourceDictionaryMeta getResourceDictionary(@PathVariable("code") String code);

    /**
     * 获取资源字典
     *
     * @param code 字典代码
     * @return 资源字典
     */
    @GetMapping("getMdmDataDictionary/{code}")
    ResourceDictionaryMeta getMdmResourceDictionary(@PathVariable("code") String code);

    /**
     * 上传单个附件
     *
     * @param file         文件内容
     * @param type         桶名称
     * @param requestParam 附加参数
     * @return rest 结果集
     */
    @PostMapping(value = "attachment/singleUpload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    RestResult<Map<String, Object>> singleUploadAttachmentFile(@RequestPart(value = Config.FILE_FIELD_NAME) MultipartFile file,
                                                               @PathVariable("type") String type,
                                                               @RequestParam Map<String, String> requestParam);

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     * @return 字节流
     */
    @GetMapping("attachment/query")
    byte[] getAttachmentFile(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName);

    /**
     * 判断文件是否存在
     *
     * @param bucketName 同名称
     * @param objectName 对象名称
     * @return true 存在，否则 false
     */
    @GetMapping("attachment/isObjectExist")
    boolean isAttachmentFileExist(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName);

    @PostMapping("attachment/delete")
    RestResult<Object> deleteAttachment(@RequestBody List<FileObject> fileObjects, @RequestParam Map<String, Object> appendParam);

    class Config {

        public static final String FILE_FIELD_NAME = "file";

        @Bean
        public RequestInterceptor feignAuthRequestInterceptor(SpringSecurityProperties springSecurityProperties) {

            return requestTemplate -> FeignAuthenticationConfiguration.initRequestTemplate(
                    requestTemplate,
                    springSecurityProperties
            );
        }

        @Bean
        public Encoder feignFormEncoder() {

            return new SpringFormEncoder();

        }
    }

    default RestResult<Map<String, Object>> export(List<Map<String, Object>> data, ExportDataMeta meta, RedissonClient redissonClient) {
        ExcelWriter writer = ExcelUtil.getWriter();

        writer.write(data, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.flush(outputStream, true);
        writer.close();

        meta.setSize(outputStream.toByteArray().length);
        RestResult<Map<String, Object>> result = singleUploadAttachmentFile(
                new MockMultipartFile(meta.getFilename(), meta.toUploadFilename(), MediaType.APPLICATION_CBOR_VALUE, outputStream.toByteArray()),
                SystemConstants.EXPORT_BUCKET.getBucketName(),
                new LinkedHashMap<>()
        );

        RBucket<ExportDataMeta> bucket = redissonClient.getBucket(SystemConstants.USER_EXPORT_CACHE.getName(meta.toExportCacheName()));
        TimeProperties time = SystemConstants.USER_EXPORT_CACHE.getExpiresTime();
        if (HttpStatus.OK.value() == result.getStatus()) {
            meta.setExecuteStatus(ExecuteStatus.Success);
            meta.setMeta(result.getData());
            bucket.setAsync(meta, time.getValue(), time.getUnit());
        } else if (meta.getRetryCount() >= meta.getMaxRetryCount()) {
            meta.setLastExportTime(new Date());
            meta.setRetryCount(meta.getRetryCount() + 1);
            bucket.setAsync(meta, time.getValue(), time.getUnit());
            throw new SystemException(result.getMessage());
        }

        RBucket<ExportDataMeta> exportBucket = redissonClient.getBucket(SystemConstants.USER_EXPORT_CACHE.getName(meta.toExportCacheName()));
        exportBucket.set(meta);

        return result;
    }

    class MockMultipartFile implements MultipartFile {

        private final String name;

        private final String originalFilename;

        private final String contentType;

        private final byte[] content;


        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name    the name of the file
         * @param content the content of the file
         */
        public MockMultipartFile(String name, @Nullable byte[] content) {
            this(name, "", null, content);
        }

        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name          the name of the file
         * @param contentStream the content of the file as stream
         * @throws IOException if reading from the stream failed
         */
        public MockMultipartFile(String name, InputStream contentStream) throws IOException {
            this(name, "", null, FileCopyUtils.copyToByteArray(contentStream));
        }

        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name             the name of the file
         * @param originalFilename the original filename (as on the client's machine)
         * @param contentType      the content type (if known)
         * @param content          the content of the file
         */
        public MockMultipartFile(
                String name, @Nullable String originalFilename, @Nullable String contentType, @Nullable byte[] content) {

            Assert.hasLength(name, "Name must not be empty");
            this.name = name;
            this.originalFilename = (originalFilename != null ? originalFilename : "");
            this.contentType = contentType;
            this.content = (content != null ? content : new byte[0]);
        }

        /**
         * Create a new MockMultipartFile with the given content.
         *
         * @param name             the name of the file
         * @param originalFilename the original filename (as on the client's machine)
         * @param contentType      the content type (if known)
         * @param contentStream    the content of the file as stream
         * @throws IOException if reading from the stream failed
         */
        public MockMultipartFile(
                String name, @Nullable String originalFilename, @Nullable String contentType, InputStream contentStream)
                throws IOException {

            this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
        }


        @Override
        @NonNull
        public String getName() {
            return this.name;
        }

        @Override
        public String getOriginalFilename() {
            return this.originalFilename;
        }

        @Nullable
        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public boolean isEmpty() {
            return (this.content.length == 0);
        }

        @Override
        public long getSize() {
            return this.content.length;
        }

        @Override
        public byte @NonNull [] getBytes() {
            return this.content;
        }

        @Override
        @NonNull
        public InputStream getInputStream() {
            return new ByteArrayInputStream(this.content);
        }

        @Override
        public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
            FileCopyUtils.copy(this.content, dest);
        }
    }
}
