/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marco Stornelli - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.CStyleCastChecker;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

public class QuickFixCStyleCastTest extends QuickFixTestCase {
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return null;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(CStyleCastChecker.ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//void func() {
	//	int a;
	//	double b = (double) a;
	//}
	public void testStaticCast() throws Exception {
		setQuickFix(new QuickFixCppCastStatic());
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("double b = static_cast<double>(a);", result);
	}

	//typedef int MyInt;
	//void func() {
	//	MyInt a;
	//	double b = (MyInt) a;
	//}
	public void testStaticCastTypedef() throws Exception {
		setQuickFix(new QuickFixCppCastStatic());
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("double b = static_cast<MyInt>(a);", result);
	}

	//using MyInt = int;
	//void func() {
	//	MyInt a;
	//	double b = (MyInt) a;
	//}
	public void testStaticCastTypeAlias() throws Exception {
		setQuickFix(new QuickFixCppCastStatic());
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("double b = static_cast<MyInt>(a);", result);
	}

	//void func() {
	//	int *a;
	//	double b = (double) a;
	//}
	public void testReinterpretCast() throws Exception {
		setQuickFix(new QuickFixCppCastReinterpret());
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("double b = reinterpret_cast<double>(a);", result);
	}

	//void func() {
	//	const int a;
	//	int b = (int) a;
	//}
	public void testConstCast() throws Exception {
		setQuickFix(new QuickFixCppCastConst());
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("int b = const_cast<int>(a);", result);
	}

	//class Base {}
	//class Child1: public Base {}
	//void func() {
	//	Base* b = new Child1();
	//	Child1 *c = (Child1*) b;
	//}
	public void testDynamicCast() throws Exception {
		setQuickFix(new QuickFixCppCastDynamic());
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("Child1 *c = dynamic_cast<Child1*>(b);", result);
	}
}
