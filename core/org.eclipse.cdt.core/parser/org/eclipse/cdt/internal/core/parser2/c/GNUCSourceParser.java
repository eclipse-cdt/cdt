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

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.parser2.Declarator;
import org.eclipse.cdt.internal.core.parser2.IDeclarator;
import org.eclipse.cdt.internal.core.parser2.cpp.IProblemRequestor;

/**
 * @author jcamelon
 */
public class GNUCSourceParser extends AbstractGNUSourceCodeParser {

    private final boolean supportGCCStyleDesignators;

    private static final int DEFAULT_DECLARATOR_LIST_SIZE = 4;

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

    /**
     * @param d
     */
    protected void throwAwayMarksForInitializerClause() {
        simpleDeclarationMark = null;
    }

    protected IASTInitializer optionalCInitializer() throws EndOfFileException,
            BacktrackException {
        if (LT(1) == IToken.tASSIGN) {
            consume(IToken.tASSIGN);
            throwAwayMarksForInitializerClause();
            return cInitializerClause(Collections.EMPTY_LIST);
        }
        return null;
    }

    /**
     * @param scope
     * @return
     */
    protected IASTInitializer cInitializerClause(List designators)
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
                List newDesignators = designatorList();
                if (newDesignators.size() != 0)
                    if (LT(1) == IToken.tASSIGN)
                        consume(IToken.tASSIGN);
                IASTInitializer initializer = cInitializerClause(newDesignators);
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
            IASTExpression assignmentExpression = assignmentExpression();
            IASTInitializerExpression result = createInitializerExpression();
            result.setExpression( assignmentExpression );
            result.setOffset( assignmentExpression.getOffset() );
            assignmentExpression.setParent( result );
            assignmentExpression.setPropertyInParent( IASTInitializerExpression.INITIALIZER_EXPRESSION );
            return result;
        } catch (BacktrackException b) {
            // do nothing
        }
        int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
        throwBacktrack(startingOffset, endOffset, line, fn);
        return null;
    }

    /**
     * @return
     */
    protected IASTInitializerExpression createInitializerExpression() {
        return new CASTInitializerExpresion();
    }

    protected List designatorList() throws EndOfFileException,
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
                    constantExpression = expression();
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
                                Object constantExpression1 = expression();
                                consume(IToken.tELLIPSIS);
                                Object constantExpression2 = expression();
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
                    Object constantExpression1 = expression();
                    consume(IToken.tELLIPSIS);
                    Object constantExpression2 = expression();
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

    protected IASTDeclaration declaration() throws EndOfFileException,
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
            cleanupLastToken();
            return null;
        default:
            IASTDeclaration d = simpleDeclaration();
            cleanupLastToken();
            return d;
        }

    }

    /**
     * @throws BacktrackException
     * @throws EndOfFileException
     */
    protected IASTDeclaration simpleDeclaration() throws BacktrackException,
            EndOfFileException {
        IToken firstToken = LA(1);
        int firstOffset = firstToken.getOffset();
        int firstLine = firstToken.getLineNumber();
        char[] fn = firstToken.getFilename();
        if (firstToken.getType() == IToken.tLBRACE)
            throwBacktrack(firstToken.getOffset(), firstToken.getEndOffset(),
                    firstToken.getLineNumber(), firstToken.getFilename());

        firstToken = null; // necessary for scalability

        IASTDeclSpecifier declSpec = declSpecifierSeq(false);

        List declarators = Collections.EMPTY_LIST;
        if (LT(1) != IToken.tSEMI) {
            declarators = new ArrayList(DEFAULT_DECLARATOR_LIST_SIZE);
            declarators.add(initDeclarator());

            while (LT(1) == IToken.tCOMMA) {
                consume(IToken.tCOMMA);
                declarators.add(initDeclarator());
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
                hasFunctionBody = true;
            }

            if (hasFunctionTryBlock && !hasFunctionBody)
                throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1)
                        .getLineNumber(), fn);
        }

        if (hasFunctionBody) {
            if (declarators.size() != 1)
                throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1)
                        .getLineNumber(), fn);

            IASTDeclarator declarator = (IASTDeclarator) declarators.get(0);
            if (!(declarator instanceof IASTFunctionDeclarator))
                throwBacktrack(firstOffset, LA(1).getEndOffset(), LA(1)
                        .getLineNumber(), fn);

            IASTFunctionDefinition funcDefinition = createFunctionDefinition();
            funcDefinition.setOffset(firstOffset);
            funcDefinition.setDeclSpecifier(declSpec);
            declSpec.setParent(funcDefinition);
            declSpec.setPropertyInParent(IASTFunctionDefinition.DECL_SPECIFIER);

            funcDefinition.setDeclarator((IASTFunctionDeclarator) declarator);
            declarator.setParent(funcDefinition);
            declarator.setPropertyInParent(IASTFunctionDefinition.DECLARATOR);

            IASTStatement s = handleFunctionBody();
            if( s != null )
            {
                funcDefinition.setBody( s );
                s.setParent( funcDefinition );
                s.setPropertyInParent( IASTFunctionDefinition.FUNCTION_BODY );
            }
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            return funcDefinition;
        }

        IASTSimpleDeclaration simpleDeclaration = createSimpleDeclaration();
        simpleDeclaration.setOffset(firstOffset);
        simpleDeclaration.setDeclSpecifier(declSpec);
        declSpec.setParent(simpleDeclaration);
        declSpec.setPropertyInParent(IASTSimpleDeclaration.DECL_SPECIFIER);

        for (int i = 0; i < declarators.size(); ++i) {
            IASTDeclarator declarator = (IASTDeclarator) declarators.get(i);
            simpleDeclaration.addDeclarator(declarator);
            declarator.setParent(simpleDeclaration);
            declarator.setPropertyInParent(IASTSimpleDeclaration.DECLARATOR);
        }
        return simpleDeclaration;
    }

    /**
     * @return
     */
    protected IASTFunctionDefinition createFunctionDefinition() {
        return new CASTFunctionDefinition();
    }

    /**
     * @return
     */
    private CASTSimpleDeclaration createSimpleDeclaration() {
        return new CASTSimpleDeclaration();
    }

    protected CASTTranslationUnit translationUnit;

    private static final int DEFAULT_POINTEROPS_LIST_SIZE = 4;

    private static final int DEFAULT_PARAMETERS_LIST_SIZE = 4;

    protected CASTTranslationUnit createTranslationUnit() {
        CASTTranslationUnit t = new CASTTranslationUnit();
        t.setOffset(0);
        t.setParent(null);
        t.setPropertyInParent(null);
        return t;
    }

    /**
     * This is the top-level entry point into the ANSI C++ grammar.
     * 
     * translationUnit : (declaration)*
     */
    protected void translationUnit() {
        try {
            translationUnit = createTranslationUnit();
        } catch (Exception e2) {
            logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
            return;
        }

        int lastBacktrack = -1;
        while (true) {
            try {
                int checkOffset = LA(1).hashCode();
                IASTDeclaration d = declaration();
                d.setParent(translationUnit);
                d.setPropertyInParent(IASTTranslationUnit.OWNED_DECLARATION);
                translationUnit.addDeclaration(d);
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
                    if (lastBacktrack != -1
                            && lastBacktrack == LA(1).hashCode()) {
                        // we haven't progressed from the
                        // last backtrack
                        // try and find tne next definition
                        failParseWithErrorHandling();
                    } else {
                        // start again from here
                        lastBacktrack = LA(1).hashCode();
                    }
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
    protected IASTExpression assignmentExpression()
            throws EndOfFileException, BacktrackException {
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
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tMODASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tXORASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(null, conditionalExpression);
        }
        return conditionalExpression;
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
        IASTExpression firstExpression = castExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
                IToken t = consume();
            	IASTExpression secondExpression = castExpression();
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
            }
        }
        return unaryExpression();
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
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_STAR_CASTEXPRESSION);
        case IToken.tAMPER:
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_AMPSND_CASTEXPRESSION);
        case IToken.tPLUS:
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_PLUS_CASTEXPRESSION);
        case IToken.tMINUS:
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_MINUS_CASTEXPRESSION);
        case IToken.tNOT:
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_NOT_CASTEXPRESSION);
        case IToken.tCOMPL:
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_TILDE_CASTEXPRESSION);
        case IToken.tINCR:
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_INCREMENT);
        case IToken.tDECR:
            consume();
            return unaryOperatorCastExpression(null);//IASTExpression.Kind.UNARY_DECREMENT);
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
        switch (LT(1)) {
        case IToken.tLPAREN:
            // ( type-name ) { initializer-list }
            // ( type-name ) { initializer-list , }
            consume(IToken.tLPAREN);
            /* Object typeId = */typeId(false);
            /* Object initializerClause = */cInitializerClause(Collections.EMPTY_LIST);
            firstExpression = null; //createExpressionHere
        default:
            firstExpression = primaryExpression();
        }

        Object secondExpression = null;
        for (;;) {
            switch (LT(1)) {
            case IToken.tLBRACKET:
                // array access
                consume(IToken.tLBRACKET);
                secondExpression = expression();
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

                secondExpression = expression();
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

                Object arrowCompletionKind = /*
                                              * (isTemplate ?
                                              * IASTExpression.Kind.POSTFIX_ARROW_TEMPL_IDEXP :
                                              * IASTExpression.Kind.POSTFIX_ARROW_IDEXPRESSION);
                                              */null;

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
            Object lhs = expression();
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
            
            int startingOffset = LA(1).getOffset();
            int line = LA(1).getLineNumber();
            IToken t1 = identifier();

            endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            
            IASTIdExpression result = createIdExpression();
            IASTName name = createName( t1 );
            result.setName( name );
            name.setParent( result );
            name.setPropertyInParent( IASTIdExpression.ID_NAME );
            return result; 
        default:
            IToken la = LA(1);
            startingOffset = la.getOffset();
            line = la.getLineNumber();
            char[] fn = la.getFilename();

            IASTExpression empty = null;
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
     * @return
     */
    protected IASTIdExpression createIdExpression() {
        return new CASTIdExpression();
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
        // compound statement
        case IToken.tLBRACE:
            compoundStatement();
            cleanupLastToken();
            return null;
        // selection statement
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
        //iteration statements
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

        //jump statement
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
        case IToken.tSEMI:
            consume();
            cleanupLastToken();
            return null;
        default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
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
            IASTDeclaration d = declaration();
            IASTDeclarationStatement ds = createDeclarationStatement();
            ds.setDeclaration(d);
            return ds;
        }

    }

    /**
     * @return
     */
    protected IASTDeclarationStatement createDeclarationStatement() {
        return new CASTDeclarationStatement();
    }

    protected Object typeId(boolean skipArrayModifiers)
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

        //        TypeId id = getTypeIdInstance(scope);
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
    protected IToken consumePointerOperators(List pointerOps)
            throws EndOfFileException, BacktrackException {
        IToken result = null;
        for (;;) {
            IToken mark = mark();

            ITokenDuple nameDuple = null;
            boolean isConst = false, isVolatile = false, isRestrict = false;
            if (LT(1) == IToken.tIDENTIFIER) {
                IToken t = identifier();
                nameDuple = TokenFactory.createTokenDuple(t, t);
            }

            if (LT(1) == IToken.tSTAR) {

                result = consume(IToken.tSTAR);
                int startOffset = result.getOffset();

                for (;;) {
                    IToken t = LA(1);
                    switch (LT(1)) {
                    case IToken.t_const:
                        result = consume(IToken.t_const);
                        isConst = true;
                        break;
                    case IToken.t_volatile:
                        result = consume(IToken.t_volatile);
                        isVolatile = true;
                        break;
                    case IToken.t_restrict:
                        result = consume(IToken.t_restrict);
                        isRestrict = true;
                        break;
                    }

                    if (t == LA(1))
                        break;
                }

                IASTPointerOperator po = null;
                if (nameDuple != null) {

                    nameDuple.freeReferences();
                } else {
                    po = createPointer();
                    ((ICASTPointer) po).setConst(isConst);
                    ((ICASTPointer) po).setVolatile(isVolatile);
                    ((ICASTPointer) po).setRestrict(isRestrict);
                }
                if (po != null) {
                    pointerOps.add(po);
                }
            }
            backup(mark);
            return result;
        }
    }

    /**
     * @return
     */
    private ICASTPointer createPointer() {
        return new CASTPointer();
    }

    protected IASTDeclSpecifier declSpecifierSeq(boolean parm)
            throws BacktrackException, EndOfFileException {
        Flags flags = new Flags(parm);
        IToken typeNameBegin = null;
        IToken typeNameEnd = null;

        int startingOffset = LA(1).getOffset();
        int storageClass = IASTDeclSpecifier.sc_unspecified;
        boolean isInline = false;
        boolean isConst = false, isRestrict = false, isVolatile = false;
        boolean isShort = false, isLong = false, isUnsigned = false, isIdentifier = false, isSigned = false;
        int simpleType = IASTSimpleDeclSpecifier.t_unspecified;

        declSpecifiers: for (;;) {
            switch (LT(1)) {
            //Storage Class Specifiers
            case IToken.t_auto:
                consume();
                storageClass = IASTDeclSpecifier.sc_auto;
                break;
            case IToken.t_register:
                storageClass = IASTDeclSpecifier.sc_register;
                consume();
                break;
            case IToken.t_static:
                storageClass = IASTDeclSpecifier.sc_static;
                consume();
                break;
            case IToken.t_extern:
                storageClass = IASTDeclSpecifier.sc_extern;
                consume();
                break;
            case IToken.t_typedef:
                storageClass = IASTDeclSpecifier.sc_typedef;
                consume();
                break;

            //Function Specifier
            case IToken.t_inline:
                isInline = true;
                consume();
                break;

            //Type Qualifiers
            case IToken.t_const:
                isConst = true;
                consume();
                break;
            case IToken.t_volatile:
                isVolatile = true;
                consume();
                break;
            case IToken.t_restrict:
                isRestrict = true;
                consume();
                break;

            //Type Specifiers
            case IToken.t_void:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_void;
                break;
            case IToken.t_char:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_char;
                break;
            case IToken.t_short:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                isShort = true;
                break;
            case IToken.t_int:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_int;
                break;
            case IToken.t_long:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                isLong = true;
                break;
            case IToken.t_float:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_float;
                break;
            case IToken.t_double:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_double;
                break;
            case IToken.t_signed:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                isSigned = true;
                break;
            case IToken.t_unsigned:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                isUnsigned = true;
                break;
            case IToken.t__Bool:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                flags.setEncounteredRawType(true);
                consume();
                simpleType = ICASTSimpleDeclSpecifier.t_Bool;
                break;
            case IToken.t__Complex:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                consume(IToken.t__Complex);
                simpleType = ICASTSimpleDeclSpecifier.t_Complex;
                break;
            case IToken.t__Imaginary:
                if (typeNameBegin == null)
                    typeNameBegin = LA(1);
                typeNameEnd = LA(1);
                consume(IToken.t__Imaginary);
                simpleType = ICASTSimpleDeclSpecifier.t_Imaginary;
                break;

            case IToken.tIDENTIFIER:
                // TODO - Kludgy way to handle constructors/destructors
                if (flags.haveEncounteredRawType()) {
                    break declSpecifiers;
                }
                if (parm && flags.haveEncounteredTypename()) {
                    break declSpecifiers;
                }
                if (lookAheadForDeclarator(flags)) {
                    break declSpecifiers;
                }

                identifier();
                isIdentifier = true;
                flags.setEncounteredTypename(true);
                break;
            case IToken.t_struct:
            case IToken.t_union:
                try {
                    structOrUnionSpecifier(null);
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    elaboratedTypeSpecifier(null);
                    flags.setEncounteredTypename(true);
                    break;
                }
            case IToken.t_enum:
                try {
                    enumSpecifier(null);
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    // this is an elaborated class specifier
                    elaboratedTypeSpecifier(null);
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
        if (isIdentifier) {
            ICASTTypedefNameSpecifier declSpec = createNamedTypeSpecifier();
            declSpec.setConst(isConst);
            declSpec.setRestrict(isRestrict);
            declSpec.setVolatile(isVolatile);
            declSpec.setInline(isInline);
            declSpec.setStorageClass(storageClass);

            declSpec.setOffset(startingOffset);
            return declSpec;
        }
        if (simpleType != IASTSimpleDeclSpecifier.t_unspecified || isLong
                || isShort || isUnsigned || isSigned) {
            ICASTSimpleDeclSpecifier declSpec = createSimpleTypeSpecifier();
            declSpec.setConst(isConst);
            declSpec.setRestrict(isRestrict);
            declSpec.setVolatile(isVolatile);
            declSpec.setInline(isInline);
            declSpec.setStorageClass(storageClass);

            declSpec.setType(simpleType);
            declSpec.setLong(isLong);
            declSpec.setUnsigned(isUnsigned);
            declSpec.setSigned(isSigned);
            declSpec.setShort(isShort);

            declSpec.setOffset(startingOffset);
            return declSpec;
        }
        return null;
    }

    /**
     * @return
     */
    protected ICASTSimpleDeclSpecifier createSimpleTypeSpecifier() {
        return new CASTSimpleDeclSpecifier();
    }

    /**
     * @return
     */
    protected ICASTTypedefNameSpecifier createNamedTypeSpecifier() {
        return new CASTTypedefNameSpecifier();
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
    protected void structOrUnionSpecifier(IASTNode parent)
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
        //        parent.setTypeSpecifier(astClassSpecifier);
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
                            declaration();
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

    protected void elaboratedTypeSpecifier(IASTNode parent)
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
        if (parent instanceof IASTSimpleDeclaration)
            ((IASTSimpleDeclaration) parent).setDeclSpecifier(null);

        if (isForewardDecl) {
            //			((IASTElaboratedTypeSpecifier) elaboratedTypeSpec).acceptElement(
            //					requestor);
        }
    }

    protected IASTDeclarator initDeclarator() throws EndOfFileException,
            BacktrackException {
        IASTDeclarator d = declarator();

        try {
            //			astFactory.constructExpressions(constructInitializers);
            IASTInitializer i = optionalCInitializer();
            if (i != null) {
                d.setInitializer(i);
                i.setParent(d);
                i.setPropertyInParent(IASTDeclarator.INITIALIZER);
            }
            return d;
        } finally {
            //			astFactory.constructExpressions(true);
        }
    }

    protected IASTDeclarator declarator() throws EndOfFileException,
            BacktrackException {
        IASTDeclarator innerDecl = null;
        IASTName declaratorName = null;
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int line = la.getLineNumber();
        char[] fn = la.getFilename();
        la = null;
        List pointerOps = new ArrayList(DEFAULT_POINTEROPS_LIST_SIZE);
        List parameters = Collections.EMPTY_LIST;
        boolean encounteredVarArgs = false;
        Object bitField = null;
        overallLoop: do {

            consumePointerOperators(pointerOps);

            if (LT(1) == IToken.tLPAREN) {
                consume();
                innerDecl = declarator();
                consume(IToken.tRPAREN);
            } else if (LT(1) == IToken.tIDENTIFIER) {
                declaratorName = createName(identifier());
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
                        //                        d.setIsFunction(true);
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
                                encounteredVarArgs = true;
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
                                IASTParameterDeclaration pd = parameterDeclaration();
                                if (parameters == Collections.EMPTY_LIST)
                                    parameters = new ArrayList(
                                            DEFAULT_PARAMETERS_LIST_SIZE);
                                parameters.add(pd);
                                seenParameter = true;
                            }
                        }
                    }
                    break;
                case IToken.tLBRACKET:
                    consumeArrayModifiers(null);
                    continue;
                case IToken.tCOLON:
                    consume(IToken.tCOLON);
                    bitField = constantExpression();
                default:
                    break;
                }
                break;
            }
            if (LA(1).getType() != IToken.tIDENTIFIER)
                break;

        } while (true);

        IASTDeclarator d = null;
        if (encounteredVarArgs || !parameters.isEmpty()) {
            IASTFunctionDeclarator fc = createFunctionDeclarator();
            fc.setVarArgs( encounteredVarArgs );
            for( int i = 0; i < parameters.size(); ++i )
                fc.addParameterDeclaration((IASTParameterDeclaration) parameters.get(i));            
            d = fc;
        } else if (bitField != null) {
            IASTFieldDeclarator fl = createFieldDeclarator();
            fl.setBitFieldSize((IASTExpression) bitField);
            d = fl;
        } else {
            d = createDeclarator();
        }
        for (int i = 0; i < pointerOps.size(); ++i) {
            IASTPointerOperator po = (IASTPointerOperator) pointerOps.get(i);
            d.addPointerOperator(po);
            po.setParent(d);
            po.setPropertyInParent(IASTDeclarator.POINTER_OPERATOR);
        }
        if (innerDecl != null) {
            d.setNestedDeclarator(innerDecl);
            innerDecl.setParent(d);
            innerDecl.setPropertyInParent(IASTDeclarator.NESTED_DECLARATOR);
        }
        if (declaratorName != null) {
            d.setName(declaratorName);
            declaratorName.setParent(d);
            declaratorName.setPropertyInParent(IASTDeclarator.DECLARATOR_NAME);
        }

        return d;
    }

    /**
     * @return
     */
    private IASTFieldDeclarator createFieldDeclarator() {
        return new CASTFieldDeclarator();
    }

    /**
     * @return
     */
    private IASTFunctionDeclarator createFunctionDeclarator() {
        return new CASTFunctionDeclarator();
    }

    /**
     * @param t
     * @return
     */
    private IASTName createName(IToken t) {
        IASTName n = new CASTName(t.getCharImage());
        n.setOffset(t.getOffset());
        return n;
    }

    /**
     * @return
     */
    protected IASTDeclarator createDeclarator() {
        return new CASTDeclarator();
    }

    protected IToken consumeArrayModifiers(IDeclarator d)
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
                    exp = assignmentExpression();
                else
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

    protected IASTParameterDeclaration parameterDeclaration()
            throws BacktrackException, EndOfFileException {
        IToken current = LA(1);
        int startingOffset = current.getOffset();
        IASTDeclSpecifier declSpec = declSpecifierSeq(true);

        IASTDeclarator declarator = null;
        if (LT(1) != IToken.tSEMI)
            declarator = initDeclarator();

        if (current == LA(1)) {
            int endOffset = (lastToken != null) ? lastToken.getEndOffset() : 0;
            throwBacktrack(current.getOffset(), endOffset, current
                    .getLineNumber(), current.getFilename());
        }

        IASTParameterDeclaration result = createParameterDeclaration();
        result.setOffset(startingOffset);
        result.setDeclSpecifier(declSpec);
        declSpec.setParent(result);
        declSpec.setPropertyInParent(IASTParameterDeclaration.DECL_SPECIFIER);
        result.setDeclarator(declarator);
        declarator.setParent(result);
        declarator.setPropertyInParent(IASTParameterDeclaration.DECLARATOR);

        return result;
    }

    /**
     * @return
     */
    protected IASTParameterDeclaration createParameterDeclaration() {
        return new CASTParameterDeclaration();
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
                simpleDeclaration();
            } catch (BacktrackException b) {
                failParse(b);
                throwBacktrack(b);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#getTranslationUnit()
     */
    protected IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
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

}