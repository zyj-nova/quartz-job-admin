package com.bjtu.zyj.jobadmin2.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CronTask{
    private String key;

    private String group;

    private String type;

    private String cron;

    private String triggerKey;

    private String triggerGroup;

    private Map<String,String> dataMap;

}
