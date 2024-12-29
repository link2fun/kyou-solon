package com.github.link2fun.framework.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.link2fun.support.annotation.RepeatSubmit;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.support.core.text.Convert;
import com.github.link2fun.support.utils.StringUtils;
import org.noear.solon.core.handle.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交拦截器</br>
 * 同一个url数据相同判断为重复提交</br>
 * @link  <a href="https://solon.noear.org/article/35">八、切面与环绕拦截（AOP）</a>
 */
public class SameUrlDataInterceptor extends RepeatSubmitInterceptor {

  public final String REPEAT_PARAMS = "repeatParams";

  public final String REPEAT_TIME = "repeatTime";

  public SameUrlDataInterceptor(final RedisCache redisCache) {
    super(redisCache);
  }

  /**
   * 判断是否重复提交
   *
   * @param context      上下文
   * @param repeatSubmit 重复提交注解
   * @return 如果重复提交返回true，否则返回false
   */
  @Override
  protected boolean isRepeatSubmit(final Context context, final RepeatSubmit repeatSubmit) {

    try {
      String nowParams = context.body();

      // body参数为空，获取Parameter的数据
      if (StringUtils.isEmpty(nowParams)) {
        nowParams = JSONUtil.toJsonStr(context.paramMap().toValuesMap());
      }
      Map<String, Object> nowDataMap = new HashMap<>();
      nowDataMap.put(REPEAT_PARAMS, nowParams);
      nowDataMap.put(REPEAT_TIME, System.currentTimeMillis());

      // 请求地址（作为存放cache的key值）
      String url = context.path();

      // 唯一值（没有消息头则使用请求地址）
      String submitKey = StrUtil.emptyToDefault(Convert.toStr(StpUtil.getLoginIdDefaultNull()), "");

      // 唯一标识（指定key + url + 消息头）
      String cacheRepeatKey = CacheConstants.REPEAT_SUBMIT_KEY + url + submitKey;

      Map<String, Object> sessionObj = redisCache.get(cacheRepeatKey);
      if (sessionObj != null) {
        if (sessionObj.containsKey(url)) {
          Map<String, Object> preDataMap = (Map<String, Object>) sessionObj.get(url);
          if (compareParams(nowDataMap, preDataMap) && compareTime(nowDataMap, preDataMap, repeatSubmit.interval())) {
            return true;
          }
        }
      }
      Map<String, Object> cacheMap = new HashMap<>();
      cacheMap.put(url, nowDataMap);
      redisCache.set(cacheRepeatKey, cacheMap, repeatSubmit.interval(),
        TimeUnit.MILLISECONDS.toChronoUnit());
      return false;
    } catch (IOException e) {
      return false;
    }

  }

  /**
   * 判断参数是否相同
   */
  private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap) {
    String nowParams = (String) nowMap.get(REPEAT_PARAMS);
    String preParams = (String) preMap.get(REPEAT_PARAMS);
    return nowParams.equals(preParams);
  }

  /**
   * 判断两次间隔时间
   */
  private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval) {
    long time1 = (Long) nowMap.get(REPEAT_TIME);
    long time2 = (Long) preMap.get(REPEAT_TIME);
    if ((time1 - time2) < interval) {
      return true;
    }
    return false;
  }
}
