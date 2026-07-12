import { ProTable } from '@ant-design/pro-components';
import { Modal } from 'antd';
import type { UseTableRowSelectionReturnType } from '@/hooks/useTableRowSelection';

interface TableGenTableImportModalProps {
  open?: boolean;
  dataSource: any[];
  onCancel?: () => void;
  selection: UseTableRowSelectionReturnType;
  onSubmit?: () => void;
  onConditionChange: (value: any) => void;
}

const TableGenTableImportModal = ({
  open,
  dataSource,
  onCancel,
  selection,
  onSubmit,
  onConditionChange,
}: TableGenTableImportModalProps) => {
  return (
    <Modal
      open={open}
      width={'80vw'}
      className={'h-96'}
      destroyOnHidden={true}
      title={'导入表结构'}
      onCancel={onCancel}
      onOk={onSubmit}
    >
      <ProTable
        rowKey={'tableName'}
        defaultSize={'small'}
        options={false}
        pagination={{
          size: 'small',
          showSizeChanger: false,
          defaultPageSize: 6,
          pageSize: 6,
        }}
        onSubmit={(params) => onConditionChange(params)}
        rowSelection={selection.rowSelection}
        dataSource={dataSource}
        columns={[
          { title: '表名称', dataIndex: 'tableName', key: 'tableName' },
          { title: '表描述', dataIndex: 'tableComment', key: 'tableComment' },
          {
            title: '实体',
            dataIndex: 'className',
            key: 'className',
            search: false,
          },
          {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            valueType: 'dateTime',
            search: false,
          },
          {
            title: '更新时间',
            dataIndex: 'updateTime',
            key: 'updateTime',
            valueType: 'dateTime',
            search: false,
          },
        ]}
      />
    </Modal>
  );
};
export default TableGenTableImportModal;
