import WebConst from '@/constants/WebConst';
import ObjectTool from '@/utils/ObjectTool';
import {deleteJSON, get, postJSON, putJSON} from '@/utils/request';

/** 新增通知公告请求字段, 与后端 SysNoticeAddReq 对齐 */
const ADD_FIELDS: string[] = [
  'noticeTitle',
  'noticeType',
  'noticeContent',
  'status',
  'remark',
];

/** 修改通知公告请求字段 */
const EDIT_FIELDS: string[] = ['noticeId', ...ADD_FIELDS];

const ApiSystemNotice = {
  /** 获取通知公告列表 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/notice/list`, params),
  /** 获取通知公告详情 */
  detail: (noticeId: number) =>
    get(`${WebConst.API_PREFIX}/system/notice/${noticeId}`),
  /** 新增通知公告 */
  add: (data: any) =>
    postJSON(
      `${WebConst.API_PREFIX}/system/notice`,
      ObjectTool.pick(data, ADD_FIELDS),
    ),
  /** 修改通知公告 */
  edit: (data: any) =>
    putJSON(
      `${WebConst.API_PREFIX}/system/notice`,
      ObjectTool.pick(data, EDIT_FIELDS),
    ),
  /** 删除通知公告 */
  remove: (noticeId: number) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/notice/${noticeId}`),
};

export default ApiSystemNotice;
