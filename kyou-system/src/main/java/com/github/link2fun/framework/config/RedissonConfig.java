package com.github.link2fun.framework.config;

import cn.dev33.satoken.solon.dao.SaTokenDaoOfRedissonJackson;
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
import org.redisson.solon.RedissonSupplier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * solon 集成 Redisson 配置
 *
 * @link <a href="https://solon.noear.org/article/533">redisson-solon-plugin</a>
 */
@Configuration
public class RedissonConfig {

  @Bean(typed = true)
  public ObjectMapper objectMapper() {
    // 参照 cn.dev33.satoken.solon.dao.SaTokenDaoOfRedissonJackson 修改 redissonClient 序列化方式
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);


    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JavaTimeModule timeModule = new JavaTimeModule();
    timeModule.addSerializer(new LocalDateTimeSerializer(SaTokenDaoOfRedissonJackson.DATE_TIME_FORMATTER));
    timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(SaTokenDaoOfRedissonJackson.DATE_TIME_FORMATTER));
    timeModule.addSerializer(new LocalDateSerializer(SaTokenDaoOfRedissonJackson.DATE_FORMATTER));
    timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(SaTokenDaoOfRedissonJackson.DATE_FORMATTER));
    timeModule.addSerializer(new LocalTimeSerializer(SaTokenDaoOfRedissonJackson.TIME_FORMATTER));
    timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(SaTokenDaoOfRedissonJackson.TIME_FORMATTER));
    objectMapper.registerModule(timeModule);
    return objectMapper;
  }


  @Bean(value = "redissonClient", typed = true)
  public RedissonClient redissonClient(@Inject("${kyou.redis}") RedissonSupplier supplier, @Inject ObjectMapper objectMapper) {

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
