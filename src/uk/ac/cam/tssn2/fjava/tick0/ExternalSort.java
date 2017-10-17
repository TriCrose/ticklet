package uk.ac.cam.tssn2.fjava.tick0;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ExternalSort {
    private static final int INTS_PER_BLOCK = 300000;

    public static void sort(String f1, String f2) throws FileNotFoundException, IOException {
        // Initialisation
        FilePointer fileA = new FilePointer(f1);
        FilePointer fileB = new FilePointer(f2);
        if (fileA.length() == 0) return;

        // This stores each block
        List<Block> blocks = new LinkedList<>();

        /* First: sort the blocks using quicksort */

        while (true) {
            // Read as many bytes as we can fit into memory
            int[] block = fileA.read(INTS_PER_BLOCK);

            // If we get null then it means we're at the end of the file
            if (block == null) break;

            // Sort this block
            Arrays.sort(block);

            // Write it to file B
            fileB.writeAndFlush(block);

            // Now add this block to our list
            Block newBlock;
            if (blocks.isEmpty()) {
                newBlock = new Block(0, block.length * 4);
            } else {
                Block lastBlock = blocks.get(blocks.size() - 1);
                newBlock = new Block(lastBlock.position + lastBlock.size, block.length * 4);
            }
            blocks.add(newBlock);
        }

        /* Second: use k-way mergesort in a single pass */

        fileA.seek(0);
        List<FilePointer> filePointers = new ArrayList<>();
        for (Block b : blocks) {
            FilePointer fp = new FilePointer(f2);
            fp.seek(b.position);
            filePointers.add(fp);
        }

        // buffer contains one integer from each block (used for the merge)
        List<Integer> buffer = new ArrayList<>(blocks.size());
        for (FilePointer fp : filePointers) buffer.add(fp.read());

        while (filePointers.size() > 0) {
            // Get the smallest number
            int indexOfSmallest = 0;
            int smallest = Integer.MAX_VALUE;

            for (int i = 0; i < buffer.size(); i++) {
                if (buffer.get(i) <= smallest) {
                    smallest = buffer.get(i);
                    indexOfSmallest = i;
                }
            }

            fileA.write(smallest);

            FilePointer pointerWithSmallest = filePointers.get(indexOfSmallest);
            Block blockWithSmallest = blocks.get(indexOfSmallest);
            if (pointerWithSmallest.getPos() >= blockWithSmallest.position + blockWithSmallest.size) {
                // If we've already read all of this block then remove it
                filePointers.remove(indexOfSmallest);
                blocks.remove(indexOfSmallest);
                buffer.remove(indexOfSmallest);
            } else {
                buffer.set(indexOfSmallest, pointerWithSmallest.read());
            }
        }

        fileA.flush();
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
        System.out.println("The checksum is: "+checkSum(f1));
    }
}