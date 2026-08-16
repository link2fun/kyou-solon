package com.github.link2fun.system.modular.group.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.group.model.SysGroup;

import java.util.List;

/** 系统群组 Service */
public interface ISystemGroupService {

  /**
   * 根据搜索条件分页查询系统群组信息。
   *
   * @param page      分页适配器
   * @param searchReq 搜索条件
   * @return 分页查询结果
   */
  Page<SysGroup> pageSearchGroup(Page<SysGroup> page, SysGroup searchReq);

  /**
   * 新增群组(内部已完成名称唯一性检查)
   *
   * @param group 群组信息
   * @return 结果
   */
  long insertGroup(SysGroup group);

  /**
   * 修改群组(内部已完成名称唯一性检查)
   *
   * @param group 群组信息
   * @return 结果
   */
  long updateGroup(SysGroup group);

  /**
   * 批量删除群组
   *
   * @param groupIds 群组ID列表
   * @return 结果
   */
  long deleteGroupByIds(List<Long> groupIds);

  /**
   * 查询所有群组
   *
   * @return 群组列表
   */
  List<SysGroup> listAll();

  SysGroup getById(Long groupId);
}
