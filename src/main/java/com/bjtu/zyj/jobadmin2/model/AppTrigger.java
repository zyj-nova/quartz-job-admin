package com.bjtu.zyj.jobadmin2.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("app_trigger")
public class AppTrigger {

    @TableId(type = IdType.AUTO)
    private int id;

    @TableField("cron")
    private String cron;

    @TableField("trigger_key")
    private String triggerName;

    @TableField("trigger_group")
    private String triggerGroup;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
