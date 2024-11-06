package org.tangyang.contract;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;
import org.tangyang.challenge.Challenge;
import org.tangyang.challenge.ChallengeQueryResult;
import org.tangyang.challenge.ChallengeQueryResultList;
import org.tangyang.dataOwner.DOQueryResult;
import org.tangyang.dataOwner.DOQueryResultList;
import org.tangyang.dataOwner.DataOwner;
import org.tangyang.file.FileQueryResult;
import org.tangyang.file.FileQueryResultList;
import org.tangyang.file.HvtFile;
import org.tangyang.fileBlock.FileBlock;
import org.tangyang.fileBlock.FileBlockQueryPageResult;
import org.tangyang.fileBlock.FileBlockQueryResult;
import org.tangyang.fileBlock.FileBlockQueryResultList;
import org.tangyang.proof.CspProof;
import org.tangyang.proof.CspProofQueryResult;
import org.tangyang.proof.CspProofQueryResultList;
import org.tangyang.user.User;
import org.tangyang.user.UserQueryPageResult;
import org.tangyang.user.UserQueryResult;
import org.tangyang.user.UserQueryResultList;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
@Contract(
        name = "CloudDataContract",
        info = @Info(
                title = "CloudData Contract",
                description = "The hyperlegendary CloudData contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))
@Default
@Log
public class CloudDataContract implements ContractInterface {

    @Transaction
    public Challenge createChallenge(final Context ctx, final String key , String g , String n , String challengeNumber, String initiator, String signature, String filessID, Integer start, Integer end) {
        ChaincodeStub stub = ctx.getStub();
        String challengeState = stub.getStringState(key);

        if (StringUtils.isNotBlank(challengeState)) {
            String errorMessage = String.format("Challenge %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Challenge challenge = new Challenge()
                .setG(g)
                .setN(n)
                .setChallengeNumber(challengeNumber)
                .setInitiator(initiator)
                .setSignature(signature)
                .setFilessID(filessID)
                .setStart(start)
                .setEnd(end);

        String json = JSON.toJSONString(challenge);
        stub.putStringState(key, json);

        stub.setEvent("createChallengeEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return challenge;
    }

    @Transaction
    public Challenge createChallengeByUser(final Context ctx, final String key ,String initiator, String filessID,String signature) {
        ChaincodeStub stub = ctx.getStub();
        String challengeState = stub.getStringState(key);

        if (StringUtils.isNotBlank(challengeState)) {
            String errorMessage = String.format("Challenge %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Challenge challenge = new Challenge()
                .setInitiator(initiator)
                .setFilessID(filessID)
                .setSignature(signature);

        String json = JSON.toJSONString(challenge);
        stub.putStringState(key, json);

        stub.setEvent("createChallengeEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return challenge;
    }

    @Transaction
    public Challenge uploadChal(final Context ctx, final String key, String g , String n , String challengeNumber,Integer start, Integer end){
        ChaincodeStub stub = ctx.getStub();
        String challengeState = stub.getStringState(key);

        Challenge oldChal = JSON.parseObject(challengeState,Challenge.class);
        String fileID = oldChal.getFilessID();
        String initiator = oldChal.getInitiator();
        String signature = oldChal.getSignature();
        Challenge newChallenge = new Challenge()
                .setG(g)
                .setN(n)
                .setChallengeNumber(challengeNumber)
                .setStart(start)
                .setEnd(end)
                .setFilessID(fileID)
                .setInitiator(initiator)
                .setSignature(signature);

        stub.putStringState(key,JSON.toJSONString(newChallenge));

        return newChallenge;
    }

    @Transaction
    public Challenge deleteChallenge(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String challengeState = stub.getStringState(key);

        if (StringUtils.isBlank(challengeState)) {
            String errorMessage = String.format("Challenge %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(key);

        return JSON.parseObject(challengeState , Challenge.class);
    }

    @Transaction
    public Challenge queryChallenge(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String catState = stub.getStringState(key);

        if (StringUtils.isBlank(catState)) {
            String errorMessage = String.format("Challenge %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(catState , Challenge.class);
    }

    @Transaction
    public ChallengeQueryResultList queryChallengeByInitiator(final Context ctx, String initiator){
        log.info(String.format("使用 initiator 查询 challenge , initiator = %s" , initiator));

        String query = String.format("{\"selector\":{\"initiator\":\"%s\"} , \"use_index\":[\"_design/indexInitiatorDoc\", \"indexInitiator\"]}", initiator);

        log.info(String.format("query string = %s" , query));
        return queryChallenge(ctx.getStub() , query);
    }

    @Transaction
    public Challenge queryChallengeByChal(final Context ctx, String challengeNumber){
        log.info(String.format("使用 challengeNumber 查询 challenge , initiator = %s" , challengeNumber));

        String query = String.format("{\"selector\":{\"initiator\":\"%s\"} , \"use_index\":[\"_design/indexChallengeNumberInitiatorDoc\", \"indexChallengeNumberInitiator\"]}", challengeNumber);

        log.info(String.format("query string = %s" , query));
        return queryChallenge(ctx.getStub() , query).getChallenges().get(0).getChallenge();
    }

    private ChallengeQueryResultList queryChallenge(ChaincodeStub stub, String query){
        ChallengeQueryResultList resultList = new ChallengeQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<ChallengeQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new ChallengeQueryResult()
                        .setKey(kv.getKey())
                        .setChallenge(JSON.parseObject(kv.getStringValue() , Challenge.class)));
            }
            resultList.setChallenges(results);
        }
        return  resultList;

    }

    /**
     *
     * @param ctx
     * @param key
     * @param rustr 随机数字符串
     * @param proofstr 证明字符串
     * @param tstr tag (i*j*..)字符串
     * @return
     */
    @Transaction
    public String disputeArbitration(final Context ctx ,final String key, String rustr,String proofstr,String tstr){
        String result;
        ChaincodeStub stub = ctx.getStub();
        Challenge challenge = JSON.parseObject(stub.getStringState(key),Challenge.class);
        BigInteger r_u = new BigInteger(rustr);
        BigInteger proof = new BigInteger(proofstr);
        BigInteger t = new BigInteger(tstr);
        BigInteger g = new BigInteger(challenge.getG());
        BigInteger n = new BigInteger(challenge.getN());
        BigInteger challengeNumber = new BigInteger(challenge.getChallengeNumber());
        //如果算出来的挑战数与记录在区块链中的挑战数相等
        if(g.modPow(r_u, n).equals(challengeNumber)){
            //HVT相等
            if(proof.equals(t.modPow(r_u,n)))
            {
                result = "bad DO or DU";
            }
            else {
                result = "bad csp";
            }
        }
        else {
            //如果算出来的挑战数与记录在区块链中的挑战数不相等，说明DO输入了假的随机挑战数恶意攻击CSP
            result = "bad DO";
        }
        if(result.equals("bad DO or DU")){
            if(challenge.getInitiator().equals("DO")){
                result = "bad DO";
            }
            else {
                result = "bad DU";
            }
        }
        return result;
    }
    @Transaction
    public HvtFile createFile(final Context ctx, final String key , String filesID, String g, String n, String count){
        ChaincodeStub stub = ctx.getStub();
        String filesState = stub.getStringState(key);

        if (StringUtils.isNotBlank(filesState)) {
            String errorMessage = String.format("File %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        HvtFile file = new HvtFile()
                .setFilesID(filesID)
                .setG(g)
                .setN(n)
                .setCount(count);

        String json = JSON.toJSONString(file);
        stub.putStringState(key, json);

        stub.setEvent("createFileEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return file;
    }

    @Transaction
    public HvtFile queryFile(final Context ctx, final String key){
        ChaincodeStub stub = ctx.getStub();
        String filesState = stub.getStringState(key);

        if (StringUtils.isBlank(filesState)) {
            String errorMessage = String.format("File %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        return JSON.parseObject(filesState, HvtFile.class);
    }

    @Transaction
    public String queryGNByFileID(final Context ctx, String fileID){
        log.info(String.format("使用 fileID 查询 file , fileID = %s" , fileID));

        String query = String.format("{\"selector\":{\"filesID\":\"%s\"} , \"use_index\":[\"_design/indexFilesIDDoc\", \"indexFilesID\"]}", fileID);

        log.info(String.format("query string = %s" , query));

        HvtFile file = queryFile(ctx.getStub() , query).getFiles().get(0).getFile();
         String g = file.getG();
         String n = file.getN();
         String count = file.getCount();

         List<String> resultList = Lists.newArrayList();

         resultList.add(g);
         resultList.add(n);
         resultList.add(count);

         return JSON.toJSONString(resultList);
    }

    @Transaction
    public FileQueryResultList queryFileByFileID(final Context ctx, String fileID){
        log.info(String.format("使用 fileID 查询 file , fileID = %s" , fileID));

        String query = String.format("{\"selector\":{\"filesID\":\"%s\"} , \"use_index\":[\"_design/indexFilesIDDoc\", \"indexFilesID\"]}", fileID);

        log.info(String.format("query string = %s" , query));
        return queryFile(ctx.getStub() , query);
    }

    private FileQueryResultList queryFile(ChaincodeStub stub, String query){
        FileQueryResultList resultList = new FileQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<FileQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new FileQueryResult()
                        .setKey(kv.getKey())
                        .setFile(JSON.parseObject(kv.getStringValue() , HvtFile.class)));
            }
            resultList.setFiles(results);
        }
        return  resultList;
    }

    @Transaction
    public HvtFile deleteFile(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String fileState = stub.getStringState(key);

        if (StringUtils.isBlank(fileState)) {
            String errorMessage = String.format("File %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(key);

        return JSON.parseObject(fileState , HvtFile.class);
    }

    @Transaction
    public FileQueryResult deleteFileByFilesID(final Context ctx, final String filesID){

        log.info(String.format("删除 fileID 对应的 fileBlock, fileID = %s", filesID));
        ChaincodeStub stub = ctx.getStub();

        FileQueryResult result = queryFileByFileID(ctx,filesID).getFiles().get(0);

        stub.delState(result.getKey());

        return result;

    }

    @Transaction
    public FileBlock createFileBlock(final Context ctx, final String key , String fileID ,String hvt) {

        ChaincodeStub stub = ctx.getStub();
        String fileBlockState = stub.getStringState(key);

        if (StringUtils.isNotBlank(fileBlockState)) {
            String errorMessage = String.format("FileBlock %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        List<BigInteger> hvts = JSON.parseArray(hvt,BigInteger.class);

        FileBlock fileBlock = new FileBlock()
                .setFileID(fileID)
                .setHvt(hvts);

        String json = JSON.toJSONString(fileBlock);
        stub.putStringState(key, json);

        stub.setEvent("createFileBlock" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return fileBlock;
    }

    /**
     * 批量删除
     * @param ctx
     * @param fileID
     * @return
     */
    @Transaction
    public String deleteFileBlockByFileID(final Context ctx, String fileID) {

        log.info(String.format("删除 fileID 对应的 fileBlock, fileID = %s", fileID));

        // 查询需要删除的文件块
        FileBlockQueryResultList fileBlockList = queryFileBlocksByFileID(ctx, fileID);

        if (fileBlockList.getFileBlocks() != null && !fileBlockList.getFileBlocks().isEmpty()) {
            for (FileBlockQueryResult fileBlockQueryResult : fileBlockList.getFileBlocks()) {
                deleteFileBlock(ctx.getStub(), fileBlockQueryResult.getKey());
            }
            log.info("成功删除 fileID 对应的 fileBlock");
            return "成功删除 fileID 对应的 fileBlock";
        } else {
            log.info("没有找到需要删除的 fileBlock");
            return "没有找到需要删除的 fileBlock";
        }
    }

    private void deleteFileBlock(ChaincodeStub stub, String key) {
        log.info(String.format("删除 fileBlock, key = %s", key));
        stub.delState(key);
    }

    @Transaction
    public FileBlock queryFileBlock(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String fileBlockState = stub.getStringState(key);

        if (StringUtils.isBlank(fileBlockState)) {
            String errorMessage = String.format("FileBlock %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(fileBlockState , FileBlock.class);
    }
    @Transaction
    public FileBlockQueryResultList queryFileBlocksByFileID(final Context ctx, String fileID) {

        log.info(String.format("使用 fileID 查询 fileBlock , fileID = %s" , fileID));

        String query = String.format("{\"selector\":{\"fileID\":\"%s\"} , \"use_index\":[\"_design/indexFileIDDoc\", \"indexFileID\"]}", fileID);

        log.info(String.format("query string = %s" , query));
        return queryBlock(ctx.getStub() , query);
    }

    @Transaction
    public FileBlock queryFileBlockByFileID(final Context ctx, String fileID) {

        log.info(String.format("使用 fileID 查询 fileBlock , fileID = %s" , fileID));

        String query = String.format("{\"selector\":{\"fileID\":\"%s\"} , \"use_index\":[\"_design/indexFileIDDoc\", \"indexFileID\"]}", fileID);

        log.info(String.format("query string = %s" , query));
        return queryBlock(ctx.getStub() , query).getFileBlocks().get(0).getFileBlock();
    }
    private FileBlockQueryResultList queryBlock(ChaincodeStub stub , String query) {

        FileBlockQueryResultList resultList = new FileBlockQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<FileBlockQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new FileBlockQueryResult()
                        .setKey(kv.getKey())
                        .setFileBlock(JSON.parseObject(kv.getStringValue() , FileBlock.class)));
            }
            resultList.setFileBlocks(results);
        }

        return resultList;
    }

    @Transaction
    public FileBlockQueryPageResult queryFileBlockPageResultByFileID(final Context ctx, String fileID , Integer pageSize , String bookmark) {

        log.info(String.format("使用 fileID 分页查询 fileBlock , fileID = %s" , fileID));

        String query = String.format("{\"selector\":{\"fileID\":\"%s\"} , \"use_index\":[\"_design/indexFileIDDoc\", \"indexFileID\"]}", fileID);

        log.info(String.format("query string = %s" , query));

        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> queryResult = stub.getQueryResultWithPagination(query, pageSize, bookmark);

        List<FileBlockQueryResult> fileBlocks = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                fileBlocks.add(new FileBlockQueryResult()
                        .setKey(kv.getKey())
                        .setFileBlock(JSON.parseObject(kv.getStringValue() , FileBlock.class)));
            }
        }

        return new FileBlockQueryPageResult()
                .setFileBlocks(fileBlocks)
                .setBookmark(queryResult.getMetadata().getBookmark());
    }

    @Transaction
    public FileBlock deleteFileBlock(final Context ctx, final String key){
        ChaincodeStub stub = ctx.getStub();
        String fileBlockState = stub.getStringState(key);
        if (StringUtils.isBlank(fileBlockState)) {
            String errorMessage = String.format("FileBlock %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(key);

        return JSON.parseObject(fileBlockState,FileBlock.class);
    }

    @Transaction
    public DataOwner createDataOwner(final Context ctx, final String key , String name, String doID) {
        ChaincodeStub stub = ctx.getStub();
        String doState = stub.getStringState(key);

        if (StringUtils.isNotBlank(doState)) {
            String errorMessage = String.format("DataOwner %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        if(uniqueDOName(ctx,name)){
            DataOwner dataOwner = new DataOwner()
                    .setDOName(doID)
                    .setDOName(name);

            String json = JSON.toJSONString(dataOwner);
            stub.putStringState(key, json);

            stub.setEvent("createDataOwnerEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
            return dataOwner;
        }else{
            String errorMessage = String.format("DataOwnerName %s already exists", name);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
    }

    @Transaction
    public Boolean uniqueDOName(final Context ctx,String name){
        String query = String.format("{\"selector\":{\"dOName\":\"%s\"} , \"use_index\":[\"_design/indexDONameDoc\", \"indexDOName\"]}", name);
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);

        return IterableUtils.isEmpty(queryResult);
    }

    @Transaction
    public DataOwner queryDOByKey(final Context ctx,String key){
        ChaincodeStub stub = ctx.getStub();
        String DOState = stub.getStringState(key);

        if (StringUtils.isBlank(DOState)) {
            String errorMessage = String.format("User %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(DOState,DataOwner.class);
    }

    @Transaction
    public DataOwner queryDOByName(final Context ctx,String name){
        String query = String.format("{\"selector\":{\"dOName\":\"%s\"} , \"use_index\":[\"_design/indexDONameDoc\", \"indexDOName\"]}", name);
        ChaincodeStub stub = ctx.getStub();
        DOQueryResultList doQueryResultList = queryDO(stub, query);

        return doQueryResultList.getDOes().get(0).getDataOwner();
    }
    private DOQueryResultList queryDO(ChaincodeStub stub, String query){
        DOQueryResultList resultList = new DOQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<DOQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new DOQueryResult().setKey(kv.getKey()).setDataOwner(JSON.parseObject(kv.getStringValue() , DataOwner.class)));
            }
            resultList.setDOes(results);
        }else {
            String errorMessage = "dataOwner does not exist";
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return resultList;
    }

    @Transaction
    public DataOwner deleteDOByKey(final Context ctx, final String key){
        ChaincodeStub stub = ctx.getStub();
        String DOState = stub.getStringState(key);

        if (StringUtils.isBlank(DOState)) {
            String errorMessage = String.format("User %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(key);

        return JSON.parseObject(DOState,DataOwner.class);
    }

    @Transaction
    public DataOwner deleteDOByName(final Context ctx, String name){
        ChaincodeStub stub = ctx.getStub();
        String query = String.format("{\"selector\":{\"dOName\":\"%s\"} , \"use_index\":[\"_design/indexDONameDoc\", \"indexDOName\"]}", name);
        DOQueryResultList doQueryResultList = queryDO(stub, query);
        String key = doQueryResultList.getDOes().get(0).getKey();

        String DOState = stub.getStringState(key);
        stub.delState(key);

        return JSON.parseObject(DOState, DataOwner.class);
    }

    @Transaction
    public User createUser(final Context ctx, final String key , String name, String userID, String attrList) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(key);

        if (StringUtils.isNotBlank(userState)) {
            String errorMessage = String.format("User %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        if(uniqueUserName(ctx,name)){
            User user = new User().setUserID(userID)
                    .setUserName(name)
                    .setUserAttrList(attrList);

            String json = JSON.toJSONString(user);
            stub.putStringState(key, json);

            stub.setEvent("createUserEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
            return user;
        }else{
            String errorMessage = String.format("UserName %s already exists", name);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
    }

    @Transaction
    public Boolean uniqueUserName(final Context ctx,String name){
        String query = String.format("{\"selector\":{\"userName\":\"%s\"} , \"use_index\":[\"_design/indexUserIDDoc\", \"indexUserID\"]}", name);
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);

        return IterableUtils.isEmpty(queryResult);
    }

    @Transaction
    public String returnIDByKey(final Context ctx, final String key){
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(key);
        if (StringUtils.isBlank(userState)) {
            String errorMessage = String.format("User %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(userState,User.class).getUserID();
    }

    @Transaction
    public User uploadUserSK(final Context ctx, final String key , String userSK) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(key);

        User oldUser = JSON.parseObject(userState,User.class);
        String userID = oldUser.getUserID();
        String attrList = oldUser.getUserAttrList();

        User user = new User().setUserID(userID)
                .setUserAttrList(attrList)
                .setUserSK(userSK);

        stub.putStringState(key, JSON.toJSONString(user));

        return user;
    }

    @Transaction
    public String downloadUserSK(final Context ctx, final String key){
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(key);

        if (StringUtils.isBlank(userState)) {
            String errorMessage = String.format("User %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(userState,User.class).getUserSK();
    }

    @Transaction
    public String queryUserSKByUserID(final Context ctx, String userID){
        log.info(String.format("使用 userID 查询 user , name = %s" , userID));

        String query = String.format("{\"selector\":{\"userID\":\"%s\"} , \"use_index\":[\"_design/indexUserIDDoc\", \"indexUserID\"]}", userID);

        log.info(String.format("query string = %s" , query));

        ChaincodeStub stub = ctx.getStub();

        String key = queryUser(stub,query).getUsers().get(0).getKey();

        String userState = stub.getStringState(key);

        if (StringUtils.isBlank(userState)) {
            String errorMessage = String.format("User %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(userState,User.class).getUserSK();
    }

    private UserQueryResultList queryUser(ChaincodeStub stub,String query){

        UserQueryResultList resultList = new UserQueryResultList();

        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<UserQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new UserQueryResult().setKey(kv.getKey()).setUser(JSON.parseObject(kv.getStringValue() , User.class)));
            }
            resultList.setUsers(results);
        }else {
            String errorMessage = "User does not exist";
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return resultList;
    }

    @Transaction
    public String getUserIDByName(final Context ctx, String name){
        log.info(String.format("使用 userID 查询 user , name = %s" , name));

        String query = String.format("{\"selector\":{\"userName\":\"%s\"} , \"use_index\":[\"_design/indexUserIDDoc\", \"indexUserID\"]}", name);

        log.info(String.format("query string = %s" , query));

        return  queryUser(ctx.getStub(),query).getUsers().get(0).getUser().getUserID();
    }

    @Transaction
    public User queryUserByKey(final Context ctx, final String key){
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(key);

        if (StringUtils.isBlank(userState)) {
            String errorMessage = String.format("User %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }
        return JSON.parseObject(userState,User.class);
    }

    @Transaction
    public User queryUserByName(final Context ctx, String name){
        log.info(String.format("使用 userID 查询 user , name = %s" , name));

        String query = String.format("{\"selector\":{\"userName\":\"%s\"} , \"use_index\":[\"_design/indexUserIDDoc\", \"indexUserID\"]}", name);

        log.info(String.format("query string = %s" , query));

        return queryUser(ctx.getStub(),query).getUsers().get(0).getUser();
    }


    @Transaction
    public User queryUserByID(final Context ctx, String userID){

        log.info(String.format("使用 userID 查询 user , name = %s" , userID));

        String query = String.format("{\"selector\":{\"userID\":\"%s\"} , \"use_index\":[\"_design/indexUserIDDoc\", \"indexUserID\"]}", userID);

        log.info(String.format("query string = %s" , query));


        return  queryUser(ctx.getStub(),query).getUsers().get(0).getUser();
    }

    @Transaction
    public UserQueryPageResult queryUserPageByID(final Context ctx, String userID , Integer pageSize , String bookmark){
        log.info(String.format("使用 userID 分页查询 user , userID = %s" , userID));
        String query = String.format("{\"selector\":{\"userID\":\"%s\"} , \"use_index\":[\"_design/indexUserIDDoc\", \"indexUserID\"]}", userID);
        log.info(String.format("query string = %s" , query));

        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> queryResult = stub.getQueryResultWithPagination(query, pageSize, bookmark);

        List<UserQueryResult> users = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                users.add(new UserQueryResult().setKey(kv.getKey()).setUser(JSON.parseObject(kv.getStringValue() , User.class)));
            }
        }

        return new UserQueryPageResult()
                .setUsers(users)
                .setBookmark(queryResult.getMetadata().getBookmark());
    }

    @Transaction
    public User deleteUserByKey(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(key);

        if (StringUtils.isBlank(userState)) {
            String errorMessage = String.format("User %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(key);

        return JSON.parseObject(userState , User.class);
    }

    @Transaction
    public User deleteUserByName(final Context ctx, String name){
        log.info(String.format("使用 userID 查询 user , name = %s" , name));

        String query = String.format("{\"selector\":{\"userName\":\"%s\"} , \"use_index\":[\"_design/indexUserIDDoc\", \"indexUserID\"]}", name);

        log.info(String.format("query string = %s" , query));
        ChaincodeStub stub = ctx.getStub();
        String key = queryUser(stub,query).getUsers().get(0).getKey();
        String userState = stub.getStringState(key);
        stub.delState(key);

        return JSON.parseObject(userState,User.class);
    }

    @Transaction
    public CspProof createProof(final Context ctx, final String key , String chal, String proof, String sender) {
        ChaincodeStub stub = ctx.getStub();
        String proofState = stub.getStringState(key);

        if (StringUtils.isNotBlank(proofState)) {
            String errorMessage = String.format("CspProof %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        CspProof cspProof = new CspProof()
                .setChal(chal)
                .setProof(proof)
                .setSender(sender);

        String json = JSON.toJSONString(cspProof);
        stub.putStringState(key, json);

        stub.setEvent("createCspProofEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return cspProof;
    }

    @Transaction
    public CspProof queryProof(final Context ctx, final String key){
        ChaincodeStub stub = ctx.getStub();
        String proofState = stub.getStringState(key);

        if (StringUtils.isBlank(proofState)) {
            String errorMessage = String.format("Proof %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(proofState , CspProof.class);
    }

    @Transaction
    public CspProof queryProofByChal(final Context ctx, String chal){
        log.info(String.format("使用 chal 查询 proof , chal = %s" , chal));

        String query = String.format("{\"selector\":{\"chal\":\"%s\"} , \"use_index\":[\"_design/indexChalSenderDoc\", \"indexChalSender\"]}", chal);
        return queryProof(ctx.getStub(), query).getProofs().get(0).getProof();
    }

    @Transaction
    public CspProofQueryResultList queryProofBySender(final Context ctx, String sender){
        log.info(String.format("使用 sender 查询 proof , chal = %s" , sender));

        String query = String.format("{\"selector\":{\"sender\":\"%s\"} , \"use_index\":[\"_design/indexChalSenderDoc\", \"indexChalSender\"]}", sender);
        return queryProof(ctx.getStub(), query);
    }

    @Transaction
    public CspProofQueryResultList queryProofByChalAndSender(final Context ctx, String chal,String sender){
        log.info(String.format("使用 chal & sender 查询 proof , chal = %s , sender = %s" , chal , sender));

        String query = String.format("{\"selector\":{\"chal\":\"%s\" , \"sender\":\"%s\"} , \"use_index\":[\"_design/indexChalSenderDoc\", \"indexChalSender\"]}", chal , sender);
        return queryProof(ctx.getStub(), query);
    }

    private CspProofQueryResultList queryProof(ChaincodeStub stub,String query){
        CspProofQueryResultList resultList = new CspProofQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<CspProofQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new CspProofQueryResult().setKey(kv.getKey()).setProof(JSON.parseObject(kv.getStringValue() , CspProof.class)));
            }
            resultList.setProofs(results);
        }

        return resultList;
    }

    @Transaction
    public String generate_g_N(final Context ctx, String keySize) throws NoSuchAlgorithmException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(Integer.parseInt(keySize));
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        BigInteger p = privateKey.getModulus();
        BigInteger q = privateKey.getPrivateExponent();
        BigInteger g = q.modInverse(p); // 生成元g的计算：g = q^(-1) % p

        List<String> resList = Lists.newArrayList();
        resList.add(g.toString());
        resList.add(p.multiply(q).toString());

        return resList.toString();
    }

    @Transaction
    public String arbitration(final Context ctx,String chal,String randNum){
        Challenge challenge = queryChallengeByChal(ctx,chal);
        CspProof proof = queryProofByChal(ctx, chal);
        FileBlock fileBlock = queryFileBlockByFileID(ctx,challenge.getFilessID());
        List<BigInteger> hvts = fileBlock.getHvt();

        BigInteger g = new BigInteger(challenge.getG());
        BigInteger n = new BigInteger(challenge.getN());
        BigInteger r = new BigInteger(randNum);
        BigInteger challengeNum = new BigInteger(chal);
        BigInteger tagR = new BigInteger("1");

        if(!g.modPow(r,n).equals(challengeNum)){
            return "bad do";
        }else {
            for(int i = challenge.getStart();i< challenge.getEnd();i++){
                tagR = tagR.multiply(hvts.get(i+2)).mod(n);
            }
            tagR = tagR.modPow(r,n);
            if(SHA256(tagR.toString()).equals(SHA256(proof.getProof()))){
                return "bad"+challenge.getInitiator();
            }else {
                return proof.getSender();
            }
        }

    }
    private String SHA256(final String strText){
        String strResult = null;//加密结果
        //判断字符串是否有效
        if(strText != null && strText.length() > 0){
            try{
                // SHA 加密开始
                // 创建加密对象，传入加密类型
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                //传入加密字符串
                messageDigest.update(strText.getBytes());
                //得到byte数组
                byte[] buffer = messageDigest.digest();
                //将byte转为String字符串
                StringBuffer hexString = new StringBuffer();
                for(int i = 0; i < buffer.length;i++){//遍历数组
                    //转换成16进制字符串放入hexString
                    //这Integer.toHexString(int i)接受一个int参数并返回一个十六进制字符串。关键是将转换byte为int并使用0xff进行掩码以防止符号扩展。
                    String hex = Integer.toHexString(0xff & buffer[i]);
                    //如果byte转的16进制字符长度为1，需要在前面加上0
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                strResult = hexString.toString();
            }
            catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }
        }
        return strResult;
    }


    @Override
    public void beforeTransaction(Context ctx) {
        log.info("*************************************** beforeTransaction ***************************************");
    }

    @Override
    public void afterTransaction(Context ctx, Object result) {
        log.info("*************************************** afterTransaction ***************************************");
        System.out.println("result --------> " + result);
    }
}
