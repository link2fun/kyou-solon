import PermissionButton from '@/components/BizButtons/PermissionButton';
import { UseActionControlReturnType } from '@/hooks/useActionControl';
import { EditOutlined } from '@ant-design/icons';
import React, { memo } from 'react';

interface TableRowEditButtonProps {
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

const TableRowEditButton: React.FC<TableRowEditButtonProps> = memo(
  ({ actionControl, record, permissionsRequired = [], buttonText = '', tooltip = '编辑' }) => {
    const handleClick = () => {
      actionControl.actions.openUpdateModal(record);
    };

    return (
      <PermissionButton
        title={tooltip}
        permissionsRequired={permissionsRequired}
        icon={<EditOutlined />}
        type="link"
        loading={actionControl.loading.value}
        onClick={handleClick}
      >
        {buttonText}
      </PermissionButton>
    );
  },
);

TableRowEditButton.displayName = 'TableRowEditButton';

export default TableRowEditButton;
