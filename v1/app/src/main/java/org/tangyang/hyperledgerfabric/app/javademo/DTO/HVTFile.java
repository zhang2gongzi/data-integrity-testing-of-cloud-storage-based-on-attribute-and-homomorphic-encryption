package org.tangyang.hyperledgerfabric.app.javademo.DTO;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HVTFile {

    String filesID;

    String g;

    String n;

    String count;
}
