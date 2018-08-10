package com.nihan.seckill.service;

import com.nihan.seckill.dao.MiaoshaUserDao;
import com.nihan.seckill.domain.MiaoshaUser;
import com.nihan.seckill.exception.GlobalException;
import com.nihan.seckill.redis.MiaoshaUserKey;
import com.nihan.seckill.redis.RedisService;
import com.nihan.seckill.result.CodeMsg;
import com.nihan.seckill.util.MD5Util;
import com.nihan.seckill.util.UUIDUtil;
import com.nihan.seckill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {
	
	
	public static final String COOKI_NAME_TOKEN = "token";

	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;
	
//	public MiaoshaUser getById(long id) {
//		//取缓存
//		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
//		if(user != null) {
//			return user;
//		}
//		//取数据库
//		user = miaoshaUserDao.getById(id);
//		if(user != null) {
//			redisService.set(MiaoshaUserKey.getById, ""+id, user);
//		}
//		return user;
//	}

	public MiaoshaUser getById(long id) {
		//取数据库
		MiaoshaUser user = miaoshaUserDao.getById(id);
		return user;
	}

	// http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323

	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}

	public String login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成cookie
		String token	 = UUIDUtil.uuid();
		System.out.println("token =======" + token);
		//将user信息以token为key，设置到redis中
		redisService.set(MiaoshaUserKey.token, token, user);
		addCookie(response, token, user);
		return token;
	}
	
	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}

}
