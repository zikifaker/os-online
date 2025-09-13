package com.github.zikifaker.osonline.os.core;

import com.github.zikifaker.osonline.websocket.WebSocketServer;
import javax.websocket.Session;


/**
 * 控制台
 */
public class DashBoard {

    private OS resource;

    public DashBoard(OS resource) {
        this.resource = resource;
    }

    // 将日志信息加入阻塞队列, 等待推送线程发送
    public void consoleLog(String logInfo) {
        Session session = resource.getSession();
        WebSocketServer.enqueueMessage(session, logInfo);
    }
}

