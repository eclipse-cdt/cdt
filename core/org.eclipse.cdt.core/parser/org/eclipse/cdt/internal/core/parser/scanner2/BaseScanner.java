/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.ast.ASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.ast.EmptyIterator;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.SimpleToken;

/**
 * @author Doug Schaefer
 *  
 */
abstract class BaseScanner implements IScanner {

    protected static final char[] ONE = "1".toCharArray(); //$NON-NLS-1$

    protected static final char[] ELLIPSIS_CHARARRAY = "...".toString().toCharArray(); //$NON-NLS-1$

    protected static final char[] VA_ARGS_CHARARRAY = "__VA_ARGS__".toCharArray(); //$NON-NLS-1$

	protected final IToken eocToken = new SimpleToken(IToken.tEOC, Integer.MAX_VALUE, null, Integer.MAX_VALUE);
	
    /**
     * @author jcamelon
     *  
     */
    protected static class InclusionData {

        public final Object inclusion;

        public final CodeReader reader;

        /**
         * @param reader
         * @param inclusion
         */
        public InclusionData(CodeReader reader, Object inclusion) {
            this.reader = reader;
            this.inclusion = inclusion;
        }
        
        public String toString() {
            return reader.toString();
        }
    }

    protected static class MacroData {
        public MacroData(int start, int end, IMacro macro) {
            this.startOffset = start;
            this.endOffset = end;
            this.macro = macro;
        }

        public final int startOffset;

        public final int endOffset;

        public final IMacro macro;
        
        public String toString() {
            return macro.toString();
        }
    }

    protected ParserLanguage language;

    protected IParserLogService log;

    protected CharArrayObjectMap definitions = new CharArrayObjectMap(512);

    protected String[] stdIncludePaths;
    protected String[] locIncludePaths = null;

    int count;

    protected ExpressionEvaluator expressionEvaluator;

    // The context stack
    protected static final int bufferInitialSize = 8;

    protected int bufferStackPos = -1;

    protected char[][] bufferStack = new char[bufferInitialSize][];

    protected Object[] bufferData = new Object[bufferInitialSize];

    protected int[] bufferPos = new int[bufferInitialSize];

    protected int[] bufferLimit = new int[bufferInitialSize];

    int[] lineNumbers = new int[bufferInitialSize];

    protected int[] lineOffsets = new int[bufferInitialSize];

    //branch tracking
    protected int branchStackPos = -1;

    protected int[] branches = new int[bufferInitialSize];

    //states
    final static protected int BRANCH_IF = 1;

    final static protected int BRANCH_ELIF = 2;

    final static protected int BRANCH_ELSE = 3;

    final static protected int BRANCH_END = 4;

    // Utility
    protected static String[] EMPTY_STRING_ARRAY = new String[0];

    protected static char[] EMPTY_CHAR_ARRAY = new char[0];

    protected static EndOfFileException EOF = new EndOfFileException();

    protected ParserMode parserMode;

    protected Iterator preIncludeFiles = EmptyIterator.EMPTY_ITERATOR;

    protected boolean isInitialized = false;
    protected boolean macroFilesInitialized = false;

    protected final char[] suffixes;

    protected final boolean support$Initializers;

    protected final boolean supportMinAndMax;

    protected final CharArrayIntMap additionalKeywords;

    protected static class ExpressionEvaluator {

        private static char[] emptyCharArray = new char[0];

        // The context stack
        private static final int initSize = 8;

        private int bufferStackPos = -1;

        private char[][] bufferStack = new char[initSize][];

        private Object[] bufferData = new Object[initSize];

        private int[] bufferPos = new int[initSize];

        private int[] bufferLimit = new int[initSize];

        private ScannerCallbackManager callbackManager = null;

        private ScannerProblemFactory problemFactory = null;

        private int lineNumber = 1;

        private char[] fileName = null;

        private int pos = 0;

        // The macros
        CharArrayObjectMap definitions;

        public ExpressionEvaluator() {
            super();
        }

        public ExpressionEvaluator(ScannerCallbackManager manager,
                ScannerProblemFactory spf) {
            this.callbackManager = manager;
            this.problemFactory = spf;
        }

        public long evaluate(char[] buffer, int p, int length,
                CharArrayObjectMap defs) {
            return evaluate(buffer, p, length, defs, 0, "".toCharArray()); //$NON-NLS-1$
        }

        public long evaluate(char[] buffer, int p, int length,
                CharArrayObjectMap defs, int ln, char[] fn) {
            this.lineNumber = ln;
            this.fileName = fn;
            bufferStack[++bufferStackPos] = buffer;
            bufferPos[bufferStackPos] = p - 1;
            bufferLimit[bufferStackPos] = p + length;
            this.definitions = defs;
            tokenType = 0;

            long r = 0;
            try {
                r = expression();
            } catch (EvalException e) {
            }

            while (bufferStackPos >= 0)
                popContext();

            return r;
        }

        private static class EvalException extends Exception {
        	private static final long serialVersionUID = 0;
            public EvalException(String msg) {
                super(msg);
            }
        }

        private long expression() throws EvalException {
            return conditionalExpression();
        }

        private long conditionalExpression() throws EvalException {
            long r1 = logicalOrExpression();
            if (LA() == tQUESTION) {
                consume();
                long r2 = expression();
                if (LA() == tCOLON)
                    consume();
                else {
                    handleProblem(IProblem.SCANNER_BAD_CONDITIONAL_EXPRESSION,
                            pos);
                    throw new EvalException("bad conditional expression"); //$NON-NLS-1$
                }
                long r3 = conditionalExpression();
                return r1 != 0 ? r2 : r3;
            }
            return r1;
        }

        private long logicalOrExpression() throws EvalException {
            long r1 = logicalAndExpression();
            while (LA() == tOR) {
                consume();
                long r2 = logicalAndExpression();
                r1 = ((r1 != 0) || (r2 != 0)) ? 1 : 0;
            }
            return r1;
        }

        private long logicalAndExpression() throws EvalException {
            long r1 = inclusiveOrExpression();
            while (LA() == tAND) {
                consume();
                long r2 = inclusiveOrExpression();
                r1 = ((r1 != 0) && (r2 != 0)) ? 1 : 0;
            }
            return r1;
        }

        private long inclusiveOrExpression() throws EvalException {
            long r1 = exclusiveOrExpression();
            while (LA() == tBITOR) {
                consume();
                long r2 = exclusiveOrExpression();
                r1 = r1 | r2;
            }
            return r1;
        }

        private long exclusiveOrExpression() throws EvalException {
            long r1 = andExpression();
            while (LA() == tBITXOR) {
                consume();
                long r2 = andExpression();
                r1 = r1 ^ r2;
            }
            return r1;
        }

        private long andExpression() throws EvalException {
            long r1 = equalityExpression();
            while (LA() == tBITAND) {
                consume();
                long r2 = equalityExpression();
                r1 = r1 & r2;
            }
            return r1;
        }

        private long equalityExpression() throws EvalException {
            long r1 = relationalExpression();
            for (int t = LA(); t == tEQUAL || t == tNOTEQUAL; t = LA()) {
                consume();
                long r2 = relationalExpression();
                if (t == tEQUAL)
                    r1 = (r1 == r2) ? 1 : 0;
                else
                    // t == tNOTEQUAL
                    r1 = (r1 != r2) ? 1 : 0;
            }
            return r1;
        }

        private long relationalExpression() throws EvalException {
            long r1 = shiftExpression();
            for (int t = LA(); t == tLT || t == tLTEQUAL || t == tGT
                    || t == tGTEQUAL; t = LA()) {
                consume();
                long r2 = shiftExpression();
                switch (t) {
                case tLT:
                    r1 = (r1 < r2) ? 1 : 0;
                    break;
                case tLTEQUAL:
                    r1 = (r1 <= r2) ? 1 : 0;
                    break;
                case tGT:
                    r1 = (r1 > r2) ? 1 : 0;
                    break;
                case tGTEQUAL:
                    r1 = (r1 >= r2) ? 1 : 0;
                    break;
                }
            }
            return r1;
        }

        private long shiftExpression() throws EvalException {
            long r1 = additiveExpression();
            for (int t = LA(); t == tSHIFTL || t == tSHIFTR; t = LA()) {
                consume();
                long r2 = additiveExpression();
                if (t == tSHIFTL)
                    r1 = r1 << r2;
                else
                    // t == tSHIFTR
                    r1 = r1 >> r2;
            }
            return r1;
        }

        private long additiveExpression() throws EvalException {
            long r1 = multiplicativeExpression();
            for (int t = LA(); t == tPLUS || t == tMINUS; t = LA()) {
                consume();
                long r2 = multiplicativeExpression();
                if (t == tPLUS)
                    r1 = r1 + r2;
                else
                    // t == tMINUS
                    r1 = r1 - r2;
            }
            return r1;
        }

        private long multiplicativeExpression() throws EvalException {
            long r1 = unaryExpression();
            for (int t = LA(); t == tMULT || t == tDIV; t = LA()) {
                int position = pos; // for IProblem /0 below, need position
                                    // before
                // consume()
                consume();
                long r2 = unaryExpression();
                if (t == tMULT)
                    r1 = r1 * r2;
                else if (r2 != 0)// t == tDIV;
                    r1 = r1 / r2;
                else {
                    handleProblem(IProblem.SCANNER_DIVIDE_BY_ZERO, position);
                    throw new EvalException("Divide by 0 encountered"); //$NON-NLS-1$
                }
            }
            return r1;
        }

        private long unaryExpression() throws EvalException {
            switch (LA()) {
            case tPLUS:
                consume();
                return unaryExpression();
            case tMINUS:
                consume();
                return -unaryExpression();
            case tNOT:
                consume();
                return unaryExpression() == 0 ? 1 : 0;
            case tCOMPL:
                consume();
                return ~unaryExpression();
            case tNUMBER:
                return consume();
            case t_defined:
                return handleDefined();
            case tLPAREN:
                consume();
                long r1 = expression();
                if (LA() == tRPAREN) {
                    consume();
                    return r1;
                }
                handleProblem(IProblem.SCANNER_MISSING_R_PAREN, pos);
                throw new EvalException("missing )"); //$NON-NLS-1$ 
            case tCHAR:
                return getChar();
            default:
                handleProblem(IProblem.SCANNER_EXPRESSION_SYNTAX_ERROR, pos);
                throw new EvalException("expression syntax error"); //$NON-NLS-1$ 
            }
        }

        private long handleDefined() throws EvalException {
            // We need to do some special handline to get the identifier without
            // it
            // being
            // expanded by macro expansion
            skipWhiteSpace();

            char[] buffer = bufferStack[bufferStackPos];
            int limit = bufferLimit[bufferStackPos];
            if (++bufferPos[bufferStackPos] >= limit)
                return 0;

            // check first character
            char c = buffer[bufferPos[bufferStackPos]];
            boolean inParens = false;
            if (c == '(') {
                inParens = true;
                skipWhiteSpace();
                if (++bufferPos[bufferStackPos] >= limit)
                    return 0;
                c = buffer[bufferPos[bufferStackPos]];
            }

            if (!((c >= 'A' && c <= 'Z') || c == '_' || (c >= 'a' && c <= 'z'))) {
                handleProblem(IProblem.SCANNER_ILLEGAL_IDENTIFIER, pos);
                throw new EvalException("illegal identifier in defined()"); //$NON-NLS-1$ 
            }

            // consume rest of identifier
            int idstart = bufferPos[bufferStackPos];
            int idlen = 1;
            while (++bufferPos[bufferStackPos] < limit) {
                c = buffer[bufferPos[bufferStackPos]];
                if ((c >= 'A' && c <= 'Z') || c == '_'
                        || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                    ++idlen;
                    continue;
                }
                break;
            }
            --bufferPos[bufferStackPos];

            // consume to the closing paren;
            if (inParens) {
                skipWhiteSpace();
                if (++bufferPos[bufferStackPos] <= limit
                        && buffer[bufferPos[bufferStackPos]] != ')') {
                    handleProblem(IProblem.SCANNER_MISSING_R_PAREN, pos);
                    throw new EvalException("missing ) on defined"); //$NON-NLS-1$
                }
            }

            // Set up the lookahead to whatever comes next
            nextToken();

            return definitions.get(buffer, idstart, idlen) != null ? 1 : 0;
        }

        // Scanner part
        int tokenType = tNULL;

        long tokenValue;

        private int LA() throws EvalException {
            if (tokenType == tNULL)
                nextToken();
            return tokenType;
        }

        private long consume() throws EvalException {
            long value = tokenValue;
            if (tokenType != tEOF)
                nextToken();
            return value;
        }

        private long getChar() throws EvalException {
            long value = 0;

            // if getting a character then make sure it's in '' otherwise leave
            // it
            // as 0
            if (bufferPos[bufferStackPos] - 1 >= 0
                    && bufferPos[bufferStackPos] + 1 < bufferStack[bufferStackPos].length
                    && bufferStack[bufferStackPos][bufferPos[bufferStackPos] - 1] == '\''
                    && bufferStack[bufferStackPos][bufferPos[bufferStackPos] + 1] == '\'')
                value = bufferStack[bufferStackPos][bufferPos[bufferStackPos]];

            if (tokenType != tEOF)
                nextToken();
            return value;
        }

        private static char[] _defined = "defined".toCharArray(); //$NON-NLS-1$

        private void nextToken() throws EvalException {
            boolean isHex = false;
            boolean isOctal = false;
            boolean isDecimal = false;

            contextLoop: while (bufferStackPos >= 0) {

                // Find the first thing we would care about
                skipWhiteSpace();

                while (++bufferPos[bufferStackPos] >= bufferLimit[bufferStackPos]) {
                    // We're at the end of a context, pop it off and try again
                    popContext();
                    continue contextLoop;
                }

                // Tokens don't span buffers, stick to our current one
                char[] buffer = bufferStack[bufferStackPos];
                int limit = bufferLimit[bufferStackPos];
                pos = bufferPos[bufferStackPos];

                if (buffer[pos] >= '1' && buffer[pos] <= '9')
                    isDecimal = true;
                else if (buffer[pos] == '0' && pos + 1 < limit)
                    if (buffer[pos + 1] == 'x' || buffer[pos + 1] == 'X') {
                        isHex = true;
                        ++bufferPos[bufferStackPos];
                        if (pos + 2 < limit)
                            if ((buffer[pos + 2] < '0' || buffer[pos + 2] > '9')
                                    && (buffer[pos + 2] < 'a' || buffer[pos + 2] > 'f')
                                    && (buffer[pos + 2] < 'A' || buffer[pos + 2] > 'F'))
                                handleProblem(IProblem.SCANNER_BAD_HEX_FORMAT,
                                        pos);
                    } else
                        isOctal = true;

                switch (buffer[pos]) {
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                    int start = bufferPos[bufferStackPos];
                    int len = 1;

                    while (++bufferPos[bufferStackPos] < limit) {
                        char c = buffer[bufferPos[bufferStackPos]];
                        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                                || c == '_' || (c >= '0' && c <= '9')) {
                            ++len;
                            continue;
                        }
                        break;
                    }

                    --bufferPos[bufferStackPos];

                    // Check for defined(
                    pos = bufferPos[bufferStackPos];
                    if (CharArrayUtils.equals(buffer, start, len, _defined)) {
                        tokenType = t_defined;
                        return;
                    }

                    // Check for macro expansion
                    Object expObject = null;
                    if (bufferData[bufferStackPos] instanceof FunctionStyleMacro.Expansion) {
                        // first check if name is a macro arg
                        expObject = ((FunctionStyleMacro.Expansion) bufferData[bufferStackPos]).definitions
                                .get(buffer, start, len);
                    }

                    if (expObject == null)
                        // now check regular macros
                        expObject = definitions.get(buffer, start, len);

                    if (expObject != null) {
                        if (expObject instanceof FunctionStyleMacro) {
                            handleFunctionStyleMacro((FunctionStyleMacro) expObject);
                        } else if (expObject instanceof ObjectStyleMacro) {
                            ObjectStyleMacro expMacro = (ObjectStyleMacro) expObject;
                            char[] expText = expMacro.expansion;
                            if (expText.length > 0)
                                pushContext(expText, expMacro);
                        } else if (expObject instanceof char[]) {
                            char[] expText = (char[]) expObject;
                            if (expText.length > 0)
                                pushContext(expText, null);
                        }
                        continue;
                    }

                    if (len == 1) { // is a character
                        tokenType = tCHAR;
                        return;
                    }

                    // undefined macro, assume 0
                    tokenValue = 0;
                    tokenType = tNUMBER;
                    return;

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    tokenValue = buffer[pos] - '0';
                    tokenType = tNUMBER;

                    while (++bufferPos[bufferStackPos] < limit) {
                        char c = buffer[bufferPos[bufferStackPos]];
                        if (isHex) {
                            if (c >= '0' && c <= '9') {
                                tokenValue *= 16;
                                tokenValue += c - '0';
                                continue;
                            } else if (c >= 'a' && c <= 'f') {
                                tokenValue = (tokenValue == 0 ? 10
                                        : (tokenValue * 16) + 10);
                                tokenValue += c - 'a';
                                continue;
                            } else if (c >= 'A' && c <= 'F') {
                                tokenValue = (tokenValue == 0 ? 10
                                        : (tokenValue * 16) + 10);
                                tokenValue += c - 'A';
                                continue;
                            } else {
                                if (bufferPos[bufferStackPos] + 1 < limit)
                                    if (!isValidTokenSeparator(
                                            c,
                                            buffer[bufferPos[bufferStackPos] + 1]))
                                        handleProblem(
                                                IProblem.SCANNER_BAD_HEX_FORMAT,
                                                pos);
                            }
                        } else if (isOctal) {
                            if (c >= '0' && c <= '7') {
                                tokenValue *= 8;
                                tokenValue += c - '0';
                                continue;
                            }
                            if (bufferPos[bufferStackPos] + 1 < limit)
                                if (!isValidTokenSeparator(c,
                                        buffer[bufferPos[bufferStackPos] + 1]))
                                    handleProblem(
                                            IProblem.SCANNER_BAD_OCTAL_FORMAT,
                                            pos);
                        } else if (isDecimal) {
                            if (c >= '0' && c <= '9') {
                                tokenValue *= 10;
                                tokenValue += c - '0';
                                continue;
                            }
                            if (bufferPos[bufferStackPos] + 1 < limit
                                    && !(c == 'L' || c == 'l' || c == 'U' || c == 'u'))
                                if (!isValidTokenSeparator(c,
                                        buffer[bufferPos[bufferStackPos] + 1]))
                                    handleProblem(
                                            IProblem.SCANNER_BAD_DECIMAL_FORMAT,
                                            pos);
                        }

                        // end of number
                        if (c == 'L' || c == 'l' || c == 'U' || c == 'u') {
                            // eat the long/unsigned
                            ++bufferPos[bufferStackPos];
                        }

                        // done
                        break;
                    }
                    --bufferPos[bufferStackPos];
                    return;
                case '(':
                    tokenType = tLPAREN;
                    return;

                case ')':
                    tokenType = tRPAREN;
                    return;

                case ':':
                    tokenType = tCOLON;
                    return;

                case '?':
                    tokenType = tQUESTION;
                    return;

                case '+':
                    tokenType = tPLUS;
                    return;

                case '-':
                    tokenType = tMINUS;
                    return;

                case '*':
                    tokenType = tMULT;
                    return;

                case '/':
                    tokenType = tDIV;
                    return;

                case '%':
                    tokenType = tMOD;
                    return;

                case '^':
                    tokenType = tBITXOR;
                    return;

                case '&':
                    if (pos + 1 < limit && buffer[pos + 1] == '&') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tAND;
                        return;
                    }
                    tokenType = tBITAND;
                    return;

                case '|':
                    if (pos + 1 < limit && buffer[pos + 1] == '|') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tOR;
                        return;
                    }
                    tokenType = tBITOR;
                    return;

