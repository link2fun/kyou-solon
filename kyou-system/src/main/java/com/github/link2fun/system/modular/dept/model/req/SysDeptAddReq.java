package com.github.link2fun.system.modular.dept.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.Email;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * {@link com.github.link2fun.support.core.domain.entity.SysDept}
 * 新增部门请求; 只含新增真正写入的字段, 审计字段由服务端填充
 */
@Data
public class SysDeptAddReq {

  /** 父部门ID */
  @NotNull(message = "上级部门不能为空")
  private Long parentId;

  /** 部门名称 */
  @NotBlank(message = "部门名称不能为空")
  @Length(max = 30, message = "部门名称长度不能超过30个字符")
  private String deptName;

  /** 显示顺序 */
  @NotNull(message = "显示顺序不能为空")
  private Integer orderNum;

  /** 负责人 */
  private String leader;

  /** 联系电话 */
  @Length(max = 11, message = "联系电话长度不能超过11个字符")
  private String phone;

  /** 邮箱 */
  @Email(message = "邮箱格式不正确")
  @Length(max = 50, message = "邮箱长度不能超过50个字符")
  private String email;

  /** 部门状态（0正常 1停用） */
  private String status;

}
