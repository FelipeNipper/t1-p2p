package fileApi;

import java.io.File;
import java.util.ArrayList;

public class MapFiles {
    private ArrayList<FileStruct> files = new ArrayList<FileStruct>();
    private String path = "src/assets";

    public MapFiles() {
        File[] dirFiles = new File(path).listFiles();

        // Erro
        if (dirFiles == null) {
            System.out.println("Erro! O diretorio informado nao existe");
            return;
        }

        // Carrega arquivos
        for (File file : dirFiles) {
            if (file.isFile()) {
                FileStruct _file = new FileStruct(file.getPath(), file.getName());
                files.add(_file);
            }
        }

    }

    public ArrayList<FileStruct> getFiles() {
        return files;
    }

    public ArrayList<FileStruct> searchFiles(String name) {
        ArrayList<FileStruct> toBeReturned = new ArrayList<FileStruct>();
        for (FileStruct file : files) {
            if (file.getName().toLowerCase().contains(name)) {
                toBeReturned.add(file);
            }
        }
        return toBeReturned;
    }

    public void print(ArrayList<FileStruct> list) {
        if (list == null) {
            list = files;
        }
        for (FileStruct file : list) {
            System.out.println("/////////////////");
            System.out.println("Nome: " + file.getName());
            System.out.println("Hash: " + file.getHash().toString());
            System.out.println("Path: " + file.getPath());
            System.out.println("/////////////////");
        }
    }
}
