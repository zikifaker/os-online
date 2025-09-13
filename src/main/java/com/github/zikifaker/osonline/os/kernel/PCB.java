package com.github.zikifaker.osonline.os.kernel;


import com.github.zikifaker.osonline.os.interrupt.InterruptVector;
import com.github.zikifaker.osonline.os.core.OS;

/**
 * 进程控制块
 */
public class PCB implements InterruptVector {

    /**
     * 进程就绪标志
     */
    public static final int READY_STATE = 0;

    /**
     * 进程运行标志
     */
    public static final int RUNNING_STATE = 1;

    /**
     * 进程阻塞标志
     */
    public static final int BLOCK_STATE = 2;

    /**
     * 进程结束标志
     */
    public static final int FINISH_STATE = 3;

    /**
     * 系统资源
     */
    private OS resource;

    /**
     * 关联作业的指针
     */
    private Job job;

    /**
     * 进程id
     */
    private int id;

    /**
     * 程序计数器, 存放进程的下一条执行指令的索引
     */
    private int pc;

    /**
     * 进程的物理基址
     */
    private int baseAddr;

    /**
     * 进程所在的多级反馈队列级数的索引
     */
    private int queueIndex;

    /**
     * 进程状态
     */
    private int state;

    /**
     * 进程剩余时间片
     */
    private int timeSlice;

    public PCB(OS resource, Job job) {
        this.resource = resource;
        this.job = job;
        this.id = job.getId();
        this.queueIndex = 0;
        this.state = READY_STATE;
        this.timeSlice = MultilevelFeedbackQueue.LEVEL_TIME_SLICE[0];
    }

    /**
     * 销毁进程
     */
    public synchronized void exit() {
        state = FINISH_STATE;
        job.releaseJobId();
        resource.getMemory().freeUserSpace(this);
        resource.getDashBoard().consoleLog(String.format("%d:[进程结束 进程ID:%d]\n",
                resource.getClock().getCurrentTime().get(),
                getId()));
    }

    /**
     * 阻塞进程
     */
    public void block(int interruptType) {
        synchronized (resource.getScheduler()) {
            // 保护CPU现场
            resource.getCPU().protectSpot();
            state = BLOCK_STATE;
            // 放入阻塞队列
            switch (interruptType) {
                case INPUT_INTERRUPT:
                    resource.getScheduler().getInputBlockQueue().offer(this);
                    break;
                case OUTPUT_INTERRUPT:
                    resource.getScheduler().getOutputBlockQueue().offer(this);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 唤醒进程
     */
    public void wake(int interruptType) {
        synchronized (resource.getScheduler()) {
            // 从阻塞队列中移除
            switch (interruptType) {
                case INPUT_INTERRUPT:
                    resource.getScheduler().getInputBlockQueue().remove(this);
                    break;
                case OUTPUT_INTERRUPT:
                    resource.getScheduler().getOutputBlockQueue().remove(this);
                    break;
                default:
                    break;
            }
            // 加入原先所在的反馈队列末尾
            state = READY_STATE;
            resource.getScheduler().getReadyQueue().getQueueByIndex(queueIndex).offer(this);
        }
    }

    public int getId() {
        return id;
    }

    public Job getJob() {
        return job;
    }

    public int getPC() {
        return pc;
    }

    public void setPC(int pc) {
        this.pc = pc;
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public int getBaseAddr() {
        return baseAddr;
    }

    public void setBaseAddr(int baseAddr) {
        this.baseAddr = baseAddr;
    }

    public void setQueueIndex(int queueIndex) {
        this.queueIndex = queueIndex;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getTimeSlice() {
        return timeSlice;
    }

    public void setTimeSlice(int timeSlice) {
        this.timeSlice = timeSlice;
    }
}
