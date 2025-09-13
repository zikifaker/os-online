package com.github.zikifaker.osonline.handler;

import com.github.zikifaker.osonline.vo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    public Response<String> handlerException(Exception e) {
        logger.error("controller error: ", e);
        return Response.error(e.getMessage());
    }
}
