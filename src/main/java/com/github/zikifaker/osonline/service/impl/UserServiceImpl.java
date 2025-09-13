package com.github.zikifaker.osonline.service.impl;

import com.github.zikifaker.osonline.constant.ControllerRespConstant;
import com.github.zikifaker.osonline.dto.UserLoginDTO;
import com.github.zikifaker.osonline.dto.UserRegisterDTO;
import com.github.zikifaker.osonline.entity.User;
import com.github.zikifaker.osonline.exception.AccountNotFoundException;
import com.github.zikifaker.osonline.exception.PasswordErrorException;
import com.github.zikifaker.osonline.mapper.UserMapper;
import com.github.zikifaker.osonline.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        User user = userMapper.getUserByName(username);
        // 若用户不存在
        if (user == null) {
            throw new AccountNotFoundException(ControllerRespConstant.ACCOUNT_NOT_FOUND);
        }

        // 校验密码
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new PasswordErrorException(ControllerRespConstant.PASSWORD_ERROR);
        }

        return user;
    }

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        User user = new User();
        // 拷贝属性值
        BeanUtils.copyProperties(userRegisterDTO, user);
        // MD5加密密码
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        userMapper.saveUser(user);
    }
}
