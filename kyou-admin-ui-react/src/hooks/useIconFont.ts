import { createFromIconfontCN } from '@ant-design/icons';

const useIconFont = () => {
  const IconFont = createFromIconfontCN({
    scriptUrl: ['https://at.alicdn.com/t/c/font_756575_e9mihysan0h.js'],
  });

  return IconFont;
};

export default useIconFont;
