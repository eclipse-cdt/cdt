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
package org.eclipse.cdt.core.dom.lrparser.action;

import static org.eclipse.cdt.core.dom.lrparser.action.ParserUtil.*;

import java.util.List;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.LRParserPlugin;
import org.eclipse.cdt.core.dom.lrparser.LRParserProperties;
import org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;

/**
 * Parser semantic actions that are common to both C and C++.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public abstract class BuildASTParserAction extends AbstractParserAction {
	

	/** Abstract factory for creating AST node objects */
	private final INodeFactory nodeFactory;
	
	/** Abstract factory for creating secondary parsers */
	private final ISecondaryParserFactory parserFactory;
	
	
	/**
	 * Returns true if the token is an identifier.
	 */
	protected abstract boolean isIdentifierToken(IToken token);
	
	
	protected IASTTranslationUnit tu = null;

	
	/**
	 * Create a new parser action.
	 * @param tu Root node of the AST, its list of declarations should be empty.
	 * @throws NullPointerException if any of the parameters are null
	 */
	public BuildASTParserAction(ITokenStream parser, ScopedStack<Object> astStack, INodeFactory nodeFactory, ISecondaryParserFactory parserFactory) {
		super(parser, astStack);
		
		if(nodeFactory == null)
			throw new NullPointerException("nodeFactory is null"); //$NON-NLS-1$
		if(parserFactory == null)
			throw new NullPointerException("parserFactory is null"); //$NON-NLS-1$
		
		this.nodeFactory = nodeFactory;
		this.parserFactory = parserFactory;
	}
	
	
	public void initializeTranslationUnit(IScanner scanner, IBuiltinBindingsProvider builtinBindingsProvider, IIndex index) {
		tu = nodeFactory.newTranslationUnit(scanner);
		tu.setIndex(index);
		
		// add built-in names to the scope
		if (builtinBindingsProvider != null) {
			IScope tuScope = tu.getScope();
			IBinding[] bindings = builtinBindingsProvider.getBuiltinBindings(tuScope);
			try {
				for (IBinding binding : bindings) {
					ASTInternal.addBinding(tuScope, binding);
				}
			} catch (DOMException e) {
				LRParserPlugin.logError(e);
			}
		}
		
		if(tu instanceof ASTTranslationUnit) {
			((ASTTranslationUnit)tu).setLocationResolver(scanner.getLocationResolver());
		}
	}


	public void consumeTranslationUnit() {
		if(tu == null)
			tu = nodeFactory.newTranslationUnit();
		
		// can't close the outermost scope
		for(Object o : astStack.topScope()) {
			tu.addDeclaration((IASTDeclaration)o);
		}
		while(!astStack.isEmpty()) {
			astStack.pop();
		}

		// this is the same way that the DOM parser computes the length
		IASTDeclaration[] declarations = tu.getDeclarations();
        if(declarations.length != 0) {
        	IASTNode d = declarations[declarations.length-1];
            ParserUtil.setOffsetAndLength(tu, 0, offset(d) + length(d));
        } 
        
        resolveAmbiguityNodes(tu);
        tu.freeze();
        
        astStack.push(tu);
	}

	
	@Override
	public ASTCompletionNode newCompletionNode(String prefix) {
		return new ASTCompletionNode(prefix, tu);
	}


	/**
	 * Removes ambiguity nodes from the AST by resolving them.
	 * 
	 * @see AbstractGNUSourceCodeParser#resolveAmbiguities()
	 */
	private static void resolveAmbiguityNodes(IASTTranslationUnit tu) {
		if (tu instanceof ASTTranslationUnit) {
			((ASTTranslationUnit)tu).resolveAmbiguities();
		}
	}
	
  	/**
  	 * Consumes a single identifier token.
  	 */
  	public void consumeIdentifierName() {
  		astStack.push(createName(stream.getRightIToken()));
  	}
  	
  	
  	/**
	 * block_item ::= declaration | statement 
	 * 
	 * TODO, be careful where exactly in the grammar this is called, it may be called unnecessarily
	 */
	public void consumeStatementDeclarationWithDisambiguation() {
		IASTDeclaration decl = (IASTDeclaration) astStack.pop();
		IASTDeclarationStatement declarationStatement = nodeFactory.newDeclarationStatement(decl);
		setOffsetAndLength(declarationStatement);
		
		// attempt to also parse the tokens as an expression
		IASTExpressionStatement expressionStatement = null;
		if(decl instanceof IASTSimpleDeclaration) {
			List<IToken> expressionTokens = stream.getRuleTokens();
			expressionTokens = expressionTokens.subList(0, expressionTokens.size()-1); // remove the semicolon at the end
			
			ISecondaryParser<IASTExpression> expressionParser = parserFactory.getExpressionParser(stream, properties);
			IASTExpression expr = runSecondaryParser(expressionParser, expressionTokens);
			
			if(expr != null) { // the parse may fail
				expressionStatement = nodeFactory.newExpressionStatement(expr);
				setOffsetAndLength(expressionStatement);
			}
		}
		
		
		List<IToken> tokens = stream.getRuleTokens();
		
		IASTNode result;
		if(expressionStatement == null) 
			result = declarationStatement;
		else if(expressionStatement.getExpression() instanceof IASTFunctionCallExpression)
			result = expressionStatement;
		else if(tokens.size() == 2 && (isCompletionToken(tokens.get(0)) || isIdentifierToken(tokens.get(0)))) // identifier followed by semicolon
			result = expressionStatement;
		else if(isImplicitInt(decl))
			result = expressionStatement;
		else {
			result = createAmbiguousStatement(declarationStatement, expressionStatement);
			setOffsetAndLength(result);
		}
			
		astStack.push(result);
	}
	
	
	protected abstract IASTAmbiguousStatement createAmbiguousStatement(IASTStatement ... statements);
	
	
	
	/**
	 * Wrap a declaration in a DeclarationStatement.
	 */
	public void consumeStatementDeclaration() {
		IASTDeclaration decl = (IASTDeclaration) astStack.pop();
		IASTDeclarationStatement declarationStatement = nodeFactory.newDeclarationStatement(decl);
		setOffsetAndLength(declarationStatement);
		astStack.push(declarationStatement);
	}
	
	
	/**
	 * Returns true if the given declaration has unspecified type,
     * in this case the type defaults to int and is know as "implicit int".
     * 
	 * With implicit int a lot of language constructs can be accidentally parsed
	 * as declarations:
	 * 
	 * eg) x = 1;
	 * Should be an assignment statement but can also be parsed as a declaration
	 * of a variable x, of unspecified type, initialized to 1.
	 * 
	 * These cases are easy to detect (using this method) and the wrong interpretation
	 * as a declaration is discarded.
     */
    protected static boolean isImplicitInt(IASTDeclaration declaration) {
    	if(declaration instanceof IASTSimpleDeclaration) {
    		IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)declaration).getDeclSpecifier();
    		if(declSpec instanceof IASTSimpleDeclSpecifier && 
    		   ((IASTSimpleDeclSpecifier)declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified) {
    			return true;
    		}
    	}
    	return false;
    }
  	
	
	/**
	 * @param kind One of the kind flags from IASTLiteralExpression or ICPPASTLiteralExpression
	 * @see IASTLiteralExpression
	 * @see ICPPASTLiteralExpression
	 */
	public void consumeExpressionLiteral(int kind) {
		IToken token = stream.getRightIToken();
		String rep = token.toString();
		
		// Strip the quotes from string literals, this is just to be consistent
		// with the dom parser (i.e. to make a test pass)
//		if(kind == IASTLiteralExpression.lk_string_literal && 
//				rep.startsWith("\"") && rep.endsWith("\"")) {
//			rep = rep.substring(1, rep.length()-1);			
//		}
		
		IASTLiteralExpression expr = nodeFactory.newLiteralExpression(kind, rep);
		ParserUtil.setOffsetAndLength(expr, token);
		astStack.push(expr);
	}
	
	

	public void consumeExpressionBracketed() {
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, operand);
        setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	

	public void consumeExpressionID() {
		IASTName name = createName(stream.getLeftIToken());
		IASTIdExpression expr = nodeFactory.newIdExpression(name);
        setOffsetAndLength(expr);
        astStack.push(expr);
	}
	
	
	public void consumeExpressionName() {
		IASTName name = (IASTName) astStack.pop();
		IASTIdExpression expr = nodeFactory.newIdExpression(name);
        setOffsetAndLength(expr);
        astStack.push(expr);
	}
	
	
	/**
	 * expression ::= <openscope-ast> expression_list_actual
	 */
	public void consumeExpressionList() {
		List<Object> expressions = astStack.closeScope();
		if(expressions.size() == 1) {
			astStack.push(expressions.get(0));
		}
		else {
			IASTExpressionList exprList = nodeFactory.newExpressionList();
			
			for(Object o : expressions) {
				exprList.addExpression((IASTExpression)o);
			}
			
			setOffsetAndLength(exprList);
			astStack.push(exprList);
		}
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '[' expression ']'
	 */
	public void consumeExpressionArraySubscript() {
		IASTExpression subscript = (IASTExpression) astStack.pop();
		IASTExpression arrayExpr = (IASTExpression) astStack.pop();
		IASTArraySubscriptExpression expr = nodeFactory.newArraySubscriptExpression(arrayExpr, subscript);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '(' expression_list_opt ')'
	 */
	public void consumeExpressionFunctionCall() {
		IASTExpression argList = (IASTExpression) astStack.pop(); // may be null
		IASTExpression idExpr  = (IASTExpression) astStack.pop();
		
		IASTFunctionCallExpression expr = nodeFactory.newFunctionCallExpression(idExpr, argList);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}

	
	/**
	 * @param operator constant for {@link ICPPASTCastExpression}
	 */
	public void consumeExpressionCast(int operator) {
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTCastExpression expr = nodeFactory.newCastExpression(operator, typeId, operand);
		setOffsetAndLength(expr);
		
		IASTExpression alternateExpr = null;
		if(operator == IASTCastExpression.op_cast) { // don't reparse for dynamic_cast etc as those are not ambiguous
			// try parsing as non-cast to resolve ambiguities
			ISecondaryParser<IASTExpression> secondaryParser = parserFactory.getNoCastExpressionParser(stream, properties);
			alternateExpr = runSecondaryParser(secondaryParser);
		}
		
		if(alternateExpr == null)
			astStack.push(expr);
		else {
			IASTNode ambiguityNode = createAmbiguousExpression(expr, alternateExpr);
			setOffsetAndLength(ambiguityNode);
			astStack.push(ambiguityNode);
		}
	}
	
	
	protected abstract IASTAmbiguousExpression createAmbiguousExpression(IASTExpression ... expressions);
	
	
	/**
	 * Lots of rules, no need to list them.
	 * @param operator From IASTUnaryExpression
	 */
	public void consumeExpressionUnaryOperator(int operator) {
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression(operator, operand);
		setOffsetAndLength(expr);
		astStack.push(expr);
	}
	
	
	
	/**
	 * unary_operation ::= 'sizeof' '(' type_name ')'
	 * @see consumeExpressionUnaryOperator For the other use of sizeof
	 */
	public void consumeExpressionTypeId(int operator) {
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTTypeIdExpression expr = nodeFactory.newTypeIdExpression(operator, typeId);
		setOffsetAndLength(expr);
		
		// try parsing as an expression to resolve ambiguities
		ISecondaryParser<IASTExpression> secondaryParser = parserFactory.getSizeofExpressionParser(stream, properties); 
		IASTExpression alternateExpr = runSecondaryParser(secondaryParser);
		
		if(alternateExpr == null)
			astStack.push(expr);
		else if(isFunctionType(expr)) // bug 252243
			astStack.push(alternateExpr);
		else {
			IASTNode ambiguityNode = createAmbiguousExpression(expr, alternateExpr);
			setOffsetAndLength(ambiguityNode);
			astStack.push(ambiguityNode);
		}
	}
	 
	
	private static boolean isFunctionType(IASTExpression expr) {
		if(expr instanceof IASTTypeIdExpression) {
			IASTTypeId typeId = ((IASTTypeIdExpression) expr).getTypeId();
			return typeId.getAbstractDeclarator() instanceof IASTFunctionDeclarator;
		}
		return false;
	}
	
	
	/**
	 * Lots of rules, no need to list them all.
	 * @param op Field from IASTBinaryExpression
	 */
	public void consumeExpressionBinaryOperator(int op) {
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		IASTBinaryExpression binExpr = nodeFactory.newBinaryExpression(op, expr1, expr2);
		setOffsetAndLength(binExpr);
		astStack.push(binExpr);
	}
	
	
	/**
	 * conditional_expression ::= logical_OR_expression '?' expression ':' conditional_expression
	 */
	public void consumeExpressionConditional() {
		IASTExpression expr3 = (IASTExpression) astStack.pop();
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		IASTConditionalExpression condExpr = nodeFactory.newConditionalExpession(expr1, expr2, expr3);
		setOffsetAndLength(condExpr);
		astStack.push(condExpr);
	}
	
	
	/**
	 * labeled_statement ::= label_identifier ':' statement
	 * label_identifier ::= identifier 
	 */
	public void consumeStatementLabeled() {
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTName label = createName(stream.getLeftIToken());

		IASTLabelStatement stat = nodeFactory.newLabelStatement(label, body);
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * labeled_statement ::= 'case' constant_expression ':' statement
	 */
	public void consumeStatementCase() { 
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		IASTCaseStatement caseStatement = nodeFactory.newCaseStatement(expr);		
		setOffsetAndLength(caseStatement); // TODO this is wrong, need to adjust length to end of colon
		
		// this is a hackey fix because case statements are not modeled correctly in the AST
		IASTCompoundStatement compound = nodeFactory.newCompoundStatement();
		setOffsetAndLength(compound);
		compound.addStatement(caseStatement);
		compound.addStatement(body);
		
		astStack.push(compound);
	}
	
	
	/**
	 * labeled_statement ::= 'default' ':' <openscope-ast> statement
	 */
	public void consumeStatementDefault() {
		IASTStatement body = (IASTStatement) astStack.pop();
		
		IASTDefaultStatement stat = nodeFactory.newDefaultStatement();
		List<IToken> tokens = stream.getRuleTokens();
		IToken defaultToken = tokens.get(0);
		IToken colonToken = tokens.get(1);
		ParserUtil.setOffsetAndLength(stat, offset(defaultToken), offset(colonToken) - offset(defaultToken) + 1);
		
		IASTCompoundStatement compound = nodeFactory.newCompoundStatement();
		setOffsetAndLength(compound);
		compound.addStatement(stat);
		compound.addStatement(body);
		
		astStack.push(compound);
	}
	
	
	
	/**
	 * expression_statement ::= ';'
	 */
	public void consumeStatementNull() {
		IASTNullStatement stat = nodeFactory.newNullStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * expression_statement ::= expression ';'
	 */
	public void consumeStatementExpression() {
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTExpressionStatement stat = nodeFactory.newExpressionStatement(expr);
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	
	/**
	 * compound_statement ::= <openscope> '{' block_item_list '}'
	 * 
	 * block_item_list ::= block_item | block_item_list block_item
	 */
	public void consumeStatementCompoundStatement(boolean hasStatementsInBody) {
		IASTCompoundStatement block = nodeFactory.newCompoundStatement();
		
		if(hasStatementsInBody) {
			for(Object o : astStack.closeScope()) {
				block.addStatement((IASTStatement)o);
			}
		}
		
		setOffsetAndLength(block);
		astStack.push(block);
	}

	
	/**
	 * iteration_statement_matched
	 *     ::= 'do' statement 'while' '(' expression ')' ';'
	 *        | 'do' statement
	 */
	public void consumeStatementDoLoop(boolean hasWhileBlock) {
		IASTExpression condition = hasWhileBlock? (IASTExpression) astStack.pop() : null;
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTDoStatement stat = nodeFactory.newDoStatement(body, condition);
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	

	/**
	 * jump_statement ::= goto goto_identifier ';'
	 */
	public void consumeStatementGoto() {
		IASTName name = createName(stream.getRuleTokens().get(1));
		IASTGotoStatement gotoStat = nodeFactory.newGotoStatement(name);
		setOffsetAndLength(gotoStat);
		astStack.push(gotoStat);
	}
	
	
	/**
	 * jump_statement ::= continue ';'
	 */
	public void consumeStatementContinue() {
		IASTContinueStatement stat = nodeFactory.newContinueStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= break ';'
	 */
	public void consumeStatementBreak() {   
		IASTBreakStatement stat = nodeFactory.newBreakStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
	}
	
	
	/**
	 * jump_statement ::= return ';'
	 * jump_statement ::= return expression ';'
	 */
	public void consumeStatementReturn(boolean hasExpr) {
		IASTExpression expr = hasExpr ? (IASTExpression) astStack.pop() : null;
		IASTReturnStatement returnStat = nodeFactory.newReturnStatement(expr);
		setOffsetAndLength(returnStat);
		astStack.push(returnStat);
	}

	

	
	/**
	 * type_name ::= specifier_qualifier_list
     *             | specifier_qualifier_list abstract_declarator
	 */
	public void consumeTypeId(boolean hasDeclarator) {
		IASTDeclarator declarator;
		if(hasDeclarator)
			declarator = (IASTDeclarator) astStack.pop();
		else {
			declarator = nodeFactory.newDeclarator(nodeFactory.newName());
			ParserUtil.setOffsetAndLength(declarator, stream.getRightIToken().getEndOffset(), 0);
		}
			
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		IASTTypeId typeId = nodeFactory.newTypeId(declSpecifier, declarator);
		setOffsetAndLength(typeId);
		astStack.push(typeId);
	}
	
	
	/**
	 * declarator
     *     ::= <openscope-ast> ptr_operator_seq direct_declarator
     *     
     * abstract_declarator
     *     ::= <openscope-ast> ptr_operator_seq
     *       | <openscope-ast> ptr_operator_seq direct_declarator
	 */
	public void consumeDeclaratorWithPointer(boolean hasDeclarator) {
		IASTDeclarator decl;
		if(hasDeclarator)
			decl = (IASTDeclarator) astStack.pop();
		else
			decl = nodeFactory.newDeclarator(nodeFactory.newName());
		
		for(Object pointer : astStack.closeScope())
			decl.addPointerOperator((IASTPointerOperator)pointer);
		
		setOffsetAndLength(decl);
		astStack.push(decl);
	}
	
	
    
    /**
     * init_declarator
     *     ::= declarator initializer
     *     
     * @param hasDeclarator in C++ its possible for a parameter declaration to specifiy
     *        a default value without also specifying a named declarator
     */
    public void consumeDeclaratorWithInitializer(boolean hasDeclarator) {
	   	IASTInitializer initializer = (IASTInitializer) astStack.pop();
	   	
	   	IASTDeclarator declarator;
	   	if(hasDeclarator) {
	   		declarator = (IASTDeclarator) astStack.peek();
	   	}
	   	else {
	   		IASTName emptyName = nodeFactory.newName();
	   		declarator = nodeFactory.newDeclarator(emptyName);
	   		setOffsetAndLength(emptyName);
	   		astStack.push(declarator);
	   	}
	   	
		declarator.setInitializer(initializer);
		setOffsetAndLength(declarator); // adjust the length to include the initializer
    }
	
    
	/**
	 * asm_definition
     *     ::= 'asm' '(' 'stringlit' ')' ';'
	 */
	public void consumeDeclarationASM() {
		String s = stream.getRuleTokens().get(2).toString();
		IASTASMDeclaration asm = nodeFactory.newASMDeclaration(s);
		
		setOffsetAndLength(asm);
		astStack.push(asm);
	}
	
	
    
    /**
	 * parameter_declaration ::= declaration_specifiers declarator
     *                         | declaration_specifiers abstract_declarator
	 */
	public void consumeParameterDeclaration() {
		IASTDeclarator declarator  = (IASTDeclarator) astStack.pop();
		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) astStack.pop();
		IASTParameterDeclaration declaration = nodeFactory.newParameterDeclaration(declSpec, declarator);
		setOffsetAndLength(declaration);
		astStack.push(declaration);
	}
	
	
	/**
	 * parameter_declaration ::= declaration_specifiers   
	 */
	public void consumeParameterDeclarationWithoutDeclarator() {
		// offsets need to be calculated differently in this case		
		final int endOffset = stream.getRightIToken().getEndOffset();
		
		IASTName name = nodeFactory.newName();
		ParserUtil.setOffsetAndLength(name, endOffset, 0);
		
		// it appears that a declarator is always required in the AST here
		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
		ParserUtil.setOffsetAndLength(declarator, endOffset, 0);
		
		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) astStack.pop();
		IASTParameterDeclaration declaration = nodeFactory.newParameterDeclaration(declSpec, declarator);
		
		setOffsetAndLength(declaration);
		astStack.push(declaration);
	}
	
	
	/**
	 * TODO: do I really want to share declaration rules between the two parsers.
	 * Even if there is potential for reuse it still may be cleaner to leave the 
	 * common stuff to just simple expressions and statements.
	 * 
	 * For C99:
	 * 
	 * declaration ::= declaration_specifiers <openscope> init_declarator_list ';'
	 * declaration ::= declaration_specifiers  ';'
	 * 
	 * 
	 * For C++:
	 * 
	 * simple_declaration
     *     ::= declaration_specifiers_opt <openscope-ast> init_declarator_list_opt ';'
     *     
     *     
     * TODO Make both grammars the same here.
	 */
//	public void consumeDeclarationSimple(boolean hasDeclaratorList) {
//		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
//		
//		List<Object> declarators = (hasDeclaratorList) ? astStack.closeScope() : Collections.emptyList();
//		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop(); // may be null
//		
//		// do not generate nodes for extra EOC tokens
//		if(matchTokens(parser.getRuleTokens(), CPPParsersym.TK_EndOfCompletion))
//			return;
//
//		if(declSpecifier == null) { // can happen if implicit int is used
//			declSpecifier = nodeFactory.newSimpleDeclSpecifier();
//			setOffsetAndLength(declSpecifier, parser.getLeftIToken().getStartOffset(), 0);
//		}
//
//		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
//		
//		for(Object declarator : declarators)
//			declaration.addDeclarator((IASTDeclarator)declarator);
//
//		setOffsetAndLength(declaration);
//		astStack.push(declaration);
//		
//		if(TRACE_AST_STACK) System.out.println(astStack);
//	}
	
	
	/**
	 * direct_declarator ::= '(' declarator ')'
	 */
	public void consumeDirectDeclaratorBracketed() {
		IASTDeclarator nested = (IASTDeclarator) astStack.pop();
		IASTDeclarator declarator = nodeFactory.newDeclarator(nodeFactory.newName());
		declarator.setNestedDeclarator(nested);
		setOffsetAndLength(declarator);
		astStack.push(declarator);
	}
	
	
	/**
	 * direct_declarator ::= declarator_id_name
	 */
	public void consumeDirectDeclaratorIdentifier() {
		IASTName name = (IASTName) astStack.pop();
		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
		setOffsetAndLength(declarator);
		astStack.push(declarator);
	}
	
	
	/**
	 *  array_modifier 
	 *      ::= '[' ']' 
     *        | '[' assignment_expression ']'
     */        
	public void consumeDirectDeclaratorArrayModifier(boolean hasAssignmentExpr) {
		IASTExpression expr = hasAssignmentExpr ? (IASTExpression)astStack.pop() : null;
		IASTArrayModifier arrayModifier = nodeFactory.newArrayModifier(expr);
		setOffsetAndLength(arrayModifier);
		astStack.push(arrayModifier);
	}
	
	
	/**
	 * When the identifier part of a declarator is parsed it will put a plain IASTDeclarator on the stack.
	 * When the array modifier part is parsed we will need to throw away the plain
	 * declarator and replace it with an array declarator. If its a multidimensional array then
	 * the additional array modifiers will need to be added to the array declarator.
	 * Special care is taken for nested declarators.
	 */
	protected void addArrayModifier(IASTArrayModifier arrayModifier) {
		IASTDeclarator node = (IASTDeclarator) astStack.pop();
		
		// Its a nested declarator so create an new ArrayDeclarator
		if(node.getNestedDeclarator() != null) {  //node.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR) {
			IASTArrayDeclarator declarator = nodeFactory.newArrayDeclarator(nodeFactory.newName());
			IASTDeclarator nested = node;
			declarator.setNestedDeclarator(nested);
			
			int offset = offset(nested);
			int length = endOffset(arrayModifier) - offset;
			ParserUtil.setOffsetAndLength(declarator, offset, length);
			
			declarator.addArrayModifier(arrayModifier);
			astStack.push(declarator);
		}
		// There is already an array declarator so just add the modifier to it
		else if(node instanceof IASTArrayDeclarator) {
			IASTArrayDeclarator decl = (IASTArrayDeclarator) node;
			((ASTNode)decl).setLength(endOffset(arrayModifier) - offset(decl));
			
			decl.addArrayModifier(arrayModifier);
			astStack.push(decl);
		}
		// The declarator is an identifier so create a new array declarator
		else  {
			IASTName name = node.getName();
			IASTArrayDeclarator decl = nodeFactory.newArrayDeclarator(name);
			
			int offset = offset(name);
			int length = endOffset(arrayModifier) - offset;
			ParserUtil.setOffsetAndLength(decl, offset, length);
			
			decl.addArrayModifier(arrayModifier);
			astStack.push(decl);
		}
	}
	
	
	/**
	 * Pops a simple declarator from the stack, converts it into 
	 * a FunctionDeclator, then pushes it.
	 * TODO: is this the best way of doing this?
	 * TODO, rename this method, its an accidental overload
	 */
	protected void addFunctionModifier(IASTFunctionDeclarator declarator, int endOffset) {
		IASTDeclarator decl = (IASTDeclarator) astStack.pop();
		 
		if(decl.getNestedDeclarator() != null) { 
			decl = decl.getNestedDeclarator(); // need to remove one level of nesting for function pointers
			declarator.setNestedDeclarator(decl);
			declarator.setName(nodeFactory.newName());
			int offset = offset(decl);
			ParserUtil.setOffsetAndLength(declarator, offset, endOffset - offset);
			astStack.push(declarator);
		}
		else  {
			IASTName name = decl.getName();
			if(name == null) {
				name = nodeFactory.newName();
			}
			declarator.setName(name);
			
			IASTPointerOperator[] pointers = decl.getPointerOperators();
			for(int i = 0; i < pointers.length; i++) {
				declarator.addPointerOperator(pointers[i]);
			}
			
			int offset = offset(name); // TODO
			ParserUtil.setOffsetAndLength(declarator, offset, endOffset - offset);
			astStack.push(declarator);
		}
	}
	
	// TODO why is this here
//	/**
//	 * direct_declarator ::= direct_declarator array_modifier
//	 * consume the direct_declarator part and add the array modifier
//	 */
//	public void consumeDirectDeclaratorArrayDeclarator() {
//		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
//		
//		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
//		addArrayModifier(arrayModifier);
//	}
	
	
	/**
	 * direct_abstract_declarator   
     *     ::= array_modifier
     *       | direct_abstract_declarator array_modifier
	 */
	public void consumeDirectDeclaratorArrayDeclarator(boolean hasDeclarator) {
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		
		if(hasDeclarator) {
			addArrayModifier(arrayModifier);
		}
		else {
			IASTArrayDeclarator decl = nodeFactory.newArrayDeclarator(nodeFactory.newName());
			decl.addArrayModifier(arrayModifier);
			setOffsetAndLength(decl);
			astStack.push(decl);
		}
	}
	
	
	
	
	/**
	 * enum_specifier ::= 'enum' '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt '}'
	 */
	public void consumeTypeSpecifierEnumeration(boolean hasIdent) {
		IASTName name = (hasIdent) ? createName(stream.getRuleTokens().get(1)) : nodeFactory.newName();
		
		IASTEnumerationSpecifier enumSpec = nodeFactory.newEnumerationSpecifier(name);

		for(Object o : astStack.closeScope())
			enumSpec.addEnumerator((IASTEnumerator)o);

		setOffsetAndLength(enumSpec);
		astStack.push(enumSpec);
	}
	
	
	/**
	 * enumerator ::= enum_identifier
     *              | enum_identifier '=' constant_expression
	 */
	public void consumeEnumerator(boolean hasInitializer) {
		IASTName name = createName(stream.getLeftIToken());
		
		IASTExpression value = null;
		if(hasInitializer)
			value = (IASTExpression) astStack.pop();
		
		IASTEnumerator enumerator = nodeFactory.newEnumerator(name, value);
		setOffsetAndLength(enumerator);
		astStack.push(enumerator);
	}
	
	
	private int initializerListNestingLevel = 0;
	
	public void initializerListStart() {
		initializerListNestingLevel++;
	}
	
	public void initializerListEnd() {
		initializerListNestingLevel--;
	}
	
	/**
	 * initializer ::= assignment_expression
	 */
	public void consumeInitializer() {
		//CDT_70_FIX_FROM_50-#4
		IASTInitializerClause  initClause = (IASTInitializerClause) astStack.pop();
		if(initClause instanceof IASTExpression){
			if(discardInitializer((IASTExpression)initClause)) { 
				astStack.push(null);
				return;
			}
		}
		//CDT_70_FIX_FROM_50-#2
		//IASTInitializerExpression initializer = nodeFactory.newInitializerExpression(expr);
		IASTEqualsInitializer initializer = nodeFactory.newEqualsInitializer(initClause);
		setOffsetAndLength(initializer);
		astStack.push(initializer);
	}

	
	private boolean discardInitializer(IASTExpression expression) {
		return initializerListNestingLevel > 0
		    && "true".equals(properties.get(LRParserProperties.SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS)) //$NON-NLS-1$
		    && !ASTQueries.canContainName(expression);
	}
	
	
	
	/**
	 * initializer ::= '{' <openscope> initializer_list '}'
     *               | '{' <openscope> initializer_list ',' '}'
	 */
	public void consumeInitializerList() {
		IASTInitializerList list = nodeFactory.newInitializerList();

		for(Object o : astStack.closeScope())
			list.addInitializer((IASTInitializer)o);
		
		setOffsetAndLength(list);
		astStack.push(list);
	}
	
	
	
	
	
	/**
	 * struct_declarator
     *     ::= ':' constant_expression  
     *       | declarator ':' constant_expression		
	 */
	public void consumeBitField(boolean hasDeclarator) {
		IASTExpression expr = (IASTExpression)astStack.pop();
		
		IASTName name;
		if(hasDeclarator) // it should have been parsed into a regular declarator
			name = ((IASTDeclarator) astStack.pop()).getName();
		else
			name = nodeFactory.newName();
		
		IASTFieldDeclarator fieldDecl = nodeFactory.newFieldDeclarator(name, expr);
		setOffsetAndLength(fieldDecl);
		astStack.push(fieldDecl);
	}
	
	

	/**
	 * statement ::= ERROR_TOKEN
	 */
	public void consumeStatementProblem() {
		consumeProblem(nodeFactory.newProblemStatement(null));
	}

	/**
	 * assignment_expression ::= ERROR_TOKEN
	 * constant_expression ::= ERROR_TOKEN
	 */
	public void consumeExpressionProblem() {
		consumeProblem(nodeFactory.newProblemExpression(null));
	}

	/**
	 * external_declaration ::= ERROR_TOKEN
	 */
	public void consumeDeclarationProblem() {
		consumeProblem(nodeFactory.newProblemDeclaration(null));
	}

	
	private void consumeProblem(IASTProblemHolder problemHolder) {
		IASTProblem problem = nodeFactory.newProblem(IProblem.SYNTAX_ERROR, new char[0], true);
		problemHolder.setProblem(problem);		
		setOffsetAndLength(problem);
		setOffsetAndLength((ASTNode)problemHolder);
		astStack.push(problemHolder);
	}

}