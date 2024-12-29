package com.github.link2fun.generator.util;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import lombok.Setter;

@Setter
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class CodeBuilder {

  public static final String PACKAGE_SEP = ".";
  public static final String DIR_SEP = "/";
  public static final String LINE_BREAK = "\n";
  public static final String TAB_2 = "  ";
  public static final String TAB_4 = "    ";


  private StringBuilder result;


  public static CodeBuilder newBuilder() {
    CodeBuilder codeBuilder = new CodeBuilder();
    codeBuilder.setResult(new StringBuilder());

    return codeBuilder;
  }


  public CodeBuilder tab_2() {
    this.result.append(TAB_2);
    return this;
  }

  public CodeBuilder tab_4() {
    result.append(TAB_4);
    return this;
  }

  public CodeBuilder space() {
    result.append(" ");
    return this;
  }

  public CodeBuilder packageSep() {
    result.append(".");
    return this;
  }

  public CodeBuilder lineBreak() {
    result.append(LINE_BREAK);
    return this;
  }

  public CodeBuilder oneLineComment(String content) {
    result.append("/** ");
    result.append(content);
    result.append(" */").append(LINE_BREAK);

    return this;
  }

  public CodeBuilder line(Boolean condition, String lineContent) {
    if (condition) {
      result.append(lineContent).append(LINE_BREAK);
    }
    return this;
  }

  public CodeBuilder line(String lineContent) {
    result.append(lineContent).append(LINE_BREAK);
    return this;
  }

  public CodeBuilder line(String... strs) {
    for (String str : strs) {
      result.append(str);
    }
    return this.lineBreak();
  }

  public CodeBuilder lineTemplate(String template, Object... params) {
    result.append(StrUtil.format(template, params));

    return this.lineBreak();
  }

  public CodeBuilder lineTemplate(Boolean condition, String template, Object... params) {
    if (condition) {
      result.append(StrUtil.format(template, params));
    }
    return this.lineBreak();
  }

  public CodeBuilder importClass(String packageInfo) {
    result.append("import ").append(packageInfo).append(";").append(LINE_BREAK);

    return this;
  }

  public CodeBuilder importClass(Boolean condition, String packageInfo) {
    if (condition) {
      result.append("import ").append(packageInfo).append(";").append(LINE_BREAK);
    }
    return this;
  }

  public CodeBuilder notBlank(String message) {
    result.append("@NotBlank(message = \"").append(message).append(" 不能为空\")").append(LINE_BREAK);
    return this;
  }

  public CodeBuilder notNull(String message) {
    result.append("@NotNull(message = \"").append(message).append(" 不能为空\")").append(LINE_BREAK);
    return this;
  }

  public CodeBuilder tableField(PDManerEntityField tableField) {
    result.append("@Column(value = \"").append(tableField.getDefKey()).append("\"");
    result.append(")").append(LINE_BREAK);
    return this;
  }

  public CodeBuilder append(String content) {
    result.append(content);
    return this;
  }

  public CodeBuilder append(boolean append, String content) {
    if (!append) {
      return this;
    }
    result.append(content);
    return this;
  }


  @Override
  public String toString() {
    return result.toString();
  }
}
