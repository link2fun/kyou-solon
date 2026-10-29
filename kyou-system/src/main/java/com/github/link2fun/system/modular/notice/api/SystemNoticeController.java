package com.github.link2fun.system.modular.notice.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.system.modular.notice.model.dto.SysNoticeDTO;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeAddReq;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeModifyReq;
import com.github.link2fun.system.modular.notice.model.req.SysNoticeSearchReq;
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
public class SystemNoticeController {
  @Inject
  private ISystemNoticeService noticeService;

  /**
   * 获取通知公告列表
   */
  @SaCheckPermission("system:notice:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysNoticeSearchReq searchReq) {
    Page<SysNoticeDTO> list = noticeService.pageSearch(Page.ofCurrentContext(), searchReq, SysNoticeDTO.class);
    return TableDataInfo.of(list);
  }

  /**
   * 根据通知公告编号获取详细信息
   */
  @SaCheckPermission("system:notice:query")
  @Mapping(value = "/{noticeId}", method = MethodType.GET)
  public Resp<SysNoticeDTO> getInfo(@Path Long noticeId) {
    return Resp.successData(noticeService.selectNoticeById(noticeId, SysNoticeDTO.class));
  }

  /**
   * 新增通知公告
   */
  @SaCheckPermission("system:notice:add")
  @Log(title = "通知公告", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public Resp<Void> add(@Validated @Body SysNoticeAddReq addReq) {
    return Resp.ofAffected(noticeService.insertNotice(addReq));
  }

  /**
   * 修改通知公告
   */
  @SaCheckPermission("system:notice:edit")
  @Log(title = "通知公告", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysNoticeModifyReq modifyReq) {
    return Resp.ofAffected(noticeService.updateNotice(modifyReq));
  }

  /**
   * 删除通知公告
   */
  @SaCheckPermission("system:notice:remove")
  @Log(title = "通知公告", businessType = BusinessType.DELETE)
  @Mapping(value = "/{noticeIds}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path List<Long> noticeIds) {
    return Resp.ofAffected(noticeService.deleteNoticeByIds(noticeIds));
  }
}
