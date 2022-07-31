package com.bjtu.zyj.jobadmin2.repo;


import com.bjtu.zyj.jobadmin2.model.AppJobDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends BaseMapper<AppJobDetail> {

    @Select("select * from app_job where job_key = #{key} and job_group = #{group}")
    AppJobDetail selectJobByNameAndGroup(@Param("key") String key, @Param("group") String group);

    @Select("selct * from app_job")
    List<AppJobDetail> getAllJobs();
}
