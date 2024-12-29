package com.github.link2fun.framework.service;

import cn.hutool.captcha.*;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.framework.config.CaptchaConfig;
import com.github.link2fun.framework.dto.CaptchaDTO;
import com.github.link2fun.framework.enums.CaptchaInvadeType;
import com.github.link2fun.framework.enums.CaptchaType;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.support.web.api.ApiExceptions;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.temporal.ChronoUnit;

/**
 * 验证码处理
 *
 * @author xm
 * @link <a href="https://gitee.com/lcm742320521/qingzhou-boot/blob/master/qingzhou-framework/src/main/java/com/qingzhou/framework/service/CaptchaService.java">改自轻舟</a>
 */
@Component
public class CaptchaService {

  /** 验证码宽度 */
  private static final int CAPTCHA_WIDTH = 160;
  /** 验证码高度 */
  private static final int CAPTCHA_HEIGHT = 60;

  @Inject
  CaptchaConfig captchaConfig;

  @Inject
  RedisCache redisCache;

  /** 获取验证码 */
  public CaptchaDTO getCaptcha() {
    CaptchaDTO captchaDTO = new CaptchaDTO();
    boolean captchaOnOff = captchaConfig.getEnabled();
    captchaDTO.setCaptchaEnabled(captchaOnOff);

    if (!captchaOnOff) {
      return captchaDTO;
    }

    // 如果开启了验证码，生成验证码
    CaptchaInvadeType invadeType = captchaConfig.getInvadeType();
    // 干扰类型
    if (invadeType == CaptchaInvadeType.circle) {
      // 圆圈干扰验证码（宽、高、验证码字符数、干扰元素个数）
      CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, 4, 10);
      createCaptcha(circleCaptcha, captchaDTO);
    } else if (invadeType == CaptchaInvadeType.shear) {
      // 扭曲干扰验证码（宽、高、验证码字符数、干扰线宽度）
      ShearCaptcha shearCaptcha = CaptchaUtil.createShearCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, 4, 5);
      createCaptcha(shearCaptcha, captchaDTO);
    } else {
      // 线段干扰验证码（宽、高、验证码字符数、干扰元素个数）
      LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, 4, 15);
      createCaptcha(lineCaptcha, captchaDTO);
    }
    return captchaDTO;
  }

  /**
   * 校验验证码
   */
  public void checkCaptcha(String code, String uuid) {
    // 如果开启了验证码
    if (captchaConfig.getEnabled()) {
      if (!StrUtil.isAllNotEmpty(code, uuid)) {
        throw ApiExceptions.CODE_400("验证码必填");
      }
      String codeKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
      // 校验

      String cacheCode = redisCache.get(codeKey);
      if (StrUtil.isEmpty(cacheCode)) {
        throw ApiExceptions.CODE_400("验证码已失效");
      }
      redisCache.del(codeKey);
      if (!StrUtil.equalsIgnoreCase(code, cacheCode)) {
        throw ApiExceptions.CODE_400("验证码错误");
      }
    }
  }

  /**
   * 生成验证码
   */
  private <T extends AbstractCaptcha> void createCaptcha(T t, CaptchaDTO captchaDTO) {
    String uuid = IdUtil.simpleUUID();
    String codeKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
    // 验证码类型
    CaptchaType type = captchaConfig.getType();
    // 验证码的内容（如果是字符串类型，则验证码的内容就是结果）
    String code = t.getCode();
    // 如果是四则运算
    if (CaptchaType.MATH == type) {
      t.setGenerator(new MathGenerator(1));
      // 重新生成code
      t.createCode();
      // 运算结果
      int result = (int) Calculator.conversion(t.getCode());
      code = String.valueOf(result);
    }
    String img = t.getImageBase64();

    // 将结果放入缓存
    redisCache.set(codeKey, code, CacheConstants.CAPTCHA_CODE_EXPIRATION, ChronoUnit.MINUTES);

    captchaDTO.setUuid(uuid);
    captchaDTO.setImg(img);

  }

}
