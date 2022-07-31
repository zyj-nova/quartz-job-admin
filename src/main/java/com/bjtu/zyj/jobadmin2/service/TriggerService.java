package com.bjtu.zyj.jobadmin2.service;

import com.bjtu.zyj.jobadmin2.dto.DtoTrigger;
import com.bjtu.zyj.jobadmin2.utils.JobUtil;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TriggerService {
    private static final Logger log = LoggerFactory.getLogger(TriggerService.class);

    Scheduler scheduler;

    @Autowired
    public void  setScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
    }

    public List<DtoTrigger> getAllTriggers() throws SchedulerException {
        List<DtoTrigger> ret = new ArrayList<>();
        try {
            List<String> triggerGroups = scheduler.getTriggerGroupNames();
            for (String group:triggerGroups){
                Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group));
                for (TriggerKey triggerKey:triggerKeys){
                    CronTriggerImpl trigger = (CronTriggerImpl)scheduler.getTrigger(triggerKey);

                    DtoTrigger t = DtoTrigger.build(triggerKey.getName(),
                            triggerKey.getGroup(),
                            trigger.getCronExpression(),
                            trigger.getJobKey().getName(),
                            trigger.getJobKey().getGroup(),
                            trigger.getNextFireTime(),
                            trigger.getPreviousFireTime(),
                            trigger.getStartTime());

                    ret.add(t);
                }
            }
            return ret;
        } catch (SchedulerException e) {
            throw new SchedulerException(e.getMessage());
        }
    }

    /**
     * 删除某个job的一个指定trigger
     * @param key 任务名称
     * @param group 任务的组
     * @return 是否删除成功
     */
    public boolean unScheduleJob(String key, String group) throws SchedulerException{

        TriggerKey tkey = new TriggerKey(key,group);
        log.info("Parameters received for un-Scheduling job : jobKey : {}", key);
        try {
            boolean status = scheduler.unscheduleJob(tkey);
            log.info("Trigger associated with jobKey : {}, unscheduled with status :{}",key, status);
            return status;
        } catch (SchedulerException e) {
            log.error("SchedulerException while un-Scheduling job with key :{}, message: {}",key, e.getMessage());
            throw new SchedulerException(e.getMessage());
        }
    }

    public boolean updateCronTrigger(String cronExpression,
                                     String triggerKey,
                                     String triggerGroup) throws SchedulerException {
        if (!isTriggerExist(triggerKey,triggerGroup)){
            return false;
        }
        TriggerKey oldTriggerKey = TriggerKey.triggerKey(triggerKey, triggerGroup);
        Trigger newTrigger = JobUtil
                .createCronTrigger(
                        cronExpression,
                        triggerKey,
                        triggerGroup);

        try {
            scheduler.rescheduleJob(oldTriggerKey,newTrigger);
            return true;
        } catch (SchedulerException e) {
            throw new SchedulerException(e.getMessage());
        }
    }

    public boolean isTriggerExist(String key, String group) throws SchedulerException {
        try {
            boolean res = scheduler.checkExists(new TriggerKey(key,group));
            return res;
        } catch (SchedulerException e) {
            throw new SchedulerException(e.getMessage());
        }
    }

}
