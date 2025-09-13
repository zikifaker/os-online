package com.github.zikifaker.osonline.os.kernel;


import com.github.zikifaker.osonline.os.core.OS;
import com.github.zikifaker.osonline.os.hardware.Memory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 调度器
 */
public class Scheduler extends Thread {

    /**
     * 系统资源
     */
    private OS resource;

    /**
     * 就绪队列
     */
    private MultilevelFeedbackQueue readyQueue;

    /**
     * 输入阻塞队列
     */
    private Queue<PCB> inputBlockQueue;

    /**
     * 输出阻塞队列
     */
    private Queue<PCB> outputBlockQueue;

    /**
     * 线程运行标志
     */
    private volatile boolean running = true;

    public Scheduler(OS resource) {
        this.resource = resource;
        this.readyQueue = new MultilevelFeedbackQueue();
        this.inputBlockQueue = new LinkedList<>();
        this.outputBlockQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        while (running) {
            // 若调度线程未被停止且不允许调度, 忙等待
            while (running && !resource.getCPU().getCanSchedule().get()) {
            }

            // 每2个时钟周期进行高级调度
            if (resource.getClock().getCurrentTime().get() % 2 == 0) {
                highLevelSchedule();
            }

            // 低级调度
            lowLevelSchedule();

            // 关闭调度
            resource.getCPU().closeSchedule();

            if (resource.getCPU().getCurrentPCB() == null) {
                resource.getDashBoard().consoleLog(String.format("%d:[CPU空闲]\n", resource.getClock().getCurrentTime().get()));
                continue;
            }

            // CPU执行一条指令
            resource.getCPU().execute();

            // 若进程处于阻塞态, 直接continue
            if (resource.getCPU().getCurrentPCB() == null) {
                continue;
            }
            checkCurrentPCBIsOver();
        }
    }

    /**
     * 高级调度
     */
    private synchronized void highLevelSchedule() {
        // 从后备队列中取出一个作业
        Job job = resource.getExternalMemory().getReserveQueue().poll();
        if (job == null) {
            return;
        }

        // 尝试分配用户区内存, 若失败放回后备队列末尾
        // baseAddress为分配的用户区内存块的起始索引
        int baseAddress = resource.getMemory().allocateUserSpace(job);
        if (baseAddress == -1) {
            resource.getExternalMemory().getReserveQueue().offer(job);
            return;
        }

        // 将创建的PCB加入就绪队列
        PCB pcb = new PCB(resource, job);
        pcb.setBaseAddr(baseAddress);
        Queue<PCB> queue = readyQueue.getQueueByIndex(pcb.getQueueIndex());
        queue.offer(pcb);
        resource.getDashBoard().consoleLog(String.format("%d:[创建进程: 进程ID:%d 用户区首地址:%d]\n",
                resource.getClock().getCurrentTime().get(),
                pcb.getId(),
                pcb.getBaseAddr() * Memory.UNIT_SIZE));
    }

    /**
     * 低级调度
     */
    private synchronized void lowLevelSchedule() {
        // 若CPU繁忙, 直接返回
        if (resource.getCPU().getCurrentPCB() != null) {
            return;
        }

        // 每5个时钟周期检查最后一级队列
        if (resource.getClock().getCurrentTime().get() % 5 == 0) {
            checkLastLevelQueue();
        }

        // 选出一个待执行的PCB
        PCB pcb = scheduleNextPCB();
        if (pcb == null) {
            return;
        }

        // 恢复CPU现场
        resource.getCPU().recoverSpot(pcb);
    }

    /**
     * 检查最后一级队列
     */
    private synchronized void checkLastLevelQueue() {
        Queue<PCB> firstLevelQueue = readyQueue.getQueueByIndex(0);
        Queue<PCB> lastLevelQueue = readyQueue.getQueueByIndex(MultilevelFeedbackQueue.LEVEL - 1);
        Iterator<PCB> iterator = lastLevelQueue.iterator();
        while (iterator.hasNext()) {
            // 从最后一级反馈队列中移除
            PCB pcb = iterator.next();
            iterator.remove();
            // 加入第一级反馈队列
            firstLevelQueue.offer(pcb);
            pcb.setQueueIndex(0);
            pcb.setTimeSlice(MultilevelFeedbackQueue.LEVEL_TIME_SLICE[0]);
        }
    }

    /**
     * 调度下一个PCB
     */
    private synchronized PCB scheduleNextPCB() {
        for (int i = 0; i < MultilevelFeedbackQueue.LEVEL; i++) {
            Queue<PCB> queue = readyQueue.getQueueByIndex(i);
            if (queue.isEmpty()) {
                continue;
            }
            return queue.poll();
        }
        return null;
    }

    /**
     * 检查进程是否执行完毕
     */
    private synchronized void checkCurrentPCBIsOver() {
        PCB currentPCB = resource.getCPU().getCurrentPCB();
        int queueLevel = currentPCB.getQueueIndex();
        // 若进程结束, 销毁进程
        if (resource.getCPU().getPC() >= currentPCB.getJob().getInstructionList().size()) {
            currentPCB.exit();
            resource.getCPU().setCurrentPCB(null);
            return;
        }

        // 若时间片不为0, 不操作
        if (resource.getCPU().getTimeSlice() != 0) {
            return;
        }

        // 若时间片为0, 保护CPU现场
        resource.getCPU().protectSpot();

        // 出队并且移入下一级反馈队列
        if (queueLevel < MultilevelFeedbackQueue.LEVEL - 1) {
            readyQueue.getQueueByIndex(queueLevel + 1).offer(currentPCB);
            currentPCB.setQueueIndex(queueLevel + 1);
            currentPCB.setTimeSlice(MultilevelFeedbackQueue.LEVEL_TIME_SLICE[queueLevel + 1]);
        } else {
            readyQueue.getQueueByIndex(queueLevel).offer(currentPCB);
        }
    }

    public void shutdown() {
        running = false;
    }

    public MultilevelFeedbackQueue getReadyQueue() {
        return readyQueue;
    }

    public Queue<PCB> getInputBlockQueue() {
        return inputBlockQueue;
    }

    public Queue<PCB> getOutputBlockQueue() {
        return outputBlockQueue;
    }
}
