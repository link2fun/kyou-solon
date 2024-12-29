package com.github.link2fun.system.modular.user.model.req;

import com.github.link2fun.support.core.itf.Model;
import com.github.link2fun.support.xss.Xss;
import lombok.Data;
import org.noear.solon.validation.annotation.Email;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serializable;
import java.util.List;

public class SysUserReq {

  @Data
  public static class AddReq implements Model {
    /** 部门ID */
    @NotNull(message = "部门ID 不能为空")
    private Long deptId;

    /** 用户账号 */
    @Xss(message = "用户账号不能包含脚本字符")
    @Length(max = 30, message = "用户账号长度不能超过30个字符")
    private String userName;

    /** 用户昵称 */
    @Xss(message = "用户昵称不能包含脚本字符")
    @Length(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    /** 用户邮箱 */

    @Email(message = "邮箱格式不正确")
    @Length(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /** 手机号码 */

    @Length(max = 11, message = "手机号码长度不能超过11个字符")
    private String phonenumber;

    /** 用户性别 */

    private String sex;

    /** 用户头像 */
    private String avatar;

    /** 密码 */
    private String password;


    /** 角色组 */
    private List<Long> roleIds;

    /** 岗位组 */
    private List<Long> postIds;

    private String createBy;

    private String remark;
  }


  @Data
  public static class UpdateReq implements Model {
    /** 用户ID */
    @NotNull(message = "用户ID 不能为空")
    private Long userId;

    /** 登录名 */
    @NotBlank(message = "登录名不能为空")
    private String userName;

    private String nickName;

    /** 手机号 */
    private String phonenumber;

    /** 邮箱 */
    private String email;

    private String sex;

    private String status;

    private String remark;

    /** 更新人 */
    private String updateBy;

    private Long deptId;

    /** 角色 id list */
    private List<Long> roleIds;

    /** 岗位 id list */
    private List<Long> postIds;


  }


  public static class SearchReq implements Serializable {
    /** 角色ID */
    private Long roleId;
  }
}
