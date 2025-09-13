package com.github.zikifaker.osonline.os.core;

import com.github.zikifaker.osonline.os.hardware.CPU;
import com.github.zikifaker.osonline.os.hardware.Clock;
import com.github.zikifaker.osonline.os.hardware.ExternalMemory;
import com.github.zikifaker.osonline.os.hardware.Memory;
import com.github.zikifaker.osonline.os.kernel.Scheduler;

import javax.websocket.Session;

public class OS {

    /**
     * 时钟
     */
    private Clock clock;

    /**
     * CPU
     */
    private CPU cpu;

    /**
     * 调度器
     */
    private Scheduler scheduler;

    /**
     * 内存
     */
    private Memory memory;

    /**
     * 外存
     */
    private ExternalMemory externalMemory;

    /**
     * 控制台
     */
    private DashBoard dashBoard;

    /**
     * websocket连接
     * 用于 OS 实例向用户推送日志
     */
    private Session session;

    public OS(Session session) {
        this.clock = new Clock(this);
        this.cpu = new CPU(this);
        this.scheduler = new Scheduler(this);
        this.memory = new Memory();
        this.externalMemory = new ExternalMemory();
        this.dashBoard = new DashBoard(this);
        this.session = session;
    }

    /**
     * 启动 OS 实例
     * 启动时钟线程和调度线程
     */
    public void start() {
        clock.start();
        scheduler.start();
    }

    /**
     * 关闭 OS 实例
     */
    public void shutdown() {
        // 关闭时钟并阻止新任务
        clock.cancel();
        // 清除已经取消的任务
        clock.purge();
        // 关闭调度线程
        scheduler.shutdown();
    }

    public Clock getClock() {
        return clock;
    }

    public CPU getCPU() {
        return cpu;
    }

    public ExternalMemory getExternalMemory() {
        return externalMemory;
    }

    public Memory getMemory() {
        return memory;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public DashBoard getDashBoard() {
        return dashBoard;
    }

    public Session getSession() {
        return session;
    }
}
