/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.internal.core.parser2.c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.SimpleDeclarationStrategy;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.parser2.DeclarationWrapper;
import org.eclipse.cdt.internal.core.parser2.Declarator;
import org.eclipse.cdt.internal.core.parser2.IDeclarator;
import org.eclipse.cdt.internal.core.parser2.IDeclaratorOwner;
import org.eclipse.cdt.internal.core.parser2.IParameterCollection;
import org.eclipse.cdt.internal.core.parser2.TypeId;
import org.eclipse.cdt.internal.core.parser2.cpp.IProblemRequestor;

/**
 * @author jcamelon
 */
public class GNUCSourceParser extends AbstractGNUSourceCodeParser {

    private final boolean supportGCCStyleDesignators;

    /**
     * @param scanner
     * @param logService
     * @param parserMode
     * @param callback
     */
    public GNUCSourceParser(IScanner scanner, ParserMode parserMode,
            IProblemRequestor callback, IParserLogService logService,
            ICParserExtensionConfiguration config) {
        super(scanner, logService, parserMode, callback, config
                .supportStatementsInExpressions(), config
                .supportTypeofUnaryExpressions(), config
                .supportAlignOfUnaryExpression());
        supportGCCStyleDesignators = config.supportGCCStyleDesignators();
    }

    protected void optionalCInitializer(Declarator d)
            throws EndOfFileException, BacktrackException {
        final Object scope = d.getDeclarationWrapper().getScope();
        if (LT(1) == IToken.tASSIGN) {
            consume(IToken.tASSIGN);
            throwAwayMarksForInitializerClause(d);
            d.setInitializerClause(cInitializerClause(scope,
                    Collections.EMPTY_LIST));
        }
    }

