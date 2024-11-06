package org.tangyang.hyperledgerfabric.app.javademo.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GenerateHVT;
import org.tangyang.hyperledgerfabric.app.javademo.utils.RSAUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class ChallengeService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    public Map<String, Object> challengeGen(String key, String initiator, String filesID, int start, int end, String importPath, String skPath) throws EndorseException, IOException, SubmitException {

        Map<String, Object> result = Maps.newConcurrentMap();
        String tagRHash = null;

        List<BigInteger> resultList = Lists.newArrayList();
        FileInputStream fis = new FileInputStream(importPath);
        byte[] bytes = fis.readAllBytes();
        fis.close();
        String resultStr = new String(bytes);

        resultList = JSON.parseArray(resultStr, BigInteger.class);
        BigInteger g = resultList.get(0);
        BigInteger n = resultList.get(1);
        String randN = System.currentTimeMillis() + "";
        BigInteger chal = g.modPow(new BigInteger(randN), n);

        if (start == -1 && end == -1) {
            contract.newProposal("uploadChal")
                    .addArguments(key, g.toString(), n.toString(), chal.toString(), String.valueOf(0), String.valueOf(resultList.size() - 2))
                    .build()
                    .endorse()
                    .submitAsync();

            tagRHash = GenerateHVT.generateTagR(resultList, n, new BigInteger(randN), 2, resultList.size());
        } else {
            String signature = RSAUtils.getSignature(initiator + System.currentTimeMillis(), skPath);
            contract.newProposal("createChallenge")
                    .addArguments(key, g.toString(), n.toString(), chal.toString(), initiator, signature, filesID, String.valueOf(start), String.valueOf(end))
                    .build()
                    .endorse()
                    .submitAsync();

            tagRHash = GenerateHVT.generateTagR(resultList, n, new BigInteger(randN), start + 2, end + 2);
        }

        result.put("hash(tagR mod N)", tagRHash);
        result.put("challengeNumber", chal.toString());
        result.put("N", n);
        result.put("status", "ok");

        return result;
    }

    public Map<String, Object> genChallengeByUser(String key,String skPath,String fileID,String name) throws Exception {

        Map<String, Object> result = Maps.newConcurrentMap();

        String signature = RSAUtils.getSignature(name + System.currentTimeMillis(),skPath);

        contract.newProposal("createChallengeByUser")
                .addArguments(key,name,fileID,signature)
                .build()
                .endorse()
                .submitAsync();

        result.put("status","ok");

        return result;

    }

    public Map<String, Object> queryChallenge(String param, String transaction) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();

        switch (transaction) {
            case "queryChallengeByKey":
                byte[] challenge = contract.evaluateTransaction("queryChallenge", param);
                result.put("payload", StringUtils.newStringUtf8(challenge));
                break;
            case "queryChallengeByfileID":
                challenge = contract.evaluateTransaction("queryChallengeByFileID", param);
                result.put("payload", StringUtils.newStringUtf8(challenge));
                break;
            case "queryChallengeBychal":
                challenge = contract.evaluateTransaction("queryChallengeByChal", param);
                result.put("payload", StringUtils.newStringUtf8(challenge));
                break;
            case "queryChallengeByInitiator":
                challenge = contract.evaluateTransaction("queryChallengeByInitiator", param);
                result.put("payload", StringUtils.newStringUtf8(challenge));
                break;
        }

        result.put("status", "ok");

        return result;
    }

}
