package com.github.zikifaker.osonline.interceptor;

import com.github.zikifaker.osonline.config.JWTConfig;
import com.github.zikifaker.osonline.constant.JWTClaimsConstant;
import com.github.zikifaker.osonline.utils.BaseContextUtil;
import com.github.zikifaker.osonline.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Component
public class JWTInterceptor implements HandlerInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(JWTInterceptor.class);

    @Autowired
    private JWTConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 判断当前请求是否映射到了一个 Controller
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtConfig.getTokenName());
        // 去除 "Bearer " 前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return false;
        }

        // 校验JWT
        try {
            logger.info("token: {}", token);
            Claims claims = JWTUtil.parseJWT(jwtConfig.getSecretKey(), token);
            // 将 user id 加入当前线程上下文
            Integer userId = Integer.valueOf(claims.get(JWTClaimsConstant.USER_ID).toString());
            BaseContextUtil.setCurrentUserId(userId);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            response.setStatus(401);
            return false;
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成时清除 threadLocal
        BaseContextUtil.removeCurrentUserId();
    }
}
