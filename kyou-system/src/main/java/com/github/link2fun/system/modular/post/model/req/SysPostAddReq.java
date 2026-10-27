package com.github.link2fun.system.modular.post.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 新增岗位请求
 * {@link com.github.link2fun.support.core.domain.entity.SysPost}
 */
@Data
public class SysPostAddReq {

  /** 岗位编码 */
  @NotBlank(message = "岗位编码不能为空")
  @Length(max = 64, message = "岗位编码长度不能超过64个字符")
  private String postCode;

  /** 岗位名称 */
  @NotBlank(message = "岗位名称不能为空")
  @Length(max = 50, message = "岗位名称长度不能超过50个字符")
  private String postName;

  /** 显示顺序 */
  @NotNull(message = "显示顺序不能为空")
  private Integer postSort;

  /** 状态（0正常 1停用） */
  private String status;

  /** 备注 */
  private String remark;

}
