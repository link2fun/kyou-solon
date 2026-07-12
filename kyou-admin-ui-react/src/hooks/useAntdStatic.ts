import {
  App,
  message as staticMessage,
  notification as staticNotification,
} from 'antd';
import type { MessageInstance } from 'antd/es/message/interface';
import type { NotificationInstance } from 'antd/es/notification/interface';
import React, { useEffect, useRef } from 'react';

// React 之外可访问的静态实例 holder（供 requestConfig / getInitialState 等非 React 代码使用）
let messageInstance: MessageInstance | undefined;
let notificationInstance: NotificationInstance | undefined;

/**
 * 在根组件调用一次，把 App.useApp() 的实例注册到模块变量。
 * 这样运行在 React 渲染之外的代码（如请求拦截器、getInitialState）也能拿到带主题/locale 上下文的实例。
 */
export const useAntdStatic = () => {
  const staticFunctions = App.useApp();
  const memoRef = useRef(staticFunctions);
  memoRef.current = staticFunctions;

  useEffect(() => {
    messageInstance = memoRef.current.message;
    notificationInstance = memoRef.current.notification;
  }, []);
};

/** React 之外获取 message 实例（未初始化时回退到 antd 静态方法） */
export const getMessage = (): MessageInstance =>
  messageInstance ?? staticMessage;

/** React 之外获取 notification 实例（未初始化时回退到 antd 静态方法） */
export const getNotification = (): NotificationInstance =>
  notificationInstance ?? staticNotification;

/**
 * 空组件：在 layout.childrenRender 中渲染一次，用于初始化静态实例。
 * 登录页（layout:false）不会渲染，故登录前会使用静态回退。
 */
export const AntdStaticHolder: React.FC = () => {
  useAntdStatic();
  return null;
};
