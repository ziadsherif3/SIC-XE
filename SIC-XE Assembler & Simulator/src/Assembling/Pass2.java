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
    public static String concatObj ="";
    public static String stored;
    public static int baseOperand;
    public static int counter = 0;
    public static byte n;
    public static byte i;
    public static byte x;
    public static byte b;
    public static byte p;
    public static byte e;

    public static void recordHandler(BufferedWriter wr, String newStr) throws IOException {
        if (concatObj.length() + newStr.length() > 60) {
            wr.write("T" + stored + "^" + newHexafy(concatObj.length()/2, 2) + "^"
                    + concatObj + "\n");
            concatObj = "";
            stored = Pass1.hexafy(Pass1.memArray.get(counter-1));
        }
        concatObj += newStr;
    }

    public static String newHexafy(int num, int len) {
        String str = Integer.toHexString(num).toUpperCase();
        while (str.length() < len) {
            str = '0' + str;
        }
        if(str.length() > len) {
            str = str.substring( str.length() - len ,str.length());
        }
        return str.toUpperCase();
    }

    public static void flow(BufferedReader br, BufferedWriter wr, HashMap symtbl, HashMap littbl) throws IOException {
        String line;
        int num;
        wr.write("H" + Pass1.progName + "^" + Pass1.startAddrss + "^" + Pass1.hexafy(Pass1.progLen) + "\n");
        stored = Pass1.hexafy(Pass1.memArray.get(counter));
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
                    recordHandler(wr, Pass1.hexafy(Integer.parseInt(division[ite++][0])));
                }
                wr.write("T" + stored + "^" + newHexafy(concatObj.length() / 2, 2) + "^" + concatObj + "\n");
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
        int locctr = Pass1.memArray.get(counter++);
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

        if (operation.charAt(0) == '+') {
            e = 1;
            operation = operation.substring(1, operation.length());
        } else {
            e = 0;
        }
        if (Opcodes.frmttbl.get(operation.toUpperCase()) != null) {
            String frmt = Opcodes.frmttbl.get(operation.toUpperCase());
            if (frmt.equals("1")) {
                recordHandler(wr, Opcodes.optbl.get(operation.toUpperCase()).toString());
            } else if (frmt.equals("2")) {
                int n = 1;
                if (twoOperand) {
                    n = 2;
                }
                recordHandler(wr, Opcodes.optbl.get(operation.toUpperCase()));
                for (int i = 0; i < n; i++) {
                    if (symtbl.get(operands[i].toUpperCase()) != null) {
                        recordHandler(wr, symtbl.get(operands[i].toUpperCase()).toString());
                    } else {
                        if (operation.equals("shiftl") || operation.equals("shiftr")) {
                            recordHandler(wr, Integer.toString(Integer.parseInt(operands[i]) - 1));
                        } else {
                            recordHandler(wr, operands[i]);
                        }
                    }
                }
                if (n == 1) {
                    recordHandler(wr, "0");
                }
            }else if(operation.equals("rsub")) {
                recordHandler(wr, "4F0000");
            } else if (frmt.equals("3,4") && !operation.equals("rsub")) {
                int disp;
                if (operand.charAt(0) == '#') {
                    n = 0;
                } else {
                    n = 1;
                }
                if (operand.charAt(0) == '@') {
                    i = 0;
                } else {
                    i = 1;
                }
                if (twoOperand && operands[1].toUpperCase().equals("X")) {
                    x = 1;
                } else {
                    x = 0;
                }
                if (operand.charAt(0) == '@' || operand.charAt(0) == '#') {
                    operand = operand.substring(1, operand.length());
                }
                try {
                    if (twoOperand) {
                        Integer.parseInt(operands[0]);
                    } else {
                        Integer.parseInt(operand);
                    }
                    disp = Integer.parseInt(operand);
                    b = 0;
                    p = 0;
                } catch (Exception e) {
                    p = 0;
                    if (operand.charAt(0) == '=') {
                        String[] words = operand.split("\'");
                        disp = Integer.parseInt(littbl.get(Integer.parseInt(words[1])).toString());
                    } else if (operand.charAt(0) == '*') {
                        disp = locctr;
                    } else {
                        if(twoOperand){
                            operand=operands[0];
                        }
                        disp = Integer.parseInt(symtbl.get(operand).toString());
                    }
                    if (disp > 255) {
                        disp = disp - locctr - 3;
                        p = 1;
                    }
                    if (operand.charAt(0) == '*' && disp > 255) {
                        disp = -3;
                        p = 1;
                    }
                }
                if (Math.abs(disp) > 2048) {
                    p = 0;
                    b = 1;
                    disp = Integer.parseInt(symtbl.get(operand).toString()) - baseOperand;
                }
                if (e == 1) {
                    b = 0;
                    p = 0;
                    disp = Integer.parseInt(symtbl.get(operand).toString());
                }
                int temp = Integer.parseInt(Opcodes.optbl.get(operation.toUpperCase()), 16) + (n * 2) + i;
                recordHandler(wr, Pass2.newHexafy(temp, 2));
                if (e == 0) {
                    temp = e * (int) Math.pow(2, 0) + p * (int) Math.pow(2, 1) + b * (int) Math.pow(2, 2)
                            + x * (int) Math.pow(2, 3);
                } else {
                    temp = e * (int) Math.pow(2, 0) + p * (int) Math.pow(2, 1) + b * (int) Math.pow(2, 2)
                            + x * (int) Math.pow(2, 3);
                }
                recordHandler(wr, Pass2.newHexafy(temp, 1));
                recordHandler(wr, Pass2.newHexafy(disp, 3));

            }
        }
        switch (operation) {
        case "word": {
            recordHandler(wr, Pass1.hexafy(Integer.parseInt(operands[0])));
            break;
        }
        case "byte": {
            String words[] = operands[0].split("\'");
            if (words[0].equals("x")) {
                recordHandler(wr, words[1].toUpperCase());
            } else if (words[0].equals("c")) {
                for (int i = 0; i < words[1].length(); i++) {
                    char c = words[1].charAt(i);
                    int ascii = c;
                    recordHandler(wr, Integer.toHexString((int) ascii).toUpperCase());
                }
            }
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
