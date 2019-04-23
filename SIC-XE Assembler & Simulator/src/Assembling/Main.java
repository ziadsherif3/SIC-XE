package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

        for(Object i : Table)
        {
            String[] division = i.toString().split("=");
            for(String w : division)
            {
                writer.write(w);
            }
            writer.write("\n");
        }
        Pass2.flow(br, writer);
        br.close();
        writer.close();
    }

}
