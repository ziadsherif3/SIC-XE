package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Pass2 {
    public static boolean twoOperand = false;
    public static boolean baseOn = false;
    public static int baseOperand;

    public static void flow(BufferedReader br, BufferedWriter wr, HashMap symtbl, HashMap littbl) throws IOException {
        String line = br.readLine();
        int num;
        while ((line = br.readLine()) != null) {
            while (line.charAt(0) == '.') {
                line = br.readLine();
            }
            num = doLine(line, wr, symtbl, littbl);
            if (num == 1) {
                Set<Map.Entry<Integer, Integer>> Table = littbl.entrySet();
                int ite = 0;
                String[][] division = new String[Table.size()][2];
                for (Object i : Table) {
                    division[ite] = i.toString().split("=");
                    wr.write(Pass1.hexafy(Integer.parseInt(division[ite++][1])) + "\n");
                }
                if (line.length() > 14) {

                    String opnd = line.substring(17, line.length()).trim();
                    if (symtbl.get(opnd) == null) {
                        wr.write("E" + Pass1.hexafy(Integer.parseInt(opnd)) + "\n");
                    } else {
                        wr.write("E" + Pass1.hexafy(Integer.parseInt(symtbl.get(opnd).toString())) + "\n");
                    }
                } else {
                    wr.write("E" + "\n");
                }
                break;
            }
        }
        return;

    }

    public static int doLine(String line, BufferedWriter wr, HashMap symtbl, HashMap littbl) throws IOException {
        String label = line.substring(0, 8).trim().toLowerCase();
        String operation;
        String operand;
        if (line.length() > 17) {
            operation = line.substring(9, 15).trim().toLowerCase();
            operand = line.substring(17, line.length()).trim().toLowerCase();
        } else {
            operation = line.substring(9, line.length()).trim().toLowerCase();
            operand = null;
        }
        String[] operands = new String[2];
        if (operand != null) {
            if (operand.contains(",")) {
                twoOperand = true;
                operands = operand.split(",");
            } else {
                operands[0] = operand;
            }
        }
        if (operation.equals("end")) {
            return 1;
        }
        if (Opcodes.frmttbl.get(operation.toUpperCase()) != null) {
            String frmt = Opcodes.frmttbl.get(operation.toUpperCase());
            if (frmt.equals("1")) {
                wr.write(Opcodes.optbl.get(operation.toUpperCase()).toString() + "\n");
            }
            if (frmt.equals("2")) {
                int n = 1;
                if (twoOperand) {
                    n = 2;
                }
                wr.write(Opcodes.optbl.get(operation.toUpperCase()));
                for (int i = 0; i < n; i++) {
                    if (symtbl.get(operands[i].toUpperCase()) != null) {
                        wr.write(symtbl.get(operands[i].toUpperCase()).toString());
                    } else {
                        if (operation.equals("shiftl") || operation.equals("shiftr")) {
                            wr.write(Integer.toString(Integer.parseInt(operands[i]) - 1));
                        } else {
                            wr.write(operands[i]);
                        }
                    }
                }
                if (n == 1) {
                    wr.write('0');
                }
                wr.write("\n");
            }
        }
        switch (operation) {
        case "word": {
            wr.write(Pass1.hexafy(Integer.parseInt(operands[0])) + "\n");
            break;
        }
        case "byte": {
            String words[] = operands[0].split("\'");
            if (words[0].equals("x")) {
                wr.write(words[1].toUpperCase());
            } else if (words[0].equals("c")) {
                for (int i = 0; i < words[1].length(); i++) {
                    char c = words[1].charAt(i);
                    int ascii = c;
                    wr.write(Integer.toHexString((int) ascii).toUpperCase());
                }
            }
            wr.write("\n");
            break;
        }
        case "base": {
            baseOn = true;
            baseOperand = Integer.parseInt(symtbl.get(operand.substring(1, operand.length())).toString());
            break;
        }
        case "nobase": {
            baseOn = false;
            break;
        }
        }
        twoOperand = false;
        return 0;
    }
}
