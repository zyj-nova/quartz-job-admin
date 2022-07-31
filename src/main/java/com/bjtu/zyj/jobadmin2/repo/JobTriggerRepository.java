package com.bjtu.zyj.jobadmin2.repo;

import com.bjtu.zyj.jobadmin2.model.JobTrigger;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface JobTriggerRepository extends BaseMapper<JobTrigger> {

    @Select("select trigger_id from job_trigger where job_id = #{id}")
    public int slectTriggerIdByJobId(@Param("id") int id);
}
