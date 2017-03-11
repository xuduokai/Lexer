package com.xu.minimizedfa;

import com.xu.dfa.DFA;
import com.xu.newdfa.Dfa;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来表示分区
 * Created by xuduokai on 2017/3/9.
 */
public class DFAGroup {
    private static int GROUP_COUNT = 0;
    private int mGroupNumber;

    /*用来存放分区中的节点*/
    List<DFA> dfaGroup = new ArrayList<>();
    /*于分区做分割时，要把分割的节点从本分区删除，
    所以tobeRemove用来存储要被分割出去的节点*/
    List<DFA> tobeRemove = new ArrayList<>();

    private DFAGroup() {
        mGroupNumber = GROUP_COUNT;
    }

    public static DFAGroup createDFAGroup() {
        DFAGroup group = new DFAGroup();
        GROUP_COUNT++;
        return group;
    }

    public void add(DFA dfa) {
        dfaGroup.add(dfa);
    }

    public void tobeRemove(DFA dfa) {
        tobeRemove.add(dfa);
    }

    public void commitRemove() {
        for (DFA aTobeRemove : tobeRemove) {
            dfaGroup.remove(aTobeRemove);
        }
        tobeRemove.clear();
    }

    public boolean contains(DFA dfa) {
        return dfaGroup.contains(dfa);
    }

    public int size() {
        return dfaGroup.size();
    }

    public DFA get(int i) {
        if (i < dfaGroup.size()) {
            return dfaGroup.get(i);
        }

        return null;
    }

    public int groupNumber() {
        return mGroupNumber;
    }

    public void printGroup() {
        /*
         * 排序是为了调试演示方便，可以去掉，不影响逻辑
    	 */
        dfaGroup.sort((o1, o2) -> {
            if (o1.stateNum > o2.stateNum) {
                return 1;
            }

            return 0;
        });

        System.out.println("Dfa Group num: " + mGroupNumber + " it has dfa: ");
        for (DFA dfa : dfaGroup) {
            System.out.print(dfa.stateNum + " ");
        }

        System.out.print("\n");
    }
}
