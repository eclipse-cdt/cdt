/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;

/**
 * @author jcamelon
 */
public abstract class AbstractGNUSourceCodeParser implements ISourceCodeParser {

    protected final IParserLogService log;

    protected final IScanner scanner;

    protected final ParserMode mode;

    protected final boolean supportStatementsInExpressions;

    protected final boolean supportTypeOfUnaries;

    protected final boolean supportAlignOfUnaries;

    protected final boolean supportKnRC;

    protected final boolean supportGCCOtherBuiltinSymbols;

    protected AbstractGNUSourceCodeParser(IScanner scanner,
            IParserLogService logService, ParserMode parserMode,
            boolean supportStatementsInExpressions,
            boolean supportTypeOfUnaries, boolean supportAlignOfUnaries,
            boolean supportKnRC, boolean supportGCCOtherBuiltinSymbols) {
        this.scanner = scanner;
        this.log = logService;
        this.mode = parserMode;
        this.supportStatementsInExpressions = supportStatementsInExpressions;
        this.supportTypeOfUnaries = supportTypeOfUnaries;
        this.supportAlignOfUnaries = supportAlignOfUnaries;
        this.supportKnRC = supportKnRC;
        this.supportGCCOtherBuiltinSymbols = supportGCCOtherBuiltinSymbols;
    }

    protected boolean parsePassed = true;

    protected BacktrackException backtrack = new BacktrackException();

    protected int backtrackCount = 0;

