package com.github.zikifaker.osonline.os.interrupt;


import com.github.zikifaker.osonline.os.kernel.PCB;
import com.github.zikifaker.osonline.os.core.OS;

/**
 * 输入输出中断
 */
public class IOInterrupt extends Thread implements InterruptVector {

    /**
     * 系统资源
     */
    private OS resource;

    /**
     * 等待输入输出的进程
     */
    private PCB pcb;

    /**
     * 中断类型
     */
    private int interruptType;

    /**
     * 输入输出用时(2个时间周期)
     */
    private static final int IO_TIME = 2;

    public IOInterrupt(OS resource, PCB pcb, int interruptType) {
        this.resource = resource;
        this.pcb = pcb;
        this.interruptType = interruptType;
    }

    @Override
    public void run() {
        int startTime = resource.getClock().getCurrentTime().get();

        // 若IO操作未完成, 忙等待
        while (resource.getClock().getCurrentTime().get() - startTime < IO_TIME) {
        }
        switch (interruptType) {
            case INPUT_INTERRUPT:
                resource.getDashBoard().consoleLog(String.format("%d:[输入操作完成 进程ID:%d]\n",
                        resource.getClock().getCurrentTime().get(),
                        pcb.getId()));
                break;
            case OUTPUT_INTERRUPT:
                resource.getDashBoard().consoleLog(String.format("%d:[输出操作完成 进程ID:%d]\n",
                        resource.getClock().getCurrentTime().get(),
                        pcb.getId()));
                break;
            default:
                break;
        }

        // 若进程结束, 撤销进程
        if (pcb.getPC() >= pcb.getJob().getInstructionList().size()) {
            pcb.exit();
            return;
        }

        // 唤醒进程
        switch (interruptType) {
            case INPUT_INTERRUPT:
                pcb.wake(INPUT_INTERRUPT);
                break;
            case OUTPUT_INTERRUPT:
                pcb.wake(OUTPUT_INTERRUPT);
                break;
            default:
                break;
        }
    }
}
