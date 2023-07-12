package com.akash.core;
import com.akash.App;

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

        public static SFR getSFR(int val) {
            switch (val) {
                case 0: {
                    return INDF;
                }
                case 1: {
                    return TMR0;
                }
                case 2: {
                    return PCL;
                }
                case 3: {
                    return STATUS;
                }
                case 4: {
                    return FSR;
                }
                case 5: {
                    return OSCCAL;
                }
                case 6: {
                    return GPIO;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    Memory () {
        program_mem = new int[256];
        data_mem = new short[34];
        stack = new short[2];

        data_mem[SFR.INDF.val] = 0x00;
        data_mem[SFR.TMR0.val] = 0;
        data_mem[SFR.PCL.val] = 0b1111_1111;
        data_mem[SFR.STATUS.val] = 0b0001_1000;
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
        if(((addr <= 0x0f && addr >= 0x07) || addr > 0x1f) && (addr != 32) && (addr != 33)) throw new IndexOutOfBoundsException();
        else if(addr == 0) {
            if(data_mem[SFR.FSR.val] == 0) return 0;
            return data_mem[data_mem[SFR.FSR.val] & 0b11111];
        }
        else if(addr == 4) return (short) (data_mem[4] & 0b11111);
        else if(addr == 6) {
            if(App.controller == null) return 0;
            return App.controller.readPort();
        }
        else {
            return (short) (data_mem[(addr & bit8_mask)] & bit8_mask);
        }
    }

    public short fetchGpio() {
        return (short) (data_mem[6] & bit8_mask);
    }

    public void setData(short addr, short data) throws IndexOutOfBoundsException {
        if(((addr <= 0x0f && addr >= 0x07) || addr > 0x1f) && (addr != 32) && (addr != 33)) throw new IndexOutOfBoundsException();
        else if(addr == 3) {
            data_mem[addr] = (short) (data | 0b0001_1111);
        }
        else if(addr == 0) {
            if(data_mem[SFR.FSR.val] == 0) return;
            data_mem[data_mem[SFR.FSR.val]] = (short) (data & bit8_mask);
        }
        else if(addr == 4) {
            data_mem[addr] = (short) (data | 0b1110_0000);
        }
        else if(addr == 6 || addr == 32) {
            data_mem[addr] = (short) (data | 0b1111_0000);
            App.controller.writePorts();
        }
        else {
            data_mem[(addr & bit8_mask)] = (short) (data & bit8_mask);
        }
    }

    public void setStatus(short data) {
        data_mem[3] = (short) (data & bit8_mask);
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
        short temp = stack[0];
        stack[1] = temp;
        stack[0] = pc;
    }

    public short pop() {
        short temp = stack[0];
        stack[0] = stack[1];
        stack[1] = 0;
        return temp;
    }

}
