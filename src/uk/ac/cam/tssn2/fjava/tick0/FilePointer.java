package uk.ac.cam.tssn2.fjava.tick0;

import java.io.*;

public class FilePointer {
    private RandomAccessFile r;
    private DataOutputStream d;

    public FilePointer(String filename) throws IOException {
        r = new RandomAccessFile(filename, "rw");
        d = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(r.getFD())));
    }

    public void write(int i) throws IOException {
        d.writeInt(i);
    }

    public int read() throws IOException {
        return r.readInt();
    }

    public int[] read(int maxSize) {
        return null;
    }

    public long length() throws IOException {
        return r.length();
    }

    public void flush() throws IOException {
        d.flush();
    }

    public void seek(long pos) throws IOException {
        r.seek(pos);
    }

    public void close() throws IOException {
        r.close();
        d.close();
    }
}