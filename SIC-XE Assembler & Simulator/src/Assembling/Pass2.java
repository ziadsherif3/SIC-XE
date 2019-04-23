package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Pass2 {
    public Pass2() {
    }

    public static void flow(BufferedReader br, BufferedWriter wr) throws IOException {
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            while (line.charAt(0) == '.') {
                line = br.readLine();
            }
            int num = doLine(line);
            if (num == 1) {
                if (line.length() > 14) {
                    wr.write("E" + Pass1.hexafy(Integer.parseInt(line.substring(17, line.length()))));
                } else {
                    wr.write("E");
                }
                break;
            }
        }
        wr.write("\nSuccsessful Assembly");
        return;
    }

    public static int doLine(String line) {
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
        if (operand.trim().toLowerCase().equals("end"))
            return 1;
        switch (operation) {
            case "word" :
            {

            }
            case "byte" :
            {

            }
        }
        return 0;
    }

}
