/**
 * 权限码常量定义
 * 按模块组织，便于统一维护和管理
 */

/**
 * 系统管理模块权限码
 * 使用 ac:module:resource:action 格式，保持与项目规范一致
 */
export const SYSTEM_PERMISSION_CODES = {
  // 用户管理
  USER: {
    VIEW: 'ac:system:user:view',
    ADD: 'ac:system:user:add',
    EDIT: 'ac:system:user:edit',
    DELETE: 'ac:system:user:delete',
  },
  // 角色管理
  ROLE: {
    VIEW: 'ac:system:role:view',
    ADD: 'ac:system:role:add',
    EDIT: 'ac:system:role:edit',
    DELETE: 'ac:system:role:delete',
  },
  // 菜单管理
  MENU: {
    VIEW: 'ac:system:menu:view',
    ADD: 'ac:system:menu:add',
    EDIT: 'ac:system:menu:edit',
    DELETE: 'ac:system:menu:delete',
  },
  // 部门管理
  DEPT: {
    VIEW: 'ac:system:dept:view',
    ADD: 'ac:system:dept:add',
    EDIT: 'ac:system:dept:edit',
    DELETE: 'ac:system:dept:delete',
  },
} as const;

/**
 * 仪表盘模块权限码
 */
export const DASHBOARD_PERMISSION_CODES = {
  VIEW: 'ac:dashboard:view',
  ANALYTICS_VIEW: 'ac:dashboard:analytics:view',
  WORKSPACE_VIEW: 'ac:dashboard:workspace:view',
} as const;

/**
 * 所有权限码的扁平化导出（用于类型推断）
 */
export const PERMISSION_CODES = {
  ...SYSTEM_PERMISSION_CODES,
  DASHBOARD: DASHBOARD_PERMISSION_CODES,
} as const;

/**
 * 提取所有权限码的联合类型
 */
type ExtractPermissionCodes<T> =
  T extends Record<string, infer U>
    ? U extends Record<string, string>
      ? U[keyof U]
      : U extends string
        ? U
        : never
    : never;

/**
 * 权限码类型定义
 */
export type PermissionCode =
  | ExtractPermissionCodes<typeof DASHBOARD_PERMISSION_CODES>
  | ExtractPermissionCodes<typeof SYSTEM_PERMISSION_CODES>;
