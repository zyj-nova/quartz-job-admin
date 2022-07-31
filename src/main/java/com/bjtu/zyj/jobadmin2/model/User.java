package com.bjtu.zyj.jobadmin2.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("app_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    //用于数据库主键
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("user_name")
    private String username;

    @TableField("pass_word")
    private String password;

    private boolean enabled;

    private String phone;

    private String email;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

}
