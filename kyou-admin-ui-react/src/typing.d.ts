import { FlowAction } from '@/hooks/useActionControl';
import { FormInstance } from '@ant-design/pro-components';
import { ModalFormProps as AntDesignModalFormProps } from '@ant-design/pro-form/es/layouts/ModalForm';
import React from 'react';

export interface EditModalProps extends AntDesignModalFormProps {
  loading: boolean;
  action: FlowAction;
  initData: any;
  onCancel: (flag?: boolean, formVals?: any) => void;
  onSubmit: (values: any) => Promise<void>;
  formRef?: React.MutableRefObject<FormInstance | undefined>;
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
