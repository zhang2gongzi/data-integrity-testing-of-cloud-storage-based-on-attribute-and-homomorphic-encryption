package org.tangyang.hyperledgerfabric.app.javademo.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GetHash {
    //传入字符串返回SHA256加密结果
    public static String getSHA256(final String strText){
        return SHA(strText,"SHA-256");
    }
    //传入字符串返回SHA512加密结果
    public static String getSHA512(final String strText){
        return SHA(strText,"SHA-512");
    }
    //传入字符串返回MD5加密结果
    public static String getMD5(final String strText){
        return SHA(strText,"SHA-512");
    }

    private static String SHA(final String strText,final String strType){
        String strResult = null;//加密结果
        //判断字符串是否有效
        if(strText != null && strText.length() > 0){
            try{
                // SHA 加密开始
                // 创建加密对象，传入加密类型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                //传入加密字符串
                messageDigest.update(strText.getBytes());
                //得到byte数组
                byte[] buffer = messageDigest.digest();
                //将byte转为String字符串
                StringBuffer hexString = new StringBuffer();
                for(int i = 0; i < buffer.length;i++){//遍历数组
                    //转换成16进制字符串放入hexString
                    //这Integer.toHexString(int i)接受一个int参数并返回一个十六进制字符串。关键是将转换byte为int并使用0xff进行掩码以防止符号扩展。
                    String hex = Integer.toHexString(0xff & buffer[i]);
                    //如果byte转的16进制字符长度为1，需要在前面加上0
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                strResult = hexString.toString();
            }
            catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }
        }
        return strResult;
    }
}
