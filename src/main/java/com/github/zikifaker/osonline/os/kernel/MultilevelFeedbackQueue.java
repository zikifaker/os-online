package com.github.zikifaker.osonline.os.kernel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 多级反馈队列
 */
public class MultilevelFeedbackQueue {

    /**
     * 多级反馈队列级数
     */
    public static final int LEVEL = 3;

    /**
     * 各级队列的时间片, 单位为时钟周期
     */
    public static final int[] LEVEL_TIME_SLICE = {1, 2, 4};

    /**
     * 多级反馈队列
     */
    private List<Queue<PCB>> queues;

    public MultilevelFeedbackQueue() {
        this.queues = new ArrayList<>(LEVEL);
        for (int i = 0; i < LEVEL; i++) {
            this.queues.add(new LinkedList<>());
        }
    }

    public Queue<PCB> getQueueByIndex(int index) {
        return queues.get(index);
    }
}