                case '~':
                    tokenType = tCOMPL;
                    return;

                case '!':
                    if (pos + 1 < limit && buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tNOTEQUAL;
                        return;
                    }
                    tokenType = tNOT;
                    return;

                case '=':
                    if (pos + 1 < limit && buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        tokenType = tEQUAL;
                        return;
                    }
                    handleProblem(IProblem.SCANNER_ASSIGNMENT_NOT_ALLOWED, pos);
                    throw new EvalException("assignment not allowed"); //$NON-NLS-1$ 

                case '<':
                    if (pos + 1 < limit) {
                        if (buffer[pos + 1] == '=') {
                            ++bufferPos[bufferStackPos];
                            tokenType = tLTEQUAL;
                            return;
                        } else if (buffer[pos + 1] == '<') {
                            ++bufferPos[bufferStackPos];
                            tokenType = tSHIFTL;
                            return;
                        }
                    }
                    tokenType = tLT;
                    return;

                case '>':
                    if (pos + 1 < limit) {
                        if (buffer[pos + 1] == '=') {
                            ++bufferPos[bufferStackPos];
                            tokenType = tGTEQUAL;
                            return;
                        } else if (buffer[pos + 1] == '>') {
                            ++bufferPos[bufferStackPos];
                            tokenType = tSHIFTR;
                            return;
                        }
                    }
                    tokenType = tGT;
                    return;

