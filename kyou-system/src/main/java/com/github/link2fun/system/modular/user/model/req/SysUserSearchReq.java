package com.github.link2fun.system.modular.user.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * SysUserSearchReq 查询条件; 只含列表页真正参与过滤的字段
 * {@link com.github.link2fun.support.core.domain.entity.SysUser}
 */
@Data
public class SysUserSearchReq {

  /** 用户ID */
  private Long userId;

  /** 角色ID; 仅角色授权列表按角色过滤用户时使用 */
  private Long roleId;

  /** 部门ID */
  private Long deptId;

  /** 用户账号 */
  private String userName;

  /** 用户昵称 */
  private String nickName;

  /** 邮箱 */
  private String email;

  /** 手机号码 */
  private String phonenumber;

  /** 账号状态（0正常 1停用） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
