package top.tinn.miaosha.redis;

/**
 * @ClassName GoodsKey
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 21:37
 */
public class GoodsKey extends BasePrefix {
    private GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    //缓存60s
    public static GoodsKey getGoodsList = new GoodsKey(60, "gl");
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");
    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0, "gs");

}
