// 运行时配置

import type { RunTimeLayoutConfig } from '@@/plugin-layout/types';
import { ErrorBoundary } from '@ant-design/pro-components';
import { history, type RequestConfig } from '@umijs/max';
import React from 'react';
// 全局初始化数据配置，用于 Layout 用户信息和权限初始化
// 更多信息见文档：https://umijs.org/docs/api/runtime-config#getinitialstate
import logo from '@/assets/logo.png';
import { OfflineBanner } from '@/components';
import HeaderTabs from '@/components/Layout/HeaderTabs';
import { AvatarDropdown } from '@/components/Layout/RightContent/AvatarDropdown';
import { AntdStaticHolder, getNotification } from '@/hooks/useAntdStatic';
import ApiCommon from '@/services/common/ApiCommon';
import type { InitialState } from '@/typing';
import StrTool from '@/utils/StrTool';
import UserTool from '@/utils/UserTool';
// Initialize dayjs plugins globally
import defaultSettings from '../config/defaultSettings';
import { requestConfig } from './requestConfig';

const loginPath: string = '/user/login';

export async function getInitialState(): Promise<InitialState> {
  const fetchUserInfo = async () => {
    // 先判断是否有 token, 如果没有 token 的话 直接不用获取接口用户信息了
    if (StrTool.isBlank(UserTool.getUserToken())) {
      // 用户的 token 为空，直接跳转到登录页面
      getNotification().info({ title: '请登录', description: '您还没有登录' });
      history.push(loginPath);
      return undefined;
    }

    // console.log('app getInitialState fetchUserInfo');
    try {
      const userInfo = await ApiCommon.getInfo();
      // console.log('app getInitialState fetchUserInfo userInfo', userInfo);

      return userInfo;
    } catch (error) {
      console.log('app getInitialState fetchUserInfo error', error);
      history.push(loginPath);
    }

    return undefined;
  };

  try {
    const userInfo = await fetchUserInfo();
    if (userInfo === undefined) {
      return {
        fetchUserInfo,
        loginValid: false,
        currentUser: { user: {}, roles: [], permissions: [] },
      };
    }
    const { permissions, roles, user } = userInfo;
    // console.log('app getInitialState currentUser', userInfo);
    return {
      fetchUserInfo,
      loginValid: true,
      currentUser: {
        user,
        permissions,
        roles,
      },
    };
  } catch (e) {
    console.log('app getInitialState error', e);
    return {
      fetchUserInfo,
      loginValid: false,
      currentUser: { user: {}, roles: [], permissions: [] },
    };
  }
}

// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  return {
    ...defaultSettings,
    logo: <img src={logo} alt={defaultSettings.title || 'Kyou Logo'} />,
    menu: {
      locale: false,
    },
    // headerContentRender: (props: HeaderViewProps) => {
    //   if (props.isMobile) {
    //     // TODO 移动端应该显示什么
    //     return <HeaderTabs />;
    //   }
    //   return <HeaderTabs />;
    // },

    avatarProps: {
      // src: 'https://gw.alipayobjects.com/zos/antfincdn/efFD%24IOql2/weixintupian_20170331104822.jpg',
      // title: <AvatarName />,
      render: (_, avatarChildren) => {
        return <AvatarDropdown menu={true}>{avatarChildren}</AvatarDropdown>;
      },
    },
    waterMarkProps: {
      content: `${initialState?.currentUser?.user.userName} ${initialState?.currentUser?.user.nickName}`,
    },
    // actionsRender: (props: HeaderViewProps) => {
    //   return <div className={'w-75 bg-gray-400'}><HeaderTabs/></div>
    // },

    // Replace ProLayout's default ErrorBoundary with our offline-aware version,
    // so chunk load errors show friendly messages instead of "Something went wrong."
    ErrorBoundary,
    childrenRender: (dom) => {
      return (
        <div
          style={{
            position: 'relative',
            overflow: 'hidden',
            marginTop: 0,
            paddingTop: 0,
            paddingBlockStart: 0,
          }}
        >
          <AntdStaticHolder />
          <HeaderTabs />
          <div
            style={{
              overflowY: 'auto',
              maxHeight: 'calc(100vh - 112px)',
            }}
          >
            {dom}
          </div>
        </div>
      );
    },
  };
};
export const request: RequestConfig = {
  // 超时时间 600秒 10分钟
  timeout: 600_000,
  ...requestConfig,
};

export function rootContainer(container: React.ReactNode) {
  return (
    <>
      <OfflineBanner />
      <ErrorBoundary>{container}</ErrorBoundary>
    </>
  );
}
