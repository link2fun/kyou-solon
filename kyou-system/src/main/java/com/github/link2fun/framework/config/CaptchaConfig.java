package com.github.link2fun.framework.config;

import com.github.link2fun.framework.enums.CaptchaInvadeType;
import com.github.link2fun.framework.enums.CaptchaType;
import lombok.Data;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

/**
 * 验证码配置
 *
 * @author xm
 * @link <a href="https://gitee.com/lcm742320521/qingzhou-boot/blob/master/qingzhou-framework/src/main/java/com/qingzhou/framework/config/CaptchaConfig.java">改自轻舟</a>
 */
@Data
@Configuration
@Inject(value = "${kyou.captcha}")
public class CaptchaConfig {

  /** 验证码开关 */
  private Boolean enabled;

  /** 干扰类型: line 线段干扰、circle 圆圈干扰、shear 扭曲干扰 */
  private CaptchaInvadeType invadeType;

  /** 验证码类型: math 四则运算、char 字符 */
  private CaptchaType type;

}
