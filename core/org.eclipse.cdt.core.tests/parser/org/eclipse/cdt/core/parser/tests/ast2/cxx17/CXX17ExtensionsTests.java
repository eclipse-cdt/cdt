/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;

import junit.framework.TestSuite;

public class CXX17ExtensionsTests extends AST2CPPTestBase {

	public static TestSuite suite() {
		return suite(CXX17ExtensionsTests.class);
	}

	//	struct Base {
	//		  int foo;
	//		};
	//		struct MyStruct : public Base {
	//		  int a;
	//		};
	//
	//		int main() {
	//		  MyStruct test = { {0}, 9 };
	//		}
	public void testAggregateInitializationOfBaseClass_549367() throws Exception {
		parseAndCheckImplicitNameBindings();
	}
}
