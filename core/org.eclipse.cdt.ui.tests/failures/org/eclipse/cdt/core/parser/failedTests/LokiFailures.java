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
package org.eclipse.cdt.core.parser.failedTests;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.DOMTests;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class LokiFailures extends DOMTests {

	public LokiFailures(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new LokiFailures("testBugSingleton192"));
		suite.addTest(new LokiFailures("testBugFunctor758"));
		
		return suite;
	}

	public void testBugSingleton192() {
		failTest("int Test::* pMember_;" );
	}
	
	public void testBugFunctor758() {
		failTest( "template <typename Fun> Functor(Fun fun) : spImpl_(new FunctorHandler<Functor, Fun>(fun)){}" ); 
	}

    public void testBugTypeManip151()
    {
		Writer code = new StringWriter();
		try
		{ 
			code.write( "template <class T, class U>	struct SuperSubclass {\n"  );
			code.write( "enum { value = (::Loki::Conversion<const volatile U*, const volatile T*>::exists && \n" );
			code.write( "!::Loki::Conversion<const volatile T*, const volatile void*>::sameType) };	};" );
		} catch( IOException ioe ){}
		failTest( code.toString() );
	
    }
	
	public void failTest( String code )
	{
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
