package com.bjtu.zyj.jobadmin2.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;

@Data
@TableName("app_job")
public class AppJobDetail {

    @TableId(type = IdType.AUTO)
    private int id;
    /**
     * job的全限定类名
     */
    private String clazz;

    @TableField("job_key")
    private String jobName;

    @TableField(value = "job_group")
    private String jobGroup;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
