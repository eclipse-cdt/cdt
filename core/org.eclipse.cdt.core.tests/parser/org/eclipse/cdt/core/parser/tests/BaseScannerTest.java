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

import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.internal.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;

/**
 * @author jcamelon
 *
 */
public class BaseScannerTest extends TestCase {

	protected IScanner scanner;
	
	public BaseScannerTest( String x )
	{
		super(x);
	}

	protected void initializeScanner( String input, ParserMode mode )
	{
		scanner= ParserFactory.createScanner( new StringReader(input),"TEXT", new ScannerInfo(), mode, ParserLanguage.CPP, callback );
	}


	protected void initializeScanner(String input)
	{
       initializeScanner( input, ParserMode.COMPLETE_PARSE );
	}

	public int fullyTokenize() throws Exception
	{
		try
		{
			IToken t= scanner.nextToken();
			while (t != null)
			{
				if (verbose)
					System.out.println("Token t = " + t);

				if ((t.getType()> IToken.tLAST))
					System.out.println("Unknown type for token " + t);
				t= scanner.nextToken();
			}
		}
		catch ( EndOfFile e)
		{
		}
		catch (ScannerException se)
		{
			throw se;
		}
		return scanner.getCount();
	}
	public void validateIdentifier(String expectedImage) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tIDENTIFIER);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFile e) {
			assertTrue(false);
		}
	}

	public void validateInteger(String expectedImage) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tINTEGER);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFile e) {
			assertTrue(false);
		}
	}
	
	public void validateFloatingPointLiteral(String expectedImage) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tFLOATINGPT);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFile e) {
			assertTrue(false);
		}
	}
	
	public void validateChar( char expected )throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tCHAR );
			Character c = new Character( expected ); 
			assertEquals( t.getImage(), c.toString() ); 
		} catch (EndOfFile e) {
			assertTrue(false);
		}		
	}
	public void validateChar( String expected ) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tCHAR );
			assertEquals( t.getImage(), expected ); 
		} catch (EndOfFile e) {
			assertTrue(false);
		}		
	}

	public void validateString( String expectedImage ) throws ScannerException
	{
		validateString( expectedImage, false );
	}

	public void validateString(String expectedImage, boolean lString ) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			if( lString )
				assertTrue(t.getType() == IToken.tLSTRING);
			else
				assertTrue(t.getType() == IToken.tSTRING);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFile e) {
			assertTrue(false);
		}
	}

	public void validateToken(int tokenType) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == tokenType);
		} catch (EndOfFile e) {
			assertTrue(false);
		}
	}

	public void validateBalance(int expected)
	{
		assertTrue(scanner.getDepth() == expected);
	}

	public void validateBalance()
	{
		assertTrue(scanner.getDepth() == 0);
	}

	public void validateEOF() throws ScannerException
	{
		try {
			assertNull(scanner.nextToken());
		} catch (EndOfFile e) {
		}
	}

	public void validateDefinition(String name, String value)
	{
		String definition= null;
		definition= (String) scanner.getDefinition(name);
		assertNotNull(definition);
		assertTrue(definition.trim().equals(value));
	}

	public void validateDefinition(String name, int value)
	{
		String definition= null;
		definition= (String) scanner.getDefinition(name);
		assertNotNull(definition);
		int intValue= (Integer.valueOf((String) definition)).intValue();
		assertEquals(value, intValue);
	}

	public void validateAsUndefined(String name)
	{
		assertNull(scanner.getDefinition(name));
	}

	public static final String EXCEPTION_THROWN = "Exception thrown ";

	public static final String EXPECTED_FAILURE = "This statement should not be reached "
				+ "as we sent in bad preprocessor input to the scanner";

	public static final boolean verbose = false;

    protected NullSourceElementRequestor callback = new NullSourceElementRequestor();
	

}
