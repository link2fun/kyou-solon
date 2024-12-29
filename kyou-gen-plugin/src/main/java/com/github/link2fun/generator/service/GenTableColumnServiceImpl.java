package com.github.link2fun.generator.service;


import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.support.core.text.Convert;
import org.noear.solon.annotation.Component;

import java.util.List;

/**
 * 业务字段 服务层实现
 *
 * @author link2fun
 */
@Component
public class GenTableColumnServiceImpl implements IGenTableColumnService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 查询业务字段列表
   *
   * @param tableId 业务字段编号
   * @return 业务字段集合
   */
  @Override
  public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId) {
    return entityQuery.queryable(GenTableColumn.class)
      .where(tableColumn -> tableColumn.tableId().eq(tableId))
      .orderBy(tableColumn -> tableColumn.sort().asc())
      .toList();

  }

  /**
   * 新增业务字段
   *
   * @param genTableColumn 业务字段信息
   * @return 结果
   */
  @Override
  public long insertGenTableColumn(List<GenTableColumn> genTableColumn) {
    return entityQuery.insertable(genTableColumn)
      .batch()
      .setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS)
      .executeRows(true);
  }

  /**
   * 修改业务字段
   *
   * @param genTableColumn 业务字段信息
   * @return 结果
   */
  @Override
  public long updateGenTableColumn(List<GenTableColumn> genTableColumn) {
    return entityQuery.updatable(genTableColumn)
      .batch()
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
      .executeRows();
  }

  /**
   * 删除业务字段对象
   *
   * @param ids 需要删除的数据ID
   * @return 结果
   */
  @Override
  public long deleteGenTableColumnByIds(String ids) {
    return entityQuery.deletable(GenTableColumn.class)
      .allowDeleteStatement(true)
      .where(tableColumn -> tableColumn.columnId().in(Convert.toLongArray(ids)))
      .executeRows();
  }

  @Override
  public List<GenTableColumn> selectDbTableColumnsByName(String tableName) {
    return entityQuery.sqlQuery("""
        select \s
          column_name,\s
          (case when (is_nullable = 'no' && column_key != 'PRI') then '1' else '0' end) as is_required,\s
          (case when column_key = 'PRI' then '1' else '0' end) as is_pk,\s
          ordinal_position as sort,\s
          column_comment,\s
          (case when extra = 'auto_increment' then '1' else '0' end) as is_increment,\s
          column_type
        from information_schema.columns\s
        where table_schema = (select database()) and table_name = ?
        order by ordinal_position""",
      GenTableColumn.class, List.of(tableName));

  }

  @Override
  public void deleteGenTableColumns(List<GenTableColumn> delColumns) {
    entityQuery.deletable(GenTableColumn.class)
      .allowDeleteStatement(true)
      .where(tableColumn -> tableColumn.columnId().in(delColumns.stream().map(GenTableColumn::getColumnId).toList()))
      .executeRows();
  }
}
