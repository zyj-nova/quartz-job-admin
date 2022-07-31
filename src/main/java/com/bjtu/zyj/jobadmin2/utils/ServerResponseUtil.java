package com.bjtu.zyj.jobadmin2.utils;

import com.bjtu.zyj.jobadmin2.dto.ServerResponse;

public class ServerResponseUtil {
	
	//SPECIFIC ERROR CODES
	public static final int JOB_WITH_SAME_NAME_EXIST = 501;
	public static final int JOB_NAME_NOT_PRESENT = 502;

	public static final int TRIGGER_NAME_NOT_PRESENT = 503;

	public static final int JOB_ALREADY_IN_RUNNING_STATE = 510;
	
	public static final int JOB_NOT_IN_PAUSED_STATE = 520;
	public static final int JOB_NOT_IN_RUNNING_STATE = 521;
	
	public static final int JOB_DOESNT_EXIST = 500;
	
	//GENERIC ERROR
	public static final int ERROR = 600;
	
	//SUCCESS CODES
	public static final int SUCCESS = 200;


	public static ServerResponse getServerResponse(int responseCode, Object data){
		ServerResponse serverResponse = new ServerResponse();
		serverResponse.setStatusCode(responseCode);
		serverResponse.setData(data);
		return serverResponse;
	}
}
