/*******************************************************************************
 * Copyright (c) 2018 Vlad Ivanov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vlad Ivanov (LabSystems) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.index.tests.IndexBindingResolutionTestBase;

public class TemplateAutoIndexTests extends IndexBindingResolutionTestBase {
	public TemplateAutoIndexTests() {
		setStrategy(new SinglePDOMTestStrategy(true));
	}

	//	template<typename Type, Type v>
	//	struct helper {
	//		static Type call() {
	//			return nullptr;
	//		}
	//	};
	//
	//	template<auto F>
	//	class call_helper {
	//		using functor_t = decltype(F);
	//
	//	public:
	//		using type = helper<functor_t, F>;
	//	};

	//	struct Something {
	//		void foo() {}
	//	};
	//
	//	using A = call_helper<&Something::foo>::type;
	//	auto waldo = A::call();
	public void testTemplateAutoIndex() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable variable = helper.assertNonProblem("waldo");
		IType variableType = variable.getType();

		assertEquals("void (Something::*)()", variableType.toString());
	}
}
