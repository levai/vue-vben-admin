-- =============================================
-- 用户模块 - 初始数据
-- =============================================

USE `vben_admin`;

-- 设置会话字符集为 UTF-8（确保中文正确插入）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 初始化超级管理员用户
-- =============================================
-- 用户名：admin
-- 密码：admin123 (BCrypt加密后的值，使用 PasswordEncoderFactories.createDelegatingPasswordEncoder() 生成)
-- 角色：超级管理员（通过 sys_user_role 表关联到 admin 角色，拥有所有权限）
-- 注意：BCrypt 每次加密都会生成不同的值（包含随机盐），但都能验证同一个明文密码
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `nickname`, `phone`, `gender`, `employee_no`, `dept_id`, `status`, `deleted`) VALUES
('1', 'admin', '{bcrypt}$2a$10$JD2zdYaU9CFJ7rG6jisYmepsfAG..pCvV0SWxqNoJuL9NwQTAOOVy', '管理员', '管理员', '13800000001', 0, 'EMP000001', '2005923742071607298', 1, 0)
ON DUPLICATE KEY UPDATE `username` = `username`;
