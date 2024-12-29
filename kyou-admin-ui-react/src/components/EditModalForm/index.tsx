import { EditModalProps } from '@/typing';
import { ModalForm } from '@ant-design/pro-components';
import type { SubmitterProps } from '@ant-design/pro-form/es/components';
import { Button, type FormInstance, Spin } from 'antd';
import React, { PropsWithChildren, useMemo } from 'react';

interface DynamicProps {
  readonly?: boolean;
  submitter?:
    | SubmitterProps<{
        form?: FormInstance;
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

  return (
    <Spin spinning={loading && open} wrapperClassName="w-full">
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
          destroyOnClose: true,
          onCancel: () => onCancel(),
        }}
        onFinish={(formData) => onSubmit({ ...initData, ...formData })}
      >
        {children}
      </ModalForm>
    </Spin>
  );
};

export default EditModalForm;
