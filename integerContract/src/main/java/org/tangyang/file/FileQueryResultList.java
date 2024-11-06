package org.tangyang.file;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;

@DataType
@Data
public class FileQueryResultList {
    @Property
    List<FileQueryResult> files;
}
