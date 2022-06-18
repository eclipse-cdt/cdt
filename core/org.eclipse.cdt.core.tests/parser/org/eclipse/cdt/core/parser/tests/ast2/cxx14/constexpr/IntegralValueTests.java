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

import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.internal.core.dom.parser.IntegralValue;

import junit.framework.TestSuite;

public abstract class IntegralValueTests extends TestBase {
	public static class NonIndexingTests extends IntegralValueTests {
		public NonIndexingTests() {
			setStrategy(new NonIndexingTestStrategy());
		}

		public static TestSuite suite() {
			return suite(NonIndexingTests.class);
		}
	}

	public static class SingleProjectTests extends IntegralValueTests {
		public SingleProjectTests() {
			setStrategy(new SinglePDOMTestStrategy(true, false));
		}

		public static TestSuite suite() {
			return suite(SingleProjectTests.class);
		}
	}

	@Override
	protected void setUp() throws Exception {
		TestScannerProvider.sDefinedSymbols.put("__SIZEOF_SHORT__", "2");
		TestScannerProvider.sDefinedSymbols.put("__SIZEOF_INT__", "4");
		TestScannerProvider.sDefinedSymbols.put("__SIZEOF_LONG__", "8");
		TestScannerProvider.sDefinedSymbols.put("__SIZEOF_LONG_LONG__", "8");
		TestScannerProvider.sDefinedSymbols.put("__SIZEOF_POINTER__", "8");
		super.setUp();
	}

