/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.DOMTests;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ACEFailedTest extends DOMTests {

	/**
	 * @param arg
	 */
	public ACEFailedTest(String arg) {
		super(arg);
	}
	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new ACEFailedTest("testBug36769"));
		suite.addTest(new ACEFailedTest("testBug36771"));
		return suite;
	}
	
	public void testBug36771(){
		boolean testPassed = false;
		try{
			Writer code = new StringWriter();
			code.write("#include /**/ \"foo.h\"\n");
			TranslationUnit tu = parse( code.toString());
			testPassed = true;
		} catch( Throwable e ){
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");		
	}
	
	public void testBug36769(){
		boolean testPassed = false;
		try{
			Writer code = new StringWriter();
			code.write("template <class A, B> cls<A, C>::operator op &() const {}\n");
			code.write("template <class A, B> cls<A, C>::cls() {}\n");
			code.write("template <class A, B> cls<A, C>::~cls() {}\n");
			
			TranslationUnit tu = parse( code.toString());
			testPassed = true;
		} catch( Throwable e ){
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");
	}
}
