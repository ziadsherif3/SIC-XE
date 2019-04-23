package Assembling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

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
        br.close();
        writer.close();
    }

}
