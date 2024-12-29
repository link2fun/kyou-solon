import { UseActionControlReturnType } from '@/hooks/useActionControl';
import useTableRowSelection, {
  UseTableRowSelectionReturnType,
} from '@/hooks/useTableRowSelection';
import ApiToolGen from '@/services/tool/ApiToolGen';
import { useEffect, useState } from 'react';

const useToolGenTableImport = (actionControl: UseActionControlReturnType) => {
  const [open, setOpen] = useState<boolean>(false);
  const [data, setData] = useState<any[]>([]);
  const selection: UseTableRowSelectionReturnType = useTableRowSelection(data, {
    itemKey: 'tableName',
  });
  const [searchParams, setSearchParams] = useState<any>({});

  const openImportModal = async () => {
    await actionControl.loading.wrap({
      action: async () => {
        const _data = await ApiToolGen.dbList(searchParams);
        setData(_data.rows);
        setOpen(true);
      },
      loadingMessage: '加载中...',
    });
  };

  const onCancel = () => {
    setData([]);
    setOpen(false);
    // 清空选择
    selection.setSelected([]);
  };

  const onSubmit = () => {
    return actionControl.loading.wrap({
      action: async () => {
        const tables = selection.selectedRowKeys;
        await ApiToolGen.importTable((tables as string[]).join(','));
        actionControl.actions.reloadTableData();
        onCancel();
      },
      loadingMessage: '加载中...',
      successMessage: '导入成功',
      failMessage: '导入失败',
    });
  };

  useEffect(() => {
    if (open) {
      ApiToolGen.dbList(searchParams).then((res) => {
        setData(res.rows);
      });
    }
  }, [searchParams]);

  return {
    openImportModal,
    modal: {
      open,
      dataSource: data,
      selection,
      onCancel,
      onSubmit,
      onConditionChange: setSearchParams,
    },
  };
};

export default useToolGenTableImport;
