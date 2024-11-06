package org.tangyang.hyperledgerfabric.app.javademo.controller;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.GatewayException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.CPABE;
import org.tangyang.hyperledgerfabric.app.javademo.utils.ConvertFiles;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/cpabe")
@Slf4j
@AllArgsConstructor
public class CPABEController {


    @GetMapping("/setup")
    public Map<String,Object> setup(@RequestParam("bitSize") int bitSize,
                                    @RequestParam("securityParameter") int securityParameter,
                                    @RequestParam("pkPath") String pkPath,
                                    @RequestParam("mskPath") String mskPath,
                                    @RequestParam("pairingParametersPath") String pairingParametersPath){
        Map<String, Object> result = Maps.newConcurrentMap();
        CPABE.setup(pairingParametersPath, pkPath, mskPath,bitSize,securityParameter);

        result.put("status","ok");

        return result;
    }
    @GetMapping("cycleE")
    public Map<String,Object> test4(@RequestParam("cycle") int cycle,
                                    @RequestParam("dataPath") String dataPath,
                                    @RequestParam("policyPath") String policyPath,
                                    @RequestParam("pkPath") String pkPath,
                                    @RequestParam("ctFilePath") String ctFilePath,
                                    @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        Map<String,Object> result = Maps.newConcurrentMap();
        int i = 6;
        while(i <= 36){
            result.put("attr"+i,test1(cycle, dataPath, policyPath+i+"attr.json", pkPath, ctFilePath, pbPath));
            i+=2;
        }
        result.put("status","ok");

        return result;
    }
    @GetMapping("/encryptTest")
    public long test1(@RequestParam("cycle") int cycle,
                      @RequestParam("dataPath") String dataPath,
                      @RequestParam("policyPath") String policyPath,
                      @RequestParam("pkPath") String pkPath,
                      @RequestParam("ctFilePath") String ctFilePath,
                      @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        int i = 0;
        int genTagTime = 0;
        while(i < cycle){
            long temp = encryptTest(dataPath, policyPath, pkPath, ctFilePath, pbPath);
            genTagTime += temp;
            i++;
        }

        return genTagTime / cycle;
    }

    @GetMapping("/encr")
    public long encryptTest(@RequestParam("dataPath") String dataPath,
                                      @RequestParam("policyPath") String policyPath,
                                      @RequestParam("pkPath") String pkPath,
                                      @RequestParam("ctFilePath") String ctFilePath,
                                      @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        long start = System.currentTimeMillis();
        byte[] fileBytes = ConvertFiles.readFileToByteArray(dataPath);
        CPABE.kemEncryptByte(fileBytes, policyPath, pkPath, ctFilePath,pbPath);

        return System.currentTimeMillis() - start;
    }

    @GetMapping("/encrypt")
    public Map<String,Object> encrypt(@RequestParam("dataPath") String dataPath,
                                      @RequestParam("policyPath") String policyPath,
                                      @RequestParam("pkPath") String pkPath,
                                      @RequestParam("ctFilePath") String ctFilePath,
                                      @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        //String data = ConvertFiles.convertBinaryToText(Path.of(dataPath));
        //CPABE.kemEncrypt(data, policyPath, pkPath, ctFilePath);
        byte[] fileBytes = ConvertFiles.readFileToByteArray(dataPath);
        CPABE.kemEncryptByte(fileBytes, policyPath, pkPath, ctFilePath,pbPath);
        result.put("time costs",(System.currentTimeMillis() - start)+"ms");
        result.put("status","ok");

        return result;
    }
    @GetMapping("cycleD")
    public Map<String,Object> test3(@RequestParam("cycle") int cycle,
                                    @RequestParam("ctFilePath") String ctFilePath,
                                    @RequestParam("skPath") String skPath,
                                    @RequestParam("decryptPath") String decryptPath,
                                    @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        Map<String,Object> result = Maps.newConcurrentMap();
        int i = 6;
        while(i <= 36){
            result.put("attr"+i,test2(cycle, ctFilePath, skPath+i, decryptPath, pbPath));
            i+=2;
        }
        result.put("status","ok");

        return result;
    }


