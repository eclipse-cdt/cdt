/*******************************************************************************
 * Copyright (c) 2018 Felix Morgner
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Felix Morgner (IFS Institute for Software) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2.cxx17;

import org.eclipse.cdt.core.parser.tests.ast2.AST2CPPTestBase;

import junit.framework.TestSuite;

public class TemplateTemplateTypenameTests extends AST2CPPTestBase {

	public static TestSuite suite() {
		return suite(TemplateTemplateTypenameTests.class);
	}

	// template<template<typename> typename T>
	// void f(T<int> param) {
	// }
	public void testFunctionTemplateWithTemplateTemplateParameterWithTypenameInsteadOfClass_537217() throws Exception {
		parseAndCheckBindings();
	}

	// template<template<typename> typename U>
	// class C {
	//     U<int> mem;
	// };
	public void testClassTemplateWithTemplateTemplateParameterWithTypenameInsteadOfClass_537217() throws Exception {
		parseAndCheckBindings();
	}
}
