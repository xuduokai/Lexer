package com.xu.minimizedfa;

import com.xu.dfa.DFA;
import com.xu.dfa.DFAConstructor;
import com.xu.newdfa.Dfa;
import com.xu.newminimizedfa.DfaGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * dfaList是最小化前DFA节点被存储的队列，
 * dfaTransTable 是前几节我们构造的DFA转移表，
 * minDfa 是最小化算法执行后所生成的最小化后的DFA转移表。
 * Created by xuduokai on 2017/3/9.
 */
public class MinimizeDFA {
    private DFAConstructor dfaConstructor;
    private DFAGroupManager groupManager = new DFAGroupManager();
    private static final int ASCII_NUM = 128;
    private int[][] dfaTransTable;
    int[][] minDFA;
    private List<DFA> dfaList;
    private DFAGroup newGroup;

    private boolean addNewGroup;
    private static final int STATE_FAILURE = -1;

    private ArrayList<Character> charSet;

    public MinimizeDFA(DFAConstructor constructor, ArrayList<Character> charSet) {
        dfaConstructor = constructor;
        dfaList = dfaConstructor.getDFAList();
        dfaTransTable = dfaConstructor.getDFATransTable();

        this.charSet = charSet;
    }

    public int[][] minimize() {
        /*
        * 生成两个分区，分别将非接受状态节点和接受状态节点放入两个分区
        * */
        addNoAcceptDFAToGroup();
        addAcceptDFAToGroup();

        /*根据输入对每个分区进行分割，一旦有新的分区生成，那么就必须对所有分区再进行分割
        * 因为新分区产生后，原来不可分割的分区就看可以分割了*/
        //为什么要区分数字和字符
        do {
            doGroupSeparation();
        } while (addNewGroup);

        printMiniDfaTable();
        createMiniDFATransTable();

        return minDFA;
    }

    private void createMiniDFATransTable() {
        /*把点与点的跳转关系转换为分区与分区的转移关系*/
        initMiniDFATransTable();
        for (DFA dfa : dfaList) {
            int from = dfa.stateNum;
            for (Character c : charSet) {
                if (dfaTransTable[from][c] != STATE_FAILURE) {
                    int to = dfaTransTable[from][c];
                    /*
                     * 找到两个节点对应的分区
    				 */
                    DFAGroup fromGroup = groupManager.getContainGroup(from);
                    DFAGroup toGroup = groupManager.getContainGroup(to);
                    /*
                     * 把点和点的跳转关系转变为分区间的跳转关系
    				 */
                    minDFA[fromGroup.groupNumber()][charSet.indexOf(c)] = toGroup.groupNumber();
                }
            }
        }
    }

    private void initMiniDFATransTable() {
        minDFA = new int[groupManager.size()][ASCII_NUM];
        for (int i = 0; i < groupManager.size(); i++)
            for (int j = 0; j < ASCII_NUM; j++) {
                minDFA[i][j] = STATE_FAILURE;
            }
    }

    private void doGroupSeparation() {
        for (int i = 0; i < groupManager.size(); i++) {
            int dfaCount = 1;
            newGroup = null;
            DFAGroup group = groupManager.get(i);

            DFA first = group.get(0);
            DFA next = group.get(dfaCount);

            //当分区包含不止一个节点时，next 不会只空
            while (next != null) {
                for (char c = 0; c < ASCII_NUM; c++) {
                    if (doGroupSeparationOnInput(group, first, next, c)) {
                        addNewGroup = true;
                        break;
                    }
                }

                dfaCount++;
                next = group.get(dfaCount);
            }
            group.commitRemove();
        }
    }

    private boolean doGroupSeparationOnInput(DFAGroup group, DFA first, DFA next, char c) {
        /*如果两个 DFA 节点跳转后的节点不子啊同一个分区，那么第二个节点必须从当前分区分割出去*/
        //这个时候他们是通过字符 c 进行转移的。
        int gotoFirst = dfaTransTable[first.stateNum][c];
        int gotoNext = dfaTransTable[next.stateNum][c];

        if (groupManager.getContainGroup(gotoFirst) !=
                groupManager.getContainGroup(gotoNext)) {
            if (newGroup == null) {
                newGroup = groupManager.createNewGroup();
            }
            group.tobeRemove(next);
            newGroup.add(next);
            System.out.println("Dfa:" + first.stateNum + " and Dfa:" +
                    next.stateNum + " jump to different group on input char " + c);

            System.out.println("remove Dfa:" + next.stateNum + " from group:" + group.groupNumber()
                    + " and add it to group:" + newGroup.groupNumber());
            return true;
        }
        return false;
    }

    private void addAcceptDFAToGroup() {
        DFAGroup group = groupManager.createNewGroup();

        for (DFA dfa : dfaList) {
            if (dfa.accepted) {
                group.add(dfa);
            }
        }

        group.printGroup();

    }

    private void addNoAcceptDFAToGroup() {
        DFAGroup group = groupManager.createNewGroup();

        for (DFA dfa : dfaList) {
            if (!dfa.accepted) {
                group.add(dfa);
            }
        }

        group.printGroup();

    }

    private void printMiniDfaTable() {
        for (int i = 0; i < groupManager.size(); i++)
            for (int j = 0; j < groupManager.size(); j++) {
                if (isOnNumberClass(i, j)) {
                    System.out.println("from " + i + " to " + j + " on D");
                }
                if (isOnDot(i, j)) {
                    System.out.println("from " + i + " to " + j + " on .");
                }
            }
    }

    private boolean isOnNumberClass(int from, int to) {
        char c;
        for (c = '0'; c <= '9'; c++) {
            if (minDFA[from][c] != to) {
                return false;
            }
        }

        return true;
    }

    private boolean isOnDot(int from, int to) {
        return minDFA[from]['.'] == to;
    }

/*
    private void runMinimizeDFAExample() {
        miniDfa = new com.xu.newminimizedfa.MinimizeDFA(dfaConstructor, charSet);
        int[][] mini = miniDfa.minimize();
        for (Character c : charSet) {
            System.out.print(" " + c + " ");
        }

        System.out.println();

        for (int[] aMini : mini) {
            for (int j = 0; j < aMini.length; j++) {
                if (aMini[j] == -1) {
                    System.out.print(aMini[j] + " ");
                } else {
                    System.out.print(" " + aMini[j] + " ");
                }
                if (j == aMini.length - 1) {
                    System.out.println();
                }
            }
        }
    }
*/
}
