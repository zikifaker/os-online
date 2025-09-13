package com.github.zikifaker.osonline.os.hardware;

import com.github.zikifaker.osonline.os.kernel.Job;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 外存
 */
public class ExternalMemory {

    /**
     * 后备队列
     */
    private Queue<Job> reserveQueue;

    public ExternalMemory() {
        reserveQueue = new LinkedList<>();
    }

    public Queue<Job> getReserveQueue() {
        return reserveQueue;
    }
}
