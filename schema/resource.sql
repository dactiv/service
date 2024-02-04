/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : dactiv_resource

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 04/02/2024 08:56:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_access_crypto
-- ----------------------------
DROP TABLE IF EXISTS `tb_access_crypto`;
CREATE TABLE `tb_access_crypto` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `type` varchar(32) NOT NULL COMMENT '类型',
  `value` varchar(256) NOT NULL COMMENT '值',
  `request_decrypt` tinyint NOT NULL COMMENT '是否请求解密，0.否, 1.是',
  `response_encrypt` tinyint NOT NULL COMMENT '是否响应加密，0.否, 1.是',
  `enabled` tinyint NOT NULL COMMENT '是否启用，1.是，0.否',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_value` (`value`) USING BTREE
) ENGINE=InnoDB COMMENT='访问加解密表';

-- ----------------------------
-- Records of tb_access_crypto
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_access_crypto_predicate
-- ----------------------------
DROP TABLE IF EXISTS `tb_access_crypto_predicate`;
CREATE TABLE `tb_access_crypto_predicate` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `value` varchar(256) NOT NULL COMMENT '值',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  `access_crypto_id` int NOT NULL COMMENT '访问加解密 id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_access_crypto_id` (`access_crypto_id`) USING BTREE
) ENGINE=InnoDB COMMENT='访问加解密条件表';

-- ----------------------------
-- Records of tb_access_crypto_predicate
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_carousel
-- ----------------------------
DROP TABLE IF EXISTS `tb_carousel`;
CREATE TABLE `tb_carousel` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `type` smallint NOT NULL COMMENT '类型',
  `link` json NOT NULL COMMENT '链接地址',
  `status` smallint NOT NULL COMMENT '状态:10.新创建,15.已更新,20.已发布',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `release_time` datetime DEFAULT NULL COMMENT '发布时间',
  `revocation_time` datetime DEFAULT NULL COMMENT '下架时间',
  `cover` json NOT NULL COMMENT '封面',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='轮播图';

