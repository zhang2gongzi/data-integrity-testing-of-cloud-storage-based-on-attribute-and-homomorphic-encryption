package org.tangyang.user;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;

@DataType
@Data
@Accessors(chain = true)
public class User {
    @Property
    String userName;

    @Property
    String userID;

    @Property
    String userAttrList;

    @Property
    String userSK;

    @Property
    List<String> skKey;

    @Property
    List<String> skValue;

    @Property
    String elementT;

    @Property
    boolean valid = false;
}
