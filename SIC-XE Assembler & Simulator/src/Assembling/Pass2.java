package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class Pass2 {

    public static void flow(BufferedReader br, BufferedWriter wr, HashMap symtbl) throws IOException {
        String line = br.readLine();
        int num;
        while ((line = br.readLine()) != null) {
            while (line.charAt(0) == '.') {
                line = br.readLine();
            }
            num = doLine(line);
            if (num == 1) {
                if (line.length() > 14) {

                    String opnd = line.substring(17, line.length()).trim();
                    if (symtbl.get(opnd) == null) {
                        wr.write("E" + Pass1.hexafy(Integer.parseInt(opnd)));
                    } else {
                        wr.write("E" + Pass1.hexafy(Integer.parseInt(symtbl.get(opnd).toString())));
                    }
                

            } else {
                wr.write("E");
            }
            break;
        }
    }return;

    }

    public static int doLine(String line) {
        String label = line.substring(0, 7).trim().toLowerCase();
        String operation;
        String operand;
        if (line.length() > 17) {
            operation = line.substring(9, 14).trim().toLowerCase();
            operand = line.substring(17, line.length()).trim().toLowerCase();
        } else {
            operation = line.substring(9, line.length()).trim().toLowerCase();
            operand = null;
        }
        if (operation.equals("end"))
            return 1;
        switch (operation) {
        case "word": {

        }
        case "byte": {

        }
        }
        return 0;
    }

}
