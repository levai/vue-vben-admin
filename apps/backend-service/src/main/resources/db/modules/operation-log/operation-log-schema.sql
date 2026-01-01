-- =============================================
-- 操作日志模块 - 表结构
-- =============================================
-- 说明：
-- 1. 使用 utf8mb4 字符集，支持完整的 UTF-8 字符（包括 emoji）
-- 2. 操作日志表不支持逻辑删除（历史记录需要完整保留）
-- 3. 自动时间戳：create_time
-- =============================================

USE `vben_admin`;

-- =============================================
-- 操作日志表
-- =============================================
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` VARCHAR(64) NOT NULL COMMENT '日志ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：view/add/edit/delete/export/import/login/logout/download/upload',
    `operation_module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块：system/user/menu/dept/role等',
    `operation_page` VARCHAR(200) DEFAULT NULL COMMENT '操作页面路径',
    `page_name` VARCHAR(100) DEFAULT NULL COMMENT '页面名称（从菜单表查询）',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数（JSON格式）',
    `response_code` INT DEFAULT NULL COMMENT '响应状态码',
    `response_data` TEXT DEFAULT NULL COMMENT '响应数据（JSON格式，可选）',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理（浏览器信息）',
    `browser` VARCHAR(50) DEFAULT NULL COMMENT '浏览器类型',
    `os` VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    `duration` INT DEFAULT NULL COMMENT '操作耗时（毫秒）',
    `status` INT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_module` (`operation_module`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
