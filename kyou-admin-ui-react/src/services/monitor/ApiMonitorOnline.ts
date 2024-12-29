import WebConst from '@/constants/WebConst';
import { deleteJSON, get } from '@/utils/request';

const ApiMonitorOnline = {
  /** 获取在线用户列表 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/monitor/online/list`, params),
  /** 强退用户 */
  forceLogout: (tokenId: string) =>
    deleteJSON(`${WebConst.API_PREFIX}/monitor/online/${tokenId}`),
};

export default ApiMonitorOnline;
