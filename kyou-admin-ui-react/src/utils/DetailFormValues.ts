/**
 * 把详情接口返回的信封组装为编辑表单值
 *
 * 详情接口返回信封 {code, msg, data: {用户对象}, roles, posts, postIds, roleIds},
 * request.get 在信封存在额外顶层键时原样返回整个信封;
 * 表单需要的是展开 data 的基础字段, 并把信封顶层的 postIds/roleIds 一并挂到表单值上,
 * 避免把信封整体塞进表单、或让基础字段回落到列表行旧值
 *
 * @param resp 详情接口返回的信封
 * @returns 组装后的表单值: {...resp.data, postIds: resp.postIds, roleIds: resp.roleIds},
 *          信封缺失 postIds/roleIds 时不注入对应键
 */
const buildDetailFormValues = (resp: any) => {
  const values = { ...(resp?.data || {}) };
  if (resp?.postIds !== undefined) {
    values.postIds = resp.postIds;
  }
  if (resp?.roleIds !== undefined) {
    values.roleIds = resp.roleIds;
  }
  return values;
};

export default buildDetailFormValues;