    /**
     * @param scope
     * @return
     */
    protected Object cInitializerClause(Object scope, List designators)
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
                Object initializer = cInitializerClause(scope, newDesignators);
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
            return null;
        }
        // if we get this far, it means that we have not yet succeeded
        // try this now instead
        // assignmentExpression
        try {
            Object assignmentExpression = assignmentExpression(scope);
            try {
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

    protected List designatorList(Object scope) throws EndOfFileException,
            BacktrackException {
        List designatorList = Collections.EMPTY_LIST;
        // designated initializers for C

        if (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {

            while (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
                IToken id = null;
                Object constantExpression = null;
                /* IASTDesignator.DesignatorKind */Object kind = null;

                if (LT(1) == IToken.tDOT) {
                    consume(IToken.tDOT);
                    id = identifier();
                    //        kind = IASTDesignator.DesignatorKind.FIELD;
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
                                extensionParms.put(null, //IASTGCCDesignator.SECOND_EXRESSION,
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
                    //        kind = IASTDesignator.DesignatorKind.SUBSCRIPT;
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

    protected void declaration(Object scope) throws EndOfFileException,
            BacktrackException {
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
        default:
            simpleDeclaration(scope);
        }

        cleanupLastToken();
    }

    /**
     * @param scope
     * @throws BacktrackException
     * @throws EndOfFileException
     */
    protected Object simpleDeclaration(Object scope) throws BacktrackException,
            EndOfFileException {
        IToken firstToken = LA(1);
        int firstOffset = firstToken.getOffset();
        int firstLine = firstToken.getLineNumber();
        char[] fn = firstToken.getFilename();
        if (firstToken.getType() == IToken.tLBRACE)
            throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(),
                    firstToken.getLineNumber(), firstToken.getFilename());
        DeclarationWrapper sdw = new DeclarationWrapper(scope, firstToken
                .getOffset(), firstToken.getLineNumber(), null, fn);
        firstToken = null; // necessary for scalability

        declSpecifierSeq(sdw, false);
        Object simpleTypeSpecifier = null;
        if (sdw.getTypeSpecifier() == null && sdw.getSimpleType() != null) //IASTSimpleTypeSpecifier.Type.UNSPECIFIED)
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
            declarator = initDeclarator(sdw);

            while (LT(1) == IToken.tCOMMA) {
                consume();
                initDeclarator(sdw);
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
        case IToken.tLBRACE:
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

        if (hasFunctionBody)
            handleFunctionBody(null /* Should be function object scope */);
        //            try {
        //                l = sdw.createASTNodes();
        //// } catch (ASTSemanticException e) {
        //// if (e.getProblem() == null) {
        //// IProblem p = problemFactory.createProblem(
        //// IProblem.SYNTAX_ERROR, sdw.startingOffset,
        //// lastToken != null ? lastToken.getEndOffset() : 0,
        //// sdw.startingLine, fn, EMPTY_STRING, false, true);
        //// throwBacktrack(p);
        //// } else {
        //// throwBacktrack(e.getProblem());
        //// }
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
        //// ((IASTOffsetableElement) declaration)
        //// .setEndingOffsetAndLineNumber(lastToken
        //// .getEndOffset(), lastToken
        //// .getLineNumber());
        //                        // declaration.acceptElement( requestor );
        //                    }
        //                    return declaration;
        //                }
        //                Object declaration = l.get(0);
        //                cleanupLastToken();
        //                // declaration.enterScope( requestor );
        //                try {
        //                    // if ( !( declaration instanceof IASTScope ) )
        //                    // throwBacktrack(firstOffset, endOffset, firstLine, fn);
        //
        //                    handleFunctionBody(declaration);
        //
        //// ((IASTOffsetableElement) declaration)
        //// .setEndingOffsetAndLineNumber(lastToken
        //// .getEndOffset(), lastToken
        //// .getLineNumber());
        //                } finally {
        //                    // declaration.exitScope( requestor );
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
        //                    // declaration.acceptElement(requestor);
        //                    return declaration;
        //                }
        //            } catch (Exception e1) {
        //                logException("simpleDeclaration:createTypeSpecDeclaration", e1);
        // //$NON-NLS-1$
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
                declaration(compilationUnit);
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
     * @param expression
     * @throws BacktrackException
     */
    protected Object assignmentExpression(Object scope)
            throws EndOfFileException, BacktrackException {
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
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_NORMAL,
                    conditionalExpression);
        case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_MULT,
                    conditionalExpression);
        case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_DIV,
                    conditionalExpression);
        case IToken.tMODASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_MOD,
                    conditionalExpression);
        case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_PLUS,
                    conditionalExpression);
        case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_MINUS,
                    conditionalExpression);
        case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_RSHIFT,
                    conditionalExpression);
        case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_LSHIFT,
                    conditionalExpression);
        case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_AND,
                    conditionalExpression);
        case IToken.tXORASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_XOR,
                    conditionalExpression);
        case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(scope, null, //IASTExpression.Kind.ASSIGNMENTEXPRESSION_OR,
                    conditionalExpression);
        }
        return conditionalExpression;
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
        Object firstExpression = castExpression(scope);
        for (;;) {
            switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
                IToken t = consume();
                Object secondExpression = castExpression(scope);
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
            }
        }
        return unaryExpression(scope);
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected Object unaryExpression(Object scope) throws EndOfFileException,
            BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        switch (LT(1)) {
        case IToken.tSTAR:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION);
        case IToken.tAMPER:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION);
        case IToken.tPLUS:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION);
        case IToken.tMINUS:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION);
        case IToken.tNOT:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION);
        case IToken.tCOMPL:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION);
        case IToken.tINCR:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_INCREMENT);
        case IToken.tDECR:
            consume();
            return unaryOperatorCastExpression(scope, null);//IASTExpression.Kind.UNARY_DECREMENT);
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
        switch (LT(1)) {
        case IToken.tLPAREN:
            // ( type-name ) { initializer-list }
            // ( type-name ) { initializer-list , }
            consume(IToken.tLPAREN);
            /* Object typeId = */typeId(scope, false);
            /* Object initializerClause = */cInitializerClause(scope,
                    Collections.EMPTY_LIST);
            firstExpression = null; //createExpressionHere
        default:
            firstExpression = primaryExpression(scope);
        }

        Object secondExpression = null;
        for (;;) {
            switch (LT(1)) {
            case IToken.tLBRACKET:
                // array access
                consume(IToken.tLBRACKET);
                secondExpression = expression(scope);
                int endOffset = consume(IToken.tRBRACKET).getEndOffset();
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

                secondExpression = expression(scope);
                endOffset = consume(IToken.tRPAREN).getEndOffset();
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

                Object memberCompletionKind = null; /*
                                                     * (isTemplate ?
                                                     * IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS :
                                                     * IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION);
                                                     */

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

                Object arrowCompletionKind = /*
                                              * (isTemplate ?
                                              * IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP :
                                              * IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION);
                                              */null;

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
            Object lhs = expression(scope);
            int endOffset = consume(IToken.tRPAREN).getEndOffset();
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
            ITokenDuple duple = null;
            int startingOffset = LA(1).getOffset();
            int line = LA(1).getLineNumber();
            IToken t1 = identifier();
            duple = TokenFactory.createTokenDuple(t1, t1);

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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.GNUBaseParser#statement(java.lang.Object)
     */
    protected void statement(Object scope) throws EndOfFileException,
            BacktrackException {

        switch (LT(1)) {
        // labeled statements
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
        // compound statement
        case IToken.tLBRACE:
            compoundStatement(scope, true);
            cleanupLastToken();
            return;
        // selection statement
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
        //iteration statements
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
        
        //jump statement
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
        case IToken.tSEMI:
            consume();
            cleanupLastToken();
            return;
        default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
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
            declaration(scope);
        }

    }

    protected Object typeId(Object scope, boolean skipArrayModifiers)
            throws EndOfFileException, BacktrackException {
        IToken mark = mark();
        IToken name = null;
        boolean isConst = false, isVolatile = false;
        boolean isSigned = false, isUnsigned = false;
        boolean isShort = false, isLong = false;
        boolean isTypename = false;

        boolean encountered = false;
        Object kind = null;
        do {
            try {
                name = identifier();
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
                    name = identifier();
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

            if (LT(1) == IToken.t_struct || LT(1) == IToken.t_enum
                    || LT(1) == IToken.t_union) {
                consume();
                try {
                    name = identifier();
                    kind = null; //IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME;
                    encountered = true;
                } catch (BacktrackException b) {
                    backup(mark);
                    throwBacktrack(b);
                }
            }

        } while (false);

        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        if (!encountered)
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
            IToken mark = mark();

            ITokenDuple nameDuple = null;
            if (LT(1) == IToken.tIDENTIFIER) {
                IToken t = identifier();
                nameDuple = TokenFactory.createTokenDuple(t, t);
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
                    d
                            .addPointerOperator(null /* ASTPointerOperator.POINTER */);
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
            declarator
                    .addPointerOperator(null /* ASTPointerOperator.CONST_POINTER */);
            break;
        case IToken.t_volatile:
            result = consume(IToken.t_volatile);
            declarator
                    .addPointerOperator(null/* ASTPointerOperator.VOLATILE_POINTER */);
            break;
        case IToken.t_restrict:
            result = consume(IToken.t_restrict);
            declarator
                    .addPointerOperator(null/* ASTPointerOperator.RESTRICT_POINTER */);
            break;
        }
        return result;
    }

    protected void declSpecifierSeq(DeclarationWrapper sdw, boolean parm)
            throws BacktrackException, EndOfFileException {
        Flags flags = new Flags(parm);
        IToken typeNameBegin = null;
        IToken typeNameEnd = null;
        declSpecifiers: for (;;) {
            switch (LT(1)) {
            //Storage Class Specifiers
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
            case IToken.t_typedef:
                sdw.setTypedef(true);
                consume();
                break;

            //Function Specifier
            case IToken.t_inline:
                consume();
                sdw.setInline(true);
                break;

            //Type Qualifiers
            case IToken.t_const:
                sdw.setConst(true);
                consume();
                break;
            case IToken.t_volatile:
                sdw.setVolatile(true);
                consume();
                break;
            case IToken.t_restrict:
                sdw.setRestrict(true);
                consume();
                break;

            //Type Specifiers
            case IToken.t_void:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.VOID);
                break;
            case IToken.t_char:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.CHAR);
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
            case IToken.t_int:
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
            case IToken.t__Bool:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type._BOOL);
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
                if (lookAheadForDeclarator(flags)) {
                    setTypeName(sdw, typeNameBegin, typeNameEnd);
                    return;
                }

                IToken i = identifier();
                ITokenDuple d = TokenFactory.createTokenDuple(i, i);
                sdw.setTypeName(d);
                sdw.setSimpleType(null);//IASTSimpleTypeSpecifier.Type.CLASS_OR_TYPENAME);
                flags.setEncounteredTypename(true);
                break;
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
        if (LT(1) == IToken.tIDENTIFIER) {
            IToken i = identifier();
            duple = TokenFactory.createTokenDuple(i, i);
        }

        if (LT(1) != IToken.tLBRACE) {
            IToken errorPoint = LA(1);
            backup(mark);
            throwBacktrack(errorPoint.getOffset(), errorPoint.getEndOffset(),
                    errorPoint.getLineNumber(), errorPoint.getFilename());
        }
        Object astClassSpecifier = null;

        try {
            astClassSpecifier = null; /*
                                       * astFactory.createClassSpecifier(sdw.getScope(),
                                       * duple, classKind, nameType, access,
                                       * classKey.getOffset(),
                                       * classKey.getLineNumber(), duple == null ?
                                       * classKey .getOffset() :
                                       * duple.getFirstToken().getOffset(),
                                       * duple == null ? classKey.getEndOffset() :
                                       * duple .getFirstToken().getEndOffset(),
                                       * duple == null ?
                                       * classKey.getLineNumber() :
                                       * duple.getFirstToken().getLineNumber(),
                                       * classKey.getFilename()); } catch
                                       * (ASTSemanticException e) {
                                       * throwBacktrack(e.getProblem());
                                       */
        } catch (Exception e) {
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            logException("classSpecifier:createClassSpecifier", e); //$NON-NLS-1$
            throwBacktrack(mark.getOffset(), endOffset, mark.getLineNumber(),
                    mark.getFilename());
        }
        sdw.setTypeSpecifier(astClassSpecifier);
        // base clause

        if (LT(1) == IToken.tLBRACE) {
            consume(IToken.tLBRACE);
            //			astClassSpecifier.enterScope(requestor);

            try {
                cleanupLastToken();
                memberDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
                    int checkToken = LA(1).hashCode();
                    switch (LT(1)) {
                    case IToken.tRBRACE:
                        consume(IToken.tRBRACE);
                        break memberDeclarationLoop;
                    default:
                        try {
                            declaration(astClassSpecifier);
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
                //					logException("classSpecifier:signalEndOfClassSpecifier", e1);
                // //$NON-NLS-1$
                //					throwBacktrack(lt.getOffset(), lt.getEndOffset(),
                // lt.getLineNumber(), lt.getFilename());
                //				}

            } finally {
                //				astClassSpecifier.exitScope(requestor);
            }

        }
    }

    protected void elaboratedTypeSpecifier(DeclarationWrapper sdw)
            throws BacktrackException, EndOfFileException {
        // this is an elaborated class specifier
        IToken t = consume();
        Object eck = null;

        switch (t.getType()) {
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

        IToken identifier = identifier();
        ITokenDuple d = TokenFactory.createTokenDuple(identifier, identifier);
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

    protected Declarator initDeclarator(DeclarationWrapper sdw)
            throws EndOfFileException, BacktrackException {
        Declarator d = declarator(sdw);

        try {
            //			astFactory.constructExpressions(constructInitializers);
            optionalCInitializer(d);
            sdw.addDeclarator(d);
            return d;
        } finally {
            //			astFactory.constructExpressions(true);
        }
    }

    protected Declarator declarator(IDeclaratorOwner owner)
            throws EndOfFileException, BacktrackException {
        Declarator d = null;
        DeclarationWrapper sdw = owner.getDeclarationWrapper();
        Object scope = sdw.getScope();
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
                declarator(d);
                consume(IToken.tRPAREN);
            } else if( LT(1) == IToken.tIDENTIFIER ){
                identifier();
            }

            for (;;) {
                switch (LT(1)) {
                case IToken.tLPAREN:

                    boolean failed = false;
                    Object parameterScope = null; /*
                                                   * astFactory
                                                   * .getDeclaratorScope(scope,
                                                   * d.getNameDuple());
                                                   */
                    // temporary fix for initializer/function declaration
                    // ambiguity
                    if (!LA(2).looksLikeExpression()) {
                        if (LT(2) == IToken.tIDENTIFIER) {
                            IToken newMark = mark();
                            consume(IToken.tLPAREN);
                            ITokenDuple queryName = null;
                            try {
                                try {
                                    IToken i = identifier();
                                    queryName = TokenFactory.createTokenDuple(
                                            i, i);
                                    // look it up
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
                    if ((!LA(2).looksLikeExpression() && !failed)) {
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

    protected IToken consumeArrayModifiers(IDeclarator d, Object scope)
            throws EndOfFileException, BacktrackException {
        int startingOffset = LA(1).getOffset();
        IToken last = null;
        while (LT(1) == IToken.tLBRACKET) {
            consume(IToken.tLBRACKET); // eat the '['

            boolean encounteredModifier = false;
            if (d instanceof Declarator) {
                outerLoop: do {
                    switch (LT(1)) {
                    case IToken.t_static:
                    case IToken.t_const:
                    case IToken.t_volatile:
                    case IToken.t_restrict:
                        //TODO should store these somewhere
                        consume();
                        encounteredModifier = true;
                        continue;
                    default:
                        break outerLoop;
                    }
                } while (true);
            }
            Object exp = null;

            if (LT(1) != IToken.tRBRACKET) {
                if (encounteredModifier)
                    exp = assignmentExpression(scope);
                else
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

    protected void parameterDeclaration(IParameterCollection collection,
            Object scope) throws BacktrackException, EndOfFileException {
        IToken current = LA(1);

        DeclarationWrapper sdw = new DeclarationWrapper(scope, current
                .getOffset(), current.getLineNumber(), null, current
                .getFilename());
        declSpecifierSeq(sdw, true);
        if (sdw.getTypeSpecifier() == null && sdw.getSimpleType() != null)//IASTSimpleTypeSpecifier.Type.UNSPECIFIED)
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
            initDeclarator(sdw);

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
                simpleDeclaration(scope);
            } catch (BacktrackException b) {
                failParse(b);
                throwBacktrack(b);
            }
        }

    }

}