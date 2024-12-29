package com.github.link2fun.generator.domain.pdmaner.dto.viewgroup;

import cn.hutool.core.util.StrUtil;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleInfo {

  private String moduleCode;

  /** 模块的中文名 */
  private String moduleDesc;

  /** 模块的驼峰简写模式。以 basedoc 为例，结果为 Bd */
  private String moduleCodeUpperCamelShort;

  /** 模块编码驼峰命名首字母大写。以 basedoc 为例 结果为 BaseDoc */
  private String moduleCodeUpperCamel;

  /** 模块编码 驼峰命名 首字母小写。以 basedoc 为例 结果为 baseDoc */
  private String moduleCodeLowerCamel;

  /** 模块编码 小写名称 全部字母小写。以 basedoc 为例 结果为 basedoc */
  private String packageName;

  /** 前端用的Dir */
  private String dirName;

  public String getDirName() {
    if (StrUtil.isBlankIfStr(dirName)) {
      return getPackageName();
    }
    return dirName;
  }


}
