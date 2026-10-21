package com.github.link2fun.framework.aop;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 日志 JSON 树的脱敏工具：按 JSON 字段名判定敏感字段，命中即把值按级别打码。
 *
 * <p>脱敏作用于<b>实际序列化出的内容</b>，因此 Map 的 key、运行时子类多出的字段、
 * 自定义序列化器的输出都在覆盖范围内——不存在「声明类型干净就跳过」的旁路。
 * 字段名判定见 {@link SensitiveFields}。
 *
 * <p>打码不整段替换：保留首尾少量字符，便于按日志核对「是哪条记录」而不暴露完整值。
 * 保留位数由 {@link SensitiveFields.MaskLevel} 决定，见 {@link #maskValue}。
 */
final class LogMasker {

  /** 打码占位符：原文过短、或字段值不是标量（对象/数组）时整体替换 */
  static final String MASK = "***";

  /** 口令/密钥保留的首尾位数：核对价值低，只留各 1 位 */
  private static final int STRICT_PREFIX = 1;
  private static final int STRICT_SUFFIX = 1;

  /** 联系方式/证件/配置值保留的首尾位数：够看出是哪条记录，又不足以还原 */
  private static final int PARTIAL_PREFIX = 3;
  private static final int PARTIAL_SUFFIX = 4;

  /** 至少要有这么多字符被打码才用「部分保留」；否则保留部分会接近全长，退化为整体打码 */
  private static final int STRICT_MIN_MASKED_LEN = 6;
  private static final int PARTIAL_MIN_MASKED_LEN = 4;

  private LogMasker() {
  }

  /**
   * 逐字段判定并打码。
   *
   * @param excludeNames 需要整体剔除的字段名（{@code @Log.excludeParamNames()}）；非空时一并处理
   */
  static void mask(JsonNode node, Set<String> excludeNames) {
    if (node instanceof ObjectNode object) {
      Set<String> toRemove = null;
      // 遍历期间只做「同 key 覆盖」，结构变更统一收集到循环之后
      for (Map.Entry<String, JsonNode> field : object.properties()) {
        String name = field.getKey();
        if (excludeNames.contains(name)) {
          if (toRemove == null) {
            toRemove = new HashSet<>(4);
          }
          toRemove.add(name);
        } else {
          SensitiveFields.MaskLevel level = SensitiveFields.levelOf(name);
          if (level != SensitiveFields.MaskLevel.NONE) {
            object.set(name, maskValue(field.getValue(), level));
          } else {
            mask(field.getValue(), excludeNames);
          }
        }
      }
      if (toRemove != null) {
        object.remove(toRemove);
      }
    } else if (node.isArray()) {
      for (JsonNode child : node) {
        mask(child, excludeNames);
      }
    }
  }

  /**
   * 按敏感级别打码单个字段值。
   *
   * <ul>
   *   <li>{@code STRICT}（口令/密钥/令牌）：只留首尾各 1 位；</li>
   *   <li>{@code PARTIAL}（证件/联系方式/配置值）：保留前 3 位与后 4 位；</li>
   *   <li>数组：逐元素按同一级别打码。query/form 参数经 {@code MultiMap.toValuesMap()}
   *       后就是数组，整体替换会丢掉「保留前后几位」的信息；</li>
   *   <li>其它非字符串（数字、布尔、对象）：没有「前后几位」的语义，整体打码；</li>
   *   <li>原文太短、保留后被打码的位数不足时整体打码——否则「保留前后几位」几乎等于原文。
   *       口令要求至少打掉 6 位（短口令留首尾两位等于没打码），其余字段至少打掉 4 位。</li>
   * </ul>
   */
  private static JsonNode maskValue(JsonNode value, SensitiveFields.MaskLevel level) {
    if (value == null) {
      return TextNode.valueOf(MASK);
    }
    if (value.isTextual()) {
      return TextNode.valueOf(maskText(value.textValue(), level));
    }
    if (value.isArray()) {
      ArrayNode masked = JsonNodeFactory.instance.arrayNode();
      for (JsonNode item : value) {
        masked.add(item.isTextual() ? maskText(item.textValue(), level) : MASK);
      }
      return masked;
    }
    return TextNode.valueOf(MASK);
  }

  private static String maskText(String text, SensitiveFields.MaskLevel level) {
    boolean strict = level == SensitiveFields.MaskLevel.STRICT;
    int prefix = strict ? STRICT_PREFIX : PARTIAL_PREFIX;
    int suffix = strict ? STRICT_SUFFIX : PARTIAL_SUFFIX;
    int minMasked = strict ? STRICT_MIN_MASKED_LEN : PARTIAL_MIN_MASKED_LEN;
    if (text.length() - prefix - suffix < minMasked) {
      return MASK;
    }
    return StrUtil.hide(text, prefix, text.length() - suffix);
  }

  /**
   * 仅剔除指定字段（任意层级），不做脱敏。
   * 供 {@code @Log(mask=false)} 但声明了 {@code excludeParamNames} 的方法使用。
   */
  static void removeNames(JsonNode node, Set<String> excludeNames) {
    if (node instanceof ObjectNode object) {
      for (String name : excludeNames) {
        object.remove(name);
      }
      for (JsonNode child : object) {
        removeNames(child, excludeNames);
      }
    } else if (node.isArray()) {
      for (JsonNode child : node) {
        removeNames(child, excludeNames);
      }
    }
  }
}
