package org.tangyang.hyperledgerfabric.app.javademo.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.Challenge;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.FileBlock;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.Proof;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GenerateHVT;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GetHash;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class ProofService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    public Map<String, Object> getCompute(String pkey,String ckey,String r) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        byte[] proofBytes = contract.evaluateTransaction("queryProof", pkey);
        Proof proof = JSON.parseObject(StringUtils.newStringUtf8(proofBytes),Proof.class);

        byte[] chalBytes = contract.evaluateTransaction("queryChallenge", ckey);
        Challenge challenge = JSON.parseObject(StringUtils.newStringUtf8(chalBytes), Challenge.class);

        BigInteger sigma = new BigInteger(proof.getSigma());
        String hashProof = proof.getProof();
        //String aa = GetHash.getSHA256(String.valueOf(sigma.modPow(new BigInteger(r),new BigInteger(challenge.getN()))));
        if(GetHash.getSHA256(sigma.modPow(new BigInteger(r),new BigInteger(challenge.getN())).toString()).equals(hashProof)){
            System.out.println(GetHash.getSHA256(sigma.modPow(new BigInteger(r),new BigInteger(challenge.getN())).toString()));
            System.out.println(hashProof);
            System.out.println(sigma.toString());
            result.put("比对结果：","数据完整");
            result.put("比对耗时：",System.currentTimeMillis() - start);
        }else {
            byte[] bytes = contract.evaluateTransaction("arbitration", ckey, r);
            System.out.println(GetHash.getSHA256(sigma.modPow(new BigInteger(r),new BigInteger(challenge.getN())).toString()));
            System.out.println(hashProof);
            System.out.println(sigma.toString());
            result.put("仲裁结果", StringUtils.newStringUtf8(bytes));
            result.put("仲裁耗时：",System.currentTimeMillis() - start);
        }

        return result;
    }

    public Map<String,Object> generate(String key, String blockPath, String pbPath, String challengeKey, String fileID, String sender) throws GatewayException, IOException {
        Map<String,Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        Challenge challenge = new Challenge();
        if(challengeKey.length() > 1){
            challenge = getChalByKey(challengeKey);
        }else {
            challenge = getChalByFileID(fileID);
        }
        long getC = System.currentTimeMillis();

        BigInteger n = new BigInteger(challenge.getN());
        BigInteger chal = new BigInteger(challenge.getChallengeNumber());
        List<BigInteger> arr = JSON.parseArray(challenge.getRandP(),BigInteger.class);
        int size = queryFileBlockSizeByFileID(challenge.getFilessID());
        BigInteger proof = GenerateHVT.generateProofFromZr(blockPath,chal,n,size,arr,pbPath);
        String hashProof = GetHash.getSHA256(proof.toString());
        long proofT = System.currentTimeMillis();
        contract.newProposal("createProof")
                .addArguments(key,challenge.getChallengeNumber(),hashProof, sender)
                .build()
                .endorse()
                .submitAsync();
        long upP = System.currentTimeMillis();

        result.put("N",n);
        result.put("proof", hashProof);
        result.put("generate proof cost time:", proofT - getC);
        result.put("blockchain cost time:", upP - proofT + getC - start);
        result.put("status","ok");

        return result;
    }

    public Map<String,Object> generateMulti(String key, String blockPaths, String pbPath, String challengeKey, String sender) throws GatewayException, IOException {
        Map<String,Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        Challenge challenge = getChalByKey(challengeKey);
        BigInteger proof = new BigInteger("1");

        long getC = System.currentTimeMillis();
        String[] blockPathsArr = blockPaths.split(",");
        BigInteger n = new BigInteger(challenge.getN());
        BigInteger chal = new BigInteger(challenge.getChallengeNumber());
        List<String> fileRandP =  challenge.getFileRandP();
        for(int i = 0; i< blockPathsArr.length;i++)
        {
            List<BigInteger> arr = JSON.parseArray(fileRandP.get(i),BigInteger.class);
            BigInteger temp = GenerateHVT.generateProofFromZr(blockPathsArr[i],chal,n,arr.size(),arr,pbPath);
            proof = proof.multiply(temp).mod(n);
        }

        String hashProof = GetHash.getSHA256(proof.toString());
        long proofT = System.currentTimeMillis();
        contract.newProposal("createMultiProof")
                .addArguments(key,challenge.getChallengeNumber(),hashProof, sender)
                .build()
                .endorse()
                .submitAsync();
        long upP = System.currentTimeMillis();

        result.put("N",n);
        result.put("proof", hashProof);
        result.put("generate proof cost time:", proofT - getC);
        result.put("blockchain cost time:", upP - proofT + getC - start);
        result.put("status","ok");

        return result;
    }

    private int queryFileBlockSizeByFileID(String fid) throws GatewayException {
        return Integer.parseInt(StringUtils.newStringUtf8(contract.evaluateTransaction("queryFileBlockSizeByFileID", fid)));
    }

    private Challenge getChalByFileID(String FileID) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("queryChallengeByFileID",FileID);

        return JSON.parseObject(StringUtils.newStringUtf8(bytes),Challenge.class);
    }

    public Challenge getChalByKey(String key) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("queryChallenge",key);

        return JSON.parseObject(StringUtils.newStringUtf8(bytes),Challenge.class);
    }

    public Map<String,Object> getGNFromFID(String fid, String gnPath) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();

        byte[] tagsByte = contract.evaluateTransaction("queryFileBlockByFileID",fid);

        FileBlock fileBlock = JSON.parseObject(StringUtils.newStringUtf8(tagsByte),FileBlock.class);
        BigInteger[] arr = new BigInteger[2];
        arr[0] = fileBlock.getHvt().get(0);
        arr[1] = fileBlock.getHvt().get(1);
        GenerateHVT.saveGNToFile(arr,gnPath);
        result.put("g",arr[0]);
        result.put("n",arr[1]);
        result.put("status", "ok");

        return result;
    }

}
