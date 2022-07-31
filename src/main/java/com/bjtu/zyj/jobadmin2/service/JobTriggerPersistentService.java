package com.bjtu.zyj.jobadmin2.service;

import com.bjtu.zyj.jobadmin2.model.JobTrigger;
import com.bjtu.zyj.jobadmin2.repo.JobTriggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobTriggerPersistentService {
    @Autowired
    private JobTriggerRepository jobTriggerRepository;

    public int save(JobTrigger jobTrigger){
        return jobTriggerRepository.insert(jobTrigger);
    }

    @Transactional
    public int selectTriggerByJobId(int id){
        return jobTriggerRepository.slectTriggerIdByJobId(id);
    }
}
