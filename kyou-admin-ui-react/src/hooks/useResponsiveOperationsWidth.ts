import { useResponsive, useSize } from 'ahooks';
import { useMemo, useRef } from 'react';

/** 使用自适应的固定宽度, 用于计算 table 操作按钮的宽度 */
const useResponsiveOperationsWidth = () => {
  // {xs: true, sm: false, md: false, lg: false, xl: false}
  const responsive = useResponsive();
  const ref = useRef<HTMLObjectElement>(null);
  const size = useSize(ref);

  const width = useMemo(() => {
    let children = (ref?.current?.children || []) as any[];
    let eleCount = children.length;
    if (eleCount < 1) {
      return 64;
    }

    let contentWith = 0;
    for (let i = 0; i < eleCount; i++) {
      if (children[i].style.display === 'none') {
        continue;
      }
      contentWith = contentWith + children[i].offsetWidth;
    }
    if (eleCount === 1) {
      // 只有一个按钮
      return Math.max(contentWith + 8, 64);
    }

    if (responsive?.md === true) {
      // 中等屏幕下所有的按钮都显示
      return Math.max(contentWith + 8, 64);
    }
    // 小屏幕下的话 需要拆行显示
    // sm 每行最多显示 3 个按钮
    if (responsive?.sm === true) {
      return contentWith / (eleCount / 3) + 8;
    }
    return contentWith / (eleCount / 2) + 8;
  }, [size, ref, responsive]);

  return {
    ref,
    width: width,
  };
};

export default useResponsiveOperationsWidth;
