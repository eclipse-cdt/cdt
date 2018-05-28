/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hansruedi Patzen (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;

/**
 * AST tests for C++17 lambda changes.
 */
public class LambdaExpressionTests extends AST2CPPTestBase {
	// struct S {
	// 	void foo() {
	// 		[*this] { }();
	// 	}
	// };
	public void testLambdaCaptures_535196_1()  throws Exception {
		parseAndCheckBindings();
	}

	// struct S {
	// 	void bar() {
	// 	}
	// 	void foo() {
	// 		[*this] { bar(); }();
	// 	}
	// };
	public void testLambdaCaptures_535196_2()  throws Exception {
		parseAndCheckBindings();
	}

	// struct S {
	// 	void bar(int k) {
	// 	}
	// 	void foo() {
	// 		[m = 3, *this] { bar(m); }();
	// 	}
	// };
	public void testLambdaCaptures_535196_3()  throws Exception {
		parseAndCheckBindings();
	}
}
