package com.github.link2fun.support.core.page;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.link2fun.support.core.itf.Model;
import com.github.link2fun.support.exception.ServiceException;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    Context request = Context.current();

    //每页条数
    final int pageSize = parsePageParam(request.param(PAGE_SIZE_PARAM_NAME), 10, 1, MAX_PAGE_SIZE);

    //第几页（current 为 ant design pro table 兼容参数, 后传者优先）
    int pageNum = parsePageParam(request.param(PAGE_NO_PARAM_NAME), 1, 1, Integer.MAX_VALUE);
    final String current = request.param(CURRENT_PARAM_NAME);
    if (ObjectUtil.isNotEmpty(current)) {
      pageNum = parsePageParam(current, 1, 1, Integer.MAX_VALUE);
    }


    if (request.param("totalRow") == null) {
      // 没有传总行数, 需要执行 count 查询
      return new Page<>(pageNum, pageSize, -1, true);
    }
    final long totalRow = request.paramAsLong("totalRow");


    return new Page<>(pageNum, pageSize, totalRow, false);
  }

  /** 每页大小允许的上限, 前端「选全部」场景(SystemRoleAuthUser.tsx)会传 10000 */
  private static final int MAX_PAGE_SIZE = 10_000;

  /**
   * 解析请求分页参数, 非数字或越界视为非法请求
   *
   * @param raw 原始参数值
   * @param def 参数缺省时的默认值
   * @param min 允许的最小值(含)
   * @param max 允许的最大值(含)
   * @return 解析后的参数值
   */
  private static int parsePageParam(final String raw, final int def, final int min, final int max) {
    if (ObjectUtil.isEmpty(raw)) {
      return def;
    }
    final int value;
    try {
      value = Integer.parseInt(raw);
    } catch (NumberFormatException e) {
      throw new ServiceException("分页参数非法: " + raw);
    }
    if (value < min || value > max) {
      throw new ServiceException("分页参数非法: " + raw);
    }
    return value;
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

  /** 「取全部」单次返回的条数上限, 顶到上限时 {@link #of(Page, EasyPageResult)} 抛异常拒绝 */
  public static final long ALL_SIZE = 100_000;

  /**
   * 静态构造方法: 取全部数据, 总数未知(total=-1)
   *
   * @param <T> 泛型
   * @return 页定义
   */
  public static <T> Page<T> ofAll() {
    return new Page<>(1, ALL_SIZE, -1, false);
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

  /**
   * 静态构造方法: 以请求分页参数为基准, 用查询结果回填记录与总数
   *
   * @param pageRequest 请求分页参数
   * @param pageResult  查询结果
   * @param <T>         请求记录类型
   * @param <R>         结果记录类型
   * @return 页定义
   */
  public static <T, R> Page<R> of(Page<T> pageRequest, EasyPageResult<R> pageResult) {
    Objects.requireNonNull(pageRequest, "pageRequest must not be null");
    Objects.requireNonNull(pageResult, "pageResult must not be null");
    Page<R> page = new Page<>();
    page.setCurrent(pageRequest.getPageNum());
    page.setSize(pageRequest.getPageSize());
    final List<R> records = pageResult.getData();
    page.setRecords(records);
    page.setTotal(pageResult.getTotal());
    // 页大小等于 ALL_SIZE 即「取全部」路径, 返回条数顶满上限说明还有数据没返回全, 直接拒绝避免静默截断; 上限更大的 ofLimit 不做截断, 不在此列
    if (page.getSize() == ALL_SIZE && CollectionUtil.size(records) >= ALL_SIZE) {
      throw new ServiceException("取全部查询返回条数达到上限 " + ALL_SIZE + " 条, 超出部分不会返回, 请缩小查询范围或分批导出");
    }
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

  @JsonIgnore
  @Override
  public List<T> getData() {
    return getRecords();
  }
}
