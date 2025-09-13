package com.github.zikifaker.osonline.mapper;

import com.github.zikifaker.osonline.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查找用户
     *
     * @param username
     * @return
     */
    @Select("SELECT * FROM `user` WHERE `username` = #{username}")
    User getUserByName(String username);


    /**
     * 新增用户
     *
     * @param user
     */
    void saveUser(User user);
}
