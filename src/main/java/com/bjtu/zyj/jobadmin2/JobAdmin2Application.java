package com.bjtu.zyj.jobadmin2;

import com.bjtu.zyj.jobadmin2.listeners.QuartzJobListener;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import org.mybatis.spring.annotation.MapperScan;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

@SpringBootApplication
@MapperScan({"com.abchina.zyj.jobadmin2.repo","com.abchina.zyj.jobadmin2.login.dao"})
public class JobAdmin2Application {

    public static void main(String[] args) {
        SpringApplication.run(JobAdmin2Application.class, args);
    }

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource druid(){
        return new DruidDataSource();
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {
            scheduler.getListenerManager().addJobListener(new QuartzJobListener(), GroupMatcher.anyGroup());
            //创建并注册一个全局的Trigger Listener
            //scheduler.getListenerManager().addTriggerListener(new QuartzTriggerListener());
            //scheduler.getListenerManager().addSchedulerListener(new QuartzSchedulerListener());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return scheduler;
    }

    @Bean
    public ServletRegistrationBean statViewServlet(){

        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        bean.addInitParameter("allow","*");
        //bean.addInitParameter("loginUsername","admin");
        //bean.addInitParameter("loginPassword","123");
        //是否能够重置数据.
        bean.addInitParameter("resetEnable","false");
        return bean;

    }
}
