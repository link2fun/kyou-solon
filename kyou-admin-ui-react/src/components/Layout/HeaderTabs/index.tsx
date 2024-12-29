import { RouteInfo } from '@/utils/utils';
import { history, matchRoutes, useModel } from '@umijs/max';
import { ConfigProvider, Tabs } from 'antd';
import React, { useContext, useLayoutEffect } from 'react';
import { UNSAFE_RouteContext } from 'react-router';

const HeaderTabs: React.FC = () => {
  // 从全局数据中获取 标签页state 和相关操作方法
  const { getHeaderTabState, closeTab, switchTab, openTab } = useModel('global');

  const { matches } = useContext(UNSAFE_RouteContext);

  /** 对于标签页的操作, 目前只监听 remove 事件, 当触发该事件时, 从全局数据中删除对应的标签页 */
  const onEditTab = (e: React.MouseEvent | React.KeyboardEvent | string, action: 'add' | 'remove') => {
    if (action === 'remove') {
      closeTab({ key: e as string });
    }
  };

  const { pathname, search } = location;

  /** 设置当前激活的标签页 */
  const setActiveKey = (key: string) => {
    const activeTab = getHeaderTabState().headerTabs.find((tab) => tab.key === key);
    if (!activeTab) {
      // 如果从state中没有找到对应的标签页, 则不进行跳转, 防止出现异常
      return;
    }
    history.push(`${activeTab.pathname}${activeTab.search}`);
    const newTab = {
      key: activeTab.key,
      pathname: activeTab.pathname,
      search: activeTab.search,
      content: '',
      closable: true,
      title: pathname,
      queryParams: activeTab.queryParams,
    };
    switchTab(newTab);
  };

  useLayoutEffect(() => {
    const routes = matches.map((m) => m.route);
    const matchedRoute = matchRoutes(routes, pathname);

    // matchedRoute 是一个数组， 取最后一个 里面的 route.name
    let _title =
      matchedRoute && matchedRoute.length > 0 && (matchedRoute[matchedRoute.length - 1].route as RouteInfo)?.name;

    const newTab = {
      key: pathname,
      pathname,
      search,
      content: '',
      closable: true,
      title: _title || pathname,
    };
    openTab(newTab);
  }, [pathname, search]);

  return (
    <div id="layout-opened-tabs" className="layout-opened-tabs">
      <ConfigProvider
        theme={{
          components: {
            Tabs: {},
          },
        }}
      >
        <Tabs
          className="tab-contents"
          type="editable-card"
          hideAdd
          tabPosition="top"
          tabBarStyle={{}}
          activeKey={getHeaderTabState().headerTabsActiveKey}
          onTabClick={setActiveKey}
          onEdit={onEditTab}
          items={getHeaderTabState().headerTabs.map((p) => {
            return { key: p.key, label: p.title, closable: p.closable };
          })}
        />
      </ConfigProvider>
    </div>
  );
};

export default HeaderTabs;
