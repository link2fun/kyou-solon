import PermissionButton from '@/components/BizButtons/PermissionButton';
import { UseActionControlReturnType } from '@/hooks/useActionControl';
import { EyeOutlined } from '@ant-design/icons';
import React, { memo } from 'react';

interface TableRowViewButtonProps {
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

const TableRowViewButton: React.FC<TableRowViewButtonProps> = memo(
  ({ actionControl, record, permissionsRequired = [], buttonText = '', tooltip = '查看详情' }) => {
    const handleClick = () => {
      actionControl.actions.openViewModal(record);
    };

    return (
      <PermissionButton
        permissionsRequired={permissionsRequired}
        title={tooltip}
        icon={<EyeOutlined />}
        type="link"
        loading={actionControl.loading.value}
        onClick={handleClick}
      >
        {buttonText}
      </PermissionButton>
    );
  },
);

TableRowViewButton.displayName = 'TableRowViewButton';

export default TableRowViewButton;
