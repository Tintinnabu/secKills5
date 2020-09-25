package top.tinn.miaosha.service;

import top.tinn.miaosha.vo.GoodsVo;

import java.util.List;

/**
 * @ClassName GoodsService
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:54
 */
public interface GoodsService {

    public List<GoodsVo> listGoodsVo();

    public GoodsVo getGoodsVoByGoodsId(long goodsId);

    public boolean reduceStock(GoodsVo goods);

    void resetStock(List<GoodsVo> goodsList);

    int refreshMiaoshaCount(int count);
}
