/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.dom.lrparser.action.c99;

import static org.eclipse.cdt.core.dom.lrparser.action.ParserUtil.*;
import static org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym.*;

import java.util.Collections;
import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
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
import org.eclipse.cdt.core.dom.ast.c.ICNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.action.BuildASTParserAction;
import org.eclipse.cdt.core.dom.lrparser.action.ISecondaryParserFactory;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenMap;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.core.dom.lrparser.action.ParserUtil;
import org.eclipse.cdt.core.dom.lrparser.action.ScopedStack;
import org.eclipse.cdt.core.dom.lrparser.action.TokenMap;
import org.eclipse.cdt.core.parser.util.CollectionUtils;
import org.eclipse.cdt.internal.core.dom.lrparser.c99.C99Parsersym;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTAmbiguousStatement;

/**
 * Semantic actions called by the C99 parser to build an AST.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public class C99BuildASTParserAction extends BuildASTParserAction  {

	private final ITokenMap tokenMap;
	
	/** Used to create the AST node objects */
	protected final ICNodeFactory nodeFactory;
	
	private final ISecondaryParserFactory parserFactory;
	
	/**
	 * @param parser
	 * @param orderedTerminalSymbols When an instance of this class is created for a parser
	 * that parsers token kinds will be mapped back to the base C99 parser's token kinds.
	 */
	public C99BuildASTParserAction(ITokenStream parser, ScopedStack<Object> astStack, ICNodeFactory nodeFactory, ISecondaryParserFactory parserFactory) {
		super(parser, astStack, nodeFactory, parserFactory);
		
		this.nodeFactory = nodeFactory;
		this.parserFactory = parserFactory;
		this.tokenMap = new TokenMap(C99Parsersym.orderedTerminalSymbols, parser.getOrderedTerminalSymbols());
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
	
	
	/********************************************************************
	 * Start of semantic actions.
	 ********************************************************************/



	/**
	 * postfix_expression ::= postfix_expression '.' ident
	 * postfix_expression ::= postfix_expression '->' ident
	 */
	public void consumeExpressionFieldReference(boolean isPointerDereference) {
		IASTName name = createName(stream.getRightIToken());
		IASTExpression owner = (IASTExpression) astStack.pop();
		IASTFieldReference expr = nodeFactory.newFieldReference(name, owner);
		expr.setIsPointerDereference(isPointerDereference);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= '(' type_id ')' initializer_list    
	 */
	public void consumeExpressionTypeIdInitializer() {
		IASTInitializerList list = (IASTInitializerList) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		ICASTTypeIdInitializerExpression expr = nodeFactory.newTypeIdInitializerExpression(typeId, list);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * Applies a specifier to a decl spec node.
	 * 
	 * In plain C99 specifiers are always just single tokens, but in language
	 * extensions specifiers may be more complex. Thats why this method takes
	 * Object as the type of the specifier, so that it may be overridden in subclasses
	 * and used with arbitrary objects as the specifier.
	 */
	public void setSpecifier(ICASTDeclSpecifier node, Object specifier) {
		if(!(specifier instanceof IToken))
			return;
		IToken token = (IToken)specifier;
		
		int kind = baseKind(token);
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
			switch(baseKind((IToken)o)) {
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
		assert isStatic || isVarSized || hasTypeQualifierList;
		
		ICASTArrayModifier arrayModifier = nodeFactory.newArrayModifier(null);
		
		// consume all the stuff between the square brackets into an array modifier
		arrayModifier.setStatic(isStatic);
		arrayModifier.setVariableSized(isVarSized);
		
		if(hasAssignmentExpr)
			arrayModifier.setConstantExpression((IASTExpression)astStack.pop());
		
		if(hasTypeQualifierList)
			collectArrayModifierTypeQualifiers(arrayModifier);

		setOffsetAndLength(arrayModifier);
		astStack.push(arrayModifier);
	}


	
	/**
	 * direct_declarator ::= direct_declarator '(' <openscope> identifier_list ')'
	 */
	public void consumeDirectDeclaratorFunctionDeclaratorKnR() {
		ICASTKnRFunctionDeclarator declarator = nodeFactory.newKnRFunctionDeclarator(null, null);
		IASTName[] names = astStack.topScope().toArray(new IASTName[0]);
		declarator.setParameterNames(names);
		astStack.closeScope();
		int endOffset = endOffset(stream.getRightIToken());
		addFunctionModifier(declarator, endOffset);
	}
	
	
	/**
	 * identifier_list
     *     ::= 'identifier'
     *       | identifier_list ',' 'identifier'
	 */
	public void consumeIdentifierKnR() {
		IASTName name = createName(stream.getRightIToken());
		astStack.push(name);
	}
	


	
	/**
	 * pointer ::= '*'
     *           | pointer '*' 
     */ 
	public void consumePointer() {
		IASTPointer pointer = nodeFactory.newPointer();
		IToken star = stream.getRightIToken();
		ParserUtil.setOffsetAndLength(pointer, star);
		astStack.push(pointer);
	}
	
	
	/**
	 * pointer ::= '*' <openscope> type_qualifier_list
     *           | pointer '*' <openscope> type_qualifier_list
	 */
	public void consumePointerTypeQualifierList() {
		ICASTPointer pointer = nodeFactory.newPointer();

		for(Object o : astStack.closeScope()) {
			IToken token = (IToken)o;			
			switch(baseKind(token)) {
				default: assert false;
				case TK_const:    pointer.setConst(true);    break;
				case TK_volatile: pointer.setVolatile(true); break;
				case TK_restrict: pointer.setRestrict(true); break;
			}
		}

		setOffsetAndLength(pointer);
		astStack.push(pointer);
	}
	
	
	
	/**
	 * direct_abstract_declarator  
	 *     ::= '(' ')'
     *       | direct_abstract_declarator '(' ')'
     *       | '(' <openscope> parameter_type_list ')'
     *       | direct_abstract_declarator '(' <openscope> parameter_type_list ')'
	 */
	public void consumeDirectDeclaratorFunctionDeclarator(boolean hasDeclarator, boolean hasParameters) {
		IASTName name = nodeFactory.newName();
		IASTStandardFunctionDeclarator declarator = nodeFactory.newFunctionDeclarator(name);
		
		if(hasParameters) {
			boolean isVarArgs = astStack.pop() == PLACE_HOLDER;
			declarator.setVarArgs(isVarArgs);
			
			for(Object param : astStack.closeScope())
				declarator.addParameterDeclaration((IASTParameterDeclaration)param);
		}
		
		if(hasDeclarator) {
			addFunctionModifier(declarator, endOffset(stream.getRightIToken()));
		}
		else {
			setOffsetAndLength(declarator);
			astStack.push(declarator);
		}
	}

	
	
	/**
	 * designated_initializer ::= <openscope> designation initializer
	 */
	public void consumeInitializerDesignated() {
		IASTInitializer initializer = (IASTInitializer)astStack.pop();
		ICASTDesignatedInitializer result = nodeFactory.newDesignatedInitializer(initializer);
		
		for(Object o : astStack.closeScope()) 
			result.addDesignator((ICASTDesignator)o);
		
		setOffsetAndLength(result);
		astStack.push(result);
	}
	
	
	/**
	 * designator ::= '[' constant_expression ']'
	 */
	public void consumeDesignatorArray() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		ICASTArrayDesignator designator = nodeFactory.newArrayDesignator(expr);
		setOffsetAndLength(designator);
		astStack.push(designator);
	}
	
	
	/**
	 *  designator ::= '.' 'identifier'
	 */
	public void consumeDesignatorField() {
		IASTName name = createName(stream.getRightIToken());
		ICASTFieldDesignator designator = nodeFactory.newFieldDesignator(name);
		setOffsetAndLength(designator);
		astStack.push(designator);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> simple_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersSimple() {
		ICASTSimpleDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifier();
		
		for(Object specifier : astStack.closeScope())
			setSpecifier(declSpec, specifier);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration_specifiers ::= <openscope> struct_or_union_declaration_specifiers
	 * declaration_specifiers ::= <openscope> enum_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersStructUnionEnum() {
		List<Object> topScope = astStack.closeScope();
		ICASTDeclSpecifier declSpec = CollectionUtils.findFirstAndRemove(topScope, ICASTDeclSpecifier.class);
		
		// now apply the rest of the specifiers
		for(Object specifier : topScope)
			setSpecifier(declSpec, specifier);
		
		setOffsetAndLength(declSpec);
		astStack.push(declSpec);
	}
	
	
	/**
	 * declaration_specifiers ::=  <openscope> typdef_name_declaration_specifiers
	 */
	public void consumeDeclarationSpecifiersTypedefName() {
		ICASTTypedefNameSpecifier declSpec = nodeFactory.newTypedefNameSpecifier(null);
		
		for(Object o : astStack.topScope()) {
			if(o instanceof IToken) {
				IToken token = (IToken) o;
				// There is one identifier token on the stack
				int kind = baseKind(token);
				if(kind == TK_identifier || kind == TK_Completion) {
					IASTName name = createName(token);
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
	}
	
	
	
	/**
	 * declaration ::= declaration_specifiers <openscope> init_declarator_list ';'
	 * declaration ::= declaration_specifiers  ';'
	 */
	public void consumeDeclarationSimple(boolean hasDeclaratorList) {
		List<Object> declarators = (hasDeclaratorList) ? astStack.closeScope() : Collections.emptyList();
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		
		List<IToken> ruleTokens = stream.getRuleTokens();
		if(ruleTokens.size() == 1 && baseKind(ruleTokens.get(0)) == TK_EndOfCompletion) 
			return; // do not generate nodes for extra EOC tokens
		
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		
		for(Object declarator : declarators)
			declaration.addDeclarator((IASTDeclarator)declarator);

		setOffsetAndLength(declaration);
		astStack.push(declaration);
	}
	
	
	
	/**
	 * external_declaration ::= ';'
	 * 
	 * TODO: doesn't the declaration need a name?
	 */
	public void consumeDeclarationEmpty() {
		// Don't generate declaration nodes for extra EOC tokens
		if(baseKind(stream.getLeftIToken()) == C99Parsersym.TK_EndOfCompletion)
			return;
		
		IASTDeclSpecifier declSpecifier   = nodeFactory.newSimpleDeclSpecifier();
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		setOffsetAndLength(declSpecifier);
		setOffsetAndLength(declaration);
		astStack.push(declaration);
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
	public void consumeTypeSpecifierComposite(boolean hasName) {
		
		int key = 0;
		switch(baseKind(stream.getLeftIToken())) {
			case TK_struct: key = IASTCompositeTypeSpecifier.k_struct;
			case TK_union:  key = IASTCompositeTypeSpecifier.k_union;
		}
		
		IASTName name = (hasName) ? createName(stream.getRuleTokens().get(1)) : nodeFactory.newName();
		
		ICASTCompositeTypeSpecifier typeSpec = nodeFactory.newCompositeTypeSpecifier(key, name);
		
		for(Object o : astStack.closeScope())
			typeSpec.addMemberDeclaration((IASTDeclaration)o);
		
		setOffsetAndLength(typeSpec);
		astStack.push(typeSpec);
	}
	
	
	/**
	 * struct_or_union_specifier
     *     ::= 'struct' struct_or_union_identifier
     *       | 'union'  struct_or_union_identifier
     *       
     * enum_specifier ::= 'enum' enum_identifier     
	 */
	public void consumeTypeSpecifierElaborated(int kind) {
		IASTName name = createName(stream.getRuleTokens().get(1));
		IASTElaboratedTypeSpecifier typeSpec = nodeFactory.newElaboratedTypeSpecifier(kind, name);
		setOffsetAndLength(typeSpec);
		astStack.push(typeSpec);
	}
	
	
	
	
	/**
	 * iteration_statement ::= 'while' '(' expression ')' statement
	 */
	public void consumeStatementWhileLoop() {
		IASTStatement  body      = (IASTStatement)  astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		IASTWhileStatement whileStatement = nodeFactory.newWhileStatement(condition, body);
		setOffsetAndLength(whileStatement);
		astStack.push(whileStatement);
	}
	
	
	
	/**
	 * iteration_statement_matched
	 *     ::= 'for' '(' expression_opt ';' expression_opt ';' expression_opt ')' statement
	 */
	public void consumeStatementForLoop() {
		IASTStatement body = (IASTStatement) astStack.pop();
		// these two expressions may be null, see consumeExpressionOptional()
		IASTExpression expr3 = (IASTExpression) astStack.pop();
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTNode node = (IASTNode) astStack.pop(); // may be an expression or a declaration
		
		IASTStatement initializer;
		if(node instanceof IASTExpression)
			initializer = nodeFactory.newExpressionStatement((IASTExpression)node);
		else if(node instanceof IASTDeclaration)
			initializer = nodeFactory.newDeclarationStatement((IASTDeclaration)node);
		else // its null
			initializer = nodeFactory.newNullStatement();
		
		
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
		
		//initializer could be an expression or a declaration
		int TK_SC = TK_SemiColon;
		IASTExpressionStatement expressionStatement = null;
		if(initializer instanceof IASTDeclarationStatement) {
			IASTDeclarationStatement declarationStatement = (IASTDeclarationStatement) initializer;
			List<IToken> expressionTokens = stream.getRuleTokens();
			
			//find the first semicolon
			int end_pos = -1;
			for(int i = 0, n = expressionTokens.size(); i < n; i++) {
				if(tokenMap.mapKind(expressionTokens.get(i).getKind()) == TK_SC) {
					end_pos = i;
					break;
				}
			}
			
			if (end_pos != -1) {	
				expressionTokens = expressionTokens.subList(2, end_pos);
				
				ISecondaryParser<IASTExpression> expressionParser = parserFactory.getExpressionParser(stream, properties);
				IASTExpression expr1 = runSecondaryParser(expressionParser, expressionTokens);
				
				if(expr1 != null) { // the parse may fail
					expressionStatement = nodeFactory.newExpressionStatement(expr1);
					setOffsetAndLength(expressionStatement);
				}
			}
			
			if(expressionStatement == null) 
				initializer = declarationStatement;
			else {
				initializer = createAmbiguousStatement(expressionStatement, declarationStatement);
				setOffsetAndLength(initializer);
			}
		}
		
		if(node != null)
			ParserUtil.setOffsetAndLength(initializer, offset(node), length(node));
		
		IASTForStatement forStat = nodeFactory.newForStatement(initializer, expr2, expr3, body);
		setOffsetAndLength(forStat);
		astStack.push(forStat);
	}
	
	
	
	/**
	 * selection_statement ::=  switch '(' expression ')' statement
	 */
	public void consumeStatementSwitch() {
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTSwitchStatement stat = nodeFactory.newSwitchStatement(expr, body);
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	

	public void consumeStatementIf(boolean hasElse) {
		IASTStatement elseClause = null;
		if(hasElse)
			elseClause = (IASTStatement) astStack.pop();
		
		IASTStatement thenClause = (IASTStatement) astStack.pop();
		IASTExpression condition = (IASTExpression) astStack.pop();
		
		IASTIfStatement ifStatement = nodeFactory.newIfStatement(condition, thenClause, elseClause);
		setOffsetAndLength(ifStatement);
		astStack.push(ifStatement);
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
		IASTCompoundStatement  body = (IASTCompoundStatement)  astStack.pop();
		IASTFunctionDeclarator decl = (IASTFunctionDeclarator) astStack.pop();
		astStack.closeScope();
		
		IASTDeclSpecifier declSpecifier;
		if(hasDeclSpecifiers) {
			declSpecifier = (IASTDeclSpecifier) astStack.pop();
		}
		else { // there are no decl specifiers, implicit int
			declSpecifier = nodeFactory.newSimpleDeclSpecifier();
		}
		
		IASTFunctionDefinition def = nodeFactory.newFunctionDefinition(declSpecifier, decl, body);
		setOffsetAndLength(def);
		astStack.push(def);
	}
	
	
	
    /**
     * function_definition
     *     ::= declaration_specifiers <openscope-ast> knr_function_declarator 
     *     <openscope-ast> declaration_list compound_statement
     */
	public void consumeFunctionDefinitionKnR() {
    	IASTCompoundStatement  body = (IASTCompoundStatement) astStack.pop();
    	
    	IASTDeclaration[] declarations = astStack.topScope().toArray(new IASTDeclaration[0]);
    	astStack.closeScope();
    	
    	ICASTKnRFunctionDeclarator decl = (ICASTKnRFunctionDeclarator) astStack.pop();
    	astStack.closeScope();

    	ICASTDeclSpecifier declSpecifier = (ICASTDeclSpecifier) astStack.pop();
    	decl.setParameterDeclarations(declarations);
		
		// re-compute the length of the declaration to take the parameter declarations into account
		ASTNode lastDeclaration = (ASTNode) declarations[declarations.length-1];
		int endOffset = lastDeclaration.getOffset() + lastDeclaration.getLength();
		((ASTNode)decl).setLength(endOffset - offset(decl));
	
		IASTFunctionDefinition def = nodeFactory.newFunctionDefinition(declSpecifier, decl, body);
		setOffsetAndLength(def);
    	astStack.push(def);
    }



	@Override
	protected IASTAmbiguousExpression createAmbiguousExpression(IASTExpression... expressions) {
		return new CASTAmbiguousExpression(expressions);
	}


	@Override
	protected IASTAmbiguousStatement createAmbiguousStatement(IASTStatement... statements) {
		return new CASTAmbiguousStatement(statements);
	}
}