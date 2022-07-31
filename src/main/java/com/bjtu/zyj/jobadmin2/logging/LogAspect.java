package com.bjtu.zyj.jobadmin2.logging;

import com.bjtu.zyj.jobadmin2.dto.CronTask;
import com.bjtu.zyj.jobadmin2.model.ScheduleLog;
import com.bjtu.zyj.jobadmin2.utils.DateTimeUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class LogAspect {

    // 修饰符 返回值 包名.类名.方法名(参数)
    @Pointcut("execution(public     * com.bjtu.zyj.jobadmin2.controller.ScheduleController.*(..))")
    public void pointcut(){}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        Object result = null;
        //执行了哪个方法
        String action = proceedingJoinPoint.getSignature().getName();

        Object[] args = proceedingJoinPoint.getArgs();
        ScheduleLog log = new ScheduleLog();
        String jobGroup = "", jobKey = "";
        if (args.length < 2){
            CronTask task = (CronTask) args[0];
            jobKey = task.getKey();
            jobGroup = task.getGroup();
        }else{
            jobGroup = (String) args[0];jobKey = (String) args[1];
        }

        log.setJobKey(jobKey);
        log.setJobGroup(jobGroup);
        log.setUsername("user");
        log.setScheduleDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateTimeUtil.DATE_PATTERN)));
        log.setAction(action);
        try {
            result = proceedingJoinPoint.proceed();
        }catch (Exception e){
            log.setResult("失败");
        }
        log.setResult("成功");
        return result;
    }
}
