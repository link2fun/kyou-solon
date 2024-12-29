import WebConst from '@/constants/WebConst';
import CollectionTool from '@/utils/CollectionTool';
import ObjectTool from '@/utils/ObjectTool';

/**
 * 获取当前的登录用户
 */
const getCurrentUser = () => {
  const currentUser: CurrentUser = JSON.parse(
    localStorage.getItem(WebConst.USER_INFO) || '{}',
  );
  return currentUser;
};

/**
 * 判断当前用户是否有某个角色
 * @param roleCodes 角色编码 满足任意一个即认为有权限
 * @param currentUser 当前用户 可不传，不传的时候从 localStorage 中取
 */
const hasRole = (roleCodes: string[], currentUser?: CurrentUser) => {
  // 暂存用户
  let user = currentUser;

  if (ObjectTool.isNull(user)) {
    user = getCurrentUser();
  }
  if (ObjectTool.isNull(user)) {
    return false;
  }
  if (CollectionTool.isEmpty(roleCodes)) {
    return false;
  }
  return CollectionTool.containsAny(user?.roles || [], roleCodes);
};

/**
 * 保存当前登录用户
 * @param currentUser 登录用户
 */
const saveCurrentUser = (currentUser: CurrentUser) => {
  if (!currentUser || !currentUser?.user?.userId) {
    // localStorage.removeItem(webConst.USER_INFO);
    // localStorage.removeItem(webConst.USER_TOKEN);
    // localStorage.removeItem(webConst.PERMISSION_RES_ARR);
    return;
  }

  const { user, permissions } = <CurrentUser>currentUser;

  // 缓存一下登陆用户信息
  localStorage.setItem(WebConst.USER_INFO, JSON.stringify(user));
  // localStorage.setItem(WebConst.USER_TOKEN, userToken || '');
  localStorage.setItem(
    WebConst.PERMISSION_RES_ARR,
    JSON.stringify(permissions || []),
  );
};

const saveUserToken = (userToken: string) => {
  localStorage.setItem(WebConst.USER_TOKEN, userToken || '');
};

const getUserToken = () => {
  return localStorage.getItem(WebConst.USER_TOKEN) || '';
};

const getUserId = () => {
  const currentUser: CurrentUser = JSON.parse(
    localStorage.getItem(WebConst.USER_INFO) || '{}',
  );
  return currentUser?.user?.userId;
};

// const getUserType = () => {
//   const currentUser: CurrentUser = JSON.parse(localStorage.getItem(WebConst.USER_INFO) || '{}');
//   return currentUser.userType;
// };

export default {
  getCurrentUser,
  saveCurrentUser,
  saveUserToken,
  getUserToken,
  getUserId,
  hasRole,
};
