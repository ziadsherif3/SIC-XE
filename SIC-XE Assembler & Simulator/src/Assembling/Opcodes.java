package Assembling;

import java.util.HashMap;

public class Opcodes {
    public static HashMap optbl = new HashMap<String, String>();

    public static void initializeTable() {
        optbl.put("RMO", "AC");
    }
}