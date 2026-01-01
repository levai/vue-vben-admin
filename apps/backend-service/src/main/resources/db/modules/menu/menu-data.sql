-- =============================================
-- 菜单模块 - 初始数据
-- =============================================
-- 使用 ac:module:resource:action 格式的权限码
-- =============================================

USE `vben_admin`;

-- 设置会话字符集为 UTF-8（确保中文正确插入）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 初始化菜单
-- =============================================
INSERT INTO `sys_menu` (`id`, `name`, `path`, `pid`, `type`, `component`, `redirect`, `auth_code`, `meta`, `sort_order`, `status`, `deleted`) VALUES
('1', 'Dashboard', '/dashboard', NULL, 'catalog', NULL, '/analytics', NULL, '{"icon":"lucide:layout-dashboard","title":"仪表盘"}', 1, 1, 0),
('8', 'Analytics', '/analytics', '1', 'menu', 'dashboard/analytics/index', NULL, 'ac:dashboard:analytics:view', '{"icon":"lucide:area-chart","title":"数据分析","affixTab":true}', 1, 1, 0),
('9', 'Workspace', '/workspace', '1', 'menu', 'dashboard/workspace/index', NULL, 'ac:dashboard:workspace:view', '{"icon":"carbon:workspace","title":"工作台"}', 2, 1, 0),
('2', 'System', '/system', NULL, 'catalog', NULL, NULL, NULL, '{"icon":"lucide:settings","title":"系统管理"}', 2, 1, 0),
('3', 'SystemUser', '/system/user', '2', 'menu', 'system/user/list', NULL, 'ac:system:user:view', '{"icon":"lucide:users","title":"用户管理"}', 1, 1, 0),
('4', 'SystemRole', '/system/role', '2', 'menu', 'system/role/list', NULL, 'ac:system:role:view', '{"icon":"lucide:shield","title":"角色管理"}', 2, 1, 0),
('5', 'SystemMenu', '/system/menu', '2', 'menu', 'system/menu/list', NULL, 'ac:system:menu:view', '{"icon":"lucide:menu","title":"菜单管理"}', 3, 1, 0),
('6', 'SystemDept', '/system/dept', '2', 'menu', 'system/dept/list', NULL, 'ac:system:dept:view', '{"icon":"lucide:building","title":"部门管理"}', 4, 1, 0),
('32', 'SystemOperationLog', '/system/operation-log', '2', 'menu', 'system/operation-log/list', NULL, 'ac:system:operation-log:view', '{"icon":"mdi:file-document-outline","title":"操作日志"}', 5, 1, 0),
-- 用户管理按钮权限（按钮类型 path 为 NULL）
('20', 'UserAdd', NULL, '3', 'button', NULL, NULL, 'ac:system:user:add', '{"title":"新增"}', 1, 1, 0),
('21', 'UserEdit', NULL, '3', 'button', NULL, NULL, 'ac:system:user:edit', '{"title":"编辑"}', 2, 1, 0),
('22', 'UserDelete', NULL, '3', 'button', NULL, NULL, 'ac:system:user:delete', '{"title":"删除"}', 3, 1, 0),
-- 角色管理按钮权限
('23', 'RoleAdd', NULL, '4', 'button', NULL, NULL, 'ac:system:role:add', '{"title":"新增"}', 1, 1, 0),
('24', 'RoleEdit', NULL, '4', 'button', NULL, NULL, 'ac:system:role:edit', '{"title":"编辑"}', 2, 1, 0),
('25', 'RoleDelete', NULL, '4', 'button', NULL, NULL, 'ac:system:role:delete', '{"title":"删除"}', 3, 1, 0),
-- 菜单管理按钮权限
('26', 'MenuAdd', NULL, '5', 'button', NULL, NULL, 'ac:system:menu:add', '{"title":"新增"}', 1, 1, 0),
('27', 'MenuEdit', NULL, '5', 'button', NULL, NULL, 'ac:system:menu:edit', '{"title":"编辑"}', 2, 1, 0),
('28', 'MenuDelete', NULL, '5', 'button', NULL, NULL, 'ac:system:menu:delete', '{"title":"删除"}', 3, 1, 0),
-- 部门管理按钮权限
('29', 'DeptAdd', NULL, '6', 'button', NULL, NULL, 'ac:system:dept:add', '{"title":"新增"}', 1, 1, 0),
('30', 'DeptEdit', NULL, '6', 'button', NULL, NULL, 'ac:system:dept:edit', '{"title":"编辑"}', 2, 1, 0),
('31', 'DeptDelete', NULL, '6', 'button', NULL, NULL, 'ac:system:dept:delete', '{"title":"删除"}', 3, 1, 0),
-- 操作日志按钮权限
('33', 'OperationLogView', NULL, '32', 'button', NULL, NULL, 'ac:system:operation-log:view', '{"title":"查看"}', 1, 1, 0),
('34', 'OperationLogDelete', NULL, '32', 'button', NULL, NULL, 'ac:system:operation-log:delete', '{"title":"删除"}', 2, 1, 0)
ON DUPLICATE KEY UPDATE `name` = `name`;
