package com.github.link2fun.framework.config;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

  //typed=true，表示默认数据源。@Db 可不带名字注入
  @Bean(value = "dataSource_default", typed = true)
  public DataSource dataSourceDefault(@Inject("${datasource.default}") HikariDataSource ds) {
    return ds;
  }


}
