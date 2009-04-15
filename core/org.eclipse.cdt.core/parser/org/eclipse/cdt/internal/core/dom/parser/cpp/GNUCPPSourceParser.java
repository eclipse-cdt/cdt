/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
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
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.BacktrackException;
import org.eclipse.cdt.internal.core.dom.parser.DeclarationOptions;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;

/**
 * This is our implementation of the IParser interface, serving as a parser for
 * GNU C and C++. From time to time we will make reference to the ANSI ISO
 * specifications.
 */
public class GNUCPPSourceParser extends AbstractGNUSourceCodeParser {
    private static final int DEFAULT_PARM_LIST_SIZE = 4;
    private static final int DEFAULT_POINTEROPS_LIST_SIZE = 4;
    private static final int DEFAULT_CATCH_HANDLER_LIST_SIZE= 4;
    private static enum DtorStrategy {PREFER_FUNCTION, PREFER_NESTED}

    private final boolean allowCPPRestrict;
    private final boolean supportExtendedTemplateSyntax;
    private final boolean supportLongLong;

	private final IIndex index;
    protected ICPPASTTranslationUnit translationUnit;

    private int functionBodyCount= 0;
	private int rejectLogicalOperatorInTemplateID= 0;
	private char[] currentClassName;

	private final ICPPNodeFactory nodeFactory;
	
