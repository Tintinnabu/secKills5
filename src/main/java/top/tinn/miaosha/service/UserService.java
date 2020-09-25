package top.tinn.miaosha.service;

import org.springframework.transaction.annotation.Transactional;
import top.tinn.miaosha.domain.User;

/**
 * @InterfaceName UserService
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 10:33
 */
public interface UserService {

    User getUserById(Integer id);

    @Transactional
    boolean tx();
}
