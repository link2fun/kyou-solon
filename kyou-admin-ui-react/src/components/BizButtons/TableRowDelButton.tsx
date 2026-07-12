import { DeleteOutlined } from '@ant-design/icons';
import React, { memo } from 'react';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import type { UseActionControlReturnType } from '@/hooks/useActionControl';

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
  /** 是否禁用, 禁用时不弹出确认框, 仅显示 disabledText 提示 */
  disabled?: boolean;
  /** 禁用状态下的提示文本 */
  disabledText?: string;
}

const TableRowDelButton: React.FC<TableRowDelButtonProps> = memo(
  ({
    actionControl,
    record,
    permissionsRequired = [],
    buttonText = '',
    tooltip = '删除',
    disabled,
    disabledText,
  }) => {
    const handleConfirm = async () => {
      await actionControl.actions.handleRemoveAction(record);
    };

    return (
      <PopconfirmButton
        tooltip={tooltip}
        permissionsRequired={permissionsRequired}
        buttonText={buttonText}
        title="删除后不可恢复, 确认删除吗?"
        disabled={disabled}
        disabledText={disabledText}
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
