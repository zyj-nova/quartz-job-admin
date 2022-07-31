package com.bjtu.zyj.jobadmin2.jobs;

import lombok.Data;

import java.util.Map;

@Data
public class Task {
    private String key;

    private String group;

    private String type;

    private Map<String,String> dataMap;

}
