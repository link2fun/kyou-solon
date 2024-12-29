package com.github.link2fun.system.modular.dict.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.entity.SysDictData;
import com.github.link2fun.support.core.domain.entity.proxy.SysDictDataProxy;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.utils.DictUtils;
import com.github.link2fun.system.modular.dict.service.ISystemDictDataService;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemDictDataServiceImpl implements ISystemDictDataService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 根据条件分页查询字典数据
   *
   * @param searchReq 字典数据信息
   * @return 字典数据集合信息
   */
  @Override
  public List<SysDictData> selectDictDataList(final SysDictData searchReq) {

    return entityQuery.queryable(SysDictData.class)
      .where(dictData -> dictData.dictType().eq(StrUtil.isNotBlank(searchReq.getDictType()), searchReq.getDictType())) // 字典类型
      .where(dictData -> dictData.dictLabel().like(StrUtil.isNotBlank(searchReq.getDictLabel()), searchReq.getDictLabel())) // 字典标签
      .where(dictData -> dictData.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus())) // 状态
      .orderBy(dictData -> dictData.dictSort().asc()) // 字典排序
      .toList();
  }

  /**
   * 根据条件查询字典数据并分页展示。
   *
   * @param page     分页适配器
   * @param dictData 字典数据对象
   * @return 分页结果
   */
  @Override
  public Page<SysDictData> pageSearch(final Page<SysDictData> page, final SysDictData dictData) {
    if (java.util.Objects.isNull(dictData)) {
      EasyPageResult<SysDictData> pageResult = entityQuery.queryable(SysDictData.class)
        .toPageResult(page.getPageNum(), page.getPageSize());
      return Page.of(pageResult);
    }

    EasyPageResult<SysDictData> pageResult = entityQuery.queryable(SysDictData.class)
      .where(dictDataQuery -> dictDataQuery.dictType().eq(StrUtil.isNotBlank(dictData.getDictType()), dictData.getDictType())) // 字典类型
      .where(dictDataQuery -> dictDataQuery.dictLabel().like(StrUtil.isNotBlank(dictData.getDictLabel()), dictData.getDictLabel())) // 字典标签
      .where(dictDataQuery -> dictDataQuery.status().eq(StrUtil.isNotBlank(dictData.getStatus()), dictData.getStatus())) // 状态
      .orderBy(dictDataQuery -> {
        dictDataQuery.dictType().asc();
        dictDataQuery.dictSort().asc();
      }) // 字典排序
      .toPageResult(page.getPageNum(), page.getPageSize(),page.getTotal());
    return Page.of(pageResult);
  }

  /**
   * 根据字典类型和字典键值查询字典数据信息
   *
   * @param dictType  字典类型
   * @param dictValue 字典键值
   * @return 字典标签
   */
  @Override
  public String selectDictLabel(final String dictType, final String dictValue) {

    return entityQuery.queryable(SysDictData.class)
      .where(dictData -> dictData.dictType().eq(dictType))
      .where(dictData -> dictData.dictValue().eq(dictValue))
      .select(SysDictDataProxy::dictLabel)
      .singleOrNull();
  }

  /**
   * 根据字典数据ID查询信息
   *
   * @param dictId 字典数据ID
   * @return 字典数据
   */
  @Override
  public SysDictData selectDictDataById(final Long dictId) {
//    return getBaseMapper().selectById(dictId);
    return entityQuery.queryable(SysDictData.class)
      .whereById(dictId)
      .singleOrNull();
  }

  /**
   * 批量删除字典数据信息
   *
   * @param dictIdList 需要删除的字典数据ID
   */
  @Override
  public void deleteDictDataByIds(final List<Long> dictIdList) {

//    final List<SysDictData> dictDataList = getBaseMapper().selectBatchIds(dictIdList);
    final List<SysDictData> dictDataList = entityQuery.queryable(SysDictData.class)
      .whereByIds(dictIdList).toList();

    final List<String> dictTypeList = dictDataList.stream().map(SysDictData::getDictType).distinct()
      .collect(Collectors.toList());

    // 直接删除所有的字典数据
    final List<Long> dictCodeList = dictDataList.stream().map(SysDictData::getDictCode).distinct()
      .collect(Collectors.toList());
//    getBaseMapper().deleteBatchIds(dictCodeList);
    entityQuery.deletable(SysDictData.class)
      .allowDeleteStatement(true)
      .whereByIds(dictCodeList)
      .executeRows();

    // 然后重新加载字典缓存
    final List<SysDictData> dictDataListForCache = entityQuery.queryable(SysDictData.class)
      .where(dictData -> dictData.dictType().in(dictTypeList))
      .toList();


    // 按照类型进行分组
    final Map<String, List<SysDictData>> dictDataMap = dictDataListForCache.stream()
      .collect(Collectors.groupingBy(SysDictData::getDictType));
    // 先移除缓存
    dictTypeList.forEach(DictUtils::removeDictCache);
    // 依次放入到缓存中
    dictDataMap.forEach(DictUtils::setDictCache);

  }

  /**
   * 新增保存字典数据信息
   *
   * @param dictData 字典数据信息
   * @return 结果
   */
  @Override
  public long insertDictData(final SysDictData dictData) {
    final long row = entityQuery.insertable(dictData).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    if (row > 0) {
      final List<SysDictData> dataList = entityQuery.queryable(SysDictData.class)
        .where(dictDataQuery -> dictDataQuery.dictType().eq(dictData.getDictType()))
        .toList();
      DictUtils.setDictCache(dictData.getDictType(), dataList);
    }
    return row;
  }

  /**
   * 修改保存字典数据信息
   *
   * @param dictData 字典数据信息
   * @return 结果
   */
  @Override
  public long updateDictData(final SysDictData dictData) {
//    final int row = getBaseMapper().updateById(dictData);
    final long row = entityQuery.updatable(dictData).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    if (row > 0) {
//      final List<SysDictData> dataList = lambdaQuery().eq(SysDictData::getDictType, dictData.getDictType()).list();
      final List<SysDictData> dataList = entityQuery.queryable(SysDictData.class)
        .where(dictDataQuery -> dictDataQuery.dictType().eq(dictData.getDictType()))
        .toList();
      DictUtils.setDictCache(dictData.getDictType(), dataList);
    }
    return row;
  }

  /**
   * 根据字典类型查询字典数据
   *
   * @param dictType 字典类型
   * @return 字典数据集合信息
   */
  @Override
  public List<SysDictData> selectDictDataByType(final String dictType) {
    return entityQuery.queryable(SysDictData.class)
      .where(dictData -> dictData.dictType().eq(dictType))
      .where(dictData -> dictData.status().eq(UserConstants.DICT_NORMAL))
      .orderBy(dictData -> dictData.dictSort().asc())
      .toList();
  }

  /**
   * 根据字典类型查询字典数据是否存
   *
   * @param dictType 字典类型
   * @return 是否存在
   */
  @Override
  public boolean existsByDictType(final String dictType) {
    return entityQuery.queryable(SysDictData.class)
      .where(dictData -> dictData.dictType().eq(dictType))
      .any();
  }

  /**
   * 同步修改字典类型
   *
   * @param dictTypeOld 旧字典类型
   * @param dictTypeNew 新旧字典类型
   * @return 结果
   */
  @Override
  public boolean updateDictDataType(final String dictTypeOld, final String dictTypeNew) {
    return entityQuery.updatable(SysDictData.class)
      .where(dictData -> dictData.dictType().eq(dictTypeOld))
      .setColumns(dictData -> dictData.dictType().set(dictTypeNew))
      .executeRows() > 0;
  }

}
