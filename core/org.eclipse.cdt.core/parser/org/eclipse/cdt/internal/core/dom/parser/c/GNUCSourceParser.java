/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *    Mike Kucera (IBM) - bug #206952
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
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
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;

/**
 * Source parser for gnu-c syntax.
 */
public class GNUCSourceParser extends AbstractGNUSourceCodeParser {
    private static final int DEFAULT_POINTEROPS_LIST_SIZE = 4;
    private static final int DEFAULT_PARAMETERS_LIST_SIZE = 4;

    private final boolean supportGCCStyleDesignators;
	private IIndex index;
    protected IASTTranslationUnit translationUnit;

    private int fPreventKnrCheck= 0;
    
    private final ICNodeFactory nodeFactory;

    public GNUCSourceParser(IScanner scanner, ParserMode parserMode,
            IParserLogService logService, ICParserExtensionConfiguration config) {
    	this(scanner, parserMode, logService, config, null);
    }
    
    public GNUCSourceParser(IScanner scanner, ParserMode parserMode,
            IParserLogService logService, ICParserExtensionConfiguration config,
            IIndex index) {
        super(scanner, logService, parserMode, CNodeFactory.getDefault(),
        		config.supportStatementsInExpressions(), 
        		config.supportTypeofUnaryExpressions(),
        		config.supportAlignOfUnaryExpression(),
        		config.supportKnRC(), 
        		config.supportAttributeSpecifiers(),
                config.supportDeclspecSpecifiers(),
                config.getBuiltinBindingsProvider());
        supportGCCStyleDesignators = config.supportGCCStyleDesignators();
        supportParameterInfoBlock= config.supportParameterInfoBlock();
        supportExtendedSizeofOperator= config.supportExtendedSizeofOperator();
        supportFunctionStyleAsm= config.supportFunctionStyleAssembler();
        this.index= index;
        this.nodeFactory = CNodeFactory.getDefault();
    }

    @Override
	protected IASTInitializer optionalInitializer(DeclarationOptions options) throws EndOfFileException, BacktrackException {
        if (LTcatchEOF(1) == IToken.tASSIGN) {
            final int offset= consume().getOffset();
            IASTInitializerClause initClause = initClause(false);
            IASTEqualsInitializer result= nodeFactory.newEqualsInitializer(initClause);
            return setRange(result, offset, calculateEndOffset(initClause));
        }
        return null;
    }

    private IASTInitializerClause initClause(boolean inAggregate) throws EndOfFileException, BacktrackException {
        final int offset = LA(1).getOffset();
        if (LT(1) != IToken.tLBRACE) {
            IASTExpression assignmentExpression= expression(ExprKind.eAssignment);
            if (inAggregate && skipTrivialExpressionsInAggregateInitializers) {
            	if (!ASTQueries.canContainName(assignmentExpression))
            		return null;
            }
            return assignmentExpression;
        }
        
        // it's an aggregate initializer
        consume(IToken.tLBRACE);
        IASTInitializerList result = nodeFactory.newInitializerList();

        // bug 196468, gcc accepts empty braces.
        if (supportGCCStyleDesignators && LT(1) == IToken.tRBRACE) {
        	int endOffset= consume().getEndOffset();
        	setRange(result, offset, endOffset);
        	return result;
        }

        for (;;) {
        	final int checkOffset= LA(1).getOffset();
        	// required at least one initializer list
        	// get designator list
        	List<? extends ICASTDesignator> designator= designatorList();
        	if (designator == null) {
        		IASTInitializerClause clause= initClause(true);
        		// depending on value of skipTrivialItemsInCompoundInitializers initializer may be null
        		// in any way add the initializer such that the actual size can be tracked.
        		result.addClause(clause);
        	} else {
        		// Gnu extension: the assign operator is optional
        		if (LT(1) == IToken.tASSIGN)
        			consume(IToken.tASSIGN);
        		
        		IASTInitializerClause clause= initClause(false);
        		ICASTDesignatedInitializer desigInitializer = nodeFactory.newDesignatedInitializer(clause);
        		setRange(desigInitializer, designator.get(0));
        		adjustLength(desigInitializer, clause);

        		for (ICASTDesignator d : designator) {
        			desigInitializer.addDesignator(d);
        		}
        		result.addClause(desigInitializer);
        	}

        	// can end with ", }" or "}"
        	boolean canContinue= LT(1) == IToken.tCOMMA;
        	if (canContinue)
        		consume();
        	
        	switch (LT(1)) {
        	case IToken.tRBRACE:
        		int lastOffset = consume().getEndOffset();
        		setRange(result, offset, lastOffset);
        		return result;

        	case IToken.tEOC:
        		setRange(result, offset, LA(1).getOffset());
        		return result;
        	}
        	
        	if (!canContinue || LA(1).getOffset() == checkOffset) {
        		throwBacktrack(offset, LA(1).getEndOffset() - offset);
        	}
        }
        // consume the closing brace
    }


    private List<? extends ICASTDesignator> designatorList() throws EndOfFileException, BacktrackException {
    	final int lt1= LT(1);
        if (lt1 == IToken.tDOT || lt1 == IToken.tLBRACKET) {
            List<ICASTDesignator> designatorList= null;
            while (true) {
            	switch (LT(1)) {
            	case IToken.tDOT:
                    int offset = consume().getOffset();
                    IASTName n = identifier();
                    ICASTFieldDesignator fieldDesignator = nodeFactory.newFieldDesignator(n);
                	setRange(fieldDesignator, offset, calculateEndOffset(n));
                    if (designatorList == null)
                        designatorList = new ArrayList<ICASTDesignator>(DEFAULT_DESIGNATOR_LIST_SIZE);
                    designatorList.add(fieldDesignator);
                    break;
                    
            	case IToken.tLBRACKET:
                    offset = consume().getOffset();
                    IASTExpression constantExpression = expression();
                    if (supportGCCStyleDesignators && LT(1) == IToken.tELLIPSIS) {
                    	consume(IToken.tELLIPSIS);
                    	IASTExpression constantExpression2 = expression();
                    	int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
                    	IGCCASTArrayRangeDesignator designator = nodeFactory.newArrayRangeDesignatorGCC(constantExpression, constantExpression2);
                    	setRange(designator, offset, lastOffset);
                    	if (designatorList == null)
                    		designatorList = new ArrayList<ICASTDesignator>(DEFAULT_DESIGNATOR_LIST_SIZE);
                    	designatorList.add(designator);
                    } else {
                        int lastOffset = consume(IToken.tRBRACKET).getEndOffset();
                        ICASTArrayDesignator designator = nodeFactory.newArrayDesignator(constantExpression);
                    	setRange(designator, offset, lastOffset);
                        if (designatorList == null)
                            designatorList = new ArrayList<ICASTDesignator>(DEFAULT_DESIGNATOR_LIST_SIZE);
                        designatorList.add(designator);
                    }
                    break;
                    
                default:
                	return designatorList;
                }
            }
        } 
        
		// fix for 84176: if reach identifier and it's not a designator then return empty designator list
		if (supportGCCStyleDesignators && lt1 == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
			int offset= LA(1).getOffset();
			IASTName n = identifier();
			int lastOffset = consume(IToken.tCOLON).getEndOffset();
			ICASTFieldDesignator designator = nodeFactory.newFieldDesignator(n);
			setRange(designator, offset, lastOffset);
			return Collections.singletonList(designator);
		}
		
        return null;
    }
    
