import EditModalForm from '@/components/EditModalForm';
import IconPicker from '@/components/ProFormItem/IconPicker';
import SystemDictDataRadio from '@/pages/system/dictData/references/SystemDictDataRadio';
import SystemMenuSelect from '@/pages/system/menu/references/SystemMenuSelect';
import { EditModalProps } from '@/typing';
import {
  ProFormDependency,
  ProFormRadio,
  ProFormText,
} from '@ant-design/pro-components';
import React from 'react';

const SystemMenuEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props} grid={true} labelCol={{}} wrapperCol={{}}>
      <SystemMenuSelect
        name={'parentId'}
        label={'上级菜单'}
        rules={[{ required: true }]}
      />
      <ProFormRadio.Group
        name={'menuType'}
        label={'菜单类型'}
        rules={[{ required: true }]}
        valueEnum={{ M: '目录', C: '菜单', F: '按钮' }}
      />

      <IconPicker
        name={'icon'}
        label={'图标'}
        proFieldProps={{ readonly: props.readonly }}
      />
      <ProFormText
        name={'menuName'}
        label={'菜单名称'}
        rules={[{ required: true }]}
        colProps={{ md: 12 }}
      />
      <ProFormText
        name={'orderNum'}
        label={'显示排序'}
        rules={[{ required: true }]}
        colProps={{ md: 12 }}
      />
      <ProFormDependency name={['menuType']}>
        {({ menuType }) => {
          if (menuType !== 'F') {
            return (
              <>
                <ProFormRadio.Group
                  name={'isFrame'}
                  label={'是否外链'}
                  options={[
                    { value: '0', label: '是' },
                    { value: '1', label: '否' },
                  ]}
                  colProps={{ md: 12 }}
                />
                <ProFormText
                  name={'path'}
                  label={'路由地址'}
                  tooltip={
                    '访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头'
                  }
                  colProps={{ md: 12 }}
                />
              </>
            );
          }
          return <></>;
        }}
      </ProFormDependency>

      <ProFormDependency name={['menuType']}>
        {({ menuType }) => {
          if (menuType === 'C') {
            return (
              <>
                <ProFormText
                  name={'component'}
                  label={'组件路径'}
                  placeholder={'请输入组件路径'}
                  tooltip={
                    '访问的组件路径，如：`system/user/index`，默认在`views or pages`目录下'
                  }
                />
                <ProFormText
                  name={'query'}
                  label={'路由参数'}
                  placeholder={'请输入路由参数'}
                  tooltip={
                    '访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`'
                  }
                />
                <ProFormRadio.Group
                  name={'isCache'}
                  label={'是否缓存'}
                  options={[
                    { value: '0', label: '缓存' },
                    { value: '1', label: '不缓存' },
                  ]}
                />
              </>
            );
          }
          return <></>;
        }}
      </ProFormDependency>
      <ProFormDependency name={['menuType']}>
        {({ menuType }) => {
          if (menuType !== 'M') {
            return (
              <>
                <ProFormText
                  name={'perms'}
                  label={'权限标识'}
                  placeholder={'请输入权限标识'}
                  tooltip={
                    "控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasPermi('system:user:list')`)"
                  }
                />
              </>
            );
          }
          return <></>;
        }}
      </ProFormDependency>

      <ProFormDependency name={['menuType']}>
        {({ menuType }) => {
          if (menuType !== 'F') {
            return (
              <>
                <SystemDictDataRadio
                  name={'visible'}
                  label={'显示状态'}
                  tooltip={'选择隐藏则路由将不会出现在侧边栏，但仍然可以访问'}
                  typeCode={'sys_show_hide'}
                  colProps={{ md: 12 }}
                  valueInLabel={false}
                />
              </>
            );
          }
          return <></>;
        }}
      </ProFormDependency>
      <SystemDictDataRadio
        name={'status'}
        label={'菜单状态'}
        tooltip={'选择停用则路由将不会出现在侧边栏，也不能被访问'}
        typeCode={'sys_normal_disable'}
        colProps={{ md: 12 }}
        valueInLabel={false}
      />
    </EditModalForm>
  );
};

export default SystemMenuEditModal;
