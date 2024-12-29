import Guide from '@/components/Layout/Guide';
import { trim } from '@/utils/format';
import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import React from 'react';

const HomePage: React.FC = () => {
  const { name } = useModel('global');
  return (
    <PageContainer>
      <div className='text-3xl font-bold'>Solon EasyQuery React 脚手架</div>
      <div>
        <h2 className='text-2xl font-bold'>1. 项目名称</h2>
        <p>Kyou (读作 Q ) 是一个基于 Solon、EasyQuery 、React 脚手架</p>
        <h2 className='text-2xl font-bold'>2. 简介</h2>
        <p>该项目是一个 Solon、EasyQuery 脚手架（接口和数据结构基本保持和 ruoyi 一致）</p>
        <p>ruoyi 分支 完全兼容 RuoYi-Vue3 版本前端，任何兼容 ruoyi 前后端分离的前端项目都可以使用当前后端作为服务。</p>
        <p>同时项目提供 React 版本前端, 见目录 kyou-admin-ui-react</p>
        <h2 className='text-2xl font-bold'>3. 演示环境</h2>
        <p>不定期还原数据库</p>
        <p>地址: https://q.next2.top</p>
        <p>账号: admin</p>
        <p>密码: 任意输入(未开启密码验证)</p>
        <h2 className='text-2xl font-bold'>4. 分支说明</h2>
        <p>master</p>
        <p>为主开发版本, 以 ruoyi 为基础进行增强, 带来更好的使用体验, 不保证对 ruiyi 的兼容性</p>
        <p>ruoyi</p>
        <p>分支为 Solon、EasyQuery 版本，接口和数据结构基本保持和 ruoyi 一致, 基本可以保证 前端后端都兼容 ruoyi</p>
        <h2 className='text-2xl font-bold'>5. 技术栈</h2>
        <h3 className='text-xl font-bold'>5.1. 后端</h3>
        <h4 className='text-lg font-bold'>5.1.1. Solon</h4>
        <p>面向全场景的 Java 应用开发框架：克制、高效、开放、生态. 详见Solon 官网</p>
        <p>选择 Solon 的原因:</p>
        <ul>
          <li>轻量, 入门快, 性能高, 功能强大, 生态丰富</li>
        </ul>
        <h4 className='text-lg font-bold'>5.1.2. EasyQuery (ORM)</h4>
        <p>Java下唯一一款同时支持强类型对象关系查询和强类型SQL语法查询的ORM,拥有对象模型筛选、隐式子查询、隐式join、显式子查询、显式join,支持Java/Kotlin. 详见EasyQuery 官网</p>
        <p>选择 EasyQuery 的原因:</p>
        <ul>
          <li>支持多表关联查询, 业务开发下有奇效</li>
          <li>支持强类型对象关系查询</li>
          <li>EasyQuery 的代码生成器插件, 可以图形化创建和修改查询实体 强烈安利, 复杂业务实体可图形化勾选创建修改</li>
        </ul>
        <h3 className='text-xl font-bold'>5.2. 前端</h3>
        <h4 className='text-lg font-bold'>5.2.1. React</h4>
        <p>选择 React 的原因:</p>
        <ul>
          <li>React 单向数据流简单</li>
          <li>And-Design 组件库成熟, 生态丰富, 同时提供高级组件 pro-components</li>
        </ul>
        <h2 className='text-2xl font-bold'>配套软件</h2>
        <p>数据库设计工具: PDManer</p>
        <h2 className='text-2xl font-bold'>目录结构</h2>
        <ul>
          <li>kyou-admin-ui-vue3-js: 前端Vue3项目(已移除, 请自行下载), 来自 ruoyi-vue3-js</li>
          <li>kyou-admin-ui-react: 前端React项目, 兼容前后端分离的 ruoyi 接口请求</li>
          <li>kyou-biz: 业务目录, 提供一些业务相关的功能, 自己写的业务功能可以放到这里面</li>
          <li>kyou-gen-plugin: 代码生成器插件, 提供一些代码生成器插件, 可以生成一些通用的代码(基于 PDManer json 文件解析)</li>
          <li>kyou-main: 主目录, 提供应用启动类及配置</li>
          <li>kyou-schedule: 定时任务模块, 提供定时任务相关的功能, 目前提供的是基于 solon simple-scheduler 的实现</li>
          <li>kyou-support: 支持包, 提供一些常用的工具类</li>
          <li>kyou-system: 系统包, 提供一些系统相关的功能, 如 用户、角色、菜单、部门、岗位、字典、参数、日志、文件、缓存等</li>
          <li>kyou-tlog-solon-plugin: 提供基于 solon 的 tlog 日志插件, 提供日志链路追踪等</li>
        </ul>
        <h2 className='text-2xl font-bold'>建议用法</h2>
        <p>使用 PDManer 设计数据库, 管理SQL版本, 使用说明见: PDManer元数建模-v4-操作手册</p>
        <p>使用自带的代码生成器或者 PDManer 的代码生成器插件, 生成代码</p>
        <p>安装 EasyQuery IDEA 插件 , 该插件可以图形化创建和修改查询实体</p>
        <h2 className='text-2xl font-bold'>QQ 群</h2>
        <p>群号: 996025948</p>
      </div>
    </PageContainer>
  );
};

export default HomePage;
