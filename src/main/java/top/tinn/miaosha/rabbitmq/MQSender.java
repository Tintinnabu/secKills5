package top.tinn.miaosha.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.tinn.miaosha.redis.RedisService;

/**
 * @ClassName MQSender
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/15 13:43
 */
@Service
public class MQSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private RedisService redisService;

    public void send(Object message){
        String msg = redisService.beanToString(message);
        amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
        LOGGER.info("send message: " + msg);
    }

    public void sendTopic(Object message){
        String msg = redisService.beanToString(message);
        LOGGER.info("send topic message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, MQConfig.ROUTING_KEY1, message + "1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, MQConfig.ROUTING_KEY2, message + "2");
    }

    public void sendFanout(Object message){
        String msg = redisService.beanToString(message);
        LOGGER.info("send fanout message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"", message);
    }

    public void sendHeader(Object message){
        String msg = redisService.beanToString(message);
        LOGGER.info("send header message: " + msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("key1", "value1");
        Message obj = new Message(msg.getBytes(), properties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"", obj);
    }

    public void sendMiaoshaMessage(MiaoshaMessage message) {
        String msg = redisService.beanToString(message);
        LOGGER.info("send top.tinn.miaosha message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
    }
}
