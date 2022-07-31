package com.bjtu.zyj.jobadmin2.controller;

import com.bjtu.zyj.jobadmin2.dto.CronTask;
import com.bjtu.zyj.jobadmin2.dto.ServerResponse;
import com.bjtu.zyj.jobadmin2.model.AppJobDetail;
import com.bjtu.zyj.jobadmin2.model.AppTrigger;
import com.bjtu.zyj.jobadmin2.model.JobTrigger;
import com.abchina.zyj.jobadmin2.service.*;
import com.bjtu.zyj.jobadmin2.service.*;
import com.bjtu.zyj.jobadmin2.utils.JobUtil;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.bjtu.zyj.jobadmin2.utils.ServerResponseUtil.*;

@RestController
@RequestMapping("/api/schedule/")
public class ScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);

    ScheduleService scheduleService;

    JobService jobService;

    @Autowired
    TriggerPersistentService triggerPersistentService;

    @Autowired
    JobPersistentService jobPersistentService;

    @Autowired
    JobTriggerPersistentService jobTriggerPersistentService;

    @Autowired
    public void setScheduleService(ScheduleService scheduleService){
        this.scheduleService = scheduleService;
    }

    @Autowired
    public void setJobService(JobService jobService) {
        this.jobService = jobService;
    }

    // 开启任务的调度
    @GetMapping("schedule/{group}/{key}")
    public ServerResponse scheduleJob(@PathVariable("group") String group, @PathVariable("key") String key){

        try {
            // 从数据库中获取到与job
            int jobId = jobPersistentService.selectJobByNameAndGroup(key, group).getId();
            // 再获取到与之关联的trigger id
            int triggerId = jobTriggerPersistentService.selectTriggerByJobId(jobId);
            System.out.println(triggerId);
            // 拿到trigger的详细信息
            AppTrigger appTrigger = triggerPersistentService.selectTriggerById(triggerId);
            System.out.println(appTrigger);
            Trigger cronTrigger = JobUtil.createCronTrigger(appTrigger.getCron(),
                    key,
                    group,
                    appTrigger.getTriggerName(),
                    appTrigger.getTriggerName());

            scheduleService.scheduleJob(cronTrigger);

        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR,false);
        }
        return getServerResponse(SUCCESS,true);
    }

    @GetMapping("un_schedule/{group}/{key}")
    public ServerResponse unScheduleJob(@PathVariable("key") String jobKey,
                                        @PathVariable("group") String group){
        try {
            List<? extends Trigger> triggersOfJob = scheduleService.getTriggersOfJob(jobKey, group);
            CronTriggerImpl trigger = (CronTriggerImpl)triggersOfJob.get(0);

            String cronExpression = trigger.getCronExpression();
            String triggerName = trigger.getKey().getName();
            String triggerGroup = trigger.getKey().getGroup();
            //将触发器信息存储在数据库中，方便下次开启任务调度，
            AppTrigger t = new AppTrigger();
            t.setCron(cronExpression);
            t.setTriggerGroup(triggerGroup);
            t.setTriggerName(triggerName);
            t.setCreateTime(LocalDateTime.now());
            t.setUpdateTime(LocalDateTime.now());

            AppJobDetail job = new AppJobDetail();
            job.setUpdateTime(LocalDateTime.now());
            job.setCreateTime(LocalDateTime.now());
            JobDetail jobDetail = jobService.getJobDetail(jobKey, group);
            job.setClazz(jobDetail.getJobClass().getName());
            job.setJobName(jobKey);
            job.setJobGroup(group);

            int jobId = jobPersistentService.saveJob(job);
            log.info("job id is {}",jobId);
            int triggerId = triggerPersistentService.saveTrigger(t);
            log.info("trigger id is {}",triggerId);

            JobTrigger jobTrigger = new JobTrigger();
            jobTrigger.setTriggerId(triggerId);
            jobTrigger.setJobId(jobId);
            jobTriggerPersistentService.save(jobTrigger);

            // 删除与任务相关联的触发器
            scheduleService.unScheduleJob(triggerName,triggerGroup);


        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("add_schedule")
    public ServerResponse scheduleJob(@RequestBody CronTask task){
        Map<String, String> dataMap = task.getDataMap();
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.putAll(dataMap);
        String jobName = task.getKey();
        String jobGroup = task.getGroup();
        String cronExpression = task.getCron();
        String triggerName = task.getTriggerKey();
        String triggerGroup = task.getTriggerGroup();
        String type = task.getType();
        try {
            if (jobService.isJobExist(jobName,jobGroup)){
                return getServerResponse(JOB_NAME_NOT_PRESENT,false);
            }
            JobDetail jobDetail = JobUtil
                    .createJobDetail((Class<? extends Job>) Class.forName(type),
                            jobName,
                            jobGroup,
                            jobDataMap
                            );
            Trigger trigger = JobUtil.createCronTrigger(cronExpression,
                    jobName,
                    jobGroup,
                    triggerName,
                    triggerGroup);
            scheduleService.scheduleJob(jobDetail,trigger);
            return getServerResponse(SUCCESS,true);
        } catch (SchedulerException | ClassNotFoundException e) {
            e.printStackTrace();
            return getServerResponse(ERROR,false);
        }

    }

    @GetMapping("stop/{group}/{key}")
    public ServerResponse stopJob(@PathVariable("group") String group,
                           @PathVariable("key") String key){
        try {
            if (!jobService.isJobExist(key,group)){
                return getServerResponse(JOB_NAME_NOT_PRESENT,false);
            }
            boolean status = jobService.isJobRunning(key,group);
            log.info("Job status is {}",status);
            if (!status){
                return getServerResponse(JOB_NOT_IN_RUNNING_STATE, false);
            }
            scheduleService.stopJob(key,group);
            return getServerResponse(SUCCESS,true);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR,false);
    }

    @GetMapping("stop/next/{group}/{key}")
    public ServerResponse stopNextFire(@PathVariable("group") String group,
                           @PathVariable("key") String key){

        try {
            scheduleService.stopNextTimeSchedule(new JobKey(key,group));
            return getServerResponse(SUCCESS, true);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR,false);
        }
    }

    @GetMapping("pause/{group}/{key}")
    public ServerResponse pauseJob(@PathVariable("group") String group,
                           @PathVariable("key") String key){
        try {
            if (!jobService.isJobRunning(key,group)){
                return getServerResponse(JOB_NOT_IN_RUNNING_STATE,false);
            }
            boolean res = scheduleService.pauseJob(key,group);
            return getServerResponse(SUCCESS,res);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR,false);
    }

    @GetMapping("resume/{group}/{key}")
    public ServerResponse resumeJob(@PathVariable("group") String group,
                            @PathVariable("key") String key){
        try {
            if (!scheduleService.isJobPaused(key,group)){
                return getServerResponse(JOB_NOT_IN_PAUSED_STATE,false);
            }
            scheduleService.resumeJob(key,group);
            return getServerResponse(SUCCESS,true);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR,false);
    }

    @GetMapping("startnow/{group}/{key}")
    public ServerResponse startJobNow(@PathVariable("group") String group,
                               @PathVariable("key") String key){
        boolean status = false;

        try {
            //先判断job是否在运行中
            if (jobService.isJobRunning(key,group)){
                return getServerResponse(JOB_ALREADY_IN_RUNNING_STATE,false);
            }
            status = scheduleService.startJobNow(key,group);
            return getServerResponse(SUCCESS,status);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR,false);
    }

    @GetMapping("/recent/{group}/{job}/{cnt}")
    public ServerResponse getRecentScheduleTime(@PathVariable("group") String group,
                                                @PathVariable("job") String key,
                                                @PathVariable("cnt") int cnt){
        List<String> ans;
        try {
            ans = scheduleService.getRecentScheduleTime(key, group,cnt);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR,null);
        }
        return getServerResponse(SUCCESS, ans);
    }
}
