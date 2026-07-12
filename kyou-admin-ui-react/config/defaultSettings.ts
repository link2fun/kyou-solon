import type { ProLayoutProps } from '@ant-design/pro-components';

/**
 * ProLayout 默认设置
 * @doc https://procomponents.ant.design/components/layout
 */
const Settings: ProLayoutProps & {
  logo?: string;
} = {
  navTheme: 'light',
  colorPrimary: '#1677ff',
  layout: 'mix',
  contentWidth: 'Fluid',
  fixedHeader: true,
  fixSiderbar: true,
  colorWeak: false,
  title: 'Kyou(Q) Solon',
  iconfontUrl: '',
  token: {
    // 参见 ts 声明，通过 token 修改样式
    // https://procomponents.ant.design/components/layout#%E9%80%9A%E8%BF%87-token-%E4%BF%AE%E6%94%B9%E6%A0%B7%E5%BC%8F
  },
};

export default Settings;
