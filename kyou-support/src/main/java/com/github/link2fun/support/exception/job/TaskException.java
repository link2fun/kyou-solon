package com.github.link2fun.support.exception.job;

import lombok.Getter;

import java.io.Serial;

/**
 * 计划策略异常
 *
 * @author ruoyi
 */
@Getter
@SuppressWarnings("unused")
public class TaskException extends Exception {
  @Serial
  private static final long serialVersionUID = 1L;

  private final Code code;

  public TaskException(String msg, Code code) {
    this(msg, code, null);
  }

  public TaskException(String msg, Code code, Exception nestedEx) {
    super(msg, nestedEx);
    this.code = code;
  }

  public enum Code {
    TASK_EXISTS, NO_TASK_EXISTS, TASK_ALREADY_STARTED, UNKNOWN, CONFIG_ERROR, TASK_NODE_NOT_AVAILABLE
  }
}