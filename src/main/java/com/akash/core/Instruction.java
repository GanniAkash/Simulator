package com.akash.core;

public class Instruction {
    public enum OpCode {
        ADDWF, ANDWF, CLRF, CLRW, COMF, DECF, DECFSZ, INCF, INCFSZ,
        IORWF, MOVF, MOVWF, NOP, RLF, RRF, SUBWF, SWAPF, XORWF, BCF,
        BSF, BTFSC, BTFSS, ANDLW, CALL, CLRWDT, GOTO, IORLW, MOVLW,
        OPTION, RETLW, SLEEP, TRIS, XORLW
    }

    private final OpCode opcode;
    private final short[] args;

    Instruction(OpCode opcode, short... args) {
        this.opcode = opcode;
        this.args = args;
    }

    public OpCode getOpCode() {
        return this.opcode;
    }

    public short[] getArgs() {
        return this.args;
    }
}
