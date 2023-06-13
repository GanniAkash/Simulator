package com.akash.core;

public class Core {
    public short pc;
    public short WReg;
    protected int clk;
    public Memory mem;
    public boolean isRunning;
    public boolean isLoaded;

    public Core () {
        mem = new Memory();
        pc = 0x0;
        clk = 0;
        isRunning = false;
        isLoaded = false;
    }

    public void load(String pathToHex) {
        pc = Parser.parse(pathToHex, mem);
        isLoaded = true;
    }

    public void step() {
        Instruction instruction = InstructionDecoder.decode(mem.fetchInstruction(pc));
        Executor.execute(instruction, this);
        if(pc > 255) {
            pc = (short) (pc & 0b1111_1111);
        }
    }
}
