package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class Pass1 {
    public static int LOCCTR = 0;
    public static int errorFlag = 0;
    public static int prefixFlag = 0;
    public static int twoOperand = 0;

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
        int num = 0;
        line = br.readLine();
        while (line.charAt(0) == '.') {
            wr.write("       " + line + '\n');
            line = br.readLine();
        }
        String operation = line.substring(9, 14).trim().toLowerCase();
        String operand = null;
        if (line.length() > 17) {
            operand = line.substring(17, line.length());
        }
        if (operation.equals("start")) {
            if (operand != null) {
                if (operand.length() > 4) {
                    wr.write("error [32] : 'START operand can not be larger than 4 decimal places'\n");
                    errorFlag = 1;
                }
                LOCCTR = Integer.parseInt(operand.trim(), 16);
            }
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
            errorFlag = 1;
        }
        // assign storage locations to literals in pool
        // reset copy file

        return;
    }

    @SuppressWarnings("rawtypes")
    public static int doLine(String line, HashMap<Object, Object> symtbl, HashMap littbl, BufferedWriter wr)
            throws IOException {
        String first = null;
        try {
            first = line.substring(0, 7);
        } catch (Exception e) {

            wr.write("error [99] : 'There must be an operation in each line'\n");
            errorFlag = 1;
            return 0;
        }
        String operation;
        String operand;
        // If longer than 14 and shorter than 17, user entered spaces after character
        // 14. Handle this error.
        // Who said if else (< 14) means that there will be an operation (something
        // written at char 9).
        // Boundaries need to be re-checked
        // For me (Ziad) check msgs
        if (line.length() > 17) {
            operation = line.substring(9, 14).trim().toLowerCase();
            operand = line.substring(17, line.length());
        } else if (line.length() > 9){
            operation = line.substring(9, line.length()).toLowerCase();
            operand = null;
        }
        else {
                wr.write("error [99] : 'There must be an operation in each line'\n");
                errorFlag = 1;
                return 0;
        }
        if (operation.charAt(0) == '+') {
            operation = operation.substring(1, operation.length());
            String allowedFrmts = Opcodes.frmttbl.get(operation);
            if(allowedFrmts.equals(null)){
                wr.write("error [08] : 'Unrecognized operation code'\n");
                errorFlag = 1;
            }
            boolean allowedFrmt4 = false;
            for(char c : allowedFrmts.toCharArray()){
                if(c == '4'){
                    allowedFrmt4 = true;
                    break;
                }
            }
            if(!allowedFrmt4){
                wr.write("error [24] : 'Format 4 isn't allowed with this operation'\n");
                errorFlag = 1;
            }
            prefixFlag = 1;
        }
        String label[] = first.split(" ");
        if (label.length > 0) {
            Object in = symtbl.get(label[0]);
            if (in == null) {
                if (Opcodes.optbl.get(label[0]) != null) {
                    wr.write("error [30] : 'Using mnemonics as labels is not allowed'\n");
                    errorFlag = 1;
                } else {
                    symtbl.putIfAbsent(label[0].toLowerCase(), LOCCTR);
                }
            } else {
                wr.write("error [04] : 'duplicate label definition:'  " + label[0] + " 'is already defined'\n");
                errorFlag = 1;
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
                if (!operation.toLowerCase().equals("byte")) {
                    wr.write("error [18] : 'operand field can not contain spaces in the middle'\n");
                    errorFlag = 1;
                }
            }
            if (operand.contains(",")) {
                twoOperand = 1;
                operands = operand.split(",");
                // Is it 2 or 1?
                if (operands.length > 2) {
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
            wr.write(
                    "error [14] : '9th character of instruction must be blank, label ends at 8, operation starts at 10 '\n");
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
            LOCCTR += 3;
            if (operand != null) {
                wr.write("error [06] : 'RSUB operation can not have an operand'\n");
                errorFlag = 1;
            }
            break;
        }
        case "org": {
            if(operand == null)
            {
                wr.write("error [98] : 'ORG operation must have an operand'\n");
                errorFlag = 1;
            } else {
                LOCCTR = Integer.parseInt(operand);
            }
            if (label.length > 0) {
                wr.write("error [05] : 'ORG operation can not have a label'\n");
                errorFlag = 1;
            }
            break;
        }
        case "resb": {
            if(operand == null)
            {
                wr.write("error [98] : 'RESB operation must have an operand'\n");
                errorFlag = 1;
            } else {
                if (operand.length() > 4) {
                    wr.write("error [32] : 'RESB operand can not be larger than 4 decimal places'\n");
                    errorFlag = 1;
                }
                LOCCTR += Integer.parseInt(operand);
            }
            break;
        }
        case "equ": {
            if(operand == null)
            {
                wr.write("error [98] : 'EQU operation must have an operand'\n");
                errorFlag = 1;
            }
            symtbl.remove(label[0]);
            symtbl.putIfAbsent(label[0].toLowerCase(), operand);
            break;
        }
        case "byte": {
            if(operand == null)
            {
                wr.write("error [98] : 'BYTE operation must have an operand'\n");
                errorFlag = 1;
            }
            String words[] = operand.split("\'");
            if (words.length != 2) {
                wr.write("error [23] : 'Error in operand'\n");
                errorFlag=1;
            } else if (words[0].length() > 1) {
                wr.write("error [23] : 'Error in operand'\n");
                errorFlag=1;
            } else if (Character.toUpperCase(words[0].charAt(0)) == 'X') {
                for (char c : words[1].toCharArray()) {
                    if (Character.digit(c, 16) == -1) {
                        wr.write("error [23] : 'Error in operand'\n");
                        errorFlag = 1;
                    }
                }
            }

            if (words[0].toLowerCase().charAt(0) == 'c') {
                if (words[1].length() > 15) {
                    wr.write("error [31] : 'Length of char string can not exceed 15'\n");
                    errorFlag = 1;
                } else {
                    LOCCTR += words[1].length();
                }
            } else if (words[0].toLowerCase().charAt(0) == 'x') {
                if (words[1].length() > 14) {
                    wr.write("error [31] : 'Length of hexadecimal can not exceed 14'\n");
                    errorFlag = 1;
                } else {
                    LOCCTR += (words[1].length() + 1) / 2;
                }
            } else {
                wr.write("error [29] : 'BYTE operand not a char string or hexadecimal'\n");
                errorFlag = 1;
            }
            break;
        }
        case "word": {
            if(operand == null)
            {
                wr.write("error [98] : 'WORD operation must have an operand'\n");
                errorFlag = 1;
            }
            operand = operand.replace("-", "");
            if(operand.length() >4)
            {
                wr.write("error [32] : 'BYTE operand can not be larger than 4 decimal places'\n");
                errorFlag = 1; 
            }
            LOCCTR += 3;
            break;
        }
        case "resw": {
            if(operand == null)
            {
                wr.write("error [98] : 'RESW operation must have an operand'\n");
                errorFlag = 1;
            }
            if(operand.length() >4)
            {
                wr.write("error [32] : 'RESW operand can not be larger than 4 decimal places'\n");
                errorFlag = 1; 
            }
            LOCCTR += 3 * Integer.parseInt(operand);
            break;
        }
        default: {
            if (operand != null) {
                if (operand.charAt(0) == '#') {
                    try {
                        operand = operand.substring(1, operand.length());
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
                            operand = operands[1];
                        }
                        if (symtbl.get(operand) == null) {
                            operand = operand.toLowerCase();
                            if (operand.charAt(0) != 'a' || operand.charAt(0) != 'b' || operand.charAt(0) != 's'
                                    || operand.charAt(0) != 'x' || operand.charAt(0) != 't' || operand.charAt(0) != 'l'
                                    || operand.charAt(0) != 'f') {
                                //wr.write("error [09] : 'undefined symbol in operand'\n");
                                //errorFlag = 1;
                               //will be handled in pass 2 due to forward refrencing");
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
                        prefixFlag = 0;
                    }
                }
            } else if (operation.equals("subr") || operation.equals("addr") || operation.equals("mulr")
                    || operation.equals("clear") || operation.equals("compr") || operation.equals("divr")
                    || operation.equals("rmo") || operation.equals("shiftr") || operation.equals("shiftl")
                    || operation.equals("sivc") || operation.equals("tixr")) {
                {
                    if (operand == null) {
                        wr.write("error [98] : 'Format 2 operations must have an operand'\n");
                        errorFlag = 1;
                    }
                    LOCCTR += 2;
                    if (prefixFlag == 1) {
                        wr.write("error [07] : 'type 2 format operation can not have a + prefix'\n");
                        errorFlag = 1;
                        prefixFlag = 0;
                    }
                }
            } else if (prefixFlag == 1) {
                if (operand == null) {
                    wr.write("error [98] : 'Format 4 operations must have an operand'\n");
                    errorFlag = 1;
                }
                LOCCTR += 4;
            } else {
                if (operand == null) {
                    wr.write("error [98] : 'Format 3 operations must have an operand'\n");
                    errorFlag = 1;
                }
                LOCCTR += 3;
            }
            break;
        }
        }
        prefixFlag = 0;
        twoOperand = 0;
        return 0;
    }

}
