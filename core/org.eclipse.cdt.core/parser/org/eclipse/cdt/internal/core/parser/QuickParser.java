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
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 */
public class QuickParser extends Parser {

	/**
	 * @param scanner
	 * @param callback
	 * @param mode
	 * @param language
	 * @param log
	 */
	public QuickParser(IScanner scanner, ISourceElementRequestor callback, ParserLanguage language, IParserLogService log) {
		super(scanner, callback, language, log);
		astFactory = ParserFactory.createASTFactory( ParserMode.QUICK_PARSE, language);
		scanner.setASTFactory(astFactory);
	}

	protected void handleFunctionBody(IASTScope scope, boolean isInlineFunction) throws BacktrackException, EndOfFileException
	{
		skipOverCompoundStatement();
	}

	protected void catchBlockCompoundStatement(IASTScope scope) throws BacktrackException, EndOfFileException 
	{
		skipOverCompoundStatement();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int)
	 */
	public IASTCompletionNode parse(int offset) throws ParserNotImplementedException {
		throw new ParserNotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParser#parse(int, int)
	 */
	public IASTNode parse(int startingOffset, int endingOffset) throws ParserNotImplementedException {
		throw new ParserNotImplementedException();
	}
	
	
}
