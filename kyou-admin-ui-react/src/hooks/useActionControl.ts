import useResponsiveOperationsWidth from '@/hooks/useResponsiveOperationsWidth';
import useTableRowSelection from '@/hooks/useTableRowSelection';
// @ts-ignore
import useLoadingState, { UseLoadingStateReturnType } from '@/hooks/useLoadingState';
import { useModel } from '@@/exports';
import { ActionType, FormInstance } from '@ant-design/pro-components';
import { message } from 'antd';
import { SizeType } from 'antd/es/config-provider/SizeContext';
import { useEffect, useMemo, useRef, useState } from 'react';

export type FlowAction = 'add' | 'edit' | 'view' | 'remove' | undefined;

interface ActionInfo {
  /** 当 Modal 打开时, 一般用于修改操作加载最新数据 */
  onModalOpen?: (values: Record<string, any>) => Promise<Record<string, any>>;

  /** 编辑完数据, 点击提交时调用 */
  onActionCall: (values: Record<string, any>) => Promise<Record<string, any>>;

  /** onActionCall 成功后的回调 */
  onActionSuccess?: (result: Record<string, any>) => void;
  /** onActionCall 失败后的回调 */
  onActionError?: (e: any) => void;
}

interface ProTableEditModalProps {
  /** 新增操作 */
  addAction?: ActionInfo;
  /** 编辑操作 */
  editAction?: ActionInfo;
  /** 删除操作 */
  removeAction?: ActionInfo;
  /** 查看操作 */
  viewAction?: ActionInfo;
  /** 额外的操作 */
  extraActionMap?: {
    [key: string]: ActionInfo;
  };
  selection?: {
    rowKey: string;
  };
}

export type UseActionControlReturnType = ReturnType<typeof useActionControl>;

/**
 * 使用 ProTable 的编辑表单
 */
