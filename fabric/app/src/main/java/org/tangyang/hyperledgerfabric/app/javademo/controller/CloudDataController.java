package org.tangyang.hyperledgerfabric.app.javademo.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.utils.GenerateHVT;
import org.tangyang.hyperledgerfabric.app.javademo.utils.SplitFileUtil;

import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cloudData")
@Slf4j
@AllArgsConstructor
public class CloudDataController {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;
    //对file,fileBlock,user,challenge的操作
    //批量上传HVT
    //实现生成proof，hvt^r次方
    //先上传challenge，csp拿到chal算出proof

    @GetMapping("/checkData")
    public Map<String,Object> checkHVT(@RequestParam("fileID") String fileID,
                                       @RequestParam("filePath") String filePath) throws GatewayException, NoSuchAlgorithmException, IOException {

        Map<String,Object> result = Maps.newConcurrentMap();

        List<BigInteger> localHvtList = Lists.newArrayList();
        List<String> gNCList = Lists.newArrayList();
        byte[] bytes = contract.evaluateTransaction("queryGNByFileID",fileID);
        String str = StringUtils.newStringUtf8(bytes);
        gNCList = JSON.parseArray(str,String.class);

        BigInteger g = new BigInteger(gNCList.get(0));
        BigInteger n = new BigInteger(gNCList.get(1));
        int count = Integer.parseInt(gNCList.get(2));

        localHvtList = GenerateHVT.generateTag(filePath,count,g,n);

        byte[] hvtBytes = contract.evaluateTransaction("queryHVTByFileID",fileID);
        List<String> networkHvtListStr = JSON.parseArray(StringUtils.newStringUtf8(hvtBytes),String.class);
        List<BigInteger> networkHvtList = networkHvtListStr.stream()
                .map(BigInteger::new)
                .collect(Collectors.toList());

        result.put("payload",networkHvtList.equals(localHvtList));
        result.put("status","ok");

        return result;
    }
    @GetMapping("/check")
    public Map<String,Object> check(@RequestParam("fileID") String fileID,
                                    @RequestParam("filePath") String filePath,
                                    @RequestParam("flag") String flag) throws GatewayException, NoSuchAlgorithmException, IOException {

        Map<String,Object> result = Maps.newConcurrentMap();

        List<BigInteger> localHvtList = Lists.newArrayList();

        byte[] hvtBytes = contract.evaluateTransaction("queryHVTByFileID",fileID);
        List<BigInteger> networkHvtList = JSON.parseArray(StringUtils.newStringUtf8(hvtBytes),BigInteger.class);

        BigInteger g = networkHvtList.get(0);
        BigInteger n = networkHvtList.get(1);
        int count = networkHvtList.size() -2;

        localHvtList = GenerateHVT.generateTag(filePath,count,g,n);
        if(networkHvtList.equals(localHvtList)){
            result.put("status","The data is corrupted.");
            if(flag.equals("删除数据")){

            }else {
                Map<String, Object> location = faultLocation(fileID, filePath);
            }
        }else {
            result.put("status","ok");
        }

        return result;
    }

    @GetMapping("/faultLocation")
    public Map<String,Object> faultLocation(@RequestParam("fileID") String fileID,
                                            @RequestParam("filePath") String filePath) throws GatewayException, NoSuchAlgorithmException, IOException {
        Map<String,Object> result = Maps.newConcurrentMap();

        List<BigInteger> localHvtList = Lists.newArrayList();
        List<String> gNCList = Lists.newArrayList();
        byte[] bytes = contract.evaluateTransaction("queryGNByFileID",fileID);
        String str = StringUtils.newStringUtf8(bytes);
        gNCList = JSON.parseArray(str,String.class);

        BigInteger g = new BigInteger(gNCList.get(0));
        BigInteger n = new BigInteger(gNCList.get(1));
        int count = Integer.parseInt(gNCList.get(2));

        localHvtList = GenerateHVT.generateTag(filePath,count,g,n);

        byte[] hvtBytes = contract.evaluateTransaction("queryHVTByFileID",fileID);
        List<BigInteger> networkHvtList = JSON.parseArray(StringUtils.newStringUtf8(hvtBytes),BigInteger.class);
        /*List<BigInteger> networkHvtList = networkHvtListStr.stream()
                .map(BigInteger::new)
                .collect(Collectors.toList());*/

        List<Integer> diffIndexList = Lists.newArrayList();
        /*List<Integer> diffIndexList = IntStream.range(0, Math.min(localHvtList.size(), networkHvtList.size()))
                .parallel()
                .filter(i -> {
                    if (!finalLocalHvtList.get(i).equals(networkHvtList.get(i))) {
                        index.incrementAndGet(); // 修改 index 变量的值
                        return true;
                    }
                    return false;
                })
                .boxed()
                .toList();*/
        for(int i = 0;i < networkHvtList.size();i++){
            if(!networkHvtList.get(i).equals(localHvtList.get(i))){
                diffIndexList.add(i);
            }
        }
        result.put("错误的数据块序列号:" , diffIndexList);
        result.put("错误块数:" , diffIndexList.size());

        return result;

    }

    @GetMapping("merge")
    public Map<String,Object> mergeBlock(@RequestParam("fileBlockPath") String fileBlockPath,
                                         @RequestParam("outPutPath") String outputPath,
                                         @RequestParam("count") int count){

        Map<String,Object> result = Maps.newConcurrentMap();

        SplitFileUtil.mergeFile(outputPath,fileBlockPath,count);

        result.put("status","ok");

        return result;
    }

    /*@GetMapping("/arbitration")
    public Map<String,Object> disputeArbitration(){

    }*/

}
