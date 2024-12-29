import { Typography } from 'antd';
import { TextProps } from 'antd/es/typography/Text';
import React, { memo } from 'react';

interface EllipsisTextProps extends TextProps {
  width?: number;
  text: string;
}

const EllipsisText: React.FC<EllipsisTextProps> = memo(({ width = 200, text, style, ...props }) => {
  return (
    <Typography.Text style={{ width, ...style }} ellipsis={{ tooltip: text }} {...props}>
      {text}
    </Typography.Text>
  );
});

EllipsisText.displayName = 'EllipsisText';

export default EllipsisText;
