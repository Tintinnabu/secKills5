package top.tinn.miaosha.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.tinn.miaosha.domain.User;

/**
 * @InterfaceName UserDao
 * @Description
 * @Author Tintinnabu
 * @Date 2020/5/14 10:30
 */
@Mapper
public interface UserDao {

    @Select("select * from user where id = #{id}")
    public User getUserById(@Param("id") Integer id);

    @Insert("insert into user(id, name) values (#{id}, #{name})")
    int insert(User user);
}
