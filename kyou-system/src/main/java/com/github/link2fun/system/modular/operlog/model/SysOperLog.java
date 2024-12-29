package com.github.link2fun.system.modular.operlog.model;

import com.easy.query.core.annotation.*;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.QueryEntity;
import com.github.link2fun.system.modular.operlog.model.proxy.SysOperLogProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志记录表 oper_log
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@EasyAlias("operLog")
@Table("sys_oper_log")
public class SysOperLog extends QueryEntity implements ProxyEntityAvailable<SysOperLog, SysOperLogProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 日志主键 */
  @Excel(name = "操作序号", cellType = Excel.ColumnType.NUMERIC)
  @Column(value = "oper_id", primaryKey = true, generatedKey = true)
  private Long operId;

  /** 操作模块 */
  @Excel(name = "操作模块")
  @Column("title")
  private String title;

  /** 业务类型（0其它 1新增 2修改 3删除） */
  @Excel(name = "业务类型", readConverterExp = "0=其它,1=新增,2=修改,3=删除,4=授权,5=导出,6=导入,7=强退,8=生成代码,9=清空数据")
  @Column("business_type")
  private Integer businessType;

  /** 业务类型数组 */
  @ColumnIgnore
  private List<Integer> businessTypes;

  /** 请求方法 */
  @Excel(name = "请求方法")
  @Column("method")
  private String method;

  /** 请求方式 */
  @Excel(name = "请求方式")
  @Column("request_method")
  private String requestMethod;

  /** 操作类别（0其它 1后台用户 2手机端用户） */
  @Excel(name = "操作类别", readConverterExp = "0=其它,1=后台用户,2=手机端用户")
  @Column("operator_type")
  private Integer operatorType;

  /** 操作人员 */
  @Excel(name = "操作人员")
  @Column("oper_name")
  private String operName;

  /** 部门名称 */
  @Excel(name = "部门名称")
  @Column("dept_name")
  private String deptName;

  /** 请求url */
  @Excel(name = "请求地址")
  @Column("oper_url")
  private String operUrl;

  /** 操作地址 */
  @Excel(name = "操作地址")
  @Column("oper_ip")
  private String operIp;

  /** 操作地点 */
  @Excel(name = "操作地点")
  @Column("oper_location")
  private String operLocation;

  /** 请求参数 */
  @Excel(name = "请求参数")
  @Column("oper_param")
  private String operParam;

  /** 返回参数 */
  @Excel(name = "返回参数")
  @Column("json_result")
  private String jsonResult;

  /** 操作状态（0正常 1异常） */
  @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
  @Column("status")
  private Integer status;

  /** 错误消息 */
  @Excel(name = "错误消息")
  @Column("error_msg")
  private String errorMsg;

  /** 操作时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Column("oper_time")
  private LocalDateTime operTime;

  /** 消耗时间 */
  @Excel(name = "消耗时间", suffix = "毫秒")
  @Column("cost_time")
  private Long costTime;

}
