package com.github.link2fun.support.xss;

import org.noear.solon.annotation.Note;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})   //只让它作用到参数，不管作用在哪，最终都是对Context的校验
@Retention(RetentionPolicy.RUNTIME)
public @interface Xss {

  @Note("保持默认")  //用Note注解，是为了用时还能看到这个注释
  String value() default "";

  String message() default "";
}
