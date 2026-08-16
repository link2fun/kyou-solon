package com.github.link2fun.framework.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.github.link2fun.framework.service.impl.RedissonCacheImpl;
import com.github.link2fun.support.context.cache.service.RedisCache;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.cache.redisson.RedissonCacheService;
import org.noear.solon.data.cache.CacheService;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.solon.RedissonClientOriginalSupplier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * solon 集成 Redisson 配置
 *
 * @link <a href="https://solon.noear.org/article/533">redisson-solon-plugin</a>
 */
@Configuration
public class RedissonConfig {


  public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static final String DATE_PATTERN = "yyyy-MM-dd";
  public static final String TIME_PATTERN = "HH:mm:ss";
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);


  @Bean(typed = true)
  public ObjectMapper objectMapper() {
    // 参照 cn.dev33.satoken.solon.dao.SaTokenDaoOfRedissonJackson 修改 redissonClient 序列化方式
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);


    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JavaTimeModule timeModule = new JavaTimeModule();
    timeModule.addSerializer(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
    timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
    timeModule.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
    timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
    timeModule.addSerializer(new LocalTimeSerializer(TIME_FORMATTER));
    timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
    objectMapper.registerModule(timeModule);
    return objectMapper;
  }


  @Bean(value = "redissonClient", typed = true)
  public RedissonClient redissonClient(@Inject("${kyou.redis}") RedissonClientOriginalSupplier supplier, @Inject ObjectMapper objectMapper) {

    return supplier
      .withConfig(config -> config.setCodec(new JsonJacksonCodec(objectMapper)))
      .get();
  }

  @Bean
  public CacheService cacheService(@Inject RedissonClient redissonClient) {
    return new RedissonCacheService(redissonClient, 30);
  }


  @Bean(typed = true)
  public RedisCache redisCache(@Inject RedissonClient redissonClient) {
    return new RedissonCacheImpl(redissonClient);
  }

}
