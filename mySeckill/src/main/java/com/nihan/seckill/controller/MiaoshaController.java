package com.nihan.seckill.controller;

import com.nihan.seckill.domain.MiaoshaOrder;
import com.nihan.seckill.domain.MiaoshaUser;
import com.nihan.seckill.domain.OrderInfo;
import com.nihan.seckill.rabbitmq.MQSender;
import com.nihan.seckill.rabbitmq.MiaoshaMessage;
import com.nihan.seckill.redis.GoodsKey;
import com.nihan.seckill.redis.RedisService;
import com.nihan.seckill.result.CodeMsg;
import com.nihan.seckill.result.Result;
import com.nihan.seckill.service.GoodsService;
import com.nihan.seckill.service.MiaoshaService;
import com.nihan.seckill.service.MiaoshaUserService;
import com.nihan.seckill.service.OrderService;
import com.nihan.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	MiaoshaService miaoshaService;

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	RedisService redisService;

	@Autowired
	MQSender sender;

	private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();

	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> home() {
		return Result.success("Hello，world");
	}

	/**
	 *  100
	 *  QPS:  470
	 *
	 *  1000
	 *  QPS: 763
	 *
	 *  2000
	 *  QPS: 968
	 *
	 *  3000
	 *  QPS:  1440
	 *
	 *  4000
	 *  QPS: 1490
	 *
	 *  5000
	 *  QPS:  1551
	 *
	 *  5000*2
	 *  QPS: 2190
	 *
	 *  5000*3
	 *  QPS:  1935
	 *
	 *  5000*4
	 *  QPS: 	机器out of memory
	 *
	 *
	 *
	 *
	 *  5000*1
	 *  QPS: 636
	 *
	 *	5000*2
	 *  QPS:
	 *
	 *
	 *
	 */

	@RequestMapping(value = "/do_miaosha", method=RequestMethod.POST)
	@ResponseBody
	public Result<Object> testMiaosha(HttpServletResponse response, Model model,
										 @CookieValue(value=MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
										 @RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN, required= false) String paramToken,
										 @RequestParam("goodsId")long goodsId) {

		System.out.println("cookieToken========" + cookieToken);
		System.out.println("paramToken=========" + paramToken);
		if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
			return Result.error(CodeMsg.SESSION_ERROR);
		}


		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);

		//判断库存
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
		System.out.println("goods=========");
		System.out.println(goods);
		int stock = goods.getStockCount();
		if(stock <= 0) {
			model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//减库存 下订单 写入秒杀订单
		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
		if(orderInfo == null){
			System.out.println("orderInfo ============ null null null!");
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		model.addAttribute("orderInfo", orderInfo);
		model.addAttribute("goods", goods);
		return Result.success(orderInfo, 0);
	}


	@RequestMapping("/do_miaosha1")
	public String testMiaosha(Model model, HttpServletResponse response,
							  @CookieValue(value=MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
							  @RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN, required= false) String paramToken,
							  @RequestParam("goodsId")long goodsId) {

		System.out.println("cookieToken========" + cookieToken);
		System.out.println("paramToken=========" + paramToken);
		if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
			return "login";
		}

		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);


		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
		int stock = goods.getStockCount();
		if(stock <= 0) {
			model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
			return "miaosha_fail";
		}

		System.out.println("=========user==========");
		System.out.println(user);
		//判断是否已经秒杀到了

		System.out.println("=========orderService===========");
		System.out.println(orderService);
		System.out.println("=========user.getId()========");
		System.out.println(user.getId());
		System.out.println("==========goodsId");
		System.out.println(goodsId);
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
//			return Result.error(CodeMsg.REPEATE_MIAOSHA);
			model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
			return "miaosha_fail";
		}
		//减库存 下订单 写入秒杀订单
		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
		System.out.println("============orderInfo=========");
		model.addAttribute("orderInfo", orderInfo);
		model.addAttribute("goods", goods);
		return "order_detail";
	}


	@RequestMapping("/do_miaosha2")
	public String miaosha( HttpServletResponse response, Model model,
							   @CookieValue(value=MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
							   @RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN, required= false) String paramToken,
							   @RequestParam("goodsId")long goodsId) {
		if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
			return "login";
		}

		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);

		//预减库存
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);//10
		if(stock < 0) {
			localOverMap.put(goodsId, true);
			return "miaosha_fail";
		}

		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
//			return Result.error(CodeMsg.REPEATE_MIAOSHA);
			model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
			return "miaosha_fail";
		}


//		OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//		if(orderInfo == null){
//			System.out.println("orderInfo ============ null null null!");
//			return Result.error(CodeMsg.REPEATE_MIAOSHA);
//		}
//		model.addAttribute("orderInfo", orderInfo);
//		model.addAttribute("goods", goods);
//
//		return "order_detail";

		//入队
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(mm);
		return "miaosha_in_process";
	}

	/**
	 * orderId：成功
	 * -1：秒杀失败
	 * 0： 排队中
	 * */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
									  @RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}

	/**
	 * 系统初始化
	 * */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if(goodsList == null) {
			return;
		}
		for(GoodsVo goods : goodsList) {
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}
}
