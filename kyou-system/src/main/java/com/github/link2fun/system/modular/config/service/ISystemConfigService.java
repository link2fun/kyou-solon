package com.github.link2fun.system.modular.config.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.config.model.SysConfig;
import com.github.link2fun.system.modular.config.model.req.SysConfigAddReq;
import com.github.link2fun.system.modular.config.model.req.SysConfigModifyReq;
import com.github.link2fun.system.modular.config.model.req.SysConfigSearchReq;

import java.util.List;

/** 参数配置 服务层 */
public interface ISystemConfigService {
  /**
   * 查询参数配置信息
   *
   * @param configId    参数配置ID
   * @param resultClass 结果类型
   * @return 参数配置信息
   */
  <T> T findOneByConfigId(Integer configId, Class<T> resultClass);

  /**
   * 根据键名查询参数配置信息
   *
   * @param configKey 参数键名
   * @return 参数键值
   */
  String selectConfigByKey(String configKey);

  /**
   * 查询参数配置列表
   *
   * @param searchReq   查询条件
   * @param resultClass 结果类型
   * @return 参数配置集合
   */
  <T> List<T> selectConfigList(SysConfigSearchReq searchReq, Class<T> resultClass);

  /** 分页查询 参数配置 */
  <T> Page<T> pageSearchConfig(Page<T> pageRequest, SysConfigSearchReq searchReq, Class<T> resultClass);

  /**
   * 新增参数配置
   *
   * @param addReq 新增参数配置请求
   * @return 结果
   */
  long insertConfig(SysConfigAddReq addReq);

  /**
   * 修改参数配置
   *
   * @param modifyReq 修改参数配置请求
   * @return 结果
   */
  long updateConfig(SysConfigModifyReq modifyReq);

  /**
   * 批量删除参数信息
   *
   * @param configIds 需要删除的参数ID
   */
  void deleteConfigByIds(List<Long> configIds);

  /**
   * 重置参数缓存数据
   */
  void resetConfigCache();

  /** 列出数据库中所有的配置 */
  List<SysConfig> listAll();

}