	//	constexpr auto x = int{} + int();
	public void testIntDefaultValue() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto f() {
	//		int x{};
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testIntValueInitialization() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto x = long{} + long();
	public void testLongDefaultValue() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto f() {
	//		long x{};
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testLongValueInitialization() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto x = short{} + short();
	public void testShortDefaultValue() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto f() {
	//		short x{};
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testShortValueInitialization() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto x = bool{} + bool();
	public void testBooleanDefaulValue() throws Exception {
		assertEvaluationEquals(false);
	}

	//	constexpr auto f() {
	//		bool x{};
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testBoolValueInitialization() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto x = char{} + char();
	public void testCharDefaultValue() throws Exception {
		assertEvaluationEquals(0);
	}

	//	constexpr auto f() {
	//		char x{'c'};
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testCharValueInitialization() throws Exception {
		assertEvaluationEquals('c');
	}

	// constexpr int mul(int op1, int op2) {
	//   int result = op1 * op2;
	//   return result;
	// }

	// constexpr int x = mul(2, 5);
	public void testDeclarationWithEqualsInitializerInSequence() throws Exception {
		assertEvaluationEquals(10);
	}

	// constexpr int mul(int op1, int op2) {
	//   int intermediate1 { op1 };
	//   int intermediate2 { op2 };
	//   return intermediate1 * intermediate2;
	// }

	// constexpr int x = mul(2, 5);
	public void testDeclarationWithDefaultInitializationInSequence() throws Exception {
		assertEvaluationEquals(10);
	}

	// constexpr int f() {
	//   int i(5);
	//	 i++;
	//   return i;
	// }

	// constexpr int x = f();
	public void testDirectInitializationOnFundamentalTypes() throws Exception {
		assertEvaluationEquals(6);
	}

	// constexpr int f() {
	//   int invalid;
	//   return invalid;
	// }

	// constexpr int x = f();
	public void testUseOfUninitializedVariableIsError() throws Exception {
		assertEvaluationEquals(IntegralValue.UNKNOWN);
	}

	//	constexpr auto f() {
	//		int x = 1, y = 1, z = 1;
	//		return x + y + z;
	//	}

	//	constexpr auto x = f();
	public void testDeclarationWithMultipleDeclarators() throws Exception {
		assertEvaluationEquals(3);
	}

	//	constexpr int f() {
	//		int i{5};
	//		i++;
	//		return i;
	//	}

	//	constexpr int x = f();
	public void testSimpleTypeConstructionInitializerList() throws Exception {
		assertEvaluationEquals(6);
	}

	//	constexpr int f() {
	//		int i(5);
	//		i++;
	//		return i;
	//	}

	//	constexpr int x = f();
	public void testSimpleTypeConstructionConstructorInitializer() throws Exception {
		assertEvaluationEquals(6);
	}

	//	constexpr int f() {
	//		int i = 5;
	//		i++;
	//		return i;
	//	}

	//	constexpr int x = f();
	public void testSimpleTypeConstructionEqualsInitializer1() throws Exception {
		assertEvaluationEquals(6);
	}

	//	constexpr int f() {
	//		int i = {5};
	//		i++;
	//		return i;
	//	}

	//	constexpr int x = f();
	public void testSimpleTypeConstructionEqualsInitializer2() throws Exception {
		assertEvaluationEquals(6);
	}

	// constexpr int f() {
	//	int a { 3 };
	//  int b = a;
	//  b++;
	//  return a + b;
	// }

	// constexpr int x = f();
	public void testCopyInitialization() throws Exception {
		assertEvaluationEquals(7);
	}

	//  constexpr int f() {
	//		int y = 0, x = 5;
	//		x++;
	//		return x;
	//	}

	//	constexpr auto x = f();
	public void testMultipleDeclaratorsInOneDeclaration() throws Exception {
		assertEvaluationEquals(6);
	}

	//	constexpr int f() {
	//		return int{5};
	//	}

	//	constexpr int x = f();
	public void testSimpleTypeConstructorExpression1() throws Exception {
		assertEvaluationEquals(5);
	}

	//	constexpr int f() {
	//		int a { 1 };   // declaration
	//		a = ++a * ++a; // assignment / side effects
	//		return a;      // returns 6
	//	}

	//	constexpr auto x = f();
	public void testSideEffects2() throws Exception {
		assertEvaluationEquals(6);
	}

	//  constexpr int x = 2;

	//	constexpr int y = x * 4;
	public void testAccessGlobalVariableFromGlobalConstexpr() throws Exception {
		assertEvaluationEquals(8);
	}

	//  constexpr int x = 2;
	//  constexpr int f() { return x * 4; }

	//	constexpr int y = f();
	public void testAccessGlobalVariableFromConstexprFunction() throws Exception {
		assertEvaluationEquals(8);
	}

	//	constexpr int x = 0x2a;
	public void testHexLiteral() throws Exception {
		assertEvaluationEquals(42);
	}

	// constexpr int x = __builtin_ffs(0) + __builtin_ffs(16);
	public void testBuiltinFfs() throws Exception {
		assertEvaluationEquals(5);
	}

	// constexpr int x = __builtin_ffs(0x100000000);
	public void testBuiltinFfsNarrowing() throws Exception {
		assertEvaluationEquals(0);
	}

	// constexpr int x = __builtin_ffsl(0x100000000);
	public void testBuiltinFfsl() throws Exception {
		assertEvaluationEquals(33);
	}

	// constexpr int x = __builtin_ctz(16);
	public void testBuiltinCtz() throws Exception {
		assertEvaluationEquals(4);
	}

	// constexpr int x = __builtin_popcount(128 + 32 + 8 + 4 + 2);
	public void testBuiltinPopcount() throws Exception {
		assertEvaluationEquals(5);
	}

	// constexpr int x = __builtin_popcountl(0x80000001);
	public void testBuiltinPopcountHighBitSet() throws Exception {
		assertEvaluationEquals(2);
	}

	// constexpr int x = __builtin_popcountl(0x8000000000000001);
	public void testBuiltinPopcountlHighBitSet() throws Exception {
		assertEvaluationEquals(2);
	}

	// constexpr int x = __builtin_popcountll(0x8000000000000001);
	public void testBuiltinPopcountllHighBitSet() throws Exception {
		assertEvaluationEquals(2);
	}

	// constexpr int x = __builtin_parity(128 + 32 + 8 + 4 + 2) + __builtin_parity(64) + __builtin_parity(0);
	public void testBuiltinParity() throws Exception {
		assertEvaluationEquals(2);
	}

	// constexpr int x = __builtin_abs(700) + __builtin_abs(50);
	public void testBuiltinAbs() throws Exception {
		assertEvaluationEquals(750);
	}

	// constexpr int x = __builtin_abs(-1);
	public void testBuiltinAbsNegativeInput() throws Exception {
		assertEvaluationEquals(1);
	}

	// constexpr int x = __builtin_abs(0xFFFFFFFF);
	public void testBuiltinAbsNarrowing() throws Exception {
		assertEvaluationEquals(1);
	}
}