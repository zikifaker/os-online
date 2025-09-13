package com.github.zikifaker.osonline.os.hardware;

import com.github.zikifaker.osonline.os.interrupt.IOInterrupt;
import com.github.zikifaker.osonline.os.interrupt.InterruptVector;
import com.github.zikifaker.osonline.os.interrupt.JobRequestInterrupt;
import com.github.zikifaker.osonline.os.kernel.Instruction;
import com.github.zikifaker.osonline.os.kernel.PCB;
import com.github.zikifaker.osonline.os.core.OS;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 中央处理器
 * 负责执行指令、处理中断、调度进程
 */
public class CPU implements InterruptVector {

    /**
     * 系统资源
     */
    private OS resource;

    /**
     * 当前运行的 PCB
     */
    private PCB currentPCB;

    /**
     * 程序计数器, 存放下一条执行指令的索引
     */
    private int pc;

    /**
     * 指令寄存器, 存放当前执行指令的索引
     */
    private int ir;

    /**
     * 剩余的时间片
     */
    private int timeSlice;

    /**
     * 允许调度标志
     */
    private AtomicBoolean canSchedule;

    public CPU(OS resource) {
        this.resource = resource;
        this.canSchedule = new AtomicBoolean(false);
    }

    /**
     * 执行当前进程的一条指令
     * 规定执行一条指令耗时一个时钟周期
     */
    public synchronized void execute() {
        if (currentPCB == null) {
            return;
        }
        ir = pc++;
        Instruction currentInstruction = currentPCB.getJob().getInstructionList().get(ir);
        switch (currentInstruction.getType()) {
            // 处理计算型指令
            case Instruction.CAL_TYPE:
                resource.getDashBoard().consoleLog(String.format("%d:[运行进程 进程ID:%d 指令ID:%d]\n",
                        resource.getClock().getCurrentTime().get(),
                        currentPCB.getId(),
                        currentInstruction.getId()));
                break;
            // 处理输入型指令
            case Instruction.INPUT_TYPE:
                handleInterrupt(INPUT_INTERRUPT);
                break;
            // 处理输出型指令
            case Instruction.OUTPUT_TYPE:
                handleInterrupt(OUTPUT_INTERRUPT);
                break;
            default:
                break;
        }
        timeSlice--;
    }

    /**
     * 恢复指定进程的执行现场
     */
    public synchronized void recoverSpot(PCB pcb) {
        currentPCB = pcb;
        pc = pcb.getPC();
        timeSlice = pcb.getTimeSlice();
    }

    /**
     * 保存当前进程的执行现场
     */
    public synchronized void protectSpot() {
        currentPCB.setPC(pc);
        currentPCB.setTimeSlice(timeSlice);
        currentPCB = null;
    }

    /**
     * 根据中断向量表执行相应的中断服务例程
     */
    public synchronized void handleInterrupt(int interruptVector) {
        switch (interruptVector) {
            // 收到时钟中断后, 打开调度模块
            case CLOCK_INTERRUPT: {
                openSchedule();
                break;
            }
            // 收到作业请求中断后, 随机创建一个作业
            case JOB_REQUEST_INTERRUPT: {
                new JobRequestInterrupt(resource).start();
                break;
            }
            // 执行到输入指令, 通知IO中断处理器
            case INPUT_INTERRUPT: {
                PCB tempPCB = currentPCB;
                currentPCB.block(INPUT_INTERRUPT);
                new IOInterrupt(resource, tempPCB, INPUT_INTERRUPT).start();
                break;
            }
            // 执行到输出指令, 通知IO中断处理器
            case OUTPUT_INTERRUPT: {
                PCB tempPCB = currentPCB;
                currentPCB.block(OUTPUT_INTERRUPT);
                new IOInterrupt(resource, tempPCB, OUTPUT_INTERRUPT).start();
                break;
            }
            default:
                break;
        }
    }

    /**
     * 启用调度程序
     */
    private void openSchedule() {
        canSchedule.set(true);
    }

    /**
     * 禁用调度程序
     */
    public void closeSchedule() {
        canSchedule.set(false);
    }

    public int getPC() {
        return pc;
    }

    public PCB getCurrentPCB() {
        return currentPCB;
    }

    public void setCurrentPCB(PCB currentPCB) {
        this.currentPCB = currentPCB;
    }

    public AtomicBoolean getCanSchedule() {
        return canSchedule;
    }

    public int getTimeSlice() {
        return timeSlice;
    }
}
