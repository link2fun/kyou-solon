// 运行时配置

// 全局初始化数据配置，用于 Layout 用户信息和权限初始化
// 更多信息见文档：https://umijs.org/docs/api/runtime-config#getinitialstate
import HeaderTabs from '@/components/Layout/HeaderTabs';
import { AvatarDropdown } from '@/components/Layout/RightContent/AvatarDropdown';
import ApiCommon from '@/services/common/ApiCommon';
import { InitialState } from '@/typing';
import StrTool from '@/utils/StrTool';
import UserTool from '@/utils/UserTool';
import { RunTimeLayoutConfig } from '@@/plugin-layout/types';
import type { HeaderViewProps } from '@ant-design/pro-layout/es/components/Header';
import { RequestConfig } from '@umijs/max';
import { notification } from 'antd';
import { AxiosRequestConfig } from 'axios';
import { history } from 'umi';
import { requestConfig } from './requestConfig';
import logo from '@/assets/logo.png';
const loginPath: string = '/user/login';

export async function getInitialState(): Promise<InitialState> {
  const fetchUserInfo = async () => {
    // 先判断是否有 token, 如果没有 token 的话 直接不用获取接口用户信息了
    if (StrTool.isBlank(UserTool.getUserToken())) {
      // 用户的 token 为空，直接跳转到登录页面
      notification.info({ message: '请登录', description: '您还没有登录' });
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

export const request: RequestConfig & AxiosRequestConfig = {
  // 超时时间 600秒 10分钟
  timeout: 600_000,
  ...requestConfig,
};

export const layout: RunTimeLayoutConfig = ({
  initialState,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  setInitialState,
}) => {
  return {
    logo: <img src={logo} /> ,
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
    fixedHeader: true,
    fixSiderbar: true,

    avatarProps: {
      // src: 'https://gw.alipayobjects.com/zos/antfincdn/efFD%24IOql2/weixintupian_20170331104822.jpg',
      // title: <AvatarName />,
      render: (_, avatarChildren) => {
        return <AvatarDropdown menu={true}>{avatarChildren}</AvatarDropdown>;
      },
    },
    // actionsRender: (props: HeaderViewProps) => {
    //   return <div className={'w-75 bg-gray-400'}><HeaderTabs/></div>
    // },
    waterMarkProps: {
      content: `${initialState?.currentUser?.user.userName} ${initialState?.currentUser?.user.nickName}`,
    },

    layout: 'mix',
    childrenRender: (dom, props) => {
      return <div style={{position: 'relative',overflow: 'hidden', marginTop:0, paddingTop: 0,
      paddingBlockStart:0}}>
        <HeaderTabs/>
        <div style={{paddingBlockStart: 56, overflowY: 'auto', maxHeight: 'calc(100vh - 106px)'}}>{dom}</div>
        </div>;
    }
  };
};
