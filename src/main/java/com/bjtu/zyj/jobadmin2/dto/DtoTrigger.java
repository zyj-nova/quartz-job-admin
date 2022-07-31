package com.bjtu.zyj.jobadmin2.dto;

import com.bjtu.zyj.jobadmin2.utils.DateTimeUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DtoTrigger implements Serializable {
    private String name;
    private String group;

    private String jobName;
    private String jobGroup;

    private String nextFireDate;
    private String previousFireDate;
    private String startDate;
    private String cron;

    public static DtoTrigger build(String name,
                                   String group,
                                   String cron,
                                   String jobName,
                                   String jobGroup,
                                   Date nextFireDate,
                                   Date previousFireDate,
                                   Date startDate){
        DtoTrigger trigger = new DtoTrigger();
        trigger.setName(name);
        trigger.setGroup(group);
        trigger.setJobName(jobName);
        trigger.setJobGroup(jobGroup);
        trigger.setCron(cron);


        trigger.setNextFireDate(DateTimeUtil.format(nextFireDate,DateTimeUtil.DATE_PATTERN));
        if (previousFireDate == null)
            trigger.setNextFireDate("null");
        else
            trigger.setPreviousFireDate(DateTimeUtil.format(previousFireDate,DateTimeUtil.DATE_PATTERN));
        trigger.setStartDate(DateTimeUtil.format(startDate,DateTimeUtil.DATE_PATTERN));

        return trigger;

    }
}
