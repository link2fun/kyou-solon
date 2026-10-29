package com.github.link2fun.system.modular.dict.service;

import com.github.link2fun.support.core.domain.dto.SysDictDataDTO;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.dict.model.req.SysDictDataAddReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictDataModifyReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictDataSearchReq;

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
   * @param searchReq 字典数据查询条件
   * @return 字典数据集合信息
   */
  <T> List<T> selectDictDataList(SysDictDataSearchReq searchReq, Class<T> resultClass);

  /**
   * 根据条件查询字典数据并分页展示。
   *
   * @param pageRequest 分页适配器
   * @param searchReq   字典数据查询条件
   * @return 分页结果
   */
  <T> Page<T> pageSearch(Page<T> pageRequest, SysDictDataSearchReq searchReq, Class<T> resultClass);

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
  <T> T selectDictDataById(Long dictCode, Class<T> resultClass);

  /**
   * 批量删除字典数据信息
   *
   * @param dictCodeList 需要删除的字典数据ID
   */
  void deleteDictDataByIds(List<Long> dictCodeList);

  /**
   * 新增保存字典数据信息
   *
   * @param addReq 新增字典数据请求
   * @return 结果
   */
  long insertDictData(SysDictDataAddReq addReq);

  /**
   * 修改保存字典数据信息
   *
   * @param modifyReq 修改字典数据请求
   * @return 结果
   */
  long updateDictData(SysDictDataModifyReq modifyReq);

  /**
   * 根据字典类型查询字典数据
   *
   * @param dictType 字典类型
   * @return 字典数据集合信息
   */
  List<SysDictDataDTO> selectDictDataByType(String dictType);

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
