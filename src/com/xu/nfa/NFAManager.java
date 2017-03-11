package com.xu.nfa;

import com.xu.input.ErrorHandler;

import java.util.Stack;

/**
 * 负责 NFA 节点的构造和回收
 * Created by xuduokai on 2017/3/1.
 */
public class NFAManager {
    private final int NFA_MAX = 256;    //最多运行分配256个 NFA 节点。
    private NFA[] mNFAStateArray = null;
    private Stack<NFA> mNFAStack = null;
    private int mNextAlloc = 0; //nfa数组下标
    private int mNFAStates = 0; //分配的nfa编号

    public NFAManager() throws Exception {
        mNFAStateArray = new NFA[NFA_MAX];
        for (int i = 0; i < NFA_MAX; i++) {
            mNFAStateArray[i] = new NFA();
        }

        mNFAStack = new Stack<>();

        if (mNFAStateArray == null) {
            ErrorHandler.parseErr(ErrorHandler.Error.E_MEM);
        }
    }

    public NFA newNFA() throws Exception {

        if (++mNFAStates >= NFA_MAX) {
            ErrorHandler.parseErr(ErrorHandler.Error.E_LENGTH);
        }

        NFA nfa;

        if (mNFAStack.size() > 0) {
            nfa = mNFAStack.pop();
        } else {
            nfa = mNFAStateArray[mNextAlloc];
            mNextAlloc++;
        }

        nfa.clearState();
        nfa.setStateNum(mNFAStates);
        nfa.setEdge(NFA.EPSILON);

        return nfa;
    }

    public void discardNFA(NFA discard) {
        --mNFAStates;
        discard.clearState();
        mNFAStack.push(discard);
    }
}
