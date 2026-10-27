package com.github.link2fun.system.modular.dict.service;

import com.github.link2fun.support.core.domain.entity.SysDictData;
import com.github.link2fun.support.core.domain.entity.SysDictType;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.dict.model.req.SysDictTypeAddReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictTypeModifyReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictTypeSearchReq;

import java.util.List;

/**
 * 字典 业务层
 *
 * @author ruoyi
 */
public interface ISystemDictTypeService {
  /**
   * 根据条件分页查询字典类型
   *
   * @param searchReq 字典类型查询条件
   * @return 字典类型集合信息
   */
  List<SysDictType> selectDictTypeList(SysDictTypeSearchReq searchReq);

  /**
   * 根据条件分页查询字典类型
   *
   * @param pageRequest 分页对象
   * @param searchReq   查询条件
   * @return 字典类型分页数据
   */
  Page<SysDictType> pageSearch(Page<SysDictType> pageRequest, SysDictTypeSearchReq searchReq);

  /**
   * 根据所有字典类型
   *
   * @return 字典类型集合信息
   */
  List<SysDictType> selectDictTypeAll();

  /**
   * 根据字典类型查询字典数据
   *
   * @param dictType 字典类型
   * @return 字典数据集合信息
   */
  List<SysDictData> selectDictDataByType(String dictType);

  /**
   * 根据字典类型ID查询信息
   *
   * @param dictId 字典类型ID
   * @return 字典类型
   */
  SysDictType selectDictTypeById(Long dictId);

  /**
   * 根据字典类型查询信息
   *
   * @param dictType 字典类型
   * @return 字典类型
   */
  SysDictType selectDictTypeByType(String dictType);

  /**
   * 批量删除字典信息
   *
   * @param dictIds 需要删除的字典ID
   */
  void deleteDictTypeByIds(List<Long> dictIds);

  /**
   * 加载字典缓存数据
   */
  void loadingDictCache();

  /**
   * 清空字典缓存数据
   */
  void clearDictCache();

  /**
   * 重置字典缓存数据
   */
  void resetDictCache();

  /**
   * 新增保存字典类型信息
   *
   * @param addReq 新增字典类型请求
   * @return 结果
   */
  long insertDictType(SysDictTypeAddReq addReq);

  /**
   * 修改保存字典类型信息
   *
   * @param modifyReq 修改字典类型请求
   * @return 结果
   */
  long updateDictType(SysDictTypeModifyReq modifyReq);

}
