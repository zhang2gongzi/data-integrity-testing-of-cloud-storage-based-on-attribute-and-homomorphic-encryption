package org.tangyang.hyperledgerfabric.app.javademo.utils;

import org.tangyang.hyperledgerfabric.app.javademo.cpabe.CPABE;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.List;

public class FileToByteArrayExample {
    public static byte[] readFileToByteArray(String filePath) throws IOException {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(new File(filePath));
            bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }
    public static void writeByteArrayToFile(byte[] fileBytes, String filePath) throws IOException {
        Path path = Path.of(filePath);
        Files.write(path, fileBytes, StandardOpenOption.CREATE);
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException, ClassNotFoundException {
        List<BigInteger> hvtList = GenerateHVT.readTagsFromFilePath("D:/org3/lab1/do/10tags/tags10.txt");
        System.out.println(hvtList.size());
        /*try {
            String filePath = "D:\\org3\\binaryTest\\VID_20210330_183412.mp4";
            byte[] fileBytes = readFileToByteArray(filePath);
            CPABE.kemEncryptByte(fileBytes,"E:\\data\\lab1\\cpabe\\config.json","E:\\data\\pk","D:\\org3\\binaryTest\\ctFileMp4");
        } catch (IOException | GeneralSecurityException e) {
            System.out.println("Error while reading file: " + e.getMessage());
        }*/
        //String data = ConvertFiles.convertBinaryToText(Path.of("D:\\org3\\binaryTest\\VID_20210330_183412.mp4"));
        //CPABE.kemEncrypt(data,"E:\\data\\lab1\\cpabe\\config.json","E:\\data\\pk","D:\\org3\\binaryTest\\ctFileStr");
        //String filePath = "D:\\org3\\binaryTest\\VID_20210330_183412.mp4";
        //byte[] fileBytes = readFileToByteArray(filePath);
        String filePath1 = "D:\\org3\\binaryTest\\byteMp4.mp4";
        byte[] bytes = CPABE.kemDecryptByte("D:\\org3\\binaryTest\\ctFileMp4", "D:\\org3\\sk");
        //String s = CPABE.kemDecrypt("D:\\org3\\binaryTest\\ctFileStr", "D:\\org3\\sk");
        //System.out.println(s.length());
        writeByteArrayToFile(bytes, filePath1);
        //System.out.println(Arrays.equals(bytes,fileBytes));
    }
}