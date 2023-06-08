package com.akash.core;

public class Core {
    protected short pc;
    protected short WReg;
    protected int clk;
    protected Memory mem;
    public boolean isRunning;
    public boolean isLoaded;

    Core () {
        mem = new Memory();
        pc = 0;
        clk = 0;
        isRunning = false;
        isLoaded = false;
    }

    public void load(String pathToHex) {
        Parser.parse(pathToHex, mem);
        isLoaded = true;
    }
}
