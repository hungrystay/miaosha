package com.nihan.seckill.service;
import com.nihan.seckill.domain.MiaoshaOrder;
import com.nihan.seckill.domain.MiaoshaUser;
import com.nihan.seckill.domain.OrderInfo;
import com.nihan.seckill.redis.MiaoshaKey;
import com.nihan.seckill.redis.RedisService;
import com.nihan.seckill.util.MD5Util;
import com.nihan.seckill.util.UUIDUtil;
import com.nihan.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	RedisService redisService;

	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		boolean success = goodsService.reduceStock(goods);
		if(success) {
			//order_info maiosha_order
			return orderService.createOrder(user, goods);
		}else {
			setGoodsOver(goods.getId());
			return null;
		}
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
	}
}
