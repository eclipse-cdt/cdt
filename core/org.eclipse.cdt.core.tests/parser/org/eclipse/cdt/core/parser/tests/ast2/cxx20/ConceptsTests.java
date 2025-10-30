/*******************************************************************************
 * Copyright (c) 2025 Igor V. Kovalenko.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Igor V. Kovalenko - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx20;

import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;
import org.junit.jupiter.api.Test;

/**
 * AST tests for C++20 requires expressions.
 */
public class ConceptsTests extends AST2CPPTestBase {
	//  template<typename T>
	//  concept A = true;
	//
	//  template<typename T>
	//  concept B = A<T>;
	//
	//  template<typename T>
	//  concept C = requires {
	//      true;
	//  };
	@Test
	public void testConceptDefinitionExpressions() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  void x() requires true;
	//
	//  template<typename T>
	//  void y() requires requires () {
	//      true;
	//  };
	//
	//  template<typename T>
	//  void z() requires true && false;
	@Test
	public void testFunctionDeclarationTrailingRequiresClauseTrue() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  concept A = false;
	//  template<typename T>
	//  concept B = true;
	//  template<typename T>
	//  concept C = true;
	//
	//  template<typename T>
	//  void x() requires true && false {}
	//
	//  template<typename T>
	//  void x_c() requires C<T> {}
	//
	//  template<typename T>
	//  void x_abc() requires A<T> || B<T> && C<T> {}
	@Test
	public void testFunctionDefinitionTrailingRequiresClause() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  concept A = false;
	//  template<typename T>
	//  concept B = true;
	//  template<typename T>
	//  concept C = true;
	//
	//  template<typename T>
	//  requires A<T> || B<T> && C<T>
	//  void x() {}
	@Test
	public void testTemplateHeadRequiresClause() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  requires requires { true; }
	//  void x() {}
	@Test
	public void testTemplateHeadAdHocRequiresExpression() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  constexpr bool value = requires () { true; };
	@Test
	public void testInitializerRequiresExpression() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  constexpr bool x() {
	//		if constexpr(requires { true; }) {
	//			return true;
	//		} else {
	//			return false;
	//		}
	//	}
	@Test
	public void testConstexprIfRequiresExpression() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  concept A = false;
	//  template<typename T>
	//  concept B = true;
	//  template<typename T>
	//  concept C = true;
	//
	//  template<A U>
	//  void f_a(U a_u);
	//  template<A U1, B U2, C U3>
	//  void f_abc(U1 a_u1, U2 b_u2, U3 c_u3);
	@Test
	public void testTemplateArgumentTypeConstraint() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}

	//  template<typename T>
	//  concept A = false;
	//  namespace Outer {
	//      template<typename T>
	//      concept B = true;
	//      namespace Inner {
	//          template<typename T>
	//          concept C = true;
	//      }
	//  }
	//
	//  template<A U1, Outer::B U2, Outer::Inner::C U3>
	//  void f_abc(U1 a_u1, U2 b_u2, U3 c_u3);
	@Test
	public void testTemplateArgumentTypeConstraintFromNamespace() throws Exception {
		parseAndCheckBindings(ScannerKind.STDCPP20);
	}
}
