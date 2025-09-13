package com.github.zikifaker.osonline.os.interrupt;


import com.github.zikifaker.osonline.os.core.OS;

import java.util.TimerTask;

/**
 * 时钟中断
 * 时钟被暂停, 整个仿真操作系统停止工作
 */

public class ClockInterrupt extends TimerTask {

    private OS resource;

    public ClockInterrupt(OS resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        // 若时钟被暂停, 直接返回
        if (resource.getClock().getIsPaused().get()) {
            return;
        }
        sendClockInterrupt();
    }

    /**
     * 发出时钟中断
     */
    public void sendClockInterrupt() {
        // 增加系统时间
        resource.getClock().addTime();
        // 向CPU发出时钟中断
        resource.getCPU().handleInterrupt(InterruptVector.CLOCK_INTERRUPT);
    }
}
