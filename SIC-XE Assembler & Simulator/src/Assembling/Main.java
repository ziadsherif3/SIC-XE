package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Main {
    public static int LOCCTR = 0;
    public static void main(String[] args) throws IOException {
        String fileName = "a_example.txt";
        String fileName2 = "b_example.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName2));
        HashMap symtbl = new HashMap<String, Integer>();
        HashMap littbl = new HashMap<Integer, Integer>();
        pass1(br, symtbl, writer, littbl);
        br.close();
        writer.close();
    }

    public static String hexafy(int num) {
        String str = Integer.toHexString(num).toUpperCase();
        while (str.length() < 6) {
            str = '0' + str;
        }
        return str;
    }

    public static void pass1(BufferedReader br, HashMap symtbl, BufferedWriter wr, HashMap littbl) throws IOException {
        String line;
        int num=0;
        line = br.readLine();
        String operation = line.substring(9, 14).trim().toLowerCase();
        String operand = line.substring(17, line.length());
        if (operation.equals("start")) {
            LOCCTR = Integer.parseInt(operand);
            wr.write(hexafy(LOCCTR) + ' ' + line + '\n');
        } else {
            num = doLine(line, symtbl, LOCCTR, littbl);
        }
        if (num == 1) {
            // assign storage locations to literals in pool
            // reset copy file
            return;
        }
        while ((line = br.readLine()) != null) {
            num = doLine(line, symtbl, LOCCTR, littbl);
            wr.write(hexafy(LOCCTR) + ' ' + line + '\n');
            if (num == 1)
                break;
            operation = line.substring(9, 14).trim().toLowerCase();
            if (operation.equals() || operation.equals() || operation.equals() || operation.equals()
                    || operation.equals() || operation.equals() || operation.equals()) {
                LOCCTR += 1;
            } else if (operation.equals("subr") || operation.equals("addr") || operation.equals("mulr")
                    || operation.equals("clear") || operation.equals("compr") || operation.equals("divr")
                    || operation.equals("rmo") || operation.equals("shiftr") || operation.equals("shiftl")
                    || operation.equals("sivc") || operation.equals("tixr")) {
                LOCCTR += 2;
            } else {
                LOCCTR += 3;
            }
        }
        // assign storage locations to literals in pool
        // reset copy file

        return;
    }

    public static int doLine(String line, HashMap symtbl, HashMap littbl) throws IOException {
        String first = line.substring(0, 7);
        String operation = line.substring(9, 14);
        String operand = line.substring(17, line.length());
        String label[] = first.split(" ");
        if (label.length > 0) {
            symtbl.putIfAbsent(label[0], LOCCTR);
        }
        if (operation.toLowerCase().equals("end")) {
            return 1;
        }
        switch (operation) {
        case "ORG": {
            LOCCTR = Integer.parseInt(operand);
        }
        default: {
            if(operand.charAt(0)=='#')
            {
                littbl.putIfAbsent(LOCCTR, operand.subSequence(1, operand.length()));
            }
        }
        }
        return 0;
    }
}
