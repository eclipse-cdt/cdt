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
package org.eclipse.cdt.core.parser;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.ast.*;

/**
 * @author jcamelon
 */
public class ContextualParser extends Parser implements IParser {

	protected CompletionKind kind;
	protected IASTScope scope;
	protected IASTNode context;
	protected IToken finalToken;
	private Set keywordSet = new HashSet();

	/**
	 * @param scanner
	 * @param callback
	 * @param language
	 * @param log
	 */
	public ContextualParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log) {
		super(scanner, callback, language, log);
		astFactory = ParserFactory.createASTFactory( ParserMode.COMPLETE_PARSE, language);
		scanner.setASTFactory(astFactory);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParserNotImplementedException {
		scanner.setOffsetBoundary(offset);
		translationUnit();
		return new ASTCompletionNode( getCompletionKind(), getCompletionScope(), getCompletionContext(), getCompletionPrefix(), getKeywordSet() );
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
	private IASTCompletionNode.CompletionKind getCompletionKind() {
		return kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public IASTNode parse(int startingOffset, int endingOffset) throws ParserNotImplementedException {
		scanner.setOffsetBoundary(endingOffset);
		translationUnit();
		return getCompletionContext();
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#setCurrentScope(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	protected void setCurrentScope(IASTScope scope) {
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
	protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws EndOfFileException, OffsetLimitReachedException {
		finalToken = exception.getFinalToken(); 
		throw exception;
	}	

}
