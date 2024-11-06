package org.tangyang.treeNode;

import lombok.Data;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Locale;
@DataType
@Data
public class TreeNode {
    @Property
    public int[] gate;

    @Property
    public int[] children;

    @Property
    public String att;

    @Property
    public boolean valid = false;

    public TreeNode(int[] gate, int[] children){
        this.gate = gate;
        this.children = children;
    }

    public TreeNode(String att){
        this.att = att.toLowerCase(Locale.ROOT);
    }
}