	@Override
	protected IASTDeclaration declaration(final DeclarationOptions declOption) throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.t_asm:
            return asmDeclaration();
        case IToken.tSEMI:
        	IToken semi= consume();
        	IASTDeclSpecifier declspec= nodeFactory.newSimpleDeclSpecifier();
        	IASTSimpleDeclaration decl= nodeFactory.newSimpleDeclaration(declspec);
        	decl.setDeclSpecifier(declspec);
        	((ASTNode) declspec).setOffsetAndLength(semi.getOffset(), 0);
        	((ASTNode) decl).setOffsetAndLength(semi.getOffset(), semi.getLength());
        	return decl;
        }

        return simpleDeclaration(declOption);
    }

	private IASTDeclaration simpleDeclaration(final DeclarationOptions declOption) throws BacktrackException, EndOfFileException {
        if (LT(1) == IToken.tLBRACE)
            throwBacktrack(LA(1));
        
        final int firstOffset= LA(1).getOffset();
        int endOffset= firstOffset;
        boolean insertSemi= false;

        IASTDeclSpecifier declSpec= null;
        IASTDeclarator dtor= null;
        IASTDeclSpecifier altDeclSpec= null;
        IASTDeclarator altDtor= null;
        IToken markBeforDtor= null;
        try {
        	Decl decl= declSpecifierSequence_initDeclarator(declOption, true);
        	markBeforDtor= decl.fDtorToken1;
        	declSpec= decl.fDeclSpec1;
        	dtor= decl.fDtor1;
        	altDeclSpec= decl.fDeclSpec2;
        	altDtor= decl.fDtor2;
        } catch (FoundAggregateInitializer lie) {
        	declSpec= lie.fDeclSpec;
        	// scalability: don't keep references to tokens, initializer may be large
        	declarationMark= null;
        	dtor= addInitializer(lie, declOption);
        } catch (BacktrackException e) {
        	IASTNode node= e.getNodeBeforeProblem();
        	if (node instanceof IASTDeclSpecifier) {
        		IASTSimpleDeclaration d= nodeFactory.newSimpleDeclaration((IASTDeclSpecifier) node);
        		setRange(d, node);
        		throwBacktrack(e.getProblem(), d);
        	}
        	throw e;
        }
        
        IASTDeclarator[] declarators= IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        if (dtor != null) {
        	declarators= new IASTDeclarator[]{dtor};
        	while (LTcatchEOF(1) == IToken.tCOMMA) {
        		consume();
        		try {
        			dtor= initDeclarator(declSpec, declOption);
        		} catch (FoundAggregateInitializer e) {
        	        // scalability: don't keep references to tokens, initializer may be large
        			declarationMark= null;
        			markBeforDtor= null;
        			dtor= addInitializer(e, declOption);
        		}
        		declarators= (IASTDeclarator[]) ArrayUtil.append( IASTDeclarator.class, declarators, dtor);
        	}
        	declarators= (IASTDeclarator[]) ArrayUtil.removeNulls( IASTDeclarator.class, declarators );
        }
        
        final int lt1= LTcatchEOF(1);
        switch (lt1) {
        case IToken.tEOC:
        	endOffset= figureEndOffset(declSpec, declarators);
        	break;
        case IToken.tSEMI:
        	endOffset= consume().getEndOffset();
        	break;
        case IToken.tLBRACE:
        	return functionDefinition(firstOffset, declSpec, declarators);

        default:
    		insertSemi= true;
        	if (declOption == DeclarationOptions.LOCAL) {
            	endOffset= figureEndOffset(declSpec, declarators);
        		if (firstOffset != endOffset) {
        			break;
        		}
        	} else {
        		if (markBeforDtor != null) {
        			endOffset= calculateEndOffset(declSpec);
        			if (firstOffset != endOffset && !isOnSameLine(endOffset, markBeforDtor.getOffset())) {
        				backup(markBeforDtor);
        				declarators= IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        				break;
        			}
        		}
        		endOffset= figureEndOffset(declSpec, declarators);
        		if (lt1 == 0) {
        			break;
        		}
        		if (firstOffset != endOffset) {
        			if (!isOnSameLine(endOffset, LA(1).getOffset())) {
        				break;
        			}
        			if (declarators.length == 1 && declarators[0] instanceof IASTFunctionDeclarator) {
        				break;
        			}
        		}
        	}
        	throwBacktrack(LA(1));
        }

        // no function body
        IASTSimpleDeclaration simpleDeclaration = nodeFactory.newSimpleDeclaration(declSpec);
        for (IASTDeclarator declarator : declarators)
            simpleDeclaration.addDeclarator(declarator);
        
    	setRange(simpleDeclaration, firstOffset, endOffset);
		if (altDeclSpec != null && altDtor != null) {
			simpleDeclaration = new CASTAmbiguousSimpleDeclaration(simpleDeclaration, altDeclSpec, altDtor);
			setRange(simpleDeclaration, firstOffset, endOffset);
		}
        
        if (insertSemi) {
    		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, endOffset, 0);
    		throwBacktrack(problem, simpleDeclaration);
        }
        return simpleDeclaration;
    }

	private IASTDeclaration functionDefinition(int firstOffset, IASTDeclSpecifier declSpec,
			IASTDeclarator[] declarators) throws BacktrackException, EndOfFileException {
		if (declarators.length != 1)
		    throwBacktrack(firstOffset, LA(1).getEndOffset());

		final IASTDeclarator outerDtor= declarators[0];
		final IASTDeclarator fdtor= ASTQueries.findTypeRelevantDeclarator(outerDtor);
		if (fdtor instanceof IASTFunctionDeclarator == false)
			throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

		IASTFunctionDefinition funcDefinition = nodeFactory.newFunctionDefinition(declSpec, (IASTFunctionDeclarator) fdtor, null);
		
		try {
			IASTStatement s= handleFunctionBody();
			funcDefinition.setBody(s);
			((ASTNode) funcDefinition).setOffsetAndLength(firstOffset, calculateEndOffset(s) - firstOffset);

			return funcDefinition;
		} catch (BacktrackException bt) {
			final IASTNode n= bt.getNodeBeforeProblem();
			if (n instanceof IASTCompoundStatement) {
				funcDefinition.setBody((IASTCompoundStatement) n);
				((ASTNode) funcDefinition).setOffsetAndLength(firstOffset, calculateEndOffset(n) - firstOffset);
				throwBacktrack(bt.getProblem(), funcDefinition);
			}
			throw bt;
		}
	}


	@Override
	protected void setupTranslationUnit() throws DOMException {
		translationUnit = nodeFactory.newTranslationUnit(scanner);
		translationUnit.setIndex(index);

		// add built-in names to the scope
		if (builtinBindingsProvider != null) {
			IScope tuScope = translationUnit.getScope();
			
			IBinding[] bindings = builtinBindingsProvider.getBuiltinBindings(tuScope);
			for (IBinding binding : bindings) {
				ASTInternal.addBinding(tuScope, binding);
			}
		}
	}
	
	@Override
	protected IASTExpression expression() throws BacktrackException, EndOfFileException {
		return expression(ExprKind.eExpression);
	}

	@Override
	protected IASTExpression constantExpression() throws BacktrackException, EndOfFileException {
    	return expression(ExprKind.eConstant);
    }

    private IASTExpression expression(final ExprKind kind) throws EndOfFileException, BacktrackException {
    	final boolean allowComma= kind==ExprKind.eExpression;
    	boolean allowAssignment= kind !=ExprKind.eConstant;
		int lt1;
		int conditionCount= 0;
		BinaryOperator lastOperator= null;
		IASTExpression lastExpression= castExpression(CastExprCtx.eBExpr);
		loop: while (true) {
			lt1= LT(1);
			switch(lt1) {
	        case IToken.tQUESTION:
				conditionCount++;
				// <logical-or> ? <expression> : <assignment-expression>
				// Precedence: 25 is lower than precedence of logical or; 0 is lower than precedence of expression
				lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 25, 0);  
				if (LT(2) ==  IToken.tCOLON) {
					// Gnu extension: The expression after '?' can be omitted.
					consume();				// Consume operator
					lastExpression= null; 	// Next cast expression is just null
					continue;
				} 
				allowAssignment= true;  // assignment expressions will be subsumed by the conditional expression
				break;
				
			case IToken.tCOLON:
				if (--conditionCount < 0) 
					break loop;
				
				// <logical-or> ? <expression> : <assignment-expression>
				// Precedence: 0 is lower than precedence of expression; 15 is lower than precedence of assignment; 
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 0, 15);  
				allowAssignment= true;  // assignment expressions will be subsumed by the conditional expression
	        	break;

			case IToken.tCOMMA:
				if (!allowComma && conditionCount == 0)
					break loop;
				// Lowest precedence except inside the conditional expression
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 10, 11);
	        	break;

	        case IToken.tASSIGN:
	        case IToken.tSTARASSIGN:
	        case IToken.tDIVASSIGN:
	        case IToken.tMODASSIGN:
	        case IToken.tPLUSASSIGN:
	        case IToken.tMINUSASSIGN:
	        case IToken.tSHIFTRASSIGN:
	        case IToken.tSHIFTLASSIGN:
	        case IToken.tAMPERASSIGN:
	        case IToken.tXORASSIGN:
	        case IToken.tBITORASSIGN:
				if (!allowAssignment && conditionCount == 0)
					break loop;
	        	// Assignments group right to left
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 21, 20); 
	        	break;
	        	
	        case IToken.tOR:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 30, 31); 
	        	break;
	        case IToken.tAND:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 40, 41);
	        	break;
	        case IToken.tBITOR:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 50, 51);
	        	break;
	        case IToken.tXOR:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 60, 61);
	        	break;
	        case IToken.tAMPER:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 70, 71);
	        	break;
	        case IToken.tEQUAL:
            case IToken.tNOTEQUAL:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 80, 81);
	        	break;
            case IToken.tGT:
            case IToken.tLT:
            case IToken.tLTEQUAL:
            case IToken.tGTEQUAL:
            case IGCCToken.tMAX:
            case IGCCToken.tMIN:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 90, 91);
	        	break;
            case IToken.tSHIFTL:
            case IToken.tSHIFTR:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 100, 101);
	        	break;
            case IToken.tPLUS:
            case IToken.tMINUS:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 110, 111);
	        	break;
            case IToken.tSTAR:
            case IToken.tDIV:
            case IToken.tMOD:
	        	lastOperator= new BinaryOperator(lastOperator, lastExpression, lt1, 120, 121);
	        	break;
            default:
            	break loop;
			}
	         
			consume(); 											// consume operator
			lastExpression= castExpression(CastExprCtx.eBExpr); 	// next cast expression
		}
		
    	// Check for incomplete conditional expression
    	if (lt1 != IToken.tEOC && conditionCount > 0)
    		throwBacktrack(LA(1));
    	
    	return buildExpression(lastOperator, lastExpression);
	}
    
    @Override
	protected IASTExpression buildBinaryExpression(int operator, IASTExpression expr1, IASTInitializerClause expr2, int lastOffset) {
        IASTBinaryExpression result = nodeFactory.newBinaryExpression(operator, expr1, (IASTExpression) expr2);
        int o = ((ASTNode) expr1).getOffset();
        ((ASTNode) result).setOffsetAndLength(o, lastOffset - o);
        return result;
    }

    @Override
	protected IASTExpression unaryExpression(CastExprCtx ctx) throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.tSTAR:
            return unaryExpression(IASTUnaryExpression.op_star, ctx);
        case IToken.tAMPER:
            return unaryExpression(IASTUnaryExpression.op_amper, ctx);
        case IToken.tPLUS:
            return unaryExpression(IASTUnaryExpression.op_plus, ctx);
        case IToken.tMINUS:
            return unaryExpression(IASTUnaryExpression.op_minus, ctx);
        case IToken.tNOT:
            return unaryExpression(IASTUnaryExpression.op_not, ctx);
        case IToken.tBITCOMPLEMENT:
            return unaryExpression(IASTUnaryExpression.op_tilde, ctx);
        case IToken.tINCR:
            return unaryExpression(IASTUnaryExpression.op_prefixIncr, ctx);
        case IToken.tDECR:
            return unaryExpression(IASTUnaryExpression.op_prefixDecr, ctx);
        case IToken.t_sizeof:
        	return parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(), 
        			IASTTypeIdExpression.op_sizeof, IASTUnaryExpression.op_sizeof, ctx);
        case IGCCToken.t___alignof__:
        	return parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(), 
        			IASTTypeIdExpression.op_alignof, IASTUnaryExpression.op_alignOf, ctx);
        default:
            return postfixExpression(ctx);
        }
    }

    protected IASTExpression postfixExpression(CastExprCtx ctx) throws EndOfFileException, BacktrackException {
        IASTExpression firstExpression = null;
        switch (LT(1)) {
        case IToken.tLPAREN:
            // ( type-name ) { initializer-list }
            // ( type-name ) { initializer-list , }
        	IToken m = mark();
        	try {
        		int offset = consume().getOffset();
        		IASTTypeId t= typeId(DeclarationOptions.TYPEID);
        		consume(IToken.tRPAREN);
        		if (LT(1) == IToken.tLBRACE) {
        			IASTInitializer i = (IASTInitializerList) initClause(false);
        			firstExpression= nodeFactory.newTypeIdInitializerExpression(t, i);
        			setRange(firstExpression, offset, calculateEndOffset(i));
        			break;        
        		}
        	} catch (BacktrackException bt) {
        	}
        	backup(m); 
        	firstExpression= primaryExpression(ctx);
        	break;
        	
        default:
            firstExpression = primaryExpression(ctx);
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
				
                IASTArraySubscriptExpression s = nodeFactory.newArraySubscriptExpression(firstExpression, secondExpression);
                ((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression).getOffset(),
                		last - ((ASTNode) firstExpression).getOffset());
                firstExpression = s;
                break;
            case IToken.tLPAREN:
                // function call
                int endOffset;
                List<IASTExpression> argList= null;
                consume(IToken.tLPAREN);
                boolean isFirst= true;
                while (true) {
                	final int lt1= LT(1);
                	if (lt1 == IToken.tRPAREN) {
                		endOffset= consume().getEndOffset();
                		break;
                	} else if (lt1 == IToken.tEOC) {
                		endOffset= LA(1).getEndOffset();
                		break;
                	} 
                	if (isFirst) {
                		isFirst= false;
                	} else {
                		consume(IToken.tCOMMA);
                	}
                	
                	IASTExpression expr= expression(ExprKind.eAssignment);
                	if (argList == null) {
                		argList= new ArrayList<IASTExpression>();
                	}
                	argList.add(expr);
                }
                
				final IASTExpression[] args;
				if (argList == null) { 
					args= IASTExpression.EMPTY_EXPRESSION_ARRAY;
				} else {
					args= argList.toArray(new IASTExpression[argList.size()]);
				}
				IASTFunctionCallExpression f = nodeFactory.newFunctionCallExpression(firstExpression, args);
                firstExpression = setRange(f, firstExpression, endOffset);
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
                IASTName name = identifier();
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + dot.getLength());
                IASTFieldReference result = nodeFactory.newFieldReference(name, firstExpression);
                result.setIsPointerDereference(false);
                ((ASTNode) result).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
                firstExpression = result;
                break;
            case IToken.tARROW:
                // member access
                IToken arrow = consume();
                name = identifier();
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + arrow.getLength());
                result = nodeFactory.newFieldReference(name, firstExpression);
                result.setIsPointerDereference(true);
                ((ASTNode) result).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
                firstExpression = result;
                break;
            default:
                return firstExpression;
            }
        }
    }

    @Override
	protected IASTExpression primaryExpression(CastExprCtx ctx) throws EndOfFileException, BacktrackException {
        IToken t = null;
        IASTLiteralExpression literalExpression = null;
        switch (LT(1)) {
        // TO DO: we need more literals...
        case IToken.tINTEGER:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tFLOATINGPT:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_float_constant, t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tSTRING:
        case IToken.tLSTRING:
        case IToken.tUTF16STRING:
        case IToken.tUTF32STRING:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tCHAR:
        case IToken.tLCHAR:
        case IToken.tUTF16CHAR:
        case IToken.tUTF32CHAR:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_char_constant, t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getLength());
            return literalExpression;
        case IToken.tLPAREN:
        	if (supportStatementsInExpressions && LT(2) == IToken.tLBRACE) {
        		return compoundStatementExpression();
        	}
            t = consume();
            IASTExpression lhs = expression(ExprKind.eExpression); // instead of expression(), to keep the stack smaller
            int finalOffset = 0;
            switch (LT(1)) {
            case IToken.tRPAREN:
            case IToken.tEOC:
                finalOffset = consume().getEndOffset();
                break;
            default:
                throwBacktrack(LA(1));
            }
            return buildUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, lhs, t.getOffset(), finalOffset);
        case IToken.tIDENTIFIER:
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            int startingOffset = LA(1).getOffset();
            IASTName name = identifier();
            IASTIdExpression idExpression = nodeFactory.newIdExpression(name);
            ((ASTNode) idExpression).setOffsetAndLength((ASTNode) name);
            return idExpression;
        default:
            IToken la = LA(1);
            startingOffset = la.getOffset();
            throwBacktrack(startingOffset, la.getLength());
            return null;
        }

    }


    @Override
	protected IASTTypeId typeId(DeclarationOptions option) throws EndOfFileException, BacktrackException {
    	if (!canBeTypeSpecifier()) {
    		return null;
    	}
    	final int offset = mark().getOffset();
        IASTDeclSpecifier declSpecifier = null;
        IASTDeclarator declarator = null;

    	fPreventKnrCheck++;
        try {
        	Decl decl= declSpecifierSequence_initDeclarator(option, false);
        	declSpecifier= decl.fDeclSpec1;
        	declarator= decl.fDtor1;
        } catch (FoundAggregateInitializer lie) {
        	// type-ids have not compound initializers
        	throwBacktrack(lie.fDeclarator);
        } finally {
        	fPreventKnrCheck--;
        }

        IASTTypeId result = nodeFactory.newTypeId(declSpecifier, declarator);
        setRange(result, offset, figureEndOffset(declSpecifier, declarator));
        return result;
    }

    /**
     * Parse a Pointer Operator.
     * 
     * ptrOperator : "*" (cvQualifier)* | "&" | ::? nestedNameSpecifier "*"
     * (cvQualifier)*
     * 
     * @throws BacktrackException to request a backtrack
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

            ICASTPointer po = nodeFactory.newPointer();
            ((ASTNode) po).setOffsetAndLength(startOffset, last.getEndOffset() - startOffset);
            po.setConst(isConst);
            po.setVolatile(isVolatile);
            po.setRestrict(isRestrict);
            pointerOps.add(po);
        }
    }


	private final static int INLINE=0x1, CONST=0x2, RESTRICT=0x4, VOLATILE=0x8, 
	    SHORT=0x10,	UNSIGNED= 0x20, SIGNED=0x40, COMPLEX=0x80, IMAGINARY=0x100;

    @Override
	protected Decl declSpecifierSeq(final DeclarationOptions declOption) throws BacktrackException, EndOfFileException {
        int storageClass= IASTDeclSpecifier.sc_unspecified;
        int simpleType= IASTSimpleDeclSpecifier.t_unspecified;
        int options= 0;
        int isLong= 0;

        IToken returnToken= null;
    	IASTDeclSpecifier result= null;
    	IASTDeclSpecifier altResult= null;
    	try {
    		IASTName identifier= null;
    		IASTExpression typeofExpression= null;
    		IASTProblem problem= null;
        
    		boolean encounteredRawType= false;
    		boolean encounteredTypename= false;

    		final int offset= LA(1).getOffset();
    		int endOffset= offset;

    		declSpecifiers: for (;;) {
    			final int lt1= LTcatchEOF(1);
    			switch (lt1) {
    			case 0: // eof
    				break declSpecifiers;
    				// storage class specifiers
    			case IToken.t_auto:
    				storageClass = IASTDeclSpecifier.sc_auto;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_register:
    				storageClass = IASTDeclSpecifier.sc_register;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_static:
    				storageClass = IASTDeclSpecifier.sc_static;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_extern:
    				storageClass = IASTDeclSpecifier.sc_extern;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_typedef:
    				storageClass = IASTDeclSpecifier.sc_typedef;
    				endOffset= consume().getEndOffset();
    				break;

    				// Function Specifier
    			case IToken.t_inline:
    				options |= INLINE;
    				endOffset= consume().getEndOffset();
    				break;

    				// Type Qualifiers
    			case IToken.t_const:
    				options |= CONST;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_volatile:
    				options |= VOLATILE;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_restrict:
    				options |= RESTRICT;
    				endOffset= consume().getEndOffset();
    				break;

    				// Type Specifiers
    			case IToken.t_void:
    				if (encounteredTypename)
    					break declSpecifiers;
    				simpleType = IASTSimpleDeclSpecifier.t_void;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_char:
    				if (encounteredTypename)
    					break declSpecifiers;
    				simpleType = IASTSimpleDeclSpecifier.t_char;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_short:
    				if (encounteredTypename)
    					break declSpecifiers;
    				options |= SHORT;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_int:
    				if (encounteredTypename)
    					break declSpecifiers;
    				simpleType = IASTSimpleDeclSpecifier.t_int;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_long:
    				if (encounteredTypename)
    					break declSpecifiers;
    				isLong++;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_float:
    				if (encounteredTypename)
    					break declSpecifiers;
    				simpleType = IASTSimpleDeclSpecifier.t_float;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_double:
    				if (encounteredTypename)
    					break declSpecifiers;
    				simpleType = IASTSimpleDeclSpecifier.t_double;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_signed:
    				if (encounteredTypename)
    					break declSpecifiers;
    				options |= SIGNED;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t_unsigned:
    				if (encounteredTypename)
    					break declSpecifiers;
    				options |= UNSIGNED;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t__Bool:
    				if (encounteredTypename)
    					break declSpecifiers;
    				simpleType = IASTSimpleDeclSpecifier.t_bool;
    				encounteredRawType= true;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t__Complex:
    				if (encounteredTypename)
    					break declSpecifiers;
    				options |= COMPLEX;
    				endOffset= consume().getEndOffset();
    				break;
    			case IToken.t__Imaginary:
    				if (encounteredTypename)
    					break declSpecifiers;
    				options |= IMAGINARY;
    				endOffset= consume().getEndOffset();
    				break;

    			case IToken.tIDENTIFIER:
    			case IToken.tCOMPLETION:
    			case IToken.tEOC:
    				if (encounteredTypename || encounteredRawType) 
    					break declSpecifiers;

    				if ((endOffset != offset || declOption.fAllowEmptySpecifier) && LT(1) != IToken.tCOMPLETION) {
    					altResult= buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset, endOffset);
    					returnToken= mark();
    				}

    				identifier = identifier();
    				endOffset= calculateEndOffset(identifier);
    				encounteredTypename= true;
    				break;
    			case IToken.t_struct:
    			case IToken.t_union:
    				if (encounteredTypename || encounteredRawType)
    					break declSpecifiers;
    				try {
    					result= structOrUnionSpecifier();
    				} catch (BacktrackException bt) {
    					result= elaboratedTypeSpecifier();
    				}
    				endOffset= calculateEndOffset(result);
    				encounteredTypename= true;
    				break;
    			case IToken.t_enum:
    				if (encounteredTypename || encounteredRawType)
    					break declSpecifiers;
    				try {
    					result= enumSpecifier();
    				} catch (BacktrackException bt) {
    					if (bt.getNodeBeforeProblem() instanceof IASTDeclSpecifier) {
    						result= (IASTDeclSpecifier) bt.getNodeBeforeProblem();
    						problem = bt.getProblem();
    						break declSpecifiers;
    					} else {
    						result= elaboratedTypeSpecifier();
    					}
    				}
    				endOffset= calculateEndOffset(result);
    				encounteredTypename= true;
    				break;

    			case IGCCToken.t__attribute__: // if __attribute__ is after the declSpec
	    			if (!supportAttributeSpecifiers)
	    				throwBacktrack(LA(1));
	    			__attribute_decl_seq(true, false);
	    			break;
    			case IGCCToken.t__declspec: // __declspec precedes the identifier
	    			if (identifier != null || !supportDeclspecSpecifiers)
	    				throwBacktrack(LA(1));
	    			__attribute_decl_seq(false, true);
	    			break;

    			case IGCCToken.t_typeof:
    				if (encounteredRawType || encounteredTypename)
    					throwBacktrack(LA(1));

    				simpleType= IASTSimpleDeclSpecifier.t_typeof;
    				consume(IGCCToken.t_typeof);
    				typeofExpression = parseTypeidInParenthesisOrUnaryExpression(false, LA(1).getOffset(), 
    						IGNUASTTypeIdExpression.op_typeof, -1, CastExprCtx.eNotBExpr);

    				encounteredTypename= true;
    				endOffset= calculateEndOffset(typeofExpression);
    				break;

    			default:
    				if (lt1 >= IExtensionToken.t__otherDeclSpecModifierFirst && lt1 <= IExtensionToken.t__otherDeclSpecModifierLast) {
    					handleOtherDeclSpecModifier();
    					endOffset= LA(1).getOffset();
    					break;
    				}
    				break declSpecifiers;
    			}
            
    			if (encounteredRawType && encounteredTypename)
    				throwBacktrack(LA(1));
    		}

    		// check for empty specification
    		if (!encounteredRawType && !encounteredTypename && LT(1) != IToken.tEOC && !declOption.fAllowEmptySpecifier) {
    			if (offset == endOffset) {
    				throwBacktrack(LA(1));
    			}
    		}
        
    		if (result != null) {
    			configureDeclSpec(result, storageClass, options);
    			if ((options & RESTRICT) != 0) {
    				if (result instanceof ICASTCompositeTypeSpecifier) {
    					((ICASTCompositeTypeSpecifier) result).setRestrict(true);
    				} else if (result instanceof CASTEnumerationSpecifier) {
    					((CASTEnumerationSpecifier) result).setRestrict(true);
    				} else if (result instanceof CASTElaboratedTypeSpecifier) {
    					((CASTElaboratedTypeSpecifier) result).setRestrict(true);
    				}
    			}
    			setRange(result, offset, endOffset);
    			if (problem != null)
    				throwBacktrack(problem, result);
    		} else if (identifier != null) { 
    			result= buildNamedTypeSpecifier(identifier, storageClass, options, offset, endOffset);
    		} else {
    			result= buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset, endOffset);
    		}
        } catch (BacktrackException e) {
        	if (returnToken != null) {
        		backup(returnToken);
        		result= altResult;
        		altResult= null;
        		returnToken= null;
        	} else {
        		throw e;
        	}
        }
        Decl target= new Decl();
        target.fDeclSpec1= result;
        target.fDeclSpec2= altResult;
        target.fDtorToken1= returnToken;
        return target;
    }

	private ICASTTypedefNameSpecifier buildNamedTypeSpecifier(IASTName name, int storageClass,
			int options, int offset, int endOffset) {
		ICASTTypedefNameSpecifier declSpec = nodeFactory.newTypedefNameSpecifier(name);
		configureDeclSpec(declSpec, storageClass, options);
		declSpec.setRestrict((options & RESTRICT) != 0);
        ((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
        return declSpec;
	}

	private ICASTSimpleDeclSpecifier buildSimpleDeclSpec(int storageClass, int simpleType,
			int options, int isLong, IASTExpression typeofExpression, int offset, int endOffset) {
		ICASTSimpleDeclSpecifier declSpec= nodeFactory.newSimpleDeclSpecifier();
		
    	configureDeclSpec(declSpec, storageClass, options);
		declSpec.setType(simpleType);
		declSpec.setLong(isLong == 1);
		declSpec.setLongLong(isLong > 1);
		declSpec.setRestrict((options & RESTRICT) != 0);
		declSpec.setUnsigned((options & UNSIGNED) != 0);
		declSpec.setSigned((options & SIGNED) != 0);
		declSpec.setShort((options & SHORT) != 0);
		declSpec.setComplex((options & COMPLEX) != 0);
		declSpec.setImaginary((options & IMAGINARY) != 0);
		if (typeofExpression != null) {
			declSpec.setDeclTypeExpression(typeofExpression);
			typeofExpression.setParent(declSpec);
		}

		((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
		return declSpec;
	}

	private void configureDeclSpec(IASTDeclSpecifier declSpec, int storageClass, int options) {
		declSpec.setStorageClass(storageClass);
		declSpec.setConst((options & CONST) != 0);
		declSpec.setVolatile((options & VOLATILE) != 0);
		declSpec.setInline((options & INLINE) != 0);
	}

    /**
     * Parse a class/struct/union definition.
     * 
     * classSpecifier : classKey name (baseClause)? "{" (memberSpecification)*
     * "}"
     * 
     * @throws BacktrackException to request a backtrack
     */
    protected ICASTCompositeTypeSpecifier structOrUnionSpecifier() throws BacktrackException, EndOfFileException {
        int classKind = 0;
        IToken mark= mark();
        final int offset= mark.getOffset();
        
        // class key
        switch (LT(1)) {
        case IToken.t_struct:
            consume();
            classKind = IASTCompositeTypeSpecifier.k_struct;
            break;
        case IToken.t_union:
            consume();
            classKind = IASTCompositeTypeSpecifier.k_union;
            break;
        default:
            throwBacktrack(LA(1));
        	return null; // line never reached, hint for the parser.
        }

        // if __attribute__ or __declspec occurs after struct/union/class and before the identifier
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        // class name
        IASTName name = null;
        if (LT(1) == IToken.tIDENTIFIER) {
            name = identifier();
        }

        // if __attribute__ or __declspec occurs after struct/union/class identifier and before the { or ;        
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        if (LT(1) != IToken.tLBRACE) {
            IToken errorPoint = LA(1);
            backup(mark);
            throwBacktrack(errorPoint);
        }

        if (name == null) {
        	name= nodeFactory.newName();
        }
        ICASTCompositeTypeSpecifier result = nodeFactory.newCompositeTypeSpecifier(classKind, name);
        declarationListInBraces(result, offset, DeclarationOptions.C_MEMBER);
        return result;
    }


    protected IASTElaboratedTypeSpecifier elaboratedTypeSpecifier() throws BacktrackException, EndOfFileException {
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
        
        // if __attribute__ or __declspec occurs after struct/union/class and before the identifier
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);

        IASTName name = identifier();
        IASTElaboratedTypeSpecifier result = nodeFactory.newElaboratedTypeSpecifier(eck, name);
        ((ASTNode) result).setOffsetAndLength(t.getOffset(), calculateEndOffset(name) - t.getOffset());
        return result;
    }


    @Override
	protected IASTDeclarator initDeclarator(IASTDeclSpecifier declspec, final DeclarationOptions option) 
    		throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
        IASTDeclarator d = declarator(declspec, option);

		final int lt1= LTcatchEOF(1);
		if (lt1 == IToken.tLBRACE) {
			if (!(ASTQueries.findTypeRelevantDeclarator(d) instanceof IASTFunctionDeclarator)) {
				throwBacktrack(LA(1));
			}
		}
		
        if (lt1 == IToken.tASSIGN && LT(2) == IToken.tLBRACE) 
        	throw new FoundAggregateInitializer(declspec, d);
       
        IASTInitializer i = optionalInitializer(option);
        if (i != null) {
            d.setInitializer(i);
            ((ASTNode) d).setLength(calculateEndOffset(i) - ((ASTNode) d).getOffset());
        }
        return d;
    }
    
    protected IASTDeclarator declarator(IASTDeclSpecifier declSpec, DeclarationOptions option) throws EndOfFileException, BacktrackException {
        final int startingOffset = LA(1).getOffset();
        int endOffset = startingOffset;

        List<IASTPointerOperator> pointerOps = new ArrayList<IASTPointerOperator>(DEFAULT_POINTEROPS_LIST_SIZE);
        consumePointerOperators(pointerOps);
        if (!pointerOps.isEmpty()) {
        	endOffset = calculateEndOffset(pointerOps.get(pointerOps.size() - 1));
        }
            
        // Accept __attribute__ or __declspec between pointer operators and declarator.
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        // Look for identifier or nested declarator
        final int lt1= LT(1);
        if (lt1 == IToken.tIDENTIFIER) {
        	if (option.fRequireAbstract)
        		throwBacktrack(LA(1));

        	final IASTName declaratorName = identifier();
        	endOffset= calculateEndOffset(declaratorName);
        	return declarator(pointerOps, declaratorName, null, startingOffset, endOffset, option);
        } 
        
        if (lt1 == IToken.tLPAREN) {
        	IASTDeclarator cand1= null;
        	IToken cand1End= null;
        	// try an abstract function declarator 
        	if (option.fAllowAbstract) {
        		final IToken mark= mark();
        		try {
        			cand1= declarator(pointerOps, nodeFactory.newName(), null, startingOffset, endOffset, option);
            		if (option.fRequireAbstract) 
            			return cand1;

            		cand1End= LA(1);
        		} catch (BacktrackException e) {
        		}
        		backup(mark);
        	}
        	// try a nested declarator
        	try {
        		consume();
        		if (LT(1) == IToken.tRPAREN)
        			throwBacktrack(LA(1));
        		
        		final IASTDeclarator nested= declarator(declSpec, option);
        		endOffset= consume(IToken.tRPAREN).getEndOffset();
        		final IASTDeclarator cand2= declarator(pointerOps, null, nested, startingOffset, endOffset, option);
        		if (cand1 == null || cand1End == null)
        			return cand2;
        		final IToken cand2End= LA(1);
        		if (cand1End == cand2End) {
        			CASTAmbiguousDeclarator result= new CASTAmbiguousDeclarator(cand1, cand2);
                    ((ASTNode) result).setOffsetAndLength((ASTNode) cand1);
                    return result;
        		}
        		// use the longer variant
        		if (cand1End.getOffset() < cand2End.getOffset()) 
        			return cand2;
        		
        	} catch (BacktrackException e) {
        		if (cand1 == null)
        			throw e;
        	}
    		backup(cand1End);
    		return cand1;
        }
        
        // try abstract declarator
        if (!option.fAllowAbstract) {
        	throwBacktrack(LA(1));
        }
        return declarator(pointerOps, nodeFactory.newName(), null, startingOffset, endOffset, option);
    }
        
	private IASTDeclarator declarator(final List<IASTPointerOperator> pointerOps,	
			final IASTName declaratorName,	final IASTDeclarator nestedDeclarator, 
			final int startingOffset, int endOffset, 
			final DeclarationOptions option) throws EndOfFileException, BacktrackException {
        IASTDeclarator result= null;
        int lt1;
        loop: while (true) {
        	lt1= LTcatchEOF(1);
        	switch (lt1) {
        	case IToken.tLPAREN:
        		result= functionDeclarator(isAbstract(declaratorName, nestedDeclarator) 
        				? DeclarationOptions.PARAMETER : DeclarationOptions.C_PARAMETER_NON_ABSTRACT);
        		setDeclaratorID(result, declaratorName, nestedDeclarator);
        		break loop;
        		
        	case IToken.tLBRACKET:
        		result= arrayDeclarator();
        		setDeclaratorID(result, declaratorName, nestedDeclarator);
        		break loop;
        		
        	case IToken.tCOLON:
        		if (!option.fAllowBitField)
        			throwBacktrack(LA(1));
        		
        		result= bitFieldDeclarator();
        		setDeclaratorID(result, declaratorName, nestedDeclarator);
        		break loop;
        		
        	case IGCCToken.t__attribute__: // if __attribute__ is after a declarator
        		if (!supportAttributeSpecifiers)
        			throwBacktrack(LA(1));
        		__attribute_decl_seq(true, supportDeclspecSpecifiers);
        		break;
        	case IGCCToken.t__declspec:
        		if (!supportDeclspecSpecifiers)
        			throwBacktrack(LA(1));
        		__attribute_decl_seq(supportAttributeSpecifiers, true);
        		break;
        	default:
        		break loop;
        	}
        }
        if (lt1 != 0)
        	__attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);

        if (result == null) {
        	result= nodeFactory.newDeclarator(null);
        	setDeclaratorID(result, declaratorName, nestedDeclarator);
        } else {
        	endOffset= calculateEndOffset(result);
        }

        if (lt1 != 0 && LT(1) == IToken.t_asm) { // asm labels bug 226121
    		consume();
    		endOffset= asmExpression(null).getEndOffset();

    		__attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
    	}

        for (IASTPointerOperator po : pointerOps) {
			result.addPointerOperator(po);
		}

        ((ASTNode) result).setOffsetAndLength(startingOffset, endOffset - startingOffset);
        return result;
    }

	private boolean isAbstract(IASTName declaratorName, IASTDeclarator nestedDeclarator) {
		nestedDeclarator= ASTQueries.findInnermostDeclarator(nestedDeclarator);
		if (nestedDeclarator != null) {
			declaratorName= nestedDeclarator.getName();
		}
		return declaratorName == null || declaratorName.toCharArray().length == 0;
	}

	private void setDeclaratorID(IASTDeclarator declarator, IASTName declaratorName, IASTDeclarator nestedDeclarator) {
		if (nestedDeclarator != null) { 
			declarator.setNestedDeclarator(nestedDeclarator);
			declarator.setName(nodeFactory.newName());
		} else {
			declarator.setName(declaratorName);
		}
	}

	private IASTDeclarator functionDeclarator(DeclarationOptions paramOption) throws EndOfFileException, BacktrackException {
		IToken last = consume(IToken.tLPAREN);
		int startOffset= last.getOffset();
		
		// check for K&R C parameters (0 means it's not K&R C)
		if (fPreventKnrCheck==0 && supportKnRC) {
			fPreventKnrCheck++;
			try {
				final int numKnRCParms = countKnRCParms();
				if (numKnRCParms > 0) { // KnR C parameters were found
					IASTName[] parmNames = new IASTName[numKnRCParms];
					IASTDeclaration[] parmDeclarations = new IASTDeclaration[numKnRCParms];

					boolean seenParameter= false;
					for (int i = 0; i <= parmNames.length; i++) {
						switch (LT(1)) {
						case IToken.tCOMMA:
							last = consume();
							parmNames[i] = identifier();
							seenParameter = true;
							break;
						case IToken.tIDENTIFIER:
							if (seenParameter)
								throwBacktrack(startOffset, last.getEndOffset() - startOffset);

							parmNames[i] = identifier();
							seenParameter = true;
							break;
						case IToken.tRPAREN:
							last = consume();
							break;
						default:
							break;
						}
					}

					// now that the parameter names are parsed, parse the parameter declarations
					// count for parameter declarations <= count for parameter names.
					int endOffset= last.getEndOffset();
					for (int i = 0; i < numKnRCParms && LT(1) != IToken.tLBRACE; i++) {
						try {
							IASTDeclaration decl= simpleDeclaration(DeclarationOptions.LOCAL);
							IASTSimpleDeclaration ok= checkKnrParameterDeclaration(decl, parmNames);
							if (ok != null) {
								parmDeclarations[i]= ok;
								endOffset= calculateEndOffset(ok);
							} else {
								final ASTNode node = (ASTNode) decl;
								parmDeclarations[i] = createKnRCProblemDeclaration(node.getOffset(), node.getLength());
								endOffset= calculateEndOffset(node);
							}
						} catch (BacktrackException b) {
							parmDeclarations[i] = createKnRCProblemDeclaration(b.getOffset(), b.getLength());
							endOffset= b.getOffset() + b.getLength();
						}
					}

					parmDeclarations = (IASTDeclaration[]) ArrayUtil.removeNulls( IASTDeclaration.class, parmDeclarations );
		            ICASTKnRFunctionDeclarator functionDecltor = nodeFactory.newKnRFunctionDeclarator(parmNames, parmDeclarations);
		            ((ASTNode) functionDecltor).setOffsetAndLength(startOffset, endOffset-startOffset);
		            return functionDecltor;
				}
			} finally {
				fPreventKnrCheck--;
			}
		}

		boolean seenParameter= false;
		boolean encounteredVarArgs= false;
		List<IASTParameterDeclaration> parameters= null;
		int endOffset= last.getEndOffset();
		
		paramLoop: while (true) {
			switch (LT(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				endOffset= consume().getEndOffset();
				break paramLoop;
			case IToken.tELLIPSIS:
				endOffset= consume().getEndOffset();
				encounteredVarArgs = true;
				break;
			case IToken.tCOMMA:
				endOffset= consume().getEndOffset();
				seenParameter = false;
				break;
			default:
				if (seenParameter)
					throwBacktrack(startOffset, endOffset - startOffset);
			
				IASTParameterDeclaration pd = parameterDeclaration(paramOption);
				endOffset = calculateEndOffset(pd);
				if (parameters == null)
					parameters = new ArrayList<IASTParameterDeclaration>(DEFAULT_PARAMETERS_LIST_SIZE);
				parameters.add(pd);
				seenParameter = true;
				break;
			}
		}
		IASTStandardFunctionDeclarator fc = nodeFactory.newFunctionDeclarator(null);
		fc.setVarArgs(encounteredVarArgs);
		if (parameters != null) {
			for (IASTParameterDeclaration pd : parameters) {
				fc.addParameterDeclaration(pd);
			}
		}
        ((ASTNode) fc).setOffsetAndLength(startOffset, endOffset-startOffset);
        return fc;
	}

	private IASTSimpleDeclaration checkKnrParameterDeclaration(IASTDeclaration decl, final IASTName[] parmNames) {
		if (decl instanceof IASTSimpleDeclaration == false) 
			return null;
		
		IASTSimpleDeclaration declaration= ((IASTSimpleDeclaration) decl);
		IASTDeclarator[] decltors = declaration.getDeclarators();
		for (IASTDeclarator decltor : decltors) {
			boolean decltorOk = false;
			final char[] nchars = decltor.getName().toCharArray();
			for (IASTName parmName : parmNames) {
				if (CharArrayUtils.equals(nchars,	parmName.toCharArray())) {
					decltorOk= true;
					break;
				}
			}
			if (!decltorOk)
				return null;
		}
		return declaration;
	}

	/**
	 * Parse an array declarator starting at the square bracket.
	 */
	private IASTArrayDeclarator arrayDeclarator() throws EndOfFileException, BacktrackException {
		ArrayList<IASTArrayModifier> arrayMods = new ArrayList<IASTArrayModifier>(DEFAULT_POINTEROPS_LIST_SIZE);
		int start= LA(1).getOffset();
		consumeArrayModifiers(arrayMods);
		if (arrayMods.isEmpty())
			throwBacktrack(LA(1));
		
		final int endOffset = calculateEndOffset(arrayMods.get(arrayMods.size() - 1));
		final IASTArrayDeclarator d = nodeFactory.newArrayDeclarator(null);
		for (IASTArrayModifier m : arrayMods) {
            d.addArrayModifier(m);
        }
		
		((ASTNode) d).setOffsetAndLength(start, endOffset-start);
		return d;
	}
	
	
	/**
	 * Parses for a bit field declarator starting with the colon
	 */
	private IASTFieldDeclarator bitFieldDeclarator() throws EndOfFileException, BacktrackException {
		int start= consume(IToken.tCOLON).getOffset();
		
		final IASTExpression bitField = constantExpression();
		final int endOffset = calculateEndOffset(bitField);
		
        IASTFieldDeclarator d = nodeFactory.newFieldDeclarator(null, bitField);
        d.setBitFieldSize(bitField);

        ((ASTNode) d).setOffsetAndLength(start, endOffset-start);
		return d;
	}
	
	
    @Override
	protected IASTName identifier() throws EndOfFileException, BacktrackException {
    	final IToken t= LA(1);
    	IASTName n;
    	switch (t.getType()) {
    	case IToken.tIDENTIFIER:
    		consume();
            n = nodeFactory.newName(t.getCharImage());
            break;
            
    	case IToken.tCOMPLETION:
    	case IToken.tEOC:
    		consume();
            n = nodeFactory.newName(t.getCharImage());
            createCompletionNode(t).addName(n);
    		return n;
    		
    	default:
    		throw backtrack;
    	}
    	
        setRange(n, t.getOffset(), t.getEndOffset());
        return n;
    }

	protected void consumeArrayModifiers(List<IASTArrayModifier> arrayMods) throws EndOfFileException, BacktrackException {
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
                    break outerLoop;
                default:
                    break outerLoop;
                }
            } while (true);

            IASTExpression exp = null;

            if (LT(1) != IToken.tRBRACKET) {
                if (!(isStatic || isRestrict || isConst || isVolatile))
                    exp = expression(ExprKind.eAssignment);
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

			ICASTArrayModifier arrayMod = nodeFactory.newArrayModifier(exp);
			arrayMod.setStatic(isStatic);
			arrayMod.setConst(isConst);
			arrayMod.setVolatile(isVolatile);
			arrayMod.setRestrict(isRestrict);
			arrayMod.setVariableSized(isVarSized);
            ((ASTNode) arrayMod).setOffsetAndLength(startOffset, lastOffset - startOffset);
            arrayMods.add(arrayMod);
        }
    }


    protected IASTParameterDeclaration parameterDeclaration(DeclarationOptions option) throws BacktrackException, EndOfFileException {
        final IToken current = LA(1);
        int startingOffset = current.getOffset();
        if (current.getType() == IToken.tLBRACKET && supportParameterInfoBlock) {
        	skipBrackets(IToken.tLBRACKET, IToken.tRBRACKET);
        }

        IASTDeclSpecifier declSpec = null;
        IASTDeclarator declarator = null;
        IASTDeclSpecifier altDeclSpec = null;
        IASTDeclarator altDeclarator = null;
        
        try {
        	fPreventKnrCheck++;
        	Decl decl= declSpecifierSequence_initDeclarator(option, false);
        	declSpec= decl.fDeclSpec1;
        	declarator= decl.fDtor1;
        	altDeclSpec= decl.fDeclSpec2;
        	altDeclarator= decl.fDtor2;
        } catch (FoundAggregateInitializer lie) {
        	declSpec= lie.fDeclSpec;
        	declarator= lie.fDeclarator;
        } finally {
        	fPreventKnrCheck--;
        }

        final int length = figureEndOffset(declSpec, declarator) - startingOffset;
        IASTParameterDeclaration result = nodeFactory.newParameterDeclaration(declSpec, declarator);
		((ASTNode) result).setOffsetAndLength(startingOffset, length);
        if (altDeclarator != null && altDeclSpec != null) {
            IASTParameterDeclaration alt = nodeFactory.newParameterDeclaration(altDeclSpec, altDeclarator);
    		((ASTNode) alt).setOffsetAndLength(startingOffset, length);
            // order is important, prefer variant with declspec over the one without
            result= new CASTAmbiguousParameterDeclaration(result, alt);
            ((ASTNode) result).setOffsetAndLength((ASTNode) alt);
        }
        return result;
    }


    @Override
	protected IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }


    @Override
	protected IASTStatement statement() throws EndOfFileException, BacktrackException {
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


    private int countKnRCParms() {
        IToken mark = null;
        int parmCount = 0;
        boolean previousWasIdentifier = false;

        try {
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
            final int lt1= LT(1);
            if (lt1 == IToken.tSEMI || lt1 == IToken.tLBRACE) {
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
            		simpleDeclaration(DeclarationOptions.LOCAL);
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
    }

    private IASTProblemDeclaration createKnRCProblemDeclaration(int offset, int length) throws EndOfFileException {
        IASTProblem p = createProblem(IProblem.SYNTAX_ERROR, offset, length);
        IASTProblemDeclaration pd = nodeFactory.newProblemDeclaration(p);
        ((ASTNode) pd).setOffsetAndLength((ASTNode) p);

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
	protected ASTVisitor createAmbiguityNodeVisitor() {
        return new CASTAmbiguityResolver();
    }

    @Override
	protected IASTAmbiguousStatement createAmbiguousStatement() {
        return new CASTAmbiguousStatement();
    }

    @Override
	protected IASTAmbiguousExpression createAmbiguousExpression() {
        return new CASTAmbiguousExpression();
    }
    
	@Override
	protected IASTAmbiguousExpression createAmbiguousBinaryVsCastExpression(IASTBinaryExpression binary, IASTCastExpression castExpr) {
		return new CASTAmbiguousBinaryVsCastExpression(binary, castExpr);
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousCastVsFunctionCallExpression(IASTCastExpression castExpr, IASTFunctionCallExpression funcCall) {
		return new CASTAmbiguousCastVsFunctionCallExpression(castExpr, funcCall);
	}

    protected IASTStatement parseIfStatement() throws EndOfFileException, BacktrackException {
        IASTIfStatement result = null;
        IASTIfStatement if_statement = null;
        int start = LA(1).getOffset();
        if_loop: while (true) {
            int so = consume(IToken.t_if).getOffset();
            consume(IToken.tLPAREN);
            // condition
            IASTExpression condition= condition(true);
            if (LT(1) == IToken.tEOC) {
            	// Completing in the condition
            	IASTIfStatement new_if = nodeFactory.newIfStatement(condition, null, null);

            	if (if_statement != null) {
            		if_statement.setElseClause(new_if);
            	}
            	return result != null ? result : new_if; 
            }
            consume(IToken.tRPAREN);
    
            IASTStatement thenClause = statement();
            IASTIfStatement new_if_statement = nodeFactory.newIfStatement(null, null, null);
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


    protected IASTStatement parseSwitchStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTExpression switch_condition = condition(true);
        switch (LT(1)) {
        case IToken.tRPAREN:
            consume();
            break;
        case IToken.tEOC:
            break;
        default:
            throwBacktrack(LA(1));
        }

        IASTStatement switch_body = parseSwitchBody();
        IASTSwitchStatement switch_statement = nodeFactory.newSwitchStatement(switch_condition, switch_body);
        ((ASTNode) switch_statement).setOffsetAndLength(startOffset,
        		(switch_body != null ? calculateEndOffset(switch_body) : LA(1).getEndOffset()) - startOffset);
        return switch_statement;
    }

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
            for_condition = condition(false);
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
        
        IASTForStatement for_statement = nodeFactory.newForStatement(init, for_condition, iterationExpression, null);
        if (LT(1) != IToken.tEOC) {
        	IASTStatement for_body = statement();
            ((ASTNode) for_statement).setOffsetAndLength(startOffset, calculateEndOffset(for_body) - startOffset);
            for_statement.setBody(for_body);
        }
        return for_statement;
    }
}
