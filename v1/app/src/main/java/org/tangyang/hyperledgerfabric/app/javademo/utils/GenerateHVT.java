package org.tangyang.hyperledgerfabric.app.javademo.utils;

import com.alibaba.fastjson.JSON;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateHVT {
    /**
     * 生成g,N
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static BigInteger[] generate_g_N() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        BigInteger p = privateKey.getModulus();
        BigInteger q = privateKey.getPrivateExponent();
        BigInteger n = p.multiply(q);
        BigInteger p1 = p.subtract(BigInteger.valueOf(1)).divide(BigInteger.valueOf(2));
        BigInteger q1 = q.subtract(BigInteger.valueOf(1)).divide(BigInteger.valueOf(2));
        BigInteger pq = p1.multiply(q1);
        BigInteger g = pq.multiply(pq).mod(n);
        if(pq.gcd(n).equals(BigInteger.valueOf(1))){
            if(g.subtract(BigInteger.valueOf(1)).gcd(n).equals(BigInteger.valueOf(1))){
                return new BigInteger[]{g,n};
            }else {
                generate_g_N();
            }
        }
        return generate_g_N();
    }

    /**
     *
     * @param filePath
     * @param outputPath
     * @param count
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static List<BigInteger> generateTag(BigInteger g, BigInteger n, String filePath,String outputPath,int count) throws NoSuchAlgorithmException, IOException {
        String nameAndPath= SplitFileUtil.getStrBeforeFileExtension(filePath);
        String data = null;
        List<BigInteger> res = new ArrayList<>();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        res.add(g);
        res.add(n);

        for(int i = 0;i < count;i++){
            data = ConvertFiles.convertBinaryToText(Path.of(nameAndPath + "_block" + i + ".block"));
            byte[] bytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            BigInteger b = new BigInteger(1, bytes);
            res.add(g.modPow(b, n));
        }
        String result = JSON.toJSONString(res);
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("同态标签已成功写入文件");
        } catch (IOException e) {
            System.out.println("写入同态标签时发生错误：" + e.getMessage());
        }
        return  res;
    }
    public static List<BigInteger> readTagsFromFilePath(String outputPath) throws IOException {
        List<BigInteger> res = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(outputPath)) {
            int content;
            StringBuilder sb = new StringBuilder();
            while ((content = fis.read()) != -1) {
                sb.append((char) content);
            }
            res = JSON.parseArray(sb.toString(), BigInteger.class);
            System.out.println("同态标签已成功读取");
        } catch (IOException e) {
            System.out.println("读取同态标签时发生错误：" + e.getMessage());
        }
        return res;
    }

    public static List<BigInteger> generateTag(String filePath,int count,BigInteger g,BigInteger n) throws NoSuchAlgorithmException, IOException {
        String nameAndPath= SplitFileUtil.getStrBeforeFileExtension(filePath);

        String data = null;
        List<BigInteger> res = new ArrayList<>();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        res.add(g);
        res.add(n);
        for(int i = 0;i < count;i++){
            data = ConvertFiles.convertBinaryToText(Path.of(nameAndPath + "_block" + i + ".block"));
            byte[] bytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            BigInteger b = new BigInteger(1, bytes);
            res.add(g.modPow(b, n));
        }

        return  res;
    }

    public static String generateTagR(List<BigInteger> resultList,BigInteger n,BigInteger randNum,int start,int end) {
        BigInteger tempNum = new BigInteger("1");
        for(int i = start;i < end;i++){
            tempNum = tempNum.multiply(resultList.get(i)).mod(n);
        }
        tempNum = tempNum.modPow(randNum,n);
        return  GetHash.getSHA256(tempNum.toString());
    }

    public static BigInteger[] randP(int size){
        BigInteger[] arr = new BigInteger[size];
        Random random = new Random();

        for(int i = 0;i < size;i++){
            BigInteger randomInteger = new BigInteger(256, random);
            arr[i] = randomInteger;
        }

        return arr;
    }

    public static BigInteger generateSigma(List<BigInteger> resultList,BigInteger n,BigInteger randNum,BigInteger[] arr) {
        BigInteger res = new BigInteger("1");

        for(int i = 2;i < resultList.size();i++){
            res = res.multiply(resultList.get(i).modPow(arr[i-2],n)).mod(n);
        }
        res = res.modPow(randNum,n);
        return res;
    }

    public static BigInteger generateProof(String filePath, BigInteger chal,BigInteger n,int count,BigInteger[] arr) throws NoSuchAlgorithmException {
        String namePath = SplitFileUtil.getStrBeforeBlockName(filePath);
        BigInteger proof = new BigInteger("1");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for(int i = 0;i < count;i++){
            String data = ConvertFiles.convertBinaryToText(Path.of(namePath + "_block" + i + ".block"));
            byte[] bytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            BigInteger b = new BigInteger(1, bytes);
            proof = proof.multiply(chal.modPow(b.multiply(arr[i]),n)).mod(n);
        }
        return proof;
    }

    public static List<BigInteger> generateTagByZr(BigInteger g, BigInteger n, String filePath, String outputPath, int count, Pairing bp){
        String nameAndPath= SplitFileUtil.getStrBeforeFileExtension(filePath);
        String data = null;
        List<BigInteger> res = new ArrayList<>();
        res.add(g);
        res.add(n);

        for(int i = 0;i < count;i++){
            data = ConvertFiles.convertBinaryToText(Path.of(nameAndPath + "_block" + i + ".block"));
            BigInteger b = bp.getZr().newElementFromBytes(data.getBytes(StandardCharsets.UTF_8)).getImmutable().toBigInteger();
            res.add(g.modPow(b, n));
        }
        String result = JSON.toJSONString(res);
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("同态标签已成功写入文件");
        } catch (IOException e) {
            System.out.println("写入同态标签时发生错误：" + e.getMessage());
        }
        return  res;
    }
    public static BigInteger generateProofFromZr(String filePath, BigInteger chal,BigInteger n,int count,BigInteger[] arr,Pairing bp){
        String namePath = SplitFileUtil.getStrBeforeBlockName(filePath);
        BigInteger proof = new BigInteger("1");
        for(int i = 0;i < count;i++){
            String data = ConvertFiles.convertBinaryToText(Path.of(namePath + "_block" + i + ".block"));
            BigInteger b = bp.getZr().newElementFromBytes(data.getBytes(StandardCharsets.UTF_8)).getImmutable().toBigInteger();
            proof = proof.multiply(chal.modPow(b.multiply(arr[i]),n)).mod(n);
        }
        return proof;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {

        BigInteger[] gn = generate_g_N();
        System.out.println(gn[0]);
        System.out.println(gn[1]);
        long start = System.currentTimeMillis();
        SplitFileUtil.cut(new File("D:\\org3\\lab1\\do\\1000\\encryptData"),"D:\\org3\\lab1\\do\\1000\\encryptData",1000);
        generateTag(gn[0],gn[1],"D:\\org3\\lab1\\do\\1000\\encryptData","D:\\org3\\lab1\\do\\labTags1000.txt",1000);
        System.out.println(System.currentTimeMillis() - start);
        /*List<BigInteger> resList = null;
        String filePath = "E:\\data\\tag\\tags10000.txt";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));

        String resStr = (String) ois.readObject();
        ois.close();
        resList = JSON.parseArray(resStr,BigInteger.class);
        String chalStr = "随机挑战数";
        System.out.println(System.currentTimeMillis() - start);
        BigInteger r = new BigInteger(1, digest.digest(chalStr.getBytes(StandardCharsets.UTF_8)));
        BigInteger g = resList.get(0);
        BigInteger n = resList.get(1);
        BigInteger result = new BigInteger("1");
        BigInteger chal = g.modPow(r,n);*/
        //BigInteger challengeNum = new BigInteger("2363765069706858465275758165428177247932618608781478363381329385584403939110634989191441283382250471987869259131841917733926486703939251939540584209117664600998708961551537564894002228623375479014105508488073173798842370171275506105377509157922336858305384892645286150344511640207329382456979840652428252627793272125951450274929521939846163387101538233885905671125711189988632660589745187129600517317837016583506694990587967808800825421229898536313848698469257516416621748374150587418695806704493112317545724093270969079796373908860049123069868120048502878117743812622445410722462095872892947468080982526692290954223");
        //BigInteger nNum = new BigInteger("2680337453250669060536280464145682523671850999476036119394128944151685819208065027236820802000990381528181548296166258568094258852676411366580705870454265596084792938361027358418138929095435818953097788041118116773938161939518376744342395137019407480820649818786421875766844498002818647222284722083582324013449837545799805682364200954932159396420116804878538533057333763358674385949317251363546410140202043969649475779986811226783902291188454426156409838677760082293612882239308882788761162667996771319889183658561839763735468238082748850182876715312058389931799824758309545235285359235446579387482296259449350922697");
        /*for(int i = 2;i < 1002;i++){
            result = result.multiply(resList.get(i).modPow(r,n)).mod(n);
            //result = result.multiply(resList.get(i)).mod(n);
        }
        //result = result.modPow(r,n);
        System.out.println(result);
        System.out.println(System.currentTimeMillis() - start);
        //System.out.println(r);
        //System.out.println(GetHash.getSHA256(result.toString()));
        BigInteger result1 = resList.get(2);
        for(int i = 3;i < resList.size();i++){
            result1 = result1.multiply(resList.get(i));
        }
        BigInteger gb_gb_gb = new BigInteger("1");*/
        //MessageDigest digest = MessageDigest.getInstance("SHA-256");
        //String data = null;
        //BigInteger blockSum = new BigInteger("0");
        //BigInteger proof = new BigInteger("1");
        /*for(int i = 0;i < 1000;i++){
            data = ConvertFiles.convertBinaryToText(Path.of("E:\\data\\test\\40mb\\" + "file_block" + i + ".block"));
            byte[] bytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            BigInteger b = new BigInteger(1, bytes);
            //blockSum = blockSum.add(b);
            //gb_gb_gb = gb_gb_gb.multiply(g.modPow(b,n)).mod(n);
            proof = proof.multiply(challengeNum.modPow(b,nNum)).mod(nNum);
        }
        System.out.println(System.currentTimeMillis() - start);
        //BigInteger g_b1_b2_b3 = g.modPow(blockSum,n);
        //System.out.println(blockSum.toString());
        //System.out.println(gb_gb_gb);
        //System.out.println(g_b1_b2_b3);//得出结论g^(b1+b2+...+bn) mod n = (g^b1 mod n)*(g^b2 mod n)*...*(g^bn mod n)
        //System.out.println("tag_r = "+GetHash.getSHA256(result.toString()));
        //System.out.println("proof = "+GetHash.getSHA256(proof.toString()));*/
        /*SplitFileUtil.cut(new File("D:/org3/encrypt"),"D:/org3/encrypt",1000);
        long start = System.currentTimeMillis();
        BigInteger n = new BigInteger("8781983506064602291682916200053752784630117763179853412429429327126555262258104047090290225939740046720403474344512514108992648481579510761814101946964087986559532045587536992825592764158081992989138856928270043880330932016839224772156511723117411069600793528169386270325248663007453266253059101268246453518734603180713145177854763000535263708298130209438881698292845432287352071694180442611920858342425435929357249635253097598942882214667607459378061890287879597982169308439505024617871274692695330771625798193109515809308994512828222074936739112295250219434877983543733327821868695189111957605962427321778905819457");
        BigInteger g = new BigInteger("49454698819002130072961663063579554077633806590529913079118869669179872959769664028357694879184451363920254487883286186093738111023464113450520210039956798358131859013496304538437031407355462556539205493125144154554296222634225958697613753220983023327116366939259421370850601812128219844265693367014817952202");
        String importPath = "D:/org3/encrypt";
        String savePath = "D:/org3/tags1000test.txt";
        generateTag(g,n,importPath,savePath,1000);
        System.out.println((System.currentTimeMillis() - start)+"ms");*/
    }
}
