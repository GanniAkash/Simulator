package com.akash.core;

public class Executor {
    private static final int bit8_mask = 0b1111_1111;
    private static final int bit12_mask = 0b1111_1111_1111;
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
                core.pc = (short) (k-1);
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
                    core.WReg = res;
                }
                else {
                    core.mem.setData(f, res);
                }
                break;
            }
        }
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
