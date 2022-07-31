package com.bjtu.zyj.jobadmin2.model;

import lombok.Data;

@Data
public class JobLog {

    private long runTime;

    private String fireTime;

    private String jobKey;

    private String jobGroup;

    private String status;

}
