package org.tangyang.hyperledgerfabric.app.javademo.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.stereotype.Service;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GenerateHVT;
import org.tangyang.hyperledgerfabric.app.javademo.utils.RSAUtils;

import java.math.BigInteger;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class ChallengeService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    public Map<String, Object> challengeGen(String key, String initiator, String filesID, int start, int end, String gnPath, String skPath,String pbPath) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();
        Pairing bp = PairingFactory.getPairing(pbPath);
        BigInteger[] resultList = GenerateHVT.loadGNFromFile(gnPath);
        assert resultList != null;
        BigInteger g = resultList[0];
        BigInteger n = resultList[1];
        BigInteger randN = bp.getZr().newRandomElement().getImmutable().toBigInteger();
        BigInteger chal = g.modPow(randN, n);
        int size = queryFileBlockSizeByFileID(filesID);
        //List<BigInteger> randP = randP(end - start);
        BigInteger sigma = new BigInteger("0");

        if (start == -1 && end == -1) {
            contract.newProposal("uploadChal")
                    .addArguments(key, g.toString(), n.toString(), chal.toString(),String.valueOf(0), String.valueOf(size))
                    .build()
                    .endorse()
                    .submitAsync();

            //tagRHash = GenerateHVT.generateTagR(resultList, n, randN, 2, resultList.size(),randP);
            //sigma = GenerateHVT.generateSigma(resultList,n,randN,randP);
        } else {
            String signature = RSAUtils.getSignature(initiator + System.currentTimeMillis(), skPath);
            contract.newProposal("createChallenge")
                    .addArguments(key, g.toString(), n.toString(), chal.toString(), initiator, signature,filesID,String.valueOf(start), String.valueOf(end))//, JSON.toJSONString(randP)
                    .build()
                    .endorse()
                    .submitAsync();

            //tagRHash = GenerateHVT.generateTagR(resultList, n, randN, start + 2, end + 2,randP);
            //sigma = GenerateHVT.generateSigma(resultList,n,randN,randP);
        }

        //result.put("hash(tagR mod N)", tagRHash);
        result.put("challengeNumber", chal.toString());
        result.put("randN", randN);
        result.put("N", n);
        result.put("sigma", sigma);
        result.put("status", "ok");

        return result;
    }

    public Map<String, Object> multiChallengeGen(String key, String initiator, String filesID, String gnPath,String pbPath) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();
        Pairing bp = PairingFactory.getPairing(pbPath);
        BigInteger[] resultList = GenerateHVT.loadGNFromFile(gnPath);
        assert resultList != null;
        BigInteger g = resultList[0];
        BigInteger n = resultList[1];
        BigInteger randN = bp.getZr().newRandomElement().getImmutable().toBigInteger();
        BigInteger chal = g.modPow(randN, n);
        String[] arr = filesID.split(",");
        List<String> fileIDS = new ArrayList<>();
        //List<String> randPs = new ArrayList<>();
        Collections.addAll(fileIDS, arr);
        /*for (String fileID : fileIDS) {
            int size = queryFileBlockSizeByFileID(fileID);
            List<BigInteger> temp = randP(size);
            randPs.add(JSON.toJSONString(temp));
        }*/

        contract.newProposal("createMultiChallenge")
                .addArguments(key, g.toString(), n.toString(), chal.toString(), initiator, JSON.toJSONString(fileIDS))//, JSON.toJSONString(randPs)
                .build()
                .endorse()
                .submitAsync();

        //tagRHash = GenerateHVT.generateTagR(resultList, n, randN, start + 2, end + 2,randP);
        //sigma = GenerateHVT.generateSigma(resultList,n,randN,randP);


        //result.put("hash(tagR mod N)", tagRHash);
        result.put("challengeNumber", chal.toString());
        result.put("randN", randN);
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
    private int queryFileBlockSizeByFileID(String fid) throws GatewayException {
        return Integer.parseInt(StringUtils.newStringUtf8(contract.evaluateTransaction("queryFileBlockSizeByFileID", fid)));
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