    protected final void throwBacktrack(int offset, int length)
            throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(offset, (length < 0) ? 0 : length);
        throw backtrack;
    }

    protected IToken currToken;

    protected ASTCompletionNode completionNode;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.dom.parser.ISourceCodeParser#getCompletionNode()
     */
    public ASTCompletionNode getCompletionNode() {
        return completionNode;
    }

    // Use to create the completion node
    protected ASTCompletionNode createCompletionNode(IToken token) {
        if (completionNode == null)
            completionNode = new ASTCompletionNode(token, getTranslationUnit());
        return completionNode;
    }

    /**
     * Look Ahead in the token list to see what is coming.
     * 
     * @param i
     *            How far ahead do you wish to peek?
     * @return the token you wish to observe
     * @throws EndOfFileException
     *             if looking ahead encounters EOF, throw EndOfFile
     */
    protected IToken LA(int i) throws EndOfFileException {

        if (isCancelled) {
            throw new ParseError(ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
        }

        if (i < 1) // can't go backwards
            return null;
        if (currToken == null)
            currToken = fetchToken();
        IToken retToken = currToken;
        for (; i > 1; --i) {
            retToken = retToken.getNext();
            if (retToken == null)
                retToken = fetchToken();
        }
        return retToken;
    }

    /**
     * Look ahead in the token list and return the token type.
     * 
     * @param i
     *            How far ahead do you wish to peek?
     * @return The type of that token
     * @throws EndOfFileException
     *             if looking ahead encounters EOF, throw EndOfFile
     */
    protected int LT(int i) throws EndOfFileException {
        return LA(i).getType();
    }

    protected int calculateEndOffset(IASTNode n) {
        ASTNode node = (ASTNode) n;
        return node.getOffset() + node.getLength();
    }

    /**
     * Consume the next token available, regardless of the type.
     * 
     * @return The token that was consumed and removed from our buffer.
     * @throws EndOfFileException
     *             If there is no token to consume.
     */
    protected IToken consume() throws EndOfFileException {

        if (currToken == null)
            currToken = fetchToken();
        IToken lastToken = null;
        if (currToken != null)
            lastToken = currToken;
        currToken = currToken.getNext();
        return lastToken;
    }

    /**
     * Consume the next token available only if the type is as specified.
     * 
     * @param type
     *            The type of token that you are expecting.
     * @return the token that was consumed and removed from our buffer.
     * @throws BacktrackException
     *             If LT(1) != type
     */
    protected IToken consume(int type) throws EndOfFileException,
            BacktrackException {
        if (LT(1) == type)
            return consume();
        IToken la = LA(1);
        throwBacktrack(la.getOffset(), la.getLength());
        return null;
    }

    /**
     * Fetches a token from the scanner.
     * 
     * @return the next token from the scanner
     * @throws EndOfFileException
     *             thrown when the scanner.nextToken() yields no tokens
     */
    protected IToken fetchToken() throws EndOfFileException {
        try {
            IToken value = scanner.nextToken();
            return value;
        } catch (OffsetLimitReachedException olre) {
            handleOffsetLimitException(olre);
            return null;
        }
    }

    protected boolean isCancelled = false;

    protected static final int DEFAULT_DESIGNATOR_LIST_SIZE = 4;

    protected static int parseCount = 0;

    protected void handleOffsetLimitException(
            OffsetLimitReachedException exception) throws EndOfFileException {
        if (mode != ParserMode.COMPLETION_PARSE)
            throw new EndOfFileException();
        throw exception;
    }

    protected static final char[] EMPTY_STRING = "".toCharArray(); //$NON-NLS-1$

    /**
     * Mark our place in the buffer so that we could return to it should we have
     * to.
     * 
     * @return The current token.
     * @throws EndOfFileException
     *             If there are no more tokens.
     */
    protected IToken mark() throws EndOfFileException {
        if (currToken == null)
            currToken = fetchToken();
        return currToken;
    }

    /**
     * Rollback to a previous point, reseting the queue of tokens.
     * 
     * @param mark
     *            The point that we wish to restore to.
     */
    protected void backup(IToken mark) {
        currToken = mark;
    }

    /**
     * This is the single entry point for setting parsePassed to false
     */
    protected void failParse() {
        parsePassed = false;
    }

    /**
     * /* (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.parser.IParser#cancel()
     */
    public synchronized void cancel() {
        isCancelled = true;
        scanner.cancel();
    }

    /**
     * Parse an identifier.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IToken identifier() throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.tIDENTIFIER:
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            return consume();
        default:
            throw backtrack;
        }

    }

    /**
     * @return Returns the backtrackCount.
     */
    public final int getBacktrackCount() {
        return backtrackCount;
    }

    /**
     * @param bt
     */
    protected void throwBacktrack(BacktrackException bt)
            throws BacktrackException {
        throw bt;
    }

    protected IASTProblem failParse(BacktrackException bt) {
        IASTProblem result = null;

        if (bt.getProblem() == null)
            result = createProblem(IASTProblem.SYNTAX_ERROR, bt.getOffset(), bt
                    .getLength());
        else
            result = bt.getProblem();

        failParse();
        return result;
    }

    /**
     * @param syntax_error
     * @param offset
     * @param length
     * @return
     */
    protected abstract IASTProblem createProblem(int signal, int offset,
            int length);

    /**
     * @param string
     * @param e
     */
    protected void logThrowable(String methodName, Throwable e) {
        if (e != null && log.isTracing()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Parser: Unexpected throwable in "); //$NON-NLS-1$
            buffer.append(methodName);
            buffer.append(":"); //$NON-NLS-1$
            buffer.append(e.getClass().getName());
            buffer.append("::"); //$NON-NLS-1$
            buffer.append(e.getMessage());
            buffer.append(". w/"); //$NON-NLS-1$
            buffer.append(scanner.toString());
            log.traceLog(buffer.toString());
            // log.errorLog( buffer.toString() );
        }
    }

    public String toString() {
        return scanner.toString(); //$NON-NLS-1$
    }

    /**
     * @param methodName
     * @param e
     */
    protected void logException(String methodName, Exception e) {
        if (!(e instanceof EndOfFileException) && e != null && log.isTracing()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Parser: Unexpected exception in "); //$NON-NLS-1$
            buffer.append(methodName);
            buffer.append(":"); //$NON-NLS-1$
            buffer.append(e.getClass().getName());
            buffer.append("::"); //$NON-NLS-1$
            buffer.append(e.getMessage());
            buffer.append(". w/"); //$NON-NLS-1$
            buffer.append(scanner.toString());
            log.traceLog(buffer.toString());
            // log.errorLog(buffer.toString());
        }
    }

    protected final void throwBacktrack(IASTProblem problem)
            throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(problem);
        throw backtrack;
    }


    private static final IASTNode[] EMPTY_NODE_ARRAY = new IASTNode[0];

    public IASTTranslationUnit parse() {
        long startTime = System.currentTimeMillis();
        translationUnit();
        log.traceLog("Parse " //$NON-NLS-1$
                + (++parseCount) + ": " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
                + (parsePassed ? "" : " - parse failure")); //$NON-NLS-1$ //$NON-NLS-2$
        startTime = System.currentTimeMillis();
        resolveAmbiguities();
        log.traceLog("Ambiguity resolution : " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
        ); //$NON-NLS-1$ //$NON-NLS-2$
        IASTTranslationUnit result = getTranslationUnit();
        nullifyTranslationUnit();
        return result;
    }

    protected void resolveAmbiguities() {
        getTranslationUnit().accept(createVisitor());
    }

    protected abstract ASTVisitor createVisitor();

    /**
     * 
     */
    protected abstract void nullifyTranslationUnit();

    protected IToken skipOverCompoundStatement() throws BacktrackException,
            EndOfFileException {
        // speed up the parser by skiping the body
        // simply look for matching brace and return
        consume(IToken.tLBRACE);
        IToken result = null;
        int depth = 1;
        while (depth > 0) {
            result = consume();
            switch (result.getType()) {
            case IToken.tRBRACE:
                --depth;
                break;
            case IToken.tLBRACE:
                ++depth;
                break;
            }
        }
        return result;
    }

    /**
     * @throws EndOfFileException
     */
    protected void errorHandling() throws EndOfFileException {
        int depth = (LT(1) == IToken.tLBRACE) ? 1 : 0;
        int type = consume().getType();
        if (type == IToken.tSEMI)
            return;
        while (!((LT(1) == IToken.tSEMI && depth == 0) || (LT(1) == IToken.tRBRACE && depth == 1))) {
            switch (LT(1)) {
            case IToken.tLBRACE:
                ++depth;
                break;
            case IToken.tRBRACE:
                --depth;
                break;
            case IToken.tEOC:
                throw new EndOfFileException();
            }
            if (depth < 0)
                return;

            consume();
        }

        // eat the SEMI/RBRACE as well
        consume();
    }

    /**
     * This function is called whenever we encounter and error that we cannot
     * backtrack out of and we still wish to try and continue on with the parse
     * to do a best-effort parse for our client.
     * 
     * @throws EndOfFileException
     *             We can potentially hit EndOfFile here as we are skipping
     *             ahead.
     */
    protected void failParseWithErrorHandling() throws EndOfFileException {
        failParse();
        errorHandling();
    }


    /**
     * @return TODO
     * @throws BacktrackException
     */
    protected IASTCompoundStatement compoundStatement()
            throws EndOfFileException, BacktrackException {
        IASTCompoundStatement result = createCompoundStatement();
        if (LT(1) == IToken.tEOC)
            return result;

        int startingOffset = consume(IToken.tLBRACE).getOffset();

        ((ASTNode) result).setOffset(startingOffset);
        result.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
        while (LT(1) != IToken.tRBRACE && LT(1) != IToken.tEOC) {
            int checkToken = LA(1).hashCode();
            try {
                IASTStatement s = statement();
                result.addStatement(s);
                s.setParent(result);
                s.setPropertyInParent(IASTCompoundStatement.NESTED_STATEMENT);
            } catch (BacktrackException b) {
                IASTProblem p = failParse(b);
                IASTProblemStatement ps = createProblemStatement();
                ps.setProblem(p);
                ((ASTNode) ps).setOffsetAndLength(((ASTNode) p).getOffset(),
                        ((ASTNode) p).getLength());
                p.setParent(ps);
                p.setPropertyInParent(IASTProblemHolder.PROBLEM);
                result.addStatement(ps);
                ps.setParent(result);
                ps.setPropertyInParent(IASTCompoundStatement.NESTED_STATEMENT);
                if (LA(1).hashCode() == checkToken)
                    failParseWithErrorHandling();
            }
        }

        IToken token = consume();
        int lastOffset = token.getEndOffset();
        ((ASTNode) result).setLength(lastOffset - startingOffset);

        return result;
    }

    /**
     * @return
     */
    protected abstract IASTProblemStatement createProblemStatement();

    /**
     * @return
     */
    protected abstract IASTCompoundStatement createCompoundStatement();

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTExpression compoundStatementExpression()
            throws EndOfFileException, BacktrackException {
        int startingOffset = consume(IToken.tLPAREN).getOffset();
        IASTCompoundStatement compoundStatement = null;
        if (mode == ParserMode.QUICK_PARSE
                || mode == ParserMode.STRUCTURAL_PARSE)
            skipOverCompoundStatement();
        else if (mode == ParserMode.COMPLETION_PARSE
                || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                compoundStatement();
            else
                skipOverCompoundStatement();
        } else if (mode == ParserMode.COMPLETE_PARSE)
            compoundStatement = compoundStatement();

        int lastOffset = consume(IToken.tRPAREN).getEndOffset();
        IGNUASTCompoundStatementExpression resultExpression = createCompoundStatementExpression();
        ((ASTNode) resultExpression).setOffsetAndLength(startingOffset,
                lastOffset - startingOffset);
        if (compoundStatement != null) {
            resultExpression.setCompoundStatement(compoundStatement);
            compoundStatement.setParent(resultExpression);
            compoundStatement
                    .setPropertyInParent(IGNUASTCompoundStatementExpression.STATEMENT);
        }

        return resultExpression;
    }

    /**
     * @return
     */
    protected abstract IGNUASTCompoundStatementExpression createCompoundStatementExpression();

    protected IASTExpression expression() throws BacktrackException,
            EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();

        if (la.getType() == IToken.tLPAREN && LT(2) == IToken.tLBRACE
                && supportStatementsInExpressions) {
            IASTExpression resultExpression = compoundStatementExpression();
            if (resultExpression != null)
                return resultExpression;
        }

        IASTExpression assignmentExpression = assignmentExpression();
        if (LT(1) != IToken.tCOMMA)
            return assignmentExpression;

        IASTExpressionList expressionList = createExpressionList();
        ((ASTNode) expressionList).setOffset(startingOffset);
        expressionList.addExpression(assignmentExpression);
        assignmentExpression.setParent(expressionList);
        assignmentExpression
                .setPropertyInParent(IASTExpressionList.NESTED_EXPRESSION);

        int lastOffset = 0;
        while (LT(1) == IToken.tCOMMA) {
            consume(IToken.tCOMMA);
            IASTExpression secondExpression = assignmentExpression();
            expressionList.addExpression(secondExpression);
            secondExpression.setParent(expressionList);
            secondExpression
                    .setPropertyInParent(IASTExpressionList.NESTED_EXPRESSION);
            lastOffset = calculateEndOffset(secondExpression);
        }
        ((ASTNode) expressionList).setLength(lastOffset - startingOffset);
        return expressionList;
    }

    /**
     * @return
     */
    protected abstract IASTExpressionList createExpressionList();

    protected abstract IASTExpression assignmentExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression relationalExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression multiplicativeExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTTypeId typeId(boolean forNewExpression)
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression castExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression unaryExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression buildTypeIdExpression(int op,
            IASTTypeId typeId, int startingOffset, int endingOffset);

    protected abstract void translationUnit();

    protected abstract IASTTranslationUnit getTranslationUnit();

    protected IASTExpression assignmentOperatorExpression(int kind,
            IASTExpression lhs) throws EndOfFileException, BacktrackException {
        consume();
        IASTExpression rhs = assignmentExpression();
        return buildBinaryExpression(kind, lhs, rhs, calculateEndOffset(rhs));
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression constantExpression() throws BacktrackException,
            EndOfFileException {
        return conditionalExpression();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression logicalOrExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = logicalAndExpression();
        while (LT(1) == IToken.tOR) {
            consume(IToken.tOR);
            IASTExpression secondExpression = logicalAndExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_logicalOr, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression logicalAndExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = inclusiveOrExpression();
        while (LT(1) == IToken.tAND) {
            consume(IToken.tAND);
            IASTExpression secondExpression = inclusiveOrExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_logicalAnd, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression inclusiveOrExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = exclusiveOrExpression();
        while (LT(1) == IToken.tBITOR) {
            consume(IToken.tBITOR);
            IASTExpression secondExpression = exclusiveOrExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryOr, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression exclusiveOrExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = andExpression();
        while (LT(1) == IToken.tXOR) {
            consume(IToken.tXOR);
            IASTExpression secondExpression = andExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryXor, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression andExpression() throws EndOfFileException,
            BacktrackException {

        IASTExpression firstExpression = equalityExpression();
        while (LT(1) == IToken.tAMPER) {
            consume();
            IASTExpression secondExpression = equalityExpression();
            firstExpression = buildBinaryExpression(
                    IASTBinaryExpression.op_binaryAnd, firstExpression,
                    secondExpression, calculateEndOffset(secondExpression));
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression equalityExpression() throws EndOfFileException,
            BacktrackException {
        IASTExpression firstExpression = relationalExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tEQUAL:
            case IToken.tNOTEQUAL:
                IToken t = consume();
                int operator = ((t.getType() == IToken.tEQUAL) ? IASTBinaryExpression.op_equals
                        : IASTBinaryExpression.op_notequals);
                IASTExpression secondExpression = relationalExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    protected IASTExpression buildBinaryExpression(int operator,
            IASTExpression firstExpression, IASTExpression secondExpression,
            int lastOffset) {
        IASTBinaryExpression result = createBinaryExpression();
        result.setOperator(operator);
        int o = ((ASTNode) firstExpression).getOffset();
        ((ASTNode) result).setOffsetAndLength(o, lastOffset - o);
        result.setOperand1(firstExpression);
        firstExpression.setParent(result);
        firstExpression.setPropertyInParent(IASTBinaryExpression.OPERAND_ONE);
        result.setOperand2(secondExpression);
        secondExpression.setParent(result);
        secondExpression.setPropertyInParent(IASTBinaryExpression.OPERAND_TWO);
        return result;
    }

    /**
     * @return
     */
    protected abstract IASTBinaryExpression createBinaryExpression();

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression shiftExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = additiveExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tSHIFTL:
            case IToken.tSHIFTR:
                IToken t = consume();
                int operator = t.getType() == IToken.tSHIFTL ? IASTBinaryExpression.op_shiftLeft
                        : IASTBinaryExpression.op_shiftRight;
                IASTExpression secondExpression = additiveExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression additiveExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = multiplicativeExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tPLUS:
            case IToken.tMINUS:
                IToken t = consume();
                int operator = (t.getType() == IToken.tPLUS) ? IASTBinaryExpression.op_plus
                        : IASTBinaryExpression.op_minus;
                IASTExpression secondExpression = multiplicativeExpression();
                firstExpression = buildBinaryExpression(operator,
                        firstExpression, secondExpression,
                        calculateEndOffset(secondExpression));
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * @param expression
     * @return
     * @throws BacktrackException
     */
    protected IASTExpression conditionalExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = logicalOrExpression();
        if (LT(1) == IToken.tQUESTION) {
            consume(IToken.tQUESTION);
            IASTExpression secondExpression = expression();
            IASTExpression thirdExpression = null;
            
            if (LT(1) != IToken.tEOC) {
                consume(IToken.tCOLON);
                thirdExpression = assignmentExpression();
            }
            
            IASTConditionalExpression result = createConditionalExpression();
            result.setLogicalConditionExpression(firstExpression);
            firstExpression.setParent(result);
            firstExpression
                    .setPropertyInParent(IASTConditionalExpression.LOGICAL_CONDITION);
            result.setPositiveResultExpression(secondExpression);
            secondExpression.setParent(result);
            secondExpression
                    .setPropertyInParent(IASTConditionalExpression.POSITIVE_RESULT);
            if (thirdExpression != null) {
                result.setNegativeResultExpression(thirdExpression);
                thirdExpression.setParent(result);
                thirdExpression
                        .setPropertyInParent(IASTConditionalExpression.NEGATIVE_RESULT);
                ((ASTNode) result).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), calculateEndOffset(thirdExpression)
                        - ((ASTNode) firstExpression).getOffset());
            }
            
            return result;
        }
        return firstExpression;
    }

    /**
     * @return
     */
    protected abstract IASTConditionalExpression createConditionalExpression();

    /**
     * @param operator
     * @param operand
     * @param offset
     *            TODO
     * @param lastOffset
     *            TODO
     * @return
     */
    protected IASTExpression buildUnaryExpression(int operator,
            IASTExpression operand, int offset, int lastOffset) {
        IASTUnaryExpression result = createUnaryExpression();
        ((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
        result.setOperator(operator);
        if (operand != null) {
            result.setOperand(operand);
            operand.setParent(result);
            operand.setPropertyInParent(IASTUnaryExpression.OPERAND);
        }
        return result;
    }

    /**
     * @return
     */
    protected abstract IASTUnaryExpression createUnaryExpression();

    /**
     * @return
     * @throws BacktrackException
     * @throws EndOfFileException
     */
    protected IASTExpression unaryAlignofExpression()
            throws EndOfFileException, BacktrackException {
        int offset = consume(IGCCToken.t___alignof__).getOffset();
        IASTTypeId d = null;
        IASTExpression unaryExpression = null;

        IToken m = mark();
        int lastOffset = 0;
        if (LT(1) == IToken.tLPAREN) {
            try {
                consume(IToken.tLPAREN);
                d = typeId(false);
                lastOffset = consume(IToken.tRPAREN).getEndOffset();
            } catch (BacktrackException bt) {
                backup(m);
                d = null;
                unaryExpression = unaryExpression();
                lastOffset = calculateEndOffset(unaryExpression);
            }
        } else {
            unaryExpression = unaryExpression();
            lastOffset = calculateEndOffset(unaryExpression);
        }
        if (d != null & unaryExpression == null)
            return buildTypeIdExpression(IGNUASTTypeIdExpression.op_alignof, d,
                    offset, lastOffset);
        else if (unaryExpression != null && d == null)
            return buildUnaryExpression(IGNUASTUnaryExpression.op_alignOf,
                    unaryExpression, offset, lastOffset);
        return null;
    }

    protected IASTExpression unaryTypeofExpression() throws EndOfFileException,
            BacktrackException {
        int offset = consume(IGCCToken.t_typeof).getOffset();
        IASTTypeId d = null;
        IASTExpression unaryExpression = null;

        IToken m = mark();
        int lastOffset = 0;
        if (LT(1) == IToken.tLPAREN) {
            if (LT(2) == IToken.tLBRACE) {
                unaryExpression = compoundStatementExpression();
                lastOffset = calculateEndOffset(unaryExpression);
            } else
                try {
                    consume(IToken.tLPAREN);
                    d = typeId(false);
                    lastOffset = consume(IToken.tRPAREN).getEndOffset();
                } catch (BacktrackException bt) {
                    backup(m);
                    d = null;
                    unaryExpression = unaryExpression();
                    lastOffset = calculateEndOffset(unaryExpression);
                }
        } else {
            unaryExpression = unaryExpression();
            lastOffset = calculateEndOffset(unaryExpression);
        }
        if (d != null & unaryExpression == null)
            return buildTypeIdExpression(IGNUASTTypeIdExpression.op_typeof, d,
                    offset, lastOffset);
        else if (unaryExpression != null && d == null)
            return buildUnaryExpression(IGNUASTUnaryExpression.op_typeof,
                    unaryExpression, offset, lastOffset);
        return null;
    }

    protected IASTStatement handleFunctionBody() throws BacktrackException,
            EndOfFileException {
        if (mode == ParserMode.QUICK_PARSE
                || mode == ParserMode.STRUCTURAL_PARSE) {
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last
                    .getEndOffset()
                    - curr.getOffset());
            return cs;
        } else if (mode == ParserMode.COMPLETION_PARSE
                || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                return functionBody();
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last
                    .getEndOffset()
                    - curr.getOffset());
            return cs;
        } else if (mode == ParserMode.COMPLETE_PARSE)
            return functionBody();
        return null;
    }

    /**
     * Parses a function body.
     * 
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTStatement functionBody() throws EndOfFileException,
            BacktrackException {
        return compoundStatement();
    }

    protected abstract IASTDeclarator initDeclarator()
            throws EndOfFileException, BacktrackException;

    /**
     * @param flags
     *            input flags that are used to make our decision
     * @throws FoundDeclaratorException 
     * @throws
     * @throws EndOfFileException
     *             we could encounter EOF while looking ahead
     */
    protected void lookAheadForDeclarator(Flags flags) throws FoundDeclaratorException {
        if (flags.typeId)
            return;
        IToken mark = null;
        try {
            mark = mark();
        } catch (EndOfFileException eof) {
            return;
        }
        try
        {
            if( LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tIDENTIFIER )
                return;
        }
        catch( EndOfFileException eof )
        {
            backup( mark );
            return;
        }
        try {
            IASTDeclarator d = initDeclarator();
            IToken la = LA(1);
            backup(mark);
            if (la == null || la.getType() == IToken.tEOC)
                return;
            final ASTNode n = ((ASTNode) d);
            final int length = n.getLength();
            final int offset = n.getOffset();
            if (length == 0)
                return;
            if (flags.parm) {
                ASTNode name = (ASTNode) d.getName();
                if (name.getOffset() == offset && name.getLength() == length)
                    return;
                if (d.getInitializer() != null) {
                    ASTNode init = (ASTNode) d.getInitializer();
                    if (name.getOffset() == offset
                            && n.getOffset() + n.getLength() == init
                                    .getOffset()
                                    + init.getLength())
                        return;
                }

                switch (la.getType()) {
                case IToken.tCOMMA:
                case IToken.tRPAREN:
                    throw new FoundDeclaratorException( d, la );
                default:
                    return;
                }
            }

            checkTokenVsDeclarator(la, d);
            return;
        } catch (BacktrackException bte) {
            backup(mark);
            return;
        } catch (EndOfFileException e) {
            backup(mark);
            return;
        }
    }

    protected void checkTokenVsDeclarator(IToken la, IASTDeclarator d) throws FoundDeclaratorException {
        switch (la.getType()) {
        case IToken.tCOMMA:
        case IToken.tLBRACE:
            throw new FoundDeclaratorException( d, la );
        case IToken.tSEMI:
            if (d instanceof IASTFieldDeclarator)
                return;
            throw new FoundDeclaratorException( d, la );
        default:
            return;
        }
    }

    public static class FoundDeclaratorException extends Exception
    {
        public final IASTDeclarator declarator;
        public final IToken currToken;
        public IASTDeclSpecifier declSpec;
        
        public FoundDeclaratorException( IASTDeclarator d, IToken t )
        {
            this.declarator = d;
            this.currToken =t;
        }
    }
    
    public static class Flags {
        private boolean encounteredTypename = false;

        // have we encountered a typeName yet?
        private boolean encounteredRawType = false;

        // have we encountered a raw type yet?
        boolean parm = false;

        // is this for a simpleDeclaration or parameterDeclaration?
        boolean constructor = false;

        boolean typeId = false;

        // are we attempting the constructor strategy?
        public Flags(boolean parm, boolean c, boolean t) {
            this.parm = parm;
            constructor = c;
            typeId = t;
        }

        public Flags(boolean parm, boolean typeId) {
            this(parm, false, typeId);
        }

        /**
         * @return true if we have encountered a simple type up to this point,
         *         false otherwise
         */
        public boolean haveEncounteredRawType() {
            return encounteredRawType;
        }

        /**
         * @return true if we have encountered a typename up to this point,
         *         false otherwise
         */
        public boolean haveEncounteredTypename() {
            return encounteredTypename;
        }

        /**
         * @param b -
         *            set to true if we encounter a raw type (int, short, etc.)
         */
        public void setEncounteredRawType(boolean b) {
            encounteredRawType = b;
        }

        /**
         * @param b -
         *            set to true if we encounter a typename
         */
        public void setEncounteredTypename(boolean b) {
            encounteredTypename = b;
        }

        /**
         * @return true if we are parsing for a ParameterDeclaration
         */
        public boolean isForParameterDeclaration() {
            return parm;
        }

        /**
         * @return whether or not we are attempting the constructor strategy or
         *         not
         */
        public boolean isForConstructor() {
            return constructor;
        }
    }

    /**
     * Parse an enumeration specifier, as according to the ANSI specs in C &
     * C++. enumSpecifier: "enum" (name)? "{" (enumerator-list) "}"
     * enumerator-list: enumerator-definition enumerator-list ,
     * enumerator-definition enumerator-definition: enumerator enumerator =
     * constant-expression enumerator: identifier
     * 
     * @param owner
     *            IParserCallback object that represents the declaration that
     *            owns this type specifier.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTEnumerationSpecifier enumSpecifier()
            throws BacktrackException, EndOfFileException {
        IToken mark = mark();
        IASTName name = null;
        int startOffset = consume(IToken.t_enum).getOffset();
        if (LT(1) == IToken.tIDENTIFIER) {
            name = createName(identifier());
        } else
            name = createName();
        if (LT(1) == IToken.tLBRACE) {

            IASTEnumerationSpecifier result = createEnumerationSpecifier();
            ((ASTNode) result).setOffset(startOffset);
            result.setName(name);
            name.setParent(result);
            name.setPropertyInParent(IASTEnumerationSpecifier.ENUMERATION_NAME);

            consume(IToken.tLBRACE);
            enumLoop: while (true) {
                
                switch (LT(1)) {
                case IToken.tRBRACE:
                case IToken.tEOC:
                    break enumLoop;
                }

                IASTName enumeratorName = null;

                int lastOffset = 0;
                if (LT(1) == IToken.tIDENTIFIER) {
                    enumeratorName = createName(identifier());
                    lastOffset = calculateEndOffset(enumeratorName);
                } else {
                    IToken la = LA(1);
                    throwBacktrack(la.getOffset(), la.getLength());
                }
                IASTExpression initialValue = null;
                if (LT(1) == IToken.tASSIGN) {
                    consume(IToken.tASSIGN);
                    initialValue = constantExpression();
                    lastOffset = calculateEndOffset(initialValue);
                }
                IASTEnumerationSpecifier.IASTEnumerator enumerator = null;
                if (LT(1) == IToken.tRBRACE) {
                    enumerator = createEnumerator();
                    enumerator.setName(enumeratorName);
                    ((ASTNode) enumerator).setOffsetAndLength(
                            ((ASTNode) enumeratorName).getOffset(), lastOffset
                                    - ((ASTNode) enumeratorName).getOffset());
                    enumeratorName.setParent(enumerator);
                    enumeratorName
                            .setPropertyInParent(IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME);
                    if (initialValue != null) {
                        enumerator.setValue(initialValue);
                        initialValue.setParent(enumerator);
                        initialValue
                                .setPropertyInParent(IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_VALUE);
                    }
                    result.addEnumerator(enumerator);
                    enumerator.setParent(result);
                    enumerator
                            .setPropertyInParent(IASTEnumerationSpecifier.ENUMERATOR);

                    break;
                }
                
                switch (LT(1)) {
                case IToken.tCOMMA:
                case IToken.tEOC:
                    consume();
                    break;
                default:
                    throwBacktrack(mark.getOffset(), mark.getLength());
                }
                
                enumerator = createEnumerator();
                enumerator.setName(enumeratorName);
                ((ASTNode) enumerator).setOffsetAndLength(
                        ((ASTNode) enumeratorName).getOffset(), lastOffset
                                - ((ASTNode) enumeratorName).getOffset());
                enumeratorName.setParent(enumerator);
                enumeratorName
                        .setPropertyInParent(IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME);
                if (initialValue != null) {
                    enumerator.setValue(initialValue);
                    initialValue.setParent(enumerator);
                    initialValue
                            .setPropertyInParent(IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_VALUE);
                }
                result.addEnumerator(enumerator);
                enumerator.setParent(result);
                enumerator
                        .setPropertyInParent(IASTEnumerationSpecifier.ENUMERATOR);
            }
            
            int lastOffset = consume().getEndOffset();
            ((ASTNode) result).setLength(lastOffset - startOffset);
            return result;
        }
        // enumSpecifierAbort
        backup(mark);
        throwBacktrack(mark.getOffset(), mark.getLength());
        return null;
    }

    protected abstract IASTStatement statement() throws EndOfFileException,
            BacktrackException;

    /**
     * @return
     */
    protected abstract IASTEnumerator createEnumerator();

    /**
     * @return
     */
    protected abstract IASTEnumerationSpecifier createEnumerationSpecifier();

    /**
     * @return
     */
    protected abstract IASTName createName();

    /**
     * @param token
     * @return
     */
    protected abstract IASTName createName(IToken token);

    /**
     * @throws BacktrackException
     */
    protected IASTExpression condition() throws BacktrackException,
            EndOfFileException {
        IASTExpression cond = expression();
        return cond;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.ISourceCodeParser#encounteredError()
     */
    public boolean encounteredError() {
        return !parsePassed;
    }

    protected abstract IASTSimpleDeclaration createSimpleDeclaration();

    protected abstract IASTNamedTypeSpecifier createNamedTypeSpecifier();

    /**
     * @return
     */
    protected abstract IASTDeclarationStatement createDeclarationStatement();

    /**
     * @return
     */
    protected abstract IASTExpressionStatement createExpressionStatement();

    /**
     * @return
     */
    protected abstract IASTLabelStatement createLabelStatement();

    /**
     * @return
     */
    protected abstract IASTNullStatement createNullStatement();

    /**
     * @return
     */
    protected abstract IASTGotoStatement createGoToStatement();

    /**
     * @return
     */
    protected abstract IASTReturnStatement createReturnStatement();

    /**
     * @return
     */
    protected abstract IASTContinueStatement createContinueStatement();

    /**
     * @return
     */
    protected abstract IASTBreakStatement createBreakStatement();

    /**
     * @return
     */
    protected abstract IASTForStatement createForStatement();

    /**
     * @return
     */
    protected abstract IASTDoStatement createDoStatement();

    /**
     * @return
     */
    protected abstract IASTWhileStatement createWhileStatement();

    /**
     * @return
     */
    protected abstract IASTIdExpression createIdExpression();

    /**
     * @return
     */
    protected abstract IASTDefaultStatement createDefaultStatement();

    /**
     * @return
     */
    protected abstract IASTCaseStatement createCaseStatement();

    protected abstract IASTDeclaration declaration() throws BacktrackException,
            EndOfFileException;



    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTDeclaration asmDeclaration() throws EndOfFileException,
            BacktrackException {
        IToken first = consume(IToken.t_asm);
        consume(IToken.tLPAREN);
        String assembly = consume(IToken.tSTRING).getImage();
        consume(IToken.tRPAREN);
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        return buildASMDirective(first.getOffset(), assembly, lastOffset);
    }

    /**
     * @param offset
     * @param assembly
     * @param lastOffset
     *            TODO
     * @return
     */
    protected IASTASMDeclaration buildASMDirective(int offset, String assembly,
            int lastOffset) {
        IASTASMDeclaration result = createASMDirective();
        ((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
        result.setAssembly(assembly);
        return result;
    }

    /**
     * @return
     */
    protected abstract IASTASMDeclaration createASMDirective();

    /**
     * @param op
     * @param typeId
     * @param subExpression
     * @param startingOffset
     * @param lastOffset
     * @return
     */
    protected IASTExpression buildTypeIdUnaryExpression(int op,
            IASTTypeId typeId, IASTExpression subExpression,
            int startingOffset, int lastOffset) {
        IASTCastExpression result = createCastExpression();
        result.setOperator(op);
        ((ASTNode) result).setOffsetAndLength(startingOffset, lastOffset
                - startingOffset);
        result.setTypeId(typeId);
        typeId.setParent(result);
        typeId.setPropertyInParent(IASTCastExpression.TYPE_ID);
        if (subExpression != null) { // which it can be in a completion
            result.setOperand(subExpression);
            subExpression.setParent(result);
            subExpression.setPropertyInParent(IASTCastExpression.OPERAND);
        }
        
        return result;
    }

    /**
     * @return
     */
    protected abstract IASTCastExpression createCastExpression();

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseDeclarationOrExpressionStatement()
            throws EndOfFileException, BacktrackException {
        // expressionStatement
        // Note: the function style cast ambiguity is handled in
        // expression
        // Since it only happens when we are in a statement
        IToken mark = mark();
        IASTExpressionStatement expressionStatement = null;
        IToken lastTokenOfExpression = null;
        BacktrackException savedBt = null;
        try {
            IASTExpression expression = expression();
            if (LT(1) == IToken.tEOC)
                lastTokenOfExpression = consume();
            else
                lastTokenOfExpression = consume(IToken.tSEMI);
            expressionStatement = createExpressionStatement();
            expressionStatement.setExpression(expression);
            ((ASTNode) expressionStatement).setOffsetAndLength(
                    mark.getOffset(), lastTokenOfExpression.getEndOffset()
                            - mark.getOffset());
            expression.setParent(expressionStatement);
            expression.setPropertyInParent(IASTExpressionStatement.EXPFRESSION);
        } catch (BacktrackException b) {
        }

        backup(mark);

        // declarationStatement
        IASTDeclarationStatement ds = null;
        try {
            IASTDeclaration d = declaration();
            ds = createDeclarationStatement();
            ds.setDeclaration(d);
            ((ASTNode) ds).setOffsetAndLength(((ASTNode) d).getOffset(),
                    ((ASTNode) d).getLength());
            d.setParent(ds);
            d.setPropertyInParent(IASTDeclarationStatement.DECLARATION);
        } catch (BacktrackException b) {
            savedBt = b;
            backup(mark);
        }

        if (expressionStatement == null && ds != null) {
            return ds;
        }
        if (expressionStatement != null && ds == null) {
            while (true) {
                if (consume() == lastTokenOfExpression)
                    break;
            }
            return expressionStatement;
        }

        if (expressionStatement == null && ds == null)
            throwBacktrack(savedBt);
        // resolve ambiguities
        // A * B = C;
        if (expressionStatement.getExpression() instanceof IASTBinaryExpression) {
            IASTBinaryExpression exp = (IASTBinaryExpression) expressionStatement
                    .getExpression();
            if (exp.getOperator() == IASTBinaryExpression.op_assign) {
                IASTExpression lhs = exp.getOperand1();
                if (lhs instanceof IASTBinaryExpression
                        && ((IASTBinaryExpression) lhs).getOperator() == IASTBinaryExpression.op_multiply) {

                    return ds;
                }
                if (lhs instanceof IASTFunctionCallExpression) {
                    // lvalue - makes no sense
                    return ds;
                }
            }
        }

        // x = y; // default to int
        // valid @ Translation Unit scope
        // but not valid as a statement in a function body
        if (ds.getDeclaration() instanceof IASTSimpleDeclaration
                && ((IASTSimpleDeclaration) ds.getDeclaration())
                        .getDeclSpecifier() instanceof IASTSimpleDeclSpecifier
                && ((IASTSimpleDeclSpecifier) ((IASTSimpleDeclaration) ds
                        .getDeclaration()).getDeclSpecifier()).getType() == IASTSimpleDeclSpecifier.t_unspecified) {
            backup(mark);
            while (true) {
                if (consume() == lastTokenOfExpression)
                    break;
            }

            return expressionStatement;
        }

        if (ds.getDeclaration() instanceof IASTAmbiguousDeclaration )
        {
            IASTAmbiguousDeclaration amb = (IASTAmbiguousDeclaration) ds.getDeclaration();
            IASTDeclaration [] ambDs = amb.getDeclarations();
            int ambCount = 0;
            for( int i = 0; i < ambDs.length; ++i )
            {
                if (ambDs[i] instanceof IASTSimpleDeclaration
                        && ((IASTSimpleDeclaration) ambDs[i])
                                .getDeclSpecifier() instanceof IASTSimpleDeclSpecifier
                        && ((IASTSimpleDeclSpecifier) ((IASTSimpleDeclaration) ambDs[i]).getDeclSpecifier()).getType() == IASTSimpleDeclSpecifier.t_unspecified) {
                    ++ambCount;
                }
            }
            if ( ambCount == ambDs.length )
            {
                backup(mark);
                while (true) {
                    if (consume() == lastTokenOfExpression)
                        break;
                }

                return expressionStatement;
            }
        }

        if (ds.getDeclaration() instanceof IASTSimpleDeclaration
                && ((IASTSimpleDeclaration) ds.getDeclaration())
                        .getDeclSpecifier() instanceof IASTNamedTypeSpecifier)

        {
            final IASTDeclarator[] declarators = ((IASTSimpleDeclaration) ds
                    .getDeclaration()).getDeclarators();
            if (declarators.length == 0
                    || (declarators.length == 1 && (declarators[0].getName()
                            .toCharArray().length == 0 && declarators[0]
                            .getNestedDeclarator() == null))) {
                backup(mark);
                while (true) {
                    if (consume() == lastTokenOfExpression)
                        break;
                }

                return expressionStatement;
            }
        }

        IASTAmbiguousStatement statement = createAmbiguousStatement();
        statement.addStatement(ds);
        ds.setParent(statement);
        ds.setPropertyInParent(IASTAmbiguousStatement.STATEMENT);
        statement.addStatement(expressionStatement);
        expressionStatement.setParent(statement);
        expressionStatement
                .setPropertyInParent(IASTAmbiguousStatement.STATEMENT);
        ((ASTNode) statement).setOffsetAndLength((ASTNode) ds);
        return statement;
    }

    protected abstract IASTAmbiguousStatement createAmbiguousStatement();

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseLabelStatement() throws EndOfFileException,
            BacktrackException {
        IToken labelName = consume(IToken.tIDENTIFIER);
        int lastOffset = consume(IToken.tCOLON).getEndOffset();
        IASTLabelStatement label_statement = createLabelStatement();
        ((ASTNode) label_statement).setOffsetAndLength(labelName.getOffset(),
                lastOffset - labelName.getOffset());
        IASTName name = createName(labelName);
        label_statement.setName(name);
        name.setParent(label_statement);
        name.setPropertyInParent(IASTLabelStatement.NAME);
        return label_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseNullStatement() throws EndOfFileException,
            BacktrackException {
        IToken t = consume(IToken.tSEMI);

        IASTNullStatement null_statement = createNullStatement();
        ((ASTNode) null_statement).setOffsetAndLength(t.getOffset(), t
                .getEndOffset()
                - t.getOffset());
        return null_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseGotoStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume(IToken.t_goto).getOffset();
        IToken identifier = consume(IToken.tIDENTIFIER);
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTName goto_label_name = createName(identifier);
        IASTGotoStatement goto_statement = createGoToStatement();
        ((ASTNode) goto_statement).setOffsetAndLength(startOffset, lastOffset
                - startOffset);
        goto_statement.setName(goto_label_name);
        goto_label_name.setParent(goto_statement);
        goto_label_name.setPropertyInParent(IASTGotoStatement.NAME);
        return goto_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseBreakStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume(IToken.t_break).getOffset();
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTBreakStatement break_statement = createBreakStatement();
        ((ASTNode) break_statement).setOffsetAndLength(startOffset, lastOffset
                - startOffset);
        return break_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseContinueStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume(IToken.t_continue).getOffset();
        int lastOffset = consume(IToken.tSEMI).getEndOffset();

        IASTContinueStatement continue_statement = createContinueStatement();
        ((ASTNode) continue_statement).setOffsetAndLength(startOffset,
                lastOffset - startOffset);
        return continue_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseReturnStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset;
        startOffset = consume(IToken.t_return).getOffset();
        IASTExpression result = null;

        // See if there is a return expression
        switch (LT(1)) {
        case IToken.tEOC:
            // We're trying to start one
            IASTName name = createName(LA(1));
            IASTIdExpression idExpr = createIdExpression();
            idExpr.setName(name);
            name.setParent(idExpr);
            name.setPropertyInParent(IASTIdExpression.ID_NAME);
            result = idExpr;
            break;
        case IToken.tSEMI:
            // None
            break;
        default:
            // Yes
            result = expression();
            break;
        }

        int lastOffset = 0;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throwBacktrack(LA(1));
        }

        IASTReturnStatement return_statement = createReturnStatement();
        ((ASTNode) return_statement).setOffsetAndLength(startOffset, lastOffset
                - startOffset);
        if (result != null) {
            return_statement.setReturnValue(result);
            result.setParent(return_statement);
            result.setPropertyInParent(IASTReturnStatement.RETURNVALUE);
        }
        return return_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseForStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset;
        startOffset = consume(IToken.t_for).getOffset();
        consume(IToken.tLPAREN);
        IASTStatement init = forInitStatement();
        IASTExpression for_condition = null;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            break;
        default:
            for_condition = condition();
        }
        switch (LT(1)) {
        case IToken.tSEMI:
            consume(IToken.tSEMI);
            break;
        case IToken.tEOC:
            break;
        default:
            throw backtrack;
        }
        IASTExpression iterationExpression = null;
        switch (LT(1)) {
        case IToken.tRPAREN:
        case IToken.tEOC:
            break;
        default:
            iterationExpression = expression();
        }
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume(IToken.tRPAREN);
            break;
        case IToken.tEOC:
            break;
        default:
            throw backtrack;
        }
        IASTForStatement for_statement = createForStatement();
        IASTStatement for_body = null;
        if (LT(1) != IToken.tEOC) {
            for_body = statement();
            ((ASTNode) for_statement).setOffsetAndLength(startOffset,
                    calculateEndOffset(for_body) - startOffset);
        }

        for_statement.setInitializerStatement(init);
        init.setParent(for_statement);
        init.setPropertyInParent(IASTForStatement.INITIALIZER);
        
        if (for_condition != null) {
            for_statement.setCondition(for_condition);
            for_condition.setParent(for_statement);
            for_condition.setPropertyInParent(IASTForStatement.CONDITION);
        }
        if (iterationExpression != null) {
            for_statement.setIterationExpression(iterationExpression);
            iterationExpression.setParent(for_statement);
            iterationExpression.setPropertyInParent(IASTForStatement.ITERATION);
        }
        if (for_body != null) {
            for_statement.setBody(for_body);
            for_body.setParent(for_statement);
            for_body.setPropertyInParent(IASTForStatement.BODY);
        }
        return for_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseDoStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset;
        startOffset = consume(IToken.t_do).getOffset();
        IASTStatement do_body = statement();

        IASTExpression do_condition = null;
        if (LT(1) != IToken.tEOC) {
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            do_condition = condition();
        }
        
        int lastOffset;
        switch (LT(1)) {
        case IToken.tRPAREN:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throw backtrack;
        }

        IASTDoStatement do_statement = createDoStatement();
        ((ASTNode) do_statement).setOffsetAndLength(startOffset, lastOffset
                - startOffset);
        do_statement.setBody(do_body);
        do_body.setParent(do_statement);
        do_body.setPropertyInParent(IASTDoStatement.BODY);
        
        if (do_condition != null) {
            do_statement.setCondition(do_condition);
            do_condition.setParent(do_statement);
            do_condition.setPropertyInParent(IASTDoStatement.CONDITION);
        }
        
        return do_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseWhileStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume(IToken.t_while).getOffset();
        consume(IToken.tLPAREN);
        IASTExpression while_condition = condition();
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(LA(1));
        }
        IASTStatement while_body = null;
        if (LT(1) != IToken.tEOC)
            while_body = statement();

        IASTWhileStatement while_statement = createWhileStatement();
        ((ASTNode) while_statement).setOffsetAndLength(startOffset,
                (while_body != null ? calculateEndOffset(while_body) : LA(1).getEndOffset()) - startOffset);
        while_statement.setCondition(while_condition);
        while_condition.setParent(while_statement);
        while_condition
                .setPropertyInParent(IASTWhileStatement.CONDITIONEXPRESSION);
        
        if (while_body != null) {
            while_statement.setBody(while_body);
            while_body.setParent(while_statement);
            while_body.setPropertyInParent(IASTWhileStatement.BODY);
        }
        
        return while_statement;
    }

    /**
     * @param result
     */
    protected void reconcileLengths(IASTIfStatement result) {
        if (result == null)
            return;
        IASTIfStatement current = result;
        while (current.getElseClause() instanceof IASTIfStatement)
            current = (IASTIfStatement) current.getElseClause();

        while (current != null) {
            ASTNode r = ((ASTNode) current);
            if (current.getElseClause() != null) {
                ASTNode else_clause = ((ASTNode) current.getElseClause());
                r.setLength(else_clause.getOffset() + else_clause.getLength()
                        - r.getOffset());
            } else {
                ASTNode then_clause = (ASTNode) current.getThenClause();
                if (then_clause != null)
                    r.setLength(then_clause.getOffset()
                            + then_clause.getLength() - r.getOffset());
            }
            if (current.getParent() != null
                    && current.getParent() instanceof IASTIfStatement)
                current = (IASTIfStatement) current.getParent();
            else
                current = null;
        }
    }

    /**
     * @return
     */
    protected abstract IASTProblemExpression createProblemExpression();

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseCompoundStatement() throws EndOfFileException,
            BacktrackException {
        IASTCompoundStatement compound = compoundStatement();
        return compound;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseDefaultStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume(IToken.t_default).getOffset();
        int lastOffset = consume(IToken.tCOLON).getEndOffset();

        IASTDefaultStatement df = createDefaultStatement();
        ((ASTNode) df)
                .setOffsetAndLength(startOffset, lastOffset - startOffset);
        return df;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseCaseStatement() throws EndOfFileException,
            BacktrackException {
        int startOffset = consume(IToken.t_case).getOffset();
        IASTExpression case_exp = constantExpression();
        int lastOffset = 0;
        switch (LT(1)) {
        case IToken.tCOLON:
        case IToken.tEOC:
            lastOffset = consume().getEndOffset();
            break;
        default:
            throwBacktrack(LA(1));
        }

        IASTCaseStatement cs = createCaseStatement();
        ((ASTNode) cs)
                .setOffsetAndLength(startOffset, lastOffset - startOffset);
        cs.setExpression(case_exp);
        case_exp.setParent(cs);
        case_exp.setPropertyInParent(IASTCaseStatement.EXPRESSION);
        return cs;
    }

    /**
     * @param declSpec
     * @param declarators
     * @return
     */
    protected int figureEndOffset(IASTDeclSpecifier declSpec,
            IASTDeclarator[] declarators) {
        if (declarators.length == 0)
            return calculateEndOffset(declSpec);
        return calculateEndOffset(declarators[declarators.length - 1]);
    }

    /**
     * @param declSpecifier
     * @param declarator
     * @return
     */
    protected int figureEndOffset(IASTDeclSpecifier declSpecifier,
            IASTDeclarator declarator) {
        if (declarator == null || ((ASTNode) declarator).getLength() == 0)
            return calculateEndOffset(declSpecifier);
        return calculateEndOffset(declarator);
    }

    /**
     * @param token
     */
    protected void throwBacktrack(IToken token) throws BacktrackException {
        throwBacktrack(token.getOffset(), token.getLength());
    }

    protected IASTNode[] parseTypeIdOrUnaryExpression(
            boolean typeIdWithParentheses) throws EndOfFileException {
        IASTTypeId typeId = null;
        IASTExpression unaryExpression = null;
        IToken typeIdLA = null, unaryExpressionLA = null;
        IToken mark = mark();
        try {
            if (typeIdWithParentheses)
                consume(IToken.tLPAREN);
            typeId = typeId(false);
            if (typeIdWithParentheses) {
                switch (LT(1)) {
                case IToken.tRPAREN:
                case IToken.tEOC:
                    consume();
                    break;
                default:
                    throw backtrack;
                }

            }
            typeIdLA = LA(1);
        } catch (BacktrackException bte) {
            typeId = null;
        }
        backup(mark);
        try {
            unaryExpression = unaryExpression();
            unaryExpressionLA = LA(1);
        } catch (BacktrackException bte) {
            unaryExpression = null;
        }
        IASTNode[] result;
        if (unaryExpression == null && typeId != null) {
            backup(typeIdLA);
            result = new IASTNode[1];
            result[0] = typeId;
            return result;
        }
        if (unaryExpression != null && typeId == null) {
            backup(unaryExpressionLA);
            result = new IASTNode[1];
            result[0] = unaryExpression;
            return result;
        }
        if (unaryExpression != null && typeId != null
                && typeIdLA == unaryExpressionLA) {
            result = new IASTNode[2];
            result[0] = typeId;
            result[1] = unaryExpression;
            return result;
        }
        return EMPTY_NODE_ARRAY;

    }

    protected abstract IASTAmbiguousExpression createAmbiguousExpression();

    protected IASTExpression parseSizeofExpression() throws BacktrackException,
            EndOfFileException {
        int startingOffset = consume(IToken.t_sizeof).getOffset();
        IASTNode[] choice = parseTypeIdOrUnaryExpression(true);
        switch (choice.length) {
        case 1:
            int lastOffset = calculateEndOffset(choice[0]);
            if (choice[0] instanceof IASTExpression)
                return buildUnaryExpression(IASTUnaryExpression.op_sizeof,
                        (IASTExpression) choice[0], startingOffset, lastOffset);
            else if (choice[0] instanceof IASTTypeId)
                return buildTypeIdExpression(IASTTypeIdExpression.op_sizeof,
                        (IASTTypeId) choice[0], startingOffset, lastOffset);
            throwBacktrack(LA(1));
            break;
        case 2:
            lastOffset = calculateEndOffset(choice[0]);
            IASTAmbiguousExpression ambExpr = createAmbiguousExpression();
            IASTExpression e1 = buildTypeIdExpression(
                    IASTTypeIdExpression.op_sizeof, (IASTTypeId) choice[0],
                    startingOffset, lastOffset);
            IASTExpression e2 = buildUnaryExpression(
                    IASTUnaryExpression.op_sizeof, (IASTExpression) choice[1],
                    startingOffset, lastOffset);
            ambExpr.addExpression(e1);
            e1.setParent(ambExpr);
            e1.setPropertyInParent(IASTAmbiguousExpression.SUBEXPRESSION);
            ambExpr.addExpression(e2);
            e2.setParent(ambExpr);
            e2.setPropertyInParent(IASTAmbiguousExpression.SUBEXPRESSION);
            ((ASTNode) ambExpr).setOffsetAndLength((ASTNode) e2);
            return ambExpr;
        default:
        }
        throwBacktrack(LA(1));
        return null;
    }
    
    protected abstract IASTDeclaration simpleDeclaration() throws BacktrackException,
    EndOfFileException;

    /**
     * @throws BacktrackException
     */
    protected IASTStatement forInitStatement() throws BacktrackException, EndOfFileException {
        if( LT(1) == IToken.tSEMI )
            return parseNullStatement();
        return parseDeclarationOrExpressionStatement();
    }

}