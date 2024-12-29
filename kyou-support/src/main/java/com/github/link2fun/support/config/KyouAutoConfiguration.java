package com.github.link2fun.support.config;

import com.github.link2fun.support.xss.Xss;
import com.github.link2fun.support.xss.XssValidator;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.validation.ValidatorManager;


@Configuration
public class KyouAutoConfiguration {

  @Bean(typed = true)
  public KyouProperties ruoyiProperties(@Inject("${kyou}") KyouProperties config) {
    return config;
  }

  @Bean
  public void xssValidator() {
    //
    // 此处为注册验证器。如果有些验证器重写了，也是在此处注册
    //
    ValidatorManager.register(Xss.class, new XssValidator());
  }
}
