package top.tinn.miaosha.rabbitmq;

import top.tinn.miaosha.domain.MiaoshaUser;

/**
 * @ClassName MiaoshaMessage
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/15 14:40
 */
public class MiaoshaMessage {

    private MiaoshaUser user;
    private long goodsId;

    public MiaoshaUser getUser() {
        return user;
    }

    public void setUser(MiaoshaUser user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}
