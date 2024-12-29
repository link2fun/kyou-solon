package com.github.link2fun.support.core.page;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.link2fun.support.core.itf.Model;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.noear.solon.core.handle.Context;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页信息展示实体
 *
 * @param <T>
 */
@SuppressWarnings("unused")
public class Page<T> implements EasyPageResult<T>, Model {

  /**
   * 每页大小（默认10）
   */
  private static final String PAGE_SIZE_PARAM_NAME = "pageSize";

  /**
   * 第几页（从1开始）
   */
  private static final String PAGE_NO_PARAM_NAME = "pageNum";
  /**
   * 第几页（从1开始）为了兼容 ant design pro table
   */
  private static final String CURRENT_PARAM_NAME = "current";

  @Getter
  @Setter
  protected List<T> records = Lists.newArrayList();

  /**
   * 总数
   */
  @Getter
  @Setter
  protected long total = 0;
  /**
   * 每页显示条数，默认 10
   */
  @Getter
  @Setter
  protected long size = 10;

  /**
   * 当前页
   */
  @Getter
  @Setter
  protected long current = 1;

  @Getter
  @Setter
  protected long pages;

  @Getter
  @Setter
  private Boolean searchCount;

  public Page() {
    super();
  }

  public static <T> Page<T> ofCurrentContext() {
    return ofCurrentRequest();
  }


  public static <T> Page<T> ofCurrentRequest() {
    int pageSize = 10;
    int pageNum = 1;

    Context request = Context.current();

    //每页条数
    String pageSizeString = request.param(PAGE_SIZE_PARAM_NAME);
    if (ObjectUtil.isNotEmpty(pageSizeString)) {
      pageSize = Integer.parseInt(pageSizeString);
    }

    //第几页
    String pageNumString = request.param(PAGE_NO_PARAM_NAME);
    if (ObjectUtil.isNotEmpty(pageNumString)) {
      pageNum = Integer.parseInt(pageNumString);
    }
    String current = request.param(CURRENT_PARAM_NAME);
    if (ObjectUtil.isNotEmpty(current)) {
      pageNum = Integer.parseInt(current);
    }


    if (Context.current().param("totalRow") == null) {
      // 没有传总行数, 需要执行 count 查询
      return new Page<>(pageNum, pageSize,-1,true);
    }
    final long totalRow = Context.current().paramAsLong("totalRow");


    return new Page<>(pageNum, pageSize, totalRow, false);
  }



  /**
   * 静态构造方法
   *
   * @param pageNum  页码
   * @param pageSize 页大小
   * @param <T>      泛型
   * @return 页定义
   */
  public static <T> Page<T> of(long pageNum, long pageSize) {
    return new Page<>(pageNum, pageSize);
  }

  /**
   * 静态构造方法
   *
   * @param pageNum     页码
   * @param pageSize    页大小
   * @param searchCount 是否执行 count 搜索
   * @param <T>         泛型
   * @return 页定义
   */
  public static <T> Page<T> of(long pageNum, long pageSize, boolean searchCount) {
    return new Page<>(pageNum, pageSize, searchCount);
  }

  /**
   * 静态构造方法
   *
   * @param limitSize 限制返回条数
   * @param <T>       泛型
   * @return 页定义
   */
  public static <T> Page<T> ofLimit(long limitSize) {
    return of(1, limitSize, false);
  }

  public static <T> Page<T> ofAll() {
    Page<T> all = new Page<>();
    all.setSearchCount(false);
    return all;
  }


  /**
   * 分页构造函数
   *
   * @param current 当前页
   * @param size    每页显示条数
   */
  public Page(long current, long size) {
    this.current = current;
    this.size = size;
  }

  public Page(long current, long size, long total) {
    this.current = current;
    this.size = size;
    this.total = total;
  }

  public Page(long current, long size, boolean isSearchCount) {
    this.current = current;
    this.size = size;
    this.setSearchCount(isSearchCount);
  }

  public Page(long current, long size, long total, boolean isSearchCount) {
//    super(current, size, total, isSearchCount);
    this.current = current;
    this.size = size;
    this.total = total;
    this.setSearchCount(isSearchCount);
  }

  public static <T> Page<T> of(EasyPageResult<T> pageResult) {
    Page<T> page = new Page<>();
    page.setRecords(pageResult.getData());
    page.setTotal(pageResult.getTotal());
    return page;
  }

  /**
   * 页大小
   *
   * @return 页大小
   */
  public long getPageSize() {
    return getSize();
  }

  /**
   * 页码
   *
   * @return 页码
   */
  public long getPageNum() {
    return getCurrent();
  }

  /** 是否有下一页 */
  public Boolean getHasMore() {
    return getPages() > getCurrent();
  }


  /**
   * 分页数据转换
   *
   * @param mapper 转换器
   * @param <R>    目标类型泛型
   * @return 转换结果
   */
  @SuppressWarnings("unused")
  public <R> Page<R> convertRecords(Function<T, R> mapper) {
    List<R> dataList = getRecords().stream().map(mapper).collect(Collectors.toList());

    Page<R> result = new Page<>();
    result.setRecords(dataList);
    result.setTotal(this.total);
    result.setSize(this.size);
    result.setCurrent(this.current);
//    result.setOrders(this.orders);
//        result.setSearchCount(this.isSearchCount);
//    result.setOptimizeCountSql(this.optimizeCountSql);
//        result.setHitCount(this.hitCount);

    return result;
  }


  /**
   * 找到 第一条数据 可为空
   *
   * @return 符合条件的数据
   */

  public T firstNullable() {
    if (CollectionUtil.isEmpty(getRecords())) {
      return null;
    }

    return records.get(0);

  }

  /**
   * 找到 第一条数据
   *
   * @return 第一条数据
   */
  public Optional<T> firstOpt() {
    if (CollectionUtil.isEmpty(getRecords())) {
      return Optional.empty();
    }

    return records.stream().filter(Objects::nonNull).findFirst();
  }


  public Page<T> buildPage(final List<T> allList) {
    PageUtil.setFirstPageNo(1);
    final int start = PageUtil.getStart((int) getPageNum(), (int) getPageSize());
    final int end = PageUtil.getEnd((int) getPageNum(), (int) getPageSize());

    setTotal(allList.size());
    final List<T> records = CollectionUtil.sub(allList, start, end);
    setRecords(records);
    return this;

  }

  public <R> Page<R> convert(List<R> records) {
    final Page<R> pageAdapter = new Page<>();
    pageAdapter.setCurrent(getCurrent());
    pageAdapter.setSize(getSize());
    pageAdapter.setTotal(getTotal());
    pageAdapter.setRecords(records);
    return pageAdapter;
  }

  public <R> Page<R> convertToPage(List<R> records) {
    return convert(records);
  }

  @JsonIgnore
  @Override
  public List<T> getData() {
    return getRecords();
  }
}
