package org.tangyang.hyperledgerfabric.app.javademo.cpabe;

import lombok.Data;

import java.util.Arrays;
import java.util.Locale;

@Data
public class PolicyNode {
    public int[] gate;
    // children表示内部节点，此字段为子节点索引列表
    // 如果是叶子节点，则为null
    public int[] children;
    // att表示属性值，全部用小写形式表示
    // 如果是内部节点，此字段null
    public String att;

    // 用于秘密恢复，表示此节点是否可以恢复
    public boolean valid;
    //内部节点的构造方法
    public PolicyNode(int[] gate, int[] children){
        this.gate = gate;
        this.children = children;
    }
    // 叶子节点的构造方法
    public PolicyNode(String att){
        this.att = att.toLowerCase(Locale.ROOT);
    }
    public boolean isLeaf() {
        return this.children==null;
    }
    @Override
    public String toString() {
        if (this.isLeaf()){
            return "this is an attribute: " + att;
        }
        else {
            return "this is a gate " + Arrays.toString(this.gate) + " with children " + Arrays.toString(this.children);
        }
    }
}
