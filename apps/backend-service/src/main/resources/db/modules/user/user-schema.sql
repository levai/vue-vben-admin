-- =============================================
-- 用户模块 - 表结构
-- =============================================
-- 说明：
-- 1. 使用 utf8mb4 字符集，支持完整的 UTF-8 字符（包括 emoji）
-- 2. 支持逻辑删除（deleted 字段）
-- 3. 自动时间戳：create_time 和 update_time
-- 4. 创建人和更新人追踪（create_by, update_by）
-- =============================================

USE `vben_admin`;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `gender` INT(1) NOT NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `employee_no` VARCHAR(50) NOT NULL COMMENT '工号',
    `dept_id` VARCHAR(64) NOT NULL COMMENT '部门ID',
    `status` INT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_employee_no` (`employee_no`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
