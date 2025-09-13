package com.github.zikifaker.osonline.os.kernel;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作业
 */
public class Job {

    /**
     * 作业id
     */
    private int id;

    /**
     * 作业到达时间
     */
    private int inTime;

    /**
     * 作业的指令列表
     */
    private List<Instruction> instructionList;

    /**
     * 计算型指令个数
     */
    private int calInstructionNum;

    /**
     * 计算型指令id-索引映射表, 索引为指令列表中去除非计算型指令后, 计算型指令的索引
     */
    private Map<Integer, Integer> calInstructionIdIndexMap;

    /**
     * 已使用的作业id集合
     */
    private static Set<Integer> usedJobIds = new HashSet<>();

    /**
     * 允许维护的最大作业数
     */
    private static final int MAX_JOB_NUM = 100;

    public Job(int id, int inTime, List<Instruction> instructionList) {
        this.id = id;
        this.inTime = inTime;
        this.instructionList = instructionList;
        this.calInstructionNum = countCalInstruction(instructionList);
        this.calInstructionIdIndexMap = createCalInstructionIdIndexMap();
    }

    /**
     * 统计计算型指令个数
     */
    private int countCalInstruction(List<Instruction> instructionList) {
        return (int) instructionList.stream()
                .filter(instruction -> instruction.getType() == Instruction.CAL_TYPE)
                .count();
    }

    /**
     * 创建计算型指令id-索引映射表
     */
    private Map<Integer, Integer> createCalInstructionIdIndexMap() {
        Map<Integer, Integer> map = new HashMap<>();
        AtomicInteger index = new AtomicInteger(0);
        instructionList.stream()
                .filter(instruction -> instruction.getType() == Instruction.CAL_TYPE)
                .forEach(instruction -> {
                    map.put(instruction.getId(), index.getAndIncrement());
                });
        return map;
    }

    /**
     * 随机生成作业id, 若返回-1表示生成失败
     */
    public static int generateJobId() {
        synchronized (usedJobIds) {
            if (usedJobIds.size() >= MAX_JOB_NUM) {
                return -1;
            }
            int id;
            do {
                Random random = new Random();
                id = random.nextInt(MAX_JOB_NUM);
            } while (usedJobIds.contains(id));
            usedJobIds.add(id);
            return id;
        }
    }

    /**
     * 归还分配的作业id
     */
    public void releaseJobId() {
        synchronized (usedJobIds) {
            usedJobIds.remove(id);
        }
    }

    public int getId() {
        return id;
    }

    public int getInTime() {
        return inTime;
    }

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public int getCalInstructionNum() {
        return calInstructionNum;
    }

    public Map<Integer, Integer> getCalInstructionIdIndexMap() {
        return calInstructionIdIndexMap;
    }
}
