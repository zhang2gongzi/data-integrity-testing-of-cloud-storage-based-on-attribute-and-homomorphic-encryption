package org.tangyang.challenge;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;

@DataType
@Data
@Accessors(chain = true)
public class Challenge {
    @Property
    String g;

    @Property
    String n;

    @Property
    String challengeNumber;//挑战数

    @Property
    String initiator;//挑战发起者

    @Property
    String signature;

    @Property
    String randP;

    @Property
    String filessID;

    @Property
    List<String> fileList;

    @Property
    List<String> fileRandP;

    @Property
    Integer start;

    @Property
    Integer end;

}
