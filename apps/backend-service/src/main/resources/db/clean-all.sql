-- =============================================
-- Vben Admin 数据库清理脚本
-- 用于清理所有表和数据，便于重新初始化
-- =============================================

USE `vben_admin`;

-- 禁用外键检查（避免删除顺序问题）
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 删除所有表（按依赖关系倒序删除）
-- =============================================

-- 删除关联表
DROP TABLE IF EXISTS `sys_user_permission`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_user_role`;

-- 删除业务表
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_dept`;
DROP TABLE IF EXISTS `sys_permission`;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 清理完成提示
-- =============================================
SELECT 'Database cleaned successfully. You can now run init-all.sh to reinitialize.' AS message;
