/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.ParserProblemFactory;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser2.c.CASTASMDeclaration;
import org.eclipse.cdt.internal.core.parser2.c.CASTBreakStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTCaseStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTContinueStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTDefaultStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTDoStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTForStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTGotoStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTIfStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTLabelStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTNullStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTReturnStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTSwitchStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTWhileStatement;
import org.eclipse.cdt.internal.core.parser2.cpp.IProblemRequestor;

/**
 * @author jcamelon
 */
public abstract class AbstractGNUSourceCodeParser implements ISourceCodeParser {

    protected final IParserLogService log;

    protected final IScanner scanner;

    protected final ParserMode mode;

    protected IProblemRequestor requestor = null;

    protected final boolean supportStatementsInExpressions;

    protected final boolean supportTypeOfUnaries;

    protected final boolean supportAlignOfUnaries;

    protected AbstractGNUSourceCodeParser(IScanner scanner,
            IParserLogService logService, ParserMode parserMode,
            IProblemRequestor callback, boolean supportStatementsInExpressions,
            boolean supportTypeOfUnaries, boolean supportAlignOfUnaries) {
        this.scanner = scanner;
        this.log = logService;
        this.mode = parserMode;
        this.requestor = callback;
        this.supportStatementsInExpressions = supportStatementsInExpressions;
        this.supportTypeOfUnaries = supportTypeOfUnaries;
        this.supportAlignOfUnaries = supportAlignOfUnaries;
    }

    protected boolean parsePassed = true;

    protected BacktrackException backtrack = new BacktrackException();

    protected int backtrackCount = 0;

