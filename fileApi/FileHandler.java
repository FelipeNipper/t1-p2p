package fileApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class FileHandler {
    // Estrutura do arquivo
    private FileStruct fileStruct;
    // Para criar os arquivos com nome "arq001";
    private int fileNameCounter = 1;
    // Tamanho fixo para cara arquivo
    private byte[] buffer = new byte[1000];

    public FileHandler(FileStruct fileStruct) {
        this.fileStruct = fileStruct;
    }

    // Quebra o arquivo em pedacos de tamanho fixo
    public int split() throws IOException {
        // Arquivo montado
        File file = new File(fileStruct.getPath());

        try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                String filePartName = String.format("%s.%03d", fileStruct.getName(), fileNameCounter++);
                File newFile = new File(fileStruct.getSplittingPath() + filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
            }
        }
        return fileNameCounter;
    }

    // Recupera o pedaco N do arquivo
    public File getFile(int lastReadFileNumer) {
        // Corrige nome do arquivo
        String lastReadFile = Integer.toString(lastReadFileNumer);
        while (lastReadFile.length() < 3) {
            lastReadFile = "0" + lastReadFile;
        }
        System.out.println("arquivo: " + lastReadFile);
        String composedName = fileStruct.getSplittingPath() + fileStruct.getName() + "." + lastReadFile;
        return new File(composedName);
    }

    // Salva um arquivo recebido
    public void saveFile(int lastReadFileNumer, byte[] fileByteArray) throws IOException {
        // Corrige nome do arquivo
        String lastReadFile = Integer.toString(lastReadFileNumer);
        while (lastReadFile.length() < 3) {
            lastReadFile = "0" + lastReadFile;
        }
        System.out.println("arquivo: " + lastReadFile);
        String composedName = fileStruct.getMergingPath() + fileStruct.getName() + "." + lastReadFile;
        File f = new File(composedName); // Creating the file
        FileOutputStream outToFile = new FileOutputStream(f);
        outToFile.write(fileByteArray);
        outToFile.close();
    }

    public void mergeFiles() throws IOException {
        System.out.println("mergeando arquivos");
        String endFile = fileStruct.getPath();
        try (FileOutputStream fos = new FileOutputStream(endFile);
                BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (File f : listOfFilesToMerge()) {
                Files.copy(f.toPath(), mergingStream);
            }
        }

    }

    public List<File> listOfFilesToMerge() {
        String composedName = fileStruct.getMergingPath() + fileStruct.getName() + ".001";
        File tmpFile = new File(composedName);
        String tmpName = tmpFile.getName();// {name}.{number}
        String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.'));// remove .{number}
        File[] files = tmpFile.getParentFile()
                .listFiles((File dir, String name) -> name.matches(destFileName + "[.]\\d+"));
        Arrays.sort(files);// ensuring order 001, 002, ..., 010, ...
        return Arrays.asList(files);
    }
}
