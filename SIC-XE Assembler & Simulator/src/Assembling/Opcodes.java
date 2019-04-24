package Assembling;

import java.util.HashMap;

public class Opcodes {
    public static HashMap optbl = new HashMap<String, String>();

    public static void initializeTable() {
        optbl.put("ADD", "18");
        optbl.put("ADDF", "58");
        optbl.put("ADDR", "90");
        optbl.put("AND", "40");
        optbl.put("CLEAR", "4");
        optbl.put("COMP", "28");
        optbl.put("COMPF", "88");
        optbl.put("COMPR", "A0");
        optbl.put("DIV", "24");
        optbl.put("DIVF", "64");
        optbl.put("DIVR", "9C");
        optbl.put("FIX", "C4");
        optbl.put("FLOAT", "C0");
        optbl.put("HIO", "F4");
        optbl.put("J", "3C");
        optbl.put("JEQ", "30");
        optbl.put("JGT", "34");
        optbl.put("JLT", "38");
        optbl.put("JSUB", "48");
        optbl.put("LDA", "00");
        optbl.put("LDB", "68");
        optbl.put("LDCH", "50");
        optbl.put("LDF", "70");
        optbl.put("LDL", "08");
        optbl.put("LDS", "6C");
        optbl.put("LDT", "74");
        optbl.put("LDX", "04");
        optbl.put("LPS", "D0");
        optbl.put("MUL", "20");
        optbl.put("MULF", "60");
        optbl.put("MULR", "98");
        optbl.put("NORM", "C8");
        optbl.put("OR", "44");
        optbl.put("RD", "D8");
        optbl.put("RMO", "AC");
        optbl.put("RSUB", "4C");
        optbl.put("SHIFTL", "A4");
        optbl.put("SHIFTR", "A8");
        optbl.put("SIO", "F0");
        optbl.put("SSK", "EC");
        optbl.put("STA", "0C");
        optbl.put("STB", "78");
        optbl.put("STCH", "54");
        optbl.put("STF", "80");
        optbl.put("STI", "D4");
        optbl.put("STL", "14");
        optbl.put("STS", "7C");
        optbl.put("STSW", "E8");
        optbl.put("STT", "84");
        optbl.put("STX", "10");
        optbl.put("SUB", "1C");
        optbl.put("SUBF", "5C");
        optbl.put("SUBR", "94");
        optbl.put("SVC", "B0");
        optbl.put("TD", "E0");
        optbl.put("TIO", "F8");
        optbl.put("TIX", "2C");
        optbl.put("TIXR", "B8");
        optbl.put("WD", "DC");
    }
}