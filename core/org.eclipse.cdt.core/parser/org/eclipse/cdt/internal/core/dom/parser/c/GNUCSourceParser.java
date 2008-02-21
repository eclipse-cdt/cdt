/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    Mike Kucera (IBM) - bug #206952
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.parser.IExtensionToken;
import org.eclipse.cdt.core.dom.parser.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;

/**
 * @author jcamelon
 */
public class GNUCSourceParser extends AbstractGNUSourceCodeParser {

    private static class EmptyVisitor extends CASTVisitor {
        {
            shouldVisitStatements = true;
        }
    }
    
    private static final EmptyVisitor EMPTY_VISITOR = new EmptyVisitor();

    private final boolean supportGCCStyleDesignators;

	private IIndex index;

    /**
     * @param scanner
     * @param parserMode
     * @param logService
     */
    public GNUCSourceParser(IScanner scanner, ParserMode parserMode,
            IParserLogService logService, ICParserExtensionConfiguration config) {
    	this(scanner, parserMode, logService, config, null);
    }
    
    public GNUCSourceParser(IScanner scanner, ParserMode parserMode,
            IParserLogService logService, ICParserExtensionConfiguration config,
            IIndex index) {
        super(scanner, logService, parserMode, 
        		config.supportStatementsInExpressions(), 
        		config.supportTypeofUnaryExpressions(),
        		config.supportAlignOfUnaryExpression(),
        		config.supportKnRC(), 
        		config.supportAttributeSpecifiers(),
                config.supportDeclspecSpecifiers(),
                config.getBuiltinBindingsProvider());
        supportGCCStyleDesignators = config.supportGCCStyleDesignators();
        this.index= index;
    }

    protected IASTInitializer optionalCInitializer() throws EndOfFileException,
            BacktrackException {
        if (LT(1) == IToken.tASSIGN) {
            consume();
            final List<IASTNode> empty= Collections.emptyList();
            return cInitializerClause(empty);
        }
        return null;
    }

    /**
     * @param scope
     * @return
     */
    protected IASTInitializer cInitializerClause(List<IASTNode> designators)
            throws EndOfFileException, BacktrackException {
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        la = null;
        if (LT(1) == IToken.tLBRACE) {
            consume();
            IASTInitializerList result = createInitializerList();
            ((ASTNode) result).setOffset(startingOffset);
            
            // bug 196468, gcc accepts empty braces.
            if (supportGCCStyleDesignators && LT(1) == (IToken.tRBRACE)) {
                int l = consume().getEndOffset();
                ((ASTNode) result).setLength(l - startingOffset);
                return result;
            }

            for (;;) {
                int checkHashcode = LA(1).hashCode();
                // required at least one initializer list
                // get designator list
                List<IASTNode> newDesignators = designatorList();
                if (newDesignators.size() != 0)
                    if (LT(1) == IToken.tASSIGN)
                        consume();

                IASTInitializer initializer = cInitializerClause(newDesignators);

                if (newDesignators.isEmpty()) {
                    result.addInitializer(initializer);
                } else {
                    ICASTDesignatedInitializer desigInitializer = createDesignatorInitializer();
                    ((ASTNode) desigInitializer).setOffsetAndLength(
                            ((ASTNode) newDesignators.get(0)).getOffset(),
							((ASTNode)initializer).getOffset() + ((ASTNode)initializer).getLength() - ((ASTNode) newDesignators.get(0)).getOffset());
                    for (int i = 0; i < newDesignators.size(); ++i) {
                        ICASTDesignator d = (ICASTDesignator) newDesignators.get(i);
                        desigInitializer.addDesignator(d);
                    }
                    desigInitializer.setOperandInitializer(initializer);
                    result.addInitializer(desigInitializer);
                }
                // can end with just a '}'
                if (LT(1) == IToken.tRBRACE)
                    break;
                // can end with ", }"
                if (LT(1) == IToken.tCOMMA)
                    consume();
                if (LT(1) == IToken.tRBRACE)
                    break;
                if (checkHashcode == LA(1).hashCode()) {
                    IToken l2 = LA(1);
                    throwBacktrack(startingOffset, l2.getEndOffset()
                            - startingOffset);
                    return null;
                }

                // otherwise, its another initializer in the list
            }
            // consume the closing brace
            int lastOffset = consume(IToken.tRBRACE).getEndOffset();
            ((ASTNode) result).setLength(lastOffset - startingOffset);
            return result;
        }
        // if we get this far, it means that we have not yet succeeded
        // try this now instead
        // assignmentExpression
        try {
            IASTExpression assignmentExpression = assignmentExpression();
            IASTInitializerExpression result = createInitializerExpression();
            result.setExpression(assignmentExpression);
            ((ASTNode) result).setOffsetAndLength(
                    ((ASTNode) assignmentExpression).getOffset(),
                    ((ASTNode) assignmentExpression).getLength());
            return result;
        } catch (BacktrackException b) {
            throwBacktrack(b);
        }
        return null;
    }

    /**
     * @return
     */
    protected ICASTDesignatedInitializer createDesignatorInitializer() {
        return new CASTDesignatedInitializer();
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
        return new CASTInitializerExpression();
    }

