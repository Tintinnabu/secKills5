package top.tinn.miaosha.service;

import top.tinn.miaosha.domain.MiaoshaUser;
import top.tinn.miaosha.vo.LoginVo;

import javax.servlet.http.HttpServletResponse;

/**
 * @InterfaceName MiaoshaUserService
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 14:13
 */
public interface MiaoshaUserService {
    MiaoshaUser getById(long id);

    String login(HttpServletResponse response, LoginVo loginVo);

    MiaoshaUser getByToken(HttpServletResponse response, String token);

    boolean updatePassword(String token, long id, String passwordNew);
}
