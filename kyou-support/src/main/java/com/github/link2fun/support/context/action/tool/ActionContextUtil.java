package com.github.link2fun.support.context.action.tool;


import com.github.link2fun.support.context.action.service.ActionContextService;
import org.noear.solon.Solon;

public class ActionContextUtil {

  static ActionContextService contextService;

  static {
    Solon.context().getBeanAsync(ActionContextService.class, bw -> contextService = bw);
  }


}
