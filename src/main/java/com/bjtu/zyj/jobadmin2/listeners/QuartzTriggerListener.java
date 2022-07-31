package com.bjtu.zyj.jobadmin2.listeners;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QuartzTriggerListener implements TriggerListener {

    private final static Logger log = LoggerFactory.getLogger(QuartzTriggerListener.class);


    @Override
    public String getName() {
        return "globalTrigger";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        log.info("TriggerListner.triggerFired()");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        log.info("TriggerListner.vetoJobExecution()");
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log.info("TriggerListner.triggerMisfired()");
        String jobName = trigger.getJobKey().getName();
        log.info("Job name: " + jobName + " is misfired");

    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        log.info("TriggerListner.triggerComplete()");
    }
}
