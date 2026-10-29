package com.github.link2fun.system.modular.group.service;

import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.group.model.req.SysGroupAddReq;
import com.github.link2fun.system.modular.group.model.req.SysGroupModifyReq;
import com.github.link2fun.system.modular.group.model.req.SysGroupSearchReq;

import java.util.List;

/** 系统群组 Service */
public interface ISystemGroupService {

  /**
   * 根据搜索条件分页查询系统群组信息。
   *
   * @param pageRequest 分页适配器
   * @param searchReq   搜索条件
   * @return 分页查询结果
   */
  <T> Page<T> pageSearchGroup(Page<T> pageRequest, SysGroupSearchReq searchReq, Class<T> resultClass);

  /**
   * 新增群组(内部已完成名称唯一性检查)
   *
   * @param addReq 新增用户组请求
   * @return 结果
   */
  long insertGroup(SysGroupAddReq addReq);

  /**
   * 修改群组(内部已完成名称唯一性检查)
   *
   * @param modifyReq 修改用户组请求
   * @return 结果
   */
  long updateGroup(SysGroupModifyReq modifyReq);

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
  <T> List<T> listAll(Class<T> resultClass);

  <T> T getById(Long groupId, Class<T> resultClass);
}
