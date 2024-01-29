package com.github.dactiv.service.resource.resolver;

import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.enumerate.AttachmentTypeEnum;
import io.minio.ObjectWriteResponse;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 附件解析器
 *
 * @author maurice.chen
 */
public interface AttachmentResolver extends Ordered {

    /**
     * 是否支持附件类型
     *
     * @param attachmentType 附件类型
     * @return true 是，否则 false
     */
    boolean isSupport(AttachmentTypeEnum attachmentType);

    /**
     * 上传文件完成后触发此方法
     *
     * @param file     文件内容
     * @param result   上传结果
     * @param response minio 响应内容
     */
    default void postUpload(MultipartFile file,
                            Map<String, Object> result,
                            ObjectWriteResponse response,
                            SecurityUserDetails userDetails,
                            Map<String, String> userMetadata,
                            Map<String, Object> appendParam) {

    }

    /**
     * 删除文件前触发此方法
     *
     * @param filename   文件名称
     * @param fileObject minio 文件对象
     * @return rest 结果集，当结果集的状态非 200 时，会终止上传
     */
    default RestResult<?> preDelete(String filename, FileObject fileObject, Map<String, Object> appendParam) {
        return null;
    }

    /**
     * 删除文件后触发此方法
     *
     * @param filename   文件名称
     * @param fileObject minio 文件对象
     */
    default void postDelete(String filename, FileObject fileObject, Map<String, Object> appendParam) {

    }

    /**
     * 上传文件前触发此方法
     *
     * @param file        上传的文件
     * @param fileObject  minio 文件对象
     * @param appendParam 附加参数
     * @param userDetails 当前上传用户
     * @return rest 结果集，当结果集的状态非 200 时，会终止上传
     */
    default RestResult<Map<String, Object>> preUpload(MultipartFile file,
                                                      FileObject fileObject,
                                                      SecurityUserDetails userDetails,
                                                      Map<String, String> userMetadata,
                                                      Map<String, Object> appendParam) throws IOException {
        return null;
    }

    /**
     * 获取预签署 url 时触发此方法
     *
     * @param fileObject  文件对象描述
     * @param url         url
     * @param appendParam 附加参数
     */
    default void presignedUrl(FileObject fileObject, String url, Map<String, Object> appendParam) {

    }

    /**
     * 获取对象后时触发此方法
     *
     * @param fileObject          文件对象
     * @param response            spring mvc 响应对象
     * @param securityUserDetails 当前用户
     * @param appendParam         附加数据
     */
    default void postGetObject(FileObject fileObject, ResponseEntity<byte[]> response, SecurityUserDetails securityUserDetails, Map<String, Object> appendParam) {

    }

    default void preGetObject(FileObject fileObject, SecurityUserDetails securityUserDetails, Map<String, Object> appendParam) {

    }

    /**
     * 获取键名称
     *
     * @return 键名称
     */
    default String getKeyName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 创建分片上传时触发此方法
     *
     * @param fileObject  文件对象
     * @param appendParam 附加参数
     * @return rest 结果集，当结果集的状态非 200 时，会终止上传
     */
    default RestResult<Map<String, Object>> createMultipartUpload(FileObject fileObject,
                                                                  Map<String, Object> appendParam) {
        return null;
    }

    /**
     * 合并分排尿完成后触发此方法
     *
     * @param fileObject  文件对象
     * @param result      合并结果内容
     * @param appendParam 附加参数
     */
    default void completeMultipartUpload(FileObject fileObject,
                                         Map<String, Object> result,
                                         Map<String, Object> appendParam) {

    }

    @Override
    default int getOrder() {
        return Integer.MIN_VALUE;
    }
}