    public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
            IParserLogService log, ICPPParserExtensionConfiguration config) {
    	this(scanner, mode, log, config, null);
    }

    public GNUCPPSourceParser(IScanner scanner, ParserMode mode,
            IParserLogService log, ICPPParserExtensionConfiguration config,
            IIndex index) {
        super(scanner, log, mode, CPPNodeFactory.getDefault(),
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
        supportParameterInfoBlock= config.supportParameterInfoBlock();
        supportExtendedSizeofOperator= config.supportExtendedSizeofOperator();
        supportFunctionStyleAsm= config.supportFunctionStyleAssembler();
        functionCallCanBeLValue= true;
        this.index= index;
        this.nodeFactory = CPPNodeFactory.getDefault();
    }

    @Override
	protected IASTName identifier() throws EndOfFileException, BacktrackException {
    	switch (LT(1)) {
    	case IToken.tIDENTIFIER:
    	case IToken.tCOMPLETION:
    	case IToken.tEOC:
    		return buildName(-1, consume());
    	}

    	throw backtrack;
    }
    
    protected IASTName idExpression() throws EndOfFileException, BacktrackException {
    	try {
    		rejectLogicalOperatorInTemplateID++;
			return qualifiedName();
		} finally {
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
    private IASTName qualifiedName() throws BacktrackException, EndOfFileException {
    	ICPPASTQualifiedName qname= null;
    	IASTName name= null;
    	final int offset= LA(1).getOffset();
    	int endOffset= offset;
    	if (LT(1) == IToken.tCOLONCOLON) {
    		endOffset= consume().getEndOffset();
    		qname= nodeFactory.newQualifiedName();
    		qname.setFullyQualified(true);
    	}

    	boolean mustBeLast= false;
    	boolean haveName= false;
    	loop: for(;;) {
    		boolean forceTemplate= false;
    		if (qname != null && LT(1) == IToken.t_template) { 
    			consume().getEndOffset();
    			forceTemplate= true;
    		}

    		int destructorOffset= -1;
    		if (LT(1) == IToken.tBITCOMPLEMENT) {
    			destructorOffset= consume().getOffset();
    			mustBeLast= true;
    		}

    		switch (LT(1)) {
    		case IToken.tIDENTIFIER:
    		case IToken.tCOMPLETION:
    		case IToken.tEOC:
    			IToken nt= consume();
    			name = buildName(destructorOffset, nt);
    			break;
    		
    		case IToken.t_operator:
    			name= operatorId();
    			break;

    		default:
    			if (!haveName || destructorOffset >= 0 || forceTemplate) {
    				throwBacktrack(LA(1));
    			}
    			name= nodeFactory.newName(CharArrayUtils.EMPTY);
    			if (qname != null) {
    				qname.addName(name);
    			}
    			break loop;
    		}

    		haveName= true;
			name= addTemplateArguments(name);
			endOffset= calculateEndOffset(name);
			if (qname != null) {
				qname.addName(name);
			}

    		if (LTcatchEOF(1) != IToken.tCOLONCOLON)
    			break loop;

    		if (mustBeLast)
    			throwBacktrack(LA(1));

    		endOffset= consume().getEndOffset(); // ::
    		if (qname == null) {
    			qname= nodeFactory.newQualifiedName();
    			qname.addName(name);
    		}
    	}
    	if (qname == null)
    		return name;

    	setRange(qname, offset, endOffset);
    	return qname;
    }

	private IASTName buildName(int destructorOffset, IToken nt) {
		IASTName name;
		if (destructorOffset < 0) {
			name= nodeFactory.newName(nt.getCharImage());
			setRange(name, nt.getOffset(), nt.getEndOffset());
		} else {
			char[] nchars= nt.getCharImage();
			final int len= nchars.length;
			char[] image = new char[len+1];
			image[0]= '~';
			System.arraycopy(nchars, 0, image, 1, len);
			name= nodeFactory.newName(image);
			setRange(name, destructorOffset, nt.getEndOffset());
		}
		switch(nt.getType()) {
		case IToken.tEOC:
		case IToken.tCOMPLETION:
			createCompletionNode(nt).addName(name);
			break;
		}
		return name;
	}

	private IASTName addTemplateArguments(IASTName templateName) throws EndOfFileException, BacktrackException {
        if (!canBeTemplateArguments()) 
        	return templateName;
        
        IToken secondMark = mark();
        consume(IToken.tLT);
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
        	IToken end= LA(1);
        	switch(end.getType()) {
        	case IToken.tGT: 
        		consume();
        		break;
        	case IToken.tEOC:
        		break;
        	default:
        		throw backtrack;
        	}
        	return buildTemplateID(templateName, end.getEndOffset(), list);
        } catch (BacktrackException bt) {
        	backup(secondMark);
        	return templateName;
        } finally {
        	onTopInTemplateArgs= wasOnTop;
        }
    }

    private ICPPASTTemplateId buildTemplateID(IASTName templateName, int endOffset, List<IASTNode> args) {
        ICPPASTTemplateId result = nodeFactory.newTemplateId(templateName);
        setRange(result, ((ASTNode) templateName).getOffset(), endOffset);
        for (IASTNode n : args) {
        	if (n instanceof IASTTypeId) {
        		result.addTemplateArgument((IASTTypeId) n);
        	} else if(n instanceof IASTExpression) {
        		result.addTemplateArgument((IASTExpression) n);
        	} else if(n instanceof ICPPASTAmbiguousTemplateArgument) {
        		result.addTemplateArgument((ICPPASTAmbiguousTemplateArgument) n);
        	}
        }
        return result;
    }

    /**
     * Makes a fast check whether there could be template arguments.
     */
    private boolean canBeTemplateArguments() throws EndOfFileException, BacktrackException {
        if (LTcatchEOF(1) != IToken.tLT) 
        	return false;
        
        final IToken mark= mark();
        try {
        	consume();
        	int nk= 0;
        	int depth= 0;
        	int angleDepth= 0;
        	int limit= 100;

        	while(--limit > 0) {
        		switch(consume().getType()) {
        		case IToken.tEOC:
        		case IToken.tCOMPLETION:
        			return true;
        			
        		case IToken.tLT:
        			if (nk == 0) {
        				angleDepth++;
        			}
        			break;
        		case IToken.tGT: 
        			if (nk == 0) {
        				if (--angleDepth < 0) {
        	        		final int lt1= LTcatchEOF(1);
        	        		return (lt1 != IToken.tINTEGER && lt1 != IToken.tFLOATINGPT);
        				}
        			}
        			break;
        		case IToken.tLBRACKET: 
        			if (nk == 0) {
        				nk= IToken.tLBRACKET;
        				depth= 0;
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
        				depth= 0;
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
        				return false;
        			}
        			break;
        		}
        	}
        	return true;
        } finally {
        	backup(mark);
        }
    }
    
    private List<IASTNode> templateArgumentList() throws EndOfFileException, BacktrackException {
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
    			try {
    				// consider ambiguity with id-expressions, only:
        			IASTDeclSpecifier declspec= typeId.getDeclSpecifier();
        			if (!(declspec instanceof IASTNamedTypeSpecifier)) 
        				throw backtrack;
        			IASTName name= ((IASTNamedTypeSpecifier) declspec).getName();
        			if (!name.contains(typeId)) 
        				throw backtrack;
        			
        			// A template-id cannot be used in an id-expression as a template argument
        			// 5.1-11 A template-id shall be used as an unqualified-id only as specified in
        			// 14.7.2, 14.7, and 14.5.4.
        			name= name.getLastName();
        			if (name instanceof ICPPASTTemplateId)
        				throw backtrack;

        			backup(argStart);
    				IASTExpression expression = assignmentExpression();
    				if (expression instanceof IASTIdExpression) {
    					if (mark() != typeIdEnd) 
    						throw backtrack;

    					ICPPASTAmbiguousTemplateArgument ambiguity= createAmbiguousTemplateArgument();
    					ambiguity.addTypeId(typeId);
    					ambiguity.addIdExpression((IASTIdExpression) expression);
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
    			IASTExpression expression = assignmentExpression();
    			list.add(expression);
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

    private IASTName operatorId() throws BacktrackException, EndOfFileException {
        final IToken firstToken = consume(IToken.t_operator);
        int endOffset= firstToken.getEndOffset();
        IASTTypeId typeId = null;
        OverloadableOperator op = null;
        final int lt1= LT(1);
        switch (lt1) {
        case IToken.tLPAREN:
        	op = OverloadableOperator.PAREN;  // operator ()
            consume(); 
            endOffset = consume(IToken.tRPAREN).getEndOffset();
            break;
        case IToken.tLBRACKET:
            op = OverloadableOperator.BRACKET; // operator []
            consume(); 
            endOffset = consume(IToken.tRBRACKET).getEndOffset();
            break;
        case IToken.t_new:
        case IToken.t_delete:
        	if (LT(2) == IToken.tLBRACKET) {
            	op= lt1 == IToken.t_new ? OverloadableOperator.NEW_ARRAY : OverloadableOperator.DELETE_ARRAY;
        		consume();
        		consume();
        		endOffset= consume(IToken.tRBRACKET).getEndOffset();
        	} else {
        		IToken t= consume();
            	endOffset= t.getEndOffset();
        		op= OverloadableOperator.valueOf(t);
        	}
        	break;
        default:
        	op= OverloadableOperator.valueOf(LA(1));
        	if (op != null) {
        		endOffset= consume().getEndOffset();
        	}
        	break;
        }  
        
        if (op != null) {
            IASTName name= nodeFactory.newOperatorName(op.toCharArray());
            setRange(name, firstToken.getOffset(), endOffset);
            return name;
        }

        // must be a conversion function
        IToken t = LA(1);
        typeId= typeId(DeclarationOptions.TYPEID_CONVERSION);
        if (typeId == null) 
        	throwBacktrack(t);

        IASTName name = nodeFactory.newConversionName(typeId);
        setRange(name, firstToken.getOffset(), calculateEndOffset(typeId));
        return name;
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
	protected boolean shallRejectLogicalOperator() {
    	return onTopInTemplateArgs && rejectLogicalOperatorInTemplateID > 0;
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

    protected IASTExpression throwExpression() throws EndOfFileException, BacktrackException {
        IToken throwToken = consume();
        IASTExpression throwExpression = null;
        try {
            throwExpression = expression();
        } catch (BacktrackException bte) {
        	backup(throwToken);
        	consume();
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

    protected IASTExpression deleteExpression() throws EndOfFileException, BacktrackException {
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
        ICPPASTDeleteExpression deleteExpression = nodeFactory.newDeleteExpression(castExpression);
        ((ASTNode) deleteExpression).setOffsetAndLength(startingOffset, calculateEndOffset(castExpression) - startingOffset);
        deleteExpression.setIsGlobal(global);
        deleteExpression.setIsVectored(vectored);
        return deleteExpression;
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
            				ASTQueries.findTypeRelevantDeclarator(typeid2.getAbstractDeclarator()) instanceof IASTArrayDeclarator) {
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
		ICPPASTNewExpression result = nodeFactory.newNewExpression(placement, init, typeid);
        result.setIsGlobal(isGlobal);
        result.setIsNewTypeId(isNewTypeId);
        ((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
        return result;
    }


    @Override
	protected IASTExpression unaryExpression() throws EndOfFileException, BacktrackException {
        switch (LT(1)) {
        case IToken.tSTAR:
            return unarayExpression(IASTUnaryExpression.op_star);
        case IToken.tAMPER:
            return unarayExpression(IASTUnaryExpression.op_amper);
        case IToken.tPLUS:
            return unarayExpression(IASTUnaryExpression.op_plus);
        case IToken.tMINUS:
            return unarayExpression(IASTUnaryExpression.op_minus);
        case IToken.tNOT:
            return unarayExpression(IASTUnaryExpression.op_not);
        case IToken.tBITCOMPLEMENT:
            return unarayExpression(IASTUnaryExpression.op_tilde);
        case IToken.tINCR:
            return unarayExpression(IASTUnaryExpression.op_prefixIncr);
        case IToken.tDECR:
            return unarayExpression(IASTUnaryExpression.op_prefixDecr);
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
        case IToken.tLPAREN:
        	// ( type-name ) { initializer-list }
        	// ( type-name ) { initializer-list , }
        	IToken m = mark();
        	try {
        		int offset = consume().getOffset();
        		IASTTypeId t= typeId(DeclarationOptions.TYPEID);
        		if (t != null) {
        			consume(IToken.tRPAREN);
        			if (LT(1) == IToken.tLBRACE) {
        				IASTInitializer i = initializerClause(false);
        		        firstExpression= nodeFactory.newTypeIdInitializerExpression(t, i);
        		        setRange(firstExpression, offset, calculateEndOffset(i));
        				break;        
        			}
        		}
        	} catch (BacktrackException bt) {
        	}
        	backup(m); 
        	firstExpression= primaryExpression();
        	break;

        case IToken.t_typename:
            int typenameOffset= consume().getOffset();

            boolean templateTokenConsumed = false;
            if (LT(1) == IToken.t_template) {
                consume();
                templateTokenConsumed = true;
            }
            IASTName name = idExpression();
            if (LT(1) != IToken.tLPAREN) {
            	throwBacktrack(LA(1));
            }

            ICPPASTTypenameExpression result = nodeFactory.newTypenameExpression(name, null, templateTokenConsumed);
            setRange(result, typenameOffset, calculateEndOffset(name));
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

                IASTArraySubscriptExpression s = nodeFactory.newArraySubscriptExpression(firstExpression, secondExpression);
                ((ASTNode) s).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), lastOffset - ((ASTNode) firstExpression).getOffset());
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

                IASTFunctionCallExpression fce = nodeFactory.newFunctionCallExpression(firstExpression, secondExpression);
                ((ASTNode) fce).setOffsetAndLength(((ASTNode) firstExpression)
                        .getOffset(), lastOffset - ((ASTNode) firstExpression).getOffset());
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

                ICPPASTFieldReference fieldReference = nodeFactory.newFieldReference(name, firstExpression);
                fieldReference.setIsPointerDereference(false);
                fieldReference.setIsTemplate(isTemplate);
                ((ASTNode) fieldReference).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
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
                
                fieldReference = nodeFactory.newFieldReference(name, firstExpression);
                fieldReference.setIsPointerDereference(true);
                fieldReference.setIsTemplate(isTemplate);
                ((ASTNode) fieldReference).setOffsetAndLength(
                        ((ASTNode) firstExpression).getOffset(),
                        calculateEndOffset(name) - ((ASTNode) firstExpression).getOffset());
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
	

    protected IASTExpression simpleTypeConstructorExpression(int operator) throws EndOfFileException, BacktrackException {
        int startingOffset = LA(1).getOffset();
        consume();
        consume(IToken.tLPAREN);
        IASTExpression operand = null;
        if (LT(1) != IToken.tRPAREN)
            operand = expression();
        int l = consume(IToken.tRPAREN).getEndOffset();
        ICPPASTSimpleTypeConstructorExpression result = nodeFactory.newSimpleTypeConstructorExpression(operator, operand); 
        ((ASTNode) result).setOffsetAndLength(startingOffset, l - startingOffset);
        return result;
    }


    @Override
	protected IASTExpression primaryExpression() throws EndOfFileException, BacktrackException {
        IToken t = null;
        IASTLiteralExpression literalExpression = null;
        switch (LT(1)) {
        // TO DO: we need more literals...
        case IToken.tINTEGER:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_integer_constant, t.getImage()); 
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset()- t.getOffset());
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
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.t_false:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_false, t.getImage()); 
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;
        case IToken.t_true:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_true, t.getImage()); 
            ((ASTNode) literalExpression).setOffsetAndLength(t.getOffset(), t.getEndOffset() - t.getOffset());
            return literalExpression;

        case IToken.t_this:
            t = consume();
            literalExpression = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_this, t.getImage()); 
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
            IASTIdExpression idExpression = nodeFactory.newIdExpression(name);
            ((ASTNode) idExpression).setOffsetAndLength(((ASTNode) name).getOffset(), ((ASTNode) name).getOffset()
                    + ((ASTNode) name).getLength() - ((ASTNode) name).getOffset());
            return idExpression;
        }
        default:
            IToken la = LA(1);
            int startingOffset = la.getOffset();
            throwBacktrack(startingOffset, la.getLength());
            return null;
        }

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
    protected IASTDeclaration usingClause() throws EndOfFileException, BacktrackException {
        final int offset= consume().getOffset();

        if (LT(1) == IToken.t_namespace) {
            // using-directive
            int endOffset = consume().getEndOffset();
            IASTName name = null;
            switch (LT(1)) {
            case IToken.tIDENTIFIER:
            case IToken.tCOLONCOLON:
            case IToken.tCOMPLETION:
                name = qualifiedName();
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

            ICPPASTUsingDirective astUD = nodeFactory.newUsingDirective(name);
            ((ASTNode) astUD).setOffsetAndLength(offset, endOffset - offset);
            return astUD;
        }

        ICPPASTUsingDeclaration result = usingDeclaration(offset);
        return result;
    }

	private ICPPASTUsingDeclaration usingDeclaration(final int offset) throws EndOfFileException, BacktrackException {
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

        ICPPASTUsingDeclaration result = nodeFactory.newUsingDeclaration(name);
        ((ASTNode) result).setOffsetAndLength(offset, end - offset);
        result.setIsTypename(typeName);
		return result;
	}



    /**
     * Implements Linkage specification in the ANSI C++ grammar.
     * linkageSpecification : extern "string literal" declaration | extern
     * "string literal" { declaration-seq }
     * 
     * @throws BacktrackException
     *             request for a backtrack
     */
    protected ICPPASTLinkageSpecification linkageSpecification() throws EndOfFileException, BacktrackException {
        int offset= consume().getOffset(); // t_extern
        String spec = consume().getImage(); // tString
        ICPPASTLinkageSpecification linkage = nodeFactory.newLinkageSpecification(spec); 

        if (LT(1) == IToken.tLBRACE) {
        	declarationListInBraces(linkage, offset, DeclarationOptions.GLOBAL);
            return linkage;
        }
        // single declaration

        IASTDeclaration d = declaration(DeclarationOptions.GLOBAL);
        linkage.addDeclaration(d);
        setRange(linkage, offset, calculateEndOffset(d));
        return linkage;
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
    protected IASTDeclaration templateDeclaration(DeclarationOptions option) throws EndOfFileException, BacktrackException {
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
    			IGPPASTExplicitTemplateInstantiation temp = nodeFactory.newExplicitTemplateInstantiationGPP(null);
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
    			templateInstantiation = nodeFactory.newExplicitTemplateInstantiation(null);
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
    		IASTDeclaration d = declaration(option);
    		ICPPASTTemplateSpecialization templateSpecialization = nodeFactory.newTemplateSpecialization(d);
    		((ASTNode) templateSpecialization).setOffsetAndLength(firstToken.getOffset(), calculateEndOffset(d) - firstToken.getOffset());
    		return templateSpecialization;
    	}

    	final boolean wasOnTop= onTopInTemplateArgs;
    	onTopInTemplateArgs= true;
    	List<ICPPASTTemplateParameter> parms;
    	try {
    		parms = templateParameterList();
    		consume(IToken.tGT);
    	} finally {
    		onTopInTemplateArgs= wasOnTop;
    	}
    	IASTDeclaration d = declaration(option);
    	ICPPASTTemplateDeclaration templateDecl = nodeFactory.newTemplateDeclaration(d);
    	((ASTNode) templateDecl).setOffsetAndLength(firstToken.getOffset(), calculateEndOffset(d) - firstToken.getOffset());
    	templateDecl.setExported(exported);
    	for (int i = 0; i < parms.size(); ++i) {
    		ICPPASTTemplateParameter parm = parms.get(i);
    		templateDecl.addTemplateParamter(parm);
    	}
    	return templateDecl;
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
    protected List<ICPPASTTemplateParameter> templateParameterList() throws BacktrackException, EndOfFileException {
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
                    identifierName = identifier();
                    lastOffset = calculateEndOffset(identifierName);
                    if (LT(1) == IToken.tASSIGN) { // optional = type-id
                        consume();
                        typeId = typeId(DeclarationOptions.TYPEID); // type-id
                        if (typeId == null) 
                        	throw backtrack;
                        lastOffset = calculateEndOffset(typeId);
                    }
                } else {
                    identifierName = nodeFactory.newName();
                }

                ICPPASTSimpleTypeTemplateParameter parm = nodeFactory.newSimpleTypeTemplateParameter(type, identifierName, typeId);
                ((ASTNode) parm).setOffsetAndLength(startingToken.getOffset(), lastOffset - startingToken.getOffset());
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
                    identifierName = identifier();
                    last = calculateEndOffset(identifierName);
                    if (LT(1) == IToken.tASSIGN) { // optional = type-id
                        consume();
                        optionalExpression = primaryExpression();
                        last = calculateEndOffset(optionalExpression);
                    }
                } else
                    identifierName = nodeFactory.newName();

                ICPPASTTemplatedTypeTemplateParameter parm = nodeFactory.newTemplatedTypeTemplateParameter(identifierName, optionalExpression);
                ((ASTNode) parm).setOffsetAndLength(firstToken.getOffset(), last - firstToken.getOffset());

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
        	IASTSimpleDeclSpecifier declspec= nodeFactory.newSimpleDeclSpecifier();
        	IASTSimpleDeclaration decl= nodeFactory.newSimpleDeclaration(declspec);
        	((ASTNode) declspec).setOffsetAndLength(t.getOffset(), 0);
        	((ASTNode) decl).setOffsetAndLength(t.getOffset(), t.getLength());
        	return decl; 
        case IToken.t_public:
        case IToken.t_protected:
        case IToken.t_private: 
        	if (option == DeclarationOptions.CPP_MEMBER) {
        		t= consume();
				int key= t.getType();
				int endOffset= consume(IToken.tCOLON).getEndOffset();
				ICPPASTVisibilityLabel label = nodeFactory.newVisibilityLabel(token2Visibility(key));
				setRange(label, t.getOffset(), endOffset);
				return label;
			}
        	break;
        }
        
		try {
	        return simpleDeclaration(option);
		} catch (BacktrackException e) {
			if (option != DeclarationOptions.CPP_MEMBER || declarationMark == null)
				throw e;
			BacktrackException orig= new BacktrackException(e); // copy the exception
			IToken mark= mark();
			backup(declarationMark);
			try {
				return usingDeclaration(declarationMark.getOffset());
			} catch (BacktrackException e2) {
				backup(mark);
				throw orig; // throw original exception;
			}
		}
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
            name = identifier();
            endOffset= calculateEndOffset(name);
        } else {
            name = nodeFactory.newName();
        }

        // bug 195701, gcc 4.2 allows visibility attribute for namespaces.
        __attribute_decl_seq(true, false);

        if (LT(1) == IToken.tLBRACE) {
	        ICPPASTNamespaceDefinition ns = nodeFactory.newNamespaceDefinition(name);
	        declarationListInBraces(ns, offset, DeclarationOptions.GLOBAL);
            return ns;
        } 
        
		if (LT(1) == IToken.tASSIGN) {
            endOffset= consume().getEndOffset();
            if (name.toString() == null) {
                throwBacktrack(offset, endOffset - offset);
                return null;
            }

            IASTName qualifiedName= qualifiedName();
            endOffset = consume(IToken.tSEMI).getEndOffset();

            ICPPASTNamespaceAlias alias = nodeFactory.newNamespaceAlias(name, qualifiedName);
            ((ASTNode) alias).setOffsetAndLength(offset, endOffset - offset);
            return alias;
        } 
		throwBacktrack(LA(1));
		return null;
    }


    /**
     * Parses a declaration with the given options.
     */
    protected IASTDeclaration simpleDeclaration(DeclarationOptions declOption) throws BacktrackException, EndOfFileException {
        if (LT(1) == IToken.tLBRACE)
            throwBacktrack(LA(1));
        
        final int firstOffset= LA(1).getOffset();
        int endOffset= firstOffset;
        boolean insertSemi= false;
        boolean parseDtors= true;

        ICPPASTDeclSpecifier declSpec= null;
        IASTDeclarator dtor= null;
        IToken markBeforDtor= null;
        try {
            declSpec = declSpecifierSeq(declOption);
            final int lt1= LTcatchEOF(1);
            switch(lt1) {
            case 0: // eof
            case IToken.tEOC:
            case IToken.tSEMI:
            	if (lt1 != IToken.tEOC && !validWithoutDtor(declOption, declSpec)) 
                	throwBacktrack(LA(1));
            	
            	parseDtors= false;
            	insertSemi= lt1==0;
            	if (lt1 == IToken.tSEMI)
            		endOffset= consume().getEndOffset();
            	else 
            		endOffset= calculateEndOffset(declSpec);
            	break;

            case IToken.tCOMMA:
            	throwBacktrack(LA(1));
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
        } catch (FoundAggregateInitializer lie) {
        	if (declSpec == null)
        		declSpec= (ICPPASTDeclSpecifier) lie.fDeclSpec;
        	// scalability: don't keep references to tokens, initializer may be large
        	declarationMark= null;
        	markBeforDtor= null;
        	dtor= addInitializer(lie, declOption);
        } catch (FoundDeclaratorException e) {
        	declSpec= (ICPPASTDeclSpecifier) e.declSpec;
        	dtor= e.declarator;
            backup(e.currToken);
        } catch (BacktrackException e) {
        	IASTNode node= e.getNodeBeforeProblem();
        	if (node instanceof ICPPASTDeclSpecifier && validWithoutDtor(declOption, (ICPPASTDeclSpecifier) node)) {
                IASTSimpleDeclaration d= nodeFactory.newSimpleDeclaration((IASTDeclSpecifier) node);
                setRange(d, node);
        		throwBacktrack(e.getProblem(), d);
        	}
        	throw e;
        }
        
        IASTDeclarator[] declarators= IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        if (parseDtors) {
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
        		declarators = (IASTDeclarator[]) ArrayUtil.append(IASTDeclarator.class, declarators, dtor);
        	}
        	declarators = (IASTDeclarator[]) ArrayUtil.removeNulls(IASTDeclarator.class, declarators);
        
        	final int lt1= LTcatchEOF(1);
        	switch (lt1) {
        	case IToken.tEOC:
        		endOffset= figureEndOffset(declSpec, declarators);
        		break;
        	case IToken.tSEMI:
        		endOffset= consume().getEndOffset();
        		break;
        	case IToken.t_try:
        	case IToken.tCOLON:
        	case IToken.tLBRACE:
        		return functionDefinition(firstOffset, declSpec, declarators);
        	default:	
        		if (declOption != DeclarationOptions.LOCAL) {
    				insertSemi= true;
        			if (validWithoutDtor(declOption, declSpec)) {
        				if (markBeforDtor != null && !isOnSameLine(calculateEndOffset(declSpec), markBeforDtor.getOffset())) {
        					backup(markBeforDtor);
        					declarators= IASTDeclarator.EMPTY_DECLARATOR_ARRAY;
        					endOffset= calculateEndOffset(declSpec);
        					break;
        				}
        			}
        			endOffset= figureEndOffset(declSpec, declarators);
        			if (lt1 == 0 || !isOnSameLine(endOffset, LA(1).getOffset())) {
        				break;
        			}
        			if (declarators.length == 1 && declarators[0] instanceof IASTFunctionDeclarator) {
        				break;
        			}
        		}
        		throwBacktrack(LA(1));
        	}
        }

        // no function body
        IASTSimpleDeclaration simpleDeclaration= nodeFactory.newSimpleDeclaration(declSpec);
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
			IASTDeclarator[] dtors) throws EndOfFileException, BacktrackException {
		
    	if (dtors.length != 1) 
    		throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);
    	
		final IASTDeclarator outerDtor= dtors[0];
		final IASTDeclarator dtor= ASTQueries.findTypeRelevantDeclarator(outerDtor);
		if (dtor instanceof ICPPASTFunctionDeclarator == false)
			throwBacktrack(firstOffset, LA(1).getEndOffset() - firstOffset);

		
		ICPPASTFunctionDefinition fdef;
		if (LT(1) == IToken.t_try) {
			consume();
			fdef= nodeFactory.newFunctionTryBlock(declSpec, (ICPPASTFunctionDeclarator) dtor, null); 
		} else {
			fdef= nodeFactory.newFunctionDefinition(declSpec, (ICPPASTFunctionDeclarator) dtor, null); 
		}
		if (LT(1) == IToken.tCOLON) {
		    ctorInitializer(fdef);
		}

		try {
			IASTStatement body= handleFunctionBody();
			fdef.setBody(body);
			setRange(fdef, firstOffset, calculateEndOffset(body));
		} catch (BacktrackException bt) {
			final IASTNode n= bt.getNodeBeforeProblem();
			if (n instanceof IASTCompoundStatement && !(fdef instanceof ICPPASTFunctionWithTryBlock)) {
				fdef.setBody((IASTCompoundStatement) n);
				setRange(fdef, firstOffset, calculateEndOffset(n));
				throwBacktrack(bt.getProblem(), fdef);
			}
			throw bt;
		}
		
		if (fdef instanceof ICPPASTFunctionWithTryBlock) {
			ICPPASTFunctionWithTryBlock tryblock= (ICPPASTFunctionWithTryBlock) fdef;
		    List<ICPPASTCatchHandler> handlers = new ArrayList<ICPPASTCatchHandler>(DEFAULT_CATCH_HANDLER_LIST_SIZE);
		    catchHandlerSequence(handlers);
		    ICPPASTCatchHandler last= null;
		    for (ICPPASTCatchHandler catchHandler : handlers) {
		    	tryblock.addCatchHandler(catchHandler);
		    	last= catchHandler;
		    }
		    if (last != null) {
		    	adjustLength(tryblock, last);
		    }
		}
		return fdef;
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
    protected void ctorInitializer(ICPPASTFunctionDefinition fdef) throws EndOfFileException, BacktrackException {
        consume();
        ctorLoop: for (;;) {
        	int offset= LA(1).getOffset();
            IASTName name = qualifiedName();

            int endOffset;
            IASTExpression expressionList = null;
            switch (LT(1)) {
            case IToken.tLPAREN:
                consume();

                if (LT(1) != IToken.tRPAREN)
                    expressionList = expression();

                switch (LT(1)) {
                case IToken.tRPAREN:
                case IToken.tEOC:
                    endOffset = consume().getEndOffset();
                    break;
                default:
                    throw backtrack;
                }
                break;
            case IToken.tEOC:
                endOffset = consume().getEndOffset();
                break;
            default:
                throw backtrack;
            }

            ICPPASTConstructorChainInitializer ctorInitializer = nodeFactory.newConstructorChainInitializer(name, expressionList);
            setRange(ctorInitializer, offset, endOffset);
            fdef.addMemberInitializer(ctorInitializer);

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
		
        IASTDeclSpecifier declSpec= null;
        IASTDeclarator declarator;
        try {
        	declSpec= declSpecifierSeq(DeclarationOptions.PARAMETER);
        	declarator= initDeclarator(declSpec, DeclarationOptions.PARAMETER);
        } catch (FoundDeclaratorException e) {
        	declSpec= e.declSpec;
        	declarator= e.declarator;
        	backup(e.currToken);
        } catch (FoundAggregateInitializer lie) {
        	if (declSpec == null)
        		declSpec= lie.fDeclSpec;
        	declarator= addInitializer(lie, DeclarationOptions.PARAMETER);
        }

        final ICPPASTParameterDeclaration parm = nodeFactory.newParameterDeclaration(declSpec, declarator);
        final int endOffset = figureEndOffset(declSpec, declarator);
        ((ASTNode) parm).setOffsetAndLength(startOffset, endOffset - startOffset);
        return parm;
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
     * @throws FoundAggregateInitializer 
     */
    @Override
	protected ICPPASTDeclSpecifier declSpecifierSeq(final DeclarationOptions option)
    		throws BacktrackException, EndOfFileException, FoundDeclaratorException, FoundAggregateInitializer {
        int storageClass = IASTDeclSpecifier.sc_unspecified;
        int simpleType = IASTSimpleDeclSpecifier.t_unspecified;
        int options= 0;
        int isLong= 0;

        IASTName identifier= null;
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
                identifier= qualifiedName();
                endOffset= calculateEndOffset(identifier);
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
                } catch (FoundAggregateInitializer e) {
                	e.fDeclSpec= buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset, endOffset);
                	throw e;
                }catch (FoundDeclaratorException e) {
                	if (e.currToken.getType() == IToken.tEOC || option == DeclarationOptions.FUNCTION_STYLE_ASM 
                			|| canBeConstructorDestructorOrConversion(option, storageClass, options, e.declarator)) {
                		e.declSpec= buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset, endOffset);
                		throw e;
                	}
                }

                identifier= qualifiedName();
                if (identifier.getLookupKey().length == 0 && LT(1) != IToken.tEOC)
                	throwBacktrack(LA(1));
                
                endOffset= calculateEndOffset(identifier);
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
            // cannot store restrict in the cpp-nodes.
            //            if ((options & RESTRICT) != 0) {
            //            }
            ((ASTNode) result).setOffsetAndLength(offset, endOffset - offset);
            if (problem != null) {
            	throwBacktrack(problem, result);
            }
            return result;
        }

        if (identifier != null) 
            return buildNamedTypeSpecifier(identifier, isTypename, storageClass, options, offset, endOffset);
        
        return buildSimpleDeclSpec(storageClass, simpleType, options, isLong, typeofExpression, offset, endOffset);
    }

	private boolean canBeConstructorDestructorOrConversion(DeclarationOptions declOption, int storageClass, int options, IASTDeclarator dtor) {
		final int forbid= CONST | RESTRICT | VOLATILE | SHORT | UNSIGNED | SIGNED | COMPLEX | IMAGINARY | FRIEND;
		if (storageClass == IASTDeclSpecifier.sc_unspecified && (options & forbid) == 0) {
			if (ASTQueries.findTypeRelevantDeclarator(dtor) instanceof IASTFunctionDeclarator) {
				IASTName name= ASTQueries.findInnermostDeclarator(dtor).getName();
				if (name instanceof ICPPASTQualifiedName) {
					final ICPPASTQualifiedName qname = (ICPPASTQualifiedName) name;
					final IASTName names[]= qname.getNames();
					final int len = names.length;
					final IASTName lastName = names[len-1];
					
					if (len > 1 && CharArrayUtils.equals(names[len-2].getLookupKey(), lastName.getLookupKey())) 
						return true; // constructor
					
					name= lastName;
				}
				if (name instanceof ICPPASTTemplateId)
					name= ((ICPPASTTemplateId) name).getTemplateName();

				if (name instanceof ICPPASTConversionName)
					return true;
				
				final char[] nchars= name.getLookupKey();
				if (nchars.length > 0 && nchars[0] == '~') 
					return true; // destructor
				if (declOption == DeclarationOptions.CPP_MEMBER && CharArrayUtils.equals(nchars, currentClassName))
					return true;
			}
		}
		return false;
	}

	private ICPPASTNamedTypeSpecifier buildNamedTypeSpecifier(IASTName name, boolean isTypename,
			int storageClass, int options, int offset, int endOffset) {
		ICPPASTNamedTypeSpecifier declSpec = nodeFactory.newTypedefNameSpecifier(name);
		declSpec.setIsTypename(isTypename);
		configureDeclSpec(declSpec, storageClass, options);
        ((ASTNode) declSpec).setOffsetAndLength(offset, endOffset - offset);
        return declSpec;
	}

	private ICPPASTSimpleDeclSpecifier buildSimpleDeclSpec(int storageClass, int simpleType,
			int options, int isLong, IASTExpression typeofExpression, int offset, int endOffset) {

		if (isLong > 1 && !supportLongLong)
			isLong= 1;
		
		ICPPASTSimpleDeclSpecifier declSpec= null;
        if (isLong > 1 || (options & (RESTRICT|COMPLEX|IMAGINARY)) != 0 || typeofExpression != null) {
        	final IGPPASTSimpleDeclSpecifier gppDeclSpec= nodeFactory.newSimpleDeclSpecifierGPP();
            gppDeclSpec.setLongLong(isLong > 1);
            gppDeclSpec.setRestrict((options & RESTRICT) != 0);
            gppDeclSpec.setComplex((options & COMPLEX) != 0);
            gppDeclSpec.setImaginary((options & IMAGINARY) != 0);
            gppDeclSpec.setTypeofExpression(typeofExpression);

        	declSpec= gppDeclSpec;
        } else {
        	declSpec = nodeFactory.newSimpleDeclSpecifier();
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
        		if (ASTQueries.findTypeRelevantDeclarator(dtor) instanceof IASTFunctionDeclarator) {
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


    /**
     * Parse an elaborated type specifier.
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    protected ICPPASTElaboratedTypeSpecifier elaboratedTypeSpecifier() throws BacktrackException, EndOfFileException {
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

        IASTName name = qualifiedName();

        ICPPASTElaboratedTypeSpecifier elaboratedTypeSpec = nodeFactory.newElaboratedTypeSpecifier(eck, name);
        ((ASTNode) elaboratedTypeSpec).setOffsetAndLength(t.getOffset(), calculateEndOffset(name) - t.getOffset());
        return elaboratedTypeSpec;
    }


    @Override
	protected IASTDeclarator initDeclarator(DeclarationOptions option) 
    		throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
    	// called from the lookahead, only.
    	return initDeclarator(DtorStrategy.PREFER_FUNCTION, option);
    }
    
	protected IASTDeclarator initDeclarator(IASTDeclSpecifier declspec, DeclarationOptions option) 
			throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
    	final IToken mark= mark();
    	IASTDeclarator dtor1= null;
    	IToken end1= null;
    	IASTDeclarator dtor2= null;
    	BacktrackException bt= null;
    	try {
    		dtor1= initDeclarator(DtorStrategy.PREFER_FUNCTION, option);
    		if (dtor1 instanceof IASTFunctionDeclarator == false)
    			return dtor1;
    		
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
    	
    	if (!option.fAllowConstructorInitializer || !canHaveConstructorInitializer(declspec, dtor1)) {
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
		
		if (functionBodyCount != 0) {
			// prefer the variable prototype:
			IASTDeclarator h= dtor1; dtor1= dtor2; dtor2= h;
		}
		CPPASTAmbiguousDeclarator dtor= new CPPASTAmbiguousDeclarator(dtor1, dtor2);
		dtor.setOffsetAndLength((ASTNode) dtor1);
		return dtor;
    }

	private boolean canHaveConstructorInitializer(IASTDeclSpecifier declspec, IASTDeclarator dtor) {
		if (declspec instanceof ICPPASTDeclSpecifier) {
			ICPPASTDeclSpecifier cppspec= (ICPPASTDeclSpecifier) declspec;			
			if (cppspec.isFriend()) {
				return false;
			}
			if (cppspec.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
				return false;
			}
		}

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
		}		
		
		if (dtor != null) {
			IASTName name = ASTQueries.findInnermostDeclarator(dtor).getName().getLastName();
			if (name instanceof ICPPASTTemplateId) {
				name= ((ICPPASTTemplateId) name).getTemplateName();
			}
			if (name instanceof ICPPASTOperatorName || name instanceof ICPPASTConversionName)
				return false;
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
	 * @throws FoundAggregateInitializer 
     */
    protected IASTDeclarator initDeclarator(DtorStrategy strategy, DeclarationOptions option)
            throws EndOfFileException, BacktrackException, FoundAggregateInitializer {
    	final IASTDeclarator dtor= declarator(strategy, option);
        if (option.fAllowInitializer) {
            if (LTcatchEOF(1) == IToken.tASSIGN && LTcatchEOF(2) == IToken.tLBRACE) 
            	throw new FoundAggregateInitializer(dtor);

        	IASTInitializer initializer= optionalCPPInitializer(dtor, option);
        	if (initializer != null) {
        		dtor.setInitializer(initializer);
        		adjustLength(dtor, initializer);
        	}
        }
        return dtor;
    }
    
    @Override
	protected IASTDeclarator addInitializer(FoundAggregateInitializer e, DeclarationOptions option) throws EndOfFileException {
	    final IASTDeclarator d = e.fDeclarator;
        try {
			IASTInitializer i = optionalCPPInitializer(e.fDeclarator, option);
			if (i != null) {
				d.setInitializer(i);
			    ((ASTNode) d).setLength(calculateEndOffset(i) - ((ASTNode) d).getOffset());
			}
		} catch (BacktrackException e1) {
			// mstodo add problem node
		}
		return d;
    }

    protected IASTInitializer optionalCPPInitializer(IASTDeclarator d, DeclarationOptions option) throws EndOfFileException, BacktrackException {
        // handle initializer
    	final int lt1= LTcatchEOF(1);
        if (lt1 == IToken.tASSIGN) {
            consume();
            // for member functions we need to consider pure-virtual syntax
			if (option == DeclarationOptions.CPP_MEMBER && LTcatchEOF(1) == IToken.tINTEGER) {
				IASTDeclarator relDtor = ASTQueries.findTypeRelevantDeclarator(d);
				// note the declarator for a member function cannot be ambiguous because it cannot be abstract
				if (relDtor instanceof ICPPASTFunctionDeclarator) {
					// check for pure virtual
					IToken t = consume();
					char[] image = t.getCharImage();
					if (image.length != 1 || image[0] != '0') {
						throwBacktrack(t); 
					}

					((ICPPASTFunctionDeclarator) relDtor).setPureVirtual(true);
					adjustEndOffset(d, t.getEndOffset()); // we can only adjust the offset of the outermost dtor.
					return null;
				}
			}
            try {
                return initializerClause(false);
            } catch (EndOfFileException eof) {
                failParse();
                throw eof;
            }
        } else if (option.fAllowConstructorInitializer && lt1 == IToken.tLPAREN) {
            if (d instanceof IASTFunctionDeclarator && d.getNestedDeclarator() == null) {
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
            ICPPASTConstructorInitializer result = nodeFactory.newConstructorInitializer(astExpression);
            ((ASTNode) result).setOffsetAndLength(o, l - o);
            return result;
        }
        return null;
    }


    protected IASTInitializer initializerClause(boolean inAggregateInitializer) throws EndOfFileException, BacktrackException {
        if (LT(1) == IToken.tLBRACE) {
            int startingOffset = consume().getOffset();

            IASTInitializerList result = nodeFactory.newInitializerList();
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

                IASTInitializer clause = initializerClause(true);
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
        if (inAggregateInitializer && skipTrivialExpressionsInAggregateInitializers) {
        	if (!ASTQueries.canContainName(assignmentExpression)) 
        		return null;
        }

        IASTInitializerExpression result = nodeFactory.newInitializerExpression(assignmentExpression);
        ((ASTNode) result).setOffsetAndLength(((ASTNode) assignmentExpression));
        return result;
    }


    @Override
	protected IASTTypeId typeId(DeclarationOptions option) throws EndOfFileException {
    	if (!canBeTypeSpecifier()) {
    		return null;
    	}
        int startingOffset = mark().getOffset();
        IASTDeclSpecifier declSpecifier = null;
        IASTDeclarator declarator = null;
        rejectLogicalOperatorInTemplateID++;
        try {
        	declSpecifier = declSpecifierSeq(option);
            if (LT(1) != IToken.tEOC) {
                declarator= declarator(DtorStrategy.PREFER_FUNCTION, option);
            }
        } catch (FoundDeclaratorException e) {
        	declSpecifier= e.declSpec;
        	declarator= e.declarator;
        	backup(e.currToken);
        } catch (FoundAggregateInitializer lie) {
            // type-ids have no initializers
        	return null;
        } catch (BacktrackException bt) {
        	return null;
        } finally {
        	rejectLogicalOperatorInTemplateID--;
        }
        IASTTypeId result = nodeFactory.newTypeId(declSpecifier, declarator); 
        ((ASTNode) result).setOffsetAndLength(startingOffset, figureEndOffset(declSpecifier, declarator) - startingOffset);
        return result;

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
        	
        	final IASTName declaratorName= qualifiedName();
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
        			cand1= declarator(pointerOps, nodeFactory.newName(), null, startingOffset, endOffset, strategy, option);
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
        			return declarator(pointerOps, nodeFactory.newName(), null, startingOffset, endOffset, strategy, option);
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
        return declarator(pointerOps, nodeFactory.newName(), null, startingOffset, endOffset, strategy, option);
    }

    /**
     * Parse a Pointer Operator. ptrOperator : "*" (cvQualifier)* | "&" | ::?
     * nestedNameSpecifier "*" (cvQualifier)*
     * 
     * @throws BacktrackException
     *             request a backtrack
     */
    private void consumePointerOperators(List<IASTPointerOperator> collection)
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
                ICPPASTReferenceOperator refOp = nodeFactory.newReferenceOperator();
                ((ASTNode) refOp).setOffsetAndLength(from, lastToken.getEndOffset()-from);
                collection.add(refOp);
                return;
            }
            
            IToken mark = mark();
            final int startOffset = mark.getOffset();
            boolean isConst = false, isVolatile = false, isRestrict = false;
            IASTName name= null;
            int coloncolon= LT(1) == IToken.tCOLONCOLON ? 1 : 0;
            loop: while (LTcatchEOF(coloncolon+1) == IToken.tIDENTIFIER) {
            	switch(LTcatchEOF(coloncolon+2)) {
            	case IToken.tCOLONCOLON:
            		coloncolon+= 2;
            		break;
            	case IToken.tLT:
            		coloncolon= 1;
            		break loop;
            	default:
            		coloncolon= 0;
            		break loop;
            	}
            }
            if (coloncolon != 0) {
                try {
                	name= qualifiedName();
                	if (name.getLookupKey().length != 0) {
                		backup(mark);
                		return;
                	}
                } catch (BacktrackException bt) {
                    backup(mark);
                    return;
                }
            }
            if (LTcatchEOF(1) != IToken.tSTAR) {
            	backup(mark);
            	return;
            }
            
            int endOffset= consume().getEndOffset();
            loop: for (;;) {
            	switch (LTcatchEOF(1)) {
            	case IToken.t_const:
            		endOffset= consume().getEndOffset();
            		isConst = true;
            		break;
            	case IToken.t_volatile:
            		endOffset= consume().getEndOffset();
            		isVolatile = true;
            		break;
            	case IToken.t_restrict:
            		if (!allowCPPRestrict) 
            			throwBacktrack(LA(1));
            		endOffset= consume().getEndOffset();
            		isRestrict = true;
            		break;
            	default:
            		break loop;
            	}
            }

                
            IASTPointer pointer;
            if (name != null) {
            	if(isRestrict) {
            		IGPPASTPointerToMember gppp2m = nodeFactory.newPointerToMemberGPP(name);
            		gppp2m.setRestrict(true);
            		pointer= gppp2m;
            	} else {
            		pointer= nodeFactory.newPointerToMember(name);
            	}
            } else {
            	if(isRestrict) {
            		IGPPASTPointer gpppo = nodeFactory.newPointerGPP();
            		gpppo.setRestrict(true);
            		pointer= gpppo;
            	} else {
            		pointer = nodeFactory.newPointer();
            	}
            }
            pointer.setConst(isConst);
            pointer.setVolatile(isVolatile);
            setRange(pointer, startOffset, endOffset);
            collection.add(pointer);
        }
    }

    private IASTDeclarator declarator(List<IASTPointerOperator> pointerOps,
    		IASTName declaratorName, IASTDeclarator nestedDeclarator, int startingOffset, int endOffset,
    		DtorStrategy strategy, DeclarationOptions option) 
    		throws EndOfFileException, BacktrackException {
        IASTDeclarator result= null;
        loop: while(true) {
        	final int lt1= LTcatchEOF(1);
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
        	result= nodeFactory.newDeclarator(null);
        	setDeclaratorID(result, declaratorName, nestedDeclarator);
        } else {
        	endOffset= calculateEndOffset(result);
        }

        if (LTcatchEOF(1) == IToken.t_asm) { // asm labels bug 226121
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
			declarator.setName(nodeFactory.newName());
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
		int endOffset= last.getEndOffset();
		
		final ICPPASTFunctionDeclarator fc = nodeFactory.newFunctionDeclarator(null);
		paramLoop: while(true) {
			switch (LT(1)) {
			case IToken.tRPAREN:
			case IToken.tEOC:
				endOffset= consume().getEndOffset();
				break paramLoop;
			case IToken.tELLIPSIS:
				endOffset= consume().getEndOffset();
				fc.setVarArgs(true);
				break;
			case IToken.tCOMMA:
				endOffset= consume().getEndOffset();
				seenParameter = false;
				break;
			default:
				if (seenParameter)
					throwBacktrack(startOffset, endOffset - startOffset);
			
				IASTParameterDeclaration pd = parameterDeclaration();
				fc.addParameterDeclaration(pd);
				endOffset = calculateEndOffset(pd);
				seenParameter = true;
				break;
			}
		}

		// Consume any number of __attribute__ tokens after the parameters
		__attribute_decl_seq(supportAttributeSpecifiers, false);

		// cv-qualifiers
		cvloop: while(true) {
			switch(LT(1)) {
			case IToken.t_const:
			    fc.setConst(true);
				endOffset= consume().getEndOffset();
				break;
			case IToken.t_volatile:
			    fc.setVolatile(true);
				endOffset= consume().getEndOffset();
				break;
			default:
				break cvloop;
			}
		}

		// throws clause
		if (LT(1) == IToken.t_throw) {
			fc.setEmptyExceptionSpecification();
			consume(); // throw
			consume(IToken.tLPAREN); 

			thloop: while (true) {
				switch (LT(1)) {
				case IToken.tRPAREN:
				case IToken.tEOC:
					endOffset = consume().getEndOffset();
					break thloop;
				case IToken.tCOMMA:
					consume();
					break;
				default:
					int thoffset = LA(1).getOffset();
					IASTTypeId typeId = typeId(DeclarationOptions.TYPEID);
					if (typeId != null) {
						fc.addExceptionSpecificationTypeId(typeId);
					} else {
						int thendoffset = LA(1).getOffset();
						if (thoffset == thendoffset) {
							thendoffset = consume().getEndOffset();
						}
						IASTProblem p = createProblem(IProblem.SYNTAX_ERROR, thoffset, thendoffset-thoffset);
						IASTProblemTypeId typeIdProblem = nodeFactory.newProblemTypeId(p);
						((ASTNode) typeIdProblem).setOffsetAndLength(((ASTNode) p));
						fc.addExceptionSpecificationTypeId(typeIdProblem);
					}
					break;
				}
			}

			// more __attribute__ after throws
			__attribute_decl_seq(supportAttributeSpecifiers, false);
		}

        setRange(fc, startOffset, endOffset);
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
        ((ASTNode) d).setOffsetAndLength(start, endOffset-start);
		return d;
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
            name = qualifiedName();
        else
            name = nodeFactory.newName();
        
        // if __attribute__ or __declspec occurs after struct/union/class identifier and before the { or ;
        __attribute_decl_seq(supportAttributeSpecifiers, supportDeclspecSpecifiers);
        
        ICPPASTCompositeTypeSpecifier astClassSpecifier = nodeFactory.newCompositeTypeSpecifier(classKind, name);

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
        final char[] outerName= currentClassName;
        currentClassName= name.getLookupKey();

        try {
        	declarationListInBraces(astClassSpecifier, offset, DeclarationOptions.CPP_MEMBER);
        } finally {
        	currentClassName= outerName;
        }
        return astClassSpecifier;
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
    protected void baseSpecifier(ICPPASTCompositeTypeSpecifier astClassSpec) throws EndOfFileException, BacktrackException {
    	int endOffset= consume().getEndOffset(); // tCOLON
        int startOffset= LA(1).getOffset();
        
        boolean isVirtual = false;
        int visibility = 0; 
        IASTName name = null;
        baseSpecifierLoop: for (;;) {
            switch (LT(1)) {
            case IToken.t_virtual:
                isVirtual = true;
            	endOffset= consume().getEndOffset();
                break;
            case IToken.t_public:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_public;
            	endOffset= consume().getEndOffset();
                break;
            case IToken.t_protected:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_protected;
            	endOffset= consume().getEndOffset();
                break;
            case IToken.t_private:
                visibility = ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.v_private;
            	endOffset= consume().getEndOffset();
                break;
            case IToken.tCOLONCOLON:
            case IToken.tIDENTIFIER:
            case IToken.tCOMPLETION:
                // to get templates right we need to use the class as the scope
                name = qualifiedName();
                endOffset= calculateEndOffset(name);
                break;
            case IToken.tCOMMA:
                if (name == null)
                    name = nodeFactory.newName();
                consume();
                ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier baseSpec = nodeFactory.newBaseSpecifier(name, visibility, isVirtual);
                setRange(baseSpec, startOffset, endOffset);
                astClassSpec.addBaseSpecifier(baseSpec);

                isVirtual = false;
                visibility = 0;
                name = null;
                startOffset= endOffset= LA(1).getOffset();
                continue baseSpecifierLoop;
            case IToken.tLBRACE:
            case IToken.tEOC:
                if (name == null)
                    name = nodeFactory.newName();
                baseSpec = nodeFactory.newBaseSpecifier(name, visibility, isVirtual);
                setRange(baseSpec, startOffset, endOffset);
                astClassSpec.addBaseSpecifier(baseSpec);
                break baseSpecifierLoop;

            default:
                break baseSpecifierLoop;
            }
        }
    }


    protected void catchHandlerSequence(List<ICPPASTCatchHandler> collection) throws EndOfFileException, BacktrackException {
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
                IASTProblemDeclaration pd = nodeFactory.newProblemDeclaration(p);
                ((ASTNode) pd).setOffsetAndLength(((ASTNode) p));
                decl = pd;
            }

            ICPPASTCatchHandler handler = nodeFactory.newCatchHandler(decl, null);

            if (LT(1) != IToken.tEOC) {
                IASTStatement compoundStatement = catchBlockCompoundStatement();
                ((ASTNode) handler).setOffsetAndLength(startOffset, calculateEndOffset(compoundStatement) - startOffset);
                handler.setIsCatchAll(isEllipsis);
                if (compoundStatement != null) {
                    handler.setCatchBody(compoundStatement);
                }
            }
            
            collection.add(handler);
            lt1 = LTcatchEOF(1);
        }
    }

	private IASTSimpleDeclaration simpleSingleDeclaration(DeclarationOptions options) throws BacktrackException,	EndOfFileException {
        final int startOffset= LA(1).getOffset();
    	IASTDeclSpecifier declSpec= null;
    	IASTDeclarator declarator;

    	try {
    		declSpec= declSpecifierSeq(options);
    		declarator= initDeclarator(declSpec, options);
    	} catch (FoundDeclaratorException e) {
    		declSpec= e.declSpec;
    		declarator= e.declarator;
    		backup(e.currToken);
        } catch (FoundAggregateInitializer lie) {
        	if (declSpec == null)
        		declSpec= lie.fDeclSpec;
        	declarator= addInitializer(lie, options);
    	}

    	final int endOffset = figureEndOffset(declSpec, declarator);
    	final IASTSimpleDeclaration decl= nodeFactory.newSimpleDeclaration(declSpec); 
    	decl.addDeclarator(declarator);
    	((ASTNode) decl).setOffsetAndLength(startOffset, endOffset - startOffset);
    	
    	return decl;
	}


    protected IASTStatement catchBlockCompoundStatement() throws BacktrackException, EndOfFileException {
        if (mode == ParserMode.QUICK_PARSE || mode == ParserMode.STRUCTURAL_PARSE || !isActiveCode()) {
            int offset = LA(1).getOffset();
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = nodeFactory.newCompoundStatement();
            setRange(cs, offset, last.getEndOffset());
            return cs;
        } else if (mode == ParserMode.COMPLETION_PARSE || mode == ParserMode.SELECTION_PARSE) {
            if (scanner.isOnTopContext())
                return compoundStatement();
            int offset = LA(1).getOffset();
            IToken last = skipOverCompoundStatement();
            IASTCompoundStatement cs = nodeFactory.newCompoundStatement();
            setRange(cs, offset, last.getEndOffset());
            return cs;
        }
        return compoundStatement();
    }

	@Override
	protected void setupTranslationUnit() throws DOMException {
		translationUnit = nodeFactory.newTranslationUnit();
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
		if(translationUnit instanceof ASTTranslationUnit)
			((ASTTranslationUnit)translationUnit).setLocationResolver(scanner.getLocationResolver());
	}


    private void consumeArrayModifiers(DeclarationOptions option, List<IASTArrayModifier> collection)
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
            IASTArrayModifier arrayMod = nodeFactory.newArrayModifier(exp);
            ((ASTNode) arrayMod).setOffsetAndLength(o, l - o);
            collection.add(arrayMod);
        }
        return;
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
        ICPPASTTryBlockStatement tryStatement = nodeFactory.newTryBlockStatement(tryBlock);
        ((ASTNode) tryStatement).setOffset(startO);

        for (int i = 0; i < catchHandlers.size(); ++i) {
            ICPPASTCatchHandler handler = catchHandlers.get(i);
            tryStatement.addCatchHandler(handler);
            ((ASTNode) tryStatement).setLength(calculateEndOffset(handler) - startO);
        }
        return tryStatement;
    }

    @Override
	protected void nullifyTranslationUnit() {
        translationUnit = null;
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

        IASTWhileStatement while_statement;
        if (while_condition instanceof IASTExpression)
        	while_statement = nodeFactory.newWhileStatement((IASTExpression)while_condition, while_body);
        else
        	while_statement = nodeFactory.newWhileStatement((IASTDeclaration)while_condition, while_body);
        
        ((ASTNode) while_statement).setOffsetAndLength(startOffset,
                (while_body != null ? calculateEndOffset(while_body) : LA(1).getEndOffset()) - startOffset);
        return while_statement;

    }

    protected IASTNode cppStyleCondition(int expectToken) throws BacktrackException, EndOfFileException {
        IASTExpression e= null;
        IASTSimpleDeclaration decl= null;
        IToken end= null;
        
        IToken mark = mark();
        try {
        	decl= simpleSingleDeclaration(DeclarationOptions.CONDITION);
        	end= LA(1);
        	final int la= end.getType();
        	if (la != expectToken && la != IToken.tEOC) {
        		end= null;
        		decl= null;
        	}        		
        } catch (BacktrackException b) {
        }

        backup(mark);
        try {
            e= expression();

            final IToken end2= LA(1);
        	final int la= end2.getType();
        	if (la != expectToken && la != IToken.tEOC) {
        		throwBacktrack(end2);
        	}
            if (end == null)
            	return e;
            

            final int endOffset = end.getOffset();
			final int endOffset2 = end2.getOffset();
			if (endOffset == endOffset2) {
                CPPASTAmbiguousCondition ambig= new CPPASTAmbiguousCondition(e, decl);
                setRange(ambig, e);
                return ambig;
            }
            
            if (endOffset < endOffset2) 
            	return e;
        } catch (BacktrackException bt) {
        	if (end == null) {
        		if (expectToken == IToken.tRPAREN) {
        			backup(mark);
        			return skipProblemConditionInParenthesis(mark.getOffset());
        		}
        		throw bt;
        	}
        }
    	backup(end);
        return decl;
    }


    @Override
	protected ASTVisitor createAmbiguityNodeVisitor() {
        return new CPPASTAmbiguityResolver();
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
            	ICPPASTIfStatement new_if = nodeFactory.newIfStatement();
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
            ICPPASTIfStatement new_if_statement = nodeFactory.newIfStatement();
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
        ICPPASTSwitchStatement switch_statement = nodeFactory.newSwitchStatement();
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
        ICPPASTForStatement for_statement = nodeFactory.newForStatement();
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
