package com.bjtu.zyj.jobadmin2.controller;

import com.bjtu.zyj.jobadmin2.dto.JobState;
import com.bjtu.zyj.jobadmin2.dto.ServerResponse;
import com.bjtu.zyj.jobadmin2.jobs.Task;
import com.bjtu.zyj.jobadmin2.service.JobPersistentService;
import com.bjtu.zyj.jobadmin2.service.JobService;
import com.bjtu.zyj.jobadmin2.service.ScheduleService;
import com.bjtu.zyj.jobadmin2.utils.DateTimeUtil;
import com.bjtu.zyj.jobadmin2.utils.JobUtil;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bjtu.zyj.jobadmin2.utils.DateTimeUtil.DATE_PATTERN;
import static com.bjtu.zyj.jobadmin2.utils.ServerResponseUtil.*;

@RestController
@RequestMapping("/api/job/")
public class JobController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    ScheduleService scheduleService;

    JobService jobService;

    @Autowired
    private JobPersistentService jobPersistentService;

    @Autowired
    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Autowired
    public void setJobService(JobService jobService) {
        this.jobService = jobService;
    }

    //添加任务，但不调度，因为没有指定触发器
    @PostMapping("add")
    public ServerResponse addJob(@RequestBody Task task) {
        try {
            if (jobService.isJobExist(task.getKey(), task.getGroup())) {
                return getServerResponse(JOB_WITH_SAME_NAME_EXIST, false);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        JobDataMap map = new JobDataMap();
        if (task.getDataMap() != null) {
            map.putAll(task.getDataMap());
        }
        try {
            JobDetail jobDetail = JobUtil.createJobDetail((Class<? extends Job>) Class.forName(task.getType()),
                    task.getKey(),
                    task.getGroup(),
                    map);
            jobService.addJob(jobDetail, true);
            return getServerResponse(SUCCESS, true);
        } catch (ClassNotFoundException | SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR, false);
    }

    @GetMapping("exist/{group}/{key}")
    public ServerResponse isJobExist(@PathVariable("group") String group, @PathVariable("key") String key) {
        try {
            boolean res = jobService.isJobExist(key, group);
            return getServerResponse(SUCCESS, res);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR, false);
    }

    @GetMapping("state/{group}/{key}")
    public ServerResponse getJobState(@PathVariable("group") String group, @PathVariable("key") String key) {
        try {
            JobState jobState = jobService.getJobState(key, group);
            return getServerResponse(SUCCESS, jobState);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR, null);
    }

    @GetMapping("jobs/not_schedule")
    public ServerResponse getUnScheduledJobs() {
        List<Map<String, Object>> allJobs;
        try {
            allJobs = jobService.getUnScheduledJobs();
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR, null);
        }
        return getServerResponse(SUCCESS, allJobs);
    }

    @GetMapping("jobs/scheduled")
    public ServerResponse getScheduledJobs() {
        List<Map<String, Object>> allJobs;
        try {
            allJobs = jobService.getAllScheduledJobs();
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR, null);
        }
        return getServerResponse(SUCCESS, allJobs);
    }

    @DeleteMapping("delete/{group}/{key}")
    public ServerResponse deleteJob(@PathVariable("group") String group,
                                    @PathVariable("key") String key) {
        System.out.println(group + "," + key);
        try {
            if (!jobService.isJobExist(key, group)) {
                return getServerResponse(JOB_NAME_NOT_PRESENT, false);
            }
            boolean res = jobService.deleteJob(key, group);
            return getServerResponse(SUCCESS, res);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR, false);
    }

    @GetMapping("running/{group}/{key}")
    public ServerResponse isJobRunning(@PathVariable("group") String group,
                                       @PathVariable("key") String key) {
        try {
            if (!jobService.isJobExist(key, group)) {
                return getServerResponse(JOB_NAME_NOT_PRESENT, false);
            }
            boolean res = jobService.isJobRunning(key, group);
            return getServerResponse(SUCCESS, res);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return getServerResponse(ERROR, false);
    }

    @GetMapping("types")
    public ServerResponse getAllJobTypes() {
        List<String> types = jobService.getAllJobTypes();
        return getServerResponse(SUCCESS, types);
    }

    @GetMapping("detail/{group}/{key}/{cnt}")
    public ServerResponse getJobDetails(@PathVariable("key") String key,
                                        @PathVariable("group") String group,
                                        @PathVariable("cnt") int cnt) {
        try {
            JobDataMap dataMap = jobService.getJobDetails(key, group);

            List<String> recentScheduleTime = scheduleService.getRecentScheduleTime(key, group, cnt);
            if (recentScheduleTime != null) {
                Map<String, Object> ret = Map.of(
                        "dataMap", dataMap,
                        "recent", recentScheduleTime
                );
                return getServerResponse(SUCCESS, ret);
            } else {
                Map<String, Object> ret = Map.of(
                        "dataMap", dataMap
                );
                return getServerResponse(SUCCESS, ret);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR, null);
        }
    }

    @GetMapping("groups")
    public ServerResponse getJobGroups() {
        try {
            List<String> group = jobService.getJobGroups();
            return getServerResponse(SUCCESS, group);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR, null);
        }
    }

    @GetMapping("runnings")
    public ServerResponse getRunningJobs() {

        try {
            List<JobExecutionContext> runningJobs = jobService.getRunningJobs();
            List<Map<String, String>> ret = new ArrayList<>();

            for (JobExecutionContext context : runningJobs) {
                JobKey key = context.getJobDetail().getKey();
                Map<String, String> map = Map.of(
                        "type", context.getJobDetail().getJobClass().getName(),
                        "jobName", key.getName(),
                        "jobGroup", key.getGroup(),
                        "runningTime", String.valueOf(context.getJobRunTime()),
                        "fireTime", DateTimeUtil.format(context.getFireTime(), DATE_PATTERN)
                );
                ret.add(map);
            }

            return getServerResponse(SUCCESS, ret);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR, null);
        }
    }

    @GetMapping("db")
    public ServerResponse getJobsFromDB() {
        try {
            List<Map<String, Object>> jobsFromDB = jobService.getJobsFromDB();
            return getServerResponse(SUCCESS, jobsFromDB);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return getServerResponse(ERROR, null);
        }
    }
}
