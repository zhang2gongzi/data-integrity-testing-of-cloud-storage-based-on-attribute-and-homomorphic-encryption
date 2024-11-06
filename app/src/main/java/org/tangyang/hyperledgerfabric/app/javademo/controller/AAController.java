package org.tangyang.hyperledgerfabric.app.javademo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.SubmitException;
import org.springframework.web.bind.annotation.*;
import org.tangyang.hyperledgerfabric.app.javademo.service.AAService;

import java.util.Map;

@RestController
@RequestMapping("/aaone")
@Slf4j
@AllArgsConstructor
public class AAController {
    final AAService aaService;
//put和post是反的
    @PutMapping("/setup")
    public Map<String,Object> setup(@RequestParam("bitSize") int bitSize,
                                    @RequestParam("securityParameter") int securityParameter,
                                    @RequestParam("pkPath") String pkPath,
                                    @RequestParam("mskPath") String mskPath,
                                    @RequestParam("pairingParametersPath") String pairingParametersPath,
                                    @RequestParam("key") String key) throws EndorseException, SubmitException {

        return aaService.setup(bitSize,securityParameter,pkPath,mskPath,pairingParametersPath,key);
    }


    @PostMapping("/aa")//整个属性私钥
    public Map<String, Object> uploadUser(@RequestParam("uid") String uid,
                                            @RequestParam("userAttr") String userAttr,
                                            @RequestParam("pkPath") String pkPath,
                                            @RequestParam("mskPath") String mskPath,
                                            @RequestParam("pbPath") String pbPath,
                                          @RequestParam("skPath") String skPath) throws Exception {

        return aaService.keyGen(userAttr, pkPath, mskPath, skPath, pbPath,uid);
    }

}
