package top.tinn.miaosha.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.tinn.miaosha.dao.UserDao;
import top.tinn.miaosha.domain.User;
import top.tinn.miaosha.service.UserService;

/**
 * @ClassName UserServiceImpl
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 10:33
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public User getUserById(Integer id) {
        return userDao.getUserById(id);
    }

    @Override
    public boolean tx() {
        User user1 = new User();
        user1.setId(2);
        user1.setName("22");
        userDao.insert(user1);

        User user2 = new User();
        user2.setId(1);
        user2.setName("111");
        userDao.insert(user2);
        return true;
    }
}
