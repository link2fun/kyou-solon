import { Access, useAccess } from '@umijs/max';
import React, { memo, PropsWithChildren } from 'react';

interface PermissionRequiredProps {
  /** 所需权限,eg: ['monitor:online:forceLogout'], 传 [] 的视作不需要权限 */
  value?: string[];
  /** 无权限时显示的内容 */
  fallback?: React.ReactNode;
}

const PermissionRequired: React.FC<PropsWithChildren<PermissionRequiredProps>> = memo(
  ({ value = [], children, fallback = <></> }) => {
    const access = useAccess();

    return (
      <Access accessible={access.hasPermission(value)} fallback={fallback}>
        {children}
      </Access>
    );
  },
);

PermissionRequired.displayName = 'PermissionRequired';

export default PermissionRequired;
