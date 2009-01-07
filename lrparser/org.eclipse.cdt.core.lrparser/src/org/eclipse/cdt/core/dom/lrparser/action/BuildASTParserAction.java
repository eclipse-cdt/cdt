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
package org.eclipse.cdt.core.dom.lrparser.action;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.IParserActionTokenProvider;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.util.DebugUtil;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;


/**
 * Parser semantic actions that are common to both C and C++.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public abstract class BuildASTParserAction {


	/**
	 * Used with very simple optional rules that just say
	 * that some particular token or keyword is optional.
	 * The presence of the PLACE_HOLDER on the stack means that the keyword
	 * was parsed, the presence of null means the keyword wasn't parsed. 
	 * 
	 * @see BuildASTParserAction#consumePlaceHolder()
	 * @see BuildASTParserAction#consumeEmpty()
	 */
	protected static final Object PLACE_HOLDER = Boolean.TRUE; // any object will do
	
	
	// turn debug tracing on and off
	// TODO move this into an AspectJ project
	protected static final boolean TRACE_ACTIONS = false;
	protected static final boolean TRACE_AST_STACK = false;
	
	
	/** Stack that holds the intermediate nodes as the AST is being built */
	protected final ScopedStack<Object> astStack = new ScopedStack<Object>();
	
	/** Provides an interface to the token stream */
	protected final IParserActionTokenProvider parser;
	
	/** The completion node, only generated during a completion parse */
	protected ASTCompletionNode completionNode;
	
	/** The root node is created outside the parser because it is also needed by the preprocessor */
	protected final IASTTranslationUnit tu;

	/** Abstract factory for creating AST node objects */
	private final INodeFactory nodeFactory;
	
	/** Options that change the behavior of the parser actions */
	protected Set<IParser.Options> options = EnumSet.noneOf(IParser.Options.class);
	
	
	/**
	 * Completion tokens are represented by different kinds by different parsers.
	 */
	protected abstract boolean isCompletionToken(IToken token);
	
	
	/**
	 * Returns true if the token is an identifier.
	 */
	protected abstract boolean isIdentifierToken(IToken token);
	
	
	/**
	 * Get the parser that will recognize expressions.
	 */
	protected abstract IParser getExpressionParser();
	
	
	/**
	 * Expression parser that does not recognize cast expressions,
	 * used to disambiguate casts. 
	 */
	protected abstract IParser getNoCastExpressionParser();
	
	
	/**
	 * Expression parser that treats all sizeof and typeid expressions
	 * as unary expressions.
	 */
	protected abstract IParser getSizeofExpressionParser();
	
	
	
	/**
	 * Create a new parser action.
	 * @param tu Root node of the AST, its list of declarations should be empty.
	 * @throws NullPointerException if any of the parameters are null
	 */
	public BuildASTParserAction(INodeFactory nodeFactory, IParserActionTokenProvider parser, IASTTranslationUnit tu) {
		if(nodeFactory == null)
			throw new NullPointerException("nodeFactory is null"); //$NON-NLS-1$
		if(parser == null)
			throw new NullPointerException("parser is null"); //$NON-NLS-1$
		if(tu == null)
			throw new NullPointerException("tu is null"); //$NON-NLS-1$
		
		this.nodeFactory = nodeFactory;
		this.parser = parser;
		this.tu = tu;
	}

	
	public void setParserOptions(Set<IParser.Options> options) {
		this.options = options == null ? EnumSet.noneOf(IParser.Options.class) : options;
	}
	
	/**
	 * Creates a completion node if one does not yet exist and adds the 
	 * given name to it.
	 */
	protected void addNameToCompletionNode(IASTName name, String prefix) {
		if(completionNode == null) {
			prefix = (prefix == null || prefix.length() == 0) ? null : prefix;
			completionNode = newCompletionNode(prefix, tu);
		}
		
		completionNode.addName(name);
	}
	
	public ASTCompletionNode newCompletionNode(String prefix, IASTTranslationUnit tu) {
		return new ASTCompletionNode((prefix == null || prefix.length() == 0) ? null : prefix, tu);
	}
	
	/**
	 * Used to combine completion nodes from secondary parsers into
	 * the main completion node.
	 */
	protected void addNameToCompletionNode(IASTCompletionNode node) {
		if(node == null)
			return;
		
		for(IASTName name : node.getNames())
			addNameToCompletionNode(name, node.getPrefix());
	}
	
	
	/**
	 * Returns the completion node if this is a completion parse, null otherwise.
	 */
	public IASTCompletionNode getASTCompletionNode() {
		return completionNode;
	}
	
	
	/**
	 * Used to get the result of secondary parsers.
	 */
	public IASTNode getSecondaryParseResult() {
		return (IASTNode) astStack.pop();
	}
	
	
	
	protected static int offset(IToken token) {
		return token.getStartOffset();
	}

	protected static int offset(IASTNode node) {
		return ((ASTNode)node).getOffset();
	}

	protected static int length(IToken token) {
		return endOffset(token) - offset(token);
	}

	protected static int length(IASTNode node) {
		return ((ASTNode)node).getLength();
	}

	protected static int endOffset(IASTNode node) {
		return offset(node) + length(node);
	}

	protected static int endOffset(IToken token) {
		return token.getEndOffset();
	}


	protected void setOffsetAndLength(IASTNode node) {
		int ruleOffset = parser.getLeftIToken().getStartOffset();
		int ruleLength = parser.getRightIToken().getEndOffset() - ruleOffset;
		((ASTNode)node).setOffsetAndLength(ruleOffset, ruleLength < 0 ? 0 : ruleLength);
	}

	protected static void setOffsetAndLength(IASTNode node, IToken token) {
		((ASTNode)node).setOffsetAndLength(offset(token), length(token));
	}

	protected static void setOffsetAndLength(IASTNode node, int offset, int length) {
		((ASTNode)node).setOffsetAndLength(offset, length);
	}
	
	protected static void setOffsetAndLength(IASTNode node, IASTNode from) {
		setOffsetAndLength(node, offset(from), length(from));
	}

	
	protected static boolean isSameName(IASTName name1, IASTName name2) {
		return Arrays.equals(name1.getLookupKey(), name2.getLookupKey());
	}
	
	/**
	 * Creates a IASTName node from an identifier token.
	 * If the token is a completion token then it is added to the completion node.
	 */
	protected IASTName createName(IToken token) {
		IASTName name = nodeFactory.newName(token.toString().toCharArray()); // TODO, token.toCharArray();
		setOffsetAndLength(name, token); 
		
		if(isCompletionToken(token))
			addNameToCompletionNode(name, token.toString());
		
		return name;
	}
	
	
	/**
	 * Runs the given parser on the given token list.
	 * 
	 */
	protected IASTNode runSecondaryParser(IParser secondaryParser) {
		return runSecondaryParser(secondaryParser, parser.getRuleTokens());
	}
	
	
	/**
	 * Runs the given parser on the tokens that make up the current rule.
	 */
	protected IASTNode runSecondaryParser(IParser secondaryParser, List<IToken> tokens) { 
		// the secondary parser will alter the token kinds, which will need to be undone
		int[] savedKinds = new int[tokens.size()];
		
		int i = 0;
		for(IToken token : tokens)
			savedKinds[i++] = token.getKind();
		
		secondaryParser.setTokens(tokens);
		
		// need to pass tu because any new completion nodes need to be linked directly to the root
		IASTCompletionNode compNode = secondaryParser.parse(tu, options);
		addNameToCompletionNode(compNode);
		IASTNode result = secondaryParser.getSecondaryParseResult();
		
		// restore the token kinds
		i = 0;
		for(IToken token : tokens)
			token.setKind(savedKinds[i++]);
		
		return result;
	}
	
	
	
	/**
	 * Allows simple pattern match testing of lists of tokens.
	 * 
	 * TODO: need to take token mapping into account
	 * 
	 * @throws NullPointerException if source or pattern is null
	 */
	public static boolean matchTokens(List<IToken> source, ITokenMap tokenMap, Integer ... pattern) {
		if(source.size() != pattern.length) // throws NPE if either parameter is null
			return false;
		
		for(int i = 0, n = pattern.length; i < n; i++) {
			if(tokenMap.mapKind(source.get(i).getKind()) != pattern[i].intValue())
				return false;
		}
		return true;
	}
	
	
	/**
	 * Finds the tokens in the given list that are between startOffset and endOffset.
	 * Note, the offsets have to be exact.
	 */
	public static List<IToken> tokenOffsetSubList(List<IToken> tokens, int startOffset, int endOffset) {
		int first = 0, last = 0;
		int i = 0;
		for(IToken t : tokens) {
			if(offset(t) == startOffset) {
				first = i;
			}
			if(endOffset(t) == endOffset) {
				last = i;
				break;
			}
			i++;
		}
		return tokens.subList(first, last + 1);
	}
	
	
	/*************************************************************************************************************
	 * Start of actions.
	 ************************************************************************************************************/
	
	

	/**
	 * Method that is called by the special <openscope> production
	 * in order to create a new scope in the AST stack.
	 */
	public void openASTScope() {
		astStack.openScope();
	}
	
	
	
	/**
	 * Place null on the stack.
	 * Usually called for optional element to indicate the element
	 * was not parsed.
	 */
	public void consumeEmpty() {
		astStack.push(null);
	}

	
	/**
	 * Place a marker on the stack.
	 * Usually used for very simple optional elements to indicate
	 * the element was parsed. Usually the existence of an AST node
	 * on the stack is used instead of the marker, but for simple
	 * cases like an optional keyword this action is useful. 
	 */
	public void consumePlaceHolder() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		astStack.push(PLACE_HOLDER);
	}
	
	
	
	/**
	 * Gets the current token and places it on the stack for later consumption.
	 */
	public void consumeDeclSpecToken() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		astStack.push(parser.getRightIToken());
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * Gets the current token and places it on the stack for later consumption.
	 */
	public void consumeToken() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		astStack.push(parser.getRightIToken());
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	

	
	public void consumeTranslationUnit() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		// can't close the outermost scope
		// the outermost scope may be empty if there are no tokens in the file
		for(Object o : astStack.topScope()) {
			tu.addDeclaration((IASTDeclaration)o);
		}

		// this is the same way that the DOM parser computes the length
		IASTDeclaration[] declarations = tu.getDeclarations();
        if (declarations.length != 0) {
        	IASTNode d = declarations[declarations.length-1];
            setOffsetAndLength(tu, 0, offset(d) + length(d));
        } 
        
        resolveAmbiguityNodes();
        tu.freeze();

        if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	/**
	 * Removes ambiguity nodes from the AST by resolving them.
	 * The ambiguity nodes resolve themselves when visited for the first time.
	 * All ambiguities must be resolved before the AST is returned.
	 * 
	 * @see CPPASTAmbiguity.accept()
	 * @see CASTAmbiguity.accept()
	 * 
	 * TODO Ambiguity resolution may be avoided in the case that no
	 * ambiguity nodes were created.
	 */
	private void resolveAmbiguityNodes() {
		tu.accept(EMPTY_VISITOR); // TODO make sure the DOM parser still does it this way
		if (tu instanceof ASTTranslationUnit) {
			((ASTTranslationUnit)tu).cleanupAfterAmbiguityResolution();
		}
	}
	
	
	/**
	 * When applied to the AST causes ambiguity nodes to be resolved.
	 */
	protected static final ASTVisitor EMPTY_VISITOR = new ASTVisitor() {
		{ shouldVisitStatements = true; }
	};
	
	
	
  	/**
  	 * Consumes a single identifier token.
  	 */
  	public void consumeIdentifierName() {
  		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
  		
  		astStack.push(createName(parser.getRightIToken()));
  		
  		if(TRACE_AST_STACK) System.out.println(astStack);
  	}
  	
  	
  	/**
	 * block_item ::= declaration | statement 
	 * 
	 * TODO, be careful where exactly in the grammar this is called, it may be called unnecessarily
	 */
	public void consumeStatementDeclarationWithDisambiguation() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclaration decl = (IASTDeclaration) astStack.pop();
		IASTDeclarationStatement declarationStatement = nodeFactory.newDeclarationStatement(decl);
		setOffsetAndLength(declarationStatement);
		
		// attempt to also parse the tokens as an expression
		IASTExpressionStatement expressionStatement = null;
		if(decl instanceof IASTSimpleDeclaration) {
			List<IToken> expressionTokens = parser.getRuleTokens();
			expressionTokens = expressionTokens.subList(0, expressionTokens.size()-1); // remove the semicolon at the end
			
			IParser expressionParser = getExpressionParser();
			IASTExpression expr = (IASTExpression) runSecondaryParser(expressionParser, expressionTokens);
			
			if(expr != null && !(expr instanceof IASTProblemExpression)) { // the parse may fail
				expressionStatement = nodeFactory.newExpressionStatement(expr);
				setOffsetAndLength(expressionStatement);
			}
		}
		
		
		List<IToken> tokens = parser.getRuleTokens();
		
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
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	protected abstract IASTAmbiguousStatement createAmbiguousStatement(IASTStatement ... statements);
	
	
	
	/**
	 * Wrap a declaration in a DeclarationStatement.
	 */
	public void consumeStatementDeclaration() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclaration decl = (IASTDeclaration) astStack.pop();
		IASTDeclarationStatement declarationStatement = nodeFactory.newDeclarationStatement(decl);
		setOffsetAndLength(declarationStatement);
		astStack.push(declarationStatement);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
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
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IToken token = parser.getRightIToken();
		String rep = token.toString();
		
		// Strip the quotes from string literals, this is just to be consistent
		// with the dom parser (i.e. to make a test pass)
