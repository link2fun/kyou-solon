// 从路由中读取 标签页的名称
// import { pathToRegexp } from 'path-to-regexp';
import { matchPath, RouteObject } from 'react-router';

export type RouteInfo = RouteObject & { name?: string; children?: RouteInfo[] };

/**
 * @deprecated 应该是用不到了 使用 umi 的 matchRoutes 方法
 * 从路由定义中提取标签页的title
 * @param routers 系统中的路由定义
 * @param pathname 当前所处的路由
 * @return 返回当前路由的标签页名称
 */
export const getTabNameFromRouter = <T extends RouteInfo>(
  routers: T[] = [],
  pathname: string = '/',
): string | undefined => {
  if (pathname === '/') {
    return '首页';
  }
  let tabName: string | undefined = undefined;

  for (let i = 0; i < routers.length; i++) {
    const route = routers[i];
    // if (pathToRegexp(`${route.path}/(.*)`).test(`${pathname}/`)) {

    if (route.name && matchPath(route.path as string, pathname)) {
      // 判断当前路径是否完全匹配
      if (route.path === pathname) {
        tabName = route.name;
        return tabName;
      }
    }
    // 存在子路由，判断子路由
    if (route.children) {
      tabName = getTabNameFromRouter(route.children, pathname);
      if (tabName) {
        return tabName;
      }
    }
  }

  return tabName;
};

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 */
export function handleTree(
  data: any,
  id: string = 'id',
  parentId: string = 'parentId',
  children: string = 'children',
) {
  let config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children',
  };

  let childrenListMap: any = {};
  let nodeIds: any = {};
  let tree: any = [];

  for (let d of data) {
    let parentId = d[config.parentId];
    if (
      childrenListMap[parentId] === null ||
      childrenListMap[parentId] === undefined
    ) {
      childrenListMap[parentId] = [];
    }
    nodeIds[d[config.id]] = d;
    childrenListMap[parentId].push(d);
  }

  for (let d of data) {
    let parentId = d[config.parentId];
    if (nodeIds[parentId] === null || nodeIds[parentId] === undefined) {
      tree.push(d);
    }
  }

  function adaptToChildrenList(o: any) {
    if (childrenListMap[o[config.id]] !== null) {
      o[config.childrenList] = childrenListMap[o[config.id]];
    }
    if (o[config.childrenList]) {
      for (let c of o[config.childrenList]) {
        adaptToChildrenList(c);
      }
    }
  }

  for (let t of tree) {
    adaptToChildrenList(t);
  }

  return tree;
}

/** 转换日期数组为请求参数 */
export function dateRangeToRequestParams(dataRange: any[]) {
  return {
    'params[beginTime]': dataRange ? dataRange[0] : '',
    'params[endTime]': dataRange ? dataRange[1] : '',
  };
}
