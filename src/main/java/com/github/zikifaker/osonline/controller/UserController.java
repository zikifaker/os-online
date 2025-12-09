package com.github.zikifaker.osonline.controller;

import com.github.zikifaker.osonline.config.JWTConfig;
import com.github.zikifaker.osonline.constant.JWTClaimsConstant;
import com.github.zikifaker.osonline.constant.ControllerRespConstant;
import com.github.zikifaker.osonline.dto.UserLoginDTO;
import com.github.zikifaker.osonline.dto.UserRegisterDTO;
import com.github.zikifaker.osonline.vo.Response;
import com.github.zikifaker.osonline.entity.User;
import com.github.zikifaker.osonline.service.UserService;
import com.github.zikifaker.osonline.utils.JWTUtil;
import com.github.zikifaker.osonline.vo.UserLoginVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JWTConfig jwtConfig;

    /**
     * 登录
     *
     * @param userLoginDTO 用户登录信息，包括用户名和密码
     * @return
     */
    @PostMapping("/login")
    public Response<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        logger.info("用户登录: {}", userLoginDTO.getUsername());

        User user = userService.login(userLoginDTO);

        // 登录成功后分发jwt
        Map<String, Object> claims = new HashMap<>();
        claims.put(JWTClaimsConstant.USER_ID, user.getId());
        String token = JWTUtil.createJWT(
                jwtConfig.getSecretKey(),
                jwtConfig.getTTL(),
                claims
        );

        UserLoginVO userLoginVO = new UserLoginVO.Builder()
                .id(user.getId())
                .userName(user.getUsername())
                .token(token)
                .build();

        return Response.success(userLoginVO);
    }


    /**
     * 注册
     *
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    public Response<String> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        logger.info("新增用户: {}", userRegisterDTO.getUsername());
        userService.register(userRegisterDTO);
        return Response.success(ControllerRespConstant.REGISTER_SUCCESS);
    }

    /**
     * 登出
     *
     * @return
     */
    @PostMapping("/logout")
    public Response<String> loggerout() {
        return Response.success();
    }
}
