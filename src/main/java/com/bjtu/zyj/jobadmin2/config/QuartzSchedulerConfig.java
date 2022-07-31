package com.bjtu.zyj.jobadmin2.config;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@AllArgsConstructor
public class QuartzSchedulerConfig {

    @Bean
    //@Bean标注的方法，发方法的参数值从容器中获取
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource,ApplicationContext applicationContext) throws IOException{
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);

        factory.setDataSource(dataSource);
        System.out.println(dataSource);
        factory.setQuartzProperties(quartzProperties());

        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        factory.setJobFactory(jobFactory);
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }
 
  
}