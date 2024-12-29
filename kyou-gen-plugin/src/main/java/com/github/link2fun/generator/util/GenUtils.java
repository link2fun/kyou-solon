package com.github.link2fun.generator.util;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.config.GenConfig;
import com.github.link2fun.generator.domain.GenTable;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.support.constant.GenConstants;
import com.github.link2fun.support.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;

import java.util.Arrays;
import java.util.List;

/**
 * 代码生成器 工具类
 *
 * @author ruoyi
 */
@Slf4j
public class GenUtils {

  @Getter
  private static GenConfig genConfig;

  static {
    Solon.context().getBeanAsync(GenConfig.class, bw -> genConfig = bw);
  }


  /**
   * 初始化表信息
   */
  public static void initTable(GenTable genTable, String operName) {
    genTable.setClassName(convertClassName(genTable.getTableName()));
    genTable.setPackageName(genConfig.getPackageName());
    genTable.setModuleName(getModuleName(genConfig.getPackageName()));
    genTable.setBusinessName(getBusinessName(genTable.getTableName()));
    genTable.setFunctionName(replaceText(genTable.getTableComment()));
    genTable.setFunctionAuthor(genConfig.getAuthor());
    genTable.setCreateBy(operName);
  }

  /**
   * 初始化列属性字段
   */
  public static void initColumnField(GenTableColumn column, GenTable table) {
    String dataType = getDbType(column.getColumnType());
    String columnName = column.getColumnName();
    column.setTableId(table.getTableId());
    column.setCreateBy(table.getCreateBy());
    // 设置java字段名
    column.setJavaField(StringUtils.toCamelCase(columnName));
    // 设置默认类型
    if (StrUtil.isBlank(column.getJavaType())) {
      column.setJavaType(GenConstants.TYPE_STRING);
    }
    column.setQueryType(GenConstants.QUERY_EQ);

    if (arraysContains(GenConstants.COLUMNTYPE_STR, dataType) || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType)) {
      // 字符串长度超过500设置为文本域
      Integer columnLength = getColumnLength(column.getColumnType());
      String htmlType = columnLength >= 500 || arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType) ? GenConstants.HTML_TEXTAREA : GenConstants.HTML_INPUT;
      column.setHtmlType(htmlType);
    } else if (arraysContains(GenConstants.COLUMNTYPE_TIME, dataType)) {
      column.setJavaType(GenConstants.TYPE_DATE);
      column.setHtmlType(GenConstants.HTML_DATETIME);
    } else if (arraysContains(GenConstants.COLUMNTYPE_NUMBER, dataType)) {
      column.setHtmlType(GenConstants.HTML_INPUT);

      // 如果是浮点型 统一用BigDecimal
      List<String> str = StringUtils.split(org.apache.commons.lang3.StringUtils.substringBetween(column.getColumnType(), "(", ")"), ",");
      if (str != null && str.size() == 2 && Integer.parseInt(str.get(1)) > 0) {
        column.setJavaType(GenConstants.TYPE_BIGDECIMAL);
      }
      // 如果是整形
      else if (str != null && str.size() == 1 && Integer.parseInt(str.get(0)) <= 10) {
        column.setJavaType(GenConstants.TYPE_INTEGER);
      }
      // 长整形
      else {
        column.setJavaType(GenConstants.TYPE_LONG);
      }
    }

    // 插入字段（默认所有字段都需要插入）
    column.setIsInsert(GenConstants.REQUIRE);

    // 编辑字段
    if (!arraysContains(GenConstants.COLUMNNAME_NOT_EDIT, columnName) && !column.isPk()) {
      column.setIsEdit(GenConstants.REQUIRE);
    }
    // 列表字段
    if (!arraysContains(GenConstants.COLUMNNAME_NOT_LIST, columnName) && !column.isPk()) {
      column.setIsList(GenConstants.REQUIRE);
    }
    // 查询字段
    if (!arraysContains(GenConstants.COLUMNNAME_NOT_QUERY, columnName) && !column.isPk()) {
      column.setIsQuery(GenConstants.REQUIRE);
    }

    // 查询字段类型
    if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "name")) {
      column.setQueryType(GenConstants.QUERY_LIKE);
    }
    // 状态字段设置单选框
    if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "status")) {
      column.setHtmlType(GenConstants.HTML_RADIO);
    }
    // 类型&性别字段设置下拉框
    else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "type")
      || org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "sex")) {
      column.setHtmlType(GenConstants.HTML_SELECT);
    }
    // 图片字段设置图片上传控件
    else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "image")) {
      column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
    }
    // 文件字段设置文件上传控件
    else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "file")) {
      column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
    }
    // 内容字段设置富文本控件
    else if (org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(columnName, "content")) {
      column.setHtmlType(GenConstants.HTML_EDITOR);
    }
  }

  /**
   * 校验数组是否包含指定值
   *
   * @param arr         数组
   * @param targetValue 值
   * @return 是否包含
   */
  public static boolean arraysContains(String[] arr, String targetValue) {
    return Arrays.asList(arr).contains(targetValue);
  }

  /**
   * 获取模块名
   *
   * @param packageName 包名
   * @return 模块名
   */
  public static String getModuleName(String packageName) {
    int lastIndex = packageName.lastIndexOf(".");
    int nameLength = packageName.length();
    return StringUtils.substring(packageName, lastIndex + 1, nameLength);
  }

  /**
   * 获取业务名
   *
   * @param tableName 表名
   * @return 业务名
   */
  public static String getBusinessName(String tableName) {
    int lastIndex = tableName.lastIndexOf("_");
    int nameLength = tableName.length();
    return StringUtils.substring(tableName, lastIndex + 1, nameLength);
  }

  /**
   * 表名转换成Java类名
   *
   * @param tableName 表名称
   * @return 类名
   */
  public static String convertClassName(String tableName) {
    boolean autoRemovePre = genConfig.getAutoRemovePre();
    String tablePrefix = genConfig.getTablePrefix();
    if (autoRemovePre && StringUtils.isNotEmpty(tablePrefix)) {
      List<String> searchList = StringUtils.split(tablePrefix, ",");
      tableName = replaceFirst(tableName, searchList);
    }
    return StringUtils.convertToCamelCase(tableName);
  }

  /**
   * 批量替换前缀
   *
   * @param replacementm 替换值
   * @param searchList   替换列表
   */
  public static String replaceFirst(String replacementm, List<String> searchList) {
    String text = replacementm;
    for (String searchString : searchList) {
      if (replacementm.startsWith(searchString)) {
        text = replacementm.replaceFirst(searchString, "");
        break;
      }
    }
    return text;
  }

  /**
   * 关键字替换
   *
   * @param text 需要被替换的名字
   * @return 替换后的名字
   */
  public static String replaceText(String text) {
    return ReUtil.replaceAll(text, "(?:表|若依)", "");
  }

  /**
   * 获取数据库类型字段
   *
   * @param columnType 列类型
   * @return 截取后的列类型
   */
  public static String getDbType(String columnType) {
    if (StringUtils.indexOf(columnType, '(') > 0) {
      return org.apache.commons.lang3.StringUtils.substringBefore(columnType, "(");
    } else {
      return columnType;
    }
  }

  /**
   * 获取字段长度
   *
   * @param columnType 列类型
   * @return 截取后的列类型
   */
  public static Integer getColumnLength(String columnType) {
    if (StringUtils.indexOf(columnType, '(') > 0) {
      String length = org.apache.commons.lang3.StringUtils.substringBetween(columnType, "(", ")");
      return Integer.valueOf(length);
    } else {
      return 0;
    }
  }

  public static String getAntDesignProComponent(String htmlType) {
    if (StrUtil.isBlank(htmlType)) {
      return StrUtil.EMPTY;
    }
    switch (htmlType) {
      case "input":
        return "ProFormText";
      case "textarea":
        return "ProFormTextArea";
        case "select":
        return "ProFormSelect";
      case "radio":
        return "ProFormRadio.Group";
      case "checkbox":
        return "ProFormCheckbox.Group";
      case "date":
        return "ProFormDatePicker";
      case "time":
        return "ProFormTimePicker";
      case "datetime":
        return "ProFormDateTimePicker";

      default:
        log.warn("未找到对应的组件：{}", htmlType);

    }

    return StrUtil.EMPTY;
  }

  public static String getAntDesignProComponentImport(String htmlType) {
    String component = getAntDesignProComponent(htmlType);
    if (StrUtil.contains(component, ".")) {
      return StrUtil.subBefore(component, ".", true);
    }
    return component;
  }
}
