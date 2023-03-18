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

public abstract class AST2CPPTestBase extends AST2TestBase {
	public AST2CPPTestBase() {
	}

	public AST2CPPTestBase(String name) {
		super(name);
	}

	protected IASTTranslationUnit parseAndCheckBindings(String code) throws Exception {
		return parseAndCheckBindings(code, ScannerKind.STD);
	}

	protected IASTTranslationUnit parseAndCheckBindings(String code, ScannerKind scannerKind) throws Exception {
		return parseAndCheckBindings(code, CPP, scannerKind);
	}

	protected IASTTranslationUnit parseAndCheckBindings() throws Exception {
		return parseAndCheckBindings(ScannerKind.STD);
	}

	protected IASTTranslationUnit parseAndCheckBindings(ScannerKind scannerKind) throws Exception {
		String code = getAboveComment();
		return parseAndCheckBindings(code, scannerKind);
	}

	protected BindingAssertionHelper getAssertionHelper() throws ParserException, IOException {
		return getAssertionHelper(ScannerKind.GNU);
	}

	protected BindingAssertionHelper getAssertionHelper(ScannerKind scannerKind) throws ParserException, IOException {
		return getAssertionHelper(CPP, scannerKind);
	}
}
