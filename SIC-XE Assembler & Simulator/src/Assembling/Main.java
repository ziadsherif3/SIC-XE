package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        String fileName = "a_example.txt";
        String fileName2 = "b_example.txt";
        File file = new File(fileName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName2));
        HashMap symtbl = new HashMap<String, Integer>();
        HashMap littbl = new HashMap<Integer, Integer>();
        Pass1.flow(br, symtbl, writer, littbl);
        writer.write("\nEnd of first pass\n");
        Set Table = symtbl.entrySet();
        int ite=0;
        String[][] division = new String[Table.size()][2];
        for(Object i : Table)
        {
            division[ite++] = i.toString().split("=");
        }
        Arrays.sort(division, (a, b) -> Integer.compare(Integer.parseInt(a[1]), Integer.parseInt(b[1])));
        writer.write("value         name\n");
        writer.write("------------------\n");
        for( String[] x : division)
        {
            String str = String.format("%3s",x[1]) +"        " + String.format("%s",x[0]);
            writer.write(str);
            writer.write("\n");
        }
        Pass2.flow(br, writer);
        if (Pass1.errorFlag != 1) {
            writer.write("\nSuccsessful Assembly");
        }
        else {
            writer.write("\nIncomplete Assembly");

        }
        br.close();
        writer.close();
    }

}
