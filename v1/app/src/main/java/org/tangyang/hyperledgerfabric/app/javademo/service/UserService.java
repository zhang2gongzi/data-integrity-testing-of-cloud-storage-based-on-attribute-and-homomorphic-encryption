package org.tangyang.hyperledgerfabric.app.javademo.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.DataOwner;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.User;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.CPABE;
import org.tangyang.hyperledgerfabric.app.javademo.utils.ConvertFiles;
import org.tangyang.hyperledgerfabric.app.javademo.utils.RSAUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class UserService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    public Map<String, Object> createUser(String key,String pkPath, String skPath,String attrList) throws EndorseException, SubmitException, NoSuchAlgorithmException {
        Map<String, Object> result = Maps.newConcurrentMap();

        List<String> keyList = RSAUtils.keyGen(pkPath, skPath);
        String userPK = keyList.get(0);

        contract.newProposal("createUser")
                .addArguments(key,userPK, attrList)
                .build()
                .endorse()
                .submitAsync();

        result.put("status", "ok");

        return result;
    }

    public Map<String,Object> downloadSK(String param, String decryptSKPath, String skDownloadPath, String mode) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();
        String userSKstr = null;
        if(mode.equals("Key")){
            byte[] userSKByte = contract.evaluateTransaction("downloadUserSK",param);
            userSKstr = StringUtils.newStringUtf8(userSKByte);//私钥解密前的用户私钥
        }else {
            byte[] userSKByte = contract.evaluateTransaction("queryUserByName",param);

            User user = JSON.parseObject(StringUtils.newStringUtf8(userSKByte),User.class);

            userSKstr = user.getUserSK();//私钥解密前的用户私钥
        }

        String decryptSK = "";
        try (FileInputStream fis = new FileInputStream(decryptSKPath)) {
            byte[] bytes = fis.readAllBytes();
            decryptSK = new String(bytes);
            System.out.println("文件内容成功写入字符串");
        } catch (IOException e) {
            System.out.println("读取文件时发生错误：" + e.getMessage());
        }

        String userskde = RSAUtils.decryptByPrivateKey(decryptSK,userSKstr);

        ConvertFiles.convertTextToBinary(userskde, Path.of(skDownloadPath));
        /*try (BufferedWriter writer = new BufferedWriter(new FileWriter(skDownloadPath))) {
            writer.write(userskde); // 将字符串写入文件
            System.out.println("写入文件成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        result.put("payload", userSKstr);
        result.put("status","ok");

        return result;
    }

    /**
     * 根据用户公钥(userID)加密用户cpabe私钥
     * @param key
     * @param skPath 私钥存放地址
     * @param userAttr 用户属性
     * @param pkPath 系统公钥
     * @param mskPath 系统主密钥
     * @return
     * @throws Exception
     */
    public Map<String, Object> uploadUserSK(String key, String skPath, String userAttr, String pkPath,String mskPath) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();

        String userPK = getPK(key);
        String[] userAttrList = userAttr.split(",");
        CPABE.keygenTest(userAttrList,pkPath,mskPath,skPath,userPK);
        String userSK = ConvertFiles.convertBinaryToText(Path.of(skPath));

        String userSKEn = RSAUtils.encryptByPublicKey(userPK, userSK);

        contract.newProposal("uploadUserSK")
                .addArguments(key, userSKEn)
                .build()
                .endorse()
                .submitAsync();

        result.put("status","ok");

        return result;

    }

    public PublicKey getUserPKByName(String name) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("getUserIDByName", name);

        return RSAUtils.getPublicKeyFromString(StringUtils.newStringUtf8(bytes));
    }

    public String getPK(String key) throws GatewayException {
        return StringUtils.newStringUtf8(contract.evaluateTransaction("returnIDByKey",key));
    }

    public Map<String,Object> query(String param, String mode) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();

        byte[] userByte = contract.evaluateTransaction("queryUserBy" + mode,param);

        result.put("payload", StringUtils.newStringUtf8(userByte));
        result.put("status", "ok");

        return result;
    }

    public Map<String,Object> delete(String param, String mode) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();

        byte[] userByte = contract.evaluateTransaction("deleteUserBy" + mode,param);

        result.put("delete:", StringUtils.newStringUtf8(userByte));
        result.put("status", "ok");

        return result;
    }

}
