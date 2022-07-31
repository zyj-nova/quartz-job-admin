package com.bjtu.zyj.jobadmin2.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限模型
 */
@Data
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;
    //用于数据库主键
    private Integer id;

    private String roleName;

    private String roleCode;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
