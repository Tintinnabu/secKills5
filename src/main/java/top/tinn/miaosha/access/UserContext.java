package top.tinn.miaosha.access;

import top.tinn.miaosha.domain.MiaoshaUser;

/**
 * @ClassName UserContext
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/15 20:35
 */
public class UserContext {

    private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<MiaoshaUser>();

    public static void setUser(MiaoshaUser user) {

        //拿到当前线程的ThreadLocalMap
        //把当前的ThreadLocal<MiaoshaUser>对象作为key,user作为value存入map
        userHolder.set(user);
    }

    public static MiaoshaUser getUser() {
        //开放定址法
        MiaoshaUser miaoshaUser = userHolder.get();
        userHolder.remove();
        return miaoshaUser;
    }
}
