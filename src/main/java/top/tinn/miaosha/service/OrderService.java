package top.tinn.miaosha.service;

import org.springframework.transaction.annotation.Transactional;
import top.tinn.miaosha.domain.MiaoshaOrder;
import top.tinn.miaosha.domain.MiaoshaUser;
import top.tinn.miaosha.domain.OrderInfo;
import top.tinn.miaosha.vo.GoodsVo;

/**
 * @InterfaceName OrderService
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:54
 */
public interface OrderService {

    MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId);

    @Transactional
    OrderInfo createOrder(MiaoshaUser user, GoodsVo goods);

    OrderInfo getOrderById(long orderId);

    void deleteOrders();

    void deleteOrdersInRedis();
}
