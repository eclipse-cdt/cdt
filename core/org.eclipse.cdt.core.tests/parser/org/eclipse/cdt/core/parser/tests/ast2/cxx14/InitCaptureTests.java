/*******************************************************************************
 * Copyright (c) 2018 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hansruedi Patzen (IFS)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14;

import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes;

/**
 * AST tests for C++14 lambda init captures.
 */
public class InitCaptureTests extends AST2CPPTestBase {

	// int main() {
	// 	[var1 { 3 }] { }();
	// }
	public void testLambdaInitCaptures_413527_1a() throws Exception {
		parseAndCheckBindings();
	}

	// int main() {
	// 	int var2 {};
	// 	[var1 { 3 }, var2] { }();
	// }
	public void testLambdaInitCaptures_413527_1b() throws Exception {
		parseAndCheckBindings();
	}

	// int main() {
	// 	int var2 {};
	// 	[var1 { 3 }, var2] { }();
	// }
	public void testLambdaInitCaptures_413527_1c() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("var2", CommonCPPTypes.int_);
		helper.assertVariableType("var1", CommonCPPTypes.int_);
	}

	// int main() {
	// 	[var1(3)] { }();
	// }
	public void testLambdaInitCaptures_413527_2a() throws Exception {
		parseAndCheckBindings();
	}

	// int main() {
	// 	int var2 { };
	// 	[var1(3), var2] { }();
	// }
	public void testLambdaInitCaptures_413527_2b() throws Exception {
		parseAndCheckBindings();
	}

	// int main() {
	// 	int var2 { };
	// 	[var1(3), var2] { }();
	// }
	public void testLambdaInitCaptures_413527_2c() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("var2", CommonCPPTypes.int_);
		helper.assertVariableType("var1", CommonCPPTypes.int_);
	}

	// int main() {
	// 	int var2 { };
	// 	[var1( { 3, 3 } ), var2] { }();
	// }
	public void testLambdaInitCaptures_413527_2d() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("var2", CommonCPPTypes.int_);
		// #include <initalizer_list> missing
		helper.assertVariableTypeProblem("var1");
	}

	// int main() {
	// 	[var1 = 3] { }();
	// }
	public void testLambdaInitCaptures_413527_3a() throws Exception {
		parseAndCheckBindings();
	}

	// int main() {
	// 	int var2 { };
	// 	[var1 = 3, var2] { }();
	// }
	public void testLambdaInitCaptures_413527_3b() throws Exception {
		parseAndCheckBindings();
	}

	// int main() {
	// 	int var2 { };
	// 	[var1 = 3, var2] { }();
	// }
	public void testLambdaInitCaptures_413527_3c() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("var2", CommonCPPTypes.int_);
		helper.assertVariableType("var1", CommonCPPTypes.int_);
	}

	// struct S {
	// 	int i;
	// 	int j;
	// };
	// int main() {
	// 	int var2 { };
	// 	[var1 = { 3, 4 }, var2] { }();
	// }
	public void testLambdaInitCaptures_413527_3d() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("var2", CommonCPPTypes.int_);
		// #include <initalizer_list> missing
		helper.assertVariableTypeProblem("var1");
	}

	// int main() {
	// 	[var1 { 3 }] {
	// 		auto var3 = var1;
	// 	}();
	// }
	public void testLambdaInitCaptures_413527_4a() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("var1", CommonCPPTypes.int_);
		helper.assertVariableType("var3", CommonCPPTypes.int_);
	}

	// int main() {
	// 	[var1 { 3 }] {
	// 		var1++;
	// 	}();
	// }
	public void testLambdaInitCaptures_413527_4b() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("var1", CommonCPPTypes.int_);
	}
}
