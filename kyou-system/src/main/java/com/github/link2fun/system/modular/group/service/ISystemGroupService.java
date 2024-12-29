package com.github.link2fun.system.modular.group.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.group.model.SysGroup;


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
   * 检测群组名称是否唯一
   *
   * @param groupName 群组名称
   * @param groupId   群组ID
   * @return 是否唯一
   */
  boolean checkGroupNameUnique(String groupName, final Long groupId);

  SysGroup getById(Long groupId);
}
