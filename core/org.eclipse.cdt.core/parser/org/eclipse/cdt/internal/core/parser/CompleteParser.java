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

import org.eclipse.cdt.core.parser.BacktrackException;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;
import org.eclipse.cdt.core.parser.extension.IParserExtension;
import org.eclipse.cdt.internal.core.parser.ast.complete.CompleteParseASTFactory;

/**
 * @author jcamelon
 */
public class CompleteParser extends Parser {

	/**
	 * @param scanner
	 * @param callback
	 * @param mode
	 * @param language
	 * @param log
	 */
	public CompleteParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log, IParserExtension extension ) {
		super(scanner, callback, language, log, extension );
	}
	
	protected void handleFunctionBody(IASTScope scope) throws BacktrackException, EndOfFileException
	{
		functionBody(scope);
	}
	
	protected void catchBlockCompoundStatement(IASTScope scope) throws BacktrackException, EndOfFileException 
	{
		compoundStatement(scope, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParseError {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public ISelectionParseResult parse(int startingOffset, int endingOffset) throws ParseError {
		throw new ParseError( ParseError.ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setupASTFactory(org.eclipse.cdt.core.parser.IScanner, org.eclipse.cdt.core.parser.ParserLanguage)
	 */
	protected void setupASTFactory(IScanner scanner, ParserLanguage language) {
		astFactory = ParserFactory.createASTFactory(  ParserMode.COMPLETE_PARSE, language);
		scanner.setASTFactory(astFactory);
		astFactory.setLogger(log);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#validateCaches()
	 */
	public boolean validateCaches() {
		if( super.validateCaches() && astFactory instanceof CompleteParseASTFactory)
			return ((CompleteParseASTFactory)astFactory).validateCaches();
		return false;
	}
	
}
