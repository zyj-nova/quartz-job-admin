package com.bjtu.zyj.jobadmin2.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class JobPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        String packageName = bean.getClass().getPackage().getName();
        if (packageName.equals("com.abchina.zyj.jobadmin2.jobs")){
            //放到job缓存中
            JobCache.jobCache.add(bean.getClass().getName());
        }
        return bean;
    }
}
