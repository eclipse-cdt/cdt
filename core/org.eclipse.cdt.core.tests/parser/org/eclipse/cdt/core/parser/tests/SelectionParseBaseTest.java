/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.parser.tests;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTNode;

/**
 * @author johnc
 *
 */
public class SelectionParseBaseTest extends CompleteParseBaseTest {

	protected IASTNode parse(String code, int offset1, int offset2) throws Exception {
		return parse( code, offset1, offset2, true );
	}

	/**
	 * @param code
	 * @param offset1
	 * @param offset2
	 * @param b
	 * @return
	 */
	protected IASTNode parse(String code, int offset1, int offset2, boolean expectedToPass) throws Exception {
		callback = new FullParseCallback();
		IParser parser = null;
	
		parser =
			ParserFactory.createParser(
					ParserFactory.createScanner(
							new CodeReader(code.toCharArray()),
							new ScannerInfo(),
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							callback,
							new NullLogService(), null),
							callback,
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							ParserFactory.createDefaultLogService());
		
		IParser.ISelectionParseResult result =parser.parse( offset1, offset2 );
		if( expectedToPass )
		{
			assertNotNull( result );
			String filename = result.getFilename();
			assertTrue( !filename.equals( "")); //$NON-NLS-1$
			return (IASTNode) result.getOffsetableNamedElement();
		}
		return null;
	}
}
