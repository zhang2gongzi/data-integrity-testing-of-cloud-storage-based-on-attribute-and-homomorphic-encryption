package org.tangyang.challenge;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class ChallengeQueryResult {
    @Property
    String key;

    @Property
    Challenge challenge;
}
