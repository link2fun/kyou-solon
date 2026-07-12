import { type ProColumns, ProList } from '@ant-design/pro-components';
import { Spin, Table, Transfer } from 'antd';
import type { TransferDirection, TransferItem } from 'antd/es/transfer';
import type { TransferKey } from 'antd/es/transfer/interface';

interface ProListTransferProps<T extends TransferItem> {
  dataSource: T[];
  targetKeys?: TransferKey[] | undefined;
  disabled?: boolean;
  showSearch?: boolean;
  showSelectAll?: boolean;
  onChange?: (
    targetKeys: TransferKey[],
    direction: TransferDirection,
    moveKeys: TransferKey[],
  ) => void;
  filterOption?: (
    inputValue: string,
    item: T,
    direction: TransferDirection,
  ) => boolean;
  columns: ProColumns<T>[];
  loading: boolean;
}

const ProListTransfer = <T extends TransferItem>({
  dataSource,
  targetKeys,
  disabled,
  showSearch,
  showSelectAll,
  onChange,
  filterOption,
  columns,
  loading,
}: ProListTransferProps<T>) => {
  return (
    <Spin spinning={loading}>
      <Transfer
        dataSource={dataSource}
        targetKeys={targetKeys}
        disabled={disabled}
        showSearch={showSearch}
        showSelectAll={showSelectAll}
        onChange={onChange}
        filterOption={filterOption}
        titles={['未授权', '已授权']}
      >
        {({ filteredItems, onItemSelectAll, selectedKeys }) => {
          return (
            <ProList<T>
              className={'m-2'}
              rowClassName={'mx-2'}
              rowKey={'key'}
              dataSource={filteredItems}
              rowSelection={{
                alwaysShowAlert: true,
                hideSelectAll: false,
                onChange(selectedRowKeys) {
                  onItemSelectAll(selectedRowKeys, 'replace');
                },
                selectedRowKeys: selectedKeys,
                selections: [
                  Table.SELECTION_ALL,
                  Table.SELECTION_INVERT,
                  Table.SELECTION_NONE,
                ],
              }}
              columns={columns}
            />
          );
        }}
      </Transfer>
    </Spin>
  );
};

export default ProListTransfer;
