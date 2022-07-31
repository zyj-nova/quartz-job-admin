package com.bjtu.zyj.jobadmin2.service;

import com.bjtu.zyj.jobadmin2.utils.DateTimeUtil;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 不要私自截留异常，捕获到异常后要向外抛
 */
@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    Scheduler scheduler;

    @Autowired
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 将任务和触发器加入到调度队列中
     *
     * @param jobDetail
     * @param trigger
     * @throws SchedulerException
     */
    public void scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 为某个job添加触发器
     *
     * @param trigger 需要添加的触发器
     * @throws SchedulerException 抛出调度异常
     */
    public void scheduleJob(Trigger trigger) throws SchedulerException {
        JobKey key = trigger.getJobKey();
        if (key.getName().equals("") || key.getGroup().equals("")) {
            throw new SchedulerException("需要指定job的key和group");
        }
        try {
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            throw new SchedulerException(e.getMessage());
        }
    }

    public void unScheduleJob(String triggerKey, String triggerGroup) throws SchedulerException{
        try{
            scheduler.unscheduleJob(new TriggerKey(triggerKey,triggerGroup));
        }catch (SchedulerException e){
            throw new SchedulerException(e.getMessage());
        }

    }

    public List<? extends Trigger> getTriggersOfJob(String key, String group) throws SchedulerException {
        try {
            return scheduler.getTriggersOfJob(new JobKey(key, group));
        } catch (SchedulerException e) {
            log.info("Get job triggers erro, job key :{}", key);
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * 注意：这个pause只是针对正在运行着的任务，相当于播放音乐，我暂停他的播放
     * 暂停当前任务的执行，trigger状态变为PAUSED，只有调用resume方法后，状态变为ACQUIRED后才会被继续调度执行
     *
     * @param key   job名称
     * @param group job组
     * @return 是否成功
     */
    public boolean pauseJob(String key, String group) throws SchedulerException {

        JobKey jkey = new JobKey(key, group);
        log.info("Parameters received for pausing job - jobKey : {}, groupKey: {} ", key, group);
        try {
            scheduler.pauseJob(jkey);
            log.info("Job with jobKey : {} paused successfully.", key);
            return true;
        } catch (SchedulerException e) {

            log.error("SchedulerException while pausing job with key : {} message {} ", key, e.getMessage());
            //  log.error(ExceptionUtils.getStackTrace(e));
            throw new SchedulerException(e.getMessage());
        }

    }

    /**
     * 在暂停job的调度后，调用此方法继续job的调度
     *
     * @param key
     * @param group
     * @return
     */
    public boolean resumeJob(String key, String group) {
        JobKey jKey = new JobKey(key, group);
        log.info("Parameters received for resuming job - jobKey :" + key);
        try {
            scheduler.resumeJob(jKey);
            log.info("Job with jobKey :{}, resumed successfully.", key);
            return true;
        } catch (SchedulerException e) {
            log.error("SchedulerException while resuming job with key : {} message {} ", key, e.getMessage());
            //log.error(ExceptionUtils.getStackTrace(e));
        }
        return false;
    }

    /**
     * 该方法会将正在运行中的任务终止，但并不影响调度器下一次对该任务的调度
     * 如果要取消这种调度，可以选择删除任务。
     * 更加进一步的需求：我并不想删除这个任务，我只想让他下一次或者下几次停止调度，该怎么做？
     * 初步思路：通过TriggerBuilder创建一个新的trigger并指定startAt日期，通过rescheduleJob方法重新调度
     * startAt日期可以通过原trigger的nextFireTime获取
     *
     * @param key   job的key
     * @param group job的group
     * @return 是否成功
     */
    public boolean stopJob(String key, String group) throws SchedulerException {
        log.info("JobServiceImpl.stopJob()");
        try {
            JobKey jkey = new JobKey(key, group);
            boolean res = scheduler.interrupt(jkey);

            return res;
        } catch (SchedulerException e) {
            log.error("SchedulerException while stopping job. error message :{}", e.getMessage());
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * 立即执行当前任务
     *
     * @param key   任务名称
     * @param group 任务组别
     * @return 是否启动成功
     */
    public boolean startJobNow(String key, String group) throws SchedulerException {
        JobKey jKey = new JobKey(key, group);

        log.info("Parameters received for starting job now : jobKey :{}", key);
        try {
            // 调用triggerJob为该任务创建一个临时匿名的触发器，并立即执行触发该触发器的执行
            scheduler.triggerJob(jKey);
            scheduler.getPausedTriggerGroups();
            log.info("Job with jobKey :{}, started now successfully.", key);
            return true;
        } catch (SchedulerException e) {
            log.error("SchedulerException while starting job with key : {} message {} ", key, e.getMessage());
            throw new SchedulerException(e.getMessage());
        }
    }

    public boolean isJobPaused(String jobKey, String jobGroup) throws SchedulerException {
        try {
            Set<String> pausedTriggerGroups = scheduler.getPausedTriggerGroups();
            if (pausedTriggerGroups.contains(jobGroup)) {
                return true;
            }
            return false;
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * 停止任务的下一次调度
     * 即任务调度从下下次开始。
     * @param jobKey job的名称和组组成的jobkey
     * @throws SchedulerException
     */
    public void stopNextTimeSchedule(JobKey jobKey) throws SchedulerException{
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
        Trigger trig = triggersOfJob.get(0);
        // 只能针对定时执行一次的任务，对于0-10s内每秒执行一次的情况并不适用
        // 获取下一次执行时间之后的执行时间（下下次）,将这个时间设置为起始调度时间
        Date fireTimeAfter = trig.getFireTimeAfter(trig.getNextFireTime());

        Trigger trigger = trig
                .getTriggerBuilder()//获取到trig的建造者，内部状态与trig一摸一样
                .startAt(fireTimeAfter)
                .build();

        scheduler.rescheduleJob(trig.getKey(),trigger);
    }

    public List<String> getRecentScheduleTime(String key, String group, int cnt) throws SchedulerException{
        JobKey jobKey = new JobKey(key,group);
        List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
        if (triggersOfJob.size() < 1){
            return null;
        }
        Trigger trigger = triggersOfJob.get(0);
        List<String> dateList = new ArrayList<>();
        Date startDate = trigger.getNextFireTime();
        dateList.add(DateTimeUtil.format(startDate,DateTimeUtil.DATE_PATTERN));
        for (int i = 1; i < cnt; i++){
            Date tmp = trigger.getFireTimeAfter(startDate);
            dateList.add(DateTimeUtil.format(tmp,DateTimeUtil.DATE_PATTERN));
            startDate = tmp;
        }
        return dateList;
    }


}