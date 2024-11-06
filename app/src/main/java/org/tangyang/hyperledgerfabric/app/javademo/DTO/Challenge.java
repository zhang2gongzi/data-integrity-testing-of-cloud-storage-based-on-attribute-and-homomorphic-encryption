package org.tangyang.hyperledgerfabric.app.javademo.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Challenge {
    String g;

    String n;

    String challengeNumber;//挑战数

    String initiator;//挑战发起者

    String signature;

    String randP;

    String filessID;

    List<String> fileList;

    List<String> fileRandP;

    Integer start;

    Integer end;
}
