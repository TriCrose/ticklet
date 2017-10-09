package uk.ac.cam.tssn2.fjava.tick0;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

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

    public void writeAndFlush(int[] buf) throws IOException {
        for (int i : buf) write(i);
        flush();
    }

    public int read() throws IOException {
        return r.readInt();
    }

    public int[] read(int maxInts) throws IOException {
        byte[] bytes = new byte[maxInts * 4];
        int bytesRead = r.read(bytes, 0, bytes.length);
        if (bytesRead == -1) {
            return null;
        } else {
            // Convert byte array to int array
            int[] integers = new int[bytesRead/4];
            ByteBuffer.wrap(bytes, 0, bytesRead)
                      .order(ByteOrder.BIG_ENDIAN)
                      .asIntBuffer()
                      .get(integers);
            return integers;
        }
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