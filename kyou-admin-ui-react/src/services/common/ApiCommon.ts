import WebConst from '@/constants/WebConst';
import { ErrorShowType } from '@/requestConfig';
import { downloadFile, get, postFile, postJSON } from '@/utils/request';

const ApiCommon = {
  /** 登录 */
  login: (loginBody: any): Promise<ExtendAjaxResult<unknown>> =>
    postJSON(
      `${WebConst.API_PREFIX}/login`,
      loginBody,
      {},
      { showType: ErrorShowType.SILENT },
    ),

  /** 登出 */
  logout: (): Promise<AjaxResult<never>> =>
    postJSON(`${WebConst.API_PREFIX}/logout`),

  /** 获取用户信息 */
  getInfo: (): Promise<
    ExtendAjaxResult<never> & {
      user: SysUserDTO;
      roles: string[];
      permissions: string[];
    }
  > => get(`${WebConst.API_PREFIX}/getInfo`),

  /** 获取路由信息 */
  getRouters: (): Promise<ExtendAjaxResult<SysMenu[]>> =>
    get(`${WebConst.API_PREFIX}/getRouters`),

  /** 注册 */
  register: (registerBody: any): Promise<ExtendAjaxResult<never>> =>
    postJSON(`${WebConst.API_PREFIX}/register`, registerBody),

  /** 通用下载 */
  download: (fileName: string, deleteAfterDownload: boolean) =>
    downloadFile(
      `${WebConst.API_PREFIX}/common/download`,
      { fileName, delete: deleteAfterDownload },
      fileName,
    ),

  /**  通用上传请求（单个） */
  upload: (
    file: File,
  ): Promise<
    ExtendAjaxResult<never> & {
      url: string;
      fileName: string;
      newFileName: string;
      originalFilename: string;
    }
  > => {
    const formData = new FormData();
    formData.append('file', file);

    return postFile(`${WebConst.API_PREFIX}/common/upload`, formData);
  },

  /**通用上传请求（多个）
   * 多个结果以英文,分割 */
  uploads: (
    files: File[],
  ): Promise<
    ExtendAjaxResult<never> & {
      url: string;
      fileName: string;
      newFileName: string;
      originalFilename: string;
    }
  > => {
    const formData = new FormData();
    files.forEach((file, index) => {
      formData.append(`file${index}`, file);
    });
    return postFile(`/common/uploads`, formData);
  },

  /** 本地资源通用下载 */
  downloadResource: (resource: string) =>
    downloadFile(`/common/download/resource`, { resource }, resource),

  /** 生成验证码 */
  captchaImage: (): Promise<
    ExtendAjaxResult<never> & {
      /** 验证码开启状态 */
      captchaEnabled: boolean;
      /** 验证码的 uuid, 用于验证码比对 */
      uuid: string;
      /** 验证码的图片 base64 */
      img: string;
    }
  > => get(`${WebConst.API_PREFIX}/captchaImage`),
};

export default ApiCommon;
