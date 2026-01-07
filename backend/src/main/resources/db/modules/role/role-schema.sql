-- =============================================
-- 角色模块 - 表结构
-- =============================================
-- 说明：
-- 1. 使用 utf8mb4 字符集，支持完整的 UTF-8 字符（包括 emoji）
-- 2. 支持逻辑删除（deleted 字段）
-- 3. 自动时间戳：create_time 和 update_time
-- =============================================

USE `vben_admin`;

-- =============================================
-- 角色表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `status` INT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
