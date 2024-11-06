package org.tangyang.hyperledgerfabric.app.javademo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class FileCreate {
    public static void main(String[] args) {
        //try {
             //generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\4kb\\4000kb.txt", 4 * 1000 * 1024);

            /*generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\50K_File.txt", 50 * 1024);   // 50K
            generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\100K_File.txt", 100 * 1024); // 100K
            generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\500K_File.txt", 500 * 1024); // 500K
            generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\1M_File.txt", 1024 * 1024);  // 1M
            generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\2M_File.txt", 2 * 1024 * 1024); // 2M
            generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\5M_File.txt", 5 * 1024 * 1024); // 5M
            generateFile("C:\\Users\\唐杨\\Desktop\\实验\\v1\\do\\data\\10M_File.txt", 10 * 1024 * 1024); // 10M*/
       /* } catch (IOException e) {
            e.printStackTrace();
        }*/
        String filePath = "C:/Users/唐杨/Desktop/实验/v1/do/data/10M_File.txt";
        long targetSizeKB = 10240;

        generateRandomFile(filePath, targetSizeKB);
    }

    private static void generateFile(String fileName, int fileSize) throws IOException {
        byte[] data = new byte[fileSize];
        new FileOutputStream(new File(fileName)).write(data);
        System.out.println(fileName + " generated successfully with size: " + fileSize + " bytes");
    }
    private static void generateRandomFile(String filePath, long targetSizeKB) {
        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            Random random = new Random();
            byte[] buffer = new byte[1024]; // 1KB buffer
            long currentSizeKB = 0;

            while (currentSizeKB < targetSizeKB) {
                random.nextBytes(buffer);
                fos.write(buffer);
                currentSizeKB++;
            }

            System.out.println("Random file generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
