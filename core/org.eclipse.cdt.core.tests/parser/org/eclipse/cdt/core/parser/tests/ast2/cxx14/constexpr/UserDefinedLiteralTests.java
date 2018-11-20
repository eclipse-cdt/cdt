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

import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;

import junit.framework.TestSuite;

public class UserDefinedLiteralTests extends TestBase {
	public static class NonIndexing extends UserDefinedLiteralTests {
		public NonIndexing() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexing.class);
		}
	}

	public static class SingleProject extends UserDefinedLiteralTests {
		public SingleProject() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProject.class);
		}
	}

	//	constexpr unsigned long long operator"" _min(unsigned long long minutes) {
	//		return minutes * 60;
	//	}

	//	constexpr auto x = 25_min;
	public void testUserDefinedIntegerLiteral() throws Exception {
		assertEvaluationEquals(1500);
	}

	//	constexpr unsigned long long operator"" _capitals(const char* str, unsigned long size) {
	//		unsigned long long  count = 0;
	//		for(int i = 0; i < size; ++i) {
	//			if(str[i] >= 'A' && str[i] <= 'Z') {
	//				count++;
	//			}
	//		}
	//		return count;
	//	}

	//	constexpr auto x = "HAllO"_capitals;
	public void testUserDefinedStringLiteral() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr bool operator "" _v(char c) {
	//		switch(c) {
	//			case 'a':
	//			case 'e':
	//			case 'i':
	//			case 'o':
	//			case 'u':
	//				return true;
	//			default:
	//				return false;
	//		}
	//	}

	//	constexpr auto x = 'a'_v;
	public void testUserDefinedCharacterLiteral() throws Exception {
		assertEvaluationEquals(true);
	}

	//	constexpr long double operator"" _deg(long double deg) {
	//	  return deg * 3.141592 / 180;
	//	}

	//	constexpr auto x = 100.0_deg;
	public void testUserDefinedFloatingPointLiteral() throws Exception {
		assertEvaluationEquals(1.74533);
	}

	//	constexpr unsigned long long operator "" _l(const char *str) {
	//		int l = 0;
	//		while(str[l] != '\0') {
	//			l++;
	//		}
	//		return l;
	//	}

	//	constexpr auto x = 20000_l;
	public void testFallbackToRawLiteralOperator() throws Exception {
		assertEvaluationEquals(5);
	}

	//	template <char... STR>
	//	constexpr unsigned operator"" _l() {
	//	  return 5;
	//	}

	//	constexpr int x = 123.20_l;
	public void testRawLiteralOperatorTemplate() throws Exception {
		assertEvaluationEquals(5);
	}

	//	constexpr unsigned operator "" _l(unsigned long long x) {
	//		return 10;
	//	}
	//	constexpr unsigned operator "" _l(const char *str) {
	//		return 20;
	//	}

	//	constexpr int x = 120_l;
	public void testChoosesCookedLiteralOverRawLiteralOperatorIfAvailable() throws Exception {
		assertEvaluationEquals(10);
	}

	//	constexpr unsigned operator "" _l(unsigned long long x) {
	//		return 10;
	//	}
	//	template <char... STR>
	//	constexpr unsigned operator"" _l() {
	//	  	return 20;
	//	}

	//	constexpr int x = 120_l;
	public void testChoosesCookedLiteralOverRawLiteralTemplateIfAvailable() throws Exception {
		assertEvaluationEquals(10);
	}

	//	constexpr unsigned operator "" _l(long double x) {
	//		return 10;
	//	}
	//	constexpr unsigned operator "" _l(const char *str) {
	//		return 20;
	//	}

	//	constexpr int x = 120_l;
	public void testFallsBackToRawLiteralOperatorIfParameterTypeDoesntMatchUnsignedLongLong() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr unsigned operator "" _l(long double x) {
	//		return 10;
	//	}
	//	template <char... STR>
	//	constexpr unsigned operator"" _l() {
	//	  	return 20;
	//	}

	//	constexpr int x = 120_l;
	public void testFallsBackToRawLiteralOperatorTemplateIfParameterTypeDoesntMatchUnsignedLongLong() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr unsigned operator "" _l(unsigned long long x) {
	//		return 10;
	//	}
	//	constexpr unsigned operator "" _l(const char *str) {
	//		return 20;
	//	}

	//	constexpr int x = 120.0_l;
	public void testFallsBackToRawLiteralOperatorIfParameterTypeDoesntMatchLongDouble() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr unsigned operator "" _l(unsigned long long x) {
	//		return 10;
	//	}
	//	template <char... STR>
	//	constexpr unsigned operator"" _l() {
	//	  	return 20;
	//	}

	//	constexpr int x = 120.0_l;
	public void testFallsBackToRawLiteralOperatorTemplateIfParameterTypeDoesntMatchLongDouble() throws Exception {
		assertEvaluationEquals(20);
	}

	//	constexpr unsigned operator "" _l(const char *str) {
	//		return 20;
	//	}

	//	constexpr int x = "hello"_l;
	public void testIgnoresRawLiteralOperatorForUserDefinedStringLiterals() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}

	//	template <char... STR>
	//	constexpr unsigned operator"" _l() {
	//	  	return 20;
	//	}

	//	constexpr int x = "hello"_l;
	public void testIgnoresRawLiteralOperatorTemplateForUserDefinedStringLiterals() throws Exception {
		assertEvaluationEquals(IntegralValue.ERROR);
	}
}