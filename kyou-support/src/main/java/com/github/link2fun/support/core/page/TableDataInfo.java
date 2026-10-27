package com.github.link2fun.support.core.page;

import com.github.link2fun.support.constant.HttpStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 *
 * @author ruoyi
 */
@Setter
@Getter
@SuppressWarnings("unused")
public class TableDataInfo implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 总记录数 */
  private long total;

  /** 列表数据 */
  private List<?> rows;

  /** 消息状态码 */
  private int code;

  /** 消息内容 */
  private String msg;

  /**
   * 表格数据对象
   */
  public TableDataInfo() {
  }

  /**
   * 分页
   *
   * @param list  列表数据
   * @param total 总记录数
   */
  public TableDataInfo(List<?> list, Long total) {
    this.rows = list;
    this.total = total;
  }

  /** 由分页结果组装: code=200, msg=查询成功, rows 与 total 取自 page */
  public static TableDataInfo of(final Page<?> page) {
    final TableDataInfo dataInfo = new TableDataInfo(page.getRecords(), page.getTotal());
    dataInfo.setCode(HttpStatus.SUCCESS);
    dataInfo.setMsg("查询成功");
    return dataInfo;
  }

}
