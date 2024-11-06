package org.tangyang.hyperledgerfabric.app.javademo.utils;

import com.alibaba.fastjson.JSON;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.CPABE;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.PolicyNode;
import org.tangyang.hyperledgerfabric.app.javademo.cpabe.TreeNode;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserSKTest {
    public static void main(String[] args) throws Exception {
        String tagsPath = "C:\\Users\\张志蕾\\Desktop\\实验\\完整性验证实验\\hvt\\tags.txt";
        String dataPath = "C:\\Users\\张志蕾\\Desktop\\实验\\完整性验证实验\\data\\et.jpg";
        String ctPath = "C:\\Users\\张志蕾\\Desktop\\实验\\完整性验证实验\\data\\ct2attr";
        String etPath = "C:\\Users\\张志蕾\\Desktop\\实验\\完整性验证实验\\data\\et2attr.jpg";
        String blockPath = "C:\\Users\\张志蕾\\Desktop\\实验\\完整性验证实验\\data\\shuju_block0.block";
        String paramsPath = "C:\\Users\\张志蕾\\Desktop\\实验\\v1\\pairing160_512.properties";
        String pkPath = "C:\\Users\\张志蕾\\Desktop\\实验\\私钥生成时间测试\\params\\pk";
        String mskPath = "C:\\Users\\张志蕾\\Desktop\\实验\\私钥生成时间测试\\params\\msk";
        String userSkPath = "C:\\Users\\张志蕾\\Desktop\\实验\\私钥生成时间测试\\params\\zuheAttr";
        String policyPath = "C:\\Users\\张志蕾\\Desktop\\实验\\v1\\do\\2attr.json";
        String decryptSKPath = "C:\\Users\\张志蕾\\Desktop\\实验\\v1\\du\\USK";
        String uid = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmnL69mY8xDP7C6Tdva347X651/nh0VbTs0eKqCjiXRbdj+0/mapmLWc7/Vaqrmzn4WgJOZ008h+6FrNgDxDIMl/7Ou++L7aIcckQzTjkD+Y4LMW6IvOakdBUlKvCrn8E5AzxWmgV/13rdZ6oDaBRueRk//jLdbd5MAuEGfYurvwIDAQAB";
        String gnPath = "C:\\Users\\张志蕾\\Desktop\\实验\\v1\\do\\gndata";
        //BigInteger[] aa = new BigInteger[2];
        //aa[0] = new BigInteger("321413243254353465");
        //aa[1] = new BigInteger("132124132432545545");
        //GenerateHVT.saveArrayToFile(aa,gnPath);

        CPABE.setup(paramsPath,pkPath,mskPath,160,512);
        Pairing pb = PairingFactory.getPairing(paramsPath);
        Element kk = pb.getGT().newRandomElement();
        byte[] key = kk.toBytes();
        System.out.println(key.length);

        long start = System.currentTimeMillis();
        String arr = "A,B,C";
        String[] userAttrList = arr.split(",");
        for (int i = 0; i < userAttrList.length; i++) {
            userAttrList[i] = userAttrList[i].toLowerCase();
        }
        String arr1 = "D,E,F";
        String[] userAttrList2 = arr1.split(",");
        for (int i = 0; i < userAttrList2.length; i++) {
            userAttrList2[i] = userAttrList2[i].toLowerCase();
        }
        String arr2 = "A,B,C,D,E,F";
        String[] userAttrList3 = arr2.split(",");
        for (int i = 0; i < userAttrList3.length; i++) {
            userAttrList3[i] = userAttrList3[i].toLowerCase();
        }
        //CPABE.keygenTest1(userAttrList,pkPath,mskPath,userSkPath,uid,paramsPath);
        //CPABE.keygenTest(userAttrList,pkPath,mskPath,userSkPath,uid,paramsPath);
        //CPABE.keygen(userAttrList,pkPath,mskPath,userSkPath,paramsPath);
        //CPABE.kemEncryptByte(ConvertFiles.readFileToByteArray(dataPath),policyPath,pkPath,ctPath,paramsPath);
        //CPABE.kemDecryptByte(ctPath,userSkPath,paramsPath);
        //byte[] bytes = CPABE.kemDecryptByte(ctPath, userSkPath, paramsPath);
        //ConvertFiles.writeByteArrayToFile(bytes,etPath);
        /*Element t = pb.getG1().newRandomElement().getImmutable();
        byte[] tBytes = t.toBytes();
        String string = Base64.getEncoder().encodeToString(t.toBytes());
        byte[] bytes = Base64.getDecoder().decode(string);
        Element t1 = pb.getG1().newElementFromBytes(bytes).getImmutable();
        System.out.println(t.equals(t1));
        System.out.println(string);
        System.out.println(Arrays.equals(tBytes,bytes));*/
        /*Map<String, String> dd = CPABE.ddGen(userAttrList, pkPath, mskPath, uid, paramsPath);
        String tt = dd.get("t");
        Map<String, String> sk1 = CPABE.attSKGen(tt, userAttrList, uid, paramsPath);
        Properties skpro = new Properties();*/
        /*for(String key: dd.keySet()){
            if(key.equals("t")) continue;
            skpro.setProperty(key,dd.get(key));
        }
        for(String key: sk1.keySet()){
            skpro.setProperty(key,sk1.get(key));
        }*/
        List<String> blockchainKey = new ArrayList<>();
        List<String> blockchainValue = new ArrayList<>();
        List<String> finalKey = new ArrayList<>();
        List<String> finalValue = new ArrayList<>();
        Properties skpro = new Properties();
        List<String> DDkey = new ArrayList<>();
        List<String> DDvalue = new ArrayList<>();

        String t = CPABE.ddGen1(userAttrList3, pkPath, mskPath, uid, paramsPath, DDkey, DDvalue);
        String encryptDDKey = RSAUtils.encryptByPublicKey(uid,JSON.toJSONString(DDkey));
        String encryptDDValue = RSAUtils.encryptByPublicKey(uid,JSON.toJSONString(DDvalue));
        blockchainKey.add(encryptDDKey);
        blockchainValue.add(encryptDDValue);

        /*for(int i = 0;i < key.size();i++){
            skpro.setProperty(key.get(i), value.get(i));
        }*/

        List<String> Dikey = new ArrayList<>();
        List<String> Divalue = new ArrayList<>();
        CPABE.attSKGen1(t, userAttrList2, uid, paramsPath ,Dikey, Divalue);
        String encryptDiKey = RSAUtils.encryptByPublicKey(uid,JSON.toJSONString(Dikey));
        String encryptDiValue = RSAUtils.encryptByPublicKey(uid,JSON.toJSONString(Divalue));
        blockchainKey.add(encryptDiKey);
        blockchainValue.add(encryptDiValue);

        List<String> Dikey1 = new ArrayList<>();
        List<String> Divalue1 = new ArrayList<>();
        CPABE.attSKGen1(t, userAttrList3, uid, paramsPath ,Dikey1, Divalue1);
        String encryptDiKey1 = RSAUtils.encryptByPublicKey(uid,JSON.toJSONString(Dikey1));
        String encryptDiValue1 = RSAUtils.encryptByPublicKey(uid,JSON.toJSONString(Divalue1));
        blockchainKey.add(encryptDiKey1);
        blockchainValue.add(encryptDiValue1);

        String decryptSK = "";
        try (FileInputStream fis = new FileInputStream(decryptSKPath)) {
            byte[] bytes = fis.readAllBytes();
            decryptSK = new String(bytes);
            System.out.println("用户RSA私钥成功写入字符串");
        } catch (IOException e) {
            System.out.println("读取用户RSA私钥时发生错误：" + e.getMessage());
        }

        for(int i =0;i <  blockchainKey.size();i++){
            List<String> tempKey = JSON.parseArray(RSAUtils.decryptByPrivateKey(decryptSK,blockchainKey.get(i)),String.class);
            List<String> tempValue = JSON.parseArray(RSAUtils.decryptByPrivateKey(decryptSK,blockchainValue.get(i)),String.class);
            finalKey.addAll(tempKey);
            finalValue.addAll(tempValue);
        }
        for(int i = 0;i < finalKey.size();i++){
            skpro.setProperty(finalKey.get(i), finalValue.get(i));
        }
        CPABE.storePropToFile(skpro,userSkPath);


        /*File file=new File(policyPath);
        String accessTreeString= FileUtils.readFileToString(file,"UTF-8");
        Map<String, TreeNode> accessTree1 = CPABE.jsonStringToAccessTree(accessTreeString);
        //String[][] tempA = CPABE.accessTreeToString(accessTree);
        //Map<String, TreeNode> accessTree1 = CPABE.stringToTree(tempA);
        List<String> indexList = new ArrayList<>();
        List<String> nodesList = new ArrayList<>();
        CPABE.accessTreeToStringList(accessTree1,indexList,nodesList);
        Map<String, TreeNode> accessTree = CPABE.stringListToTree(indexList, nodesList);
        System.out.println(CPABE.validCheck(accessTree, "0", userAttrList));*/
        /*for(String key:accessTree.keySet()){
            TreeNode temp = accessTree.get(key);
            //System.out.println(temp.toString());
            if(temp.isLeaf()){
                String nodeStr = JSON.toJSONString(temp);
                String[] arr1 = nodeStr.split("\"");
                System.out.println("有属性？"+Arrays.asList(arr1).contains("att"));
                //System.out.println(arr1[3]);
                System.out.println(new PolicyNode(arr1[3]).toString());
                //System.out.println(nodeStr);
            }else {
                String nodeStr = JSON.toJSONString(temp);
                String[] arr1 = nodeStr.split("\"");
                //TreeNode treeNode3 = JSON.parseObject(nodeStr, TreeNode.class);
                PolicyNode nodes = JSON.parseObject(nodeStr,PolicyNode.class);
                System.out.println("没属性？"+Arrays.asList(arr1).contains("att"));
                System.out.println(nodes.toString());
            }
        }*/

        //System.out.println(accessTree.toString());
        //System.out.println(CPABE.validCheck(accessTree,"0",userAttrList));
        /*String[][] treeStr = CPABE.accessTreeToString(accessTree);
        Map<String, TreeNode> stringTreeNodeMap = CPABE.stringToTree(treeStr);
        System.out.println(accessTree);
        System.out.println("================");
        System.out.println(stringTreeNodeMap);
        System.out.println(stringTreeNodeMap.equals(accessTree));*/
        System.out.println(System.currentTimeMillis() - start);
        /*String a = "b";

        switch (a){
            case "a":
                String b = "a";
                break;
            case "b":
                b = "b";
                System.out.println(b);
                break;
            case "c":
                b = "c";
                break;
        }*/

        /*String r = System.currentTimeMillis()+"";
        System.out.println(new BigInteger(r));*/
        /*String signature = RSAUtils.getSignature("123456", "D:\\org3\\lab1\\du\\sk");
        boolean verify = RSAUtils.verify("", "123456", signature);
        System.out.println(verify);*/
        //用户生成RSA公私钥对
        /*RSAUtils.RSAKeyPair keyPair= RSAUtils.generateKeyPair();
        String pk = keyPair.getPublicKey();
        String sk = keyPair.getPrivateKey();
        try (FileOutputStream fos = new FileOutputStream("E:\\data\\RSA\\pk")) {
            byte[] bytes = pk.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("字符串已成功写入文件");
        } catch (IOException e) {
            System.out.println("写入文件时发生错误：" + e.getMessage());
        }
        try (FileOutputStream fos = new FileOutputStream("E:\\data\\RSA\\sk")) {
            byte[] bytes = sk.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            System.out.println("字符串已成功写入文件");
        } catch (IOException e) {
            System.out.println("写入文件时发生错误：" + e.getMessage());
        }
        String decryptPK = "";
        try (FileInputStream fis = new FileInputStream("E:\\data\\RSA\\pk")) {
            byte[] bytes = fis.readAllBytes();
            decryptPK = new String(bytes);
            System.out.println("文件内容成功写入字符串");
        } catch (IOException e) {
            System.out.println("读取文件时发生错误：" + e.getMessage());
        }
        String decryptSK = "";
        try (FileInputStream fis = new FileInputStream("E:\\data\\RSA\\sk")) {
            byte[] bytes = fis.readAllBytes();
            decryptSK = new String(bytes);
            System.out.println("文件内容成功写入字符串");
        } catch (IOException e) {
            System.out.println("读取文件时发生错误：" + e.getMessage());
        }
        //DO根据用户公钥加密CPABE的用户私钥
        String cpabeSK = ConvertFiles.convertBinaryToText(Path.of("E:\\data\\sk_ac"));
        String encpabeSK = RSAUtils.encryptByPublicKey(decryptPK,cpabeSK);//经过网络
        //uploadSK
        //用户接收CPABE私钥并用RSA私钥解密
        String decpabeSK = RSAUtils.decryptByPrivateKey(decryptSK,encpabeSK);//经过网络
        ConvertFiles.convertTextToBinary(decpabeSK,Path.of("E:\\data\\firstSK"));
        //downloadSK*/
        /*List<String> stringList = new ArrayList<>();
        stringList.add("123");
        stringList.add("456");
        stringList.add("789");

        List<BigInteger> bigIntegerList = stringList.stream()
                .map(BigInteger::new)
                .collect(Collectors.toList());

        // 打印结果
        for (BigInteger bigInteger : bigIntegerList) {
            System.out.println(bigInteger);
        }*/
       /* List<BigInteger> localHvtList = new ArrayList<>();
        // 添加 localHvtList 的元素...
        String filePath = "E:\\data\\tag\\tags10000.txt";
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));

        String resStr = (String) ois.readObject();
        ois.close();
        localHvtList = JSON.parseArray(resStr,BigInteger.class);

        List<BigInteger> networkHvtList = new ArrayList<>();
        // 添加 networkHvtList 的元素...
        networkHvtList = JSON.parseArray(resStr,BigInteger.class);

        networkHvtList.remove(99);
        networkHvtList.add(99,new BigInteger("47564745675"));

        AtomicInteger index = new AtomicInteger(0); // 定义一个 AtomicInteger 对象

        List<BigInteger> finalLocalHvtList = localHvtList;
        List<BigInteger> finalNetworkHvtList = networkHvtList;
        List<Integer> diffIndexList = IntStream.range(0, Math.min(localHvtList.size(), networkHvtList.size()))
                .parallel()
                .filter(i -> {
                    if (!finalLocalHvtList.get(i).equals(finalNetworkHvtList.get(i))) {
                        index.incrementAndGet(); // 修改 index 变量的值
                        return true;
                    }
                    return false;
                })
                .boxed()
                .toList();

        System.out.println("两个 List 不相等的下标为: " + diffIndexList);
        System.out.println("index 的值为: " + index.get());*/
    }
}
