package org.hepeng.hyperledgerfabric.app.javademo;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.ChaincodeEvent;
import org.hyperledger.fabric.client.CloseableIterator;
import org.hyperledger.fabric.client.Network;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;

/**
 * @author he peng
 * @date 2022/3/4
 */

@Slf4j
@ActiveProfiles("prod-network-org1")
public class ChaincodeEventTests extends HyperledgerFabricAppJavaDemoApplicationTests {

    @Test
    public void test0() {


    }

}
