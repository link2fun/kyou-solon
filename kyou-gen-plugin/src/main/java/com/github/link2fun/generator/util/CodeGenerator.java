package com.github.link2fun.generator.util;

import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.template.CodeTemplate;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CodeGenerator {


  @Getter
  private static final List<CodeTemplate> handlers = Lists.newLinkedList();

  static {
    Solon.context().subBeansOfType(CodeTemplate.class, handlers::add);
  }

  public static List<String> getAllTemplateCode() {
    return handlers.stream().map(CodeTemplate::getTemplateCode).collect(Collectors.toList());
  }


  public static String generate(TableInfo tableInfo, String templateCode) {
    for (CodeTemplate handler : handlers) {
      if (handler.use(templateCode)) {
        return handler.getTemplateResult(tableInfo);
      }
    }
    return "未匹配到模板";
  }


  public static String getFilePath(TableInfo tableInfo, String templateCode) {
    for (CodeTemplate handler : handlers) {
      if (handler.use(templateCode)) {
        return handler.getFilePath(tableInfo);
      }
    }
    return "";
  }
}
