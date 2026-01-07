import { isNil, pickBy } from '@vben-core/shared/utils';

/**
 * 从对象中提取有值的字段（过滤掉 undefined、null 和空字符串）
 *
 * 使用 es-toolkit/compat 的 pickBy 和 isNil 函数实现
 *
 * @param obj - 源对象
 * @param options - 配置选项
 * @param options.includeEmptyString - 是否包含空字符串，默认为 false（即过滤掉空字符串）
 * @returns 只包含有值字段的新对象
 *
 * @example
 * ```ts
 * const data = {
 *   name: 'John',
 *   age: undefined,
 *   email: null,
 *   phone: '',
 *   address: '123 Main St'
 * };
 *
 * pickDefined(data)
 * // { name: 'John', address: '123 Main St' }
 *
 * pickDefined(data, { includeEmptyString: true })
 * // { name: 'John', phone: '', address: '123 Main St' }
 * ```
 */
export function pickDefined<T extends Record<string, any>>(
  obj: T,
  options: { includeEmptyString?: boolean } = {},
): Partial<T> {
  const { includeEmptyString = false } = options;

  return pickBy(obj, (value) => {
    const isDefined = !isNil(value);
    const isEmptyString = value === '';
    return isDefined && (includeEmptyString || !isEmptyString);
  }) as Partial<T>;
}
