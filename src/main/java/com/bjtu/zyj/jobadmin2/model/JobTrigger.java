package com.bjtu.zyj.jobadmin2.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("job_trigger")
public class JobTrigger {

    @TableId(type = IdType.AUTO)
    private int id;

    private int triggerId;

    private int jobId;

}
