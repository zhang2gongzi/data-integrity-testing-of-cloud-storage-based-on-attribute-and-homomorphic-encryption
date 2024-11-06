package org.tangyang.hyperledgerfabric.app.javademo.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.hyperledger.fabric.client.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.tangyang.hyperledgerfabric.app.javademo.DTO.User;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.CPABE;
import org.tangyang.hyperledgerfabric.app.javademo.utils.ConvertFiles;
import org.tangyang.hyperledgerfabric.app.javademo.utils.RSAUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class AAService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;

    public Map<String,Object> setup(int bitSize,int securityParameter,String pkPath, String mskPath,String pairingParametersPath,String key) throws EndorseException, SubmitException {
        Map<String, Object> result = Maps.newConcurrentMap();
        String params = CPABE.setup(pairingParametersPath, pkPath, mskPath,bitSize,securityParameter);
        contract.newProposal("createParams")
                .addArguments(key,params)
                .build()
                .endorse()
                .submitAsync();
        result.put("status","ok");

        return result;
    }

    public Map<String,Object> keyGen(String userAttr,String pkPath,String mskPath,String skPath,String pbPath,String uid) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        String[] userAttrList = userAttr.split(",");
        String pk = getUserByID(uid).getUserID();

        CPABE.keygenTest1(userAttrList,pkPath,mskPath,skPath,pk,pbPath);

        String userSK = ConvertFiles.convertBinaryToText(Path.of(skPath));

        String userSKEn = RSAUtils.encryptByPublicKey(pk, userSK);

        contract.newProposal("uploadUserSK")
                .addArguments(uid, userSKEn)
                .build()
                .endorse()
                .submitAsync();

        result.put("time costs",(System.currentTimeMillis() - start)+"ms");
        result.put("status","ok");

        return result;
    }

    public Map<String,Object> keyDDGen(String userAttr,String pkPath,String mskPath,String pbPath,String uid) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        String[] userAttrList = userAttr.split(",");
        List<String> keyList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();

        String pk = getUserByID(uid).getUserID();
        String t = CPABE.ddGen1(userAttrList,pkPath,mskPath,pk,pbPath,keyList,valueList);

        result.put("sk time costs",(System.currentTimeMillis() - start)+"ms");

        String encryKeyList = RSAUtils.encryptByPublicKey(pk,JSON.toJSONString(keyList));
        String encryValueList = RSAUtils.encryptByPublicKey(pk,JSON.toJSONString(valueList));

        contract.newProposal("uploadUserDD")
                .addArguments(uid, encryKeyList, encryValueList, t)
                .build()
                .endorse()
                .submitAsync();

        result.put("total time costs",(System.currentTimeMillis() - start)+"ms");
        result.put("status","ok");

        return result;
    }

    public Map<String,Object> keyDIGen(String userAttr,String pbPath,String uid) throws Exception {
        Map<String, Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        String[] userAttrList = userAttr.split(",");
        List<String> keyList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        User user = getUserByID(uid);
        String t = user.getElementT();
        //keyList.add("1");
        //valueList.add("1");
        CPABE.attSKGen1(t,userAttrList,user.getUserID(),pbPath,keyList,valueList);
        result.put("gensk time costs",(System.currentTimeMillis() - start)+"ms");
        String encryKeyList = RSAUtils.encryptByPublicKey(user.getUserID(),JSON.toJSONString(keyList));
        String encryValueList = RSAUtils.encryptByPublicKey(user.getUserID(),JSON.toJSONString(valueList));

        contract.newProposal("uploadUserDI")
                .addArguments(uid, encryKeyList, encryValueList)
                .build()
                .endorse()
                .submitAsync();

        result.put("time costs",(System.currentTimeMillis() - start)+"ms");
        result.put("status","ok");

        return result;
    }


    public User getUserByID(String uid) throws GatewayException {
        byte[] bytes = contract.evaluateTransaction("queryUserByKey", uid);

        return JSON.parseObject(StringUtils.newStringUtf8(bytes),User.class);
    }

}
