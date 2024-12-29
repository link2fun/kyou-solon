import PermissionButton from '@/components/BizButtons/PermissionButton';
import { UseActionControlReturnType } from '@/hooks/useActionControl';
import { PlusOutlined } from '@ant-design/icons';

interface ActionControlAddButtonProps {
  actionControl: UseActionControlReturnType;
  permissionsRequired?: string[];
  initData?: Record<string, any>;
}

const ActionControlAddButton = ({
  actionControl,
  permissionsRequired = [],
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
      type="primary"
      onClick={handleClick}
    >
      新增
    </PermissionButton>
  );
};

export default ActionControlAddButton;
