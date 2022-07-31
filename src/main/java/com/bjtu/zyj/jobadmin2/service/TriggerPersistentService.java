package com.bjtu.zyj.jobadmin2.service;

import com.bjtu.zyj.jobadmin2.model.AppTrigger;
import com.bjtu.zyj.jobadmin2.repo.TriggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TriggerPersistentService {
    @Autowired
    private TriggerRepository repository;

    @Transactional
    public int saveTrigger(AppTrigger trigger){
        repository.insert(trigger);
        return trigger.getId();
    }

    @Transactional
    public AppTrigger selectTriggerById(int id){
        return repository.selectById(id);
    }
}
