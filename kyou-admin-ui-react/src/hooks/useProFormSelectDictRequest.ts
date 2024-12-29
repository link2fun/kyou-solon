import useDict from '@/hooks/useDict';

type UseProFormSelectDictRequestProps = {
  typeCode: string;
  valueInLabel?: boolean;
};

/**
 * 加载字典数据, 用于 ProFormSelect 的 request 属性
 * @param props
 */
const useProFormSelectDictRequest = (
  props: UseProFormSelectDictRequestProps,
) => {
  const dict = useDict(props);
  return dict.proFormSelectRequest;
};

export default useProFormSelectDictRequest;
