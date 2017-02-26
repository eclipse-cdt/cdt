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

public class ReferenceTests extends TestBase {
	public static class NonIndexing extends ReferenceTests {
		public NonIndexing() {setStrategy(new NonIndexingTestStrategy());}
		public static TestSuite suite() {return suite(NonIndexing.class);}
	}
	
	public static class SingleProject extends ReferenceTests {
		public SingleProject() {setStrategy(new SinglePDOMTestStrategy(true, false));}
		public static TestSuite suite() {return suite(SingleProject.class);}
	}
	
	//	constexpr int f() {
	//		int a { 1 };
	//		int &aRef { a };
	//		aRef++;
	//		return a;
	//	}
	
	// constexpr int x = f();
	public void testSideEffectsOnReferences() throws Exception {
		assertEvaluationEquals(2);
	}
	
	//	constexpr int f() {
	//		int a { 1 };
	//		int &aRef { a };
	//		aRef = aRef + 1;
	//		return a;
	//	}
	
	// constexpr int x = f();
	public void testAssignmentsOnReferences() throws Exception {
		assertEvaluationEquals(2);
	}
	
	//	constexpr int f() {
	//		int a { 1 };
	//		int &aRef { a };
	//		int &aRefRef { aRef };
	//		aRefRef++;
	//		return a;
	//	}
	
	//	constexpr auto x = f();
	public void testSideEffectsOnNestedReferences() throws Exception {
		assertEvaluationEquals(2);
	}
	
	//	constexpr int f() {
	//		int a { 1 };
	//		int &aRef { a };
	//		int &aRefRef { aRef };
	//		aRefRef = aRef + aRefRef;
	//		return a;
	//	}
	
	//	constexpr auto x = f();
	public void testAssignmentOnNestedReferences() throws Exception {
		assertEvaluationEquals(2);
	}
}