                default:
                // skip over anything we don't handle
                }
            }

            // We've run out of contexts, our work is done here
            tokenType = tEOF;
            return;
        }

        private void handleFunctionStyleMacro(FunctionStyleMacro macro) {
            char[] buffer = bufferStack[bufferStackPos];
            int limit = bufferLimit[bufferStackPos];

            skipWhiteSpace();
            if (++bufferPos[bufferStackPos] >= limit
                    || buffer[bufferPos[bufferStackPos]] != '(')
                return;

            FunctionStyleMacro.Expansion exp = macro.new Expansion();
            char[][] arglist = macro.arglist;
            int currarg = -1;
            int parens = 0;

            while (bufferPos[bufferStackPos] < limit) {
                if (++currarg >= arglist.length || arglist[currarg] == null)
                    // too many args
                    break;

                skipWhiteSpace();

                int p = ++bufferPos[bufferStackPos];
                char c = buffer[p];
                if (c == ')') {
                    if (parens == 0)
                        // end of macro
                        break;
                    --parens;
                    continue;
                } else if (c == ',') {
                    // empty arg
                    exp.definitions.put(arglist[currarg], emptyCharArray);
                    continue;
                } else if (c == '(') {
                    ++parens;
                    continue;
                }

                // peel off the arg
                int argstart = p;
                int argend = argstart - 1;

                // Loop looking for end of argument
                while (bufferPos[bufferStackPos] < limit) {
                    skipOverMacroArg();
                    argend = bufferPos[bufferStackPos];
                    skipWhiteSpace();

                    if (++bufferPos[bufferStackPos] >= limit)
                        break;
                    c = buffer[bufferPos[bufferStackPos]];
                    if (c == ',' || c == ')')
                        break;
                }

                char[] arg = emptyCharArray;
                int arglen = argend - argstart + 1;
                if (arglen > 0) {
                    arg = new char[arglen];
                    System.arraycopy(buffer, argstart, arg, 0, arglen);
                }
                exp.definitions.put(arglist[currarg], arg);

                if (c == ')')
                    break;
            }

            char[] expText = macro.expansion;
            if (expText.length > 0)
                pushContext(expText, exp);
        }

        private void skipOverMacroArg() {
            char[] buffer = bufferStack[bufferStackPos];
            int limit = bufferLimit[bufferStackPos];

            while (++bufferPos[bufferStackPos] < limit) {
                switch (buffer[bufferPos[bufferStackPos]]) {
                case ' ':
                case '\t':
                case '\r':
                case ',':
                case ')':
                    --bufferPos[bufferStackPos];
                    return;
                case '\n':
                    lineNumber++;
                    --bufferPos[bufferStackPos];
                    return;
                case '\\':
                    int p = bufferPos[bufferStackPos];
                    if (p + 1 < limit && buffer[p + 1] == '\n') {
                        // \n is whitespace
                        lineNumber++;
                        --bufferPos[bufferStackPos];
                        return;
                    }
                    break;
                case '"':
                    boolean escaped = false;
                    loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                        switch (buffer[bufferPos[bufferStackPos]]) {
                        case '\\':
                            escaped = !escaped;
                            continue;
                        case '"':
                            if (escaped) {
                                escaped = false;
                                continue;
                            }
                            break loop;
                        default:
                            escaped = false;
                        }
                    }
                    break;
                }
            }
            --bufferPos[bufferStackPos];
        }

        private void skipWhiteSpace() {
            char[] buffer = bufferStack[bufferStackPos];
            int limit = bufferLimit[bufferStackPos];

            while (++bufferPos[bufferStackPos] < limit) {
                int p = bufferPos[bufferStackPos];
                switch (buffer[p]) {
                case ' ':
                case '\t':
                case '\r':
                    continue;
                case '/':
                    if (p + 1 < limit) {
                        if (buffer[p + 1] == '/') {
                            // C++ comment, skip rest of line
                            for (bufferPos[bufferStackPos] += 2; bufferPos[bufferStackPos] < limit; ++bufferPos[bufferStackPos]) {
                                p = bufferPos[bufferStackPos];
                                if (buffer[p] == '\\' && p + 1 < limit
                                        && buffer[p + 1] == '\n') {
                                    bufferPos[bufferStackPos] += 2;
                                    continue;
                                }
                                if (buffer[p] == '\\' && p + 1 < limit
                                        && buffer[p + 1] == '\r'
                                        && p + 2 < limit
                                        && buffer[p + 2] == '\n') {
                                    bufferPos[bufferStackPos] += 3;
                                    continue;
                                }

                                if (buffer[p] == '\n')
                                    break; // break when find non-escaped
                                           // newline
                            }
                            continue;
                        } else if (buffer[p + 1] == '*') { // C comment, find
                            // closing */
                            for (bufferPos[bufferStackPos] += 2; bufferPos[bufferStackPos] < limit; ++bufferPos[bufferStackPos]) {
                                p = bufferPos[bufferStackPos];
                                if (buffer[p] == '*' && p + 1 < limit
                                        && buffer[p + 1] == '/') {
                                    ++bufferPos[bufferStackPos];
                                    break;
                                }
                            }
                            continue;
                        }
                    }
                    break;
                case '\\':
                    if (p + 1 < limit && buffer[p + 1] == '\n') {
                        // \n is a whitespace
                        lineNumber++;
                        ++bufferPos[bufferStackPos];
                        continue;
                    }
                }

                // fell out of switch without continuing, we're done
                --bufferPos[bufferStackPos];
                return;
            }

            // fell out of while without continuing, we're done
            --bufferPos[bufferStackPos];
            return;
        }

        private static final int tNULL = 0;

        private static final int tEOF = 1;

        private static final int tNUMBER = 2;

        private static final int tLPAREN = 3;

        private static final int tRPAREN = 4;

        private static final int tNOT = 5;

        private static final int tCOMPL = 6;

        private static final int tMULT = 7;

        private static final int tDIV = 8;

        private static final int tMOD = 9;

        private static final int tPLUS = 10;

        private static final int tMINUS = 11;

        private static final int tSHIFTL = 12;

        private static final int tSHIFTR = 13;

        private static final int tLT = 14;

        private static final int tGT = 15;

        private static final int tLTEQUAL = 16;

        private static final int tGTEQUAL = 17;

        private static final int tEQUAL = 18;

        private static final int tNOTEQUAL = 19;

        private static final int tBITAND = 20;

        private static final int tBITXOR = 21;

        private static final int tBITOR = 22;

        private static final int tAND = 23;

        private static final int tOR = 24;

        private static final int tQUESTION = 25;

        private static final int tCOLON = 26;

        private static final int t_defined = 27;

        private static final int tCHAR = 28;

        private void pushContext(char[] buffer, Object data) {
            if (++bufferStackPos == bufferStack.length) {
                int size = bufferStack.length * 2;

                char[][] oldBufferStack = bufferStack;
                bufferStack = new char[size][];
                System.arraycopy(oldBufferStack, 0, bufferStack, 0,
                        oldBufferStack.length);

                Object[] oldBufferData = bufferData;
                bufferData = new Object[size];
                System.arraycopy(oldBufferData, 0, bufferData, 0,
                        oldBufferData.length);

                int[] oldBufferPos = bufferPos;
                bufferPos = new int[size];
                System.arraycopy(oldBufferPos, 0, bufferPos, 0,
                        oldBufferPos.length);

                int[] oldBufferLimit = bufferLimit;
                bufferLimit = new int[size];
                System.arraycopy(oldBufferLimit, 0, bufferLimit, 0,
                        oldBufferLimit.length);
            }

            bufferStack[bufferStackPos] = buffer;
            bufferPos[bufferStackPos] = -1;
            bufferLimit[bufferStackPos] = buffer.length;
            bufferData[bufferStackPos] = data;
        }

        private void popContext() {
            bufferStack[bufferStackPos] = null;
            bufferData[bufferStackPos] = null;
            --bufferStackPos;
        }

        private void handleProblem(int id, int startOffset) {
            if (callbackManager != null && problemFactory != null)
                callbackManager
                        .pushCallback(problemFactory
                                .createProblem(
                                        id,
                                        startOffset,
                                        bufferPos[(bufferStackPos == -1 ? 0
                                                : bufferStackPos)],
                                        lineNumber,
                                        (fileName == null ? "".toCharArray() : fileName), emptyCharArray, false, true)); //$NON-NLS-1$
        }

        private boolean isValidTokenSeparator(char c, char c2)
                throws EvalException {
            switch (c) {
            case '\t':
            case '\r':
            case '\n':
            case ' ':
            case '(':
            case ')':
            case ':':
            case '?':
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
            case '^':
            case '&':
            case '|':
            case '~':
            case '!':
            case '<':
            case '>':
                return true;
            case '=':
                if (c2 == '=')
                    return true;
                return false;
            }

            return false;
        }
    }

    public BaseScanner(CodeReader reader, IScannerInfo info,
            ParserMode parserMode, ParserLanguage language,
            IParserLogService log, IScannerExtensionConfiguration configuration) {

        this.parserMode = parserMode;
        this.language = language;
        this.log = log;

        if (configuration.supportAdditionalNumericLiteralSuffixes() != null)
            suffixes = configuration.supportAdditionalNumericLiteralSuffixes();
        else
            suffixes = EMPTY_CHAR_ARRAY;
        support$Initializers = configuration.support$InIdentifiers();
        supportMinAndMax = configuration.supportMinAndMaxOperators();

        if (language == ParserLanguage.C)
            keywords = ckeywords;
        else
            keywords = cppkeywords;

        additionalKeywords = configuration.getAdditionalKeywords();

        setupBuiltInMacros(configuration);

        if (info.getDefinedSymbols() != null) {
            Map symbols = info.getDefinedSymbols();
            String[] keys = (String[]) symbols.keySet().toArray(
                    EMPTY_STRING_ARRAY);
            for (int i = 0; i < keys.length; ++i) {
                String symbolName = keys[i];
                Object value = symbols.get(symbolName);

                if (value instanceof String) {
                    if (configuration.initializeMacroValuesTo1()
                            && ((String) value).trim().equals(EMPTY_STRING))
                        addDefinition(symbolName.toCharArray(), ONE);
                    else
                        addDefinition(symbolName.toCharArray(),
                                ((String) value).toCharArray());
                }
            }
        }
        stdIncludePaths = info.getIncludePaths();
        

    }

    /**
     * @param reader
     * @param info
     */
    protected void postConstructorSetup(CodeReader reader, IScannerInfo info) {
        if (info instanceof IExtendedScannerInfo) {
            extendedScannerInfoSetup(reader, info);
        } else {
            macroFilesInitialized = true;
            pushContext(reader.buffer, reader);
            isInitialized = true;
        }
    }

    /**
     * @param reader
     * @param info
     */
    protected void extendedScannerInfoSetup(CodeReader reader, IScannerInfo info) {
        IExtendedScannerInfo einfo = (IExtendedScannerInfo) info;
        if (einfo.getMacroFiles() != null)
            for (int i = 0; i < einfo.getMacroFiles().length; ++i) {
                CodeReader r = createReaderDuple(einfo.getMacroFiles()[i]);
                if (r == null)
                    continue;
                pushContext(r.buffer, r);
                while (true) {
                    try {
                        nextToken();
                    } catch (EndOfFileException e) {
                        finished = false;
                        break;
                    }
                }
            }

        macroFilesInitialized = true;
        if (parserMode != ParserMode.QUICK_PARSE && einfo.getIncludeFiles() != null
                && einfo.getIncludeFiles().length > 0)
            preIncludeFiles = Arrays.asList(einfo.getIncludeFiles()).iterator();

        locIncludePaths = einfo.getLocalIncludePath();
        pushContext(reader.buffer, reader);

        while (preIncludeFiles.hasNext())
            pushForcedInclusion();

        isInitialized = true;
    }

    protected void pushContext(char[] buffer) {
        if (++bufferStackPos == bufferStack.length) {
            int size = bufferStack.length * 2;

            char[][] oldBufferStack = bufferStack;
            bufferStack = new char[size][];
            System.arraycopy(oldBufferStack, 0, bufferStack, 0,
                    oldBufferStack.length);

            Object[] oldBufferData = bufferData;
            bufferData = new Object[size];
            System.arraycopy(oldBufferData, 0, bufferData, 0,
                    oldBufferData.length);

            int[] oldBufferPos = bufferPos;
            bufferPos = new int[size];
            System
                    .arraycopy(oldBufferPos, 0, bufferPos, 0,
                            oldBufferPos.length);

            int[] oldBufferLimit = bufferLimit;
            bufferLimit = new int[size];
            System.arraycopy(oldBufferLimit, 0, bufferLimit, 0,
                    oldBufferLimit.length);

            int[] oldLineNumbers = lineNumbers;
            lineNumbers = new int[size];
            System.arraycopy(oldLineNumbers, 0, lineNumbers, 0,
                    oldLineNumbers.length);

            int[] oldLineOffsets = lineOffsets;
            lineOffsets = new int[size];
            System.arraycopy(oldLineOffsets, 0, lineOffsets, 0,
                    oldLineOffsets.length);

        }

        bufferStack[bufferStackPos] = buffer;
        bufferPos[bufferStackPos] = -1;
        lineNumbers[bufferStackPos] = 1;
        lineOffsets[bufferStackPos] = 0;
        bufferLimit[bufferStackPos] = buffer.length;
    }

    protected void pushContext(char[] buffer, Object data) {
        if (data instanceof InclusionData) {
            if (isCircularInclusion( (InclusionData)data ))
                return;
        }
        pushContext(buffer);
        bufferData[bufferStackPos] = data;

    }

    protected boolean isCircularInclusion(InclusionData data) {
        for (int i = 0; i < bufferStackPos; ++i) {
            if (bufferData[i] instanceof CodeReader
                    && CharArrayUtils.equals(
                            ((CodeReader) bufferData[i]).filename,
                            data.reader.filename)) {
                return true;
            } else if (bufferData[i] instanceof InclusionData
                    && CharArrayUtils
                            .equals(
                                    ((InclusionData) bufferData[i]).reader.filename,
                                    data.reader.filename)) {
                return true;
            }
        }
        return false;
    }

    protected Object popContext() {
        //NOTE - do not set counters to 0 or -1 or something
        //Subclasses may require those values in their popContext()
        bufferStack[bufferStackPos] = null;

        Object result = bufferData[bufferStackPos];
        bufferData[bufferStackPos] = null;
        --bufferStackPos;

        return result;
    }

    /**
     *  
     */
    protected void pushForcedInclusion() {
        CodeReader r = null;
        while (r == null) {
            if (preIncludeFiles.hasNext())
                r = createReaderDuple((String) preIncludeFiles.next());
            else
                break;
        }
        if (r == null)
            return;
        int o = getCurrentOffset() + 1; 
        int l = getLineNumber(o);
        Object i = createInclusionConstruct(r.filename, r.filename, false, o,
                l, o, o, l, o, l, true);
        InclusionData d = new InclusionData(r, i);
        pushContext(r.buffer, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#addDefinition(java.lang.String,
     *      java.lang.String)
     */
    public void addDefinition(char[] key, char[] value) {
        int idx = CharArrayUtils.indexOf('(', key);
        if (idx == -1)
            definitions.put(key, new ObjectStyleMacro(key, value));
        else {
            pushContext(key);
            bufferPos[bufferStackPos] = idx;
            char[][] args = null;
            try {
                args = extractMacroParameters(0, EMPTY_STRING_CHAR_ARRAY, false);
            } finally {
                popContext();
            }

            if (args != null) {
                key = CharArrayUtils.extract(key, 0, idx);
                definitions.put(key, new FunctionStyleMacro(key, value, args));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#getCount()
     */
    public int getCount() {
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#getDefinitions()
     */
    public Map getDefinitions() {
        CharArrayObjectMap objMap = getRealDefinitions();
        int size = objMap.size();
        Map hashMap = new HashMap(size);
        for (int i = 0; i < size; i++) {
            hashMap.put(String.valueOf(objMap.keyAt(i)), objMap.getAt(i));
        }

        return hashMap;
    }

    public CharArrayObjectMap getRealDefinitions() {
        return definitions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#getIncludePaths()
     */
    public String[] getIncludePaths() {
        return stdIncludePaths;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#isOnTopContext()
     */
    public boolean isOnTopContext() {
		for (int i = 1; i <= bufferStackPos; ++i)
			if (bufferData[i] instanceof InclusionData)
				return false;
		return true;
    }

    protected IToken lastToken;

    protected IToken nextToken;

    protected boolean finished = false;

    protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

    protected static final char[] EMPTY_STRING_CHAR_ARRAY = new char[0];

    protected boolean isCancelled = false;

    public synchronized void cancel() {
        isCancelled = true;
        int index = bufferStackPos < 0 ? 0 : bufferStackPos;
        bufferPos[index] = bufferLimit[index];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#nextToken()
     */
    public IToken nextToken() throws EndOfFileException {
        boolean exception = false;
        if (nextToken == null && !finished) {
            try {
                nextToken = fetchToken();
            } catch (Exception e) {
                if (e instanceof OffsetLimitReachedException)
                    throw (OffsetLimitReachedException) e;
                if (e instanceof ArrayIndexOutOfBoundsException && isCancelled)
                    throw new ParseError(
                            ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);

                exception = true;
                errorHandle();
            }
            if (nextToken == null && !exception) {
                finished = true;
            }
        }

        beforeSecondFetchToken();

        if (finished) {
        	if (contentAssistMode) {
				if (lastToken != null)
					lastToken.setNext(nextToken);
        		lastToken = nextToken;
        		nextToken = eocToken;
       			return lastToken;
        	}
        	
            if (isCancelled == true)
                throw new ParseError(
                        ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);

            if (offsetBoundary == -1)
                throwEOF();
            throwOLRE();
        }

        if (lastToken != null)
            lastToken.setNext(nextToken);
        IToken oldToken = lastToken;
        lastToken = nextToken;

        try {
            nextToken = fetchToken();
        } catch (Exception e) {
            if (e instanceof OffsetLimitReachedException)
                throw (OffsetLimitReachedException) e;

            nextToken = null;
            exception = true;
            errorHandle();
        }

        if (nextToken == null) {
            if (!exception)
                finished = true;
        } else if (nextToken.getType() == IToken.tCOMPLETION) {
        	finished = true;
        } else if (nextToken.getType() == IToken.tPOUNDPOUND) {
            // time for a pasting
            IToken token2 = fetchToken();
            if (token2 == null) {
                nextToken = null;
                finished = true;
            } else {
                char[] pb = CharArrayUtils.concat(lastToken.getCharImage(),
                        token2.getCharImage());
                pushContext(pb);
                lastToken = oldToken;
                nextToken = null;
                return nextToken();
            }
        } else if (lastToken != null
                && (lastToken.getType() == IToken.tSTRING || lastToken
                        .getType() == IToken.tLSTRING)) {
            while (nextToken != null
                    && (nextToken.getType() == IToken.tSTRING || nextToken
                            .getType() == IToken.tLSTRING)) {
                // Concatenate the adjacent strings
                int tokenType = IToken.tSTRING;
                if (lastToken.getType() == IToken.tLSTRING
                        || nextToken.getType() == IToken.tLSTRING)
                    tokenType = IToken.tLSTRING;
                lastToken = newToken(tokenType, CharArrayUtils.concat(lastToken
                        .getCharImage(), nextToken.getCharImage()));
                if (oldToken != null)
                    oldToken.setNext(lastToken);
                nextToken = fetchToken();
            }
        }

        return lastToken;
    }

    /**
     * @throws EndOfFileException
     */
    protected void throwEOF() throws EndOfFileException {
        throw EOF;
    }

    /**
     *  
     */
    protected void beforeSecondFetchToken() {
    }

    /**
     *  
     */
    protected void errorHandle() {
        if( bufferStackPos > 0 )
            ++bufferPos[bufferStackPos];
    }

    /**
     *  
     */
    protected void throwOLRE() throws OffsetLimitReachedException {
        if (lastToken != null && lastToken.getEndOffset() != offsetBoundary)
            throw new OffsetLimitReachedException((IToken) null);
        throw new OffsetLimitReachedException(lastToken);
    }

    // Return null to signify end of file
    protected IToken fetchToken() throws EndOfFileException {
        ++count;
        while (bufferStackPos >= 0) {
            if (isCancelled == true)
                throw new ParseError(
                        ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);

            // Find the first thing we would care about
            skipOverWhiteSpace();

            if (++bufferPos[bufferStackPos] >= bufferLimit[bufferStackPos]) {
                // We're at the end of a context, pop it off and try again
                popContext();
                continue;
            }

            // Tokens don't span buffers, stick to our current one
            char[] buffer = bufferStack[bufferStackPos];
            int limit = bufferLimit[bufferStackPos];
            int pos = bufferPos[bufferStackPos];

            switch (buffer[pos]) {
            case '\r':
            case '\n':
                continue;

            case 'L':
                if (pos + 1 < limit && buffer[pos + 1] == '"')
                    return scanString();
                if (pos + 1 < limit && buffer[pos + 1] == '\'')
                    return scanCharLiteral();

                IToken t = scanIdentifier();
                if (t instanceof MacroExpansionToken)
                    continue;
                return t;

            case '"':
                return scanString();

            case '\'':
                return scanCharLiteral();

            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '_':
                t = scanIdentifier();
                if (t instanceof MacroExpansionToken)
                    continue;
                return t;

            case '\\':
                if (pos + 1 < limit
                        && (buffer[pos + 1] == 'u' || buffer[pos + 1] == 'U')) {
                    t = scanIdentifier();
                    if (t instanceof MacroExpansionToken)
                        continue;
                    return t;
                }
                handleProblem(IProblem.SCANNER_BAD_CHARACTER,
                        bufferPos[bufferStackPos], new char[] { '\\' });
                continue;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return scanNumber();

            case '.':
                if (pos + 1 < limit) {
                    switch (buffer[pos + 1]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return scanNumber();

                    case '.':
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '.') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tELLIPSIS);
                            }
                        }
                    case '*':
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tDOTSTAR);
                    }
                }
                return newToken(IToken.tDOT);

            case '#':
                if (pos + 1 < limit && buffer[pos + 1] == '#') {
                    ++bufferPos[bufferStackPos];
                    return newToken(IToken.tPOUNDPOUND);
                }

                // Should really check to make sure this is the first
                // non whitespace character on the line
                handlePPDirective(pos);
                continue;

            case '{':
                return newToken(IToken.tLBRACE);

            case '}':
                return newToken(IToken.tRBRACE);

            case '[':
                return newToken(IToken.tLBRACKET);

            case ']':
                return newToken(IToken.tRBRACKET);

            case '(':
                return newToken(IToken.tLPAREN);

            case ')':
                return newToken(IToken.tRPAREN);

            case ';':
                return newToken(IToken.tSEMI);

            case ':':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == ':'
                            && getLanguage() == ParserLanguage.CPP) {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tCOLONCOLON);
                    }
                }
                return newToken(IToken.tCOLON);

            case '?':
                return newToken(IToken.tQUESTION);

            case '+':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '+') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tINCR);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tPLUSASSIGN);
                    }
                }
                return newToken(IToken.tPLUS);

            case '-':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '>') {
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '*') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tARROWSTAR);
                            }
                        }
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tARROW);
                    } else if (buffer[pos + 1] == '-') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tDECR);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tMINUSASSIGN);
                    }
                }
                return newToken(IToken.tMINUS);

            case '*':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tSTARASSIGN);
                    }
                }
                return newToken(IToken.tSTAR);

            case '/':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tDIVASSIGN);
                    }
                }
                return newToken(IToken.tDIV);

            case '%':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tMODASSIGN);
                    }
                }
                return newToken(IToken.tMOD);

            case '^':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tXORASSIGN);
                    }
                }
                return newToken(IToken.tXOR);

            case '&':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '&') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tAND);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tAMPERASSIGN);
                    }
                }
                return newToken(IToken.tAMPER);

            case '|':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '|') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tOR);
                    } else if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tBITORASSIGN);
                    }
                }
                return newToken(IToken.tBITOR);

            case '~':
                return newToken(IToken.tCOMPL);

            case '!':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tNOTEQUAL);
                    }
                }
                return newToken(IToken.tNOT);

            case '=':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tEQUAL);
                    }
                }
                return newToken(IToken.tASSIGN);

            case '<':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tLTEQUAL);
                    } else if (buffer[pos + 1] == '<') {
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '=') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tSHIFTLASSIGN);
                            }
                        }
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tSHIFTL);
                    } else if (buffer[pos + 1] == '?' && supportMinAndMax) {
                        ++bufferPos[bufferStackPos];
                        return newToken(IGCCToken.tMIN, CharArrayUtils.extract(
                                buffer, pos, 2));
                    }
                }
                return newToken(IToken.tLT);

            case '>':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '=') {
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tGTEQUAL);
                    } else if (buffer[pos + 1] == '>') {
                        if (pos + 2 < limit) {
                            if (buffer[pos + 2] == '=') {
                                bufferPos[bufferStackPos] += 2;
                                return newToken(IToken.tSHIFTRASSIGN);
                            }
                        }
                        ++bufferPos[bufferStackPos];
                        return newToken(IToken.tSHIFTR);
                    } else if (buffer[pos + 1] == '?' && supportMinAndMax) {
                        ++bufferPos[bufferStackPos];
                        return newToken(IGCCToken.tMAX, CharArrayUtils.extract(
                                buffer, pos, 2));
                    }

                }
                return newToken(IToken.tGT);

            case ',':
                return newToken(IToken.tCOMMA);

            default:
                if (Character.isLetter(buffer[pos]) || buffer[pos] == '_'
                        || (support$Initializers && buffer[pos] == '$')) {
                    t = scanIdentifier();
                    if (t instanceof MacroExpansionToken)
                        continue;
                    return t;
                }

            // skip over anything we don't handle
                char [] x = new char [1];
                x[0] = buffer[pos];
                handleProblem( IASTProblem.SCANNER_BAD_CHARACTER, pos, x );
            }
        }

        // We've run out of contexts, our work is done here
        return contentAssistMode ? eocToken : null;
    }

    protected IToken scanIdentifier() {
        char[] buffer = bufferStack[bufferStackPos];
        boolean escapedNewline = false;
        int start = bufferPos[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int len = 1;

        while (++bufferPos[bufferStackPos] < limit) {
            char c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c)) {
                ++len;
                continue;
            } else if (c == '\\' && bufferPos[bufferStackPos] + 1 < limit
                    && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                // escaped newline
                ++bufferPos[bufferStackPos];
                len += 2;
                escapedNewline = true;
                continue;
            } else if (c == '\\' && (bufferPos[bufferStackPos] + 1 < limit)) {
                if ((buffer[bufferPos[bufferStackPos] + 1] == 'u')
                        || buffer[bufferPos[bufferStackPos] + 1] == 'U') {
                    ++bufferPos[bufferStackPos];
                    len += 2;
                    continue;
                }
            } else if ((support$Initializers && c == '$')) {
                ++len;
                continue;
            }
            break;
        }

        --bufferPos[bufferStackPos];

        if (contentAssistMode && bufferStackPos == 0 && bufferPos[bufferStackPos] + 1 == limit) {
        	// return the text as a content assist token
        	return newToken(IToken.tCOMPLETION, CharArrayUtils.extract(buffer, start, bufferPos[bufferStackPos] - start + 1));
        }

        // Check for macro expansion
        Object expObject = definitions.get(buffer, start, len);

        if (expObject != null && !isLimitReached()
                && shouldExpandMacro((IMacro) expObject)) {
            boolean expanding = true;
            if (expObject instanceof FunctionStyleMacro) {
                if (handleFunctionStyleMacro((FunctionStyleMacro) expObject,
                        true) == null)
                    expanding = false;
            } else if (expObject instanceof ObjectStyleMacro) {
                ObjectStyleMacro expMacro = (ObjectStyleMacro) expObject;
                char[] expText = expMacro.expansion;
                if (expText.length > 0)
                    pushContext(expText, new MacroData(
                            bufferPos[bufferStackPos] - expMacro.name.length + 1, 
                            bufferPos[bufferStackPos], expMacro));
            } else if (expObject instanceof DynamicStyleMacro) {
                DynamicStyleMacro expMacro = (DynamicStyleMacro) expObject;
                char[] expText = expMacro.execute();
                if (expText.length > 0)
                    pushContext(expText, new MacroData(
                            bufferPos[bufferStackPos] - expMacro.name.length
                                    + 1, bufferPos[bufferStackPos], expMacro));

            } else if (expObject instanceof char[]) {
                char[] expText = (char[]) expObject;
                if (expText.length > 0)
                    pushContext(expText);
            }
            if (expanding)
                return EXPANSION_TOKEN;
        }

        char[] result = escapedNewline ? removedEscapedNewline(buffer, start,
                len) : CharArrayUtils.extract(buffer, start, len);
        int tokenType = escapedNewline ? keywords.get(result, 0, result.length)
                : keywords.get(buffer, start, len);

        if (tokenType == keywords.undefined) {
            tokenType = escapedNewline ? additionalKeywords.get(result, 0,
                    result.length) : additionalKeywords.get(buffer, start, len);

            if (tokenType == additionalKeywords.undefined) {
                result = (result != null) ? result : CharArrayUtils.extract(
                        buffer, start, len);
                return newToken(IToken.tIDENTIFIER, result);
            }
            result = (result != null) ? result : CharArrayUtils.extract(buffer,
                    start, len);
            return newToken(tokenType, result);
        }
        return newToken(tokenType);
    }

    /**
     * @param buffer
     * @param start
     * @param len
     * @param expObject
     * @return
     */
    protected boolean shouldExpandMacro(IMacro macro) {
        // but not if it has been expanded on the stack already
        // i.e. recursion avoidance
        if (macro != null && !isLimitReached())
            for (int stackPos = bufferStackPos; stackPos >= 0; --stackPos)
                if (bufferData[stackPos] != null
                        && bufferData[stackPos] instanceof MacroData
                        && CharArrayUtils.equals(macro.getName(),
                                ((MacroData) bufferData[stackPos]).macro
                                        .getName())) {
                    return false;
                }
        return true;
    }

    /**
     * @return
     */
    protected final boolean isLimitReached() {
        if (offsetBoundary == -1 || bufferStackPos != 0)
            return false;
        if (bufferPos[bufferStackPos] == offsetBoundary - 1)
            return true;
        if (bufferPos[bufferStackPos] == offsetBoundary) {
            int c = bufferStack[bufferStackPos][bufferPos[bufferStackPos]];
            if (c == '\n' || c == ' ' || c == '\t' || c == '\r')
                return true;
        }
        return false;
    }

    protected IToken scanString() {
        char[] buffer = bufferStack[bufferStackPos];

        int tokenType = IToken.tSTRING;
        if (buffer[bufferPos[bufferStackPos]] == 'L') {
            ++bufferPos[bufferStackPos];
            tokenType = IToken.tLSTRING;
        }

        int stringStart = bufferPos[bufferStackPos] + 1;
        int stringLen = 0;
        boolean escaped = false;
        boolean foundClosingQuote = false;
        while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
            ++stringLen;
            char c = buffer[bufferPos[bufferStackPos]];
            if (c == '"') {
                if (!escaped) {
                    foundClosingQuote = true;
                    break;
                }
            } else if (c == '\\') {
                escaped = !escaped;
                continue;
            } else if (c == '\n') {
                //unescaped end of line before end of string
                if (!escaped)
                    break;
            } else if (c == '\r') {
                if (bufferPos[bufferStackPos] + 1 < bufferLimit[bufferStackPos]
                        && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                    ++bufferPos[bufferStackPos];
                    if (!escaped)
                        break;
                }
            }
            escaped = false;
        }
        --stringLen;

        // We should really throw an exception if we didn't get the terminating
        // quote before the end of buffer
        char[] result = CharArrayUtils.extract(buffer, stringStart, stringLen);
        if (!foundClosingQuote) {
            handleProblem(IProblem.SCANNER_UNBOUNDED_STRING, stringStart,
                    result);
        }
        return newToken(tokenType, result);
    }

    protected IToken scanCharLiteral() {
        char[] buffer = bufferStack[bufferStackPos];
        int start = bufferPos[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        int tokenType = IToken.tCHAR;
        int length = 1;
        if (buffer[bufferPos[bufferStackPos]] == 'L') {
            ++bufferPos[bufferStackPos];
            tokenType = IToken.tLCHAR;
            ++length;
        }

        if (start >= limit) {
            return newToken(tokenType, EMPTY_CHAR_ARRAY);
        }

        boolean escaped = false;
        while (++bufferPos[bufferStackPos] < limit) {
            ++length;
            int pos = bufferPos[bufferStackPos];
            if (buffer[pos] == '\'') {
                if (!escaped)
                    break;
            } else if (buffer[pos] == '\\') {
                escaped = !escaped;
                continue;
            }
            escaped = false;
        }

        if (bufferPos[bufferStackPos] == limit) {
            handleProblem(IProblem.SCANNER_BAD_CHARACTER, start, CharArrayUtils
                    .extract(buffer, start, length));
            return newToken(tokenType, EMPTY_CHAR_ARRAY);
        }

        char[] image = length > 0 ? CharArrayUtils.extract(buffer, start,
                length) : EMPTY_CHAR_ARRAY;

        return newToken(tokenType, image);
    }

    /**
     * @param scanner_bad_character
     */
    protected abstract void handleProblem(int id, int offset, char[] arg);

    /**
     * @param i
     * @return
     */
    protected int getLineNumber(int offset) {
        if (parserMode == ParserMode.COMPLETION_PARSE)
            return -1;
        int index = getCurrentFileIndex();
        if (offset >= bufferLimit[index])
            return -1;

        int lineNum = lineNumbers[index];
        int startingPoint = lineOffsets[index];

        for (int i = startingPoint; i < offset; ++i) {
            if (bufferStack[index][i] == '\n')
                ++lineNum;
        }
        if (startingPoint < offset) {
            lineNumbers[index] = lineNum;
            lineOffsets[index] = offset;
        }
        return lineNum;
    }

    protected IToken scanNumber() throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int start = bufferPos[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        boolean isFloat = buffer[start] == '.';
        boolean hasExponent = false;

        boolean isHex = false;
        boolean isOctal = false;
        boolean isMalformedOctal = false;

        if (buffer[start] == '0' && start + 1 < limit) {
            switch (buffer[start + 1]) {
            case 'x':
            case 'X':
                isHex = true;
                ++bufferPos[bufferStackPos];
                break;
            default:
                if (buffer[start + 1] > '0' && buffer[start + 1] < '7')
                    isOctal = true;
                else if (buffer[start + 1] == '8' || buffer[start + 1] == '9') {
                    isOctal = true;
                    isMalformedOctal = true;
                }
            }
        }

        while (++bufferPos[bufferStackPos] < limit) {
            int pos = bufferPos[bufferStackPos];
            switch (buffer[pos]) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                if ((buffer[pos] == '8' || buffer[pos] == '9') && isOctal) {
                    isMalformedOctal = true;
                    break;
                }

                continue;

            case '.':
                if (isLimitReached())
                    handleNoSuchCompletion();

                if (isFloat) {
                    // second dot
                    handleProblem(IProblem.SCANNER_BAD_FLOATING_POINT, start,
                            null);
                    break;
                }

                isFloat = true;
                continue;

            case 'E':
            case 'e':
                if (isHex)
                    // a hex 'e'
                    continue;

                if (hasExponent)
                    // second e
                    break;

                if (pos + 1 >= limit)
                    // ending on the e?
                    break;

                switch (buffer[pos + 1]) {
                case '+':
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    // looks like a good exponent
                    isFloat = true;
                    hasExponent = true;
                    ++bufferPos[bufferStackPos];
                    continue;
                default:
                    // no exponent number?
                    break;
                }
                break;

            case 'a':
            case 'A':
            case 'b':
            case 'B':
            case 'c':
            case 'C':
            case 'd':
            case 'D':
                if (isHex)
                    continue;

                // not ours
                break;

            case 'f':
            case 'F':
                if (isHex)
                    continue;

                // must be float suffix
                ++bufferPos[bufferStackPos];

                if (bufferPos[bufferStackPos] < buffer.length
                        && buffer[bufferPos[bufferStackPos]] == 'i')
                    continue; // handle GCC extension 5.10 Complex Numbers

                break; // fix for 77281 (used to be continue)

            case 'p':
            case 'P':
                // Hex float exponent prefix
                if (!isFloat || !isHex) {
                    --bufferPos[bufferStackPos];
                    break;
                }

                if (hasExponent)
                    // second p
                    break;

                if (pos + 1 >= limit)
                    // ending on the p?
                    break;

                switch (buffer[pos + 1]) {
                case '+':
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    // looks like a good exponent
                    isFloat = true;
                    hasExponent = true;
                    ++bufferPos[bufferStackPos];
                    continue;
                default:
                    // no exponent number?
                    break;
                }
                break;

            case 'u':
            case 'U':
            case 'L':
            case 'l':
                // unsigned suffix
                suffixLoop: while (++bufferPos[bufferStackPos] < limit) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case 'U':
                    case 'u':
                    case 'l':
                    case 'L':
                        break;
                    default:

                        break suffixLoop;
                    }
                }
                break;

            default:
                boolean success = false;
                for (int iter = 0; iter < suffixes.length; iter++)
                    if (buffer[pos] == suffixes[iter]) {
                        success = true;
                        break;
                    }
                if (success)
                    continue;
            }

            // If we didn't continue in the switch, we're done
            break;
        }

        --bufferPos[bufferStackPos];

        char[] result = CharArrayUtils.extract(buffer, start,
                bufferPos[bufferStackPos] - start + 1);
        int tokenType = isFloat ? IToken.tFLOATINGPT : IToken.tINTEGER;

        if (tokenType == IToken.tINTEGER && isHex && result.length == 2) {
            handleProblem(IProblem.SCANNER_BAD_HEX_FORMAT, start, result);
        } else if (tokenType == IToken.tINTEGER && isOctal && isMalformedOctal) {
            handleProblem(IProblem.SCANNER_BAD_OCTAL_FORMAT, start, result);
        }

        return newToken(tokenType, result);
    }

    protected boolean branchState(int state) {
        if (state != BRANCH_IF && branchStackPos == -1)
            return false;

        switch (state) {
        case BRANCH_IF:
            if (++branchStackPos == branches.length) {
                int[] temp = new int[branches.length << 1];
                System.arraycopy(branches, 0, temp, 0, branches.length);
                branches = temp;
            }
            branches[branchStackPos] = BRANCH_IF;
            return true;
        case BRANCH_ELIF:
        case BRANCH_ELSE:
            switch (branches[branchStackPos]) {
            case BRANCH_IF:
            case BRANCH_ELIF:
                branches[branchStackPos] = state;
                return true;
            default:
                return false;
            }
        case BRANCH_END:
            switch (branches[branchStackPos]) {
            case BRANCH_IF:
            case BRANCH_ELSE:
            case BRANCH_ELIF:
                --branchStackPos;
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    protected void handlePPDirective(int pos) throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int startingLineNumber = getLineNumber(pos);
        skipOverWhiteSpace();
        if (isLimitReached())
            handleCompletionOnPreprocessorDirective("#"); //$NON-NLS-1$

        // find the directive
        int start = ++bufferPos[bufferStackPos];

        // if new line or end of buffer, we're done
        if (start >= limit || buffer[start] == '\n')
            return;

        boolean problem = false;
        char c = buffer[start];
        if ((c >= 'a' && c <= 'z')) {
            while (++bufferPos[bufferStackPos] < limit) {
                c = buffer[bufferPos[bufferStackPos]];
                if ((c >= 'a' && c <= 'z') || c == '_')
                    continue;
                break;
            }
            --bufferPos[bufferStackPos];
            int len = bufferPos[bufferStackPos] - start + 1;
            if (isLimitReached())
                handleCompletionOnPreprocessorDirective(new String(buffer, pos,
                        len + 1));

            int type = ppKeywords.get(buffer, start, len);
            if (type != ppKeywords.undefined) {
                switch (type) {
                case ppInclude:
                    handlePPInclude(pos, false, startingLineNumber);
                    return;
                case ppInclude_next:
                    handlePPInclude(pos, true, startingLineNumber);
                    return;
                case ppDefine:
                    handlePPDefine(pos, startingLineNumber);
                    return;
                case ppUndef:
                    handlePPUndef(pos);
                    return;
                case ppIfdef:
                    handlePPIfdef(pos, true);
                    return;
                case ppIfndef:
                    handlePPIfdef(pos, false);
                    return;
                case ppIf:
                    start = bufferPos[bufferStackPos] + 1;
                    skipToNewLine();
                    len = bufferPos[bufferStackPos] - start;
                    if (isLimitReached())
                        handleCompletionOnExpression(CharArrayUtils.extract(
                                buffer, start, len));
                    branchState(BRANCH_IF);
                    
                    if (expressionEvaluator.evaluate(buffer, start, len,
                            definitions,
                            getLineNumber(bufferPos[bufferStackPos]),
                            getCurrentFilename()) == 0) {
                    	processIf(pos, bufferPos[bufferStackPos], true);
                        skipOverConditionalCode(true);
                        if (isLimitReached())
                            handleInvalidCompletion();
                    } else {
                    	processIf(pos, bufferPos[bufferStackPos], false);
                    }
                    return;
                case ppElse:
                case ppElif:
                    // Condition must have been true, skip over the rest

                    if (branchState(type == ppElse ? BRANCH_ELSE : BRANCH_ELIF)) {
                        if (type == ppElse)
                            processElse(pos, bufferPos[bufferStackPos] + 1,
                                    false);
                        else
                            processElsif(pos, bufferPos[bufferStackPos], false);
                        skipToNewLine();
                        skipOverConditionalCode(false);
                    } else {
                        handleProblem(
                                IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                start, ppKeywords.findKey(buffer, start, len));
                        skipToNewLine();
                    }

                    if (isLimitReached())
                        handleInvalidCompletion();
                    return;
                case ppError:
                    skipOverWhiteSpace();
                    start = bufferPos[bufferStackPos] + 1;
                    skipToNewLine();
                    if (bufferPos[bufferStackPos] - 1 > 0
                            && buffer[bufferPos[bufferStackPos] - 1] == '\r')
                        len = bufferPos[bufferStackPos] - start - 1;
                    else
                        len = bufferPos[bufferStackPos] - start;
                    handleProblem(IProblem.PREPROCESSOR_POUND_ERROR, start,
                            CharArrayUtils.extract(buffer, start, len));
                    processError(pos, pos + len);
                    break;
                case ppEndif:
                    if (!branchState(BRANCH_END))
                        handleProblem(
                                IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                start, ppKeywords.findKey(buffer, start, len));
                    processEndif(pos, bufferPos[bufferStackPos] + 1);
                    break;
                case ppPragma:
                    skipToNewLine();
                    processPragma(pos, bufferPos[bufferStackPos]);
                default:
                    problem = true;
                    break;
                }
            }
        } else
            problem = true;

        if (problem)
            handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, start, null);

        // don't know, chew up to the end of line
        // includes endif which is immatereal at this point
        skipToNewLine();
    }

    /**
     * @param startPos
     * @param endPos
     */
    protected abstract void processPragma(int startPos, int endPos);

    /**
     * @param pos
     * @param i
     */
    protected abstract void processEndif(int pos, int i);

    /**
     * @param startPos
     * @param endPos
     */
    protected abstract void processError(int startPos, int endPos);

    protected abstract void processElsif(int startPos, int endPos, boolean taken);

    protected abstract void processElse(int startPos, int endPos, boolean taken);

    /**
     * @param pos
     * @param i
     * @param b
     */
    protected abstract void processIf(int startPos, int endPos, boolean taken);

    protected void handlePPInclude(int pos2, boolean include_next,
            int startingLineNumber) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        skipOverWhiteSpace();
        int startOffset = pos2;
        int pos = ++bufferPos[bufferStackPos];
        if (pos >= limit)
            return;

        boolean local = false;
        String filename = null;

        int endOffset = startOffset;
        int nameOffset = 0;
        int nameEndOffset = 0;

        int nameLine = 0, endLine = 0;
        char c = buffer[pos];
        int start;
        int length;

        switch (c) {
        case '\n':
            return;
        case '"':
            nameLine = getLineNumber(bufferPos[bufferStackPos]);
            local = true;
            start = bufferPos[bufferStackPos] + 1;
            length = 0;
            boolean escaped = false;
            while (++bufferPos[bufferStackPos] < limit) {
                ++length;
                c = buffer[bufferPos[bufferStackPos]];
                if (c == '"') {
                    if (!escaped)
                        break;
                } else if (c == '\\') {
                    escaped = !escaped;
                    continue;
                }
                escaped = false;
            }
            --length;

            filename = new String(buffer, start, length);
            nameOffset = start;
            nameEndOffset = start + length;
            endOffset = start + length + 1;
            break;
        case '<':
            nameLine = getLineNumber(bufferPos[bufferStackPos]);
            local = false;
            start = bufferPos[bufferStackPos] + 1;
            length = 0;

            while (++bufferPos[bufferStackPos] < limit
                    && buffer[bufferPos[bufferStackPos]] != '>')
                ++length;
            endOffset = start + length + 1;
            nameOffset = start;
            nameEndOffset = start + length;

            filename = new String(buffer, start, length);
            break;
        default:
            // handle macro expansions
            int startPos = pos;
            int len = 1;
            while (++bufferPos[bufferStackPos] < limit) {
                c = buffer[bufferPos[bufferStackPos]];
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                        || c == '_' || (c >= '0' && c <= '9')
                        || Character.isUnicodeIdentifierPart(c)) {
                    ++len;
                    continue;
                } else if (c == '\\'
                        && bufferPos[bufferStackPos] + 1 < buffer.length
                        && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                    // escaped newline
                    ++bufferPos[bufferStackPos];
                    len += 2;
                    continue;
                }
                break;
            }

            Object expObject = definitions.get(buffer, startPos, len);

            if (expObject != null) {
                --bufferPos[bufferStackPos];
                char[] t = null;
                if (expObject instanceof FunctionStyleMacro) {
                    t = handleFunctionStyleMacro(
                            (FunctionStyleMacro) expObject, false);
                } else if (expObject instanceof ObjectStyleMacro) {
                    t = ((ObjectStyleMacro) expObject).expansion;
                }
                if (t != null) {
                    t = replaceArgumentMacros(t);
                    if ((t[t.length - 1] == t[0]) && (t[0] == '\"')) {
                        local = true;
                        filename = new String(t, 1, t.length - 2);
                    } else if (t[0] == '<' && t[t.length - 1] == '>') {
                        local = false;
                        filename = new String(t, 1, t.length - 2);
                    }
                }
            }
            break;
        }

        if (filename == null || filename == EMPTY_STRING) {
            handleProblem(IProblem.PREPROCESSOR_INVALID_DIRECTIVE, startOffset,
                    null);
            return;
        }
        char[] fileNameArray = filename.toCharArray();
        // TODO else we need to do macro processing on the rest of the line
        endLine = getLineNumber(bufferPos[bufferStackPos]);
        skipToNewLine();

        findAndPushInclusion(filename, fileNameArray, local, include_next, startOffset, nameOffset, nameEndOffset, endOffset, startingLineNumber, nameLine, endLine);
    }

    /**
     * @param filename
     * @param fileNameArray
     * @param local
     * @param include_next
     * @param startOffset
     * @param nameOffset
     * @param nameEndOffset
     * @param endOffset
     * @param startingLine
     * @param nameLine
     * @param endLine
     */
    protected void findAndPushInclusion(String filename, char[] fileNameArray, boolean local, boolean include_next, int startOffset, int nameOffset, int nameEndOffset, int endOffset, int startingLine, int nameLine, int endLine) {
        if (parserMode == ParserMode.QUICK_PARSE) {
            Object inclusion = createInclusionConstruct(fileNameArray,
                    EMPTY_CHAR_ARRAY, local, startOffset, startingLine,
                    nameOffset, nameEndOffset, nameLine, endOffset, endLine,
                    false);
            quickParsePushPopInclusion(inclusion);
            return;
        }
        
        CodeReader reader = null;
		// filename is an absolute path or it is a Linux absolute path on a windows machine
		if (new File(filename).isAbsolute() || filename.startsWith("/")) { //$NON-NLS-1$
		    reader = createReader( EMPTY_STRING, filename );
		    if (reader != null) {
		        pushContext(reader.buffer, new InclusionData(reader,
		                createInclusionConstruct(fileNameArray,
		                        reader.filename, local, startOffset,
		                        startingLine, nameOffset,
		                        nameEndOffset, nameLine, endOffset,
		                        endLine, false)));
		        return;
		    }
		    handleProblem(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, startOffset,
		            fileNameArray);
		    return;
		}
        
        File currentDirectory = null;
        if (local || include_next) {
            // if the include is eclosed in quotes OR we are in an include_next
            // then we need to know what the current directory is!
            File file = new File(String.valueOf(getCurrentFilename()));
            currentDirectory = file.getParentFile();
        }       
        
        if (local && !include_next) {
            // Check to see if we find a match in the current directory
            if (currentDirectory != null) {
                String absolutePath = currentDirectory.getAbsolutePath();
                reader = createReader(absolutePath, filename);
                if (reader != null) {
                    pushContext(reader.buffer, new InclusionData(reader,
                            createInclusionConstruct(fileNameArray,
                                    reader.filename, local, startOffset,
                                    startingLine, nameOffset,
                                    nameEndOffset, nameLine, endOffset,
                                    endLine, false)));
                    return;
                }
            }
        }
        // if we're not include_next, then we are looking for the
        // first occurance of the file, otherwise, we ignore all the paths
        // before
        // the
        // current directory
        
        String [] includePathsToUse = stdIncludePaths;
        if( local && locIncludePaths != null && locIncludePaths.length > 0 ) {
            includePathsToUse = new String[locIncludePaths.length + stdIncludePaths.length];
            System.arraycopy(locIncludePaths, 0, includePathsToUse, 0, locIncludePaths.length);
            System.arraycopy(stdIncludePaths, 0, includePathsToUse, locIncludePaths.length, stdIncludePaths.length);
        }
        
        if (includePathsToUse != null ) {
            int startpos = 0;
            if (include_next)
                startpos = findIncludePos(includePathsToUse, currentDirectory) + 1;
            for (int i = startpos; i < includePathsToUse.length; ++i) {
                reader = createReader(includePathsToUse[i], filename);
                if (reader != null) {
                    pushContext(reader.buffer, new InclusionData(reader,
                            createInclusionConstruct(fileNameArray,
                                    reader.filename, local, startOffset,
                                    startingLine, nameOffset,
                                    nameEndOffset, nameLine, endOffset,
                                    endLine, false)));
                    return;
                }
            }
        }
        handleProblem(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND, startOffset,
                fileNameArray);
    }

    protected abstract CodeReader createReader(String path, String fileName);
    

    private int findIncludePos(String[] paths, File currentDirectory) {
        for (int i = 0; i < paths.length; ++i)
            try {
                String path = new File(paths[i]).getCanonicalPath();
                String parent = currentDirectory.getCanonicalPath();
                if (path.equals(parent))
                    return i;
            } catch (IOException e) {
            }

        return -1;
    }

    /**
     * @param finalPath
     * @return
     */
    protected abstract CodeReader createReaderDuple(String finalPath);

    /**
     * @param inclusion
     */
    protected abstract void quickParsePushPopInclusion(Object inclusion);

    /**
     * @param fileName
     * @param local
     * @param startOffset
     * @param startingLineNumber
     * @param nameOffset
     * @param nameEndOffset
     * @param nameLine
     * @param endOffset
     * @param endLine
     * @param isForced
     * @param reader
     * @return
     */
    protected abstract Object createInclusionConstruct(char[] fileName,
            char[] filenamePath, boolean local, int startOffset,
            int startingLineNumber, int nameOffset, int nameEndOffset,
            int nameLine, int endOffset, int endLine, boolean isForced);

    protected void handlePPDefine(int pos2, int startingLineNumber) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        int startingOffset = pos2;
        int endingLine = 0, nameLine = 0;
        skipOverWhiteSpace();

        // get the Identifier
        int idstart = ++bufferPos[bufferStackPos];
        if (idstart >= limit)
            return;

        char c = buffer[idstart];
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character
                .isUnicodeIdentifierPart(c))) {
            handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN, idstart,
                    null);
            skipToNewLine();
            return;
        }

        int idlen = 1;
        while (++bufferPos[bufferStackPos] < limit) {
            c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c)) {
                ++idlen;
                continue;
            }
            break;
        }
        --bufferPos[bufferStackPos];
        nameLine = getLineNumber(bufferPos[bufferStackPos]);
        char[] name = new char[idlen];
        System.arraycopy(buffer, idstart, name, 0, idlen);

        // Now check for function style macro to store the arguments
        char[][] arglist = null;
        int pos = bufferPos[bufferStackPos];
        if (pos + 1 < limit && buffer[pos + 1] == '(') {
            ++bufferPos[bufferStackPos];
            arglist = extractMacroParameters(idstart, name, true);
            if (arglist == null)
                return;
        }

        // Capture the replacement text
        skipOverWhiteSpace();
        int textstart = bufferPos[bufferStackPos] + 1;
        int textend = textstart - 1;
        int varArgDefinitionInd = -1;

        boolean encounteredMultilineComment = false;
        boolean usesVarArgInDefinition = false;
        while (bufferPos[bufferStackPos] + 1 < limit
                && buffer[bufferPos[bufferStackPos] + 1] != '\n') {

            if (CharArrayUtils.equals(buffer, bufferPos[bufferStackPos] + 1,
                    VA_ARGS_CHARARRAY.length, VA_ARGS_CHARARRAY)) {
                usesVarArgInDefinition = true; // __VA_ARGS__ is in definition,
                                               // used
                // to check C99 6.10.3-5
                varArgDefinitionInd = bufferPos[bufferStackPos] + 1;
            }

            //16.3.2-1 Each # preprocessing token in the replacement list for a
            // function-like-macro shall
            //be followed by a parameter as the next preprocessing token
            if (arglist != null && !skipOverNonWhiteSpace(true)) {
                ++bufferPos[bufferStackPos]; //advances us to the #
                if (skipOverWhiteSpace())
                    encounteredMultilineComment = true;

                boolean isArg = false;
                if (bufferPos[bufferStackPos] + 1 < limit) {
                    ++bufferPos[bufferStackPos]; //advances us past the # (or
                                                 // last
                    // whitespace)
                    for (int i = 0; i < arglist.length && arglist[i] != null; i++) {
                        if (bufferPos[bufferStackPos] + arglist[i].length - 1 < limit) {
                            if (arglist[i].length > 3
                                    && arglist[i][arglist[i].length - 3] == '.'
                                    && arglist[i][arglist[i].length - 2] == '.'
                                    && arglist[i][arglist[i].length - 3] == '.') {
                                char[] varArgName = new char[arglist[i].length - 3];
                                System.arraycopy(arglist[i], 0, varArgName, 0,
                                        arglist[i].length - 3);
                                if (CharArrayUtils.equals(buffer,
                                        bufferPos[bufferStackPos],
                                        varArgName.length, varArgName)) {
                                    isArg = true;
                                    //advance us to the end of the arg
                                    bufferPos[bufferStackPos] += arglist[i].length - 4;
                                    break;
                                }
                            } else if (CharArrayUtils.equals(buffer,
                                    bufferPos[bufferStackPos],
                                    arglist[i].length, arglist[i])
                                    || (CharArrayUtils.equals(arglist[i],
                                            ELLIPSIS_CHARARRAY) && CharArrayUtils
                                            .equals(buffer,
                                                    bufferPos[bufferStackPos],
                                                    VA_ARGS_CHARARRAY.length,
                                                    VA_ARGS_CHARARRAY))) {
                                isArg = true;
                                //advance us to the end of the arg
                                bufferPos[bufferStackPos] += arglist[i].length - 1;
                                break;
                            }
                        }
                    }
                }
                if (!isArg)
                    handleProblem(IProblem.PREPROCESSOR_MACRO_PASTING_ERROR,
                            bufferPos[bufferStackPos], null);
            } else {
                skipOverNonWhiteSpace();
            }
            textend = bufferPos[bufferStackPos];
            if (skipOverWhiteSpace())
                encounteredMultilineComment = true;
        }

        int textlen = textend - textstart + 1;
        endingLine = getLineNumber(bufferPos[bufferStackPos]);
        char[] text = EMPTY_CHAR_ARRAY;
        if (textlen > 0) {
            text = new char[textlen];
            System.arraycopy(buffer, textstart, text, 0, textlen);
        }

        if (encounteredMultilineComment)
            text = removeMultilineCommentFromBuffer(text);
        text = removedEscapedNewline(text, 0, text.length);

        IMacro result = null;
        if (arglist == null)
            result = new ObjectStyleMacro(name, text);
        else
            result = new FunctionStyleMacro(name, text, arglist);

        // Throw it in
        definitions.put(name, result);

        if (usesVarArgInDefinition
                && definitions.get(name) instanceof FunctionStyleMacro
                && !((FunctionStyleMacro) definitions.get(name)).hasVarArgs())
            handleProblem(IProblem.PREPROCESSOR_INVALID_VA_ARGS,
                    varArgDefinitionInd, null);

        int idend = idstart + idlen;
        int textEnd = textstart + textlen;
        processMacro(name, startingOffset, startingLineNumber, idstart, idend,
                nameLine, textEnd, endingLine, result);
    }

    /**
     * @param name
     * @param startingOffset
     * @param startingLineNumber
     * @param idstart
     * @param idend
     * @param nameLine
     * @param textEnd
     * @param endingLine
     * @param macro
     *            TODO
     */
    protected abstract void processMacro(char[] name, int startingOffset,
            int startingLineNumber, int idstart, int idend, int nameLine,
            int textEnd, int endingLine,
            org.eclipse.cdt.core.parser.IMacro macro);

    protected char[][] extractMacroParameters(int idstart, char[] name,
            boolean reportProblems) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        if (bufferPos[bufferStackPos] >= limit
                || buffer[bufferPos[bufferStackPos]] != '(')
            return null;

        char c;
        char[][] arglist = new char[4][];
        int currarg = -1;
        while (bufferPos[bufferStackPos] < limit) {
            skipOverWhiteSpace();
            if (++bufferPos[bufferStackPos] >= limit)
                return null;
            c = buffer[bufferPos[bufferStackPos]];
            int argstart = bufferPos[bufferStackPos];
            if (c == ')') {
                break;
            } else if (c == ',') {
                continue;
            } else if (c == '.' && bufferPos[bufferStackPos] + 1 < limit
                    && buffer[bufferPos[bufferStackPos] + 1] == '.'
                    && bufferPos[bufferStackPos] + 2 < limit
                    && buffer[bufferPos[bufferStackPos] + 2] == '.') {
                bufferPos[bufferStackPos]--; // move back and let
                                             // skipOverIdentifier
                // handle the ellipsis
            } else if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || c == '_' || Character.isUnicodeIdentifierPart(c))) {
                if (reportProblems) {
                    handleProblem(IProblem.PREPROCESSOR_INVALID_MACRO_DEFN,
                            idstart, name);

                    // yuck
                    skipToNewLine();
                    return null;
                }
            }

            skipOverIdentifier();
            if (++currarg == arglist.length) {
                char[][] oldarglist = arglist;
                arglist = new char[oldarglist.length * 2][];
                System.arraycopy(oldarglist, 0, arglist, 0, oldarglist.length);
            }
            int arglen = bufferPos[bufferStackPos] - argstart + 1;
            char[] arg = new char[arglen];
            System.arraycopy(buffer, argstart, arg, 0, arglen);
            arglist[currarg] = arg;
        }

        return arglist;
    }

    /**
     * @param text
     * @return
     */
    protected char[] removedEscapedNewline(char[] text, int start, int len) {
        if (CharArrayUtils.indexOf('\n', text, start, len) == -1)
            return text;
        char[] result = new char[text.length];
        Arrays.fill(result, ' ');
        int counter = 0;
        for (int i = 0; i < text.length; ++i) {
            if (text[i] == '\\' && i + 1 < text.length && text[i + 1] == '\n')
                ++i;
            else if (text[i] == '\\' && i + 1 < text.length
                    && text[i + 1] == '\r' && i + 2 < text.length
                    && text[i + 2] == '\n')
                i += 2;
            else
                result[counter++] = text[i];
        }
        return CharArrayUtils.trim(result);
    }

    /**
     * @param text
     * @return
     */
    protected char[] removeMultilineCommentFromBuffer(char[] text) {
        char[] result = new char[text.length];
        Arrays.fill(result, ' ');
        int resultCount = 0;
        for (int i = 0; i < text.length; ++i) {
            if (text[i] == '/' && (i + 1 < text.length) && text[i + 1] == '*') {
                i += 2;
                while (i < text.length
                        && !(text[i] == '*' && i + 1 < text.length && text[i + 1] == '/'))
                    ++i;
                ++i;
            } else
                result[resultCount++] = text[i];

        }
        return CharArrayUtils.trim(result);
    }

    protected void handlePPUndef(int pos) throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        skipOverWhiteSpace();

        // get the Identifier
        int idstart = ++bufferPos[bufferStackPos];
        if (idstart >= limit)
            return;

        char c = buffer[idstart];
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character
                .isUnicodeIdentifierPart(c))) {
            skipToNewLine();
            return;
        }

        int idlen = 1;
        while (++bufferPos[bufferStackPos] < limit) {
            c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '_'
                    || (c >= '0' && c <= '9' || Character
                            .isUnicodeIdentifierPart(c))) {
                ++idlen;
                continue;
            }
            break;

        }
        --bufferPos[bufferStackPos];

        if (isLimitReached())
            handleCompletionOnDefinition(new String(buffer, idstart, idlen));

        skipToNewLine();
        

        Object definition = definitions.remove(buffer, idstart, idlen);
        processUndef(pos, bufferPos[bufferStackPos], CharArrayUtils.extract(buffer, idstart, idlen ), idstart, definition);
    }

    /**
     * @param pos
     * @param endPos
     * @param symbol TODO
     * @param namePos TODO
     * @param definition TODO
     */
    protected abstract void processUndef(int pos, int endPos, char[] symbol, int namePos, Object definition);

    protected void handlePPIfdef(int pos, boolean positive)
            throws EndOfFileException {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        if (isLimitReached())
            handleCompletionOnDefinition(EMPTY_STRING);

        skipOverWhiteSpace();

        if (isLimitReached())
            handleCompletionOnDefinition(EMPTY_STRING);

        // get the Identifier
        int idstart = ++bufferPos[bufferStackPos];
        if (idstart >= limit)
            return;

        char c = buffer[idstart];
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || Character
                .isUnicodeIdentifierPart(c))) {
            skipToNewLine();
            return;
        }

        int idlen = 1;
        while (++bufferPos[bufferStackPos] < limit) {
            c = buffer[bufferPos[bufferStackPos]];
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '_'
                    || (c >= '0' && c <= '9' || Character
                            .isUnicodeIdentifierPart(c))) {
                ++idlen;
                continue;
            }
            break;

        }
        --bufferPos[bufferStackPos];

        if (isLimitReached())
            handleCompletionOnDefinition(new String(buffer, idstart, idlen));

        skipToNewLine();

        branchState(BRANCH_IF);

        if ((definitions.get(buffer, idstart, idlen) != null) == positive) {
            processIfdef(pos, bufferPos[bufferStackPos], positive, true);
            return;
        }

        processIfdef(pos, bufferPos[bufferStackPos], positive, false);
        // skip over this group
        skipOverConditionalCode(true);
        if (isLimitReached())
            handleInvalidCompletion();
    }

    protected abstract void processIfdef(int startPos, int endPos,
            boolean positive, boolean taken);

    // checkelse - if potential for more, otherwise skip to endif
    protected void skipOverConditionalCode(boolean checkelse) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int nesting = 0;

        while (bufferPos[bufferStackPos] < limit) {

            skipOverWhiteSpace();

            if (++bufferPos[bufferStackPos] >= limit)
                return;

            char c = buffer[bufferPos[bufferStackPos]];
            if (c == '#') {
                int startPos = bufferPos[bufferStackPos];
                skipOverWhiteSpace();

                // find the directive
                int start = ++bufferPos[bufferStackPos];

                // if new line or end of buffer, we're done
                if (start >= limit || buffer[start] == '\n')
                    continue;

                c = buffer[start];
                if ((c >= 'a' && c <= 'z')) {
                    while (++bufferPos[bufferStackPos] < limit) {
                        c = buffer[bufferPos[bufferStackPos]];
                        if ((c >= 'a' && c <= 'z'))
                            continue;
                        break;
                    }
                    --bufferPos[bufferStackPos];
                    int len = bufferPos[bufferStackPos] - start + 1;
                    int type = ppKeywords.get(buffer, start, len);
                    if (type != ppKeywords.undefined) {
                        switch (type) {
                        case ppIfdef:
                        case ppIfndef:
                        case ppIf:
                            ++nesting;
                            branchState(BRANCH_IF);
                            skipToNewLine();
                            if (type == ppIfdef)
                                processIfdef(startPos,
                                        bufferPos[bufferStackPos], true, false);
                            else if (type == ppIfndef)
                                processIfdef(startPos,
                                        bufferPos[bufferStackPos], false, false);
                            else
                                processIf(startPos, bufferPos[bufferStackPos],
                                        false);
                            break;
                        case ppElse:
                            if (branchState(BRANCH_ELSE)) {
                                skipToNewLine();
                                if (checkelse && nesting == 0) {
                                    processElse(startPos,
                                            bufferPos[bufferStackPos], true);
                                    return;
                                }
                                processElse(startPos,
                                        bufferPos[bufferStackPos], false);
                            } else {
                                //problem, ignore this one.
                                handleProblem(
                                        IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                        start, ppKeywords.findKey(buffer,
                                                start, len));
                                skipToNewLine();
                            }
                            break;
                        case ppElif:
                            if (branchState(BRANCH_ELIF)) {
                                if (checkelse && nesting == 0) {
                                    // check the condition
                                    start = bufferPos[bufferStackPos] + 1;
                                    skipToNewLine();
                                    len = bufferPos[bufferStackPos] - start;
                                    if (expressionEvaluator
                                            .evaluate(
                                                    buffer,
                                                    start,
                                                    len,
                                                    definitions,
                                                    getLineNumber(bufferPos[bufferStackPos]),
                                                    getCurrentFilename()) != 0) {
										// condition passed, we're good
                                        processElsif(startPos,
                                                bufferPos[bufferStackPos], true);
                                        return;
                                    }
                                    processElsif(startPos,
                                            bufferPos[bufferStackPos], false);
                                } else {
                                    skipToNewLine();
                                    processElsif(startPos,
                                            bufferPos[bufferStackPos], false);
                                }
                            } else {
                                //problem, ignore this one.
                                handleProblem(
                                        IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                        start, ppKeywords.findKey(buffer,
                                                start, len));
                                skipToNewLine();
                            }
                            break;
                        case ppEndif:
                            if (branchState(BRANCH_END)) {
                                processEndif(startPos,
                                        bufferPos[bufferStackPos] + 1);
                                if (nesting > 0) {
                                    --nesting;
                                } else {
                                    skipToNewLine();
                                    return;
                                }
                            } else {
                                //problem, ignore this one.
                                handleProblem(
                                        IProblem.PREPROCESSOR_UNBALANCE_CONDITION,
                                        start, ppKeywords.findKey(buffer,
                                                start, len));
                                skipToNewLine();
                            }
                            break;
                        }
                    }
                }
            } else if (c != '\n')
                skipToNewLine();
        }
    }

    protected boolean skipOverWhiteSpace() {

        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        int pos = bufferPos[bufferStackPos];
        //		if( pos > 0 && pos < limit && buffer[pos] == '\n')
        //			return false;

        boolean encounteredMultiLineComment = false;
        while (++bufferPos[bufferStackPos] < limit) {
            pos = bufferPos[bufferStackPos];
            switch (buffer[pos]) {
            case ' ':
            case '\t':
            case '\r':
                continue;
            case '/':
                if (pos + 1 < limit) {
                    if (buffer[pos + 1] == '/') {
                        // C++ comment, skip rest of line
                        skipToNewLine(true);
                        // leave the new line there
                        --bufferPos[bufferStackPos];
                        return false;
                    } else if (buffer[pos + 1] == '*') {
                        // C comment, find closing */
                        for (bufferPos[bufferStackPos] += 2; bufferPos[bufferStackPos] < limit; ++bufferPos[bufferStackPos]) {
                            pos = bufferPos[bufferStackPos];
                            if (buffer[pos] == '*' && pos + 1 < limit
                                    && buffer[pos + 1] == '/') {
                                ++bufferPos[bufferStackPos];
                                encounteredMultiLineComment = true;
                                break;
                            }
                        }
                        continue;
                    }
                }
                break;
            case '\\':
                if (pos + 1 < limit && buffer[pos + 1] == '\n') {
                    // \n is a whitespace
                    ++bufferPos[bufferStackPos];
                    continue;
                }
                if (pos + 1 < limit && buffer[pos + 1] == '\r') {
                    if (pos + 2 < limit && buffer[pos + 2] == '\n') {
                        bufferPos[bufferStackPos] += 2;
                        continue;
                    }
                }
                break;
            }

            // fell out of switch without continuing, we're done
            --bufferPos[bufferStackPos];
            return encounteredMultiLineComment;
        }
        --bufferPos[bufferStackPos];
        return encounteredMultiLineComment;
    }

    protected int indexOfNextNonWhiteSpace(char[] buffer, int start, int limit) {
        if (start < 0 || start >= buffer.length || limit > buffer.length)
            return -1;

        int pos = start + 1;
        while (pos < limit) {
            switch (buffer[pos++]) {
            case ' ':
            case '\t':
            case '\r':
                continue;
            case '/':
                if (pos < limit) {
                    if (buffer[pos] == '/') {
                        // C++ comment, skip rest of line
                        while (++pos < limit) {
                            switch (buffer[pos]) {
                            case '\\':
                                ++pos;
                                break;
                            case '\n':
                                break;
                            }
                        }
                    } else if (buffer[pos] == '*') {
                        // C comment, find closing */
                        while (++pos < limit) {
                            if (buffer[pos] == '*' && pos + 1 < limit
                                    && buffer[pos + 1] == '/') {
                                pos += 2;
                                break;
                            }
                        }
                    }
                }
                continue;
            case '\\':
                if (pos < limit && (buffer[pos] == '\n' || buffer[pos] == '\r')) {
                    ++pos;
                    continue;
                }
            }
            // fell out of switch without continuing, we're done
            return --pos;
        }
        return pos;
    }

    protected void skipOverNonWhiteSpace() {
        skipOverNonWhiteSpace(false);
    }

    protected boolean skipOverNonWhiteSpace(boolean stopAtPound) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        while (++bufferPos[bufferStackPos] < limit) {
            switch (buffer[bufferPos[bufferStackPos]]) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                --bufferPos[bufferStackPos];
                return true;
            case '/':
                int pos = bufferPos[bufferStackPos];
                if (pos + 1 < limit && (buffer[pos + 1] == '/')
                        || (buffer[pos + 1] == '*')) {
                    --bufferPos[bufferStackPos];
                    return true;
                }
                break;

            case '\\':
                pos = bufferPos[bufferStackPos];
                if (pos + 1 < limit && buffer[pos + 1] == '\n') {
                    // \n is whitespace
                    --bufferPos[bufferStackPos];
                    return true;
                }
                if (pos + 1 < limit && buffer[pos + 1] == '\r') {
                    if (pos + 2 < limit && buffer[pos + 2] == '\n') {
                        bufferPos[bufferStackPos] += 2;
                        continue;
                    }
                }
                break;
            case '"':
                boolean escaped = false;
                if (bufferPos[bufferStackPos] - 1 > 0
                        && buffer[bufferPos[bufferStackPos] - 1] == '\\')
                    escaped = true;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escaped = !escaped;
                        continue;
                    case '"':
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        break loop;
                    case '\n':
                        if (!escaped)
                            break loop;
                    case '/':
                        if (escaped
                                && (bufferPos[bufferStackPos] + 1 < limit)
                                && (buffer[bufferPos[bufferStackPos] + 1] == '/' || buffer[bufferPos[bufferStackPos] + 1] == '*')) {
                            --bufferPos[bufferStackPos];
                            return true;
                        }

                    default:
                        escaped = false;
                    }
                }
                //if we hit the limit here, then the outer while loop will
                // advance
                //us 2 past the end and we'll back up one and still be past the
                // end,
                //so back up here as well to leave us at the last char.
                if (bufferPos[bufferStackPos] == bufferLimit[bufferStackPos])
                    bufferPos[bufferStackPos]--;
                break;
            case '\'':
                escaped = false;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escaped = !escaped;
                        continue;
                    case '\'':
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        break loop;
                    default:
                        escaped = false;
                    }
                }
                if (bufferPos[bufferStackPos] == bufferLimit[bufferStackPos])
                    bufferPos[bufferStackPos]--;

                break;
            case '#':
                if (stopAtPound) {
                    if (bufferPos[bufferStackPos] + 1 >= limit
                            || buffer[bufferPos[bufferStackPos] + 1] != '#') {
                        --bufferPos[bufferStackPos];
                        return false;
                    }
                    ++bufferPos[bufferStackPos];
                }
                break;
            }
        }
        --bufferPos[bufferStackPos];
        return true;
    }

    protected int skipOverMacroArg() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int argEnd = bufferPos[bufferStackPos]--;
        int nesting = 0;
        while (++bufferPos[bufferStackPos] < limit) {
            switch (buffer[bufferPos[bufferStackPos]]) {
            case '(':
                ++nesting;
                break;
            case ')':
                if (nesting == 0) {
                    --bufferPos[bufferStackPos];
                    return argEnd;
                }
                --nesting;
                break;
            case ',':
                if (nesting == 0) {
                    --bufferPos[bufferStackPos];
                    return argEnd;
                }
                break;
            // fix for 95119
            case '\'':
                boolean escapedChar = false;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escapedChar = !escapedChar;
                        continue;
                    case '\'':
                        if (escapedChar) {
                            escapedChar = false;
                            continue;
                        }
                        break loop;
                    default:
                       escapedChar = false;
                    }
                }
                break;
            case '"':
                boolean escaped = false;
                loop: while (++bufferPos[bufferStackPos] < bufferLimit[bufferStackPos]) {
                    switch (buffer[bufferPos[bufferStackPos]]) {
                    case '\\':
                        escaped = !escaped;
                        continue;
                    case '"':
                        if (escaped) {
                            escaped = false;
                            continue;
                        }
                        break loop;
                    default:
                        escaped = false;
                    }
                }
                break;
            }
            argEnd = bufferPos[bufferStackPos];
            skipOverWhiteSpace();
        }
        --bufferPos[bufferStackPos];
        return argEnd;
    }

    protected void skipOverIdentifier() {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];

        while (++bufferPos[bufferStackPos] < limit) {
            char c = buffer[bufferPos[bufferStackPos]];
            if (c == '.' && bufferPos[bufferStackPos] + 1 < limit
                    && buffer[bufferPos[bufferStackPos] + 1] == '.'
                    && bufferPos[bufferStackPos] + 2 < limit
                    && buffer[bufferPos[bufferStackPos] + 2] == '.') {
                // encountered "..." make sure it's the last argument, if not
                // raise
                // IProblem

                bufferPos[bufferStackPos] += 2;
                int end = bufferPos[bufferStackPos];

                while (++bufferPos[bufferStackPos] < limit) {
                    char c2 = buffer[bufferPos[bufferStackPos]];

                    if (c2 == ')') { // good
                        bufferPos[bufferStackPos] = end; // point at the end of
                                                         // ... to
                        // get the argument
                        return;
                    }

                    switch (c2) {
                    case ' ':
                    case '\t':
                    case '\r':
                        continue;
                    case '\\':
                        if (bufferPos[bufferStackPos] + 1 < limit
                                && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                            // \n is a whitespace
                            ++bufferPos[bufferStackPos];
                            continue;
                        }
                        if (bufferPos[bufferStackPos] + 1 < limit
                                && buffer[bufferPos[bufferStackPos] + 1] == '\r') {
                            if (bufferPos[bufferStackPos] + 2 < limit
                                    && buffer[bufferPos[bufferStackPos] + 2] == '\n') {
                                bufferPos[bufferStackPos] += 2;
                                continue;
                            }
                        }
                        break;
                    default:
                        // bad
                        handleProblem(
                                IProblem.PREPROCESSOR_MISSING_RPAREN_PARMLIST,
                                bufferPos[bufferStackPos], String.valueOf(c2)
                                        .toCharArray());
                        return;
                    }
                }
                // "..." was the last macro argument
                break;
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || c == '_' || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c)) {
                continue;
            }
            break; // found the end of the argument
        }

        --bufferPos[bufferStackPos];
    }

    protected void skipToNewLine() {
    	skipToNewLine(false);
    }
    
    protected void skipToNewLine(boolean insideComment) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int pos = ++bufferPos[bufferStackPos];

        if ((pos < limit && buffer[pos] == '\n') ||
				(pos+1 < limit && buffer[pos] == '\r' && buffer[pos+1] == '\n'))
            return;

        boolean escaped = false;
        while (++bufferPos[bufferStackPos] < limit) {
            switch (buffer[bufferPos[bufferStackPos]]) {
            case '/':
            	if (insideComment)
            		break;
            	
                pos = bufferPos[bufferStackPos];
                if (pos + 1 < limit && buffer[pos + 1] == '*') {
                    ++bufferPos[bufferStackPos];
                    while (++bufferPos[bufferStackPos] < limit) {
                        pos = bufferPos[bufferStackPos];
                        if (buffer[pos] == '*' && pos + 1 < limit
                                && buffer[pos + 1] == '/') {
                            ++bufferPos[bufferStackPos];
                            break;
                        }
                    }
                }
                break;
            case '\\':
                escaped = !escaped;
                continue;
            case '\n':
                if (escaped) {
                    escaped = false;
                    break;
                }
                return;
            case '\r':
                if (escaped && bufferPos[bufferStackPos] < limit
                        && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
                    escaped = false;
                    bufferPos[bufferStackPos]++;
                    break;
                } else if (!escaped && bufferPos[bufferStackPos] < limit
                        && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
//                    bufferPos[bufferStackPos]++;  // Do not want to skip past the \r
                    return;
                }
                break;
            }
            escaped = false;
        }
    }

    protected char[] handleFunctionStyleMacro(FunctionStyleMacro macro,
            boolean pushContext) {
        char[] buffer = bufferStack[bufferStackPos];
        int limit = bufferLimit[bufferStackPos];
        int start = bufferPos[bufferStackPos] - macro.name.length + 1;
        skipOverWhiteSpace();
        while (bufferPos[bufferStackPos] < limit
                && buffer[bufferPos[bufferStackPos]] == '\\'
                && bufferPos[bufferStackPos] + 1 < buffer.length
                && buffer[bufferPos[bufferStackPos] + 1] == '\n') {
            bufferPos[bufferStackPos] += 2;
            skipOverWhiteSpace();
        }

        if (++bufferPos[bufferStackPos] >= limit) {
            //allow a macro boundary cross here, but only if the caller was
            // prepared to accept a bufferStackPos change
            if (pushContext) {
                int idx = -1;
                int stackpPos = bufferStackPos;
                while (bufferData[stackpPos] != null
                        && bufferData[stackpPos] instanceof MacroData) {
                    stackpPos--;
                    if (stackpPos < 0)
                        return EMPTY_CHAR_ARRAY;
                    idx = indexOfNextNonWhiteSpace(bufferStack[stackpPos],
                            bufferPos[stackpPos], bufferLimit[stackpPos]);
                    if (idx >= bufferLimit[stackpPos])
                        continue;
                    if (idx > 0 && bufferStack[stackpPos][idx] == '(')
                        break;
                    bufferPos[bufferStackPos]--;
                    return null;
                }
                if (idx == -1) {
                    bufferPos[bufferStackPos]--;
                    return null;
                }

                MacroData data = (MacroData) bufferData[stackpPos + 1];
                for (int i = bufferStackPos; i > stackpPos; i--)
                    popContext();

                bufferPos[bufferStackPos] = idx;
                buffer = bufferStack[bufferStackPos];
                limit = bufferLimit[bufferStackPos];
                start = data.startOffset;
            } else {
                bufferPos[bufferStackPos]--;
                return null;
            }
        }

        // fix for 107150: the scanner stops at the \n or \r after skipOverWhiteSpace() take that into consideration
        while (bufferPos[bufferStackPos] + 1 < limit && (buffer[bufferPos[bufferStackPos]] == '\n' || buffer[bufferPos[bufferStackPos]] == '\r')) {
        	bufferPos[bufferStackPos]++; // skip \n or \r
        	skipOverWhiteSpace(); // skip any other spaces after the \n
        	
        	if (bufferPos[bufferStackPos] + 1 < limit && buffer[bufferPos[bufferStackPos]] != '(' && buffer[bufferPos[bufferStackPos] + 1] == '(')
        		bufferPos[bufferStackPos]++; // advance to ( if necessary
        }

        if (buffer[bufferPos[bufferStackPos]] != '(') {
            bufferPos[bufferStackPos]--;
            return null;
        }

        char[][] arglist = macro.arglist;
        int currarg = -1;
        CharArrayObjectMap argmap = new CharArrayObjectMap(arglist.length);

        boolean insideString = false;
        while (bufferPos[bufferStackPos] < limit) {
            skipOverWhiteSpace();

            if( bufferPos[bufferStackPos] + 1 >= limit )
            	break;
            
            if (buffer[++bufferPos[bufferStackPos]] == ')') {
                // end of macro
                break;
            } else if (buffer[bufferPos[bufferStackPos]] == ',') {
                continue;
            }

            if ((++currarg >= arglist.length || arglist[currarg] == null)
                    && !macro.hasVarArgs() && !macro.hasGCCVarArgs()) {
                // too many args and no variable argument
                handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR,
                        bufferPos[bufferStackPos], macro.name);
                break;
            }

            int argstart = bufferPos[bufferStackPos];

            int argend = -1;
            if ((macro.hasGCCVarArgs() || macro.hasVarArgs())
                    && currarg == macro.getVarArgsPosition()) {
                --bufferPos[bufferStackPos]; // go back to first char of macro
                                             // args

                // there are varargs and the other parms have been accounted
                // for,
                // the rest will replace __VA_ARGS__ or name where "name..." is
                // the
                // parm
                do {
                    if (buffer[bufferPos[bufferStackPos]] == '"') {
                        if (insideString)
                            insideString = false;
                        else
                            insideString = true;
                    }

                    if (!insideString
                            && buffer[bufferPos[bufferStackPos]] == ')') {
                        --bufferPos[bufferStackPos];
                        break;
                    }
                } while (++bufferPos[bufferStackPos] < limit);
                argend = bufferPos[bufferStackPos];
            } else
                argend = skipOverMacroArg();

            char[] arg = EMPTY_CHAR_ARRAY;
            int arglen = argend - argstart + 1;
            if (arglen > 0) {
                arg = new char[arglen];
                System.arraycopy(buffer, argstart, arg, 0, arglen);
            }

            argmap.put(arglist[currarg], arg);
        }

        int numRequiredArgs = arglist.length;
        for (int i = 0; i < arglist.length; i++) {
            if (arglist[i] == null) {
            	numRequiredArgs = i;
                break;
            }
        }

        /* Don't require a match for the vararg placeholder */
        /* Workaround for bugzilla 94365 */
        if (macro.hasGCCVarArgs()|| macro.hasVarArgs())
        	numRequiredArgs--;
 
        if (argmap.size() < numRequiredArgs) {
            handleProblem(IProblem.PREPROCESSOR_MACRO_USAGE_ERROR,
                    bufferPos[bufferStackPos], macro.name);
        }

        char[] result = null;
        if (macro instanceof DynamicFunctionStyleMacro) {
            result = ((DynamicFunctionStyleMacro) macro).execute(argmap);
        } else {
            CharArrayObjectMap replacedArgs = new CharArrayObjectMap(argmap
                    .size());
            int size = expandFunctionStyleMacro(macro.expansion, argmap,
                    replacedArgs, null);
            result = new char[size];
            expandFunctionStyleMacro(macro.expansion, argmap, replacedArgs,
                    result);
        }
        if (pushContext)
        {
            pushContext(result, new MacroData(start, bufferPos[bufferStackPos],
                    macro));
        }
        return result;
    }

    protected char[] replaceArgumentMacros(char[] arg) {
        int limit = arg.length;
        int start = -1, end = -1;
        Object expObject = null;
        for (int pos = 0; pos < limit; pos++) {
            char c = arg[pos];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || Character.isLetter(c)
                    || (support$Initializers && c == '$')) {
                start = pos;
                while (++pos < limit) {
                    c = arg[pos];
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                            || c == '_' || (c >= '0' && c <= '9')
                            || (support$Initializers && c == '$')
                            || Character.isUnicodeIdentifierPart(c)) {
                        continue;
                    }
                    break;
                }
                end = pos - 1;
            }

            if (start != -1 && end >= start) {
                //Check for macro expansion
                expObject = definitions.get(arg, start, (end - start + 1));
                if (expObject == null || !shouldExpandMacro((IMacro) expObject)) {
                    expObject = null;
                    start = -1;
                    continue;
                }
                //else, break and expand macro
                break;
            }
        }

        if (expObject == null)
        {
            return arg;
        }
        

        char[] expansion = null;
        if (expObject instanceof FunctionStyleMacro) {
            FunctionStyleMacro expMacro = (FunctionStyleMacro) expObject;
            pushContext((start == 0) ? arg : CharArrayUtils.extract(arg, start,
                    arg.length - start));
            bufferPos[bufferStackPos] += end - start + 1;
            expansion = handleFunctionStyleMacro(expMacro, false);
            end = bufferPos[bufferStackPos] + start;
            popContext();
        } else if (expObject instanceof ObjectStyleMacro) {
            ObjectStyleMacro expMacro = (ObjectStyleMacro) expObject;
            expansion = expMacro.expansion;
        } else if (expObject instanceof char[]) {
            expansion = (char[]) expObject;
        } else if (expObject instanceof DynamicStyleMacro) {
            DynamicStyleMacro expMacro = (DynamicStyleMacro) expObject;
            expansion = expMacro.execute();
        }

        if (expansion != null) {
            int newlength = start + expansion.length + (limit - end - 1);
            char[] result = new char[newlength];
            System.arraycopy(arg, 0, result, 0, start);
            System.arraycopy(expansion, 0, result, start, expansion.length);
            if (arg.length > end + 1)
                System.arraycopy(arg, end + 1, result,
                        start + expansion.length, limit - end - 1);

            
            beforeReplaceAllMacros();
            //we need to put the macro on the context stack in order to detect
            // recursive macros
            pushContext(EMPTY_CHAR_ARRAY,
                    new MacroData(start, start
                            + ((IMacro) expObject).getName().length,
                            (IMacro) expObject));
            arg = replaceArgumentMacros(result); //rescan for more macros
            popContext();
            afterReplaceAllMacros();
        }
        
        return arg;
    }


    /**
     * Hook for subclasses.
     */
    protected void afterReplaceAllMacros() {
        // TODO Auto-generated method stub
        
    }

    /**
     * Hook for subclasses.
     */
    protected void beforeReplaceAllMacros() {
        // TODO Auto-generated method stub
        
    }

    protected int expandFunctionStyleMacro(char[] expansion,
            CharArrayObjectMap argmap, CharArrayObjectMap replacedArgs,
            char[] result) {

        // The current position in the expansion string that we are looking at
        int pos = -1;
        // The last position in the expansion string that was copied over
        int lastcopy = -1;
        // The current write offset in the result string - also tells us the
        // length of the result string
        int outpos = 0;
        // The first character in the current block of white space - there are
        // times when we don't
        // want to copy over the whitespace
        int wsstart = -1;
        //whether or not we are on the second half of the ## operator
        boolean prevConcat = false;
        //for handling ##
        char[] prevArg = null;
        int prevArgStart = -1;
        int prevArgLength = -1;
        int prevArgTarget = -1;

        int limit = expansion.length;

        while (++pos < limit) {
            char c = expansion[pos];

            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c < '9')
                    || Character.isUnicodeIdentifierPart(c)) {

                wsstart = -1;
                int idstart = pos;
                while (++pos < limit) {
                    c = expansion[pos];
                    if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                            || (c >= '0' && c <= '9') || c == '_' || Character
                            .isUnicodeIdentifierPart(c))) {
                        break;
                    }
                }
                --pos;

                char[] repObject = (char[]) argmap.get(expansion, idstart, pos
                        - idstart + 1);

                int next = indexOfNextNonWhiteSpace(expansion, pos, limit);
                boolean nextIsPoundPound = (next + 1 < limit
                        && expansion[next] == '#' && expansion[next + 1] == '#');

                if (prevConcat && prevArgStart > -1 && prevArgLength > 0) {
                    int l1 = prevArg != null ? prevArg.length : prevArgLength;
                    int l2 = repObject != null ? repObject.length : pos
                            - idstart + 1;
                    char[] newRep = new char[l1 + l2];
                    if (prevArg != null)
                        System.arraycopy(prevArg, 0, newRep, 0, l1);
                    else
                        System
                                .arraycopy(expansion, prevArgStart, newRep, 0,
                                        l1);

                    if (repObject != null)
                        System.arraycopy(repObject, 0, newRep, l1, l2);
                    else
                        System.arraycopy(expansion, idstart, newRep, l1, l2);
                    idstart = prevArgStart;
                    repObject = newRep;
                }
                if (repObject != null) {
                    // copy what we haven't so far
                    if (++lastcopy < idstart) {
                        int n = idstart - lastcopy;
                        if (result != null)
                            System.arraycopy(expansion, lastcopy, result,
                                    outpos, n);
                        outpos += n;
                    }

                    if (prevConcat)
                        outpos = prevArgTarget;

                    if (!nextIsPoundPound) {
                        //16.3.1 completely macro replace the arguments before
                        // substituting them in
                        char[] rep = (char[]) ((replacedArgs != null) ? replacedArgs
                                .get(repObject)
                                : null);
                        
                        if (rep != null)
                            repObject = rep;
                        else {
                            rep = replaceArgumentMacros(repObject);
                            if (replacedArgs != null)
                                replacedArgs.put(repObject, rep);
                            repObject = rep;
                        }
                      
                        if (result != null )
                            System.arraycopy(repObject, 0, result, outpos, repObject.length);
                    }
                    outpos += repObject.length;

                    lastcopy = pos;
                }

                prevArg = repObject;
                prevArgStart = idstart;
                prevArgLength = pos - idstart + 1;
                prevArgTarget = repObject != null ? outpos - repObject.length
                        : outpos + idstart - lastcopy - 1;
                prevConcat = false;
            } else if (c == '"') {

                // skip over strings
                wsstart = -1;
                boolean escaped = false;
                while (++pos < limit) {
                    c = expansion[pos];
                    if (c == '"') {
                        if (!escaped)
                            break;
                    } else if (c == '\\') {
                        escaped = !escaped;
                    }
                    escaped = false;
                }
                prevConcat = false;
            } else if (c == '\'') {

                // skip over character literals
                wsstart = -1;
                boolean escaped = false;
                while (++pos < limit) {
                    c = expansion[pos];
                    if (c == '\'') {
                        if (!escaped)
                            break;
                    } else if (c == '\\') {
                        escaped = !escaped;
                    }
                    escaped = false;
                }
                prevConcat = false;
            } else if (c == ' ' || c == '\t') {
                // obvious whitespace
                if (wsstart < 0)
                    wsstart = pos;
            } else if (c == '/' && pos + 1 < limit) {

                // less than obvious, comments are whitespace
                c = expansion[++pos];
                if (c == '/') {
                    // copy up to here or before the last whitespace
                    ++lastcopy;
                    int n = wsstart < 0 ? pos - 1 - lastcopy : wsstart
                            - lastcopy;
                    if (result != null)
                        System
                                .arraycopy(expansion, lastcopy, result, outpos,
                                        n);
                    outpos += n;

                    // skip the rest
                    lastcopy = expansion.length - 1;
                } else if (c == '*') {
                    if (wsstart < 1)
                        wsstart = pos - 1;
                    while (++pos < limit) {
                        if (expansion[pos] == '*' && pos + 1 < limit
                                && expansion[pos + 1] == '/') {
                            ++pos;
                            break;
                        }
                    }
                } else
                    wsstart = -1;

            } else if (c == '\\' && pos + 1 < limit
                    && expansion[pos + 1] == 'n') {
                // skip over this
                ++pos;

            } else if (c == '#') {

                if (pos + 1 < limit && expansion[pos + 1] == '#') {
                    prevConcat = true;
                    ++pos;
                    // skip whitespace
                    if (wsstart < 0)
                        wsstart = pos - 1;
                    while (++pos < limit) {
                        switch (expansion[pos]) {
                        case ' ':
                        case '\t':
                            continue;

                        case '/':
                            if (pos + 1 < limit) {
                                c = expansion[pos + 1];
                                if (c == '/')
                                    // skip over everything
                                    pos = expansion.length;
                                else if (c == '*') {
                                    ++pos;
                                    while (++pos < limit) {
                                        if (expansion[pos] == '*'
                                                && pos + 1 < limit
                                                && expansion[pos + 1] == '/') {
                                            ++pos;
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }
                        }
                        break;
                    }
                    --pos;
                } else {
                    prevConcat = false;
                    // stringify

                    // copy what we haven't so far
                    if (++lastcopy < pos) {
                        int n = pos - lastcopy;
                        if (result != null)
                            System.arraycopy(expansion, lastcopy, result,
                                    outpos, n);
                        outpos += n;
                    }

                    // skip whitespace
                    while (++pos < limit) {
                        switch (expansion[pos]) {
                        case ' ':
                        case '\t':
                            continue;
                        case '/':
                            if (pos + 1 < limit) {
                                c = expansion[pos + 1];
                                if (c == '/')
                                    // skip over everything
                                    pos = expansion.length;
                                else if (c == '*') {
                                    ++pos;
                                    while (++pos < limit) {
                                        if (expansion[pos] == '*'
                                                && pos + 1 < limit
                                                && expansion[pos + 1] == '/') {
                                            ++pos;
                                            break;
                                        }
                                    }
                                    continue;
                                }
                            }
                        //TODO handle comments
                        }
                        break;
                    }

                    // grab the identifier
                    c = expansion[pos];
                    int idstart = pos;
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'X')
                            || c == '_' || Character.isUnicodeIdentifierPart(c)) {
                        while (++pos < limit) {
                            c = expansion[pos];
                            if (!((c >= 'a' && c <= 'z')
                                    || (c >= 'A' && c <= 'X')
                                    || (c >= '0' && c <= '9') || c == '_' || Character
                                    .isUnicodeIdentifierPart(c)))
                                break;
                        }
                    } // else TODO something
                    --pos;
                    int idlen = pos - idstart + 1;
                    char[] argvalue = (char[]) argmap.get(expansion, idstart,
                            idlen);
                    if (argvalue != null) {
                        //16.3.2-2 ... a \ character is inserted before each "
                        // and \
                        // character
                        //of a character literal or string literal

                        //technically, we are also supposed to replace each
                        // occurence
                        // of whitespace
                        //(including comments) in the argument with a single
                        // space.
                        // But, at this time
                        //we don't really care what the contents of the string
                        // are,
                        // just that we get the string
                        //so we won't bother doing that
                        if (result != null) {
                            result[outpos++] = '"';
                            for (int i = 0; i < argvalue.length; i++) {
                                if (argvalue[i] == '"' || argvalue[i] == '\\')
                                    result[outpos++] = '\\';
                                if (argvalue[i] == '\r' || argvalue[i] == '\n')
                                    result[outpos++] = ' ';
                                else
                                    result[outpos++] = argvalue[i];
                            }
                            result[outpos++] = '"';
                        } else {
                            for (int i = 0; i < argvalue.length; i++) {
                                if (argvalue[i] == '"' || argvalue[i] == '\\')
                                    ++outpos;
                                ++outpos;
                            }
                            outpos += 2;
                        }
                    }
                    lastcopy = pos;
                    wsstart = -1;
                }
            } else {
                prevConcat = false;
                // not sure what it is but it sure ain't whitespace
                wsstart = -1;
            }

        }

        if (wsstart < 0 && ++lastcopy < expansion.length) {
            int n = expansion.length - lastcopy;
            if (result != null)
                System.arraycopy(expansion, lastcopy, result, outpos, n);
            outpos += n;
        }

        return outpos;
    }

    // standard built-ins
    protected static final ObjectStyleMacro __cplusplus = new ObjectStyleMacro(
            "__cplusplus".toCharArray(), ONE); //$NON-NLS-1$ //$NON-NLS-2$

    protected static final ObjectStyleMacro __STDC__ = new ObjectStyleMacro(
            "__STDC__".toCharArray(), ONE); //$NON-NLS-1$ //$NON-NLS-2$

    protected static final ObjectStyleMacro __STDC_HOSTED__ = new ObjectStyleMacro(
            "__STDC_HOSTED_".toCharArray(), ONE); //$NON-NLS-1$ //$NON-NLS-2$

    protected static final ObjectStyleMacro __STDC_VERSION__ = new ObjectStyleMacro(
            "__STDC_VERSION_".toCharArray(), "199901L".toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

    protected final DynamicStyleMacro __FILE__ = new DynamicStyleMacro(
            "__FILE__".toCharArray()) { //$NON-NLS-1$

        public char[] execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            buffer.append(getCurrentFilename());
            buffer.append('\"');
            return buffer.toString().toCharArray();
        }
    };

    protected final DynamicStyleMacro __DATE__ = new DynamicStyleMacro(
            "__DATE__".toCharArray()) { //$NON-NLS-1$

        protected final void append(StringBuffer buffer, int value) {
            if (value < 10)
                buffer.append("0"); //$NON-NLS-1$
            buffer.append(value);
        }

        public char[] execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            buffer.append(cal.get(Calendar.MONTH));
            buffer.append(" "); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.DAY_OF_MONTH));
            buffer.append(" "); //$NON-NLS-1$
            buffer.append(cal.get(Calendar.YEAR));
            buffer.append("\""); //$NON-NLS-1$
            return buffer.toString().toCharArray();
        }
    };

    protected final DynamicStyleMacro __TIME__ = new DynamicStyleMacro(
            "__TIME__".toCharArray()) { //$NON-NLS-1$

        protected final void append(StringBuffer buffer, int value) {
            if (value < 10)
                buffer.append("0"); //$NON-NLS-1$
            buffer.append(value);
        }

        public char[] execute() {
            StringBuffer buffer = new StringBuffer("\""); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            append(buffer, cal.get(Calendar.HOUR));
            buffer.append(":"); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.MINUTE));
            buffer.append(":"); //$NON-NLS-1$
            append(buffer, cal.get(Calendar.SECOND));
            buffer.append("\""); //$NON-NLS-1$
            return buffer.toString().toCharArray();
        }
    };

    protected final DynamicStyleMacro __LINE__ = new DynamicStyleMacro(
            "__LINE__".toCharArray()) { //$NON-NLS-1$

        public char[] execute() {
            int lineNumber = lineNumbers[bufferStackPos];
            return Long.toString(lineNumber).toCharArray();
        }
    };

    protected int offsetBoundary = -1;
    
    protected boolean contentAssistMode = false;

    protected void setupBuiltInMacros(IScannerExtensionConfiguration config) {

        definitions.put(__STDC__.name, __STDC__);
        definitions.put(__FILE__.name, __FILE__);
        definitions.put(__DATE__.name, __DATE__);
        definitions.put(__TIME__.name, __TIME__);
        definitions.put(__LINE__.name, __LINE__);

        if (language == ParserLanguage.CPP)
            definitions.put(__cplusplus.name, __cplusplus);
        else {
            definitions.put(__STDC_HOSTED__.name, __STDC_HOSTED__);
            definitions.put(__STDC_VERSION__.name, __STDC_VERSION__);
        }

        CharArrayObjectMap toAdd = config.getAdditionalMacros();
        for (int i = 0; i < toAdd.size(); ++i)
            definitions.put(toAdd.keyAt(i), toAdd.getAt(i));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IScanner#setOffsetBoundary(int)
     */
    public final void setOffsetBoundary(int offset) {
        offsetBoundary = offset;
        bufferLimit[0] = offset;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScanner#setContentAssistMode(int)
	 */
	public void setContentAssistMode(int offset) {
		bufferLimit[0] = offset;
		contentAssistMode = true;
	}
	
    protected ParserLanguage getLanguage() {
        return language;
    }

    protected CodeReader getMainReader() {
        if (bufferData != null && bufferData[0] != null
                && bufferData[0] instanceof CodeReader)
            return ((CodeReader) bufferData[0]);
        return null;
    }

    public char[] getMainFilename() {
        if (bufferData != null && bufferData[0] != null
                && bufferData[0] instanceof CodeReader)
            return ((CodeReader) bufferData[0]).filename;

        return EMPTY_CHAR_ARRAY;
    }

    protected final char[] getCurrentFilename() {
        for (int i = bufferStackPos; i >= 0; --i) {
            if (bufferData[i] instanceof InclusionData)
                return ((InclusionData) bufferData[i]).reader.filename;
            if (bufferData[i] instanceof CodeReader)
                return ((CodeReader) bufferData[i]).filename;
        }
        return EMPTY_CHAR_ARRAY;
    }

    protected final int getCurrentFileIndex() {
        for (int i = bufferStackPos; i >= 0; --i) {
            if (bufferData[i] instanceof InclusionData
                    || bufferData[i] instanceof CodeReader)
                return i;
        }
        return 0;
    }

    protected final CharArrayIntMap keywords;

    protected static CharArrayIntMap ckeywords;

    protected static CharArrayIntMap cppkeywords;

    protected static CharArrayIntMap ppKeywords;

    protected static final int ppIf = 0;

    protected static final int ppIfdef = 1;

    protected static final int ppIfndef = 2;

    protected static final int ppElif = 3;

    protected static final int ppElse = 4;

    protected static final int ppEndif = 5;

    protected static final int ppInclude = 6;

    protected static final int ppDefine = 7;

    protected static final int ppUndef = 8;

    protected static final int ppError = 9;

    protected static final int ppInclude_next = 10;

    protected static final int ppPragma = 11;

    protected static final char[] TAB = { '\t' };

    protected static final char[] SPACE = { ' ' };

    private static final MacroExpansionToken EXPANSION_TOKEN = new MacroExpansionToken();

    static {
        CharArrayIntMap words = new CharArrayIntMap(IToken.tLAST, -1);

        // Common keywords
        words.put(Keywords.cAUTO, IToken.t_auto); //$NON-NLS-1$
        words.put(Keywords.cBREAK, IToken.t_break); //$NON-NLS-1$
        words.put(Keywords.cCASE, IToken.t_case); //$NON-NLS-1$
        words.put(Keywords.cCHAR, IToken.t_char); //$NON-NLS-1$
        words.put(Keywords.cCONST, IToken.t_const); //$NON-NLS-1$
        words.put(Keywords.cCONTINUE, IToken.t_continue); //$NON-NLS-1$
        words.put(Keywords.cDEFAULT, IToken.t_default); //$NON-NLS-1$
        words.put(Keywords.cDO, IToken.t_do); //$NON-NLS-1$
        words.put(Keywords.cDOUBLE, IToken.t_double); //$NON-NLS-1$
        words.put(Keywords.cELSE, IToken.t_else); //$NON-NLS-1$
        words.put(Keywords.cENUM, IToken.t_enum); //$NON-NLS-1$
        words.put(Keywords.cEXTERN, IToken.t_extern); //$NON-NLS-1$
        words.put(Keywords.cFLOAT, IToken.t_float); //$NON-NLS-1$
        words.put(Keywords.cFOR, IToken.t_for); //$NON-NLS-1$
        words.put(Keywords.cGOTO, IToken.t_goto); //$NON-NLS-1$
        words.put(Keywords.cIF, IToken.t_if); //$NON-NLS-1$
        words.put(Keywords.cINLINE, IToken.t_inline); //$NON-NLS-1$
        words.put(Keywords.cINT, IToken.t_int); //$NON-NLS-1$
        words.put(Keywords.cLONG, IToken.t_long); //$NON-NLS-1$
        words.put(Keywords.cREGISTER, IToken.t_register); //$NON-NLS-1$
        words.put(Keywords.cRETURN, IToken.t_return); //$NON-NLS-1$
        words.put(Keywords.cSHORT, IToken.t_short); //$NON-NLS-1$
        words.put(Keywords.cSIGNED, IToken.t_signed); //$NON-NLS-1$
        words.put(Keywords.cSIZEOF, IToken.t_sizeof); //$NON-NLS-1$
        words.put(Keywords.cSTATIC, IToken.t_static); //$NON-NLS-1$
        words.put(Keywords.cSTRUCT, IToken.t_struct); //$NON-NLS-1$
        words.put(Keywords.cSWITCH, IToken.t_switch); //$NON-NLS-1$
        words.put(Keywords.cTYPEDEF, IToken.t_typedef); //$NON-NLS-1$
        words.put(Keywords.cUNION, IToken.t_union); //$NON-NLS-1$
        words.put(Keywords.cUNSIGNED, IToken.t_unsigned); //$NON-NLS-1$
        words.put(Keywords.cVOID, IToken.t_void); //$NON-NLS-1$
        words.put(Keywords.cVOLATILE, IToken.t_volatile); //$NON-NLS-1$
        words.put(Keywords.cWHILE, IToken.t_while); //$NON-NLS-1$
        words.put(Keywords.cASM, IToken.t_asm); //$NON-NLS-1$

        // ANSI C keywords
        ckeywords = (CharArrayIntMap) words.clone();
        ckeywords.put(Keywords.cRESTRICT, IToken.t_restrict); //$NON-NLS-1$
        ckeywords.put(Keywords.c_BOOL, IToken.t__Bool); //$NON-NLS-1$
        ckeywords.put(Keywords.c_COMPLEX, IToken.t__Complex); //$NON-NLS-1$
        ckeywords.put(Keywords.c_IMAGINARY, IToken.t__Imaginary); //$NON-NLS-1$

        // C++ Keywords
        cppkeywords = words;
        cppkeywords.put(Keywords.cBOOL, IToken.t_bool); //$NON-NLS-1$
        cppkeywords.put(Keywords.cCATCH, IToken.t_catch); //$NON-NLS-1$
        cppkeywords.put(Keywords.cCLASS, IToken.t_class); //$NON-NLS-1$
        cppkeywords.put(Keywords.cCONST_CAST, IToken.t_const_cast); //$NON-NLS-1$
        cppkeywords.put(Keywords.cDELETE, IToken.t_delete); //$NON-NLS-1$
        cppkeywords.put(Keywords.cDYNAMIC_CAST, IToken.t_dynamic_cast); //$NON-NLS-1$
        cppkeywords.put(Keywords.cEXPLICIT, IToken.t_explicit); //$NON-NLS-1$
        cppkeywords.put(Keywords.cEXPORT, IToken.t_export); //$NON-NLS-1$
        cppkeywords.put(Keywords.cFALSE, IToken.t_false); //$NON-NLS-1$
        cppkeywords.put(Keywords.cFRIEND, IToken.t_friend); //$NON-NLS-1$
        cppkeywords.put(Keywords.cMUTABLE, IToken.t_mutable); //$NON-NLS-1$
        cppkeywords.put(Keywords.cNAMESPACE, IToken.t_namespace); //$NON-NLS-1$
        cppkeywords.put(Keywords.cNEW, IToken.t_new); //$NON-NLS-1$
        cppkeywords.put(Keywords.cOPERATOR, IToken.t_operator); //$NON-NLS-1$
        cppkeywords.put(Keywords.cPRIVATE, IToken.t_private); //$NON-NLS-1$
        cppkeywords.put(Keywords.cPROTECTED, IToken.t_protected); //$NON-NLS-1$
        cppkeywords.put(Keywords.cPUBLIC, IToken.t_public); //$NON-NLS-1$
        cppkeywords.put(Keywords.cREINTERPRET_CAST, IToken.t_reinterpret_cast); //$NON-NLS-1$
        cppkeywords.put(Keywords.cSTATIC_CAST, IToken.t_static_cast); //$NON-NLS-1$
        cppkeywords.put(Keywords.cTEMPLATE, IToken.t_template); //$NON-NLS-1$
        cppkeywords.put(Keywords.cTHIS, IToken.t_this); //$NON-NLS-1$
        cppkeywords.put(Keywords.cTHROW, IToken.t_throw); //$NON-NLS-1$
        cppkeywords.put(Keywords.cTRUE, IToken.t_true); //$NON-NLS-1$
        cppkeywords.put(Keywords.cTRY, IToken.t_try); //$NON-NLS-1$
        cppkeywords.put(Keywords.cTYPEID, IToken.t_typeid); //$NON-NLS-1$
        cppkeywords.put(Keywords.cTYPENAME, IToken.t_typename); //$NON-NLS-1$
        cppkeywords.put(Keywords.cUSING, IToken.t_using); //$NON-NLS-1$
        cppkeywords.put(Keywords.cVIRTUAL, IToken.t_virtual); //$NON-NLS-1$
        cppkeywords.put(Keywords.cWCHAR_T, IToken.t_wchar_t); //$NON-NLS-1$

        // C++ operator alternative
        cppkeywords.put(Keywords.cAND, IToken.t_and); //$NON-NLS-1$
        cppkeywords.put(Keywords.cAND_EQ, IToken.t_and_eq); //$NON-NLS-1$
        cppkeywords.put(Keywords.cBITAND, IToken.t_bitand); //$NON-NLS-1$
        cppkeywords.put(Keywords.cBITOR, IToken.t_bitor); //$NON-NLS-1$
        cppkeywords.put(Keywords.cCOMPL, IToken.t_compl); //$NON-NLS-1$
        cppkeywords.put(Keywords.cNOT, IToken.t_not); //$NON-NLS-1$
        cppkeywords.put(Keywords.cNOT_EQ, IToken.t_not_eq); //$NON-NLS-1$
        cppkeywords.put(Keywords.cOR, IToken.t_or); //$NON-NLS-1$
        cppkeywords.put(Keywords.cOR_EQ, IToken.t_or_eq); //$NON-NLS-1$
        cppkeywords.put(Keywords.cXOR, IToken.t_xor); //$NON-NLS-1$
        cppkeywords.put(Keywords.cXOR_EQ, IToken.t_xor_eq); //$NON-NLS-1$

        // Preprocessor keywords
        ppKeywords = new CharArrayIntMap(16, -1);
        ppKeywords.put(Keywords.cIF, ppIf); //$NON-NLS-1$
        ppKeywords.put(Keywords.cIFDEF, ppIfdef); //$NON-NLS-1$
        ppKeywords.put(Keywords.cIFNDEF, ppIfndef); //$NON-NLS-1$
        ppKeywords.put(Keywords.cELIF, ppElif); //$NON-NLS-1$
        ppKeywords.put(Keywords.cELSE, ppElse); //$NON-NLS-1$
        ppKeywords.put(Keywords.cENDIF, ppEndif); //$NON-NLS-1$
        ppKeywords.put(Keywords.cINCLUDE, ppInclude); //$NON-NLS-1$
        ppKeywords.put(Keywords.cDEFINE, ppDefine); //$NON-NLS-1$
        ppKeywords.put(Keywords.cUNDEF, ppUndef); //$NON-NLS-1$
        ppKeywords.put(Keywords.cERROR, ppError); //$NON-NLS-1$
        ppKeywords.put(Keywords.cINCLUDE_NEXT, ppInclude_next); //$NON-NLS-1$
    }

    /**
     * @param definition
     */
    protected void handleCompletionOnDefinition(String definition)
            throws EndOfFileException {
        IASTCompletionNode node = new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.MACRO_REFERENCE, null, null,
                definition, KeywordSets.getKeywords(KeywordSetKey.EMPTY,
                        language), EMPTY_STRING, null);

        throw new OffsetLimitReachedException(node);
    }

    /**
     * @param expression2
     */
    protected void handleCompletionOnExpression(char[] buffer)
            throws EndOfFileException {

        IASTCompletionNode.CompletionKind kind = IASTCompletionNode.CompletionKind.MACRO_REFERENCE;
        int lastSpace = CharArrayUtils.lastIndexOf(SPACE, buffer);
        int lastTab = CharArrayUtils.lastIndexOf(TAB, buffer);
        int max = lastSpace > lastTab ? lastSpace : lastTab;

        char[] prefix = CharArrayUtils.trim(CharArrayUtils.extract(buffer, max,
                buffer.length - max));
        for (int i = 0; i < prefix.length; ++i) {
            char c = prefix[i];
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                    || (c >= '0' && c <= '9')
                    || Character.isUnicodeIdentifierPart(c))
                continue;
            handleInvalidCompletion();
        }
        IASTCompletionNode node = new ASTCompletionNode(
                kind,
                null,
                null,
                new String(prefix),
                KeywordSets
                        .getKeywords(
                                ((kind == IASTCompletionNode.CompletionKind.NO_SUCH_KIND) ? KeywordSetKey.EMPTY
                                        : KeywordSetKey.MACRO), language),
                EMPTY_STRING, null);

        throw new OffsetLimitReachedException(node);
    }

    protected void handleNoSuchCompletion() throws EndOfFileException {
        throw new OffsetLimitReachedException(new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.NO_SUCH_KIND, null, null,
                EMPTY_STRING, KeywordSets.getKeywords(KeywordSetKey.EMPTY,
                        language), EMPTY_STRING, null));
    }

    protected void handleInvalidCompletion() throws EndOfFileException {
        throw new OffsetLimitReachedException(new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.UNREACHABLE_CODE, null, null,
                EMPTY_STRING, KeywordSets.getKeywords(KeywordSetKey.EMPTY,
                        language), EMPTY_STRING, null));
    }

    protected void handleCompletionOnPreprocessorDirective(String prefix)
            throws EndOfFileException {
        throw new OffsetLimitReachedException(new ASTCompletionNode(
                IASTCompletionNode.CompletionKind.PREPROCESSOR_DIRECTIVE, null,
                null, prefix, KeywordSets.getKeywords(
                        KeywordSetKey.PP_DIRECTIVE, language), EMPTY_STRING,
                null));
    }

    protected int getCurrentOffset() {
        return bufferPos[bufferStackPos];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("Scanner @ file:"); //$NON-NLS-1$
        buffer.append(getCurrentFilename());
        buffer.append(" line: "); //$NON-NLS-1$
        buffer.append(getLineNumber(getCurrentOffset()));
        return buffer.toString();
    }

    protected abstract IToken newToken(int signal);

    protected abstract IToken newToken(int signal, char[] buffer);

}
