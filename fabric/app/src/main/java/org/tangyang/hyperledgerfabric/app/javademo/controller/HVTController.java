package org.tangyang.hyperledgerfabric.app.javademo.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.HVTFile;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GenerateHVT;
import org.tangyang.hyperledgerfabric.app.javademo.utils.SplitFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/HVT")
@Slf4j
@AllArgsConstructor
public class HVTController {//HVT操作
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    @PutMapping("/generateTag")//只有一个交易
    public Map<String,Object> generateHVT(@RequestParam("key") String key,
                                          @RequestParam("filesID") String filesID,
                                          @RequestParam("blockCount") String blockCount,
                                          @RequestParam("keySize") String keySize,
                                          @RequestParam("importPath") String importPath,
                                          @RequestParam("savePath") String savePath) throws NoSuchAlgorithmException, IOException, GatewayException, CommitException {

        long start = System.currentTimeMillis();
        Map<String,Object> result = Maps.newConcurrentMap();

        File importFile = new File(importPath);
        //文件分片
        SplitFileUtil.cut(importFile,importPath,Integer.parseInt(blockCount));
        //生成HVT
        BigInteger[] bigIntegers = GenerateHVT.generate_g_N();
        BigInteger g = bigIntegers[0];
        BigInteger n = bigIntegers[1];
        GenerateHVT.generateTag(g,n,importPath,savePath,Integer.parseInt(blockCount));
        //contract.submitTransaction("createFile", key,filesID,g.toString(),n.toString(),blockCount);
        contract.newProposal("createFile")
                .addArguments(key,filesID,g.toString(),n.toString(),blockCount)
                .build()
                .endorse()
                .submitAsync();

        result.put("timeCost:",System.currentTimeMillis() - start + "ms");
        result.put("status", "ok");

        return result;
    }

    @GetMapping("genGN")
    public List<BigInteger> genGN(String keySize) throws NoSuchAlgorithmException {
        List<BigInteger> resList = Lists.newArrayList();

        /*byte[] bytes = contract.evaluateTransaction("generate_g_N", keySize);
        List<String> stringList = JSON.parseArray(StringUtils.newStringUtf8(bytes),String.class);*/
        BigInteger[] bigIntegers = GenerateHVT.generate_g_N();

        //resList.add(new BigInteger(stringList.get(0)));
        //resList.add(new BigInteger(stringList.get(1)));
        resList.add(bigIntegers[0]);
        resList.add(bigIntegers[1]);

        return resList;
    }

    @PutMapping("/uploadHVT")//上传HVT,前两个是g,n
    public Map<String,Object> uploadHVT(@RequestParam("key") String key,
                                        @RequestParam("filesID") String filesID,
                                        @RequestParam("HVTPath") String HVTPath) throws IOException, ClassNotFoundException, EndorseException, SubmitException {
        Map<String,Object> result = Maps.newConcurrentMap();
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HVTPath));
        String hvtStr = (String) ois.readObject();
        ois.close();
        List<BigInteger> hvtList = JSON.parseArray(hvtStr,BigInteger.class);
        contract.newProposal("createFileBlock")
                .addArguments(key,filesID,JSON.toJSONString(hvtList))
                .build()
                .endorse()
                .submitAsync();

        result.put("status", "ok");

        return result;
    }

    @PutMapping("/generateTagTime")
    public long timeCost(@RequestParam("importPath") String importPath,
                         @RequestParam("savePath") String savePath,
                         @RequestParam("blockCount") String blockCount) throws NoSuchAlgorithmException, IOException, GatewayException {

        File importFile = new File(importPath);
        long start = System.currentTimeMillis();
        //文件分片
        SplitFileUtil.cut(importFile,importPath,Integer.parseInt(blockCount));
        //通过智能合约生成g,n
        byte[] gNbytes = contract.evaluateTransaction("generate_g_N","1024");
        List<String> gnListStr = JSON.parseArray(StringUtils.newStringUtf8(gNbytes),String.class);
        //生成HVT
        BigInteger g = new BigInteger(gnListStr.get(0));
        BigInteger n = new BigInteger(gnListStr.get(1));
        List<BigInteger> resultList = GenerateHVT.generateTag(g,n,importPath,savePath,Integer.parseInt(blockCount));
        return System.currentTimeMillis() - start;
    }

    @GetMapping("/queryFileByKey")
    public Map<String,Object> queryFileByKey(@RequestParam("key") String key) throws GatewayException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] file = contract.evaluateTransaction("queryFile", key);

        HVTFile hvtFile = JSON.parseObject(StringUtils.newStringUtf8(file),HVTFile.class);
        result.put("payload", StringUtils.newStringUtf8(file));
        result.put("chal",hvtFile.getN());
        result.put("status", "ok");

        return result;
    }

    @GetMapping("/queryFileByFilesID")
    public Map<String,Object> queryFileByFilesID(@RequestParam("fileID") String fileID) throws GatewayException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] file = contract.evaluateTransaction("queryFileByFileID", fileID);

        result.put("payload", StringUtils.newStringUtf8(file));
        result.put("status", "ok");

        return result;
    }

    @GetMapping("/queryHVTByKey")
    public Map<String,Object> queryHVTByKey(@RequestParam("key") String key) throws GatewayException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] hvt = contract.evaluateTransaction("queryFileBlock", key);

        result.put("payload", StringUtils.newStringUtf8(hvt));
        result.put("status", "ok");

        return result;
    }

    @GetMapping("/queryHVTByFilesID")
    public Map<String,Object> queryHVTByFilesID(@RequestParam("fileID") String fileID) throws GatewayException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] hvts = contract.evaluateTransaction("queryFileBlockByFileID", fileID);

        result.put("payload", StringUtils.newStringUtf8(hvts));
        result.put("status", "ok");

        return result;
    }


    @DeleteMapping("/deleteFileByKey")
    public Map<String,Object> deleteFileByKey(@RequestParam("key") String key) throws EndorseException, CommitException, SubmitException, CommitStatusException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] file = contract.submitTransaction("deleteFile",key);

        result.put("payload", StringUtils.newStringUtf8(file));
        result.put("status", "ok");

        return result;
    }

    @DeleteMapping("/deleteFileByFileID")
    public Map<String,Object> deleteFileByFileID(@RequestParam("fileID") String fileID) throws EndorseException, CommitException, SubmitException, CommitStatusException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] file = contract.submitTransaction("deleteFileByFilesID",fileID);

        result.put("payload", StringUtils.newStringUtf8(file));
        result.put("status", "ok");

        return result;
    }

    @DeleteMapping("deleteFileBlockByKey")
    public Map<String,Object> deleteFileBlockByKey(@RequestParam("key") String key) throws EndorseException, CommitException, SubmitException, CommitStatusException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] block = contract.submitTransaction("deleteFileBlock",key);

        result.put("payload", StringUtils.newStringUtf8(block));
        result.put("status", "ok");

        return result;
    }

    @DeleteMapping("/deleteFileBlockByFileID")
    public Map<String,Object> deleteFileBlockByFileID(@RequestParam("fileID") String fileID) throws EndorseException, CommitException, SubmitException, CommitStatusException {

        Map<String,Object> result = Maps.newConcurrentMap();
        byte[] blocks = contract.submitTransaction("deleteFileBlockByFileID",fileID);

        result.put("payload", StringUtils.newStringUtf8(blocks));
        result.put("status", "ok");

        return result;
    }

}
