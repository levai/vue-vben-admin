-- =============================================
-- 菜单模块 - 表结构
-- =============================================
-- 说明：
-- 1. 使用 utf8mb4 字符集，支持完整的 UTF-8 字符（包括 emoji）
-- 2. 菜单表使用 sort_order 字段（非 MySQL 保留关键字）
-- 3. 支持逻辑删除（deleted 字段）
-- 4. 自动时间戳：create_time 和 update_time
-- =============================================

USE `vben_admin`;

-- =============================================
-- 菜单表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` VARCHAR(64) NOT NULL COMMENT '菜单ID',
    `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    `pid` VARCHAR(64) DEFAULT NULL COMMENT '父级ID',
    `type` VARCHAR(20) NOT NULL COMMENT '菜单类型：catalog-目录，menu-菜单，embedded-内嵌，link-外链，button-按钮',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `redirect` VARCHAR(255) DEFAULT NULL COMMENT '重定向路径',
    `auth_code` VARCHAR(100) DEFAULT NULL COMMENT '后端权限标识',
    `meta` TEXT DEFAULT NULL COMMENT '菜单元数据（JSON格式）',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
    `status` INT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_path` (`path`),
    KEY `idx_pid` (`pid`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';
