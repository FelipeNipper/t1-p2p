package com.t1;

import java.io.File;

public class FileTerminal {
    // public static String InputFile(String pathDir){
    // File input = new File(pathDir, "ArquivoInput.txt");
    // input.createTempFile(arg0, arg1)
    // }

    public static String InputTempFile(String pathDir) {
        try {
            // create a temp file
            File f = File.createTempFile("geeks", ".txt", new File(pathDir));

            // check if the file is created
            if (f.exists()) {

                // the file is created
                // as the function returned true
                System.out.println("Temp File created: "
                        + f.getName());
            }

            else {

                // display the file cannot be created
                // as the function returned false
                System.out.println("Temp File cannot be created: "
                        + f.getName());
            }
            f.deleteOnExit();
        } catch (Exception e) {

            // display the error message
            System.err.println(e);
        }
        return "";
    }
}
// criar o arquivo
// entre com o comando no arquivo <>.txt
// if readline == true
// ler o arquivo
// guarda o que foi digitado
// deleta o arquivo