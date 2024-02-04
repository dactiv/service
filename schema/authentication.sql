SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_authorization_consent
-- ----------------------------
DROP TABLE IF EXISTS `tb_authorization_consent`;
CREATE TABLE `tb_authorization_consent` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime DEFAULT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `user_id` int NOT NULL COMMENT '用户 id',
  `user_type` varchar(64) NOT NULL COMMENT '用户类型',
  `username` varchar(32) NOT NULL COMMENT '登陆账号',
  `authorities` json DEFAULT NULL COMMENT '权限信息',
  `merchant_client_id` char(32) NOT NULL COMMENT '对应的商户 id',
  `last_consent_time` datetime DEFAULT NULL COMMENT '最后同意时间',
  `expiration_time` json DEFAULT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  KEY `ix_merchant_client_id` (`merchant_client_id`)
) ENGINE=InnoDB COMMENT='商户授权同意用户信息';

-- ----------------------------
-- Table structure for tb_console_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_console_user`;
CREATE TABLE `tb_console_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `password` char(64) NOT NULL COMMENT '密码',
  `status` tinyint NOT NULL COMMENT '状态:1.启用、2.禁用、3.锁定',
  `username` varchar(32) NOT NULL COMMENT '登录帐号',
  `gender` tinyint NOT NULL COMMENT '性别:10.男,20.女',
  `real_name` varchar(16) NOT NULL COMMENT '真实姓名',
  `phone_number` varchar(64) DEFAULT NULL COMMENT '电话号码',
  `groups_info` json DEFAULT NULL COMMENT '组信息',
  `resource_map` json DEFAULT NULL COMMENT '资源信息',
  `last_authentication_time` datetime DEFAULT NULL COMMENT '最后认证(登入)时间',
  `remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `phone_number_verified` tinyint DEFAULT NULL COMMENT '是验证码手机号码',
  `email_verified` tinyint DEFAULT NULL COMMENT '是否验证邮箱',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_username` (`username`) USING BTREE,
  UNIQUE KEY `ux_email` (`email`) USING BTREE,
  UNIQUE KEY `ux_phone_number` (`phone_number`) USING BTREE
) ENGINE=InnoDB COMMENT='后台用户表';

