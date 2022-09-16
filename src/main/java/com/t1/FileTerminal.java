package com.t1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileTerminal {
    public static String inputFile(String terminalPath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(terminalPath));
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                return line;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            br.close();
        }
        return "";
    }

    public static void cleanFile(String terminalPath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(terminalPath));
        bw.write("");
    }

    public static int hashFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        String hashFile = "";
        try {
            while ((line = br.readLine()) != null) {
                hashFile += line;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            br.close();
        }
        return hashFile.hashCode();
    }
}