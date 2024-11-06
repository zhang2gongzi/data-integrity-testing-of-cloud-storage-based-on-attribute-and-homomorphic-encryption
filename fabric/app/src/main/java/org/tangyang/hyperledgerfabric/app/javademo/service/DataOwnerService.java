package org.tangyang.hyperledgerfabric.app.javademo.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.stereotype.Service;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.DataOwner;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.utils.RSAUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class DataOwnerService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    public Map<String, Object> create(String key, String name, String pkPath, String skPath) throws NoSuchAlgorithmException, EndorseException, SubmitException {//生成RSA公私钥对
        Map<String, Object> result = Maps.newConcurrentMap();
        List<String> keyList = RSAUtils.keyGen(pkPath, skPath);
        String userPK = keyList.get(0);

        contract.newProposal("createDataOwner")
                .addArguments(key,name,userPK)
                .build()
                .endorse()
                .submitAsync();

        result.put("status", "ok");

        return result;
    }

    public DataOwner getByName(String name) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("queryDOByName", name);

        return JSON.parseObject(StringUtils.newStringUtf8(bytes),DataOwner.class);
    }

    public DataOwner getByKey(String key) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("queryDOByKey", key);

        return JSON.parseObject(StringUtils.newStringUtf8(bytes),DataOwner.class);
    }

    public Map<String, Object> delete(String param,String mode) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] bytes = contract.evaluateTransaction("deleteDOBy"+mode, param);

        result.put("delete:",StringUtils.newStringUtf8(bytes));
        result.put("status", "ok");

        return result;
    }

    public Map<String, Object> query(String param,String mode) throws GatewayException {
        Map<String, Object> result = Maps.newConcurrentMap();
        byte[] bytes = contract.evaluateTransaction("queryDOBy"+mode, param);

        result.put("DataOwner:",StringUtils.newStringUtf8(bytes));
        result.put("status", "ok");

        return result;
    }
}
