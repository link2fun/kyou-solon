package com.github.link2fun.system.modular.post.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.entity.SysPost;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.post.model.req.SysPostAddReq;
import com.github.link2fun.system.modular.post.model.req.SysPostModifyReq;
import com.github.link2fun.system.modular.post.model.req.SysPostSearchReq;
import com.github.link2fun.system.modular.post.service.ISystemPostService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;

/**
 * 岗位信息操作处理
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/post")
public class SystemPostController {
  @Inject
  private ISystemPostService postService;

  /**
   * 获取岗位列表
   */
  @SaCheckPermission("system:post:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysPostSearchReq searchReq) {
    final Page<SysPost> list = postService.pageSearch(Page.ofCurrentContext(), searchReq);
    return TableDataInfo.of(list);
  }

  @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:post:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysPostSearchReq searchReq) {
    List<SysPost> list = postService.selectPostList(searchReq);
    ExcelUtil<SysPost> util = new ExcelUtil<>(SysPost.class);
    util.exportExcel(response, list, "岗位数据");
  }

  /**
   * 根据岗位编号获取详细信息
   */
  @SaCheckPermission("system:post:query")
  @Mapping(value = "/{postId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long postId) {
    return AjaxResult.successData(postService.selectPostById(postId));
  }

  /**
   * 新增岗位
   */
  @SaCheckPermission("system:post:add")
  @Log(title = "岗位管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysPostAddReq addReq) {
    return AjaxResult.ofAffected(postService.insertPost(addReq));
  }

  /**
   * 修改岗位
   */
  @SaCheckPermission("system:post:edit")
  @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysPostModifyReq modifyReq) {
    return AjaxResult.ofAffected(postService.updatePost(modifyReq));
  }

  /**
   * 删除岗位
   */
  @SaCheckPermission("system:post:remove")
  @Log(title = "岗位管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{postIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> postIds) {
    return AjaxResult.ofAffected(postService.deletePostByIds(postIds));
  }

  /**
   * 获取岗位选择框列表
   */
  @Mapping(value = "/optionselect", method = MethodType.GET)
  public AjaxResult optionselect() {
    List<SysPost> posts = postService.selectPostAll();
    return AjaxResult.successData(posts);
  }
}
