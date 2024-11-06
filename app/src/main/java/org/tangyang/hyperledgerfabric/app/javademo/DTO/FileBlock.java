package org.tangyang.hyperledgerfabric.app.javademo.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class FileBlock {

    String fileID;

    List<BigInteger> hvt;
}
