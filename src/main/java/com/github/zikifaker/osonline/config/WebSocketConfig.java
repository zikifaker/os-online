package com.github.zikifaker.osonline.config;

import com.github.zikifaker.osonline.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.annotation.PostConstruct;


@Configuration
public class WebSocketConfig {

    /**
     * 扫描并注册所有带有 @ServerEndpoint 注解的类作为 WebSocket endpoint
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Autowired
    private JWTConfig jwtConfig;

    @PostConstruct
    public void init() {
        // 将 JWT 配置注入 WebSocketServer 类
        WebSocketServer.setJWTConfig(jwtConfig);
    }
}
