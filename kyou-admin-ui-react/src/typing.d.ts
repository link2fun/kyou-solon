import type {
  ModalFormProps as AntDesignModalFormProps,
  ProFormInstance,
} from '@ant-design/pro-components';
import React from 'react';
import { FlowAction } from '@/hooks/useActionControl';

// 全局变量声明：本文件含 import 属于模块，需用 declare global 块才能声明真正的全局类型。
// 这些变量由 config/config.ts 的 define 在构建时注入，供 Footer 等组件使用。
declare global {
  const __APP_VERSION__: string;
  const __UMI_VERSION__: string;
  const __UTOO_VERSION__: string;
}

export interface EditModalProps extends AntDesignModalFormProps {
  loading: boolean;
  action: FlowAction;
  initData: any;
  onCancel: (flag?: boolean, formVals?: any) => void;
  onSubmit: (values: any) => Promise<void>;
  formRef?: React.MutableRefObject<ProFormInstance | undefined>;
}

export type FormItemProps = {
  value?: React.Key | React.Key[];
  onChange?: (value: React.Key | React.Key[]) => void;
};

export type InitialState = {
  currentUser: CurrentUser;
  loading?: boolean;
  loginValid: boolean;
  fetchUserInfo?: () => Promise<CurrentUser>;
};
