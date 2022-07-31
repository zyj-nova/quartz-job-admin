package com.bjtu.zyj.jobadmin2.dto;

import lombok.NonNull;
import org.quartz.Trigger;

import java.util.stream.Stream;

/**
 * 用于与前端进行交互job状态的传输数据类，
 * 列举的状态来自 org.quartz.Trigger这个接口
 */
public enum JobState {
    NONE("NONE"),
    SCHEDULED("NORMAL"),
    PAUSED("PAUSED"),
    COMPLETE("COMPLETE"),
    ERROR("ERROR"),
    BLOCKED("BLOCKED");

    private String triggerState;

    JobState(String triggerState){
        this.triggerState = triggerState;
    }

    @NonNull
    public static JobState toJobState(Trigger.TriggerState triggerState){
        JobState state = Stream.of(JobState.values())
                .filter(jobState -> jobState.triggerState.equalsIgnoreCase(triggerState.toString()))
                .findFirst()
                .orElse(NONE);
        return state;
    }

    @NonNull
    public Trigger.TriggerState triggerState(){
        return Trigger.TriggerState.valueOf(this.triggerState);
    }
}