    protected final void throwBacktrack(int startingOffset, int endingOffset,
            int lineNumber, char[] f) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(startingOffset,
                (endingOffset == 0) ? startingOffset + 1 : endingOffset,
                lineNumber, f);
        throw backtrack;
    }

    protected IToken currToken;

    protected IToken lastToken;

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
        throwBacktrack(la.getOffset(), la.getEndOffset(), la.getLineNumber(),
                la.getFilename());
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

    protected IProblemFactory problemFactory = new ParserProblemFactory();

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
     *  
     */
    protected void backup(IToken mark) {
        currToken = mark;
        lastToken = null; // this is not entirely right ...
    }

    /**
     * This is the single entry point for setting parsePassed to false, and also
     * making note what token offset we failed upon.
     * 
     * @throws EndOfFileException
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
        return consume(IToken.tIDENTIFIER);
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

    protected void failParse(BacktrackException bt) {
        if (requestor != null) {
            if (bt.getProblem() == null) {
                IProblem problem = problemFactory.createProblem(
                        IProblem.SYNTAX_ERROR, bt.getStartingOffset(), bt
                                .getEndOffset(), bt.getLineNumber(), bt
                                .getFilename(), EMPTY_STRING, false, true);
                requestor.acceptProblem(problem);
            } else
                requestor.acceptProblem(bt.getProblem());
        }
        failParse();
    }

    protected void failParse(IProblem problem) {
        if (problem != null && requestor != null) {
            requestor.acceptProblem(problem);
        }
        failParse();
    }

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
            //			log.errorLog( buffer.toString() );
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
            //			log.errorLog(buffer.toString());
        }
    }

    protected final void throwBacktrack(IProblem problem)
            throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(problem);
        throw backtrack;
    }

    protected IToken simpleDeclarationMark;

    private static final int DEFAULT_COMPOUNDSTATEMENT_LIST_SIZE = 8;

    /**
     *  
     */
    protected void cleanupLastToken() {
        if (lastToken != null)
            lastToken.setNext(null);
        simpleDeclarationMark = null;
    }

    public IASTTranslationUnit parse() {
        long startTime = System.currentTimeMillis();
        translationUnit();
        // For the debuglog to take place, you have to call
        // Util.setDebugging(true);
        // Or set debug to true in the core plugin preference
        log.traceLog("Parse " //$NON-NLS-1$
                + (++parseCount) + ": " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
                + (parsePassed ? "" : " - parse failure")); //$NON-NLS-1$ //$NON-NLS-2$
        return getTranslationUnit();
    }

    protected void skipOverCompoundStatement() throws BacktrackException,
            EndOfFileException {
        // speed up the parser by skiping the body
        // simply look for matching brace and return
        consume(IToken.tLBRACE);
        int depth = 1;
        while (depth > 0) {
            switch (consume().getType()) {
            case IToken.tRBRACE:
                --depth;
                break;
            case IToken.tLBRACE:
                ++depth;
                break;
            }
        }
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
     * @param d
     */
    protected void throwAwayMarksForInitializerClause(Declarator d) {
        simpleDeclarationMark = null;
        if (d.getNameDuple() != null)
            d.getNameDuple().getLastToken().setNext(null);
        if (d.getPointerOperatorNameDuple() != null)
            d.getPointerOperatorNameDuple().getLastToken().setNext(null);
    }

    /**
     * @return TODO
     * @throws BacktrackException
     */
    protected IASTCompoundStatement compoundStatement()
            throws EndOfFileException, BacktrackException {
        int startingOffset = consume(IToken.tLBRACE).getOffset();

        List statements = Collections.EMPTY_LIST;
        while (LT(1) != IToken.tRBRACE) {
            int checkToken = LA(1).hashCode();
            try {
                IASTStatement s = statement();
                if (statements == Collections.EMPTY_LIST)
                    statements = new ArrayList(
                            DEFAULT_COMPOUNDSTATEMENT_LIST_SIZE);
                statements.add(s);
            } catch (BacktrackException b) {
                failParse(b);
                if (LA(1).hashCode() == checkToken)
                    failParseWithErrorHandling();
            }
        }
        consume(IToken.tRBRACE);

        IASTCompoundStatement result = createCompoundStatement();
        ((ASTNode)result).setOffset(startingOffset);
        for (int i = 0; i < statements.size(); ++i) {
            IASTStatement s = (IASTStatement) statements.get(i);
            result.addStatement(s);
            s.setParent(result);
            s.setPropertyInParent(IASTCompoundStatement.NESTED_STATEMENT);
        }
        return result;
    }

    /**
     * @return
     */
    protected abstract IASTCompoundStatement createCompoundStatement();

    /**
     * @param la
     * @return @throws
     *         EndOfFileException
     * @throws BacktrackException
     */
    protected IASTExpression compoundStatementExpression(IToken la)
            throws EndOfFileException, BacktrackException {
        int startingOffset = la.getOffset();
        consume(IToken.tLPAREN);
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

        consume(IToken.tRPAREN);
        IGNUASTCompoundStatementExpression resultExpression = createCompoundStatementExpression();
        ((ASTNode)resultExpression).setOffset( startingOffset );
        resultExpression.setCompoundStatement(compoundStatement);
        compoundStatement.setParent(resultExpression);
        compoundStatement
                .setPropertyInParent(IGNUASTCompoundStatementExpression.STATEMENT);

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
            IASTExpression resultExpression = compoundStatementExpression(la);
            if (resultExpression != null)
                return resultExpression;
        }


        IASTExpression assignmentExpression = assignmentExpression();
        if( LT(1) != IToken.tCOMMA )
            return assignmentExpression;
        
        IASTExpressionList expressionList = createExpressionList();
        ((ASTNode)expressionList).setOffset( startingOffset );
        expressionList.addExpression( assignmentExpression );
        assignmentExpression.setParent( expressionList );
        assignmentExpression.setPropertyInParent( IASTExpressionList.NESTED_EXPRESSION );
        
        while (LT(1) == IToken.tCOMMA) {
            consume(IToken.tCOMMA);
            IASTExpression secondExpression = assignmentExpression();
            expressionList.addExpression( secondExpression );
            secondExpression.setParent( expressionList );
            secondExpression.setPropertyInParent( IASTExpressionList.NESTED_EXPRESSION );
        }
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

    protected abstract IASTTypeId typeId(boolean skipArrayMods)
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression castExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression unaryExpression()
            throws BacktrackException, EndOfFileException;

    protected abstract IASTExpression buildTypeIdExpression(int op_sizeof, IASTTypeId typeId, int startingOffset);
    
    protected abstract void translationUnit();

    protected abstract IASTTranslationUnit getTranslationUnit();

    protected IASTExpression assignmentOperatorExpression(int kind,
            IASTExpression lhs) throws EndOfFileException, BacktrackException {
        consume();
        IASTExpression rhs = assignmentExpression();
        return buildBinaryExpression(kind, lhs, rhs);
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
                    secondExpression);
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
                    secondExpression);
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
                    secondExpression);
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
                    secondExpression);
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
                    secondExpression);
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
                        firstExpression, secondExpression);
                break;
            default:
                return firstExpression;
            }
        }
    }

    protected IASTExpression buildBinaryExpression(int operator,
            IASTExpression firstExpression, IASTExpression secondExpression) {
        IASTBinaryExpression result = createBinaryExpression();
        result.setOperator(operator);
        ((ASTNode)result).setOffset(((ASTNode)firstExpression).getOffset());
        result.setOperand1(firstExpression);
        firstExpression.setParent(result);
        firstExpression.setPropertyInParent(IASTBinaryExpression.OPERAND_ONE);
        result.setOperand2(secondExpression);
        secondExpression.setParent(result);
        secondExpression.setPropertyInParent(IASTBinaryExpression.OPERAND_TWO);
        return result;
    }

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
                        firstExpression, secondExpression);
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
                        firstExpression, secondExpression);
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * @return
     */
    protected abstract IASTBinaryExpression createBinaryExpression();

    /**
     * @param expression
     * @return @throws
     *         BacktrackException
     */
    protected IASTExpression conditionalExpression() throws BacktrackException,
            EndOfFileException {
        IASTExpression firstExpression = logicalOrExpression();
        if (LT(1) == IToken.tQUESTION) {
            consume(IToken.tQUESTION);
            IASTExpression secondExpression = expression();
            consume(IToken.tCOLON);
            IASTExpression thirdExpression = assignmentExpression();
            IASTConditionalExpression result = createConditionalExpression();
            result.setLogicalConditionExpression(firstExpression);
            firstExpression.setParent(result);
            firstExpression
                    .setPropertyInParent(IASTConditionalExpression.LOGICAL_CONDITION);
            result.setPositiveResultExpression(secondExpression);
            secondExpression.setParent(result);
            secondExpression
                    .setPropertyInParent(IASTConditionalExpression.POSITIVE_RESULT);
            result.setNegativeResultExpression(thirdExpression);
            thirdExpression.setParent(result);
            thirdExpression
                    .setPropertyInParent(IASTConditionalExpression.NEGATIVE_RESULT);
            return result;
        }
        return firstExpression;
    }

    /**
     * @return
     */
    protected abstract IASTConditionalExpression createConditionalExpression();

    protected IASTExpression unaryOperatorCastExpression(int operator)
            throws EndOfFileException, BacktrackException {
        int offset = consume().getOffset();
        IASTExpression castExpression = castExpression();
        return buildUnaryExpression(operator, castExpression, offset);
    }

    /**
     * @param operator
     * @param operand
     * @param offset
     *            TODO
     * @return
     */
    protected IASTExpression buildUnaryExpression(int operator,
            IASTExpression operand, int offset) {
        IASTUnaryExpression result = createUnaryExpression();
        ((ASTNode)result).setOffset(offset);
        result.setOperator(operator);
        result.setOperand(operand);
        operand.setParent(result);
        operand.setPropertyInParent(IASTUnaryExpression.OPERAND);
        return result;
    }

    /**
     * @return
     */
    protected abstract IASTUnaryExpression createUnaryExpression();

    /**
     * @return @throws
     *         BacktrackException
     * @throws EndOfFileException
     */
    protected IASTExpression unaryAlignofExpression()
            throws EndOfFileException, BacktrackException {
        int offset = consume(IGCCToken.t___alignof__).getOffset();
        IASTTypeId d = null;
        IASTExpression unaryExpression = null;

        IToken m = mark();
        if (LT(1) == IToken.tLPAREN) {
            try {
                consume(IToken.tLPAREN);
                d = typeId(false);
                consume(IToken.tRPAREN);
            } catch (BacktrackException bt) {
                backup(m);
                d = null;
                unaryExpression = unaryExpression();
            }
        } else {
            unaryExpression = unaryExpression();
        }
        if (d != null & unaryExpression == null) 
            return buildTypeIdExpression( IGNUASTTypeIdExpression.op_alignof, d, offset );
        else if (unaryExpression != null && d == null)
            return buildUnaryExpression( IGNUASTUnaryExpression.op_alignOf, unaryExpression, offset );
        return null;
    }

    protected IASTExpression unaryTypeofExpression() throws EndOfFileException,
            BacktrackException {
        int offset = consume(IGCCToken.t_typeof).getOffset();
        IASTTypeId d = null;
        IASTExpression unaryExpression = null;

        IToken m = mark();
        if (LT(1) == IToken.tLPAREN) {
            try {
                consume(IToken.tLPAREN);
                d = typeId(false);
                consume(IToken.tRPAREN);
            } catch (BacktrackException bt) {
                backup(m);
                d = null;
                unaryExpression = unaryExpression();
            }
        } else {
            unaryExpression = unaryExpression();
        }
        if (d != null & unaryExpression == null) 
            return buildTypeIdExpression( IGNUASTTypeIdExpression.op_typeof, d, offset );
        else if (unaryExpression != null && d == null)
            return buildUnaryExpression( IGNUASTUnaryExpression.op_typeof, unaryExpression, offset );
        return null;
    }

    protected IASTStatement handleFunctionBody() throws BacktrackException,
            EndOfFileException {
        if (mode == ParserMode.QUICK_PARSE
                || mode == ParserMode.STRUCTURAL_PARSE) {
            skipOverCompoundStatement();
            return null;
        } else if (mode == ParserMode.COMPLETION_PARSE
                || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                return functionBody();
            skipOverCompoundStatement();
            return null;
        } else if (mode == ParserMode.COMPLETE_PARSE)
            return functionBody();
        return null;
    }

    /**
     * Parses a function body.
     * 
     * @return TODO
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTStatement functionBody() throws EndOfFileException,
            BacktrackException {
        return compoundStatement();
    }

    /**
     * @param sdw
     * @param typeNameBegin
     * @param typeNameEnd
     */
    protected void setTypeName(DeclarationWrapper sdw, IToken typeNameBegin,
            IToken typeNameEnd) {
        if (typeNameBegin != null)
            sdw.setTypeName(TokenFactory.createTokenDuple(typeNameBegin,
                    typeNameEnd));
    }

    /**
     * @param flags
     *            input flags that are used to make our decision
     * @return whether or not this looks like a a declarator follows
     * @throws EndOfFileException
     *             we could encounter EOF while looking ahead
     */
    protected boolean lookAheadForDeclarator(Flags flags)
            throws EndOfFileException {
        return flags.haveEncounteredTypename()
                && ((LT(2) != IToken.tIDENTIFIER || (LT(3) != IToken.tLPAREN && LT(3) != IToken.tASSIGN)) && !LA(
                        2).isPointer());
    }

    public static class Flags {
        private boolean encounteredTypename = false;

        // have we encountered a typeName yet?
        private boolean encounteredRawType = false;

        // have we encountered a raw type yet?
        private final boolean parm;

        // is this for a simpleDeclaration or parameterDeclaration?
        private final boolean constructor;

        // are we attempting the constructor strategy?
        public Flags(boolean parm, boolean c) {
            this.parm = parm;
            constructor = c;
        }

        public Flags(boolean parm) {
            this(parm, false);
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
     * C++.
     * 
     * enumSpecifier: "enum" (name)? "{" (enumerator-list) "}" enumerator-list:
     * enumerator-definition enumerator-list , enumerator-definition
     * enumerator-definition: enumerator enumerator = constant-expression
     * enumerator: identifier
     * 
     * @param owner
     *            IParserCallback object that represents the declaration that
     *            owns this type specifier.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTEnumerationSpecifier enumSpecifier(DeclarationWrapper sdw)
            throws BacktrackException, EndOfFileException {
        IToken mark = mark();
        IASTName name = null;
        int startOffset = consume(IToken.t_enum).getOffset();
        if (LT(1) == IToken.tIDENTIFIER) {
            name = createName( identifier() );
        }
        else
            name = createName();
        if (LT(1) == IToken.tLBRACE) {
            
            IASTEnumerationSpecifier result = createEnumerationSpecifier();
            ((ASTNode)result).setOffset( startOffset );
            result.setName( name );
            name.setParent( result );
            name.setPropertyInParent( IASTEnumerationSpecifier.ENUMERATION_NAME );
            cleanupLastToken();
            consume(IToken.tLBRACE);
            while (LT(1) != IToken.tRBRACE) {
                IASTName enumeratorName = null;
                if (LT(1) == IToken.tIDENTIFIER) {
                    enumeratorName = createName( identifier() );
                } else {
                    IToken la = LA(1);
                    throwBacktrack(la.getOffset(), la.getEndOffset(), la
                            .getLineNumber(), la.getFilename());
                }
                IASTExpression initialValue = null;
                if (LT(1) == IToken.tASSIGN) {
                    consume(IToken.tASSIGN);
                    initialValue = constantExpression();
                }
                IASTEnumerationSpecifier.IASTEnumerator enumerator = null;
                if (LT(1) == IToken.tRBRACE) {
                    enumerator = createEnumerator();
                    enumerator.setName( enumeratorName );
                    ((ASTNode)enumerator).setOffset( ((ASTNode)enumeratorName).getOffset() );
                    enumeratorName.setParent( enumerator );
                    enumeratorName.setPropertyInParent( IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME );
                    if( initialValue != null )
                    {
                        enumerator.setValue( initialValue );
                        initialValue.setParent( enumerator );
                        initialValue.setPropertyInParent( IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_VALUE );
                    }
                    result.addEnumerator( enumerator );
                    enumerator.setParent( result );
                    enumerator.setPropertyInParent( IASTEnumerationSpecifier.ENUMERATOR );
                    cleanupLastToken();
                    break;
                }
                if (LT(1) != IToken.tCOMMA) {
                    int endOffset = (lastToken != null) ? lastToken
                            .getEndOffset() : 0;
                    throwBacktrack(mark.getOffset(), endOffset, mark
                            .getLineNumber(), mark.getFilename());
                }

                enumerator = createEnumerator();
                enumerator.setName( enumeratorName );
                ((ASTNode)enumerator).setOffset( ((ASTNode)enumeratorName).getOffset() );
                enumeratorName.setParent( enumerator );
                enumeratorName.setPropertyInParent( IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME );
                if( initialValue != null )
                {
                    enumerator.setValue( initialValue );
                    initialValue.setParent( enumerator );
                    initialValue.setPropertyInParent( IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_VALUE );
                }
                result.addEnumerator( enumerator );
                enumerator.setParent( result );
                enumerator.setPropertyInParent( IASTEnumerationSpecifier.ENUMERATOR );
                cleanupLastToken();

                consume(IToken.tCOMMA);
            }
            consume(IToken.tRBRACE);
            return result;
        }
        // enumSpecifierAbort
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        backup(mark);
        throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(),
                mark.getFilename());
        return null;
    }

    /**
     * @return
     */
    protected abstract IASTEnumerator createEnumerator();        

    /**
     * @return
     */
    protected abstract IASTEnumerationSpecifier createEnumerationSpecifier();

    /**
     * @param token
     * @return
     */
    protected abstract IASTName createName(IToken token); 
    /**
     * @return
     */
    protected abstract IASTName createName();

    /**
     * @throws BacktrackException
     */
    protected IASTExpression condition() throws BacktrackException, EndOfFileException {
        IASTExpression cond = expression();
        cleanupLastToken();
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

    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.GNUBaseParser#statement(java.lang.Object)
     */
    protected IASTStatement statement() throws EndOfFileException,
            BacktrackException {

        switch (LT(1)) {
        // labeled statements
        case IToken.t_case:
            int startOffset = consume(IToken.t_case).getOffset();
            IASTExpression case_exp = constantExpression();
            consume(IToken.tCOLON);
            cleanupLastToken();
            IASTCaseStatement cs = createCaseStatement();
            ((ASTNode)cs).setOffset( startOffset );
            cs.setExpression( case_exp );
            case_exp.setParent( cs );
            case_exp.setPropertyInParent( IASTCaseStatement.EXPRESSION );
            return cs;
        case IToken.t_default:
            startOffset = consume(IToken.t_default).getOffset();
            consume(IToken.tCOLON);
            cleanupLastToken();
            IASTDefaultStatement df = createDefaultStatement();
            ((ASTNode)df).setOffset( startOffset );
            return df;
        // compound statement
        case IToken.tLBRACE:
            IASTCompoundStatement compound = compoundStatement();
            cleanupLastToken();
            return compound;
        // selection statement
        case IToken.t_if:
			startOffset = consume(IToken.t_if).getOffset();
			consume(IToken.tLPAREN);
			IASTExpression if_condition = condition();
			consume(IToken.tRPAREN);
			IASTStatement then_clause = statement();
			IASTStatement else_clause = null;
			if (LT(1) == IToken.t_else) {
				consume(IToken.t_else);
				else_clause = statement();
			}
			
			IASTIfStatement if_stmt = createIfStatement();
			if_stmt.setCondition( if_condition );
			((ASTNode)if_stmt).setOffset( startOffset );
		    if_condition.setParent( if_stmt );
		    if_condition.setPropertyInParent( IASTIfStatement.CONDITION );
		    if_stmt.setThenClause( then_clause );
		    then_clause.setParent( if_stmt );
		    then_clause.setPropertyInParent( IASTIfStatement.THEN );
		    if( else_clause != null )
		    {
		        if_stmt.setElseClause( else_clause );
		        else_clause.setParent( if_stmt );
		        else_clause.setPropertyInParent( IASTIfStatement.ELSE );
		    }
			cleanupLastToken();
			return if_stmt;
        case IToken.t_switch:
            startOffset = consume( IToken.t_switch ).getOffset();
            consume(IToken.tLPAREN);
            IASTExpression switch_condition = condition();
            consume(IToken.tRPAREN);
            IASTStatement switch_body = statement();
            cleanupLastToken();
            IASTSwitchStatement switch_statement = createSwitchStatement();
            ((ASTNode)switch_statement).setOffset( startOffset );
            switch_statement.setController( switch_condition );
            switch_condition.setParent( switch_statement );
            switch_condition.setPropertyInParent( IASTSwitchStatement.CONTROLLER );
            switch_statement.setBody( switch_body );
            switch_body.setParent( switch_statement );
            switch_body.setPropertyInParent( IASTSwitchStatement.BODY );
            return switch_statement;
        //iteration statements
        case IToken.t_while:
            startOffset = consume(IToken.t_while).getOffset();
            consume(IToken.tLPAREN);
            IASTExpression while_condition = condition();
            consume(IToken.tRPAREN);
            IASTStatement while_body = statement();
            cleanupLastToken();
            IASTWhileStatement while_statement = createWhileStatement();
            ((ASTNode)while_statement).setOffset( startOffset );
            while_statement.setCondition( while_condition );
            while_condition.setParent( while_statement );
            while_condition.setPropertyInParent( IASTWhileStatement.CONDITION );
            while_statement.setBody( while_body );
            while_condition.setParent( while_statement );
            while_condition.setPropertyInParent( IASTWhileStatement.BODY );
            while_body.setParent( while_statement );
            return while_statement;
        case IToken.t_do:
            startOffset = consume(IToken.t_do).getOffset();
            IASTStatement do_body = statement();
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            IASTExpression do_condition = condition();
            consume(IToken.tRPAREN);
            cleanupLastToken();
            IASTDoStatement do_statement = createDoStatement();
            ((ASTNode)do_statement).setOffset( startOffset );
            do_statement.setBody( do_body );
            do_body.setParent( do_statement );
            do_body.setPropertyInParent( IASTDoStatement.BODY );
            do_statement.setCondition( do_condition );
            do_condition.setParent( do_statement );
            do_condition.setPropertyInParent( IASTDoStatement.CONDITION );
            return do_statement;
        case IToken.t_for:
            startOffset = consume( IToken.t_for ).getOffset();
            consume(IToken.tLPAREN);
            IASTNode init = forInitStatement();
            IASTExpression for_condition = null;
            if (LT(1) != IToken.tSEMI)
                for_condition = condition();
            consume(IToken.tSEMI);
            IASTExpression iterationExpression = null;
            if (LT(1) != IToken.tRPAREN) {
                iterationExpression = expression();
                cleanupLastToken();
            }
            consume(IToken.tRPAREN);
            IASTStatement for_body = statement();
            cleanupLastToken();
            IASTForStatement for_statement = createForStatement();
            ((ASTNode)for_statement).setOffset( startOffset );
            if( init instanceof IASTDeclaration )
            {
                for_statement.setInit((IASTDeclaration) init);
                ((IASTDeclaration) init).setParent( for_statement );
                ((IASTDeclaration) init).setPropertyInParent( IASTForStatement.INITDECLARATION );
            }
            else if( init instanceof IASTExpression )
            {
                for_statement.setInit((IASTExpression) init);
                ((IASTExpression) init).setParent( for_statement );
                ((IASTExpression) init).setPropertyInParent( IASTForStatement.INITEXPRESSION );
            }
            if( for_condition != null )
            {
                for_statement.setCondition( for_condition );
                for_condition.setParent( for_statement );
                for_condition.setPropertyInParent( IASTForStatement.CONDITION );
            }
            if( iterationExpression != null )
            {
                for_statement.setIterationExpression( iterationExpression );
                iterationExpression.setParent( for_statement );
                iterationExpression.setPropertyInParent( IASTForStatement.ITERATION );
            }
            for_statement.setBody( for_body );
            for_body.setParent( for_statement );
            for_body.setPropertyInParent( IASTForStatement.BODY );
            return for_statement;
        //jump statement
        case IToken.t_break:
            startOffset = consume(IToken.t_break).getOffset();
            consume(IToken.tSEMI);
            cleanupLastToken();
            IASTBreakStatement break_statement = createBreakStatement();
            ((ASTNode)break_statement).setOffset( startOffset );
            return break_statement;
        case IToken.t_continue:
            startOffset = consume(IToken.t_continue).getOffset();
            consume(IToken.tSEMI);
            cleanupLastToken();
            IASTContinueStatement continue_statement = createContinueStatement();
            ((ASTNode)continue_statement).setOffset( startOffset );
            return continue_statement;
        case IToken.t_return:
            startOffset = consume(IToken.t_return).getOffset();
        	IASTExpression result = null;
            if (LT(1) != IToken.tSEMI) {
                result = expression();
                cleanupLastToken();
            }
            consume(IToken.tSEMI);
            cleanupLastToken();
            IASTReturnStatement return_statement = createReturnStatement();
            ((ASTNode)return_statement).setOffset( startOffset );
            if( result != null )
            {
                return_statement.setReturnValue( result );
                result.setParent( return_statement );
                result.setPropertyInParent( IASTReturnStatement.RETURNVALUE );
            }
            return return_statement;
        case IToken.t_goto:
            startOffset = consume(IToken.t_goto).getOffset();
            IToken identifier = consume(IToken.tIDENTIFIER);
            consume(IToken.tSEMI);
            cleanupLastToken();
            IASTName goto_label_name = createName( identifier );
            IASTGotoStatement goto_statement = createGoToStatement();
            ((ASTNode)goto_statement).setOffset( startOffset );
            goto_statement.setName( goto_label_name );
            goto_label_name.setParent( goto_statement );
            goto_label_name.setPropertyInParent( IASTGotoStatement.NAME );
            return goto_statement;
        case IToken.tSEMI:
            startOffset = consume(IToken.tSEMI ).getOffset();
        	cleanupLastToken();
        	IASTNullStatement null_statement = createNullStatement();
        	((ASTNode)null_statement).setOffset( startOffset );
        	return null_statement;
        default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
                IToken labelName = consume(IToken.tIDENTIFIER);
                consume(IToken.tCOLON);
                IASTLabelStatement label_statement = createLabelStatement();
                ((ASTNode)label_statement).setOffset( labelName.getOffset() );
                IASTName name = createName( labelName );
                label_statement.setName( name );
                name.setParent( label_statement );
                name.setPropertyInParent( IASTLabelStatement.NAME );
                return label_statement;
            }
            // expressionStatement
            // Note: the function style cast ambiguity is handled in
            // expression
            // Since it only happens when we are in a statement
            IToken mark = mark();
            try {
                IASTExpression expression = expression();
                consume(IToken.tSEMI);
                IASTExpressionStatement expressionStatement  = createExpressionStatement();
                expressionStatement.setExpression( expression );
                expression.setParent( expressionStatement );
                expression.setPropertyInParent( IASTExpressionStatement.EXPFRESSION );
                cleanupLastToken();
                return expressionStatement;
            } catch (BacktrackException b) {
                backup(mark);
            }

            // declarationStatement
            IASTDeclaration d = declaration();
            IASTDeclarationStatement ds = createDeclarationStatement();
            ds.setDeclaration(d);
            d.setParent( ds );
            d.setPropertyInParent( IASTDeclarationStatement.DECLARATION );
            cleanupLastToken();
            return ds;
        }

    }



    protected abstract IASTDeclaration declaration() throws BacktrackException, EndOfFileException;

    /**
     * @return
     */
    protected IASTLabelStatement createLabelStatement() {
        return new CASTLabelStatement();
    }

    /**
     * @return
     */
    protected IASTGotoStatement createGoToStatement() {
        return new CASTGotoStatement();
    }

    /**
     * @return
     */
    protected IASTReturnStatement createReturnStatement() {
        return new CASTReturnStatement();
    }

    /**
     * @return
     */
    protected IASTForStatement createForStatement() {
        return new CASTForStatement();
    }

    /**
     * @return
     */
    protected IASTContinueStatement createContinueStatement() {
        return new CASTContinueStatement();
    }

    /**
     * @return
     */
    protected IASTDoStatement createDoStatement() {
        return new CASTDoStatement();
    }

    /**
     * @return
     */
    protected IASTBreakStatement createBreakStatement() {
        return new CASTBreakStatement();
    }

    /**
     * @return
     */
    protected IASTWhileStatement createWhileStatement() {
        return new CASTWhileStatement();
    }

    /**
     * @return
     */
    protected IASTNullStatement createNullStatement() {
        return new CASTNullStatement();
    }

    /**
     * @return
     */
    protected IASTSwitchStatement createSwitchStatement() {
        return new CASTSwitchStatement();
    }

    /**
     * @return
     */
    protected IASTIfStatement createIfStatement() {
        return new CASTIfStatement();
    }

    /**
     * @return
     */
    protected IASTDefaultStatement createDefaultStatement() {
        return new CASTDefaultStatement();
    }

    /**
     * @return
     */
    protected IASTCaseStatement createCaseStatement() {
        return new CASTCaseStatement();
    }

    /**
     * @return
     */
    protected IASTExpressionStatement createExpressionStatement() {
        return new CASTExpressionStatement();
    }

    /**
     * @return
     */
    protected IASTDeclarationStatement createDeclarationStatement() {
        return new CASTDeclarationStatement();
    }
    
    protected abstract IASTNode forInitStatement() throws BacktrackException,
    EndOfFileException;
    
    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTDeclaration asmDeclaration() throws EndOfFileException, BacktrackException {
        IToken first = consume(IToken.t_asm);
        consume(IToken.tLPAREN);
        String assembly = consume(IToken.tSTRING).getImage();
        consume(IToken.tRPAREN);
        consume(IToken.tSEMI);
        cleanupLastToken();
        return buildASMDirective( first.getOffset(), assembly );
    }

    /**
     * @param offset
     * @param assembly
     * @return
     */
    protected IASTASMDeclaration buildASMDirective(int offset, String assembly) {
        IASTASMDeclaration result = createASMDirective();
        ((ASTNode)result).setOffset( offset );
        result.setAssembly( assembly );
        return result;
    }

    /**
     * @return
     */
    protected IASTASMDeclaration createASMDirective() {
        return new CASTASMDeclaration();
    }

}