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
import org.eclipse.cdt.core.parser.ParseError;
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
public class CompletionParser extends CompleteParser implements IParser {

	protected CompletionKind kind;
	protected IASTNode context;
	protected IToken finalToken;
	private Set keywordSet;
	protected IASTScope scope;
	
	
	/**
	 * @param scanner
	 * @param callback
	 * @param language
	 * @param log
	 */
	public CompletionParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log) {
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
		if( keywords == null ) return null;
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
	protected Set getKeywordSet() {
		return keywordSet;
	}

	/**
	 * @return
	 */
	protected String getCompletionPrefix() {
		return ( finalToken == null ? "" : finalToken.getImage() );
	}

	/**
	 * @return
	 */
	protected IASTNode getCompletionContext() {
		return context;
	}

	/**
	 * @return
	 */
	protected IASTCompletionNode.CompletionKind getCompletionKind() {
		return kind;
	}

	protected void setCompletionContext( IASTNode node )
	{
		this.context = node;
	}
	
	protected void setCompletionKind( IASTCompletionNode.CompletionKind kind )
	{
		this.kind = kind;
	}    
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleOffsetLimitException()
	 */
	protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws OffsetLimitReachedException {
		if( exception.getCompletionNode() == null )
		{	
			setCompletionToken( exception.getFinalToken() );
			if( (finalToken!= null )&& (finalToken.isKeywordOrOperator() ))
				setCompletionToken(null);
		}
		else
		{
			ASTCompletionNode node = (ASTCompletionNode) exception.getCompletionNode();
			setCompletionValues( node.getCompletionKind(), node.getKeywordSet(), node.getCompletionPrefix() );
		}
	
		throw exception;
	}	

	/**
	 * @param compilationUnit
	 * @param kind2
	 * @param set
	 * @param object
	 * @param string
	 */
	protected void setCompletionValues(CompletionKind kind, Set keywordSet, String prefix ) {
		setCompletionScope(compilationUnit);
		this.keywordSet = keywordSet;
		setCompletionKind(kind);
		setCompletionContext(null);
		setCompletionToken( new Token( IToken.tIDENTIFIER, prefix ) );
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
		setCompletionToken( new Token( IToken.tIDENTIFIER, prefix ) );
		setCompletionValues(scope, kind, key, node );
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

	protected void catchHandlerSequence(IASTScope scope)
	throws EndOfFileException, BacktrackException {
		if( LT(1) != IToken.t_catch )
			throw backtrack; // error, need at least one of these
		while (LT(1) == IToken.t_catch)
		{
			consume(IToken.t_catch);
			setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,Key.EMPTY);
			consume(IToken.tLPAREN);
			setCompletionValues(scope,CompletionKind.EXCEPTION_REFERENCE,Key.DECL_SPECIFIER_SEQUENCE );
			if( LT(1) == IToken.tELLIPSIS )
				consume( IToken.tELLIPSIS );
			else 
				declaration(scope, null, CompletionKind.EXCEPTION_REFERENCE); // was exceptionDeclaration
			consume(IToken.tRPAREN);
			
			catchBlockCompoundStatement(scope);
		}
	}

	/**
	 * @return
	 */
	protected IASTScope getCompletionScope() {
		return scope;
	}

	public IASTNode parse(int startingOffset, int endingOffset) {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}

	protected void setCompletionScope(IASTScope scope) {
		this.scope = scope;
	}

	public boolean parse() {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );	
	}

}
