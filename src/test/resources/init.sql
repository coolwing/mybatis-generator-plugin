/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50728
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 50728
 File Encoding         : 65001

 Date: 27/11/2021 13:23:36
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        varchar(256) COLLATE utf8mb4_bin NOT NULL COMMENT '姓名',
    `age`         int(11) NOT NULL COMMENT '年龄',
    `address`     varchar(1024) COLLATE utf8mb4_bin         DEFAULT NULL COMMENT '地址',
    `status`      int(11) NOT NULL DEFAULT '0' COMMENT '状态',
    `feature`     blob COMMENT '属性',
    `create_time` datetime                         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='用户表';

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user`
VALUES (1, 'A', 11, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:18:36');
INSERT INTO `user`
VALUES (2, 'B', 12, '江苏省徐州市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:19:50');
INSERT INTO `user`
VALUES (3, 'C', 13, '江苏省盐城市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:19:55');
INSERT INTO `user`
VALUES (4, 'D', 14, '江苏省盐城市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:19:59');
INSERT INTO `user`
VALUES (5, 'E', 15, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');
INSERT INTO `user`
VALUES (6, 'F', 16, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');
INSERT INTO `user`
VALUES (7, 'G', 17, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');
INSERT INTO `user`
VALUES (8, 'H', 18, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');
INSERT INTO `user`
VALUES (9, 'I', 19, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');
INSERT INTO `user`
VALUES (10, 'J', 20, '江苏省南京市', 1, NULL, '2021-11-27 13:18:32', '2021-11-27 13:20:10');
COMMIT;

SET
FOREIGN_KEY_CHECKS = 1;
