package com.github.dactiv.service.resource.resolver.support;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.enumerate.AttachmentTypeEnum;
import com.github.dactiv.service.commons.service.enumerate.ResourceSourceEnum;
import com.github.dactiv.service.resource.config.AttachmentConfig;
import com.github.dactiv.service.resource.resolver.AttachmentResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基础文件解析器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class BasicFileResolver implements AttachmentResolver {

    private final AttachmentConfig attachmentConfig;

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isSupport(AttachmentTypeEnum attachmentType) {
        return true;
    }

    @Override
    public RestResult<Map<String, Object>> preUpload(MultipartFile file,
                                                     FileObject fileObject,
                                                     SecurityUserDetails userDetails,
                                                     Map<String, String> userMetadata,
                                                     Map<String, Object> appendParam) throws IOException {

        RestResult<Map<String, Object>> result = preValid(fileObject, appendParam);

        if (Objects.nonNull(result)) {
            return result;
        }

        return AttachmentResolver.super.preUpload(file, fileObject, userDetails, userMetadata, appendParam);
    }

    @Override
    public RestResult<Map<String, Object>> createMultipartUpload(FileObject fileObject, Map<String, Object> appendParam) {
        RestResult<Map<String, Object>> result = preValid(fileObject, appendParam);

        if (Objects.nonNull(result)) {
            return result;
        }

        return AttachmentResolver.super.createMultipartUpload(fileObject, appendParam);
    }

    public RestResult<Map<String, Object>> preValid(FileObject fileObject, Map<String, Object> appendParam) {

        String filePrefix = appendParam.getOrDefault(attachmentConfig.getUploadFilePrefixParamName(), StringUtils.EMPTY).toString();
        if (StringUtils.isBlank(filePrefix)) {

            List<String> prefixBucketNameList = attachmentConfig
                    .getUploadPrefixType()
                    .stream()
                    .map(attachmentConfig::getBucketName)
                    .toList();

            if (prefixBucketNameList.contains(fileObject.getBucketName()) && attachmentConfig.getUploadPrefixType().contains(fileObject.getBucketName())) {
                return RestResult.of(
                        "参数 " + attachmentConfig.getUploadFilePrefixParamName() + " 不能为空",
                        HttpStatus.BAD_REQUEST.value(),
                        ErrorCodeException.DEFAULT_EXCEPTION_CODE
                );
            }
        }

        if (StringUtils.isNotEmpty(filePrefix)) {
            String path = StringUtils.appendIfMissing(filePrefix, AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            fileObject.setObjectName(path + fileObject.getObjectName());
        }

        return null;
    }

    @Override
    public void preGetObject(FileObject fileObject, SecurityUserDetails securityUserDetails, Map<String, Object> appendParam) {
        if (!fileObject.getBucketName().contains(AttachmentTypeEnum.CAPTCHA_VERIFY_FILE.getValue())) {
            AttachmentResolver.super.preGetObject(fileObject, securityUserDetails, appendParam);
            return;
        }

        if (securityUserDetails == null) {
            throw new InternalAuthenticationServiceException(HttpStatus.FORBIDDEN.getReasonPhrase());
        }

        if (!ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(securityUserDetails.getType())) {
            throw new InternalAuthenticationServiceException(HttpStatus.FORBIDDEN.getReasonPhrase());
        }

    }
}
