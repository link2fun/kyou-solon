package com.github.link2fun.framework.aop;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 敏感字段判定：按「字段全名」匹配，不做子串正则。
 *
 * <p>语义是「字段名本身是敏感词」，而不是「字段名里含敏感子串」：子串匹配会把
 * {@code passengerCount}、{@code phoneModel}、{@code emailNotificationEnabled} 打成
 * 敏感字段，日志失去排查价值。分三级判定：
 * <ol>
 *   <li>归一化全名（小写、去掉 {@code _ - . 空格}）命中全名集合，
 *       覆盖 {@code phoneNumber}、{@code idCardNo}、{@code access_token} 这类连写；</li>
 *   <li>按驼峰/分隔符切成词段，任一段命中强词集合，
 *       覆盖 {@code newPassword}、{@code oldPwd}、{@code accessToken}、{@code tokenId}；</li>
 *   <li>弱词（{@code phone}/{@code mobile}/{@code tel}/{@code email}/{@code mail}）只在
 *       「末段」或「后接限定词」时命中，覆盖 {@code userPhone}、{@code phoneNumber}，
 *       放过 {@code phoneModel}、{@code emailNotificationEnabled}。</li>
 * </ol>
 *
 * <p>判定结果分两档，决定日志里保留多少原文字符（见 {@link MaskLevel}）：
 * 口令/密钥核对价值低、泄漏代价高，只留首尾各一位；联系方式/证件/配置值需要按日志
 * 核对「是哪个人、哪条配置」，保留前三位与后四位。
 *
 * <p>判定方向始终偏保守：命中即打码，漏判比误伤更不可接受。强词不做位置限制
 * （{@code pwdExpireDays} 也会被打码），这是有意为之。
 */
final class SensitiveFields {

  private SensitiveFields() {
  }

  /** 打码强度：决定日志里保留多少原文字符 */
  enum MaskLevel {
    /** 不敏感，原样输出 */
    NONE,
    /** 口令/密钥/令牌：只留首尾各一位 */
    STRICT,
    /** 证件/联系方式/配置值：保留前三位与后四位，便于核对 */
    PARTIAL
  }

  /** 归一化后属于「口令/密钥/令牌」的完整字段名 */
  private static final Set<String> SECRET_FULL_NAMES = Set.of(
    // 口令
    "password", "passwd", "pwd", "pass",
    "passwordencoder", "rawpassword", "plainpassword", "encryptedpassword", "encodedpassword",
    // 令牌 / 密钥
    "token", "tokenid", "accesstoken", "refreshtoken", "idtoken", "authtoken", "bearertoken",
    "apikey", "apisecret", "secret", "secretkey", "clientsecret", "privatekey",
    "credential", "credentials"
  );

  /** 归一化后属于「证件/联系方式/配置值」的完整字段名 */
  private static final Set<String> PARTIAL_FULL_NAMES = Set.of(
    // 配置项的值：内容由管理员自由填写，常放 OSS/邮件/短信密钥，只能整体按敏感处理
    "configvalue",
    // 证件
    "idcard", "idcardno", "idcardnumber", "identitycard", "identityno",
    // 联系方式：这些写法里弱词后面跟的不是「限定词」，靠全名兜住
    "phone", "phonenumber", "phoneno", "mobile", "mobileno", "mobilephone",
    "telephone", "tel", "contactphone",
    "email", "emailaddress", "mail"
  );

  /** 出现即按口令/密钥处理的「词段」，不限制位置 */
  private static final Set<String> SECRET_SEGMENTS = Set.of(
    "password", "passwd", "pwd", "secret", "token", "credential", "credentials"
  );

  /** 出现即按证件处理的「词段」 */
  private static final Set<String> PARTIAL_SEGMENTS = Set.of("idcard");

  /** 弱词：只有在末段、或后接限定词时才算敏感 */
  private static final Set<String> TRAILING_SEGMENTS = Set.of("phone", "mobile", "tel", "email", "mail");

  /** 允许跟在弱词后面的限定词：phoneNumber / emailAddress / mobileNo */
  private static final Set<String> QUALIFIERS = Set.of("number", "no", "num", "address", "addr", "code", "id");

  /** 词段数量上限，超过按敏感处理（正常字段名不会这么长） */
  private static final int MAX_SEGMENTS = 8;

