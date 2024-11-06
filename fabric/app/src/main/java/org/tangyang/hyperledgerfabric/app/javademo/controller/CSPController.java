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
import org.tangyang.hyperledgerfabric.app.javademo.utils.ConvertFiles;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GetHash;
import org.tangyang.hyperledgerfabric.app.javademo.utils.SplitFileUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/CSP")
@Slf4j
@AllArgsConstructor
public class CSPController {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;
    @PutMapping("/generate")
    public Map<String,Object> generate(@RequestParam("key") String key,
                                       @RequestParam("filePath") String filePath,
                                       @RequestParam("challengeKey") String challengeKey,
                                       @RequestParam("fileID") String fileID,
                                       @RequestParam("sender") String sender) throws NoSuchAlgorithmException, GatewayException {
        Map<String,Object> result = Maps.newConcurrentMap();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String data = null;
        BigInteger proof = new BigInteger("1");
        String namePath = SplitFileUtil.getStrBeforeBlockName(filePath);
        Challenge challenge = new Challenge();
        if(challengeKey.length() > 1){
            challenge = getChalByKey(challengeKey);
        }else {
            challenge = getChalByFileID(fileID);
        }

        BigInteger n = new BigInteger(challenge.getN());
        BigInteger chal = new BigInteger(challenge.getChallengeNumber());

        for(int i = challenge.getStart();i < challenge.getEnd();i++){
            data = ConvertFiles.convertBinaryToText(Path.of(namePath + "_block" + i + ".block"));
            byte[] bytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            BigInteger b = new BigInteger(1, bytes);
            proof = proof.multiply(chal.modPow(b,n)).mod(n);
        }

        contract.newProposal("createProof")
                .addArguments(key,challenge.getChallengeNumber(), proof.toString(), sender)
                .build()
                .endorse()
                .submitAsync();

        result.put("N",n);
        result.put("proof", GetHash.getSHA256(proof.toString()));
        result.put("status","ok");

        return result;
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
