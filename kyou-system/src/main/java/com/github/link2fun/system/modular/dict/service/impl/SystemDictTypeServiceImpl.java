package com.github.link2fun.system.modular.dict.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.entity.SysDictData;
import com.github.link2fun.support.core.domain.entity.SysDictType;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.DictUtils;
import com.github.link2fun.system.modular.dict.service.ISystemDictDataService;
import com.github.link2fun.system.modular.dict.service.ISystemDictTypeService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemDictTypeServiceImpl implements ISystemDictTypeService {

  @Inject
  private ISystemDictTypeService self;

  @Inject
  private ISystemDictDataService dictDataService;


  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 项目启动时，初始化字典到缓存
   *
   * @link <a href="https://solon.noear.org/article/603">@Init 用法说明</a>
   */
  @Init
  public void init() {
    self.loadingDictCache();
  }


  /**
   * 根据条件分页查询字典类型
   *
   * @param searchReq 字典类型信息
   * @return 字典类型集合信息
   */
  @Override
  public List<SysDictType> selectDictTypeList(final SysDictType searchReq) {


    return entityQuery.queryable(SysDictType.class)
      .where(dictType -> dictType.dictName().like(StrUtil.isNotBlank(searchReq.getDictName()), searchReq.getDictName())) // 字典名称
      .where(dictType -> dictType.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus())) // 状态
      .where(dictType -> dictType.dictType().like(StrUtil.isNotBlank(searchReq.getDictType()), searchReq.getDictType())) // 字典类型
      .where(dictType -> dictType.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime())) // 开始时间
      .where(dictType -> dictType.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime())) // 结束时间
      .toList();

  }

  /**
   * 根据条件分页查询字典类型
   *
   * @param page      分页对象
   * @param searchReq 查询条件
   * @return 字典类型分页数据
   */
  @Override
  public Page<SysDictType> pageSearch(final Page<SysDictType> page, final SysDictType searchReq) {

    EasyPageResult<SysDictType> pageResult = entityQuery.queryable(SysDictType.class)
      .where(dictType -> dictType.dictName().like(StrUtil.isNotBlank(searchReq.getDictName()), searchReq.getDictName())) // 字典名称
      .where(dictType -> dictType.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus())) // 状态
      .where(dictType -> dictType.dictType().like(StrUtil.isNotBlank(searchReq.getDictType()), searchReq.getDictType())) // 字典类型
      .where(dictType -> dictType.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime())) // 开始时间
      .where(dictType -> dictType.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime())) // 结束时间
      .toPageResult(page.getPageNum(), page.getPageSize(),page.getTotal());
    return Page.of(pageResult);
  }

  /**
   * 根据所有字典类型
   *
   * @return 字典类型集合信息
   */
  @Override
  public List<SysDictType> selectDictTypeAll() {
    return entityQuery.queryable(SysDictType.class).toList();
  }

  /**
   * 根据字典类型查询字典数据
   *
   * @param dictType 字典类型
   * @return 字典数据集合信息
   */
  @Override
  public List<SysDictData> selectDictDataByType(final String dictType) {

    // 有缓存则取缓存
    final List<SysDictData> dictDataList = DictUtils.getDictCache(dictType);
    if (CollectionUtil.isNotEmpty(dictDataList)) {
      return dictDataList;
    }
    // 无缓存则查询数据库
    List<SysDictData> dictDataListFromDb = dictDataService.selectDictDataByType(dictType);
    if (CollectionUtil.isNotEmpty(dictDataListFromDb)) {
      DictUtils.setDictCache(dictType, dictDataListFromDb);
      return dictDataListFromDb;
    }

    return Lists.newArrayList();
  }

  /**
   * 根据字典类型ID查询信息
   *
   * @param dictId 字典类型ID
   * @return 字典类型
   */
  @Override
  public SysDictType selectDictTypeById(final Long dictId) {
    return entityQuery.queryable(SysDictType.class)
      .whereById(dictId)
      .singleOrNull();
  }

  /**
   * 根据字典类型查询信息
   *
   * @param dictType 字典类型
   * @return 字典类型
   */
  @Override
  public SysDictType selectDictTypeByType(final String dictType) {
    return entityQuery.queryable(SysDictType.class)
      .where(_dictType -> _dictType.dictType().eq(dictType))
      .singleOrNull();
  }

  /**
   * 批量删除字典信息
   *
   * @param dictIds 需要删除的字典ID
   */
  @Override
  public void deleteDictTypeByIds(final List<Long> dictIds) {

    final List<SysDictType> typeList = entityQuery.queryable(SysDictType.class)
      .whereByIds(dictIds)
      .toList();
    for (final SysDictType dictType : typeList) {
      if (dictDataService.existsByDictType(dictType.getDictType())) {
        log.error("字典类型{}存在字典数据，不允许删除", dictType.getDictType());
        throw new ServiceException("字典类型" + dictType.getDictType() + "存在字典数据，不允许删除");
      }
      entityQuery.deletable(SysDictType.class)
        .allowDeleteStatement(true)
        .whereById(dictType.getDictId())
        .executeRows();
      DictUtils.removeDictCache(dictType.getDictType());
    }

  }

  /**
   * 加载字典缓存数据
   */
  @Override
  public void loadingDictCache() {

    final SysDictData dictData = new SysDictData();
    dictData.setStatus(UserConstants.DICT_NORMAL);
    final List<SysDictData> dictDataList = dictDataService.selectDictDataList(dictData);
    dictDataList.stream()
      .sorted(Comparator.comparing(SysDictData::getDictSort))
      .collect(Collectors.groupingBy(SysDictData::getDictType))
      .forEach(DictUtils::setDictCache);
    log.info("字典缓存数据加载成功");
  }

  /**
   * 清空字典缓存数据
   */
  @Override
  public void clearDictCache() {
    DictUtils.clearDictCache();
  }

  /**
   * 重置字典缓存数据
   */
  @Override
  public void resetDictCache() {
    self.clearDictCache();
    self.loadingDictCache();
  }

  /**
   * 新增保存字典类型信息
   *
   * @param dictType 字典类型信息
   * @return 结果
   */
  @Override
  public long insertDictType(final SysDictType dictType) {
//    final int row = getBaseMapper().insert(dictType);
    long row = entityQuery.insertable(dictType).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    if (row > 0) {
      DictUtils.setDictCache(dictType.getDictName(), null);
    }
    return row;
  }

  /**
   * 修改保存字典类型信息
   *
   * @param dictTypeNew 字典类型信息
   * @return 结果
   */
  @Override
  public long updateDictType(final SysDictType dictTypeNew) {

//    final SysDictType dictTypeOld = getBaseMapper().selectById(dictTypeNew.getDictId());
    final SysDictType dictTypeOld = entityQuery.queryable(SysDictType.class)
      .whereById(dictTypeNew.getDictId())
      .singleNotNull();
    if (!StrUtil.equals(dictTypeOld.getDictType(), dictTypeNew.getDictType())) {

      dictDataService.updateDictDataType(dictTypeOld.getDictType(), dictTypeNew.getDictType());
    }

//    final int row = getBaseMapper().updateById(dictTypeNew);
    final long row = entityQuery.updatable(dictTypeNew).setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS).executeRows();
    if (row > 0) {
      final List<SysDictData> dataList = dictDataService.selectDictDataByType(dictTypeNew.getDictType());
      DictUtils.setDictCache(dictTypeNew.getDictType(), dataList);
    }

    return row;
  }

  /**
   * 校验字典类型称是否唯一
   *
   * @param dictType 字典类型
   * @return 结果
   */
  @Override
  public boolean checkDictTypeUnique(final SysDictType dictType) {

    final SysDictType dictTypeDb = entityQuery.queryable(SysDictType.class)
      .where(_dictType -> _dictType.dictType().eq(dictType.getDictType()))
      .singleOrNull();
    if (Objects.nonNull(dictTypeDb) && Objects.equals(dictTypeDb.getDictId(), dictType.getDictId())) {
      return UserConstants.UNIQUE;
    }

    return UserConstants.NOT_UNIQUE;
  }
}
