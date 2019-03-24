/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.ShallowCopyChecker;

/**
 * Test for {@link ShallowCopyChecker} class
 */
public class ShallowCopyCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = ShallowCopyChecker.PROBLEM_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	private void setOnlyNew(boolean val) {
		IProblemPreference pref = getPreference(ShallowCopyChecker.PROBLEM_ID, ShallowCopyChecker.PARAM_ONLY_NEW);
		pref.setValue(val);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean isHeader() {
		return true;
	}

	//class Foo {
	//public:
	//void* p;
	//};
	public void testWithPointerOnly() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo(const Foo& o);
	//void* p;
	//};
	public void testWithCopyConstrOnly() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo& operator=(const Foo& f);
	//void* p;
	//};
	public void testWithAssignOnly() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo(const Foo& o);
	//Foo& operator=(const Foo& f);
	//void* p;
	//};
	public void testWithCopyMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//};
	public void testWithoutPointers() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	/****************/
	//class Foo {
	//public:
	//Foo(int& f);
	//int& p;
	//};
	public void testWithRefOnly() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo(const Foo& o);
	//Foo(int& f);
	//int& p;
	//};
	public void testWithRefCopyConstrOnly() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo& operator=(const Foo& f);
	//Foo(int& f);
	//int& p;
	//};
	public void testWithRefAssignOnly() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo(const Foo& o);
	//Foo& operator=(const Foo& f);
	//Foo(int& f);
	//int& p;
	//};
	public void testWithRefCopyMethods() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//int* p;
	//};
	//Foo::Foo() {
	//p = new int;
	//}
	public void testOnlyNewWithFieldPtr() throws Exception {
		setOnlyNew(true);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//};
	//Foo::Foo() {
	//int* p = new int;
	//}
	public void testOnlyNewNoField() throws Exception {
		setOnlyNew(true);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//Foo(const Foo& e);
	//Foo& operator=(const Foo& e);
	//int* p;
	//};
	//Foo::Foo() {
	//p = new int;
	//}
	public void testOnlyNewWithFieldPtrCopyPresent() throws Exception {
		setOnlyNew(true);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//Foo(const Foo& e);
	//Foo& operator=(const int& e);
	//int* p;
	//};
	//Foo::Foo() {
	//p = new int;
	//}
	public void testOnlyNewWithOtherCopyAssignment() throws Exception {
		setOnlyNew(true);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(8, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//Foo(const Foo& e);
	//Foo& operator=(const int& e);
	//int* p;
	//};
	public void testWithOtherCopyAssignment() throws Exception {
		setOnlyNew(false);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}
}
