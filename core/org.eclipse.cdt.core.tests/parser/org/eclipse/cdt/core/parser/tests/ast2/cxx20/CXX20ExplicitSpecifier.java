/*******************************************************************************
 * Copyright (c) 2026 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx20;

import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.junit.jupiter.api.Test;

/**
 * AST tests for C++20 explicit specifier.
 */
public class CXX20ExplicitSpecifier extends AST2CPPTestBase {

	//	class Test
	//	{
	//	public:
	//		explicit Test() {}
	//
	//	};
	//
	//	class Test2
	//	{
	//	public:
	//		explicit(false) Test2() {}
	//
	//	};
	//
	//	class Test3
	//	{
	//	public:
	//		explicit(true) Test3() {}
	//
	//	};
	@Test
	public void testSimpleExplicitSpecifier() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//	constexpr bool returnFalse() { return false; }
	//	constexpr bool returnTrue() { return true; }
	//
	//	class Test4
	//	{
	//	public:
	//		explicit(returnFalse()) Test4() {}
	//		explicit(returnTrue()) Test4(int) {}
	//
	//	};
	//
	//	template <bool V>
	//	class Test5
	//	{
	//	public:
	//		explicit(V) Test5() {}
	//
	//	};
	//
	//	Test5<returnFalse()> t;
	//	Test5<returnTrue()> u;
	// TODO: Unsupported for now
	//	@Test
	//	public void testExplicitSpecifierConstantExpression() throws Exception {
	//		parseAndCheckBindings(ScannerKind.STDCPP20);
	//	}
}
