package com.github.zikifaker.osonline.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zikifaker.osonline.constant.OSConstant;
import com.github.zikifaker.osonline.dto.OSCommandDTO;
import com.github.zikifaker.osonline.os.core.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.github.zikifaker.osonline.constant.OSConstant.MESSAGE_QUEUE_CAPACITY;
import static com.github.zikifaker.osonline.os.interrupt.InterruptVector.JOB_REQUEST_INTERRUPT;


@Component
@ServerEndpoint("/ws/os/{session-id}")
public class WebSocketServer {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    /**
     * 维护 websocket 连接
     */
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 维护 os 实例
     */
    private static Map<String, OS> osMap = new ConcurrentHashMap<>();

    /**
     * 序列化/反序列化器
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 阻塞队列
     * 保证推送消息时线程安全
     */
    private static BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>(MESSAGE_QUEUE_CAPACITY);

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

    static {
        pushOSLogThread.setDaemon(true);
        pushOSLogThread.start();
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("session-id") String sessionId) {
        logger.info("建立连接: {}", sessionId);
        OS os = new OS(session);
        osMap.put(sessionId, os);
        sessionMap.put(sessionId, session);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("session-id") String sessionId) {
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
            logger.error("json 解析错误: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(@PathParam("session-id") String sessionId) {
        logger.info("断开连接: {}", sessionId);
        logger.info("{} 的 OS 实例关闭", sessionId);
        // 关闭当前会话启动的 OS 实例
        OS os = osMap.get(sessionId);
        os.shutdown();
        osMap.remove(sessionId);
        sessionMap.remove(sessionId);
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
            logger.error("推送OS实例日志失败: {}", e.getMessage());
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