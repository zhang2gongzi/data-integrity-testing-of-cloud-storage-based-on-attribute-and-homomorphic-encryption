package org.tangyang.hyperledgerfabric.app.javademo.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.stereotype.Service;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.CPABE;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.TreeNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class PolicyService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    public Map<String,Object> uploadPolicy(String policyPath, String fid, int flag) throws IOException, EndorseException, SubmitException {

        Map<String, Object> result = Maps.newConcurrentMap();

        File file = new File(policyPath);
        String accessTreeString = FileUtils.readFileToString(file,"UTF-8");
        Map<String, TreeNode> accessTree = CPABE.jsonStringToAccessTree(accessTreeString);
        List<String> indexList = new ArrayList<>();
        List<String> nodesList = new ArrayList<>();
        CPABE.accessTreeToStringList(accessTree,indexList,nodesList);
        String method;
        if(flag == 1){
            method = "createPolicy";
        }else {
            method = "changePolicy";
        }
        contract.newProposal(method)
                .addArguments(fid, JSON.toJSONString(indexList),JSON.toJSONString(nodesList))
                .build()
                .endorse()
                .submitAsync();
        result.put("status","ok");

        return result;
    }
    public Map<String,Object> deletePolicy(String fid) throws GatewayException {

        Map<String, Object> result = Maps.newConcurrentMap();


        byte[] bytes = contract.evaluateTransaction("deletePolicy", fid);

        result.put("policy",StringUtils.newStringUtf8(bytes));
        result.put("status","ok");

        return result;
    }
}
