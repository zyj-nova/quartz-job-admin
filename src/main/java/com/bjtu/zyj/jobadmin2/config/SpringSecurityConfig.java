package com.bjtu.zyj.jobadmin2.config;

import com.bjtu.zyj.jobadmin2.login.filter.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// 这个注解默认包含了@Configuration注解
@EnableWebSecurity
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter{

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Override
    public void configure(HttpSecurity http) throws Exception {

        http.formLogin()
                .loginProcessingUrl("/api/login")
                .successHandler(new CustomAuthenticationSuccessHandler());

        http.authorizeRequests().antMatchers("/","/api/login").permitAll()
                .antMatchers("/api/job/**").authenticated()
                .antMatchers("/api/trigger/**").authenticated();


        http.csrf().disable();
        http.logout().logoutSuccessUrl("/");
    }

}
