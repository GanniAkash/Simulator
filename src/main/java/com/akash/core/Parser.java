package com.akash.core;
import java.util.*;
import java.io.File;

public class Parser {
    public static void parse(String pathToHex, Memory mem) {
        try {
            File file = new File(pathToHex);
            Scanner reader = new Scanner(file);
            String word;
            String upperAddrString = "0000000000000000";
            label:
            while(reader.hasNextLine()) {
                word = reader.nextLine().substring(1);
                int lineLen = Integer.parseInt(word.substring(0, 2), 16)/2;
                String rw = word.substring(6, 8);
                int addrVal = Integer.parseInt(upperAddrString+word.substring(2, 6), 16)/2;
                if(addrVal > 256) continue;
                switch (rw) {
                    case "00":{
                        for (int i = 0; i < lineLen; i++) {
                            int j = 4 * i;
                            int opcode = Integer.parseInt(word.substring(10 + j, 12 + j) + word.substring(8 + j, 10 + j), 16);
                            mem.setInstruction((short) (addrVal + i), opcode);
                        }
                        break;
                    }
                    case "04": {
                        upperAddrString = word.substring(8, 12);
                        break;
                    }
                    case "01":
                        break label;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
