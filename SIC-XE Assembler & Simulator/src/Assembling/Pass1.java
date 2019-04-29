package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class Pass1 {
    public static int LOCCTR = 0;
    public static int errorFlag =0;
    public static int prefixFlag =0;
    public static int twoOperand =0;
    public Pass1() {
    }

    public static String hexafy(int num) {
        String str = Integer.toHexString(num).toUpperCase();
        while (str.length() < 6) {
            str = '0' + str;
        }
        return str;
    }

    @SuppressWarnings("rawtypes")
	public static void flow(BufferedReader br, HashMap symtbl, BufferedWriter wr, HashMap littbl) throws IOException {
        String line;
        int num=0;
        line = br.readLine();
        while (line.charAt(0) == '.') {
            wr.write("       " + line + '\n');
            line = br.readLine();
        }
        String operation = line.substring(9, 14).trim().toLowerCase();
        String operand = line.substring(17, line.length());
        if (operation.equals("start")) {
            LOCCTR = Integer.parseInt(operand, 16);
            wr.write(hexafy(LOCCTR) + ' ' + line + '\n');
        } else {
            num = doLine(line, symtbl, littbl, wr);
        }
        if (num == 1) {
            // assign storage locations to literals in pool
            // reset copy file
            return;
        }
        int flag = 0;
        while ((line = br.readLine()) != null) {
            while (line.charAt(0) == '.') {
                wr.write("       " + line + '\n');
                line = br.readLine();
            }
            wr.write(hexafy(LOCCTR) + ' ' + line + '\n');
            num = doLine(line, symtbl, littbl, wr);
            if (num == 1) {
                flag = 1;
                break;
            }
        }
        if (flag != 1) {
            wr.write("error [13] : 'missing END statement' '\n");
            errorFlag =1;
        }
        // assign storage locations to literals in pool
        // reset copy file

        return;
    }

    @SuppressWarnings("rawtypes")
	public static int doLine(String line, HashMap symtbl, HashMap littbl, BufferedWriter wr) throws IOException {
        String first = line.substring(0, 7);
        String operation;
        String operand;
        if (line.length() > 14) {
            operation = line.substring(9, 14).trim().toLowerCase();
            operand = line.substring(17, line.length());
        } else {
            operation = line.substring(9, line.length()).toLowerCase();
            operand = null;
        }
        if (operation.charAt(0) == '+') {
            prefixFlag = 1;
            operation = operation.substring(1, operation.length());
        }
        String label[] = first.split(" ");
        if (label.length > 0) {
            Object in = symtbl.get(label[0]);
            if (in == null) {
                symtbl.putIfAbsent(label[0].toLowerCase(), LOCCTR);
            } else {
                wr.write("error [04] : 'duplicate label definition:'  " + label[0] + " 'is already defined'\n");
                errorFlag =1;
            }        
            if (line.charAt(0) == ' ') {
                wr.write("error [15] : '1st charcter of label can not be blank'\n");
                errorFlag = 1;
            }
        }
        // errors
        String operands[] = null;
        if (operand != null) {
            if (operand.split(" ").length > 1) {
                wr.write("error [18] : 'operarnd field can not contain spaces in the middle'\n");
                errorFlag = 1;
            }
            if (operand.contains(",")) {
                twoOperand = 1;
                operands = operand.split(",");
                if (operands.length > 1) {
                    wr.write("error [23] : 'Can not have more than two operands'\n");
                    errorFlag = 1;
                }
            }
        }
        if (operation.split(" ").length > 1) {
            wr.write("error [17] : 'operartion field can not contain spaces in the middle'\n");
            errorFlag = 1;
        }
        if (label.length > 1) {
            wr.write("error [16] : 'label field can not contain spaces in the middle'\n");
            errorFlag = 1;
        }
        if (line.charAt(8) != ' ') {
            wr.write("error [14] : '9th character of instruction must be blank, label ends at 8, operation starts at 10 '\n");
            errorFlag = 1;
        }
        if (line.length() > 16) {
            if (line.charAt(15) != ' ' || line.charAt(16) != ' ') {
                wr.write(
                        "error [19] : '16th and 17th characters of instruction must be blank, operation ends at 16, operation starts at 19 '\n");
                errorFlag = 1;
            }
        }
        if (line.charAt(9) == ' ') {
            wr.write("error [20] : '1st character of operation can not be blank'\n");
            errorFlag = 1;
        }
        if (operand != null) {
            if (line.charAt(17) == ' ') {
                wr.write("error [21] : '1st character of operand can not be blank'\n");
                errorFlag = 1;
            }
        }
        if (line.length() > 35 && line.charAt(35) != '.') {
            wr.write("error [22] : 'operand must end at 35th character'\n");
            errorFlag = 1;
        }

        if (operation.equals("end")) {
            if (label.length > 0) {
                wr.write("error [05] : 'END operation can not have a label'\n");
                errorFlag = 1;
            }
            return 1;
        }
        if (Opcodes.optbl.get(operation.toUpperCase()) == null) {
            wr.write("error [08] : 'Unrecognized operation code'\n");
                errorFlag = 1;
        }
        switch (operation) {
        case "rsub": {
            if (operand != null) {
                wr.write("error [06] : 'RSUB operation can not have an operand'\n");
                errorFlag = 1;
            }
            break;
        }
        case "org": {
            LOCCTR = Integer.parseInt(operand);
            if (label.length > 0) {
                wr.write("error [05] : 'ORG operation can not have a label'\n");
                errorFlag = 1;
            }
            break;
        }
        case "resb": {
            LOCCTR += Integer.parseInt(operand);
            break;
        }
        case "equ": {
            symtbl.remove(label[0]);
            symtbl.putIfAbsent(label[0].toLowerCase(), operand);
            break;
        }
        case "byte": {
            String words[] = operand.split("\'");
            LOCCTR += words[1].length();
            break;
        }
        case "word": {
            LOCCTR += 3;
            break;
        }
        case "resw": {
            LOCCTR += 3 * Integer.parseInt(operand);
            break;
        }
        default: {
            if (operand != null) {
                if (operand.charAt(0) == '#') {
                    try {
                        Integer.parseInt(operand, 16);
                    } catch (Exception e) {
                        wr.write("error [10] : 'Immediate operand is not a number'\n");
                        errorFlag = 1;
                    }
                    littbl.putIfAbsent(LOCCTR, operand.subSequence(1, operand.length()));
                } else {
                    int n = 1;
                    if (twoOperand == 1) {
                        operand = operands[0];
                        n = 2;
                    }
                    for (int i = 0; i < n; i++) {
                        if (i == 1) {
                            System.out.println(line);
                            operand = operands[1];
                        }
                        if (symtbl.get(operand) == null) {
                            operand = operand.toLowerCase();
                            if (operand.charAt(0) != 'a' || operand.charAt(0) != 'b' || operand.charAt(0) != 's'
                                    || operand.charAt(0) != 'x' || operand.charAt(0) != 't' || operand.charAt(0) != 'l'
                                    || operand.charAt(0) != 'f') {
                                wr.write("error [09] : 'undefined symbol in operand'\n");
                                errorFlag = 1;
                            }
                        }
                    }
                }
            }
            if (operation.equals("fix") || operation.equals("float") || operation.equals("hio")
                    || operation.equals("norm") || operation.equals("sio") || operation.equals("tio")) {
                {
                    LOCCTR += 1;
                    if (prefixFlag == 1) {
                        wr.write("error [07] : 'type 1 format operation can not have a + prefix'\n");
                        errorFlag = 1;
                    }
                }
            } else if (operation.equals("subr") || operation.equals("addr") || operation.equals("mulr")
                    || operation.equals("clear") || operation.equals("compr") || operation.equals("divr")
                    || operation.equals("rmo") || operation.equals("shiftr") || operation.equals("shiftl")
                    || operation.equals("sivc") || operation.equals("tixr")) {
                {
                    LOCCTR += 2;
                    if (prefixFlag == 1) {
                        wr.write("error [07] : 'type 2 format operation can not have a + prefix'\n");
                        errorFlag = 1;
                    }
                }
            } else {
                LOCCTR += 3;
            }
            break;
        }
        }
        return 0;
    }

}
