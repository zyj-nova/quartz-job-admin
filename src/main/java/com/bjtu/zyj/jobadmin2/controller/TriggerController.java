package com.bjtu.zyj.jobadmin2.controller;

import com.bjtu.zyj.jobadmin2.dto.DtoTrigger;
import com.bjtu.zyj.jobadmin2.dto.ServerResponse;
import com.bjtu.zyj.jobadmin2.service.TriggerService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static com.bjtu.zyj.jobadmin2.utils.ServerResponseUtil.*;
@RequestMapping("/api/trigger/")
@RestController
public class TriggerController {

    private static final Logger log = LoggerFactory.getLogger(TriggerController.class);

   TriggerService triggerService;

    @Autowired
    public void setScheduleService(TriggerService triggerService){
        this.triggerService = triggerService;
    }

    //为某个job添加 trigger

//    public void addTrigger(String triggerKey,
//                           String triggerGroup,
//                           String cron,
//                           String jobKey,
//                           String jobGroup)  {
//        Trigger cronTrigger = JobUtil.createCronTrigger(cron,
//                jobKey,
//                jobGroup,
//                triggerKey,
//                triggerGroup);
//        try {
//            scheduleService.scheduleJob(cronTrigger);
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
//    }

    @GetMapping("all")
    public ServerResponse getAllTriggers(){
        try {
            List<DtoTrigger> allTriggers = triggerService.getAllTriggers();
            return getServerResponse(SUCCESS,allTriggers);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR,null);
    }

    @DeleteMapping("delete/{key}/{group}")
    public ServerResponse deleteTrigger(@PathVariable("key") String triggerKey,
                              @PathVariable("group") String triggerGroup){
        try {
            if (!triggerService.isTriggerExist(triggerKey,triggerGroup)){
                return getServerResponse(TRIGGER_NAME_NOT_PRESENT,false);
            }
            boolean res = triggerService.unScheduleJob(triggerKey,triggerGroup);
            return getServerResponse(SUCCESS,res);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR, false);
    }

}
