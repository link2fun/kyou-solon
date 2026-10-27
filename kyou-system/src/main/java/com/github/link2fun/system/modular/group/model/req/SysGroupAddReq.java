package com.github.link2fun.system.modular.group.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * {@link com.github.link2fun.system.modular.group.model.SysGroup}
 * 新增群组请求
 */
@Data
public class SysGroupAddReq {

  /** 群组名称 */
  @NotBlank(message = "群组名称不能为空")
  @Length(max = 50, message = "群组名称长度不能超过50")
  private String groupName;

  /** 群组排序 */
  @NotNull(message = "群组排序不能为空")
  private Integer groupSort;

  /** 群组状态 */
  private String status;

}
