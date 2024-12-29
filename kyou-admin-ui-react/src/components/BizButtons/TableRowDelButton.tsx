import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import { UseActionControlReturnType } from '@/hooks/useActionControl';
import { DeleteOutlined } from '@ant-design/icons';
import React, { memo } from 'react';

interface TableRowDelButtonProps {
  /** 操作控制器 */
  actionControl: UseActionControlReturnType;
  /** 当前行数据 */
  record: Record<string, any>;
  /** 所需权限,eg: ['monitor:online:forceLogout'], 传 [] 的视作不需要权限, 默认不需要权限 */
  permissionsRequired?: string[];
  /** 按钮的文字, 默认为空 即只显示图标 */
  buttonText?: string;
  /** 按钮的提示文本 */
  tooltip?: string;
}

const TableRowDelButton: React.FC<TableRowDelButtonProps> = memo(
  ({ actionControl, record, permissionsRequired = [], buttonText = '', tooltip = '删除' }) => {
    const handleConfirm = async () => {
      await actionControl.actions.handleRemoveAction(record);
    };

    return (
      <PopconfirmButton
        tooltip={tooltip}
        permissionsRequired={permissionsRequired}
        buttonText={buttonText}
        title="删除后不可恢复, 确认删除吗?"
        buttonProps={{
          danger: true,
          type: 'link',
          icon: <DeleteOutlined />,
          loading: actionControl.loading.value,
        }}
        onConfirm={handleConfirm}
      />
    );
  },
);

TableRowDelButton.displayName = 'TableRowDelButton';

export default TableRowDelButton;