-- ----------------------------
-- Records of tb_console_user
-- ----------------------------
BEGIN;
INSERT INTO `tb_console_user` (`id`, `creation_time`, `version`, `email`, `password`, `status`, `username`, `gender`, `real_name`, `phone_number`, `groups_info`, `resource_map`, `last_authentication_time`, `remark`, `phone_number_verified`, `email_verified`) VALUES (1, '2021-08-18 09:40:46.953', 1851, 'maurice.chen@foxmail.com', '$2a$10$U2787VFuFP9NMyxwdsP1bOmtvofTgwU5nLcdV7Gj3ZyhdiZO.T8mG', 1, 'admin', 10, '超级管理员', '18776974353', '[{\"id\": 1, \"name\": \"超级管理员\", \"status\": {\"name\": \"启用\", \"value\": 1}, \"authority\": \"ADMIN\"}]', '{}', '2024-01-26 15:31:46', '', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
DROP TABLE IF EXISTS `tb_group`;
CREATE TABLE `tb_group` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `creation_time` datetime(3) NOT NULL COMMENT '创建时间',
  `version` int NOT NULL DEFAULT '1' COMMENT '更新版本号',
  `name` varchar(32) NOT NULL COMMENT '名称',
  `authority` varchar(64) DEFAULT NULL COMMENT 'spring security role 的 authority 值',
  `sources` json NOT NULL COMMENT '来源',
  `parent_id` int DEFAULT NULL COMMENT '父类 id',
  `removable` tinyint NOT NULL COMMENT '是否可删除:0.否、1.是',
  `modifiable` tinyint NOT NULL COMMENT '是否可修改:0.否、1.是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态:0.禁用、1.启用',
  `resource_map` json DEFAULT NULL COMMENT '资源信息',
  `remark` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_name` (`name`) USING BTREE,
  UNIQUE KEY `ux_authority` (`authority`) USING BTREE
) ENGINE=InnoDB COMMENT='用户组表';

-- ----------------------------
-- Records of tb_group
-- ----------------------------
BEGIN;
INSERT INTO `tb_group` (`id`, `creation_time`, `version`, `name`, `authority`, `sources`, `parent_id`, `removable`, `modifiable`, `status`, `resource_map`, `remark`) VALUES (1, '2022-03-31 13:50:37.408', 18412, '超级管理员', 'ADMIN', '[\"CONSOLE\", \"SYSTEM\"]', NULL, 0, 1, 1, '{\"message\": [\"a0f2d25bf2ef74bc2ebff4d9eb7f4cd2\", \"be8e1a3df6ae809be3d57a9062df1413\", \"6b3f9d31e50fc1098f0c2b092cc1ea45\", \"a59b63ef86c075e96f71622f13a23ca8\", \"27577840c71a932c2a2675d78d560818\", \"db96e5b5e1032627a935a42e9ccfca82\", \"6b82516a8c10187be3b5dadfc2be45c6\", \"f7f9862a7aeeaab4289d1a047c3f038a\", \"97148f6b699cf46391eb1329542a14f0\", \"b1b33681c74e80023e489a8ee269ae51\", \"76872b6bb5e6f74ce40c940e39e6ab16\", \"b442f2c2620af0f116ec3ac57fb1b079\", \"2bb251dddeb54246f992621179770fcc\", \"5c77f38ab71a372c9c57b6d36f0ee458\", \"5d457c8425b3b8f51992bd274d3ce3ac\", \"9019e4c3cbaaa9642c64d2d5d34e3c0d\", \"1d512cf4076704ee1fb898880e87f335\", \"263a61d21524d4e155583b928d3be9d2\"], \"resource\": [\"ed2e2b02550eaf01cf236a948f013ae7\", \"68eee8afe8f71ae8595848181c1cdb01\", \"04c65a85c6a47b826a3dc1ef4b7a88e8\", \"bbd8056daeae646f8e32803ba23581b9\", \"1681f152e32a9e71180870decf283138\", \"8808ab117492f76f4652a5e9c0098689\", \"9b0fa20e1030063a5e76175d5984b689\", \"03cbf14bc30bad5ab3e678cbeb386c45\", \"4f60e24e0bb730c47dfd46fc90436f0f\", \"a63732ed2630e229f2b533afe61d5427\", \"074231d7ac992d54bc10575d6a74c177\", \"1286b5a03420a8d1ab07b99fe4f8b0f6\", \"34fdd9a0e09d6363f918c1e0e38d04d4\", \"a88131e6e9d1b842787b1df347a4b803\", \"eea79c5b59af69bbc1009a067c742098\", \"715c85ea46260874e644cd46378c2a1a\", \"66ed02bdc692d915372efeb533d17409\", \"b66b2590367b029f13e5745e9ce93880\"], \"master-data\": [\"bfe05408cc0508f4723542627aa26e64\", \"33c53eeb7d85d71bcc2f51bd59c596c2\", \"115957ace01053ed180a8ba8af1d5d15\", \"fc89781b2f3642e70daa770c1dcb604f\", \"55d9eb3784323068859b8ba6f48e849c\", \"2804ed259a211802f3202c45fc6283d2\", \"d8b14ab8877e5cdf5e2dcf2e80106578\", \"ae4e3562716080a5927b04a0d306f157\", \"de392fa3757c056e552ba56649733679\", \"36eedb32ebc14bbf3f5223959fdf4e20\", \"60d3db9c586ead2dfebaafb72fff958d\", \"2eb43ba9620d7ae58ebea0837c57f1aa\", \"69f381c58abed3bc8496afdf06005185\", \"c874e2ff8d8fb09f50207a58505faa1e\", \"d1a26d70064d257facecefadc8962fcd\", \"618079043a46e907ee0e509835b88627\", \"9c998626563e98d0e18f2b55e6a7629b\", \"9c26911d8a5de6d535217e195e2bd281\", \"57e89be63552af1f5cbfbe12d78d23c4\", \"f5370bb203099bfd513821b966daf825\", \"a6f6afbe34cc45fa8d32c213b7e83675\", \"2b49e27daec6b3ddb7be9b6718cfa5ec\", \"6a51bf090e6047eccb9ed4ff5a87110b\", \"04181cbfcbfb8fd944071ed5a165d079\", \"8f1da45c5f00c28b174bcbd361d32835\", \"0dd9ae6a2f74c1cc8803daafca4b1468\", \"7ce7b246512a109b1cdd9166ba36f444\", \"9595c809aab7dc5c6e10025810820767\", \"cee87d9e5e83c8a4f246804abd8c256f\", \"d3d540ef2361400320b06a06563acde2\", \"dec43f54d201060a8d70219af95d8e70\", \"7fd5dd2d016b1eb6c39d837c05b55412\", \"44fa0e3c88384f9ed392e79c61d592ca\", \"057e6d447a38c58a57b4f67d5d7df217\", \"8af3f83af4db6f06917f02dd1fd40ed5\", \"48e870346e2f9dc9d5a75ad44dbcada7\", \"eacd949ab38fb5304b96e5dbc0642889\", \"8289f842c7421fe35dfd35f32c9afa42\", \"934d455e1fcea05f0554620f7d539a0c\", \"f393fd4c69c5cd173d3b0edd24dc4d69\", \"a0c147edf2987397a37cb1b6b0f698a2\", \"4e773d5687a607347467c0438dde269b\"], \"authentication\": [\"f6fd3bb5fc01fbec5cd2bac6aeabfd68\", \"cb2c4335cdfb20116f1853fbee996f35\", \"2a65e78385ab6d7fc6382c5466008e17\", \"42d7809c9c3e4fe7d517a6f6d2bf2f41\", \"e3a35af48806fbd0d6d2e6619972429d\", \"bd8945869bb2ff541bf040e8c0806f1b\", \"1c41d4f61c8c1640364cb50b02c60b8c\", \"4f06ceb42529fb68fa9e9dbb84c8185c\", \"d95a86bc60ccfa71505a3e11981939c5\", \"f9b6beb3f33d1345ba26117595e85794\", \"1e169b726d51b6bab310095623c4ec4f\", \"209bfbfae0311b050b51ac97691f9de1\", \"856a252dba30e3cd6c7f1a05035faad0\", \"388a0f4bfd9624514298d69ed979b316\", \"4aa28de8d4671305d88be2a6aa5dacaa\", \"6303e9d0b8129b35c04dfe6690c89ad8\", \"32e1cd3a3580be914f3370de130618ea\", \"0a1a8b1eba799bb16d3580abd8a4027f\", \"4bf6f8284d5ae0a3ede4ea26408fc561\", \"86b0ed3d2ed6fed94b5f2227195e1e4f\", \"68f685a5b5df5ba3eb5104cb004ae5c2\", \"56afc875028c736f64ddca0cf541fd8d\", \"db93fa3b54aaaae17ec51660440fdb4b\", \"67b3215ef0b23b777f3d05101f4190b7\", \"436d94b4bc00705341395046b429328d\", \"9080cd3c936224b0157da2c09063880c\", \"5ed0e86335c762ce9d8c1cb59868a479\", \"516342ef9428454a32768d948e3a85f6\", \"94e57609d007bd91714d756e36c1dd1b\", \"d6c0f669fa91b033d07df99b378b84c6\", \"d77e898488aadeefda72f1846b1ac7b3\", \"c3804eae4d4aad5b123da25cb610e6fb\", \"caf08b310b4b3c9f1c97c60838784cbb\", \"485467cbe9a61055b86348bc650c9cd1\", \"e1eb6a6012b4714f3334014f2a6ec8f8\", \"78863e4fc51cd0744b472039faa4b936\", \"dbbfa7d852a31ab7c08f5799a42d038e\", \"af5110da0f73cef1237009c9fe049e6a\"]}', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_merchant_client
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_client`;
CREATE TABLE `tb_merchant_client` (
  `id` char(32) NOT NULL COMMENT '主键 id',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `merchant_id` int NOT NULL COMMENT '商户 id',
  `client_id` varchar(256) NOT NULL COMMENT '客户端 id',
  `client_id_issued_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '客户端 id发放时间',
  `client_secret` varchar(512) NOT NULL COMMENT '客户端密钥',
  `client_secret_expires_at` datetime NOT NULL COMMENT '客户端密钥过期时间',
  `client_name` varchar(256) NOT NULL COMMENT '客户端名称',
  `client_authentication_methods` json NOT NULL COMMENT '授权方法',
  `authorization_grant_types` json NOT NULL COMMENT '认证类型',
  `redirect_uris` json DEFAULT NULL COMMENT '重定向 url',
  `scopes` json NOT NULL COMMENT '授权作用域',
  `client_settings` json NOT NULL COMMENT '客户端设置',
  `token_settings` json NOT NULL COMMENT 'token 设置',
  `enable` tinyint NOT NULL COMMENT '是否启用:0.否,1.是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_client_id` (`client_id`) USING BTREE,
  UNIQUE KEY `ux_merchant_id` (`merchant_id`)
) ENGINE=InnoDB COMMENT='商家 OAuth 2 客户端注册信息';

-- ----------------------------
-- Records of tb_merchant_client
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for tb_sso_application
-- ----------------------------
DROP TABLE IF EXISTS `tb_sso_application`;
CREATE TABLE `tb_sso_application` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `creation_time` datetime NOT NULL COMMENT '创建时间',
  `merchant_client_id` char(32) NOT NULL COMMENT '商户客户端 id',
  `icon` varchar(1024) DEFAULT NULL COMMENT 'icon 图标',
  `url` varchar(1024) NOT NULL COMMENT '应用链接',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `environment` tinyint NOT NULL COMMENT '应用环境',
  `type` tinyint NOT NULL COMMENT '应用类型',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用',
  `meta` json DEFAULT NULL COMMENT '元数据信息',
  `remark` varchar(246) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `ix_merchant_client_id` (`merchant_client_id`)
) ENGINE=InnoDB COMMENT='单点登陆应用';

-- ----------------------------
-- Records of tb_sso_application
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
