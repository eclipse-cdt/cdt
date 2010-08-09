/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import static org.eclipse.cdt.core.dom.lrparser.action.ParserUtil.*;
import static org.eclipse.cdt.core.parser.util.CollectionUtils.findFirstAndRemove;
import static org.eclipse.cdt.core.parser.util.CollectionUtils.reverseIterable;
import static org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParsersym.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAmbiguousTemplateArgument;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.LPGTokenAdapter;
import org.eclipse.cdt.core.dom.lrparser.action.BuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ParserUtil;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParsersym;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousTemplateArgument;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 * Semantic actions that build the AST during the parse. 
 * These are the actions that are specific to the C++ parser, the superclass
 * contains actions that can be shared with the C99 parser.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class CPPBuildASTParserAction extends BuildASTParserAction {

	/** Allows code in this class to refer to the token kinds in CPPParsersym */
	private final ITokenMap tokenMap;
	
	/** Used to create the AST node objects */
	protected final ICPPNodeFactory nodeFactory;
	
	protected final ICPPSecondaryParserFactory parserFactory;
	
	/** Stack that provides easy access to the current class name, used to disambiguate declarators. */
	protected final LinkedList<IASTName> classNames = new LinkedList<IASTName>();
	
	
	/**
	 * @param parser
	 * @param orderedTerminalSymbols When an instance of this class is created for a parser
	 * that parsers token kinds will be mapped back to the base C99 parser's token kinds.
	 */
	public CPPBuildASTParserAction(ITokenStream parser, ScopedStack<Object> astStack, ICPPNodeFactory nodeFactory, ICPPSecondaryParserFactory parserFactory) {
		super(parser, astStack, nodeFactory, parserFactory);
		
		this.nodeFactory = nodeFactory;
		this.parserFactory = parserFactory;
		this.tokenMap = new TokenMap(CPPParsersym.orderedTerminalSymbols, parser.getOrderedTerminalSymbols());
	}
	
	
	private int baseKind(IToken token) {
		return tokenMap.mapKind(token.getKind());
	}
	
	@Override 
	protected boolean isCompletionToken(IToken token) {
		return baseKind(token) == TK_Completion;
	}
	
	@Override
	protected boolean isIdentifierToken(IToken token) {
		return baseKind(token) == TK_identifier;
	}
	
	@Override
	protected IASTName createName(char[] image) {
		return nodeFactory.newName(image);
	}


	
	public void consumeNewInitializer() {
		if(astStack.peek() == null) { // if there is an empty set of parens
			astStack.pop();
			IASTExpression initializer = nodeFactory.newExpressionList();
			setOffsetAndLength(initializer);
			astStack.push(initializer);
		}
	}
	
	
	
	// TODO can the new_array_expressions be removed? it looks like they parse as part of the type id
	/**
	 * new_expression
     *     ::= dcolon_opt 'new' new_placement_opt new_type_id <openscope-ast> new_array_expressions_op new_initializer_opt
     *       | dcolon_opt 'new' new_placement_opt '(' type_id ')' <openscope-ast> new_array_expressions_op new_initializer_opt
	 */
	public void consumeExpressionNew(boolean isNewTypeId) {
		IASTExpression initializer = (IASTExpression) astStack.pop(); // may be null
		List<Object> arrayExpressions = astStack.closeScope();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTExpression placement = (IASTExpression) astStack.pop(); // may be null
		boolean hasDoubleColon = astStack.pop() != null;
		
		ICPPASTNewExpression newExpression = nodeFactory.newNewExpression(placement, initializer, typeId);
		newExpression.setIsGlobal(hasDoubleColon);
		newExpression.setIsNewTypeId(isNewTypeId);
		setOffsetAndLength(newExpression);
		
		for(Object expr : arrayExpressions)
			newExpression.addNewTypeIdArrayExpression((IASTExpression)expr);
		
		// handle ambiguities of the form:  (A)(B)
		if(!isNewTypeId && initializer == null &&
				placement instanceof IASTIdExpression && 
				typeId != null && typeId.getDeclSpecifier() instanceof IASTNamedTypeSpecifier) {
			
			IASTName firstName = ((IASTIdExpression)placement).getName();
			IASTName secondName = ((IASTNamedTypeSpecifier)typeId.getDeclSpecifier()).getName();
			
			IASTNamedTypeSpecifier newTypeSpecifier = nodeFactory.newTypedefNameSpecifier(firstName.copy());
			ParserUtil.setOffsetAndLength(newTypeSpecifier, firstName);
			IASTDeclarator newDeclarator = nodeFactory.newDeclarator(nodeFactory.newName());
			ParserUtil.setOffsetAndLength(newDeclarator, endOffset(firstName), 0);
			IASTTypeId newTypeId = nodeFactory.newTypeId(newTypeSpecifier, newDeclarator);
			ParserUtil.setOffsetAndLength(newTypeId, firstName);
			
			IASTIdExpression newInitializer = nodeFactory.newIdExpression(secondName.copy());
			ParserUtil.setOffsetAndLength(newInitializer, secondName);
			
			ICPPASTNewExpression alternate = nodeFactory.newNewExpression(null, newInitializer, newTypeId);
			ParserUtil.setOffsetAndLength(alternate, newExpression);
			newExpression.setIsGlobal(hasDoubleColon);
			newExpression.setIsNewTypeId(isNewTypeId);
			
			IASTAmbiguousExpression ambiguity = createAmbiguousExpression(newExpression, alternate);
			astStack.push(ambiguity);
		}
		else {
			astStack.push(newExpression);
		}
	}
	
	
	/**
	 * new_declarator -- pointer operators are part of the type id, held in an empty declarator
     *     ::= <openscope-ast> new_pointer_operators
	 */
	public void consumeNewDeclarator() {
		IASTName name = nodeFactory.newName();
		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
		
		for(Object pointer : astStack.closeScope())
			declarator.addPointerOperator((IASTPointerOperator)pointer);
		
		setOffsetAndLength(declarator);
		astStack.push(declarator);
	}
	
	
	/**
	 * throw_expression
     *     ::= 'throw'
     *       | 'throw' assignment_expression
	 */
	public void consumeExpressionThrow(boolean hasExpr) {
		IASTExpression operand = hasExpr ? (IASTExpression) astStack.pop() : null;
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression(ICPPASTUnaryExpression.op_throw, operand);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * delete_expression
     *     ::= dcolon_opt 'delete' cast_expression
     *       | dcolon_opt 'delete' '[' ']' cast_expression
	 * @param isVectorized
	 */
	public void consumeExpressionDelete(boolean isVectorized) {
		IASTExpression operand = (IASTExpression) astStack.pop();
		boolean hasDoubleColon = astStack.pop() != null;
		
		ICPPASTDeleteExpression deleteExpr = nodeFactory.newDeleteExpression(operand);
		deleteExpr.setIsGlobal(hasDoubleColon);
		deleteExpr.setIsVectored(isVectorized);
		
		setOffsetAndLength(deleteExpr);
		astStack.push(deleteExpr);
	}
	
	
	/**
	 * 
	 */
	public void consumeExpressionFieldReference(boolean isPointerDereference, boolean hasTemplateKeyword) {
		IASTName name = (IASTName) astStack.pop();
		IASTExpression owner = (IASTExpression) astStack.pop();
		ICPPASTFieldReference expr = nodeFactory.newFieldReference(name, owner);
		expr.setIsPointerDereference(isPointerDereference);
		expr.setIsTemplate(hasTemplateKeyword);
		
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	/**
	 * postfix_expression
	 *     ::= simple_type_specifier '(' expression_list_opt ')'
	 */
	public void consumeExpressionSimpleTypeConstructor() {
		IASTExpression expression = (IASTExpression) astStack.pop();
		IToken token = (IToken) astStack.pop();
		////CDT_70_FIX_FROM_50-#3
		//int type = asICPPASTSimpleTypeConstructorExpressionType(token);
		ICPPASTConstructorInitializer init=null;
		if(expression!=null){
			init = new CPPASTConstructorInitializer();
			init.setExpression(expression);
    	
    		((ASTNode)init).setOffsetAndLength(((ASTNode)expression).getOffset(),((ASTNode)expression).getLength());
    	}
    	ICPPASTDeclSpecifier declspec = transformIntoSimpleTypeSpecifier(token);
		ICPPASTSimpleTypeConstructorExpression typeConstructor = nodeFactory.newSimpleTypeConstructorExpression(declspec, init);
		
		setOffsetAndLength(typeConstructor);
		astStack.push(typeConstructor);
	}
	
	
	private int asICPPASTSimpleTypeConstructorExpressionType(IToken token) {
		assert token != null;
		
		switch(baseKind(token)) {
			case TK_char     : return ICPPASTSimpleTypeConstructorExpression.t_char;
			case TK_wchar_t  : return ICPPASTSimpleTypeConstructorExpression.t_wchar_t;
			case TK_bool     : return ICPPASTSimpleTypeConstructorExpression.t_bool;
			case TK_short    : return ICPPASTSimpleTypeConstructorExpression.t_short;
			case TK_int      : return ICPPASTSimpleTypeConstructorExpression.t_int;
			case TK_long     : return ICPPASTSimpleTypeConstructorExpression.t_long;
			case TK_signed   : return ICPPASTSimpleTypeConstructorExpression.t_signed;
			case TK_unsigned : return ICPPASTSimpleTypeConstructorExpression.t_unsigned;
			case TK_float    : return ICPPASTSimpleTypeConstructorExpression.t_float;
			case TK_double   : return ICPPASTSimpleTypeConstructorExpression.t_double;
			case TK_void     : return ICPPASTSimpleTypeConstructorExpression.t_void;
		
			default:
				assert false : "type parsed wrong"; //$NON-NLS-1$
				return ICPPASTSimpleTypeConstructorExpression.t_unspecified;
		}
	}
	
	private ICPPASTDeclSpecifier transformIntoSimpleTypeSpecifier(IToken token){
		ICPPASTSimpleDeclSpecifier declspec= nodeFactory.newSimpleDeclSpecifier();
		switch(baseKind(token)) {
			case TK_char     : declspec.setType(Kind.eChar); break;
			case TK_wchar_t  : declspec.setType(Kind.eWChar); break;
			case TK_bool     : declspec.setType(Kind.eBoolean);break;
			case TK_short    : declspec.setShort(true); break;
			case TK_int      : declspec.setType(Kind.eInt); break;
			case TK_long     : declspec.setLong(true);	break;
			case TK_signed   : declspec.setSigned(true); break;
			case TK_unsigned : declspec.setUnsigned(true); break;
			case TK_float    : declspec.setType(Kind.eFloat); break;
			case TK_double   : declspec.setType(Kind.eDouble); break;
			case TK_void     : declspec.setType(Kind.eVoid); break;
		
			default:
				assert false : "type parsed wrong"; //$NON-NLS-1$
			    declspec.setType(Kind.eUnspecified);
			    break;
		}
		((ASTNode) declspec).setOffset(token.getStartOffset());
		int ruleLength = token.getEndOffset() - token.getStartOffset();
		((ASTNode) declspec).setLength(ruleLength < 0 ? 0 : ruleLength);
		
		return declspec;
		
	
	}
	
	
	/**
	 * postfix_expression
	 *     ::= 'typename' dcolon_opt nested_name_specifier <empty>  identifier_name '(' expression_list_opt ')'
     *       | 'typename' dcolon_opt nested_name_specifier template_opt template_id '(' expression_list_opt ')'
	 */
	@SuppressWarnings("unchecked")
	public void consumeExpressionTypeName() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTName name = (IASTName) astStack.pop();
		boolean isTemplate = astStack.pop() == PLACE_HOLDER;
		LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
		IToken dColon = (IToken) astStack.pop();

		nestedNames.addFirst(name);
		
		int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
		int endOffset   = endOffset(name);
		IASTName qualifiedName = createQualifiedName(nestedNames, startOffset, endOffset, dColon != null);
		
		ICPPASTTypenameExpression typenameExpr = nodeFactory.newTypenameExpression(qualifiedName, expr, isTemplate);
		
		setOffsetAndLength(typenameExpr);
		astStack.push(typenameExpr);
	}
	
	
	/**
	 * condition
     *     ::= type_specifier_seq declarator '=' assignment_expression
	 */
	public void consumeConditionDeclaration() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTDeclarator declarator = (IASTDeclarator) astStack.pop();
		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) astStack.pop();
		//CDT_70_FIX_FROM_50-#2
		//IASTInitializerExpression initializer = nodeFactory.newInitializerExpression(expr);
		IASTEqualsInitializer initializer = nodeFactory.newEqualsInitializer(expr);
		ParserUtil.setOffsetAndLength(initializer, offset(expr), length(expr));
		declarator.setInitializer(initializer);
		
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpec);
		declaration.addDeclarator(declarator);
		
		setOffsetAndLength(declaration);
		astStack.push(declaration);
	}
	
	
	/**
	 * template_id
     *     ::= identifier_name '<' <openscope-ast> template_argument_list_opt '>'
     *     
     * operator_function_id
     *     ::= operator_id '<' <openscope-ast> template_argument_list_opt '>'
	 */
	public void consumeTemplateId() {
		List<Object> templateArguments = astStack.closeScope();
		IASTName name = (IASTName) astStack.pop();
		
		ICPPASTTemplateId templateId = nodeFactory.newTemplateId(name);
		
		for(Object arg : templateArguments) {
			if(arg instanceof IASTExpression)
				templateId.addTemplateArgument((IASTExpression)arg);
			else if(arg instanceof IASTTypeId)
				templateId.addTemplateArgument((IASTTypeId)arg);
			else if(arg instanceof ICPPASTAmbiguousTemplateArgument)
				templateId.addTemplateArgument((ICPPASTAmbiguousTemplateArgument)arg);
		}
		
		setOffsetAndLength(templateId);
		astStack.push(templateId);
	}
	

	/**
	 * Disambiguates template arguments.
	 */
	public void consumeTemplateArgumentTypeId() {
		// TODO is this necessary? It should be able to tell if it looks like an id expression
		ISecondaryParser<IASTExpression> secondaryParser = parserFactory.getExpressionParser(stream, properties);
		IASTExpression result = runSecondaryParser(secondaryParser);
		
		// The grammar rule allows assignment_expression, but the ambiguity
		// only arises with id_expressions.
		if(!(result instanceof IASTIdExpression))
			return;
		
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTIdExpression expr = (IASTIdExpression) result;
		
		ICPPASTAmbiguousTemplateArgument ambiguityNode = new CPPASTAmbiguousTemplateArgument(typeId, expr);
		//setOffsetAndLength(ambiguityNode);
		
		astStack.push(ambiguityNode);
	}
	
	
	/**
	 * Disambiguates template arguments.
	 * Qualified names parse as an expression, so generate the corresponding
	 * typeId and create an ambiguity node.
	 */
	public void consumeTemplateArgumentExpression() {
		IASTExpression expr = (IASTExpression) astStack.peek();
		
		if(expr instanceof IASTIdExpression) {
			IASTName orgName =((IASTIdExpression)expr).getName();
			IASTName name = null;
			try{
			 name = orgName.copy();
			 //if there is node throws UnsupportedOperationException in copy, just use the original node
			} catch(UnsupportedOperationException ue){
				name = orgName;
			}
			
			ParserUtil.setOffsetAndLength(name, expr);
			
			IASTNamedTypeSpecifier declSpec = nodeFactory.newTypedefNameSpecifier(name);
			ParserUtil.setOffsetAndLength(declSpec, name);
			
			IASTDeclarator declarator = nodeFactory.newDeclarator(nodeFactory.newName());
			ParserUtil.setOffsetAndLength(declarator, endOffset(declSpec), 0);
			
			IASTTypeId typeId = nodeFactory.newTypeId(declSpec, declarator);
			setOffsetAndLength(typeId);
			
			ICPPASTAmbiguousTemplateArgument ambiguityNode = new CPPASTAmbiguousTemplateArgument(typeId, expr);
			
			astStack.pop();
			astStack.push(ambiguityNode);
		}
	}
	
	
	/**
	 * operator_id
     *     ::= 'operator' overloadable_operator
	 */
	public void consumeOperatorName() {
		List<IToken> tokens = stream.getRuleTokens();
		tokens = tokens.subList(1, tokens.size());
		OverloadableOperator operator = getOverloadableOperator(tokens);
		
		ICPPASTOperatorName name = nodeFactory.newOperatorName(operator.toCharArray());
		setOffsetAndLength(name);
		astStack.push(name);
	}

	
	private OverloadableOperator getOverloadableOperator(List<IToken> tokens) {
		if(tokens.size() == 1) {
			// TODO this is a hack that I did to save time
			LPGTokenAdapter coreToken = (LPGTokenAdapter) tokens.get(0);
			return OverloadableOperator.valueOf(coreToken.getWrappedToken());
		}
		else if(matchTokens(tokens, tokenMap, TK_new, TK_LeftBracket, TK_RightBracket)) {
			return OverloadableOperator.NEW_ARRAY;
		}
		else if(matchTokens(tokens, tokenMap, TK_delete, TK_LeftBracket, TK_RightBracket)) {
			return OverloadableOperator.DELETE_ARRAY;
		}
		else if(matchTokens(tokens, tokenMap, TK_LeftBracket, TK_RightBracket)) {
			return OverloadableOperator.BRACKET;
		}
		else if(matchTokens(tokens, tokenMap, TK_LeftParen, TK_RightParen)) {
			return OverloadableOperator.PAREN;
		}
		
		return null;
	}
	
	
	/**
	 * conversion_function_id
     *     ::= 'operator' conversion_type_id
	 */
	public void consumeConversionName() {
//	    Representation is computed by the conversion name itself, see bug 258054
//		String rep = createStringRepresentation(parser.getRuleTokens());
//		char[] chars = rep.toCharArray();
		
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		ICPPASTConversionName name = nodeFactory.newConversionName(typeId);
		setOffsetAndLength(name);
		astStack.push(name);
	}
	
	
	
	
    /**
     * unqualified_id
     *     ::= '~' identifier_token
     */
  	public void consumeDestructorName() {
  		char[] chars = ("~" + stream.getRightIToken()).toCharArray(); //$NON-NLS-1$
  		
  		IASTName name = nodeFactory.newName(chars);
  		setOffsetAndLength(name);
  		astStack.push(name);
  	}
  	
  	
  	/**
  	 * destructor_type_name
     *     ::= '~' template_id_name
  	 */
  	public void consumeDestructorNameTemplateId() {
  		ICPPASTTemplateId templateId = (ICPPASTTemplateId) astStack.peek();
  		
  		IASTName oldName = templateId.getTemplateName();
  		char[] newChars = ("~" + oldName).toCharArray(); //$NON-NLS-1$
  		
  		IASTName newName = nodeFactory.newName(newChars);
  		
  		int offset = offset(stream.getLeftIToken());
  		int length = offset - endOffset(oldName);
  		ParserUtil.setOffsetAndLength(newName, offset, length);
  		
  		templateId.setTemplateName(newName);
  	}
  	
  	
  	
  	/**
  	 * qualified_id
     *     ::= '::' identifier_name
     *       | '::' operator_function_id
     *       | '::' template_id
  	 */

	public void consumeGlobalQualifiedId() {
  		IASTName name = (IASTName) astStack.pop();
  		
  		ICPPASTQualifiedName qualifiedName = nodeFactory.newQualifiedName();
  		qualifiedName.addName(name);
  		qualifiedName.setFullyQualified(true);
  		setOffsetAndLength(qualifiedName);
  		astStack.push(qualifiedName);
  	}
  	
  	
  	/**
	 * selection_statement ::=  switch '(' condition ')' statement
	 */
	public void consumeStatementSwitch() {
		IASTStatement body = (IASTStatement) astStack.pop();
		
		Object condition = astStack.pop();
		
		IASTSwitchStatement stat;
		if(condition instanceof IASTExpression)
			stat = nodeFactory.newSwitchStatement((IASTExpression)condition, body);
		else
			stat = nodeFactory.newSwitchStatement((IASTDeclaration)condition, body);
		
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	

	public void consumeStatementIf(boolean hasElse) {
		IASTStatement elseClause = hasElse ? (IASTStatement)astStack.pop() : null;		
		IASTStatement thenClause = (IASTStatement) astStack.pop();
		
		Object condition = astStack.pop();
		
		IASTIfStatement ifStatement;
		if(condition instanceof IASTExpression)
			ifStatement = nodeFactory.newIfStatement((IASTExpression)condition, thenClause, elseClause);
		else
			ifStatement = nodeFactory.newIfStatement((IASTDeclaration)condition, thenClause, elseClause);
		
		setOffsetAndLength(ifStatement);
		astStack.push(ifStatement);
	}
	
	
	/**
	 * iteration_statement ::= 'while' '(' condition ')' statement
	 */
	public void consumeStatementWhileLoop() {
		IASTStatement body = (IASTStatement) astStack.pop();
		
		Object condition = astStack.pop();
		
		IASTWhileStatement whileStatement;
		if(condition instanceof IASTExpression)
			whileStatement = nodeFactory.newWhileStatement((IASTExpression)condition, body);
		else
			whileStatement = nodeFactory.newWhileStatement((IASTDeclaration)condition, body);
		
		setOffsetAndLength(whileStatement);
		astStack.push(whileStatement);
	}
	
	
	/**
	 */
	public void consumeStatementForLoop() {
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		Object condition = astStack.pop(); // can be an expression or a declaration
		IASTStatement initializer = (IASTStatement) astStack.pop();
		
		// bug 234463, fix for content assist to work in this case
		int TK_EOC = TK_EndOfCompletion; // TODO: change this in the grammar file
		List<IToken> tokens = stream.getRuleTokens();
		if(matchTokens(tokens, tokenMap, 
				TK_for, TK_LeftParen, TK_Completion, TK_EOC, TK_EOC, TK_EOC, TK_EOC)) {
			IASTName name = createName(tokens.get(2));
			IASTIdExpression idExpression = nodeFactory.newIdExpression(name);
			ParserUtil.setOffsetAndLength(idExpression, offset(name), length(name));
			initializer = nodeFactory.newExpressionStatement(idExpression);
			ParserUtil.setOffsetAndLength(initializer, offset(name), length(name));
		}
		
		
		IASTForStatement forStat;
		if(condition instanceof IASTExpression)
			forStat = nodeFactory.newForStatement(initializer, (IASTExpression)condition, expr, body);
		else // its a declaration or its null
			forStat = nodeFactory.newForStatement(initializer, (IASTDeclaration)condition, expr, body);
		
		setOffsetAndLength(forStat);
		astStack.push(forStat);
	}
	
	
	/**
	 * try_block
     *     ::= 'try' compound_statement <openscope-ast> handler_seq
     */
	public void consumeStatementTryBlock(boolean hasCatchBlock) {
		List<Object> handlerSeq = hasCatchBlock ? astStack.closeScope() : Collections.emptyList();
		IASTStatement body = (IASTStatement) astStack.pop();
		
		ICPPASTTryBlockStatement tryStatement = nodeFactory.newTryBlockStatement(body);
		
		for(Object handler : handlerSeq)
			tryStatement.addCatchHandler((ICPPASTCatchHandler)handler);
		
		setOffsetAndLength(tryStatement);
		astStack.push(tryStatement);
	}
	
	
	/**
	 * handler
     *     ::= 'catch' '(' exception_declaration ')' compound_statement
     *       | 'catch' '(' '...' ')' compound_statement
	 */
	 public void consumeStatementCatchHandler(boolean hasEllipsis) {
		 IASTStatement body = (IASTStatement) astStack.pop();
		 IASTDeclaration decl = hasEllipsis ? null : (IASTDeclaration) astStack.pop();
		 
		 ICPPASTCatchHandler catchHandler = nodeFactory.newCatchHandler(decl, body);
		 catchHandler.setIsCatchAll(hasEllipsis);

		 setOffsetAndLength(catchHandler);
	     astStack.push(catchHandler);
	 }
	 
	
	/**
	 * nested_name_specifier
     *     ::= class_or_namespace_name '::' nested_name_specifier_with_template
     *       | class_or_namespace_name '::' 
     *
     * nested_name_specifier_with_template
     *     ::= class_or_namespace_name_with_template '::' nested_name_specifier_with_template
     *       | class_or_namespace_name_with_template '::'
     *       
     *        
     * Creates and updates a list of the nested names on the stack.
     * Important: the names in the list are in *reverse* order,
     * this is because the actions fire in reverse order.
	 */
	@SuppressWarnings("unchecked")
	public void consumeNestedNameSpecifier(final boolean hasNested) {
		LinkedList<IASTName> names;
		if(hasNested)
			names = (LinkedList<IASTName>) astStack.pop();
		else
			names = new LinkedList<IASTName>();
		
		IASTName name = (IASTName) astStack.pop();
		names.add(name);
		
		astStack.push(names);
	}

	
	public void consumeNestedNameSpecifierEmpty() {
		// can't use Collections.EMPTY_LIST because we need a list thats mutable
		astStack.push(new LinkedList<IASTName>());
	}
	
	
	
	
	/**
	 * The template keyword is optional but must be the leftmost token.
	 * 
	 * This just throws away the template keyword.
	 */
	public void consumeNameWithTemplateKeyword() { 
		IASTName name = (IASTName) astStack.pop();
		astStack.pop(); // pop the template keyword
		astStack.push(name);
	}

	
	
	/**
	 * qualified_id
     *     ::= dcolon_opt nested_name_specifier any_name
	 */
	public void consumeQualifiedId(boolean hasTemplateKeyword) {
		IASTName qualifiedName = subRuleQualifiedName(hasTemplateKeyword);
		astStack.push(qualifiedName);
	}
	
	
	private IASTName createQualifiedName(LinkedList<IASTName> nestedNames, int startOffset, int endOffset, boolean startsWithColonColon) {
		return createQualifiedName(nestedNames, startOffset, endOffset, startsWithColonColon, false);
	}
	
	
	/**
	 * Creates a qualified name from a list of names (that must be in reverse order).
	 * 
	 * @param names List of name nodes in reverse order
	 */
	private IASTName createQualifiedName(LinkedList<IASTName> names, int startOffset, int endOffset, boolean startsWithColonColon, boolean endsWithColonColon) {
		if(!endsWithColonColon && !startsWithColonColon && names.size() == 1) 
			return names.getFirst(); // its actually an unqualified name

		ICPPASTQualifiedName qualifiedName = nodeFactory.newQualifiedName();
		qualifiedName.setFullyQualified(startsWithColonColon);
		ParserUtil.setOffsetAndLength(qualifiedName, startOffset, endOffset - startOffset);
		for(IASTName name : reverseIterable(names))
			qualifiedName.addName(name);
	
		// there must be a dummy name in the AST after the last double colon, this happens with pointer to member names
		if(endsWithColonColon) {
			IASTName dummyName = nodeFactory.newName();
			ParserUtil.setOffsetAndLength(dummyName, endOffset, 0);
			qualifiedName.addName(dummyName);
		}
		
		return qualifiedName;
	}

	
	
	/**
	 * Consumes grammar sub-rules of the following form:
	 * 
	 * dcolon_opt nested_name_specifier_opt keyword_opt name
	 * 
	 * Where name is any rule that produces an IASTName node on the stack.
	 * Does not place the resulting node on the stack, returns it instead.
	 */
	@SuppressWarnings("unchecked")
	private IASTName subRuleQualifiedName(boolean hasOptionalKeyword) {
		IASTName lastName = (IASTName) astStack.pop();
		
		if(hasOptionalKeyword) // this is usually a template keyword and can be ignored
			astStack.pop();
		
		LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
		IToken dColon = (IToken) astStack.pop();
		
		if(nestedNames.isEmpty() && dColon == null) { // then its not a qualified name
			return lastName;
		}

		nestedNames.addFirst(lastName); // the list of names is in reverse order
		
		int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
		int endOffset = endOffset(lastName);
		
		return createQualifiedName(nestedNames, startOffset, endOffset, dColon != null);
	}
	
	
	
	/**
	 * pseudo_destructor_name
     *     ::= dcolon_opt nested_name_specifier_opt type_name '::' destructor_type_name
     *       | dcolon_opt nested_name_specifier 'template' template_id '::' destructor_type_name
     *       | dcolon_opt nested_name_specifier_opt destructor_type_name
     */
	@SuppressWarnings("unchecked")
	public void consumePsudoDestructorName(boolean hasExtraTypeName) {
		IASTName destructorTypeName = (IASTName) astStack.pop();
		IASTName extraName = hasExtraTypeName ? (IASTName) astStack.pop() : null;
		LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
		IToken dColon = (IToken) astStack.pop();
		
		if(hasExtraTypeName)
			nestedNames.addFirst(extraName);
		
		nestedNames.addFirst(destructorTypeName);
		
		int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
		int endOffset = endOffset(destructorTypeName);
		IASTName qualifiedName = createQualifiedName(nestedNames, startOffset, endOffset, dColon != null);
		
		setOffsetAndLength(qualifiedName);
		astStack.push(qualifiedName);
	}
	
	
	/**
	 * namespace_alias_definition
     *     ::= 'namespace' 'identifier' '=' dcolon_opt nested_name_specifier_opt namespace_name ';'
     */
	public void consumeNamespaceAliasDefinition() {
		IASTName qualifiedName = subRuleQualifiedName(false);
		
		IASTName alias = createName(stream.getRuleTokens().get(1));
		ICPPASTNamespaceAlias namespaceAlias = nodeFactory.newNamespaceAlias(alias, qualifiedName);
		
		setOffsetAndLength(namespaceAlias);
		astStack.push(namespaceAlias);
	}
	
	
	/**
	 * using_declaration
     *     ::= 'using' typename_opt dcolon_opt nested_name_specifier_opt unqualified_id ';'
	 */
	public void consumeUsingDeclaration() {
		IASTName qualifiedName = subRuleQualifiedName(false);
		boolean hasTypenameKeyword = astStack.pop() == PLACE_HOLDER;
		
		ICPPASTUsingDeclaration usingDeclaration = nodeFactory.newUsingDeclaration(qualifiedName);
		usingDeclaration.setIsTypename(hasTypenameKeyword);
		
		setOffsetAndLength(usingDeclaration);
		astStack.push(usingDeclaration);
	}
	
	
	/**
	 * using_directive
     *     ::= 'using' 'namespace' dcolon_opt nested_name_specifier_opt namespace_name ';'
	 */
	public void consumeUsingDirective() {
		IASTName qualifiedName = subRuleQualifiedName(false);
		
		ICPPASTUsingDirective usingDirective = nodeFactory.newUsingDirective(qualifiedName);
		setOffsetAndLength(usingDirective);
		astStack.push(usingDirective);
	}
	
	
	/**
	 * linkage_specification
     *     ::= 'extern' 'stringlit' '{' <openscope-ast> declaration_seq_opt '}'
     *       | 'extern' 'stringlit' <openscope-ast> declaration
	 */
	public void consumeLinkageSpecification() {
		String name = stream.getRuleTokens().get(1).toString();
		ICPPASTLinkageSpecification linkageSpec = nodeFactory.newLinkageSpecification(name);
		
		for(Object declaration : astStack.closeScope())
			linkageSpec.addDeclaration((IASTDeclaration)declaration);
			
		setOffsetAndLength(linkageSpec);
		astStack.push(linkageSpec);
	}
	
	
	
	/**
	 * original_namespace_definition
     *     ::= 'namespace' identifier_name '{' <openscope-ast> declaration_seq_opt '}'
     *    
     * extension_namespace_definition
     *     ::= 'namespace' original_namespace_name '{' <openscope-ast> declaration_seq_opt '}'
     *
     * unnamed_namespace_definition
     *     ::= 'namespace' '{' <openscope-ast> declaration_seq_opt '}'
	 */
	public void consumeNamespaceDefinition(boolean hasName) {
		List<Object> declarations = astStack.closeScope();
		IASTName namespaceName = hasName ? (IASTName)astStack.pop() : nodeFactory.newName();
		
		ICPPASTNamespaceDefinition definition = nodeFactory.newNamespaceDefinition(namespaceName);
		
		for(Object declaration : declarations)
			definition.addDeclaration((IASTDeclaration)declaration);
		
		setOffsetAndLength(definition);
		astStack.push(definition);
	}
	
	
	/**
	 * template_declaration
     *     ::= export_opt 'template' '<' <openscope-ast> template_parameter_list '>' declaration
	 */
	public void consumeTemplateDeclaration() {
		IASTDeclaration declaration = (IASTDeclaration) astStack.pop();
		
		// For some reason ambiguous declarators cause bugs when they are a part of a template declaration.
		// But it shouldn't be ambiguous anyway, so just throw away the ambiguity node.
		resolveAmbiguousDeclaratorsToFunction(declaration);
		
		ICPPASTTemplateDeclaration templateDeclaration = nodeFactory.newTemplateDeclaration(declaration);
		
		for(Object param : astStack.closeScope())
			templateDeclaration.addTemplateParamter((ICPPASTTemplateParameter)param);

		boolean hasExportKeyword = astStack.pop() == PLACE_HOLDER;
		templateDeclaration.setExported(hasExportKeyword);
		
		setOffsetAndLength(templateDeclaration);
		astStack.push(templateDeclaration);
	}
	
	
	/**
	 * If we know that a declarator must be a function declarator then we can resolve
	 * the ambiguity without resorting to binding resolution.
	 */
	private static void resolveAmbiguousDeclaratorsToFunction(IASTDeclaration declaration) {
		if(declaration instanceof IASTSimpleDeclaration) {
			for(IASTDeclarator declarator : ((IASTSimpleDeclaration)declaration).getDeclarators()) {
				if(declarator instanceof CPPASTAmbiguousDeclarator) {
					IASTAmbiguityParent owner = (IASTAmbiguityParent) declaration;
					CPPASTAmbiguousDeclarator ambiguity = (CPPASTAmbiguousDeclarator)declarator;
					owner.replace(ambiguity, ambiguity.getDeclarators()[0]);
				}
			}
		}
	}
	
	
    /**
     * explicit_instantiation
     *    ::= 'template' declaration
     */
	public void consumeTemplateExplicitInstantiation() {
		IASTDeclaration declaration = (IASTDeclaration) astStack.pop();
		ICPPASTExplicitTemplateInstantiation instantiation = nodeFactory.newExplicitTemplateInstantiation(declaration);
		
		setOffsetAndLength(instantiation);
		astStack.push(instantiation);
	}
	
	
	/**
	 * explicit_specialization
     *     ::= 'template' '<' '>' declaration
     */
	public void consumeTemplateExplicitSpecialization() {
		IASTDeclaration declaration = (IASTDeclaration) astStack.pop();
		ICPPASTTemplateSpecialization specialization = nodeFactory.newTemplateSpecialization(declaration);
		
		setOffsetAndLength(specialization);
		astStack.push(specialization);
	}
	
	
	/**
	 * Sets a token specifier.
	 * Needs to be overrideable for new decl spec keywords.
	 * 
	 * @param token Allows subclasses to override this method and use any
	 * object to determine how to set a specifier.
	 */
	public void setSpecifier(ICPPASTDeclSpecifier node, Object specifier) {
		if(!(specifier instanceof IToken))
			return;
		
		IToken token = (IToken)specifier;
		int kind = baseKind(token);
		switch(kind){
			case TK_typedef:  node.setStorageClass(IASTDeclSpecifier.sc_typedef);    return;
			case TK_extern:   node.setStorageClass(IASTDeclSpecifier.sc_extern);     return;
			case TK_static:   node.setStorageClass(IASTDeclSpecifier.sc_static);     return;
			case TK_auto:     node.setStorageClass(IASTDeclSpecifier.sc_auto);       return;
			case TK_register: node.setStorageClass(IASTDeclSpecifier.sc_register);   return;
			case TK_mutable:  node.setStorageClass(ICPPASTDeclSpecifier.sc_mutable); return;
			
			case TK_inline:   node.setInline(true);   return;
			case TK_const:    node.setConst(true);    return;
			case TK_friend:   node.setFriend(true);   return;
			case TK_virtual:  node.setVirtual(true);  return;
			case TK_volatile: node.setVolatile(true); return;
			case TK_explicit: node.setExplicit(true); return;
		}
		
		if(node instanceof ICPPASTSimpleDeclSpecifier) {
			ICPPASTSimpleDeclSpecifier n = (ICPPASTSimpleDeclSpecifier) node;
			switch(kind) {
				case TK_void:     n.setType(IASTSimpleDeclSpecifier.t_void);       return;
				case TK_char:     n.setType(IASTSimpleDeclSpecifier.t_char);       return;
				case TK_int:      n.setType(IASTSimpleDeclSpecifier.t_int);        return;
				case TK_float:    n.setType(IASTSimpleDeclSpecifier.t_float);      return;
				case TK_double:   n.setType(IASTSimpleDeclSpecifier.t_double);     return;
				case TK_bool:     n.setType(ICPPASTSimpleDeclSpecifier.t_bool);    return;
				case TK_wchar_t:  n.setType(ICPPASTSimpleDeclSpecifier.t_wchar_t); return;
				
				case TK_signed:   n.setSigned(true);   return;
				case TK_unsigned: n.setUnsigned(true); return;
				//if it is a longlong, donot set long, CDT_70_FIX_FROM_50-#8
				case TK_long:     if(!n.isLongLong()) n.setLong(true);     return;
				case TK_short:    n.setShort(true);    return;
			}
		}
	}
	
	
	public void consumeDeclarationSpecifiersSimple() {
		ICPPASTDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifier();
		
		for(Object token : astStack.closeScope())
			setSpecifier(declSpec, token);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}

	
	/**
	 * TODO: maybe move this into the superclass
	 */
	public void consumeDeclarationSpecifiersComposite() {
		List<Object> topScope = astStack.closeScope();
		
		// There's already a composite or elaborated or enum type specifier somewhere on the stack, find it.
		ICPPASTDeclSpecifier declSpec = findFirstAndRemove(topScope, ICPPASTDeclSpecifier.class);
		
		// now apply the rest of the specifiers
		for(Object token : topScope)
			setSpecifier(declSpec, token);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	
	
//	/**
//	 * declaration_specifiers ::=  <openscope> type_name_declaration_specifiers
//	 */
	public void consumeDeclarationSpecifiersTypeName() {
		List<Object> topScope = astStack.closeScope();
		// There's a name somewhere on the stack, find it		
		IASTName typeName = findFirstAndRemove(topScope, IASTName.class);
		
		// TODO what does the second argument mean?
		ICPPASTNamedTypeSpecifier declSpec = nodeFactory.newTypedefNameSpecifier(typeName);
		
		// now apply the rest of the specifiers
		for(Object token : topScope) {
			setSpecifier(declSpec, token);
		}
		
		// the only way there could be a typename token
		for(IToken token : stream.getRuleTokens()) {
			if(baseKind(token) == TK_typename) {
				declSpec.setIsTypename(true);
				break;
			}
		}

		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * elaborated_type_specifier
     *     ::= class_keyword dcolon_opt nested_name_specifier_opt identifier_name
     *       | class_keyword dcolon_opt nested_name_specifier_opt template_opt template_id_name
     *       | 'enum' dcolon_opt nested_name_specifier_opt identifier_name      
	 */
	public void consumeTypeSpecifierElaborated(boolean hasOptionalTemplateKeyword) {
		IASTName name = subRuleQualifiedName(hasOptionalTemplateKeyword);
		int kind = getElaboratedTypeSpecifier(stream.getLeftIToken());
		
		IASTElaboratedTypeSpecifier typeSpecifier = nodeFactory.newElaboratedTypeSpecifier(kind, name);
		
		setOffsetAndLength(typeSpecifier);
		astStack.push(typeSpecifier);
	}
	
	
	private int getElaboratedTypeSpecifier(IToken token) {
		int kind = baseKind(token);
		switch(kind) {
			default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
			case TK_struct: return IASTElaboratedTypeSpecifier.k_struct;
			case TK_union:  return IASTElaboratedTypeSpecifier.k_union;
			case TK_enum:   return IASTElaboratedTypeSpecifier.k_enum;
			case TK_class:  return ICPPASTElaboratedTypeSpecifier.k_class;   
		}
	}
	
	
	
	/**
	 * simple_declaration
     *     ::= declaration_specifiers_opt <openscope-ast> init_declarator_list_opt ';'
	 */
	public void consumeDeclarationSimple(boolean hasDeclaratorList) {
		List<Object> declarators = hasDeclaratorList ? astStack.closeScope() : Collections.emptyList();
		ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) astStack.pop(); // may be null
		
		List<IToken> ruleTokens = stream.getRuleTokens();
		IToken nameToken = null;
		
		
		// do not generate nodes for extra EOC tokens
		if(matchTokens(ruleTokens, tokenMap, TK_EndOfCompletion)) {
			return;
		}
		
		// In the case that a single completion token is parsed then it needs
		// to be interpreted as a named type specifier for content assist to work.
		else if(matchTokens(ruleTokens, tokenMap, TK_Completion, TK_EndOfCompletion)) {
			IASTName name = createName(stream.getLeftIToken());
			declSpec = nodeFactory.newTypedefNameSpecifier(name);
			ParserUtil.setOffsetAndLength(declSpec, offset(name), length(name));
			declarators = Collections.emptyList(); // throw away the bogus declarator
		}
		
		// can happen if implicit int is used
		else if(declSpec == null) { 
			declSpec = nodeFactory.newSimpleDeclSpecifier();
			ParserUtil.setOffsetAndLength(declSpec, stream.getLeftIToken().getStartOffset(), 0);
		}
		
		
		else if(declarators.size() == 1 && disambiguateToConstructor(declSpec, (IASTDeclarator)declarators.get(0))) { // puts results of disambiguation onto stack
			declSpec = (ICPPASTDeclSpecifier) astStack.pop(); 
			declarators = Arrays.asList(astStack.pop());
		}
		
		// bug 80171, check for situation similar to: static var;
		// this will get parsed wrong, the following is a hack to rebuild the AST as it should have been parsed
		else if(declarators.isEmpty() && 
		   declSpec instanceof ICPPASTNamedTypeSpecifier &&
		   ruleTokens.size() >= 2 &&
		   baseKind(nameToken = ruleTokens.get(ruleTokens.size() - 2)) == TK_identifier) {
			
			declSpec = nodeFactory.newSimpleDeclSpecifier();
			for(IToken t : ruleTokens.subList(0, ruleTokens.size()-1))
				setSpecifier(declSpec, t);
			
			int offset = offset(stream.getLeftIToken());
			int length = endOffset(ruleTokens.get(ruleTokens.size()-2)) - offset;
			ParserUtil.setOffsetAndLength(declSpec, offset, length);
			
			IASTName name = createName(nameToken);
			IASTDeclarator declarator = nodeFactory.newDeclarator(name);
			ParserUtil.setOffsetAndLength(declarator, nameToken);
			declarators.add(declarator);
		}

		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpec);
		setOffsetAndLength(declaration);
		for(Object declarator : declarators)
			declaration.addDeclarator((IASTDeclarator)declarator);
				
		// simple ambiguity resolutions
//		if(declSpecifier.isFriend())
//			resolveAmbiguousDeclaratorsToFunction(declaration);
//		
//		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
//			IASTSimpleDeclSpecifier simple = (IASTSimpleDeclSpecifier) declSpecifier;
//			if(simple.getType() == IASTSimpleDeclSpecifier.t_void && declaration.getDeclarators()[0].getPointerOperators().length == 0)
//				resolveAmbiguousDeclaratorsToFunction(declaration);
//			
//		}
		
		astStack.push(declaration);
	}
	
	

	private boolean disambiguateToConstructor(IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		if(!(declSpec instanceof IASTNamedTypeSpecifier))
			return false;
			
		IASTNamedTypeSpecifier namedTypeSpecifier = (IASTNamedTypeSpecifier) declSpec;
		IASTName name = namedTypeSpecifier.getName();
		IASTDeclarator nested = declarator.getNestedDeclarator();
		
		ICPPASTSimpleDeclSpecifier simpleDeclSpec = nodeFactory.newSimpleDeclSpecifier(); // empty
		ParserUtil.setOffsetAndLength(simpleDeclSpec, stream.getLeftIToken().getStartOffset(), 0);
		
		if(!classNames.isEmpty() && nested != null && ParserUtil.isSameName(name, classNames.getLast())) {

			IASTName paramTypeName = nested.getName();  // reuse the parameter name node
			IASTNamedTypeSpecifier paramName = nodeFactory.newTypedefNameSpecifier(paramTypeName);
			ParserUtil.setOffsetAndLength(paramName, paramTypeName);
			
			IASTDeclarator paramDeclarator = nodeFactory.newDeclarator(nodeFactory.newName());
			ParserUtil.setOffsetAndLength(paramDeclarator, offset(paramName) + length(paramName), 0);
			
			ICPPASTParameterDeclaration parameter = nodeFactory.newParameterDeclaration(paramName, paramDeclarator);
			ParserUtil.setOffsetAndLength(parameter, paramName);
			
			ICPPASTFunctionDeclarator constructorDeclarator = nodeFactory.newFunctionDeclarator(name); // reuse the name node
			constructorDeclarator.addParameterDeclaration(parameter);
			ParserUtil.setOffsetAndLength(constructorDeclarator, offset(simpleDeclSpec), endOffset(paramDeclarator) - offset(simpleDeclSpec) + 1);
			
			astStack.push(constructorDeclarator);
			astStack.push(simpleDeclSpec);
			return true;
		}
		
		if(declarator instanceof IASTFunctionDeclarator && declarator.getName() instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qualifiedName = (ICPPASTQualifiedName) declarator.getName();
			//IASTName lastName = qualifiedName.getLastName();
			
			if(qualifiedName.isFullyQualified()) {
				
				ICPPASTQualifiedName newQualifiedName = nodeFactory.newQualifiedName();
				newQualifiedName.addName(name);
				for(IASTName n : qualifiedName.getNames())
					newQualifiedName.addName(n);
				
				ParserUtil.setOffsetAndLength(newQualifiedName, offset(name), endOffset(qualifiedName.getLastName()) - offset(name));
				
				
				declarator.setName(newQualifiedName);
				ParserUtil.setOffsetAndLength(declarator, offset(name), length(declarator) + offset(declarator) - offset(name));
				
				astStack.push(declarator);
				astStack.push(simpleDeclSpec);
				return true;
			}
		}
		
		return false;
	}
	

		
	public void consumeInitDeclaratorComplete() {
		// Don't do disambiguation when parsing for content assist,
		// trust me this makes things work out a lot better.
		if(completionNode != null)
			return;
		
		IASTDeclarator declarator = (IASTDeclarator) astStack.peek();
		if(!(declarator instanceof IASTFunctionDeclarator))
			return;
		
		ISecondaryParser<IASTDeclarator> secondaryParser = parserFactory.getNoFunctionDeclaratorParser(stream, properties); 
		IASTDeclarator notFunctionDeclarator = runSecondaryParser(secondaryParser);
	
		if(notFunctionDeclarator == null)
			return;
		
		astStack.pop();

		IASTNode ambiguityNode = new CPPASTAmbiguousDeclarator(declarator, notFunctionDeclarator);

		setOffsetAndLength(ambiguityNode);
		astStack.push(ambiguityNode); 
	}
	
	
	
	/**
	 * visibility_label
     *     ::= access_specifier_keyword ':'
	 */
	public void consumeVisibilityLabel() {
		IToken specifier = (IToken)astStack.pop();
		int visibility = getAccessSpecifier(specifier);
		ICPPASTVisibilityLabel visibilityLabel = nodeFactory.newVisibilityLabel(visibility);
		setOffsetAndLength(visibilityLabel);
		astStack.push(visibilityLabel);
	}
	
	
	private int getAccessSpecifier(IToken token) {
		int kind = baseKind(token);
		switch(kind) {
			default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
			case TK_private:   return ICPPASTVisibilityLabel.v_private;
			case TK_public:    return ICPPASTVisibilityLabel.v_public;    
			case TK_protected: return ICPPASTVisibilityLabel.v_protected;
		}
	}
	
	
	/**
	 * base_specifier
     *     ::= dcolon_opt nested_name_specifier_opt class_name
     *       | 'virtual' access_specifier_keyword_opt dcolon_opt nested_name_specifier_opt class_name
     *       | access_specifier_keyword 'virtual' dcolon_opt nested_name_specifier_opt class_name
     *       | access_specifier_keyword dcolon_opt nested_name_specifier_opt class_name
	 */
	public void consumeBaseSpecifier(boolean hasAccessSpecifier, boolean isVirtual) {
		IASTName name = subRuleQualifiedName(false);
		
		int visibility = 0; // this is the default value that the DOM parser uses
		if(hasAccessSpecifier) {
			IToken accessSpecifierToken = (IToken) astStack.pop();
			if(accessSpecifierToken != null)
				visibility = getAccessSpecifier(accessSpecifierToken);
		}
		
		ICPPASTBaseSpecifier baseSpecifier = nodeFactory.newBaseSpecifier(name, visibility, isVirtual);
		setOffsetAndLength(baseSpecifier);
		astStack.push(baseSpecifier);
	}
		
	
	/**
	 * class_specifier
     *     ::= class_head '{' <openscope-ast> member_declaration_list_opt '}'
     */
	public void consumeClassSpecifier() {
		List<Object> declarations = astStack.closeScope();
		
		// the class specifier is created by the rule for class_head
		IASTCompositeTypeSpecifier classSpecifier = (IASTCompositeTypeSpecifier) astStack.peek();
		
		for(Object declaration : declarations)
			classSpecifier.addMemberDeclaration((IASTDeclaration)declaration);
		
		setOffsetAndLength(classSpecifier);
		
		classNames.removeLast(); // pop the stack of class names
	}
	
	
	/**
     * class_head
     *     ::= class_keyword identifier_name_opt <openscope-ast> base_clause_opt
     *       | class_keyword template_id <openscope-ast> base_clause_opt
     *       | class_keyword nested_name_specifier identifier_name <openscope-ast> base_clause_opt
     *       | class_keyword nested_name_specifier template_id <openscope-ast> base_clause_opt
	 */
	@SuppressWarnings("unchecked")
	public void consumeClassHead(boolean hasNestedNameSpecifier) {
		int key = getCompositeTypeSpecifier(stream.getLeftIToken());
		List<Object> baseSpecifiers = astStack.closeScope();
		// may be null, but if it is then hasNestedNameSpecifier == false
		IASTName className = (IASTName) astStack.pop();
		
		if(hasNestedNameSpecifier) {
			LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
			nestedNames.addFirst(className);
			int startOffset = offset(nestedNames.getLast());
			int endOffset = endOffset(className);
			className = createQualifiedName(nestedNames, startOffset, endOffset, false);
		}

		if(className == null)
			className = nodeFactory.newName();
		
		ICPPASTCompositeTypeSpecifier classSpecifier = nodeFactory.newCompositeTypeSpecifier(key, className);
		
		for(Object base : baseSpecifiers)
			classSpecifier.addBaseSpecifier((ICPPASTBaseSpecifier)base);
		
		// the offset and length are set in consumeClassSpecifier()
		astStack.push(classSpecifier);
		classNames.add(className); // push
	}
	
	
	private int getCompositeTypeSpecifier(IToken token) {
		final int kind = baseKind(token);
		switch(kind) {
			default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
			case TK_struct: return IASTCompositeTypeSpecifier.k_struct;
			case TK_union:  return IASTCompositeTypeSpecifier.k_union;
			case TK_class:  return ICPPASTCompositeTypeSpecifier.k_class;   
		}
	}
	
	
	/**
	 * ptr_operator
     *     ::= '*' <openscope-ast> cv_qualifier_seq_opt
     */
    public void consumePointer() {
    	IASTPointer pointer = nodeFactory.newPointer();
    	List<Object> tokens = astStack.closeScope();
    	addCVQualifiersToPointer(pointer, tokens);
		setOffsetAndLength(pointer);
		astStack.push(pointer);
    }
    
    
    protected void addCVQualifiersToPointer(IASTPointer pointer, List<Object> tokens) {
    	for(Object t : tokens) {
    		switch(baseKind((IToken) t)) {
				case TK_const:    pointer.setConst(true);    break;
				case TK_volatile: pointer.setVolatile(true); break;
			}
		}
    }
    
    /**
	 * ptr_operator
     *     ::= '&'
     */ 
    public void consumeReferenceOperator() {
        ICPPASTReferenceOperator referenceOperator = nodeFactory.newReferenceOperator();
        setOffsetAndLength(referenceOperator);
		astStack.push(referenceOperator);
    }
    
    
    /**
	 * ptr_operator
     *     ::= dcolon_opt nested_name_specifier '*' <openscope-ast> cv_qualifier_seq_opt
     */
     @SuppressWarnings("unchecked")
	public void consumePointerToMember() {
    	 List<Object> qualifiers = astStack.closeScope();
    	 LinkedList<IASTName> nestedNames = (LinkedList<IASTName>) astStack.pop();
    	 IToken dColon = (IToken) astStack.pop();
    	 
    	 int startOffset = dColon == null ? offset(nestedNames.getLast()) : offset(dColon);
    	 int endOffset   = endOffset(nestedNames.getFirst()); // temporary
    	 
    	 // find the last double colon by searching for it
    	 for(IToken t : reverseIterable(stream.getRuleTokens())) {
    		 if(baseKind(t) == TK_ColonColon) {
    			 endOffset = endOffset(t);
    			 break;
    		 }
    	 }
    	 
    	 IASTName name = createQualifiedName(nestedNames, startOffset, endOffset, dColon != null, true);
    	 
     	 ICPPASTPointerToMember pointer = nodeFactory.newPointerToMember(name);
     	 addCVQualifiersToPointer(pointer, qualifiers);
     	 setOffsetAndLength(pointer);
		 astStack.push(pointer);
     }

     
     
     /**
      * initializer
      *     ::= '(' expression_list ')'
      */
     public void consumeInitializerConstructor() {
    	 //CDT_70_FIX_FROM_50-#5
     	 Object o = astStack.pop();
    	 IASTInitializerClause[] initClauseList =null;
    	 if(o instanceof IASTExpressionList){
    		 initClauseList = ((IASTExpressionList) o).getExpressions();
    	 }else if(o instanceof IASTInitializerClause){
    		 initClauseList = new IASTInitializerClause[]{(IASTInitializerClause)o};
    	 }
    	 
    	 ICPPASTConstructorInitializer initializer = nodeFactory.newConstructorInitializer(initClauseList);
    	 setOffsetAndLength(initializer);
		 astStack.push(initializer);
     }
     
     
    /**
 	 * function_direct_declarator
     *     ::= basic_direct_declarator '(' <openscope-ast> parameter_declaration_clause ')' 
     *         <openscope-ast> cv_qualifier_seq_opt <openscope-ast> exception_specification_opt
 	 */
 	public void consumeDirectDeclaratorFunctionDeclarator(boolean hasDeclarator) {
 		IASTName name = nodeFactory.newName();
 		ICPPASTFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(name);
 		
 		List<Object> typeIds = astStack.closeScope();
 		if(typeIds.size() == 1 && typeIds.get(0) == PLACE_HOLDER) { // fix for bug 86943
 			declarator.setEmptyExceptionSpecification(); 
 		}
 		else {
	 		for(Object typeId : typeIds) {
	 			declarator.addExceptionSpecificationTypeId((IASTTypeId) typeId);
	 		}
 		}
 		
 		for(Object token : astStack.closeScope()) {
 			int kind = baseKind((IToken)token);
 			switch(kind) {
 				default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
 				case TK_const:    declarator.setConst(true); break;
 				case TK_volatile: declarator.setVolatile(true); break;
 			}
 		}
 		
 		boolean isVarArgs = astStack.pop() == PLACE_HOLDER;
 		declarator.setVarArgs(isVarArgs);
 			
 		for(Object o : astStack.closeScope()) {
 			declarator.addParameterDeclaration((IASTParameterDeclaration)o);
 		}
 		
 		if(hasDeclarator) {
 			int endOffset = endOffset(stream.getRightIToken());
 			addFunctionModifier(declarator, endOffset);
 		}
 		else {
 			setOffsetAndLength(declarator);
			astStack.push(declarator);
 		}
 	}
 
 	
 	/**
 	 * Consume an empty bracketed abstract declarator.
 	 */
 	public void consumeAbstractDeclaratorEmpty() {
 		IASTName name = nodeFactory.newName();
 		ParserUtil.setOffsetAndLength(name, offset(stream.getLeftIToken())+1, 0);
 		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
 		setOffsetAndLength(declarator);
 		astStack.push(declarator);
 	}
 	
 	
 	/**
 	 * mem_initializer
     *     ::= mem_initializer_id '(' expression_list_opt ')'
 	 */
 	public void consumeConstructorChainInitializer() {
 		IASTExpression expr = (IASTExpression) astStack.pop();
 		IASTName name = (IASTName) astStack.pop();
 		ICPPASTConstructorChainInitializer initializer = nodeFactory.newConstructorChainInitializer(name, expr);
 		setOffsetAndLength(initializer);
		astStack.push(initializer);
 	}
 	
 	
 	
 	/**
 	 * function_definition
     *     ::= declaration_specifiers_opt function_direct_declarator 
     *         <openscope-ast> ctor_initializer_list_opt function_body
     *         
     *       | declaration_specifiers_opt function_direct_declarator 
     *         'try' <openscope-ast> ctor_initializer_list_opt function_body <openscope-ast> handler_seq
     *         
 	 */
 	public void consumeFunctionDefinition(boolean isTryBlockDeclarator) {
 		List<Object> handlers = isTryBlockDeclarator ? astStack.closeScope() : Collections.emptyList();
 		IASTCompoundStatement body = (IASTCompoundStatement) astStack.pop();
 		List<Object> initializers = astStack.closeScope();
 		Object o = astStack.pop();
 		IASTFunctionDeclarator declarator = (IASTFunctionDeclarator) o;
 		Object o2 = astStack.pop();
 		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) o2; // may be null
 		
 		if(declSpec == null) { // can happen if implicit int is used
 			declSpec = nodeFactory.newSimpleDeclSpecifier();
 			ParserUtil.setOffsetAndLength(declSpec, stream.getLeftIToken().getStartOffset(), 0);
		}
 		else if(disambiguateToConstructor(declSpec, declarator)) {
 			declSpec = (IASTDeclSpecifier) astStack.pop(); 
			declarator = (IASTFunctionDeclarator) astStack.pop();
 		}
 		
 		ICPPASTFunctionDefinition definition;
 		if (isTryBlockDeclarator) {
 			ICPPASTFunctionWithTryBlock tryblock= nodeFactory.newFunctionTryBlock(declSpec, declarator, body);
 			for(Object handler : handlers)
 				tryblock.addCatchHandler((ICPPASTCatchHandler)handler);
 			definition = tryblock;
 		} else {
 			definition = nodeFactory.newFunctionDefinition(declSpec, declarator, body);
 		}
 		
 		
 		if(initializers != null && !initializers.isEmpty()) {
 			for(Object initializer : initializers)
 	 			definition.addMemberInitializer((ICPPASTConstructorChainInitializer)initializer);
 		}
 		
 		setOffsetAndLength(definition);
		astStack.push(definition);
 	}
 	
 	
 	/**
 	 * member_declaration
 	 *     ::= dcolon_opt nested_name_specifier template_opt unqualified_id_name ';'
 	 */
 	public void consumeMemberDeclarationQualifiedId() {
 		IASTName qualifiedId = subRuleQualifiedName(true);
 		
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=92793
 		ICPPASTUsingDeclaration declaration = nodeFactory.newUsingDeclaration(qualifiedId);
 		setOffsetAndLength(declaration);
 		
 		astStack.push(declaration);
 	}
 	
 	
 	/**
 	 * member_declarator
     *     ::= declarator constant_initializer
 	 */
 	
    public void consumeMemberDeclaratorWithInitializer() {
    	
    	//CDT_70_FIX_FROM_50-#2
    	//IASTInitializerExpression initializer = (IASTInitializerExpression) astStack.pop();
    	IASTEqualsInitializer initializer = (IASTEqualsInitializer) astStack.pop();
    	IASTDeclarator declarator = (IASTDeclarator) astStack.peek();
    	setOffsetAndLength(declarator);
    	
    	if(declarator instanceof ICPPASTFunctionDeclarator) {
    		IASTExpression expr = (IASTExpression)initializer.getInitializerClause();
    		if(expr instanceof IASTLiteralExpression && "0".equals(expr.toString())) { //$NON-NLS-1$
    			((ICPPASTFunctionDeclarator)declarator).setPureVirtual(true);
    			return;
    		}
    	}
    	
    	declarator.setInitializer(initializer);
    }

    
    /**
     * type_parameter
     *     ::= 'class' identifier_name_opt -- simple type template parameter     
     *       | 'class' identifier_name_opt '=' type_id
     *       | 'typename' identifier_name_opt
     *       | 'typename' identifier_name_opt '=' type_id
     */
    public void consumeSimpleTypeTemplateParameter(boolean hasTypeId) {
    	IASTTypeId typeId = hasTypeId ? (IASTTypeId)astStack.pop() : null;
    	
    	IASTName name = (IASTName)astStack.pop();
    	if(name == null)
    		name = nodeFactory.newName();
    	
    	int type = getTemplateParameterType(stream.getLeftIToken()); 
    	
    	ICPPASTSimpleTypeTemplateParameter templateParameter = nodeFactory.newSimpleTypeTemplateParameter(type, name, typeId);
    	
    	setOffsetAndLength(templateParameter);
		astStack.push(templateParameter);
    }
    
    
    private int getTemplateParameterType(IToken token) {
    	int kind = baseKind(token);
    	switch(kind) {
    		default: assert false : "wrong token kind: " + kind; //$NON-NLS-1$
    		case TK_class:    return ICPPASTSimpleTypeTemplateParameter.st_class;
    		case TK_typename: return ICPPASTSimpleTypeTemplateParameter.st_typename;
    	}
    }
    
    
    /**
     * Simple type template parameters using the 'class' keyword are being parsed
     * wrong due to an ambiguity between type_parameter and parameter_declaration.
     * 
     * eg) template <class T>
     * 
     * The 'class T' part is being parsed as an elaborated type specifier instead
     * of a simple type template parameter.
     * 
     * This method detects the incorrect parse, throws away the incorrect AST fragment,
     * and replaces it with the correct AST fragment.
     * 
     * Yes its a hack.
     */
    public void consumeTemplateParamterDeclaration() {
    	ISecondaryParser<ICPPASTTemplateParameter> typeParameterParser = parserFactory.getTemplateTypeParameterParser(stream, properties);
    	IASTNode alternate = runSecondaryParser(typeParameterParser);
    	
		if(alternate == null)
			return;
		
		astStack.pop(); // throw away the incorrect AST
		astStack.push(alternate);  // replace it with the correct AST
    }

    
    
    /**
     * type_parameter
     *     ::= 'template' '<' <openscope-ast> template_parameter_list '>' 'class' identifier_name_opt
     *       | 'template' '<' <openscope-ast> template_parameter_list '>' 'class' identifier_name_opt '=' id_expression
     * @param hasIdExpr
     */
    public void consumeTemplatedTypeTemplateParameter(boolean hasIdExpr) {
    	IASTExpression idExpression = hasIdExpr ? (IASTExpression)astStack.pop() : null;
    	IASTName name = (IASTName) astStack.pop();
    	
    	ICPPASTTemplatedTypeTemplateParameter templateParameter = nodeFactory.newTemplatedTypeTemplateParameter(name, idExpression);
    	
    	for(Object param : astStack.closeScope())
    		templateParameter.addTemplateParamter((ICPPASTTemplateParameter)param);
    	
    	setOffsetAndLength(templateParameter);
		astStack.push(templateParameter);
    }


	@Override
	protected IASTAmbiguousExpression createAmbiguousExpression(IASTExpression... expressions) {
		return new CPPASTAmbiguousExpression(expressions);
	}


	@Override
	protected IASTAmbiguousStatement createAmbiguousStatement(IASTStatement... statements) {
		return new CPPASTAmbiguousStatement(statements);
	}
}