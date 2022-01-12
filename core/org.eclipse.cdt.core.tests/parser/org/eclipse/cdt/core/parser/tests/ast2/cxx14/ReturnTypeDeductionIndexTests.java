/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx14;

import org.eclipse.cdt.core.parser.tests.ast2.CommonCPPTypes;
import org.eclipse.cdt.internal.index.tests.IndexBindingResolutionTestBase;

public class ReturnTypeDeductionIndexTests extends IndexBindingResolutionTestBase {
	public ReturnTypeDeductionIndexTests() {
		setStrategy(new SinglePDOMTestStrategy(true));
	}

	//	struct A {
	//		auto f();
	//	};
	//	auto A::f() { return 42; }

	//	auto waldo = A().f();
	public void testOutOfLineMethod1() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("waldo", CommonCPPTypes.int_);
	}

	//	struct A {
	//		auto f();
	//	};

	//	auto A::f() { return 42; }
	//	auto waldo = A().f();
	public void _testOutOfLineMethod2() throws Exception {
		// TODO(nathanridge): Definition is a different file from the declaration is not supported yet.
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableType("waldo", CommonCPPTypes.int_);
	}
}
