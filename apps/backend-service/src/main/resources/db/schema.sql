-- =============================================
-- Vben Admin 数据库初始化脚本
-- =============================================
-- 说明：
-- 1. 所有表使用 utf8mb4 字符集，支持完整的 UTF-8 字符（包括 emoji）
-- 2. 菜单表使用 sort_order 字段（非 MySQL 保留关键字）
-- 3. 所有表支持逻辑删除（deleted 字段）
-- 4. 自动时间戳：create_time 和 update_time
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `vben_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `vben_admin`;

-- 设置会话字符集为 UTF-8（确保后续操作使用正确的字符集）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `dept_id` VARCHAR(64) DEFAULT NULL COMMENT '部门ID',
    `status` INT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `home_path` VARCHAR(255) DEFAULT NULL COMMENT '首页路径',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

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

-- =============================================
-- 菜单表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` VARCHAR(64) NOT NULL COMMENT '菜单ID',
    `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(255) NOT NULL COMMENT '路由路径',
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

-- =============================================
-- 部门表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_dept` (
    `id` VARCHAR(64) NOT NULL COMMENT '部门ID',
    `name` VARCHAR(50) NOT NULL COMMENT '部门名称',
    `pid` VARCHAR(64) DEFAULT NULL COMMENT '父级ID',
    `status` INT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
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

-- =============================================
-- 用户角色关联表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `role_id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 角色菜单关联表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `role_id` VARCHAR(64) NOT NULL COMMENT '角色ID',
    `menu_id` VARCHAR(64) NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

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

-- =============================================
-- 用户权限关联表（用于存储用户拥有的权限码）
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_user_permission` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `permission_id` VARCHAR(64) NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission` (`user_id`, `permission_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限关联表';
