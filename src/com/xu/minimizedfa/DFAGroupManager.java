package com.xu.minimizedfa;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来对分区进行管理
 * dfaGroupManager用来对分区进行管理，
 * groupList存储了所以分区，
 * 当有节点分割需要新分区时，
 * 调用它的createNewGroup来生成新的分区，
 * getContainingGroup用来获取给定的DFA节点所在的分区。
 * Created by xuduokai on 2017/3/9.
 */
public class DFAGroupManager {
    private List<DFAGroup> groupList = new ArrayList<>();

    public DFAGroup createNewGroup() {
        DFAGroup group = DFAGroup.createDFAGroup();
        groupList.add(group);
        return group;
    }

    public DFAGroup getContainGroup(int dfaStateNumber) {
        for (DFAGroup group : groupList) {
            if (groupContainsDFA(group, dfaStateNumber)) {
                return group;
            }
        }
        return null;
    }

    private boolean groupContainsDFA(DFAGroup group, int dfaStateNumber) {
        for (int i = 0; i < group.size(); i++) {
            if (group.get(i).stateNum == dfaStateNumber) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return groupList.size();
    }

    public DFAGroup get(int n) {
        if (n < groupList.size()) {
            return groupList.get(n);
        }

        return null;
    }
}
