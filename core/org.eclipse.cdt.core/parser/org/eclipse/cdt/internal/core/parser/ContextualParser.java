/*******************************************************************************
 * Copyright (c) 2000 - 2004 IBM Corporation and others.
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
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.TokenDuple;
import org.eclipse.cdt.internal.core.parser.token.TokenFactory;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key;

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

	protected IASTScope scope;

	/**
	 * @return
	 */
	protected IASTScope getCompletionScope() {
		return scope;
	}

	protected CompletionKind kind;

	/**
	 * @return
	 */
	protected IASTCompletionNode.CompletionKind getCompletionKind() {
		return kind;
	}

	protected IASTNode context;
	protected IToken finalToken;
	protected Set keywordSet;
	protected String functionOrConstructorName = "";

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
		this.kind = kind;
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
		setCompletionToken( TokenFactory.createToken( IToken.tIDENTIFIER, prefix ) );
	}

	/**
	 */
	protected void setCompletionFunctionName() {
		functionOrConstructorName = currentFunctionName;
	}

	protected void setCompletionKeywords(KeywordSets.Key key) {
		this.keywordSet = KeywordSets.getKeywords( key, language );
	}

	protected void setCompletionToken(IToken token) {
		finalToken = token;
	}

	protected IASTNode getCompletionContextForExpression(IASTExpression firstExpression, boolean isTemplate) {
		return astFactory.getCompletionContext( (isTemplate
				? IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS
				: IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION), 
				firstExpression ) ;
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, Key key, IASTNode node, String prefix) throws EndOfFileException {
		setCompletionToken( TokenFactory.createToken( IToken.tIDENTIFIER, prefix ) );
		setCompletionValues(scope, kind, key, node );
	}

	protected void setCompletionValues(IASTScope scope, CompletionKind kind, Key key) throws EndOfFileException {
		setCompletionValues(scope, kind, key, null );
	}

	
	
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, Key key, IASTNode node) throws EndOfFileException {
		setCompletionScope(scope);
		setCompletionKeywords(key);
		setCompletionKind(kind);
		setCompletionContext(node);
		setCompletionFunctionName( );
		checkEndOfFile();
	}

	
	
	protected void setCompletionValues( IASTScope scope, CompletionKind kind, IToken first, IToken last ) throws EndOfFileException{		
		setCompletionScope( scope );
		setCompletionKind( kind );
		setCompletionKeywords( Key.EMPTY );
		ITokenDuple duple = new TokenDuple( first, last );
		try {
			setCompletionContext( astFactory.lookupSymbolInContext( scope, duple ) );
		} catch (ASTNotImplementedException e) {
		}
		setCompletionFunctionName();
	}

	private String currentFunctionName = EMPTY_STRING;
	
	
	
	protected void setCompletionValues(IASTScope scope, CompletionKind kind, Key key, IASTExpression firstExpression, boolean isTemplate) throws EndOfFileException {
		setCompletionValues(scope,kind,key, getCompletionContextForExpression(firstExpression,isTemplate)  );
	}

	protected void setCompletionScope(IASTScope scope) {
		this.scope = scope;
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
}
