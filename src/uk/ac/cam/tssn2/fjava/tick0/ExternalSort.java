package uk.ac.cam.tssn2.fjava.tick0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ExternalSort {
    private static final int INTS_PER_BLOCK = 2000;

    // Quicksort
    private static void quick(int[] array, int start, int end) {
        if (start >= end) return;

        // Select pivot
        int pivotIndex = new Random().nextInt(end - start) + start;
        int temp = array[start];
        array[start] = array[pivotIndex];
        array[pivotIndex] = temp;

        // Set up pointers
        int pivot = array[pivotIndex];
        int left = start - 1;
        int right = end + 1;

        // Partition
        while (true) {
            do left++; while (array[left] < pivot);
            do right--; while (array[right] > pivot);
            if (left >= right) break;
            int temp2 = array[left];
            array[left] = array[right];
            array[right] = temp2;
        }

        // Recurse
        quick(array, start, right);
        quick(array, right + 1, end);
    }

    // block1 MUST come before block2 in the file for this function to work
    public static void merge(FilePointer from1, FilePointer from2, FilePointer to, Block block1, Block block2) throws IOException {
        // Setup pointers
        from1.seek(block1.position);
        from2.seek(block2.position);
        to.seek(block1.position);

        long bytesWritten = 0;
        while (bytesWritten < block1.size + block2.size) {
            int block1Int = from1.read();
            int block2Int = from2.read();

            if (block1Int < block2Int) {
                to.write(block1Int);
                from2.rewind(4);
            } else {
                to.write(block2Int);
                from1.rewind(4);
            }

            bytesWritten += 4;
        }

        to.flush();
    }

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
            quick(block, 0, block.length - 1);

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

        /* Second: use mergesort in multiple passes to finish off the sorting */

        FilePointer secondaryFileA = new FilePointer(f1);
        FilePointer secondaryFileB = new FilePointer(f2);
        boolean copyingFromA = false;

        while (blocks.size() > 1) {
            // Do the merge
            for (int i = 0; i < blocks.size() - 1; i++) {
                merge(copyingFromA ? fileA : fileB, copyingFromA ? secondaryFileA : secondaryFileB, copyingFromA ? fileB : fileA, blocks.get(i), blocks.get(i + 1));
                blocks.get(i).size += blocks.get(i + 1).size;
                blocks.remove(i + 1);
            }

            // Switch which file we're reading from/to
            copyingFromA = !copyingFromA;
        }

        /* Finally: copy from B to A if we ended up with B */
        if (!copyingFromA) {
            fileB.seek(0);
            fileA.seek(0);

            while (true) {
                int[] block = fileB.read(INTS_PER_BLOCK);
                if (block == null) break;
                fileA.writeAndFlush(block);
            }
        }

        fileA.close();
        secondaryFileA.close();
        fileB.close();
        secondaryFileB.close();
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
