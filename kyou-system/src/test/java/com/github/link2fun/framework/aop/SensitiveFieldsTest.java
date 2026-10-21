package com.github.link2fun.framework.aop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 敏感字段判定语义：按字段全名/驼峰分段匹配，不是子串包含。
 *
 * <p>这里钉住两件事：
 * <ul>
 *   <li>复合词形（{@code newPassword}、{@code userPhone}）不能漏；</li>
 *   <li>{@code passengerCount}、{@code phoneModel} 这类「含敏感子串但不是敏感字段」不能误伤。</li>
 * </ul>
 */
class SensitiveFieldsTest {

  @Test
  @DisplayName("敏感词作为完整词段出现时必须命中")
  void matchesWholeSegments() {
    String[] names = {
      // 口令
      "password", "passwd", "pwd", "newPassword", "oldPwd", "confirm_password", "passwordEncoder",
      // 强词不限位置：pwdExpireDays 也要打码（判定方向偏保守）
      "pwdExpireDays",
      // 令牌 / 密钥
      "token", "tokenId", "accessToken", "refresh_token", "access_token", "clientSecret", "secretKey", "credentials",
      // 配置项的值：内容由管理员自由填写，可能放 OSS/邮件/短信密钥
      "configValue", "config_value",
      // 证件
      "idCard", "idCardNo", "identityCard",
      // 联系方式
      "phone", "phonenumber", "phoneNumber", "phone_number", "userPhone", "contactMobile", "mobileNo",
      "email", "userEmail", "emailAddress", "login_email", "telephone"
    };
    for (String name : names) {
      assertTrue(SensitiveFields.isSensitive(name), name + " 应判定为敏感");
    }
  }

  @Test
  @DisplayName("只是包含敏感子串、本身不是敏感字段的不能误伤")
  void ignoresSubstringOnly() {
    String[] names = {
      "passengerCount", "phoneModel", "emailNotificationEnabled", "emailVerified",
      "userName", "deptId", "remark", "status", "configKey", "orderNum", "createTime", "avatar"
    };
    for (String name : names) {
      assertFalse(SensitiveFields.isSensitive(name), name + " 不应判定为敏感");
    }
  }

  @Test
  @DisplayName("null / 空串安全")
  void handlesNullAndEmpty() {
    assertFalse(SensitiveFields.isSensitive(null));
    assertFalse(SensitiveFields.isSensitive(""));
  }
}
