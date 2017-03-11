package com.xu.nfa;

import com.xu.input.ErrorHandler;
import com.xu.input.Lexer;

import java.util.Set;

/**
 * 因为优先级关系是：
 * 选择<连接<闭包<括号
 * 所以最先被调用的就是选择操作
 * Created by xuduokai on 2017/3/1.
 */
public class NFAMachineConstructor {
    private Lexer lexer;
    private NFAManager nfaManager;

    public NFAMachineConstructor(Lexer lexer) throws Exception {
        this.lexer = lexer;
        nfaManager = new NFAManager();

        while (lexer.MatchToken(Lexer.Token.EOS)) {
            lexer.advance();
        }
    }

    /* 选择：| 连接符
    * a|b */
    public void expr(NFAPair pairOut) throws Exception {
        //先处理 a，将其变为一个 pair;
        cat_expr(pairOut);
        NFAPair localPair = new NFAPair();

        //如果下一个字符是|
        while (lexer.MatchToken(Lexer.Token.OR)) {
            lexer.advance();
            //处理 b
            cat_expr(localPair);

            //开始构造连接的 NFA
            NFA startNode = nfaManager.newNFA();
            startNode.next = pairOut.startNode;
            startNode.next2 = localPair.startNode;
            pairOut.startNode = startNode;

            NFA endNode = nfaManager.newNFA();
            pairOut.endNode.next = endNode;
            localPair.endNode.next = endNode;
            pairOut.endNode = endNode;
        }
    }

    /* 连接：ab */
    public void cat_expr(NFAPair pairOut) throws Exception {
        //先处理 a，将其变为一个 pair;
        if (first_in_cat(lexer.getCurrentToken())) {
            factor(pairOut);
        }

        //再处理 b 以及有可能的 c 之类的剩余的符号
        while (first_in_cat(lexer.getCurrentToken())) {
            NFAPair pairLocal = new NFAPair();
            factor(pairLocal);

            pairOut.endNode.next = pairLocal.startNode;
            pairOut.endNode = pairLocal.endNode;
        }
    }

    /* 闭包：a* */
    private void factor(NFAPair pairOut) throws Exception {

        term(pairOut);

        boolean handled = matchStarClosure(pairOut);
        if (!handled) {
            handled = matchPlusClosure(pairOut);
        }

        if (!handled) {
            handled = matchOptionsClosure(pairOut);
        }
    }

    /*
    * 就是按照图去连边，因为节点的定义我们已经给了。
    * 注意：连完以后要讲 start 和 end 设置一下*/
    private boolean matchStarClosure(NFAPair pairOut) throws Exception {
        /*
         * term*
    	 */
        NFA start, end;
        //term(pairOut);

        //如果不是*，就返回
        if (!lexer.MatchToken(Lexer.Token.CLOSURE)) {
            return false;
        }

        start = nfaManager.newNFA();
        end = nfaManager.newNFA();

        start.next = pairOut.startNode;
        pairOut.endNode.next = end;

        start.next2 = end;
        pairOut.endNode.next2 = pairOut.startNode;

        pairOut.startNode = start;
        pairOut.endNode = end;

        lexer.advance();
        return true;
    }

    private boolean matchPlusClosure(NFAPair pairOut) throws Exception {
        /*
         * term+
    	 */
        NFA start, end;
        //term(pairOut);

        //如果不是+，就返回
        if (!lexer.MatchToken(Lexer.Token.PLUS_CLOSE)) {
            return false;
        }

        start = nfaManager.newNFA();
        end = nfaManager.newNFA();

        start.next = pairOut.startNode;
        pairOut.endNode.next = end;

        pairOut.endNode.next2 = pairOut.startNode;

        pairOut.startNode = start;
        pairOut.endNode = end;

        lexer.advance();
        return true;
    }

    private boolean matchOptionsClosure(NFAPair pairOut) throws Exception {
        /*
         * term?
         */
        NFA start, end;
        //term(pairOut);

        //如果不是?，就返回
        if (!lexer.MatchToken(Lexer.Token.OPTIONAL)) {
            return false;
        }

        start = nfaManager.newNFA();
        end = nfaManager.newNFA();

        start.next = pairOut.startNode;
        pairOut.endNode.next = end;

        start.next2 = end;

        pairOut.startNode = start;
        pairOut.endNode = end;

        lexer.advance();
        return true;
    }

    /**
     * term ->  char
     * | [...]
     * | [^...]
     * | [char-char]
     * | .
     * | (expr)
     *
     * @param pairOut
     */
    private void term(NFAPair pairOut) throws Exception {
        boolean handled = matchExprInParen(pairOut);
        if (!handled) {
            handled = matchSingleCharacter(pairOut);
        }
        if (!handled) {
            handled = matchDot(pairOut);
        }

        if (!handled) {
            matcherCharacterSet(pairOut);
        }
    }

