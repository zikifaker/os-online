package com.github.zikifaker.osonline.os.hardware;

import com.github.zikifaker.osonline.os.kernel.Instruction;
import com.github.zikifaker.osonline.os.kernel.Job;
import com.github.zikifaker.osonline.os.kernel.PCB;

/**
 * 内存
 */
public class Memory {

    /**
     * 用户区，每个bit表示一个存储单元，使用固定分区管理内存
     */
    private byte[] userSpace;

    /**
     * 用户区的内存块数
     */
    public static final int BLOCK_NUM = 16;

    /**
     * 每个内存块的存储单元数
     */
    public static final int UNIT_NUM_PER_BLOCK = 10;

    /**
     * 每个存储单元的大小(byte)
     */
    public static final int UNIT_SIZE = 100;

    public Memory() {
        this.userSpace = new byte[BLOCK_NUM * UNIT_NUM_PER_BLOCK / 8];
    }

    /**
     * 使用最佳适应算法为作业分配内存，返回分配的存储单元起始索引。
     *
     * @param job 需要分配内存的作业
     * @return 分配的存储单元起始索引，若无法分配则返回 -1
     */
    public int allocateUserSpace(Job job) {
        // 计算作业需要的存储单元数，向上取整
        int jobSize = job.getCalInstructionNum() * Instruction.CAL_SIZE;
        int unitNum = (jobSize + UNIT_SIZE - 1) / UNIT_SIZE;

        int bestFitIndex = getBestFitIndex(unitNum);
        if (bestFitIndex == -1) {
            return -1;
        }

        for (int i = 0; i < unitNum; i++) {
            int byteIndex = (bestFitIndex + i) / 8;
            int bitIndex = (bestFitIndex + i) % 8;
            userSpace[byteIndex] |= (byte) (1 << bitIndex);
        }

        return bestFitIndex;
    }

    /**
     * 获取最佳适应位置的起始索引。
     *
     * @param unitNum 所需连续存储单元的数量
     * @return 最佳适应位置的起始索引，若无法找到合适区域则返回 -1
     */
    private int getBestFitIndex(int unitNum) {
        int bestFitIndex = -1;
        int bestFitSize = Integer.MAX_VALUE;

        // 遍历每个存储单元
        for (int i = 0; i < BLOCK_NUM * UNIT_NUM_PER_BLOCK; i++) {
            int byteIndex = i / 8, bitIndex = i % 8;

            // 若当前位空闲，统计连续的空闲位
            if ((userSpace[byteIndex] & (1 << bitIndex)) == 0) {
                int count = 0;
                for (int j = i; j < BLOCK_NUM * UNIT_NUM_PER_BLOCK && count < unitNum; j++) {
                    int nextByteIndex = j / 8, nextBitIndex = j % 8;
                    if ((userSpace[nextByteIndex] & (1 << nextBitIndex)) == 0) {
                        count++;
                    } else {
                        break;
                    }
                }

                // 若找到足够大的空闲区域
                if (count >= unitNum && count < bestFitSize) {
                    bestFitIndex = i;
                    bestFitSize = count;
                }

                // 跳过已统计的空闲区域
                i += count - 1;
            }
        }

        return bestFitIndex;
    }

    /**
     * 释放指定进程占用的用户区内存
     *
     * @param pcb 需要释放内存的进程
     */
    public void freeUserSpace(PCB pcb) {
        int jobSize = pcb.getJob().getCalInstructionNum() * Instruction.CAL_SIZE;
        int unitNum = (jobSize + UNIT_SIZE - 1) / UNIT_SIZE;
        int startIndex = pcb.getBaseAddr();

        for (int i = startIndex; i <= startIndex + unitNum - 1; i++) {
            int byteIndex = i / 8, bitIndex = i % 8;
            userSpace[byteIndex] &= (byte) ~(1 << bitIndex);
        }
    }

    public byte[] getUserSpace() {
        return userSpace;
    }
}
