import WebConst from '@/constants/WebConst';
import { deleteJSON, get } from '@/utils/request';

const ApiMonitorCache = {
  /** 获取缓存信息摘要 */
  detail: () => get(`${WebConst.API_PREFIX}/monitor/cache`),
  /** 获取缓存名称列表 */
  getNames: () => get(`${WebConst.API_PREFIX}/monitor/cache/getNames`),
  /** 获取缓存键列表 */
  getCacheKeys: (cacheName: string) =>
    get(`${WebConst.API_PREFIX}/monitor/cache/getKeys/${cacheName}`),
  /** 获取缓存值 */
  getCacheValue: (cacheName: string, cacheKey: string) =>
    get(
      `${WebConst.API_PREFIX}/monitor/cache/getValue/${cacheName}/${cacheKey}`,
    ),
  /** 清空指定缓存空间下的缓存 */
  clearCacheName: (cacheName: string) =>
    deleteJSON(
      `${WebConst.API_PREFIX}/monitor/cache/clearCacheName/${cacheName}`,
    ),
  /** 清空指定缓存key的指定缓存 */
  clearCacheKey: (cacheKey: string) =>
    deleteJSON(
      `${WebConst.API_PREFIX}/monitor/cache/clearCacheKey/${cacheKey}`,
    ),
  /** 清空所有缓存 */
  clearCacheAll: () =>
    deleteJSON(`${WebConst.API_PREFIX}/monitor/cache/clearCacheAll`),
};

export default ApiMonitorCache;
