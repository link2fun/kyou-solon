package com.github.link2fun.framework.config.easyquery;

import cn.hutool.core.util.IdUtil;
import com.easy.query.core.basic.extension.generated.PrimaryKeyGenerator;
import com.easy.query.core.metadata.ColumnMetadata;
import org.noear.solon.annotation.Component;

import java.io.Serializable;
import java.util.Objects;

@Component
public class SnowflakePrimaryKeyGenerator implements PrimaryKeyGenerator {
  /**
   * 返回一个主键
   */
  @Override
  public Serializable getPrimaryKey() {
    return IdUtil.getSnowflakeNextIdStr();
  }

  @Override
  public void setPrimaryKey(Object entity, ColumnMetadata columnMetadata) {
    Object primaryKey = columnMetadata.getGetterCaller().apply(entity);
    if (Objects.isNull(primaryKey)) {
      // 只有没有主动赋值的时候, 才自动赋值
       columnMetadata.getSetterCaller().call(entity, getPrimaryKey());
    }
  }
}
