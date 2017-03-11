package com.xu.newnfa;

import java.util.HashSet;
import java.util.Set;

public class Nfa {

    public static final int EPSILON = -1; //边对应的是ε
    public static final int CCL = -2; //边对应的是字符集
    public static final int EMPTY = -3; //该节点没有出去的边
    private static final int ASCII_COUNT = 127;

    private int edge; //记录转换边对应的输入，输入可以是空, ε，字符集(CCL),或空，也就是没有出去的边

    public int getEdge() {
        return edge;
    }

    public void setEdge(int type) {
        edge = type;
    }

    public Set<Byte> inputSet; //用来存储字符集类
    public Nfa next;  //跳转的下一个状态，可以是空
    public Nfa next2; //跳转的另一个状态，当状态含有两条ε边时，这个指针才有效
    private int stateNum; //节点编号
    private boolean visited = false; //节点是否被访问过，用于节点打印

    public void setVisited() {
        visited = true;
    }

    public boolean isVisited() {
        return visited;
    }


    public Nfa() {
        inputSet = new HashSet<>();
        clearState();
    }

    public void setStateNum(int num) {
        stateNum = num;
    }

    public int getStateNum() {
        return stateNum;
    }

    public void addToSet(Byte b) {
        inputSet.add(b);
    }

    public void setComplement() {
        Set<Byte> newSet = new HashSet<Byte>();

        for (byte b = 0; b < ASCII_COUNT; b++) {
            if (!inputSet.contains(b)) {
                newSet.add(b);
            }
        }

        inputSet = null;
        inputSet = newSet;
    }


    public void clearState() {
        inputSet.clear();
        next = next2 = null;
        stateNum = -1;
    }
}
