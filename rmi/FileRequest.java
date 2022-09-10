package rmi;

import java.io.Serializable;

public class FileRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String receiverHost;
    private int receiverPort;
    private String fileName;

    public FileRequest(String receiverHost, int receiverPort, String fileName) {
        this.receiverHost = receiverHost;
        this.receiverPort = receiverPort;
        this.fileName = fileName;
    }

    public String getReceiverHost() {
        return receiverHost;
    }

    public void setReceiverHost(String receiverHost) {
        this.receiverHost = receiverHost;
    }

    public int getReceiverPort() {
        return receiverPort;
    }

    public void setReceiverPort(int receiverPort) {
        this.receiverPort = receiverPort;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
