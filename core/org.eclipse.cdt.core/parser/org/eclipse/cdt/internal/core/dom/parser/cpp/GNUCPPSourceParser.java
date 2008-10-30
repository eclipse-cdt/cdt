/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Camelon (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=151207
 *     Ed Swartz (Nokia)
 *     Mike Kucera (IBM)
 *     Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
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
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
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
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.parser.IExtensionToken;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.TemplateParameterManager;
import org.eclipse.cdt.internal.core.parser.token.BasicTokenDuple;
import org.eclipse.cdt.internal.core.parser.token.OperatorTokenDuple;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;

/**
 * This is our implementation of the IParser interface, serving as a parser for
 * GNU C and C++. From time to time we will make reference to the ANSI ISO
 * specifications.
 */
public class GNUCPPSourceParser extends AbstractGNUSourceCodeParser {
    private static final int DEFAULT_PARM_LIST_SIZE = 4;
    private static final int DEFAULT_POINTEROPS_LIST_SIZE = 4;
    private static final int DEFAULT_SIZE_EXCEPTIONS_LIST = 2;
    private static final int DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE = 4;
    private static final int DEFAULT_CATCH_HANDLER_LIST_SIZE= 4;
    private static final int DEFAULT_PARAMETER_LIST_SIZE= 4;
    private static final ASTVisitor EMPTY_VISITOR = new ASTVisitor() {};
    private static enum DtorStrategy {PREFER_FUNCTION, PREFER_NESTED}

    private final boolean allowCPPRestrict;
    private final boolean supportExtendedTemplateSyntax;
    private final boolean supportLongLong;

	private final IIndex index;
    protected CPPASTTranslationUnit translationUnit;

    private int templateCount = 0;
    private int functionBodyCount= 0;
	private int rejectLogicalOperatorInTemplateID= 0;
	private char[] currentClassName;

    public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
            IParserLogService log, ICPPParserExtensionConfiguration config) {
    	this(scanner, mode, log, config, null);
    }

    public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
            IParserLogService log, ICPPParserExtensionConfiguration config,
            IIndex index) {
        super(scanner, log, mode, 
        		config.supportStatementsInExpressions(),
                config.supportTypeofUnaryExpressions(), 
                config.supportAlignOfUnaryExpression(), 
                config.supportKnRC(),
                config.supportAttributeSpecifiers(), 
                config.supportDeclspecSpecifiers(),
                config.getBuiltinBindingsProvider());
        allowCPPRestrict = config.allowRestrictPointerOperators();
        supportExtendedTemplateSyntax = config.supportExtendedTemplateSyntax();
        supportLongLong = config.supportLongLongs();
        functionCallCanBeLValue= true;
        this.index= index;
    }

    /**
     * Identifies the first and last tokens that make up the template parameter list.
     * Used as part of parsing an idExpression().
     * 
     * @param previousLast Previous "last" token (returned if nothing was consumed)
     * @return Last consumed token, or <code>previousLast</code> if nothing was consumed
     * @throws BacktrackException request a backtrack
     */
    protected IToken consumeTemplateParameters(IToken previousLast) throws EndOfFileException, BacktrackException {
        final int offset = previousLast == null ? LA(1).getOffset() : previousLast.getOffset();
        IToken last = previousLast; // if there are no parameters then previousLast gets returned
        
        if (LT(1) == IToken.tLT) {
            last= consume();
            int nk= 0;
            int depth= 0;
            int angleDepth= 0;

            while(true) {
                last= consume();
                switch(last.getType()) {
                	case IToken.tLT:
                		if (nk == 0) {
                			angleDepth++;
                		}
                		break;
	                case IToken.tGT: 
	                	if (nk == 0) {
	                		if (--angleDepth < 0)
	                			return last;
	                	}
	                    break;
	                    
	                case IToken.tLBRACKET: 
	                	if (nk == 0) {
	                		nk= IToken.tLBRACKET;
	                	} else if (nk == IToken.tLBRACKET) {
	                		depth++;
	                	} 
	                	break;
	                	
	                case IToken.tRBRACKET: 
	                	if (nk == IToken.tLBRACKET) {
	                		if (--depth < 0) {
	                			nk= 0;
	                		}
	                	}
	                	break;
	                    
	                case IToken.tLPAREN: 
	                	if (nk == 0) {
	                		nk= IToken.tLPAREN;
	                	} else if (nk == IToken.tLPAREN) {
	                		depth++;
	                	} 
	                	break;
	                	
	                case IToken.tRPAREN: 
	                	if (nk == IToken.tLPAREN) {
	                		if (--depth < 0) {
	                			nk= 0;
	                		}
	                	}
	                	break;
	                case IToken.tSEMI:
	                case IToken.tLBRACE:
	                case IToken.tRBRACE:
	                	if (nk == 0) {
	                		throwBacktrack(offset, last.getOffset() - offset);
	                	}
	                	break;
                }
            }
        }
        return last;
    }
    
    protected List<IASTNode> templateArgumentList() throws EndOfFileException, BacktrackException {
    	IToken start = LA(1);
    	int startingOffset = start.getOffset();
    	int endOffset = 0;
    	start = null;
    	List<IASTNode> list = new ArrayList<IASTNode>();

    	boolean failed = false;

    	while (LT(1) != IToken.tGT && LT(1) != IToken.tEOC) {
    		IToken argStart = mark();
    		IASTTypeId typeId = typeId(DeclarationOptions.TYPEID); 

    		if(typeId != null && (LT(1)==IToken.tCOMMA || LT(1)==IToken.tGT || LT(1)==IToken.tEOC)) {
    			// potentially a type-id - check for id-expression ambiguity
    			IToken typeIdEnd= mark();

    			backup(argStart);
    			try {
    				IASTExpression expression = assignmentExpression();
    				if(expression instanceof IASTIdExpression) {
    					IASTIdExpression idExpression= (IASTIdExpression) expression;
    					if(idExpression.getName() instanceof ICPPASTTemplateId) {
    						/*
    						 * A template-id cannot be used in an id-expression as a template argument.
    						 * 
    						 * 5.1-11 A template-id shall be used as an unqualified-id only as specified in
    						 * 14.7.2, 14.7, and 14.5.4.
    						 */
    						throw backtrack;
    					}

    					if (mark() != typeIdEnd) 
    						throw backtrack;

    					ICPPASTAmbiguousTemplateArgument ambiguity= createAmbiguousTemplateArgument();
    					ambiguity.addTypeId(typeId);
    					ambiguity.addIdExpression(idExpression);
    					list.add(ambiguity);
    				} else {
    					// prefer the typeId at this stage
    					throw backtrack;
    				}
    			} catch (BacktrackException e) {
    				// no ambiguity - its a type-id
    				list.add(typeId);
    				backup(typeIdEnd);
    			}
    		} else {
    			// not a type-id - try as expression
    			backup(argStart);
    			try {
    				IASTExpression expression = assignmentExpression();
    				list.add(expression);
    			} catch (BacktrackException e) {
    				backup(argStart);
    			}
    		}

    		if (LT(1) == IToken.tCOMMA) {
    			consume();
    		} else if (LT(1) != IToken.tGT && LT(1) != IToken.tEOC) {
    			failed = true;
    			endOffset = LA(1).getEndOffset();
    			break;
    		}
    	}
    	if (failed)
    		throwBacktrack(startingOffset, endOffset - startingOffset);

    	return list;
    }

    /**
     * To disambiguate between logical expressions and template id's in some situations
     * we forbid the usage of the logical operators '&&' or '||' within template ids. 
     * @throws EndOfFileException
     * @since 5.0
     */
    protected final ITokenDuple nameWithoutLogicalOperatorInTemplateID() throws BacktrackException, EndOfFileException {
    	rejectLogicalOperatorInTemplateID++;
    	try {
    		return name();
    	}
    	finally {
    		rejectLogicalOperatorInTemplateID--;
    	}
    }
    /**
     * Parse a name. 
     * name  ::= ("::")? name2 ("::" name2)* 
     * name2 ::= IDENTIFER | template-id
     * 
     * @throws BacktrackException request a backtrack
     */
    protected ITokenDuple name() throws BacktrackException, EndOfFileException {

        TemplateParameterManager argumentList = TemplateParameterManager.getInstance();

        try {
            IToken first = LA(1);
            IToken last = null;
            IToken mark = mark();

            boolean hasTemplateId = false;

            if (LT(1) == IToken.tCOLONCOLON) {
                argumentList.addSegment(null);
                last = consume();
            }

            if (LT(1) == IToken.tBITCOMPLEMENT)
                consume();

            switch (LT(1)) {
            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
            case IToken.tEOC:
                last = consume();
                IToken templateLast = consumeTemplateArguments(last, argumentList);
                if (last != templateLast) {
                	last = templateLast;
                	hasTemplateId = true;
                }
                break;

            default:
                IToken l = LA(1);
                backup(mark);
                throwBacktrack(first.getOffset(), l.getEndOffset()
                        - first.getOffset());
            }

            while (LT(1) == IToken.tCOLONCOLON) {
                last = consume();

                if (LT(1) == IToken.t_template) {
                    consume();
                }

                if (LT(1) == IToken.tBITCOMPLEMENT)
                    consume();

                switch (LT(1)) {
                case IToken.t_operator:
                    IToken l = LA(1);
                    backup(mark);
                    throwBacktrack(first.getOffset(), l.getEndOffset() - first.getOffset());
                    break;
                case IToken.tIDENTIFIER:
                case IToken.tCOMPLETION:
                case IToken.tEOC:
                    last = consume();
                    last = consumeTemplateArguments(last, argumentList);
                    if (last.getType() == IToken.tGT || last.getType() == IToken.tEOC)
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

    @Override
	protected IASTExpression conditionalExpression() throws BacktrackException, EndOfFileException {
    	final IASTExpression expr= super.conditionalExpression();
    	if (onTopInTemplateArgs && rejectLogicalOperatorInTemplateID > 0) {
    		// bug 104706, don't allow usage of logical operators in template argument lists.
    		if (expr instanceof IASTConditionalExpression) {
				final ASTNode node = (ASTNode) expr;
				throwBacktrack(node.getOffset(), node.getLength());
    		}    			
    		else if (expr instanceof IASTBinaryExpression) {
    			IASTBinaryExpression bexpr= (IASTBinaryExpression) expr;
    			switch (bexpr.getOperator()) {
    			case IASTBinaryExpression.op_logicalAnd:
    			case IASTBinaryExpression.op_logicalOr:
    				final ASTNode node = (ASTNode) expr;
					throwBacktrack(node.getOffset(), node.getLength());
    			}
    		}
    	}
    	return expr;
    }

	protected IToken consumeTemplateArguments(IToken last, TemplateParameterManager argumentList) throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.tLT) {
            IToken secondMark = mark();
            consume();
        	final boolean wasOnTop= onTopInTemplateArgs;
        	onTopInTemplateArgs= true;
            try {
        		// bug 229062: content assist after '<' needs to prefer to backtrack here
        		if (rejectLogicalOperatorInTemplateID == 1) {
        			final int lt1= LT(1);
        			if (lt1 == IToken.tCOMPLETION || lt1 == IToken.tEOC) {
        				throw backtrack;
        			}
        		}

                List<IASTNode> list = templateArgumentList();
                switch(LT(1)) {
                case IToken.tGT: 
                	final int lt2= LT(2);
                	if (lt2 == IToken.tINTEGER || lt2 == IToken.tFLOATINGPT) {
                		throw backtrack;
                	}
                	break;
                case IToken.tEOC:
                	break;
                default:
                	throw backtrack;
                }
                argumentList.addSegment(list);
                last = consume();
            } catch (BacktrackException bt) {
                argumentList.addSegment(null);
                backup(secondMark);
            } finally {
            	onTopInTemplateArgs= wasOnTop;
            }
        } else {
            argumentList.addSegment(null);
        }
        return last;
    }

    
    
    protected IASTName operatorId(IToken originalToken, TemplateParameterManager templateArgs) throws BacktrackException, EndOfFileException {
        // we know this is an operator
        IToken operatorToken = consume();
        IToken toSend = null;
        IASTTypeId typeId = null;
        OverloadableOperator op = null;
        if (LA(1).isOperator() || LT(1) == IToken.tLPAREN || LT(1) == IToken.tLBRACKET) {
            if ((LT(1) == IToken.t_new || LT(1) == IToken.t_delete) && 
                 LT(2) == IToken.tLBRACKET && LT(3) == IToken.tRBRACKET) {
            	op = LT(1) == IToken.t_new ? OverloadableOperator.NEW_ARRAY : OverloadableOperator.DELETE_ARRAY;
            	consume(); // new or delete
                consume(); // lbracket
                toSend = consume(); // rbracket
                // vector new and delete operators
            } else if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tRPAREN) {
                // operator ()
                consume(); // "("
                toSend = consume(); // ")"
                op = OverloadableOperator.PAREN;
            } else if (LT(1) == IToken.tLBRACKET && LT(2) == IToken.tRBRACKET) {
                consume(); // "["
                toSend = consume(); // "]"
                op = OverloadableOperator.BRACKET;
            } else if (LA(1).isOperator()) {
            	toSend = consume();
            	op = OverloadableOperator.valueOf(toSend);
            }
            else
                throwBacktrack(operatorToken.getOffset(), 0); // toSend must be null
        } else {
            // must be a conversion function
            IToken t = LA(1);
            typeId= typeId(DeclarationOptions.TYPEID_CONVERSION);
            if (typeId == null) 
            	throwBacktrack(t);
            
            if (t != LA(1)) {
                while (t.getNext() != LA(1)) {
                    t = t.getNext();
                }
                toSend = t;
            }
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

            OperatorTokenDuple operatorDuple = new OperatorTokenDuple(duple, op);
            if (typeId != null) { // if it's a conversion operator
                operatorDuple.setConversionOperator(true);
                operatorDuple.setTypeId(typeId);
            }

            return createName(operatorDuple);
        } finally {
            if (grabbedNewInstance)
                TemplateParameterManager.returnInstance(templateArgs);
        }
    }

    /**
     * Parse a Pointer Operator. ptrOperator : "*" (cvQualifier)* | "&" | ::?
     * nestedNameSpecifier "*" (cvQualifier)*
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void consumePointerOperators(List<IASTPointerOperator> collection)
            throws EndOfFileException, BacktrackException {

        for (;;) {
        	// __attribute__ in-between pointers
            __attribute_decl_seq(supportAttributeSpecifiers, false);
        	
            if (LT(1) == IToken.tAMPER) {
//            	boolean isRestrict= false;
            	IToken lastToken= consume();
            	final int from= lastToken.getOffset();
            	if (allowCPPRestrict && LT(1) == IToken.t_restrict) {
//            		isRestrict= true;
            		lastToken= consume();
            	}
                ICPPASTReferenceOperator refOp = createReferenceOperator();
                ((ASTNode) refOp).setOffsetAndLength(from, lastToken.getEndOffset()-from);
                collection.add(refOp);
                return;
            }
            IToken last = null;
            IToken mark = mark();
            ITokenDuple nameDuple = null;
            boolean isConst = false, isVolatile = false, isRestrict = false;
            if (LT(1) == IToken.tIDENTIFIER || LT(1) == IToken.tCOLONCOLON) {
                try {
                    nameDuple = name();
                    if (nameDuple.length() == 1) {
                        backup(mark);
                        return;
                    }
                    if (nameDuple.getLastToken().getType() != IToken.tCOLONCOLON) {
                        backup(mark);
                        return;
                    }
                    last = nameDuple.getLastToken();
                } catch (BacktrackException bt) {
                    backup(mark);
                    return;
                }
            }
            if (LT(1) == IToken.tSTAR) {
                last = consume();
                int starOffset = last.getOffset();

                for (;;) {
                    IToken t = LA(1);
                    int startingOffset = LA(1).getOffset();
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
                        if (allowCPPRestrict) {
                            last = consume();
                            isRestrict = true;
                            break;
                        }
                        IToken la = LA(1);
                        throwBacktrack(startingOffset, la.getEndOffset() - startingOffset);

                    }
                    if (t == LA(1))
                        break;
                }

                IASTPointerOperator po = null;
                if (nameDuple != null) {
                    IASTName name = createName(nameDuple);
                    ICPPASTPointerToMember p2m = createPointerToMember(isRestrict);
                    ((ASTNode) p2m).setOffsetAndLength(nameDuple
                            .getFirstToken().getOffset(), last.getEndOffset()
                            - nameDuple.getFirstToken().getOffset());
                    p2m.setConst(isConst);
                    p2m.setVolatile(isVolatile);
                    p2m.setName(name);
                    if (isRestrict) {
                        IGPPASTPointerToMember newPo = (IGPPASTPointerToMember) p2m;
                        newPo.setRestrict(isRestrict);
                        p2m = newPo;
                    }
                    po = p2m;

                } else {
                    po = createPointer(isRestrict);
                    ((ASTNode) po).setOffsetAndLength(starOffset, last.getEndOffset() - starOffset);
                    ((IASTPointer) po).setConst(isConst);
                    ((IASTPointer) po).setVolatile(isVolatile);
                    if (isRestrict) {
                        IGPPASTPointer newPo = (IGPPASTPointer) po;
                        newPo.setRestrict(isRestrict);
                        po = newPo;
                    }
                }
                collection.add(po);
                continue;
            }

            backup(mark);
            return;
        }
    }

    protected ICPPASTPointerToMember createPointerToMember(boolean gnu) {
        if (gnu)
            return new GPPASTPointerToMember();
        return new CPPASTPointerToMember();
    }

    protected IASTPointerOperator createPointer(boolean gnu) {
        if (gnu)
            return new GPPASTPointer();
        return new CPPASTPointer();
    }

    protected ICPPASTReferenceOperator createReferenceOperator() {
        return new CPPASTReferenceOperator();
    }

    @Override
	protected IASTExpression expression() throws EndOfFileException, BacktrackException {
    	final boolean wasOnTop= onTopInTemplateArgs;
    	onTopInTemplateArgs= false;
    	try {
    		return super.expression();
    	} finally {
    		onTopInTemplateArgs= wasOnTop;
    	}
    }

    @Override
	protected IASTExpression constantExpression() throws EndOfFileException, BacktrackException {
    	final boolean wasOnTop= onTopInTemplateArgs;
    	onTopInTemplateArgs= false;
    	try {
    		return super.constantExpression();
    	} finally {
    		onTopInTemplateArgs= wasOnTop;
    	}
    }

    @Override
	protected IASTExpression assignmentExpression() throws EndOfFileException,
            BacktrackException {
        if (LT(1) == IToken.t_throw) {
            return throwExpression();
        }

//        if (LT(1) == IToken.tLPAREN && LT(2) == IToken.tLBRACE
//                && supportStatementsInExpressions) {
//            IASTExpression resultExpression = compoundStatementExpression();
//            if (resultExpression != null)
//                return resultExpression;
//        }

        IASTExpression conditionalExpression = conditionalExpression();
        // if the condition not taken, try assignment operators
        if (conditionalExpression != null
                && conditionalExpression instanceof IASTConditionalExpression) // &&
            return conditionalExpression;

        switch (LT(1)) {
        case IToken.tASSIGN:
            return assignmentOperatorExpression(IASTBinaryExpression.op_assign,
                    conditionalExpression);
        case IToken.tSTARASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_multiplyAssign,
                    conditionalExpression);
        case IToken.tDIVASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_divideAssign, conditionalExpression);
        case IToken.tMODASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_moduloAssign, conditionalExpression);
        case IToken.tPLUSASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_plusAssign, conditionalExpression);
        case IToken.tMINUSASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_minusAssign, conditionalExpression);
        case IToken.tSHIFTRASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_shiftRightAssign,
                    conditionalExpression);
        case IToken.tSHIFTLASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_shiftLeftAssign,
                    conditionalExpression);
        case IToken.tAMPERASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_binaryAndAssign,
                    conditionalExpression);
        case IToken.tXORASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_binaryXorAssign,
                    conditionalExpression);
        case IToken.tBITORASSIGN:
            return assignmentOperatorExpression(
                    IASTBinaryExpression.op_binaryOrAssign,
                    conditionalExpression);
        }
        return conditionalExpression;
    }

    protected IASTExpression throwExpression() throws EndOfFileException,
            BacktrackException {
        IToken throwToken = consume();
        IASTExpression throwExpression = null;
        try {
            throwExpression = expression();
        } catch (BacktrackException bte) {
        }
        int o = throwExpression != null ? calculateEndOffset(throwExpression)
                : throwToken.getEndOffset();
        return buildUnaryExpression(ICPPASTUnaryExpression.op_throw,
                throwExpression, throwToken.getOffset(), o); // fix for 95225
    }

    @Override
	protected IASTExpression pmExpression() throws EndOfFileException, BacktrackException {
        IASTExpression firstExpression = castExpression();
        for (;;) {
            switch (LT(1)) {
            case IToken.tDOTSTAR:
            case IToken.tARROWSTAR:
                IToken t = consume();
                IASTExpression secondExpression = castExpression();
                int operator = 0;
                switch (t.getType()) {
                case IToken.tDOTSTAR:
                    operator = ICPPASTBinaryExpression.op_pmdot;
                    break;
                case IToken.tARROWSTAR:
                    operator = ICPPASTBinaryExpression.op_pmarrow;
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

    @Override
	protected IASTTypeId typeId(DeclarationOptions option) throws EndOfFileException {
    	if (!canBeTypeSpecifier()) {
    		return null;
    	}
        IToken mark = mark();
        int startingOffset = mark.getOffset();
        IASTDeclSpecifier declSpecifier = null;
        IASTDeclarator declarator = null;
        try {
        	declSpecifier = declSpecifierSeq(option);
            if (LT(1) != IToken.tEOC) {
                declarator= declarator(DtorStrategy.PREFER_FUNCTION, option);
            }
        } catch (FoundDeclaratorException e) {
        	declSpecifier= e.declSpec;
        	declarator= e.declarator;
        	backup(e.currToken);
        } catch (BacktrackException bt) {
        	return null;
        }
        IASTTypeId result = createTypeId();
        ((ASTNode) result).setOffsetAndLength(startingOffset, figureEndOffset(
                declSpecifier, declarator) - startingOffset);

        result.setDeclSpecifier(declSpecifier);
        result.setAbstractDeclarator(declarator);
        return result;

    }

    protected IASTTypeId createTypeId() {
        return new CPPASTTypeId();
    }

    protected IASTExpression deleteExpression() throws EndOfFileException,
            BacktrackException {
        int startingOffset = LA(1).getOffset();
        boolean global = false;
        if (LT(1) == IToken.tCOLONCOLON) {
            // global scope
            consume();
            global = true;
        }

        consume(IToken.t_delete);

        boolean vectored = false;
        if (LT(1) == IToken.tLBRACKET) {
            // array delete
            consume();
            consume(IToken.tRBRACKET);
            vectored = true;
        }
        IASTExpression castExpression = castExpression();
        ICPPASTDeleteExpression deleteExpression = createDeleteExpression();
        ((ASTNode) deleteExpression).setOffsetAndLength(startingOffset,
                calculateEndOffset(castExpression) - startingOffset);
        deleteExpression.setIsGlobal(global);
        deleteExpression.setIsVectored(vectored);
        deleteExpression.setOperand(castExpression);
        return deleteExpression;
    }

    protected ICPPASTDeleteExpression createDeleteExpression() {
        return new CPPASTDeleteExpression();
    }

    /**
     * Parse a new-expression. There is room for ambiguities. With P for placement, T for typeid,
     * and I for initializer the potential patterns (with the new omitted) are:
     * easy: 	T, T(I)
     * medium: 	(P) T(I), (P) (T)(I)
     * hard:    (T), (P) T, (P) T, (P) (T), (T)(I)
     */
    protected IASTExpression newExpression() throws BacktrackException, EndOfFileException {
        IToken la = LA(1);
        int offset= la.getOffset();

        final boolean isGlobal = la.getType() == IToken.tCOLONCOLON;
        if (isGlobal) {
            consume();
        }
        consume(IToken.t_new);
        if (LT(1) == IToken.tLPAREN) {
    		consume();

    		// consider placement first (P) ...
            IASTExpression plcmt= null;
            IASTTypeId     typeid= null;
            boolean isNewTypeId= true;
            IASTExpression init= null;
            int endOffset= 0;
        	IToken mark= mark();
        	IToken end= null;
        	try {
        		plcmt= expression();
        		endOffset= consumeOrEOC(IToken.tRPAREN).getEndOffset();
        		
        		if (LT(1) == IToken.tLPAREN) {
        			// (P)(T) ...
        			isNewTypeId= false;
        			consume(IToken.tLPAREN);
        			typeid= typeId(DeclarationOptions.TYPEID);
        			if (typeid == null)
        				throw backtrack;
        			endOffset= consumeOrEOC(IToken.tRPAREN).getEndOffset();
        		} else {
        			// (P) T ...
        			typeid= typeId(DeclarationOptions.TYPEID_NEW);
        			if (typeid == null)
        				throw backtrack;
        			endOffset= calculateEndOffset(typeid);
        		}
        		end= LA(1);
        	} catch (BacktrackException e) {
        		plcmt= null;
        		typeid= null;
        	}

        	if (typeid != null && plcmt != null && LT(1) == IToken.tLPAREN) {        		
        		// (P)(T)(I) or (P) T (I)
        		consume(IToken.tLPAREN);
        		init= possiblyEmptyExpressionList(IToken.tRPAREN);
        		endOffset= consumeOrEOC(IToken.tRPAREN).getEndOffset();
        		return newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);
        	}

        	// (T) ...
    		backup(mark);
            IASTTypeId     typeid2= null;
            IASTExpression init2= null;
            int endOffset2;
        	try {
        		typeid2= typeId(DeclarationOptions.TYPEID);
        		if (typeid2 == null)
        			throw backtrack;
        		endOffset2= consumeOrEOC(IToken.tRPAREN).getEndOffset();
        	
        		if (LT(1) == IToken.tLPAREN) {
            		if (plcmt != null && 
            				CPPVisitor.findTypeRelevantDeclarator(typeid2.getAbstractDeclarator()) instanceof IASTArrayDeclarator) {
            			throwBacktrack(LA(1));
            		}

        			// (T)(I)
            		consume(IToken.tLPAREN);
            		init2= possiblyEmptyExpressionList(IToken.tRPAREN);
            		endOffset2= consumeOrEOC(IToken.tRPAREN).getEndOffset();
        		}
        	} catch (BacktrackException e) {
        		if (plcmt == null)
        			throw e;
        		endOffset2= -1;
        	}
        	

        	if (plcmt == null || endOffset2 > endOffset) 
        		return newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);

        	if (endOffset != endOffset2) {
        		backup(end);
        		return newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);
        	}

        	// ambiguity:
        	IASTExpression ex1= newExpression(isGlobal, plcmt, typeid, isNewTypeId, init, offset, endOffset);
        	IASTExpression ex2= newExpression(isGlobal, null, typeid2, false, init2, offset, endOffset2);
        	IASTAmbiguousExpression ambiguity= createAmbiguousExpression();
        	ambiguity.addExpression(ex1);
        	ambiguity.addExpression(ex2);
        	((ASTNode) ambiguity).setOffsetAndLength((ASTNode) ex1);
        	return ambiguity;
        }
        
        // T ...
		final IASTTypeId typeid = typeId(DeclarationOptions.TYPEID_NEW);
		if (typeid == null)
			throw backtrack;
		int endOffset = calculateEndOffset(typeid);
		IASTExpression init= null;
		if (LT(1) == IToken.tLPAREN) {
			// T(I)
			consume(IToken.tLPAREN);
			init= possiblyEmptyExpressionList(IToken.tRPAREN);
			endOffset= consumeOrEOC(IToken.tRPAREN).getEndOffset();
		}
		return newExpression(isGlobal, null, typeid, true, init, offset, endOffset);
	}

	private IASTExpression newExpression(boolean isGlobal, IASTExpression placement, IASTTypeId typeid,
			boolean isNewTypeId, IASTExpression init, int offset, int endOffset) {
		ICPPASTNewExpression result = createNewExpression();
        result.setIsGlobal(isGlobal);
        result.setIsNewTypeId(isNewTypeId);
        result.setTypeId(typeid);
        if (placement != null) {
        	result.setNewPlacement(placement);
        }
        if (init != null) {
        	result.setNewInitializer(init);
        }
        ((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
        return result;
    }

    protected ICPPASTNewExpression createNewExpression() {
        return new CPPASTNewExpression();
    }

    @Override
	protected IASTExpression unaryExpression() throws EndOfFileException, BacktrackException {
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
        case IToken.t_sizeof:
        	return parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(), 
        			IASTTypeIdExpression.op_sizeof, IASTUnaryExpression.op_sizeof);
        case IGCCToken.t_typeof:
        	return parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(), 
        			IASTTypeIdExpression.op_typeof, IASTUnaryExpression.op_typeof);
        case IGCCToken.t___alignof__:
        	return parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(), 
        			IASTTypeIdExpression.op_alignof, IASTUnaryExpression.op_alignOf);
        default:
            return postfixExpression();
        }
    }

    protected IASTExpression postfixExpression() throws EndOfFileException, BacktrackException {
        IASTExpression firstExpression = null;
        boolean isTemplate = false;

        switch (LT(1)) {
        case IToken.t_typename:
            int typenameOffset= consume().getOffset();

            boolean templateTokenConsumed = false;
            if (LT(1) == IToken.t_template) {
                consume();
                templateTokenConsumed = true;
            }
            ITokenDuple nestedName = name();
            IASTName name = createName(nestedName);
            if (LT(1) != IToken.tLPAREN) {
            	throwBacktrack(nestedName.getFirstToken().getOffset(), nestedName.getLastToken().getEndOffset());
            }

            ICPPASTTypenameExpression result = createTypenameExpression();
            ((ASTNode) result).setOffsetAndLength(typenameOffset, nestedName.getLastToken().getEndOffset() - typenameOffset);
            result.setIsTemplate(templateTokenConsumed);
            result.setName(name);
            firstExpression = result;
            break;
        // simple-type-specifier ( assignment-expression , .. )
        case IToken.t_char:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_char);
            break;
        case IToken.t_wchar_t:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_wchar_t);
            break;
        case IToken.t_bool:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_bool);
            break;
        case IToken.t_short:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_short);
            break;
        case IToken.t_int:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_int);
            break;
        case IToken.t_long:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_long);
            break;
        case IToken.t_signed:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_signed);
            break;
        case IToken.t_unsigned:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_unsigned);
            break;
        case IToken.t_float:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_float);
            break;
        case IToken.t_double:
            firstExpression = simpleTypeConstructorExpression(ICPPASTSimpleTypeConstructorExpression.t_double);
            break;
        case IToken.t_dynamic_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_dynamic_cast);
            break;
        case IToken.t_static_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_static_cast);
            break;
        case IToken.t_reinterpret_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_reinterpret_cast);
            break;
        case IToken.t_const_cast:
            firstExpression = specialCastExpression(ICPPASTCastExpression.op_const_cast);
            break;
        case IToken.t_typeid:
            int so = consume().getOffset();
            firstExpression= parseTypeidInParenthesisOrUnaryExpression(true, so, ICPPASTTypeIdExpression.op_typeid, ICPPASTUnaryExpression.op_typeid);
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
                int lastOffset;
                secondExpression = expression();
                switch (LT(1)) {
                case IToken.tRBRACKET:
                case IToken.tEOC:
                	lastOffset = consume().getEndOffset();
                	break;
                default:
                	throw backtrack;
                }

                IASTArraySubscriptExpression s = createArraySubscriptExpression();
                ((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), lastOffset - ((ASTNode) firstExpression).getOffset());
                s.setArrayExpression(firstExpression);
                s.setSubscriptExpression(secondExpression);
                firstExpression = s;
                break;
            case IToken.tLPAREN:
                // function call
                consume();

                if (LT(1) != IToken.tRPAREN)
                	secondExpression = expression();
                else
                	secondExpression = null;
                switch (LT(1)) {
                case IToken.tRPAREN:
                case IToken.tEOC:
                	lastOffset = consume().getEndOffset();
                	break;
                default:
                	throw backtrack;
                }

                IASTFunctionCallExpression fce = createFunctionCallExpression();
                ((ASTNode) fce).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), lastOffset - ((ASTNode) firstExpression).getOffset());
                fce.setFunctionNameExpression(firstExpression);
                if (secondExpression != null) {
                    fce.setParameterExpression(secondExpression);
                }
                firstExpression = fce;
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
                if (LT(1) == IToken.t_template) {
                    consume();
                    isTemplate = true;
                }

                IASTName name = idExpression();
                
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + dot.getLength());

                ICPPASTFieldReference fieldReference = createFieldReference();
                ((ASTNode) fieldReference).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
                fieldReference.setIsTemplate(isTemplate);
                fieldReference.setIsPointerDereference(false);
                fieldReference.setFieldName(name);
                fieldReference.setFieldOwner(firstExpression);
                firstExpression = fieldReference;
                break;
            case IToken.tARROW:
                // member access
                IToken arrow = consume();

                if (LT(1) == IToken.t_template) {
                    consume();
                    isTemplate = true;
                }

                name = idExpression();
                
                if (name == null)
                	throwBacktrack(((ASTNode) firstExpression).getOffset(), 
                			((ASTNode) firstExpression).getLength() + arrow.getLength());
                
                fieldReference = createFieldReference();
                ((ASTNode) fieldReference).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
                fieldReference.setIsTemplate(isTemplate);
                fieldReference.setIsPointerDereference(true);
                fieldReference.setFieldName(name);
                fieldReference.setFieldOwner(firstExpression);
                firstExpression = fieldReference;
                break;
            default:
                return firstExpression;
            }
        }
    }

    @Override
	protected IASTAmbiguousExpression createAmbiguousExpression() {
        return new CPPASTAmbiguousExpression();
    }
    
	@Override
	protected IASTAmbiguousExpression createAmbiguousBinaryVsCastExpression(IASTBinaryExpression binary, IASTCastExpression castExpr) {
		return new CPPASTAmbiguousBinaryVsCastExpression(binary, castExpr);
	}

	@Override
	protected IASTAmbiguousExpression createAmbiguousCastVsFunctionCallExpression(IASTCastExpression castExpr, IASTFunctionCallExpression funcCall) {
		return new CPPASTAmbiguousCastVsFunctionCallExpression(castExpr, funcCall);
	}

	protected ICPPASTAmbiguousTemplateArgument createAmbiguousTemplateArgument() {
    	return new CPPASTAmbiguousTemplateArgument();
    }

    protected IASTArraySubscriptExpression createArraySubscriptExpression() {
        return new CPPASTArraySubscriptExpression();
    }

    protected ICPPASTTypenameExpression createTypenameExpression() {
        return new CPPASTTypenameExpression();
    }

    @Override
	protected IASTFunctionCallExpression createFunctionCallExpression() {
        return new CPPASTFunctionCallExpression();
    }

    protected ICPPASTFieldReference createFieldReference() {
        return new CPPASTFieldReference();
    }

    protected IASTExpression simpleTypeConstructorExpression(int operator)
            throws EndOfFileException, BacktrackException {
        int startingOffset = LA(1).getOffset();
        consume();
        consume(IToken.tLPAREN);
        IASTExpression operand = null;
        if (LT(1) != IToken.tRPAREN)
            operand = expression();
        int l = consume(IToken.tRPAREN).getEndOffset();
        ICPPASTSimpleTypeConstructorExpression result = createSimpleTypeConstructorExpression();
        ((ASTNode) result).setOffsetAndLength(startingOffset, l - startingOffset);
        result.setSimpleType(operator);
        if (operand != null) {
            result.setInitialValue(operand);
        }
        return result;
    }

    protected ICPPASTSimpleTypeConstructorExpression createSimpleTypeConstructorExpression() {
        return new CPPASTSimpleTypeConstructorExpression();
    }

    @Override
	protected IASTExpression primaryExpression() throws EndOfFileException,
            BacktrackException {
        IToken t = null;
        ICPPASTLiteralExpression literalExpression = null;
        switch (LT(1)) {
        // TO DO: we need more literals...
        case IToken.tINTEGER:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(IASTLiteralExpression.lk_integer_constant);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset()- t.getOffset());
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
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.t_false:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_false);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.t_true:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_true);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;

        case IToken.t_this:
            t = consume();
            literalExpression = createLiteralExpression();
            literalExpression.setKind(ICPPASTLiteralExpression.lk_this);
            literalExpression.setValue(t.getImage());
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.tLPAREN:
        	if (supportStatementsInExpressions && LT(2) == IToken.tLBRACE) {
        		return compoundStatementExpression();
        	}
            t = consume();
            int finalOffset= 0;
            IASTExpression lhs= expression();
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
        case IToken.tCOLONCOLON:
        case IToken.t_operator:
        case IToken.tCOMPLETION:
        case IToken.tBITCOMPLEMENT: {
            IASTName name = idExpression();
            IASTIdExpression idExpression = createIdExpression();
            ((ASTNode) idExpression).setOffsetAndLength(((ASTNode) name)
                    .getOffset(), ((ASTNode) name).getOffset()
                    + ((ASTNode) name).getLength()
                    - ((ASTNode) name).getOffset());
            idExpression.setName(name);
            return idExpression;
        }
        default:
            IToken la = LA(1);
            int startingOffset = la.getOffset();
            throwBacktrack(startingOffset, la.getLength());
            return null;
        }

    }

    protected ICPPASTLiteralExpression createLiteralExpression() {
        return new CPPASTLiteralExpression();
    }

    @Override
	protected IASTIdExpression createIdExpression() {
        return new CPPASTIdExpression();
    }

    protected IASTName idExpression() throws EndOfFileException, BacktrackException {
        IASTName name = null;
        try {
            name = createName(nameWithoutLogicalOperatorInTemplateID());
        } catch (BacktrackException bt) {
            IToken mark = mark();
            if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                IToken start = consume();
                IToken end = null;
                if (start.getType() == IToken.tIDENTIFIER)
                    end = consumeTemplateParameters(end);
                while (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                    end = consume();
                    if (end.getType() == IToken.tIDENTIFIER)
                        end = consumeTemplateParameters(end);
                }

                if (LT(1) == IToken.t_operator)
                    name = operatorId(start, null);
                else {
                    backup(mark);
                    throwBacktrack(start.getOffset(), end == null ? start.getLength() : end.getEndOffset());
                }
            } else if (LT(1) == IToken.t_operator)
                name = operatorId(null, null);
        }
        return name;

    }

    protected IASTExpression specialCastExpression(int kind) throws EndOfFileException, BacktrackException {
        final int offset = LA(1).getOffset();
        final int optype= consume().getType();
        consume(IToken.tLT);
        final IASTTypeId typeID = typeId(DeclarationOptions.TYPEID);
        if (typeID == null) 
        	throw backtrack; 
        consume(IToken.tGT);
        consume(IToken.tLPAREN);
        final IASTExpression operand= expression();
        final int endOffset= consume(IToken.tRPAREN).getEndOffset();
        int operator;
        switch(optype) {
	        case IToken.t_dynamic_cast:
	        	operator = ICPPASTCastExpression.op_dynamic_cast;
	        	break;
	        case IToken.t_static_cast:
	        	operator = ICPPASTCastExpression.op_static_cast;
	        	break;
	        case IToken.t_reinterpret_cast:
	        	operator = ICPPASTCastExpression.op_reinterpret_cast;
	        	break;
	        case IToken.t_const_cast:
	        	operator = ICPPASTCastExpression.op_const_cast;
	        	break;
	        default:
	        	operator = IASTCastExpression.op_cast;
	    		break; 
        }
        return buildCastExpression(operator, typeID, operand, offset, endOffset);
    }

    /**
     * The merger of using-declaration and using-directive in ANSI C++ grammar.
     * using-declaration: using typename? ::? nested-name-specifier
     * unqualified-id ; using :: unqualified-id ; using-directive: using
     * namespace ::? nested-name-specifier? namespace-name ;
     * 
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected IASTDeclaration usingClause() throws EndOfFileException,
            BacktrackException {
        final int offset= consume().getOffset();

        if (LT(1) == IToken.t_namespace) {
            // using-directive
            int endOffset = consume().getEndOffset();
            IASTName name = null;
            switch (LT(1)) {
            case IToken.tIDENTIFIER:
            case IToken.tCOLONCOLON:
            case IToken.tCOMPLETION:
                name = createName(name());
                break;
            default:
                throwBacktrack(offset, endOffset - offset);
            }

            switch (LT(1)) {
            case IToken.tSEMI:
            case IToken.tEOC:
                endOffset = consume().getEndOffset();
                break;
            default:
                throw backtrack;
            }

            ICPPASTUsingDirective astUD = createUsingDirective();
            ((ASTNode) astUD).setOffsetAndLength(offset, endOffset - offset);
            astUD.setQualifiedName(name);
            return astUD;
        }

        ICPPASTUsingDeclaration result = usingDeclaration(offset);
        return result;
    }

	private ICPPASTUsingDeclaration usingDeclaration(final int offset) throws EndOfFileException,
			BacktrackException {
		boolean typeName = false;
        if (LT(1) == IToken.t_typename) {
            typeName = true;
            consume();
        }

        IASTName name = idExpression();
        int end;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            end = consume().getEndOffset();
            break;
        default:
            throw backtrack;
        }

        ICPPASTUsingDeclaration result = createUsingDeclaration();
        ((ASTNode) result).setOffsetAndLength(offset, end - offset);
        result.setIsTypename(typeName);
        result.setName(name);
		return result;
	}


    protected ICPPASTUsingDeclaration createUsingDeclaration() {
        return new CPPASTUsingDeclaration();
    }


    protected ICPPASTUsingDirective createUsingDirective() {
        return new CPPASTUsingDirective();
    }

    /**
     * Implements Linkage specification in the ANSI C++ grammar.
     * linkageSpecification : extern "string literal" declaration | extern
     * "string literal" { declaration-seq }
     * 
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected ICPPASTLinkageSpecification linkageSpecification()
            throws EndOfFileException, BacktrackException {
        int offset= consume().getOffset(); // t_extern
        String spec = consume().getImage(); // tString
        ICPPASTLinkageSpecification linkage = createLinkageSpecification();
        linkage.setLiteral(spec);

        if (LT(1) == IToken.tLBRACE) {
            int endOffset= consume().getEndOffset();
            int declOffset= -1;
            while(true) {
            	IToken next= LAcatchEOF(1);
            	if (next == null) {
            		((ASTNode) linkage).setOffsetAndLength(offset, endOffset-offset);
            		throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, endOffset, 0), linkage);
            		return null; // hint for java-compiler
            	}
                try {
                	if (next.getType() == IToken.tEOC)
                		break;
                	
                	if (next.getType() == IToken.tRBRACE) {
                		endOffset= consume().getEndOffset();
                		break;
                	}
                		    
                	final int nextOffset = next.getOffset();
            		declarationMark= next;
            		next= null; // don't hold on to the token while parsing namespaces, class bodies, etc.

            		IASTDeclaration d;
            		if (declOffset == nextOffset) {
            			// no progress
            			d= skipProblemDeclaration(declOffset);
                	} else {
                		declOffset= nextOffset;
                		d= declaration(DeclarationOptions.GLOBAL);
                	}
            		linkage.addDeclaration(d);
            		endOffset= calculateEndOffset(d);
                } catch (BacktrackException bt) {
                	IASTDeclaration[] decls= problemDeclaration(declOffset, bt, DeclarationOptions.GLOBAL);
                	for (IASTDeclaration declaration : decls) {
    					linkage.addDeclaration(declaration);
        				endOffset= calculateEndOffset(declaration);
                	}
                } catch (EndOfFileException e) {
                	IASTDeclaration d= skipProblemDeclaration(declOffset);
            		linkage.addDeclaration(d);
            		endOffset= calculateEndOffset(d);
            		break;
                } finally {
                	declarationMark= null;
                }
            }
            ((ASTNode) linkage).setOffsetAndLength(offset, endOffset - offset);
            return linkage;
        }
        // single declaration

        IASTDeclaration d = declaration(DeclarationOptions.GLOBAL);
        linkage.addDeclaration(d);
        int endOffset= calculateEndOffset(d);
        ((ASTNode) linkage).setOffsetAndLength(offset, endOffset-offset);
        return linkage;
    }

    protected ICPPASTLinkageSpecification createLinkageSpecification() {
        return new CPPASTLinkageSpecification();
    }

    /**
     * Represents the amalgamation of template declarations, template
     * instantiations and specializations in the ANSI C++ grammar.
     * template-declaration: export? template < template-parameter-list >
     * declaration explicit-instantiation: template declaration
     * explicit-specialization: template <>declaration
     * @param option 
     * 
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected IASTDeclaration templateDeclaration(DeclarationOptions option) throws EndOfFileException,
            BacktrackException {
    	++templateCount;
    	try {
    		IToken mark = mark();
    		IToken firstToken = null;
    		boolean exported = false;
    		boolean encounteredExtraMod = false;
    		if (LT(1) == IToken.t_export) {
    			exported = true;
    			firstToken = consume();
    			consume(IToken.t_template);
    		} else {
    			if (supportExtendedTemplateSyntax) {
    				switch (LT(1)) {
    				case IToken.t_static:
    				case IToken.t_extern:
    				case IToken.t_inline:
    					firstToken = consume();
    					consume(IToken.t_template);
    					encounteredExtraMod = true;
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
    			ICPPASTExplicitTemplateInstantiation templateInstantiation = null;
    			if (encounteredExtraMod && supportExtendedTemplateSyntax) {
    				IGPPASTExplicitTemplateInstantiation temp = createGnuTemplateInstantiation();
    				switch (firstToken.getType()) {
    				case IToken.t_static:
    					temp.setModifier(IGPPASTExplicitTemplateInstantiation.ti_static);
    					break;
    				case IToken.t_extern:
    					temp.setModifier(IGPPASTExplicitTemplateInstantiation.ti_extern);
    					break;
    				case IToken.t_inline:
    					temp.setModifier(IGPPASTExplicitTemplateInstantiation.ti_inline);
    					break;
    				}
    				templateInstantiation = temp;
    			} else {
    				templateInstantiation = createTemplateInstantiation();
    			}
    			IASTDeclaration d = declaration(option);
    			((ASTNode) templateInstantiation).setOffsetAndLength(firstToken
    					.getOffset(), calculateEndOffset(d) - firstToken.getOffset());
    			templateInstantiation.setDeclaration(d);
    			return templateInstantiation;
    		}
    		consume(); // check for LT made before
    		if (LT(1) == IToken.tGT) {
    			// explicit-specialization
    			consume();

    			ICPPASTTemplateSpecialization templateSpecialization = createTemplateSpecialization();
    			IASTDeclaration d = declaration(option);
    			((ASTNode) templateSpecialization).setOffsetAndLength(
    					firstToken.getOffset(), calculateEndOffset(d) - firstToken.getOffset());
    			templateSpecialization.setDeclaration(d);
    			return templateSpecialization;
    		}

    		try {
            	final boolean wasOnTop= onTopInTemplateArgs;
            	onTopInTemplateArgs= true;
            	List<ICPPASTTemplateParameter> parms;
            	try {
                	parms = templateParameterList();
                	consume(IToken.tGT);
                } finally {
                	onTopInTemplateArgs= wasOnTop;
                }
    			ICPPASTTemplateDeclaration templateDecl = createTemplateDeclaration();
    			IASTDeclaration d = declaration(option);
    			((ASTNode) templateDecl).setOffsetAndLength(firstToken.getOffset(),
    					calculateEndOffset(d) - firstToken.getOffset());
    			templateDecl.setExported(exported);
    			templateDecl.setDeclaration(d);
    			for (int i = 0; i < parms.size(); ++i) {
    				ICPPASTTemplateParameter parm = parms.get(i);
    				templateDecl.addTemplateParamter(parm);
    			}
    			return templateDecl;
    		} catch (BacktrackException bt) {
    			backup(mark);
    			throw bt;
    		}
    	} finally {
    		templateCount--;
    	}
    }


    protected ICPPASTTemplateDeclaration createTemplateDeclaration() {
        return new CPPASTTemplateDeclaration();
    }


    protected ICPPASTTemplateSpecialization createTemplateSpecialization() {
        return new CPPASTTemplateSpecialization();
    }


    protected IGPPASTExplicitTemplateInstantiation createGnuTemplateInstantiation() {
        return new GPPASTExplicitTemplateInstantiation();
    }


    protected ICPPASTExplicitTemplateInstantiation createTemplateInstantiation() {
        return new CPPASTExplicitTemplateInstantiation();
    }

    /**
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
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected List<ICPPASTTemplateParameter> templateParameterList() throws BacktrackException,
            EndOfFileException {
        // if we have gotten this far then we have a true template-declaration
        // iterate through the template parameter list
        List<ICPPASTTemplateParameter> returnValue = new ArrayList<ICPPASTTemplateParameter>(DEFAULT_PARM_LIST_SIZE);

        for (;;) {
            if (LT(1) == IToken.tGT)
                return returnValue;
            if (LT(1) == IToken.t_class || LT(1) == IToken.t_typename) {
                IToken startingToken = LA(1);
                int lastOffset = 0;
                int type = (LT(1) == IToken.t_class ? ICPPASTSimpleTypeTemplateParameter.st_class
                        : ICPPASTSimpleTypeTemplateParameter.st_typename);
                lastOffset = consume().getEndOffset();
                IASTName identifierName = null;
                IASTTypeId typeId = null;

                if (LT(1) == IToken.tIDENTIFIER) { // optional identifier
                    identifierName = createName(identifier());
                    lastOffset = calculateEndOffset(identifierName);
                    if (LT(1) == IToken.tASSIGN) { // optional = type-id
                        consume();
                        typeId = typeId(DeclarationOptions.TYPEID); // type-id
                        if (typeId == null) 
                        	throw backtrack;
                        lastOffset = calculateEndOffset(typeId);
                    }
                } else {
                    identifierName = createName();
                }

                ICPPASTSimpleTypeTemplateParameter parm = createSimpleTemplateParameter();
                ((ASTNode) parm).setOffsetAndLength(startingToken.getOffset(),
                        lastOffset - startingToken.getOffset());
                parm.setParameterType(type);
                parm.setName(identifierName);
                if (typeId != null) {
                    parm.setDefaultType(typeId);
                }
                returnValue.add(parm);

            } else if (LT(1) == IToken.t_template) {
                IToken firstToken = consume();
                consume(IToken.tLT);

                List<ICPPASTTemplateParameter> subResult = templateParameterList();
                consume(IToken.tGT);
                int last = consume(IToken.t_class).getEndOffset();
                IASTName identifierName = null;
                IASTExpression optionalExpression = null;

                if (LT(1) == IToken.tIDENTIFIER) { // optional identifier
                    identifierName = createName(identifier());
                    last = calculateEndOffset(identifierName);
                    if (LT(1) == IToken.tASSIGN) { // optional = type-id
                        consume();
                        optionalExpression = primaryExpression();
                        last = calculateEndOffset(optionalExpression);
                    }
                } else
                    identifierName = createName();

                ICPPASTTemplatedTypeTemplateParameter parm = createTemplatedTemplateParameter();
                ((ASTNode) parm).setOffsetAndLength(firstToken.getOffset(), last - firstToken.getOffset());
                parm.setName(identifierName);
                if (optionalExpression != null) {
                    parm.setDefaultValue(optionalExpression);
                }

                for (int i = 0; i < subResult.size(); ++i) {
                    ICPPASTTemplateParameter p = subResult.get(i);
                    parm.addTemplateParamter(p);
                }
                returnValue.add(parm);

            } else if (LT(1) == IToken.tCOMMA) {
                consume();
                continue;
            } else {
                ICPPASTParameterDeclaration parm = parameterDeclaration();
                returnValue.add(parm);
            }
        }
    }


    protected ICPPASTTemplatedTypeTemplateParameter createTemplatedTemplateParameter() {
        return new CPPASTTemplatedTypeTemplateParameter();
    }

    protected ICPPASTSimpleTypeTemplateParameter createSimpleTemplateParameter() {
        return new CPPASTSimpleTypeTemplateParameter();
    }

    /**
     * The most abstract construct within a translationUnit : a declaration.
     * declaration : {"asm"} asmDefinition | {"namespace"} namespaceDefinition |
     * {"using"} usingDeclaration | {"export"|"template"} templateDeclaration |
     * {"extern"} linkageSpecification | simpleDeclaration Notes: - folded in
     * blockDeclaration - merged alternatives that required same LA -
     * functionDefinition into simpleDeclaration - namespaceAliasDefinition into
     * namespaceDefinition - usingDirective into usingDeclaration -
     * explicitInstantiation and explicitSpecialization into templateDeclaration
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    @Override
	protected IASTDeclaration declaration(DeclarationOptions option) throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.t_asm:
            return asmDeclaration();
        case IToken.t_namespace:
            return namespaceDefinitionOrAlias();
        case IToken.t_using:
            return usingClause();
        case IToken.t_export:
        case IToken.t_template:
            return templateDeclaration(option);
        case IToken.t_extern:
            if (LT(2) == IToken.tSTRING)
                return linkageSpecification();
        	if (supportExtendedTemplateSyntax && LT(2) == IToken.t_template)
                return templateDeclaration(option);
            break;
        case IToken.t_static:
        case IToken.t_inline:
        	if (supportExtendedTemplateSyntax && LT(2) == IToken.t_template)
                return templateDeclaration(option);
        	break;
        case IToken.tSEMI:
        	IToken t= consume();
        	IASTSimpleDeclaration decl= createSimpleDeclaration();
        	IASTSimpleDeclSpecifier declspec= createSimpleDeclSpecifier();
        	decl.setDeclSpecifier(declspec);
        	((ASTNode) declspec).setOffsetAndLength(t.getOffset(), 0);
        	((ASTNode) decl).setOffsetAndLength(t.getOffset(), t.getLength());
        	return decl; 
        }
        
        return simpleDeclaration(option);
    }
    
    /**
     * Serves as the namespace declaration portion of the ANSI C++ grammar.
     * namespace-definition: namespace identifier { namespace-body } | namespace {
     * namespace-body } namespace-body: declaration-seq?
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTDeclaration namespaceDefinitionOrAlias() throws BacktrackException, EndOfFileException {
        final int offset= consume().getOffset();
        int endOffset;
        
        // optional name
        IASTName name = null;
        if (LT(1) == IToken.tIDENTIFIER) {
            name = createName(identifier());
            endOffset= calculateEndOffset(name);
        } else {
            name = createName();
        }

        // bug 195701, gcc 4.2 allows visibility attribute for namespaces.
        __attribute_decl_seq(true, false);

        if (LT(1) == IToken.tLBRACE) {
	        ICPPASTNamespaceDefinition ns= createNamespaceDefinition();
	        ns.setName(name);
            endOffset= consume().getEndOffset();
            int declOffset= -1;
            while(true) {
            	IToken next= LAcatchEOF(1);
            	if (next == null) {
            		((ASTNode) ns).setOffsetAndLength(offset, endOffset-offset);
            		throwBacktrack(createProblem(IProblem.SYNTAX_ERROR, endOffset, 0), ns);
            		return null; // hint for java-compiler
            	}
                try {
                	if (next.getType() == IToken.tEOC)
                		break;
                	
                	if (next.getType() == IToken.tRBRACE) {
                		endOffset= consume().getEndOffset();
                		break;
                	}
                		    
                	final int nextOffset = next.getOffset();
            		declarationMark= next;
            		next= null; // don't hold on to the token while parsing namespaces, class bodies, etc.

            		IASTDeclaration d;
            		if (declOffset == nextOffset) {
            			// no progress
            			d= skipProblemDeclaration(declOffset);
                	} else {
                		declOffset= nextOffset;
                		d= declaration(DeclarationOptions.GLOBAL);
                	}
            		ns.addDeclaration(d);
            		endOffset= calculateEndOffset(d);
                } catch (BacktrackException bt) {
                	IASTDeclaration[] decls= problemDeclaration(declOffset, bt, DeclarationOptions.GLOBAL);
                	for (IASTDeclaration declaration : decls) {
                		ns.addDeclaration(declaration);
        				endOffset= calculateEndOffset(declaration);
                	}
                } catch (EndOfFileException e) {
                	IASTDeclaration d= skipProblemDeclaration(declOffset);
                	ns.addDeclaration(d);
            		endOffset= calculateEndOffset(d);
            		break;
                } finally {
                	declarationMark= null;
                }
            }
            ((ASTNode) ns).setOffsetAndLength(offset, endOffset - offset);
            return ns;
        } 
        
		if (LT(1) == IToken.tASSIGN) {
            endOffset= consume().getEndOffset();
            if (name.toString() == null) {
                throwBacktrack(offset, endOffset - offset);
                return null;
            }

            ITokenDuple duple = name();
            IASTName qualifiedName = createName(duple);
            endOffset = consume(IToken.tSEMI).getEndOffset();

            ICPPASTNamespaceAlias alias = createNamespaceAlias();
            ((ASTNode) alias).setOffsetAndLength(offset, endOffset - offset);
            alias.setAlias(name);
            alias.setMappingName(qualifiedName);
            return alias;
        } 
		throwBacktrack(LA(1));
		return null;
    }

    protected ICPPASTNamespaceAlias createNamespaceAlias() {
        return new CPPASTNamespaceAlias();
    }


    protected ICPPASTQualifiedName createQualifiedName(ITokenDuple duple) {
        CPPASTQualifiedName result = new CPPASTQualifiedName();
        result.setOffsetAndLength(duple.getStartOffset(), duple.getEndOffset() - duple.getStartOffset());
        result.setSignature(duple.toString());
        ITokenDuple[] segments = duple.getSegments();
        int startingValue = 0;
        if (segments.length > 0) {
        	final ITokenDuple firstSeg= segments[0];
        	final IToken firstToken= firstSeg.getFirstToken();
        	if (firstToken.getType() == IToken.tCOLONCOLON) {
        		if (firstToken == firstSeg.getLastToken() && firstSeg.getTemplateIdArgLists() == null) {
        			++startingValue;
        			result.setFullyQualified(true);
        		}
        	}
        }
        for (int i = startingValue; i < segments.length; ++i) {
            IASTName subName = null;
            // take each name and add it to the result
            final ITokenDuple seg= segments[i];
            if (seg.getTemplateIdArgLists() == null) {
            	final IToken firstToken= seg.getFirstToken();
            	if (firstToken == seg.getLastToken()) {
            		subName= createName(firstToken);
            	}
            	else {
            		subName = createName(seg);
            	}
            }
            else {
                // templateID
                subName = createTemplateID(segments[i]);
            }

            if (i == segments.length - 1 && duple instanceof OperatorTokenDuple) { 
            	// make sure the last segment is an OperatorName/ConversionName
                subName = createOperatorName((OperatorTokenDuple) duple, subName);
            }

            // bug 189299, 193152 indicate that there have been nested qualified names
            // as a work around just flatten them.
            if (subName instanceof ICPPASTQualifiedName) {
            	IASTName[] subNames= ((ICPPASTQualifiedName) subName).getNames();
            	for (IASTName subName2 : subNames) {
					subName = subName2;
	            	result.addName(subName);
				}            
            }
            else {
            	((ASTNode) subName).setOffsetAndLength(segments[i].getStartOffset(), 
            			segments[i].getEndOffset() - segments[i].getStartOffset());
            	result.addName(subName);
            }
        }
        return result;
    }

    protected ICPPASTTemplateId createTemplateID(ITokenDuple duple) {
        ICPPASTTemplateId result = new CPPASTTemplateId();
        ((ASTNode) result).setOffsetAndLength(duple.getStartOffset(), duple
                .getEndOffset() - duple.getStartOffset());
        CPPASTName templateIdName= null;
        if (duple instanceof BasicTokenDuple) {
        	ITokenDuple nameDuple= ((BasicTokenDuple)duple).getTemplateIdNameTokenDuple();
        	templateIdName= (CPPASTName) createName(nameDuple);
        }
        else {
        	templateIdName= (CPPASTName) createName();
            char[] image = duple.extractNameFromTemplateId();
        	templateIdName.setOffsetAndLength(duple.getStartOffset(), image.length);
            templateIdName.setName(image);
        }
        result.setTemplateName(templateIdName);
        if (duple.getTemplateIdArgLists() != null) {
            List<IASTNode> args= duple.getTemplateIdArgLists()[0];
            if (args != null)
                for (int i = 0; i < args.size(); ++i) {
                    IASTNode n = args.get(i);
                    if (n instanceof IASTTypeId) {
                        result.addTemplateArgument((IASTTypeId) n);
                    } else if(n instanceof IASTExpression) {
                        result.addTemplateArgument((IASTExpression) n);
                    } else if(n instanceof ICPPASTAmbiguousTemplateArgument) {
                    	result.addTemplateArgument((ICPPASTAmbiguousTemplateArgument) n);
                    }
                }
        }
        return result;
    }


    protected IASTName createName(ITokenDuple duple) {
        if (duple == null)
            return createName();
        if (duple.getSegmentCount() != 1) {
        	// workaround for bug 193152, 
        	// looks like duple.getSeqmentCount() and duple.getSegments().length can be different.
        	ICPPASTQualifiedName qname= createQualifiedName(duple);
        	if (qname.getNames().length > 0) {
        		return qname;
        	}
        }
        if (duple.getTemplateIdArgLists() != null)
            return createTemplateID(duple);

        // We're a single name
        IASTName name = new CPPASTName(duple.toCharArray());
        if (duple instanceof OperatorTokenDuple) {
            name = createOperatorName((OperatorTokenDuple) duple, name);
        }

        IToken token = duple.getFirstToken();
        switch (token.getType()) {
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            createCompletionNode(token).addName(name);
            break;
        }

        ((ASTNode) name).setOffsetAndLength(duple.getStartOffset(), duple
                .getEndOffset() - duple.getStartOffset());

        return name;
    }

    protected IASTName createOperatorName(OperatorTokenDuple duple, IASTName name) {
        IASTName aName = null;

        if (duple.isConversionOperator()) {
            aName = new CPPASTConversionName(name.toCharArray());
            IASTTypeId typeId = duple.getTypeId();
            ((CPPASTConversionName) aName).setTypeId(typeId);
        } else {
            aName = new CPPASTOperatorName(duple.getOperator());
        }

        if (name instanceof ICPPASTTemplateId) {
            ((ICPPASTTemplateId) name).setTemplateName(aName);
            return name;
        }

        return aName;
    }


    protected ICPPASTNamespaceDefinition createNamespaceDefinition() {
        return new CPPASTNamespaceDefinition();
    }

    /**
     * Parses a declaration with the given options.
     */
    protected IASTDeclaration simpleDeclaration(DeclarationOptions declOption)
            throws BacktrackException, EndOfFileException {
        if (LT(1) == IToken.tLBRACE)
            throwBacktrack(LA(1));
        
        final int firstOffset= LA(1).getOffset();
        int endOffset= firstOffset;

        ICPPASTDeclSpecifier declSpec;
        IASTDeclarator dtor= null;
        IToken markBeforDtor= null;
        try {
            declSpec = declSpecifierSeq(declOption);
            switch(LTcatchEOF(1)) {
            case 0: // eof
            case IToken.tSEMI:
            	if (!validWithoutDtor(declOption, declSpec)) {
                	throwBacktrack(LA(1));
            	}
            	break;
            case IToken.tCOMMA:
            	throwBacktrack(LA(1));
            	break;
            case IToken.tEOC:
            	break;
            default:
            	markBeforDtor= mark();
            	try {
            		dtor= initDeclarator(declSpec, declOption);
            	} catch (BacktrackException e) {
            		if (!validWithoutDtor(declOption, declSpec)) 
                    	throw e;
            		backup(markBeforDtor);
            	} catch (EndOfFileException e) {
            		if (!validWithoutDtor(declOption, declSpec)) 
                    	throw e;
            		backup(markBeforDtor);
            	}
            	break;
            }
        } catch (FoundDeclaratorException e) {
        	declSpec= (ICPPASTDeclSpecifier) e.declSpec;
        	dtor= e.declarator;
            backup(e.currToken);
        } catch (BacktrackException e) {
        	IASTNode node= e.getNodeBeforeProblem();
        	if (node instanceof ICPPASTDeclSpecifier && validWithoutDtor(declOption, (ICPPASTDeclSpecifier) node)) {
                IASTSimpleDeclaration d= createSimpleDeclaration();
                d.setDeclSpecifier((IASTDeclSpecifier) node);
                setRange(d, node);
        		throwBacktrack(e.getProblem(), d);
        	}
        	throw e;
        }
        
        IASTDeclarator[] declarators= {dtor};
        while (LTcatchEOF(1) == IToken.tCOMMA) {
        	consume();
        	declarators= (IASTDeclarator[]) ArrayUtil.append( IASTDeclarator.class, declarators, initDeclarator(declSpec, declOption));
        }

        declarators= (IASTDeclarator[]) ArrayUtil.removeNulls( IASTDeclarator.class, declarators );

        boolean insertSemi= false;
        final int lt1= LTcatchEOF(1);
        switch (lt1) {
        case IToken.tEOC:
        	endOffset= figureEndOffset(declSpec, declarators);
            break;
        case IToken.tSEMI:
            endOffset= consume().getEndOffset();
            break;
        case IToken.t_try:
            consume();
            return functionDefinition(firstOffset, declSpec, declarators, true);
        case IToken.tCOLON:
        case IToken.tLBRACE:
            return functionDefinition(firstOffset, declSpec, declarators, false);
        default:
        	if (declOption != DeclarationOptions.LOCAL) {
        		insertSemi= true;
        		if (validWithoutDtor(declOption, declSpec)) {
        			// class definition without semicolon
        			if (markBeforDtor == null || !isOnSameLine(calculateEndOffset(declSpec), markBeforDtor.getOffset())) {
        				if (markBeforDtor != null) {
        					backup(markBeforDtor);
        				}
        				declarators= IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        				endOffset= calculateEndOffset(declSpec);
        				break;
        			}
        		} 
        		endOffset= figureEndOffset(declSpec, declarators);
        		if (lt1 == 0 || !isOnSameLine(endOffset, LA(1).getOffset())) {
        			insertSemi= true;
        			break;
        		}
        		if (declarators.length == 1 && declarators[0] instanceof IASTFunctionDeclarator) {
        			break;
        		}
        	}
        	throwBacktrack(LA(1));
        }

        // no function body
        IASTSimpleDeclaration simpleDeclaration= createSimpleDeclaration();
        simpleDeclaration.setDeclSpecifier(declSpec);
        for (IASTDeclarator declarator : declarators) {
            simpleDeclaration.addDeclarator(declarator); 
        }
        
        ((ASTNode) simpleDeclaration).setOffsetAndLength(firstOffset, endOffset-firstOffset);
        
        if (insertSemi) {
    		IASTProblem problem= createProblem(IProblem.SYNTAX_ERROR, endOffset, 0);
    		throwBacktrack(problem, simpleDeclaration);
        }
        return simpleDeclaration;
    }

	private boolean validWithoutDtor(DeclarationOptions option, ICPPASTDeclSpecifier declSpec) {
		if (declSpec instanceof IASTCompositeTypeSpecifier)
			return true;
		if (declSpec instanceof IASTElaboratedTypeSpecifier)
			return true;
		if (declSpec instanceof IASTEnumerationSpecifier)
			return true;
		
		return option == DeclarationOptions.FUNCTION_STYLE_ASM;
	}

	private IASTDeclaration functionDefinition(final int firstOffset, IASTDeclSpecifier declSpec,
			IASTDeclarator[] dtors, boolean hasFunctionTryBlock) throws EndOfFileException, BacktrackException {
		
    	if (dtors.length != 1) 
    		throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
    	
		final IASTDeclarator outerDtor= dtors[0];
		final IASTDeclarator dtor= CPPVisitor.findTypeRelevantDeclarator(outerDtor);
		if (dtor instanceof ICPPASTFunctionDeclarator == false)
			throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

		final ICPPASTFunctionDeclarator fdtor= (ICPPASTFunctionDeclarator) dtor;
		
		if (LT(1) == IToken.tCOLON) {
			List<ICPPASTConstructorChainInitializer> constructorChain= new ArrayList<ICPPASTConstructorChainInitializer>(DEFAULT_CONSTRUCTOR_CHAIN_LIST_SIZE);
		    ctorInitializer(constructorChain);
		    if (!constructorChain.isEmpty()) {
		    	for (ICPPASTConstructorChainInitializer initializer : constructorChain) {
		    		fdtor.addConstructorToChain(initializer);
		    	}
		    	// fix for 86698, update the declarator's length
		    	adjustLength(outerDtor, constructorChain.get(constructorChain.size()-1));
		    }
		}

		IASTStatement body;
		try {
			body= handleFunctionBody();
		} catch (BacktrackException bt) {
			final IASTNode n= bt.getNodeBeforeProblem();
			if (n instanceof IASTCompoundStatement) {
				IASTFunctionDefinition funcDefinition = createFunctionDefinition();
				funcDefinition.setDeclSpecifier(declSpec);
				funcDefinition.setDeclarator(fdtor);
				funcDefinition.setBody((IASTCompoundStatement) n);
				((ASTNode) funcDefinition).setOffsetAndLength(firstOffset, calculateEndOffset(n) - firstOffset);
				throwBacktrack(bt.getProblem(), funcDefinition);
			}
			throw bt;
		}
		
		int endOffset= calculateEndOffset(body);
		if (hasFunctionTryBlock) {
		    List<ICPPASTCatchHandler> handlers = new ArrayList<ICPPASTCatchHandler>(DEFAULT_CATCH_HANDLER_LIST_SIZE);
		    catchHandlerSequence(handlers);
		    if (!handlers.isEmpty() && fdtor instanceof ICPPASTFunctionTryBlockDeclarator) {
		    	ICPPASTFunctionTryBlockDeclarator tbd= (ICPPASTFunctionTryBlockDeclarator) fdtor;
		    	for (ICPPASTCatchHandler catchHandler : handlers) {
					tbd.addCatchHandler(catchHandler);
				}
		    	endOffset= calculateEndOffset(handlers.get(handlers.size()-1));
		    }
		}

		IASTFunctionDefinition funcDefinition = createFunctionDefinition();
		funcDefinition.setDeclSpecifier(declSpec);
		funcDefinition.setDeclarator(fdtor);
		funcDefinition.setBody(body);
		
		((ASTNode) funcDefinition).setOffsetAndLength(firstOffset, endOffset-firstOffset);
		return funcDefinition;
	}


    @Override
	protected IASTFunctionDefinition createFunctionDefinition() {
        return new CPPASTFunctionDefinition();
    }


    @Override
	protected IASTSimpleDeclaration createSimpleDeclaration() {
        return new CPPASTSimpleDeclaration();
    }

    /**
     * This method parses a constructor chain ctorinitializer: :
     * meminitializerlist meminitializerlist: meminitializer | meminitializer ,
     * meminitializerlist meminitializer: meminitializerid | ( expressionlist? )
     * meminitializerid: ::? nestednamespecifier? classname identifier
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected void ctorInitializer(List<ICPPASTConstructorChainInitializer> collection) throws EndOfFileException,
            BacktrackException {
        consume();
        ctorLoop: for (;;) {
            ITokenDuple duple = name();
            IASTName name = createName(duple);

            int end;
            IASTExpression expressionList = null;
            switch (LT(1)) {
            case IToken.tLPAREN:
                consume();

                if (LT(1) != IToken.tRPAREN)
                    expressionList = expression();

                switch (LT(1)) {
                case IToken.tRPAREN:
                case IToken.tEOC:
                    end = consume().getEndOffset();
                    break;
                default:
                    throw backtrack;
                }
                break;
            case IToken.tEOC:
                end = consume().getEndOffset();
                break;
            default:
                throw backtrack;
            }

            ICPPASTConstructorChainInitializer ctorInitializer = createConstructorChainInitializer();
            ((ASTNode) ctorInitializer).setOffsetAndLength(duple.getStartOffset(), end - duple.getStartOffset());
            ctorInitializer.setMemberInitializerId(name);

            if (expressionList != null) {
                ctorInitializer.setInitializerValue(expressionList);
            }
            collection.add(ctorInitializer);

            switch (LT(1)) {
            case IToken.tCOMMA:
                consume();
                break;
            case IToken.tLBRACE:
            case IToken.tEOC:
                break ctorLoop;
            }
        }
    }

    protected ICPPASTConstructorChainInitializer createConstructorChainInitializer() {
        return new CPPASTConstructorChainInitializer();
    }

    /**
     * This routine parses a parameter declaration
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICPPASTParameterDeclaration parameterDeclaration() throws BacktrackException, EndOfFileException {
        final int startOffset= LA(1).getOffset();
        
		if (LT(1) == IToken.tLBRACKET && supportParameterInfoBlock) {
			skipBrackets(IToken.tLBRACKET, IToken.tRBRACKET);
		}
		
        IASTDeclSpecifier declSpec;
        IASTDeclarator declarator;
        try {
        	declSpec= declSpecifierSeq(DeclarationOptions.PARAMETER);
        	declarator= initDeclarator(declSpec, DeclarationOptions.PARAMETER);
        } catch (FoundDeclaratorException e) {
        	declSpec= e.declSpec;
        	declarator= e.declarator;
        	backup(e.currToken);
        }

        final ICPPASTParameterDeclaration parm = createParameterDeclaration();
        parm.setDeclSpecifier(declSpec);
        parm.setDeclarator(declarator);
        
        final int endOffset = figureEndOffset(declSpec, declarator);
        ((ASTNode) parm).setOffsetAndLength(startOffset, endOffset - startOffset);
        return parm;
    }

    protected ICPPASTParameterDeclaration createParameterDeclaration() {
        return new CPPASTParameterDeclaration();
    }

    
	private final static int INLINE=0x1, CONST=0x2, RESTRICT=0x4, VOLATILE=0x8, 
    SHORT=0x10,	UNSIGNED= 0x20, SIGNED=0x40, COMPLEX=0x80, IMAGINARY=0x100,
    VIRTUAL=0x200, EXPLICIT=0x400, FRIEND=0x800;


    /**
     * This function parses a declaration specifier sequence, as according to
     * the ANSI C++ specification. 
     * declSpecifier : 
     * 		"auto" | "register" | "static" | "extern" | "mutable" | 
     * 		"inline" | "virtual" | "explicit" | 
     * 		"typedef" | "friend" | 
     * 		"const" | "volatile" | 
     * 		"short" | "long" | "signed" | "unsigned" | "int" |
     * 		"char" | "wchar_t" | "bool" | "float" | "double" | "void" | 
     * 		("typename")? name | 
     * 		{ "class" | "struct" | "union" } classSpecifier | 
     * 		{"enum"} enumSpecifier
     */
    @Override
	protected ICPPASTDeclSpecifier declSpecifierSeq(final DeclarationOptions option)
    		throws BacktrackException, EndOfFileException, FoundDeclaratorException {
        int storageClass = IASTDeclSpecifier.sc_unspecified;
        int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
        int options= 0;
        int isLong= 0;

        ITokenDuple identifier= null;
        ICPPASTDeclSpecifier result= null;
        IASTExpression typeofExpression= null;
        IASTProblem problem= null;

        boolean isTypename = false;
        boolean encounteredRawType= false;
        boolean encounteredTypename= false;

        final int offset = LA(1).getOffset();
        int endOffset= offset;

        declSpecifiers: for (;;) {
        	final int lt1= LTcatchEOF(1);
            switch (lt1) {
            case 0: // encountered eof
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
            case IToken.t_mutable:
                storageClass = ICPPASTDeclSpecifier.sc_mutable;
                endOffset= consume().getEndOffset();
                break;
            case IToken.t_typedef:
                storageClass = IASTDeclSpecifier.sc_typedef;
                endOffset= consume().getEndOffset();
                break;
            // function specifiers
            case IToken.t_inline:
            	options |= INLINE;
            	endOffset= consume().getEndOffset();
                break;
            case IToken.t_virtual:
                options |= VIRTUAL;
                endOffset= consume().getEndOffset();
                break;
            case IToken.t_explicit:
                options |= EXPLICIT;
                endOffset= consume().getEndOffset();
                break;
            case IToken.t_friend:
                options |= FRIEND;
                endOffset= consume().getEndOffset();
                break;
            // type specifier
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
            case IToken.t_short:
            	if (encounteredTypename)
            		break declSpecifiers;
                options |= SHORT;
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
            case IToken.t_char:
            	if (encounteredTypename)
            		break declSpecifiers;
                simpleType = IASTSimpleDeclSpecifier.t_char;
                encounteredRawType= true;
                endOffset= consume().getEndOffset();
                break;
            case IToken.t_wchar_t:
            	if (encounteredTypename)
            		break declSpecifiers;
                simpleType = ICPPASTSimpleDeclSpecifier.t_wchar_t;
                encounteredRawType= true;
                endOffset= consume().getEndOffset();
                break;
            case IToken.t_bool:
            	if (encounteredTypename)
            		break declSpecifiers;
                simpleType = ICPPASTSimpleDeclSpecifier.t_bool;
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
            case IToken.t_void:
            	if (encounteredTypename)
            		break declSpecifiers;
                simpleType = IASTSimpleDeclSpecifier.t_void;
                encounteredRawType= true;
                endOffset= consume().getEndOffset();
                break;
            case IToken.t_typename:
            	if (encounteredTypename || encounteredRawType)
            		break declSpecifiers;
            	consume();
                identifier= name();
                endOffset= identifier.getLastToken().getEndOffset();
                isTypename = true;
                encounteredTypename= true;
                break;
            case IToken.tBITCOMPLEMENT:
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
                if (encounteredRawType || encounteredTypename)
                    break declSpecifiers;

                try {
                	if (option.fAllowEmptySpecifier && LT(1) != IToken.tCOMPLETION) {
                		lookAheadForDeclarator(option);
                	}
                } catch (FoundDeclaratorException e) {
                	if (e.currToken.getType() == IToken.tEOC || option == DeclarationOptions.FUNCTION_STYLE_ASM 
                			|| canBeConstructorDestructorOrConversion(option, storageClass, options, e.declarator)) {
                		e.declSpec= createSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset, endOffset);
                		throw e;
                	}
                }

                identifier= name();
                if (identifier.getLastToken().getType() == IToken.tCOLONCOLON)
                	throwBacktrack(identifier.getLastToken());
                
                endOffset= identifier.getLastToken().getEndOffset();
                encounteredTypename= true;
                break;
            case IToken.t_class:
            case IToken.t_struct:
            case IToken.t_union:
                if (encounteredTypename || encounteredRawType)
                    break declSpecifiers;
                try {
                    result= classSpecifier();
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
                    result= (ICPPASTDeclSpecifier) enumSpecifier();
                } catch (BacktrackException bt) {
                	if (bt.getNodeBeforeProblem() instanceof ICPPASTDeclSpecifier) {
                		result= (ICPPASTDeclSpecifier) bt.getNodeBeforeProblem();
                		problem= bt.getProblem();
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

            	final boolean wasInBinary= inBinaryExpression;
            	try {
            		inBinaryExpression= false;
            		typeofExpression= parseTypeidInParenthesisOrUnaryExpression(false, consume().getOffset(), 
            				IGNUASTTypeIdExpression.op_typeof, IGNUASTUnaryExpression.op_typeof);
            	} finally {
            		inBinaryExpression= wasInBinary;
            	}

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
        if (!encounteredRawType && !encounteredTypename && LT(1) != IToken.tEOC && !option.fAllowEmptySpecifier) {
        	throwBacktrack(LA(1));
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
            ((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
            if (problem != null) {
            	throwBacktrack(problem, result);
            }
            return result;
        }

        if (identifier != null) 
            return createNamedTypeSpecifier(identifier, isTypename, storageClass, options, offset, endOffset);
        
        return createSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset, endOffset);
    }

	private boolean canBeConstructorDestructorOrConversion(DeclarationOptions declOption, int storageClass, int options, IASTDeclarator dtor) {
		final int forbid= CONST | RESTRICT | VOLATILE | SHORT | UNSIGNED | SIGNED | COMPLEX | IMAGINARY | FRIEND;
		if (storageClass == IASTDeclSpecifier.sc_unspecified && (options & forbid) == 0) {
			if (CPPVisitor.findTypeRelevantDeclarator(dtor) instanceof IASTFunctionDeclarator) {
				IASTName name= CPPVisitor.findInnermostDeclarator(dtor).getName();
				if (name instanceof ICPPASTQualifiedName) {
					final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
					final IASTName names[]= qname.getNames();
					final int len = names.length;
					if (len > 1 && CharArrayUtils.equals(names[len-2].toCharArray(), names[len-1].toCharArray())) 
						return true; // constructor
					
					name= qname.getLastName();
				}
				if (name instanceof ICPPASTConversionName)
					return true; // conversion
				if (name instanceof ICPPASTTemplateId) {
					if (((ICPPASTTemplateId) name).getTemplateName() instanceof ICPPASTConversionName) {
						return true;
					}
					
				}
				final char[] nchars= name.toCharArray();
				if (nchars.length > 0 && nchars[0] == '~') 
					return true; // destructor
				
				if (declOption == DeclarationOptions.CPP_MEMBER && CharArrayUtils.equals(nchars, currentClassName))
					return true;
			}
		}
		return false;
	}

	private ICPPASTNamedTypeSpecifier createNamedTypeSpecifier(ITokenDuple identifier, boolean isTypename,
			int storageClass, int options, int offset, int endOffset) {
		ICPPASTNamedTypeSpecifier declSpec = (ICPPASTNamedTypeSpecifier)createNamedTypeSpecifier();
		IASTName name = createName(identifier);
		declSpec.setName(name);
		declSpec.setIsTypename(isTypename);
		configureDeclSpec(declSpec, storageClass, options);
        ((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
        return declSpec;
	}

	private ICPPASTSimpleDeclSpecifier createSimpleDeclSpec(int storageClass, int simpleType,
			int options, int isLong, IASTExpression typeofExpression, int offset, int endOffset) {

		if (isLong > 1 && !supportLongLong)
			isLong= 1;
		
		ICPPASTSimpleDeclSpecifier declSpec= null;
        if (isLong > 1 || (options & (RESTRICT|COMPLEX|IMAGINARY)) != 0 || typeofExpression != null) {
        	final IGPPASTSimpleDeclSpecifier gppDeclSpec= createGPPSimpleDeclSpecifier();
            gppDeclSpec.setLongLong(isLong > 1);
            gppDeclSpec.setRestrict((options & RESTRICT) != 0);
            gppDeclSpec.setComplex((options & COMPLEX) != 0);
            gppDeclSpec.setImaginary((options & IMAGINARY) != 0);
            gppDeclSpec.setTypeofExpression(typeofExpression);

        	declSpec= gppDeclSpec;
        } else {
        	declSpec = createSimpleDeclSpecifier();
        }
        
        configureDeclSpec(declSpec, storageClass, options);

        declSpec.setType(simpleType);
        declSpec.setLong(isLong == 1);
        declSpec.setShort((options & SHORT) != 0);
        declSpec.setUnsigned((options & UNSIGNED) != 0);
        declSpec.setSigned((options & SIGNED) != 0);

        ((ASTNode) declSpec).setOffsetAndLength(offset, endOffset-offset);
        return declSpec;
    }

	private void configureDeclSpec(ICPPASTDeclSpecifier declSpec, int storageClass, int options) {
		declSpec.setStorageClass(storageClass);
		declSpec.setConst((options & CONST) != 0);
		declSpec.setVolatile((options & VOLATILE) != 0);
		declSpec.setInline((options & INLINE) != 0);
        declSpec.setFriend((options & FRIEND) != 0);
        declSpec.setVirtual((options & VIRTUAL) != 0);
        declSpec.setExplicit((options & EXPLICIT) != 0);
	}

	@Override
	protected boolean verifyLookaheadDeclarator(DeclarationOptions option, IASTDeclarator dtor, IToken nextToken) {
        switch (nextToken.getType()) {
        case IToken.tCOMMA:
        	return true;
        	
        case IToken.tCOLON:
        case IToken.t_try:
        case IToken.t_catch:
        case IToken.tLBRACE:
        case IToken.t_const:
        case IToken.t_volatile:
        	if (option == DeclarationOptions.GLOBAL || option == DeclarationOptions.CPP_MEMBER
        			|| option == DeclarationOptions.FUNCTION_STYLE_ASM) {
        		if (CVisitor.findTypeRelevantDeclarator(dtor) instanceof IASTFunctionDeclarator) {
        			return true;
        		}
        	}
        	break;
        case IToken.tSEMI:
        	return option == DeclarationOptions.GLOBAL || option == DeclarationOptions.CPP_MEMBER ||
        	option == DeclarationOptions.LOCAL;

        case IToken.tRPAREN:
        	return option == DeclarationOptions.PARAMETER;
        	
        case IToken.tEOC:
        	return true;
        }
        return false;
	}

    protected IGPPASTSimpleDeclSpecifier createGPPSimpleDeclSpecifier() {
        return new GPPASTSimpleDeclSpecifier();
    }


    protected ICPPASTSimpleDeclSpecifier createSimpleDeclSpecifier() {
        return new CPPASTSimpleDeclSpecifier();
    }


    @Override
	protected IASTNamedTypeSpecifier createNamedTypeSpecifier() {
        return new CPPASTNamedTypeSpecifier();
    }

    /**
     * Parse an elaborated type specifier.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier()
            throws BacktrackException, EndOfFileException {
        // this is an elaborated class specifier
        IToken t = consume();
        int eck = 0;

        switch (t.getType()) {
        case IToken.t_class:
            eck = ICPPASTElaboratedTypeSpecifier.k_class;
            break;
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

        IASTName name = createName(name());

        ICPPASTElaboratedTypeSpecifier elaboratedTypeSpec = createElaboratedTypeSpecifier();
        ((ASTNode) elaboratedTypeSpec).setOffsetAndLength(t.getOffset(), calculateEndOffset(name) - t.getOffset());
        elaboratedTypeSpec.setKind(eck);
        elaboratedTypeSpec.setName(name);
        return elaboratedTypeSpec;
    }


    protected ICPPASTElaboratedTypeSpecifier createElaboratedTypeSpecifier() {
        return new CPPASTElaboratedTypeSpecifier();
    }

    @Override
	protected IASTDeclarator initDeclarator(DeclarationOptions option) throws EndOfFileException, BacktrackException {
    	// called from the lookahead, only.
    	return initDeclarator(DtorStrategy.PREFER_FUNCTION, option);
    }
    
	protected IASTDeclarator initDeclarator(IASTDeclSpecifier declspec, DeclarationOptions option) throws EndOfFileException, BacktrackException {
    	final IToken mark= mark();
    	IASTDeclarator dtor1= null;
    	IToken end1= null;
    	IASTDeclarator dtor2= null;
    	BacktrackException bt= null;
    	try {
    		dtor1= initDeclarator(DtorStrategy.PREFER_FUNCTION, option);
    		if (dtor1 instanceof IASTFunctionDeclarator == false)
    			return dtor1;
    		
    		// optimization outside of function bodies and inside of templates
    		if (functionBodyCount == 0 || templateCount != 0) 
    			return dtor1;
    		
    		// avoid second option for function definitions
    		end1= LA(1);
    		switch(end1.getType()) {
    		case IToken.tLBRACE: case IToken.tCOLON:
    		case IToken.t_throw: case IToken.t_try:
    		case IToken.t_const: case IToken.t_volatile:
    			return dtor1;
    		}
    	} catch (BacktrackException e) {
    		bt= e;
    	}
    	
    	if (!option.fAllowConstructorInitializer || !canHaveConstructorInitializer(declspec)) {
    		if (bt != null)
    			throw bt;
    		return dtor1;
    	}

    	backup(mark);
    	try {
    		dtor2= initDeclarator(DtorStrategy.PREFER_NESTED, option);
    		if (dtor1 == null) { 
    			return dtor2;
    		}
    	} catch (BacktrackException e) {
    		if (dtor1 != null) {
    			backup(end1);
    			return dtor1;
    		}
    		throw e;
    	}
    	
		// we have an ambiguity
		if (end1 != null && LA(1).getEndOffset() != end1.getEndOffset()) {
			backup(end1);
			return dtor1;
		}
		
		CPPASTAmbiguousDeclarator dtor= new CPPASTAmbiguousDeclarator(dtor2, dtor1);
		dtor.setOffsetAndLength((ASTNode) dtor1);
		return dtor;
    }

	private boolean canHaveConstructorInitializer(IASTDeclSpecifier declspec) {
		if (declspec instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier sspec= (ICPPASTSimpleDeclSpecifier) declspec;
			switch(sspec.getType()) {
			case IASTSimpleDeclSpecifier.t_unspecified:
				if (sspec.isLong() || sspec.isShort() || sspec.isSigned() || sspec.isUnsigned())
					return true;
				if (sspec instanceof IGPPASTSimpleDeclSpecifier) {
					final IGPPASTSimpleDeclSpecifier gspec = (IGPPASTSimpleDeclSpecifier) sspec;
					if (gspec.isLongLong())
						return true;
				}
				return false;
				
			case IASTSimpleDeclSpecifier.t_void:
				return false;
			}
			
			if (sspec.isFriend()) {
				return false;
			}
			if (sspec.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				return false;
			}
		}
		return true;
	}

	/**
     * Parses the initDeclarator construct of the ANSI C++ spec. initDeclarator :
     * declarator ("=" initializerClause | "(" expressionList ")")?
     * 
     * @return declarator that this parsing produced.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTDeclarator initDeclarator(DtorStrategy strategy, DeclarationOptions option)
            throws EndOfFileException, BacktrackException {
    	final IASTDeclarator dtor= declarator(strategy, option);
        if (option.fAllowInitializer) {
        	IASTInitializer initializer= optionalCPPInitializer(dtor);
        	if (initializer != null) {
        		dtor.setInitializer(initializer);
        		adjustLength(dtor, initializer);
        	}
        }
        return dtor;
    }

    protected IASTInitializer optionalCPPInitializer(IASTDeclarator d)
            throws EndOfFileException, BacktrackException {
        // handle initializer

        if (LT(1) == IToken.tASSIGN) {
            consume();
            try {
                return initializerClause();
            } catch (EndOfFileException eof) {
                failParse();
                throw eof;
            }
        } else if (LT(1) == IToken.tLPAREN) {
            if (d instanceof IASTFunctionDeclarator
                    && d.getNestedDeclarator() == null) {
                // constructor initializer doesn't make sense for a function
                // declarator,
                // we must have an object to initialize, a function doesn't
                // work.
                return null;
            }
            // initializer in constructor
            IToken t = consume(); // EAT IT!
            int o = t.getOffset();
            IASTExpression astExpression = expression();
            if( astExpression == null )
                throwBacktrack( t );
            int l = consumeOrEOC(IToken.tRPAREN).getEndOffset();
            ICPPASTConstructorInitializer result = createConstructorInitializer();
            ((ASTNode) result).setOffsetAndLength(o, l - o);
            result.setExpression(astExpression);
            return result;
        }
        return null;
    }


    protected ICPPASTConstructorInitializer createConstructorInitializer() {
        return new CPPASTConstructorInitializer();
    }


    protected IASTInitializer initializerClause() throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.tLBRACE) {
            int startingOffset = consume().getOffset();

            IASTInitializerList result = createInitializerList();
            ((ASTNode) result).setOffset(startingOffset);

            if (LT(1) == (IToken.tRBRACE)) {
                int l = consume().getEndOffset();
                ((ASTNode) result).setLength(l - startingOffset);
                return result;
            }

            // otherwise it is a list of initializer clauses

            for (;;) {
                if (LT(1) == IToken.tRBRACE)
                    break;

                IASTInitializer clause = initializerClause();
                if (clause != null) {
                    result.addInitializer(clause);
                }
                if (LT(1) == IToken.tRBRACE || LT(1) == IToken.tEOC)
                    break;
                consume(IToken.tCOMMA);
            }
            int l = consume().getEndOffset(); // tRBRACE
            ((ASTNode) result).setLength(l - startingOffset);
            return result;
        }

        // if we get this far, it means that we did not
        // try this now instead
        // assignmentExpression
        IASTExpression assignmentExpression = assignmentExpression();
        IASTInitializerExpression result = createInitializerExpression();
        ((ASTNode) result).setOffsetAndLength(((ASTNode) assignmentExpression));
        result.setExpression(assignmentExpression);
        return result;
    }


    protected IASTInitializerList createInitializerList() {
        return new CPPASTInitializerList();
    }


    protected IASTInitializerExpression createInitializerExpression() {
        return new CPPASTInitializerExpression();
    }

    /**
     * Parse a declarator, as according to the ANSI C++ specification.
     * declarator : (ptrOperator)* directDeclarator 
     * directDeclarator : 
     *    declaratorId | 
     *    directDeclarator "(" parameterDeclarationClause ")" (cvQualifier)* (exceptionSpecification)* | 
     *    directDeclarator "[" (constantExpression)? "]" | 
     *    "(" declarator")" | 
     *    directDeclarator "(" parameterDeclarationClause ")" (oldKRParameterDeclaration)* 
     *    
     * declaratorId : name
     * @return declarator that this parsing produced.
     * @throws BacktrackException
     *             request a backtrack
     */
    protected IASTDeclarator declarator(DtorStrategy strategy, DeclarationOptions option)
            throws EndOfFileException, BacktrackException {

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
        switch (lt1) {
        case IToken.tBITCOMPLEMENT:
        case IToken.t_operator:
        case IToken.tCOLONCOLON:
        case IToken.tIDENTIFIER:
        case IToken.tCOMPLETION:
        	if (option.fRequireAbstract)
        		throwBacktrack(LA(1));
        	
        	final IASTName declaratorName= consumeTemplatedOperatorName();
        	endOffset= calculateEndOffset(declaratorName);
        	return declarator(pointerOps, declaratorName, null, startingOffset, endOffset, strategy, option);
        } 
            
        if (lt1 == IToken.tLPAREN) {
        	IASTDeclarator cand1= null;
        	IToken cand1End= null;
        	// try an abstract function declarator 
        	if (option.fAllowAbstract && option.fAllowFunctions) {
        		final IToken mark= mark();
        		try {
        			cand1= declarator(pointerOps, createName(), null, startingOffset, endOffset, strategy, option);
        			if (option.fRequireAbstract || !option.fAllowNested) 
        				return cand1;

        			cand1End= LA(1);
        		} catch (BacktrackException e) {
        		}
        		backup(mark);
        	}
        	
        	// type-ids for new or operator-id:
        	if (!option.fAllowNested) {
        		if (option.fAllowAbstract) {
        			return declarator(pointerOps, createName(), null, startingOffset, endOffset, strategy, option);
        		}
        		throwBacktrack(LA(1));
        	}
        	
        	// try a nested declarator
        	try {
        		consume();
        		if (LT(1) == IToken.tRPAREN)
        			throwBacktrack(LA(1));

        		final IASTDeclarator nested= declarator(DtorStrategy.PREFER_FUNCTION, option);
        		endOffset= consume(IToken.tRPAREN).getEndOffset();
        		final IASTDeclarator cand2= declarator(pointerOps, null, nested, startingOffset, endOffset, strategy, option);
        		if (cand1 == null || cand1End == null)
        			return cand2;
        		final IToken cand2End= LA(1);
        		if (cand1End == cand2End) {
        			CPPASTAmbiguousDeclarator result= new CPPASTAmbiguousDeclarator(cand1, cand2);
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
        	// bit-fields may be abstract
        	if (!option.fAllowBitField || LT(1) != IToken.tCOLON)
        		throwBacktrack(LA(1));
        }
        return declarator(pointerOps, createName(), null, startingOffset, endOffset, strategy, option);
    }

    private IASTDeclarator declarator(List<IASTPointerOperator> pointerOps,
    		IASTName declaratorName, IASTDeclarator nestedDeclarator, int startingOffset, int endOffset,
    		DtorStrategy strategy, DeclarationOptions option) 
    		throws EndOfFileException, BacktrackException {
        IASTDeclarator result= null;
        loop: while(true) {
        	final int lt1= LT(1);
        	switch (lt1) {
        	case IToken.tLPAREN:
        		if (option.fAllowFunctions && strategy == DtorStrategy.PREFER_FUNCTION) {
        			result= functionDeclarator();
        			setDeclaratorID(result, declaratorName, nestedDeclarator);
        		}
        		break loop;
        		
        	case IToken.tLBRACKET:
        		result= arrayDeclarator(option);
        		setDeclaratorID(result, declaratorName, nestedDeclarator);
        		break loop;
        		
        	case IToken.tCOLON:
        		if (!option.fAllowBitField)
        			break loop;	// no backtrack because typeid can be followed by colon
        		
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
		__attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);

        if (result == null) {
        	result= createDeclarator();
        	setDeclaratorID(result, declaratorName, nestedDeclarator);
        } else {
        	endOffset= calculateEndOffset(result);
        }

        if (LT(1) == IToken.t_asm) { // asm labels bug 226121
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

	private void setDeclaratorID(IASTDeclarator declarator, IASTName declaratorName, IASTDeclarator nestedDeclarator) {
		if (nestedDeclarator != null) { 
			declarator.setNestedDeclarator(nestedDeclarator);
			declarator.setName(createName());
		} else {
			declarator.setName(declaratorName);
		}
	}
    
    /**
     * Parse a function declarator starting with the left parenthesis.
	 */
	private IASTDeclarator functionDeclarator() throws EndOfFileException, BacktrackException {
		IToken last = consume(IToken.tLPAREN);
		int startOffset= last.getOffset();
		boolean seenParameter= false;
		boolean encounteredVarArgs= false;
		List<IASTParameterDeclaration> parameters= null;
		int endOffset= last.getEndOffset();
		
		paramLoop: while(true) {
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
			
				IASTParameterDeclaration pd = parameterDeclaration();
				endOffset = calculateEndOffset(pd);
				if (parameters == null)
					parameters = new ArrayList<IASTParameterDeclaration>(DEFAULT_PARAMETER_LIST_SIZE);
				parameters.add(pd);
				seenParameter = true;
				break;
			}
		}

		// Consume any number of __attribute__ tokens after the parameters
		__attribute_decl_seq(supportAttributeSpecifiers, false);

		boolean isTryCatch= false;
		boolean isConst= false;
		boolean isVolatile= false;
		boolean isPureVirtual= false;
		ArrayList<IASTTypeId> exceptionSpecIds= null;

		if (LT(1) == IToken.t_try) {
			isTryCatch= true;
		} else {
			// cv-qualifiers
			cvloop: while(true) {
				switch(LT(1)) {
				case IToken.t_const:
					isConst= true;
					endOffset= consume().getEndOffset();
					break;
				case IToken.t_volatile:
					isVolatile= true;
					endOffset= consume().getEndOffset();
					break;
				default:
					break cvloop;
				}
			}

			// throws clause
			if (LT(1) == IToken.t_throw) {
				exceptionSpecIds = new ArrayList<IASTTypeId>(DEFAULT_SIZE_EXCEPTIONS_LIST);
				consume(); // throw
				consume(IToken.tLPAREN); 

				thloop: while(true) {
					switch (LT(1)) {
					case IToken.tRPAREN:
					case IToken.tEOC:
						endOffset= consume().getEndOffset();
						break thloop;
					case IToken.tCOMMA:
						consume();
						break;
					default:
						int thoffset= LA(1).getOffset();
						IASTTypeId typeId= typeId(DeclarationOptions.TYPEID);
						if (typeId != null) {
							exceptionSpecIds.add(typeId);
						} else {
							int thendoffset= LA(1).getOffset();
							if (thoffset == thendoffset) {
								thendoffset= consume().getEndOffset();
							}
							IASTProblem p= createProblem(IProblem.SYNTAX_ERROR, thoffset, thendoffset-thoffset);
							IASTProblemTypeId typeIdProblem = createTypeIDProblem();
							typeIdProblem.setProblem(p);
							((ASTNode) typeIdProblem).setOffsetAndLength(((ASTNode) p));
							exceptionSpecIds.add(typeIdProblem);
						}
						break;
					}
				}

				// more __attribute__ after throws
				__attribute_decl_seq(supportAttributeSpecifiers, false);
			}
		
			// pure virtual
			if (LT(1) == IToken.tASSIGN && LT(2) == IToken.tINTEGER) {
				char[] image = LA(2).getCharImage();
				if (image.length == 1 && image[0] == '0') {
					consume(); // tASSIGN
					endOffset= consume().getEndOffset(); // tINTEGER
					isPureVirtual= true;
				}
			}
		}

		final ICPPASTFunctionDeclarator fc= isTryCatch ? createTryBlockDeclarator() : createFunctionDeclarator();
		fc.setVarArgs(encounteredVarArgs);
	    fc.setConst(isConst);
	    fc.setVolatile(isVolatile);
	    fc.setPureVirtual(isPureVirtual);
	    if (parameters != null) {
	    	for (IASTParameterDeclaration param : parameters) {
	    		fc.addParameterDeclaration(param);
	    	}
	    }
	    if (exceptionSpecIds != null)
		for (IASTTypeId exception : exceptionSpecIds) {
	        fc.addExceptionSpecificationTypeId(exception);
		}
        ((ASTNode) fc).setOffsetAndLength(startOffset, endOffset-startOffset);
        return fc;
	}

	/**
	 * Parse an array declarator starting at the square bracket.
	 */
	private IASTArrayDeclarator arrayDeclarator(DeclarationOptions option) throws EndOfFileException, BacktrackException {
		ArrayList<IASTArrayModifier> arrayMods = new ArrayList<IASTArrayModifier>(DEFAULT_POINTEROPS_LIST_SIZE);
		int start= LA(1).getOffset();
		consumeArrayModifiers(option, arrayMods);
		if (arrayMods.isEmpty())
			throwBacktrack(LA(1));
		
		final int endOffset = calculateEndOffset(arrayMods.get(arrayMods.size() - 1));
		final IASTArrayDeclarator d = createArrayDeclarator();
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
		
        IASTFieldDeclarator d = createFieldDeclarator();
        d.setBitFieldSize(bitField);

        ((ASTNode) d).setOffsetAndLength(start, endOffset-start);
		return d;
	}

    protected IASTProblemTypeId createTypeIDProblem() {
        return new CPPASTProblemTypeId();
    }


    protected ICPPASTFunctionTryBlockDeclarator createTryBlockDeclarator() {
        return new CPPASTFunctionTryBlockDeclarator();
    }


    protected ICPPASTFunctionDeclarator createFunctionDeclarator() {
        return new CPPASTFunctionDeclarator();
    }


    protected IASTFieldDeclarator createFieldDeclarator() {
        return new CPPASTFieldDeclarator();
    }


    protected IASTArrayDeclarator createArrayDeclarator() {
        return new CPPASTArrayDeclarator();
    }


    protected IASTDeclarator createDeclarator() {
        return new CPPASTDeclarator();
    }

    protected IASTName consumeTemplatedOperatorName()
            throws EndOfFileException, BacktrackException {
        TemplateParameterManager argumentList = TemplateParameterManager.getInstance();
        try {
            if (LT(1) == IToken.t_operator)
                return operatorId(null, null);

            try {
                return createName(name());
            } catch (BacktrackException bt) {
            }
            IToken start = null;

            IToken mark = mark();
            if (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                start = consume();
                IToken end = null;

                if (start.getType() == IToken.tIDENTIFIER) {
                    end = consumeTemplateArguments(end, argumentList);
                }

                while (LT(1) == IToken.tCOLONCOLON || LT(1) == IToken.tIDENTIFIER) {
                    end = consume();
                    if (end.getType() == IToken.tIDENTIFIER) {
                        end = consumeTemplateArguments(end, argumentList);
                    }
                }

                if (LT(1) == IToken.t_operator) {
                    return operatorId(start, argumentList);
                }

                int endOffset = (end != null) ? end.getEndOffset() : 0;
                backup(mark);
                throwBacktrack(mark.getOffset(), endOffset - mark.getOffset());
            }
            int endOffset= mark.getEndOffset();
            backup(mark);
            throwBacktrack(mark.getOffset(), endOffset - mark.getOffset());

            return null;
        } finally {
            TemplateParameterManager.returnInstance(argumentList);
        }
    }

    /**
     * Parse a class/struct/union definition. classSpecifier : classKey name
     * (baseClause)? "{" (memberSpecification)* "}"
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICPPASTCompositeTypeSpecifier classSpecifier() throws BacktrackException, EndOfFileException {
        int classKind = 0;
        IToken mark = mark();
        final int offset= mark.getOffset();

        // class key
        switch (LT(1)) {
        case IToken.t_class:
            consume();
            classKind = ICPPASTCompositeTypeSpecifier.k_class;
            break;
        case IToken.t_struct:
        	consume();
            classKind = IASTCompositeTypeSpecifier.k_struct;
            break;
        case IToken.t_union:
            consume();
            classKind = IASTCompositeTypeSpecifier.k_union;
            break;
        default:
            throwBacktrack(mark);
        	return null; // line is never reached, hint for the parser
        }

        // if __attribute__ or __declspec occurs after struct/union/class and before the identifier        
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        // class name
        IASTName name = null;
        if (LT(1) == IToken.tIDENTIFIER)
            name = createName(name());
        else
            name = createName();
        
        // if __attribute__ or __declspec occurs after struct/union/class identifier and before the { or ;
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        ICPPASTCompositeTypeSpecifier astClassSpecifier = createClassSpecifier();
        astClassSpecifier.setKey(classKind);
        astClassSpecifier.setName(name);

        // base clause
        if (LT(1) == IToken.tCOLON) {
            baseSpecifier(astClassSpecifier);
            // content assist within the base-clause
            if (LT(1) == IToken.tEOC) {
            	return astClassSpecifier;
            }
        }

        if (LT(1) != IToken.tLBRACE) {
            IToken errorPoint = LA(1);
            backup(mark);
            throwBacktrack(errorPoint);
        }
        mark= null;  // don't hold on to tokens while parsing the members.


        int endOffset= consume().getEndOffset();
        final char[] outerName= currentClassName;
        if (name instanceof ICPPASTQualifiedName) {
        	currentClassName= ((ICPPASTQualifiedName)name).getLastName().toCharArray();
        } else {
        	currentClassName= name.toCharArray();
        }
        try {
        	int declOffset= -1;
        	loop: while (true) {
        		BacktrackException origBackTrack= null;
        		try {
        			IToken next= LAcatchEOF(1);
        			if (next == null || next.getType() == IToken.tEOC)
        				break loop; // the missing semicolon will cause a problem, just break the loop.

        			if (next.getType() == IToken.tRBRACE) {
        				endOffset= consume().getEndOffset();
        				break loop;
        			}

        			final int nextOffset = next.getOffset();
        			declarationMark= next;
        			next= null; // don't hold on to the token while parsing namespaces, class bodies, etc.

        			IASTDeclaration d;
        			if (declOffset == nextOffset) {
        				// no progress
        				d= skipProblemDeclaration(declOffset);
        			} else {
        				declOffset= nextOffset;
        				switch (LT(1)) {
        				case IToken.t_public:
        				case IToken.t_protected:
        				case IToken.t_private: 
        					int key= consume().getType();
        					endOffset= consume(IToken.tCOLON).getEndOffset();
        					ICPPASTVisibilityLabel label = createVisibilityLabel();
        					label.setVisibility(token2Visibility(key));
        					((ASTNode) label).setOffsetAndLength(declOffset, endOffset - declOffset);
        					astClassSpecifier.addMemberDeclaration(label);
        					continue loop;
        				}
        				try {
        					d= declaration(DeclarationOptions.CPP_MEMBER);
        				} catch (BacktrackException e) {
        					if (declarationMark == null)
        						throw e;
        					origBackTrack= new BacktrackException(e);
        					backup(declarationMark);
        					d= usingDeclaration(declarationMark.getOffset());
        				}
        			}
        			astClassSpecifier.addMemberDeclaration(d);
        			endOffset= calculateEndOffset(d);
        		} catch (BacktrackException bt) {
        			if (origBackTrack != null) {
        				bt= origBackTrack;
        			}
        			IASTDeclaration[] decls= problemDeclaration(declOffset, bt, DeclarationOptions.CPP_MEMBER);
        			for (IASTDeclaration declaration : decls) {
        				astClassSpecifier.addMemberDeclaration(declaration);
        				endOffset= calculateEndOffset(declaration);
        			}
        		} catch (EndOfFileException e) {
        			astClassSpecifier.addMemberDeclaration(skipProblemDeclaration(declOffset));
        			endOffset= eofOffset;
        			break loop;
        		} finally {
        			declarationMark= null;
        		}
        	}
        } finally {
        	currentClassName= outerName;
        }
        ((ASTNode) astClassSpecifier).setOffsetAndLength(offset, endOffset - offset);
        return astClassSpecifier;
    }


    protected ICPPASTCompositeTypeSpecifier createClassSpecifier() {
        return new CPPASTCompositeTypeSpecifier();
    }


    protected ICPPASTVisibilityLabel createVisibilityLabel() {
        return new CPPASTVisibilityLabel();
    }


    protected int token2Visibility(int type) {
        switch (type) {
        case IToken.t_public:
            return ICPPASTVisibilityLabel.v_public;
        case IToken.t_protected:
            return ICPPASTVisibilityLabel.v_protected;
        case IToken.t_private:
            return ICPPASTVisibilityLabel.v_private;
        }
        return 0;
    }

    /**
     * Parse the subclass-baseclauses for a class specification. baseclause: :
     * basespecifierlist basespecifierlist: basespecifier basespecifierlist,
     * basespecifier basespecifier: ::? nestednamespecifier? classname virtual
     * accessspecifier? ::? nestednamespecifier? classname accessspecifier
     * virtual? ::? nestednamespecifier? classname accessspecifier: private |
     * protected | public
     * 
     * @throws BacktrackException
     */
    protected void baseSpecifier(ICPPASTCompositeTypeSpecifier astClassSpec)
            throws EndOfFileException, BacktrackException {

        IToken last = consume(); // tCOLON

        boolean isVirtual = false;
        int visibility = 0; // ASTAccessVisibility.PUBLIC;
        IASTName name = null;
        IToken firstToken = null;
        baseSpecifierLoop: for (;;) {
            switch (LT(1)) {
            case IToken.t_virtual:
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                isVirtual = true;
                break;
            case IToken.t_public:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_public;
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                break;
            case IToken.t_protected:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_protected;
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                break;
            case IToken.t_private:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_private;
                if (firstToken == null) {
                    firstToken = consume();
                    last = firstToken;
                } else
                    last = consume();
                break;
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
                // to get templates right we need to use the class as the scope
                ITokenDuple d = name();
                name = createName(d);
                if (firstToken == null)
                    firstToken = d.getFirstToken();
                last = d.getLastToken();
                break;
            case IToken.tCOMMA:
                if (name == null)
                    name = createName();
                consume();
                ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec = createBaseSpecifier();
                if (firstToken != null)
                    ((ASTNode) baseSpec).setOffsetAndLength(firstToken.getOffset(), last.getEndOffset() - firstToken.getOffset());
                baseSpec.setVirtual(isVirtual);
                baseSpec.setVisibility(visibility);
                baseSpec.setName(name);
                astClassSpec.addBaseSpecifier(baseSpec);

                isVirtual = false;
                visibility = 0;
                name = null;
                firstToken = null;

                continue baseSpecifierLoop;
            case IToken.tLBRACE:
            case IToken.tEOC:
                if (name == null)
                    name = createName();
                baseSpec = createBaseSpecifier();
                if (firstToken != null)
                    ((ASTNode) baseSpec).setOffsetAndLength(firstToken.getOffset(), last.getEndOffset() - firstToken.getOffset());
                baseSpec.setVirtual(isVirtual);
                baseSpec.setVisibility(visibility);
                baseSpec.setName(name);
                astClassSpec.addBaseSpecifier(baseSpec);
                break baseSpecifierLoop;
            default:
                break baseSpecifierLoop;
            }
        }
    }

    protected ICPPASTBaseSpecifier createBaseSpecifier() {
        return new CPPASTBaseSpecifier();
    }

    protected void catchHandlerSequence(List<ICPPASTCatchHandler> collection)
            throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.tEOC)
            return;
        
        if (LT(1) != IToken.t_catch) 
            throwBacktrack(LA(1)); // error, need at least one
        
		int lt1 = LT(1);
        while (lt1 == IToken.t_catch) {
            int startOffset = consume().getOffset();
            consume(IToken.tLPAREN);
            boolean isEllipsis = false;
            IASTDeclaration decl = null;
            try {
                if (LT(1) == IToken.tELLIPSIS) {
                    consume(IToken.tELLIPSIS);
                    isEllipsis = true;
                } else {
                	decl= simpleSingleDeclaration(DeclarationOptions.EXCEPTION);
                }
                if (LT(1) != IToken.tEOC)
                    consume(IToken.tRPAREN);
            } catch (BacktrackException bte) {
            	failParse();
                IASTProblem p = createProblem(bte);
                IASTProblemDeclaration pd = createProblemDeclaration();
                pd.setProblem(p);
                ((ASTNode) pd).setOffsetAndLength(((ASTNode) p));
                decl = pd;
            }

            ICPPASTCatchHandler handler = createCatchHandler();
            if (decl != null) {
                handler.setDeclaration(decl);
            }

            if (LT(1) != IToken.tEOC) {
                IASTStatement compoundStatement = catchBlockCompoundStatement();
                ((ASTNode) handler).setOffsetAndLength(startOffset,
                        calculateEndOffset(compoundStatement) - startOffset);
                handler.setIsCatchAll(isEllipsis);
                if (compoundStatement != null) {
                    handler.setCatchBody(compoundStatement);
                }
            }
            
            collection.add(handler);
			
			try {
				lt1 = LT(1);
			} catch (EndOfFileException eofe) {
				// if EOF is reached, then return here and let it be encountered elsewhere 
				// (i.e. try/catch won't be added to the declaration if the exception is thrown here)
				return; 
			}
        }
    }

	private IASTSimpleDeclaration simpleSingleDeclaration(DeclarationOptions options) throws BacktrackException,	EndOfFileException {
        final int startOffset= LA(1).getOffset();
    	IASTDeclSpecifier declSpec;
    	IASTDeclarator declarator;

    	try {
    		declSpec= declSpecifierSeq(options);
    		declarator= initDeclarator(declSpec, options);
    	} catch (FoundDeclaratorException e) {
    		declSpec= e.declSpec;
    		declarator= e.declarator;
    		backup(e.currToken);
    	}

    	final int endOffset = figureEndOffset(declSpec, declarator);
    	final IASTSimpleDeclaration decl= createSimpleDeclaration();
    	decl.setDeclSpecifier(declSpec);
    	decl.addDeclarator(declarator);
    	((ASTNode) decl).setOffsetAndLength(startOffset, endOffset - startOffset);
    	
    	return decl;
	}

    protected ICPPASTCatchHandler createCatchHandler() {
        return new CPPASTCatchHandler();
    }

    protected IASTStatement catchBlockCompoundStatement()
            throws BacktrackException, EndOfFileException {
        if (mode == ParserMode.QUICK_PARSE
                || mode == ParserMode.STRUCTURAL_PARSE) {
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last.getEndOffset() - curr.getOffset());
            return cs;
        } else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                return compoundStatement();
            IToken curr = LA(1);
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = createCompoundStatement();
            ((ASTNode) cs).setOffsetAndLength(curr.getOffset(), last.getEndOffset() - curr.getOffset());
            return cs;
        }
        return compoundStatement();
    }

	@Override
	protected void setupTranslationUnit() throws DOMException {
		translationUnit = createTranslationUnit();
		translationUnit.setIndex(index);

		// add built-in names to the scope
		// add built-in names to the scope
		if (builtinBindingsProvider != null) {
			IScope tuScope = translationUnit.getScope();
			
			IBinding[] bindings = builtinBindingsProvider.getBuiltinBindings(tuScope);
			for (IBinding binding : bindings) {
				ASTInternal.addBinding(tuScope, binding);
			}
		}
		translationUnit.setLocationResolver(scanner.getLocationResolver());
	}


    @Override
	protected IASTProblemDeclaration createProblemDeclaration() {
        return new CPPASTProblemDeclaration();
    }

    protected CPPASTTranslationUnit createTranslationUnit() {
        return new CPPASTTranslationUnit();
    }

    protected void consumeArrayModifiers(DeclarationOptions option, List<IASTArrayModifier> collection)
            throws EndOfFileException, BacktrackException {
    	boolean allowExpression= option == DeclarationOptions.TYPEID_NEW;
        while (LT(1) == IToken.tLBRACKET) {
            int o = consume().getOffset(); // eat the '['

            IASTExpression exp = null;
            if (LT(1) != IToken.tRBRACKET && LT(1) != IToken.tEOC) {
                exp = allowExpression ? expression() : constantExpression();
                allowExpression= false;
            }
            int l;
            switch (LT(1)) {
            case IToken.tRBRACKET:
            case IToken.tEOC:
                l = consume().getEndOffset();
                break;
            default:
                throw backtrack;
            }
            IASTArrayModifier arrayMod = createArrayModifier();
            ((ASTNode) arrayMod).setOffsetAndLength(o, l - o);
            if (exp != null) {
                arrayMod.setConstantExpression(exp);
            }
            collection.add(arrayMod);
        }
        return;
    }


    protected IASTArrayModifier createArrayModifier() {
        return new CPPASTArrayModifier();
    }

    @Override
	protected IASTTranslationUnit getTranslationUnit() {
        return translationUnit;
    }

    @Override
	protected IASTCompoundStatement createCompoundStatement() {
        return new CPPASTCompoundStatement();
    }

    @Override
	protected IASTBinaryExpression createBinaryExpression() {
        return new CPPASTBinaryExpression();
    }

    @Override
	protected IASTConditionalExpression createConditionalExpression() {
        return new CPPASTConditionalExpression();
    }

    @Override
	protected IASTUnaryExpression createUnaryExpression() {
        return new CPPASTUnaryExpression();
    }

    @Override
	protected IGNUASTCompoundStatementExpression createCompoundStatementExpression() {
        return new CPPASTCompoundStatementExpression();
    }

    @Override
	protected IASTExpressionList createExpressionList() {
        return new CPPASTExpressionList();
    }

    @Override
	protected IASTName createName(IToken token) {
        IASTName n = null;

        if (token instanceof OperatorTokenDuple) {
            n = createOperatorName((OperatorTokenDuple) token, n);
        } else {
            n = new CPPASTName(token.getCharImage());
        }

        switch (token.getType()) {
        case IToken.tCOMPLETION:
        case IToken.tEOC:
            createCompletionNode(token).addName(n);
            break;
        }
        ((ASTNode) n).setOffsetAndLength(token.getOffset(), token.getLength());

        return n;
    }

    @Override
	protected IASTName createName() {
        return new CPPASTName();
    }

    @Override
	protected IASTEnumerator createEnumerator() {
        return new CPPASTEnumerator();
    }


    @Override
	protected IASTExpression buildTypeIdExpression(int op, IASTTypeId typeId,
            int startingOffset, int endingOffset) {
        ICPPASTTypeIdExpression typeIdExpression = createTypeIdExpression();
        ((ASTNode) typeIdExpression).setOffsetAndLength(startingOffset, endingOffset - startingOffset);
        ((ASTNode) typeIdExpression).setLength(endingOffset - startingOffset);
        typeIdExpression.setOperator(op);
        typeIdExpression.setTypeId(typeId);
        return typeIdExpression;
    }

    protected ICPPASTTypeIdExpression createTypeIdExpression() {
        return new CPPASTTypeIdExpression();
    }

    @Override
	protected IASTEnumerationSpecifier createEnumerationSpecifier() {
        return new CPPASTEnumerationSpecifier();
    }

    @Override
	protected IASTLabelStatement createLabelStatement() {
        return new CPPASTLabelStatement();
    }

    @Override
	protected IASTGotoStatement createGoToStatement() {
        return new CPPASTGotoStatement();
    }

    @Override
	protected IASTReturnStatement createReturnStatement() {
        return new CPPASTReturnStatement();
    }

    protected ICPPASTForStatement createForStatement() {
        return new CPPASTForStatement();
    }

    @Override
	protected IASTContinueStatement createContinueStatement() {
        return new CPPASTContinueStatement();
    }

    @Override
	protected IASTDoStatement createDoStatement() {
        return new CPPASTDoStatement();
    }

    @Override
	protected IASTBreakStatement createBreakStatement() {
        return new CPPASTBreakStatement();
    }

    @Override
	protected IASTWhileStatement createWhileStatement() {
        return new CPPASTWhileStatement();
    }

    @Override
	protected IASTNullStatement createNullStatement() {
        return new CPPASTNullStatement();
    }

    protected ICPPASTSwitchStatement createSwitchStatement() {
        return new CPPASTSwitchStatement();
    }

    protected ICPPASTIfStatement createIfStatement() {
        return new CPPASTIfStatement();
    }

    @Override
	protected IASTDefaultStatement createDefaultStatement() {
        return new CPPASTDefaultStatement();
    }

    @Override
	protected IASTCaseStatement createCaseStatement() {
        return new CPPASTCaseStatement();
    }

    @Override
	protected IASTExpressionStatement createExpressionStatement() {
        return new CPPASTExpressionStatement();
    }

    @Override
	protected IASTDeclarationStatement createDeclarationStatement() {
        return new CPPASTDeclarationStatement();
    }

    @Override
	protected IASTASMDeclaration createASMDirective() {
        return new CPPASTASMDeclaration();
    }

    @Override
	protected IASTCastExpression createCastExpression() {
        return new CPPASTCastExpression();
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
        case IToken.t_try:
            return parseTryStatement();
        default:
            // can be many things:
            // label
            if (LT(1) == IToken.tIDENTIFIER && LT(2) == IToken.tCOLON) {
                return parseLabelStatement();
            }

            return parseDeclarationOrExpressionStatement(DeclarationOptions.LOCAL);
        }
    }

    protected IASTStatement parseTryStatement() throws EndOfFileException, BacktrackException {
        int startO = consume().getOffset();
        IASTStatement tryBlock = compoundStatement();
        List<ICPPASTCatchHandler> catchHandlers = new ArrayList<ICPPASTCatchHandler>(DEFAULT_CATCH_HANDLER_LIST_SIZE);
        catchHandlerSequence(catchHandlers);
        ICPPASTTryBlockStatement tryStatement = createTryBlockStatement();
        ((ASTNode) tryStatement).setOffset(startO);
        tryStatement.setTryBody(tryBlock);

        for (int i = 0; i < catchHandlers.size(); ++i) {
            ICPPASTCatchHandler handler = catchHandlers.get(i);
            tryStatement.addCatchHandler(handler);
            ((ASTNode) tryStatement).setLength(calculateEndOffset(handler) - startO);
        }
        return tryStatement;
    }


    protected ICPPASTTryBlockStatement createTryBlockStatement() {
        return new CPPASTTryBlockStatement();
    }

    @Override
	protected void nullifyTranslationUnit() {
        translationUnit = null;
    }

    @Override
	protected IASTProblemStatement createProblemStatement() {
        return new CPPASTProblemStatement();
    }

    @Override
	protected IASTProblemExpression createProblemExpression() {
        return new CPPASTProblemExpression();
    }

    @Override
	protected IASTProblem createProblem(int signal, int offset, int length) {
        IASTProblem result = new CPPASTProblem(signal, CharArrayUtils.EMPTY, true);
        ((ASTNode) result).setOffsetAndLength(offset, length);
        ((ASTNode) result).setLength(length);
        return result;
    }

    @Override
	protected IASTStatement parseWhileStatement() throws EndOfFileException, BacktrackException {
        int startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTNode while_condition = cppStyleCondition(IToken.tRPAREN);
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

        ICPPASTWhileStatement while_statement = (ICPPASTWhileStatement) createWhileStatement();
        ((ASTNode) while_statement).setOffsetAndLength(startOffset,
                (while_body != null ? calculateEndOffset(while_body) : LA(1).getEndOffset()) - startOffset);
        if (while_condition instanceof IASTExpression) {
            while_statement.setCondition((IASTExpression) while_condition);
        } else if (while_condition instanceof IASTDeclaration) {
            while_statement.setConditionDeclaration((IASTDeclaration) while_condition);
        }
        
        if (while_body != null) {
            while_statement.setBody(while_body);
        }

        return while_statement;

    }

    protected IASTNode cppStyleCondition(int expectToken) throws BacktrackException, EndOfFileException {
        IToken mark = mark();
        try {
            IASTExpression e = expression();
            final int lt1= LT(1);
            if (lt1 == expectToken || lt1 == IToken.tEOC) {
            	return e;
            }
        } catch (BacktrackException bt) {
        }
        backup(mark);
        try {
        	return simpleSingleDeclaration(DeclarationOptions.CONDITION);
        } catch (BacktrackException b) {
        	if (expectToken == IToken.tRPAREN) {
        		backup(mark);
        		return skipProblemConditionInParenthesis(mark.getOffset());
        	}
        	throw b;
        }
    }


    @Override
	protected ASTVisitor createAmbiguityNodeVisitor() {
        return EMPTY_VISITOR;
    }

    @Override
	protected IASTAmbiguousStatement createAmbiguousStatement() {
        return new CPPASTAmbiguousStatement();
    }

    protected IASTStatement parseIfStatement() throws EndOfFileException, BacktrackException {
        ICPPASTIfStatement result = null;
        ICPPASTIfStatement if_statement = null;
        int start = LA(1).getOffset();
        if_loop: while (true) {
            int so = consume(IToken.t_if).getOffset();
            consume(IToken.tLPAREN);
            // condition
            IASTNode condition= cppStyleCondition(IToken.tRPAREN); 
            if (LT(1) == IToken.tEOC) {
            	// Completing in the condition
            	ICPPASTIfStatement new_if = createIfStatement();
            	if (condition instanceof IASTExpression)
            		new_if.setConditionExpression((IASTExpression) condition);
            	else if (condition instanceof IASTDeclaration)
            		new_if.setConditionDeclaration((IASTDeclaration) condition);

            	if (if_statement != null) {
            		if_statement.setElseClause(new_if);
            	}
            	return result != null ? result : new_if;
            }
            consume(IToken.tRPAREN);


            IASTStatement thenClause = statement();
            ICPPASTIfStatement new_if_statement = createIfStatement();
            ((ASTNode) new_if_statement).setOffset(so);
            if (condition != null && (condition instanceof IASTExpression || condition instanceof IASTDeclaration)) 
            	// shouldn't be possible but failure in condition() makes it so
            {
            	if( condition instanceof IASTExpression )
            		new_if_statement.setConditionExpression((IASTExpression) condition);
            	else if( condition instanceof IASTDeclaration )
            		new_if_statement.setConditionDeclaration((IASTDeclaration) condition);
            }
            if (thenClause != null) {
                new_if_statement.setThenClause(thenClause);
                ((ASTNode) new_if_statement).setLength(calculateEndOffset(thenClause) - ((ASTNode) new_if_statement).getOffset());
            }
            if (LT(1) == IToken.t_else) {
                consume();
                if (LT(1) == IToken.t_if) {
                    // an else if, don't recurse, just loop and do another if

                    if (if_statement != null) {
                        if_statement.setElseClause(new_if_statement);
                        ((ASTNode) if_statement).setLength(calculateEndOffset(new_if_statement) - ((ASTNode) if_statement).getOffset());
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
                    ((ASTNode) if_statement).setLength(calculateEndOffset(new_if_statement) - ((ASTNode) if_statement).getOffset());
                } else {
                    if (result == null)
                        result = new_if_statement;
                    if_statement = new_if_statement;
                }
            } else {
                if (thenClause != null)
                    ((ASTNode) new_if_statement).setLength(calculateEndOffset(thenClause) - start);
                if (if_statement != null) {
                    if_statement.setElseClause(new_if_statement);
                    ((ASTNode) new_if_statement).setLength(calculateEndOffset(new_if_statement) - start);
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

    
    @Override
	protected IASTStatement functionBody() throws EndOfFileException, BacktrackException {
        ++functionBodyCount;
        IASTStatement s = super.functionBody();
        --functionBodyCount;
        return s;
    }

    protected IASTExpression unaryOperatorCastExpression(int operator) throws EndOfFileException, BacktrackException {
        IToken mark = mark();
        int offset = consume().getOffset();
        IASTExpression castExpression = castExpression();
        if (castExpression instanceof IASTLiteralExpression) {
        	IASTLiteralExpression literal = (IASTLiteralExpression) castExpression;
            if( literal.getKind() != ICPPASTLiteralExpression.lk_this ) {
            	if ( operator == IASTUnaryExpression.op_amper || 
            			(operator == IASTUnaryExpression.op_star && 
            					literal.getKind() != IASTLiteralExpression.lk_string_literal) ) {
            		backup( mark );
            		throwBacktrack( mark );
            	}
            }
        }
        return buildUnaryExpression(operator, castExpression, offset,
                calculateEndOffset(castExpression));
    }

    protected IASTStatement parseSwitchStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTNode switch_condition = cppStyleCondition(IToken.tRPAREN);
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
        ICPPASTSwitchStatement switch_statement = createSwitchStatement();
        ((ASTNode) switch_statement).setOffsetAndLength(startOffset,
                (switch_body != null ? calculateEndOffset(switch_body) : LA(1).getEndOffset()) - startOffset);
        if( switch_condition instanceof IASTExpression ) {
            switch_statement.setControllerExpression((IASTExpression) switch_condition);
        }
        else if( switch_condition instanceof IASTDeclaration ) {
            switch_statement.setControllerDeclaration((IASTDeclaration) switch_condition);
        }
        
        if (switch_body != null) {
            switch_statement.setBody(switch_body);
        }

        return switch_statement;
    }

    protected IASTStatement parseForStatement() throws EndOfFileException, BacktrackException {
        int startOffset;
        startOffset = consume().getOffset();
        consume(IToken.tLPAREN);
        IASTStatement init = forInitStatement(DeclarationOptions.LOCAL);
        IASTNode for_condition = null;
        switch (LT(1)) {
        case IToken.tSEMI:
        case IToken.tEOC:
            break;
        default:
            for_condition = cppStyleCondition(IToken.tSEMI);
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
        ICPPASTForStatement for_statement = createForStatement();
        IASTStatement for_body = null;
        if (LT(1) != IToken.tEOC) {
            for_body = statement();
            ((ASTNode) for_statement).setOffsetAndLength(startOffset, calculateEndOffset(for_body) - startOffset);
        }
    
        for_statement.setInitializerStatement(init);
        
        if (for_condition != null) {
            if( for_condition instanceof IASTExpression ) {
                for_statement.setConditionExpression((IASTExpression) for_condition);
            }
            else if( for_condition instanceof IASTDeclaration ) {
                for_statement.setConditionDeclaration((IASTDeclaration) for_condition);              
            }
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
