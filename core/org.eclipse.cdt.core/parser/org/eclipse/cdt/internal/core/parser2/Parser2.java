/**********************************************************************
 * Copyright (c) 2002,2003, 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.parser2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.ParserProblemFactory;
import org.eclipse.cdt.internal.core.parser.SimpleDeclarationStrategy;
import org.eclipse.cdt.internal.core.parser.TemplateParameterManager;
import org.eclipse.cdt.internal.core.parser.Parser.Flags;
import org.eclipse.cdt.internal.core.parser.problem.IProblemFactory;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;

/**
 * This is our first implementation of the IParser interface, serving as a
 * parser for ANSI C and C++.
 * 
 * From time to time we will make reference to the ANSI ISO specifications.
 * 
 * @author jcamelon
 */
public class Parser2 {
    protected final ParserMode mode;

    protected static final char[] EMPTY_STRING = "".toCharArray(); //$NON-NLS-1$

    protected boolean parsePassed = true;

    private BacktrackException backtrack = new BacktrackException();

    private int backtrackCount = 0;

    protected final void throwBacktrack(IProblem problem)
            throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(problem);
        throw backtrack;
    }

    protected final void throwBacktrack(int startingOffset, int endingOffset,
            int lineNumber, char[] f) throws BacktrackException {
        ++backtrackCount;
        backtrack.initialize(startingOffset,
                (endingOffset == 0) ? startingOffset + 1 : endingOffset,
                lineNumber, f);
        throw backtrack;
    }

    //TODO this stuff needs to be encapsulated by IParserData
    protected final IParserLogService log;

    protected ParserLanguage language = ParserLanguage.CPP;

    protected IScanner scanner;

    protected IToken currToken;

    protected IToken lastToken;

    private ScopeStack templateIdScopes = new ScopeStack();

    private TypeId typeIdInstance = new TypeId();

    private static class ScopeStack {
        private int[] stack;

        private int index = -1;

        public ScopeStack() {
            stack = new int[8];
        }

        private void grow() {
            int[] newStack = new int[stack.length << 1];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }

        final public void push(int i) {
            if (++index == stack.length)
                grow();
            stack[index] = i;
        }

        final public int pop() {
            if (index >= 0)
                return stack[index--];
            return -1;
        }

        final public int peek() {
            if (index >= 0)
                return stack[index];
            return -1;
        }

        final public int size() {
            return index + 1;
        }
    }

    /**
     * @return Returns the log.
     */
    public IParserLogService getLog() {
        return log;
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
    public IToken LA(int i) throws EndOfFileException {

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
    public int LT(int i) throws EndOfFileException {
        return LA(i).getType();
    }

    /**
     * Consume the next token available, regardless of the type.
     * 
     * @return The token that was consumed and removed from our buffer.
     * @throws EndOfFileException
     *             If there is no token to consume.
     */
    public IToken consume() throws EndOfFileException {

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
    public IToken consume(int type) throws EndOfFileException,
            BacktrackException {
        if (LT(1) == type)
            return consume();
        IToken la = LA(1);
        throwBacktrack(la.getOffset(), la.getEndOffset(), la.getLineNumber(),
                la.getFilename());
        return null;
    }

    /**
     * Mark our place in the buffer so that we could return to it should we have
     * to.
     * 
     * @return The current token.
     * @throws EndOfFileException
     *             If there are no more tokens.
     */
    public IToken mark() throws EndOfFileException {
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
    public void backup(IToken mark) {
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
     * Consumes template parameters.
     * 
     * @param previousLast
     *            Previous "last" token (returned if nothing was consumed)
     * @return Last consumed token, or <code>previousLast</code> if nothing
     *         was consumed
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IToken consumeTemplateParameters(IToken previousLast)
            throws EndOfFileException, BacktrackException {
        if (language != ParserLanguage.CPP)
            return previousLast;
        int startingOffset = previousLast == null ? lastToken.getOffset()
                : previousLast.getOffset();
        IToken last = previousLast;
        if (LT(1) == IToken.tLT) {
            last = consume(IToken.tLT);
            // until we get all the names sorted out
            ScopeStack scopes = new ScopeStack();
            scopes.push(IToken.tLT);

            while (scopes.size() > 0) {
                int top;
                last = consume();

                switch (last.getType()) {
                case IToken.tGT:
                    if (scopes.peek() == IToken.tLT) {
                        scopes.pop();
                    }
                    break;
                case IToken.tRBRACKET:
                    do {
                        top = scopes.pop();
                    } while (scopes.size() > 0
                            && (top == IToken.tGT || top == IToken.tLT));
                    if (top != IToken.tLBRACKET)
                        throwBacktrack(startingOffset, last.getEndOffset(),
                                last.getLineNumber(), last.getFilename());

                    break;
                case IToken.tRPAREN:
                    do {
                        top = scopes.pop();
                    } while (scopes.size() > 0
                            && (top == IToken.tGT || top == IToken.tLT));
                    if (top != IToken.tLPAREN)
                        throwBacktrack(startingOffset, last.getEndOffset(),
                                last.getLineNumber(), last.getFilename());

                    break;
                case IToken.tLT:
                case IToken.tLBRACKET:
                case IToken.tLPAREN:
                    scopes.push(last.getType());
                    break;
                }
            }
        }
        return last;
    }

    protected List templateArgumentList(Object scope)
            throws EndOfFileException, BacktrackException {
        IToken start = LA(1);
        int startingOffset = start.getOffset();
        int startingLineNumber = start.getOffset();
        char[] fn = start.getFilename();
        start = null;
        Object expression = null;
        List list = new ArrayList();

        boolean completedArg = false;
        boolean failed = false;

        templateIdScopes.push(IToken.tLT);

        while (LT(1) != IToken.tGT) {
            completedArg = false;

            IToken mark = mark();

            try {
                Object typeId = typeId(scope, false);

                expression = null; /*
                                    * astFactory.createExpression(scope,
                                    * IASTExpression.Kind.POSTFIX_TYPEID_TYPEID,
                                    * null, null, null, typeId, null,
                                    * EMPTY_STRING, null);
                                    */
                list.add(expression);
                completedArg = true;
            } catch (BacktrackException e) {
                backup(mark);
            } /*
               * catch (ASTSemanticException e) { backup(mark); }
               */

            if (!completedArg) {
                try {
                    IToken la = LA(1);
                    int so = la.getOffset();
                    int ln = la.getLineNumber();
                    expression = assignmentExpression(scope);

                    //					if ( ( expression == null ) ||
                    // expression.getExpressionKind() ==
                    // IASTExpression.Kind.PRIMARY_EMPTY) {
                    //						throwBacktrack(so, ( lastToken != null ) ?
                    // lastToken.getEndOffset() : 0, ln, fn );
                    //					}
                    list.add(expression);
                    completedArg = true;
                } catch (BacktrackException e) {
                    backup(mark);
                }
            }
            if (!completedArg) {
                try {
                    ITokenDuple nameDuple = name(scope);
                    expression = null; /*
                                        * astFactory.createExpression(scope,
                                        * IASTExpression.Kind.ID_EXPRESSION,
                                        * null, null, null, null, nameDuple,
                                        * EMPTY_STRING, null);
                                        */
                    list.add(expression);
                    continue;
                } /*
                   * catch (ASTSemanticException e) { failed = true; break; }
                   */catch (BacktrackException e) {
                    failed = true;
                    break;
                } catch (Exception e) {
                    logException("templateArgumentList::createExpression()", e); //$NON-NLS-1$
                    failed = true;
                    break;
                }
            }

            if (LT(1) == IToken.tCOMMA) {
                consume();
            } else if (LT(1) != IToken.tGT) {
                failed = true;
                break;
            }
        }

        templateIdScopes.pop();

        if (failed) {
            //			if (expression != null)
            //				expression.freeReferences();
            throwBacktrack(startingOffset, 0, startingLineNumber, fn);
        }

        return list;
    }

    /**
     * Parse a template-id, according to the ANSI C++ spec.
     * 
     * template-id: template-name < template-argument-list opt > template-name :
     * identifier
     * 
     * @return the last token that we consumed in a successful parse
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IToken templateId(Object scope) throws EndOfFileException,
            BacktrackException {
        ITokenDuple duple = name(scope);
        //IToken last = consumeTemplateParameters(duple.getLastToken());
        return duple.getLastToken();//last;
    }

    /**
     * Parse a name.
     * 
     * name : ("::")? name2 ("::" name2)*
     * 
     * name2 : IDENTIFER : template-id
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ITokenDuple name(Object scope) throws BacktrackException,
            EndOfFileException {

        TemplateParameterManager argumentList = TemplateParameterManager
                .getInstance();

        try {
            IToken first = LA(1);
            IToken last = null;
            IToken mark = mark();

            boolean hasTemplateId = false;

            if (LT(1) == IToken.tCOLONCOLON) {
                argumentList.addSegment(null);
                last = consume(IToken.tCOLONCOLON);
            }

            if (LT(1) == IToken.tCOMPL)
                consume();

            switch (LT(1)) {
            case IToken.tIDENTIFIER:
                last = consume(IToken.tIDENTIFIER);
                last = consumeTemplateArguments(scope, last, argumentList);
                if (last.getType() == IToken.tGT)
                    hasTemplateId = true;
                break;

            default:
                IToken l = LA(1);
                backup(mark);
                throwBacktrack(first.getOffset(), l.getEndOffset(), first
                        .getLineNumber(), l.getFilename());
            }

            while (LT(1) == IToken.tCOLONCOLON) {
                last = consume(IToken.tCOLONCOLON);

                if (queryLookaheadCapability() && LT(1) == IToken.t_template)
                    consume();

                if (queryLookaheadCapability() && LT(1) == IToken.tCOMPL)
                    consume();

                switch (LT(1)) {
                case IToken.t_operator:
                    IToken l = LA(1);
                    backup(mark);
                    throwBacktrack(first.getOffset(), l.getEndOffset(), first
                            .getLineNumber(), l.getFilename());
                case IToken.tIDENTIFIER:
                    last = consume();
                    last = consumeTemplateArguments(scope, last, argumentList);
                    if (last.getType() == IToken.tGT)
                        hasTemplateId = true;
                }
            }

            ITokenDuple tokenDuple = TokenFactory.createTokenDuple(first, last,
                    (hasTemplateId ? argumentList.getTemplateArgumentsList()
                            : null));
            return tokenDuple;
        } finally {
            TemplateParameterManager.returnInstance(argumentList);
        }

    }

    /**
     * @param scope
     * @param last
     * @param argumentList
     * @return @throws
     *         EndOfFileException
     * @throws BacktrackException
     */
    protected IToken consumeTemplateArguments(Object scope, IToken last,
            TemplateParameterManager argumentList) throws EndOfFileException,
            BacktrackException {
        if (language != ParserLanguage.CPP)
            return last;
        if (LT(1) == IToken.tLT) {
            IToken secondMark = mark();
            consume(IToken.tLT);
            try {
                List list = templateArgumentList(scope);
                argumentList.addSegment(list);
                last = consume(IToken.tGT);
            } catch (BacktrackException bt) {
                argumentList.addSegment(null);
                backup(secondMark);
            }
        } else {
            argumentList.addSegment(null);
        }
        return last;
    }

    /**
     * Parse a const-volatile qualifier.
     * 
     * cvQualifier : "const" | "volatile"
     * 
     * TODO: fix this
     * 
     * @param ptrOp
     *            Pointer Operator that const-volatile applies to.
     * @return Returns the same object sent in.
     * @throws BacktrackException
     */
    protected IToken cvQualifier(IDeclarator declarator)
            throws EndOfFileException, BacktrackException {
        IToken result = null;
        int startingOffset = LA(1).getOffset();
        switch (LT(1)) {
        case IToken.t_const:
            result = consume(IToken.t_const);
            declarator.addPointerOperator(null /*ASTPointerOperator.CONST_POINTER*/);
            break;
        case IToken.t_volatile:
            result = consume(IToken.t_volatile);
            declarator.addPointerOperator(null/*ASTPointerOperator.VOLATILE_POINTER*/);
            break;
        case IToken.t_restrict:
            if (language == ParserLanguage.C || allowCPPRestrict) {
                result = consume(IToken.t_restrict);
                declarator
                        .addPointerOperator(null/*ASTPointerOperator.RESTRICT_POINTER*/);
                break;
            }
            IToken la = LA(1);
            throwBacktrack(startingOffset, la.getEndOffset(), la
                    .getLineNumber(), la.getFilename());

        }
        return result;
    }

    protected IToken consumeArrayModifiers(IDeclarator d, Object scope)
            throws EndOfFileException, BacktrackException {
        int startingOffset = LA(1).getOffset();
        IToken last = null;
        while (LT(1) == IToken.tLBRACKET) {
            consume(IToken.tLBRACKET); // eat the '['

            Object exp = null;
            if (LT(1) != IToken.tRBRACKET) {
                exp = constantExpression(scope);
            }
            last = consume(IToken.tRBRACKET);
            Object arrayMod = null;
            try {
                arrayMod = null; /* astFactory.createArrayModifier(exp); */
            } catch (Exception e) {
                logException("consumeArrayModifiers::createArrayModifier()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, last.getEndOffset(), last
                        .getLineNumber(), last.getFilename());
            }
            d.addArrayModifier(arrayMod);
        }
        return last;
    }

    protected void operatorId(Declarator d, IToken originalToken,
            TemplateParameterManager templateArgs) throws BacktrackException,
            EndOfFileException {
        // we know this is an operator
        IToken operatorToken = consume(IToken.t_operator);
        IToken toSend = null;
        if (LA(1).isOperator() || LT(1) == IToken.tLPAREN
                || LT(1) == IToken.tLBRACKET) {
            if ((LT(1) == IToken.t_new || LT(1) == IToken.t_delete)
                    && LT(2) == IToken.tLBRACKET && LT(3) == IToken.tRBRACKET) {
                consume();
                consume(IToken.tLBRACKET);
                toSend = consume(IToken.tRBRACKET);
                // vector new and delete operators
            } else if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tRPAREN) {
                // operator ()
                consume(IToken.tLPAREN);
                toSend = consume(IToken.tRPAREN);
            } else if (LT(1) == IToken.tLBRACKET && LT(2) == IToken.tRBRACKET) {
                consume(IToken.tLBRACKET);
                toSend = consume(IToken.tRBRACKET);
            } else if (LA(1).isOperator())
                toSend = consume();
            else
                throwBacktrack(operatorToken.getOffset(),
                        toSend != null ? toSend.getEndOffset() : 0,
                        operatorToken.getLineNumber(), operatorToken
                                .getFilename());
        } else {
            // must be a conversion function
            typeId(d.getDeclarationWrapper().getScope(), true);
            toSend = lastToken;
        }

        boolean hasTemplateId = (templateArgs != null);
        boolean grabbedNewInstance = false;
        if (templateArgs == null) {
            templateArgs = TemplateParameterManager.getInstance();
            grabbedNewInstance = true;
        }

        try {
            toSend = consumeTemplateArguments(d.getDeclarationWrapper()
                    .getScope(), toSend, templateArgs);
            if (toSend.getType() == IToken.tGT) {
                hasTemplateId = true;
            }

            ITokenDuple duple = TokenFactory.createTokenDuple(
                    originalToken == null ? operatorToken : originalToken,
                    toSend, (hasTemplateId ? templateArgs
                            .getTemplateArgumentsList() : null));

            d.setName(duple);
        } finally {
            if (grabbedNewInstance)
                TemplateParameterManager.returnInstance(templateArgs);
        }
    }

    /**
     * Parse a Pointer Operator.
     * 
     * ptrOperator : "*" (cvQualifier)* | "&" | ::? nestedNameSpecifier "*"
     * (cvQualifier)*
     * 
     * @param owner
     *            Declarator that this pointer operator corresponds to.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IToken consumePointerOperators(IDeclarator d)
            throws EndOfFileException, BacktrackException {
        IToken result = null;
        for (;;) {
            if (LT(1) == IToken.tAMPER) {
                result = consume(IToken.tAMPER);
                d.addPointerOperator(null /*ASTPointerOperator.REFERENCE*/);
                return result;

            }
            IToken mark = mark();

            ITokenDuple nameDuple = null;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON) {
                try {
                    nameDuple = name(d.getScope());
                } catch (BacktrackException bt) {
                    backup(mark);
                    return null;
                }
            }
            if (LT(1) == IToken.tSTAR) {
                result = consume(IToken.tSTAR);

                d.setPointerOperatorName(nameDuple);

                IToken successful = null;
                for (;;) {
                    IToken newSuccess = cvQualifier(d);
                    if (newSuccess != null)
                        successful = newSuccess;
                    else
                        break;

                }

                if (successful == null) {
                    d.addPointerOperator(null /*ASTPointerOperator.POINTER*/ );
                }
                continue;
            }
            if (nameDuple != null)
                nameDuple.freeReferences();
            backup(mark);
            return result;
        }
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object constantExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        return conditionalExpression(scope);
    }

    public Object expression(Object scope) throws BacktrackException,
            EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int ln = la.getLineNumber();
        char[] fn = la.getFilename();

        Object resultExpression = null;
        if (la.getType() == IToken.tLPAREN && LT(2) == IToken.tLBRACE
                && supportStatementsInExpressions) {
            resultExpression = compoundStatementExpression(scope, la,
                    resultExpression);
        }

        if (resultExpression != null)
            return resultExpression;

        Object assignmentExpression = assignmentExpression(scope);
        while (LT(1) == IToken.tCOMMA) {
            consume(IToken.tCOMMA);

            Object secondExpression = assignmentExpression(scope);

            int endOffset = lastToken != null ? lastToken.getEndOffset() : 0;
            try {
                assignmentExpression = null; /*
                                              * astFactory.createExpression(scope,
                                              * IASTExpression.Kind.EXPRESSIONLIST,
                                              * assignmentExpression,
                                              * secondExpression, null, null,
                                              * null, EMPTY_STRING, null);
                                              */
            } /*
               * catch (ASTSemanticException e) {
               * throwBacktrack(e.getProblem()); }
               */catch (Exception e) {
                logException("expression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, ln, fn);
            }
        }
        return assignmentExpression;
    }

    /**
     * @param scope
     * @param la
     * @param resultExpression
     * @return @throws
     *         EndOfFileException
     * @throws BacktrackException
     */
    private Object compoundStatementExpression(Object scope, IToken la,
            Object resultExpression) throws EndOfFileException,
            BacktrackException {
        int startingOffset = la.getOffset();
        int ln = la.getLineNumber();
        char[] fn = la.getFilename();
        consume(IToken.tLPAREN);
        try {
            if (mode == ParserMode.QUICK_PARSE
                    || mode == ParserMode.STRUCTURAL_PARSE)
                skipOverCompoundStatement();
            else if (mode == ParserMode.COMPLETION_PARSE
                    || mode == ParserMode.SELECTION_PARSE) {
                if (scanner.isOnTopContext())
                    compoundStatement(scope, true);
                else
                    skipOverCompoundStatement();
            } else if (mode == ParserMode.COMPLETE_PARSE)
                compoundStatement(scope, true);

            consume(IToken.tRPAREN);
            try {
                resultExpression = null; /*
                                          * astFactory.createExpression( scope,
                                          * IASTGCCExpression.Kind.STATEMENT_EXPRESSION,
                                          * null, null, null, null,
                                          * null,EMPTY_STRING, null );
                                          */
            }
            /*
             * catch (ASTSemanticException e) { throwBacktrack(e.getProblem()); }
             */catch (Exception e) {
                logException("expression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, lastToken != null ? lastToken
                        .getEndOffset() : 0, ln, fn);
            }
        } catch (BacktrackException bte) {
            backup(la);
        }
        return resultExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object assignmentExpression(Object scope)
            throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.t_throw) {
            return throwExpression(scope);
        }

        Object conditionalExpression = conditionalExpression(scope);
        // if the condition not taken, try assignment operators
        if (conditionalExpression != null) //&&
                                           // conditionalExpression.getExpressionKind()
                                           // ==
                                           // IASTExpression.Kind.CONDITIONALEXPRESSION
                                           // )
            return conditionalExpression;
        switch (LT(1)) {
        case IToken.tASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_NORMAL,
                    conditionalExpression);
        case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT,
                    conditionalExpression);
        case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV,
                    conditionalExpression);
        case IToken.tMODASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD,
                    conditionalExpression);
        case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS,
                    conditionalExpression);
        case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS,
                    conditionalExpression);
        case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT,
                    conditionalExpression);
        case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT,
                    conditionalExpression);
        case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND,
                    conditionalExpression);
        case IToken.tXORASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR,
                    conditionalExpression);
        case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(scope,
                    null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR,
                    conditionalExpression);
        }
        return conditionalExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object throwExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken throwToken = consume(IToken.t_throw);
        Object throwExpression = null;
        throwExpression = expression(scope);

        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            return null; /*
                          * astFactory.createExpression(scope,
                          * IASTExpression.Kind.THROWEXPRESSION,
                          * throwExpression, null, null, null, null,
                          * EMPTY_STRING, null);
                          */
        } /*
           * catch (ASTSemanticException e) { throwBacktrack(e.getProblem()); }
           */catch (Exception e) {
            logException("throwExpression::createExpression()", e); //$NON-NLS-1$
            throwBacktrack(throwToken.getOffset(), endOffset, throwToken
                    .getLineNumber(), throwToken.getFilename());

        }
        return null;
    }

    /**
     * @param expression
     * @return @throws
     *         BacktrackException
     */
    protected Object conditionalExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int ln = la.getLineNumber();
        char[] fn = la.getFilename();

        Object firstExpression = logicalOrExpression(scope);
        if (LT(1) == IToken.tQUESTION) {
            consume(IToken.tQUESTION);
            Object secondExpression = expression(scope);
            consume(IToken.tCOLON);
            Object thirdExpression = assignmentExpression(scope);
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.CONDITIONALEXPRESSION,
                              * firstExpression, secondExpression,
                              * thirdExpression, null, null, EMPTY_STRING,
                              * null);
                              */
            } /*
               * catch (ASTSemanticException e) {
               * throwBacktrack(e.getProblem()); }
               */catch (Exception e) {
                logException("conditionalExpression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, ln, fn);
            }
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object logicalOrExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        Object firstExpression = logicalAndExpression(scope);
        while (LT(1) == IToken.tOR) {
            consume(IToken.tOR);
            Object secondExpression = logicalAndExpression(scope);
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            try {
                firstExpression = null; /*
                                         * astFactory.createExpression(scope,
                                         * IASTExpression.Kind.LOGICALOREXPRESSION,
                                         * firstExpression, secondExpression,
                                         * null, null, null, EMPTY_STRING,
                                         * null); } catch (ASTSemanticException
                                         * e) { throwBacktrack(e.getProblem());
                                         */
            } catch (Exception e) {
                logException("logicalOrExpression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object logicalAndExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        Object firstExpression = inclusiveOrExpression(scope);
        while (LT(1) == IToken.tAND) {
            consume(IToken.tAND);
            Object secondExpression = inclusiveOrExpression(scope);
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            try {
                firstExpression = null; /*
                                         * astFactory.createExpression(scope,
                                         * IASTExpression.Kind.LOGICALANDEXPRESSION,
                                         * firstExpression, secondExpression,
                                         * null, null, null, EMPTY_STRING,
                                         * null); } catch (ASTSemanticException
                                         * e) { throwBacktrack(e.getProblem());
                                         */
            } catch (Exception e) {
                logException("logicalAndExpression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object inclusiveOrExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        Object firstExpression = exclusiveOrExpression(scope);
        while (LT(1) == IToken.tBITOR) {
            consume();
            Object secondExpression = exclusiveOrExpression(scope);
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            try {
                firstExpression = null; /*
                                         * astFactory.createExpression(scope,
                                         * IASTExpression.Kind.INCLUSIVEOREXPRESSION,
                                         * firstExpression, secondExpression,
                                         * null, null, null, EMPTY_STRING,
                                         * null); } catch (ASTSemanticException
                                         * e) { throwBacktrack(e.getProblem());
                                         */
            } catch (Exception e) {
                logException("inclusiveOrExpression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object exclusiveOrExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        Object firstExpression = andExpression(scope);
        while (LT(1) == IToken.tXOR) {
            consume();

            Object secondExpression = andExpression(scope);
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            try {
                firstExpression = null; /*
                                         * astFactory.createExpression(scope,
                                         * IASTExpression.Kind.EXCLUSIVEOREXPRESSION,
                                         * firstExpression, secondExpression,
                                         * null, null, null, EMPTY_STRING,
                                         * null); } catch (ASTSemanticException
                                         * e) { throwBacktrack(e.getProblem());
                                         */
            } catch (Exception e) {
                logException("exclusiveORExpression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
        }
        return firstExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object andExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        Object firstExpression = equalityExpression(scope);
        while (LT(1) == IToken.tAMPER) {
            consume();
            Object secondExpression = equalityExpression(scope);
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            try {
                firstExpression = null; /*
                                         * astFactory.createExpression(scope,
                                         * IASTExpression.Kind.ANDEXPRESSION,
                                         * firstExpression, secondExpression,
                                         * null, null, null, EMPTY_STRING,
                                         * null); } catch (ASTSemanticException
                                         * e) { throwBacktrack(e.getProblem());
                                         */
            } catch (Exception e) {
                logException("andExpression::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
        }
        return firstExpression;
    }

    /**
     * @param methodName
     * @param e
     */
    public void logException(String methodName, Exception e) {
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

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object equalityExpression(Object scope)
            throws EndOfFileException, BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        Object firstExpression = relationalExpression(scope);
        for (;;) {
            switch (LT(1)) {
            case IToken.tEQUAL:
            case IToken.tNOTEQUAL:
                IToken t = consume();
                Object secondExpression = relationalExpression(scope);
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * (t .getType() == IToken.tEQUAL) ?
                                             * IASTExpression.Kind.EQUALITY_EQUALS :
                                             * IASTExpression.Kind.EQUALITY_NOTEQUALS,
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e) {
                                             * throwBacktrack(e.getProblem());
                                             */
                } catch (Exception e) {
                    logException("equalityExpression::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
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
    protected Object relationalExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        Object firstExpression = shiftExpression(scope);
        for (;;) {
            switch (LT(1)) {
            case IToken.tGT:
                if (templateIdScopes.size() > 0
                        && templateIdScopes.peek() == IToken.tLT) {
                    return firstExpression;
                }
            case IToken.tLT:
            case IToken.tLTEQUAL:
            case IToken.tGTEQUAL:
                IToken mark = mark();
                int t = consume().getType();
                Object secondExpression = shiftExpression(scope);
                if (LA(1) == mark.getNext()) {
                    // we did not consume anything
                    // this is most likely an error
                    backup(mark);
                    return firstExpression;
                }
                Object expressionKind = null;
                switch (t) {
                case IToken.tGT:
                    expressionKind = null; //IASTExpression.Kind.RELATIONAL_GREATERTHAN;
                    break;
                case IToken.tLT:
                    expressionKind = null; //IASTExpression.Kind.RELATIONAL_LESSTHAN;
                    break;
                case IToken.tLTEQUAL:
                    expressionKind = null; //IASTExpression.Kind.RELATIONAL_LESSTHANEQUALTO;
                    break;
                case IToken.tGTEQUAL:
                    expressionKind = null; //IASTExpression.Kind.RELATIONAL_GREATERTHANEQUALTO;
                    break;
                }
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * expressionKind, firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e) {
                                             * throwBacktrack(e.getProblem());
                                             */
                } catch (Exception e) {
                    logException("relationalExpression::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            default:
                if (language == ParserLanguage.CPP && supportMinAndMaxOperators
                        && (LT(1) == IGCCToken.tMIN || LT(1) == IGCCToken.tMAX)) {
                    IToken m = mark();
                    Object k = null;
                    switch (LT(1)) {
                    case IGCCToken.tMAX:
                        consume();
                        k = null; //IASTGCCExpression.Kind.RELATIONAL_MAX;
                        break;
                    case IGCCToken.tMIN:
                        consume();
                        k = null; //IASTGCCExpression.Kind.RELATIONAL_MIN;
                    default:
                        break;
                    }
                    IToken next = LA(1);
                    Object se = shiftExpression(scope);
                    if (next == LA(1)) {
                        backup(m);
                        return firstExpression;
                    }
                    Object resultExpression = null;
                    //try {
                    resultExpression = null; /*
                                              * astFactory.createExpression(
                                              * scope, k, firstExpression, se,
                                              * null, null, null, EMPTY_STRING,
                                              * null ); } catch
                                              * (ASTSemanticException e1) {
                                              * throwBacktrack( e1.getProblem() ); }
                                              */
                    return resultExpression;
                }
                return firstExpression;
            }
        }
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    public Object shiftExpression(Object scope) throws BacktrackException,
            EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        Object firstExpression = additiveExpression(scope);
        for (;;) {
            switch (LT(1)) {
            case IToken.tSHIFTL:
            case IToken.tSHIFTR:
                IToken t = consume();
                Object secondExpression = additiveExpression(scope);
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * ((t.getType() == IToken.tSHIFTL) ?
                                             * IASTExpression.Kind.SHIFT_LEFT :
                                             * IASTExpression.Kind.SHIFT_RIGHT),
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e) {
                                             * throwBacktrack(e.getProblem());
                                             */
                } catch (Exception e) {
                    logException("shiftExpression::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
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
    protected Object additiveExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        Object firstExpression = multiplicativeExpression(scope);
        for (;;) {
            switch (LT(1)) {
            case IToken.tPLUS:
            case IToken.tMINUS:
                IToken t = consume();
                Object secondExpression = multiplicativeExpression(scope);
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * ((t.getType() == IToken.tPLUS) ?
                                             * IASTExpression.Kind.ADDITIVE_PLUS :
                                             * IASTExpression.Kind.ADDITIVE_MINUS),
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e) {
                                             * throwBacktrack(e.getProblem());
                                             */
                } catch (Exception e) {
                    logException("additiveExpression::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
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
    protected Object multiplicativeExpression(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        Object firstExpression = pmExpression(scope);
        for (;;) {
            switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
                IToken t = consume();
                Object secondExpression = pmExpression(scope);
                Object expressionKind = null;
                switch (t.getType()) {
                case IToken.tSTAR:
                    expressionKind = null; //IASTExpression.Kind.MULTIPLICATIVE_MULTIPLY;
                    break;
                case IToken.tDIV:
                    expressionKind = null; //IASTExpression.Kind.MULTIPLICATIVE_DIVIDE;
                    break;
                case IToken.tMOD:
                    expressionKind = null; //IASTExpression.Kind.MULTIPLICATIVE_MODULUS;
                    break;
                }
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * expressionKind, firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e) {
                                             * firstExpression.freeReferences();
                                             * throwBacktrack(e.getProblem());
                                             */
                } catch (Exception e) {
                    logException(
                            "multiplicativeExpression::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
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
    protected Object pmExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        Object firstExpression = castExpression(scope);
        for (;;) {
            switch (LT(1)) {
            case IToken.tDOTSTAR:
            case IToken.tARROWSTAR:
                IToken t = consume();
                Object secondExpression = castExpression(scope);
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * ((t.getType() == IToken.tDOTSTAR) ?
                                             * IASTExpression.Kind.PM_DOTSTAR :
                                             * IASTExpression.Kind.PM_ARROWSTAR),
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e) {
                                             * throwBacktrack(e.getProblem());
                                             */
                } catch (Exception e) {
                    logException("pmExpression::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * castExpression : unaryExpression | "(" typeId ")" castExpression
     */
    protected Object castExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        // TO DO: we need proper symbol checkint to ensure type name
        if (LT(1) == IToken.tLPAREN) {
            IToken la = LA(1);
            int startingOffset = la.getOffset();
            int line = la.getLineNumber();
            char[] fn = la.getFilename();
            IToken mark = mark();
            consume();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            boolean popped = false;
            Object typeId = null;
            // If this isn't a type name, then we shouldn't be here
            try {
                try {
                    typeId = typeId(scope, false);
                    consume(IToken.tRPAREN);
                } catch (BacktrackException bte) {
                    backup(mark);
                    //					if (typeId != null)
                    //						typeId.freeReferences();
                    throwBacktrack(bte);
                }

                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                    popped = true;
                }
                Object castExpression = castExpression(scope);
                //				if( castExpression != null &&
                // castExpression.getExpressionKind() ==
                // IASTExpression.Kind.PRIMARY_EMPTY )
                //				{
                //					backup( mark );
                //					if (typeId != null)
                //						typeId.freeReferences();
                //					return unaryExpression(scope);
                //				}
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                mark = null; // clean up mark so that we can garbage collect
                try {
                    return null; /*
                                  * astFactory.createExpression(scope,
                                  * IASTExpression.Kind.CASTEXPRESSION,
                                  * castExpression, null, null, typeId, null,
                                  * EMPTY_STRING, null); } catch
                                  * (ASTSemanticException e) {
                                  * throwBacktrack(e.getProblem());
                                  */
                } catch (Exception e) {
                    logException("castExpression::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
            } catch (BacktrackException b) {
                if (templateIdScopes.size() > 0 && !popped) {
                    templateIdScopes.pop();
                }
            }
        }
        return unaryExpression(scope);

    }

    /**
     * @throws BacktrackException
     */
    public Object typeId(Object scope, boolean skipArrayModifiers)
            throws EndOfFileException, BacktrackException {
        IToken mark = mark();
        ITokenDuple name = null;
        boolean isConst = false, isVolatile = false;
        boolean isSigned = false, isUnsigned = false;
        boolean isShort = false, isLong = false;
        boolean isTypename = false;

        boolean encountered = false;
        Object kind = null;
        do {
            try {
                name = name(scope);
                kind = null; //IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
                encountered = true;
                break;
            } catch (BacktrackException b) {
                // do nothing
            }

            boolean encounteredType = false;
            simpleMods: for (;;) {
                switch (LT(1)) {
                case IToken.t_signed:
                    consume();
                    isSigned = true;
                    break;

                case IToken.t_unsigned:
                    consume();
                    isUnsigned = true;
                    break;

                case IToken.t_short:
                    consume();
                    isShort = true;
                    break;

                case IToken.t_long:
                    consume();
                    isLong = true;
                    break;

                case IToken.t_const:
                    consume();
                    isConst = true;
                    break;

                case IToken.t_volatile:
                    consume();
                    isVolatile = true;
                    break;

                case IToken.tIDENTIFIER:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    name = name(scope);
                    kind = null; //IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
                    encountered = true;
                    break;

                case IToken.t_int:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type.INT;
                    encountered = true;
                    consume();
                    break;

                case IToken.t_char:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type.CHAR;
                    encountered = true;
                    consume();
                    break;

                case IToken.t_bool:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type.BOOL;
                    encountered = true;
                    consume();
                    break;

                case IToken.t_double:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type.DOUBLE;
                    encountered = true;
                    consume();
                    break;

                case IToken.t_float:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type.FLOAT;
                    encountered = true;
                    consume();
                    break;

                case IToken.t_wchar_t:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type.WCHAR_T;
                    encountered = true;
                    consume();
                    break;

                case IToken.t_void:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type.VOID;
                    encountered = true;
                    consume();
                    break;

                case IToken.t__Bool:
                    if (encounteredType)
                        break simpleMods;
                    encounteredType = true;
                    kind = null; //IASTSimpleTypeSpecifier.Type._BOOL;
                    encountered = true;
                    consume();
                    break;

                default:
                    break simpleMods;
                }
            }

            if (encountered)
                break;

            if (isShort || isLong || isUnsigned || isSigned) {
                encountered = true;
                kind = null; //IASTSimpleTypeSpecifier.Type.INT;
                break;
            }

            if (LT(1) == IToken.t_typename || LT(1) == IToken.t_struct
                    || LT(1) == IToken.t_class || LT(1) == IToken.t_enum
                    || LT(1) == IToken.t_union) {
                consume();
                try {
                    name = name(scope);
                    kind = null; //IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
                    encountered = true;
                } catch (BacktrackException b) {
                    backup(mark);
                    throwBacktrack(b);
                }
            }

        } while (false);

        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        if (! encountered )
            throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(),
                    mark.getFilename());

        TypeId id = getTypeIdInstance(scope);
        IToken last = lastToken;
        IToken temp = last;

        //template parameters are consumed as part of name
        //lastToken = consumeTemplateParameters( last );
        //if( lastToken == null ) lastToken = last;

        temp = consumePointerOperators(id);
        if (temp != null)
            last = temp;

        if (!skipArrayModifiers) {
            temp = consumeArrayModifiers(id, scope);
            if (temp != null)
                last = temp;
        }

        endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            char[] signature = EMPTY_STRING;
            if (last != null) {
                if (lastToken == null)
                    lastToken = last;
                signature = TokenFactory.createCharArrayRepresentation(mark,
                        last);
            }
            return null; /*
                          * astFactory.createTypeId(scope, kind, isConst,
                          * isVolatile, isShort, isLong, isSigned, isUnsigned,
                          * isTypename, name, id .getPointerOperators(),
                          * id.getArrayModifiers(), signature); } catch
                          * (ASTSemanticException e) { backup(mark);
                          * throwBacktrack(e.getProblem());
                          */
        } catch (Exception e) {
            logException("typeId::createTypeId()", e); //$NON-NLS-1$
            throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(),
                    mark.getFilename());
        }
        return null;
    }

    /**
     * @param scope
     * @return
     */
    private TypeId getTypeIdInstance(Object scope) {
        typeIdInstance.reset(scope);
        return typeIdInstance;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object deleteExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        if (LT(1) == IToken.tCOLONCOLON) {
            // global scope
            consume(IToken.tCOLONCOLON);
        }

        consume(IToken.t_delete);

        boolean vectored = false;
        if (LT(1) == IToken.tLBRACKET) {
            // array delete
            consume();
            consume(IToken.tRBRACKET);
            vectored = true;
        }
        Object castExpression = castExpression(scope);
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            return null; /*
                          * astFactory.createExpression(scope, (vectored ?
                          * IASTExpression.Kind.DELETE_VECTORCASTEXPRESSION :
                          * IASTExpression.Kind.DELETE_CASTEXPRESSION),
                          * castExpression, null, null, null, null,
                          * EMPTY_STRING, null); } catch (ASTSemanticException
                          * e) { throwBacktrack(e.getProblem());
                          */
        } catch (Exception e) {
            logException("deleteExpression::createExpression()", e); //$NON-NLS-1$
            throwBacktrack(startingOffset, endOffset, line, fn);
        }
        return null;
    }

    /**
     * Pazse a new-expression.
     * 
     * @param expression
     * 
     * @throws BacktrackException
     * 
     * 
     * newexpression: ::? new newplacement? newtypeid newinitializer? ::? new
     * newplacement? ( typeid ) newinitializer? newplacement: ( expressionlist )
     * newtypeid: typespecifierseq newdeclarator? newdeclarator: ptroperator
     * newdeclarator? | directnewdeclarator directnewdeclarator: [ expression ]
     * directnewdeclarator [ constantexpression ] newinitializer: (
     * expressionlist? )
     */
    protected Object newExpression(Object scope) throws BacktrackException,
            EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        if (LT(1) == IToken.tCOLONCOLON) {
            // global scope
            consume(IToken.tCOLONCOLON);
        }
        consume(IToken.t_new);
        boolean typeIdInParen = false;
        boolean placementParseFailure = true;
        IToken beforeSecondParen = null;
        IToken backtrackMarker = null;
        Object typeId = null;
        ArrayList newPlacementExpressions = new ArrayList();
        ArrayList newTypeIdExpressions = new ArrayList();
        ArrayList newInitializerExpressions = new ArrayList();

        if (LT(1) == IToken.tLPAREN) {
            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            try {
                // Try to consume placement list
                // Note: since expressionList and expression are the same...
                backtrackMarker = mark();
                newPlacementExpressions.add(expression(scope));
                consume(IToken.tRPAREN);
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                } //pop 1st Parent
                placementParseFailure = false;
                if (LT(1) == IToken.tLPAREN) {
                    beforeSecondParen = mark();
                    consume(IToken.tLPAREN);
                    if (templateIdScopes.size() > 0) {
                        templateIdScopes.push(IToken.tLPAREN);
                    } //push 2nd Paren
                    typeIdInParen = true;
                }
            } catch (BacktrackException e) {
                backup(backtrackMarker);
            }
            if (placementParseFailure) {
                // CASE: new (typeid-not-looking-as-placement) ...
                // the first expression in () is not a placement
                // - then it has to be typeId
                typeId = typeId(scope, true);
                consume(IToken.tRPAREN);
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                } //pop 1st Paren
            } else {
                if (!typeIdInParen) {
                    if (LT(1) == IToken.tLBRACKET) {
                        // CASE: new (typeid-looking-as-placement) [expr]...
                        // the first expression in () has been parsed as a
                        // placement;
                        // however, we assume that it was in fact typeId, and
                        // this
                        // new statement creates an array.
                        // Do nothing, fallback to array/initializer processing
                    } else {
                        // CASE: new (placement) typeid ...
                        // the first expression in () is parsed as a placement,
                        // and the next expression doesn't start with '(' or '['
                        // - then it has to be typeId
                        try {
                            backtrackMarker = mark();
                            typeId = typeId(scope, true);
                        } catch (BacktrackException e) {
                            // Hmmm, so it wasn't typeId after all... Then it is
                            // CASE: new (typeid-looking-as-placement)
                            backup(backtrackMarker);
                            // TODO fix this
                            return null;
                        }
                    }
                } else {
                    // Tricky cases: first expression in () is parsed as a
                    // placement,
                    // and the next expression starts with '('.
                    // The problem is, the first expression might as well be a
                    // typeid
                    try {
                        typeId = typeId(scope, true);
                        consume(IToken.tRPAREN);
                        if (templateIdScopes.size() > 0) {
                            templateIdScopes.pop();
                        } //popping the 2nd Paren

                        if (LT(1) == IToken.tLPAREN
                                || LT(1) == IToken.tLBRACKET) {
                            // CASE: new (placement)(typeid)(initializer)
                            // CASE: new (placement)(typeid)[] ...
                            // Great, so far all our assumptions have been
                            // correct
                            // Do nothing, fallback to array/initializer
                            // processing
                        } else {
                            // CASE: new (placement)(typeid)
                            // CASE: new
                            // (typeid-looking-as-placement)(initializer-looking-as-typeid)
                            // Worst-case scenario - this cannot be resolved w/o
                            // more semantic information.
                            // Luckily, we don't need to know what was that - we
                            // only know that
                            // new-expression ends here.
                            int endOffset = (lastToken != null) ? lastToken
                                    .getEndOffset() : 0;
                            try {
                                return null; /*
                                              * astFactory.createExpression(scope,
                                              * IASTExpression.Kind.NEW_TYPEID,
                                              * null, null, null, typeId, null,
                                              * EMPTY_STRING,
                                              * astFactory.createNewDescriptor(
                                              * newPlacementExpressions,
                                              * newTypeIdExpressions,
                                              * newInitializerExpressions)); }
                                              * catch (ASTSemanticException e) {
                                              * throwBacktrack(e.getProblem());
                                              */
                            } catch (Exception e) {
                                logException(
                                        "newExpression_1::createExpression()", e); //$NON-NLS-1$
                                throwBacktrack(startingOffset, endOffset, line,
                                        fn);
                            }
                        }
                    } catch (BacktrackException e) {
                        // CASE: new
                        // (typeid-looking-as-placement)(initializer-not-looking-as-typeid)
                        // Fallback to initializer processing
                        backup(beforeSecondParen);
                        if (templateIdScopes.size() > 0) {
                            templateIdScopes.pop();
                        }//pop that 2nd paren
                    }
                }
            }
        } else {
            // CASE: new typeid ...
            // new parameters do not start with '('
            // i.e it has to be a plain typeId
            typeId = typeId(scope, true);
        }
        while (LT(1) == IToken.tLBRACKET) {
            // array new
            consume();

            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLBRACKET);
            }

            newTypeIdExpressions.add(assignmentExpression(scope));
            consume(IToken.tRBRACKET);

            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }
        }
        // newinitializer
        if (LT(1) == IToken.tLPAREN) {
            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }

            //we want to know the difference between no newInitializer and an
            // empty new Initializer
            //if the next token is the RPAREN, then we have an Empty expression
            // in our list.
            newInitializerExpressions.add(expression(scope));

            consume(IToken.tRPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }
        }
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            return null; /*
                          * astFactory.createExpression(scope,
                          * IASTExpression.Kind.NEW_TYPEID, null, null, null,
                          * typeId, null, EMPTY_STRING,
                          * astFactory.createNewDescriptor(
                          * newPlacementExpressions, newTypeIdExpressions,
                          * newInitializerExpressions)); } catch
                          * (ASTSemanticException e) {
                          * throwBacktrack(e.getProblem()); return null;
                          */
        } catch (Exception e) {
            logException("newExpression_2::createExpression()", e); //$NON-NLS-1$
            throwBacktrack(startingOffset, endOffset, line, fn);
        }
        return null;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    public Object unaryExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        switch (LT(1)) {
        case IToken.tSTAR:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION);
        case IToken.tAMPER:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION);
        case IToken.tPLUS:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION);
        case IToken.tMINUS:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION);
        case IToken.tNOT:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION);
        case IToken.tCOMPL:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION);
        case IToken.tINCR:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_INCREMENT);
        case IToken.tDECR:
            consume();
            return unaryOperatorCastExpression(scope,
                    null );//IASTExpression.Kind.UNARY_DECREMENT);
        case IToken.t_sizeof:
            consume(IToken.t_sizeof);
            IToken mark = LA(1);
            Object d = null;
            Object unaryExpression = null;
            if (LT(1) == IToken.tLPAREN) {
                try {
                    consume(IToken.tLPAREN);
                    d = typeId(scope, false);
                    consume(IToken.tRPAREN);
                } catch (BacktrackException bt) {
                    backup(mark);
                    unaryExpression = unaryExpression(scope);
                }
            } else {
                unaryExpression = unaryExpression(scope);
            }
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            if (unaryExpression == null)
                try {
                    return null; /*
                                  * astFactory.createExpression(scope,
                                  * IASTExpression.Kind.UNARY_SIZEOF_TYPEID,
                                  * null, null, null, d, null, EMPTY_STRING,
                                  * null); } catch (ASTSemanticException e) {
                                  * throwBacktrack(e.getProblem());
                                  */
                } catch (Exception e) {
                    logException("unaryExpression_1::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.UNARY_SIZEOF_UNARYEXPRESSION,
                              * unaryExpression, null, null, null, null,
                              * EMPTY_STRING, null); } catch
                              * (ASTSemanticException e1) {
                              * throwBacktrack(e1.getProblem());
                              */
            } catch (Exception e) {
                logException("unaryExpression_1::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
        case IToken.t_new:
            return newExpression(scope);
        case IToken.t_delete:
            return deleteExpression(scope);
        case IToken.tCOLONCOLON:
            if (queryLookaheadCapability(2)) {
                switch (LT(2)) {
                case IToken.t_new:
                    return newExpression(scope);
                case IToken.t_delete:
                    return deleteExpression(scope);
                default:
                    return postfixExpression(scope);
                }
            }
        default:
            if (LT(1) == IGCCToken.t_typeof && supportTypeOfUnaries) {
                Object unary = unaryTypeofExpression(scope);
                if (unary != null)
                    return unary;
            }
            if (LT(1) == IGCCToken.t___alignof__ && supportAlignOfUnaries) {
                Object align = unaryAlignofExpression(scope);
                if (align != null)
                    return align;
            }
            return postfixExpression(scope);
        }
    }

    /**
     * @param scope
     * @return @throws
     *         BacktrackException
     * @throws EndOfFileException
     */
    private Object unaryAlignofExpression(Object scope)
            throws EndOfFileException, BacktrackException {
        consume(IGCCToken.t___alignof__);
        Object d = null;
        Object unaryExpression = null;

        IToken m = mark();
        if (LT(1) == IToken.tLPAREN) {
            try {
                consume(IToken.tLPAREN);
                d = typeId(scope, false);
                consume(IToken.tRPAREN);
            } catch (BacktrackException bt) {
                backup(m);
                d = null;
                unaryExpression = unaryExpression(scope);
            }
        } else {
            unaryExpression = unaryExpression(scope);
        }
        if (d != null & unaryExpression == null) {
            //                try {
            return null; /*
                          * astFactory.createExpression( scope,
                          * IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID, null,
                          * null, null, d, null, EMPTY_STRING, null); } catch
                          * (ASTSemanticException e2) { throwBacktrack(
                          * e2.getProblem() ); }
                          */
        } else if (unaryExpression != null && d == null)
            //            try
            //            {
            return null; /*
                          * .createExpression( scope,
                          * IASTGCCExpression.Kind.UNARY_ALIGNOF_UNARYEXPRESSION,
                          * unaryExpression, null, null, null, null,
                          * EMPTY_STRING, null); } catch (ASTSemanticException
                          * e1) { throwBacktrack( e1.getProblem() ); }
                          */
        return null;

    }

    protected Object unaryTypeofExpression(Object scope)
            throws EndOfFileException, BacktrackException {
        consume(IGCCToken.t_typeof);
        Object d = null;
        Object unaryExpression = null;

        IToken m = mark();
        if (LT(1) == IToken.tLPAREN) {
            try {
                consume(IToken.tLPAREN);
                d = typeId(scope, false);
                consume(IToken.tRPAREN);
            } catch (BacktrackException bt) {
                backup(m);
                d = null;
                unaryExpression = unaryExpression(scope);
            }
        } else {
            unaryExpression = unaryExpression(scope);
        }
        if (d != null & unaryExpression == null) {
            //                try {
            return null; /*
                          * astFactory.createExpression( scope,
                          * IASTGCCExpression.Kind.UNARY_TYPEOF_TYPEID, null,
                          * null, null, d, null, EMPTY_STRING, null); } catch
                          * (ASTSemanticException e2) { throwBacktrack(
                          * e2.getProblem() ); }
                          */
        } else if (unaryExpression != null && d == null)
            //            try
            //            {
            return null; /*
                          * astFactory.createExpression( scope,
                          * IASTGCCExpression.Kind.UNARY_TYPEOF_UNARYEXPRESSION,
                          * unaryExpression, null, null, null, null,
                          * EMPTY_STRING, null); } catch (ASTSemanticException
                          * e1) { throwBacktrack( e1.getProblem() ); }
                          */
        return null;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object postfixExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        Object firstExpression = null;
        boolean isTemplate = false;

        switch (LT(1)) {
        case IToken.t_typename:
            consume(IToken.t_typename);

            boolean templateTokenConsumed = false;
            if (LT(1) == IToken.t_template) {
                consume(IToken.t_template);
                templateTokenConsumed = true;
            }
            ITokenDuple nestedName = name(scope);

            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            Object expressionList = expression(scope);
            int endOffset = consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }

            try {
                firstExpression = null; /*
                                         * astFactory .createExpression( scope,
                                         * (templateTokenConsumed ?
                                         * IASTExpression.Kind.POSTFIX_TYPENAME_TEMPLATEID :
                                         * IASTExpression.Kind.POSTFIX_TYPENAME_IDENTIFIER),
                                         * expressionList, null, null, null,
                                         * nestedName, EMPTY_STRING, null); }
                                         * catch (ASTSemanticException ase) {
                                         * throwBacktrack(ase.getProblem());
                                         */
            } catch (Exception e) {
                logException("postfixExpression_1::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
            break;
        // simple-type-specifier ( assignment-expression , .. )
        case IToken.t_char:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR);
            break;
        case IToken.t_wchar_t:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART);
            break;
        case IToken.t_bool:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL);
            break;
        case IToken.t_short:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT);
            break;
        case IToken.t_int:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT);
            break;
        case IToken.t_long:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG);
            break;
        case IToken.t_signed:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED);
            break;
        case IToken.t_unsigned:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED);
            break;
        case IToken.t_float:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT);
            break;
        case IToken.t_double:
            firstExpression = simpleTypeConstructorExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE);
            break;
        case IToken.t_dynamic_cast:
            firstExpression = specialCastExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_DYNAMIC_CAST);
            break;
        case IToken.t_static_cast:
            firstExpression = specialCastExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_STATIC_CAST);
            break;
        case IToken.t_reinterpret_cast:
            firstExpression = specialCastExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_REINTERPRET_CAST);
            break;
        case IToken.t_const_cast:
            firstExpression = specialCastExpression(scope,
                    null );//IASTExpression.Kind.POSTFIX_CONST_CAST);
            break;
        case IToken.t_typeid:
            consume();
            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            boolean isTypeId = true;
            Object lhs = null;
            Object typeId = null;
            try {
                typeId = typeId(scope, false);
            } catch (BacktrackException b) {
                isTypeId = false;
                lhs = expression(scope);
            }
            endOffset = consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }
            try {
                firstExpression = null; /*
                                         * astFactory .createExpression( scope,
                                         * (isTypeId ?
                                         * IASTExpression.Kind.POSTFIX_TYPEID_TYPEID :
                                         * IASTExpression.Kind.POSTFIX_TYPEID_EXPRESSION),
                                         * lhs, null, null, typeId, null,
                                         * EMPTY_STRING, null); } catch
                                         * (ASTSemanticException e6) {
                                         * throwBacktrack(e6.getProblem());
                                         */
            } catch (Exception e) {
                logException("postfixExpression_2::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
            break;
        default:
            firstExpression = primaryExpression(scope);
        }
        Object secondExpression = null;
        for (;;) {
            switch (LT(1)) {
            case IToken.tLBRACKET:
                // array access
                consume(IToken.tLBRACKET);
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.push(IToken.tLBRACKET);
                }
                secondExpression = expression(scope);
                int endOffset = consume(IToken.tRBRACKET).getEndOffset();
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                }
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * IASTExpression.Kind.POSTFIX_SUBSCRIPT,
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e2) {
                                             * throwBacktrack(e2.getProblem());
                                             */
                } catch (Exception e) {
                    logException("postfixExpression_3::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            case IToken.tLPAREN:
                // function call
                consume(IToken.tLPAREN);

                if (templateIdScopes.size() > 0) {
                    templateIdScopes.push(IToken.tLPAREN);
                }
                secondExpression = expression(scope);
                endOffset = consume(IToken.tRPAREN).getEndOffset();
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.pop();
                }
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * IASTExpression.Kind.POSTFIX_FUNCTIONCALL,
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e3) {
                                             * throwBacktrack(e3.getProblem());
                                             */
                } catch (Exception e) {
                    logException("postfixExpression_4::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            case IToken.tINCR:
                endOffset = consume(IToken.tINCR).getEndOffset();
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * IASTExpression.Kind.POSTFIX_INCREMENT,
                                             * firstExpression, null, null,
                                             * null, null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e1) {
                                             * throwBacktrack(e1.getProblem());
                                             */
                } catch (Exception e) {
                    logException("postfixExpression_5::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            case IToken.tDECR:
                endOffset = consume().getEndOffset();
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * IASTExpression.Kind.POSTFIX_DECREMENT,
                                             * firstExpression, null, null,
                                             * null, null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e4) {
                                             * throwBacktrack(e4.getProblem());
                                             */
                } catch (Exception e) {
                    logException("postfixExpression_6::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            case IToken.tDOT:
                // member access
                consume(IToken.tDOT);

                if (queryLookaheadCapability())
                    if (LT(1) == IToken.t_template) {
                        consume(IToken.t_template);
                        isTemplate = true;
                    }

                Object memberCompletionKind = null; /*(isTemplate ? IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS
                        : IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION); */

                secondExpression = primaryExpression(scope);
                endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;

                //					if (secondExpression != null
                //							&& secondExpression.getExpressionKind() == Kind.ID_EXPRESSION
                //							&& CharArrayUtils.indexOf( '~',
                // secondExpression.getIdExpressionCharArray() ) != -1)
                //						memberCompletionKind = Kind.POSTFIX_DOT_DESTRUCTOR;

                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * memberCompletionKind,
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e5) {
                                             * throwBacktrack(e5.getProblem());
                                             */
                } catch (Exception e) {
                    logException("postfixExpression_7::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            case IToken.tARROW:
                // member access
                consume(IToken.tARROW);

                if (queryLookaheadCapability())
                    if (LT(1) == IToken.t_template) {
                        consume(IToken.t_template);
                        isTemplate = true;
                    }

                Object arrowCompletionKind = /*(isTemplate ? IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP
                        : IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION); */ null;

                secondExpression = primaryExpression(scope);
                endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
                //					if (secondExpression != null
                //							&& secondExpression.getExpressionKind() == Kind.ID_EXPRESSION
                //							&& CharArrayUtils.indexOf( '~',
                // secondExpression.getIdExpressionCharArray() ) != -1)
                //						arrowCompletionKind = Kind.POSTFIX_ARROW_DESTRUCTOR;
                try {
                    firstExpression = null; /*
                                             * astFactory.createExpression(scope,
                                             * arrowCompletionKind,
                                             * firstExpression,
                                             * secondExpression, null, null,
                                             * null, EMPTY_STRING, null); }
                                             * catch (ASTSemanticException e) {
                                             * throwBacktrack(e.getProblem());
                                             */
                } catch (Exception e) {
                    logException("postfixExpression_8::createExpression()", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, line, fn);
                }
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * @return @throws
     *         EndOfFileException
     */
    protected boolean queryLookaheadCapability(int count) {
        //make sure we can look ahead one before doing this
        boolean result = true;
        try {
            LA(count);
        } catch (EndOfFileException olre) {
            result = false;
        }
        return result;
    }

    protected boolean queryLookaheadCapability() {
        return queryLookaheadCapability(1);
    }

    protected void checkEndOfFile() throws EndOfFileException {
        if (mode != ParserMode.SELECTION_PARSE)
            LA(1);
    }

    protected Object simpleTypeConstructorExpression(Object scope, Object type)
            throws EndOfFileException, BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;
        consume();
        consume(IToken.tLPAREN);
        Object inside = expression(scope);
        int endOffset = consume(IToken.tRPAREN).getEndOffset();
        try {
            return null; /*
                          * astFactory.createExpression(scope, type, inside,
                          * null, null, null, null, EMPTY_STRING, null); } catch
                          * (ASTSemanticException e) {
                          * throwBacktrack(e.getProblem());
                          */
        } catch (Exception e) {
            logException(
                    "simpleTypeConstructorExpression::createExpression()", e); //$NON-NLS-1$
            throwBacktrack(startingOffset, endOffset, line, fn);
        }
        return null;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object primaryExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken t = null;
        switch (LT(1)) {
        // TO DO: we need more literals...
        case IToken.tINTEGER:
            t = consume();
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.PRIMARY_INTEGER_LITERAL,
                              * null, null, null, null, null, t.getCharImage(),
                              * null); } catch (ASTSemanticException e1) {
                              * throwBacktrack(e1.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_1::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(t.getOffset(), t.getEndOffset(), t
                        .getLineNumber(), t.getFilename());
            }
        case IToken.tFLOATINGPT:
            t = consume();
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.PRIMARY_FLOAT_LITERAL, null,
                              * null, null, null, null, t.getCharImage(), null); }
                              * catch (ASTSemanticException e2) {
                              * throwBacktrack(e2.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_2::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(t.getOffset(), t.getEndOffset(), t
                        .getLineNumber(), t.getFilename());
            }
        case IToken.tSTRING:
        case IToken.tLSTRING:
            t = consume();
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.PRIMARY_STRING_LITERAL,
                              * null, null, null, null, null, t.getCharImage(),
                              * null); } catch (ASTSemanticException e5) {
                              * throwBacktrack(e5.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_3::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(t.getOffset(), t.getEndOffset(), t
                        .getLineNumber(), t.getFilename());
            }

        case IToken.t_false:
        case IToken.t_true:
            t = consume();
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.PRIMARY_BOOLEAN_LITERAL,
                              * null, null, null, null, null, t.getCharImage(),
                              * null); } catch (ASTSemanticException e3) {
                              * throwBacktrack(e3.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_4::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(t.getOffset(), t.getEndOffset(), t
                        .getLineNumber(), t.getFilename());
            }

        case IToken.tCHAR:
        case IToken.tLCHAR:

            t = consume();
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.PRIMARY_CHAR_LITERAL, null,
                              * null, null, null, null, t.getCharImage(), null); }
                              * catch (ASTSemanticException e4) {
                              * throwBacktrack(e4.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_5::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(t.getOffset(), t.getEndOffset(), t
                        .getLineNumber(), t.getFilename());
            }

        case IToken.t_this:
            t = consume(IToken.t_this);
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.PRIMARY_THIS, null, null,
                              * null, null, null, EMPTY_STRING, null); } catch
                              * (ASTSemanticException e7) {
                              * throwBacktrack(e7.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_6::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(t.getOffset(), t.getEndOffset(), t
                        .getLineNumber(), t.getFilename());
            }
        case IToken.tLPAREN:
            t = consume();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            Object lhs = expression(scope);
            int endOffset = consume(IToken.tRPAREN).getEndOffset();
            if (templateIdScopes.size() > 0) {
                templateIdScopes.pop();
            }
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.PRIMARY_BRACKETED_EXPRESSION,
                              * lhs, null, null, null, null, EMPTY_STRING,
                              * null); } catch (ASTSemanticException e6) {
                              * throwBacktrack(e6.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_7::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(t.getOffset(), endOffset, t.getLineNumber(), t
                        .getFilename());
            }
        case IToken.tIDENTIFIER:
        case IToken.tCOLONCOLON:
        case IToken.t_operator:
        case IToken.tCOMPL:
            ITokenDuple duple = null;
            int startingOffset = LA(1).getOffset();
            int line = LA(1).getLineNumber();
            try {
                duple = name(scope);
            } catch (BacktrackException bt) {
                IToken mark = mark();
                Declarator d = new Declarator(new DeclarationWrapper(scope,
                        mark.getOffset(), mark.getLineNumber(), null, mark
                                .getFilename()));

                if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                    IToken start = consume();
                    IToken end = null;
                    if (start.getType() == IToken.tIDENTIFIER)
                        end = consumeTemplateParameters(end);
                    while (LT(1) == IToken.tCOLONCOLON
                            || LT(1) == IToken.tIDENTIFIER) {
                        end = consume();
                        if (end.getType() == IToken.tIDENTIFIER)
                            end = consumeTemplateParameters(end);
                    }
                    if (LT(1) == IToken.t_operator)
                        operatorId(d, start, null);
                    else {
                        backup(mark);
                        throwBacktrack(startingOffset, end.getEndOffset(), end
                                .getLineNumber(), t.getFilename());
                    }
                } else if (LT(1) == IToken.t_operator)
                    operatorId(d, null, null);

                duple = d.getNameDuple();
            }

            endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            try {
                return null; /*
                              * astFactory.createExpression(scope,
                              * IASTExpression.Kind.ID_EXPRESSION, null, null,
                              * null, null, duple, EMPTY_STRING, null); } catch
                              * (ASTSemanticException e8) {
                              * throwBacktrack(e8.getProblem());
                              */
            } catch (Exception e) {
                logException("primaryExpression_8::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, duple
                        .getFilename());
            }
        default:
            IToken la = LA(1);
            startingOffset = la.getOffset();
            line = la.getLineNumber();
            char[] fn = la.getFilename();
            if (!queryLookaheadCapability(2)) {
                if (LA(1).canBeAPrefix()) {
                    consume();
                    checkEndOfFile();
                }
            }
            Object empty = null;
            try {
                empty = null; /*
                               * astFactory.createExpression(scope,
                               * IASTExpression.Kind.PRIMARY_EMPTY, null, null,
                               * null, null, null, EMPTY_STRING, null); } catch
                               * (ASTSemanticException e9) { throwBacktrack(
                               * e9.getProblem() ); return null;
                               */
            } catch (Exception e) {
                logException("primaryExpression_9::createExpression()", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, 0, line, fn);
            }
            return empty;
        }

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

    protected Object assignmentOperatorExpression(Object scope,
            Object kind, Object lhs) throws EndOfFileException,
            BacktrackException {
        IToken t = consume();
        Object assignmentExpression = assignmentExpression(scope);
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            return null; /*
                          * astFactory.createExpression(scope, kind, lhs,
                          * assignmentExpression, null, null, null,
                          * EMPTY_STRING, null); } catch (ASTSemanticException
                          * e) { throwBacktrack(e.getProblem());
                          */
        } catch (Exception e) {
            logException("assignmentOperatorExpression::createExpression()", e); //$NON-NLS-1$
            throwBacktrack(t.getOffset(), endOffset, t.getLineNumber(), t
                    .getFilename());
        }
        return null;
    }

    protected Object unaryOperatorCastExpression(Object scope,
            Object kind) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;
        Object castExpression = castExpression(scope);
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            return null; /*
                          * astFactory.createExpression(scope, kind,
                          * castExpression, null, null, null, null,
                          * EMPTY_STRING, null); } catch (ASTSemanticException
                          * e) { throwBacktrack(e.getProblem());
                          */
        } catch (Exception e) {
            logException("unaryOperatorCastExpression::createExpression()", e); //$NON-NLS-1$
            throwBacktrack(startingOffset, endOffset, line, fn);
        }
        return null;
    }

    protected Object specialCastExpression(Object scope,
            Object kind) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;

        consume();
        consume(IToken.tLT);
        Object typeID = typeId(scope, false);
        consume(IToken.tGT);
        consume(IToken.tLPAREN);
        Object lhs = expression(scope);
        int endOffset = consume(IToken.tRPAREN).getEndOffset();
        try {
            return null; /*
                          * astFactory.createExpression(scope, kind, lhs, null,
                          * null, duple, null, EMPTY_STRING, null); } catch
                          * (ASTSemanticException e) {
                          * throwBacktrack(e.getProblem());
                          */
        } catch (Exception e) {
            logException("specialCastExpression::createExpression()", e); //$NON-NLS-1$
            throwBacktrack(startingOffset, endOffset, line, fn);
        }
        return null;
    }

    protected boolean isCancelled = false;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.IParserData#getLastToken()
     */
    public IToken getLastToken() {
        return lastToken;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.IParserData#getParserLanguage()
     */
    public final ParserLanguage getParserLanguage() {
        return language;
    }

    /**
     * Parse an identifier.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    public IToken identifier() throws EndOfFileException, BacktrackException {
        IToken first = consume(IToken.tIDENTIFIER); // throws backtrack if its
                                                    // not that
        return first;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return scanner.toString(); //$NON-NLS-1$
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

    private static final int DEFAULT_DESIGNATOR_LIST_SIZE = 4;

    protected IProblemRequestor requestor = null;

    private IProblemFactory problemFactory = new ParserProblemFactory();

    private final boolean allowCPPRestrict;

    private final boolean supportTypeOfUnaries;

    private final boolean supportAlignOfUnaries;

    private final boolean supportExtendedTemplateSyntax;

    private final boolean supportMinAndMaxOperators;

    private final boolean supportStatementsInExpressions;

    private final boolean supportGCCStyleDesignators;

    /**
     * This is the standard cosntructor that we expect the Parser to be
     * instantiated with.
     * 
     * @param mode
     *            TODO
     *  
     */
    public Parser2(IScanner scanner, ParserMode mode,
            IProblemRequestor callback, ParserLanguage language,
            IParserLogService log, IParserExtensionConfiguration config) {
        this.scanner = scanner;
        this.language = language;
        this.log = log;
        this.mode = mode;
        requestor = callback;
        if (this.mode == ParserMode.QUICK_PARSE)
            constructInitializersInDeclarations = false;
        allowCPPRestrict = config.allowRestrictPointerOperatorsCPP();
        supportTypeOfUnaries = config.supportTypeofUnaryExpressionsCPP();
        supportAlignOfUnaries = config.supportAlignOfUnaryExpressionCPP();
        supportExtendedTemplateSyntax = config
                .supportExtendedTemplateSyntaxCPP();
        supportMinAndMaxOperators = config.supportMinAndMaxOperatorsCPP();
        supportStatementsInExpressions = config
                .supportStatementsInExpressions();
        supportGCCStyleDesignators = config.supportGCCStyleDesignatorsC();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#failParse()
     */
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

    // counter that keeps track of the number of times Parser.parse() is called
    private static int parseCount = 0;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.IParser#parse()
     */
    public boolean parse() {
        long startTime = System.currentTimeMillis();
        translationUnit();
        // For the debuglog to take place, you have to call
        // Util.setDebugging(true);
        // Or set debug to true in the core plugin preference
        log.traceLog("Parse " //$NON-NLS-1$
                + (++parseCount) + ": " //$NON-NLS-1$
                + (System.currentTimeMillis() - startTime) + "ms" //$NON-NLS-1$
                + (parsePassed ? "" : " - parse failure")); //$NON-NLS-1$ //$NON-NLS-2$
        return parsePassed;
    }

    /**
     * This is the top-level entry point into the ANSI C++ grammar.
     * 
     * translationUnit : (declaration)*
     */
    protected void translationUnit() {
        try {
            compilationUnit = null; /* astFactory.createCompilationUnit(); */
        } catch (Exception e2) {
            logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
            return;
        }

        //		compilationUnit.enterScope( requestor );

        while (true) {
            try {
                int checkOffset = LA(1).hashCode();
                declaration(compilationUnit, null);
                if (LA(1).hashCode() == checkOffset)
                    failParseWithErrorHandling();
            } catch (EndOfFileException e) {
                // Good
                break;
            } catch (BacktrackException b) {
                try {
                    // Mark as failure and try to reach a recovery point
                    failParse(b);
                    errorHandling();
                    //                    if (lastBacktrack != -1 && lastBacktrack ==
                    // LA(1).hashCode())
                    //                    {
                    //                        // we haven't progressed from the last backtrack
                    //                        // try and find tne next definition
                    //                        failParseWithErrorHandling();
                    //                    }
                    //                    else
                    //                    {
                    //                        // start again from here
                    //                        lastBacktrack = LA(1).hashCode();
                    //                    }
                } catch (EndOfFileException e) {
                    break;
                }
            } catch (OutOfMemoryError oome) {
                logThrowable("translationUnit", oome); //$NON-NLS-1$
                throw oome;
            } catch (Exception e) {
                logException("translationUnit", e); //$NON-NLS-1$
                try {
                    failParseWithErrorHandling();
                } catch (EndOfFileException e3) {
                    //nothing
                }
            } catch (ParseError perr) {
                throw perr;
            } catch (Throwable e) {
                logThrowable("translationUnit", e); //$NON-NLS-1$
                try {
                    failParseWithErrorHandling();
                } catch (EndOfFileException e3) {
                    //break;
                }
            }
        }
        //        compilationUnit.exitScope( requestor );
    }

    /**
     * @param string
     * @param e
     */
    private void logThrowable(String methodName, Throwable e) {
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
     * The merger of using-declaration and using-directive in ANSI C++ grammar.
     * 
     * using-declaration: using typename? ::? nested-name-specifier
     * unqualified-id ; using :: unqualified-id ; using-directive: using
     * namespace ::? nested-name-specifier? namespace-name ;
     * 
     * @param container
     *            Callback object representing the scope these definitions fall
     *            into.
     * @return TODO
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected Object usingClause(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken firstToken = consume(IToken.t_using);

        if (LT(1) == IToken.t_namespace) {
            // using-directive
            consume(IToken.t_namespace);

            // optional :: and nested classes handled in name
            ITokenDuple duple = null;
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON)
                duple = name(scope);
            else
                throwBacktrack(firstToken.getOffset(), endOffset, firstToken
                        .getLineNumber(), firstToken.getFilename());
            if (LT(1) == IToken.tSEMI) {
                IToken last = consume(IToken.tSEMI);
                Object astUD = null;

                try {
                    astUD = null; /*
                                   * astFactory.createUsingDirective(scope,
                                   * duple, firstToken.getOffset(),
                                   * firstToken.getLineNumber(),
                                   * last.getEndOffset(), last.getLineNumber()); }
                                   * catch( ASTSemanticException ase ) { backup(
                                   * last ); throwBacktrack( ase.getProblem() );
                                   */
                } catch (Exception e1) {
                    logException("usingClause:createUsingDirective", e1); //$NON-NLS-1$
                    throwBacktrack(firstToken.getOffset(), last.getEndOffset(),
                            firstToken.getLineNumber(), last.getFilename());
                }
                //                astUD.acceptElement(requestor );
                return astUD;
            }
            endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            throwBacktrack(firstToken.getOffset(), endOffset, firstToken
                    .getLineNumber(), firstToken.getFilename());
        }
        boolean typeName = false;

        if (LT(1) == IToken.t_typename) {
            typeName = true;
            consume(IToken.t_typename);
        }

        ITokenDuple name = null;
        if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON) {
            //	optional :: and nested classes handled in name
            name = name(scope);
        } else {
            throwBacktrack(firstToken.getOffset(),
                    (lastToken != null) ? lastToken.getEndOffset() : 0,
                    firstToken.getLineNumber(), firstToken.getFilename());
        }
        if (LT(1) == IToken.tSEMI) {
            IToken last = consume(IToken.tSEMI);
            Object declaration = null;
            try {
                declaration = null; /*
                                     * astFactory.createUsingDeclaration( scope,
                                     * typeName, name, firstToken.getOffset(),
                                     * firstToken.getLineNumber(),
                                     * last.getEndOffset(),
                                     * last.getLineNumber());
                                     */
            } catch (Exception e1) {
                logException("usingClause:createUsingDeclaration", e1); //$NON-NLS-1$
//                if (e1 instanceof ASTSemanticException
//                        && ((ASTSemanticException) e1).getProblem() != null)
//                    throwBacktrack(((ASTSemanticException) e1).getProblem());
//                else
                    throwBacktrack(firstToken.getOffset(), last.getEndOffset(),
                            firstToken.getLineNumber(), firstToken
                                    .getFilename());
            }
            //            declaration.acceptElement( requestor );
            return declaration;
        }
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        throwBacktrack(firstToken.getOffset(), endOffset, firstToken
                .getLineNumber(), firstToken.getFilename());
        return null;
    }

    /**
     * Implements Linkage specification in the ANSI C++ grammar.
     * 
     * linkageSpecification : extern "string literal" declaration | extern
     * "string literal" { declaration-seq }
     * 
     * @param container
     *            Callback object representing the scope these definitions fall
     *            into.
     * @return TODO
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected Object linkageSpecification(Object scope)
            throws EndOfFileException, BacktrackException {
        IToken firstToken = consume(IToken.t_extern);
        if (LT(1) != IToken.tSTRING)
            throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(),
                    firstToken.getLineNumber(), firstToken.getFilename());
        IToken spec = consume(IToken.tSTRING);

        if (LT(1) == IToken.tLBRACE) {
            IToken lbrace = consume(IToken.tLBRACE);
            Object linkage = null;
            try {
                linkage = null; /*
                                 * astFactory.createLinkageSpecification( scope,
                                 * spec.getCharImage(), firstToken.getOffset(),
                                 * firstToken.getLineNumber(),
                                 * firstToken.getFilename());
                                 */
            } catch (Exception e) {
                logException(
                        "linkageSpecification_1:createLinkageSpecification", e); //$NON-NLS-1$
                throwBacktrack(firstToken.getOffset(), lbrace.getEndOffset(),
                        lbrace.getLineNumber(), lbrace.getFilename());
            }

            //            linkage.enterScope( requestor );
            try {
                linkageDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
                    int checkToken = LA(1).hashCode();
                    switch (LT(1)) {
                    case IToken.tRBRACE:
                        consume(IToken.tRBRACE);
                        break linkageDeclarationLoop;
                    default:
                        try {
                            declaration(linkage, null);
                        } catch (BacktrackException bt) {
                            failParse(bt);
                            if (checkToken == LA(1).hashCode())
                                failParseWithErrorHandling();
                        }
                    }
                    if (checkToken == LA(1).hashCode())
                        failParseWithErrorHandling();
                }
                // consume the }
                IToken lastTokenConsumed = consume();
                //	            linkage.setEndingOffsetAndLineNumber(lastTokenConsumed.getEndOffset(),
                // lastTokenConsumed.getLineNumber());
            } finally {
                //            	linkage.exitScope( requestor );
            }
            return linkage;
        }
        // single declaration

        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        Object linkage;
        try {
            linkage = null; /*
                             * astFactory.createLinkageSpecification( scope,
                             * spec.getCharImage(), firstToken.getOffset(),
                             * firstToken.getLineNumber(),
                             * firstToken.getFilename());
                             */
        } catch (Exception e) {
            logException("linkageSpecification_2:createLinkageSpecification", e); //$NON-NLS-1$
            throwBacktrack(firstToken.getOffset(), endOffset, firstToken
                    .getLineNumber(), firstToken.getFilename());
            return null;
        }
        //		linkage.enterScope( requestor );
        try {
            declaration(linkage, null);
        } finally {
            //			linkage.exitScope( requestor );
        }
        return linkage;

    }

    /**
     * 
     * Represents the emalgamation of template declarations, template
     * instantiations and specializations in the ANSI C++ grammar.
     * 
     * template-declaration: export? template < template-parameter-list >
     * declaration explicit-instantiation: template declaration
     * explicit-specialization: template <>declaration
     * 
     * @param container
     *            Callback object representing the scope these definitions fall
     *            into.
     * @return TODO
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected Object templateDeclaration(Object scope)
            throws EndOfFileException, BacktrackException {
        IToken mark = mark();
        IToken firstToken = null;
        boolean exported = false;
        if (LT(1) == IToken.t_export) {
            exported = true;
            firstToken = consume(IToken.t_export);
            consume(IToken.t_template);
        } else {
            if (supportExtendedTemplateSyntax) {
                switch (LT(1)) {
                case IToken.t_static:
                case IToken.t_extern:
                case IToken.t_inline:
                    firstToken = consume();
                    consume(IToken.t_template);
                    break;
                default:
                    firstToken = consume(IToken.t_template);
                    break;
                }
            } else
                firstToken = consume(IToken.t_template);
        }
        if (LT(1) != IToken.tLT) {
            // explicit-instantiation
            Object templateInstantiation;
            try {
                templateInstantiation = null; /*
                                               * astFactory.createTemplateInstantiation(
                                               * scope, firstToken.getOffset(),
                                               * firstToken.getLineNumber(),
                                               * firstToken.getFilename());
                                               */
            } catch (Exception e) {
                logException(
                        "templateDeclaration:createTemplateInstantiation", e); //$NON-NLS-1$
                backup(mark);
                throwBacktrack(firstToken.getOffset(), firstToken
                        .getEndOffset(), firstToken.getLineNumber(), firstToken
                        .getFilename());
                return null;
            }
            //            templateInstantiation.enterScope( requestor );
            try {
                declaration(templateInstantiation, templateInstantiation);
                //            	templateInstantiation.setEndingOffsetAndLineNumber(lastToken.getEndOffset(),
                // lastToken.getLineNumber());
            } finally {
                //				templateInstantiation.exitScope( requestor );
            }

            return templateInstantiation;
        }
        consume(IToken.tLT);
        if (LT(1) == IToken.tGT) {
            IToken gt = consume(IToken.tGT);
            // explicit-specialization

            Object templateSpecialization;
            try {
                templateSpecialization = null; /*
                                                * astFactory.createTemplateSpecialization(
                                                * scope, firstToken.getOffset(),
                                                * firstToken.getLineNumber(),
                                                * firstToken.getFilename());
                                                */
            } catch (Exception e) {
                logException(
                        "templateDeclaration:createTemplateSpecialization", e); //$NON-NLS-1$
                backup(mark);
                throwBacktrack(firstToken.getOffset(), gt.getEndOffset(), gt
                        .getLineNumber(), gt.getFilename());
                return null;
            }
            //			templateSpecialization.enterScope(requestor);
            try {
                declaration(templateSpecialization, templateSpecialization);
                //				templateSpecialization.setEndingOffsetAndLineNumber(
                //							lastToken.getEndOffset(), lastToken.getLineNumber());
            } finally {
                //				templateSpecialization.exitScope(requestor);
            }
            return templateSpecialization;
        }

        try {
            List parms = templateParameterList(scope);
            IToken gt = consume(IToken.tGT);
            Object templateDecl;
            try {
                templateDecl = null; /*
                                      * astFactory.createTemplateDeclaration(
                                      * scope, parms, exported,
                                      * firstToken.getOffset(),
                                      * firstToken.getLineNumber(),
                                      * firstToken.getFilename());
                                      */
            } catch (Exception e) {
                logException("templateDeclaration:createTemplateDeclaration", e); //$NON-NLS-1$
                throwBacktrack(firstToken.getOffset(), gt.getEndOffset(), gt
                        .getLineNumber(), gt.getFilename());
                return null;
            }
            //            templateDecl.enterScope( requestor );
            try {
                declaration(templateDecl, templateDecl);
                //            	templateDecl.setEndingOffsetAndLineNumber(
                // lastToken.getEndOffset(), lastToken.getLineNumber() );
            } finally {
                //    			templateDecl.exitScope( requestor );
            }
            return templateDecl;
        } catch (BacktrackException bt) {
            backup(mark);
            throw bt;
        }
    }

    /**
     * 
     * 
     * 
     * template-parameter-list: template-parameter template-parameter-list ,
     * template-parameter template-parameter: type-parameter
     * parameter-declaration type-parameter: class identifier? class identifier? =
     * type-id typename identifier? typename identifier? = type-id template <
     * template-parameter-list > class identifier? template <
     * template-parameter-list > class identifier? = id-expression template-id:
     * template-name < template-argument-list?> template-name: identifier
     * template-argument-list: template-argument template-argument-list ,
     * template-argument template-argument: assignment-expression type-id
     * id-expression
     * 
     * @param templateDeclaration
     *            Callback's templateDeclaration which serves as a scope to this
     *            list.
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected List templateParameterList(Object scope)
            throws BacktrackException, EndOfFileException {
        // if we have gotten this far then we have a true template-declaration
        // iterate through the template parameter list
        List returnValue = new ArrayList();

        Object parameterScope = null; /* astFactory.createNewCodeBlock( scope ); */
        if (parameterScope == null)
            parameterScope = scope;

        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int lnum = la.getLineNumber();
        char[] fn = la.getFilename();

        for (;;) {
            if (LT(1) == IToken.tGT)
                return returnValue;
            if (LT(1) == IToken.t_class || LT(1) == IToken.t_typename) {
                consume();
//                IASTTemplateParameter.ParamKind kind = (consume().getType() == IToken.t_class) ? IASTTemplateParameter.ParamKind.CLASS
//                        : IASTTemplateParameter.ParamKind.TYPENAME;
                IToken startingToken = lastToken;
                IToken id = null;
                Object typeId = null;
                try {
                    if (LT(1) == IToken.tIDENTIFIER) // optional identifier
                    {
                        id = identifier();

                        if (LT(1) == IToken.tASSIGN) // optional = type-id
                        {
                            consume(IToken.tASSIGN);
                            typeId = typeId(parameterScope, false); // type-id
                        }
                    }

                } catch (BacktrackException bt) {
                    throw bt;
                }
                try {
                    int nameStart = (id != null) ? id.getOffset() : 0;
                    int nameEnd = (id != null) ? id.getEndOffset() : 0;
                    int nameLine = (id != null) ? id.getLineNumber() : 0;
                    returnValue
                            .add(null /*
                                       * astFactory.createTemplateParameter(
                                       * kind, ( id == null )? EMPTY_STRING :
                                       * id.getCharImage(), //$NON-NLS-1$
                                       * typeId, null, null, ( parameterScope
                                       * instanceof IASTCodeScope ) ?
                                       * (IASTCodeScope) parameterScope : null,
                                       * startingToken.getOffset(),
                                       * startingToken.getLineNumber(),
                                       * nameStart, nameEnd, nameLine,
                                       * (lastToken != null ) ?
                                       * lastToken.getEndOffset() : nameEnd,
                                       * (lastToken != null ) ?
                                       * lastToken.getLineNumber() : nameLine,
                                       * startingToken.getFilename() )
                                       */);
                }
                //				catch( ASTSemanticException ase )
                //				{
                //					throwBacktrack(ase.getProblem());
                //				}
                catch (Exception e) {
                    logException(
                            "templateParameterList_1:createTemplateParameter", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset,
                            (lastToken != null) ? lastToken.getEndOffset() : 0,
                            lnum, fn);
                }

            } else if (LT(1) == IToken.t_template) {
                consume(IToken.t_template);
                IToken startingToken = lastToken;
                consume(IToken.tLT);

                List subResult = templateParameterList(parameterScope);
                consume(IToken.tGT);
                consume(IToken.t_class);
                IToken optionalId = null;
                Object optionalTypeId = null;
                if (LT(1) == IToken.tIDENTIFIER) // optional identifier
                {
                    optionalId = identifier();

                    if (LT(1) == IToken.tASSIGN) // optional = type-id
                    {
                        consume(IToken.tASSIGN);
                        optionalTypeId = typeId(parameterScope, false);

                    }
                }

                try {
                    returnValue
                            .add(null /*
                                       * astFactory.createTemplateParameter(
                                       * IASTTemplateParameter.ParamKind.TEMPLATE_LIST, (
                                       * optionalId == null )? EMPTY_STRING :
                                       * optionalId.getCharImage(),
                                       * //$NON-NLS-1$ optionalTypeId, null,
                                       * subResult, ( parameterScope instanceof
                                       * IASTCodeScope ) ? (IASTCodeScope)
                                       * parameterScope : null,
                                       * startingToken.getOffset(),
                                       * startingToken.getLineNumber(),
                                       * (optionalId != null) ?
                                       * optionalId.getOffset() : 0, (optionalId !=
                                       * null) ? optionalId.getEndOffset() : 0,
                                       * (optionalId != null) ?
                                       * optionalId.getLineNumber() : 0,
                                       * lastToken.getEndOffset(),
                                       * lastToken.getLineNumber(),
                                       * lastToken.getFilename() )
                                       */);
                }
                //				catch( ASTSemanticException ase )
                //				{
                //					throwBacktrack(ase.getProblem());
                //				}
                catch (Exception e) {
                    int endOffset = (lastToken != null) ? lastToken
                            .getEndOffset() : 0;
                    logException(
                            "templateParameterList_2:createTemplateParameter", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, lnum, fn);
                }
            } else if (LT(1) == IToken.tCOMMA) {
                consume(IToken.tCOMMA);
                continue;
            } else {
                ParameterCollection c = new ParameterCollection();
                parameterDeclaration(c, parameterScope);
                DeclarationWrapper wrapper = (DeclarationWrapper) c
                        .getParameters().get(0);
                Declarator declarator = (Declarator) wrapper.getDeclarators()
                        .next();
                try {
                    returnValue.add(null
                    /*
                     * astFactory.createTemplateParameter(
                     * IASTTemplateParameter.ParamKind.PARAMETER, null, null,
                     * astFactory.createParameterDeclaration( wrapper.isConst(),
                     * wrapper.isVolatile(), wrapper.getTypeSpecifier(),
                     * declarator.getPointerOperators(),
                     * declarator.getArrayModifiers(), null, null,
                     * declarator.getName(), declarator.getInitializerClause(),
                     * wrapper.startingOffset, wrapper.startingLine,
                     * declarator.getNameStartOffset(),
                     * declarator.getNameEndOffset(), declarator.getNameLine(),
                     * wrapper.endOffset, wrapper.endLine, fn ), null, (
                     * parameterScope instanceof IASTCodeScope ) ?
                     * (IASTCodeScope) parameterScope : null,
                     * wrapper.startingOffset, wrapper.startingLine,
                     * declarator.getNameStartOffset(),
                     * declarator.getNameEndOffset(), declarator.getNameLine(),
                     * wrapper.endOffset, wrapper.endLine, fn )
                     */);
                }
                //				catch( ASTSemanticException ase )
                //				{
                //					throwBacktrack(ase.getProblem());
                //				}
                catch (Exception e) {
                    int endOffset = (lastToken != null) ? lastToken
                            .getEndOffset() : 0;
                    logException(
                            "templateParameterList:createParameterDeclaration", e); //$NON-NLS-1$
                    throwBacktrack(startingOffset, endOffset, lnum, fn);
                }
            }
        }
    }

    /**
     * The most abstract construct within a translationUnit : a declaration.
     * 
     * declaration : {"asm"} asmDefinition | {"namespace"} namespaceDefinition |
     * {"using"} usingDeclaration | {"export"|"template"} templateDeclaration |
     * {"extern"} linkageSpecification | simpleDeclaration
     * 
     * Notes: - folded in blockDeclaration - merged alternatives that required
     * same LA - functionDefinition into simpleDeclaration -
     * namespaceAliasDefinition into namespaceDefinition - usingDirective into
     * usingDeclaration - explicitInstantiation and explicitSpecialization into
     * templateDeclaration
     * 
     * @param container
     *            IParserCallback object which serves as the owner scope for
     *            this declaration.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void declaration(Object scope, Object ownerTemplate)
            throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.t_asm:
            IToken first = consume(IToken.t_asm);
            consume(IToken.tLPAREN);
            char[] assembly = consume(IToken.tSTRING).getCharImage();
            consume(IToken.tRPAREN);
            IToken last = consume(IToken.tSEMI);

            try {
                //                    astFactory.createASMDefinition(
                //                            scope,
                //                            assembly,
                //                            first.getOffset(),
                //                            first.getLineNumber(), last.getEndOffset(),
                // last.getLineNumber(), last.getFilename());
            } catch (Exception e) {
                logException("declaration:createASMDefinition", e); //$NON-NLS-1$
                throwBacktrack(first.getOffset(), last.getEndOffset(), first
                        .getLineNumber(), first.getFilename());
            }
            // if we made it this far, then we have all we need
            // do the callback
            // 				resultDeclaration.acceptElement(requestor);
            break;
        case IToken.t_namespace:
            namespaceDefinition(scope);
            break;
        case IToken.t_using:
            usingClause(scope);
            break;
        case IToken.t_export:
        case IToken.t_template:
            templateDeclaration(scope);
            break;
        case IToken.t_extern:
            if (LT(2) == IToken.tSTRING) {
                linkageSpecification(scope);
                break;
            }
        default:
            if (supportExtendedTemplateSyntax
                    && (LT(1) == IToken.t_static || LT(1) == IToken.t_inline || LT(1) == IToken.t_extern)
                    && LT(2) == IToken.t_template)
                templateDeclaration(scope);
            else
                simpleDeclarationStrategyUnion(scope, ownerTemplate);
        }

        cleanupLastToken();
    }

    protected Object simpleDeclarationStrategyUnion(Object scope,
            Object ownerTemplate) throws EndOfFileException, BacktrackException {
        simpleDeclarationMark = mark();
        IProblem firstFailure = null;
        IProblem secondFailure = null;
        try {
            return simpleDeclaration(SimpleDeclarationStrategy.TRY_CONSTRUCTOR,
                    scope, ownerTemplate, false);
            // try it first with the original strategy
        } catch (BacktrackException bt) {
            if (simpleDeclarationMark == null)
                throwBacktrack(bt);
            firstFailure = bt.getProblem();
            // did not work
            backup(simpleDeclarationMark);

            try {
                return simpleDeclaration(
                        SimpleDeclarationStrategy.TRY_FUNCTION, scope,
                        ownerTemplate, false);
            } catch (BacktrackException bt2) {
                if (simpleDeclarationMark == null) {
                    if (firstFailure != null && (bt2.getProblem() == null))
                        throwBacktrack(firstFailure);
                    else
                        throwBacktrack(bt2);
                }

                secondFailure = bt2.getProblem();
                backup(simpleDeclarationMark);

                try {
                    return simpleDeclaration(
                            SimpleDeclarationStrategy.TRY_VARIABLE, scope,
                            ownerTemplate, false);
                } catch (BacktrackException b3) {
                    backup(simpleDeclarationMark); //TODO - necessary?

                    if (firstFailure != null)
                        throwBacktrack(firstFailure);
                    else if (secondFailure != null)
                        throwBacktrack(secondFailure);
                    else
                        throwBacktrack(b3);
                    return null;
                }
            }

        }
    }

    /**
     * Serves as the namespace declaration portion of the ANSI C++ grammar.
     * 
     * namespace-definition: namespace identifier { namespace-body } | namespace {
     * namespace-body } namespace-body: declaration-seq?
     * 
     * @param container
     *            IParserCallback object which serves as the owner scope for
     *            this declaration.
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     *  
     */
    protected Object namespaceDefinition(Object scope)
            throws BacktrackException, EndOfFileException {
        IToken first = consume(IToken.t_namespace);

        IToken identifier = null;
        // optional name
        if (LT(1) == IToken.tIDENTIFIER)
            identifier = identifier();

        if (LT(1) == IToken.tLBRACE) {
            IToken lbrace = consume();
            Object namespaceDefinition = null;
            try {
                namespaceDefinition = null; /*
                                             * astFactory.createNamespaceDefinition(
                                             * scope, (identifier == null ?
                                             * EMPTY_STRING:
                                             * identifier.getCharImage()),
                                             * //$NON-NLS-1$ first.getOffset(),
                                             * first.getLineNumber(),
                                             * (identifier == null ?
                                             * first.getOffset() :
                                             * identifier.getOffset()),
                                             * (identifier == null ?
                                             * first.getEndOffset() :
                                             * identifier.getEndOffset() ),
                                             * (identifier == null ?
                                             * first.getLineNumber() :
                                             * identifier.getLineNumber() ),
                                             * first.getFilename());
                                             */
            } catch (Exception e1) {

                logException(
                        "namespaceDefinition:createNamespaceDefinition", e1); //$NON-NLS-1$
                throwBacktrack(first.getOffset(), lbrace.getEndOffset(), first
                        .getLineNumber(), first.getFilename());
                return null;
            }
            //            namespaceDefinition.enterScope( requestor );
            try {
                cleanupLastToken();
                namespaceDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
                    int checkToken = LA(1).hashCode();
                    switch (LT(1)) {
                    case IToken.tRBRACE:
                        //consume(Token.tRBRACE);
                        break namespaceDeclarationLoop;
                    default:
                        try {
                            declaration(namespaceDefinition, null);
                        } catch (BacktrackException bt) {
                            failParse(bt);
                            if (checkToken == LA(1).hashCode())
                                failParseWithErrorHandling();
                        }
                    }
                    if (checkToken == LA(1).hashCode())
                        failParseWithErrorHandling();
                }
                // consume the }
                IToken last = consume(IToken.tRBRACE);

                //		        namespaceDefinition.setEndingOffsetAndLineNumber(
                //		                last.getOffset() + last.getLength(), last.getLineNumber());
            } finally {
                //            	namespaceDefinition.exitScope( requestor );
            }
            return namespaceDefinition;
        } else if (LT(1) == IToken.tASSIGN) {
            IToken assign = consume(IToken.tASSIGN);

            if (identifier == null) {
                throwBacktrack(first.getOffset(), assign.getEndOffset(), first
                        .getLineNumber(), first.getFilename());
                return null;
            }

            ITokenDuple duple = name(scope);
            IToken semi = consume(IToken.tSEMI);

            Object alias = null;
            try {
                alias = null; /*
                               * astFactory.createNamespaceAlias( scope,
                               * identifier.getCharImage(), duple,
                               * first.getOffset(), first.getLineNumber(),
                               * identifier.getOffset(),
                               * identifier.getEndOffset(),
                               * identifier.getLineNumber(),
                               * duple.getLastToken().getEndOffset(),
                               * duple.getLastToken().getLineNumber() );
                               */
            } catch (Exception e1) {
                logException("namespaceDefinition:createNamespaceAlias", e1); //$NON-NLS-1$
                throwBacktrack(first.getOffset(), semi.getEndOffset(), first
                        .getLineNumber(), first.getFilename());
                return null;
            }
            return alias;
        } else {
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            throwBacktrack(first.getOffset(), endOffset, first.getLineNumber(),
                    first.getFilename());
            return null;
        }
    }

    /**
     * Serves as the catch-all for all complicated declarations, including
     * function-definitions.
     * 
     * simpleDeclaration : (declSpecifier)* (initDeclarator (","
     * initDeclarator)*)? (";" | { functionBody }
     * 
     * Notes: - append functionDefinition stuff to end of this rule
     * 
     * To do: - work in functionTryBlock
     * 
     * @param container
     *            IParserCallback object which serves as the owner scope for
     *            this declaration.
     * @param tryConstructor
     *            true == take strategy1 (constructor ) : false == take strategy
     *            2 ( pointer to function)
     * 
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected Object simpleDeclaration(SimpleDeclarationStrategy strategy,
            Object scope, Object ownerTemplate, boolean fromCatchHandler)
            throws BacktrackException, EndOfFileException {
        IToken firstToken = LA(1);
        int firstOffset = firstToken.getOffset();
        int firstLine = firstToken.getLineNumber();
        char[] fn = firstToken.getFilename();
        if (firstToken.getType() == IToken.tLBRACE)
            throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(),
                    firstToken.getLineNumber(), firstToken.getFilename());
        DeclarationWrapper sdw = new DeclarationWrapper(scope, firstToken
                .getOffset(), firstToken.getLineNumber(), ownerTemplate, fn);
        firstToken = null; // necessary for scalability

        declSpecifierSeq(sdw, false,
                strategy == SimpleDeclarationStrategy.TRY_CONSTRUCTOR);
        Object simpleTypeSpecifier = null;
        if (sdw.getTypeSpecifier() == null
                && sdw.getSimpleType() != null ) //IASTSimpleTypeSpecifier.Type.UNSPECIFIED)
            try {
                simpleTypeSpecifier = null; /*
                                             * astFactory.createSimpleTypeSpecifier(
                                             * scope, sdw.getSimpleType(),
                                             * sdw.getName(), sdw.isShort(),
                                             * sdw.isLong(), sdw.isSigned(),
                                             * sdw.isUnsigned(),
                                             * sdw.isTypeNamed(),
                                             * sdw.isComplex(),
                                             * sdw.isImaginary(),
                                             * sdw.isGloballyQualified(),
                                             * sdw.getExtensionParameters());
                                             * sdw.setTypeSpecifier(
                                             * simpleTypeSpecifier);
                                             * sdw.setTypeName( null );
                                             */
            } catch (Exception e1) {
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                logException("simpleDeclaration:createSimpleTypeSpecifier", e1); //$NON-NLS-1$
//                if (e1 instanceof ASTSemanticException
//                        && ((ASTSemanticException) e1).getProblem() != null)
//                    throwBacktrack(((ASTSemanticException) e1).getProblem());
//                else
                    throwBacktrack(firstOffset, endOffset, firstLine, fn);
            }

            Declarator declarator = null;
            if (LT(1) != IToken.tSEMI) {
                declarator = initDeclarator(sdw, strategy,
                        constructInitializersInDeclarations);

                while (LT(1) == IToken.tCOMMA) {
                    consume();
                    initDeclarator(sdw, strategy,
                            constructInitializersInDeclarations);
                }
            }

            boolean hasFunctionBody = false;
            boolean hasFunctionTryBlock = false;
            boolean consumedSemi = false;

            switch (LT(1)) {
            case IToken.tSEMI:
                consume(IToken.tSEMI);
                consumedSemi = true;
                break;
            case IToken.t_try:
                consume(IToken.t_try);
                if (LT(1) == IToken.tCOLON)
                    ctorInitializer(declarator);
                hasFunctionTryBlock = true;
                declarator.setFunctionTryBlock(true);
                break;
            case IToken.tCOLON:
                ctorInitializer(declarator);
                break;
            case IToken.tLBRACE:
                break;
            case IToken.tRPAREN:
                if (!fromCatchHandler)
                    throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1)
                            .getLineNumber(), fn);
                break;
            default:
                throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1)
                        .getLineNumber(), fn);
            }

            if (!consumedSemi) {
                if (LT(1) == IToken.tLBRACE) {
                    declarator.setHasFunctionBody(true);
                    hasFunctionBody = true;
                }

                if (hasFunctionTryBlock && !hasFunctionBody)
                    throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1)
                            .getLineNumber(), fn);
            }
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;

            if( hasFunctionBody )
                handleFunctionBody(null );
