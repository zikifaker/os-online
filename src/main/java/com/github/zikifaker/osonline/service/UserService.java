package com.github.zikifaker.osonline.service;

import com.github.zikifaker.osonline.dto.UserLoginDTO;
import com.github.zikifaker.osonline.dto.UserRegisterDTO;
import com.github.zikifaker.osonline.entity.User;

public interface UserService {

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);


    /**
     * 用户注册
     * @param userRegisterDTO
     */
    void register(UserRegisterDTO userRegisterDTO);
}
