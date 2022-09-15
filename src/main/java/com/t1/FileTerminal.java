package com.t1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileTerminal {
    public static String InputFile(String terminalPath) throws IOException {
        // FileWriter input = new FileWriter(terminalPath + "ArquivoInput.txt");
        // input.createTempFile("arg0", "arg1");
        BufferedReader br = new BufferedReader(new FileReader(terminalPath));
        String line = "";
        String input = "";
        try {
            while ((line = br.readLine()) != null) {
                input = line;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            br.close();
        }
        return input;
    }
}
// criar o arquivo
// entre com o comando no arquivo <>.txt
// if readline == true
// ler o arquivo
// guarda o que foi digitado
// deleta o arquivo