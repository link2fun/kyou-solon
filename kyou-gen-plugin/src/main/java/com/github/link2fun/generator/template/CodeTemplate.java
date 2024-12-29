package com.github.link2fun.generator.template;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;

public interface CodeTemplate {

  default boolean use(String templateCode) {
    return StrUtil.equals(getTemplateCode(), templateCode);
  }

  default String source() {
    return "db" ;
  }

  /** 获取模板索引 */
  Integer getTemplateIndex();

  /** 获取模板编码 */
  String getTemplateCode();

  /** 获取模板名称 */
  String getTemplateName();

  /** 获取渲染结果 */
  String getTemplateResult(TableInfo tableInfo);

  String getFilePath(TableInfo tableInfo);
}
