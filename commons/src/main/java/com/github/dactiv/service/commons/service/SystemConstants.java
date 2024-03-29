package com.github.dactiv.service.commons.service;

import com.github.dactiv.framework.commons.CacheProperties;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.minio.Bucket;

import java.util.concurrent.TimeUnit;

/**
 * 系统常量类
 *
 * @author maurice.chen
 */
public interface SystemConstants {


    String DATA_CRYPTO_BEAN_NAME = "aesEcbCryptoService";

    String LOGIN_TYPE_KEY = "loginType";

    /**
     * 默认 rabbit mq 交换机名称
     */
    String RABBIT_EXCHANGE_NAME = "dactiv_service";

    /**
     * 认证系统名称
     */
    String SYS_AUTHENTICATION_NAME = "authentication";

    /**
     * 数据中台系统名称
     */
    String SYS_DMP_NAME = "dmp";

    /**
     * 网关服务名称
     */
    String SYS_GATEWAY_NAME = "gateway";

    /**
     * 资源系统名称
     */
    String SYS_RESOURCE_NAME = "resource";

    /**
     * 消息系统名称
     */
    String SYS_MESSAGE_NAME = "message";

    /**
     * 认证服务 rabbit mq topic
     */
    String AUTHENTICATION_RABBIT_EXCHANGE = RABBIT_EXCHANGE_NAME + Casts.UNDERSCORE + SYS_AUTHENTICATION_NAME;

    /**
     * 资源服务 rabbit mq 交换机
     */
    String RESOURCE_RABBIT_EXCHANGE = RABBIT_EXCHANGE_NAME + Casts.UNDERSCORE + SYS_RESOURCE_NAME;

    /**
     * 消息服务 rabbit mq topic
     */
    String MESSAGE_RABBIT_EXCHANGE = RABBIT_EXCHANGE_NAME + Casts.UNDERSCORE + SYS_MESSAGE_NAME;

    /**
     * 数据中台服务 rabbit mq topic
     */
    String DMP_RABBIT_EXCHANGE = RABBIT_EXCHANGE_NAME + Casts.UNDERSCORE + SYS_DMP_NAME;

    /**
     * 第三方商户数据变更队列名称
     */
    String MERCHANT_DATA_CHANGE_QUEUE_NAME = RABBIT_EXCHANGE_NAME + "_resource_merchant_data_change";

    /**
     * 第三方商户数据删除队列名称
     */
    String MERCHANT_DATA_DELETE_QUEUE_NAME = RABBIT_EXCHANGE_NAME + "_resource_merchant_data_delete";

    /**
     * 电话号码正则表达式
     */
    String PHONE_NUMBER_REGULAR_EXPRESSION = "^1[3456789]\\d{9}$";

    /**
     * 验证码 token 名称
     */
    String CAPTCHA_TOKEN_NAME = "captchaToken";

    String CAPTCHA_TOKEN_PARAM_NAME = "tokenParamName";

    String IP_ADDRESS_NAME = "ipAddress";

    String MAC_ADDRESS_NAME = "macAddress";

    String MERCHANT_ID_TABLE_FIELD_NAME = "merchant_id";

    String APP_ID_FIELD_NAME = "appId";

    String APP_KEY_FIELD_NAME = "appKey";

    String STATUS_TABLE_FLED_NAME = "status";

    String TOKEN_FIELD_NAME = "token";

    String EXECUTE_STATUS_TABLE_FIELD_NAME = "execute_status";

    String ELASTICSEARCH_SYNC_QUEUE_NAME = DMP_RABBIT_EXCHANGE + "_elasticsearch_sync";

    /**
     * 导出的桶信息
     */
    Bucket EXPORT_BUCKET = Bucket.of("dactiv.service.resource.temp");

    /**
     * 用户导出内容缓存
     */
    CacheProperties USER_IMPORT_CACHE = CacheProperties.of("dactiv:service:resources:user:import", TimeProperties.of(7, TimeUnit.DAYS));

    /**
     * 用户导入内容缓存
     */
    CacheProperties USER_EXPORT_CACHE = CacheProperties.of("dactiv:service:resources:user:export:", TimeProperties.of(7, TimeUnit.DAYS));

}
