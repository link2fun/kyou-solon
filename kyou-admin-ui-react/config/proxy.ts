/**
 * @name 代理的配置
 * @see 在生产环境 代理是无法生效的，所以这里没有生产环境的配置
 * -------------------------------
 * The agent cannot take effect in the production environment
 * so there is no configuration of the production environment
 * For details, please see
 * https://pro.ant.design/docs/deploy
 *
 * @doc https://umijs.org/docs/guides/proxy
 */
interface ProxyRule {
  target: string;
  changeOrigin?: boolean;
  pathRewrite?: Record<string, string>;
}

interface ProxyConfig {
  [key: string]: ProxyRule;
}

// 后端服务地址
const DEV_TARGET = 'http://127.0.0.1:8078';
const TEST_TARGET = 'http://127.0.0.1:8078';
const PRE_TARGET = 'http://127.0.0.1:8078';

const buildProxy = (target: string): ProxyConfig => ({
  '/api': {
    target,
    changeOrigin: true,
    pathRewrite: { '^/api': '' },
  },
  '/profile/avatar': {
    target,
    changeOrigin: true,
  },
});

export default {
  dev: buildProxy(DEV_TARGET),
  test: buildProxy(TEST_TARGET),
  pre: buildProxy(PRE_TARGET),
} as Record<string, ProxyConfig>;
