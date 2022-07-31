package com.bjtu.zyj.jobadmin2.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 中间表，用于管理用户与角色的关联关系。
 * 1个用户可有多个角色，用户->角色 1:n
 * 1个角色也可由多个用户，角色->用户 1:n
 * 因此需要一个中间表进行管理。
 */
@Data
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer roleId;

    private Integer userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
