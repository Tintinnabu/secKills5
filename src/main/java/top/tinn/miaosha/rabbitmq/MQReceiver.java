package top.tinn.miaosha.rabbitmq;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import top.tinn.miaosha.domain.MiaoshaOrder;
import top.tinn.miaosha.domain.OrderInfo;
import top.tinn.miaosha.redis.RedisService;
import top.tinn.miaosha.service.GoodsService;
import top.tinn.miaosha.service.MiaoshaService;
import top.tinn.miaosha.service.OrderService;
import top.tinn.miaosha.vo.GoodsVo;

import java.io.IOException;

/**
 * @ClassName MQReceiver
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/15 13:43
 */
@Service
public class MQReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;


    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String msg){
        LOGGER.info("receivd message: " + msg);
    }

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE )
    public void receiveMiaosha(@Payload String msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        LOGGER.info("receive top.tinn.miaosha message: " + msg);
        MiaoshaMessage message = redisService.stringToBean(msg,
                MiaoshaMessage.class);

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(
                message.getGoodsId());
        int stock = goodsVo.getStockCount();
        //判断库存
        if (stock < 0){
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(
                message.getUser().getId(), message.getGoodsId());
        if(order != null) {
            //channel.basicAck(tag, false);
            return;
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(message.getUser(), goodsVo);
        /*if (orderInfo != null){
            channel.basicAck(tag, false);
        }*/
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String msg){
        LOGGER.info("topic queue1 message: " + msg);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String msg){
        LOGGER.info("topic queue2 message: " + msg);
    }

    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
    public void receiveHeader(byte[] msg){
        LOGGER.info("header queue message: " + new String(msg));
    }
}
