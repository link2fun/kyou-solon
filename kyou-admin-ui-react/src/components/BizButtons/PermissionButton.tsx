import { useAccess } from '@umijs/max';
import { Button } from 'antd';
import { ButtonProps } from 'antd/es/button';
import React, { memo } from 'react';

interface PermissionButtonProps extends ButtonProps {
  /** 需要的权限列表 */
  permissionsRequired?: string[];
}

/**
 * 权限按钮组件
 * 根据用户权限决定是否显示按钮
 */
const PermissionButton: React.FC<PermissionButtonProps> = memo(({ permissionsRequired, children, ...buttonProps }) => {
  const access = useAccess();

  // 如果没有所需权限,则不渲染按钮
  if (!access.hasPermission(permissionsRequired)) {
    return null;
  }

  return <Button {...buttonProps}>{children}</Button>;
});

PermissionButton.displayName = 'PermissionButton';

export default PermissionButton;
