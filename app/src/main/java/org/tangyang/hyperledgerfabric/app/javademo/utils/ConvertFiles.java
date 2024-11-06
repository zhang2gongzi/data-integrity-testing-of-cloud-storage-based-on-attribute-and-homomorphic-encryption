package org.tangyang.hyperledgerfabric.app.javademo.utils;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        Path path = Paths.get(filePath);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             FileInputStream fis = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }
    public static BigInteger readFileToByteArray1(String filePath,String pbPath) throws IOException {
        Path path = Paths.get(filePath);
        Pairing bp = PairingFactory.getPairing(pbPath);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             FileInputStream fis = new FileInputStream(path.toFile())) {
            byte[] buffer = new byte[8192]; // 8KB缓冲区
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bp.getZr().newElementFromBytes(bos.toByteArray()).getImmutable().toBigInteger();
        }
    }

    public static void writeByteArrayToFile(byte[] fileBytes, String filePath) throws IOException {
        Path path = Path.of(filePath);
        Files.write(path, fileBytes, StandardOpenOption.CREATE);
    }

    public static String readParams(String pbPath){
        try (BufferedReader reader = new BufferedReader(new FileReader(pbPath))) {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            System.out.println("读取文件成功！");
            System.out.println("文件内容:\n" + content.toString());
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void writeParams(String params,String pbPath){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pbPath))) {
            writer.write(params.toString()); // 将字符串写入文件
            System.out.println("写入文件成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
