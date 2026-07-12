import {
  ModalForm,
  type ProFormInstance,
  type SubmitterProps,
} from '@ant-design/pro-components';
import { Button, Spin } from 'antd';
import React, {
  type PropsWithChildren,
  useEffect,
  useMemo,
  useState,
} from 'react';
import type { EditModalProps } from '@/typing';

interface DynamicProps {
  readonly?: boolean;
  submitter?:
    | SubmitterProps<{
        form?: ProFormInstance;
      }>
    | false;
}

/**
 * EditModalForm组件：用于编辑、新增或查看详情的模态表单
 * @param props 组件属性
 * @returns 返回一个模态表单组件
 */
const EditModalForm: React.FC<PropsWithChildren<EditModalProps>> = ({
  action,
  children,
  formRef,
  initData,
  loading,
  onCancel,
  onSubmit,
  open,
  readonly,
  title,
  ...restProps
}) => {
  // 只读模式的属性配置
  const readonlyProps: DynamicProps = {
    readonly: true,
    submitter: {
      render: () => <Button onClick={() => onCancel()}>关闭</Button>,
    },
  };

  // 根据readonly属性决定使用哪种配置
  const dynamicProps = readonly ? readonlyProps : restProps;

  // 根据props计算模态框标题
  const modalTitle = useMemo(() => {
    if (title) return title;

    switch (action) {
      case 'add':
        return '新增';
      case 'edit':
        return '编辑';
      default:
        return '详情';
    }
  }, [action, title]);

  // 表单内容延迟挂载：open 时先渲染 loading 占位，下一轮事件循环再挂载 children
  const [contentReady, setContentReady] = useState(false);
  useEffect(() => {
    if (!open) {
      setContentReady(false);
      return;
    }
    const timer = setTimeout(() => setContentReady(true), 0);
    return () => clearTimeout(timer);
  }, [open]);

  return (
    <Spin spinning={loading && open} classNames={{ root: 'w-full' }}>
      <ModalForm
        layout="horizontal"
        title={modalTitle}
        labelCol={{ span: 6 }}
        wrapperCol={{ span: 14 }}
        open={open}
        formRef={formRef}
        initialValues={initData}
        {...dynamicProps}
        modalProps={{
          destroyOnHidden: true,
          onCancel: () => onCancel(),
        }}
        onFinish={(formData) => onSubmit({ ...initData, ...formData })}
      >
        {contentReady ? (
          children
        ) : (
          <div
            style={{
              minHeight: 120,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Spin />
          </div>
        )}
      </ModalForm>
    </Spin>
  );
};

export default EditModalForm;
