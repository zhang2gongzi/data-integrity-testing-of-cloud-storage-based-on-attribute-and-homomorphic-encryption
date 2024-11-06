package org.tangyang.hyperledgerfabric.app.javademo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.service.ChallengeService;
import org.tangyang.hyperledgerfabric.app.javademo.service.ProofService;
import org.tangyang.hyperledgerfabric.app.javademo.service.UserService;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@AllArgsConstructor
public class UserController {

    final ChallengeService challengeService;

    final UserService userService;

    final ProofService proofService;

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
                                          @RequestParam("name") String name,
                                          @RequestParam("pkPath") String pkPath,
                                          @RequestParam("skPath") String skPath,
                                          @RequestParam("attrList") String attrList) throws EndorseException, SubmitException, NoSuchAlgorithmException {

        return userService.createUser(key,name, pkPath, skPath, attrList);
    }

    @PutMapping("/initiatorChallenge")
    public Map<String,Object> initiateChallenge(@RequestParam("key") String key,
                                                @RequestParam("initiator") String name,
                                                @RequestParam("filesID") String filesID,
                                                @RequestParam("start") int start,
                                                @RequestParam("end") int end,
                                                @RequestParam("importPath") String importPath,
                                                @RequestParam("skPath") String skPath,
                                                @RequestParam("pbPath") String pbPath) throws GatewayException {

        return challengeService.challengeGen(key,name,filesID,start,end,importPath,skPath,pbPath);
    }
    @PutMapping("/initiateMultiChallenge")
    public Map<String, Object> initiateMultiChallenge(@RequestParam("key") String key,
                                                      @RequestParam("name") String name,
                                                      @RequestParam("filesID") String filesID,
                                                      @RequestParam("gnPath") String gnPath,
                                                      @RequestParam("pbPath") String pbPath) throws Exception {

        return challengeService.multiChallengeGen(key,name,filesID,gnPath,pbPath);
    }

    @GetMapping("/getGN")
    public  Map<String,Object> getGN(@RequestParam("fid") String fid,
                                     @RequestParam("savePath") String savePath) throws GatewayException {
        return userService.getGNFromFID(fid, savePath);
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

    @GetMapping("/downloadSK")
    public Map<String,Object> downloadSKByUserID(@RequestParam("uid") String uid,
                                                 @RequestParam("decryptSKPath") String decryptSKPath,//用户RSA私钥密钥路径
                                                 @RequestParam("skDownloadPath") String skDownloadPath) throws Exception {

        return userService.getUserSK(uid, decryptSKPath, skDownloadPath);
    }
    @PostMapping("/validCheck")
    public Map<String,Object> validCheck(@RequestParam("key") String key,
                                         @RequestParam("fid") String fid) throws GatewayException{

        return userService.validCheck(key,fid);
    }
    @GetMapping("/compute")
    public Map<String,Object> compute(@RequestParam("r") String r,
                                      @RequestParam("challengeKey") String challengeKey,
                                      @RequestParam("proofKey") String proofKey) throws GatewayException {

        return proofService.getCompute(proofKey,challengeKey,r);
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
