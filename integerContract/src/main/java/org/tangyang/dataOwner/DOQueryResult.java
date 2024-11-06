package org.tangyang.dataOwner;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class DOQueryResult {
    @Property
    String key;

    @Property
    DataOwner dataOwner;
}
