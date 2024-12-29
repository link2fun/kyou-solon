package com.github.link2fun.support.core.controller;


import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * web层通用数据处理
 *
 * @author link2fun
 */
@Slf4j
public class BaseController {

  protected <T> Page<T> getPage() {
    return Page.ofCurrentContext();
  }

  /**
   * 响应请求分页数据
   */
  protected TableDataInfo getDataTable(Page<?> page) {
    TableDataInfo rspData = new TableDataInfo();
    rspData.setCode(HttpStatus.SUCCESS);
    rspData.setMsg("查询成功");
    rspData.setRows(page.getRecords());
    rspData.setTotal(page.getTotal());
    return rspData;
  }

  /**
   * 返回成功
   */
  public AjaxResult success() {
    return AjaxResult.success();
  }

  /**
   * 返回失败消息
   */
  public AjaxResult error() {
    return AjaxResult.error();
  }

  /**
   * 返回成功消息
   */
  public AjaxResult success(String message) {
    return AjaxResult.success(message);
  }

  /**
   * 返回成功消息
   */
  public AjaxResult success(Object data) {
    return AjaxResult.success(data);
  }

  /**
   * 返回失败消息
   */
  public AjaxResult error(String message) {
    return AjaxResult.error(message);
  }

  /**
   * 返回警告消息
   */
  public AjaxResult warn(String message) {
    return AjaxResult.warn(message);
  }

  /**
   * 响应返回结果
   *
   * @param rows 影响行数
   * @return 操作结果
   */
  protected AjaxResult toAjax(int rows) {
    return rows != 0 ? AjaxResult.success() : AjaxResult.error();
  }
  protected AjaxResult toAjax(long rows) {
    return rows != 0 ? AjaxResult.success() : AjaxResult.error();
  }

  /**
   * 响应返回结果
   *
   * @param result 结果
   * @return 操作结果
   */
  protected AjaxResult toAjax(boolean result) {
    return result ? success() : error();
  }

  /**
   * 页面跳转
   */
  public String redirect(String url) {
    return StringUtils.format("redirect:{}", url);
  }

  /**
   * 获取用户缓存信息
   */
  public SessionUser getCurrentUser() {
    return SecurityUtils.currentUser();
  }

  /**
   * 获取登录用户id
   */
  public Long getUserId() {
    return getCurrentUser().getUserId();
  }

  /**
   * 获取登录部门id
   */
  public Long getDeptId() {
    return getCurrentUser().getDeptId();
  }

  /**
   * 获取登录用户名
   */
  public String getUsername() {
    return getCurrentUser().getUsername();
  }
}
