package org.tangyang.fileBlock;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.math.BigInteger;
import java.util.List;

@DataType
@Data
@Accessors(chain = true)
public class FileBlock {
    @Property
    String fileID;//属于哪一个文件

    @Property
    List<BigInteger> hvt;//数据块同态标签集合
}
