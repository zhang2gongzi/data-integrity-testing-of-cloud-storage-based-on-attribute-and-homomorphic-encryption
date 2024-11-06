package org.tangyang.fileBlock;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;

@DataType
@Data
public class FileBlockQueryResultList {
    @Property
    List<FileBlockQueryResult> fileBlocks;
}
