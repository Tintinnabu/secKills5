package top.tinn.miaosha.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.tinn.miaosha.dao.GoodsDao;
import top.tinn.miaosha.domain.MiaoshaGoods;
import top.tinn.miaosha.service.GoodsService;
import top.tinn.miaosha.vo.GoodsVo;

import java.util.List;

/**
 * @ClassName GoodsServiceImpl
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:55
 */
@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;
    @Override
    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    @Override
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    @Override
    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods goods1 = new MiaoshaGoods();
        goods1.setGoodsId(goods.getId());
        goodsDao.reduceStock(goods1);
        return true;
    }

    @Override
    public void resetStock(List<GoodsVo> goodsList) {
        for(GoodsVo goods : goodsList ) {
            MiaoshaGoods g = new MiaoshaGoods();
            g.setGoodsId(goods.getId());
            g.setStockCount(goods.getStockCount());
            goodsDao.resetStock(g);
        }
    }

    @Override
    public int refreshMiaoshaCount(int count) {
        return goodsDao.refreshMiaoshaCount(count);
    }
}
