package org.tangyang.file;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class FileQueryResult {
    @Property
    String key;

    @Property
    HvtFile file;
}
