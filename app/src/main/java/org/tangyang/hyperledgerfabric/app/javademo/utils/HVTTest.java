package org.tangyang.hyperledgerfabric.app.javademo.utils;

import com.alibaba.fastjson.JSON;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HVTTest {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        String tagsPath = "C:\\Users\\唐杨\\Desktop\\实验\\完整性验证实验\\hvt\\tags.txt";
        String dataPath = "C:/Users/唐杨/Desktop/实验/v1/do/data/4kb/4000kb";
        String blockPath = "C:\\Users\\唐杨\\Desktop\\实验\\完整性验证实验\\data\\shuju_block0.block";
        String paramsPath = "C:\\Users\\唐杨\\Desktop\\实验\\完整性验证实验\\params\\pairingParameters.properties";
        //String uid = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCszxEVvUExqnPCdjkZXtgVz6KY0P4Wda8LJ70ehhcaV9omc21OTJwJA9FvDLqyx4bjG61TA9HmRNCC/B++hOhZ5Y/L2ElM2TsawgatLq3w8gyLQMe05gJa+aewQ5vJO/DeYT+EPZEi6Qrb9/aEHUsEEvNbvQ32RIsaA9ol7Xj1NwIDAQAB";

        BigInteger[] gn = GenerateHVT.generate_g_N();
        //SplitFileUtil.cut(new File(dataPath),dataPath,1000);

        Pairing bp = PairingFactory.getPairing(paramsPath);
        BigInteger sita = bp.getZr().newRandomElement().getImmutable().toBigInteger();
        long start = System.currentTimeMillis();
        List<BigInteger> hvts = GenerateHVT.generateTagByZr(gn[0],gn[1],dataPath,tagsPath,1000,paramsPath);
        //System.out.println(System.currentTimeMillis() - start);
        /*List<BigInteger> ranP = GenerateHVT.randP(1000);
        //List<BigInteger> hvts = GenerateHVT.readTagsFromFilePath(tagsPath);
        BigInteger g = hvts.get(0);
        BigInteger n = hvts.get(1);
        BigInteger chal = g.modPow(sita,n);

        BigInteger sigma = GenerateHVT.generateSigma(hvts,n,sita,ranP);
        BigInteger proof = GenerateHVT.generateProofFromZr(blockPath, chal, n, 1000,ranP,paramsPath);

        System.out.println(sigma);
        System.out.println(proof);
        List<BigInteger> p = GenerateHVT.randP(1000);

        String str = p.toString();
        List<BigInteger> p1 = JSON.parseArray(str, BigInteger.class);
        System.out.println(p1.equals(p));*/



    }
}
