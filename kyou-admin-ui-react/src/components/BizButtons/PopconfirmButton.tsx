import { useAccess } from '@umijs/max';
import { Button, Popconfirm, PopconfirmProps, Tooltip } from 'antd';
import { ButtonProps } from 'antd/es/button';
import React, { memo } from 'react';

interface PopconfirmButtonProps extends PopconfirmProps {
  /** 禁用状态提示文本 */
  disabledText?: string;
  /** 按钮的名字 */
  buttonText: string;
  /** 其他按钮属性 */
  buttonProps?: Partial<Pick<ButtonProps, Exclude<keyof ButtonProps, 'onClick' | 'title'>>>;
  /** 所需权限,eg: ['monitor:online:forceLogout'], 传 [] 的视作不需要权限, 默认不需要权限 */
  permissionsRequired?: string[];
  /** 按钮的操作提示 */
  tooltip?: string;
  /** 点击事件默认不向上传递 */
  stopPropagation?: boolean;
}

const PopconfirmButton: React.FC<PopconfirmButtonProps> = memo(
  ({
    disabledText = '当前按钮不可用',
    buttonText,
    buttonProps,
    permissionsRequired = [],
    tooltip,
    stopPropagation = true,
    ...popconfirmProps
  }) => {
    const access = useAccess();

    // 没有权限的情况下直接不显示
    if (!access.hasPermission(permissionsRequired)) {
      return null;
    }

    const dynamicButtonProps: ButtonProps = {
      ...buttonProps,
      ...(stopPropagation && { onClick: (e) => e.stopPropagation() }),
      disabled: popconfirmProps?.disabled || buttonProps?.disabled,
    };

    const buttonElement = <Button {...dynamicButtonProps}>{buttonText}</Button>;

    // 禁用状态下只显示 Tooltip 包裹的按钮
    if (popconfirmProps?.disabled) {
      return <Tooltip title={disabledText}>{buttonElement}</Tooltip>;
    }

    // 正常状态下显示完整的 Popconfirm
    return (
      <Popconfirm {...popconfirmProps}>
        <Tooltip title={tooltip}>{buttonElement}</Tooltip>
      </Popconfirm>
    );
  },
);

PopconfirmButton.displayName = 'PopconfirmButton';

export default PopconfirmButton;
