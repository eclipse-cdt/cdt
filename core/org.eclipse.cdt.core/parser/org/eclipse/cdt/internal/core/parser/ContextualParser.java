/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.Set;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTExpression.Kind;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;

/**
 * @author jcamelon
 */
public class ContextualParser extends CompleteParser {

	/**
	 * @param scanner
	 * @param callback
	 * @param language
	 * @param log
	 */
	public ContextualParser(
		IScanner scanner,
		ISourceElementRequestor callback,
		ParserLanguage language,
		IParserLogService log, IParserExtension extension ) {
		super(scanner, callback, language, log, extension );
	}

	protected IASTScope contextualScope;

	/**
	 * @return
	 */
	protected IASTScope getCompletionScope() {
		return contextualScope;
	}

	protected CompletionKind completionKind;

	/**
	 * @return
	 */
	protected IASTCompletionNode.CompletionKind getCompletionKind() {
		return completionKind;
	}

	protected IASTNode context;
	protected IToken finalToken;
	protected Set keywordSet;
	protected String functionOrConstructorName = "";//$NON-NLS-1$

	/**
	 * @return
	 */
	protected String getCompletionPrefix() {
		return ( finalToken == null ? EMPTY_STRING : finalToken.getImage() ); 
	}

	/**
	 * @return
	 */
	protected IASTNode getCompletionContext() {
		return context;
	}

	protected void setCompletionContext(IASTNode node) {
		this.context = node;
	}

	protected void setCompletionKind(IASTCompletionNode.CompletionKind kind) {
		this.completionKind = kind;
	}

	/**
	 * @param compilationUnit
	 * @param kind2
	 * @param set
	 * @param object
	 * @param string
	 */
	protected void setCompletionValues(CompletionKind kind, Set keywordSet, String prefix) {
		setCompletionScope(compilationUnit);
		this.keywordSet = keywordSet;
		setCompletionKind(kind);
		setCompletionContext(null);
		setCompletionFunctionName( );
		setCompletionToken( TokenFactory.createStandAloneToken( IToken.tIDENTIFIER, prefix ) );
	}

	/**
	 */
	protected void setCompletionFunctionName() {
		functionOrConstructorName = currentFunctionName;
	}
	
	

	protected void setCompletionKeywords(KeywordSetKey key) {
		this.keywordSet = KeywordSets.getKeywords( key, language );
	}

	protected void setCompletionToken(IToken token) {
		finalToken = token;
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTNode node, String prefix) throws EndOfFileException {
		setCompletionToken( TokenFactory.createStandAloneToken( IToken.tIDENTIFIER, prefix ) );
		setCompletionValues(scope, kind, key, node );
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key) throws EndOfFileException {
		setCompletionValues(scope, kind, key, null );
	}

	
	
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTNode node) throws EndOfFileException {
		setCompletionScope(scope);
		setCompletionKeywords(key);
		setCompletionKind(kind);
		setCompletionContext(node);
		setCompletionFunctionName( );
		checkEndOfFile();
	}

	
	
	protected void setCompletionValues( IASTScope scope, CompletionKind kind, IToken first, IToken last, KeywordSetKey key ) throws EndOfFileException{		
		setCompletionScope( scope );
		setCompletionKind( kind );
		setCompletionKeywords(key);
		ITokenDuple duple = TokenFactory.createTokenDuple( first, last );
		try {
			setCompletionContext( astFactory.lookupSymbolInContext( scope, duple, null ) );
		} catch (ASTNotImplementedException e) {
		}
		setCompletionFunctionName();
	}

	private String currentFunctionName = EMPTY_STRING;
	protected IASTExpression parameterListExpression;
	
	
	
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, KeywordSetKey key, IASTExpression firstExpression, Kind expressionKind) throws EndOfFileException {
		IASTNode node = astFactory.expressionToMostPreciseASTNode( scope, firstExpression );
		if( kind == CompletionKind.MEMBER_REFERENCE )
		{
			if( ! validMemberOperation( node, expressionKind ))
				node =null;
		}
		setCompletionValues(scope,kind,key, node  );
	}

	/**
	 * @param node
	 * @param expressionKind
	 * @return
	 */
	private boolean validMemberOperation(IASTNode node, Kind expressionKind) {
		if( expressionKind == Kind.POSTFIX_ARROW_IDEXPRESSION || expressionKind == Kind.POSTFIX_ARROW_TEMPL_IDEXP )
			return astFactory.validateIndirectMemberOperation( node );
		else if( expressionKind == Kind.POSTFIX_DOT_IDEXPRESSION || expressionKind == Kind.POSTFIX_DOT_TEMPL_IDEXPRESS )
			return astFactory.validateDirectMemberOperation( node );
		return false;
	}	

	protected void setCompletionScope(IASTScope scope) {
		this.contextualScope = scope;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCompletionValues(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind)
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind) throws EndOfFileException {
		setCompletionScope(scope);
		setCompletionKind(kind);
		setCompletionFunctionName( );
		checkEndOfFile();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCompletionValues(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind, org.eclipse.cdt.core.parser.ast.IASTNode)
	 */
	protected void setCompletionValues(IASTScope scope, CompletionKind kind,
			IASTNode context) throws EndOfFileException {
		setCompletionScope(scope);
		setCompletionKind(kind);
		setCompletionContext(context);
		setCompletionFunctionName( );
		checkEndOfFile();	}

	/**
	 * @return
	 */
	protected String getCompletionFunctionName() {
		return functionOrConstructorName;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCurrentFunctionName(java.lang.String)
	 */
	protected void setCurrentFunctionName(String functionName) {
		currentFunctionName = functionName;
	}
	
	protected void handleFunctionBody(IASTScope scope) throws BacktrackException, EndOfFileException
	{
		if( scanner.isOnTopContext() )
			functionBody(scope);
		else
			skipOverCompoundStatement();
	}
	
	protected void catchBlockCompoundStatement(IASTScope scope) throws BacktrackException, EndOfFileException 
	{
		if( scanner.isOnTopContext() )
			compoundStatement(scope, true);
		else
			skipOverCompoundStatement();
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setCompletionValuesNoContext(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind, org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key)
	 */
	protected void setCompletionValuesNoContext(IASTScope scope,
			CompletionKind kind, KeywordSetKey key) throws EndOfFileException {
		setCompletionScope(scope);
		setCompletionKeywords(key);
		setCompletionKind(kind);
		checkEndOfFile();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setParameterListExpression(org.eclipse.cdt.core.parser.ast.IASTExpression)
	 */
	protected void setParameterListExpression(
			IASTExpression assignmentExpression) {
		parameterListExpression = assignmentExpression;
	}
	/**
	 * @return Returns the parameterListExpression.
	 */
	public final IASTExpression getParameterListExpression() {
		return parameterListExpression;
	}
}
