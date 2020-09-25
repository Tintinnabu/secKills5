package top.tinn.miaosha.service;

import top.tinn.miaosha.domain.MiaoshaUser;
import top.tinn.miaosha.domain.OrderInfo;
import top.tinn.miaosha.vo.GoodsVo;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @InterfaceName MiaoshaService
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:54
 */
public interface MiaoshaService {

    OrderInfo miaosha(MiaoshaUser user, GoodsVo goods);

    long getMiaoshaResult(Long id, long goodsId);

    void reset(List<GoodsVo> goodsList);

    String createMiaoshaPath(MiaoshaUser user, long goodsId);

    boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode);

    BufferedImage createVerifyCode(MiaoshaUser user, long goodsId);

    boolean checkPath(MiaoshaUser user, long goodsId, String path);
}
