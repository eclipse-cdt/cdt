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
package org.eclipse.cdt.internal.core.parser2.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.gcc.IGCCASTCompoundStatementExpression;
import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.SimpleDeclarationStrategy;
import org.eclipse.cdt.internal.core.parser.TemplateParameterManager;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.parser2.DeclarationWrapper;
import org.eclipse.cdt.internal.core.parser2.Declarator;
import org.eclipse.cdt.internal.core.parser2.IDeclarator;
import org.eclipse.cdt.internal.core.parser2.IDeclaratorOwner;
import org.eclipse.cdt.internal.core.parser2.IParameterCollection;
import org.eclipse.cdt.internal.core.parser2.ParameterCollection;
import org.eclipse.cdt.internal.core.parser2.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.parser2.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.parser2.c.CASTCompoundStatementExpression;
import org.eclipse.cdt.internal.core.parser2.c.CASTConditionalExpression;
import org.eclipse.cdt.internal.core.parser2.c.CASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.parser2.c.CASTEnumerator;
import org.eclipse.cdt.internal.core.parser2.c.CASTExpressionList;
import org.eclipse.cdt.internal.core.parser2.c.CASTName;
import org.eclipse.cdt.internal.core.parser2.c.CASTUnaryExpression;

/**
 * This is our implementation of the IParser interface, serving as a
 * parser for GNU C and C++.
 * 
 * From time to time we will make reference to the ANSI ISO specifications.
 * 
 * @author jcamelon
 */
public class GNUCPPSourceParser extends AbstractGNUSourceCodeParser {
    
    private ScopeStack templateIdScopes = new ScopeStack();
    protected Object translationUnit;
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

    protected List templateArgumentList()
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
                Object typeId = typeId(false);

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
                    expression = assignmentExpression();

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
                    ITokenDuple nameDuple = name();
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
        ITokenDuple duple = name();
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
    protected ITokenDuple name() throws BacktrackException,
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
                last = consumeTemplateArguments(last, argumentList);
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

                if (LT(1) == IToken.t_template)
                    consume();

                if (LT(1) == IToken.tCOMPL)
                    consume();

