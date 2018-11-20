/*******************************************************************************
* Copyright (c) 2016 Institute for Software, HSR Hochschule fuer Technik
* Rapperswil, University of applied sciences and others
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14.constexpr;

import junit.framework.TestSuite;

public class CStringValueTests extends TestBase {
	public static class NonIndexing extends CStringValueTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends CStringValueTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	constexpr auto x = "Hello, World!";
	public void testWithoutPrefix() throws Exception {
		assertEvaluationEquals("Hello, World!");
	}

	//	constexpr auto y = "Hello, World!";

	//	constexpr auto x = y;
	public void testStringAssignment() throws Exception {
		assertEvaluationEquals("Hello, World!");
	}

	//	constexpr auto x = L"Hello, World!";
	public void testLPrefix() throws Exception {
		assertEvaluationEquals("Hello, World!");
	}

	//	constexpr auto x = u8"Hello, World!";
	public void testu8Prefix() throws Exception {
		assertEvaluationEquals("Hello, World!");
	}

	//	constexpr auto x = u"Hello, World!";
	public void testuPrefix() throws Exception {
		assertEvaluationEquals("Hello, World!");
	}

	//	constexpr auto x = U"Hello, World!";
	public void testUPrefix() throws Exception {
		assertEvaluationEquals("Hello, World!");
	}

	//constexpr auto x = R"(This is
	//a "raw" \n\n
	//	literal\0end)";
	public void testRawStringLiteral() throws Exception {
		assertEvaluationEquals("This is\na \"raw\" \\n\\n\n\tliteral\\0end");
	}

	//constexpr auto x = R"ab(This is)"
	//a "raw" literal)ab";
	public void testRawStringLiteralWithDelimiter() throws Exception {
		assertEvaluationEquals("This is)\"\na \"raw\" literal");
	}

	//	constexpr auto x = "line 1\n"
	//						"line 2\n"
	//						"line 3";
	public void testCStringLiteralConcatenation() throws Exception {
		assertEvaluationEquals("line 1\nline 2\nline 3");
	}

	//	constexpr auto x = "PI = \u03C0";
	public void test16bitUnicodeEscapeSequence() throws Exception {
		assertEvaluationEquals("PI = \u03C0");
	}

	//	constexpr int len(const char *str) {
	//		int len = 0;
	//		while(str[len] != '\0') {
	//			len++;
	//		}
	//		return len;
	//	}
	//
	//	constexpr int f() {
	//		const char *str = "hello";
	//		return len(str);
	//	}

	//	constexpr int x = f();
	public void testCStringParam() throws Exception {
		assertEvaluationEquals(5);
	}
}