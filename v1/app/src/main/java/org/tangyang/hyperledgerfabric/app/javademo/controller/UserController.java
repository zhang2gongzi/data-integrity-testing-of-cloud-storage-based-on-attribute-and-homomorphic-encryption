package org.tangyang.hyperledgerfabric.app.javademo.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.bouncycastle.asn1.cmp.Challenge;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.User;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.service.ChallengeService;
import org.tangyang.hyperledgerfabric.app.javademo.service.UserService;
import org.tangyang.hyperledgerfabric.app.javademo.utils.ConvertFiles;
import org.tangyang.hyperledgerfabric.app.javademo.utils.RSAUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@AllArgsConstructor
public class UserController {

    final ChallengeService challengeService;

    final UserService userService;

    /**
     * 用户生成用于加解密cpabe私钥的RSA密钥对
     * @param key
     * @param pkPath 公钥存储路径
     * @param skPath 私钥存储路径
     * @param attrList 属性列表
     * @return
     * @throws EndorseException
     * @throws SubmitException
     * @throws NoSuchAlgorithmException
     */
    @PutMapping("/create")//用户生成公钥与私钥并保存至本地目录下
    public Map<String, Object> createUser(@RequestParam("key") String key,
                                          @RequestParam("pkPath") String pkPath,
                                          @RequestParam("skPath") String skPath,
                                          @RequestParam("attrList") String attrList) throws EndorseException, SubmitException, NoSuchAlgorithmException {

        return userService.createUser(key, pkPath, skPath, attrList);
    }
    @PutMapping("/initiatorChallenge")
    public Map<String, Object> initiatorChallenge(@RequestParam("key") String key,
                                                  @RequestParam("skPath") String skPath,
                                                  @RequestParam("fileID") String fileID,
                                                  @RequestParam("name") String name) throws Exception {

        return challengeService.genChallengeByUser(key, skPath, fileID, name);
    }

    @PostMapping("/uploadSK")//AA上传用户私钥并保存在本地
    public Map<String, Object> uploadUserSK(@RequestParam("key") String key,
                                            @RequestParam("skPath") String skPath,
                                            @RequestParam("userAttr") String userAttr,
                                            @RequestParam("pkPath") String pkPath,
                                            @RequestParam("mskPath") String mskPath) throws Exception {

        return userService.uploadUserSK(key, skPath, userAttr, pkPath,mskPath);
    }

    /**
     *
     * @param key
     * @param decryptSKPath 解密密钥路径
     * @param skDownloadPath CPABE私钥下载路径
     * @return
     * @throws Exception
     */
    @GetMapping("/downloadSKByKey")
    public Map<String,Object> downloadSKByKey(@RequestParam("key") String key,
                                              @RequestParam("decryptSKPath") String decryptSKPath,//解密密钥路径
                                              @RequestParam("skDownloadPath") String skDownloadPath) throws Exception {

        return userService.downloadSK(key,decryptSKPath,skDownloadPath,"Key");
    }

    @GetMapping("/downloadSKByName")
    public Map<String,Object> downloadSKByUserID(@RequestParam("name") String name,
                                                 @RequestParam("decryptSKPath") String decryptSKPath,//解密密钥路径
                                                 @RequestParam("skDownloadPath") String skDownloadPath) throws Exception {

        return userService.downloadSK(name,decryptSKPath,skDownloadPath,"Name");
    }

    @GetMapping("/queryByKey")
    public Map<String,Object> queryByKey(@RequestParam("key") String key) throws GatewayException {

        return userService.query(key,"Key");
    }

    @GetMapping("/queryByName")
    public Map<String,Object> queryByName(@RequestParam("name") String name) throws GatewayException {

        return userService.query(name,"Name");
    }

    @GetMapping("/queryByID")
    public Map<String,Object> queryByID(@RequestParam("pkPath") String pkPath) throws GatewayException {
        String pk = null;
        try (FileInputStream fis = new FileInputStream(pkPath)) {
            byte[] bytes = fis.readAllBytes();
            pk = new String(bytes);
            System.out.println("RSA公钥成功写入字符串");
        } catch (IOException e) {
            System.out.println("读取RSA公钥时发生错误：" + e.getMessage());
        }
        return userService.query(pk,"ID");
    }

    @DeleteMapping("/deleteByKey")
    public Map<String,Object> deleteByKey(@RequestParam("key") String key) throws GatewayException {

        return userService.delete(key,"Key");
    }

    @DeleteMapping("/deleteByName")
    public Map<String,Object> deleteByName(@RequestParam("name") String name) throws GatewayException {

        return userService.delete(name,"Name");
    }
}
