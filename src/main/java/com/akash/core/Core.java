package com.akash.core;

public class Core {
    public short pc, spc;
    public short WReg;
    public int clk, wdt;

    public int mclrStart = -1;
    public boolean mclre;

    public double freq = 4; // GHz
    public Memory mem;
    public boolean isRunning;

    public boolean isLoaded, isRunnable;

    public Core () {
        mem = new Memory();
        pc = 0x0;
        spc = pc;
        clk = 0;
        isRunning = false;
        isLoaded = false;
    }

    public void reset() {
        mem = new Memory();
        pc = 0x0;
        spc = pc;
        clk = 0;
        isRunning = false;
        isLoaded = false;
    }
    public void load(String pathToHex) {
        pc = Parser.parse(pathToHex, mem);
        spc = pc;
        isLoaded = true;
        isRunnable = true;
    }

    public void step() {
        Instruction instruction = InstructionDecoder.decode(mem.fetchInstruction(pc));
        Executor.execute(instruction, this);
        if(pc > 255) {
            pc = (short) (pc & 0b1111_1111);
        }
        if(mclre && mclrStart != -1 && clk > ((18 * freq * (10^3)) + mclrStart)) {
            isRunnable = false;
            mclrStart = -1;
            reset();
        }
    }
}
