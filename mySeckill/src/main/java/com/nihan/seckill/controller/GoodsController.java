package com.nihan.seckill.controller;

import com.nihan.seckill.domain.MiaoshaUser;
import com.nihan.seckill.redis.RedisService;
import com.nihan.seckill.service.GoodsService;
import com.nihan.seckill.service.MiaoshaUserService;
import com.nihan.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;

	@Autowired
    ApplicationContext applicationContext;

	@RequestMapping(value="/to_list")
	public String list(Session session, HttpServletResponse response, Model model,
					   @CookieValue(value=MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
					   @RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN, required= false) String paramToken) {
		Session.Cookie cookie = session.getCookie();

		System.out.println("=========cookie=========");
		System.out.println(cookie.getName());

		System.out.println("cookieToken========" + cookieToken);
		System.out.println("paramToken=========" + paramToken);
		if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
			return "login";
		}

		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);
		System.out.println("user============" + user);

		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		model.addAttribute("user", user);
		return "goods_list";
	}

	@RequestMapping(value="/to_detail/{goodsId}")
	public String detail(HttpServletResponse response, Model model,
                         @CookieValue(value=MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
                         @RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN, required= false) String paramToken,
                         @PathVariable("goodsId")long goodsId) {
		System.out.println("cookieToken========" + cookieToken);
		System.out.println("paramToken=========" + paramToken);
		if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
			return "login";
		}

		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);
		System.out.println("user============" + user);
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

		model.addAttribute("user", user);
		model.addAttribute("goods", goods);

		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();

		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		return "goods_detail";
	}
}
