import useLoadingState from '@/hooks/useLoadingState';
import ApiMonitorCache from '@/services/monitor/ApiMonitorCache';
import { FormInstance } from '@ant-design/pro-components';
import { useEffect, useRef, useState } from 'react';

const useCacheListControl = () => {
  /** 定义 state 存储缓存名list */
  const [cacheNameList, setCacheNameList] = useState<any[]>([]);
  const cacheNameLoading = useLoadingState();
  const [cacheNameSelected, setCacheNameSelected] = useState<any>('');
  /** 定义 state 存储缓存key List */
  const [cacheKeyList, setCacheKeyList] = useState<any[]>([]);
  const cacheKeyLoading = useLoadingState();

  /** 定义 state 存储缓存内容 */
  const [cacheContent, setCacheContent] = useState<any>({});
  const cacheContentLoading = useLoadingState();

  const contentFormRef = useRef<FormInstance>();

  /** 初始加载缓存Name List */
  const loadCacheNameList = async () => {
    await cacheNameLoading.wrap(async () => {
      // 清空一下值
      setCacheNameList([]);
      setCacheKeyList([]);
      setCacheContent({});
      contentFormRef.current?.resetFields();
      const data = await ApiMonitorCache.getNames();
      setCacheNameList(data);
    });
  };

  /** 加载缓存 Key List */
  const loadCacheKeyList = async (cacheName: string) => {
    if (!cacheName) {
      return;
    }
    const data = await ApiMonitorCache.getCacheKeys(cacheName);
    setCacheKeyList(
      (data || []).map((item: string) => {
        return { key: item };
      }),
    );
  };

  /** 加载缓存内容 */
  const loadCacheContent = async (cacheName: string, cacheKey: string) => {
    if (!cacheName || !cacheKey) {
      return;
    }
    const data = await ApiMonitorCache.getCacheValue(cacheName, cacheKey);
    setCacheContent(data);
    contentFormRef.current?.setFieldsValue(data);
  };

  /** 点击缓存名加载缓存Key */
  const onClickCacheName = async (record: any) => {
    await cacheKeyLoading.wrap(async () => {
      setCacheContent({});
      setCacheKeyList([]);
      setCacheNameSelected(record);
      contentFormRef.current?.resetFields();
      await loadCacheKeyList(record.cacheName);
    });
  };

  /** 点击缓存Key加载缓存内容 */
  const onClickCacheKey = async (record: any) => {
    await cacheContentLoading.wrap(async () => {
      setCacheContent({});
      contentFormRef.current?.resetFields();
      await loadCacheContent(cacheNameSelected.cacheName, record.key);
    });
  };

  /** 清空指定缓存名称下的缓存 */
  const clearCacheName = async (record: any) => {
    await ApiMonitorCache.clearCacheName(record.cacheName);
    await loadCacheNameList();
  };
  /** 清除指定缓存key的缓存*/
  const clearCacheKey = async (record: any) => {
    await ApiMonitorCache.clearCacheKey(record.key);
    await loadCacheKeyList(cacheNameSelected.cacheName);
  };

  /** 清除全部缓存, 可能会造成异常, 请谨慎使用 */
  const clearCacheAll = async () => {
    await ApiMonitorCache.clearCacheAll();
    await loadCacheNameList();
  };

  useEffect(() => {
    loadCacheNameList().then(() => {});
  }, []);

  return {
    cacheNameList,
    cacheNameLoading: cacheNameLoading.value,
    cacheKeyList,
    cacheKeyLoading: cacheKeyLoading.value,
    cacheContent,
    cacheContentLoading: cacheContentLoading.value,
    cacheKeySelected: cacheNameSelected,
    contentFormRef,

    onClickCacheName,
    onClickCacheKey,
    clearCacheName,
    clearCacheKey,
    clearCacheAll,

    loadCacheNameList,
    loadCacheKeyList: async () => {
      await cacheKeyLoading.wrap(async () => {
        await loadCacheKeyList(cacheNameSelected?.cacheName);
      });
    },
  };
};

export default useCacheListControl;
