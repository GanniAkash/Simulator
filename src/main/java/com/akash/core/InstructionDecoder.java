package com.akash.core;

public class InstructionDecoder {
    private static final int type_mask = 0b1100_0000_0000;
    private static final int type_offset = 10;
    private static final int sub_type_mask = 0b0011_0000_0000;
    private static final int sub_type_offset = 8;
    private static final int bit_mask = 0b0000_1110_0000;
    private static final int bit_offset = 5;
    private static final int bit_file_mask = 0b0000_0001_1111;
    private static final int literal_mask = 0b0000_1111_1111;
    private static final int literal_mask_9 = 0b0001_1111_1111;
    private static final int tris_mask = 0b0000_0000_0111;
    private static final int tris_offset = 3;
    private static final int operation_sub_type_mask = 0b0011_1100_0000;
    private static final int operation_sub_type_offset = 6;
    private static final int d_mask = 0b0000_0010_0000;
    private static final int d_offset = 5;

    public static Instruction decode(int instruction) {
        switch (instruction) {
            case 64: {
                return new Instruction(Instruction.OpCode.CLRW, (short) 0);
            }
            case 0: {
                return new Instruction(Instruction.OpCode.NOP, (short) 0);
            }
            case 4: {
                return new Instruction(Instruction.OpCode.CLRWDT, (short) 0);
            }
            case 2: {
                return new Instruction(Instruction.OpCode.OPTION, (short) 0);
            }
            case 3: {
                return new Instruction(Instruction.OpCode.SLEEP, (short) 0);
            }
        }

        if((instruction & ~tris_mask) >> tris_offset == 0) {
            return new Instruction(Instruction.OpCode.TRIS, (short) (instruction&tris_mask));
        }

        switch((instruction & type_mask) >> type_offset) {
            case 0b00: return decodeArithmeticOrientedInstruction(instruction);
            case 0b01: return decodeBitOrientedInstruction(instruction);
            case 0b11: return decodeLiteralOrientedInstruction(instruction);
            case 0b10: return decodeBranchOrientedInstruction(instruction);
            default: return new Instruction(Instruction.OpCode.NOP, (short) 0);
        }
    }

    private static Instruction decodeArithmeticOrientedInstruction(int instruction) {
        int d = (instruction & d_mask) >> d_offset;
        int f = instruction & bit_file_mask;
        switch((instruction & operation_sub_type_mask) >> operation_sub_type_offset) {
            case 0b0111:
                return new Instruction(Instruction.OpCode.ADDWF, (short) f, (short) d);
            case 0b0101:
                return new Instruction(Instruction.OpCode.ANDWF, (short) f, (short) d);
            case 0b0001:
                return new Instruction(Instruction.OpCode.CLRF, (short) f);
            case 0b1001:
                return new Instruction(Instruction.OpCode.COMF, (short) f, (short) d);
            case 0b0011:
                return new Instruction(Instruction.OpCode.DECF, (short) f, (short) d);
            case 0b1011:
                return new Instruction(Instruction.OpCode.DECFSZ, (short) f, (short) d);
            case 0b1010:
                return new Instruction(Instruction.OpCode.INCF, (short) f, (short) d);
            case 0b1111:
                return new Instruction(Instruction.OpCode.INCFSZ, (short) f, (short) d);
            case 0b0100:
                return new Instruction(Instruction.OpCode.IORWF, (short) f, (short) d);
            case 0b1000:
                return new Instruction(Instruction.OpCode.MOVF, (short) f, (short) d);
            case 0b0000:
                return new Instruction(Instruction.OpCode.MOVWF, (short) f);
            case 0b1101:
                return new Instruction(Instruction.OpCode.RLF, (short) f, (short) d);
            case 0b1100:
                return new Instruction(Instruction.OpCode.RRF, (short) f, (short) d);
            case 0b0010:
                return new Instruction(Instruction.OpCode.SUBWF, (short) f, (short) d);
            case 0b1110:
                return new Instruction(Instruction.OpCode.SWAPF, (short) f, (short) d);
            case 0b0110:
                return new Instruction(Instruction.OpCode.XORWF, (short) f, (short) d);
            default: return new Instruction(Instruction.OpCode.NOP, (short) 0);
        }
    }

    private static Instruction decodeBitOrientedInstruction(int instruction) {
        int b = (instruction & bit_mask) >> bit_offset;
        int f = instruction & bit_file_mask;
        switch((instruction & sub_type_mask) >> sub_type_offset) {
            case 0b00:
                return new Instruction(Instruction.OpCode.BCF, (short) f, (short) b);
            case 0b01:
                return new Instruction(Instruction.OpCode.BSF, (short) f, (short) b);
            case 0b10:
                return new Instruction(Instruction.OpCode.BTFSC, (short) f, (short) b);
            case 0b11:
                return new Instruction(Instruction.OpCode.BTFSS, (short) f, (short) b);
            default: return new Instruction(Instruction.OpCode.NOP, (short) 0);
        }
    }

    private static Instruction decodeLiteralOrientedInstruction(int instruction) {
        int k = instruction & literal_mask;
        switch((instruction & sub_type_mask) >> sub_type_offset) {
            case 0b10:
                return new Instruction(Instruction.OpCode.ANDLW, (short) k);
            case 0b11:
                return new Instruction(Instruction.OpCode.XORLW, (short) k);
            case 0b01:
                return new Instruction(Instruction.OpCode.IORLW, (short) k);
            case 0b00:
                return new Instruction(Instruction.OpCode.MOVLW, (short) k);
            default: return new Instruction(Instruction.OpCode.NOP, (short) 0);
        }
    }

    private static Instruction decodeBranchOrientedInstruction(int instruction) {
        switch((instruction & sub_type_mask)>>sub_type_offset) {
            case 0b01:
                return new Instruction(Instruction.OpCode.CALL, (short) (instruction&literal_mask));
            case 0b00:
                return new Instruction(Instruction.OpCode.RETLW, (short) (instruction&literal_mask));
            default:
                return new Instruction(Instruction.OpCode.GOTO, (short) (instruction&literal_mask_9));
        }
    }

}
