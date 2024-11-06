package org.tangyang.hyperledgerfabric.app.javademo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.springframework.stereotype.Service;
import org.tangyang.hyperledgerfabric.app.javademo.config.HyperLedgerFabricProperties;

@Slf4j
@AllArgsConstructor
@Service
public class ProofService {
    final Gateway gateway;

    final Contract contract;

    final HyperLedgerFabricProperties hyperLedgerFabricProperties;
}
