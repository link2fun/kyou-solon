package com.github.link2fun.system.modular.dict.service;

import com.github.link2fun.support.core.domain.entity.SysDictData;
import com.github.link2fun.support.core.page.Page;


import java.util.List;

/**
 * 字典 业务层
 *
 * @author ruoyi
 */
public interface ISystemDictDataService {
  /**
   * 根据条件分页查询字典数据
   *
   * @param dictData 字典数据信息
   * @return 字典数据集合信息
   */
  List<SysDictData> selectDictDataList(SysDictData dictData);

  /**
   * 根据条件查询字典数据并分页展示。
   *
   * @param page     分页适配器
   * @param dictData 字典数据对象
   * @return 分页结果
   */
  Page<SysDictData> pageSearch(Page<SysDictData> page, SysDictData dictData);

  /**
   * 根据字典类型和字典键值查询字典数据信息
   *
   * @param dictType  字典类型
   * @param dictValue 字典键值
   * @return 字典标签
   */
  String selectDictLabel(String dictType, String dictValue);

  /**
   * 根据字典数据ID查询信息
   *
   * @param dictCode 字典数据ID
   * @return 字典数据
   */
  SysDictData selectDictDataById(Long dictCode);

  /**
   * 批量删除字典数据信息
   *
   * @param dictCodeList 需要删除的字典数据ID
   */
  void deleteDictDataByIds(List<Long> dictCodeList);

  /**
   * 新增保存字典数据信息
   *
   * @param dictData 字典数据信息
   * @return 结果
   */
  long insertDictData(SysDictData dictData);

  /**
   * 修改保存字典数据信息
   *
   * @param dictData 字典数据信息
   * @return 结果
   */
  long updateDictData(SysDictData dictData);

  /**
   * 根据字典类型查询字典数据
   *
   * @param dictType 字典类型
   * @return 字典数据集合信息
   */
  List<SysDictData> selectDictDataByType(String dictType);

  /**
   * 根据字典类型查询字典数据是否存
   *
   * @param dictType 字典类型
   * @return 是否存在
   */
  boolean existsByDictType(String dictType);

  /**
   * 同步修改字典类型
   *
   * @param dictTypeOld 旧字典类型
   * @param dictTypeNew 新旧字典类型
   * @return 结果
   */
  boolean updateDictDataType(String dictTypeOld, String dictTypeNew);

}
