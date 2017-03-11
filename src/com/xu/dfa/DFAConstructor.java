package com.xu.dfa;

import com.xu.newdfa.Dfa;
import com.xu.nfa.NFA;
import com.xu.nfa.NFAInterpreter;
import com.xu.nfa.NFAPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by xuduokai on 2017/3/7.
 */
public class DFAConstructor {
    private NFAPair nfaMachine = null;
    private NFAInterpreter nfaInterpreter = null;

    /*用来存储每一个生成的 DFA 节点，生成新节点时，先从该队列中取出原有节点
    * 看看要生成的节点是否已经存在*/
    private ArrayList<DFA> dfaList = new ArrayList<>();

    //假定 DFA 状态机节点数不会超过254个
    private static final int MAX_DFA_STATE_COUNT = 254;
    private static final int ASCII_COUNT = 128;
    private static final int STATE_FAILURE = -1;

    //使用二维数组表示 DFA 有限状态自动机
    private int[][] dfaStateTransformTable = new int[MAX_DFA_STATE_COUNT][ASCII_COUNT + 1];

    public DFAConstructor(NFAPair pair, NFAInterpreter nfaInterpreter) {
        this.nfaInterpreter = nfaInterpreter;

        this.nfaMachine = pair;

        initTransformTable();
    }

    private void initTransformTable() {
        for (int i = 0; i < MAX_DFA_STATE_COUNT; i++) {
            for (int j = 0; j <= ASCII_COUNT; j++) {
                dfaStateTransformTable[i][j] = STATE_FAILURE;
            }
        }
    }

    public int[][] converNFAToDFA() {
        Set<NFA> input = new HashSet<>();
        input.add(nfaMachine.startNode);
        //你将闭包构成的集合转成 DFA，而这个是没有难点的
        Set<NFA> nfaStartClosure = nfaInterpreter.e_closure(input);
        DFA start = DFA.getDFAFromNFASet(nfaStartClosure);
        dfaList.add(start);

        System.out.println("Create DFA start node: ");
        printDFA(start);

        int nextState;
        int currentDFAIndex = 0;

        while (currentDFAIndex < dfaList.size()) {
            DFA currentDFA = dfaList.get(currentDFAIndex);

            //我没有找专门的 char 集合，而是直接用 ASCII_COUNT 了
            for (char c = 0; c <= ASCII_COUNT; c++) {
                //从这个闭包中去 move，找他能达到的点，将这些点放入一个集合中
                Set<NFA> move = nfaInterpreter.move(currentDFA.nfaStates, c);

                if (move.isEmpty()) {
                    nextState = STATE_FAILURE;
                } else {
                    Set<NFA> closure = nfaInterpreter.e_closure(move);
                    DFA dfa = isNFAStatesExistInDFA(closure);

                    if (dfa == null) {
                        System.out.println("Create DFA node: ");
                        DFA newDFA = DFA.getDFAFromNFASet(closure);
                        printDFA(newDFA);

                        dfaList.add(newDFA);
                        nextState = newDFA.stateNum;
                    } else {
                        System.out.println("Get a existed DFA node: ");
                        printDFA(dfa);
                        nextState = dfa.stateNum;
                    }
                }

                if (nextState != STATE_FAILURE) {
                    System.out.println("DFA form state: " + currentDFA.stateNum + " to state:" + nextState + " on char:" + c);
                }
                //表里的值是记录下一次的转移。
                dfaStateTransformTable[currentDFA.stateNum][c] = nextState;
            }
            System.out.println("\n");
            currentDFAIndex++;
        }
        return dfaStateTransformTable;
    }

    public ArrayList<DFA>  getDFAList() {
        return dfaList;
    }

    public int[][] getDFATransTable() {
        return dfaStateTransformTable;
    }

    private void printDFA(DFA dfa) {
        System.out.print("Dfa state: " + dfa.stateNum + " its nfa states are: ");
        Iterator<NFA> it = dfa.nfaStates.iterator();
        while (it.hasNext()) {
            System.out.print(it.next().getStateNum());
            if (it.hasNext()) {
                System.out.print(",");
            }
        }

        System.out.print("\n");
    }

    private DFA isNFAStatesExistInDFA(Set<NFA> closure) {
        for (DFA dfa : dfaList) {
            if (dfa.hasNFAStates(closure)) {
                return dfa;
            }
        }

        return null;
    }

    public void printDFA() {
        int dfaNum = dfaList.size();
        for (int i = 0; i < dfaNum; i++)
            for (int j = 0; j < dfaNum; j++) {
                if (isOnNumberClass(i, j)) {
                    System.out.println("From state " + i + " to state " + j + " on D");
                }

                if (isOnDot(i, j)) {
                    System.out.println("From state " + i + " to state " + j + " on dot");
                }
            }
    }


    private boolean isOnNumberClass(int from, int to) {
        char c;
        for (c = '0'; c <= '9'; c++) {
            if (dfaStateTransformTable[from][c] != to) {
                return false;
            }
        }

        return true;
    }

    private boolean isOnDot(int from, int to) {
        return dfaStateTransformTable[from]['.'] == to;
    }
}
