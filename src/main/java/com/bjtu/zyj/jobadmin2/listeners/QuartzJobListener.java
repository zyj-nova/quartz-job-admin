package com.bjtu.zyj.jobadmin2.listeners;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class QuartzJobListener implements JobListener {

    private static final Logger log = LoggerFactory.getLogger(QuartzJobListener.class);

    // 可以将这个数字写入到redis缓存中
    AtomicInteger totalSuccessJobs = new AtomicInteger();

    AtomicInteger totalFailedJobs = new AtomicInteger();

    public static final String LISTENER_NAME = "AppJobListener";

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (jobException != null){
            jobException.unscheduleFiringTrigger();
            // 邮件告警！
            totalFailedJobs.incrementAndGet();
            return;
        }
//        long jobRunTime = context.getJobRunTime();
//        Date fireTime = context.getFireTime();
//        JobKey jobKey = context.getJobDetail().getKey();
//        String key = jobKey.getName();
//        String group = jobKey.getGroup();

//        JobLog log = new JobLog();
//        log.setRunTime(jobRunTime);
//        log.setJobKey(key);
//        log.setJobGroup(group);
//        log.setFireTime(DateTimeUtil.format(fireTime,DateTimeUtil.DATE_PATTERN));
//        log.setStatus("SUCCESS");
        log.info("success count is {}", totalSuccessJobs.incrementAndGet());

        //System.out.println(totalFailedJobs.get());
    }
}
