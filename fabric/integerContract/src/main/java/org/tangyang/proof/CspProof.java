package org.tangyang.proof;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class CspProof {

    @Property
    String chal;//挑战数

    @Property
    String proof;//挑战证明

    @Property
    String sender;//生成挑战证明的云服务商
}
