package com.nihan.seckill.access;

import com.nihan.seckill.domain.MiaoshaUser;

import java.util.HashMap;
import java.util.Map;

public class UserContext {
	
	private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();
	
	public static void setUser(MiaoshaUser user) {
		userHolder.set(user);
	}
	
	public static MiaoshaUser getUser() {
		return userHolder.get();
	}


	Map map = new HashMap();
}
