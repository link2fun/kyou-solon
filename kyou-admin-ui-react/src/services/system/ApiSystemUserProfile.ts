import WebConst from '@/constants/WebConst';
import { get, postFile, putForm, putJSON } from '@/utils/request';

const ApiSystemUserProfile = {
  /** 获取个人信息 */
  profile: (): Promise<
    ExtendAjaxResult<SysUserDTO> & {
      roleGroup: any[];
      postGroup: any[];
    }
  > => get(`${WebConst.API_PREFIX}/system/user/profile`),

  /** 修改个人信息 */
  updateProfile: (updateReq: any): Promise<ExtendAjaxResult<never>> =>
    putJSON(`${WebConst.API_PREFIX}/system/user/profile`, updateReq),

  /** 修改密码 */
  updatePwd: (oldPassword: string, newPassword: string) =>
    putForm(`${WebConst.API_PREFIX}/system/user/profile/updatePwd`, {
      oldPassword,
      newPassword,
    }),

  /** 头像上传 */
  avatar: (avatarfile: File): Promise<ExtendAjaxResult<string>> => {
    const formData = new FormData();
    formData.append('avatarfile', avatarfile);

    return postFile(
      `${WebConst.API_PREFIX}/system/user/profile/avatar`,
      formData,
    );
  },
};

export default ApiSystemUserProfile;
