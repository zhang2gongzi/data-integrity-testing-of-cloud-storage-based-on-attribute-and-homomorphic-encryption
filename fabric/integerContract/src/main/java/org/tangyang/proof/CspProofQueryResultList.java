package org.tangyang.proof;

import lombok.Data;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;

@DataType
@Data
public class CspProofQueryResultList {

    @Property
    List<CspProofQueryResult> proofs;
}
