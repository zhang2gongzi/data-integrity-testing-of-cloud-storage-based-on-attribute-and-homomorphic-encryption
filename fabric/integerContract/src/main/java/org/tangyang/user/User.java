package org.tangyang.user;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

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
}
