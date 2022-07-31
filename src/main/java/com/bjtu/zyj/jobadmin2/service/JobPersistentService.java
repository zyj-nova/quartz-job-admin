package com.bjtu.zyj.jobadmin2.service;

import com.bjtu.zyj.jobadmin2.model.AppJobDetail;
import com.bjtu.zyj.jobadmin2.repo.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobPersistentService {

    @Autowired
    private JobRepository jobRepository;

    @Transactional
    public int saveJob(AppJobDetail jobDetail){
        jobRepository.insert(jobDetail);
        return jobDetail.getId();
    }

    public void deleteJob(AppJobDetail jobDetail){
        jobRepository.deleteById(jobDetail.getId());
    }

    @Transactional
    public AppJobDetail selectJobByNameAndGroup(String key, String group){
        return jobRepository.selectJobByNameAndGroup(key,group);
    }

    @Transactional
    public List<AppJobDetail> getAllJobs(){
        return jobRepository.getAllJobs();
    }
}
