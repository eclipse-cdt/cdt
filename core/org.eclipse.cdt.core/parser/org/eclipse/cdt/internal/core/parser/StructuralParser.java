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
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParseError.ParseErrorKind;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 */
public class StructuralParser extends Parser implements IParser {

	/**
	 * @param scanner
	 * @param ourCallback
	 * @param language
	 * @param logService
	 */
	public StructuralParser(IScanner scanner, ISourceElementRequestor ourCallback, ParserLanguage language, IParserLogService logService) {
		super(scanner, ourCallback, language, logService);
		setupASTFactory(scanner, language );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#handleFunctionBody(org.eclipse.cdt.core.parser.ast.IASTScope, boolean)
	 */
	protected void handleFunctionBody(
		IASTScope scope)
		throws BacktrackException, EndOfFileException {
		skipOverCompoundStatement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.Parser#catchBlockCompoundStatement(org.eclipse.cdt.core.parser.ast.IASTScope)
	 */
	protected void catchBlockCompoundStatement(IASTScope scope)
		throws BacktrackException, EndOfFileException {
		skipOverCompoundStatement();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParseError {
		throw new ParseError( ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public IASTNode parse(int startingOffset, int endingOffset) throws ParseError {
		throw new ParseError( ParseErrorKind.METHOD_NOT_IMPLEMENTED );
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ExpressionParser#setupASTFactory(org.eclipse.cdt.core.parser.IScanner, org.eclipse.cdt.core.parser.ParserLanguage)
	 */
	protected void setupASTFactory(IScanner scanner, ParserLanguage language) {
		astFactory = ParserFactory.createASTFactory( this, ParserMode.COMPLETE_PARSE, language);
		scanner.setASTFactory(astFactory);
		astFactory.setLogger(log);
	}

}
