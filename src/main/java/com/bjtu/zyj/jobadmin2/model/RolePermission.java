package com.bjtu.zyj.jobadmin2.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理不同角色可以有什么权限
 */
@Data
public class RolePermission {

    private Integer id;

    private String roleId;

    private String permissionId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
