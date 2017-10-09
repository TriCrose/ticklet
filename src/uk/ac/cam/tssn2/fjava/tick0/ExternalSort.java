package uk.ac.cam.tssn2.fjava.tick0;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ExternalSort {
    public static void sort(String f1, String f2) throws FileNotFoundException, IOException {
        // Two pointers to file 1
        FilePointer a1 = new FilePointer(f1);
        FilePointer a2 = new FilePointer(f1);

        // Two pointers to file 2
        FilePointer b1 = new FilePointer(f2);
        FilePointer b2 = new FilePointer(f2);
    }

    private static String byteToHex(byte b) {
        String r = Integer.toHexString(b);
        if (r.length() == 8) return r.substring(6);
        return r;
    }

    public static String checkSum(String f) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream ds = new DigestInputStream(new FileInputStream(f), md);
            byte[] b = new byte[512];
            while (ds.read(b) != -1);

            String computed = "";
            for(byte v : md.digest()) computed += byteToHex(v);

            return computed;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "<error computing checksum>";
    }

    public static void main(String[] args) throws Exception {
        String f1 = args[0];
        String f2 = args[1];
        sort(f1, f2);
        System.out.println("The checksum is: " + checkSum(f1) + "\nand the correct checksum is: 468c1c2b4c1b74ddd44ce2ce775fb35c");
    }
}
