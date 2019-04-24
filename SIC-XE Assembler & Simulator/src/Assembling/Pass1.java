package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class Pass1 {
    public static int LOCCTR = 0;
    public Pass1() {
    }

    public static String hexafy(int num) {
        String str = Integer.toHexString(num).toUpperCase();
        while (str.length() < 6) {
            str = '0' + str;
        }
        return str;
    }

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
            LOCCTR = Integer.parseInt(operand);
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
            wr.write("error [13] : ‘missing END statement '\n");
        }
        // assign storage locations to literals in pool
        // reset copy file

        return;
    }

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
        String label[] = first.split(" ");
        if (label.length > 0) {
            Object in = symtbl.get(label[0]);
            if (in == null) {
                symtbl.putIfAbsent(label[0], LOCCTR);
            } else {
                wr.write("error [04] : 'duplicate label definition:'  " + label[0] + " 'is already defined'\n");
            }
        }
        if (operation.equals("end")) {
            return 1;
        }
        switch (operation) {
        case "org": {
            LOCCTR = Integer.parseInt(operand);
            break;
        }
        case "resb": {
            LOCCTR += Integer.parseInt(operand);
            break;
        }
        case "equ": {
            symtbl.remove(label[0]);
            symtbl.putIfAbsent(label[0], operand);
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
                    littbl.putIfAbsent(LOCCTR, operand.subSequence(1, operand.length()));
                }
            }
            if (operation.equals("fix") || operation.equals("float") || operation.equals("hio")
                || operation.equals("norm") || operation.equals("sio") || operation.equals("tio")) {
                LOCCTR += 1;
            } else if (operation.equals("subr") || operation.equals("addr") || operation.equals("mulr")
                    || operation.equals("clear") || operation.equals("compr") || operation.equals("divr")
                    || operation.equals("rmo") || operation.equals("shiftr") || operation.equals("shiftl")
                    || operation.equals("sivc") || operation.equals("tixr")) {
                LOCCTR += 2;
            } else {
                LOCCTR += 3;
            }
            break;
        }
        }
        return 0;
    }

}
