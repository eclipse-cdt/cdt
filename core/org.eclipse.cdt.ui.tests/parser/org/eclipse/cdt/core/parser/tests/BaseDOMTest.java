/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 *
 */
public class BaseDOMTest extends TestCase {

	public BaseDOMTest( String arg )
	{
		super( arg );
	}
	
	public TranslationUnit parse( String code ) throws Exception
	{
		return parse( code, true, true );
	}
	
	public TranslationUnit parse(String code, boolean quickParse, boolean throwOnError ) throws Exception {
		DOMBuilder domBuilder = new DOMBuilder(); 
		ParserMode mode = quickParse ? ParserMode.QUICK_PARSE : ParserMode.COMPLETE_PARSE; 
		IParser parser = ParserFactory.createParser(ParserFactory.createScanner( new StringReader( code ), null, null, null, mode ), domBuilder, mode );
		if( ! parser.parse() )
			if( throwOnError ) throw new ParserException( "Parse failure" );
			else domBuilder.getTranslationUnit().setParseSuccessful( false ); 
		
		return domBuilder.getTranslationUnit();
	}

	public void failTest(String code) {
		boolean testPassed = false;
		try {
			TranslationUnit tu = parse(code);
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());
		}
		if (testPassed)
			fail("The expected error did not occur.");
	}

}