-- ----------------------------
-- Records of tb_carousel
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_data_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `tb_data_dictionary`;
CREATE TABLE `tb_data_dictionary` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `code` varchar(256) NOT NULL COMMENT '键名称',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `level` varchar(32) DEFAULT NULL COMMENT '等级',
  `value` text NOT NULL COMMENT '值',
  `value_type` smallint DEFAULT NULL COMMENT '值类型',
  `meta` json DEFAULT NULL,
  `enabled` tinyint DEFAULT '1' COMMENT '状态:0.禁用,1.启用',
  `type_id` int NOT NULL COMMENT '对应字典类型',
  `parent_id` int DEFAULT NULL COMMENT '根节点为 null',
  `sort` smallint DEFAULT NULL COMMENT '顺序值',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_code` (`code`) USING BTREE,
  KEY `ix_parent_id` (`parent_id`) USING BTREE,
  KEY `ix_type_id` (`type_id`) USING BTREE
) ENGINE=InnoDB COMMENT='数据字典';

-- ----------------------------
-- Records of tb_data_dictionary
-- ----------------------------
BEGIN;
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (1, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.type.server', '服务端加解密', NULL, 'server', 30, NULL, 1, 5, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (2, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.type.mobile', '移动端加解密', NULL, 'mobile', 30, NULL, 1, 5, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (3, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.after', '时间之后', NULL, 'After', 30, NULL, 1, 4, NULL, NULL, '在该日期时间之后发生的请求都将被匹配，如：datetime=2020-01-20T17:42:47.789，在 2020-01-20 17:42:47之后发生的请求都被匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (4, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.before', '时间之前', NULL, 'Before', 30, NULL, 1, 4, NULL, NULL, '在该日期时间之后发生的请求都将被匹配，如：datetime=2020-01-20T17:42:47.789，在 2020-01-20 17:42:47之前发生的请求都被匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (5, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.between', '时间范围', NULL, 'Between', 30, NULL, 1, 4, NULL, NULL, '在该日期时间范围发生的请求都将被匹配，如：datetime1=2020-01-20T17:42:47.789 datetime1=2020-03-20T17:42:47.789，在 2020-01-20 17:42:47 到 2020-03-20T17:42:47 发生的请求都被匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (6, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.cookie', '请求Cookie匹配', NULL, 'Cookie', 30, NULL, 1, 4, NULL, NULL, '请求 Cookie 匹配，如：name=chocolate regexp=ch.p，表示 cookei 存在 chocolate 并且正则表达式对条件 ch.p 通过则匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (7, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.header', '请求头匹配', NULL, 'Header', 30, NULL, 1, 4, NULL, NULL, '请求头匹配，如：name=X-REQUST-ID regexp=d+，表示 header 存在 X-REQUST-ID 并且正则表达式对条件 d+ 通过则匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (8, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.host', '访问主机匹配', NULL, 'Host', 30, NULL, 1, 4, NULL, NULL, '访问主机匹配，如：patterns=**.somehost.org,**.anotherhost.org，表示访问来源是 somehost.org 或 **.anotherhost.org 时则匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (9, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.method', '请求方法匹配', NULL, 'Method', 30, NULL, 1, 4, NULL, NULL, '请求方法匹配，如：methods=POST,GET，表示请求是 POST 或 GET，表示请求是 时则匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (10, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.path', '请求路径匹配', NULL, 'Path', 30, NULL, 1, 4, NULL, NULL, '请求路径匹配，如：patterns=/foo/**,/bar/**，表示请求路径是带有/foo/前缀 或 /bar/前缀时则匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (11, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.query', '请求参数匹配', NULL, 'Query', 30, NULL, 1, 4, NULL, NULL, '请求参数匹配，如：param=id regexp=d+，表示请求参数是 id 并且正则表达式对条件 d+ 通过则匹配');
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (12, '2020-03-29 14:20:36.000', 1, 'system.crypto.access.predicate.remote-address', '访问IP匹配', NULL, 'RemoteAddr', 30, NULL, 1, 4, NULL, NULL, '访问IP匹配，如：sources=192.168.0.1/24,192.168.6.1/24 表示只有访问 IP 在 192.168.0.[1到24] 或 192.168.6.[1到24] 时则匹配');

INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (13, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.none', '无', NULL, 'NONE', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (14, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.iso10126', 'ISO10126Padding', NULL, 'ISO10126', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (15, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.oaep', 'OAEPPadding', NULL, 'OAEP', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (16, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-md5-and-mgf1', 'OAEPWithMD5AndMGF1Padding', NULL, 'OAEPWithMd5AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (17, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-sha1-and-mgf1', 'OAEPWithSHA-1AndMGF1Padding', NULL, 'OAEPWithSha1AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (19, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-sha-384-and-mgf1', 'OAEPWithSHA-384AndMGF1Padding', NULL, 'OAEPWithSha384AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (20, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.oaep-with-sha-512-and-mgf1', 'OAEPWithSHA-512AndMGF1Padding', NULL, 'OAEPWithSha512AndMgf1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (21, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.pkcs1', 'PKCS1Padding', NULL, 'PKCS1', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (22, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.pkcs5', 'PKCS5Padding', NULL, 'PKCS5', 30, NULL, 1, 6, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (23, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.padding-scheme.ssl3', 'SSL3Padding', NULL, 'SSL3', 30, NULL, 1, 6, NULL, NULL, NULL);

INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (24, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.mode.none', '无', NULL, 'NONE', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (25, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.mode.cbc', 'CBC', NULL, 'CBC', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (26, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.mode.cfb', 'CFB', NULL, 'CFB', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (27, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.mode.ctr', 'CTR', NULL, 'CTR', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (28, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.mode.ecb', 'ECB', NULL, 'ECB', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (29, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.mode.ofb', 'OFB', NULL, 'OFB', 30, NULL, 1, 7, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (30, '2020-03-29 14:20:36.000', 1, 'system.crypto.algorithm.mode.pcbc', 'PCBC', NULL, 'PCBC', 30, NULL, 1, 7, NULL, NULL, NULL);

INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (31, '2020-03-29 14:20:36.000', 1, 'system.sms.captcha.login', '登录或注册验证码', NULL, '【云众科技】您好，您正在执行 [{0}] 操作，此次操作的验证码为：{1}，请在{2}分钟内按页面提示提交验证码，切勿将验证码泄漏与他人，验证码提供给他人可能导致帐号被盗。', 30, NULL, 1, 8, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (32, '2023-06-23 00:43:03.000', 1, 'system.sms.captcha.bind.phone', '绑定手机号码', NULL, '【云众科技】您好，您正在执行 [{0}] 操作，此次操作的验证码为：{1}，请在{2}分钟内按页面提示提交验证码，切勿将验证码泄漏与他人，验证码提供给他人可能导致帐号被盗。', 30, NULL, 1, 8, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (33, '2023-06-23 00:44:29.000', 1, 'system.sms.captcha.unbind.phone', '解绑手机号码', NULL, '【云众科技】您好，您正在执行 [{0}] 操作，此次操作的验证码为：{1}，请在{2}分钟内按页面提示提交验证码，切勿将验证码泄漏与他人，验证码提供给他人可能导致帐号被盗。', 30, NULL, 1, 8, NULL, NULL, NULL);

INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (34, '2020-03-29 14:20:36.000', 1, 'system.email.captcha.bind', '绑定邮箱', NULL, '<div>\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\n               cellpadding=\"0\" role=\"presentation\">\n            <tbody>\n            <tr>\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0;\">\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                            <tbody>\n                                <tr>\n                                    <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                        <table border=\"0\" align=\"center\" style=\"border-collapse:collapse;border-spacing:0;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                                            <tbody>\n                                                <tr>\n                                                    <td style=\"width:150px;\">\n                                                        <a rel=\"noopener\" target=\"_blank\" href=\"https://5419n73j71.goho.co\">\n                                                            <img width=\"150\" style=\"border:none;\" src=\'https://5419n73j71.goho.co/login_logo.svg?version=1\'/>\n                                                        </a>\n                                                    </td>\n                                                </tr>\n                                            </tbody>\n                                        </table>\n                                    </td>\n                                </tr>\n                            </tbody>\n                        </table>\n                    </div>\n                </td>\n            </tr>\n            </tbody>\n        </table>\n    </div>\n    <div style=\"max-width:640px;margin:0 auto;box-shadow:0 1px 5px rgba(0,0,0,0.1);border-radius:4px;overflow:hidden\">\n        <div style=\"margin:0 auto;max-width:640px;background:#ffffff;\">\n            <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:#ffffff;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                <tbody>\n                    <tr>\n                        <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:40px 50px;\">\n                            <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\" class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                                <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                                    <tbody>\n                                    <tr>\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                            <div style=\"cursor:auto;color:#737F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:16px;line-height:24px;text-align:left;\">\n                                                <h2 style=\"font-family: Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-weight: 500;font-size: 20px;color: #4F545C;letter-spacing: 0.27px;\">您好!</h2>\n                                                <p>您正在使用【{0}】功能，验证码为：<strong>{1}</strong></p>\n                                                <p>请在{2}分钟内按页面提示提交验证码，切勿将验证码泄漏与他人，验证码提供给他人可能导致帐号被盗。</p>\n                                            </div>\n                                        </td>\n                                    </tr>\n\n                                    <tr>\n                                        <td style=\"word-break:break-word;font-size:0;padding:30px 0px;\"><p style=\"font-size:1px;margin:0 auto;border-top:1px solid #DCDDDE;width:100%;\"></p></td>\n                                    </tr>\n\n                                    <tr>\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                            <div style=\"cursor:auto;color:#747F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:13px;line-height:16px;text-align:left;\">\n                                                <p>\n                                                    此邮件为系统邮件，请勿回复，如果此邮箱与你无关，请忽略。\n                                                </p>\n                                            </div>\n                                        </td>\n                                    </tr>\n                                    </tbody>\n                                </table>\n                            </div>\n                        </td>\n                    </tr>\n                </tbody>\n            </table>\n        </div>\n    </div>\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\n               cellpadding=\"0\" role=\"presentation\">\n            <tbody>\n            <tr>\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0px;\">\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                            <tbody>\n                            <tr>\n                                <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                    <div style=\"cursor:auto;color:#99AAB5;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:12px;line-height:24px;text-align:center;\">\n                                        COPYRIGHT © 2023 广西云众科技有限责任公司, All rights ReservedHand-crafted & Made with 1.0.0\n                                    </div>\n                                </td>\n                            </tr>\n                            </tbody>\n                        </table>\n                    </div>\n                </td>\n            </tr>\n            </tbody>\n        </table>\n    </div>\n</div>', 30, NULL, 1, 8, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (35, '2023-06-23 00:39:15.000', 1, 'system.email.captcha.unbind', '解绑邮箱', NULL, '<div>\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\n               cellpadding=\"0\" role=\"presentation\">\n            <tbody>\n            <tr>\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0;\">\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                            <tbody>\n                                <tr>\n                                    <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                        <table border=\"0\" align=\"center\" style=\"border-collapse:collapse;border-spacing:0;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                                            <tbody>\n                                                <tr>\n                                                    <td style=\"width:150px;\">\n                                                        <a rel=\"noopener\" target=\"_blank\" href=\"https://5419n73j71.goho.co\">\n                                                            <img width=\"150\" style=\"border:none;\" src=\'https://5419n73j71.goho.co/login_logo.svg?version=1\'/>\n                                                        </a>\n                                                    </td>\n                                                </tr>\n                                            </tbody>\n                                        </table>\n                                    </td>\n                                </tr>\n                            </tbody>\n                        </table>\n                    </div>\n                </td>\n            </tr>\n            </tbody>\n        </table>\n    </div>\n    <div style=\"max-width:640px;margin:0 auto;box-shadow:0 1px 5px rgba(0,0,0,0.1);border-radius:4px;overflow:hidden\">\n        <div style=\"margin:0 auto;max-width:640px;background:#ffffff;\">\n            <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:#ffffff;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                <tbody>\n                    <tr>\n                        <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:40px 50px;\">\n                            <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\" class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                                <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                                    <tbody>\n                                    <tr>\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                            <div style=\"cursor:auto;color:#737F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:16px;line-height:24px;text-align:left;\">\n                                                <h2 style=\"font-family: Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-weight: 500;font-size: 20px;color: #4F545C;letter-spacing: 0.27px;\">您好!</h2>\n                                                <p>您正在使用【{0}】功能，验证码为：<strong>{1}</strong></p>\n                                                <p>请在{2}分钟内按页面提示提交验证码，切勿将验证码泄漏与他人，验证码提供给他人可能导致帐号被盗。</p>\n                                            </div>\n                                        </td>\n                                    </tr>\n\n                                    <tr>\n                                        <td style=\"word-break:break-word;font-size:0;padding:30px 0px;\"><p style=\"font-size:1px;margin:0 auto;border-top:1px solid #DCDDDE;width:100%;\"></p></td>\n                                    </tr>\n\n                                    <tr>\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                            <div style=\"cursor:auto;color:#747F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:13px;line-height:16px;text-align:left;\">\n                                                <p>\n                                                    此邮件为系统邮件，请勿回复，如果此邮箱与你无关，请忽略。\n                                                </p>\n                                            </div>\n                                        </td>\n                                    </tr>\n                                    </tbody>\n                                </table>\n                            </div>\n                        </td>\n                    </tr>\n                </tbody>\n            </table>\n        </div>\n    </div>\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\n               cellpadding=\"0\" role=\"presentation\">\n            <tbody>\n            <tr>\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0px;\">\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\n                            <tbody>\n                            <tr>\n                                <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\n                                    <div style=\"cursor:auto;color:#99AAB5;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:12px;line-height:24px;text-align:center;\">\n                                        COPYRIGHT © 2023 广西云众科技有限责任公司, All rights ReservedHand-crafted & Made with 1.0.0\n                                    </div>\n                                </td>\n                            </tr>\n                            </tbody>\n                        </table>\n                    </div>\n                </td>\n            </tr>\n            </tbody>\n        </table>\n    </div>\n</div>', 30, NULL, 1, 8, NULL, NULL, NULL);

INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (36, '2020-03-29 14:20:36.000', 1, 'system.notification.dynamic.at-user', '动态内容 @ 用户通知', NULL, '{0} 在他的动态中提到了您', 30, NULL, 1, 11, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (37, '2020-03-29 14:20:36.000', 1, 'system.notification.dynamic.comment', '动态评论通知', NULL, '{0} 在评论了您的动态', 30, NULL, 1, 11, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (38, '2020-03-29 14:20:36.000', 1, 'system.notification.dynamic.like', '动态点赞通知', NULL, '{0} 赞了您的动态', 30, NULL, 1, 11, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (39, '2020-03-29 14:20:36.000', 1, 'system.notification.dynamic.forward', '转发动态通知', NULL, '{0} 转发了您的动态', 30, NULL, 1, 11, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (40, '2020-03-29 14:20:36.000', 1, 'system.notification.comment.at-user', '评论内容 @ 用户通知', NULL, '{0} 在评论内容时提到了你的', 30, NULL, 1, 12, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (41, '2020-03-29 14:20:36.000', 1, 'system.notification.comment.reply', '回复评论通知', NULL, '{0} 回复了你的评论', 30, NULL, 1, 12, NULL, NULL, NULL);
INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (42, '2020-03-29 14:20:36.000', 1, 'system.notification.comment.like', '回复点赞通知', NULL, '{0} 点赞了你的评论', 30, NULL, 1, 12, NULL, NULL, NULL);

INSERT INTO `tb_data_dictionary` (`id`, `creation_time`, `version`, `code`, `name`, `level`, `value`, `value_type`, `meta`, `enabled`, `type_id`, `parent_id`, `sort`, `remark`) VALUES (43, '2023-06-30 01:01:51.000', 1, 'system.attachment.send.email', '导出文件', NULL, '<div>\r\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\r\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\r\n               cellpadding=\"0\" role=\"presentation\">\r\n            <tbody>\r\n            <tr>\r\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0;\">\r\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\r\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\r\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\r\n                            <tbody>\r\n                                <tr>\r\n                                    <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\r\n                                        <table border=\"0\" align=\"center\" style=\"border-collapse:collapse;border-spacing:0;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\r\n                                            <tbody>\r\n                                                <tr>\r\n                                                    <td style=\"width:150px;\">\r\n                                                           <img width=\"150\" style=\"border:none;\" src=\'https://i22g472016.zicp.fun/server/resource/attachment/query?bucketName=cloudmasses.saas.resource.system.file&objectName=skn.jpg\'/>\r\n                                                    </td>\r\n                                                </tr>\r\n                                            </tbody>\r\n                                        </table>\r\n                                    </td>\r\n                                </tr>\r\n                            </tbody>\r\n                        </table>\r\n                    </div>\r\n                </td>\r\n            </tr>\r\n            </tbody>\r\n        </table>\r\n    </div>\r\n    <div style=\"max-width:640px;margin:0 auto;box-shadow:0 1px 5px rgba(0,0,0,0.1);border-radius:4px;overflow:hidden\">\r\n        <div style=\"margin:0 auto;max-width:640px;background:#ffffff;\">\r\n            <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:#ffffff;\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\r\n                <tbody>\r\n                    <tr>\r\n                        <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:40px 50px;\">\r\n                            <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\" class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\r\n                                <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\r\n                                    <tbody>\r\n                                    <tr>\r\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\r\n                                            <div style=\"cursor:auto;color:#737F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:16px;line-height:24px;text-align:left;\">\r\n                                                <h2 style=\"font-family: Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-weight: 500;font-size: 20px;color: #4F545C;letter-spacing: 0.27px;\">您好!</h2>\r\n                                                <p>这是系统为您导出的文件数据【{0}】</p>\r\n                                                <p>请点击附件进行下载，保存好文件，以免邮箱附件过期。</p>\r\n                                            </div>\r\n                                        </td>\r\n                                    </tr>\r\n\r\n                                    <tr>\r\n                                        <td style=\"word-break:break-word;font-size:0;padding:30px 0px;\"><p style=\"font-size:1px;margin:0 auto;border-top:1px solid #DCDDDE;width:100%;\"></p></td>\r\n                                    </tr>\r\n\r\n                                    <tr>\r\n                                        <td align=\"left\" style=\"word-break:break-word;font-size:0;padding:0;\">\r\n                                            <div style=\"cursor:auto;color:#747F8D;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:13px;line-height:16px;text-align:left;\">\r\n                                                <p>\r\n                                                    此邮件为系统邮件，请勿回复，如果此邮箱与你无关，请忽略。\r\n                                                </p>\r\n                                            </div>\r\n                                        </td>\r\n                                    </tr>\r\n                                    </tbody>\r\n                                </table>\r\n                            </div>\r\n                        </td>\r\n                    </tr>\r\n                </tbody>\r\n            </table>\r\n        </div>\r\n    </div>\r\n    <div style=\"margin:0 auto;max-width:640px;background:transparent;\">\r\n        <table border=\"0\" align=\"center\" style=\"font-size:0;width:100%;background:transparent;\" cellspacing=\"0\"\r\n               cellpadding=\"0\" role=\"presentation\">\r\n            <tbody>\r\n            <tr>\r\n                <td style=\"text-align:center;vertical-align:top;direction:ltr;font-size:0;padding:20px 0px;\">\r\n                    <div style=\"vertical-align:top;display:inline-block;direction:ltr;font-size:13px;text-align:left;width:100%;\"\r\n                         class=\"mj-column-per-100 outlook-group-fix\" aria-labelledby=\"mj-column-per-100\">\r\n                        <table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" role=\"presentation\">\r\n                            <tbody>\r\n                            <tr>\r\n                                <td align=\"center\" style=\"word-break:break-word;font-size:0;padding:0;\">\r\n                                    <div style=\"cursor:auto;color:#99AAB5;font-family:Helvetica Neue, Helvetica, Arial, Lucida Grande, sans-serif;font-size:12px;line-height:24px;text-align:center;\">\r\n                                        COPYRIGHT © 2023 广西云众科技有限责任公司, All rights ReservedHand-crafted & Made with 1.0.0\r\n                                    </div>\r\n                                </td>\r\n                            </tr>\r\n                            </tbody>\r\n                        </table>\r\n                    </div>\r\n                </td>\r\n            </tr>\r\n            </tbody>\r\n        </table>\r\n    </div>\r\n</div> ', NULL, NULL, 1, 8, NULL, NULL, NULL);

COMMIT;

-- ----------------------------
-- Table structure for tb_dictionary_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_dictionary_type`;
CREATE TABLE `tb_dictionary_type` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `code` varchar(128) NOT NULL COMMENT '键名称',
  `name` varchar(64) NOT NULL COMMENT '类型名称',
  `parent_id` int DEFAULT NULL COMMENT '父字典类型,根节点为 null',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_type` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据字典类型';

-- ----------------------------
-- Records of tb_dictionary_type
-- ----------------------------
BEGIN;
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (1, '2020-03-29 13:48:39.000', 5, 'system', '系统配置项', NULL, '');
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (2, '2020-03-29 13:49:01.000', 1, 'system.crypto', '加解密', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (3, '2020-03-29 14:16:09.000', 1, 'system.crypto.access', '访问', 2, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (4, '2020-03-29 14:18:01.000', 1, 'system.crypto.access.predicate', '条件', 3, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (5, '2020-03-29 14:18:54.000', 1, 'system.crypto.access.type', '类型', 3, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (6, '2020-03-29 14:18:54.000', 1, 'system.crypto.algorithm.padding-scheme', '加解密算法填充方案', 3, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (7, '2020-03-29 14:18:54.000', 1, 'system.crypto.algorithm.mode', '加解密算法模型', 3, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (8, '2020-03-29 14:18:54.000', 1, 'system.email', '邮箱', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (9, '2020-03-29 14:18:54.000', 1, 'system.sms', '短信', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (10, '2020-03-29 14:18:54.000', 1, 'system.notification', '通知', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (11, '2020-03-29 14:18:54.000', 1, 'system.notification.dynamic', '动态通知', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (12, '2020-03-29 14:18:54.000', 1, 'system.notification.comment', '评论通知', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (13, '2020-03-29 14:18:54.000', 1, 'system.region', '区域', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (14, '2021-08-16 11:21:52.873', 1, 'system.region.province', '省', 13, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (15, '2020-03-29 14:18:54.000', 1, 'system.region.city', '市', 13, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (16, '2020-03-29 14:18:54.000', 1, 'system.region.area', '县', 13, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (58, '2023-05-16 14:16:22.000', 1, 'system.hobby', '兴趣爱好', 1, NULL);
INSERT INTO `tb_dictionary_type` (`id`, `creation_time`, `version`, `code`, `name`, `parent_id`, `remark`) VALUES (59, '2023-07-22 15:37:55.000', 1, 'system.bank', '银行', 1, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_merchant
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant`;
CREATE TABLE `tb_merchant` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `name` varchar(128) NOT NULL COMMENT '名称',
  `app_id` varchar(32) NOT NULL COMMENT 'app id',
  `app_key` text NOT NULL COMMENT 'app key',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='第三方商户';

-- ----------------------------
-- Records of tb_merchant
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
