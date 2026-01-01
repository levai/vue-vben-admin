-- =============================================
-- Vben Admin 初始数据脚本
-- =============================================

USE `vben_admin`;

-- 设置会话字符集为 UTF-8（确保中文正确插入）
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 初始化部门（必须在用户之前初始化，因为用户需要关联部门）
-- =============================================
INSERT INTO `sys_dept` (`id`, `name`, `pid`, `status`, `remark`, `deleted`) VALUES
('2005923742071607298', '总公司', NULL, 1, '公司总部', 0)
ON DUPLICATE KEY UPDATE `name` = `name`;

-- =============================================
-- 初始化管理员用户
-- =============================================
-- 密码：admin123 (BCrypt加密后的值，使用 PasswordEncoderFactories.createDelegatingPasswordEncoder() 生成)
-- 注意：BCrypt 每次加密都会生成不同的值（包含随机盐），但都能验证同一个明文密码
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `nickname`, `phone`, `gender`, `employee_no`, `dept_id`, `status`, `deleted`) VALUES
('1', 'admin_user', '{bcrypt}$2a$10$JD2zdYaU9CFJ7rG6jisYmepsfAG..pCvV0SWxqNoJuL9NwQTAOOVy', '管理员', '管理员', '13800000001', 0, 'EMP000001', '2005923742071607298', 1, 0)
ON DUPLICATE KEY UPDATE `username` = `username`;

-- =============================================
-- 初始化角色
-- =============================================
INSERT INTO `sys_role` (`id`, `name`, `status`, `remark`, `deleted`) VALUES
('1', 'admin', 1, '超级管理员', 0),
('2', 'user', 1, '普通用户', 0);

-- =============================================
-- 初始化用户角色关联
-- =============================================
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
('1', '1', '1');

-- =============================================
-- 初始化菜单
-- =============================================
INSERT INTO `sys_menu` (`id`, `name`, `path`, `pid`, `type`, `component`, `redirect`, `auth_code`, `meta`, `sort_order`, `status`, `deleted`) VALUES
('1', 'Dashboard', '/dashboard', NULL, 'catalog', NULL, '/analytics', NULL, '{"icon":"lucide:layout-dashboard","title":"仪表盘"}', 1, 1, 0),
('8', 'Analytics', '/analytics', '1', 'menu', 'dashboard/analytics/index', NULL, 'dashboard:analytics:view', '{"icon":"lucide:area-chart","title":"数据分析","affixTab":true}', 1, 1, 0),
('9', 'Workspace', '/workspace', '1', 'menu', 'dashboard/workspace/index', NULL, 'dashboard:workspace:view', '{"icon":"carbon:workspace","title":"工作台"}', 2, 1, 0),
('2', 'System', '/system', NULL, 'catalog', NULL, NULL, NULL, '{"icon":"lucide:settings","title":"系统管理"}', 2, 1, 0),
('3', 'SystemUser', '/system/user', '2', 'menu', 'system/user/index', NULL, 'system:user:view', '{"icon":"lucide:users","title":"用户管理"}', 1, 1, 0),
('4', 'SystemRole', '/system/role', '2', 'menu', 'system/role/list', NULL, 'system:role:view', '{"icon":"lucide:shield","title":"角色管理"}', 2, 1, 0),
('5', 'SystemMenu', '/system/menu', '2', 'menu', 'system/menu/list', NULL, 'system:menu:view', '{"icon":"lucide:menu","title":"菜单管理"}', 3, 1, 0),
('6', 'SystemDept', '/system/dept', '2', 'menu', 'system/dept/list', NULL, 'system:dept:view', '{"icon":"lucide:building","title":"部门管理"}', 4, 1, 0),
('7', 'Profile', '/profile', NULL, 'menu', '_core/profile/index', NULL, NULL, '{"icon":"lucide:user","title":"个人中心","hideInMenu":true}', 3, 1, 0);

-- =============================================
-- 初始化角色菜单关联（admin角色拥有所有菜单权限）
-- =============================================
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
('1', '1', '1'),
('7', '1', '8'),
('8', '1', '9'),
('2', '1', '2'),
('3', '1', '3'),
('4', '1', '4'),
('5', '1', '5'),
('6', '1', '6');

-- =============================================
-- 初始化权限码
-- 使用 ac:module:resource:action 格式，保持与项目规范一致
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
('17', 'ac:system:dept:delete', '部门管理删除', 'button', 0);

-- =============================================
-- 初始化用户权限关联（admin用户拥有所有权限）
-- =============================================
INSERT INTO `sys_user_permission` (`id`, `user_id`, `permission_id`) VALUES
('1', '1', '1'),
('18', '1', '18'),
('19', '1', '19'),
('2', '1', '2'),
('3', '1', '3'),
('4', '1', '4'),
('5', '1', '5'),
('6', '1', '6'),
('7', '1', '7'),
('8', '1', '8'),
('9', '1', '9'),
('10', '1', '10'),
('11', '1', '11'),
('12', '1', '12'),
('13', '1', '13'),
('14', '1', '14'),
('15', '1', '15'),
('16', '1', '16'),
('17', '1', '17');
