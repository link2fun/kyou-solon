// 全局共享数据示例
import { DEFAULT_NAME } from '@/constants';
// @ts-ignore
import { history } from '@umijs/max';
import { useGetState } from 'ahooks';
import { useState } from 'react';

export interface HeaderTab {
  // key === pathname
  key: string;
  active: boolean;
  pathname: string;
  search: string;
  hash?: string;
  query: object;
  content: string;
  closable: boolean;
  title: string;
  queryParams: object;
}

export interface HeaderTabStateType {
  headerTabs: HeaderTab[];
  headerTabsActiveKey: string;
}

const useUser = () => {
  const [name, setName] = useState<string>(DEFAULT_NAME);

  const [, setHeaderTabState, getHeaderTabState] =
    useGetState<HeaderTabStateType>({
      headerTabs: [],
      headerTabsActiveKey: '',
    });

  /** 切换标签, 需要保持查询参数 */
  const switchTab = (payload: any) => {
    // 根路径不进行显示
    if (payload.key === '/') {
      return;
    }
    const { headerTabs, ...rest } = getHeaderTabState();

    const tabIndex = headerTabs.findIndex((tab) => tab.key === payload.key);
    if (tabIndex >= 0) {
      setHeaderTabState({
        ...rest,
        headerTabs,
        headerTabsActiveKey: payload.key,
      });
      return;
    }
  };

  const openTab = (payload: any) => {
    // 根路径不进行显示
    if (payload.key === '/') {
      return;
    }
    const { headerTabs, headerTabsActiveKey, ...rest } = getHeaderTabState();

    const tabIndex = headerTabs.findIndex((tab) => tab.key === payload.key);
    if (tabIndex >= 0) {
      // 页签已经存在了，找到这个页签，更新页签中的参数
      const newHeaderTabs = headerTabs.map((tab) => {
        if (tab.key === payload.key) {
          return { ...tab, ...payload };
        }
        return tab;
      });
      setHeaderTabState({
        ...rest,
        headerTabs: newHeaderTabs,
        headerTabsActiveKey: payload.key,
      });
      return;
    }

    // 全新的页签，直接添加到 headerTabs 中
    const { search, ...restProp } = payload;
    const newTab: HeaderTab = {
      search:
        search && search[0] && (search[0] === '?' ? search : `?${search}`),
      queryParams: {},
      ...restProp,
    };
    // const newHeaderTabs = [...state.headerTabs, newTab];
    const newHeaderTabs = [...getHeaderTabState().headerTabs];
    const originActiveIndex = newHeaderTabs.findIndex(
      (tab) => tab.key === headerTabsActiveKey,
    );
    newHeaderTabs.splice(originActiveIndex + 1, 0, newTab);

    setHeaderTabState({
      ...getHeaderTabState(),
      headerTabs: newHeaderTabs,
      headerTabsActiveKey: payload.key,
    });
  };

  const updateTab = (payload: any) => {
    // 根路径不进行显示
    if (payload.key === '/') {
      return;
    }
    const { headerTabs, headerTabsActiveKey, ...rest } = getHeaderTabState();

    const tabIndex = headerTabs.findIndex((tab) => tab.key === payload.key);
    if (tabIndex >= 0) {
      // 页签已经存在了，找到这个页签，更新页签中的参数
      const newHeaderTabs = headerTabs.map((tab) => {
        if (tab.key === payload.key) {
          return { ...tab, ...payload };
        }
        return tab;
      });
      setHeaderTabState({
        ...rest,
        headerTabs: newHeaderTabs,
        headerTabsActiveKey: payload.key,
      });
      return;
    }

    // 全新的页签，直接添加到 headerTabs 中
    const { search, ...restProp } = payload;
    const newTab: HeaderTab = {
      search:
        search && search[0] && (search[0] === '?' ? search : `?${search}`),
      ...restProp,
    };
    const headerTabState = getHeaderTabState();
    // const newHeaderTabs = [...state.headerTabs, newTab];
    const newHeaderTabs = [...headerTabState.headerTabs];
    const originActiveIndex = newHeaderTabs.findIndex(
      (tab) => tab.key === headerTabsActiveKey,
    );
    newHeaderTabs.splice(originActiveIndex + 1, 0, newTab);

    setHeaderTabState({
      ...headerTabState,
      headerTabs: newHeaderTabs,
      headerTabsActiveKey: payload.key,
    });
  };

  const closeTab = (payload: any) => {
    const headerTabState = getHeaderTabState();
    const { headerTabs, headerTabsActiveKey } = headerTabState;
    const { key } = payload;

    // 如果没有打开任何标签页，则去打开首页
    if (headerTabs.length <= 1) {
      history.push('/');
      setHeaderTabState({
        ...headerTabState,
        headerTabs: [],
        headerTabsActiveKey: '',
      });
      return;
    }

    const tabIndex = headerTabs.findIndex((tab) => tab.key === key);
    const newTabs = headerTabs.filter((tab) => tab.key !== key);

    // 如果关闭的不是当前激活的标签页, 当前激活的标签页不变
    if (headerTabsActiveKey !== key) {
      setHeaderTabState({ ...headerTabState, headerTabs: newTabs });
      return;
    }
    // 关闭的是当前激活的标签页
    const activeTab = newTabs[tabIndex === 0 ? 0 : tabIndex - 1];

    if (activeTab) {
      // 先更新标签页状态
      setHeaderTabState({
        ...headerTabState,
        headerTabs: newTabs,
        headerTabsActiveKey: activeTab && activeTab.key,
      });

      // 延迟切换路由, 确保标签页状态先发生变化, 不然会导致标签页状态未更新
      setTimeout(() => {
        history.push(`${activeTab.pathname}${activeTab.search}`);
      }, 100);
    }
  };

  /** 获取当前页签的信息 */
  const getActiveTab = () => {
    const headerTabState = getHeaderTabState();
    const { headerTabs, headerTabsActiveKey } = headerTabState;
    return headerTabs.find((tab) => tab.key === headerTabsActiveKey);
  };

  /** 关闭当前页签 */
  const closeActiveTab = () => {
    const activeTab = getActiveTab();
    if (activeTab) {
      closeTab(activeTab);
    }
  };

  return {
    name,
    setName,
    getHeaderTabState,
    switchTab,
    openTab,
    updateTab,
    closeTab,
    getActiveTab,
    closeActiveTab,
  };
};

export default useUser;
