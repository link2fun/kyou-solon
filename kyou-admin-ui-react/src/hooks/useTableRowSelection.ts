import ObjectTool from '@/utils/ObjectTool';
import { useSelections } from 'ahooks';
import { Options } from 'ahooks/lib/useSelections';
import { Key, useMemo } from 'react';

export type UseTableRowSelectionReturnType = ReturnType<
  typeof useTableRowSelection
>;

export default function useTableRowSelection<T>(
  rows: T[],
  options?: Options<T>,
) {
  const selections = useSelections<T>(rows, options);

  const selectedRowKeys: Key[] = useMemo(() => {
    return (selections.selected || [])
      .map((item) => {
        if (item === undefined || item === null) {
          return undefined;
        }
        if (!ObjectTool.hasAnyKey(item, [options?.itemKey as string])) {
          return undefined;
        }
        return item[options?.itemKey as keyof T] as Key;
      })
      .filter((key) => key !== undefined) as Key[];
  }, [selections.selected]);

  /** 是否选中了单个 */
  const selectSingle = useMemo(() => {
    return selectedRowKeys && selectedRowKeys.length === 1;
  }, [selectedRowKeys]);

  /** 是否选中了多个 */
  const selectMultiple = useMemo(() => {
    return selectedRowKeys && selectedRowKeys.length > 1;
  }, [selectedRowKeys]);

  if (options?.itemKey === undefined) {
    // 没有传入 itemKey, 不启用 rowSelection
    return {
      rowSelection: false as false,
      setSelected: () => {},
      selectedRowKeys: [],
      selectedRowObjs: [],
      selectSingle: false,
      selectMultiple: false,
    };
  }

  const rowSelection = {
    preserveSelectedRowKeys: true,
    selectedRowKeys: selectedRowKeys,
    alwaysShowAlert: true,
    onChange: (selectedRowKeys: Key[], selectedRows: T[]) => {
      selections.setSelected(selectedRows);
    },
  };

  return {
    rowSelection,
    selectedRowKeys,
    selectedRowObjs: selections.selected,
    selectSingle,
    selectMultiple,
    ...selections,
  };
}
