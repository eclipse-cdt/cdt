/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.internal.core.parser.ast.ASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets;
import org.eclipse.cdt.internal.core.parser.token.Token;
import org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key;

/**
 * @author jcamelon
 */
public class ContextualParser extends Parser implements IParser {

	protected CompletionKind kind;
	protected IASTScope scope;
	protected IASTNode context;
	protected IToken finalToken;
	private Set keywordSet;

	/**
	 * @param scanner
	 * @param callback
	 * @param language
	 * @param log
	 */
	public ContextualParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log) {
		super(scanner, callback, language, log);
		astFactory = ParserFactory.createASTFactory( ParserMode.COMPLETION_PARSE, language);
		scanner.setASTFactory(astFactory);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) {
		scanner.setOffsetBoundary(offset);
		translationUnit();
		return new ASTCompletionNode( getCompletionKind(), getCompletionScope(), getCompletionContext(), getCompletionPrefix(), reconcileKeywords( getKeywordSet(), getCompletionPrefix() ) );
	}

	/**
	 * @param set
	 * @param string
	 * @return
	 */
	private Set reconcileKeywords(Set keywords, String prefix) {
		Set resultSet = new TreeSet(); 
		Iterator i = keywords.iterator(); 
		while( i.hasNext() )
		{
			String value = (String) i.next();
			if( value.startsWith( prefix ) ) 
				resultSet.add( value );
			else if( value.compareTo( prefix ) > 0 ) 
				break;
		}
		return resultSet;
	}

	/**
	 * @return
	 */
	private Set getKeywordSet() {
		return keywordSet;
	}

	/**
	 * @return
	 */
	private String getCompletionPrefix() {
		return ( finalToken == null ? "" : finalToken.getImage() );
	}

	/**
	 * @return
	 */
	private IASTNode getCompletionContext() {
		return context;
	}

	/**
	 * @return
	 */
	private IASTScope getCompletionScope() {
		return scope;
	}

	/**
	 * @return
	 */
	protected IASTCompletionNode.CompletionKind getCompletionKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public IASTNode parse(int startingOffset, int endingOffset) {
		scanner.setOffsetBoundary(endingOffset);
		translationUnit();
		return getCompletionContext();
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCurrentScope(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	protected void setCompletionScope(IASTScope scope) {
		this.scope = scope;
	}
	
	protected void setCompletionContext( IASTNode node )
	{
		this.context = node;
	}
	
	protected void setCompletionKind( IASTCompletionNode.CompletionKind kind )
	{
		this.kind = kind;
	}    
	
	
	protected void handleFunctionBody(IASTScope scope, boolean isInlineFunction) throws BacktrackException, EndOfFileException
	{
		if ( isInlineFunction ) 
			skipOverCompoundStatement();
		else
			functionBody(scope);
	}
	
	protected void catchBlockCompoundStatement(IASTScope scope) throws BacktrackException, EndOfFileException 
	{
		compoundStatement(scope, true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleOffsetLimitException()
	 */
	protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws OffsetLimitReachedException {
		setCompletionToken( exception.getFinalToken() );
		if( (finalToken!= null )&& (finalToken.isKeywordOrOperator() ))
			setCompletionToken(null);
	
		throw exception;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCompletionKeywords(java.lang.String[])
	 */
	protected void setCompletionKeywords(KeywordSets.Key key) {
		this.keywordSet = KeywordSets.getKeywords( key, language );
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCompletionToken(org.eclipse.cdt.core.parser.IToken)
	 */
	protected void setCompletionToken(IToken token) {
		finalToken = token;
	}

	protected IToken getCompletionToken()
	{ 
		return finalToken;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCompletionContextForExpression(org.eclipse.cdt.core.parser.ast.IASTExpression, boolean)
	 */
	protected IASTNode getCompletionContextForExpression(
		IASTExpression firstExpression,
		boolean isTemplate) {
		return astFactory.getCompletionContext( (isTemplate
				? IASTExpression.Kind.POSTFIX_DOT_TEMPL_IDEXPRESS
				: IASTExpression.Kind.POSTFIX_DOT_IDEXPRESSION), 
				firstExpression ) ;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCompletionValues(org.eclipse.cdt.core.parser.ast.IASTNode, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind, org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key, java.lang.String)
	 */
	protected void setCompletionValues(
		IASTScope scope,
		CompletionKind kind,
		Key key,
		IASTNode node, String prefix) throws EndOfFileException {
		setCompletionValues(scope, kind, key, node );
		setCompletionToken( new Token( IToken.tIDENTIFIER, prefix ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCompletionValues(org.eclipse.cdt.core.parser.ast.IASTNode, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind, org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key)
	 */
	protected void setCompletionValues(
		IASTScope scope,
		CompletionKind kind,
		Key key ) throws EndOfFileException {
		setCompletionValues(scope, kind, key, null );
	}
	
	protected void setCompletionValues(
			IASTScope scope,
			CompletionKind kind,
			Key key,
			IASTNode node ) throws EndOfFileException
	{
		setCompletionScope(scope);
		setCompletionKeywords(key);
		setCompletionKind(kind);
		setCompletionContext(node);
		checkEndOfFile();
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#getCompletionKindForDeclaration(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	protected CompletionKind getCompletionKindForDeclaration(IASTScope scope, CompletionKind overide) {
		IASTCompletionNode.CompletionKind kind = null;
		if( overide != null )
			kind = overide;
		else if( scope instanceof IASTClassSpecifier )
			kind = CompletionKind.FIELD_TYPE;
		else if (scope instanceof IASTCodeScope)
//			kind = CompletionKind.STATEMENT_START;
			kind = CompletionKind.SINGLE_NAME_REFERENCE;
		else
			kind = CompletionKind.VARIABLE_TYPE;
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCompletionValues(org.eclipse.cdt.core.parser.ast.IASTScope, org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind, org.eclipse.cdt.internal.core.parser.token.KeywordSets.Key, org.eclipse.cdt.core.parser.ast.IASTExpression, boolean)
	 */
	protected void setCompletionValues(
		IASTScope scope,
		CompletionKind kind,
		Key key,
		IASTExpression firstExpression,
		boolean isTemplate) throws EndOfFileException {
		setCompletionValues(scope,kind,key, getCompletionContextForExpression(firstExpression,isTemplate) );
	}

}
