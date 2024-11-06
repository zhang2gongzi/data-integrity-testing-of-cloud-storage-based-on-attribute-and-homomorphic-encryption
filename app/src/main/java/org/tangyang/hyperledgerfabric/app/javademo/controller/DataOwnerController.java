package org.tangyang.hyperledgerfabric.app.javademo.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.Proof;
import org.tangyang.hyperledgerfabric.app.javademo.service.*;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GenerateHVT;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/DataOwner")//由数据持有者控制的controller，应包含：生成并上传HVT、发起挑战、申请仲裁、加密数据、上传用户私钥
@Slf4j
@AllArgsConstructor
public class DataOwnerController {
    final Contract contract;

    final ChallengeService challengeService;

    final DataOwnerService dataOwnerService;

    final ProofService proofService;

    final PolicyService policyService;

    final UserService userService;

    @GetMapping("/genGN")
    public Map<String,Object> genGN(@RequestParam("filePath") String filePath) throws NoSuchAlgorithmException {
        Map<String,Object> result = Maps.newConcurrentMap();

        BigInteger[] bigIntegers = GenerateHVT.generate_g_N();
        BigInteger g = bigIntegers[0];
        BigInteger n = bigIntegers[1];
        GenerateHVT.saveGNToFile(bigIntegers,filePath);

        result.put("g",g);
        result.put("N",n);
        result.put("status","ok");
        return result;
    }

    @PostMapping("uploadChal")
    public Map<String,Object> uploadChal(@RequestParam("key") String key,
                                         @RequestParam("importPath") String importPath,
                                         @RequestParam("pbPath") String pbPath) throws GatewayException, IOException {

        return challengeService.challengeGen(key,"","",-1,-1,importPath,"",pbPath);
    }

    /**
     *
     * @param key
     * @param name 挑战发起者
     * @param filesID 文件ID
     * @param start
     * @param end
     * @param gnPath HVT存放路径
     * @param skPath RSA私钥存放地址
     * @return
     * @throws EndorseException
     * @throws SubmitException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @PutMapping("/generate")
    public Map<String,Object> initiateChallenge(@RequestParam("key") String key,
                                                @RequestParam("initiator") String name,
                                                @RequestParam("filesID") String filesID,
                                                @RequestParam("start") int start,
                                                @RequestParam("end") int end,
                                                @RequestParam("importPath") String gnPath,
                                                @RequestParam("skPath") String skPath,
                                                @RequestParam("pbPath") String pbPath) throws GatewayException {

        return challengeService.challengeGen(key,name,filesID,start,end,gnPath,skPath,pbPath);
    }
    @PutMapping("/initiateMultiChallenge")
    public Map<String, Object> initiateMultiChallenge(@RequestParam("key") String key,
                                                       @RequestParam("name") String name,
                                                       @RequestParam("filesID") String filesID,
                                                       @RequestParam("gnPath") String gnPath,
                                                       @RequestParam("pbPath") String pbPath) throws Exception {

        return challengeService.multiChallengeGen(key,name,filesID,gnPath,pbPath);
    }

    @PostMapping("/validCheck")
    public Map<String,Object> validCheck(@RequestParam("key") String key,
                                         @RequestParam("fid") String fid) throws GatewayException{

        return userService.validCheck(key,fid);
    }

    @PostMapping("/changeUserAttr")
    public Map<String,Object> changeUserAttr(@RequestParam("key") String key,
                                         @RequestParam("attr") String attr) throws GatewayException{

        return userService.changeAttr(key,attr);
    }

    @PutMapping("/createDO")
    public Map<String,Object> createDO(@RequestParam("key") String key,
                                       @RequestParam("name") String name,
                                       @RequestParam("pkPath") String pkPath,
                                       @RequestParam("skPath") String skPath) throws GatewayException, NoSuchAlgorithmException {

        return dataOwnerService.create(key,name,pkPath,skPath);
    }

    @PutMapping("/uploadPolicy")
    public Map<String,Object> uploadPolicy(@RequestParam("policyPath") String policyPath,
                                           @RequestParam("fileKey") String fid,
                                           @RequestParam("flag") int flag) throws EndorseException, SubmitException, IOException {
        return policyService.uploadPolicy(policyPath, fid, flag);
    }

    @DeleteMapping("/deletePolicy")
    public Map<String,Object> deletePolicy(@RequestParam("fileKey") String fid) throws GatewayException {
        return policyService.deletePolicy(fid);
    }

    @GetMapping("/compute")
    public Map<String,Object> compute(@RequestParam("r") String r,
                                      @RequestParam("challengeKey") String challengeKey,
                                      @RequestParam("proofKey") String proofKey) throws GatewayException {

        return proofService.getCompute(proofKey,challengeKey,r);
    }
    @GetMapping("/queryProofByKey")
    public Map<String,Object> queryProofByKey(@RequestParam("key") String key) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();

        byte[] proofBytes = contract.evaluateTransaction("queryProof", key);
        Proof proof = JSON.parseObject(StringUtils.newStringUtf8(proofBytes),Proof.class);
        result.put("proof",proof);

        return result;
    }

    @GetMapping("/queryChallengeByKey")
    public Map<String,Object> queryChallengeByKey(@RequestParam("key") String key) throws GatewayException {

        return challengeService.queryChallenge(key,"queryChallengeByKey");
    }

    @GetMapping("/queryChallengeByFileID")
    public Map<String,Object> queryChallengeByfileID(@RequestParam("fileID") String fileID) throws GatewayException {

        return challengeService.queryChallenge(fileID,"queryChallengeByfileID");
    }

    @GetMapping("/queryChallengeBychal")
    public Map<String,Object> queryChallengeBychal(@RequestParam("chal") String chal) throws GatewayException {

        return challengeService.queryChallenge(chal,"queryChallengeBychal");
    }

    @GetMapping("/queryChallengeByInitiator")
    public Map<String,Object> queryChallengeByInitiator(@RequestParam("initiator") String initiator) throws GatewayException {

        return challengeService.queryChallenge(initiator,"queryChallengeByInitiator");
    }

    @GetMapping("queryDOByKey")
    public Map<String,Object> queryDOByKey(@RequestParam("key") String key) throws GatewayException {

        return dataOwnerService.query(key,"Key");
    }

    @GetMapping("queryDOByName")
    public Map<String,Object> queryDOByName(@RequestParam("name") String name) throws GatewayException {

        return dataOwnerService.query(name,"Name");
    }

    @DeleteMapping("deleteDOByKey")
    public Map<String,Object> deleteDOByKey(@RequestParam("key") String key) throws GatewayException {

        return dataOwnerService.delete(key,"Key");
    }

    @DeleteMapping("deleteDOByName")
    public Map<String,Object> deleteDOByName(@RequestParam("name") String name) throws GatewayException {

        return dataOwnerService.delete(name,"Name");
    }

}
