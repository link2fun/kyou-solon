package com.github.link2fun.system.modular.notice.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.system.modular.notice.model.SysNotice;
import com.github.link2fun.system.modular.notice.service.ISystemNoticeService;

import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;


/**
 * 公告 信息操作处理
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/notice")
public class SystemNoticeController extends BaseController {
  @Inject
  private ISystemNoticeService noticeService;

  /**
   * 获取通知公告列表
   */
  @SaCheckPermission("system:notice:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysNotice searchReq) {
    Page<SysNotice> list = noticeService.pageSearch(Page.ofCurrentContext(), searchReq);
    return getDataTable(list);
  }

  /**
   * 根据通知公告编号获取详细信息
   */
  @SaCheckPermission("system:notice:query")
  @Mapping(value = "/{noticeId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long noticeId) {
    return success(noticeService.selectNoticeById(noticeId));
  }

  /**
   * 新增通知公告
   */
  @SaCheckPermission("system:notice:add")
  @Log(title = "通知公告", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysNotice notice) {
    notice.setCreateBy(getUsername());
    return toAjax(noticeService.insertNotice(notice));
  }

  /**
   * 修改通知公告
   */
  @SaCheckPermission("system:notice:edit")
  @Log(title = "通知公告", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysNotice notice) {
    notice.setUpdateBy(getUsername());
    return toAjax(noticeService.updateNotice(notice));
  }

  /**
   * 删除通知公告
   */
  @SaCheckPermission("system:notice:remove")
  @Log(title = "通知公告", businessType = BusinessType.DELETE)
  @Mapping(value = "/{noticeIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> noticeIds) {
    return toAjax(noticeService.deleteNoticeByIds(noticeIds));
  }
}
