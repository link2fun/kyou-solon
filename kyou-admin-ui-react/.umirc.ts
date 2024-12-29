import { defineConfig } from '@umijs/max';
import routes from './config/routes';
// noinspection JSUnusedGlobalSymbols
export default defineConfig({
  antd: {
    appConfig: {},
  },
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {
    title: 'Kyou(Q) Solon',
  },
  // mako: {}, // 开启mako build 会有问题, 当前只能在开发环境开启
  // mpa: false,
  routes: routes,
  esbuildMinifyIIFE:true,

  npmClient: 'pnpm',
  tailwindcss: {},
  proxy: {
    '/api': {
      target: 'http://127.0.0.1:8078',
      changeOrigin: true,
      pathRewrite: { '^/api': '' },
    },
    '/profile/avatar': {
      target: 'http://127.0.0.1:8078',
      changeOrigin: true,
    },
    // '/api': {
    //   target: 'https://vue.ruoyi.vip',
    //   changeOrigin: true,
    //   pathRewrite: { '^/api': '/prod-api' },
    // },
  },
});
