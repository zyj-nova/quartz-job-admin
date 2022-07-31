package com.bjtu.zyj.jobadmin2.jobs;


import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class SimpleJob extends QuartzJobBean  implements InterruptableJob {

    private final static Logger log = LoggerFactory.getLogger(SimpleJob.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        while (true){//模拟任务一直在运行
//            log.info("运行中");
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        long start = System.currentTimeMillis();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        long end = System.currentTimeMillis();
//        log.info("Running..., time :{}",end - start);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("running");
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        log.info("interrupted.....");
    }
}
