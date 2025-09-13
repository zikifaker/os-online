package com.github.zikifaker.osonline.os.interrupt;

/**
 * 中断向量表
 */
public interface InterruptVector {

    /**
     * 时钟中断向量
     */
    int CLOCK_INTERRUPT = 0;

    /**
     * 作业请求中断向量
     */
    int JOB_REQUEST_INTERRUPT = 1;

    /**
     * 输入操作中断向量
     */
    int INPUT_INTERRUPT = 2;

    /**
     * 输出操作中断向量
     */
    int OUTPUT_INTERRUPT = 3;
}
