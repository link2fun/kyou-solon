import { UseLoadingStateReturnType } from '@/hooks/useLoadingState';
import ApiToolGen from '@/services/tool/ApiToolGen';
import { useGetState } from 'ahooks';
import { useMemo } from 'react';

const useToolGenPreview = (loading: UseLoadingStateReturnType) => {
  const [open, setOpen] = useGetState<boolean>(false);
  const [data, setData] = useGetState<Record<string, string>>({});
  const [, setTableId, getTableId] = useGetState<string>('');

  const tabList = useMemo(() => {
    // 提取 data 中的 key
    return (
      Object.keys(data)
        // 对key进行排序
        .sort((a, b) => a.localeCompare(b))
        .map((key) => ({
          key,
          label: key,
          content: data[key],
          // children: React.createElement(CodeMirror, {value: data[key]})
        })) || []
    );
  }, [data]);

  const onRefresh = async () => {
    await loading.wrap({
      action: async () => {
        const data = await ApiToolGen.preview(getTableId());
        setData(data);
        setTimeout(() => {
          setOpen(true);
        }, 500);
      },
      loadingMessage: '加载中...',
    });
  };

  const onPreview = async (_tableId: string) => {
    setOpen(true);
    setTableId(_tableId);
    setTimeout(() => {
      onRefresh();
    }, 100);
  };

  const onCancel = () => {
    setOpen(false);
    setData({});
  };

  // 为了方便调整模板, 这里放一个定时刷新
  // useInterval(() => {
  //   if (open) {
  //     onPreview(tableId).then(() => {});
  //   }
  // }, 10000);

  return {
    onPreview,

    modal: {
      open,
      onCancel,
      items: tabList,
      onRefresh: onRefresh,
    },
  };
};

export default useToolGenPreview;
