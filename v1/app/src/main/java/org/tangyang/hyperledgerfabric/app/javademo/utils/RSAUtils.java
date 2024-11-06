package org.tangyang.hyperledgerfabric.app.javademo.utils;

import com.google.common.collect.Lists;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

public class RSAUtils {

    public static PublicKey getPublicKeyFromString(String pkString) {
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );
        try {
            // 进行Base64解码
            byte[] publicKeyEncoded = java.util.Base64.getDecoder().decode(pkString);
            // 获取密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            // 获取公钥
            return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyEncoded));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean verify(String pkString, String data, String signatureData) {
        try {
            PublicKey publicKey = getPublicKeyFromString(pkString);
            // 获取签名对象
            Signature signature = Signature.getInstance("RSA");
            // 传入公钥
            signature.initVerify(publicKey);
            //传入原文
            signature.update(data.getBytes());
            // 校验签名
            return signature.verify(java.util.Base64.getDecoder().decode(signatureData));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static PrivateKey getPrivateKeyFromString(String privateKeyString) {
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );
        try {
            // 进行Base64解码
            byte[] privateKeyEncoded = java.util.Base64.getDecoder().decode(privateKeyString);
            // 获取密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            // 获取私钥
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyEncoded));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSignature(String data,String skPath) {
        try {
            String skStr = "";
            try (FileInputStream fis = new FileInputStream(skPath)) {
                byte[] bytes = fis.readAllBytes();
                skStr = new String(bytes);
                System.out.println("文件内容成功写入字符串");
            } catch (IOException e) {
                System.out.println("读取文件时发生错误：" + e.getMessage());
            }

            PrivateKey privateKey = getPrivateKeyFromString(skStr);
            // 获取签名对象
            Signature signature = Signature.getInstance("RSA");
            // 传入私钥

            signature.initSign(privateKey);
            // 传入原文
            signature.update(data.getBytes());
            // 签名
            byte[] sign = signature.sign();
            // 对签名数据进行Base64编码
            return java.util.Base64.getEncoder().encodeToString(sign);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decryptByPrivateKey(String privateKeyText, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedData = Base64.decodeBase64(text);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        int maxDecryptBlockSize = 128; // RSA 密钥长度为 1024 位时，对应的最大解密块大小为 128 字节

        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxDecryptBlockSize) {
                cache = cipher.doFinal(encryptedData, offSet, maxDecryptBlockSize);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxDecryptBlockSize;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();

        return new String(decryptedData, StandardCharsets.UTF_8);
    }
    public static String encryptByPublicKey(String publicKeyText, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] inputBytes = text.getBytes();
        int maxBlockSize = 117; // RSA 密钥长度限制为 1024 位，对应的最大加密块大小为 117 字节
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 分块加密
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while (inputBytes.length - offSet > 0) {
            if (inputBytes.length - offSet > maxBlockSize) {
                cache = cipher.doFinal(inputBytes, offSet, maxBlockSize);
            } else {
                cache = cipher.doFinal(inputBytes, offSet, inputBytes.length - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlockSize;
        }
        out.close();

        byte[] encryptedBytes = out.toByteArray();
        return Base64.encodeBase64String(encryptedBytes);
    }

    public static List<String> keyGen(String pkPath, String skPath) throws NoSuchAlgorithmException {
        List<String> keyList = Lists.newArrayList();

        RSAUtils.RSAKeyPair userKeyPair = RSAUtils.generateKeyPair();
        String userPK = userKeyPair.getPublicKey();
        String userSK = userKeyPair.getPrivateKey();

        keyList.add(userPK);
        keyList.add(userSK);

        try (FileOutputStream fos = new FileOutputStream(pkPath)) {
            byte[] bytes = userPK.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("RSA公钥已成功写入文件");
        } catch (IOException e) {
            System.out.println("RSA公钥写入文件时发生错误：" + e.getMessage());
        }
        try (FileOutputStream fos = new FileOutputStream(skPath)) {
            byte[] bytes = userSK.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("RSA私钥已成功写入文件");
        } catch (IOException e) {
            System.out.println("RSA私钥写入文件时发生错误：" + e.getMessage());
        }

        return keyList;
    }
    public static RSAKeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        String publicKeyString = Base64.encodeBase64String(rsaPublicKey.getEncoded());
        String privateKeyString = Base64.encodeBase64String(rsaPrivateKey.getEncoded());
        RSAKeyPair rsaKeyPair = new RSAKeyPair(publicKeyString, privateKeyString);
        return rsaKeyPair;
    }
    public static class RSAKeyPair {

        private String publicKey;
        private String privateKey;

        public RSAKeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

    }
}