    //处理带括号的情况
    private boolean matchExprInParen(NFAPair pairOut) throws Exception {
        if (lexer.MatchToken(Lexer.Token.OPEN_PAREN)) {
            lexer.advance();
            expr(pairOut);
            if (lexer.MatchToken(Lexer.Token.CLOSE_PAREN)) {
                lexer.advance();
            } else {
                ErrorHandler.parseErr(ErrorHandler.Error.E_PAREN);
            }

            return true;
        }

        return false;
    }

    /**
     * 匹配单个字符，如 a、b、c之类的。
     */
    private boolean matchSingleCharacter(NFAPair pairOut) throws Exception {
        if (!lexer.MatchToken((Lexer.Token.L))) {
            return false;
        }
        pairOut.startNode = nfaManager.newNFA();
        pairOut.endNode = nfaManager.newNFA();
        pairOut.startNode.next = pairOut.endNode;
        pairOut.startNode.setEdge(lexer.getLexeme());
        lexer.advance();
        return true;
    }

    /**
     * 匹配 .
     */
    private boolean matchDot(NFAPair pairOut) throws Exception {
        if (!lexer.MatchToken((Lexer.Token.ANY))) {
            return false;
        }

        NFA start;
        start = pairOut.startNode = nfaManager.newNFA();
        pairOut.endNode = pairOut.startNode.next = nfaManager.newNFA();
        start.setEdge((NFA.CHAR));
        start.addToSet((byte) '\n');
        start.addToSet((byte) '\r');
        start.setComplement();

        lexer.advance();

        return true;
    }

    /**
     * 匹配 [adu]或者[0-9]
     */
    private boolean matcherCharacterSet(NFAPair pairOut) throws Exception {
        if (!lexer.MatchToken((Lexer.Token.CCL_START))) {
            return false;
        }

        lexer.advance();

        boolean negative = false;
        if (lexer.MatchToken((Lexer.Token.AT_BOL))) {
            negative = true;
        }

        NFA start;
        start = pairOut.startNode = nfaManager.newNFA();
        pairOut.endNode = pairOut.startNode.next = nfaManager.newNFA();
        start.setEdge(NFA.CHAR);
        if (!lexer.MatchToken((Lexer.Token.CCL_END))) {
            doDash(start.inputSet);
        }

        if (!lexer.MatchToken((Lexer.Token.CCL_END))) {
            ErrorHandler.parseErr(ErrorHandler.Error.E_BADEXPR);
        }

        //取反
        if (negative) {
            start.setComplement();
        }

        lexer.advance();

        return true;
    }

    /**
     * 将字符集类对应的字符放入到 NFA 节点的 inputSet 中
     * 字符集分为两种情况：
     * 1、[abcd]
     * 2、[0-9]
     *
     * @param set
     */
    private void doDash(Set<Byte> set) {
        int first = 0;
        while (!lexer.MatchToken(Lexer.Token.EOS)
                && !lexer.MatchToken(Lexer.Token.CCL_END)) {

            if (lexer.MatchToken(Lexer.Token.DASH)) {
                lexer.advance();//越过 -
                for (; first <= lexer.getLexeme(); first++) {
                    set.add((byte) first);
                }
            } else {
                first = lexer.getLexeme();
                set.add((byte) first);
            }
            lexer.advance();
        }
    }

    /**
     * 判断输入的正则表达式是否合法。
     * 如果表达式以 ], ) ,*, 等符号出现在开头，那么输入的表达式就是错误的，
     * 错误的表达式也就没有构建的必要,
     * 如果表达式以EOS开始，也就是表达式解析结束了，
     * 这意味factor再也构建不了新的nfa状态机，
     * 那么连接操作就不需要再进行了
     *
     * @param tok 当前的字符
     * @return 是否是一个合法的表达式
     * @throws Exception
     */
    private boolean first_in_cat(Lexer.Token tok) throws Exception {
        switch (tok) {
            case CLOSE_PAREN:
            case AT_EOL:
            case EOS:
                //正确的表达式不会以) $ 开头，如果遇到 EOS 表示正则表达式解析完毕，那么就不应该执行该函数
                return false;
            case CLOSURE:
            case PLUS_CLOSE:
            case OPTIONAL:
                //*,+,? 这几个符号应该放在表达式的末尾
                ErrorHandler.parseErr(ErrorHandler.Error.E_CLOSE);
                return false;
            case CCL_END:
                //表达式不应该以]开头
                ErrorHandler.parseErr(ErrorHandler.Error.E_BRACKET);
                return false;
            case AT_BOL:
                //^必须在表达式的最开始
                ErrorHandler.parseErr(ErrorHandler.Error.E_BOL);
                return false;
        }
        return true;
    }
}
