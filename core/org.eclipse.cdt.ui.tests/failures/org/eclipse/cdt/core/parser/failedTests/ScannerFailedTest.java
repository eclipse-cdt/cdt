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
               suite.addTest( new ScannerFailedTest( "testBug36509" ) ); 
               suite.addTest( new ScannerFailedTest( "testBug36521" ) ); 
 
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
       public void testBug36509() throws Exception 
       { 
               boolean testPassed = false; 
               
               try{ 
                       StringWriter writer = new StringWriter(); 
                       writer.write("#define debug(s, t) printf(\"x\" # s \"= %d, x\" # t \"= %s\", \\\n"); 
                       writer.write("                    x ## s, x ## t) \n"); 
                       
                       initializeScanner( writer.toString() ); 
                       //printf("x" "1" "=%d, x" "2" "= %s", x1, x2); 
                       validateIdentifier( "printf" ); 
                       validateToken( Token.tLPAREN ); 
                       validateString("x"); 
                       validateString("1"); 
                       validateString("= %d, x"); 
                       validateString("2"); 
                       validateString("= %s"); 
                       validateToken(Token.tCOMMA); 
                       validateIdentifier("x1"); 
                       validateToken(Token.tCOMMA); 
                       validateIdentifier("x2"); 
                       validateToken(Token.tRPAREN); 
                       validateToken(Token.tSEMI); 
                       
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
       public void testBug36521() throws Exception 
       { 
               boolean testPassed = false; 
               
               try{ 
                       StringWriter writer = new StringWriter(); 
                       writer.write("#define str(s)      # s\n"); 
                       writer.write("fputs(str(strncmp(\"abc\\0d\", \"abc\", \'\\4\')\n"); 
                       writer.write("        == 0) str(: @\\n), s);\n"); 
                                               
                       initializeScanner( writer.toString() ); 
                       validateIdentifier("fputs"); 
                       validateToken(Token.tLPAREN); 
                       validateString("strncmp(\\\"abc\\\\0d\\\", \\\"abc\\\", '\\\\4') == 0"); 
                       validateString(": @\\n"); 
                       validateToken(Token.tCOMMA); 
                       validateIdentifier("s"); 
                       validateToken(Token.tRPAREN); 
                       validateToken(Token.tSEMI); 
                       
                       testPassed = true; 
               } 
               catch( ScannerException e ) 
               { 
                       if( !e.getMessage().equals( "Improper use of macro str" ) ) 
                               fail( "Unexpected Error: " + e.getMessage() ); 
		}
	
		if( testPassed )
			fail( "The expected error did not occur." );
	}
}
