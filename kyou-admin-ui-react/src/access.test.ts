import { describe, expect, it } from 'vitest';
import accessFactory from './access';
import type { InitialState } from '@/typing';

/** 构造最小初始状态 */
const stateOf = (permissions: string[], roles: string[]) =>
  ({
    currentUser: { user: {}, roles, permissions },
    loginValid: true,
  }) as InitialState;

describe('hasPermissionOrRole', () => {
  it('权限命中即通过', () => {
    const access = accessFactory(stateOf(['system:user:list'], ['common']));
    expect(access.hasPermissionOrRole(['system:user:list'], undefined)).toBe(
      true,
    );
    // 角色未命中但权限命中
    expect(access.hasPermissionOrRole(['system:user:list'], ['admin'])).toBe(
      true,
    );
  });

  it('角色命中即通过(权限未命中)', () => {
    const access = accessFactory(stateOf(['system:user:list'], ['admin']));
    expect(access.hasPermissionOrRole(['system:role:add'], ['admin'])).toBe(
      true,
    );
  });

  it('权限与角色都未命中即拒绝', () => {
    const access = accessFactory(stateOf(['system:user:list'], ['common']));
    expect(access.hasPermissionOrRole(['system:role:add'], ['admin'])).toBe(
      false,
    );
  });

  it('权限为空且只配角色时, 只看角色', () => {
    const access = accessFactory(stateOf([], ['admin']));
    expect(access.hasPermissionOrRole(undefined, ['admin'])).toBe(true);
    expect(access.hasPermissionOrRole([], ['admin'])).toBe(true);
    expect(access.hasPermissionOrRole([], ['common'])).toBe(false);
  });

  it('权限与角色都没配时不校验', () => {
    const access = accessFactory(stateOf([], []));
    expect(access.hasPermissionOrRole(undefined, undefined)).toBe(true);
    expect(access.hasPermissionOrRole([], [])).toBe(true);
  });

  it('未配 orRole 时与 hasPermission 等价', () => {
    const access = accessFactory(stateOf(['system:user:list'], []));
    expect(access.hasPermissionOrRole(['system:user:list'])).toBe(true);
    expect(access.hasPermissionOrRole(['system:role:add'])).toBe(false);
    expect(access.hasPermissionOrRole([])).toBe(true);
    expect(access.hasPermissionOrRole()).toBe(true);
  });

  it('超管通配符 *:*:* 在权限侧生效', () => {
    const access = accessFactory(stateOf(['*:*:*'], []));
    expect(access.hasPermissionOrRole(['system:user:list'], ['admin'])).toBe(
      true,
    );
  });
});

describe('permissionFilter 的 orRole', () => {
  it('路由配了 orRole 时按权限或角色判定', () => {
    const access = accessFactory(stateOf([], ['admin']));
    expect(
      access.permissionFilter({
        permissionRequired: ['system:user:list'],
        orRole: ['admin'],
      }),
    ).toBe(true);
    // 未配 orRole: 权限不命中即拒绝
    expect(
      access.permissionFilter({ permissionRequired: ['system:user:list'] }),
    ).toBe(false);
  });
});