    @GetMapping("/decryptTest")
    public long test2(@RequestParam("cycle") int cycle,
                                    @RequestParam("ctFilePath") String ctFilePath,
                                    @RequestParam("skPath") String skPath,
                                    @RequestParam("decryptPath") String decryptPath,
                                    @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        int i = 0;
        int genTagTime = 0;
        while(i < cycle){
            long temp = decryptTest(ctFilePath, skPath, decryptPath, pbPath);
            genTagTime += temp;
            i++;
        }

        return genTagTime / cycle;
    }
    @GetMapping("/decr")
    public long decryptTest(@RequestParam("ctFilePath") String ctFilePath,
                            @RequestParam("skPath") String skPath,
                            @RequestParam("decryptPath") String decryptPath,
                            @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        long start = System.currentTimeMillis();
        byte[] fileBytes = CPABE.kemDecryptByte(ctFilePath, skPath,pbPath);
        ConvertFiles.writeByteArrayToFile(fileBytes,decryptPath);

        return System.currentTimeMillis() - start;
    }

    @GetMapping("/decrypt")
    public Map<String,Object> decrypt(@RequestParam("ctFilePath") String ctFilePath,
                                      @RequestParam("skPath") String skPath,
                                      @RequestParam("decryptPath") String decryptPath,
                                      @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();

        long start = System.currentTimeMillis();
        //String decrypt = CPABE.kemDecrypt(ctFilePath, skPath);
        //ConvertFiles.convertTextToBinary(decrypt, Path.of(decryptPath));
        byte[] fileBytes = CPABE.kemDecryptByte(ctFilePath, skPath,pbPath);
        ConvertFiles.writeByteArrayToFile(fileBytes,decryptPath);
        result.put("time costs",(System.currentTimeMillis() - start)+"ms");
        result.put("status","ok");

        return result;
    }

    @GetMapping("/generateUserSK")
    public Map<String,Object> keyGen(@RequestParam("userAttr") String userAttr,
                                     @RequestParam("pkPath") String pkPath,
                                     @RequestParam("mskPath") String mskPath,
                                     @RequestParam("skPath") String skPath,
                                     @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();
        String uid = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDiSKIT1K9kGeKVL6uXu3U9LXnpPN+4UoauF7cfhBbnwSptwIz42YK5cV/q8/NwlDE" +
                "2IAHoXI6gMcS2FkkiCmrS4LAlDFkF9nb+ruVT2YiOc/GmIOx62O6Jn5h0BdiTnlY3v0+XCN00kBmscBg8DsA+442wltYTuZBbnsIpms4CbQIDAQAB";
        long start = System.currentTimeMillis();
        String[] userAttrList = userAttr.split(",");
        CPABE.keygenTest1(userAttrList,pkPath,mskPath,skPath,uid,pbPath);

        result.put("time costs",(System.currentTimeMillis() - start)+"ms");
        result.put("status","ok");

        return result;
    }

    public long keyGen1(@RequestParam("userAttr") String[] userAttrList,
                                     @RequestParam("pkPath") String pkPath,
                                     @RequestParam("mskPath") String mskPath,
                                     @RequestParam("skPath") String skPath,
                                     @RequestParam("pbPath") String pbPath) throws GeneralSecurityException, IOException {

        String uid = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDiSKIT1K9kGeKVL6uXu3U9LXnpPN+4UoauF7cfhBbnwSptwIz42YK5cV/q8/NwlDE" +
                "2IAHoXI6gMcS2FkkiCmrS4LAlDFkF9nb+ruVT2YiOc/GmIOx62O6Jn5h0BdiTnlY3v0+XCN00kBmscBg8DsA+442wltYTuZBbnsIpms4CbQIDAQAB";
        long start = System.currentTimeMillis();
        CPABE.keygenTest1(userAttrList,pkPath,mskPath,skPath,uid,pbPath);


        return System.currentTimeMillis() - start;
    }
    @GetMapping("/cycleG")
    public Map<String,Object> keyGenCycle(@RequestParam("pkPath") String pkPath,
                                          @RequestParam("mskPath") String mskPath,
                                          @RequestParam("skPath") String skPath,
                                          @RequestParam("pbPath") String pbPath,
                                          @RequestParam("cycle") int count) throws GeneralSecurityException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();
        String str = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,一,二,三,四,五,六,7,八,九,十";
        String[] arr = str.split(",");
        int length = arr.length;
        int index = 0;
        for(int i = length;i>=6;i=i-2){
            String[] temp = Arrays.copyOfRange(arr,0,length - index);
            index+=2;
            long time = 0;
            for (int j = 0; j < count; j++) {
                time += keyGen1(temp,pkPath,mskPath,skPath,pbPath);
            }
            result.put(i+"attr",time/count);
        }

        result.put("status","ok");

        return result;
    }

}
