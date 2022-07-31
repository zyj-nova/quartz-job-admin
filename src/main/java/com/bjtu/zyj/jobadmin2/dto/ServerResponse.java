package com.bjtu.zyj.jobadmin2.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 12222L;
    private int statusCode;
    private Object data;
}