//            try {
//                l = sdw.createASTNodes();
////            } catch (ASTSemanticException e) {
////                if (e.getProblem() == null) {
////                    IProblem p = problemFactory.createProblem(
////                            IProblem.SYNTAX_ERROR, sdw.startingOffset,
////                            lastToken != null ? lastToken.getEndOffset() : 0,
////                            sdw.startingLine, fn, EMPTY_STRING, false, true);
////                    throwBacktrack(p);
////                } else {
////                    throwBacktrack(e.getProblem());
////                }
//            } catch (Exception e) {
//                logException("simpleDecl", e); //$NON-NLS-1$
//                throwBacktrack(firstOffset, endOffset, firstLine, fn);
//            }

//            if (hasFunctionBody && l.size() != 1) {
//                throwBacktrack(firstOffset, endOffset, firstLine, fn); //TODO
                                                                       // Should
                                                                       // be an
                                                                       // IProblem
//            }
//            if (!l.isEmpty()) // no need to do this unless we have a declarator
//            {
//                if (!hasFunctionBody || fromCatchHandler) {
//                    Object declaration = null;
//                    for (int i = 0; i < l.size(); ++i) {
//                        declaration = l.get(i);
//
////                            ((IASTOffsetableElement) declaration)
////                                    .setEndingOffsetAndLineNumber(lastToken
////                                            .getEndOffset(), lastToken
////                                            .getLineNumber());
//                        //			            declaration.acceptElement( requestor );
//                    }
//                    return declaration;
//                }
//                Object declaration = l.get(0);
//                cleanupLastToken();
//                //			    declaration.enterScope( requestor );
//                try {
//                    //					if ( !( declaration instanceof IASTScope ) )
//                    //						throwBacktrack(firstOffset, endOffset, firstLine, fn);
//
//                    handleFunctionBody(declaration);
//
////                        ((IASTOffsetableElement) declaration)
////                                .setEndingOffsetAndLineNumber(lastToken
////                                        .getEndOffset(), lastToken
////                                        .getLineNumber());
//                } finally {
//                    //			    	declaration.exitScope( requestor );
//                }
//
//                if (hasFunctionTryBlock)
//                    catchHandlerSequence(scope);
//
//                return declaration;
//
//            }
//
//            try {
//                if (sdw.getTypeSpecifier() != null) {
//                    Object declaration = null; /*
//                                                * astFactory.createTypeSpecDeclaration(
//                                                * sdw.getScope(),
//                                                * sdw.getTypeSpecifier(),
//                                                * ownerTemplate,
//                                                * sdw.startingOffset,
//                                                * sdw.startingLine,
//                                                * lastToken.getEndOffset(),
//                                                * lastToken.getLineNumber(),
//                                                * sdw.isFriend(),
//                                                * lastToken.getFilename());
//                                                */
//                    //					declaration.acceptElement(requestor);
//                    return declaration;
//                }
//            } catch (Exception e1) {
//                logException("simpleDeclaration:createTypeSpecDeclaration", e1); //$NON-NLS-1$
//                throwBacktrack(firstOffset, endOffset, firstLine, fn);
//            }
//
//            return null;
//        } catch (BacktrackException be) {
//            throwBacktrack(be);
//            return null;
//        } catch (EndOfFileException eof) {
//            throw eof;
        return null;
    }

    protected void handleFunctionBody(Object scope) throws BacktrackException,
            EndOfFileException {
        if (mode == ParserMode.QUICK_PARSE
                || mode == ParserMode.STRUCTURAL_PARSE)
            skipOverCompoundStatement();
        else if (mode == ParserMode.COMPLETION_PARSE
                || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                functionBody(scope);
            else
                skipOverCompoundStatement();
        } else if (mode == ParserMode.COMPLETE_PARSE)
            functionBody(scope);

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
     * This method parses a constructor chain ctorinitializer: :
     * meminitializerlist meminitializerlist: meminitializer | meminitializer ,
     * meminitializerlist meminitializer: meminitializerid | ( expressionlist? )
     * meminitializerid: ::? nestednamespecifier? classname identifier
     * 
     * @param declarator
     *            IParserCallback object that represents the declarator
     *            (constructor) that owns this initializer
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void ctorInitializer(Declarator d) throws EndOfFileException,
            BacktrackException {
        int startingOffset = consume(IToken.tCOLON).getOffset();
        Object scope = d.getDeclarationWrapper().getScope();
        scope = null; /* astFactory.getDeclaratorScope(scope, d.getNameDuple()); */
        for (;;) {
            if (LT(1) == IToken.tLBRACE)
                break;

            ITokenDuple duple = name(scope);

            consume(IToken.tLPAREN);
            Object expressionList = null;

            if (LT(1) != IToken.tRPAREN)
                expressionList = expression(scope);

            IToken rparen = consume(IToken.tRPAREN);

            try {
                d
                        .addConstructorMemberInitializer(null /*
                                                               * astFactory.createConstructorMemberInitializer(scope,
                                                               * duple,
                                                               * expressionList)
                                                               */);
            } catch (Exception e1) {
                logException(
                        "ctorInitializer:addConstructorMemberInitializer", e1); //$NON-NLS-1$
                throwBacktrack(startingOffset, rparen.getEndOffset(), rparen
                        .getLineNumber(), rparen.getFilename());
            }
            if (LT(1) == IToken.tLBRACE)
                break;
            consume(IToken.tCOMMA);
        }

    }

    protected boolean constructInitializersInParameters = true;

    protected boolean constructInitializersInDeclarations = true;

    /**
     * This routine parses a parameter declaration
     * 
     * @param containerObject
     *            The IParserCallback object representing the
     *            parameterDeclarationClause owning the parm.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void parameterDeclaration(IParameterCollection collection,
            Object scope) throws BacktrackException, EndOfFileException {
        IToken current = LA(1);

        DeclarationWrapper sdw = new DeclarationWrapper(scope, current
                .getOffset(), current.getLineNumber(), null, current
                .getFilename());
        declSpecifierSeq(sdw, true, false);
        if (sdw.getTypeSpecifier() == null
                && sdw.getSimpleType() != null )//IASTSimpleTypeSpecifier.Type.UNSPECIFIED)
            try {
                sdw.setTypeSpecifier(null /*
                                           * astFactory.createSimpleTypeSpecifier(
                                           * scope, sdw.getSimpleType(),
                                           * sdw.getName(), sdw.isShort(),
                                           * sdw.isLong(), sdw.isSigned(),
                                           * sdw.isUnsigned(),
                                           * sdw.isTypeNamed(), sdw.isComplex(),
                                           * sdw.isImaginary(),
                                           * sdw.isGloballyQualified(), null)
                                           */);
            }
            //            catch (ASTSemanticException e)
            //            {
            //                throwBacktrack(e.getProblem());
            //            }
            catch (Exception e) {
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                logException(
                        "parameterDeclaration:createSimpleTypeSpecifier", e); //$NON-NLS-1$
                throwBacktrack(current.getOffset(), endOffset, current
                        .getLineNumber(), current.getFilename());
            }

        if (LT(1) != IToken.tSEMI)
            initDeclarator(sdw, SimpleDeclarationStrategy.TRY_FUNCTION,
                    constructInitializersInParameters);

        if (lastToken != null)
            sdw.setEndingOffsetAndLineNumber(lastToken.getEndOffset(),
                    lastToken.getLineNumber());

        if (current == LA(1)) {
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            throwBacktrack(current.getOffset(), endOffset, current
                    .getLineNumber(), current.getFilename());
        }
        collection.addParameter(sdw);
    }

    /**
     * @param flags
     *            input flags that are used to make our decision
     * @return whether or not this looks like a constructor (true or false)
     * @throws EndOfFileException
     *             we could encounter EOF while looking ahead
     */
    private boolean lookAheadForConstructorOrConversion(Flags flags,
            DeclarationWrapper sdw) throws EndOfFileException {
        if (flags.isForParameterDeclaration())
            return false;
        if (queryLookaheadCapability(2) && LT(2) == IToken.tLPAREN
                && flags.isForConstructor())
            return true;

        IToken mark = mark();
        Declarator d = new Declarator(sdw);
        try {
            consumeTemplatedOperatorName(d);
        } catch (BacktrackException e) {
            backup(mark);
            return false;
        } catch (EndOfFileException eof) {
            backup(mark);
            return false;
        }

        ITokenDuple duple = d.getNameDuple();
        if (duple == null) {
            backup(mark);
            return false;
        }

        ITokenDuple leadingSegments = duple.getLeadingSegments();
        if (leadingSegments == null) {
            backup(mark);
            return false;
        }
        ITokenDuple lastSegment = duple.getLastSegment();
        char[] className = lastSegment.extractNameFromTemplateId();
        if (className == null || CharArrayUtils.equals(className, EMPTY_STRING)) {
            backup(mark);
            return false;
        }

        ITokenDuple secondlastSegment = leadingSegments.getLastSegment();
        if (secondlastSegment == null) {
            backup(mark);
            return false;
        }
        char[] otherName = secondlastSegment.extractNameFromTemplateId();
        if (otherName == null || CharArrayUtils.equals(otherName, EMPTY_STRING)) {
            backup(mark);
            return false;
        }

        if( lastSegment.isConversion() ) 
        {
            backup( mark ); 
            return true;
        }
        
        if (CharArrayUtils.equals(className, otherName)) {
            backup(mark);
            return true;
        }

        backup(mark);
        return false;
    }

    /**
     * @param flags
     *            input flags that are used to make our decision
     * @return whether or not this looks like a a declarator follows
     * @throws EndOfFileException
     *             we could encounter EOF while looking ahead
     */
    private boolean lookAheadForDeclarator(Flags flags)
            throws EndOfFileException {
        return flags.haveEncounteredTypename()
                && ((LT(2) != IToken.tIDENTIFIER || (LT(3) != IToken.tLPAREN && LT(3) != IToken.tASSIGN)) && !LA(
                        2).isPointer());
    }

    /**
     * This function parses a declaration specifier sequence, as according to
     * the ANSI C++ spec.
     * 
     * declSpecifier : "auto" | "register" | "static" | "extern" | "mutable" |
     * "inline" | "virtual" | "explicit" | "char" | "wchar_t" | "bool" | "short" |
     * "int" | "long" | "signed" | "unsigned" | "float" | "double" | "void" |
     * "const" | "volatile" | "friend" | "typedef" | ("typename")? name |
     * {"class"|"struct"|"union"} classSpecifier | {"enum"} enumSpecifier
     * 
     * Notes: - folded in storageClassSpecifier, typeSpecifier,
     * functionSpecifier - folded elaboratedTypeSpecifier into classSpecifier
     * and enumSpecifier - find template names in name
     * 
     * @param parm
     *            Is this for a parameter declaration (true) or simple
     *            declaration (false)
     * @param tryConstructor
     *            true for constructor, false for pointer to function strategy
     * @param decl
     *            IParserCallback object representing the declaration that owns
     *            this specifier sequence
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void declSpecifierSeq(DeclarationWrapper sdw, boolean parm,
            boolean tryConstructor) throws BacktrackException,
            EndOfFileException {
        Flags flags = new Flags(parm, tryConstructor);
        IToken typeNameBegin = null;
        IToken typeNameEnd = null;
        declSpecifiers: for (;;) {
            switch (LT(1)) {
            case IToken.t_inline:
                consume();
                sdw.setInline(true);
                break;
            case IToken.t_auto:
                consume();
                sdw.setAuto(true);
                break;
            case IToken.t_register:
                sdw.setRegister(true);
                consume();
                break;
            case IToken.t_static:
                sdw.setStatic(true);
                consume();
                break;
            case IToken.t_extern:
                sdw.setExtern(true);
                consume();
                break;
            case IToken.t_mutable:
                sdw.setMutable(true);
                consume();
                break;
            case IToken.t_virtual:
                sdw.setVirtual(true);
                consume();
                break;
            case IToken.t_explicit:
                sdw.setExplicit(true);
                consume();
                break;
            case IToken.t_typedef:
                sdw.setTypedef(true);
                consume();
                break;
            case IToken.t_friend:
                sdw.setFriend(true);
                consume();
                break;
            case IToken.t_const:
                sdw.setConst(true);
                consume();
                break;
            case IToken.t_volatile:
                sdw.setVolatile(true);
                consume();
                break;
            case IToken.t_signed:
                sdw.setSigned(true);
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.INT);
                break;
            case IToken.t_unsigned:
                sdw.setUnsigned(true);
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.INT);
                break;
            case IToken.t_short:
                sdw.setShort(true);
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.INT);
                break;
            case IToken.t_long:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.INT);
                sdw.setLong(true);
                break;
            case IToken.t__Complex:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                consume(IToken.t__Complex);
                sdw.setComplex(true);
                break;
            case IToken.t__Imaginary:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                consume(IToken.t__Imaginary);
                sdw.setImaginary(true);
                break;
            case IToken.t_char:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.CHAR);
                break;
            case IToken.t_wchar_t:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.WCHAR_T);
                break;
            case IToken.t_bool:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.BOOL);
                break;
            case IToken.t__Bool:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type._BOOL);
                break;
            case IToken.t_int:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.INT);
                break;
            case IToken.t_float:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.FLOAT);
                break;
            case IToken.t_double:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.DOUBLE);
                break;
            case IToken.t_void:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.VOID);
                break;
            case IToken.t_typename:
                sdw.setTypenamed(true);
                consume(IToken.t_typename);
                ITokenDuple duple = name(sdw.getScope());
                sdw.setTypeName(duple);
                sdw
                        .setSimpleType(null);//IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME);
                flags.setEncounteredTypename(true);
                break;
            case IToken.tCOLONCOLON:
                sdw.setGloballyQualified(true);
                consume(IToken.tCOLONCOLON);
                break;
            case IToken.tIDENTIFIER:
                // TODO - Kludgy way to handle constructors/destructors
                if (flags.haveEncounteredRawType()) {
                    setTypeName(sdw, typeNameBegin, typeNameEnd);
                    return;
                }
                if (parm && flags.haveEncounteredTypename()) {
                    setTypeName(sdw, typeNameBegin, typeNameEnd);
                    return;
                }
                if (lookAheadForConstructorOrConversion(flags, sdw)) {
                    setTypeName(sdw, typeNameBegin, typeNameEnd);
                    return;
                }
                if (lookAheadForDeclarator(flags)) {
                    setTypeName(sdw, typeNameBegin, typeNameEnd);
                    return;
                }

                ITokenDuple d = name(sdw.getScope());
                sdw.setTypeName(d);
                sdw
                        .setSimpleType(null);//IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME);
                flags.setEncounteredTypename(true);
                break;
            case IToken.t_class:
            case IToken.t_struct:
            case IToken.t_union:
                try {
                    classSpecifier(sdw);
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    elaboratedTypeSpecifier(sdw);
                    flags.setEncounteredTypename(true);
                    break;
                }
            case IToken.t_enum:
                try {
                    enumSpecifier(sdw);
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    // this is an elaborated class specifier
                    elaboratedTypeSpecifier(sdw);
                    flags.setEncounteredTypename(true);
                    break;
                }
            default:
                if (supportTypeOfUnaries && LT(1) == IGCCToken.t_typeof) {
                    IToken start = LA(1);
                    Object expression = unaryTypeofExpression(sdw.getScope());
                    if (expression != null) {
                        flags.setEncounteredTypename(true);
                        if (typeNameBegin == null)
                            typeNameBegin = start;
                        typeNameEnd = lastToken;
                    }
                }
                break declSpecifiers;
            }
        }
        setTypeName(sdw, typeNameBegin, typeNameEnd);
        return;
    }

    /**
     * @param sdw
     * @param typeNameBegin
     * @param typeNameEnd
     */
    private void setTypeName(DeclarationWrapper sdw, IToken typeNameBegin,
            IToken typeNameEnd) {
        if (typeNameBegin != null)
            sdw.setTypeName(TokenFactory.createTokenDuple(typeNameBegin,
                    typeNameEnd));
    }

    /**
     * Parse an elaborated type specifier.
     * 
     * @param decl
     *            Declaration which owns the elaborated type
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void elaboratedTypeSpecifier(DeclarationWrapper sdw)
            throws BacktrackException, EndOfFileException {
        // this is an elaborated class specifier
        IToken t = consume();
        Object eck = null;

        switch (t.getType()) {
        case IToken.t_class:
            eck = null; //ASTClassKind.CLASS;
            break;
        case IToken.t_struct:
            eck = null; //ASTClassKind.STRUCT;
            break;
        case IToken.t_union:
            eck = null; //ASTClassKind.UNION;
            break;
        case IToken.t_enum:
            eck = null; //ASTClassKind.ENUM;
            break;
        default:
            backup(t);
            throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(),
                    t.getFilename());
        }

        ITokenDuple d = name(sdw.getScope());
        Object elaboratedTypeSpec = null;
        final boolean isForewardDecl = (LT(1) == IToken.tSEMI);

        try {
            elaboratedTypeSpec = null; /*
                                        * astFactory.createElaboratedTypeSpecifier(sdw
                                        * .getScope(), eck, d, t.getOffset(),
                                        * t.getLineNumber(), d
                                        * .getLastToken().getEndOffset(),
                                        * d.getLastToken() .getLineNumber(),
                                        * isForewardDecl, sdw.isFriend()); }
                                        * catch (ASTSemanticException e) {
                                        * throwBacktrack(e.getProblem());
                                        */
        } catch (Exception e) {
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            logException(
                    "elaboratedTypeSpecifier:createElaboratedTypeSpecifier", e); //$NON-NLS-1$
            throwBacktrack(t.getOffset(), endOffset, t.getLineNumber(), t
                    .getFilename());
        }
        sdw.setTypeSpecifier(elaboratedTypeSpec);

        if (isForewardDecl) {
            //			((IASTElaboratedTypeSpecifier) elaboratedTypeSpec).acceptElement(
            //					requestor);
        }
    }

    /**
     * Parses the initDeclarator construct of the ANSI C++ spec.
     * 
     * initDeclarator : declarator ("=" initializerClause | "(" expressionList
     * ")")?
     * 
     * @param constructInitializers
     *            TODO
     * @param owner
     *            IParserCallback object that represents the owner declaration
     *            object.
     * 
     * @return declarator that this parsing produced.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected Declarator initDeclarator(DeclarationWrapper sdw,
            SimpleDeclarationStrategy strategy, boolean constructInitializers)
            throws EndOfFileException, BacktrackException {
        Declarator d = declarator(sdw, sdw.getScope(), strategy);

        try {
            //			astFactory.constructExpressions(constructInitializers);
            if (language == ParserLanguage.CPP)
                optionalCPPInitializer(d, constructInitializers);
            else if (language == ParserLanguage.C)
                optionalCInitializer(d, constructInitializers);
            sdw.addDeclarator(d);
            return d;
        } finally {
            //			astFactory.constructExpressions(true);
        }
    }

    protected void optionalCPPInitializer(Declarator d,
            boolean constructInitializers) throws EndOfFileException,
            BacktrackException {
        // handle initializer
        Object scope = d.getDeclarationWrapper().getScope();

        if (LT(1) == IToken.tASSIGN) {
            consume(IToken.tASSIGN);
            throwAwayMarksForInitializerClause(d);
            try {
                Object clause = initializerClause(scope,
                        constructInitializers);
                d.setInitializerClause(clause);
            } catch (EndOfFileException eof) {
                failParse();
                throw eof;
            }
        } else if (LT(1) == IToken.tLPAREN) {
            // initializer in constructor
            consume(IToken.tLPAREN); // EAT IT!
            Object astExpression = null;
            astExpression = expression(scope);
            consume(IToken.tRPAREN);
            d.setConstructorExpression(astExpression);
        }
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

    protected void optionalCInitializer(Declarator d,
            boolean constructInitializers) throws EndOfFileException,
            BacktrackException {
        final Object scope = d.getDeclarationWrapper().getScope();
        if (LT(1) == IToken.tASSIGN) {
            consume(IToken.tASSIGN);
            throwAwayMarksForInitializerClause(d);
            d.setInitializerClause(cInitializerClause(scope,
                    Collections.EMPTY_LIST, constructInitializers));
        }
    }

    /**
     * @param scope
     * @return
     */
    protected Object cInitializerClause(Object scope,
            List designators, boolean constructInitializers)
            throws EndOfFileException, BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;
        if (LT(1) == IToken.tLBRACE) {
            consume(IToken.tLBRACE);
            List initializerList = new ArrayList();
            for (;;) {
                int checkHashcode = LA(1).hashCode();
                // required at least one initializer list
                // get designator list
                List newDesignators = designatorList(scope);
                if (newDesignators.size() != 0)
                    if (LT(1) == IToken.tASSIGN)
                        consume(IToken.tASSIGN);
                Object initializer = cInitializerClause(scope,
                        newDesignators, constructInitializers);
                initializerList.add(initializer);
                // can end with just a '}'
                if (LT(1) == IToken.tRBRACE)
                    break;
                // can end with ", }"
                if (LT(1) == IToken.tCOMMA)
                    consume(IToken.tCOMMA);
                if (LT(1) == IToken.tRBRACE)
                    break;
                if (checkHashcode == LA(1).hashCode()) {
                    IToken l2 = LA(1);
                    throwBacktrack(startingOffset, l2.getEndOffset(), l2
                            .getLineNumber(), l2.getFilename());
                    return null;
                }

                // otherwise, its another initializer in the list
            }
            // consume the closing brace
            consume(IToken.tRBRACE);
            if (!constructInitializers)
                return null;
            return null;
        }
        // if we get this far, it means that we have not yet succeeded
        // try this now instead
        // assignmentExpression
        try {
            Object assignmentExpression = assignmentExpression(scope);
            try {
                if (!constructInitializers)
                    return null;
                return null;
            } catch (Exception e) {
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                logException("cInitializerClause:createInitializerClause", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
        } catch (BacktrackException b) {
            // do nothing
        }
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        throwBacktrack(startingOffset, endOffset, line, fn);
        return null;
    }

    /**
     *  
     */
    protected Object initializerClause(Object scope,
            boolean constructInitializers) throws EndOfFileException,
            BacktrackException {
        if (LT(1) == IToken.tLBRACE) {
            IToken t = consume(IToken.tLBRACE);
            IToken last = null;
            if (LT(1) == (IToken.tRBRACE)) {
                last = consume(IToken.tRBRACE);
                try {
                    if (!constructInitializers)
                        return null;
                    return null;
                } catch (Exception e) {
                    logException(
                            "initializerClause_1:createInitializerClause", e); //$NON-NLS-1$
                    throwBacktrack(t.getOffset(), last.getEndOffset(), t
                            .getLineNumber(), last.getFilename());
                    return null;
                }
            }

            // otherwise it is a list of initializer clauses
            List initializerClauses = null;
            int startingOffset = LA(1).getOffset();
            for (;;) {
                Object clause = initializerClause(scope,
                        constructInitializers);
                if (clause != null) {
                    if (initializerClauses == null)
                        initializerClauses = new ArrayList();
                    initializerClauses.add(clause);
                }
                if (LT(1) == IToken.tRBRACE)
                    break;
                consume(IToken.tCOMMA);
            }
            last = consume(IToken.tRBRACE);
            try {
                if (!constructInitializers)
                    return null;
                return null;
            } catch (Exception e) {
                logException("initializerClause_2:createInitializerClause", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, last.getEndOffset(), last
                        .getLineNumber(), last.getFilename());
                return null;
            }
        }

        // if we get this far, it means that we did not
        // try this now instead
        // assignmentExpression
        IToken la = LA(1);
        char[] fn = la.getFilename();
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        la = null;

        Object assignmentExpression = assignmentExpression(scope);
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            if (!constructInitializers)
                return null;
            return null;
        } catch (Exception e) {
            logException("initializerClause_3:createInitializerClause", e); //$NON-NLS-1$
        }
        throwBacktrack(startingOffset, endOffset, line, fn);
        return null;
    }

    protected List designatorList(Object scope) throws EndOfFileException,
            BacktrackException {
        List designatorList = Collections.EMPTY_LIST;
        // designated initializers for C

        if (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {

            while (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
                IToken id = null;
                Object constantExpression = null;
                /*IASTDesignator.DesignatorKind */Object kind = null;

                if (LT(1) == IToken.tDOT) {
                    consume(IToken.tDOT);
                    id = identifier();
//                    kind = IASTDesignator.DesignatorKind.FIELD;
                } else if (LT(1) == IToken.tLBRACKET) {
                    IToken mark = consume(IToken.tLBRACKET);
                    constantExpression = expression(scope);
                    if (LT(1) != IToken.tRBRACKET) {
                        backup(mark);
                        if (supportGCCStyleDesignators
                                && (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tLBRACKET)) {

                            Object d = null;
                            if (LT(1) == IToken.tIDENTIFIER) {
                                IToken identifier = identifier();
                                consume(IToken.tCOLON);
                                d = null; /*
                                           * astFactory.createDesignator(
                                           * IASTDesignator.DesignatorKind.FIELD,
                                           * null, identifier, null );
                                           */
                            } else if (LT(1) == IToken.tLBRACKET) {
                                consume(IToken.tLBRACKET);
                                Object constantExpression1 = expression(scope);
                                consume(IToken.tELLIPSIS);
                                Object constantExpression2 = expression(scope);
                                consume(IToken.tRBRACKET);
                                Map extensionParms = new Hashtable();
                                extensionParms.put(
                                        null, //IASTGCCDesignator.SECOND_EXRESSION,
                                        constantExpression2);
                                d = null; /*
                                           * astFactory.createDesignator(
                                           * IASTGCCDesignator.DesignatorKind.SUBSCRIPT_RANGE,
                                           * constantExpression1, null,
                                           * extensionParms );
                                           */
                            }

                            if (d != null) {
                                if (designatorList == Collections.EMPTY_LIST)
                                    designatorList = new ArrayList(
                                            DEFAULT_DESIGNATOR_LIST_SIZE);
                                designatorList.add(d);
                            }
                            break;
                        }
                    }
                    consume(IToken.tRBRACKET);
//                    kind = IASTDesignator.DesignatorKind.SUBSCRIPT;
                }

                Object d = null; /*
                                          * astFactory.createDesignator(kind,
                                          * constantExpression, id, null);
                                          */
                if (designatorList == Collections.EMPTY_LIST)
                    designatorList = new ArrayList(DEFAULT_DESIGNATOR_LIST_SIZE);
                designatorList.add(d);

            }
        } else {
            if (supportGCCStyleDesignators
                    && (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tLBRACKET)) {
                Object d = null;
                if (LT(1) == IToken.tIDENTIFIER) {
                    IToken identifier = identifier();
                    consume(IToken.tCOLON);
                    d = null; /*
                               * astFactory.createDesignator(
                               * IASTDesignator.DesignatorKind.FIELD, null,
                               * identifier, null );
                               */
                } else if (LT(1) == IToken.tLBRACKET) {
                    consume(IToken.tLBRACKET);
                    Object constantExpression1 = expression(scope);
                    consume(IToken.tELLIPSIS);
                    Object constantExpression2 = expression(scope);
                    consume(IToken.tRBRACKET);
                    Map extensionParms = new Hashtable();
                    extensionParms.put(null, //IASTGCCDesignator.SECOND_EXRESSION,
                            constantExpression2);
                    d = null; /*
                               * astFactory.createDesignator(
                               * IASTGCCDesignator.DesignatorKind.SUBSCRIPT_RANGE,
                               * constantExpression1, null, extensionParms );
                               */
                }
                if (d != null) {
                    if (designatorList == Collections.EMPTY_LIST)
                        designatorList = new ArrayList(
                                DEFAULT_DESIGNATOR_LIST_SIZE);
                    designatorList.add(d);
                }
            }
        }
        return designatorList;
    }

    /**
     * Parse a declarator, as according to the ANSI C++ specification.
     * 
     * declarator : (ptrOperator)* directDeclarator
     * 
     * directDeclarator : declaratorId | directDeclarator "("
     * parameterDeclarationClause ")" (cvQualifier)* (exceptionSpecification)* |
     * directDeclarator "[" (constantExpression)? "]" | "(" declarator")" |
     * directDeclarator "(" parameterDeclarationClause ")"
     * (oldKRParameterDeclaration)*
     * 
     * declaratorId : name
     * 
     * @param container
     *            IParserCallback object that represents the owner declaration.
     * 
     * @return declarator that this parsing produced.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected Declarator declarator(IDeclaratorOwner owner, Object scope,
            SimpleDeclarationStrategy strategy) throws EndOfFileException,
            BacktrackException {
        Declarator d = null;
        DeclarationWrapper sdw = owner.getDeclarationWrapper();
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;
        overallLoop: do {
            d = new Declarator(owner);

            consumePointerOperators(d);

            if (LT(1) == IToken.tLPAREN) {
                consume();
                declarator(d, scope, strategy);
                consume(IToken.tRPAREN);
            } else
                consumeTemplatedOperatorName(d);

            for (;;) {
                switch (LT(1)) {
                case IToken.tLPAREN:

                    boolean failed = false;
                    Object parameterScope = null; /*astFactory
                     .getDeclaratorScope(scope, d.getNameDuple()); */
                    // temporary fix for initializer/function declaration
                    // ambiguity
                    if (queryLookaheadCapability(2)
                            && !LA(2).looksLikeExpression()
                            && strategy != SimpleDeclarationStrategy.TRY_VARIABLE) {
                        if (LT(2) == IToken.tIDENTIFIER) {
                            IToken newMark = mark();
                            consume(IToken.tLPAREN);
                            ITokenDuple queryName = null;
                            try {
                                try {
                                    queryName = name(parameterScope);
                                    failed = true;
                                } catch (Exception e) {
                                    int endOffset = (lastToken != null) ? lastToken
                                            .getEndOffset()
                                            : 0;
                                    logException(
                                            "declarator:queryIsTypeName", e); //$NON-NLS-1$
                                    throwBacktrack(startingOffset, endOffset,
                                            line, newMark.getFilename());
                                }
                            } catch (BacktrackException b) {
                                failed = true;
                            }

                            if (queryName != null)
                                queryName.freeReferences();
                            backup(newMark);
                        }
                    }
                    if ((queryLookaheadCapability(2)
                            && !LA(2).looksLikeExpression()
                            && strategy != SimpleDeclarationStrategy.TRY_VARIABLE && !failed)
                            || !queryLookaheadCapability(3)) {
                        // parameterDeclarationClause
                        d.setIsFunction(true);
                        // TODO need to create a temporary scope object here
                        consume(IToken.tLPAREN);
                        boolean seenParameter = false;
                        parameterDeclarationLoop: for (;;) {
                            switch (LT(1)) {
                            case IToken.tRPAREN:
                                consume();
                                break parameterDeclarationLoop;
                            case IToken.tELLIPSIS:
                                consume();
                                d.setIsVarArgs(true);
                                break;
                            case IToken.tCOMMA:
                                consume();
                                seenParameter = false;
                                break;
                            default:
                                int endOffset = (lastToken != null) ? lastToken
                                        .getEndOffset() : 0;
                                if (seenParameter)
                                    throwBacktrack(startingOffset, endOffset,
                                            line, fn);
                                parameterDeclaration(d, parameterScope);
                                seenParameter = true;
                            }
                        }
                    }

                    if (LT(1) == IToken.tCOLON || LT(1) == IToken.t_try)
                        break overallLoop;

                    IToken beforeCVModifier = mark();
                    IToken[] cvModifiers = new IToken[2];
                    int numCVModifiers = 0;
                    IToken afterCVModifier = beforeCVModifier;
                    // const-volatile
                    // 2 options: either this is a marker for the method,
                    // or it might be the beginning of old K&R style
                    // parameter declaration, see
                    //      void getenv(name) const char * name; {}
                    // This will be determined further below
                    while ((LT(1) == IToken.t_const || LT(1) == IToken.t_volatile)
                            && numCVModifiers < 2) {
                        cvModifiers[numCVModifiers++] = consume();
                        afterCVModifier = mark();
                    }
                    //check for throws clause here
                    List exceptionSpecIds = null;
                    if (LT(1) == IToken.t_throw) {
                        exceptionSpecIds = new ArrayList();
                        consume(); // throw
                        consume(IToken.tLPAREN); // (
                        boolean done = false;
                        Object exceptionTypeId = null;
                        while (!done) {
                            switch (LT(1)) {
                            case IToken.tRPAREN:
                                consume();
                                done = true;
                                break;
                            case IToken.tCOMMA:
                                consume();
                                break;
                            default:
                                try {
                                    exceptionTypeId = typeId(scope, false);
                                    exceptionSpecIds.add(exceptionTypeId);
                                    //											exceptionTypeId
                                    //													.acceptElement(
                                    //															requestor);
                                } catch (BacktrackException e) {
                                    failParse(e);
                                    consume();
                                    // eat this token anyway
                                    continue;
                                }
                                break;
                            }
                        }
                        if (exceptionSpecIds != null)
                            try {
                                d.setExceptionSpecification(null /*astFactory
                                 .createExceptionSpecification(d
                                 .getDeclarationWrapper()
                                 .getScope(),
                                 exceptionSpecIds) */);
                                //								} catch (ASTSemanticException e) {
                                //									throwBacktrack(e.getProblem());
                            } catch (Exception e) {
                                int endOffset = (lastToken != null) ? lastToken
                                        .getEndOffset() : 0;
                                logException(
                                        "declarator:createExceptionSpecification", e); //$NON-NLS-1$
                                throwBacktrack(startingOffset, endOffset, line,
                                        fn);
                            }
                    }
                    // check for optional pure virtual
                    if (LT(1) == IToken.tASSIGN && LT(2) == IToken.tINTEGER) {
                        char[] image = LA(2).getCharImage();
                        if (image.length == 1 && image[0] == '0') {
                            consume(IToken.tASSIGN);
                            consume(IToken.tINTEGER);
                            d.setPureVirtual(true);
                        }
                    }
                    if (afterCVModifier != LA(1) || LT(1) == IToken.tSEMI) {
                        // There were C++-specific clauses after
                        // const/volatile modifier
                        // Then it is a marker for the method
                        if (numCVModifiers > 0) {
                            for (int i = 0; i < numCVModifiers; i++) {
                                if (cvModifiers[i].getType() == IToken.t_const)
                                    d.setConst(true);
                                if (cvModifiers[i].getType() == IToken.t_volatile)
                                    d.setVolatile(true);
                            }
                        }
                        afterCVModifier = mark();
                        // In this case (method) we can't expect K&R
                        // parameter declarations,
                        // but we'll check anyway, for errorhandling
                    }
                    break;
                case IToken.tLBRACKET:
                    consumeArrayModifiers(d, sdw.getScope());
                    continue;
                case IToken.tCOLON:
                    consume(IToken.tCOLON);
                    Object exp = constantExpression(scope);
                    d.setBitFieldExpression(exp);
                default:
                    break;
                }
                break;
            }
            if (LA(1).getType() != IToken.tIDENTIFIER)
                break;

        } while (true);
        if (d.getOwner() instanceof IDeclarator)
            ((Declarator) d.getOwner()).setOwnedDeclarator(d);
        return d;
    }

    protected void consumeTemplatedOperatorName(Declarator d)
            throws EndOfFileException, BacktrackException {
        TemplateParameterManager argumentList = TemplateParameterManager
                .getInstance();
        try {
            if (LT(1) == IToken.t_operator)
                operatorId(d, null, null);
            else {
                try {
                    ITokenDuple duple = name(d.getDeclarationWrapper()
                            .getScope());
                    d.setName(duple);

                } catch (BacktrackException bt) {
                    Declarator d1 = d;
                    Declarator d11 = d1;
                    IToken start = null;

                    boolean hasTemplateId = false;

                    IToken mark = mark();
                    if (LT(1) == IToken.tCOLONCOLON
                            || LT(1) == IToken.tIDENTIFIER) {
                        start = consume();
                        IToken end = null;

                        if (start.getType() == IToken.tIDENTIFIER) {
                            end = consumeTemplateArguments(d
                                    .getDeclarationWrapper().getScope(), end,
                                    argumentList);
                            if (end != null && end.getType() == IToken.tGT)
                                hasTemplateId = true;
                        }

                        while (LT(1) == IToken.tCOLONCOLON
                                || LT(1) == IToken.tIDENTIFIER) {
                            end = consume();
                            if (end.getType() == IToken.tIDENTIFIER) {
                                end = consumeTemplateArguments(d
                                        .getDeclarationWrapper().getScope(),
                                        end, argumentList);
                                if (end.getType() == IToken.tGT)
                                    hasTemplateId = true;
                            }
                        }
                        if (LT(1) == IToken.t_operator)
                            operatorId(d11, start,
                                    (hasTemplateId ? argumentList : null));
                        else {
                            int endOffset = (lastToken != null) ? lastToken
                                    .getEndOffset() : 0;
                            backup(mark);
                            throwBacktrack(mark.getOffset(), endOffset, mark
                                    .getLineNumber(), mark.getFilename());
                        }
                    }
                }
            }
        } finally {
            TemplateParameterManager.returnInstance(argumentList);
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
    protected void enumSpecifier(DeclarationWrapper sdw)
            throws BacktrackException, EndOfFileException {
        IToken mark = mark();
        IToken identifier = null;
        consume(IToken.t_enum);
        if (LT(1) == IToken.tIDENTIFIER) {
            identifier = identifier();
        }
        if (LT(1) == IToken.tLBRACE) {
            Object enumeration = null;
            try {
                enumeration = null; /*astFactory.createEnumerationSpecifier(sdw
                 .getScope(), ((identifier == null)
                 ? EMPTY_STRING : identifier.getCharImage()), //$NON-NLS-1$
                 mark.getOffset(), mark.getLineNumber(),
                 ((identifier == null) ? mark.getOffset() : identifier
                 .getOffset()), ((identifier == null) ? mark
                 .getEndOffset() : identifier.getEndOffset()),
                 ((identifier == null)
                 ? mark.getLineNumber()
                 : identifier.getLineNumber()), mark.getFilename());
                 } catch (ASTSemanticException e) {
                 throwBacktrack(e.getProblem()); */
            } catch (Exception e) {
                int endOffset = (lastToken != null) ? lastToken.getEndOffset()
                        : 0;
                logException("enumSpecifier:createEnumerationSpecifier", e); //$NON-NLS-1$
                throwBacktrack(mark.getOffset(), endOffset, mark
                        .getLineNumber(), mark.getFilename());
            }
            cleanupLastToken();
            consume(IToken.tLBRACE);
            while (LT(1) != IToken.tRBRACE) {
                IToken enumeratorIdentifier = null;
                if (LT(1) == IToken.tIDENTIFIER) {
                    enumeratorIdentifier = identifier();
                } else {
                    IToken la = LA(1);
                    throwBacktrack(la.getOffset(), la.getEndOffset(), la
                            .getLineNumber(), la.getFilename());
                }
                Object initialValue = null;
                if (LT(1) == IToken.tASSIGN) {
                    consume(IToken.tASSIGN);
                    initialValue = constantExpression(sdw.getScope());
                }
                Object enumerator = null;
                if (LT(1) == IToken.tRBRACE) {
                    try {
                        enumerator = null; /*astFactory.addEnumerator(enumeration,
                         enumeratorIdentifier.getCharImage(),
                         enumeratorIdentifier.getOffset(),
                         enumeratorIdentifier.getLineNumber(),
                         enumeratorIdentifier.getOffset(),
                         enumeratorIdentifier.getEndOffset(),
                         enumeratorIdentifier.getLineNumber(), lastToken
                         .getEndOffset(), lastToken
                         .getLineNumber(), initialValue, lastToken.getFilename()); */
                        cleanupLastToken();
                        //					} catch (ASTSemanticException e1) {
                        //						throwBacktrack(e1.getProblem());
                    } catch (Exception e) {
                        int endOffset = (lastToken != null) ? lastToken
                                .getEndOffset() : 0;
                        logException("enumSpecifier:addEnumerator", e); //$NON-NLS-1$
                        throwBacktrack(mark.getOffset(), endOffset, mark
                                .getLineNumber(), mark.getFilename());
                    }
                    break;
                }
                if (LT(1) != IToken.tCOMMA) {
//                    enumeration.freeReferences();
//                  enumerator.freeReferences();
                    int endOffset = (lastToken != null) ? lastToken
                            .getEndOffset() : 0;
                    throwBacktrack(mark.getOffset(), endOffset, mark
                            .getLineNumber(), mark.getFilename());
                }
                try {
                    enumerator = null; /*astFactory.addEnumerator(enumeration,
                     enumeratorIdentifier.getCharImage(),
                     enumeratorIdentifier.getOffset(),
                     enumeratorIdentifier.getLineNumber(),
                     enumeratorIdentifier.getOffset(),
                     enumeratorIdentifier.getEndOffset(),
                     enumeratorIdentifier.getLineNumber(), lastToken
                     .getEndOffset(), lastToken.getLineNumber(),
                     initialValue, lastToken.getFilename()); */
                    cleanupLastToken();
                    //				} catch (ASTSemanticException e1) {
                    //					throwBacktrack(e1.getProblem());
                } catch (Exception e) {
                    int endOffset = (lastToken != null) ? lastToken
                            .getEndOffset() : 0;
                    logException("enumSpecifier:addEnumerator", e); //$NON-NLS-1$
                    throwBacktrack(mark.getOffset(), endOffset, mark
                            .getLineNumber(), mark.getFilename());
                }
                consume(IToken.tCOMMA);
            }
            IToken t = consume(IToken.tRBRACE);
//                enumeration.setEndingOffsetAndLineNumber(t.getEndOffset(), t
//                        .getLineNumber());
            //			enumeration.acceptElement(requestor);
            sdw.setTypeSpecifier(enumeration);
        } else {
            // enumSpecifierAbort
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            backup(mark);
            throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(),
                    mark.getFilename());
        }
    }

    /**
     * Parse a class/struct/union definition.
     * 
     * classSpecifier : classKey name (baseClause)? "{" (memberSpecification)*
     * "}"
     * 
     * @param owner
     *            IParserCallback object that represents the declaration that
     *            owns this classSpecifier
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void classSpecifier(DeclarationWrapper sdw)
            throws BacktrackException, EndOfFileException {
        Object nameType = null; //ClassNameType.IDENTIFIER;
        Object classKind = null;
        Object access = null; //ASTAccessVisibility.PUBLIC;
        IToken classKey = null;
        IToken mark = mark();

        // class key
        switch (LT(1)) {
        case IToken.t_class:
            classKey = consume();
            classKind = null; //ASTClassKind.CLASS;
            access = null; //ASTAccessVisibility.PRIVATE;
            break;
        case IToken.t_struct:
            classKey = consume();
            classKind = null; //ASTClassKind.STRUCT;
            break;
        case IToken.t_union:
            classKey = consume();
            classKind = null; //ASTClassKind.UNION;
            break;
        default:
            throwBacktrack(mark.getOffset(), mark.getEndOffset(), mark
                    .getLineNumber(), mark.getFilename());
        }

        ITokenDuple duple = null;

        // class name
        if (LT(1) == IToken.tIDENTIFIER)
            duple = name(sdw.getScope());
        if (duple != null && !duple.isIdentifier())
            nameType = null; //ClassNameType.TEMPLATE;
        if (LT(1) != IToken.tCOLON && LT(1) != IToken.tLBRACE) {
            IToken errorPoint = LA(1);
            backup(mark);
            throwBacktrack(errorPoint.getOffset(), errorPoint.getEndOffset(),
                    errorPoint.getLineNumber(), errorPoint.getFilename());
        }
        Object astClassSpecifier = null;

        try {
            astClassSpecifier = null; /*astFactory.createClassSpecifier(sdw.getScope(),
             duple, classKind, nameType, access, classKey.getOffset(),
             classKey.getLineNumber(), duple == null ? classKey
             .getOffset() : duple.getFirstToken().getOffset(),
             duple == null ? classKey.getEndOffset() : duple
             .getFirstToken().getEndOffset(), duple == null
             ? classKey.getLineNumber()
             : duple.getFirstToken().getLineNumber(), classKey.getFilename());
             } catch (ASTSemanticException e) {
             throwBacktrack(e.getProblem()); */
        } catch (Exception e) {
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            logException("classSpecifier:createClassSpecifier", e); //$NON-NLS-1$
            throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(),
                    mark.getFilename());
        }
        sdw.setTypeSpecifier(astClassSpecifier);
        // base clause
        if (LT(1) == IToken.tCOLON) {
            baseSpecifier(astClassSpecifier);
        }

        if (LT(1) == IToken.tLBRACE) {
            consume(IToken.tLBRACE);
            //			astClassSpecifier.enterScope(requestor);

            try {
                cleanupLastToken();
                memberDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
                    int checkToken = LA(1).hashCode();
                    switch (LT(1)) {
                    case IToken.t_public:
                        consume();
                        consume(IToken.tCOLON);
//                            astClassSpecifier
//                                    .setCurrentVisibility(ASTAccessVisibility.PUBLIC);
                        break;
                    case IToken.t_protected:
                        consume();
                        consume(IToken.tCOLON);
//                            astClassSpecifier
//                                    .setCurrentVisibility(ASTAccessVisibility.PROTECTED);
                        break;

                    case IToken.t_private:
                        consume();
                        consume(IToken.tCOLON);
//                            astClassSpecifier
//                                    .setCurrentVisibility(ASTAccessVisibility.PRIVATE);
                        break;
                    case IToken.tRBRACE:
                        consume(IToken.tRBRACE);
                        break memberDeclarationLoop;
                    default:
                        try {
                            declaration(astClassSpecifier, null);
                        } catch (BacktrackException bt) {
                            if (checkToken == LA(1).hashCode())
                                failParseWithErrorHandling();
                        }
                    }
                    if (checkToken == LA(1).hashCode())
                        failParseWithErrorHandling();
                }
                // consume the }
                IToken lt = consume(IToken.tRBRACE);
//                astClassSpecifier.setEndingOffsetAndLineNumber(lt
//                            .getEndOffset(), lt.getLineNumber());
                //				try {
                //					astFactory.signalEndOfClassSpecifier(astClassSpecifier);
                //				} catch (Exception e1) {
                //					logException("classSpecifier:signalEndOfClassSpecifier", e1); //$NON-NLS-1$
                //					throwBacktrack(lt.getOffset(), lt.getEndOffset(), lt.getLineNumber(), lt.getFilename());
                //				}

            } finally {
                //				astClassSpecifier.exitScope(requestor);				
            }

        }
    }

    /**
     * Parse the subclass-baseclauses for a class specification.  
     * 
     * baseclause:	: basespecifierlist
     * basespecifierlist: 	basespecifier
     * 						basespecifierlist, basespecifier
     * basespecifier:	::? nestednamespecifier? classname
     * 					virtual accessspecifier? ::? nestednamespecifier? classname
     * 					accessspecifier virtual? ::? nestednamespecifier? classname
     * accessspecifier:	private | protected | public
     * @param classSpecOwner
     * @throws BacktrackException
     */
    protected void baseSpecifier(Object astClassSpec)
            throws EndOfFileException, BacktrackException {
        IToken la = LA(1);
        char[] fn = la.getFilename();
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        la = null;
        consume(IToken.tCOLON);

        boolean isVirtual = false;
        Object visibility = null; //ASTAccessVisibility.PUBLIC;
        ITokenDuple nameDuple = null;

        ArrayList bases = null;

        baseSpecifierLoop: for (;;) {
            switch (LT(1)) {
            case IToken.t_virtual:
                consume(IToken.t_virtual);
                isVirtual = true;
                break;
            case IToken.t_public:
                consume();
                break;
            case IToken.t_protected:
                consume();
                visibility = null; //ASTAccessVisibility.PROTECTED;
                break;
            case IToken.t_private:
                visibility = null; //ASTAccessVisibility.PRIVATE;
                consume();
                break;
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
                //to get templates right we need to use the class as the scope
                nameDuple = name(astClassSpec);
                break;
            case IToken.tCOMMA:
                //because we are using the class as the scope to get the name, we need to postpone adding the base 
                //specifiers until after we have all the nameDuples
                if (bases == null) {
                    bases = new ArrayList(4);
                }
                bases.add(new Object[] {
                        isVirtual ? Boolean.TRUE : Boolean.FALSE, visibility,
                        nameDuple });

                isVirtual = false;
                visibility = null; //ASTAccessVisibility.PUBLIC;
                nameDuple = null;
                consume();
                continue baseSpecifierLoop;
            default:
                break baseSpecifierLoop;
            }
        }

        try {
            if (bases != null) {
                int size = bases.size();
                for (int i = 0; i < size; i++) {
                    Object[] data = (Object[]) bases.get(i);
                    //	            		try {
                    //							astFactory.addBaseSpecifier( astClassSpec, 
                    //									                     ((Boolean)data[0]).booleanValue(),
                    //							                             (ASTAccessVisibility) data[1], 
                    //														 (ITokenDuple)data[2] );
                    //						} catch (ASTSemanticException e1) {
                    //							failParse( e1.getProblem() );
                    //						}
                }
            }

            //	        	astFactory.addBaseSpecifier(
            //	                astClassSpec,
            //	                isVirtual,
            //	                visibility,
            //	                nameDuple );
            //	        }
            //	        catch (ASTSemanticException e)
            //	        {
            //				failParse( e.getProblem() );
        } catch (Exception e) {
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            logException("baseSpecifier_2::addBaseSpecifier", e); //$NON-NLS-1$
            throwBacktrack(startingOffset, endOffset, line, fn);
        }
    }

    /**
     * Parses a function body.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void functionBody(Object scope) throws EndOfFileException,
            BacktrackException {
        compoundStatement(scope, false);
    }

    /**
     * Parses a statement.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void statement(Object scope) throws EndOfFileException,
            BacktrackException {

        switch (LT(1)) {
        case IToken.t_case:
            consume(IToken.t_case);
            constantExpression(scope);
            cleanupLastToken();
            consume(IToken.tCOLON);
            statement(scope);
            cleanupLastToken();
            return;
        case IToken.t_default:
            consume(IToken.t_default);
            consume(IToken.tCOLON);
            statement(scope);
            cleanupLastToken();
            return;
        case IToken.tLBRACE:
            compoundStatement(scope, true);
            cleanupLastToken();
            return;
        case IToken.t_if:
            consume(IToken.t_if);
            consume(IToken.tLPAREN);
            condition(scope);
            consume(IToken.tRPAREN);
            if (LT(1) != IToken.tLBRACE)
                singleStatementScope(scope);
            else
                statement(scope);
            if (LT(1) == IToken.t_else) {
                consume(IToken.t_else);
                if (LT(1) == IToken.t_if) {
                    //an else if, return and get the rest of the else if as
                    // the next statement instead of recursing
                    cleanupLastToken();
                    return;
                } else if (LT(1) != IToken.tLBRACE)
                    singleStatementScope(scope);
                else
                    statement(scope);
            }
            cleanupLastToken();
            return;
        case IToken.t_switch:
            consume();
            consume(IToken.tLPAREN);
            condition(scope);
            consume(IToken.tRPAREN);
            statement(scope);
            cleanupLastToken();
            return;
        case IToken.t_while:
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            condition(scope);
            consume(IToken.tRPAREN);
            if (LT(1) != IToken.tLBRACE)
                singleStatementScope(scope);
            else
                statement(scope);
            cleanupLastToken();
            return;
        case IToken.t_do:
            consume(IToken.t_do);
            if (LT(1) != IToken.tLBRACE)
                singleStatementScope(scope);
            else
                statement(scope);
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            condition(scope);
            consume(IToken.tRPAREN);
            cleanupLastToken();
            return;
        case IToken.t_for:
            consume();
            consume(IToken.tLPAREN);
            forInitStatement(scope);
            if (LT(1) != IToken.tSEMI)
                condition(scope);
            consume(IToken.tSEMI);
            if (LT(1) != IToken.tRPAREN) {
                expression(scope);
                cleanupLastToken();
            }
            consume(IToken.tRPAREN);
            statement(scope);
            cleanupLastToken();
            return;
        case IToken.t_break:
            consume();
            consume(IToken.tSEMI);
            cleanupLastToken();
            return;
        case IToken.t_continue:
            consume();
            consume(IToken.tSEMI);
            cleanupLastToken();
            return;
        case IToken.t_return:
            consume();
            if (LT(1) != IToken.tSEMI) {
                expression(scope);
                cleanupLastToken();
            }
            consume(IToken.tSEMI);
            cleanupLastToken();
            return;
        case IToken.t_goto:
            consume();
            consume(IToken.tIDENTIFIER);
            consume(IToken.tSEMI);
            cleanupLastToken();
            return;
        case IToken.t_try:
            consume();
            compoundStatement(scope, true);
            catchHandlerSequence(scope);
            cleanupLastToken();
            return;
        case IToken.tSEMI:
            consume();
            cleanupLastToken();
            return;
        default:
            // can be many things:
            // label

            if (queryLookaheadCapability(2) && LT(1) == IToken.tIDENTIFIER
                    && LT(2) == IToken.tCOLON) {
                consume(IToken.tIDENTIFIER);
                consume(IToken.tCOLON);
                statement(scope);
                cleanupLastToken();
                return;
            }
            // expressionStatement
            // Note: the function style cast ambiguity is handled in
            // expression
            // Since it only happens when we are in a statement
            IToken mark = mark();
            Object expressionStatement = null;
            try {
                expressionStatement = expression(scope);
                consume(IToken.tSEMI);
                cleanupLastToken();
                return;
            } catch (BacktrackException b) {
                backup(mark);
                //					if (expressionStatement != null)
                //						expressionStatement.freeReferences();
            }

            // declarationStatement
            declaration(scope, null);
        }

    }

    protected void catchHandlerSequence(Object scope)
            throws EndOfFileException, BacktrackException {
        if (LT(1) != IToken.t_catch) {
            IToken la = LA(1);
            throwBacktrack(la.getOffset(), la.getEndOffset(), la
                    .getLineNumber(), la.getFilename()); // error, need at least one of these
        }
        while (LT(1) == IToken.t_catch) {
            consume(IToken.t_catch);
            consume(IToken.tLPAREN);
            try {
                if (LT(1) == IToken.tELLIPSIS)
                    consume(IToken.tELLIPSIS);
                else
                    simpleDeclaration(SimpleDeclarationStrategy.TRY_VARIABLE,
                            scope, null, true);
                consume(IToken.tRPAREN);

                catchBlockCompoundStatement(scope);
            } catch (BacktrackException bte) {
                failParse(bte);
                failParseWithErrorHandling();
            }
        }
    }

    protected void catchBlockCompoundStatement(Object scope)
            throws BacktrackException, EndOfFileException {
        if (mode == ParserMode.QUICK_PARSE
                || mode == ParserMode.STRUCTURAL_PARSE)
            skipOverCompoundStatement();
        else if (mode == ParserMode.COMPLETION_PARSE
                || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                compoundStatement(scope, true);
            else
                skipOverCompoundStatement();
        } else if (mode == ParserMode.COMPLETE_PARSE)
            compoundStatement(scope, true);
    }

    protected void singleStatementScope(Object scope)
            throws EndOfFileException, BacktrackException {
        Object newScope;
        try {
            newScope = null; /*astFactory.createNewCodeBlock(scope); */
        } catch (Exception e) {
            logException("singleStatementScope:createNewCodeBlock", e); //$NON-NLS-1$
            IToken la = LA(1);
            throwBacktrack(la.getOffset(), la.getEndOffset(), la
                    .getLineNumber(), la.getFilename());
            return;
        }
        //		newScope.enterScope(requestor);
        try {
            statement(newScope);
        } finally {
            //			newScope.exitScope(requestor);
        }
    }

    /**
     * @throws BacktrackException
     */
    protected void condition(Object scope) throws BacktrackException,
            EndOfFileException {
        expression(scope);
        cleanupLastToken();
    }

    /**
     * @throws BacktrackException
     */
    protected void forInitStatement(Object scope) throws BacktrackException,
            EndOfFileException {
        IToken mark = mark();
        try {
            expression(scope);
            consume(IToken.tSEMI);
            //			e.acceptElement(requestor);

        } catch (BacktrackException bt) {
            backup(mark);
            try {
                simpleDeclarationStrategyUnion(scope, null);
            } catch (BacktrackException b) {
                failParse(b);
                throwBacktrack(b);
            }
        }

    }

    /**
     * @throws BacktrackException
     */
    protected void compoundStatement(Object scope, boolean createNewScope)
            throws EndOfFileException, BacktrackException {
        IToken la = LA(1);
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        int startingOffset = consume(IToken.tLBRACE).getOffset();

        Object newScope = null;
        if (createNewScope) {
            try {
                newScope = null; /*astFactory.createNewCodeBlock(scope); */
            } catch (Exception e) {
                int endOffset = (lastToken == null) ? 0 : lastToken
                        .getEndOffset();
                logException("compoundStatement:createNewCodeBlock", e); //$NON-NLS-1$
                throwBacktrack(startingOffset, endOffset, line, fn);
            }
            //			newScope.enterScope(requestor);
        }

        try {

            while (LT(1) != IToken.tRBRACE) {
                int checkToken = LA(1).hashCode();
                try {
                    statement((createNewScope ? newScope
                            : scope));
                } catch (BacktrackException b) {
                    failParse(b);
                    if (LA(1).hashCode() == checkToken)
                        failParseWithErrorHandling();
                }
            }

            consume(IToken.tRBRACE);
        } finally {
            //			if (createNewScope)
            //				newScope.exitScope(requestor);
        }
    }

    protected Object compilationUnit;

    protected IToken simpleDeclarationMark;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.IParser#getLanguage()
     */
    public ParserLanguage getLanguage() {
        return language;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser.IParser#setLanguage(Language)
     */
    public void setLanguage(ParserLanguage l) {
        language = l;
    }

    /**
     *  
     */
    protected void cleanupLastToken() {
        if (lastToken != null)
            lastToken.setNext(null);
        simpleDeclarationMark = null;
    }

    /**

     /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IParser#cancel()
     */
    public synchronized void cancel() {
        isCancelled = true;
        scanner.cancel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser.Parser#handleOffsetLimitException()
     */
    protected void handleOffsetLimitException(
            OffsetLimitReachedException exception) throws EndOfFileException {
        if (mode != ParserMode.COMPLETION_PARSE)
            throw new EndOfFileException();
        throw exception;
    }

}