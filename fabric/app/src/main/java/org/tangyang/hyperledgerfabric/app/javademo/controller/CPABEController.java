package org.tangyang.hyperledgerfabric.app.javademo.controller;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.CPABE;
import org.tangyang.hyperledgerfabric.app.javademo.utils.ConvertFiles;

import java.io.IOException;
import java.security.GeneralSecurityException;
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

    @GetMapping("/encrypt")
    public Map<String,Object> encrypt(@RequestParam("dataPath") String dataPath,
                                      @RequestParam("policyPath") String policyPath,
                                      @RequestParam("pkPath") String pkPath,
                                      @RequestParam("ctFilePath") String ctFilePath) throws GeneralSecurityException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();
        long start = System.currentTimeMillis();
        //String data = ConvertFiles.convertBinaryToText(Path.of(dataPath));
        //CPABE.kemEncrypt(data, policyPath, pkPath, ctFilePath);
        byte[] fileBytes = ConvertFiles.readFileToByteArray(dataPath);
        CPABE.kemEncryptByte(fileBytes, policyPath, pkPath, ctFilePath);
        result.put("time costs",(System.currentTimeMillis() - start)+"ms");
        result.put("status","ok");

        return result;
    }

    @GetMapping("/decrypt")
    public Map<String,Object> decrypt(@RequestParam("ctFilePath") String ctFilePath,
                                      @RequestParam("skPath") String skPath,
                                      @RequestParam("decryptPath") String decryptPath) throws GeneralSecurityException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();

        //String decrypt = CPABE.kemDecrypt(ctFilePath, skPath);
        //ConvertFiles.convertTextToBinary(decrypt, Path.of(decryptPath));
        byte[] fileBytes = CPABE.kemDecryptByte(ctFilePath, skPath);
        ConvertFiles.writeByteArrayToFile(fileBytes,decryptPath);

        result.put("status","ok");

        return result;
    }

    @GetMapping("/generateUserSK")
    public Map<String,Object> keyGen(@RequestParam("userAttr") String userAttr,
                                     @RequestParam("pkPath") String pkPath,
                                     @RequestParam("mskPath") String mskPath,
                                     @RequestParam("skPath") String skPath) throws GeneralSecurityException, IOException {
        Map<String, Object> result = Maps.newConcurrentMap();
        String[] userAttrList = userAttr.split(",");
        CPABE.keygen(userAttrList,pkPath,mskPath,skPath);

        result.put("status","ok");

        return result;
    }

}
