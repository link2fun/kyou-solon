package com.github.link2fun.system.modular.operlog.model.dto;

import com.github.link2fun.support.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志响应
 * {@link com.github.link2fun.system.modular.operlog.model.SysOperLog }
 */
@Data
public class SysOperLogDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 日志主键 */
  @Excel(name = "操作序号", cellType = Excel.ColumnType.NUMERIC)
  private Long operId;
  /** 模块标题 */
  @Excel(name = "操作模块")
  private String title;
  /** 业务类型 */
  @Excel(name = "业务类型", readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
  private Integer businessType;
  /** 方法名称 */
  @Excel(name = "请求方法")
  private String method;
  /** 请求方式 */
  @Excel(name = "请求方式")
  private String requestMethod;
  /** 操作类型 */
  @Excel(name = "操作类别", readConverterExp = "0=其它,1=后台用户,2=手机端用户")
  private Integer operatorType;
  /** 操作人员 */
  @Excel(name = "操作人员")
  private String operName;
  /** 部门名称 */
  @Excel(name = "部门名称")
  private String deptName;
  /** 请求URL */
  @Excel(name = "请求地址")
  private String operUrl;
  /** 操作地址 */
  @Excel(name = "操作地址")
  private String operIp;
  /** 操作地点 */
  @Excel(name = "操作地点")
  private String operLocation;
  /** 请求参数 */
  @Excel(name = "请求参数")
  private String operParam;
  /** 返回参数 */
  @Excel(name = "返回参数")
  private String jsonResult;
  /** 操作状态 */
  @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
  private Integer status;
  /** 错误消息 */
  @Excel(name = "错误消息")
  private String errorMsg;
  /** 操作时间 */
  @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime operTime;
  /** 消耗时间 */
  @Excel(name = "消耗时间", suffix = "毫秒")
  private Long costTime;
}
