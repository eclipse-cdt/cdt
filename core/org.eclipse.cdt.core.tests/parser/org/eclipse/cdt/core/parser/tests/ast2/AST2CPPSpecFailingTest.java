/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Devin Steffler (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * NOTE:  Once these tests pass (are fixed) then fix the test to work so that they
 * are tested for a pass instead of a failure and move them to AST2CPPSpecTest.java.
 */
public class AST2CPPSpecFailingTest extends AST2SpecBaseTest {

	public AST2CPPSpecFailingTest() {
	}

	public AST2CPPSpecFailingTest(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(AST2CPPSpecFailingTest.class);
	}
	
	public void testDummy() {} // avoids JUnit "no tests" warning

	// template <class T> struct B { };
	// template <class T> struct D : public B<T> {};
	// struct D2 : public B<int> {};
	// template <class T> void f(B<T>&){}
	// void t()
	// {
	// D<int> d;
	// D2 d2;
	// f(d); //calls f(B<int>&)
	// f(d2); //calls f(B<int>&)
	// }
	public void _test14_8_2_4s8() throws Exception {
		parse(getAboveComment(), ParserLanguage.CPP, true, 0);
	}
}
