import { useAccess } from '@vben/access';

/**
 * 权限码配置对象（扁平结构，键值对都是字符串）
 */
type PermissionCodesObject = Record<string, string>;

/**
 * 创建权限管理 Hook 的工厂函数
 * @param codes 权限码配置对象（如 { VIEW: 'system:menu:view', ADD: 'system:menu:add' }）
 * @returns 权限管理 Hook，包含权限码和权限检查方法
 *
 * @example
 * ```ts
 * const { permissionCodes, hasPermission } = usePermissions(SYSTEM_PERMISSION_CODES.MENU);
 * // permissionCodes: { VIEW: 'ac:system:menu:view', ADD: 'ac:system:menu:add', ... }
 * // hasPermission: { VIEW: () => boolean, ADD: () => boolean, ... }
 * ```
 */
export function usePermissions<T extends PermissionCodesObject>(codes: T) {
  const { hasAccessByCodes } = useAccess();

  // 创建权限检查方法，保持大写的键名
  const hasPermission = {} as Record<keyof T, () => boolean>;

  for (const key in codes) {
    if (Object.prototype.hasOwnProperty.call(codes, key)) {
      const code = codes[key] as string;
      hasPermission[key] = () => hasAccessByCodes([code]);
    }
  }

  return {
    permissionCodes: codes,
    hasPermission,
  };
}
