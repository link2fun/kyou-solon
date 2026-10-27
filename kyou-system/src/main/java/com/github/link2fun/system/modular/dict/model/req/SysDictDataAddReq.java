package com.github.link2fun.system.modular.dict.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * {@link com.github.link2fun.support.core.domain.entity.SysDictData}
 * 新增字典数据请求
 */
@Data
public class SysDictDataAddReq {

  /** 字典排序 */
  @NotNull(message = "字典排序不能为空")
  private Long dictSort;

  /** 字典标签 */
  @NotBlank(message = "字典标签不能为空")
  @Length(max = 100, message = "字典标签长度不能超过100个字符")
  private String dictLabel;

  /** 字典键值 */
  @NotBlank(message = "字典键值不能为空")
  @Length(max = 100, message = "字典键值长度不能超过100个字符")
  private String dictValue;

  /** 字典类型 */
  @NotBlank(message = "字典类型不能为空")
  @Length(max = 100, message = "字典类型长度不能超过100个字符")
  private String dictType;

  /** 样式属性 */
  @Length(max = 100, message = "样式属性长度不能超过100个字符")
  private String cssClass;

  /** 表格回显样式 */
  private String listClass;

  /** 是否默认（Y是 N否） */
  private String isDefault;

  /** 状态（0正常 1停用） */
  private String status;

  /** 备注 */
  private String remark;

}
