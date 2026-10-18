import { describe, expect, it } from 'vitest';
import buildDetailFormValues from '@/utils/DetailFormValues';

describe('buildDetailFormValues', () => {
  it('正常组装: 展开 data 基础字段, 挂载信封顶层 postIds/roleIds', () => {
    const resp = {
      code: 200,
      msg: '操作成功',
      data: { userId: 1, userName: 'admin', nickName: '若依' },
      roles: [{ roleId: 2, roleName: '普通角色' }],
      posts: [{ postId: 1, postName: 'CEO' }],
      postIds: [1],
      roleIds: [2],
    };

    expect(buildDetailFormValues(resp)).toEqual({
      userId: 1,
      userName: 'admin',
      nickName: '若依',
      postIds: [1],
      roleIds: [2],
    });
  });

  it('缺 data 容错: 不抛错, 仅返回顶层 postIds/roleIds', () => {
    const resp = { code: 200, msg: '操作成功', postIds: [1], roleIds: [2] };

    expect(buildDetailFormValues(resp)).toEqual({
      postIds: [1],
      roleIds: [2],
    });
  });

  it('缺 roleIds 与 postIds 容错: data 正常展开, 不注入缺失键', () => {
    const resp = {
      code: 200,
      msg: '操作成功',
      data: { userId: 1, nickName: '若依' },
    };

    const values = buildDetailFormValues(resp);
    expect(values).toEqual({ userId: 1, nickName: '若依' });
    expect(values).not.toHaveProperty('postIds');
    expect(values).not.toHaveProperty('roleIds');
  });

  it('信封为空容错: resp 缺失时不抛错, 返回空对象', () => {
    expect(buildDetailFormValues(undefined)).toEqual({});
    expect(buildDetailFormValues(null)).toEqual({});
  });

  it('顶层 postIds/roleIds 覆盖 data 内同名字段', () => {
    const resp = {
      code: 200,
      data: { userId: 1, postIds: [9], roleIds: [9] },
      postIds: [1],
      roleIds: [2],
    };

    expect(buildDetailFormValues(resp)).toEqual({
      userId: 1,
      postIds: [1],
      roleIds: [2],
    });
  });
});
