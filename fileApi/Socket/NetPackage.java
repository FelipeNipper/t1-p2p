package fileApi.Socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

public class NetPackage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -7448291063913733021L;
    private int seq;
    private byte[] fileArray;

    public NetPackage() {
    }

    public void createObj(int seq, File file) {
        this.setSeq(seq);
        this.setFileArray(file);
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public void print() {
        System.out.println("---------------");
        System.out.println("obj: >");
        System.out.println("seq: " + seq);
        System.out.println("fileArray: " + fileArray);
        System.out.println("---------------");
    }

    public byte[] getFileArray() {
        return fileArray;
    }

    public void setFileArray(File file) {
        byte[] sendData = new byte[1000];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(sendData);
            fis.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.fileArray = sendData;
    }
}