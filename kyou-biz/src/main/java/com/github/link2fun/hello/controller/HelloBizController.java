package com.github.link2fun.hello.controller;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;

@Mapping(path = "/hello")
@Controller
public class HelloBizController {


  @Mapping(path = "/biz")
  public String biz() {
    return "Hello Biz!";
  }
}
