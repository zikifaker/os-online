package com.github.zikifaker.osonline.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zikifaker.osonline.config.JWTConfig;
import com.github.zikifaker.osonline.constant.JWTClaimsConstant;
import com.github.zikifaker.osonline.constant.OSConstant;
import com.github.zikifaker.osonline.dto.OSCommandDTO;
import com.github.zikifaker.osonline.os.core.OS;
import com.github.zikifaker.osonline.utils.BaseContextUtil;
import com.github.zikifaker.osonline.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.github.zikifaker.osonline.constant.OSConstant.MESSAGE_QUEUE_CAPACITY;
import static com.github.zikifaker.osonline.os.interrupt.InterruptVector.JOB_REQUEST_INTERRUPT;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;


@Component
@ServerEndpoint("/ws/os/{sessionId}")
public class WebSocketServer {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    /**
     * 维护全局的 websocket 连接
     */
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 维护全局的 os 实例
     */
    private static Map<String, OS> osMap = new ConcurrentHashMap<>();

    /**
     * 序列化/反序列化器
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 阻塞队列
     * 保证推送消息时线程安全
     */
    private static BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(MESSAGE_QUEUE_CAPACITY);

    private static JWTConfig jwtConfig;

    /**
     * 推送 OS 实例日志守护线程
     */
    private static final Thread pushOSLogThread = new Thread(() -> {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message msg = messageQueue.take();
                sendMessage(msg.session, msg.logInfo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    });

    /**
     * WebSocket endpoint不由 Spring 容器管理，需要通过自定义配置来解决依赖注入问题
     * @param jwtConfig
     */
    public static void setJWTConfig(JWTConfig jwtConfig) {
        WebSocketServer.jwtConfig = jwtConfig;
    }

    static {
        pushOSLogThread.setDaemon(true);
        pushOSLogThread.start();
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        // JWT 鉴权
        String token = extractTokenFromSession(session);
        if(!validateToken(token)){
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
                return;
            } catch (IOException e) {
                logger.error("关闭会话失败: ", e);
            }
        }

        logger.info("建立连接: {}", sessionId);
        OS os = new OS(session);
        osMap.put(sessionId, os);
        sessionMap.put(sessionId, session);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sessionId") String sessionId) {
        logger.info("接收来自 {} 消息: {}", sessionId, message);

        try {
            OS os = osMap.get(sessionId);
            OSCommandDTO osCommandDTO = objectMapper.readValue(message, OSCommandDTO.class);
            String command = osCommandDTO.getCommand();
            // 执行用户发出的指令
            switch (command) {
                case OSConstant.POWER_ON:
                    logger.info("{} 的 OS 实例启动", sessionId);
                    os.start();
                    break;
                case OSConstant.REVERSE_CLOCK:
                    os.getClock().reverseClockState();
                    break;
                case OSConstant.REALTIME_JOB:
                    os.getCPU().handleInterrupt(JOB_REQUEST_INTERRUPT);
                    break;
                default:
                    break;
            }

        } catch (JsonProcessingException e) {
            logger.error("json 解析错误: ", e);
        }
    }

    @OnClose
    public void onClose(@PathParam("sessionId") String sessionId) {
        logger.info("断开连接: {}", sessionId);
        logger.info("{} 的 OS 实例关闭", sessionId);

        // 关闭当前会话启动的 OS 实例
        OS os = osMap.get(sessionId);
        if (os != null) {
            logger.info("{} 的 OS 实例关闭", sessionId);
            os.shutdown();
        } else {
            logger.warn("未找到 sessionId {} 对应的 OS 实例", sessionId);
        }

        osMap.remove(sessionId);
        sessionMap.remove(sessionId);
    }

    private String extractTokenFromSession(Session session) {
        String queryString = session.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }

        // 提取 token 参数
        String[] params = queryString.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                String encodedToken = param.substring(6);
                try {
                    // URL 解码 token
                    String decodedToken = java.net.URLDecoder.decode(encodedToken, "UTF-8");
                    return decodedToken;
                } catch (Exception e) {
                    logger.error("URL 解码失败: ", e);
                    return encodedToken;
                }
            }
        }
        return null;
    }

    private boolean validateToken(String token){
        // 去除前缀
        if (token == null) {
            logger.error("非法JWT");
            return false;
        }

        try {
            logger.info("token: {}", token);
            JWTUtil.parseJWT(jwtConfig.getSecretKey(), token);
            return true;
        } catch (Exception e) {
            logger.error("校验JWT失败: ", e);
            return false;
        }
    }

    /**
     * 推送消息
     *
     * @param session websocket 连接
     * @param message 消息
     */
    public static void sendMessage(Session session, String message) {
        if (session == null) {
            return;
        }
        if (!session.isOpen()) {
            logger.info("websocket session {} 已关闭", session.getId());
            return;
        }
        try {
            logger.info("发送消息: {}", message);
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("推送OS实例日志失败: ", e);
        }
    }

    public static void enqueueMessage(Session session, String logInfo) {
        messageQueue.offer(new Message(session, logInfo));
    }

    /**
     * websocket 消息
     */
    static class Message {
        private Session session;

        private String logInfo;

        public Message(Session session, String logInfo) {
            this.session = session;
            this.logInfo = logInfo;
        }
    }
}