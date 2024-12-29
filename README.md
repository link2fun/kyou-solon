## 1. 项目名称

Kyou (读作 Q ) 是一个基于 Solon、EasyQuery 、React 脚手架

## 2. 简介
- 该项目是一个 Solon、EasyQuery 脚手架（接口和数据结构基本保持和 ruoyi 一致）
- ruoyi 分支 完全兼容 [RuoYi-Vue3](https://github.com/yangzongzhuan/RuoYi-Vue3) 版本前端，任何兼容 ruoyi 前后端分离的前端项目都可以使用当前后端作为服务。
- 同时项目提供 React 版本前端, 见目录 `kyou-admin-ui-react`


## 3. 演示环境
> _不定期还原数据库_

- 地址: https://q.next2.top
- 账号: admin
- 密码: 任意输入(未开启密码验证)

## 4. 分支说明

### master
为主开发版本, 以 ruoyi 为基础进行增强, 带来更好的使用体验, 不保证对 ruiyi 的兼容性

### ruoyi 
分支为 Solon、EasyQuery 版本，接口和数据结构基本保持和 ruoyi 一致, 基本可以保证 前端后端都兼容 ruoyi

## 5. 技术栈

### 5.1. 后端
> Java 17 / MySQL 8

- Solon https://solon.noear.org/
- EasyQuery http://www.easy-query.com/
- EasyQuery IDEA 插件 https://plugins.jetbrains.com/plugin/23121-easyqueryassistant
- Hutool https://hutool.cn/
- PDManer http://pdmaner.com/
- TLog https://tlog.yomahub.com/

### 5.2. 前端
> node 16 / pnpm 8
- React https://zh-hans.react.dev/
- Ant Design https://ant.design/components/overview-cn/
- Ant Design ProComponents https://procomponents.ant.design/
- Umi Max https://umijs.org/docs/max/introduce
- Tailwind CSS https://tailwindcss.com/



## 参考项目
- 若依分离版 https://doc.ruoyi.vip/ruoyi-vue/
- 轻舟单体 https://gitee.com/opensolon/qingzhou-boot

## 配套软件
- 数据库设计工具: [PDManer](http://www.pdmaner.com/#/download)

## 功能说明
除了没有数据源监控, 其他的和若依分离版保持一致, 提供的是 React UI, 也可以使用 ruoyi-vue3-js 作为前端

## 目录结构
```
├──  kyou-admin-ui-vue3-js: 前端Vue3项目(已移除, 请自行下载), 来自 [ruoyi-vue3-js](https://github.com/yangzongzhuan/RuoYi-Vue3)
├──  kyou-admin-ui-react: 前端React项目, 兼容前后端分离的 ruoyi 接口请求

├──  kyou-biz: 业务目录, 提供一些业务相关的功能, 自己写的业务功能可以放到这里面
├──  kyou-gen-plugin: 代码生成器插件, 提供一些代码生成器插件, 可以生成一些通用的代码(基于 PDManer json 文件解析)
├──  kyou-main: 主目录, 提供应用启动类及配置 [8078]
├──  kyou-schedule: 定时任务模块, 提供定时任务相关的功能, 目前提供的是基于 solon simple-scheduler 的实现
├──  kyou-support: 支持包, 提供一些常用的工具类
├──  kyou-system: 系统包, 提供一些系统相关的功能, 如 用户、角色、菜单、部门、岗位、字典、参数、日志、文件、缓存等
├──  kyou-tlog-solon-plugin: 提供基于 solon 的 tlog 日志插件, 提供日志链路追踪等
```

## 建议用法
- 使用 PDManer 设计数据库, 管理SQL版本, 使用说明见: [PDManer元数建模-v4-操作手册](https://www.yuque.com/pdmaner/docs/pdmaner-manual)
- 使用自带的代码生成器或者 PDManer 的代码生成器插件, 生成代码
- 安装 [EasyQuery IDEA 插件](https://plugins.jetbrains.com/plugin/23121-easyqueryassistant) , 该插件可以图形化创建和修改查询实体


## QQ 群
- Solon 交流群:  22200020
- EasyQuery 交流群:  170029046
- kyou-solon 群号: 996025948


## FAQ
### 1. Q: 直接下载项目, 无法运行?

   A: 项目使用了 buildnumber 插件, 会读取项目的 git 信息, 所以建议克隆项目而不是直接下载压缩包

