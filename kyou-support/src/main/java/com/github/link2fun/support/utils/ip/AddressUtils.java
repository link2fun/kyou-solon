package com.github.link2fun.support.utils.ip;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.link2fun.support.config.KyouConfig;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.context.cache.tool.CacheTool;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取地址类
 *
 * @author ruoyi
 */
public class AddressUtils {
  private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

  // IP地址查询
  public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

  // 未知地址
  public static final String UNKNOWN = "XX XX";


  /** 获取地址(带缓存) */
  public static String getRealAddressByIP(String ip) {
    return CacheTool.cacheGet("ip2address:", ip, ()-> _getRealAddressByIP(ip));
  }

  private static String _getRealAddressByIP(String ip) {
    // 内网不查询
    if (IpUtils.internalIp(ip)) {
      return "内网IP";
    }
    if (KyouConfig.addressEnabled()) {
      try {
        String rspStr = HttpUtils.sendGet(IP_URL, "ip=" + ip + "&json=true", Constants.GBK);
        if (StringUtils.isEmpty(rspStr)) {
          log.error("获取地理位置失败 {}", ip);
          return UNKNOWN;
        }
        JSONObject obj = JSONUtil.parseObj(rspStr);
        String region = obj.getStr("pro");
        String city = obj.getStr("city");
        return String.format("%s %s", region, city);
      } catch (Exception e) {
        log.error("获取地理位置异常 {}", ip);
      }
    }
    return UNKNOWN;
  }
}
