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
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
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
            consume(IToken.tLBRACE).getOffset();
            IASTInitializerList result = createInitializerList();
            result.setOffset( startingOffset );
            for (;;) {
                int checkHashcode = LA(1).hashCode();
                // required at least one initializer list
                // get designator list
                List newDesignators = designatorList();
                if (newDesignators.size() != 0)
                    if (LT(1) == IToken.tASSIGN)
                        consume(IToken.tASSIGN);
                IASTInitializer initializer = cInitializerClause(newDesignators);
                result.addInitializer(initializer);
                initializer.setParent( result );
                initializer.setPropertyInParent( IASTInitializerList.NESTED_INITIALIZER );
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
            return result;
        }
        // if we get this far, it means that we have not yet succeeded
        // try this now instead
        // assignmentExpression
        try {
            IASTExpression assignmentExpression = assignmentExpression();
            IASTInitializerExpression result = createInitializerExpression();
            result.setExpression(assignmentExpression);
            result.setOffset(assignmentExpression.getOffset());
            assignmentExpression.setParent(result);
            assignmentExpression
                    .setPropertyInParent(IASTInitializerExpression.INITIALIZER_EXPRESSION);
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
    protected IASTInitializerList createInitializerList() {
        return new CASTInitializerList();
    }

    /**
     * @return
     */
    protected IASTInitializerExpression createInitializerExpression() {
        return new CASTInitializerExpresion();
    }

    protected List designatorList() throws EndOfFileException,
            BacktrackException {
        //designated initializers for C
        List designatorList = Collections.EMPTY_LIST;
        
        if (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
            while (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
                if (LT(1) == IToken.tDOT) {
                    int offset = consume(IToken.tDOT).getOffset();
                    IToken id = identifier();
                    ICASTFieldDesignator designator = createFieldDesignator();
                    designator.setOffset( offset );
                    IASTName n = createName( id );
                    designator.setName( n );
                    n.setParent( designator );
                    n.setPropertyInParent( ICASTFieldDesignator.FIELD_NAME );
                    if( designatorList == Collections.EMPTY_LIST )
                        designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
                    designatorList.add( designator );
                } else if (LT(1) == IToken.tLBRACKET) {
                    IToken mark = consume(IToken.tLBRACKET);
                    int offset = mark.getOffset();
                    IASTExpression constantExpression = expression();
                    if (LT(1) == IToken.tRBRACKET) {
                        consume(IToken.tRBRACKET);
                        ICASTArrayDesignator designator = createArrayDesignator();
                        designator.setOffset( offset );
                        designator.setSubscriptExpression( constantExpression );
                        constantExpression.setParent( designator );
                        constantExpression.setPropertyInParent( ICASTArrayDesignator.SUBSCRIPT_EXPRESSION );
                        if( designatorList == Collections.EMPTY_LIST )
                            designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
                        designatorList.add( designator );
                        continue;
                    }
                    backup(mark);
                    if (supportGCCStyleDesignators ) {
                        int startOffset = consume(IToken.tLBRACKET).getOffset();
                        IASTExpression constantExpression1 = expression();
                        consume(IToken.tELLIPSIS);
                        IASTExpression constantExpression2 = expression();
                        consume(IToken.tRBRACKET);
                        IGCCASTArrayRangeDesignator designator = createArrayRangeDesignator();
                        designator.setOffset( startOffset );
                        designator.setRangeFloor( constantExpression1 );
                        constantExpression1.setParent( designator );
                        constantExpression1.setPropertyInParent( IGCCASTArrayRangeDesignator.SUBSCRIPT_FLOOR_EXPRESSION );
                        designator.setRangeCeiling( constantExpression2 );
                        constantExpression2.setParent( designator );
                        constantExpression2.setPropertyInParent( IGCCASTArrayRangeDesignator.SUBSCRIPT_CEILING_EXPRESSION );
                        if( designatorList == Collections.EMPTY_LIST )
                            designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
                        designatorList.add( designator );                                
                    }
                } else if ( supportGCCStyleDesignators && LT(1) == IToken.tIDENTIFIER )
                {
                    IToken identifier = identifier();
                    consume(IToken.tCOLON);
                    ICASTFieldDesignator designator = createFieldDesignator();
                    designator.setOffset( identifier.getOffset() );
                    IASTName n = createName( identifier );
                    designator.setName( n );
                    n.setParent( designator );
                    n.setPropertyInParent( ICASTFieldDesignator.FIELD_NAME );
                    if( designatorList == Collections.EMPTY_LIST )
                        designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
                    designatorList.add( designator );                    
                }
            }
        } else {
            if (supportGCCStyleDesignators
                    && (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tLBRACKET)) {

                if (LT(1) == IToken.tIDENTIFIER) {
                    IToken identifier = identifier();
                    consume(IToken.tCOLON);
                    ICASTFieldDesignator designator = createFieldDesignator();
                    designator.setOffset( identifier.getOffset() );
                    IASTName n = createName( identifier );
                    designator.setName( n );
                    n.setParent( designator );
                    n.setPropertyInParent( ICASTFieldDesignator.FIELD_NAME );
                    if( designatorList == Collections.EMPTY_LIST )
                        designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
                    designatorList.add( designator );
                } else if (LT(1) == IToken.tLBRACKET) {
                    int startOffset = consume(IToken.tLBRACKET).getOffset();
                    IASTExpression constantExpression1 = expression();
                    consume(IToken.tELLIPSIS);
                    IASTExpression constantExpression2 = expression();
                    consume(IToken.tRBRACKET);
                    IGCCASTArrayRangeDesignator designator = createArrayRangeDesignator();
                    designator.setOffset( startOffset );
                    designator.setRangeFloor( constantExpression1 );
                    constantExpression1.setParent( designator );
                    constantExpression1.setPropertyInParent( IGCCASTArrayRangeDesignator.SUBSCRIPT_FLOOR_EXPRESSION );
                    designator.setRangeCeiling( constantExpression2 );
                    constantExpression2.setParent( designator );
                    constantExpression2.setPropertyInParent( IGCCASTArrayRangeDesignator.SUBSCRIPT_CEILING_EXPRESSION );
                    if( designatorList == Collections.EMPTY_LIST )
                        designatorList = new ArrayList( DEFAULT_DESIGNATOR_LIST_SIZE );
                    designatorList.add( designator );                                
                }
            }
        }
        return designatorList;
    }

    /**
     * @return
     */
    protected IGCCASTArrayRangeDesignator createArrayRangeDesignator() {
        return new CASTArrayRangeDesignator();
    }

    /**
     * @return
     */
    protected ICASTArrayDesignator createArrayDesignator() {
        return new CASTArrayDesignator();
    }

    /**
     * @return
     */
    protected ICASTFieldDesignator createFieldDesignator() {
        return new CASTFieldDesignator();
    }

    protected IASTDeclaration declaration() throws EndOfFileException,
            BacktrackException {
        switch (LT(1)) {
        case IToken.t_asm:
            IToken first = consume(IToken.t_asm);
            consume(IToken.tLPAREN);
            String assembly = consume(IToken.tSTRING).getImage();
            consume(IToken.tRPAREN);
            consume(IToken.tSEMI);
            cleanupLastToken();
            return buildASMDirective( first.getOffset(), assembly );
        default:
            IASTDeclaration d = simpleDeclaration();
            cleanupLastToken();
            return d;
        }

    }

    /**
     * @param offset
     * @param assembly
     * @return
     */
    protected IASTASMDeclaration buildASMDirective(int offset, String assembly) {
        IASTASMDeclaration result = createASMDirective();
        result.setOffset( offset );
        result.setAssembly( assembly );
        return result;
    }

    /**
     * @return
     */
    protected IASTASMDeclaration createASMDirective() {
        return new CASTASMDeclaration();
    }

    /**
     * @throws BacktrackException
     * @throws EndOfFileException
     */
    protected IASTDeclaration simpleDeclaration() throws BacktrackException,
            EndOfFileException {
        IToken firstToken = LA(1);
        int firstOffset = firstToken.getOffset();
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
            if (s != null) {
                funcDefinition.setBody(s);
                s.setParent(funcDefinition);
                s.setPropertyInParent(IASTFunctionDefinition.FUNCTION_BODY);
            }
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
    protected CASTSimpleDeclaration createSimpleDeclaration() {
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
    protected IASTExpression assignmentExpression() throws EndOfFileException,
            BacktrackException {
        IASTExpression conditionalExpression = conditionalExpression();
        // if the condition not taken, try assignment operators
        if (conditionalExpression != null && conditionalExpression instanceof IASTConditionalExpression ) //&&
            return conditionalExpression;
        switch (LT(1)) {
        case IToken.tASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_assign, conditionalExpression);
        case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_multiplyAssign, conditionalExpression);
        case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_divideAssign, conditionalExpression);
        case IToken.tMODASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_moduloAssign, conditionalExpression);
        case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_plusAssign, conditionalExpression);
        case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_minusAssign, conditionalExpression);
        case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_shiftRightAssign, conditionalExpression);
        case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_shiftLeftAssign, conditionalExpression);
        case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_binaryAndAssign, conditionalExpression);
        case IToken.tXORASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_binaryXorAssign, conditionalExpression);
        case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_binaryOrAssign, conditionalExpression);
        }
        return conditionalExpression;
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression relationalExpression() throws BacktrackException,
            EndOfFileException {

        IASTExpression firstExpression = shiftExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tGT:
            case IToken.tLT:
            case IToken.tLTEQUAL:
            case IToken.tGTEQUAL:
                int t = consume().getType();
                IASTExpression secondExpression = shiftExpression();
                int operator = 0;
                switch (t) {
                case IToken.tGT:
                    operator = IASTBinaryExpression.op_greaterThan; 
                    break;
                case IToken.tLT:
                    operator = IASTBinaryExpression.op_lessThan; 
                    break;
                case IToken.tLTEQUAL:
                    operator = IASTBinaryExpression.op_lessEqual;
                    break;
                case IToken.tGTEQUAL:
                    operator = IASTBinaryExpression.op_greaterEqual; 
                    break;
                }
                firstExpression = buildBinaryExpression( operator, firstExpression, secondExpression );
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
        IASTExpression firstExpression = castExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
                IToken t = consume();
                IASTExpression secondExpression = castExpression();
                int operator = 0;
                switch (t.getType()) {
                case IToken.tSTAR:
                    operator = IASTBinaryExpression.op_multiply;
                    break;
                case IToken.tDIV:
                    operator = IASTBinaryExpression.op_divide; 
                    break;
                case IToken.tMOD:
                    operator = IASTBinaryExpression.op_modulo; 
                    break;
                }
                IASTBinaryExpression result = createBinaryExpression();
                result.setOffset( firstExpression.getOffset() );
                result.setOperator( operator );
                result.setOperand1( firstExpression );
                firstExpression.setParent( result );
                firstExpression.setPropertyInParent( IASTBinaryExpression.OPERAND_ONE );
                result.setOperand2( secondExpression );
                secondExpression.setParent( result );
                secondExpression.setPropertyInParent( IASTBinaryExpression.OPERAND_TWO );
                firstExpression = result;
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
            IToken mark = mark();
            int startingOffset = mark.getOffset();
            consume();
            IASTTypeId typeId = null;
            IASTExpression castExpression = null;
            // If this isn't a type name, then we shouldn't be here
            try {
                try {
                    typeId = typeId(false);
                    consume(IToken.tRPAREN);
                    castExpression = castExpression();
                } catch (BacktrackException bte) {
                    backup(mark);
                    throwBacktrack(bte);
                }

                
                return buildTypeIdUnaryExpression( IASTUnaryTypeIdExpression.op_cast, typeId, castExpression, startingOffset );
            } catch (BacktrackException b) {
            }
        }
        return unaryExpression();
    }

    /**
     * @param op
     * @param typeId
     * @param subExpression
     * @param startingOffset
     * @return
     */
    protected IASTExpression buildTypeIdUnaryExpression(int op, IASTTypeId typeId, IASTExpression subExpression, int startingOffset) {
        IASTUnaryTypeIdExpression result = createUnaryTypeIdExpression();
        result.setOperator( op );
        result.setOffset( startingOffset );
        result.setTypeId(typeId);
        typeId.setParent( result );
        typeId.setPropertyInParent( IASTUnaryTypeIdExpression.TYPE_ID );
        result.setOperand( subExpression );
        subExpression.setParent( result );
        subExpression.setPropertyInParent( IASTUnaryTypeIdExpression.OPERAND );
        return result;
    }

    /**
     * @return
     */
    protected IASTUnaryTypeIdExpression createUnaryTypeIdExpression() {
        return new CASTUnaryTypeIdExpression();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression unaryExpression() throws EndOfFileException,
            BacktrackException {
        int startingOffset = LA(1).getOffset();
        switch (LT(1)) {
        case IToken.tSTAR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_star );
        case IToken.tAMPER:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_amper);
        case IToken.tPLUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_plus );
        case IToken.tMINUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_minus );
        case IToken.tNOT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_not );
        case IToken.tCOMPL:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_tilde);
        case IToken.tINCR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixIncr);
        case IToken.tDECR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixDecr);
        case IToken.t_sizeof:
            startingOffset = consume(IToken.t_sizeof).getOffset();
            IToken mark = LA(1);
            IASTExpression unaryExpression = null;
            IASTTypeId typeId = null;
            if (LT(1) == IToken.tLPAREN) {
                try {
                    consume(IToken.tLPAREN);
                    typeId = typeId(false);
                    consume(IToken.tRPAREN);
                } catch (BacktrackException bt) {
                    backup(mark);
                    typeId = null;
                    unaryExpression = unaryExpression();
                }
            } else {
                unaryExpression = unaryExpression();
            }
            mark = null;
            if (typeId == null && unaryExpression != null )
                return buildUnaryExpression( IASTUnaryExpression.op_sizeof, unaryExpression, startingOffset );
            return buildTypeIdExpression( IASTTypeIdExpression.op_sizeof, typeId, startingOffset ); 
            
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
     * @param op_sizeof
     * @param typeId
     * @param startingOffset
     * @return
     */
    protected IASTExpression buildTypeIdExpression(int op_sizeof, IASTTypeId typeId, int startingOffset) {
        IASTTypeIdExpression result = createTypeIdExpression();
        result.setOperator( op_sizeof );
        result.setOffset( startingOffset );
        result.setTypeId( typeId );
        typeId.setParent( result );
        typeId.setPropertyInParent( IASTTypeIdExpression.TYPE_ID );
        return result;
    }

    /**
     * @return
     */
    protected IASTTypeIdExpression createTypeIdExpression() {
        return new CASTTypeIdExpression();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression postfixExpression() throws EndOfFileException,
            BacktrackException {

        IASTExpression firstExpression = null;
        switch (LT(1)) {
        case IToken.tLPAREN:
            // ( type-name ) { initializer-list }
            // ( type-name ) { initializer-list , }
            IToken m = mark();
        	try
        	{
	            int offset = consume(IToken.tLPAREN).getOffset();
	            IASTTypeId t = typeId(false);
	            consume( IToken.tRPAREN );
	            IASTInitializer i = cInitializerClause(Collections.EMPTY_LIST);
	            firstExpression = buildTypeIdInitializerExpression( t, i, offset );
	            break;
        	}
        	catch( BacktrackException bt )
        	{
        	    backup( m );
        	}
            
        default:
            firstExpression = primaryExpression();
        }

        IASTExpression secondExpression = null;
        for (;;) {
            switch (LT(1)) {
            case IToken.tLBRACKET:
                // array access
                consume(IToken.tLBRACKET);
                secondExpression = expression();
                consume(IToken.tRBRACKET);
                IASTArraySubscriptExpression s  = createArraySubscriptExpression();
                s.setOffset( firstExpression.getOffset() );
                s.setArrayExpression( firstExpression );
                firstExpression.setParent( s );
                firstExpression.setPropertyInParent( IASTArraySubscriptExpression.ARRAY );
                s.setSubscriptExpression( secondExpression );
                secondExpression.setParent( s );
                secondExpression.setPropertyInParent( IASTArraySubscriptExpression.SUBSCRIPT );
                firstExpression = s;
                break;
            case IToken.tLPAREN:
                // function call
                consume(IToken.tLPAREN);
            	if( LT(1) != IToken.tRPAREN )
            	    secondExpression = expression();
                consume(IToken.tRPAREN);
                IASTFunctionCallExpression f = createFunctionCallExpression();
                f.setOffset( firstExpression.getOffset() );
                f.setFunctionNameExpression( firstExpression );
                firstExpression.setParent( f );
                firstExpression.setPropertyInParent( IASTFunctionCallExpression.FUNCTION_NAME );
                
                if( secondExpression != null )
                {
                    f.setParameterExpression( secondExpression );
	                secondExpression.setParent( f );
	                secondExpression.setPropertyInParent( IASTFunctionCallExpression.PARAMETERS );
                }
                firstExpression = f;
                break;
            case IToken.tINCR:
                int offset = consume(IToken.tINCR).getOffset();
            	firstExpression = buildUnaryExpression( IASTUnaryExpression.op_postFixIncr, firstExpression, offset );
                break;
            case IToken.tDECR:
                offset = consume().getOffset();
        		firstExpression = buildUnaryExpression( IASTUnaryExpression.op_postFixDecr, firstExpression, offset );
                break;
            case IToken.tDOT:
                // member access
                consume(IToken.tDOT);
                IASTName name = createName( identifier() );
                IASTFieldReference result = createFieldReference();
                result.setOffset( firstExpression.getOffset() );
                result.setFieldOwner( firstExpression );
                result.setIsPointerDereference(false);
                firstExpression.setParent( result );
                firstExpression.setPropertyInParent( IASTFieldReference.FIELD_OWNER );
                result.setFieldName( name );
                name.setParent( result );
                name.setPropertyInParent( IASTFieldReference.FIELD_NAME );
                firstExpression = result;
                break;
            case IToken.tARROW:
                // member access
                consume(IToken.tARROW);
	            name = createName( identifier() );
	            result = createFieldReference();
	            result.setOffset( firstExpression.getOffset() );
	            result.setFieldOwner( firstExpression );
	            result.setIsPointerDereference(true);
	            firstExpression.setParent( result );
	            firstExpression.setPropertyInParent( IASTFieldReference.FIELD_OWNER );
	            result.setFieldName( name );
	            name.setParent( result );
	            name.setPropertyInParent( IASTFieldReference.FIELD_NAME );
	            firstExpression = result;
                break;
            default:
                return firstExpression;
            }
        }
    }

    /**
     * @return
     */
    protected  IASTFunctionCallExpression createFunctionCallExpression() {
        return new CASTFunctionCallExpression();
    }

    /**
     * @return
     */
    protected IASTArraySubscriptExpression createArraySubscriptExpression() {
        return new CASTArraySubscriptExpression();
    }

    /**
     * @param t
     * @param i
     * @param offset
     * @return
     */
    protected ICASTTypeIdInitializerExpression buildTypeIdInitializerExpression(IASTTypeId t, IASTInitializer i, int offset) {
        ICASTTypeIdInitializerExpression result = createTypeIdInitializerExpression();
        result.setOffset( offset );
        result.setTypeId( t );
        t.setParent( result );
        t.setPropertyInParent( ICASTTypeIdInitializerExpression.TYPE_ID );
        result.setInitializer( i );
        i.setParent( result );
        i.setPropertyInParent( ICASTTypeIdInitializerExpression.INITIALIZER );
        return result;
    }

    /**
     * @return
     */
    protected ICASTTypeIdInitializerExpression createTypeIdInitializerExpression() {
        return new CASTTypeIdInitializerExpression();
    }

    /**
     * @return
     */
    protected IASTFieldReference createFieldReference() {
        return new CASTFieldReference();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    protected IASTExpression primaryExpression() throws EndOfFileException,
            BacktrackException {
        IToken t = null;
        IASTLiteralExpression literalExpression = null;
        switch (LT(1)) {
        // TO DO: we need more literals...
        case IToken.tINTEGER:
            t = consume();
        	literalExpression = createLiteralExpression();
        	literalExpression.setKind( IASTLiteralExpression.lk_integer_constant);
        	literalExpression.setValue( t.getImage() );
        	literalExpression.setOffset( t.getOffset() );
        	return literalExpression;
        case IToken.tFLOATINGPT:
            t = consume();
	    	literalExpression = createLiteralExpression();
	    	literalExpression.setKind( IASTLiteralExpression.lk_float_constant );
	    	literalExpression.setValue( t.getImage() );
	    	literalExpression.setOffset( t.getOffset() );
	    	return literalExpression;
        case IToken.tSTRING:
        case IToken.tLSTRING:
            t = consume();
	    	literalExpression = createLiteralExpression();
	    	literalExpression.setKind( IASTLiteralExpression.lk_string_literal );
	    	literalExpression.setValue( t.getImage() );
	    	literalExpression.setOffset( t.getOffset() );
	    	return literalExpression;
        case IToken.tCHAR:
        case IToken.tLCHAR:
            t = consume();
	    	literalExpression = createLiteralExpression();
	    	literalExpression.setKind( IASTLiteralExpression.lk_char_constant );
	    	literalExpression.setValue( t.getImage() );
	    	literalExpression.setOffset( t.getOffset() );
	    	return literalExpression;
        case IToken.tLPAREN:
            t = consume();
        	//TODO - do we need to return a wrapper?
            IASTExpression lhs = expression();
            consume(IToken.tRPAREN);
            return lhs;
        case IToken.tIDENTIFIER:

            int startingOffset = LA(1).getOffset();
            int line = LA(1).getLineNumber();
            IToken t1 = identifier();
            IASTIdExpression idExpression = createIdExpression();
            IASTName name = createName(t1);
            idExpression.setName(name);
            name.setParent(idExpression);
            name.setPropertyInParent(IASTIdExpression.ID_NAME);
            return idExpression;
        default:
            IToken la = LA(1);
            startingOffset = la.getOffset();
            line = la.getLineNumber();
            throwBacktrack( startingOffset, startingOffset, line, la.getFilename() );
            return null;
        }

    }

    /**
     * @return
     */
    protected IASTLiteralExpression createLiteralExpression() {
        return new CASTLiteralExpression();
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
            int startOffset = consume(IToken.t_case).getOffset();
            IASTExpression case_exp = constantExpression();
            consume(IToken.tCOLON);
            cleanupLastToken();
            IASTCaseStatement cs = createCaseStatement();
            cs.setOffset( startOffset );
            cs.setExpression( case_exp );
            case_exp.setParent( cs );
            case_exp.setPropertyInParent( IASTCaseStatement.EXPRESSION );
            return cs;
        case IToken.t_default:
            startOffset = consume(IToken.t_default).getOffset();
            consume(IToken.tCOLON);
            cleanupLastToken();
            IASTDefaultStatement df = createDefaultStatement();
            df.setOffset( startOffset );
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
		    if_stmt.setOffset( startOffset );
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
            switch_statement.setOffset( startOffset );
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
            while_statement.setOffset( startOffset );
            while_statement.setCondition( while_condition );
            while_condition.setParent( while_statement );
            while_condition.setPropertyInParent( IASTWhileStatement.CONDITION );
            while_statement.setBody( while_body );
            while_condition.setParent( while_statement );
            while_condition.setPropertyInParent( IASTWhileStatement.BODY );
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
            do_statement.setOffset( startOffset );
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
            for_statement.setOffset( startOffset );
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
            break_statement.setOffset( startOffset );
            return break_statement;
        case IToken.t_continue:
            startOffset = consume(IToken.t_continue).getOffset();
            consume(IToken.tSEMI);
            cleanupLastToken();
            IASTContinueStatement continue_statement = createContinueStatement();
            continue_statement.setOffset( startOffset );
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
            return_statement.setOffset( startOffset );
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
            goto_statement.setOffset( startOffset );
            goto_statement.setName( goto_label_name );
            goto_label_name.setParent( goto_statement );
            goto_label_name.setPropertyInParent( IASTGotoStatement.NAME );
            return goto_statement;
        case IToken.tSEMI:
            startOffset = consume(IToken.tSEMI ).getOffset();
        	cleanupLastToken();
        	IASTNullStatement null_statement = createNullStatement();
        	null_statement.setOffset( startOffset );
        	return null_statement;
        default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
                IToken labelName = consume(IToken.tIDENTIFIER);
                consume(IToken.tCOLON);
                IASTLabelStatement label_statement = createLabelStatement();
                label_statement.setOffset( labelName.getOffset() );
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

    protected IASTTypeId typeId(boolean skipArrayModifiers)
            throws EndOfFileException, BacktrackException {
        IToken mark = mark();
        int startingOffset = mark.getOffset();
        char [] filename = mark.getFilename();
        int lineNumber = mark.getLineNumber();
        IASTDeclSpecifier declSpecifier = null;
        IASTDeclarator declarator = null;

        try
        {
	        declSpecifier = declSpecifierSeq(false);
	        declarator = declarator();
        }
        catch( BacktrackException bt )
        {
            int endingOffset = lastToken == null ? 0 : lastToken.getEndOffset();
            backup( mark );
            throwBacktrack( startingOffset, endingOffset, lineNumber, filename );            
        }
        if( declarator == null || declarator.getName().toString() != null )   //$NON-NLS-1$
        {
            int endingOffset = lastToken == null ? 0 : lastToken.getEndOffset();
            backup( mark );
            throwBacktrack( startingOffset, endingOffset, lineNumber, filename );
        }
        
        IASTTypeId result = createTypeId();
        result.setOffset( startingOffset );
        
        result.setDeclSpecifier( declSpecifier );
        declSpecifier.setParent( result );
        declSpecifier.setPropertyInParent( IASTTypeId.DECL_SPECIFIER );
        
        result.setAbstractDeclarator( declarator );
        declarator.setParent( result );
        declarator.setPropertyInParent( IASTTypeId.ABSTRACT_DECLARATOR );
        
        return result;
    }

    /**
     * @return
     */
    protected IASTTypeId createTypeId() {
        return new CASTTypeId();
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
    protected void consumePointerOperators(List pointerOps)
            throws EndOfFileException, BacktrackException {
        for (;;) {
            IToken mark = mark();

            ITokenDuple nameDuple = null;
            boolean isConst = false, isVolatile = false, isRestrict = false;
            if (LT(1) == IToken.tIDENTIFIER) {
                IToken t = identifier();
                nameDuple = TokenFactory.createTokenDuple(t, t);
            }

            if( LT(1) != IToken.tSTAR )
            {
                backup( mark );
                break;
            }

            consume(IToken.tSTAR);
            int startOffset = mark.getOffset();
            for (;;) {
                IToken t = LA(1);
                switch (LT(1)) {
                case IToken.t_const:
                    consume(IToken.t_const);
                    isConst = true;
                    break;
                case IToken.t_volatile:
                    consume(IToken.t_volatile);
                    isVolatile = true;
                    break;
                case IToken.t_restrict:
                    consume(IToken.t_restrict);
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
                po.setOffset( startOffset );
                ((ICASTPointer) po).setConst(isConst);
                ((ICASTPointer) po).setVolatile(isVolatile);
                ((ICASTPointer) po).setRestrict(isRestrict);
            }
            if (po != null) 
                pointerOps.add(po);
        }
    }

    /**
     * @return
     */
    protected ICASTPointer createPointer() {
        return new CASTPointer();
    }

    protected IASTDeclSpecifier declSpecifierSeq(boolean parm)
            throws BacktrackException, EndOfFileException {
        Flags flags = new Flags(parm);

        int startingOffset = LA(1).getOffset();
        int storageClass = IASTDeclSpecifier.sc_unspecified;
        boolean isInline = false;
        boolean isConst = false, isRestrict = false, isVolatile = false;
        boolean isShort = false, isLong = false, isUnsigned = false, isIdentifier = false, isSigned = false;
        int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
        IToken identifier = null;
        ICASTCompositeTypeSpecifier structSpec = null;
        ICASTElaboratedTypeSpecifier elabSpec = null;
        ICASTEnumerationSpecifier enumSpec = null;
        
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
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_void;
                break;
            case IToken.t_char:
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_char;
                break;
            case IToken.t_short:
                flags.setEncounteredRawType(true);
                consume();
                isShort = true;
                break;
            case IToken.t_int:
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_int;
                break;
            case IToken.t_long:
                flags.setEncounteredRawType(true);
                consume();
                isLong = true;
                break;
            case IToken.t_float:
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_float;
                break;
            case IToken.t_double:
                flags.setEncounteredRawType(true);
                consume();
                simpleType = IASTSimpleDeclSpecifier.t_double;
                break;
            case IToken.t_signed:
                flags.setEncounteredRawType(true);
                consume();
                isSigned = true;
                break;
            case IToken.t_unsigned:
                flags.setEncounteredRawType(true);
                consume();
                isUnsigned = true;
                break;
            case IToken.t__Bool:
                flags.setEncounteredRawType(true);
                consume();
                simpleType = ICASTSimpleDeclSpecifier.t_Bool;
                break;
            case IToken.t__Complex:
                consume(IToken.t__Complex);
                simpleType = ICASTSimpleDeclSpecifier.t_Complex;
                break;
            case IToken.t__Imaginary:
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

                identifier = identifier();
                isIdentifier = true;
                flags.setEncounteredTypename(true);
                break;
            case IToken.t_struct:
            case IToken.t_union:
                try {
                    structSpec = structOrUnionSpecifier( );
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    elabSpec = elaboratedTypeSpecifier(  );
                    flags.setEncounteredTypename(true);
                    break;
                }
            case IToken.t_enum:
                try {
                    enumSpec = (ICASTEnumerationSpecifier) enumSpecifier(null);
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    // this is an elaborated class specifier
                    elabSpec = elaboratedTypeSpecifier( );
                    flags.setEncounteredTypename(true);
                    break;
                }
            default:
                if (supportTypeOfUnaries && LT(1) == IGCCToken.t_typeof) {
                    Object expression = unaryTypeofExpression();
                    if (expression != null) {
                        flags.setEncounteredTypename(true);
                    }
                }
                break declSpecifiers;
            }
        }
        
        if( structSpec != null )
        {
            structSpec.setOffset( startingOffset );
            structSpec.setConst(isConst);
            structSpec.setRestrict(isRestrict);
            structSpec.setVolatile(isVolatile);
            structSpec.setInline(isInline);
            structSpec.setStorageClass(storageClass);

            return structSpec;
        }
        
        if( enumSpec != null )
        {
            enumSpec.setOffset( startingOffset );
            enumSpec.setConst(isConst);
            enumSpec.setRestrict(isRestrict);
            enumSpec.setVolatile(isVolatile);
            enumSpec.setInline(isInline);
            enumSpec.setStorageClass(storageClass);
            return enumSpec;
            
        }
        if( elabSpec != null )
        {
            elabSpec.setOffset( startingOffset );
            elabSpec.setConst(isConst);
            elabSpec.setRestrict(isRestrict);
            elabSpec.setVolatile(isVolatile);
            elabSpec.setInline(isInline);
            elabSpec.setStorageClass(storageClass);

            return elabSpec;
        }
        if (isIdentifier) {
            ICASTTypedefNameSpecifier declSpec = createNamedTypeSpecifier();
            declSpec.setConst(isConst);
            declSpec.setRestrict(isRestrict);
            declSpec.setVolatile(isVolatile);
            declSpec.setInline(isInline);
            declSpec.setStorageClass(storageClass);

            declSpec.setOffset(startingOffset);
            IASTName name = createName( identifier );
            declSpec.setName( name );
            name.setParent( declSpec );
            name.setPropertyInParent( IASTTypedefNameSpecifier.NAME );
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
     * @param owner
     *            IParserCallback object that represents the declaration that
     *            owns this classSpecifier
     * 
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICASTCompositeTypeSpecifier structOrUnionSpecifier( )
            throws BacktrackException, EndOfFileException {

        int classKind = 0;
        IToken classKey = null;
        IToken mark = mark();

        // class key
        switch (LT(1)) {
        case IToken.t_struct:
            classKey = consume();
            classKind = IASTCompositeTypeSpecifier.k_struct;
            break;
        case IToken.t_union:
            classKey = consume();
            classKind = IASTCompositeTypeSpecifier.k_union;
            break;
        default:
            throwBacktrack(mark.getOffset(), mark.getEndOffset(), mark
                    .getLineNumber(), mark.getFilename());
        }

        IToken nameToken = null;
        // class name
        if (LT(1) == IToken.tIDENTIFIER) {
            nameToken = identifier();
        }

        if (LT(1) != IToken.tLBRACE) {
            IToken errorPoint = LA(1);
            backup(mark);
            throwBacktrack(errorPoint.getOffset(), errorPoint.getEndOffset(),
                    errorPoint.getLineNumber(), errorPoint.getFilename());
        }

        consume(IToken.tLBRACE);
        cleanupLastToken();
        
        IASTName name = null;
        if( nameToken != null )
            name = createName( nameToken );
        else
            name = createName();
        
        ICASTCompositeTypeSpecifier result = createCompositeTypeSpecifier();
        
        result.setKey( classKind );
        result.setOffset( classKey.getOffset() );
        
        result.setName( name );
        if( name != null )
        {
            name.setParent( result );
            name.setPropertyInParent( IASTCompositeTypeSpecifier.TYPE_NAME );
        }

        memberDeclarationLoop: while (LT(1) != IToken.tRBRACE) {
            int checkToken = LA(1).hashCode();
            switch (LT(1)) {
            case IToken.tRBRACE:
                consume(IToken.tRBRACE);
                break memberDeclarationLoop;
            default:
                try {
                    IASTDeclaration d = declaration();
                    d.setParent( result );
                    d.setPropertyInParent( IASTCompositeTypeSpecifier.MEMBER_DECLARATION );
                    result.addMemberDeclaration( d );
                } catch (BacktrackException bt) {
                    if (checkToken == LA(1).hashCode())
                        failParseWithErrorHandling();
                }
            }
            if (checkToken == LA(1).hashCode())
                failParseWithErrorHandling();
        }
        // consume the }
        consume(IToken.tRBRACE);
        return result;
    }

    /**
     * @return
     */
    protected IASTName createName() {
        return new CASTName();
    }

    /**
     * @return
     */
    protected ICASTCompositeTypeSpecifier createCompositeTypeSpecifier() {
        return new CASTCompositeTypeSpecifier();
    }

    protected ICASTElaboratedTypeSpecifier elaboratedTypeSpecifier()
            throws BacktrackException, EndOfFileException {
        // this is an elaborated class specifier
        IToken t = consume();
        int eck = 0;

        switch (t.getType()) {
        case IToken.t_struct:
            eck = IASTElaboratedTypeSpecifier.k_struct;
            break;
        case IToken.t_union:
            eck = IASTElaboratedTypeSpecifier.k_union;
            break;
        case IToken.t_enum:
            eck = IASTElaboratedTypeSpecifier.k_enum;
            break;
        default:
            backup(t);
            throwBacktrack(t.getOffset(), t.getEndOffset(), t.getLineNumber(),
                    t.getFilename());
        }

        IToken identifier = identifier();
        IASTName name = createName( identifier );
        ICASTElaboratedTypeSpecifier result = createElaboratedTypeSpecifier();
        result.setName( name );
        name.setParent( result );
        name.setPropertyInParent( IASTElaboratedTypeSpecifier.TYPE_NAME );
        result.setKind( eck );
        return result;
    }

    /**
     * @return
     */
    protected ICASTElaboratedTypeSpecifier createElaboratedTypeSpecifier() {
        return new CASTElaboratedTypeSpecifier();
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
        List arrayMods = Collections.EMPTY_LIST;
        boolean encounteredVarArgs = false;
        Object bitField = null;
        boolean isFunction = false;
        overallLoop: do {

            consumePointerOperators(pointerOps);

            if (LT(1) == IToken.tLPAREN) {
                consume();
                innerDecl = declarator();
                consume(IToken.tRPAREN);
                declaratorName = createName();
            } else if (LT(1) == IToken.tIDENTIFIER) {
                declaratorName = createName(identifier());
            }
            else
                declaratorName = createName();

            for (;;) {
                switch (LT(1)) {
                case IToken.tLPAREN:
                    boolean failed = false;
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
                        isFunction = true;
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
                    if( arrayMods == Collections.EMPTY_LIST )
                        arrayMods = new ArrayList( DEFAULT_POINTEROPS_LIST_SIZE );
                    consumeArrayModifiers( arrayMods );
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
        if (isFunction) {
            IASTFunctionDeclarator fc = createFunctionDeclarator();
            fc.setVarArgs(encounteredVarArgs);
            for (int i = 0; i < parameters.size(); ++i)
            {
                IASTParameterDeclaration p = (IASTParameterDeclaration) parameters
                .get(i);
                p.setParent( fc );
                p.setPropertyInParent( IASTFunctionDeclarator.FUNCTION_PARAMETER );
                fc.addParameterDeclaration(p);
            }
            d = fc;
        } else if( arrayMods != Collections.EMPTY_LIST )
        {
            d = createArrayDeclarator();
            for( int i = 0; i < arrayMods.size(); ++i )
            {
                IASTArrayModifier m = (IASTArrayModifier) arrayMods.get(i);
                m.setParent( d );
                m.setPropertyInParent( IASTArrayDeclarator.ARRAY_MODIFIER );
                ((IASTArrayDeclarator)d).addArrayModifier( m );
            }
        }
        else if (bitField != null) {
            IASTFieldDeclarator fl = createFieldDeclarator();
            fl.setBitFieldSize((IASTExpression) bitField);
            d = fl;
        } else 
        {
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


	protected IASTArrayDeclarator createArrayDeclarator() {
	    return new CASTArrayDeclarator();
	}

    /**
     * @return
     */
    protected IASTFieldDeclarator createFieldDeclarator() {
        return new CASTFieldDeclarator();
    }

    /**
     * @return
     */
    protected IASTFunctionDeclarator createFunctionDeclarator() {
        return new CASTFunctionDeclarator();
    }

    
    
    
    /**
     * @param t
     * @return
     */
    protected IASTName createName(IToken t) {
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

    protected void consumeArrayModifiers(List arrayMods )
            throws EndOfFileException, BacktrackException {

        while (LT(1) == IToken.tLBRACKET) {
            //eat the '['
            int startOffset = consume(IToken.tLBRACKET).getOffset(); 

            int modifier = -1;
            
            outerLoop: do {
                switch (LT(1)) {
                case IToken.t_static:
                case IToken.t_const:
                case IToken.t_volatile:
                case IToken.t_restrict:
                    modifier = consume().getType();
                    continue;
                default:
                    break outerLoop;
                }
            } while (true);

            IASTExpression exp = null;

            if (LT(1) != IToken.tRBRACKET) {
                if (modifier != -1 )
                    exp = assignmentExpression();
                else
                    exp = constantExpression();
            }
            consume(IToken.tRBRACKET);
            
            IASTArrayModifier arrayMod = null;
            if( modifier == -1 )
                arrayMod = createArrayModifier();
            else
            {
                ICASTArrayModifier temp = createCArrayModifier();
                switch( modifier )
                {
                	case IToken.t_static:
                	    temp.setConst( true );
                		break;
                	case IToken.t_const:
                	    temp.setConst( true );
                		break;
                	case IToken.t_volatile:
                	    temp.setVolatile( true );
                		break;
                	case IToken.t_restrict:
                	    temp.setRestrict( true );
                		break;                	    
                }
                arrayMod = temp;
            }
            arrayMod.setConstantExpression( exp );
            arrayMod.setOffset( startOffset );
            exp.setParent( arrayMod );
            exp.setPropertyInParent( IASTArrayModifier.CONSTANT_EXPRESSION );
            arrayMods.add( arrayMod );
        }
    }

    /**
     * @return
     */
    protected ICASTArrayModifier createCArrayModifier() {
        return new CASTModifiedArrayModifier();
    }

    /**
     * @return
     */
    protected IASTArrayModifier createArrayModifier() {
        return new CASTArrayModifier();
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
    protected IASTNode forInitStatement() throws BacktrackException,
            EndOfFileException {
        IToken mark = mark();
        try {
            IASTExpression e = expression();
            consume(IToken.tSEMI);
            return e;
        } catch (BacktrackException bt) {
            backup(mark);
            try {
                return simpleDeclaration();
            } catch (BacktrackException b) {
                failParse(b);
                throwBacktrack(b);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#getTranslationUnit()
     */
    protected IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createCompoundStatement()
     */
    protected IASTCompoundStatement createCompoundStatement() {
        return new CASTCompoundStatement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createBinaryExpression()
     */
    protected IASTBinaryExpression createBinaryExpression() {
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
    protected IGNUASTCompoundStatementExpression createCompoundStatementExpression() {
        return new CASTCompoundStatementExpression();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.parser2.AbstractGNUSourceCodeParser#createExpressionList()
     */
    protected IASTExpressionList createExpressionList() {
        return new CASTExpressionList();
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