    protected List<IASTNode> designatorList() throws EndOfFileException,
            BacktrackException {
        // designated initializers for C
        List<IASTNode> designatorList= Collections.emptyList();

        if (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
            while (LT(1) == IToken.tDOT || LT(1) == IToken.tLBRACKET) {
                if (LT(1) == IToken.tDOT) {
                    int offset = consume().getOffset();
                    IToken id = identifier();
                    ICASTFieldDesignator designator = createFieldDesignator();
                    ((ASTNode) designator).setOffsetAndLength(offset, id.getEndOffset() - offset);
                    IASTName n = createName(id);
                    designator.setName(n);
                    if (designatorList == Collections.EMPTY_LIST)
                        designatorList = new ArrayList<IASTNode>(DEFAULT_DESIGNATOR_LIST_SIZE);
                    designatorList.add(designator);
                } else if (LT(1) == IToken.tLBRACKET) {
                    IToken mark = consume();
                    int offset = mark.getOffset();
                    IASTExpression constantExpression = expression();
                    if (LT(1) == IToken.tRBRACKET) {
                        int lastOffset = consume().getEndOffset();
                        ICASTArrayDesignator designator = createArrayDesignator();
                        ((ASTNode) designator).setOffsetAndLength(offset, lastOffset - offset);
                        designator.setSubscriptExpression(constantExpression);
                        if (designatorList == Collections.EMPTY_LIST)
                            designatorList = new ArrayList<IASTNode>(DEFAULT_DESIGNATOR_LIST_SIZE);
                        designatorList.add(designator);
                        continue;
                    }
                    backup(mark);
                    if (supportGCCStyleDesignators) {
                        int startOffset = consume(IToken.tLBRACKET).getOffset();
                        IASTExpression constantExpression1 = expression();
                        consume(IToken.tELLIPSIS);
                        IASTExpression constantExpression2 = expression();
                        int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
                        IGCCASTArrayRangeDesignator designator = createArrayRangeDesignator();
                        ((ASTNode) designator).setOffsetAndLength(startOffset, lastOffset - startOffset);
                        designator.setRangeFloor(constantExpression1);
                        designator.setRangeCeiling(constantExpression2);
                        if (designatorList == Collections.EMPTY_LIST)
                            designatorList = new ArrayList<IASTNode>(DEFAULT_DESIGNATOR_LIST_SIZE);
                        designatorList.add(designator);
                    }
                } else if (supportGCCStyleDesignators
                        && LT(1) == IToken.tIDENTIFIER) {
                    IToken identifier = identifier();
                    int lastOffset = consume(IToken.tCOLON).getEndOffset();
                    ICASTFieldDesignator designator = createFieldDesignator();
                    ((ASTNode) designator).setOffsetAndLength(identifier
                            .getOffset(), lastOffset - identifier.getOffset());
                    IASTName n = createName(identifier);
                    designator.setName(n);
                    if (designatorList == Collections.EMPTY_LIST)
                        designatorList = new ArrayList<IASTNode>(DEFAULT_DESIGNATOR_LIST_SIZE);
                    designatorList.add(designator);
                }
            }
        } else {
            if (supportGCCStyleDesignators
                    && (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tLBRACKET)) {

                if (LT(1) == IToken.tIDENTIFIER) {
                	// fix for 84176: if reach identifier and it's not a designator then return empty designator list
                	if (LT(2) != IToken.tCOLON)
                		return designatorList;
                	
                    IToken identifier = identifier();
                    int lastOffset = consume(IToken.tCOLON).getEndOffset();
                    ICASTFieldDesignator designator = createFieldDesignator();
                    ((ASTNode) designator).setOffsetAndLength(identifier
                            .getOffset(), lastOffset - identifier.getOffset());
                    IASTName n = createName(identifier);
                    designator.setName(n);
                    if (designatorList == Collections.EMPTY_LIST)
                        designatorList = new ArrayList<IASTNode>(DEFAULT_DESIGNATOR_LIST_SIZE);
                    designatorList.add(designator);
                } else if (LT(1) == IToken.tLBRACKET) {
                    int startOffset = consume().getOffset();
                    IASTExpression constantExpression1 = expression();
                    consume(IToken.tELLIPSIS);
                    IASTExpression constantExpression2 = expression();
                    int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
                    IGCCASTArrayRangeDesignator designator = createArrayRangeDesignator();
                    ((ASTNode) designator).setOffsetAndLength(startOffset, lastOffset - startOffset);
                    designator.setRangeFloor(constantExpression1);
                    designator.setRangeCeiling(constantExpression2);
                    if (designatorList == Collections.EMPTY_LIST)
                        designatorList = new ArrayList<IASTNode>(DEFAULT_DESIGNATOR_LIST_SIZE);
                    designatorList.add(designator);
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

    @Override
	protected IASTDeclaration declaration() throws EndOfFileException,
            BacktrackException {
        switch (LT(1)) {
        case IToken.t_asm:
            return asmDeclaration();
        default:
            IASTDeclaration d = simpleDeclaration();
            return d;
        }

    }

    /**
     * @throws BacktrackException
     * @throws EndOfFileException
     */
    @Override
	protected IASTDeclaration simpleDeclaration() throws BacktrackException,
            EndOfFileException {
        IToken firstToken = LA(1);
        int firstOffset = firstToken.getOffset();
        if (firstToken.getType() == IToken.tLBRACE)
            throwBacktrack(firstToken.getOffset(), firstToken.getLength());

        firstToken = null; // necessary for scalability

        IASTDeclSpecifier declSpec;
        IASTDeclarator [] declarators = new IASTDeclarator[2];
        boolean skipAhead = false;
        try {
            declSpec = declSpecifierSeq(false, false);
        } catch (FoundDeclaratorException e) {
            skipAhead = true;
            declSpec = e.declSpec;
            declarators = (IASTDeclarator[]) ArrayUtil.append( IASTDeclarator.class, declarators, e.declarator );
            backup( e.currToken );
        }

        
        if (LT(1) != IToken.tSEMI) {
            if( ! skipAhead )
                declarators = (IASTDeclarator[]) ArrayUtil.append( IASTDeclarator.class, declarators, initDeclarator());

            while (LT(1) == IToken.tCOMMA) {
                consume();
                declarators = (IASTDeclarator[]) ArrayUtil.append( IASTDeclarator.class, declarators, initDeclarator());
            }
        }
        declarators = (IASTDeclarator[]) ArrayUtil.removeNulls( IASTDeclarator.class, declarators );

        boolean hasFunctionBody = false;
        boolean hasFunctionTryBlock = false;
        boolean consumedSemi = false;
        int semiOffset = 0;

        switch (LT(1)) {
        case IToken.tSEMI:
            semiOffset = consume().getEndOffset();
            consumedSemi = true;
            break;
        case IToken.tLBRACE:
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
        }

        if (!consumedSemi) {
            if (LT(1) == IToken.tLBRACE) {
                hasFunctionBody = true;
            }

            if (hasFunctionTryBlock && !hasFunctionBody)
                throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
        }

        if (hasFunctionBody) {
            if (declarators.length != 1)
                throwBacktrack(firstOffset, LA(1).getEndOffset());

            IASTDeclarator declarator = declarators[0];
            if (!(declarator instanceof IASTFunctionDeclarator))
                throwBacktrack(firstOffset, LA(1).getEndOffset());

            IASTFunctionDefinition funcDefinition = createFunctionDefinition();
            ((ASTNode) funcDefinition).setOffset(firstOffset);
            funcDefinition.setDeclSpecifier(declSpec);
            funcDefinition.setDeclarator((IASTFunctionDeclarator) declarator);

            IASTStatement s = handleFunctionBody();
            if (s != null) {
                funcDefinition.setBody(s);
            }
            ((ASTNode) funcDefinition).setLength(calculateEndOffset(s) - firstOffset);
            return funcDefinition;
        }

        IASTSimpleDeclaration simpleDeclaration = createSimpleDeclaration();

        int length = figureEndOffset(declSpec, declarators) - firstOffset;
        if (consumedSemi)
            length = semiOffset - firstOffset;
        ((ASTNode) simpleDeclaration).setOffsetAndLength(firstOffset, length);
        simpleDeclaration.setDeclSpecifier(declSpec);

        for (int i = 0; i < declarators.length; ++i) {
            IASTDeclarator declarator = declarators[i];
            simpleDeclaration.addDeclarator(declarator);
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
    @Override
	protected IASTSimpleDeclaration createSimpleDeclaration() {
        return new CASTSimpleDeclaration();
    }

    protected CASTTranslationUnit translationUnit;

    private boolean knr = false;

    private static final int DEFAULT_POINTEROPS_LIST_SIZE = 4;

    private static final int DEFAULT_PARAMETERS_LIST_SIZE = 4;

    protected CASTTranslationUnit createTranslationUnit() {
        CASTTranslationUnit t = new CASTTranslationUnit();
        t.setOffset(0);
        return t;
    }

    /**
     * This is the top-level entry point into the ANSI C++ grammar.
     * 
     * translationUnit : (declaration)*
     */
    @Override
	protected void translationUnit() {
        try {
            translationUnit = createTranslationUnit();
            translationUnit.setIndex(index);

			// add built-in names to the scope
			if (builtinBindingsProvider != null) {
				IScope tuScope = translationUnit.getScope();
				
				IBinding[] bindings = builtinBindingsProvider.getBuiltinBindings(tuScope);
				for(int i=0; i<bindings.length; i++) {
					ASTInternal.addBinding(tuScope, bindings[i]);
				}
			}
        } catch (Exception e2) {
            logException("translationUnit::createCompilationUnit()", e2); //$NON-NLS-1$
            return;
        }

        translationUnit.setLocationResolver(scanner.getLocationResolver());

        int lastBacktrack = -1;
        while (true) {
            try {
				if (LT(1) == IToken.tEOC)
					break;
                int checkOffset = LA(1).hashCode();
                IASTDeclaration d = declaration();
                translationUnit.addDeclaration(d);
                if (LA(1).hashCode() == checkOffset)
                    failParseWithErrorHandling();
            } catch (EndOfFileException e) {
                IASTDeclaration[] declarations = translationUnit.getDeclarations();
				// As expected
                if (declarations.length != 0) {
                    ASTNode d = (ASTNode) declarations[declarations.length-1];
                    ((ASTNode) translationUnit).setLength(d.getOffset() + d.getLength());
                } else
                    ((ASTNode) translationUnit).setLength(0);
                break;
            } catch (BacktrackException b) {
                try {
                    // Mark as failure and try to reach a recovery point
                    IASTProblem p = failParse(b);
                    IASTProblemDeclaration pd = createProblemDeclaration();
                    pd.setProblem(p);
                    ((ASTNode) pd).setOffsetAndLength(((ASTNode) p).getOffset(), ((ASTNode) p).getLength());
                    translationUnit.addDeclaration(pd);
                    errorHandling();
                    if (lastBacktrack != -1 && lastBacktrack == LA(1).hashCode()) {
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
                    // nothing
                }
            }
        }
    }

    /**
     * @return
     */
    protected IASTProblemDeclaration createProblemDeclaration() {
        return new CASTProblemDeclaration();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    @Override
	protected IASTExpression assignmentExpression() throws EndOfFileException,
            BacktrackException {
        if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tLBRACE && supportStatementsInExpressions) {
            IASTExpression resultExpression = compoundStatementExpression();
            if (resultExpression != null)
                return resultExpression;
        }

        IASTExpression conditionalExpression = conditionalExpression();
        // if the condition not taken, try assignment operators
        if (conditionalExpression != null
                && conditionalExpression instanceof IASTConditionalExpression) // &&
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
    @Override
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
    @Override
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
     * castExpression : unaryExpression | "(" typeId ")" castExpression
     */
    @Override
	protected IASTExpression castExpression() throws EndOfFileException,
            BacktrackException {
        // TO DO: we need proper symbol checkint to ensure type name
        if (LT(1) == IToken.tLPAREN) {
            IToken mark = mark();
            int startingOffset = mark.getOffset();
            consume();
            IASTTypeId typeId = null;
            IASTExpression castExpression = null;
            boolean proper=false;
            IToken startCastExpression=null;
            // If this isn't a type name, then we shouldn't be here
            boolean needBack = false;
            try {
                try {
                	if (!avoidCastExpressionByHeuristics()) {
                		typeId = typeId(false);
                	}
                    if (typeId != null) {
                    	switch (LT(1)) {
                    	case IToken.tRPAREN:
                    		consume();
                    		proper=true;
                    		startCastExpression=mark();
                    		castExpression = castExpression();
                    		break;
//                    	case IToken.tEOC:	// support for completion removed
//                    		break;			// in favour of another parse tree
                    	default:
                    		needBack = true;
//                    		throw backtrack;
                    	}
                    } else {needBack = true;}
                } catch (BacktrackException bte) {
                	needBack = true;
                }
                if (needBack) {
            	    try {
        	           	// try a compoundStatementExpression
    	        		backup(startCastExpression);
	                	if (typeId != null && proper && LT(1) == IToken.tLPAREN) {
	                		castExpression = compoundStatementExpression();
	            	        mark = null; // clean up mark so that we can garbage collect
	        	            return buildTypeIdUnaryExpression(IASTCastExpression.op_cast,
	    	                        typeId, castExpression, startingOffset,
		                            LT(1) == IToken.tEOC ? LA(1).getEndOffset() : calculateEndOffset(castExpression));
	                	}
  	                } catch (BacktrackException bte2) {}
                	
                    backup(mark);
                    return unaryExpression();
//                    throwBacktrack(bte);
                }

                return buildTypeIdUnaryExpression(IASTCastExpression.op_cast,
                        typeId, castExpression, startingOffset,
                        LT(1) == IToken.tEOC ? LA(1).getEndOffset() : calculateEndOffset(castExpression));
            } catch (BacktrackException b) {
            }
        }
        return unaryExpression();
    }

    /**
     * @param expression
     * @throws BacktrackException
     */
    @Override
	protected IASTExpression unaryExpression() throws EndOfFileException,
            BacktrackException {
        switch (LT(1)) {
        case IToken.tSTAR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_star);
        case IToken.tAMPER:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_amper);
        case IToken.tPLUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_plus);
        case IToken.tMINUS:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_minus);
        case IToken.tNOT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_not);
        case IToken.tBITCOMPLEMENT:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_tilde);
        case IToken.tINCR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixIncr);
        case IToken.tDECR:
            return unaryOperatorCastExpression(IASTUnaryExpression.op_prefixDecr);
        case IToken.t_sizeof:
        	return parseSizeofExpression();

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
     * @param typeId
     * @param startingOffset
     * @param op
     * @return
     */
    @Override
	protected IASTExpression buildTypeIdExpression(int op, IASTTypeId typeId,
            int startingOffset, int endingOffset) {
        IASTTypeIdExpression result = createTypeIdExpression();
        result.setOperator(op);
        ((ASTNode) result).setOffsetAndLength(startingOffset, endingOffset - startingOffset);
        ((ASTNode) result).setLength(endingOffset - startingOffset);
        result.setTypeId(typeId);
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
        	try {
        		int offset = consume().getOffset();
        		IASTTypeId t = typeId(false);
        		if (t != null) {
        			consume(IToken.tRPAREN).getEndOffset();
                	if (LT(1) == IToken.tLBRACE) {
        				final List<IASTNode> emptyList = Collections.emptyList();
						IASTInitializer i = cInitializerClause(emptyList);
        				firstExpression = buildTypeIdInitializerExpression(t, i, offset, calculateEndOffset(i));
        				break;        
                	}
        		}
        	} catch (BacktrackException bt) {
        	}
        	backup(m); 
        	firstExpression= primaryExpression();
        	break;
        	
        default:
            firstExpression = primaryExpression();
        	break;
        }

        IASTExpression secondExpression = null;
        for (;;) {
            switch (LT(1)) {
            case IToken.tLBRACKET:
                // array access
                consume();
                secondExpression = expression();
                int last;
				switch (LT(1)) {
				case IToken.tRBRACKET:
					last = consume().getEndOffset();
					break;
				case IToken.tEOC:
					last = Integer.MAX_VALUE;
					break;
				default:
					throw backtrack;
				}
				
                IASTArraySubscriptExpression s = createArraySubscriptExpression();
                ((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), last - ((ASTNode) firstExpression).getOffset());
                s.setArrayExpression(firstExpression);
                s.setSubscriptExpression(secondExpression);
                firstExpression = s;
                break;
            case IToken.tLPAREN:
                // function call
                consume();
                if (LT(1) != IToken.tRPAREN)
                    secondExpression = expression();
				if (LT(1) == IToken.tRPAREN)
					last = consume().getEndOffset();
				else
					// must be EOC
					last = Integer.MAX_VALUE;
                IASTFunctionCallExpression f = createFunctionCallExpression();
                ((ASTNode) f).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), last - ((ASTNode) firstExpression).getOffset());
                f.setFunctionNameExpression(firstExpression);

                if (secondExpression != null) {
                    f.setParameterExpression(secondExpression);
                }
                firstExpression = f;
                break;
            case IToken.tINCR:
                int offset = consume().getEndOffset();
                firstExpression = buildUnaryExpression(
                        IASTUnaryExpression.op_postFixIncr, firstExpression,
                        ((ASTNode) firstExpression).getOffset(), offset);
                break;
            case IToken.tDECR:
                offset = consume().getEndOffset();
                firstExpression = buildUnaryExpression(
                        IASTUnaryExpression.op_postFixDecr, firstExpression,
                        ((ASTNode) firstExpression).getOffset(), offset);
                break;
            case IToken.tDOT:
                // member access
                IToken dot = consume();
                IASTName name = createName(identifier());
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + dot.getLength());
                IASTFieldReference result = createFieldReference();
                ((ASTNode) result).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name)
                                - ((ASTNode) firstExpression).getOffset());
                result.setFieldOwner(firstExpression);
                result.setIsPointerDereference(false);
                result.setFieldName(name);
                firstExpression = result;
                break;
            case IToken.tARROW:
                // member access
                IToken arrow = consume();
                name = createName(identifier());
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + arrow.getLength());
                result = createFieldReference();
                ((ASTNode) result).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
                result.setFieldOwner(firstExpression);
                result.setIsPointerDereference(true);
                result.setFieldName(name);
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
    protected IASTFunctionCallExpression createFunctionCallExpression() {
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
     * @param lastOffset
     *            TODO
     * @return
     */
    protected ICASTTypeIdInitializerExpression buildTypeIdInitializerExpression(
            IASTTypeId t, IASTInitializer i, int offset, int lastOffset) {
        ICASTTypeIdInitializerExpression result = createTypeIdInitializerExpression();
        ((ASTNode) result).setOffsetAndLength(offset, lastOffset - offset);
        result.setTypeId(t);
        result.setInitializer(i);
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
            literalExpression.setKind(IASTLiteralExpression.lk_integer_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tFLOATINGPT:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_float_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tSTRING:
        case IToken.tLSTRING:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_string_literal);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tCHAR:
        case IToken.tLCHAR:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_char_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getLength());
            return literalExpression;
        case IToken.tLPAREN:
            t = consume();
            // TODO - do we need to return a wrapper?
            IASTExpression lhs = expression();
            int finalOffset = 0;
            switch (LT(1)) {
            case IToken.tRPAREN:
            case IToken.tEOC:
                finalOffset = consume().getEndOffset();
                break;
            default:
                throwBacktrack(LA(1));
            }
            
            return buildUnaryExpression(
                    IASTUnaryExpression.op_bracketedPrimary, lhs, t.getOffset(), finalOffset);
        case IToken.tIDENTIFIER:
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            int startingOffset = LA(1).getOffset();
            IToken t1 = identifier();
            IASTIdExpression idExpression = createIdExpression();
            IASTName name = createName(t1);
            idExpression.setName(name);
            ((ASTNode) idExpression).setOffsetAndLength((ASTNode) name);
            return idExpression;
        default:
            IToken la = LA(1);
            startingOffset = la.getOffset();
            throwBacktrack(startingOffset, la.getLength());
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
    @Override
	protected IASTIdExpression createIdExpression() {
        return new CASTIdExpression();
    }

    @Override
	protected IASTTypeId typeId(boolean forNewExpression) throws EndOfFileException {
    	if (!canBeTypeSpecifier()) {
    		return null;
    	}
        IToken mark = mark();
        int startingOffset = mark.getOffset();
        IASTDeclSpecifier declSpecifier = null;
        IASTDeclarator declarator = null;

        try {
            try
            {
                declSpecifier = declSpecifierSeq(false, true);
            } catch (FoundDeclaratorException  e) {
            	return null;
//                backup(mark);
//                throwBacktrack( e.currToken );
            }
            declarator = declarator();
        } catch (BacktrackException bt) {
        	return null;
//            backup(mark);
//            throwBacktrack(bt);
        }
        if (declarator == null || declarator.getName().toCharArray().length > 0) 
        {
        	return null;
//            backup(mark);
//            throwBacktrack(startingOffset, figureEndOffset(declSpecifier,
//                    declarator)
//                    - startingOffset);
        }

        IASTTypeId result = createTypeId();
        ((ASTNode) result).setOffsetAndLength(startingOffset, figureEndOffset(
                declSpecifier, declarator) - startingOffset);

        result.setDeclSpecifier(declSpecifier);
        result.setAbstractDeclarator(declarator);

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
    protected void consumePointerOperators(List<IASTPointerOperator> pointerOps)
            throws EndOfFileException, BacktrackException {
        for (;;) {
        	// __attribute__ in-between pointers
            __attribute_decl_seq(supportAttributeSpecifiers, false);

            IToken mark = mark();
            IToken last = null;

            boolean isConst = false, isVolatile = false, isRestrict = false;

            if (LT(1) != IToken.tSTAR) {
                backup(mark);
                break;
            }

            last = consume();
            int startOffset = mark.getOffset();
            for (;;) {
                IToken t = LA(1);
                switch (LT(1)) {
                case IToken.t_const:
                    last = consume();
                    isConst = true;
                    break;
                case IToken.t_volatile:
                    last = consume();
                    isVolatile = true;
                    break;
                case IToken.t_restrict:
                    last = consume();
                    isRestrict = true;
                    break;
                }

                if (t == LA(1))
                    break;
            }

            IASTPointerOperator po = createPointer();
            ((ASTNode) po).setOffsetAndLength(startOffset, last.getEndOffset() - startOffset);
            ((ICASTPointer) po).setConst(isConst);
            ((ICASTPointer) po).setVolatile(isVolatile);
            ((ICASTPointer) po).setRestrict(isRestrict);
            pointerOps.add(po);
        }
    }

    /**
     * @return
     */
    protected ICASTPointer createPointer() {
        return new CASTPointer();
    }

    protected IASTDeclSpecifier declSpecifierSeq(boolean parm, boolean forTypeId)
            throws BacktrackException, EndOfFileException, FoundDeclaratorException {
        Flags flags = new Flags(parm,forTypeId);

        int startingOffset = LA(1).getOffset();
        int storageClass = IASTDeclSpecifier.sc_unspecified;
        boolean isInline = false;
        boolean isConst = false, isRestrict = false, isVolatile = false;
        boolean isShort = false, isLong = false, isUnsigned = false, isIdentifier = false, isSigned = false, isLongLong = false;
        boolean isComplex = false, isImaginary = false;
        int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
        IToken identifier = null;
        IASTCompositeTypeSpecifier structSpec = null;
        IASTElaboratedTypeSpecifier elabSpec = null;
        IASTEnumerationSpecifier enumSpec = null;
        IASTExpression typeofExpression = null;
        IToken last = null;

        declSpecifiers: for (;;) {
            switch (LT(1)) {
            // Storage Class Specifiers
            case IToken.t_auto:
                last = consume();
                storageClass = IASTDeclSpecifier.sc_auto;
                break;
            case IToken.t_register:
                storageClass = IASTDeclSpecifier.sc_register;
                last = consume();
                break;
            case IToken.t_static:
                storageClass = IASTDeclSpecifier.sc_static;
                last = consume();
                break;
            case IToken.t_extern:
                storageClass = IASTDeclSpecifier.sc_extern;
                last = consume();
                break;
            case IToken.t_typedef:
                storageClass = IASTDeclSpecifier.sc_typedef;
                last = consume();
                break;

            // Function Specifier
            case IToken.t_inline:
                isInline = true;
                last = consume();
                break;

            // Type Qualifiers
            case IToken.t_const:
                isConst = true;
                last = consume();
                break;
            case IToken.t_volatile:
                isVolatile = true;
                last = consume();
                break;
            case IToken.t_restrict:
                isRestrict = true;
                last = consume();
                break;

            // Type Specifiers
            case IToken.t_void:
                flags.setEncounteredRawType(true);
                last = consume();
                simpleType = IASTSimpleDeclSpecifier.t_void;
                break;
            case IToken.t_char:
                flags.setEncounteredRawType(true);
                last = consume();
                simpleType = IASTSimpleDeclSpecifier.t_char;
                break;
            case IToken.t_short:
                flags.setEncounteredRawType(true);
                last = consume();
                isShort = true;
                break;
            case IToken.t_int:
                flags.setEncounteredRawType(true);
                last = consume();
                simpleType = IASTSimpleDeclSpecifier.t_int;
                break;
            case IToken.t_long:
                flags.setEncounteredRawType(true);
                last = consume();
                if (isLong) {
                    isLongLong = true;
                    isLong = false;
                } else
                    isLong = true;
                break;
            case IToken.t_float:
                flags.setEncounteredRawType(true);
                last = consume();
                simpleType = IASTSimpleDeclSpecifier.t_float;
                break;
            case IToken.t_double:
                flags.setEncounteredRawType(true);
                last = consume();
                simpleType = IASTSimpleDeclSpecifier.t_double;
                break;
            case IToken.t_signed:
                flags.setEncounteredRawType(true);
                last = consume();
                isSigned = true;
                break;
            case IToken.t_unsigned:
                flags.setEncounteredRawType(true);
                last = consume();
                isUnsigned = true;
                break;
            case IToken.t__Bool:
                flags.setEncounteredRawType(true);
                last = consume();
                simpleType = ICASTSimpleDeclSpecifier.t_Bool;
                break;
            case IToken.t__Complex:
                last = consume();
                isComplex=true;
                break;
            case IToken.t__Imaginary:
                last = consume();
                isImaginary=true;
                break;

            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
            case IToken.tEOC:
                // TODO - Kludgy way to handle constructors/destructors
                if (flags.haveEncounteredRawType()) {
                    break declSpecifiers;
                }
                if (flags.haveEncounteredTypename()) {
                    break declSpecifiers;
                }
                
                try {
                        lookAheadForDeclarator(flags);
                    } catch (FoundDeclaratorException e) {
                        ICASTSimpleDeclSpecifier declSpec = null;
                        if (typeofExpression != null) {
                            declSpec = createGCCSimpleTypeSpecifier();
                            ((IGCCASTSimpleDeclSpecifier) declSpec).setTypeofExpression(typeofExpression);
                        } else {
                            declSpec = createSimpleTypeSpecifier();
                        }
                        
                        declSpec.setConst(isConst);
                        declSpec.setRestrict(isRestrict);
                        declSpec.setVolatile(isVolatile);
                        declSpec.setInline(isInline);
                        declSpec.setStorageClass(storageClass);

                        declSpec.setType(simpleType);
                        declSpec.setLong(isLong);
                        declSpec.setLongLong(isLongLong);
                        declSpec.setUnsigned(isUnsigned);
                        declSpec.setSigned(isSigned);
                        declSpec.setShort(isShort);
                        if( typeofExpression != null && last == null ){
                            ((ASTNode)declSpec).setOffsetAndLength( (ASTNode)typeofExpression );
                        } else {
                            ((ASTNode) declSpec).setOffsetAndLength(startingOffset,
                                    (last != null) ? last.getEndOffset() - startingOffset : 0);
                        }
                        e.declSpec = declSpec;
                        throw e;
                    }
                identifier = identifier();
                last = identifier;
                isIdentifier = true;
                flags.setEncounteredTypename(true);
                break;
            case IToken.t_struct:
            case IToken.t_union:
                if (flags.haveEncounteredTypename())
                    throwBacktrack(LA(1));
                try {
                    structSpec = structOrUnionSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    elabSpec = elaboratedTypeSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                }
            case IToken.t_enum:
                if (flags.haveEncounteredTypename())
                    throwBacktrack(LA(1));
                try {
                    enumSpec = enumSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                } catch (BacktrackException bt) {
                    // this is an elaborated class specifier
                    elabSpec = elaboratedTypeSpecifier();
                    flags.setEncounteredTypename(true);
                    break;
                }
            case IGCCToken.t__attribute__: // if __attribute__ is after the declSpec
	            	if (supportAttributeSpecifiers)
	            		__attribute_decl_seq(true, false);
	            	else
	            		throwBacktrack(LA(1).getOffset(), LA(1).getLength());
            	break;
            case IGCCToken.t__declspec: // __declspec precedes the identifier
            	if (identifier == null && supportDeclspecSpecifiers)
            		__attribute_decl_seq(false, true);
            	else
            		throwBacktrack(LA(1).getOffset(), LA(1).getLength());
            	break;
            default:
            	if (LT(1) >= IExtensionToken.t__otherDeclSpecModifierFirst && LT(1) <= IExtensionToken.t__otherDeclSpecModifierLast) {
            		handleOtherDeclSpecModifier();
            		break;
            	}
            	if (supportTypeOfUnaries && LT(1) == IGCCToken.t_typeof) {
                    typeofExpression = unaryTypeofExpression();
                    if (typeofExpression != null) {
                        flags.setEncounteredTypename(true);
                    }
                }
                break declSpecifiers;
            }
        }

        if (structSpec != null) {
            ((ASTNode) structSpec).setOffsetAndLength(startingOffset,
                    calculateEndOffset(structSpec) - startingOffset);
            structSpec.setConst(isConst);
            ((ICASTCompositeTypeSpecifier) structSpec).setRestrict(isRestrict);
            structSpec.setVolatile(isVolatile);
            structSpec.setInline(isInline);
            structSpec.setStorageClass(storageClass);

            return structSpec;
        }

        if (enumSpec != null) {
            ((ASTNode) enumSpec).setOffsetAndLength(startingOffset,
                    calculateEndOffset(enumSpec) - startingOffset);
            enumSpec.setConst(isConst);
            ((CASTEnumerationSpecifier) enumSpec).setRestrict(isRestrict);
            enumSpec.setVolatile(isVolatile);
            enumSpec.setInline(isInline);
            enumSpec.setStorageClass(storageClass);
            return enumSpec;

        }
        if (elabSpec != null) {
            ((ASTNode) elabSpec).setOffsetAndLength(startingOffset,
                    calculateEndOffset(elabSpec) - startingOffset);
            elabSpec.setConst(isConst);
            ((CASTElaboratedTypeSpecifier) elabSpec).setRestrict(isRestrict);
            elabSpec.setVolatile(isVolatile);
            elabSpec.setInline(isInline);
            elabSpec.setStorageClass(storageClass);

            return elabSpec;
        }
        if (isIdentifier) {
            ICASTTypedefNameSpecifier declSpec = (ICASTTypedefNameSpecifier)createNamedTypeSpecifier();
            declSpec.setConst(isConst);
            declSpec.setRestrict(isRestrict);
            declSpec.setVolatile(isVolatile);
            declSpec.setInline(isInline);
            declSpec.setStorageClass(storageClass);

            ((ASTNode) declSpec).setOffsetAndLength(startingOffset, last.getEndOffset() - startingOffset);
            IASTName name = createName(identifier);
            declSpec.setName(name);
            return declSpec;
        }
        ICASTSimpleDeclSpecifier declSpec = null;
		if (typeofExpression != null) {
			declSpec = createGCCSimpleTypeSpecifier();
            ((IGCCASTSimpleDeclSpecifier) declSpec).setTypeofExpression(typeofExpression);
          
        } else {
			declSpec = createSimpleTypeSpecifier();
        }
		
        declSpec.setConst(isConst);
        declSpec.setRestrict(isRestrict);
        declSpec.setVolatile(isVolatile);
        declSpec.setInline(isInline);
        declSpec.setStorageClass(storageClass);

        declSpec.setType(simpleType);
        declSpec.setLong(isLong);
        declSpec.setLongLong(isLongLong);
        declSpec.setUnsigned(isUnsigned);
        declSpec.setSigned(isSigned);
        declSpec.setShort(isShort);
        declSpec.setComplex(isComplex);
        declSpec.setImaginary(isImaginary);
		if( typeofExpression != null && last == null ){
			((ASTNode)declSpec).setOffsetAndLength( (ASTNode)typeofExpression );
		} else {
	        ((ASTNode) declSpec).setOffsetAndLength(startingOffset,
	                (last != null) ? last.getEndOffset() - startingOffset : 0);
		}
        return declSpec;
    }

	/**
     * @return
     */
    protected ICASTSimpleDeclSpecifier createSimpleTypeSpecifier() {
        return new CASTSimpleDeclSpecifier();
    }

	protected IGCCASTSimpleDeclSpecifier createGCCSimpleTypeSpecifier() {
		return new GCCASTSimpleDeclSpecifier();
	}
	
    /**
     * @return
     */
    @Override
	protected IASTNamedTypeSpecifier createNamedTypeSpecifier() {
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
     * 
     * @return TODO
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICASTCompositeTypeSpecifier structOrUnionSpecifier()
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
            throwBacktrack(mark.getOffset(), mark.getLength());
        }

        // if __attribute__ or __declspec occurs after struct/union/class and before the identifier
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        IToken nameToken = null;
        // class name
        if (LT(1) == IToken.tIDENTIFIER) {
            nameToken = identifier();
        }

        // if __attribute__ or __declspec occurs after struct/union/class identifier and before the { or ;        
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        if (LT(1) != IToken.tLBRACE) {
            IToken errorPoint = LA(1);
            backup(mark);
            throwBacktrack(errorPoint.getOffset(), errorPoint.getLength());
        }

        consume();

        IASTName name = (nameToken == null) ? createName() : createName(nameToken);
        ICASTCompositeTypeSpecifier result = createCompositeTypeSpecifier();

        result.setKey(classKind);
        ((ASTNode) result).setOffset(classKey.getOffset());

        result.setName(name);

        int endOffset;
        memberDeclarationLoop: while (true) {
            switch (LT(1)) {
            case IToken.tRBRACE:
            case IToken.tEOC:
                endOffset = consume().getEndOffset();
                break memberDeclarationLoop;
            default:
                int checkToken = LA(1).hashCode();
                try {
                    IASTDeclaration d = declaration();
                    result.addMemberDeclaration(d);
                } catch (BacktrackException bt) {
                    if (checkToken == LA(1).hashCode())
                        failParseWithErrorHandling();
                }
                if (checkToken == LA(1).hashCode())
                    failParseWithErrorHandling();
            }
        }
        ((ASTNode) result).setLength(endOffset - classKey.getOffset());
        return result;
    }

    /**
     * @return
     */
    @Override
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
            throwBacktrack(t.getOffset(), t.getLength());
        }

        IToken identifier = identifier();
        IASTName name = createName(identifier);
        ICASTElaboratedTypeSpecifier result = createElaboratedTypeSpecifier();
        result.setName(name);
        result.setKind(eck);
        ((ASTNode) result).setOffsetAndLength(t.getOffset(), calculateEndOffset(name) - t.getOffset());
        return result;
    }

    /**
     * @return
     */
    protected ICASTElaboratedTypeSpecifier createElaboratedTypeSpecifier() {
        return new CASTElaboratedTypeSpecifier();
    }

    @Override
	protected IASTDeclarator initDeclarator() throws EndOfFileException, BacktrackException {
        IASTDeclarator d = declarator();

        IASTInitializer i = optionalCInitializer();
        if (i != null) {
            d.setInitializer(i);
            ((ASTNode) d).setLength(calculateEndOffset(i) - ((ASTNode) d).getOffset());
        }
        return d;
    }

    protected IASTDeclarator declarator() throws EndOfFileException, BacktrackException {
        IASTDeclarator innerDecl = null;
        IASTName declaratorName = null;
        IToken la = LA(1);
        int startingOffset = la.getOffset();
        int finalOffset = startingOffset;
        la = null;
        List<IASTPointerOperator> pointerOps = new ArrayList<IASTPointerOperator>(DEFAULT_POINTEROPS_LIST_SIZE);
        List<IASTNode> parameters = Collections.emptyList();
        List<IASTNode> arrayMods = Collections.emptyList();
        boolean encounteredVarArgs = false;
        IASTExpression bitField = null;
        boolean isFunction = false;
        IASTName[] parmNames = null;
        IASTDeclaration[] parmDeclarations = null;
        int numKnRCParms = 0;

        overallLoop: do {

            consumePointerOperators(pointerOps);
            
            // Accept __attribute__ or __declspec after the pointer ops and before the declarator ex: void * __attribute__((__cdecl__)) foo();
            __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
            
            if (!pointerOps.isEmpty()) {
                finalOffset = calculateEndOffset(pointerOps
                        .get(pointerOps.size() - 1));
            }

            if (LT(1) == IToken.tLPAREN) {
                consume();
                innerDecl = declarator();
                finalOffset = consume(IToken.tRPAREN).getEndOffset();
                declaratorName = createName();
            } else if (LT(1) == IToken.tIDENTIFIER) {
                declaratorName = createName(identifier());
                finalOffset = calculateEndOffset(declaratorName);
            } else
                declaratorName = createName();

            for (;;) {
                switch (LT(1)) {
                case IToken.tLPAREN:
                    // parameterDeclarationClause
                    // d.setIsFunction(true);
                    // TODO need to create a temporary scope object here
                    IToken last = consume();
                    finalOffset = last.getEndOffset();
                    isFunction = true;
                    boolean seenParameter = false;

                    // count the number of K&R C parameters (0 K&R C parameters
                    // essentially means it's not K&R C)
                    if( !knr && supportKnRC)
                        numKnRCParms = countKnRCParms();
					
                    if (numKnRCParms > 0) { // KnR C parameters were found so
                        // handle the declarator accordingly
                        parmNames = new IASTName[numKnRCParms];
                        parmDeclarations = new IASTDeclaration[numKnRCParms];

                        for (int i = 0; i <= parmNames.length; i++) {
                            switch (LT(1)) {
                            case IToken.tCOMMA:
                                last = consume();
                                seenParameter = false;
                            case IToken.tIDENTIFIER:
                                if (seenParameter)
                                    throwBacktrack(startingOffset, last.getEndOffset() - startingOffset);

                                parmNames[i] = createName(identifier());

                                seenParameter = true;
                                break;
                            case IToken.tRPAREN:
                                last = consume();
                                break;
                            default:
                                break;
                            }
                        }

                        // now that the parameter names are parsed, parse the
                        // parameter declarations
                        for (int i = 0; i < numKnRCParms
                                && LT(1) != IToken.tLBRACE; i++) { // max
                            // parameter
                            // declarations
                            // same as
                            // parameter
                            // name count
                            // (could be
                            // less)
                            try {
                                boolean hasValidDecltors = true;

                                IASTDeclaration decl = simpleDeclaration();
                                IASTSimpleDeclaration declaration = null;
                                if (decl instanceof IASTSimpleDeclaration) {
                                    declaration = ((IASTSimpleDeclaration) decl);

                                    IASTDeclarator[] decltors = declaration.getDeclarators();
                                    for (int k = 0; k < decltors.length; k++) {
                                        boolean decltorOk = false;
                                        for (int j = 0; j < parmNames.length; j++) {
                                            if (CharArrayUtils.equals(
                                                    decltors[k].getName().toCharArray(),
                                                    parmNames[j].toCharArray())) {
                                                decltorOk = true;
                                                break;
                                            }
                                        }
                                        if (!decltorOk)
                                            hasValidDecltors = false;
                                    }
                                } else {
                                    hasValidDecltors = false;
                                }

                                if (hasValidDecltors) {
                                    parmDeclarations[i] = declaration;
                                } else {
                                    parmDeclarations[i] = createKnRCProblemDeclaration(
                                            ((ASTNode) declaration).getLength(),
                                            ((ASTNode) declaration).getOffset());
                                }
                            } catch (BacktrackException b) {
                                parmDeclarations[i] = createKnRCProblemDeclaration(
                                        b.getLength(), b.getOffset());
                            }
                        }

                        break overallLoop;
                    }

                    parameterDeclarationLoop: for (;;) {
                        switch (LT(1)) {
                        case IToken.tRPAREN:
                        case IToken.tEOC:
                            last = consume();
                            finalOffset = last.getEndOffset();
                            break parameterDeclarationLoop;
                        case IToken.tELLIPSIS:
                            last = consume();
                            encounteredVarArgs = true;
                            finalOffset = last.getEndOffset();
                            break;
                        case IToken.tCOMMA:
                            last = consume();
                            finalOffset = last.getEndOffset();
                            seenParameter = false;
                            break;
                        default:
                            if (seenParameter)
                                throwBacktrack(startingOffset, last.getEndOffset() - startingOffset);
                            IASTParameterDeclaration pd = parameterDeclaration();
                            finalOffset = calculateEndOffset(pd);
                            if (parameters == Collections.EMPTY_LIST)
                                parameters = new ArrayList<IASTNode>(DEFAULT_PARAMETERS_LIST_SIZE);
                            parameters.add(pd);
                            seenParameter = true;
                        }
                    }

                    break;
                case IToken.tLBRACKET:
                    if (arrayMods == Collections.EMPTY_LIST)
                        arrayMods = new ArrayList<IASTNode>(DEFAULT_POINTEROPS_LIST_SIZE);
                    consumeArrayModifiers(arrayMods);
                    if (!arrayMods.isEmpty())
                        finalOffset = calculateEndOffset(arrayMods.get(arrayMods.size() - 1));
                    continue;
                case IToken.tCOLON:
                    consume();
                    bitField = constantExpression();
                    finalOffset = calculateEndOffset(bitField);
                    break;
                case IGCCToken.t__attribute__: // if __attribute__ is after the declarator
                	if(supportAttributeSpecifiers)
                		__attribute_decl_seq(true, false);
                	else
                		throwBacktrack(LA(1).getOffset(), LA(1).getLength());
                	break;
                case IGCCToken.t__declspec:
                	if(supportDeclspecSpecifiers)
                		__attribute_decl_seq(false, true);
                	else
                		throwBacktrack(LA(1).getOffset(), LA(1).getLength());
                	break;
                default:
                    break;
                }
                break;
            }

        } while (false);

        // Consume any number of __attribute__ and __declspec tokens after the parameters
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);

        IASTDeclarator d = null;
        if (numKnRCParms > 0 && supportKnRC) {
            ICASTKnRFunctionDeclarator functionDecltor = createKnRFunctionDeclarator();
            parmDeclarations = (IASTDeclaration[]) ArrayUtil.removeNulls( IASTDeclaration.class, parmDeclarations );
            for (int i = 0; i < parmDeclarations.length; ++i) {
                if (parmDeclarations[i] != null) {
                    finalOffset = calculateEndOffset(parmDeclarations[i]);
                }
            }
            functionDecltor.setParameterDeclarations(parmDeclarations);
            functionDecltor.setParameterNames(parmNames);
            if (declaratorName != null) {
                functionDecltor.setName(declaratorName);
            }

            d = functionDecltor;
        } else if (isFunction) {
            IASTStandardFunctionDeclarator fc = createFunctionDeclarator();
            fc.setVarArgs(encounteredVarArgs);
            for (int i = 0; i < parameters.size(); ++i) {
                IASTParameterDeclaration p = (IASTParameterDeclaration) parameters.get(i);
                fc.addParameterDeclaration(p);
            }
            d = fc;
        } else if (arrayMods != Collections.EMPTY_LIST) {
            d = createArrayDeclarator();
            for (int i = 0; i < arrayMods.size(); ++i) {
                IASTArrayModifier m = (IASTArrayModifier) arrayMods.get(i);
                ((IASTArrayDeclarator) d).addArrayModifier(m);
            }
        } else if (bitField != null) {
            IASTFieldDeclarator fl = createFieldDeclarator();
            fl.setBitFieldSize(bitField);
            d = fl;
        } else {
            d = createDeclarator();
        }
        for (int i = 0; i < pointerOps.size(); ++i) {
            IASTPointerOperator po = pointerOps.get(i);
            d.addPointerOperator(po);
        }
        if (innerDecl != null) {
            d.setNestedDeclarator(innerDecl);
        }
        if (declaratorName != null) {
            d.setName(declaratorName);
        }

        ((ASTNode) d).setOffsetAndLength(startingOffset, finalOffset - startingOffset);
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
    protected IASTStandardFunctionDeclarator createFunctionDeclarator() {
        return new CASTFunctionDeclarator();
    }

    /**
     * @return
     */
    protected ICASTKnRFunctionDeclarator createKnRFunctionDeclarator() {
        return new CASTKnRFunctionDeclarator();
    }

    /**
     * @param t
     * @return
     */
    @Override
	protected IASTName createName(IToken t) {
        IASTName n = new CASTName(t.getCharImage());
        switch (t.getType()) {
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            createCompletionNode(t).addName(n);
            break;
        }
        ((ASTNode) n).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
        return n;
    }

    /**
     * @return
     */
    protected IASTDeclarator createDeclarator() {
        return new CASTDeclarator();
    }

    protected void consumeArrayModifiers(List<IASTNode> arrayMods)
            throws EndOfFileException, BacktrackException {

        while (LT(1) == IToken.tLBRACKET) {
            // eat the '['
            int startOffset = consume().getOffset();

            boolean isStatic = false;
            boolean isConst = false;
            boolean isRestrict = false;
            boolean isVolatile = false;
            boolean isVarSized = false;

            outerLoop: do {
                switch (LT(1)) {
                case IToken.t_static:
                    isStatic = true;
                    consume();
                    break;
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
                case IToken.tSTAR:
                    isVarSized = true;
                    consume();
                // deliberate fall through
                default:
                    break outerLoop;
                }
            } while (true);

            IASTExpression exp = null;

            if (LT(1) != IToken.tRBRACKET) {
                if (!(isStatic || isRestrict || isConst || isVolatile))
                    exp = assignmentExpression();
                else
                    exp = constantExpression();
            }
            int lastOffset;
			switch (LT(1)) {
			case IToken.tRBRACKET:
				lastOffset = consume().getEndOffset();
				break;
			case IToken.tEOC:
				lastOffset = Integer.MAX_VALUE;
				break;
			default:
				throw backtrack;
			}


            IASTArrayModifier arrayMod = null;
            if (!(isStatic || isRestrict || isConst || isVolatile || isVarSized))
                arrayMod = createArrayModifier();
            else {
                ICASTArrayModifier temp = createCArrayModifier();
                temp.setStatic(isStatic);
                temp.setConst(isConst);
                temp.setVolatile(isVolatile);
                temp.setRestrict(isRestrict);
                temp.setVariableSized(isVarSized);
                arrayMod = temp;
            }
            ((ASTNode) arrayMod).setOffsetAndLength(startOffset, lastOffset - startOffset);
            if (exp != null) {
                arrayMod.setConstantExpression(exp);
            }
            arrayMods.add(arrayMod);
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
        
        IASTDeclSpecifier declSpec = null;
        try
        {
            declSpec = declSpecifierSeq(true, false);
        }
        catch( FoundDeclaratorException fd )
        {
            declSpec = fd.declSpec;
        }

        // bug 167833, no declspec no parameter.
        if (current == LA(1))
            throwBacktrack(current.getOffset(), current.getLength());

        IASTDeclarator declarator = null;
        if (LT(1) != IToken.tSEMI)
            declarator = initDeclarator();

        if (current == LA(1))
            throwBacktrack(current.getOffset(), figureEndOffset(declSpec,
                    declarator)
                    - current.getOffset());

        IASTParameterDeclaration result = createParameterDeclaration();
        ((ASTNode) result).setOffsetAndLength(startingOffset, figureEndOffset(declSpec, declarator) - startingOffset);
        result.setDeclSpecifier(declSpec);
        result.setDeclarator(declarator);
        return result;
    }

    protected IASTParameterDeclaration createParameterDeclaration() {
        return new CASTParameterDeclaration();
    }


    @Override
	protected IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }

    @Override
	protected IASTCompoundStatement createCompoundStatement() {
        return new CASTCompoundStatement();
    }

    @Override
	protected IASTBinaryExpression createBinaryExpression() {
        return new CASTBinaryExpression();
    }

    @Override
	protected IASTConditionalExpression createConditionalExpression() {
        return new CASTConditionalExpression();
    }

    @Override
	protected IASTUnaryExpression createUnaryExpression() {
        return new CASTUnaryExpression();
    }

    @Override
	protected IGNUASTCompoundStatementExpression createCompoundStatementExpression() {
        return new CASTCompoundStatementExpression();
    }

    @Override
	protected IASTExpressionList createExpressionList() {
        return new CASTExpressionList();
    }

    @Override
	protected IASTEnumerator createEnumerator() {
        return new CASTEnumerator();
    }

    @Override
	protected IASTLabelStatement createLabelStatement() {
        return new CASTLabelStatement();
    }

    @Override
	protected IASTGotoStatement createGoToStatement() {
        return new CASTGotoStatement();
    }

    @Override
	protected IASTReturnStatement createReturnStatement() {
        return new CASTReturnStatement();
    }

    protected IASTForStatement createForStatement() {
        return new CASTForStatement();
    }

    @Override
	protected IASTContinueStatement createContinueStatement() {
        return new CASTContinueStatement();
    }

    @Override
	protected IASTDoStatement createDoStatement() {
        return new CASTDoStatement();
    }

    @Override
	protected IASTBreakStatement createBreakStatement() {
        return new CASTBreakStatement();
    }

    @Override
	protected IASTWhileStatement createWhileStatement() {
        return new CASTWhileStatement();
    }

    @Override
	protected IASTNullStatement createNullStatement() {
        return new CASTNullStatement();
    }

    protected IASTSwitchStatement createSwitchStatement() {
        return new CASTSwitchStatement();
    }

    protected IASTIfStatement createIfStatement() {
        return new CASTIfStatement();
    }

    @Override
	protected IASTDefaultStatement createDefaultStatement() {
        return new CASTDefaultStatement();
    }

    @Override
	protected IASTCaseStatement createCaseStatement() {
        return new CASTCaseStatement();
    }

    @Override
	protected IASTExpressionStatement createExpressionStatement() {
        return new CASTExpressionStatement();
    }

    @Override
	protected IASTDeclarationStatement createDeclarationStatement() {
        return new CASTDeclarationStatement();
    }

    @Override
	protected IASTASMDeclaration createASMDirective() {
        return new CASTASMDeclaration();
    }

    @Override
	protected IASTEnumerationSpecifier createEnumerationSpecifier() {
        return new CASTEnumerationSpecifier();
    }

    @Override
	protected IASTCastExpression createCastExpression() {
        return new CASTCastExpression();
    }

    @Override
	protected IASTStatement statement() throws EndOfFileException,
            BacktrackException {
        switch (LT(1)) {
        // labeled statements
        case IToken.t_case:
            return parseCaseStatement();
        case IToken.t_default:
            return parseDefaultStatement();
        // compound statement
        case IToken.tLBRACE:
            return parseCompoundStatement();
        // selection statement
        case IToken.t_if:
            return parseIfStatement();
        case IToken.t_switch:
            return parseSwitchStatement();
        // iteration statements
        case IToken.t_while:
            return parseWhileStatement();
        case IToken.t_do:
            return parseDoStatement();
        case IToken.t_for:
            return parseForStatement();
        // jump statement
        case IToken.t_break:
            return parseBreakStatement();
        case IToken.t_continue:
            return parseContinueStatement();
        case IToken.t_return:
            return parseReturnStatement();
        case IToken.t_goto:
            return parseGotoStatement();
        case IToken.tSEMI:
            return parseNullStatement();
        default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
                return parseLabelStatement();
            }

            return parseDeclarationOrExpressionStatement();
        }

    }

    @Override
	protected void nullifyTranslationUnit() {
        translationUnit = null;
    }

    @Override
	protected IASTProblemStatement createProblemStatement() {
        return new CASTProblemStatement();
    }

    @Override
	protected IASTProblemExpression createProblemExpression() {
        return new CASTProblemExpression();
    }

    @Override
	protected IASTProblem createProblem(int signal, int offset, int length) {
        IASTProblem result = new CASTProblem(signal, EMPTY_STRING, true);
        ((ASTNode) result).setOffsetAndLength(offset, length);
        return result;
    }

    private int countKnRCParms() {
        IToken mark = null;
        int parmCount = 0;
        boolean previousWasIdentifier = false;

        try {
            knr = true;
            mark = mark();

            // starts at the beginning of the parameter list
            for (;;) {
                if (LT(1) == IToken.tCOMMA) {
                    consume();
                    previousWasIdentifier = false;
                } else if (LT(1) == IToken.tIDENTIFIER) {
                    consume();
                    if (previousWasIdentifier == true) {
                        backup(mark);
                        return 0; // i.e. KnR C won't have int f(typedef x)
                                    // char
                        // x; {}
                    }
                    previousWasIdentifier = true;
                    parmCount++;
                } else if (LT(1) == IToken.tRPAREN) {
                	if (!previousWasIdentifier) { 
                		// if the first token encountered is tRPAREN then it's not K&R C
                		// the first token when counting K&R C parms is always an identifier
                		backup(mark);
                		return 0;
                	}
                    consume();
                    break;
                } else {
                    backup(mark);
                    return 0; // i.e. KnR C won't have int f(char) char x; {}
                }
            }

            // if the next token is a tSEMI then the declaration was a regular
            // declaration statement i.e. int f(type_def);
            if (LT(1) == IToken.tSEMI) {
                backup(mark);
                return 0;
            }

            // look ahead for the start of the function body, if end of file is
            // found then return 0 parameters found (implies not KnR C)
            int previous=-1;
            int next=LA(1).hashCode();
            while (LT(1) != IToken.tLBRACE) {
            	// fix for 100104: check if the parameter declaration is a valid one
            	try {
            		simpleDeclaration();
				} catch (BacktrackException e) {
					backup(mark);
					return 0;
				}            	
            	
               	next = LA(1).hashCode();
               	if (next == previous) { // infinite loop detected
               		break;
               	}
               	previous = next;
            }

            backup(mark);
            return parmCount;
        } catch (EndOfFileException eof) {
            if (mark != null)
                backup(mark);

            return 0;
        }
        finally
        {
            knr = false;
        }
    }

    private IASTProblemDeclaration createKnRCProblemDeclaration(int length, int offset) throws EndOfFileException {
        IASTProblem p = createProblem(IProblem.SYNTAX_ERROR, offset, length);
        IASTProblemDeclaration pd = createProblemDeclaration();
        pd.setProblem(p);
        ((ASTNode) pd).setOffsetAndLength(((ASTNode) p).getOffset(), ((ASTNode) p).getLength());

        // consume until LBRACE is found (to leave off at the function body and
        // continue from there)
        IToken previous=null;
        IToken next=null;
        while (LT(1) != IToken.tLBRACE) {
           	next = consume();
           	if (next == previous) { // infinite loop detected
           		break;
           	}
           	previous = next;
        }

        return pd;
    }

    @Override
	protected ASTVisitor createVisitor() {
        return EMPTY_VISITOR;
    }

    @Override
	protected IASTAmbiguousStatement createAmbiguousStatement() {
        return new CASTAmbiguousStatement();
    }

    @Override
	protected IASTAmbiguousExpression createAmbiguousExpression() {
        return new CASTAmbiguousExpression();
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseIfStatement() throws EndOfFileException, BacktrackException {
        IASTIfStatement result = null;
        IASTIfStatement if_statement = null;
        int start = LA(1).getOffset();
        if_loop: while (true) {
            int so = consume(IToken.t_if).getOffset();
            consume(IToken.tLPAREN);
            IASTExpression condition = null;
            try {
                condition = condition();
    			if (LT(1) == IToken.tEOC) {
    				// Completing in the condition
    				IASTIfStatement new_if = createIfStatement();
    				new_if.setConditionExpression(condition);
    				
    				if (if_statement != null) {
                        if_statement.setElseClause(new_if);
    				}
    				return result != null ? result : new_if; 
    			}
                consume(IToken.tRPAREN);
            } catch (BacktrackException b) {
                IASTProblem p = failParse(b);
                IASTProblemExpression ps = createProblemExpression();
                ps.setProblem(p);
                ((ASTNode) ps).setOffsetAndLength(((ASTNode) p).getOffset(), ((ASTNode) p).getLength());
                condition = ps;
                if( LT(1) == IToken.tRPAREN )
                	consume();
                else if( LT(2) == IToken.tRPAREN )
                {
                	consume();
                	consume();
                }
                else
                	failParseWithErrorHandling();
            }
    
            IASTStatement thenClause = statement();
    
            IASTIfStatement new_if_statement = createIfStatement();
            ((ASTNode) new_if_statement).setOffset(so);
            if( condition != null ) // shouldn't be possible but failure in condition() makes it so
            {
                new_if_statement.setConditionExpression(condition);
            }
            if (thenClause != null) {
                new_if_statement.setThenClause(thenClause);
                ((ASTNode) new_if_statement).setLength(calculateEndOffset(thenClause)
                                - ((ASTNode) new_if_statement).getOffset());
            }
            if (LT(1) == IToken.t_else) {
                consume();
                if (LT(1) == IToken.t_if) {
                    // an else if, don't recurse, just loop and do another if
    
                    if (if_statement != null) {
                        if_statement.setElseClause(new_if_statement);
                        ((ASTNode) if_statement)
                                .setLength(calculateEndOffset(new_if_statement)
                                        - ((ASTNode) if_statement).getOffset());
                    }
                    if (result == null && if_statement != null)
                        result = if_statement;
                    if (result == null)
                        result = new_if_statement;
    
                    if_statement = new_if_statement;
                    continue if_loop;
                }
                IASTStatement elseStatement = statement();
                new_if_statement.setElseClause(elseStatement);
                if (if_statement != null) {
                    if_statement.setElseClause(new_if_statement);
                    ((ASTNode) if_statement)
                            .setLength(calculateEndOffset(new_if_statement)
                                    - ((ASTNode) if_statement).getOffset());
                } else {
                    if (result == null && if_statement != null)
                        result = if_statement;
                    if (result == null)
                        result = new_if_statement;
                    if_statement = new_if_statement;
                }
            } else {
            	if( thenClause != null )
                    ((ASTNode) new_if_statement)
                            .setLength(calculateEndOffset(thenClause) - start);
                if (if_statement != null) {
                    if_statement.setElseClause(new_if_statement);
                    ((ASTNode) new_if_statement)
                            .setLength(calculateEndOffset(new_if_statement) - start);
                }
                if (result == null && if_statement != null)
                    result = if_statement;
                if (result == null)
                    result = new_if_statement;
    
                if_statement = new_if_statement;
            }
            break if_loop;
        }
    
        reconcileLengths(result);
        return result;
    }

    protected IASTExpression unaryOperatorCastExpression(int operator) throws EndOfFileException, BacktrackException {
        IToken mark = mark();
        int offset = consume().getOffset();
        IASTExpression castExpression = castExpression();
        if( castExpression instanceof IASTLiteralExpression ) {
        	IASTLiteralExpression lit= (IASTLiteralExpression) castExpression;
        	if ( operator == IASTUnaryExpression.op_amper || 
        			(operator == IASTUnaryExpression.op_star && lit.getKind() != IASTLiteralExpression.lk_string_literal) )
        	{
        		backup( mark );
        		throwBacktrack( mark );
        	}
        }
        return buildUnaryExpression(operator, castExpression, offset,
                calculateEndOffset(castExpression));
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseSwitchStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTExpression switch_condition = condition();
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(LA(1));
        }
        IASTStatement  switch_body = null;
        if (LT(1) != IToken.tEOC)
        	switch_body = statement();
    
        IASTSwitchStatement switch_statement = createSwitchStatement();
        ((ASTNode) switch_statement).setOffsetAndLength(startOffset,
        		(switch_body != null ? calculateEndOffset(switch_body) : LA(1).getEndOffset()) - startOffset);
        switch_statement.setControllerExpression(switch_condition);
        
        if (switch_body != null) {
            switch_statement.setBody(switch_body);
        }

        return switch_statement;
    }

    /**
     * @return
     * @throws EndOfFileException
     * @throws BacktrackException
     */
    protected IASTStatement parseForStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset();
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
            consume();
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
            consume();
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
            ((ASTNode) for_statement).setOffsetAndLength(startOffset, calculateEndOffset(for_body) - startOffset);
        }
    
        for_statement.setInitializerStatement(init);
        
        if (for_condition != null) {
            for_statement.setConditionExpression(for_condition);
        }
        if (iterationExpression != null) {
            for_statement.setIterationExpression(iterationExpression);
        }
        if (for_body != null) {
            for_statement.setBody(for_body);
        }
        return for_statement;
    }
}
