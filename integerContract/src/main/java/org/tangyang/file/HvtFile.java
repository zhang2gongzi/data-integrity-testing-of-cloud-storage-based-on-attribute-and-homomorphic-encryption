package org.tangyang.file;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
@Data
@Accessors(chain = true)
public class HvtFile {
    @Property
    String filesID;

    @Property
    String g;

    @Property
    String n;

    @Property
    String count;
}
