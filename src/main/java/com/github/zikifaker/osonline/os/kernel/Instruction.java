package com.github.zikifaker.osonline.os.kernel;

import java.util.Random;

/**
 * 指令
 */
public class Instruction {

    /**
     * 计算型指令标识
     */
    public static final int CAL_TYPE = 0;

    /**
     * 输入型指令标识
     */
    public static final int INPUT_TYPE = 1;

    /**
     * 输出型指令标识
     */
    public static final int OUTPUT_TYPE = 2;

    /**
     * 指令种类数
     */
    private static final int INSTRUCTION_TYPE_NUM = 3;

    /**
     * 每条计算型指令大小(byte), 规定只为计算型指令分配内存
     */
    public static final int CAL_SIZE = 100;

    /**
     * 指令id
     */
    private int id;

    /**
     * 指令类型
     */
    private int type;

    public Instruction(int id, int type) {
        this.id = id;
        this.type = type;
    }

    public static int generateInstructionType() {
        return new Random().nextInt(INSTRUCTION_TYPE_NUM);
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }
}
