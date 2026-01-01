-- =============================================
-- 权限模块 - 表结构
-- =============================================
-- 说明：
-- 1. 使用 utf8mb4 字符集，支持完整的 UTF-8 字符（包括 emoji）
-- 2. 支持逻辑删除（deleted 字段）
-- 3. 自动时间戳：create_time 和 update_time
-- =============================================

USE `vben_admin`;

-- =============================================
-- 权限码表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` VARCHAR(64) NOT NULL COMMENT '权限ID',
    `code` VARCHAR(100) NOT NULL COMMENT '权限码',
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `type` VARCHAR(20) DEFAULT NULL COMMENT '权限类型',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_type` (`type`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限码表';
