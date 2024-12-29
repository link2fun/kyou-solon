import WebConst from '@/constants/WebConst';
import { deleteJSON, get, postJSON, putJSON } from '@/utils/request';

const ApiSystemNotice = {
  /** 获取通知公告列表 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/notice/list`, params),
  /** 获取通知公告详情 */
  detail: (noticeId: number) =>
    get(`${WebConst.API_PREFIX}/system/notice/${noticeId}`),
  /** 新增通知公告 */
  add: (data: any) => postJSON(`${WebConst.API_PREFIX}/system/notice`, data),
  /** 修改通知公告 */
  edit: (data: any) => putJSON(`${WebConst.API_PREFIX}/system/notice`, data),
  /** 删除通知公告 */
  remove: (noticeId: number) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/notice/${noticeId}`),
};

export default ApiSystemNotice;