const useActionControl = (props: ProTableEditModalProps) => {
  const tableActionRef = useRef<ActionType>();
  const tableFormRef = useRef<FormInstance>();

  const editModalFormRef = useRef<FormInstance>();
  const { getActiveTab, updateTab, getHeaderTabState } = useModel('global');

  /** state 定义当前的操作 */
  const [action, setAction] = useState<FlowAction>(undefined);
  /** state 定义初始化的数据 */
  const [initData, setInitData] = useState<any>({});

  const isCreateOrUpdate = action === 'add' || action === 'edit';

  const loading: UseLoadingStateReturnType = useLoadingState();

  /** 定义一个 tableData 用于缓存表格数据 */
  const [rowData, setRowData] = useState<any[]>([]);

  // 定义一个 rowSelection
  const { rowSelection, setSelected, selectedRowKeys, selectedRowObjs, selectSingle, selectMultiple } =
    useTableRowSelection(rowData, { itemKey: props.selection?.rowKey });

  /** 定义一个响应式的 操作按钮宽度 */
  const responsiveRowAction = useResponsiveOperationsWidth();

  /** 打开新增Modal */
  const openAddModal = (initData?: any) => {
    setInitData({ ...initData });
    setAction('add');
    editModalFormRef.current?.setFieldsValue({ ...initData });
  };

  /** 打开修改Modal */
  const openUpdateModalAsync = async (initData?: any) => {
    // 设置初始化数据
    setInitData({ ...initData });
    setAction('edit');
    await loading.wrap({
      action: async () => {
        if (props?.editAction?.onModalOpen) {
          const formInitData = (await props?.editAction?.onModalOpen(initData || {})) || {};
          editModalFormRef.current?.setFieldsValue({ ...formInitData });
        } else {
          editModalFormRef.current?.setFieldsValue({ ...initData });
        }
      },
      loadingMessage: '加载中...',
    });
  };
  const openUpdateModal = (initData: any) => {
    openUpdateModalAsync(initData).then(() => {});
  };

  const openViewModal: (initData: any) => void = async (initData: any) => {
    setInitData(initData);
    setAction('view');
    if (props.viewAction?.onModalOpen) {
      await loading.wrap({
        action: async () => {
          if (props.viewAction?.onModalOpen) {
            const formInitData = await props.viewAction?.onModalOpen(initData);
            editModalFormRef.current?.setFieldsValue({ ...formInitData });
          }
        },
        loadingMessage: '加载中...',
      });
    } else {
      editModalFormRef.current?.setFieldsValue({ ...initData });
    }
  };

  /** 关闭Modal */
  const onCancel = () => {
    setAction(undefined);
    setInitData({});
  };

  const handleAddAction = async (values: any) => {
    if (props.addAction) {
      try {
        loading.begin();
        const result = await props.addAction.onActionCall(values);
        message.success('操作成功');
        tableActionRef.current?.reload();
        props.addAction.onActionSuccess?.(result);
        onCancel();
      } catch (e) {
        props.addAction.onActionError?.(e);
      } finally {
        loading.end();
      }
    }
  };

  const handleUpdateAction = async (values: any) => {
    if (props.editAction) {
      try {
        loading.begin();
        const result = await props.editAction.onActionCall(values);
        message.success('操作成功');
        tableActionRef.current?.reload();
        props.editAction.onActionSuccess?.(result);
        onCancel();
      } catch (e) {
        props.editAction.onActionError?.(e);
      } finally {
        loading.end();
      }
    }
  };

  const handleRemoveAction = async (values: any) => {
    if (props.removeAction) {
      try {
        loading.begin();
        const result = await props.removeAction.onActionCall(values);
        message.success('操作成功');
        tableActionRef.current?.reload();
        props.removeAction.onActionSuccess?.(result);
        onCancel();
      } catch (e) {
        props.removeAction.onActionError?.(e);
      } finally {
        loading.end();
      }
    }
  };

  const handleExtraAction = async (values: any) => {
    const actionExecutor = props.extraActionMap?.action;
    if (actionExecutor) {
      try {
        loading.begin();
        const result = await actionExecutor?.onActionCall?.(values);
        actionExecutor.onActionSuccess?.(result);
        onCancel();
      } catch (e) {
        actionExecutor.onActionError?.(e);
      } finally {
        loading.end();
      }
    }
  };

  /** 处理提交操作, 这里只能处理常规的 add/edit/remove 复杂的需要自己手动实现 */
  const onSubmit: (values: any) => Promise<void> = async (values: any) => {
    if (action === 'add') {
      return await handleAddAction(values);
    } else if (action === 'edit') {
      return await handleUpdateAction(values);
    } else if (action === 'remove') {
      return await handleRemoveAction(values);
    } else {
      return await handleExtraAction(values);
    }
  };

  /** 处理删除操作 */

  /** 获取缓存的查询条件 */
  const queryParams = useMemo(() => {
    const activeTab = getActiveTab();
    return activeTab?.queryParams;
  }, [getHeaderTabState().headerTabsActiveKey]);

  /** 更新缓存的查询条件 */
  const updateQueryParams = (queryParams: any) => {
    const activeTab = getActiveTab();
    updateTab({
      ...activeTab,
      queryParams,
    });
  };

  /** 缓存的查询条件 重新填充到 表格查询表单 */
  const fillQueryParamsToTableForm = () => {
    tableFormRef.current?.setFieldsValue(getActiveTab()?.queryParams);
  };

  const reloadTableData = () => {
    tableActionRef.current?.reload();
  };

  const onDataSourceChange = (dataSource: any[]) => {
    setRowData(dataSource);
  };

  useEffect(() => {
    // 每次加载的时候 填充查询条件到表单
    fillQueryParamsToTableForm();
  }, []);

  // noinspection JSUnusedGlobalSymbols
  return {
    table: {
      actionRef: tableActionRef,
      formRef: tableFormRef,
      params: getActiveTab()?.queryParams,
      onSubmit: (params: any) => updateQueryParams(params),
      // 重置查询条件, 清空查询条件
      onReset: () => updateQueryParams({}),
      // 默认密度
      defaultSize: 'small' as SizeType,
      // 行选择
      rowSelection: rowSelection,
      rowKey: props.selection?.rowKey,
      dataSource: rowData,
      onDataSourceChange,
      scroll: { x: 'max-content' },
    },
    editModal: {
      loading: loading.value,
      action: action,
      readonly: action === 'view',
      initData: initData,
      onCancel: onCancel,
      onSubmit: onSubmit,
      open: action !== undefined,
      formRef: editModalFormRef,
    },
    queryParams: {
      value: queryParams,
      update: updateQueryParams,
      refill: fillQueryParamsToTableForm,
      /** 给请求参数添加分页的 total 信息, 当传递 totalRow 后, 服务端分页时不会重新执行 count 查询<br/>
       * 当前只会在非首页时添加 totalRow, 首页(current === 1)不会添加
       * */
      wrapPageTotal: (params: any) => {
        const { current } = params;
        if (current === undefined || current === null || current === 0 || current === 1) {
          return params;
        }
        return {
          ...params,
          totalRow: tableActionRef.current?.pageInfo?.total,
        };
      },
    },

    actions: {
      openAddModal,
      openUpdateModal,
      /** 打开只读表单 */
      openViewModal,
      handleExtraAction,
      handleRemoveAction,
      /** 重新加载表格数据 */
      reloadTableData,
    },
    loading: loading,
    selection: {
      setSelected,
      selectedRowKeys,
      selectedRowObjs,
      /** 选中了单个 */
      selectSingle,
      /** 选中了多个 */
      selectMultiple,
      /** 未选中 */
      selectNone: !selectSingle && !selectMultiple,
      /** 清理选择 */
      clean: () => setSelected([]),
    },
    rowAction: {
      width: responsiveRowAction.width,
      ref: responsiveRowAction.ref,
    },
    isCreateOrUpdate,
  };
};

export default useActionControl;
