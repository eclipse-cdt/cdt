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
import org.junit.jupiter.api.Test;

public class TemplateTemplateTypenameTests extends AST2CPPTestBase {

	// template<template<typename> typename T>
	// void f(T<int> param) {
	// }
	@Test
	public void testFunctionTemplateWithTemplateTemplateParameterWithTypenameInsteadOfClass_537217() throws Exception {
		parseAndCheckBindings();
	}

	// template<template<typename> typename U>
	// class C {
	//     U<int> mem;
	// };
	@Test
	public void testClassTemplateWithTemplateTemplateParameterWithTypenameInsteadOfClass_537217() throws Exception {
		parseAndCheckBindings();
	}
}
