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
import org.eclipse.cdt.core.parser.KeywordSetKey;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCodeScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.ast.ASTCompletionNode;

/**
 * @author jcamelon
 */
public class CompletionParser extends ContextualParser implements IParser {

	/**
	 * @param scanner
	 * @param callback
	 * @param language
	 * @param log
	 */
	public CompletionParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log, IParserExtension extension ) {
		super(scanner, callback, language, log, extension );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) {
		scanner.setOffsetBoundary(offset);
		//long startTime = System.currentTimeMillis();
		translationUnit();
		//long stopTime = System.currentTimeMillis();
		//System.out.println("Completion Parse time: " + (stopTime - startTime) + "ms");
		return new ASTCompletionNode( getCompletionKind(), getCompletionScope(), getCompletionContext(), getCompletionPrefix(), reconcileKeywords( getKeywordSet(), getCompletionPrefix() ), getCompletionFunctionName(), getParameterListExpression() );
	}

	/**
	 * @param set
	 * @param string
	 * @return
	 */
	private Set reconcileKeywords(Set keywords, String prefix) {
		if( keywords == null ) return null;
		if( prefix.equals( "")) return keywords; //$NON-NLS-1$
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleOffsetLimitException()
	 */
	protected void handleOffsetLimitException(OffsetLimitReachedException exception) throws OffsetLimitReachedException {
		if( exception.getCompletionNode() == null )
		{	
			setCompletionToken( exception.getFinalToken() );
			if( (finalToken!= null )&& (!finalToken.canBeAPrefix() ))
				setCompletionToken(null);
		}
		else
		{
			ASTCompletionNode node = (ASTCompletionNode) exception.getCompletionNode();
			setCompletionValues( node.getCompletionKind(), node.getKeywordSet(), node.getCompletionPrefix() );
		}
	
		throw exception;
	}	

	protected IToken getCompletionToken()
	{ 
		return finalToken;
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

	protected void catchHandlerSequence(IASTScope scope)
	throws EndOfFileException, BacktrackException {
		if( LT(1) != IToken.t_catch )
		{
			IToken la = LA(1);
			throwBacktrack(la.getOffset(), la.getEndOffset(), la.getLineNumber(), la.getFilename()); // error, need at least one of these
		}
		while (LT(1) == IToken.t_catch)
		{
			consume(IToken.t_catch);
			setCompletionValues(scope,CompletionKind.NO_SUCH_KIND,KeywordSetKey.EMPTY );
			consume(IToken.tLPAREN);
			setCompletionValues(scope,CompletionKind.EXCEPTION_REFERENCE,KeywordSetKey.DECL_SPECIFIER_SEQUENCE);
			if( LT(1) == IToken.tELLIPSIS )
				consume( IToken.tELLIPSIS );
			else 
				simpleDeclarationStrategyUnion( scope, null, CompletionKind.EXCEPTION_REFERENCE, KeywordSetKey.DECLARATION); // was exceptionDeclaration
			consume(IToken.tRPAREN);
			
			catchBlockCompoundStatement(scope);
		}
	}

	public ISelectionParseResult parse(int startingOffset, int endingOffset) {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}

	public boolean parse() {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );	
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setupASTFactory(org.eclipse.cdt.core.parser.IScanner, org.eclipse.cdt.core.parser.ParserLanguage)
	 */
	protected void setupASTFactory(IScanner scanner, ParserLanguage language) {
		astFactory = ParserFactory.createASTFactory( ParserMode.COMPLETION_PARSE, language);
		scanner.setASTFactory(astFactory);
		astFactory.setLogger(log);
	}

}
