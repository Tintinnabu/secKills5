package top.tinn.miaosha.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.tinn.miaosha.domain.MiaoshaOrder;
import top.tinn.miaosha.domain.MiaoshaUser;
import top.tinn.miaosha.domain.OrderInfo;
import top.tinn.miaosha.redis.MiaoshaKey;
import top.tinn.miaosha.redis.RedisService;
import top.tinn.miaosha.service.GoodsService;
import top.tinn.miaosha.service.MiaoshaService;
import top.tinn.miaosha.service.OrderService;
import top.tinn.miaosha.util.MD5Util;
import top.tinn.miaosha.util.UUIDUtil;
import top.tinn.miaosha.vo.GoodsVo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @ClassName MiaoshaServiceImpl
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:58
 */
@Service
public class MiaoshaServiceImpl implements MiaoshaService {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisService redisService;

    @Transactional
    @Override
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);
        //order_info maiosha_order
        if (success){
            OrderInfo info = orderService.createOrder(user, goods);
            if (info != null){
                return info;
            }
        }
        setGoodsOver(goods.getId());
        return null;
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver, "" + goodsId, true);
    }

    @Override
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if (order != null){
            return order.getOrderId();
        }else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver) return -1;
            return 0;
        }
    }

    @Override
    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    @Override
    public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0){
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, str);
        return str;
    }

    @Override
    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0){
            return false;
        }
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode,
                "" + user.getId() + "," + goodsId, Integer.class);
        if (codeOld == null || !codeOld.equals(verifyCode)){
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, "" + user.getId() + "," + goodsId);
        return true;
    }

    @Override
    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0){
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, "" + user.getId() + "," + goodsId, rnd);
        return image;
    }

    @Override
    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
        if (user == null || goodsId <= 0 || path == null){
            return false;
        }
        String oldPath = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(oldPath);
    }

    /**
     * 9 + 6 * 2 - 5 = 9 + 12 - 5 = 16
     * @param verifyCode
     * @return
     */
    private int calc(String verifyCode) {
        return dfs(verifyCode, new int[1]);
    }

    private int dfs(String verifyCode, int[] i) {
        Deque<Integer> stack = new LinkedList<>();
        int num = 0;
        //前一个运算符
        char op = '+';
        for (; i[0] < verifyCode.length(); i[0]++){
            //当前的char
            char ch = verifyCode.charAt(i[0]);
            if (Character.isDigit(ch)){
                num = 10 * num + ch - '0';
            }
            if (!Character.isDigit(ch) && ch != ' ' || i[0] == verifyCode.length() - 1){
                switch (op){
                    case '+':
                        stack.push(num);
                        break;
                    case '-':
                        stack.push(-num);
                        break;
                    case '*':
                        int pre = stack.pop();
                        stack.push(pre * num);
                        break;
                }
                num = 0;
                op = ch;
            }
        }
        int res = 0;
        while (!stack.isEmpty()){
            res += stack.pop();
        }
        return res;
    }

    public static void main(String[] args) {
        MiaoshaServiceImpl service = new MiaoshaServiceImpl();
        System.out.println(service.calc("8+7*5"));
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver, "" + goodsId);
    }
}
