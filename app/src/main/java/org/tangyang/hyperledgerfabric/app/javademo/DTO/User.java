package org.tangyang.hyperledgerfabric.app.javademo.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class User {

    String userName;

    String userID;

    String userAttrList;

    String userSK;

    List<String> skKey;

    List<String> skValue;

    String elementT;

    boolean valid = false;
}
