package top.tinn.miaosha.controller;

import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import top.tinn.miaosha.access.AccessLimit;
import top.tinn.miaosha.domain.MiaoshaOrder;
import top.tinn.miaosha.domain.MiaoshaUser;
import top.tinn.miaosha.domain.OrderInfo;
import top.tinn.miaosha.rabbitmq.MQSender;
import top.tinn.miaosha.rabbitmq.MiaoshaMessage;
import top.tinn.miaosha.redis.GoodsKey;
import top.tinn.miaosha.redis.MiaoshaKey;
import top.tinn.miaosha.redis.OrderKey;
import top.tinn.miaosha.redis.RedisService;
import top.tinn.miaosha.result.CodeMsg;
import top.tinn.miaosha.result.Result;
import top.tinn.miaosha.service.GoodsService;
import top.tinn.miaosha.service.MiaoshaService;
import top.tinn.miaosha.service.MiaoshaUserService;
import top.tinn.miaosha.service.OrderService;
import top.tinn.miaosha.vo.GoodsVo;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
	private static Logger LOGGER = LoggerFactory.getLogger(MiaoshaController.class);

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;

	@Autowired
	private MQSender sender;

	private Map<Long, Boolean> localOverMap;

	/**
	 * initialize
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		//redisService.clear();
		int count = 10;
		String className = this.getClass().getSimpleName();
		LOGGER.info("{}: initial miaosha count = {}", className,count);

		goodsService.refreshMiaoshaCount(count);
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		localOverMap = new HashMap<>();
		if (goodsList == null) return;
		LOGGER.info("{}：delete orders from MySQL and Redis", className);
		orderService.deleteOrders();
		orderService.deleteOrdersInRedis();

		//加载商品库存
		for (GoodsVo goods : goodsList){
			localOverMap.put(goods.getId(), false);
			redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
		}
		LOGGER.info("{}: load miaosha count to Redis", className);

	}

	@RequestMapping(value="/reset", method=RequestMethod.GET)
	@ResponseBody
	public Result<Boolean> reset(Model model) {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for(GoodsVo goods : goodsList) {
			goods.setStockCount(10);
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
			localOverMap.put(goods.getId(), false);
		}
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
		redisService.delete(MiaoshaKey.isGoodsOver);
		miaoshaService.reset(goodsList);
		return Result.success(true);
	}

	@RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
	@ResponseBody
	public Result<Integer> miaoshaForJMeter(Model model,
											MiaoshaUser user,
											@RequestParam("goodsId")long goodsId){
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//验证path
		//JMeter不需要验证path
		/*boolean check = miaoshaService.checkPath(user, goodsId, path);
		if (!check){
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}*/

		//内存标记
		if (localOverMap.get(goodsId)) return Result.error(CodeMsg.MIAO_SHA_OVER);

		//预减库存
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
		if (stock < 0){
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//入队
		MiaoshaMessage message = new MiaoshaMessage();
		message.setUser(user);
		message.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(message);
		return Result.success(0);//排队
	}

    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
	@ResponseBody
    @AccessLimit(seconds = 60, maxCount = 1, needLogin = true)
    public Result<Integer> miaosha(Model model,
								   MiaoshaUser user,
								   @RequestParam("goodsId")long goodsId,
								   @PathVariable("path") String path) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
		//验证path
		boolean check = miaoshaService.checkPath(user, goodsId, path);
    	if (!check){
    		return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}

    	//内存标记
    	if (localOverMap.get(goodsId)) return Result.error(CodeMsg.MIAO_SHA_OVER);

    	//预减库存
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
		if (stock < 0){
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//入队
		MiaoshaMessage message = new MiaoshaMessage();
		message.setUser(user);
		message.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(message);
		return Result.success(0);//排队

    	/*
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId); //一个用户发出多个请求
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
			return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.top.tinn.miaosha(user, goods);
    	return Result.success(orderInfo);
    	*/
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
		long result  = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}

	@AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
	@RequestMapping(value = "/path", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaPath(MiaoshaUser user,
										 @RequestParam("goodsId") long goodsId,
										 @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode){
		if (user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
		if (!check){
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		String path = miaoshaService.createMiaoshaPath(user, goodsId);
		return Result.success(path);
	}

	@RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaVerifyCode(HttpServletResponse response,
											   MiaoshaUser user,
											   @RequestParam("goodsId") long goodsId){
		if (user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		try {
			BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		}catch (Exception e){
			e.printStackTrace();
			return Result.error(CodeMsg.MIAOSHA_FAIL);
		}
	}
}
