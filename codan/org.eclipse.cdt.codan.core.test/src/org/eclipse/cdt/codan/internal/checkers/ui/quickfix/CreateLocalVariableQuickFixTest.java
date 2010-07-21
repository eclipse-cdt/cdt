/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

/**
 * @author Tomasz Wesolowski
 */
public class CreateLocalVariableQuickFixTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new QuickFixCreateLocalVariable();
	}

	// void func() {
	// aChar = 'a';
	// }
	@SuppressWarnings("restriction")
	public void testChar() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("char aChar;", result);
	}

	// void func() {
	// aDouble = 40.;
	// }
	public void testDouble() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("double aDouble;", result);
	}

	// void func() {
	// aString = "foo";
	// }
	public void testString() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("const char *aString;", result);
	}

	// void func() {
	// aWString = L"foo";
	// }
	public void testWString() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("const wchar_t *aWString;", result);
	}

	// void func() {
	// aFuncPtr = func;
	// }
	public void testFuncPtr() {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("void (*aFuncPtr)();", result);
	}
}
