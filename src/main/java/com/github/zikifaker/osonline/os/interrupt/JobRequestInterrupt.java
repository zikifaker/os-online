package com.github.zikifaker.osonline.os.interrupt;

import com.github.zikifaker.osonline.os.kernel.Instruction;
import com.github.zikifaker.osonline.os.kernel.Job;
import com.github.zikifaker.osonline.os.core.OS;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业请求中断
 */
public class JobRequestInterrupt extends Thread {

    /**
     * 系统资源
     */
    private OS resource;

    /**
     * 一个作业的指令数
     */
    private static final int INSTRUCTION_NUM = 20;

    public JobRequestInterrupt(OS resource) {
        super("JobRequestInterrupt");
        this.resource = resource;
    }

    @Override
    public void run() {
        Job randomJob = createRandomJob();
        if (randomJob == null) {
            resource.getDashBoard().consoleLog(String.format("%d:[创建作业失败]", resource.getClock().getCurrentTime().get()));
            return;
        }
        synchronized (resource.getExternalMemory().getReserveQueue()) {
            resource.getExternalMemory().getReserveQueue().offer(randomJob);
        }
        resource.getDashBoard().consoleLog(String.format("%d:[创建作业 作业ID:%d]\n",
                resource.getClock().getCurrentTime().get(),
                randomJob.getId()));
    }

    /**
     * 随机创建作业
     */
    private Job createRandomJob() {
        int jobId = Job.generateJobId();
        if (jobId == -1) {
            return null;
        }

        List<Instruction> instructionList = new ArrayList<>();
        for (int i = 0; i < INSTRUCTION_NUM; i++) {
            instructionList.add(createRandomInstruction(i));
        }
        return new Job(jobId, resource.getClock().getCurrentTime().get(), instructionList);
    }

    /**
     * 随机创建指令
     */
    private Instruction createRandomInstruction(int instructionId) {
        return new Instruction(instructionId, Instruction.generateInstructionType());
    }
}
