package com.bjtu.zyj.jobadmin2;

import com.bjtu.zyj.jobadmin2.repo.TriggerRepository;
import com.bjtu.zyj.jobadmin2.service.JobPersistentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;

@SpringBootTest
class JobAdmin2ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private JobPersistentService service;

    @Autowired
    private TriggerRepository repository;


    @Test
    public void test2(){
        String password = "123456";
        System.out.println(BCrypt.hashpw(password,BCrypt.gensalt()));
    }
}
