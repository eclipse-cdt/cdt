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

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.extension.ExtensionDialect;
import org.eclipse.cdt.internal.core.parser.ParserExtensionFactory;
import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayUtils;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.Scanner2;

/**
 * @author jcamelon
 *
 */
public class BaseScanner2Test extends TestCase {

	protected Scanner2 scanner;
	
	public BaseScanner2Test( String x )
	{
		super(x);
	}

	protected void initializeScanner( String input, ParserMode mode ) throws ParserFactoryError
	{
		initializeScanner( input, mode, new NullSourceElementRequestor( mode ));
	}

	protected void initializeScanner( String input, ParserMode mode, ISourceElementRequestor requestor ) throws ParserFactoryError
	{
		scanner = createScanner( new CodeReader(input.toCharArray()), new ScannerInfo(), mode, ParserLanguage.CPP, requestor, null, null ); //$NON-NLS-1$
	}

	protected void initializeScanner(String input) throws ParserFactoryError
	{
       initializeScanner( input, ParserMode.COMPLETE_PARSE );
	}
	
    public static Scanner2 createScanner( CodeReader code, IScannerInfo config, ParserMode mode, ParserLanguage language, ISourceElementRequestor requestor, IParserLogService log, List workingCopies ) throws ParserFactoryError
    {
    	if( config == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_CONFIG );
    	if( language == null ) throw new ParserFactoryError( ParserFactoryError.Kind.NULL_LANGUAGE );
    	IParserLogService logService = ( log == null ) ? ParserFactory.createDefaultLogService() : log;
		ParserMode ourMode = ( (mode == null )? ParserMode.COMPLETE_PARSE : mode );
		ISourceElementRequestor ourRequestor = (( requestor == null) ? new NullSourceElementRequestor() : requestor ); 
		return new Scanner2( code, config, ourRequestor, ourMode, language, logService, new ParserExtensionFactory( ExtensionDialect.GCC ).createScannerExtension(), workingCopies );
    }

	public int fullyTokenize() throws Exception
	{
		try
		{
			IToken t= scanner.nextToken();
			while (t != null)
			{
				if (verbose)
					System.out.println("Token t = " + t); //$NON-NLS-1$

				if ((t.getType()> IToken.tLAST))
					System.out.println("Unknown type for token " + t); //$NON-NLS-1$
				t= scanner.nextToken();
			}
		}
		catch ( EndOfFileException e)
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
			assertEquals( t.getType(), IToken.tIDENTIFIER );
			assertEquals(t.getImage(), expectedImage );
		} catch (EndOfFileException e) {
			assertTrue(false);
		} 
	}

	public void validateInteger(String expectedImage) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tINTEGER);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFileException e) {
			assertTrue(false);
		}
	}
	
	public void validateFloatingPointLiteral(String expectedImage) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tFLOATINGPT);
			assertTrue(t.getImage().equals(expectedImage));
		} catch (EndOfFileException e) {
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
		} catch (EndOfFileException e) {
			assertTrue(false);
		}
	}

	public void validateChar( String expected ) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tCHAR );
			assertEquals( t.getImage(), expected ); 
		} catch (EndOfFileException e) {
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
		} catch (EndOfFileException e) {
			assertTrue(false);
		} 
	}

	public void validateToken(int tokenType) throws ScannerException
	{
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == tokenType);
		} catch (EndOfFileException e) {
			assertTrue(false);
		} 
	}

	public void validateBalance(int expected)
	{
		// This isn't kept track of any more
		//assertTrue(scanner.getDepth() == expected);
	}

	public void validateBalance()
	{
		assertTrue(scanner.getDepth() == 0);
	}

	public void validateEOF() throws ScannerException
	{
		try {
			assertNull(scanner.nextToken());
		} catch (EndOfFileException e) {
		} 
	}

	public void validateDefinition(String name, String value)
	{
		Object expObject = scanner.getRealDefinitions().get(name.toCharArray());
		if (expObject == null)
			System.out.println("Hi");
		assertNotNull(expObject);
		assertTrue(expObject instanceof ObjectStyleMacro);
		assertTrue(CharArrayUtils.equals(value.toCharArray(), ((ObjectStyleMacro)expObject).expansion));
	}

	public void validateDefinition(String name, int value)
	{
		validateDefinition(name, String.valueOf(value));
	}

	public void validateAsUndefined(String name)
	{
		assertNull(scanner.getDefinition(name));
	}

	public static final String EXCEPTION_THROWN = "Exception thrown "; //$NON-NLS-1$

	public static final String EXPECTED_FAILURE = "This statement should not be reached " //$NON-NLS-1$
				+ "as we sent in bad preprocessor input to the scanner"; //$NON-NLS-1$

	public static final boolean verbose = false;


    /**
         * @param string
         */
    protected void validateWideChar(String string) throws Exception
    {
		try {
			IToken t= scanner.nextToken();
			assertTrue(t.getType() == IToken.tLCHAR );
			assertEquals( t.getImage(), string ); 
		} catch (EndOfFileException e) {
			assertTrue(false);
		}		
    }
	

}
