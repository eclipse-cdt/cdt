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

import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.internal.core.parser.token.OffsetDuple;
import org.eclipse.cdt.internal.core.parser.token.TokenDuple;

/**
 * @author jcamelon
 */
public class SelectionParser extends ContextualParser {

	private OffsetDuple offsetRange;
	private IToken firstTokenOfDuple = null, lastTokenOfDuple = null;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleNewToken(org.eclipse.cdt.core.parser.IToken)
	 */
	protected void handleNewToken(IToken value) {
		if( value != null )
		{
			if( value.getOffset() == offsetRange.getFloorOffset() )
				firstTokenOfDuple = value;
			if( value.getEndOffset() == offsetRange.getCeilingOffset() )
				lastTokenOfDuple = value;
		}
	}

	
	/**
	 * @param scanner
	 * @param callback
	 * @param mode
	 * @param language
	 * @param log
	 */
	public SelectionParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log) {
		super(scanner, callback, language, log);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public IASTNode parse(int startingOffset, int endingOffset) {
//		scanner.setOffsetBoundary(endingOffset);
		offsetRange = new OffsetDuple( startingOffset, endingOffset );
		translationUnit();
		return reconcileTokenDuple();
	}

	/**
	 * 
	 */
	protected IASTNode reconcileTokenDuple() throws ParseError {
		if( firstTokenOfDuple == null || lastTokenOfDuple == null )
			throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		
		if( getCompletionKind() == IASTCompletionNode.CompletionKind.UNREACHABLE_CODE )
			throw new ParseError( ParseError.ParseErrorKind.OFFSETDUPLE_UNREACHABLE );
		
		ITokenDuple duple = new TokenDuple( firstTokenOfDuple, lastTokenOfDuple );
		return duple.lookup( astFactory, getCompletionScope() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );	
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#checkEndOfFile()
	 */
	protected void checkEndOfFile() throws EndOfFileException {
		if( lastToken != null && lastToken.getEndOffset() >= offsetRange.getCeilingOffset() )
			throw new EndOfFileException();
	}

}
