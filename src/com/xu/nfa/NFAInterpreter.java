package com.xu.nfa;

import com.xu.input.system.Input;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * Created by xuduokai on 2017/3/7.
 */
public class NFAInterpreter {

    private NFA start;
    private Input input;

    public NFAInterpreter(NFA start, Input input) {
        this.start = start;
        this.input = input;
    }

    /**
     * 计算 input 集合中 NFA 节点所对应的 e 闭包，
     * 并将闭包的节点加入到 input 中
     *
     * @param input 一个 NFA 对象集合，是 e闭包运算的输入集合。
     * @return 是输入集合的节点通过 e 边所能抵达的所有状态节点的集合
     */
    public Set<NFA> e_closure(Set<NFA> input) {
        /*
        * 就是图搜索，从给定节点开始，记录所有给定要求的可达节点
        * p.next 相当于图的边
        * 所以我们是广度搜索*/

        System.out.println("e-Closure(" + stringFromNFASet(input) + ") = ");

        Stack<NFA> nfaStack = new Stack<>();
        if ((input == null || input.isEmpty())) {
            return null;
        }

        for (NFA anInput : input) {
            nfaStack.add(anInput);
        }

        while (!nfaStack.empty()) {
            NFA p = nfaStack.pop();

            if (p.next != null && p.getEdge() == NFA.EPSILON) {
                if (!input.contains(p.next)) {
                    nfaStack.push(p.next);
                    input.add(p.next);
                }
            }

            if (p.next2 != null && p.getEdge() == NFA.EPSILON) {
                if (!input.contains(p.next2)) {
                    nfaStack.push(p.next2);
                    input.add(p.next2);
                }
            }
        }

        System.out.println("{" + stringFromNFASet(input) + '}');

        return input;
    }

    public Set<NFA> move(Set<NFA> input, char c) {
        Set<NFA> outSet = new HashSet<>();

        for (NFA p : input) {
            //如果当前节点的边是字符，就转移
            if (p.getEdge() == c || (p.getEdge() == NFA.CHAR && p.inputSet.contains((byte) c))) {
                outSet.add(p.next);
            }
        }

        System.out.print("move({ " + stringFromNFASet(input) + " }, '" + c + "')=");
        System.out.println("{ " + stringFromNFASet(outSet) + " }");

        return outSet;
    }

    private String stringFromNFASet(Set<NFA> input) {

        StringBuilder s = new StringBuilder();
        Iterator<NFA> it = input.iterator();
        while (it.hasNext()) {
            s.append(it.next().getStateNum());

            if (it.hasNext()) {
                s.append(",");
            }
        }
        return s.toString();
    }

    public void interpretNFA() {
        //从控制台读入要解读的字符串
        System.out.println("Input string: ");
        input.ii_newFile(null);
        input.ii_advance();
        input.ii_pushback(1);

        Set<NFA> next = new HashSet<>();
        next.add(start);
        e_closure(next);    //产生了教程中的第一个闭包集合

        Set<NFA> current;
        char c;
        String inputStr = "";
        boolean lastAccepted = false;

        while ((c = (char) input.ii_advance()) != Input.EOF) {
            current = move(next, c);
            next = e_closure(current);

            if (next != null) {
                if (hasAcceptState(next)) {
                    lastAccepted = true;
                }
            } else {
                /*next 为空的情况就是没有 e—边，只能通过字符转移或者是接受状态*/
                break;
            }

            inputStr += c;
        }

        if (lastAccepted) {
            System.out.println("The Nfa Machine can recognize string: " + inputStr);
        }

    }

    /*接受的条件：
    * 两条变都为 null，也就是没有出去的边。*/
    private boolean hasAcceptState(Set<NFA> input) {
        boolean isAccepted = false;
        if (input == null || input.isEmpty()) {
            return false;
        }

        String acceptedStatement = "Accept State: ";
        for (NFA p : input) {
            if (p.next == null && p.next2 == null) {
                isAccepted = true;
                acceptedStatement += p.getStateNum() + " ";
            }
        }

        if (isAccepted) {
            System.out.println(acceptedStatement);
        }

        return isAccepted;
    }
}
