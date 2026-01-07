-- =============================================
-- 部门模块 - 表结构
-- =============================================
-- 说明：
-- 1. 使用 utf8mb4 字符集，支持完整的 UTF-8 字符（包括 emoji）
-- 2. 支持逻辑删除（deleted 字段）
-- 3. 自动时间戳：create_time 和 update_time
-- 4. 创建人和更新人追踪（create_by, update_by）
-- =============================================

USE `vben_admin`;

-- =============================================
-- 部门表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_dept` (
    `id` VARCHAR(64) NOT NULL COMMENT '部门ID',
    `name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `pid` VARCHAR(64) DEFAULT NULL COMMENT '父级ID',
    `status` INT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    -- 唯一约束：只对未删除的记录生效（使用函数索引，MySQL 8.0+）
    -- 当 deleted=0 时，第二个字段为 0，检查唯一性；当 deleted=1 时，第二个字段为 id（唯一），不检查唯一性
    UNIQUE KEY `uk_name` (`name`, (IF(`deleted` = 0, 0, `id`))),
    KEY `idx_pid` (`pid`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';