//		if(kind == IASTLiteralExpression.lk_string_literal && 
//				rep.startsWith("\"") && rep.endsWith("\"")) {
//			rep = rep.substring(1, rep.length()-1);			
//		}
		
		IASTLiteralExpression expr = nodeFactory.newLiteralExpression(kind, rep);
		setOffsetAndLength(expr, token);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	

	public void consumeExpressionBracketed() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression(IASTUnaryExpression.op_bracketedPrimary, operand);
        setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	

	public void consumeExpressionID() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		//IASTName name = createName(parser.getRightIToken());
		IASTName name = createName(parser.getLeftIToken());
		IASTIdExpression expr = nodeFactory.newIdExpression(name);
        setOffsetAndLength(expr);
        astStack.push(expr);
        
        if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	public void consumeExpressionName() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = (IASTName) astStack.pop();
		IASTIdExpression expr = nodeFactory.newIdExpression(name);
        setOffsetAndLength(expr);
        astStack.push(expr);
        
        if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * expression ::= <openscope-ast> expression_list_actual
	 */
	public void consumeExpressionList() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
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
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '[' expression ']'
	 */
	public void consumeExpressionArraySubscript() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression subscript = (IASTExpression) astStack.pop();
		IASTExpression arrayExpr = (IASTExpression) astStack.pop();
		IASTArraySubscriptExpression expr = nodeFactory.newArraySubscriptExpression(arrayExpr, subscript);
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * postfix_expression ::= postfix_expression '(' expression_list_opt ')'
	 */
	public void consumeExpressionFunctionCall() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression argList = (IASTExpression) astStack.pop(); // may be null
		IASTExpression idExpr  = (IASTExpression) astStack.pop();
		
		IASTFunctionCallExpression expr = nodeFactory.newFunctionCallExpression(idExpr, argList);
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	/**
	 * @param operator constant for {@link ICPPASTCastExpression}
	 */
	public void consumeExpressionCast(int operator) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTCastExpression expr = nodeFactory.newCastExpression(operator, typeId, operand);
		setOffsetAndLength(expr);
		
		IASTNode alternateExpr = null;
		if(operator == IASTCastExpression.op_cast) { // don't reparse for dynamic_cast etc as those are not ambiguous
			// try parsing as non-cast to resolve ambiguities
			IParser secondaryParser = getNoCastExpressionParser();
			alternateExpr = runSecondaryParser(secondaryParser);
		}
		
		if(alternateExpr == null || alternateExpr instanceof IASTProblemExpression)
			astStack.push(expr);
		else {
			IASTNode ambiguityNode = createAmbiguousExpression(expr, (IASTExpression)alternateExpr);
			setOffsetAndLength(ambiguityNode);
			astStack.push(ambiguityNode);
		}

		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	protected abstract IASTAmbiguousExpression createAmbiguousExpression(IASTExpression ... expressions);
	
	
	/**
	 * Lots of rules, no need to list them.
	 * @param operator From IASTUnaryExpression
	 */
	public void consumeExpressionUnaryOperator(int operator) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression operand = (IASTExpression) astStack.pop();
		IASTUnaryExpression expr = nodeFactory.newUnaryExpression(operator, operand);
		setOffsetAndLength(expr);
		astStack.push(expr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * unary_operation ::= 'sizeof' '(' type_name ')'
	 * @see consumeExpressionUnaryOperator For the other use of sizeof
	 */
	public void consumeExpressionTypeId(int operator) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTTypeId typeId = (IASTTypeId) astStack.pop();
		IASTTypeIdExpression expr = nodeFactory.newTypeIdExpression(operator, typeId);
		setOffsetAndLength(expr);
		
		// try parsing as an expression to resolve ambiguities
		IParser secondaryParser = getSizeofExpressionParser(); 
		IASTNode alternateExpr = runSecondaryParser(secondaryParser);
		
		if(alternateExpr == null || alternateExpr instanceof IASTProblemExpression)
			astStack.push(expr);
		else {
			IASTNode ambiguityNode = createAmbiguousExpression(expr, (IASTExpression)alternateExpr);
			setOffsetAndLength(ambiguityNode);
			astStack.push(ambiguityNode);
		}
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * Lots of rules, no need to list them all.
	 * @param op Field from IASTBinaryExpression
	 */
	public void consumeExpressionBinaryOperator(int op) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		IASTBinaryExpression binExpr = nodeFactory.newBinaryExpression(op, expr1, expr2);
		setOffsetAndLength(binExpr);
		astStack.push(binExpr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * conditional_expression ::= logical_OR_expression '?' expression ':' conditional_expression
	 */
	public void consumeExpressionConditional() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr3 = (IASTExpression) astStack.pop();
		IASTExpression expr2 = (IASTExpression) astStack.pop();
		IASTExpression expr1 = (IASTExpression) astStack.pop();
		IASTConditionalExpression condExpr = nodeFactory.newConditionalExpession(expr1, expr2, expr3);
		setOffsetAndLength(condExpr);
		astStack.push(condExpr);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * labeled_statement ::= label_identifier ':' statement
	 * label_identifier ::= identifier 
	 */
	public void consumeStatementLabeled() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTName label = createName(parser.getLeftIToken());

		IASTLabelStatement stat = nodeFactory.newLabelStatement(label, body);
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * labeled_statement ::= 'case' constant_expression ':' statement
	 */
	public void consumeStatementCase() { 
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTExpression expr = (IASTExpression) astStack.pop();
		
		IASTCaseStatement caseStatement = nodeFactory.newCaseStatement(expr);		
		setOffsetAndLength(caseStatement); // TODO this is wrong, need to adjust length to end of colon
		
		IASTCompoundStatement compound = nodeFactory.newCompoundStatement();
		setOffsetAndLength(compound);
		compound.addStatement(caseStatement);
		compound.addStatement(body);
		
		astStack.push(compound);

		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * labeled_statement ::= 'default' ':' <openscope-ast> statement
	 */
	public void consumeStatementDefault() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTStatement body = (IASTStatement) astStack.pop();
		
		IASTDefaultStatement stat = nodeFactory.newDefaultStatement();
		List<IToken> tokens = parser.getRuleTokens();
		IToken defaultToken = tokens.get(0);
		IToken colonToken = tokens.get(1);
		setOffsetAndLength(stat, offset(defaultToken), offset(colonToken) - offset(defaultToken) + 1);
		
		IASTCompoundStatement compound = nodeFactory.newCompoundStatement();
		setOffsetAndLength(compound);
		compound.addStatement(stat);
		compound.addStatement(body);
		
		astStack.push(compound);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * expression_statement ::= ';'
	 */
	public void consumeStatementNull() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTNullStatement stat = nodeFactory.newNullStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * expression_statement ::= expression ';'
	 */
	public void consumeStatementExpression() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr = (IASTExpression) astStack.pop();
		IASTExpressionStatement stat = nodeFactory.newExpressionStatement(expr);
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	/**
	 * compound_statement ::= <openscope> '{' block_item_list '}'
	 * 
	 * block_item_list ::= block_item | block_item_list block_item
	 */
	public void consumeStatementCompoundStatement(boolean hasStatementsInBody) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTCompoundStatement block = nodeFactory.newCompoundStatement();
		
		if(hasStatementsInBody) {
			for(Object o : astStack.closeScope()) {
				block.addStatement((IASTStatement)o);
			}
		}
		
		setOffsetAndLength(block);
		astStack.push(block);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	/**
	 * iteration_statement_matched
	 *     ::= 'do' statement 'while' '(' expression ')' ';'
	 */
	public void consumeStatementDoLoop() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression condition = (IASTExpression) astStack.pop();
		IASTStatement body = (IASTStatement) astStack.pop();
		IASTDoStatement stat = nodeFactory.newDoStatement(body, condition);
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	

	/**
	 * jump_statement ::= goto goto_identifier ';'
	 */
	public void consumeStatementGoto(/*IBinding binding*/) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = createName(parser.getRuleTokens().get(1));
		//name.setBinding(binding);
		IASTGotoStatement gotoStat = nodeFactory.newGotoStatement(name);
		setOffsetAndLength(gotoStat);
		astStack.push(gotoStat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * jump_statement ::= continue ';'
	 */
	public void consumeStatementContinue() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTContinueStatement stat = nodeFactory.newContinueStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * jump_statement ::= break ';'
	 */
	public void consumeStatementBreak() {   
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTBreakStatement stat = nodeFactory.newBreakStatement();
		setOffsetAndLength(stat);
		astStack.push(stat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * jump_statement ::= return ';'
	 * jump_statement ::= return expression ';'
	 */
	public void consumeStatementReturn(boolean hasExpr) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr = hasExpr ? (IASTExpression) astStack.pop() : null;
		IASTReturnStatement returnStat = nodeFactory.newReturnStatement(expr);
		setOffsetAndLength(returnStat);
		astStack.push(returnStat);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

	

	
	/**
	 * type_name ::= specifier_qualifier_list
     *             | specifier_qualifier_list abstract_declarator
	 */
	public void consumeTypeId(boolean hasDeclarator) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclarator declarator;
		if(hasDeclarator)
			declarator = (IASTDeclarator) astStack.pop();
		else {
			declarator = nodeFactory.newDeclarator(nodeFactory.newName());
			setOffsetAndLength(declarator, parser.getRightIToken().getEndOffset(), 0);
		}
			
		IASTDeclSpecifier declSpecifier = (IASTDeclSpecifier) astStack.pop();
		IASTTypeId typeId = nodeFactory.newTypeId(declSpecifier, declarator);
		setOffsetAndLength(typeId);
		astStack.push(typeId);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
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
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclarator decl;
		if(hasDeclarator)
			decl = (IASTDeclarator) astStack.pop();
		else
			decl = nodeFactory.newDeclarator(nodeFactory.newName());
		
		for(Object pointer : astStack.closeScope())
			decl.addPointerOperator((IASTPointerOperator)pointer);
		
		setOffsetAndLength(decl);
		astStack.push(decl);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
    
    /**
     * init_declarator
     *     ::= declarator initializer
     *     
     * @param hasDeclarator in C++ its possible for a parameter declaration to specifiy
     *        a default value without also specifying a named declarator
     */
    public void consumeDeclaratorWithInitializer(boolean hasDeclarator) {
	   	if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
	   	 
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
	   	 
	   	if(TRACE_AST_STACK) System.out.println(astStack);
    }
	
	
    
    /**
	 * parameter_declaration ::= declaration_specifiers declarator
     *                         | declaration_specifiers abstract_declarator
	 */
	public void consumeParameterDeclaration() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclarator declarator  = (IASTDeclarator) astStack.pop();
		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) astStack.pop();
		IASTParameterDeclaration declaration = nodeFactory.newParameterDeclaration(declSpec, declarator);
		setOffsetAndLength(declaration);
		astStack.push(declaration);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * parameter_declaration ::= declaration_specifiers   
	 */
	public void consumeParameterDeclarationWithoutDeclarator() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		// offsets need to be calculated differently in this case		
		final int endOffset = parser.getRightIToken().getEndOffset();
		
		IASTName name = nodeFactory.newName();
		setOffsetAndLength(name, endOffset, 0);
		
		// it appears that a declarator is always required in the AST here
		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
		setOffsetAndLength(declarator, endOffset, 0);
		
		IASTDeclSpecifier declSpec = (IASTDeclSpecifier) astStack.pop();
		IASTParameterDeclaration declaration = nodeFactory.newParameterDeclaration(declSpec, declarator);
		
		setOffsetAndLength(declaration);
		astStack.push(declaration);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
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
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclarator nested = (IASTDeclarator) astStack.pop();
		IASTDeclarator declarator = nodeFactory.newDeclarator(nodeFactory.newName());
		declarator.setNestedDeclarator(nested);
		setOffsetAndLength(declarator);
		astStack.push(declarator);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * direct_declarator ::= declarator_id_name
	 */
	public void consumeDirectDeclaratorIdentifier() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = (IASTName) astStack.pop();
		IASTDeclarator declarator = nodeFactory.newDeclarator(name);
		setOffsetAndLength(declarator);
		astStack.push(declarator);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 *  array_modifier 
	 *      ::= '[' ']' 
     *        | '[' assignment_expression ']'
     */        
	public void consumeDirectDeclaratorArrayModifier(boolean hasAssignmentExpr) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();

		IASTExpression expr = hasAssignmentExpr ? (IASTExpression)astStack.pop() : null;
		IASTArrayModifier arrayModifier = nodeFactory.newArrayModifier(expr);
		setOffsetAndLength(arrayModifier);
		astStack.push(arrayModifier);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * When the identifier part of a declarator is parsed it will put a plain IASTDeclarator on the stack.
	 * When the array modifier part is parsed we will need to throw away the plain
	 * declarator and replace it with an array declarator. If its a multidimensional array then
	 * the additional array modifiers will need to be added to the array declarator.
	 * Special care is taken for nested declarators.
	 */
	protected void addArrayModifier(IASTArrayModifier arrayModifier) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTDeclarator node = (IASTDeclarator) astStack.pop();
		
		// Its a nested declarator so create an new ArrayDeclarator
		if(node.getNestedDeclarator() != null) {  //node.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR) {
			IASTArrayDeclarator declarator = nodeFactory.newArrayDeclarator(nodeFactory.newName());
			IASTDeclarator nested = node;
			declarator.setNestedDeclarator(nested);
			
			int offset = offset(nested);
			int length = endOffset(arrayModifier) - offset;
			setOffsetAndLength(declarator, offset, length);
			
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
			setOffsetAndLength(decl, offset, length);
			
			decl.addArrayModifier(arrayModifier);
			astStack.push(decl);
		}
		
		if(TRACE_AST_STACK) System.out.println(astStack);
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
			setOffsetAndLength(declarator, offset, endOffset - offset);
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
			setOffsetAndLength(declarator, offset, endOffset - offset);
			astStack.push(declarator);
		}

		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
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
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTArrayModifier arrayModifier = (IASTArrayModifier) astStack.pop();
		
		if(hasDeclarator) {
			addArrayModifier(arrayModifier);
		}
		else {
			IASTArrayDeclarator decl = nodeFactory.newArrayDeclarator(nodeFactory.newName());
			decl.addArrayModifier(arrayModifier);
			setOffsetAndLength(decl);
			astStack.push(decl);
			
			if(TRACE_AST_STACK) System.out.println(astStack);
		}
	}
	
	
	
	
	/**
	 * enum_specifier ::= 'enum' '{' <openscope> enumerator_list_opt '}'
     *                  | 'enum' enum_identifier '{' <openscope> enumerator_list_opt '}'
	 */
	public void consumeTypeSpecifierEnumeration(boolean hasIdent) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = (hasIdent) ? createName(parser.getRuleTokens().get(1)) : nodeFactory.newName();
		
		IASTEnumerationSpecifier enumSpec = nodeFactory.newEnumerationSpecifier(name);

		for(Object o : astStack.closeScope())
			enumSpec.addEnumerator((IASTEnumerator)o);

		setOffsetAndLength(enumSpec);
		astStack.push(enumSpec);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	/**
	 * enumerator ::= enum_identifier
     *              | enum_identifier '=' constant_expression
	 */
	public void consumeEnumerator(boolean hasInitializer) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTName name = createName(parser.getLeftIToken());
		
		IASTExpression value = null;
		if(hasInitializer)
			value = (IASTExpression) astStack.pop();
		
		IASTEnumerator enumerator = nodeFactory.newEnumerator(name, value);
		setOffsetAndLength(enumerator);
		astStack.push(enumerator);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
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
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr = (IASTExpression) astStack.pop();
		if(discardInitializer(expr)) { 
			astStack.push(null);
			return;
		}
		
		IASTInitializerExpression initializer = nodeFactory.newInitializerExpression(expr);
		setOffsetAndLength(initializer);
		astStack.push(initializer);
        
        if(TRACE_AST_STACK) System.out.println(astStack);
	}

	
	private boolean discardInitializer(IASTExpression expression) {
		return initializerListNestingLevel > 0
		    && options.contains(IParser.Options.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS)
		    && !ASTQueries.canContainName(expression);
	}
	
	
	
	/**
	 * initializer ::= '{' <openscope> initializer_list '}'
     *               | '{' <openscope> initializer_list ',' '}'
	 */
	public void consumeInitializerList() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTInitializerList list = nodeFactory.newInitializerList();

		for(Object o : astStack.closeScope())
			list.addInitializer((IASTInitializer)o);
		
		setOffsetAndLength(list);
		astStack.push(list);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	
	
	
	
	/**
	 * struct_declarator
     *     ::= ':' constant_expression  
     *       | declarator ':' constant_expression		
	 */
	public void consumeBitField(boolean hasDeclarator) {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		IASTExpression expr = (IASTExpression)astStack.pop();
		
		IASTName name;
		if(hasDeclarator) // it should have been parsed into a regular declarator
			name = ((IASTDeclarator) astStack.pop()).getName();
		else
			name = nodeFactory.newName();
		
		IASTFieldDeclarator fieldDecl = nodeFactory.newFieldDeclarator(name, expr);
		setOffsetAndLength(fieldDecl);
		astStack.push(fieldDecl);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}
	
	

	/**
	 * statement ::= ERROR_TOKEN
	 */
	public void consumeStatementProblem() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		consumeProblem(nodeFactory.newProblemStatement(null));
	}

	/**
	 * assignment_expression ::= ERROR_TOKEN
	 * constant_expression ::= ERROR_TOKEN
	 */
	public void consumeExpressionProblem() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		consumeProblem(nodeFactory.newProblemExpression(null));
	}

	/**
	 * external_declaration ::= ERROR_TOKEN
	 */
	public void consumeDeclarationProblem() {
		if(TRACE_ACTIONS) DebugUtil.printMethodTrace();
		
		consumeProblem(nodeFactory.newProblemDeclaration(null));
	}

	
	private void consumeProblem(IASTProblemHolder problemHolder) {
		IASTProblem problem = nodeFactory.newProblem(IProblem.SYNTAX_ERROR, new char[0], true);
		problemHolder.setProblem(problem);		
		setOffsetAndLength(problem);
		setOffsetAndLength((ASTNode)problemHolder);
		astStack.push(problemHolder);
		
		if(TRACE_AST_STACK) System.out.println(astStack);
	}

}