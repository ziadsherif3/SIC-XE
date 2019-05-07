package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        String fileName = "c_example.txt";
        String fileName2 = "b_example.txt";
        String fileName3 = "obj_file.txt";
        String fileName4 = "copyFile.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        BufferedReader objbr = new BufferedReader(new FileReader(new File(fileName4)));
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName2));
        BufferedWriter objwriter = new BufferedWriter(new FileWriter(fileName3));
        HashMap<String, Integer> symtbl = new HashMap<String, Integer>();
        Main.initializeSymTbl(symtbl);
        HashMap<Integer, Integer> littbl = new HashMap<Integer, Integer>();
        Opcodes.initializeTable();
        Pass1.flow(br, symtbl, writer, littbl);
        writer.write("\nEnd of first pass\n");
        Set<Map.Entry<String, Integer>> Table = symtbl.entrySet();
        int ite=0;
        String[][] division = new String[Table.size()][2];
        for(Object i : Table)
        {
            division[ite++] = i.toString().split("=");
        }
        Arrays.sort(division, (a, b) -> Integer.compare(Integer.parseInt(a[1]), Integer.parseInt(b[1])));
        writer.write("value         name\n");
        writer.write("------------------\n");
        for (String[] x : division) {
            if (!(x[0].equals("A") || x[0].equals("X") || x[0].equals("L") || x[0].equals("B") || x[0].equals("S")
                    || x[0].equals("T") || x[0].equals("F") || x[0].equals("PC") || x[0].equals("SW"))) {
                String str = String.format("%3s", x[1]) + "        " + String.format("%s", x[0]);
                writer.write(str);
                writer.write("\n");
            }
        }
        Pass2.flow(objbr, objwriter, symtbl);
        if (Pass1.errorFlag != 1) {
            writer.write("\nSuccsessful Assembly");
        }
        else {
            writer.write("\nIncomplete Assembly");

        }
        br.close();
        writer.close();
        objwriter.close();
    }

    public static void initializeSymTbl(HashMap<String, Integer> symtbl) {
        symtbl.put("A", 0);
        symtbl.put("X", 1);
        symtbl.put("L", 2);
        symtbl.put("B", 3);
        symtbl.put("S", 4);
        symtbl.put("T", 5);
        symtbl.put("F", 6);
        symtbl.put("PC", 8);
        symtbl.put("SW", 9);
    }

}
