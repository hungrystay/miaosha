package com.nihan.seckill.controller;

import com.nihan.seckill.domain.MiaoshaUser;
import com.nihan.seckill.domain.OrderInfo;
import com.nihan.seckill.result.CodeMsg;
import com.nihan.seckill.result.Result;
import com.nihan.seckill.service.GoodsService;
import com.nihan.seckill.service.MiaoshaUserService;
import com.nihan.seckill.service.OrderService;
import com.nihan.seckill.vo.GoodsVo;
import com.nihan.seckill.vo.OrderDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model,
									  @CookieValue(value=MiaoshaUserService.COOKI_NAME_TOKEN, required = false) String cookieToken,
									  @RequestParam(value=MiaoshaUserService.COOKI_NAME_TOKEN, required= false) String paramToken,
									  @RequestParam("orderId") long orderId) {

		if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
			Result.error(CodeMsg.SESSION_ERROR);
		}
//		if(user == null) {
//    		return Result.error(CodeMsg.SESSION_ERROR);
//    	}
    	OrderInfo order = orderService.getOrderById(orderId);
    	if(order == null) {
    		return Result.error(CodeMsg.ORDER_NOT_EXIST);
    	}
    	long goodsId = order.getGoodsId();
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	OrderDetailVo vo = new OrderDetailVo();
    	vo.setOrder(order);
    	vo.setGoods(goods);
    	return Result.success(vo);
    }
    
}
