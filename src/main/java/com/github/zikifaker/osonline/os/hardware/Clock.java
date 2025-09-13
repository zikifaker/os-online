package com.github.zikifaker.osonline.os.hardware;


import com.github.zikifaker.osonline.os.interrupt.ClockInterrupt;
import com.github.zikifaker.osonline.os.core.OS;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 时钟
 */
public class Clock extends Timer {

    /**
     * 系统资源
     */
    private OS resource;

    /**
     * 时钟周期
     */
    public static final int INTERVAL = 1000;

    /**
     * 系统时间
     */
    private AtomicInteger currentTime;

    /**
     * 系统暂停标志
     */
    private AtomicBoolean isPaused;

    public Clock(OS resource) {
        super("Clock");
        this.resource = resource;
        this.currentTime = new AtomicInteger(0);
        this.isPaused = new AtomicBoolean(false);
    }

    /**
     * 启动时钟
     */
    public void start() {
        schedule(new ClockInterrupt(resource), 0, INTERVAL);
    }

    /**
     * 增加系统时间
     */
    public void addTime() {
        currentTime.incrementAndGet();
    }

    /**
     * 反转时钟状态
     */
    public synchronized void reverseClockState() {
        isPaused.set(!isPaused.get());
    }

    public AtomicInteger getCurrentTime() {
        return currentTime;
    }

    public AtomicBoolean getIsPaused() {
        return isPaused;
    }
}
