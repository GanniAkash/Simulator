package com.akash.core;

public class Memory {
    private final int[] program_mem;
    private final short[] data_mem;
    private final short[] stack;

    private final int bit12_mask = 0b1111_1111_1111;

    private final int bit8_mask = 0b1111_1111;

    public enum SFR {
        INDF(0), TMR0(1), PCL(2), STATUS(3), FSR(4),
        OSCCAL(5), GPIO(6), TRISGPIO(32), OPTION(33);

        public final int val;

        SFR(int val) {
            this.val = val;
        }
    }

    Memory () {
        program_mem = new int[256];
        data_mem = new short[34];
        stack = new short[2];

        data_mem[SFR.INDF.val] = 0x00;
        data_mem[SFR.TMR0.val] = 0;
        data_mem[SFR.PCL.val] = 0b1111_1111;
        data_mem[SFR.STATUS.val] = 0b001_1000;
        data_mem[SFR.FSR.val] = 0b1110_0000;
        data_mem[SFR.OSCCAL.val] = 0b1111_1110;
        data_mem[SFR.GPIO.val] = 0;
        data_mem[SFR.TRISGPIO.val] = 0b0000_1111;
        data_mem[SFR.OPTION.val] = 0b0000_1111;
    }

    public int fetchInstruction(short addr) {

        return (program_mem[addr] & bit12_mask);
    }

    public void setInstruction(short addr, int inst) {

        program_mem[(addr & bit8_mask)] = (inst & bit12_mask);
    }

    public short fetchData(short addr) throws IndexOutOfBoundsException {
        if((addr <= 0x0f && addr >= 0x07) || addr > 0x1f) throw new IndexOutOfBoundsException();
        else {
            return (short) (data_mem[(addr & bit8_mask)] & bit8_mask);
        }
    }

    public void setData(short addr, short data) throws IndexOutOfBoundsException {
        if((addr <= 0x0f && addr >= 0x07) || addr > 0x1f) throw new IndexOutOfBoundsException();
        else {
            data_mem[(addr & bit8_mask)] = (short) (data & bit8_mask);
        }
    }

    public void setStatusBit(int bit) {
        int bitMask = 0b01 << bit;
        data_mem[SFR.STATUS.val] = (short) (data_mem[SFR.STATUS.val] | bitMask);
    }

    public void clearStatusBit(int bit) {
        int bitMask = ~(0b01 << bit);
        data_mem[SFR.STATUS.val] = (short) (data_mem[SFR.STATUS.val] & bitMask);
    }

    public void push(short pc) {
        pc = (short) (pc & bit8_mask);
        short temp = stack[0];
        stack[1] = temp;
        stack[0] = pc;
    }

    public short pop() {
        short temp = stack[0];
        stack[0] = stack[1];
        stack[1] = 0;
        return (short) (temp & bit8_mask);
    }
}
