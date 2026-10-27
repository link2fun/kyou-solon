package com.github.link2fun.system.modular.dict.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Pattern;

/**
 * {@link com.github.link2fun.support.core.domain.entity.SysDictType}
 * 修改字典类型请求
 */
@Data
public class SysDictTypeModifyReq {

  /** 字典主键 */
  @NotNull(message = "字典主键不能为空")
  private Long dictId;

  /** 字典名称 */
  @NotBlank(message = "字典名称不能为空")
  @Length(max = 100, message = "字典名称长度不能超过100个字符")
  private String dictName;

  /** 字典类型 */
  @NotBlank(message = "字典类型不能为空")
  @Length(max = 100, message = "字典类型长度不能超过100个字符")
  @Pattern(value = "[A-Za-z0-9_]+", message = "字典类型只能包含字母、数字、下划线")
  private String dictType;

  /** 状态（0正常 1停用） */
  private String status;

  /** 备注 */
  private String remark;

}
