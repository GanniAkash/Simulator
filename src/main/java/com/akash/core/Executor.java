package com.akash.core;

public class Executor {
    private static final int bit8_mask = 0b1111_1111;
    public static void execute(Instruction ins, Core core) {
        Instruction.OpCode op = ins.getOpCode();

        switch (op) {
            case ADDWF: {
                short f = (short) (ins.getArgs()[0] & 255);
                int d = ins.getArgs()[1];
                short res = (short) (core.WReg + core.mem.fetchData(f));
                checkC(res, core);
                checkDC(res, core);
                checkZ(res, core);
                if(d == 0){
                    core.WReg = (short) (res & bit8_mask);
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case BCF: {
                short f = ins.getArgs()[0];
                int b = ins.getArgs()[1];
                short temp = core.mem.fetchData(f);
                int bit_mask = ~(0b01 << b);
                core.mem.setData(f,(short) (temp & bit_mask));
                break;
            }
            case BSF: {
                short f = ins.getArgs()[0];
                int b = ins.getArgs()[1];
                short temp = core.mem.fetchData(f);
                int bit_mask = (0b01 << b);
                core.mem.setInstruction(f, (short) (temp | bit_mask));
                break;
            }
            case BTFSC: {
                short f = ins.getArgs()[0];
                int b = ins.getArgs()[1];
                short temp = core.mem.fetchData(f);
                int bit_mask = (0b01 << b);
                int bit = (temp & bit_mask) >> b;
                if(bit == 0) {
                    Executor.execute(new Instruction(Instruction.OpCode.NOP, (short) 0), core);
                }
                break;
            }
            case BTFSS: {
                short f = ins.getArgs()[0];
                int b = ins.getArgs()[1];
                short temp = core.mem.fetchData(f);
                int bit_mask = (0b01 << b);
                int bit = (temp & bit_mask) >> b;
                if(bit == 1) {
                    Executor.execute(new Instruction(Instruction.OpCode.NOP, (short) 0), core);
                }
                break;
            }
            case ANDLW: {
                short k = ins.getArgs()[0];
                short res = (short) ((core.WReg & k) & bit8_mask);
                core.WReg = res;
                checkZ(res, core);
            }
            break;
            case ANDWF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) (core.WReg & core.mem.fetchData(f));
                checkZ(res, core);
                if(d == 0) {
                    core.WReg = res;
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case CLRW: {
                core.WReg = 0;
                checkZ(0, core);
                break;
            }
            case CALL: {
                short k = ins.getArgs()[0];
                short tempAddr = (short) (core.pc + 1);
                core.mem.push(tempAddr);
                core.pc = (short) ((k-1) & bit8_mask);
            }
            case CLRF: {
                short f = ins.getArgs()[0];
                core.mem.setData(f, (short) 0);
                checkZ(0, core);
                break;
            }
            case COMF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) ~(core.mem.fetchData(f));
                checkZ(res, core);
                if(d == 0) {
                    core.WReg = (short) (res & bit8_mask);
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case DECF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) (core.mem.fetchData(f) - 1);
                checkZ(res, core);
                if(d == 0) {
                    core.WReg = (short) (res & bit8_mask);
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case DECFSZ: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) ((core.mem.fetchData(f) - 1) & bit8_mask);
                if(d == 0) {
                    core.WReg = res;
                }
                else {
                    core.mem.setData(f, res);
                }
                if (res == 0) {
                    Executor.execute(new Instruction(Instruction.OpCode.NOP), core);
                }
                break;
            }
            case INCF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) (core.mem.fetchData(f) + 1);
                checkZ(res, core);
                if(d == 0) {
                    core.WReg = (short) (res & bit8_mask);
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case INCFSZ: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) ((core.mem.fetchData(f) + 1) & bit8_mask);
                if(d == 0) {
                    core.WReg = res;
                }
                else {
                    core.mem.setData(f, res);
                }
                if (res == 0) {
                    Executor.execute(new Instruction(Instruction.OpCode.NOP), core);
                }
                break;
            }
            case GOTO: {
                core.pc = ins.getArgs()[0];
                break;
            }
            case IORLW: {
                short k = ins.getArgs()[0];
                short res = (short) (core.WReg | k);
                checkZ(res, core);
                core.WReg = res;
                break;
            }
            case IORWF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) (core.WReg | core.mem.fetchData(f));
                checkZ(res, core);
                if(d == 0) {
                    core.WReg = res;
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case MOVWF: {
                short f = ins.getArgs()[0];
                core.mem.setData(f, core.WReg);
                break;
            }
            case MOVF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = core.mem.fetchData(f);
                checkZ(res, core);
                if (d == 0) {
                    core.WReg = res;
                }
                break;
            }
            case NOP:{
                break;
            }
            case MOVLW: {
                core.WReg = ins.getArgs()[0];
                break;
            }
            case OPTION: {
                core.mem.setData((short) Memory.SFR.OPTION.val, core.WReg);
                break;
            }
            case RETLW: {
                core.WReg = ins.getArgs()[0];
                core.pc = core.mem.pop();
                break;
            }
            case RLF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short temp = core.mem.fetchData(f);
                short prev = (short) ((core.mem.fetchData((short) Memory.SFR.STATUS.val) & 0b100) >> 2);
                if(((temp & 0b1000_0000) >> 7) == 0) {
                    core.mem.clearStatusBit(2);
                }
                else {
                    core.mem.setStatusBit(2);
                }
                temp = (short) (((temp << 1) | (prev)) & bit8_mask);
                if (d == 0) {
                    core.WReg = temp;
                }
                else {
                    core.mem.setData(f, temp);
                }
                break;
            }
            case RRF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short temp = core.mem.fetchData(f);
                short prev = (short) ((core.mem.fetchData((short) Memory.SFR.STATUS.val) & 0b100) >> 2);
                if(((temp & 0b1)) == 0) {
                    core.mem.clearStatusBit(2);
                }
                else {
                    core.mem.setStatusBit(2);
                }
                temp = (short) (((temp >> 1) | (prev << 7)) & bit8_mask);
                if (d == 0) {
                    core.WReg = temp;
                }
                else {
                    core.mem.setData(f, temp);
                }
                break;
            }
            case SUBWF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) ((core.mem.fetchData(f) + (((~core.WReg) + 1) & bit8_mask)));
                checkZ(res, core);
                checkDC(res, core);
                checkC(res,core);
                if(d == 0) {
                    core.WReg = (short) (res & bit8_mask);
                }
                else {
                    core.mem.setData(f, (short) (res&bit8_mask));
                }
                break;
            }
            case SWAPF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = core.mem.fetchData(f);
                res = (short) (((res & 0b1111) << 4) | ((res & 0b1111_0000) >> 4));
                if(d == 0) {
                    core.WReg = res;
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case TRIS: {
                short f = ins.getArgs()[0];
                if (f != 6) break;
                core.mem.setData(f, core.WReg);
                break;
            }
            case XORWF: {
                short f = ins.getArgs()[0];
                int d = ins.getArgs()[1];
                short res = (short) (core.WReg ^ core.mem.fetchData(f));
                checkZ(res, core);
                if(d == 0) {
                    core.WReg = res;
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
            case XORLW: {
                short k = ins.getArgs()[0];
                short res = (short) (core.WReg ^ k);
                checkZ(res, core);
                core.WReg = res;
                break;
            }
            default: break;
        }
        core.pc = (short) ((core.pc + 1) & bit8_mask);
    }

    private static void checkZ(int res, Core core) {
        if((res & bit8_mask)== 0) {
            core.mem.setStatusBit(2);
        }
        else {
            core.mem.clearStatusBit(2);
        }
    }

    private static void checkDC(int res, Core core) {
        if(res > 15) {
            core.mem.setStatusBit(1);
        }
        else {
            core.mem.clearStatusBit(1);
        }
    }

    private static void checkC(int res, Core core) {
        if(res > 255) {
            core.mem.setStatusBit(0);
        }
        else {
            core.mem.clearStatusBit(0);
        }
    }
}
