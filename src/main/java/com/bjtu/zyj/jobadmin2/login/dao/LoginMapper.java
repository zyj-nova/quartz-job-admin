package com.bjtu.zyj.jobadmin2.login.dao;

import com.bjtu.zyj.jobadmin2.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginMapper extends BaseMapper<User> {

    @Select("select * from app_user where user_name = #{username}")
    User selectUserByUserName(@Param("username") String username);
}
