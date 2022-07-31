package com.bjtu.zyj.jobadmin2.service;

import com.bjtu.zyj.jobadmin2.dto.JobState;
import com.bjtu.zyj.jobadmin2.repo.JobRepository;
import com.bjtu.zyj.jobadmin2.utils.DateTimeUtil;
import com.bjtu.zyj.jobadmin2.utils.JobCache;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    Scheduler scheduler;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    public void  setScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
    }

    /**
     * 创建任务时，判断当前任务名称与组别是否已经存在于调度器中
     * @param group
     * @param key
     * @return true:存在，false，不存在
     * @throws SchedulerException
     */
    public boolean isJobExist(String key, String group) throws SchedulerException {
        try {
            boolean res = scheduler.checkExists(new JobKey(key,group));
            return res;
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * 添加任务，如果当前任务已经存在，使用replace决定是否替换原有任务，
     * 注意，此时并不会将job加入到调度队列中
     *
     */
    public void addJob(JobDetail jobDetail, boolean replace) throws SchedulerException {
        try {
            scheduler.addJob(jobDetail,replace);
        }catch (SchedulerException e){
            throw new SchedulerException(e.getMessage());
        }

    }

    public JobDetail getJobDetail(String key, String group) throws SchedulerException{
        try {
            return scheduler.getJobDetail(new JobKey(key,group));
        } catch (SchedulerException e) {
            log.info("Get job key:{} detail erro.",key);
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * 从任务列表中删除某个任务，以及和这个任务相关的所有触发器
     * 注意：删除某一个任务时，quartz底层会自动把与任务相关的trigger一并从数据库中删除
     * @param key 任务名称
     * @param group 任务组
     * @return  是否删除成功
     */
    public boolean deleteJob(String key, String group) throws SchedulerException {
        JobKey jkey = new JobKey(key, group);
        log.info("Parameters received for deleting job : jobKey :{}", key);

        try {
            boolean status = scheduler.deleteJob(jkey);
            log.info("Job with jobKey :{}, deleted with status :{}",key, status);
            return status;
        } catch (SchedulerException e) {
            log.error("SchedulerException while deleting job with key :{}, message: {}",key, e.getMessage());
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * 获取当前任务的执行状态，本质上是获取对应触发器的状态
     * @param key
     * @param group
     * @return job的状态：共有六种：NONE, SCHEDULED(NORMAL), PAUSED, COMPLETE, ERROR, BLOCKED
     */
    public JobState getJobState(String key, String group) throws SchedulerException {
        JobKey jobKey = new JobKey(key, group);
        try {
            //JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

            if(triggers != null && triggers.size() > 0){
                //注意：某个job可能绑定了很多的trigger，这里只返回匹配到的第一个，即最合适的
                Trigger.TriggerState triggerState = scheduler.getTriggerState(triggers.get(0).getKey());

                return JobState.toJobState(triggerState);
            }
        } catch (SchedulerException e) {
            log.error("SchedulerException while checking job with name and group exist: {}", e.getMessage());
            throw new SchedulerException(e.getMessage());
        }
        //当前job并没有被调度，返回NONE
        return JobState.NONE;
    }


    public List<Map<String, Object>> getUnScheduledJobs() throws SchedulerException {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        try {
            List<String> groupNames = scheduler.getJobGroupNames();
            for (String groupName:groupNames) {
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
                for (JobKey jobKey:jobKeys){
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    Class<? extends Job> jobClass = jobDetail.getJobClass();
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    if (triggers.size() == 0){
                        Map<String, Object> map = Map.of(
                                "jobName",jobKey.getName(),
                                "jobGroup",jobKey.getGroup(),
                                "type",jobClass.getName(),
                                "nextFireTime","暂未加入调度队列",
                                "jobStatus","暂未加入调度队列",
                                "scheduleTime","暂未加入调度队列"
                        );
                        list.add(map);
                    }
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new SchedulerException(e.getMessage());
        }
        return list;
    }

    /**
     * 获取调度队列中所有任务
     * 返回形式：List列表中包括每个任务的状态（以Map的方式返回）
     * @return 所有任务的状态
     */
    public List<Map<String, Object>> getAllScheduledJobs() throws SchedulerException{
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        try {
            List<String> jobGroupNames = scheduler.getJobGroupNames();
            for (String jobGroup:jobGroupNames){
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
                for (JobKey jobKey:jobKeys){
                    // 注意区分，如果这个job不在数据库中，说明这个job还没有指定触发器，应该跳过，
                    // 如果在，但是触发器数量为0，说明被禁用了，
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    if (triggers.size() < 1){
                        continue;
                    }
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    Class<? extends Job> jobClass = jobDetail.getJobClass();
                    Date nextFireTime, startTime;

                    //获取最近一次执行时间
                    nextFireTime = triggers.get(0).getNextFireTime();
                    startTime = triggers.get(0).getStartTime();

                    String jobStatus;
                    if(isJobRunning(jobKey.getName(),jobGroup)){
                        jobStatus = "RUNNING";
                    }else{
                        JobState jobState = this.getJobState(jobKey.getName(),jobGroup);
                        jobStatus = jobState.toString();
                    }
                    Map<String, Object> map = Map.of(
                            "jobName",jobKey.getName(),
                            "jobGroup",jobKey.getGroup(),
                            "type",jobClass.getName(),
                            "nextFireTime",nextFireTime == null ? "暂未加入队列" :DateTimeUtil.format(nextFireTime,DateTimeUtil.DATE_PATTERN),
                            "jobStatus",jobStatus,
                            "enable",true,
                            "scheduleTime",startTime == null ? "暂未加入队列" :DateTimeUtil.format(startTime,DateTimeUtil.DATE_PATTERN)
                    );
                    list.add(map);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new SchedulerException(e.getMessage());
        }
        return list;
    }

    /**
     * 从数据库中获取到之前关联过触发器但被禁用的任务，这样直接开启就不用再指定触发器了
     * @return 任务列表，每个任务都是一个map包含各种属性
     * @throws SchedulerException 异常
     */
    public List<Map<String, Object>> getJobsFromDB() throws SchedulerException{
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        try {
            List<String> jobGroupNames = scheduler.getJobGroupNames();
            for (String jobGroup:jobGroupNames){
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroup));
                for (JobKey jobKey:jobKeys){
                    // 注意区分，如果这个job不在数据库中，说明这个job还没有指定触发器，应该跳过，
                    // 如果在，但是触发器数量为0，说明被禁用了，

                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    if (triggers.size() < 1 && jobRepository.selectJobByNameAndGroup(jobKey.getName(),jobGroup) == null){
                        continue;
                    }
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    Class<? extends Job> jobClass = jobDetail.getJobClass();
                    Date startTime = null, nextFireTime = null;
                    if (triggers.size() >= 1){
                        startTime = triggers.get(0).getStartTime();
                        nextFireTime = triggers.get(0).getNextFireTime();
                    }
                    String jobStatus;
                    if(isJobRunning(jobKey.getName(),jobGroup)){
                        jobStatus = "RUNNING";
                    }else{
                        JobState jobState = this.getJobState(jobKey.getName(),jobGroup);
                        jobStatus = jobState.toString();
                    }
                    Map<String, Object> map = Map.of(
                            "jobName",jobKey.getName(),
                            "jobGroup",jobKey.getGroup(),
                            "type",jobClass.getName(),
                            "nextFireTime", nextFireTime == null ? "暂未加入队列" : DateTimeUtil.format(nextFireTime,DateTimeUtil.DATE_PATTERN),
                            "jobStatus",jobStatus,
                            "enable", triggers.size() >= 1 ,// 没有触发器，说明被禁用了，enable属性为 false
                            "scheduleTime", startTime == null ? "暂未加入队列" : DateTimeUtil.format(startTime, DateTimeUtil.DATE_PATTERN)
                    );
                    list.add(map);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new SchedulerException(e.getMessage());
        }
        return list;
    }
    /**
     * 判断当前任务是否在执行中
     * @param key 任务名称
     * @param group 任务组别
     * @return 运行->true；没有运行->false
     */
    public boolean isJobRunning(String key, String group) throws SchedulerException {
        log.info("Parameters received for checking job is running ? : jobKey :{}", key);

        List<JobExecutionContext> contexts = scheduler.getCurrentlyExecutingJobs();
        if (contexts != null){
            for (JobExecutionContext context:contexts){
                String jobName = context.getJobDetail().getKey().getName();
                String jobGroup = context.getJobDetail().getKey().getGroup();
                if (jobName.equalsIgnoreCase(key) && jobGroup.equalsIgnoreCase(group)){
                    return true;
                }
            }
        }
        return false;
    }

    // 返回运行中的任务的详细信息
    public List<JobExecutionContext> getRunningJobs() throws SchedulerException{
        List<JobExecutionContext> currentlyExecutingJobs = scheduler.getCurrentlyExecutingJobs();
        if (currentlyExecutingJobs == null){
            return null;
        }
        return currentlyExecutingJobs;
    }

    /**
     * 从缓存中获取到实现了job接口的类
     * @return 每个类的全限定类名
     */
    public List<String> getAllJobTypes(){
        List<String> ans = JobCache.jobCache.stream().collect(Collectors.toList());
        return ans;
    }

    /**
     *
     * @param key jobkey
     * @param group jobgroup
     * @return 与该任务关联的dataMap
     * @throws SchedulerException
     */
    public JobDataMap getJobDetails(String key, String group) throws SchedulerException{
        JobKey jobKey = new JobKey(key,group);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        return jobDetail.getJobDataMap();
    }

    /**
     *
     * @return 返回当前调度队列当中的job的组
     * @throws SchedulerException
     */
    public List<String> getJobGroups() throws SchedulerException{
        List<String> jobGroupNames = scheduler.getJobGroupNames();
        return jobGroupNames;
    }
}