                switch (LT(1)) {
                case IToken.t_operator:
                    IToken l = LA(1);
                    backup(mark);
                    throwBacktrack(first.getOffset(), l.getEndOffset(), first
                            .getLineNumber(), l.getFilename());
                case IToken.tIDENTIFIER:
                    last = consume();
                    last = consumeTemplateArguments(last, argumentList);
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
     * @param last
     * @param argumentList
     * @return @throws
     *         EndOfFileException
     * @throws BacktrackException
     */
    protected IToken consumeTemplateArguments(IToken last, TemplateParameterManager argumentList) throws EndOfFileException,
            BacktrackException {
//        if (language != ParserLanguage.CPP)
//            return last;
        if (LT(1) == IToken.tLT) {
            IToken secondMark = mark();
            consume(IToken.tLT);
            try {
                List list = templateArgumentList();
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
            if (allowCPPRestrict) {
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
            typeId(true);
            toSend = lastToken;
        }

        boolean hasTemplateId = (templateArgs != null);
        boolean grabbedNewInstance = false;
        if (templateArgs == null) {
            templateArgs = TemplateParameterManager.getInstance();
            grabbedNewInstance = true;
        }

        try {
            toSend = consumeTemplateArguments(toSend, templateArgs);
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
                    nameDuple = name();
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
    protected IASTExpression assignmentExpression()
            throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.t_throw) {
            return throwExpression();
        }

        IASTExpression conditionalExpression = conditionalExpression();
        // if the condition not taken, try assignment operators
        if (conditionalExpression != null) //&&
                                           // conditionalExpression.getExpressionKind()
                                           // ==
                                           // IASTExpression.Kind.CONDITIONALEXPRESSION
                                           // )
            return conditionalExpression;
        switch (LT(1)) {
        case IToken.tASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_assign,
                    conditionalExpression);
        case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_multiplyAssign,
                    conditionalExpression);
        case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_divideAssign,
                    conditionalExpression);
        case IToken.tMODASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_moduloAssign,
                    conditionalExpression);
        case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_plusAssign,
                    conditionalExpression);
        case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_minusAssign,
                    conditionalExpression);
        case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_shiftRightAssign,
                    conditionalExpression);
        case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_shiftLeftAssign,
                    conditionalExpression);
        case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_binaryAndAssign,
                    conditionalExpression);
        case IToken.tXORASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_binaryXorAssign,
                    conditionalExpression);
        case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_binaryOrAssign,
                    conditionalExpression);
        }
        return conditionalExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression throwExpression() throws EndOfFileException,
            BacktrackException {
        IToken throwToken = consume(IToken.t_throw);
        IASTExpression throwExpression = null;
        throwExpression = expression();

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
     * @throws BacktrackException
     */
    protected IASTExpression relationalExpression()
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        IASTExpression firstExpression = shiftExpression();
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
                Object secondExpression = shiftExpression();
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
                if ( supportMinAndMaxOperators
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
                    Object se = shiftExpression();
                    if (next == LA(1)) {
                        backup(m);
                        return firstExpression;
                    }
                    IASTExpression resultExpression = null;
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
    protected IASTExpression multiplicativeExpression()
            throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        IASTExpression firstExpression = pmExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
                IToken t = consume();
            	IASTExpression secondExpression = pmExpression();
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
    protected IASTExpression pmExpression() throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();

        IASTExpression firstExpression = castExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tDOTSTAR:
            case IToken.tARROWSTAR:
                IToken t = consume();
                Object secondExpression = castExpression();
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
    protected IASTExpression castExpression() throws EndOfFileException,
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
                    typeId = typeId(false);
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
                Object castExpression = castExpression();
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
        return unaryExpression();

    }

    /**
     * @throws BacktrackException
     */
    protected IASTTypeId typeId(boolean skipArrayModifiers)
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
                name = name();
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
                    name = name();
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
                    name = name();
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

//        TypeId id = getTypeIdInstance();
        IToken last = lastToken;
        IToken temp = last;

        //template parameters are consumed as part of name
        //lastToken = consumeTemplateParameters( last );
        //if( lastToken == null ) lastToken = last;

        temp = consumePointerOperators(null);
        if (temp != null)
            last = temp;

        if (!skipArrayModifiers) {
            temp = consumeArrayModifiers(null);
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
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression deleteExpression() throws EndOfFileException,
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
        Object castExpression = castExpression();
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
    protected IASTExpression newExpression() throws BacktrackException,
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
                newPlacementExpressions.add(expression());
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
                typeId = typeId(true);
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
                            typeId = typeId(true);
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
                        typeId = typeId(true);
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
            typeId = typeId(true);
        }
        while (LT(1) == IToken.tLBRACKET) {
            // array new
            consume();

            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLBRACKET);
            }

            newTypeIdExpressions.add(assignmentExpression());
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
            newInitializerExpressions.add(expression());

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
    protected IASTExpression unaryExpression() throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        switch (LT(1)) {
        case IToken.tSTAR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_star );//IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION);
        case IToken.tAMPER:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_amper);//IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION);
        case IToken.tPLUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_plus );//IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION);
        case IToken.tMINUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_minus );//IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION);
        case IToken.tNOT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_not );//IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION);
        case IToken.tCOMPL:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_tilde);//IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION);
        case IToken.tINCR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixIncr);//IASTExpression.Kind.UNARY_INCREMENT);
        case IToken.tDECR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixDecr);//IASTExpression.Kind.UNARY_DECREMENT);
        case IToken.t_sizeof:
            consume(IToken.t_sizeof);
            IToken mark = LA(1);
            Object d = null;
            Object unaryExpression = null;
            if (LT(1) == IToken.tLPAREN) {
                try {
                    consume(IToken.tLPAREN);
                    d = typeId(false);
                    consume(IToken.tRPAREN);
                } catch (BacktrackException bt) {
                    backup(mark);
                    unaryExpression = unaryExpression();
                }
            } else {
                unaryExpression = unaryExpression();
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
            return newExpression();
        case IToken.t_delete:
            return deleteExpression();
        case IToken.tCOLONCOLON:
                switch (LT(2)) {
                case IToken.t_new:
                    return newExpression();
                case IToken.t_delete:
                    return deleteExpression();
                default:
                    return postfixExpression();
                }
        default:
            if (LT(1) == IGCCToken.t_typeof && supportTypeOfUnaries) {
                IASTExpression unary = unaryTypeofExpression();
                if (unary != null)
                    return unary;
            }
            if (LT(1) == IGCCToken.t___alignof__ && supportAlignOfUnaries) {
                IASTExpression align = unaryAlignofExpression();
                if (align != null)
                    return align;
            }
            return postfixExpression();
        }
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression postfixExpression() throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        IASTExpression firstExpression = null;
        boolean isTemplate = false;

        switch (LT(1)) {
        case IToken.t_typename:
            consume(IToken.t_typename);

            boolean templateTokenConsumed = false;
            if (LT(1) == IToken.t_template) {
                consume(IToken.t_template);
                templateTokenConsumed = true;
            }
            ITokenDuple nestedName = name();

            consume(IToken.tLPAREN);
            if (templateIdScopes.size() > 0) {
                templateIdScopes.push(IToken.tLPAREN);
            }
            Object expressionList = expression();
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
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_CHAR);
            break;
        case IToken.t_wchar_t:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_WCHART);
            break;
        case IToken.t_bool:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_BOOL);
            break;
        case IToken.t_short:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_SHORT);
            break;
        case IToken.t_int:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_INT);
            break;
        case IToken.t_long:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_LONG);
            break;
        case IToken.t_signed:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_SIGNED);
            break;
        case IToken.t_unsigned:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_UNSIGNED);
            break;
        case IToken.t_float:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_FLOAT);
            break;
        case IToken.t_double:
            firstExpression = simpleTypeConstructorExpression(null );//IASTExpression.Kind.POSTFIX_SIMPLETYPE_DOUBLE);
            break;
        case IToken.t_dynamic_cast:
            firstExpression = specialCastExpression(null );//IASTExpression.Kind.POSTFIX_DYNAMIC_CAST);
            break;
        case IToken.t_static_cast:
            firstExpression = specialCastExpression(null );//IASTExpression.Kind.POSTFIX_STATIC_CAST);
            break;
        case IToken.t_reinterpret_cast:
            firstExpression = specialCastExpression(null );//IASTExpression.Kind.POSTFIX_REINTERPRET_CAST);
            break;
        case IToken.t_const_cast:
            firstExpression = specialCastExpression(null );//IASTExpression.Kind.POSTFIX_CONST_CAST);
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
                typeId = typeId(false);
            } catch (BacktrackException b) {
                isTypeId = false;
                lhs = expression();
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
            firstExpression = primaryExpression();
        }
        IASTExpression secondExpression = null;
        for (;;) {
            switch (LT(1)) {
            case IToken.tLBRACKET:
                // array access
                consume(IToken.tLBRACKET);
                if (templateIdScopes.size() > 0) {
                    templateIdScopes.push(IToken.tLBRACKET);
                }
                secondExpression = expression();
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
                secondExpression = expression();
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

                if (LT(1) == IToken.t_template) {
                    consume(IToken.t_template);
                    isTemplate = true;
                }

                Object memberCompletionKind = null; /*(isTemplate ? IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS
                        : IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION); */

                secondExpression = primaryExpression();
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

                if (LT(1) == IToken.t_template) {
                    consume(IToken.t_template);
                    isTemplate = true;
                }

                Object arrowCompletionKind = /*(isTemplate ? IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP
                        : IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION); */ null;

                secondExpression = primaryExpression();
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

    protected IASTExpression simpleTypeConstructorExpression(Object type)
            throws EndOfFileException, BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;
        consume();
        consume(IToken.tLPAREN);
        IASTExpression inside = expression();
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
    protected IASTExpression primaryExpression() throws EndOfFileException,
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
            IASTExpression lhs = expression();
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
                duple = name();
            } catch (BacktrackException bt) {
                IToken mark = mark();
//                Declarator d = new Declarator(new DeclarationWrapper(scope,
//                        mark.getOffset(), mark.getLineNumber(), null, mark
//                                .getFilename()));

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
                        operatorId(null /*d*/, start, null);
                    else {
                        backup(mark);
                        throwBacktrack(startingOffset, end.getEndOffset(), end
                                .getLineNumber(), t.getFilename());
                    }
                } else if (LT(1) == IToken.t_operator)
                    operatorId(/*d*/null, null, null);

                duple = null; /*d.getNameDuple();*/
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
            throwBacktrack(startingOffset, startingOffset, line, fn);
            return null;
        }

    }

    protected IASTExpression specialCastExpression(Object kind) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;

        consume();
        consume(IToken.tLT);
        Object typeID = typeId(false);
        consume(IToken.tGT);
        consume(IToken.tLPAREN);
        Object lhs = expression();
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

    private final boolean allowCPPRestrict;


    private final boolean supportExtendedTemplateSyntax;
    private final boolean supportMinAndMaxOperators;

    /**
     * This is the standard cosntructor that we expect the Parser to be
     * instantiated with.
     * @param mode
     *            TODO
     *  
     */
    public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
            IProblemRequestor callback, IParserLogService log,
            ICPPParserExtensionConfiguration config) {
        super( scanner, log, mode, callback, config.supportStatementsInExpressions(),
                config.supportTypeofUnaryExpressions(), config.supportAlignOfUnaryExpression() );
        allowCPPRestrict = config.allowRestrictPointerOperators();
        supportExtendedTemplateSyntax = config
                .supportExtendedTemplateSyntax();
        supportMinAndMaxOperators = config.supportMinAndMaxOperators();
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
                duple = name();
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
            name = name();
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
                            typeId = typeId(false); // type-id
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
                        optionalTypeId = typeId(false);

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
                    ownerTemplate, false);
            // try it first with the original strategy
        } catch (BacktrackException bt) {
            if (simpleDeclarationMark == null)
                throwBacktrack(bt);
            firstFailure = bt.getProblem();
            // did not work
            backup(simpleDeclarationMark);

            try {
                return simpleDeclaration(
                        SimpleDeclarationStrategy.TRY_FUNCTION, ownerTemplate,
                        false);
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
                            SimpleDeclarationStrategy.TRY_VARIABLE, ownerTemplate,
                            false);
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

            ITokenDuple duple = name();
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
            Object ownerTemplate, boolean fromCatchHandler)
            throws BacktrackException, EndOfFileException {
        IToken firstToken = LA(1);
        int firstOffset = firstToken.getOffset();
        int firstLine = firstToken.getLineNumber();
        char[] fn = firstToken.getFilename();
        if (firstToken.getType() == IToken.tLBRACE)
            throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(),
                    firstToken.getLineNumber(), firstToken.getFilename());
        DeclarationWrapper sdw = new DeclarationWrapper(null, firstToken
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
                declarator = initDeclarator(sdw, strategy );

                while (LT(1) == IToken.tCOMMA) {
                    consume();
                    initDeclarator(sdw, strategy );
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
                handleFunctionBody( );
            
            if (hasFunctionTryBlock)
              catchHandlerSequence();

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

            ITokenDuple duple = name();

            consume(IToken.tLPAREN);
            Object expressionList = null;

            if (LT(1) != IToken.tRPAREN)
                expressionList = expression();

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
            initDeclarator(sdw, SimpleDeclarationStrategy.TRY_FUNCTION);

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
        if (LT(2) == IToken.tLPAREN
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
                ITokenDuple duple = name();
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

                ITokenDuple d = name();
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
                    Object expression = unaryTypeofExpression();
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

        ITokenDuple d = name();
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
            SimpleDeclarationStrategy strategy )
            throws EndOfFileException, BacktrackException {
        Declarator d = declarator(sdw, sdw.getScope(), strategy);

        try {
            //			astFactory.constructExpressions(constructInitializers);
              optionalCPPInitializer(d );
//            else if (language == ParserLanguage.C)
//                optionalCInitializer(d );
            sdw.addDeclarator(d);
            return d;
        } finally {
            //			astFactory.constructExpressions(true);
        }
    }

    protected void optionalCPPInitializer(Declarator d) throws EndOfFileException,
            BacktrackException {
        // handle initializer
        Object scope = d.getDeclarationWrapper().getScope();

        if (LT(1) == IToken.tASSIGN) {
            consume(IToken.tASSIGN);
            throwAwayMarksForInitializerClause(d);
            try {
                Object clause = initializerClause(scope);
                d.setInitializerClause(clause);
            } catch (EndOfFileException eof) {
                failParse();
                throw eof;
            }
        } else if (LT(1) == IToken.tLPAREN) {
            // initializer in constructor
            consume(IToken.tLPAREN); // EAT IT!
            Object astExpression = null;
            astExpression = expression();
            consume(IToken.tRPAREN);
            d.setConstructorExpression(astExpression);
        }
    }

    /**
     *  
     */
    protected Object initializerClause(Object scope ) throws EndOfFileException,
            BacktrackException {
        if (LT(1) == IToken.tLBRACE) {
            IToken t = consume(IToken.tLBRACE);
            IToken last = null;
            if (LT(1) == (IToken.tRBRACE)) {
                last = consume(IToken.tRBRACE);
                try {
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
                Object clause = initializerClause(scope );
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

        Object assignmentExpression = assignmentExpression();
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        try {
            return null;
        } catch (Exception e) {
            logException("initializerClause_3:createInitializerClause", e); //$NON-NLS-1$
        }
        throwBacktrack(startingOffset, endOffset, line, fn);
        return null;
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
                    if ( !LA(2).looksLikeExpression()
                            && strategy != SimpleDeclarationStrategy.TRY_VARIABLE) {
                        if (LT(2) == IToken.tIDENTIFIER) {
                            IToken newMark = mark();
                            consume(IToken.tLPAREN);
                            ITokenDuple queryName = null;
                            try {
                                try {
                                    queryName = name();
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
                    if ((!LA(2).looksLikeExpression()
                            && strategy != SimpleDeclarationStrategy.TRY_VARIABLE && !failed)
                            ) {
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
                                    exceptionTypeId = typeId(false);
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
                    consumeArrayModifiers(d);
                    continue;
                case IToken.tCOLON:
                    consume(IToken.tCOLON);
                    Object exp = constantExpression();
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
                    ITokenDuple duple = name();
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
                            end = consumeTemplateArguments(end, argumentList);
                            if (end != null && end.getType() == IToken.tGT)
                                hasTemplateId = true;
                        }

                        while (LT(1) == IToken.tCOLONCOLON
                                || LT(1) == IToken.tIDENTIFIER) {
                            end = consume();
                            if (end.getType() == IToken.tIDENTIFIER) {
                                end = consumeTemplateArguments(end,
                                        argumentList);
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
            duple = name();
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
                nameDuple = name();
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
     * Parses a statement.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTStatement statement() throws EndOfFileException,
            BacktrackException {

        switch (LT(1)) {
        case IToken.t_case:
            consume(IToken.t_case);
            constantExpression();
            cleanupLastToken();
            consume(IToken.tCOLON);
            statement();
            cleanupLastToken();
            return null;
        case IToken.t_default:
            consume(IToken.t_default);
            consume(IToken.tCOLON);
            statement();
            cleanupLastToken();
            return null;
        case IToken.tLBRACE:
            compoundStatement();
            cleanupLastToken();
            return null;
        case IToken.t_if:
            consume(IToken.t_if);
            consume(IToken.tLPAREN);
            condition();
            consume(IToken.tRPAREN);
            if (LT(1) != IToken.tLBRACE)
                singleStatementScope();
            else
                statement();
            if (LT(1) == IToken.t_else) {
                consume(IToken.t_else);
                if (LT(1) == IToken.t_if) {
                    //an else if, return and get the rest of the else if as
                    // the next statement instead of recursing
                    cleanupLastToken();
                    return null;
                } else if (LT(1) != IToken.tLBRACE)
                    singleStatementScope();
                else
                    statement();
            }
            cleanupLastToken();
            return null;
        case IToken.t_switch:
            consume();
            consume(IToken.tLPAREN);
            condition();
            consume(IToken.tRPAREN);
            statement();
            cleanupLastToken();
            return null;
        case IToken.t_while:
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            condition();
            consume(IToken.tRPAREN);
            if (LT(1) != IToken.tLBRACE)
                singleStatementScope();
            else
                statement();
            cleanupLastToken();
            return null;
        case IToken.t_do:
            consume(IToken.t_do);
            if (LT(1) != IToken.tLBRACE)
                singleStatementScope();
            else
                statement();
            consume(IToken.t_while);
            consume(IToken.tLPAREN);
            condition();
            consume(IToken.tRPAREN);
            cleanupLastToken();
            return null;
        case IToken.t_for:
            consume();
            consume(IToken.tLPAREN);
            forInitStatement();
            if (LT(1) != IToken.tSEMI)
                condition();
            consume(IToken.tSEMI);
            if (LT(1) != IToken.tRPAREN) {
                expression();
                cleanupLastToken();
            }
            consume(IToken.tRPAREN);
            statement();
            cleanupLastToken();
            return null;
        case IToken.t_break:
            consume();
            consume(IToken.tSEMI);
            cleanupLastToken();
            return null;
        case IToken.t_continue:
            consume();
            consume(IToken.tSEMI);
            cleanupLastToken();
            return null;
        case IToken.t_return:
            consume();
            if (LT(1) != IToken.tSEMI) {
                expression();
                cleanupLastToken();
            }
            consume(IToken.tSEMI);
            cleanupLastToken();
            return null;
        case IToken.t_goto:
            consume();
            consume(IToken.tIDENTIFIER);
            consume(IToken.tSEMI);
            cleanupLastToken();
            return null;
        case IToken.t_try:
            consume();
            compoundStatement();
            catchHandlerSequence();
            cleanupLastToken();
            return null;
        case IToken.tSEMI:
            consume();
            cleanupLastToken();
            return null;
        default:
            // can be many things:
            // label

            if (LT(1) == IToken.tIDENTIFIER
                    && LT(2) == IToken.tCOLON) {
                consume(IToken.tIDENTIFIER);
                consume(IToken.tCOLON);
                statement();
                cleanupLastToken();
                return null;
            }
            // expressionStatement
            // Note: the function style cast ambiguity is handled in
            // expression
            // Since it only happens when we are in a statement
            IToken mark = mark();
            Object expressionStatement = null;
            try {
                expressionStatement = expression();
                consume(IToken.tSEMI);
                cleanupLastToken();
                return null;
            } catch (BacktrackException b) {
                backup(mark);
                //					if (expressionStatement != null)
                //						expressionStatement.freeReferences();
            }

            // declarationStatement
            declaration(null, null);
            return null;
        }

    }

    protected void catchHandlerSequence()
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
                            null, true);
                consume(IToken.tRPAREN);

                catchBlockCompoundStatement();
            } catch (BacktrackException bte) {
                failParse(bte);
                failParseWithErrorHandling();
            }
        }
    }

    protected void catchBlockCompoundStatement()
            throws BacktrackException, EndOfFileException {
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
            compoundStatement();
    }

    /**
     * @throws BacktrackException
     */
    protected void forInitStatement() throws BacktrackException,
            EndOfFileException {
        IToken mark = mark();
        try {
            expression();
            consume(IToken.tSEMI);
            //			e.acceptElement(requestor);

        } catch (BacktrackException bt) {
            backup(mark);
            try {
                simpleDeclarationStrategyUnion(null, null);
            } catch (BacktrackException b) {
                failParse(b);
                throwBacktrack(b);
            }
        }

    }

    
    /**
     * This is the top-level entry point into the ANSI C++ grammar.
     * 
     * translationUnit : (declaration)*
     */
    protected void translationUnit() {
        try {
            translationUnit = null; /* astFactory.createCompilationUnit(); */
        } catch (Exception e2) {
            logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
            return;
        }

        //		compilationUnit.enterScope( requestor );

        while (true) {
            try {
                int checkOffset = LA(1).hashCode();
                declaration(translationUnit, null);
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

    protected IToken consumeArrayModifiers(IDeclarator d) throws EndOfFileException, BacktrackException {
        int startingOffset = LA(1).getOffset();
        IToken last = null;
        while (LT(1) == IToken.tLBRACKET) {
            consume(IToken.tLBRACKET); // eat the '['
    
            Object exp = null;
            if (LT(1) != IToken.tRBRACKET) {
                exp = constantExpression();
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#getTranslationUnit()
     */
    protected IASTTranslationUnit getTranslationUnit() {
        return (IASTTranslationUnit) translationUnit;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createCompoundStatement()
     */
    protected IASTCompoundStatement createCompoundStatement() {
        return new CASTCompoundStatement();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createBinaryExpression()
     */
    protected IASTBinaryExpression createBinaryExpression()  {
        return new CASTBinaryExpression();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createConditionalExpression()
     */
    protected IASTConditionalExpression createConditionalExpression() {
        return new CASTConditionalExpression();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createUnaryExpression()
     */
    protected IASTUnaryExpression createUnaryExpression() {
        return new CASTUnaryExpression();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createCompoundStatementExpression()
     */
    protected IGCCASTCompoundStatementExpression createCompoundStatementExpression() {
        return new CASTCompoundStatementExpression();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createExpressionList()
     */
    protected IASTExpressionList createExpressionList() {
        return new CASTExpressionList();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createName(org.eclipse.cdt.core.parser.IToken)
     */
    protected IASTName createName(IToken token) {
        return new CASTName( token.getCharImage() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createName()
     */
    protected IASTName createName() {
        return new CASTName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createEnumerator()
     */
    protected IASTEnumerator createEnumerator() {
        return new CASTEnumerator();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createEnumerationSpecifier()
     */
    protected IASTEnumerationSpecifier createEnumerationSpecifier() {
        return new CASTEnumerationSpecifier();
    }

}