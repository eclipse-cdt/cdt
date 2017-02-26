/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr;

import junit.framework.TestSuite;

public class TypeAliasTests extends TestBase {
	public static class NonIndexing extends TypeAliasTests {
		public NonIndexing() {setStrategy(new NonIndexingTestStrategy());}
		public static TestSuite suite() {return suite(NonIndexing.class);}
	}
	
	public static class SingleProject extends TypeAliasTests {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true, false));}
		public static TestSuite suite() {return suite(SingleProject.class);}
	}
	
	//	constexpr int f() {
	//		typedef int myint;
	//		myint x = 5;
	//		x *= 5;
	//		return x;
	//	}
	
	//	constexpr int x = f();
	public void testTypedefDeclaration() throws Exception {
		assertEvaluationEquals(25);
	}
	
	//	constexpr int f() {
	//		using myint = int;
	//		myint x = 5;
	//		x *= 5;
	//		return x;
	//	}
	
	//	constexpr int x = f();
	public void testAliasDeclaration() throws Exception {
		assertEvaluationEquals(25);
	}
}