package rmi;

import fileApi.FileStruct;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Peer implements Serializable {

    private static final long serialVersionUID = 1L;

    private String host;
    private int port;
    private long lastPing;
    private List<FileStruct> files;
    private FileRequest fileRequest;

    public Peer(String host, int port, List<FileStruct> files) {
        this.host = host;
        this.port = port;
        this.lastPing = System.nanoTime();
        this.files = files;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastPing() {
        return lastPing;
    }

    public void setLastPing(long lastPing) {
        this.lastPing = lastPing;
    }

    public List<FileStruct> getFiles() {
        return files;
    }

    public void setFiles(List<FileStruct> files) {
        this.files = files;
    }

    public FileRequest getFileRequest() {
        return fileRequest;
    }

    public void setFileRequest(FileRequest fileRequest) {
        this.fileRequest = fileRequest;
    }

    public boolean isPeerDead() {
        long now = System.nanoTime();
        long elapsedTime = now - this.lastPing;
        long convertedTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);

        return convertedTime > 30;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", files={" + this.getFilesNames() + "}" +
                '}';
    }

    private String getFilesNames() {
        String filesNames = "";
        if(this.files != null) {
            for (FileStruct fs : this.files) {
                filesNames += fs.getName() + ", ";
            }

            filesNames = filesNames.substring(0, filesNames.length()-2);
        }

        return filesNames;
    }
}
