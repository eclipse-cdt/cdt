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
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.internal.core.parser.token.OffsetDuple;
import org.eclipse.cdt.internal.core.parser.token.TokenDuple;
import org.eclipse.cdt.internal.core.parser.util.TraceUtil;

/**
 * @author jcamelon
 */
public class SelectionParser extends ContextualParser {

	private OffsetDuple offsetRange;
	private IToken firstTokenOfDuple = null, lastTokenOfDuple = null;
	private IASTScope ourScope = null;
	private IASTCompletionNode.CompletionKind ourKind = null;
	private IASTNode ourContext = null;
	private ITokenDuple greaterContextDuple = null;
	private boolean pastPointOfSelection = false;

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleNewToken(org.eclipse.cdt.core.parser.IToken)
	 */
	protected void handleNewToken(IToken value) {
		if( value != null && scanner.isOnTopContext() )
		{
			TraceUtil.outputTrace(log, "IToken provided w/offsets ", null, value.getOffset(), " & ", value.getEndOffset() ); //$NON-NLS-1$ //$NON-NLS-2$
			if( value.getOffset() == offsetRange.getFloorOffset() )
			{
				TraceUtil.outputTrace(log, "Offset Floor Hit w/token \"", null, value.getImage(), "\"", null ); //$NON-NLS-1$ //$NON-NLS-2$
				firstTokenOfDuple = value;
			}
			if( value.getEndOffset() == offsetRange.getCeilingOffset() )
			{
				TraceUtil.outputTrace(log, "Offset Ceiling Hit w/token \"", null, value.getImage(), "\"", null ); //$NON-NLS-1$ //$NON-NLS-2$
				lastTokenOfDuple = value;
			}
			if( lastTokenOfDuple != null && lastTokenOfDuple.getEndOffset() >= offsetRange.getCeilingOffset() )
			{
				if ( ourScope == null )
					ourScope = getCompletionScope();
				if( ourContext == null )
					ourContext = getCompletionContext();
				if( ourKind == null )
					ourKind = getCompletionKind();
			}
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
		
		if( ! duple.syntaxOfName() )
			throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
		
		return provideSelectionNode(duple);
	}

	/**
	 * @param duple
	 * @return
	 */
	protected IASTNode provideSelectionNode(ITokenDuple duple) {
		
		ITokenDuple finalDuple = null;
		// reconcile the name to look up first
		if( ! duple.equals( greaterContextDuple ))
		{
			// 3 cases
			
			// duple is prefix of greaterContextDuple
			if( duple.getFirstToken().equals( greaterContextDuple.getFirstToken() ))
			{
				//	=> do not use greaterContextDuple
				finalDuple = duple;
			}
			// duple is suffix of greaterContextDuple
			else if( duple.getLastToken().equals( greaterContextDuple.getLastToken() ))
			{
				//  => use greaterContextDuple
				finalDuple = greaterContextDuple;
			}
			// duple is a sub-duple of greaterContextDuple
			else
			{
				//	=> throw ParseError
				throw new ParseError( ParseError.ParseErrorKind.OFFSET_RANGE_NOT_NAME );
			}
		}
		else
			finalDuple = greaterContextDuple;
		return finalDuple.lookup( astFactory, ourScope );
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setGreaterNameContext(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	protected void setGreaterNameContext(ITokenDuple tokenDuple) {
		
		if( pastPointOfSelection ) return;
		if( greaterContextDuple == null && scanner.isOnTopContext() && lastTokenOfDuple != null && firstTokenOfDuple != null )
		{
			if( tokenDuple.getStartOffset() > lastTokenOfDuple.getEndOffset() )
			{
				pastPointOfSelection = true;
				return;
			}
			int tokensFound = 0;
			Iterator i = tokenDuple.iterator();
			while( i.hasNext() )
			{
				IToken token = (IToken) i.next();
				if( token == firstTokenOfDuple ) ++tokensFound;
				if( token == lastTokenOfDuple ) ++tokensFound;
			}
			if( tokensFound == 2 )
				greaterContextDuple = tokenDuple;
		}
	}
}
