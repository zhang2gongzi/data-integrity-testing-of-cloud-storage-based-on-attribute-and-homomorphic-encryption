package org.tangyang.hyperledgerfabric.app.javademo.DTO;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Proof {
    String chal;//挑战数

    String proof;//挑战证明

    String sender;//生成挑战证明的云服务商

    String signature;

    String sigma;
}
