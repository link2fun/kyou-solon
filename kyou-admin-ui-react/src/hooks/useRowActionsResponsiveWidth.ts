import { useResponsive, useSize } from 'ahooks';
import { useCallback, useMemo, useRef } from 'react';

/** 使用自适应的固定宽度, 用于计算 table 操作按钮的宽度 */
const useRowActionsResponsiveWidth = () => {
  // {xs: true, sm: false, md: false, lg: false, xl: false}
  const responsive = useResponsive();
  const ref = useRef<HTMLObjectElement>(null);
  const size = useSize(ref);

  // 预设的最大宽度计算 - 基于所有可能的按钮组合
  const getPresetMaxWidth = useCallback(() => {
    // 根据当前页面的按钮类型，预设最大宽度
    // 这里可以根据实际情况调整按钮宽度
    const buttonWidth = 32; // 每个按钮的预估宽度
    const maxButtons = 3; // 最大按钮数量
    const padding = 12;

    if (responsive?.md === true) {
      // 中等屏幕下所有的按钮都显示
      return Math.max(buttonWidth * maxButtons + padding, 64);
    } else if (responsive?.sm === true) {
      // 小屏幕下每行最多显示 3 个按钮
      return Math.max(buttonWidth * 3 + padding, 64);
    } else {
      // 超小屏幕下每行最多显示 2 个按钮
      return Math.max(buttonWidth * 2 + padding, 64);
    }
  }, [responsive]);

  // 添加一个方法来计算所有操作按钮容器的最大宽度
  const calculateMaxWidth = useCallback(() => {
    // 查找表格中所有的操作按钮容器
    const tableElement = ref.current?.closest('.ant-table-content');
    if (!tableElement) {
      return getPresetMaxWidth(); // 使用预设宽度
    }

    const operationContainers = tableElement.querySelectorAll('.row-actions');
    let maxWidth = 64;

    operationContainers.forEach((container) => {
      const children = container.children as unknown as any[];
      let visibleCount = 0;
      let totalWidth = 0;

      for (let i = 0; i < children.length; i++) {
        if (
          children[i].style.display !== 'none' &&
          children[i].offsetWidth > 0
        ) {
          visibleCount++;
          totalWidth += children[i].offsetWidth;
        }
      }

      if (visibleCount > 0) {
        let rowWidth: number;
        if (visibleCount === 1) {
          rowWidth = Math.max(totalWidth + 12, 64);
        } else if (responsive?.md === true) {
          rowWidth = Math.max(totalWidth + 12, 64);
        } else if (responsive?.sm === true) {
          rowWidth = totalWidth / (visibleCount / 3) + 12;
        } else {
          rowWidth = totalWidth / (visibleCount / 2) + 12;
        }
        maxWidth = Math.max(maxWidth, rowWidth);
      }
    });

    return maxWidth;
  }, [responsive, getPresetMaxWidth]);

  const width = useMemo(() => {
    // 首先尝试使用最大宽度计算方法
    const maxWidth = calculateMaxWidth();
    if (maxWidth > 64) {
      return maxWidth;
    }

    // 如果无法计算最大宽度，使用预设宽度
    const presetWidth = getPresetMaxWidth();
    if (presetWidth > 64) {
      return presetWidth;
    }

    // 最后回退到原来的逻辑
    const children = (ref?.current?.children || []) as any[];
    const eleCount = children.length;
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
      return Math.max(contentWith + 12, 64);
    }

    if (responsive?.md === true) {
      // 中等屏幕下所有的按钮都显示
      return Math.max(contentWith + 12, 64);
    }
    // 小屏幕下的话 需要拆行显示
    // sm 每行最多显示 3 个按钮
    if (responsive?.sm === true) {
      return contentWith / (eleCount / 3) + 12;
    }
    return contentWith / (eleCount / 2) + 12;
  }, [size, ref, responsive]);

  return {
    ref,
    width: width,
  };
};

export default useRowActionsResponsiveWidth;
