package com.xu.dfa;

import com.xu.nfa.NFA;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xuduokai on 2017/3/7.
 */
public class DFA {
    //用来标记 DFA 状态节点编号，每生成一个 DFA 节点，该编号就自动加一
    private static int STATE_NUM = 0;
    public int stateNum = 0;
    /*
    * 记录 DFA 节点对应的 NFA 闭包集合*/
    Set<NFA> nfaStates = new HashSet<>();
    public boolean accepted = false;

    public static DFA getDFAFromNFASet(Set<NFA> input) {
        DFA dfa = new DFA();

        for (NFA nfa : input) {
            dfa.nfaStates.add(nfa);
            if (nfa.next == null && nfa.next2 == null) {
                //终结节点？
                dfa.accepted = true;
            }
        }

        dfa.stateNum = STATE_NUM;
        STATE_NUM++;
        return dfa;
    }

    /**
     * 用来判断传入的Nfa集合是否与节点对应的Nfa集合相同，
     * 大家还记得上一节，每次构造转移集合的ε闭包时，
     * 都要到队列里查看有没有该闭包对应的Dfa节点存在吧，
     * 这个函数就用来做闭包将的判断。
     * @param set
     * @return
     */
    public boolean hasNFAStates(Set<NFA> set){
        return this.nfaStates.equals(set);
    }
}
