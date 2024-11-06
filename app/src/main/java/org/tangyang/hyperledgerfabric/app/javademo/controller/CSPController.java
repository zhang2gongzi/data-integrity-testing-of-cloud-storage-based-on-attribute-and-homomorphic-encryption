package org.tangyang.hyperledgerfabric.app.javademo.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.Challenge;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.service.ProofService;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GenerateHVT;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GetHash;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/CSP")
@Slf4j
@AllArgsConstructor
public class CSPController {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    final ProofService proofService;
    @PutMapping("/test")
    public Map<String,Object> test(@RequestParam("ckey") String ckey,
                                   @RequestParam("pey") String pkey,
                                   @RequestParam("count") int count){
        Map<String,Object> result = Maps.newConcurrentMap();
        int i = 0;
        int genTagTime = 0;
        int upTagTime = 0;
        while(i < count){
            try {
                long[] temp = generate1("C:/Users/唐杨/Desktop/实验/v1/do/data/4kb/4000kb_block0.block"
                        ,"C:/Users/唐杨/Desktop/实验/v1/pairing160_128.properties",ckey,"csp-1",pkey+i);
                genTagTime += temp[0];
                upTagTime += temp[1];
                i++;
            } catch (GatewayException | IOException e) {
                e.printStackTrace();
            }
        }
        result.put("generating proof timeCost:",genTagTime / count + "ms");
        result.put("upload proof timeCost:",upTagTime / count + "ms");
        result.put("i:",i);
        result.put("status",  "ok");

        return result;
    }
    @PutMapping("/generate1")
    public long[] generate1(@RequestParam("blockPath") String blockPath,
                            @RequestParam("pbPath") String pbPath,
                            @RequestParam("challengeKey") String challengeKey,
                            @RequestParam("sender") String sender,
                            @RequestParam("pkey") String pkey) throws GatewayException, IOException {
        long start = System.currentTimeMillis();
        Challenge challenge = new Challenge();
        challenge = getChalByKey(challengeKey);

        long getC = System.currentTimeMillis();

        BigInteger n = new BigInteger(challenge.getN());
        BigInteger chal = new BigInteger(challenge.getChallengeNumber());
        List<BigInteger> arr = JSON.parseArray(challenge.getRandP(),BigInteger.class);
        BigInteger proof = GenerateHVT.generateProofFromZr(blockPath,chal,n,challenge.getEnd()-challenge.getStart(),arr,pbPath);
        String hashProof = GetHash.getSHA256(proof.toString());
        long proofT = System.currentTimeMillis();
        contract.newProposal("createProof")
                .addArguments(pkey,challenge.getChallengeNumber(),hashProof, sender)
                .build()
                .endorse()
                .submitAsync();
        long upP = System.currentTimeMillis();

        return new long[]{proofT - getC, upP - proofT + getC - start};
    }
    @PutMapping("/generate")
    public Map<String,Object> generate(@RequestParam("key") String key,
                                       @RequestParam("blockPath") String blockPath,
                                       @RequestParam("pbPath") String pbPath,
                                       @RequestParam("challengeKey") String challengeKey,
                                       @RequestParam("fileID") String fileID,
                                       @RequestParam("sender") String sender) throws GatewayException, IOException {

        return proofService.generate(key, blockPath, pbPath, challengeKey, fileID, sender);
    }

    @PutMapping("/generateMultiProof")
    public Map<String,Object> generateMultiProof(@RequestParam("key") String key,
                                       @RequestParam("blockPaths") String blockPaths,
                                       @RequestParam("pbPath") String pbPath,
                                       @RequestParam("challengeKey") String challengeKey,
                                       @RequestParam("sender") String sender) throws GatewayException, IOException {


        return proofService.generateMulti(key, blockPaths, pbPath, challengeKey, sender);
    }

    @GetMapping("/getGN")
    public  Map<String,Object> getGN(@RequestParam("fid") String fid,
                                     @RequestParam("savePath") String savePath) throws GatewayException {
        return proofService.getGNFromFID(fid, savePath);
    }

    @GetMapping("/getChalByKey")
    public Challenge getChalByKey(@RequestParam("challengeKey") String key) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("queryChallenge",key);

        return JSON.parseObject(StringUtils.newStringUtf8(bytes),Challenge.class);
    }
    @GetMapping("/getChalByFileID")
    public Challenge getChalByFileID(@RequestParam("FileID") String FileID) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("queryChallengeByFileID",FileID);

        return JSON.parseObject(StringUtils.newStringUtf8(bytes),Challenge.class);
    }

    @GetMapping("/queryByKey")
    public Map<String,Object> queryByKey(@RequestParam("key") String key) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] proof = contract.evaluateTransaction("queryProof", key);

        result.put("payload", StringUtils.newStringUtf8(proof));
        result.put("status", "ok");

        return result;
    }

    @GetMapping("/queryByChal")
    public Map<String,Object> queryByChal(@RequestParam("chal") String chal) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] proof = contract.evaluateTransaction("queryProofByChal", chal);

        result.put("payload", StringUtils.newStringUtf8(proof));
        result.put("status", "ok");

        return result;
    }

    @GetMapping("/queryBySender")
    public Map<String,Object> queryBySender(@RequestParam("sender") String sender) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] proof = contract.evaluateTransaction("queryProofBySender", sender);

        result.put("payload", StringUtils.newStringUtf8(proof));
        result.put("status", "ok");

        return result;
    }
}
