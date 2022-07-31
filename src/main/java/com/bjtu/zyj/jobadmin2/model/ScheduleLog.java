package com.bjtu.zyj.jobadmin2.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleLog {

    //调度job名称
    private String jobKey;

    // 调度的job组别
    private String jobGroup;

    // 执行这个调度操作的时间
    private String scheduleDate;

    // 操作结果
    private String result;

    // 操作名称
    private String action;

    // 操作者的用户名
    private String username;
}
