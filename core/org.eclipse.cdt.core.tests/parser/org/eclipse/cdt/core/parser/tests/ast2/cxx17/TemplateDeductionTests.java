/*******************************************************************************
 * Copyright (c) 2018 Vlad Ivanov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Ivanov (LabSystems) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;

import junit.framework.TestSuite;

public class TemplateDeductionTests extends AST2CPPTestBase {

	public static TestSuite suite() {
		return suite(TemplateDeductionTests.class);
	}

	//	template<auto F>
	//	struct C {
	//		using T = decltype(F);
	//	};
	//
	//	void foo() {
	//
	//	}
	//
	//	using A = C<&foo>::T;
	public void testTemplateNontypeParameterTypeDeductionParsing_519361() throws Exception {
		parseAndCheckBindings();
	}
}
