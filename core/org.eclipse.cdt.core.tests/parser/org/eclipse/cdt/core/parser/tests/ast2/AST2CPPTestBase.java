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
package org.eclipse.cdt.core.parser.tests.ast2;

import static org.eclipse.cdt.core.parser.ParserLanguage.CPP;

import java.io.IOException;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class AST2CPPTestBase extends AST2TestBase {
	public AST2CPPTestBase() {
	}

	public AST2CPPTestBase(String name) {
		super(name);
	}

	protected IASTTranslationUnit parseAndCheckBindings(String code) throws Exception {
		return parseAndCheckBindings(code, CPP);
	}

	protected IASTTranslationUnit parseAndCheckBindings() throws Exception {
		String code = getAboveComment();
		return parseAndCheckBindings(code);
	}

	protected BindingAssertionHelper getAssertionHelper() throws ParserException, IOException {
		return getAssertionHelper(CPP);
	}
}
