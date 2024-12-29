import useLoadingState from '@/hooks/useLoadingState';
import SystemMenuSelect from '@/pages/system/menu/references/SystemMenuSelect';
import ToolGenPreviewModal from '@/pages/tool/gen/components/ToolGenPreviewModal';
import useToolGenPreview from '@/pages/tool/gen/hooks/useToolGenPreview';
import ApiSystemDictType from '@/services/system/ApiSystemDictType';
import ApiToolGen from '@/services/tool/ApiToolGen';
import Convert from '@/utils/Convert';
import { useModel } from '@@/exports';
import { EyeOutlined, FullscreenOutlined, SaveOutlined } from '@ant-design/icons';
import {
  EditableFormInstance,
  EditableProTable,
  FormInstance,
  ProForm,
  ProFormDependency,
  ProFormGroup,
  ProFormList,
  ProFormRadio,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import type { ActionType } from '@ant-design/pro-table/es/typing';
import { useLocation } from '@umijs/max';
import { useToggle } from 'ahooks';
import { Button, Space, Spin, Tabs } from 'antd';
import { createStyles } from 'antd-style';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';

// 样式定义
const useStyle = createStyles(({ css, token }) => {
  // @ts-ignore
  const { antCls } = token;
  return {
    customTable: css`
      ${antCls}-table {
        ${antCls}-table-container {
          ${antCls}-table-body,
          ${antCls}-table-content {
            scrollbar-width: thin;
            scrollbar-color: #eaeaea transparent;
            scrollbar-gutter: stable;
          }
        }
      }
    `,
  };
});

// 布局常量
const halfLine = { span: 24, md: 12 };
const fullLine = { span: 24 };

const ToolGenEditIndex = () => {
  // refs
  const tableActionRef = useRef<ActionType>();
  const genInfoFormRef = useRef<FormInstance>();
  const editableFormRef = useRef<EditableFormInstance>();

  // hooks
  const { closeActiveTab } = useModel('global');
  const [fullScreen, { toggle: toggleScreen }] = useToggle(false);
  const { styles } = useStyle();
  const { search } = useLocation();
  const loadingState = useLoadingState({
    defaultValue: true,
    wait: 500,
  });

  // state
  const [rowDataSource, setRowDataSource] = useState<any[]>([]);
  const [baseInfo, setBaseInfo] = useState({});

  // 预览模态框
  const previewModal = useToolGenPreview(loadingState);

  // 获取表ID
  const tableId = useMemo(() => {
    const searchParams = new URLSearchParams(search);
    return searchParams.get('tableId') || '';
  }, [search]);

  // 加载数据
  const loadData = useCallback(async () => {
    await loadingState.wrap({
      action: async () => {
        const { info, rows } = await ApiToolGen.detail(tableId);
        const { options: optionsStr, ...restInfo } = info;

        // 处理基础信息
        const options = optionsStr ? JSON.parse(optionsStr) : {};
        setBaseInfo({ ...restInfo, ...options });

        // 处理行数据
        const formattedRows = (rows as any[]).map(({ isInsert, isEdit, isList, isQuery, isRequired, ...rest }) => ({
          ...rest,
          isInsert: Convert.toBoolean(isInsert),
          isEdit: Convert.toBoolean(isEdit),
          isList: Convert.toBoolean(isList),
          isQuery: Convert.toBoolean(isQuery),
          isRequired: Convert.toBoolean(isRequired),
        }));
        setRowDataSource(formattedRows);
      },
      loadingMessage: '加载中...',
    });
  }, [tableId]);

  // 可编辑的行keys
  const editableKeys = useMemo(() => rowDataSource.map((row) => row.columnId), [rowDataSource]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // 保存处理
  const handleSave = async () => {
    const genInfo = await genInfoFormRef.current?.getFieldsValue();

    await loadingState.wrap({
      action: async () => {
        const formattedRows = rowDataSource.map(({ isInsert, isEdit, isList, isQuery, isRequired, ...rest }) => ({
          ...rest,
          isInsert: Convert.toBooleanStr1or0(isInsert),
          isEdit: Convert.toBooleanStr1or0(isEdit),
          isList: Convert.toBooleanStr1or0(isList),
          isQuery: Convert.toBooleanStr1or0(isQuery),
          isRequired: Convert.toBooleanStr1or0(isRequired),
        }));

        await ApiToolGen.edit({
          tableId,
          ...baseInfo,
          ...genInfo,
          columns: formattedRows,
          params: {
            parentMenuId: genInfo?.parentMenuId,
            treeCode: genInfo?.treeCode,
            treeName: genInfo?.treeName,
            treeParentCode: genInfo?.treeParentCode,
            moduleCode: genInfo?.moduleCode,
            moduleDesc: genInfo?.moduleDesc,
            moduleCodeUpperCamelShort: genInfo?.moduleCodeUpperCamelShort,
            moduleCodeUpperCamel: genInfo?.moduleCodeUpperCamel,
            moduleCodeLowerCamel: genInfo?.moduleCodeLowerCamel,
            javaPackageName: genInfo?.javaPackageName,
            dirName: genInfo?.dirName,
          },
        });
      },
      loadingMessage: '保存中...',
      successMessage: '保存成功',
      failMessage: '保存失败',
    });
  };

  // noinspection JSUnusedGlobalSymbols
  return (
    <div>
      <Space className={'ml-6 mb-3'}>
        <Button type={'default'} onClick={closeActiveTab}>
          关闭
        </Button>
        <Button type={'primary'} icon={<SaveOutlined />} loading={loadingState.value} onClick={handleSave}>
          保存
        </Button>
        <Button icon={<EyeOutlined />} loading={loadingState.value} onClick={() => previewModal.onPreview(tableId)}>
          预览
        </Button>
      </Space>

      <Tabs
        defaultActiveKey={'tab2'}
        items={[
          {
            label: '生成信息',
            key: 'tab1',
            children: (
              <Spin spinning={loadingState.value}>
                <ProForm
                  initialValues={baseInfo}
                  submitter={false}
                  colProps={halfLine}
                  layout={'horizontal'}
                  grid={true}
                  formRef={genInfoFormRef}
                  labelCol={{ span: 6 }}
                  className={'mt-14'}
                  loading={loadingState.value}
                >
                  <ProFormText name={'tableName'} label={'表名称'} />
                  <ProFormText name={'tableComment'} label={'表描述'} />
                  <ProFormText name={'className'} label={'实体类名称'} />
                  <ProFormText name={'functionAuthor'} label={'作者'} />
                  <ProFormTextArea name={'remark'} label={'备注'} labelCol={{ span: 3 }} colProps={fullLine} />
                  <ProFormSelect
                    label={'生成模板'}
                    name={'tplCategory'}
                    options={[
                      { label: '单表（增删改查）', value: 'crud' },
                      { label: '树表（增删改查）', value: 'tree' },
                      { label: '主子表（增删改查）', value: 'sub' },
                    ]}
                  />
                  <ProFormSelect
                    label={'前端类型'}
                    name={'tplWebType'}
                    options={[
                      { label: 'AntDesign Umi Max 模板', value: 'ant-design-umi-max' },
                      { label: 'Vue2 Element UI 模版', value: 'element-ui' },
                      { label: 'Vue3 Element Plus 模版', value: 'element-plus' },
                    ]}
                  />
                  <ProFormText
                    label={'生成包路径'}
                    name={'packageName'}
                    tooltip={'生成在哪个java包下，例如 com.ruoyi.system'}
                  />
                  <ProFormText label={'生成模块名'} name={'moduleName'} tooltip={'可理解为子系统名，例如 system'} />
                  <ProFormText label={'生成业务名'} name={'businessName'} tooltip={'可理解为功能英文名，例如 user'} />
                  <ProFormText label={'生成功能名'} name={'functionName'} tooltip={'用作类描述，例如 用户'} />
                  <ProFormRadio.Group
                    label={'生成代码方式'}
                    name={'genType'}
                    options={[
                      { label: 'zip压缩包', value: '0' },
                      { label: '自定义路径', value: '1' },
                    ]}
                  />
                  <SystemMenuSelect
                    label={'上级菜单'}
                    name={'parentMenuId'}
                    tooltip={'分配到指定菜单下，例如 系统管理'}
                  />
                  <ProFormDependency name={['genType']}>
                    {({ genType }) =>
                      genType === '1' && (
                        <ProFormText
                          label={'自定义路径'}
                          name={'genPath'}
                          labelCol={{ span: 4 }}
                          tooltip={'填写磁盘绝对路径，若不填写，则生成到当前Web项目下'}
                        />
                      )
                    }
                  </ProFormDependency>
                  <ProFormDependency name={['tplWebType']}>
                    {({ tplWebType }) =>
                      tplWebType === 'ant-design-umi-max' && (
                        <>
                          <ProFormText
                            label={'模块编码'}
                            name={'moduleCode'}
                            tooltip={'模块编码，例如 system 用于权限控制符的生成 eg: ["模块编码:dict:view"]'}
                          />
                          <ProFormText
                            label={'模块的中文名'}
                            name={'moduleDesc'}
                            tooltip={'模块的中文名，例如 系统管理'}
                          />
                          <ProFormText
                            label={'模块编码大驼峰缩写'}
                            name={'moduleCodeUpperCamelShort'}
                            tooltip={'模块编码大驼峰缩写。以 basedoc 为例，结果为 Bd'}
                          />
                          <ProFormText
                            label={'模块编码大驼峰'}
                            name={'moduleCodeUpperCamel'}
                            tooltip={' 模块编码驼峰命名首字母大写。以 basedoc 为例 结果为 BaseDoc'}
                          />
                          <ProFormText
                            label={'模块编码小驼峰'}
                            name={'moduleCodeLowerCamel'}
                            tooltip={'模块编码 驼峰命名 首字母小写。以 basedoc 为例 结果为 baseDoc'}
                          />
                          <ProFormText
                            label={'Java包名'}
                            name={'javaPackageName'}
                            tooltip={'Java包名，例如 basedoc 需全小写'}
                          />
                          <ProFormText label={'前端路径'} name={'dirName'} tooltip={'前端路径，例如 basedoc'} />
                        </>
                      )
                    }
                  </ProFormDependency>
                </ProForm>
              </Spin>
            ),
          },
          {
            label: '字段信息',
            key: 'tab2',
            children: (
              <EditableProTable<any>
                editableFormRef={editableFormRef}
                actionRef={tableActionRef}
                className={styles.customTable}
                loading={loadingState.value}
                toolbar={{
                  settings: [
                    {
                      icon: <FullscreenOutlined />,
                      tooltip: '全屏',
                      onClick: () => {
                        toggleScreen();
                        tableActionRef?.current?.fullScreen?.();
                      },
                    },
                  ],
                }}
                defaultSize={'small'}
                scroll={{
                  x: 'max-content',
                  y: fullScreen ? 32 * 18 : 32 * 10 + 20,
                }}
                rowKey="columnId"
                value={rowDataSource}
                onValuesChange={setRowDataSource}
                recordCreatorProps={false}
                editable={{
                  type: 'multiple',
                  editableKeys,
                  onValuesChange: (_record, recordList) => setRowDataSource(recordList),
                }}
                columns={[
                  {
                    title: '字段列名',
                    dataIndex: 'columnName',
                    readonly: true,
                    fixed: true,
                    width: 120,
                  },
                  {
                    title: '字段描述',
                    dataIndex: 'columnComment',
                    fixed: true,
                    formItemProps: { rules: [{ required: true }] },
                  },
                  {
                    title: 'Java类型',
                    dataIndex: 'javaType',
                    valueType: 'select',
                    valueEnum: {
                      Long: 'Long',
                      String: 'String',
                      Integer: 'Integer',
                      Double: 'Double',
                      BigDecimal: 'BigDecimal',
                      LocalDate: 'LocalDate',
                      LocalDateTime: 'LocalDateTime',
                      Boolean: 'Boolean',
                      Date: 'Date',
                    },
                    formItemProps: { rules: [{ required: true }] },
                  },
                  {
                    title: 'java属性',
                    dataIndex: 'javaField',
                    formItemProps: { rules: [{ required: true }] },
                  },
                  {
                    title: '插入',
                    dataIndex: 'isInsert',
                    valueType: 'switch',
                  },
                  {
                    title: '编辑',
                    dataIndex: 'isEdit',
                    valueType: 'switch',
                  },
                  {
                    title: '列表',
                    dataIndex: 'isList',
                    valueType: 'switch',
                  },
                  {
                    title: '查询',
                    dataIndex: 'isQuery',
                    valueType: 'switch',
                  },
                  {
                    title: '查询类型',
                    dataIndex: 'queryType',
                    valueType: 'select',
                    valueEnum: {
                      EQ: '=',
                      NE: '!=',
                      GT: '>',
                      GE: '>=',
                      LT: '<',
                      LE: '<=',
                      LIKE: 'LIKE',
                      BETWEEN: 'BETWEEN',
                      NOT_BETWEEN: 'NOT_BETWEEN',
                      IN: 'IN',
                      NOT_IN: 'NOT_IN',
                    },
                  },
                  {
                    title: '必填',
                    dataIndex: 'isRequired',
                    valueType: 'switch',
                  },
                  {
                    title: '显示类型',
                    dataIndex: 'htmlType',
                    valueType: 'select',
                    valueEnum: {
                      input: '文本框',
                      textarea: '文本域',
                      select: '下拉框',
                      radio: '单选框',
                      checkbox: '复选框',
                      datetime: '日期控件',
                      imageUpload: '图片上传',
                      fileUpload: '文件上传',
                      editor: '富文本控件',
                    },
                  },
                  {
                    title: '字典类型',
                    dataIndex: 'dictType',
                    valueType: 'select',
                    request: ApiSystemDictType.optionSelectProSelectRequest,
                  },
                  {
                    title: '复杂格式',
                    dataIndex: 'dictFormat',
                    width: 780,
                    renderFormItem: () => {
                      return (
                        <ProFormList name={'dictFormat'} alwaysShowItemLabel>
                          <ProFormGroup>
                            <ProFormText name={'key'} label={'键'} />
                            <ProFormText name={'value'} label={'值'} />
                          </ProFormGroup>
                        </ProFormList>
                      );
                    },
                  },
                ]}
              />
            ),
          },
        ]}
      />

      <ToolGenPreviewModal
        open={previewModal.modal.open}
        onCancel={previewModal.modal.onCancel}
        items={previewModal.modal.items}
        onRefresh={previewModal.modal.onRefresh}
      />
    </div>
  );
};

export default ToolGenEditIndex;
