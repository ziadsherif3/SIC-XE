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
    public static int errCounter = 0;
    //indirect addressing flag
    public static byte n;
    //immediate addressing flag
    public static byte i;
    //indexed addressing flag
    public static byte x;
    //base relative addressing flag
    public static byte b;
    //PC relative addressing flag
    public static byte p;
    //flag to distinguish between format 3 and 4
    public static byte e;

    /** Function to handle the Text record in the OBJFILE **/
    public static void recordHandler(BufferedWriter wr, String newStr) throws IOException {
        //Each Text record can't exceed 60 bytes
    	//Checks if This text record has to be written when it reaches 60 bytes
    	if (concatObj.length() + newStr.length() > 60) {
           //Prints the Starting address Object code in this text record(hex)
    		//followed by the length of object code in this record in bytes(hex)
    		//followed by Object code, represented in hex (2 coloumns per byte)
    		wr.write("T" + stored + "^" + newHexafy(concatObj.length()/2, 2) + "^"
                    + concatObj + "\n");
            //Reset the concatenated string for the next Text record
    		concatObj = "";
            //String where the starting address of the text record is stored
            stored = Pass1.hexafy(Pass1.memArray.get(counter-1));
        }
        //concatenate the 2 strings together if they are less than 60 byes 
    	concatObj += newStr;
    }

    // same function used in pass 1 with 1 minor tweak
    // now the function asks for the length of the string u want it to produce
    // it was always 6 in the LOCCTR in pass 1
    // here we use in multiple purposes while printing to object file
    public static String newHexafy(int num, int len) {
        String str = Integer.toHexString(num).toUpperCase();
        //adds 0 before the string if it is too short
        while (str.length() < len) {
            str = '0' + str;
        }
        //splits the string if it is too long
        if(str.length() > len) {
            str = str.substring(str.length() - len ,str.length());
        }
        return str.toUpperCase();
    }

    public static void flow(BufferedReader br, BufferedWriter wr, HashMap symtbl, HashMap littbl) throws IOException {
        String line;
        int num;
        //Starting writing into the object code
        //Write the header following by the program name, starting address and program length
        wr.write("H" + Pass1.progName + "^" + Pass1.startAddrss + "^" + Pass1.hexafy(Pass1.progLen) + "\n");
        //String where the stating address of next record is stored
        stored = Pass1.hexafy(Pass1.memArray.get(counter));
       //Start reading from copy file
        while ((line = br.readLine()) != null) {
            //if line is a comment
        	while (line.charAt(0) == '.') {
                //read another line
        		line = br.readLine();
            }
            //doLine breaks line into label,operation and operand
        	//sets the addressing mode flag as well as the object code for the instructions
        	num = doLine(line, wr, symtbl, littbl);
            //if end operation is found
        	if (num == 1) {
                Set<Map.Entry<Integer, Integer>> Table = littbl.entrySet();
                int ite = 0;
                String[][] division = new String[Table.size()][2];
                for (Object i : Table) {
                    division[ite] = i.toString().split("=");
                    recordHandler(wr, Pass1.hexafy(Integer.parseInt(division[ite++][0])));
                }
                wr.write("T" + stored + "^" + newHexafy(concatObj.length() / 2, 2) + "^" + concatObj + "\n");
                //End record
                //Prints E followed by Address of first executable instruction in object program
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
        //gets current LOCCTR from memory array
        int locctr = Pass1.memArray.get(counter++);
        //Checks if there are errors in the error array
        if (!Pass1.errArray.isEmpty()) {
            //checks if the error counter is less than the size of the error array
        	if (errCounter < Pass1.errArray.size()) {
                //checks if the LOCCTR is the same as the LOCCTR of the current error
        		if (locctr == Pass1.errArray.get(errCounter)) {
                    errCounter++;
                    return -1;
                }
            }
        }
       //Breaks each line into operation and operands based on its size (same as in pass1)
        if (line.length() > 17) {
            operation = line.substring(9, 15).trim().toLowerCase();
            operand = line.substring(17, line.length()).trim().toLowerCase();
        } else {
            operation = line.substring(9, line.length()).trim().toLowerCase();
            operand = null;
        }
        String[] operands = new String[2];
        //check for double operand
        if (operand != null) {
            if (operand.contains(",")) {
                twoOperand = true;
                operands = operand.split(",");
            } else {
                operands[0] = operand;
            }
        }
        //check for END statement
        if (operation.equals("end")) {
            return 1;
        }

        //Check for Format 4 instruction
        //if so Set e Flag to 1 else 0
        if (operation.charAt(0) == '+') {
            e = 1;
            //Gets instruction name starting from after the '+'
            operation = operation.substring(1, operation.length());
        } else {
            e = 0;
        }
        //Check for which format
        if (Opcodes.frmttbl.get(operation.toUpperCase()) != null) {
            String frmt = Opcodes.frmttbl.get(operation.toUpperCase());
            //if format 1 instruction, Call the Text record handling function, no need to set flags
            if (frmt.equals("1")) {
                recordHandler(wr, Opcodes.optbl.get(operation.toUpperCase()).toString());
            } else if (frmt.equals("2")) {
                //Set 1 operand as default, then check if the instruction has 2 operands
            	int n = 1;
                if (twoOperand) {
                    n = 2;
                }
                String tempr = "";
                for (int i = 0; i < n; i++) {
                    if (symtbl.get(operands[i].toUpperCase()) != null) {
                                tempr += symtbl.get(operands[i].toUpperCase()).toString();
                    } else {
                        // Check if shift left or right Operation and handle it
                    	if (operation.equals("shiftl") || operation.equals("shiftr")) {
                                    tempr += Integer.toString(Integer.parseInt(operands[i]) - 1);
                        } else {
                            tempr += operands[i];
                        }
                    }
                }
                //calls text record function with operation opcode + operand opcode
                recordHandler(wr, Opcodes.optbl.get(operation.toUpperCase()) + tempr);
                if (n == 1) {
                    recordHandler(wr, "0");
                }
            }
            //RSUB has no operand
            //so we call record handler with RSUB object code
            else if(operation.equals("rsub")) {
                recordHandler(wr, "4F0000");
            } 
            //Now to handle format 3 or 4 instructions
            	else if (frmt.equals("3,4") && !operation.equals("rsub")) {
                int disp;
                //Check operands and Start setting the Address mode flags
                //check for indirect addressing
                if (operand.charAt(0) == '#') {
                    n = 0;
                } else {
                    n = 1;
                }
                //check for immediate addressing
                if (operand.charAt(0) == '@') {
                    i = 0;
                } else {
                    i = 1;
                }
                //check for indexed addressing, with double operand instructions
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
                    //check for displacement to know if PC relative addressing
                    if (disp > 255) {
                        disp = disp - locctr - 3;
                        p = 1;
                    }
                    if (operand.charAt(0) == '*' && disp > 255) {
                        disp = -3;
                        p = 1;
                    }
                }
                //Check if base relative
                if (Math.abs(disp) > 2048) {
                    p = 0;
                    b = 1;
                    disp = Integer.parseInt(symtbl.get(operand).toString()) - baseOperand;
                }
                //if format 4 instruction set PC and Base relative flags to 0
                //and set the displacement
                if (e == 1) {
                    b = 0;
                    p = 0;
                    if (operand.charAt(0) == '#') {
                        System.out.println("yes");
                    }
                    if (symtbl.get(operand) != null) {
                        String store = symtbl.get(operand).toString();
                        disp = Integer.parseInt(store);
                    } else {
                        disp = Integer.parseInt(operand);
                    }
                }
                //Calculating object code based on the flags
                //Change from binary to decimal then hexadecimal using the hexaify function
                int temp = Integer.parseInt(Opcodes.optbl.get(operation.toUpperCase()), 16) + (n * 2) + i;
                int temp2;
                if (e == 0) {
                    temp2 = e * (int) Math.pow(2, 0) + p * (int) Math.pow(2, 1) + b * (int) Math.pow(2, 2)
                            + x * (int) Math.pow(2, 3);
                } else {
                    temp2 = e * (int) Math.pow(2, 0) + p * (int) Math.pow(2, 1) + b * (int) Math.pow(2, 2)
                            + x * (int) Math.pow(2, 3);
                }
                recordHandler(wr, newHexafy(temp, 2) + newHexafy(temp2, 1) + newHexafy(disp, 3));

            }
        }

        switch (operation) {
        case "word": {
            recordHandler(wr, Pass1.hexafy(Integer.parseInt(operands[0])));
            break;
        }
        //in case of byte operation, check if hexadecimal or character declaration
        //then calculate object code from Parsing ASCII letter to integer then changing it to a hexadecimal string (same as Hexafiy function)
        case "byte": {
            String words[] = operands[0].split("\'");
            if (words[0].equals("x")) {
                recordHandler(wr, words[1].toUpperCase());
            } else if (words[0].equals("c")) {
                String tempoo= "";
                for (int i = 0; i < words[1].length(); i++) {
                    char c = words[1].charAt(i);
                    int ascii = c;
                    tempoo += Integer.toHexString((int) ascii).toUpperCase();
                }
                recordHandler(wr, tempoo);
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
