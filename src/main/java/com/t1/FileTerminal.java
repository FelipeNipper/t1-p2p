package com.t1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileTerminal {
    public static String inputFile(String terminalPath) throws IOException {
        // FileWriter input = new FileWriter(terminalPath + "ArquivoInput.txt");
        // input.createTempFile("arg0", "arg1");
        BufferedReader br = new BufferedReader(new FileReader(terminalPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(terminalPath));
        String line = "";
        String input = "";
        try {
            while ((line = br.readLine()) != null) {
                input = line;
                // bw.write("");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            br.close();
        }
        return input;
    }

    public static void cleanFile() {

    }
}
// criar o arquivo
// entre com o comando no arquivo <>.txt
// if readline == true
// ler o arquivo
// guarda o que foi digitado
// deleta o arquivo