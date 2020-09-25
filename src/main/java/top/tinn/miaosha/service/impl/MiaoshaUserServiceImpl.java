package top.tinn.miaosha.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.tinn.miaosha.dao.MiaoshaUserDao;
import top.tinn.miaosha.domain.MiaoshaUser;
import top.tinn.miaosha.exception.GlobalException;
import top.tinn.miaosha.redis.MiaoshaUserKey;
import top.tinn.miaosha.redis.RedisService;
import top.tinn.miaosha.result.CodeMsg;
import top.tinn.miaosha.service.MiaoshaUserService;
import top.tinn.miaosha.util.MD5Util;
import top.tinn.miaosha.util.UUIDUtil;
import top.tinn.miaosha.vo.LoginVo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName MiaoshaUserServiceImpl
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:14
 */
@Service
public class MiaoshaUserServiceImpl implements MiaoshaUserService {
    @Autowired
    private MiaoshaUserDao miaoshaUserDao;
    @Autowired
    private RedisService redisService;

    public static final String COOKI_NAME_TOKEN = "token";

    @Override
    public MiaoshaUser getById(long id) {
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if (user != null) return user;
        //取数据库
        user =  miaoshaUserDao.getById(id);
        if (user != null)
            redisService.set(MiaoshaUserKey.getById, "" + id, user);
        return user;
    }

    @Override
    public String login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号用户是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if(!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    @Override
    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //
        if (user != null){
            addCookie(response, token, user);
        }
        return user;
    }

    @Override
    public boolean updatePassword(String token, long id, String formPass) {
        MiaoshaUser user = getById(id);
        if (user == null) throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        //更新数据库
        MiaoshaUser updateUser = new MiaoshaUser();
        updateUser.setId(id);
        updateUser.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        miaoshaUserDao.update(updateUser);
        //处理缓存
        redisService.delete(MiaoshaUserKey.getById, "" + id);
        user.setPassword(updateUser.getPassword());
        redisService.set(MiaoshaUserKey.token, token, user);
        return true;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
