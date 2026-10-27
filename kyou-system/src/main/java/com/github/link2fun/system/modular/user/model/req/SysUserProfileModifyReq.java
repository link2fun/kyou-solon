package com.github.link2fun.system.modular.user.model.req;

import lombok.Data;

/**
 * 修改个人资料请求; 只含本人可改的四项, 账号/部门/角色/状态由服务端维护
 * {@link com.github.link2fun.support.core.domain.entity.SysUser}
 */
@Data
public class SysUserProfileModifyReq {

  /** 用户昵称 */
  private String nickName;

  /** 手机号 */
  private String phonenumber;

  /** 邮箱 */
  private String email;

  /** 性别 */
  private String sex;
}
