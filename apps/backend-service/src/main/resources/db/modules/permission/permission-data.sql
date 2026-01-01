-- =============================================
-- 权限模块 - 初始数据
-- =============================================
-- 使用 ac:module:resource:action 格式，保持与项目规范一致
-- =============================================

USE `vben_admin`;

-- 设置会话字符集为 UTF-8（确保中文正确插入）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 初始化权限码
-- =============================================
INSERT INTO `sys_permission` (`id`, `code`, `name`, `type`, `deleted`) VALUES
('1', 'ac:dashboard:view', '仪表盘查看', 'menu', 0),
('18', 'ac:dashboard:analytics:view', '数据分析查看', 'menu', 0),
('19', 'ac:dashboard:workspace:view', '工作台查看', 'menu', 0),
('2', 'ac:system:user:view', '用户管理查看', 'menu', 0),
('3', 'ac:system:user:add', '用户管理新增', 'button', 0),
('4', 'ac:system:user:edit', '用户管理编辑', 'button', 0),
('5', 'ac:system:user:delete', '用户管理删除', 'button', 0),
('6', 'ac:system:role:view', '角色管理查看', 'menu', 0),
('7', 'ac:system:role:add', '角色管理新增', 'button', 0),
('8', 'ac:system:role:edit', '角色管理编辑', 'button', 0),
('9', 'ac:system:role:delete', '角色管理删除', 'button', 0),
('10', 'ac:system:menu:view', '菜单管理查看', 'menu', 0),
('11', 'ac:system:menu:add', '菜单管理新增', 'button', 0),
('12', 'ac:system:menu:edit', '菜单管理编辑', 'button', 0),
('13', 'ac:system:menu:delete', '菜单管理删除', 'button', 0),
('14', 'ac:system:dept:view', '部门管理查看', 'menu', 0),
('15', 'ac:system:dept:add', '部门管理新增', 'button', 0),
('16', 'ac:system:dept:edit', '部门管理编辑', 'button', 0),
('17', 'ac:system:dept:delete', '部门管理删除', 'button', 0),
('20', 'ac:system:operation-log:view', '操作日志查看', 'menu', 0),
('21', 'ac:system:operation-log:delete', '操作日志删除', 'button', 0)
ON DUPLICATE KEY UPDATE `code` = `code`;
