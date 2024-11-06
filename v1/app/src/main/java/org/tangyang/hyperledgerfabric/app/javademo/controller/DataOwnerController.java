package org.tangyang.hyperledgerfabric.app.javademo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.service.ChallengeService;
import org.tangyang.hyperledgerfabric.app.javademo.service.DataOwnerService;
import org.tangyang.hyperledgerfabric.app.javademo.utils.RSAUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/DataOwner")//由数据持有者控制的controller，应包含：生成并上传HVT、发起挑战、申请仲裁、加密数据、上传用户私钥
@Slf4j
@AllArgsConstructor
public class DataOwnerController {

    final ChallengeService challengeService;

    final DataOwnerService dataOwnerService;

    @PostMapping("uploadChal")
    public Map<String,Object> uploadChal(@RequestParam("key") String key,
                                         @RequestParam("importPath") String importPath) throws GatewayException, IOException {

        return challengeService.challengeGen(key,"","",-1,-1,importPath,"");
    }

    /**
     *
     * @param key
     * @param name 挑战发起者
     * @param filesID 文件ID
     * @param start
     * @param end
     * @param importPath HVT存放路径
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
                                                @RequestParam("importPath") String importPath,
                                                @RequestParam("skPath") String skPath) throws EndorseException, SubmitException, IOException, ClassNotFoundException {

        return challengeService.challengeGen(key,name,filesID,start,end,importPath,skPath);
    }

    @PutMapping("/createDO")
    public Map<String,Object> createDO(@RequestParam("key") String key,
                                       @RequestParam("name") String name,
                                       @RequestParam("pkPath") String pkPath,
                                       @RequestParam("skPath") String skPath) throws GatewayException, NoSuchAlgorithmException {

        return dataOwnerService.create(key,name,pkPath,skPath);
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
