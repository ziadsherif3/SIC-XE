package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Pass1 {
    public static int LOCCTR = 0;
    public static String progName;
    public static String startAddrss;
    public static int progLen = -1;
    public static int errorFlag = 0;
    public static int prefixFlag = 0;
    public static int twoOperand = 0;
    //Array List of LOCCTR for all instructions
    public static ArrayList<Integer> memArray = new ArrayList<>();
    //Array List of Literals
    public static ArrayList<Integer> litarray = new ArrayList<>();

    public Pass1() {
    }

    /** Changes the LOCCTR from decimal to 6 digit hexadecimal number **/
    public static String hexafy(int num) {
        //defines a string with the LOCCTR integer number in hexadecimal
    	String str = Integer.toHexString(num).toUpperCase();
        //Loop to convert it to 6 digit address
    	//22B~00022B
    	while (str.length() < 6) {
            str = '0' + str;
        }
        return str;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void flow(BufferedReader br, HashMap symtbl, BufferedWriter wr, HashMap littbl) throws IOException {
        String line;
        int num = 0;
        // read line
        line = br.readLine();
        //check if it is a comment line
        while (line.charAt(0) == '.') {
            //write the comment line as it is to Copy file
        	wr.write("       " + line + '\n');
            //read another line
        	line = br.readLine();
        }
        //Extract the operation from the line
        String operation = line.substring(9, 14).trim().toLowerCase();
        String operand = null;
        //Extracts the operand from the line
        if (line.length() > 17) {
            operand = line.substring(17, line.length());
        }
        // if opcode= 'START'
        if (operation.equals("start")) {
            progName = line.substring(0,8);
            if (operand != null) {
                if (operand.length() > 4) {
                    wr.write("error [32] : 'START operand can not be larger than 4 decimal places'\n");
                    errorFlag = 1;
                }
                //LOCCTR equals the starting address
                LOCCTR = Integer.parseInt(operand.trim(), 16);
                //change the LOCCTR to Hexadecimal so we can write it in copy file
                startAddrss = hexafy(LOCCTR);
            }
            wr.write(hexafy(LOCCTR) + ' ' + line + '\n');
            //Add the Start LOCCTR to array
            memArray.add(LOCCTR);
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
            memArray.add(LOCCTR);
            num = doLine(line, symtbl, littbl, wr);
            if (num == 1) {
                flag = 1;
                break;
            }
        }
        //check if there is an END statement
        //print error if not
        if (flag != 1) {
            wr.write("error [13] : 'missing END statement' '\n");
            errorFlag = 1;
        }
        // assign storage locations to literals in pool
        for( int i : litarray) {
            littbl.putIfAbsent(i, LOCCTR);
            LOCCTR += 3;
        }
        // reset copy file

        return;
    }

    @SuppressWarnings("rawtypes")
    public static int doLine(String line, HashMap<Object, Object> symtbl, HashMap littbl, BufferedWriter wr)
            throws IOException {
        String first = null;
        try {
            first = line.substring(0, 8);
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
        	operation = line.substring(9, 15).trim().toLowerCase();
            operand = line.substring(17, line.length());
        } else if (line.length() > 9){
            operation = line.substring(9, line.length()).toLowerCase();
            operand = null;
        }
        // if line length is less than 9
        //then there is no operation
        //print error
        else {	
                wr.write("error [99] : 'There must be an operation in each line'\n");
                errorFlag = 1;
                return 0;
        }
        //bassem fill this
        if (operation.charAt(0) == '+') {
            operation = operation.toUpperCase().substring(1, operation.length());
            String allowedFrmts = Opcodes.frmttbl.get(operation);
            if (allowedFrmts == null) {
                wr.write("error [08] : 'Unrecognized operation code'\n");
                errorFlag = 1;
            }
            boolean allowedFrmt4 = false;
            if (allowedFrmts != null) {
                for (char c : allowedFrmts.toCharArray()) {
                    if (c == '4') {
                        allowedFrmt4 = true;
                        break;
                    }
                }
            }
            if (!allowedFrmt4) {
                wr.write("error [24] : 'Format 4 is not allowed with this operation'\n");
                errorFlag = 1;
            }
            prefixFlag = 1;
        }
        String label[] = first.split(" ");
        if (label.length > 0) {
            Object in = symtbl.get(label[0].toLowerCase());
            //if the label doesn't exist in symbol table
            if (in == null) {
               //check the optable for current label name
            	//if it exists print error, can't use mnemonics names for labels
            	if (Opcodes.optbl.get(label[0]) != null) {
                    wr.write("error [30] : 'Using mnemonics as labels is not allowed'\n");
                    errorFlag = 1;
                } else {
                    //if the label isn't a mnemonic
                	//add the label to symbol table with its current LOCCTR if it isn't already there
                	symtbl.putIfAbsent(label[0].toLowerCase(), LOCCTR);
                }
            } 	
            // if label name already exist in symbol table
            // print error, duplicate label name found
            else {
                wr.write("error [04] : 'duplicate label definition:'  " + label[0] + " 'is already defined'\n");
                errorFlag = 1;
            }
            //check if first letter in label is a space
            // if so print error
            if (line.charAt(0) == ' ') {
                wr.write("error [15] : '1st charcter of label can not be blank'\n");
                errorFlag = 1;
            }
        }
        // errors
        String operands[] = null;
        if (operand != null) {
           //check if there are spaces in the middle of operation field
        	//if so print an error
        	if (operand.split(" ").length > 1) {
                if (!operation.toLowerCase().equals("byte")) {
                    wr.write("error [18] : 'operand field can not contain spaces in the middle'\n");
                    errorFlag = 1;
                }
            }
            //check if there are 2 operands
        	//if so split the 2 operands
        	if (operand.contains(",")) {
                twoOperand = 1;
                operands = operand.split(",");
                /* Is it 2 or 1?*/
               //check if there is more than 2 operands after the split, if so print an error
                if (operands.length > 2) {
                    wr.write("error [23] : 'Can not have more than two operands'\n");
                    errorFlag = 1;
                }
                //check if the operation for the 2 operand instruction is a format 2 instruction
                //and check if it has indexed addressing mode
                //if not print an error
                if (!(Opcodes.frmttbl.get(operation.toUpperCase()).equals("2"))
                        && !operands[1].toLowerCase().equals("x")) {
                    wr.write(
                            "error [97] : 'This instruction can not have two operands unless in case of indexed addressing mode'\n");
                    errorFlag = 1;
                }
            }
        }
        //check if operation field has spaces
        //print error if so
        if (operation.split(" ").length > 1) {
            wr.write("error [17] : 'operartion field can not contain spaces in the middle'\n");
            errorFlag = 1;
        }
        //check if the label has spaces in the middle
        if (label.length > 1) {
            wr.write("error [16] : 'label field can not contain spaces in the middle'\n");
            errorFlag = 1;
        }
        //check for space violation, if 8th character isn't a space print an error
        if (line.charAt(8) != ' ') {
            wr.write(
                    "error [14] : '9th character of instruction must be blank, label ends at 8, operation starts at 10 '\n");
            errorFlag = 1;
        }
        if (line.length() > 16) {
            //keep checking for space violation and print error if present
        	if (line.charAt(15) != ' ' || line.charAt(16) != ' ') {
                wr.write(
                        "error [19] : '16th and 17th characters of instruction must be blank, operation ends at 16, operation starts at 19 '\n");
                errorFlag = 1;
            }
        }
        //check if the first char of the operation is a space, print error if so
        if (line.charAt(9) == ' ') {
            wr.write("error [20] : '1st character of operation can not be blank'\n");
            errorFlag = 1;
        }
        //check if there is an operand
        if (operand != null) {
            //if so character 17 can't be a blank as it is the start of the operand
        	if (line.charAt(17) == ' ') {
                wr.write("error [21] : '1st character of operand can not be blank'\n");
                errorFlag = 1;
            }
        }
        //check if operand ends at 35th char, if not print an error
        if (line.length() > 35 && line.charAt(35) != '.') {
            wr.write("error [22] : 'operand must end at 35th character'\n");
            errorFlag = 1;
        }

        
        if (operation.equals("end")) {
        //if there is more than 1 word after split, and the operation is end, print error, end can't have a label
        	if (label.length > 0) {
                wr.write("error [05] : 'END operation can not have a label'\n");
                errorFlag = 1;
            }
            return 1;
        }
        //if the mnemonic doesn't exist in optable print error
        if (Opcodes.optbl.get(operation.toUpperCase()) == null) {
            wr.write("error [08] : 'Unrecognized operation code'\n");
            errorFlag = 1;
        }
        if (Opcodes.frmttbl.get(operation.toUpperCase()) != null) {
            if (Opcodes.frmttbl.get(operation.toUpperCase()).equals("2")) {
                if (twoOperand == 1) {
                    if (symtbl.get(operands[0]) == null && symtbl.get(operands[1]) == null) {
                        wr.write("error [12] : 'Incorrect register address'\n");
                        errorFlag = 1;
                    }
                }
            }
        }
       //switch on the operations to increment the Location counter and Program Length according to each instruction type
        switch (operation) {
        case "rsub": {
        	LOCCTR += 3;
            progLen += 3;
            //check if rsub has an operand, if so print violation error
            if (operand != null) {
                wr.write("error [06] : 'RSUB operation can not have an operand'\n");
                errorFlag = 1;
            }
            break;
        }
        case "org": {
            //check if operand is missing after ORG instruction, print error if so
        	if(operand == null) {
                wr.write("error [98] : 'ORG operation must have an operand'\n");
                errorFlag = 1;
            } else {
               //bassem fill this
            	if (symtbl.get(operand.toLowerCase()) != null) {
                    LOCCTR = Integer.parseInt(symtbl.get(operand.toLowerCase()).toString(), 16);
                } 
            	//bassem fill this
            	else {
                    LOCCTR = Integer.parseInt(operand.trim(), 16);
                }
            }
            //check if ORG instruction have a label after it instead of an operand, if so print an error
        	if (label.length > 0) {
                wr.write("error [05] : 'ORG operation can not have a label'\n");
                errorFlag = 1;
            }
            break;
        }
        case "resb": {
            //check if there is an operand after RESB operation, if not print an error
        	if(operand == null)
            {
                wr.write("error [98] : 'RESB operation must have an operand'\n");
                errorFlag = 1;
            } else {
               //check if the Operand has more than 4 decimal places, if so print an error
            	if (operand.length() > 4) {
                    wr.write("error [32] : 'RESB operand can not be larger than 4 decimal places'\n");
                    errorFlag = 1;
                }
                LOCCTR += Integer.parseInt(operand);
                progLen += Integer.parseInt(operand);
            }
            break;
        }
        case "equ": {
        	//check if there is an operand after EQU operation, if not print an error
        	if(operand == null)
            {
                wr.write("error [98] : 'EQU operation must have an operand'\n");
                errorFlag = 1;
            }
            //removes the label that was linked to the current LOCCTR and equates it with the operand
        	symtbl.remove(label[0]);
            symtbl.putIfAbsent(label[0].toLowerCase(), symtbl.get(operand));
            break;
        }
        case "byte": {
        	//check if there is an operand after BYTE operation, if not print an error
        	if(operand == null)
            {
                wr.write("error [98] : 'BYTE operation must have an operand'\n");
                errorFlag = 1;
            }
            String words[] = operand.split("\'");
            //check for operand violation
            if (words.length != 2) {
                wr.write("error [23] : 'Error in operand'\n");
                errorFlag=1;
            } else if (words[0].length() > 1) {
                wr.write("error [23] : 'Error in operand'\n");
                errorFlag=1;
            } 
            //bassem fill this
            else if (Character.toUpperCase(words[0].charAt(0)) == 'X') {
                for (char c : words[1].toCharArray()) {
                    if (Character.digit(c, 16) == -1) {
                        wr.write("error [23] : 'Error in operand'\n");
                        errorFlag = 1;
                    }
                }
            }

            //if the byte is defined as character
            if (words[0].toLowerCase().charAt(0) == 'c') {
                //if the char length is bigger than 15, print an error can't exceed 15 letters
            	if (words[1].length() > 15) {
                    wr.write("error [31] : 'Length of char string can not exceed 15'\n");
                    errorFlag = 1;
                } else {
                   //increment location counter and program length by the length of the string 
                	LOCCTR += words[1].length();
                    progLen += words[1].length();
                }
            } 
            //if the byte is defined as a hexadecimal
            else if (words[0].toLowerCase().charAt(0) == 'x') {
            	//if the hexadecimal length is bigger than 14, print an error can't exceed 14 numbers
            	if (words[1].length() > 14) {
                    wr.write("error [31] : 'Length of hexadecimal can not exceed 14'\n");
                    errorFlag = 1;
                } 
            	//increment location counter and program length by half the length of the hexadecimal +1
            	else {
                    LOCCTR += (words[1].length() + 1) / 2;
                    progLen += (words[1].length() + 1) / 2;
                }
            } else {
                //if not char or hexadecimal print error
            	wr.write("error [29] : 'BYTE operand not a char string or hexadecimal'\n");
                errorFlag = 1;
            }
            break;
        }
        case "word": {
        	//check if there is an operand after WORD operation, if not print an error
        	if(operand == null)
            {
                wr.write("error [98] : 'WORD operation must have an operand'\n");
                errorFlag = 1;
            }
            //bassem fill this
        	operand = operand.replace("-", "");
            //
        	//check if WORD operand is larger than 4 decimal places, print error if so
        	if(operand.length() >4)
            {
                wr.write("error [32] : 'WORD operand can not be larger than 4 decimal places'\n");
                errorFlag = 1; 
            }
            LOCCTR += 3;
            progLen += 3;
            break;
        }
        case "resw": {
        	//check if there is an operand after RESW operation, if not print an error
        	if(operand == null)
            {
                wr.write("error [98] : 'RESW operation must have an operand'\n");
                errorFlag = 1;
            }
        	//check if RESW operand is larger than 4 decimal places, print error if so
        	if(operand.length() >4)
            {
                wr.write("error [32] : 'RESW operand can not be larger than 4 decimal places'\n");
                errorFlag = 1; 
            }
            //increment location counter and program length by 3x signed decimal integer value of the operand
        	LOCCTR += 3 * Integer.parseInt(operand);
            progLen += 3 * Integer.parseInt(operand);
            break;
        }
        case "nobase": {
            break;
        }
        case "base": {
            break;
        }
        default: {
            //bassem fill this, msh fahm l literal ana
        	if (operand != null) {
                if (operand.charAt(0) == '=') {
                    String [] words = new String[2];
                    try {
                        words = operand.split("\'");
                        Integer.parseInt(words[1], 16);
                    } catch (Exception e) {
                        wr.write("error [10] : ' Literal is not a number'\n");
                        errorFlag = 1;
                    }
                    litarray.add(Integer.parseInt(words[1]));
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
           //type 1 format instructions
            //check if they have a prefix if so print an error
            if (operation.equals("fix") || operation.equals("float") || operation.equals("hio")
                    || operation.equals("norm") || operation.equals("sio") || operation.equals("tio")) {
                {
                    LOCCTR += 1;
                    progLen += 1;
                    if (prefixFlag == 1) {
                        wr.write("error [07] : 'type 1 format operation can not have a + prefix'\n");
                        errorFlag = 1;
                        prefixFlag = 0;
                    }
                }
            } 
            //type 2 format instructions
            else if (operation.equals("subr") || operation.equals("addr") || operation.equals("mulr")
                    || operation.equals("clear") || operation.equals("compr") || operation.equals("divr")
                    || operation.equals("rmo") || operation.equals("shiftr") || operation.equals("shiftl")
                    || operation.equals("svc") || operation.equals("tixr")) {
                {
                   //check if operand is missing from type 2 instructions, if so print an error
                	if (operand == null) {
                        wr.write("error [98] : 'Format 2 operations must have an operand'\n");
                        errorFlag = 1;
                    }
                    LOCCTR += 2;
                    progLen += 2;
                  //check if they have a prefix if so print an error
                    if (prefixFlag == 1) {
                        wr.write("error [07] : 'type 2 format operation can not have a + prefix'\n");
                        errorFlag = 1;
                        prefixFlag = 0;
                    }
                }
            } 
            //if the operation has a prefix then it is a Format 4 operation, check if operand is missing if so print an error
            else if (prefixFlag == 1) {
                if (operand == null) {
                    wr.write("error [98] : 'Format 4 operations must have an operand'\n");
                    errorFlag = 1;
                }
                LOCCTR += 4;
                progLen += 4;
            } 
            //if not format 1,2,4 then it is a format 3 instruction
            //check if operand is missing and pint an error
            else {
                if (operand == null) {
                    wr.write("error [98] : 'Format 3 operations must have an operand'\n");
                    errorFlag = 1;
                }
                LOCCTR += 3;
                progLen += 3;
            }
            break;
        }
        }
        prefixFlag = 0;
        twoOperand = 0;
        return 0;
    }

}
