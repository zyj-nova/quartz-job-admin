package com.bjtu.zyj.jobadmin2.utils;

import org.quartz.*;

public class JobUtil {

    public static Trigger createCronTrigger(String cronExpression, String triggerKey, String triggerGroup){
        return TriggerBuilder
                .newTrigger()
                .withIdentity(triggerKey,triggerGroup)
                .withSchedule(CronScheduleBuilder
                        .cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionIgnoreMisfires())
                .build();
    }

    public static JobDetail createJobDetail(Class<? extends Job> jobClass,
                                            String jobKey,
                                            String jobGroup,
                                            JobDataMap jobDataMap){
        return JobBuilder
                .newJob(jobClass)
                .withIdentity(jobKey,jobGroup)
                .storeDurably()//当没有触发器与该任务关联时，不删除该任务
                .setJobData(jobDataMap)
                .build();
    }

    /**
     * 为trigger分配自己的key和group
     * @param cronExpression cron表达式
     * @param jobKey job的key
     * @param jobGroup job的group
     * @param triggerKey trigger的key
     * @param triggerGroup trigger的group
     * @return 创建好的trigger
     */
    public static Trigger createCronTrigger(String cronExpression,
                                            String jobKey,
                                            String jobGroup,
                                            String triggerKey,
                                            String triggerGroup){
        return TriggerBuilder
                .newTrigger()
                .forJob(jobKey,jobGroup)
                .withIdentity(triggerKey,triggerGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();
    }

}