  /**
   * 字段名 -> 敏感级别。
   *
   * <p>请求 DTO 的字段名是有限集合，缓存命中率极高；query 参数名则由客户端控制，
   * 所以必须有容量上限，否则可以被海量随机参数名撑爆。
   */
  private static final ConcurrentHashMap<String, MaskLevel> CACHE = new ConcurrentHashMap<>();
  private static final AtomicInteger CACHE_SIZE = new AtomicInteger();
  private static final int CACHE_LIMIT = 4096;

  /**
   * 字段名是否敏感。
   *
   * @param fieldName JSON 字段名（Jackson 序列化后的名字，可能来自 {@code @JsonProperty}）
   */
  static boolean isSensitive(String fieldName) {
    return levelOf(fieldName) != MaskLevel.NONE;
  }

  /**
   * 字段名的敏感级别，决定日志里的打码强度。
   *
   * @param fieldName JSON 字段名（Jackson 序列化后的名字，可能来自 {@code @JsonProperty}）
   */
  static MaskLevel levelOf(String fieldName) {
    if (fieldName == null || fieldName.isEmpty()) {
      return MaskLevel.NONE;
    }
    MaskLevel cached = CACHE.get(fieldName);
    if (cached != null) {
      return cached;
    }
    MaskLevel level = compute(fieldName);
    if (CACHE_SIZE.get() < CACHE_LIMIT && CACHE.putIfAbsent(fieldName, level) == null) {
      CACHE_SIZE.incrementAndGet();
    }
    return level;
  }

  private static MaskLevel compute(String fieldName) {
    // 绝大多数字段名本身就是小写连写，直接命中，省掉一次归一化分配
    MaskLevel direct = fullNameLevel(fieldName);
    if (direct != MaskLevel.NONE) {
      return direct;
    }
    MaskLevel normalized = fullNameLevel(normalize(fieldName));
    if (normalized != MaskLevel.NONE) {
      return normalized;
    }
    return matchSegments(fieldName);
  }

  private static MaskLevel fullNameLevel(String name) {
    if (SECRET_FULL_NAMES.contains(name)) {
      return MaskLevel.STRICT;
    }
    if (PARTIAL_FULL_NAMES.contains(name)) {
      return MaskLevel.PARTIAL;
    }
    return MaskLevel.NONE;
  }

  /** 小写化并去掉常见分隔符 */
  private static String normalize(String name) {
    int len = name.length();
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char c = name.charAt(i);
      if (c == '_' || c == '-' || c == '.' || c == ' ') {
        continue;
      }
      sb.append(Character.toLowerCase(c));
    }
    return sb.toString();
  }

  /** 按驼峰与分隔符切词，逐段判定；强词优先于证件词与弱词 */
  private static MaskLevel matchSegments(String name) {
    int len = name.length();
    String[] segments = new String[MAX_SEGMENTS];
    int count = 0;
    int start = 0;
    boolean prevLower = false;

    for (int i = 0; i <= len; i++) {
      char c = i < len ? name.charAt(i) : 0;
      boolean end = i == len;
      boolean separator = !end && (c == '_' || c == '-' || c == '.' || c == ' ');
      // 驼峰边界：大写字母属于「下一段」，所以切点落在 i 上而不是 i+1
      boolean camel = !end && !separator && prevLower && Character.isUpperCase(c);

      if (end || separator || camel) {
        if (i > start) {
          if (count == MAX_SEGMENTS) {
            // 词段过多，保守按口令处理
            return MaskLevel.STRICT;
          }
          segments[count++] = name.substring(start, i).toLowerCase(Locale.ROOT);
        }
        start = separator ? i + 1 : i;
        prevLower = false;
        continue;
      }
      prevLower = Character.isLowerCase(c) || Character.isDigit(c);
    }

    for (int i = 0; i < count; i++) {
      if (SECRET_SEGMENTS.contains(segments[i])) {
        return MaskLevel.STRICT;
      }
    }
    for (int i = 0; i < count; i++) {
      if (PARTIAL_SEGMENTS.contains(segments[i])) {
        return MaskLevel.PARTIAL;
      }
    }
    for (int i = 0; i < count; i++) {
      if (!TRAILING_SEGMENTS.contains(segments[i])) {
        continue;
      }
      if (i == count - 1) {
        // userPhone / contactEmail
        return MaskLevel.PARTIAL;
      }
      if (QUALIFIERS.contains(segments[i + 1])) {
        // phoneNumber / emailAddress
        return MaskLevel.PARTIAL;
      }
    }
    return MaskLevel.NONE;
  }
}
