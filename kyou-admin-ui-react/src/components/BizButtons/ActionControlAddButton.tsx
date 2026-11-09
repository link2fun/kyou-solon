import { PlusOutlined } from '@ant-design/icons';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import type { UseActionControlReturnType } from '@/hooks/useActionControl';

interface ActionControlAddButtonProps {
  actionControl: UseActionControlReturnType;
  permissionsRequired?: string[];
  /** 权限或角色: 任一命中即通过 */
  orRole?: string[];
  initData?: Record<string, any>;
}

const ActionControlAddButton = ({
  actionControl,
  permissionsRequired = [],
  orRole,
  initData = {},
}: ActionControlAddButtonProps) => {
  const handleClick = () => {
    actionControl.actions.openAddModal({ ...initData });
  };

  return (
    <PermissionButton
      loading={actionControl.loading.value}
      icon={<PlusOutlined />}
      permissionsRequired={permissionsRequired}
      orRole={orRole}
      type="primary"
      onClick={handleClick}
    >
      新增
    </PermissionButton>
  );
};

export default ActionControlAddButton;
