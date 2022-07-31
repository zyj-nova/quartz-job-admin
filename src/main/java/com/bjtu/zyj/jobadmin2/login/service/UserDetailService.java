package com.bjtu.zyj.jobadmin2.login.service;

import com.bjtu.zyj.jobadmin2.login.dao.LoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    private LoginMapper loginMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        com.bjtu.zyj.jobadmin2.model.User user = loginMapper.selectUserByUserName(username);
        if (user == null){
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        String password = user.getPassword();
        String name = user.getUsername();
        System.out.println(name);
        return new User(name,password, AuthorityUtils
                .commaSeparatedStringToAuthorityList("admin"));
    }
}
