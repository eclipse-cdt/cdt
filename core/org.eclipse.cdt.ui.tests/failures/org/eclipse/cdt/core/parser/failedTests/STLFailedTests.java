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
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.DOMTests;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author hamer
 */
public class STLFailedTests extends DOMTests {

	public STLFailedTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
	
		suite.addTest(new STLFailedTests("testBug36766A"));
		suite.addTest(new STLFailedTests("testBug36766B"));
		suite.addTest(new STLFailedTests("testBug36766C"));
	
		return suite;
	}

	public void testBug36766A() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write("template <class _CharT, class _Alloc>\n");
			code.write("rope<_CharT, _Alloc>::rope(size_t __n, _CharT __c,\n");
			code.write("const allocator_type& __a): _Base(__a)\n");
			code.write("{}\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());
	
			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36766B() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write("template<class _CharT>\n");
			code.write("bool _Rope_insert_char_consumer<_CharT>::operator()\n");
			code.write("(const _CharT* __leaf, size_t __n)\n");
			code.write("{}\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());
	
			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36766C() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write("template <class _CharT, class _Alloc>\n");
			code.write("_Rope_char_ref_proxy<_CharT, _Alloc>&\n");
			code.write("_Rope_char_ref_proxy<_CharT, _Alloc>::operator= (_CharT __c)\n");
			code.write("{}\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());
	
			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

}
