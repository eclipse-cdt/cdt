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

package org.eclipse.cdt.core.parser.failedTests;

import java.io.StringWriter;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.ScannerTestCase;
import org.eclipse.cdt.internal.core.parser.ScannerException;
import org.eclipse.cdt.internal.core.parser.Token;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ScannerFailedTest extends ScannerTestCase {

	public ScannerFailedTest(String name){
		super(name);
	}
	
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		
		suite.addTest( new ScannerFailedTest( "testBug36475" ) );
	
		return suite;
	}
	
	public void testBug36475() throws Exception
	{
		boolean testPassed = false;
		try
		{
			StringWriter writer = new StringWriter(); 
			writer.write( " \"A\" \"B\" \"C\" " ); 
			
			initializeScanner( writer.toString() );
			  
			validateString( "ABC" );
			validateEOF(); 
		
			testPassed = true;
		
		}
		catch( Throwable e )
		{
			if( !(e instanceof AssertionFailedError) ){
				fail( "Unexpected Error: " + e.getMessage() );
			}
		}
	
		if( testPassed )
			fail( "The expected error did not occur." );
	}
}
