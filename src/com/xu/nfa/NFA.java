package com.xu.nfa;

import java.util.HashSet;
import java.util.Set;

/**
 * 每个节点有最多有两个输入边和一个输出边
 * Created by xuduokai on 2017/3/1.
 */
public class NFA {
    public static final int EPSILON = -1;   //对应的边是ε
    public static final int CHAR = -2;       //输入边是字符集
    public static final int ACCEPT = -3;     //没有出去的边
    public static final int ASCII_COUNT = 127;


    private int stateNum;
    //目前看书，可以是 e 或者是字符，或者为空，标识是接受状态
    private int edge;//记录转换边对应的输入，输入可以是空，E，字符集（CHAR），或空，也就是没有出去的边。
    //两条出去的边
    public NFA next;
    public NFA next2;

    /**
     * 用来存储字符集类。
     * 因为我们要处理[0-9]或者[abcd]或者[a-z]这样的字符集
     * 他们本质上是(a|b|c|d)这样的形式，但是如果我们都转换成选择操作，那么太占用资源了
     * 所以选择同一个集合来存储这些字符集。
     * 当字符属于 inputSet 时，就进行转移。
     * 并且记录^取反操作时也很简单了。
     */
    public Set<Byte> inputSet;

    private boolean visited = false; //节点是否被访问过，用于节点打印

    public void setStateNum(int stateNum) {
        this.stateNum = stateNum;
    }

    public int getStateNum() {
        return stateNum;
    }

    public int getEdge() {
        return edge;
    }

    public void setEdge(int edge) {
        this.edge = edge;
    }

    public void addToSet(Byte b) {
        inputSet.add(b);
    }

    //取反
    public void setComplement() {
        Set<Byte> newSet = new HashSet<>();
        for (byte b = 0; b < ASCII_COUNT; b++) {
            if (!inputSet.contains(b)) {
                newSet.add(b);
            }
        }
        inputSet = null;
        inputSet = newSet;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }


    public void clearState() {
        inputSet.clear();
        next = next2 = null;
        stateNum = -1;
    }

    public NFA() {
        inputSet = new HashSet<>();

    }

}
