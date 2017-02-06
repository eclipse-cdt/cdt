/*******************************************************************************
 * Copyright (c) 2010, 2011 Tomasz Wesolowski and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ProblemBindingChecker;
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

	@Override
	public boolean isCpp() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.codan.core.test.CodanTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ProblemBindingChecker.ERR_ID_FieldResolutionProblem, ProblemBindingChecker.ERR_ID_MethodResolutionProblem,
				ProblemBindingChecker.ERR_ID_VariableResolutionProblem);
	}

	// void func() {
	// aChar = 'a';
	// }
	public void testChar() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("char aChar;", result); //$NON-NLS-1$
	}

	// void func() {
	// aDouble = 40.;
	// }
	public void testDouble() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("double aDouble;", result); //$NON-NLS-1$
	}

	// void func() {
	// aString = "foo";
	// }
	public void testString() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("const char* aString;", result); //$NON-NLS-1$
	}

	// void func() {
	// aWString = L"foo";
	// }
	public void testWString() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("const wchar_t* aWString;", result); //$NON-NLS-1$
	}

	// void func() {
	// aFuncPtr = func;
	// }
	public void testFuncPtr() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("void (*aFuncPtr)();", result); //$NON-NLS-1$
	}

	//class Foo {
	//  void bar(char);
	//};
	//void func() {
	//Foo foo;
	//foo.bar(aChar);
	//}
	public void testInMethodCall() throws Exception {
		loadcode(getAboveComment());
		indexFiles();
		String result = runQuickFixOneFile();
		assertContainedIn("char aChar", result); //$NON-NLS-1$
	}
}
