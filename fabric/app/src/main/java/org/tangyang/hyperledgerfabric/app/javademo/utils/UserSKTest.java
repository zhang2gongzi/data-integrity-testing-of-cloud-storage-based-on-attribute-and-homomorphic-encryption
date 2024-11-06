package org.tangyang.hyperledgerfabric.app.javademo.utils;

import com.alibaba.fastjson.JSON;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserSKTest {
    public static void main(String[] args) throws Exception {
        String a = "b";

        switch (a){
            case "a":
                String b = "a";
                break;
            case "b":
                b = "b";
                System.out.println(b);
                break;
            case "c":
                b = "c";
                break;
        }
        /*String r = System.currentTimeMillis()+"";
        System.out.println(new BigInteger(r));*/
        /*String signature = RSAUtils.getSignature("123456", "D:\\org3\\lab1\\du\\sk");
        boolean verify = RSAUtils.verify("", "123456", signature);
        System.out.println(verify);*/
        //用户生成RSA公私钥对
        /*RSAUtils.RSAKeyPair keyPair= RSAUtils.generateKeyPair();
        String pk = keyPair.getPublicKey();
        String sk = keyPair.getPrivateKey();
        try (FileOutputStream fos = new FileOutputStream("E:\\data\\RSA\\pk")) {
            byte[] bytes = pk.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("字符串已成功写入文件");
        } catch (IOException e) {
            System.out.println("写入文件时发生错误：" + e.getMessage());
        }
        try (FileOutputStream fos = new FileOutputStream("E:\\data\\RSA\\sk")) {
            byte[] bytes = sk.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("字符串已成功写入文件");
        } catch (IOException e) {
            System.out.println("写入文件时发生错误：" + e.getMessage());
        }
        String decryptPK = "";
        try (FileInputStream fis = new FileInputStream("E:\\data\\RSA\\pk")) {
            byte[] bytes = fis.readAllBytes();
            decryptPK = new String(bytes);
            System.out.println("文件内容成功写入字符串");
        } catch (IOException e) {
            System.out.println("读取文件时发生错误：" + e.getMessage());
        }
        String decryptSK = "";
        try (FileInputStream fis = new FileInputStream("E:\\data\\RSA\\sk")) {
            byte[] bytes = fis.readAllBytes();
            decryptSK = new String(bytes);
            System.out.println("文件内容成功写入字符串");
        } catch (IOException e) {
            System.out.println("读取文件时发生错误：" + e.getMessage());
        }
        //DO根据用户公钥加密CPABE的用户私钥
        String cpabeSK = ConvertFiles.convertBinaryToText(Path.of("E:\\data\\sk_ac"));
        String encpabeSK = RSAUtils.encryptByPublicKey(decryptPK,cpabeSK);//经过网络
        //uploadSK
        //用户接收CPABE私钥并用RSA私钥解密
        String decpabeSK = RSAUtils.decryptByPrivateKey(decryptSK,encpabeSK);//经过网络
        ConvertFiles.convertTextToBinary(decpabeSK,Path.of("E:\\data\\firstSK"));
        //downloadSK*/
        /*List<String> stringList = new ArrayList<>();
        stringList.add("123");
        stringList.add("456");
        stringList.add("789");

        List<BigInteger> bigIntegerList = stringList.stream()
                .map(BigInteger::new)
                .collect(Collectors.toList());

        // 打印结果
        for (BigInteger bigInteger : bigIntegerList) {
            System.out.println(bigInteger);
        }*/
       /* List<BigInteger> localHvtList = new ArrayList<>();
        // 添加 localHvtList 的元素...
        String filePath = "E:\\data\\tag\\tags10000.txt";
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));

        String resStr = (String) ois.readObject();
        ois.close();
        localHvtList = JSON.parseArray(resStr,BigInteger.class);

        List<BigInteger> networkHvtList = new ArrayList<>();
        // 添加 networkHvtList 的元素...
        networkHvtList = JSON.parseArray(resStr,BigInteger.class);

        networkHvtList.remove(99);
        networkHvtList.add(99,new BigInteger("47564745675"));

        AtomicInteger index = new AtomicInteger(0); // 定义一个 AtomicInteger 对象

        List<BigInteger> finalLocalHvtList = localHvtList;
        List<BigInteger> finalNetworkHvtList = networkHvtList;
        List<Integer> diffIndexList = IntStream.range(0, Math.min(localHvtList.size(), networkHvtList.size()))
                .parallel()
                .filter(i -> {
                    if (!finalLocalHvtList.get(i).equals(finalNetworkHvtList.get(i))) {
                        index.incrementAndGet(); // 修改 index 变量的值
                        return true;
                    }
                    return false;
                })
                .boxed()
                .toList();

        System.out.println("两个 List 不相等的下标为: " + diffIndexList);
        System.out.println("index 的值为: " + index.get());*/
    }
}
