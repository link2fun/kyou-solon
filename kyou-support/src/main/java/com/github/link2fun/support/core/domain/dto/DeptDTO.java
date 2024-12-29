package com.github.link2fun.support.core.domain.dto;

import com.github.link2fun.support.core.domain.entity.SysDept;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DeptDTO implements Serializable {

  private Long deptId;

  private String deptName;

  private Long parentId;

  private String ancestors;

  private Integer orderNum;

  private String leader;
  private String status;


  public static DeptDTO of(SysDept dept) {
    DeptDTO deptDTO = new DeptDTO();
    deptDTO.setDeptId(dept.getDeptId());
    deptDTO.setDeptName(dept.getDeptName());
    deptDTO.setParentId(dept.getParentId());
    deptDTO.setAncestors(dept.getAncestors());
    deptDTO.setOrderNum(dept.getOrderNum());
    deptDTO.setLeader(dept.getLeader());
    deptDTO.setStatus(dept.getStatus());
    return deptDTO;
  }

}
