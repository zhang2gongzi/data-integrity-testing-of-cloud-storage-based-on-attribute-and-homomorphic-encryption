package org.tangyang.hyperledgerfabric.app.javademo.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Set;

public class ConvertFiles {
    /**
     * 将目标文件转为String输出
     * @param pathIn
     * @return
     * @throws IOException
     */
    public static String convertBinaryToText(Path pathIn) {
        try {
            byte[] fileBytes = Files.readAllBytes(pathIn);
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void convertTextToBinary(String ciphertext, Path pathOut) {
        try {
            byte[] fileBytes = Base64.getDecoder().decode(ciphertext);
            Files.write(pathOut, fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
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
}
