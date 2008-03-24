/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.lrparser.action.c99;

import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_Completion;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK__Bool;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK__Complex;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_auto;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_char;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_const;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_double;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_extern;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_float;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_identifier;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_inline;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_int;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_long;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_register;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_restrict;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_short;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_signed;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_static;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_typedef;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_unsigned;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_void;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.TK_volatile;

import java.util.Collections;
import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignatedInitializer;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.dom.lrparser.action.BuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.core.parser.util.CollectionUtils;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99ExpressionStatementParser;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99NoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99SizeofExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPParsersym;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Semantic actions called by the C99 parser to build an AST.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class C99BuildASTParserAction extends BuildASTParserAction  {

	private ITokenMap tokenMap = null;
	
	/** Used to create the AST node objects */
	protected final IC99ASTNodeFactory nodeFactory;
	
	/**
	 * @param parser
	 * @param orderedTerminalSymbols When an instance of this class is created for a parser
	 * that parsers token kinds will be mapped back to the base C99 parser's token kinds.
	 */
	public C99BuildASTParserAction(IC99ASTNodeFactory nodeFactory, IParserActionTokenProvider parser, IASTTranslationUnit tu) {
		super(nodeFactory, parser, tu);
		this.nodeFactory = nodeFactory;
	}
	
	
	@Override protected boolean isCompletionToken(IToken token) {
		return asC99Kind(token) == TK_Completion;
	}
	
	
	public void setTokenMap(String[] orderedTerminalSymbols) {
		this.tokenMap = new TokenMap(C99Parsersym.orderedTerminalSymbols, orderedTerminalSymbols);
	}
	
	
	int asC99Kind(IToken token) {
		return asC99Kind(token.getKind());
	}
	
	private int asC99Kind(int tokenKind) {
		return tokenMap == null ? tokenKind : tokenMap.mapKind(tokenKind);
	}
	
	
	@Override
	protected IParser getExpressionStatementParser() {
		DebugUtil.printMethodTrace();
		return new C99ExpressionStatementParser(parser.getOrderedTerminalSymbols()); 
	}

	@Override
	protected IParser getNoCastExpressionParser() {
		DebugUtil.printMethodTrace();
		return new C99NoCastExpressionParser(parser.getOrderedTerminalSymbols());
	}
	
	@Override
	protected IParser getSizeofExpressionParser() {
		DebugUtil.printMethodTrace();
		return new C99SizeofExpressionParser(parser.getOrderedTerminalSymbols());
	}
	
	
	/********************************************************************
	 * Start of semantic actions.
	 ********************************************************************/

	
	


	/**
	 * postfix_expression ::= postfix_expression '.' ident
	 * postfix_expression ::= postfix_expression '->' ident
	 */
	public void consumeExpressionFieldReference(/*IBinding field, */ boolean isPointerDereference) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = createName(parser.getRightIToken());
		//name.setBinding(field);
		IASTExpression owner = (IASTExpression) astStack.pop();
		IASTFieldReference expr = nodeFactory.newFieldReference(name, owner, isPointerDereference);
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list '}'
	 * postfix_expression ::= '(' type_name ')' '{' <openscope> initializer_list ',' '}'            
	 */
	public void consumeExpressionTypeIdInitializer() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		consumeInitializerList(); // closes the scope
		IASTInitializerList list = (IASTInitializerList) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		ICASTTypeIdInitializerExpression expr = nodeFactory.newCTypeIdInitializerExpression(typeId, list);
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
//	/**
//	 * Lots of rules, no need to list them.
//	 * @param operator From IASTUnaryExpression
//	 */
//	public void consumeExpressionSizeofTypeId() {
//		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
//		
//		IASTTypeId typeId = (IASTTypeId) astStack.pop();
//		IASTTypeIdExpression expr = nodeFactory.newTypeIdExpression(IASTTypeIdExpression.op_sizeof, typeId);
//		setOffsetAndLength(expr);
//		
//		// try parsing as an expression to resolve ambiguities
//		C99SizeofExpressionParser secondaryParser = new C99SizeofExpressionParser(C99Parsersym.orderedTerminalSymbols); 
//		IASTNode alternateExpr = runSecondaryParser(secondaryParser);
//		
//		if(alternateExpr == null || alternateExpr instanceof IASTProblemExpression)
//			astStack.push(expr);
//		else
//			astStack.push(nodeFactory.newAmbiguousExpression(expr, (IASTExpression)alternateExpr));
//		
//		if(TRACE_AST_STACK) System.out.println(astStack);
//	}
	
	
	/**
	 * Sets a token specifier.
	 * Needs to be overrideable for new decl spec keywords.
	 * 
	 * @param token Allows subclasses to override this method and use any
	 * object to determine how to set a specifier.
	 */
	protected void setSpecifier(ICASTDeclSpecifier node, IToken token) {
		int kind = asC99Kind(token);
		switch(kind){
			case TK_typedef:  node.setStorageClass(IASTDeclSpecifier.sc_typedef);  return;
			case TK_extern:   node.setStorageClass(IASTDeclSpecifier.sc_extern);   return;
			case TK_static:   node.setStorageClass(IASTDeclSpecifier.sc_static);   return;
			case TK_auto:     node.setStorageClass(IASTDeclSpecifier.sc_auto);     return;
			case TK_register: node.setStorageClass(IASTDeclSpecifier.sc_register); return;
			case TK_inline:   node.setInline(true);   return;
			case TK_const:    node.setConst(true);    return;
			case TK_restrict: node.setRestrict(true); return;
			case TK_volatile: node.setVolatile(true); return;
		}
		
		if(node instanceof ICASTSimpleDeclSpecifier) {
			ICASTSimpleDeclSpecifier n = (ICASTSimpleDeclSpecifier) node;
			switch(kind) {
				case TK_void:     n.setType(IASTSimpleDeclSpecifier.t_void);   break;
				case TK_char:     n.setType(IASTSimpleDeclSpecifier.t_char);   break;
				case TK__Bool:    n.setType(ICASTSimpleDeclSpecifier.t_Bool);  break;
				case TK_int:      n.setType(IASTSimpleDeclSpecifier.t_int);    break;
				case TK_float:    n.setType(IASTSimpleDeclSpecifier.t_float);  break;
				case TK_double:   n.setType(IASTSimpleDeclSpecifier.t_double); break;
				case TK_signed:   n.setSigned(true);   break;
				case TK_unsigned: n.setUnsigned(true); break;
				case TK_short:    n.setShort(true);    break;
				case TK__Complex: n.setComplex(true);  break;
				case TK_long:
					boolean isLong = n.isLong();
					n.setLongLong(isLong);
					n.setLong(!isLong);
					break;
			}
		}
	}
	
	
	
	/**
	 * type_qualifier ::= const | restrict | volatile
	 */
	private void collectArrayModifierTypeQualifiers(ICASTArrayModifier arrayModifier) {
		for(Object o : astStack.closeScope()) {
			switch(asC99Kind((IToken)o)) {
				case TK_const:    arrayModifier.setConst(true);    break;
				case TK_restrict: arrayModifier.setRestrict(true); break;
				case TK_volatile: arrayModifier.setVolatile(true); break;
			}
		}
	}
	
	
	/**
	 *  array_modifier 
     *      ::= '[' <openscope> type_qualifier_list ']'
     *        | '[' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' 'static' assignment_expression ']'
     *        | '[' 'static' <openscope> type_qualifier_list assignment_expression ']'
     *        | '[' <openscope> type_qualifier_list 'static' assignment_expression ']'
     *        | '[' '*' ']'
     *        | '[' <openscope> type_qualifier_list '*' ']'
     *        
     * The main reason to separate array_modifier into its own rule is to
     * make calculating the offset and length much easier.
	 */
	public void consumeDirectDeclaratorModifiedArrayModifier(boolean isStatic, 
			 boolean isVarSized, boolean hasTypeQualifierList, boolean hasAssignmentExpr) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		assert isStatic || isVarSized || hasTypeQualifierList;
		
		ICASTArrayModifier arrayModifier = nodeFactory.newModifiedArrayModifier();
		
		// consume all the stuff between the square brackets into an array modifier
		arrayModifier.setStatic(isStatic);
		arrayModifier.setVariableSized(isVarSized);
		
		if(hasAssignmentExpr)
			arrayModifier.setConstantExpression((IASTExpression)astStack.pop());
		
		if(hasTypeQualifierList)
			collectArrayModifierTypeQualifiers(arrayModifier);

		setOffsetAndLength(arrayModifier);
		astStack.push(arrayModifier);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}


	
	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> identifier_list ')'
	 */
	public void consumeDirectDeclaratorFunctionDeclaratorKnR() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		ICASTKnRFunctionDeclarator declarator = nodeFactory.newCKnRFunctionDeclarator();
		IASTName[] names = astStack.topScope().toArray(new IASTName[0]);
		declarator.setParameterNames(names);
		astStack.closeScope();
		int endOffset = endOffset(parser.getRightIToken());
		addFunctionModifier(declarator, endOffset);
	}
	
	
	/**
	 * identifier_list
     *     ::= 'identifier'
     *       | identifier_list ',' 'identifier'
	 */
	public void consumeIdentifierKnR() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = createName(parser.getRightIToken());
		astStack.push(name);
	}
	


	
	/**
	 * pointer ::= '*'
     *           | pointer '*' 
     */ 
	public void consumePointer() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTPointer pointer = nodeFactory.newCPointer();
		IToken star = parser.getRightIToken();
		setOffsetAndLength(pointer, star);
		astStack.push(pointer);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * pointer ::= '*' <openscope> type_qualifier_list
     *           | pointer '*' <openscope> type_qualifier_list
	 */
	public void consumePointerTypeQualifierList() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		ICASTPointer pointer = nodeFactory.newCPointer();

		for(Object o : astStack.closeScope()) {
			IToken token = (IToken)o;			
			switch(asC99Kind(token)) {
				default: assert false;
				case TK_const:    pointer.setConst(true);    break;
				case TK_volatile: pointer.setVolatile(true); break;
				case TK_restrict: pointer.setRestrict(true); break;
			}
		}

		setOffsetAndLength(pointer);
		astStack.push(pointer);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * direct_abstract_declarator  
	 *     ::= '(' ')'
     *       | direct_abstract_declarator '(' ')'
     *       | '(' <openscope> parameter_type_list ')'
     *       | direct_abstract_declarator '(' <openscope> parameter_type_list ')'
	 */
	public void consumeDirectDeclaratorFunctionDeclarator(boolean hasDeclarator, boolean hasParameters) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = nodeFactory.newName();
		IASTStandardFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(name);
		
		if(hasParameters) {
			boolean isVarArgs = astStack.pop() == PLACE_HOLDER;
			declarator.setVarArgs(isVarArgs);
			
			for(Object param : astStack.closeScope())
				declarator.addParameterDeclaration((IASTParameterDeclaration)param);
		}
		
		if(hasDeclarator) {
			addFunctionModifier(declarator, endOffset(parser.getRightIToken()));
		}
		else {
			setOffsetAndLength(declarator);
			astStack.push(declarator);
		}
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	
	/**
	 * designated_initializer ::= <openscope> designation initializer
	 */
	public void consumeInitializerDesignated() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTInitializer initializer = (IASTInitializer)astStack.pop();
		ICASTDesignatedInitializer result = nodeFactory.newCDesignatedInitializer(initializer);
		
		for(Object o : astStack.closeScope()) 
			result.addDesignator((ICASTDesignator)o);
		
		setOffsetAndLength(result);
		astStack.push(result);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * designator ::= '[' constant_expression ']'
	 */
	public void consumeDesignatorArray() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr = (IASTExpression) astStack.pop();
		ICASTArrayDesignator designator = nodeFactory.newCArrayDesignator(expr);
		setOffsetAndLength(designator);
		astStack.push(designator);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 *  designator ::= '.' 'identifier'
	 */
	public void consumeDesignatorField(/*IBinding binding*/) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = createName( parser.getRightIToken() );
		//name.setBinding(binding);
		ICASTFieldDesignator designator = nodeFactory.newCFieldDesignator(name);
		setOffsetAndLength(designator);
		astStack.push(designator);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> simple_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersSimple() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

		ICASTSimpleDeclSpecifier declSpec = nodeFactory.newCSimpleDeclSpecifier();
		
		for(Object token : astStack.closeScope())
			setSpecifier(declSpec, (IToken)token);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> struct_or_union_declaration_specifiers
	 * declaration_specifiers ::= <openscope> enum_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersStructUnionEnum() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> topScope = astStack.closeScope();
		ICASTDeclSpecifier declSpec = CollectionUtils.findFirstAndRemove(topScope, ICASTDeclSpecifier.class);
		
		// now apply the rest of the specifiers
		for(Object token : topScope)
			setSpecifier(declSpec, (IToken)token);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * declaration_specifiers ::=  <openscope> typdef_name_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersTypedefName() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		ICASTTypedefNameSpecifier declSpec = nodeFactory.newCTypedefNameSpecifier();
		
		for(Object o : astStack.topScope()) {
			if(o instanceof IToken) {
				IToken token = (IToken) o;
				// There is one identifier token on the stack
				int kind = asC99Kind(token);
				if(kind == TK_identifier || kind == TK_Completion) {
					IASTName name = createName(token);
					//name.setBinding(binding);
					declSpec.setName(name);
				}
				else {
					setSpecifier(declSpec, token);
				}
			}
		}

		astStack.closeScope();
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * declaration ::= declaration_specifiers <openscope> init_declarator_list ';'
	 * declaration ::= declaration_specifiers  ';'
	 */
	public void consumeDeclarationSimple(boolean hasDeclaratorList) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		List<Object> declarators = (hasDeclaratorList) ? astStack.closeScope() : Collections.emptyList();
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		
		// do not generate nodes for extra EOC tokens
		if(matchTokens(parser.getRuleTokens(), CPPParsersym.TK_EndOfCompletion))
			return;

		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		
		for(Object declarator : declarators)
			declaration.addDeclarator((IASTDeclarator)declarator);

		setOffsetAndLength(declaration);
		astStack.push(declaration);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * external_declaration ::= ';'
	 * 
	 * TODO: doesn't the declaration need a name?
	 */
	public void consumeDeclarationEmpty() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		// Don't generate declaration nodes for extra EOC tokens
		// TODO: the token type must be converted
		if(asC99Kind(parser.getLeftIToken()) == C99Parsersym.TK_EndOfCompletion)
			return;
		
		IASTDeclSpecifier declSpecifier   = nodeFactory.newCSimpleDeclSpecifier();
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		setOffsetAndLength(declSpecifier);
		setOffsetAndLength(declaration);
		astStack.push(declaration);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * a declaration inside of a struct
	 * 
	 * struct_declaration ::= specifier_qualifier_list <openscope> struct_declarator_list ';'
	 * 
	 * specifier_qualifier_list is a subset of declaration_specifiers,
	 * struct_declarators are declarators that are allowed inside a struct,
	 * a struct declarator is a regular declarator plus bit fields
	 */
	public void consumeStructDeclaration(boolean hasDeclaration) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		consumeDeclarationSimple(hasDeclaration); // TODO this is ok as long as bit fields implement IASTDeclarator (see consumeDeclaration())
	} 
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  '{' <openscope> struct_declaration_list_opt '}'
     *       | 'struct' struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
     *       | 'union'  struct_or_union_identifier '{' <openscope> struct_declaration_list_opt '}'
	 * 
	 * @param key either k_struct or k_union from IASTCompositeTypeSpecifier
	 */
	public void consumeTypeSpecifierComposite(boolean hasName, int key) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = (hasName) ? createName(parser.getRuleTokens().get(1)) : nodeFactory.newName();
		
		ICASTCompositeTypeSpecifier typeSpec = nodeFactory.newCCompositeTypeSpecifier(key, name);
		
		for(Object o : astStack.closeScope())
			typeSpec.addMemberDeclaration((IASTDeclaration)o);
		
		setOffsetAndLength(typeSpec);
		astStack.push(typeSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' struct_or_union_identifier
     *       | 'union'  struct_or_union_identifier
     *       
     * enum_specifier ::= 'enum' enum_identifier     
	 */
	public void consumeTypeSpecifierElaborated(int kind  /*, IBinding binding*/) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = createName(parser.getRuleTokens().get(1));
		//name.setBinding(binding);
		IASTElaboratedTypeSpecifier typeSpec = nodeFactory.newElaboratedTypeSpecifier(kind, name);
		setOffsetAndLength(typeSpec);
		astStack.push(typeSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	
	/**
	 * iteration_statement ::= 'while' '(' expression ')' statement
	 */
	public void consumeStatementWhileLoop() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement  body      = (IASTStatement)  astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		IASTWhileStatement whileStatement = nodeFactory.newWhileStatement(condition, body);
		setOffsetAndLength(whileStatement);
		astStack.push(whileStatement);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * selection_statement ::=  switch '(' expression ')' statement
	 */
	public void consumeStatementSwitch() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement body  = (IASTStatement)  astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTSwitchStatement stat = nodeFactory.newSwitchStatment(expr, body);
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	

	public void consumeStatementIf(boolean hasElse) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement elseClause = null;
		if(hasElse)
			elseClause = (IASTStatement) astStack.pop();
		
		IASTStatement thenClause = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		
		IASTIfStatement ifStatement = nodeFactory.newIfStatement(condition, thenClause, elseClause);
		setOffsetAndLength(ifStatement);
		astStack.push(ifStatement);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * function_definition
     *    ::= declaration_specifiers <openscope> declarator compound_statement
     *      | function_declarator compound_statement
     *      
     * The seemingly pointless <openscope> is just there to 
     * prevent a shift/reduce conflict in the grammar.
     */
	public void consumeFunctionDefinition(boolean hasDeclSpecifiers) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace(String.valueOf(hasDeclSpecifiers));
		
		IASTCompoundStatement  body = (IASTCompoundStatement)  astStack.pop();
		IASTFunctionDeclarator decl = (IASTFunctionDeclarator) astStack.pop();
		astStack.closeScope();
		
		IASTDeclSpecifier declSpecifier;
		if(hasDeclSpecifiers) {
			declSpecifier = (IASTDeclSpecifier) astStack.pop();
		}
		else { // there are no decl specifiers, implicit int
			declSpecifier = nodeFactory.newCSimpleDeclSpecifier();
		}
		
		IASTFunctionDefinition def = nodeFactory.newFunctionDefinition(declSpecifier, decl, body);
		setOffsetAndLength(def);
		astStack.push(def);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
    /**
     * function_definition
     *     ::= declaration_specifiers <openscope> declarator 
     *         <openscope> declaration_list compound_statement
     */
	public void consumeFunctionDefinitionKnR() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
    	IASTCompoundStatement  body = (IASTCompoundStatement) astStack.pop();
    	
    	IASTDeclaration[] declarations = (IASTDeclaration[]) astStack.topScope().toArray(new IASTDeclaration[0]);
    	astStack.closeScope();
    	
    	ICASTKnRFunctionDeclarator decl = (ICASTKnRFunctionDeclarator) astStack.pop();
    	astStack.closeScope();

    	ICASTSimpleDeclSpecifier declSpecifier = (ICASTSimpleDeclSpecifier) astStack.pop();
    	decl.setParameterDeclarations(declarations);
		
		// re-compute the length of the declaration to take the parameter declarations into account
		ASTNode lastDeclaration = (ASTNode) declarations[declarations.length-1];
		int endOffset = lastDeclaration.getOffset() + lastDeclaration.getLength();
		((ASTNode)decl).setLength(endOffset - offset(decl));
	
		IASTFunctionDefinition def = nodeFactory.newFunctionDefinition(declSpecifier, decl, body);
		setOffsetAndLength(def);
    	astStack.push(def);
    	
    	if(TRACE_AST_STACK) System.out.println(astStack);
    }
}