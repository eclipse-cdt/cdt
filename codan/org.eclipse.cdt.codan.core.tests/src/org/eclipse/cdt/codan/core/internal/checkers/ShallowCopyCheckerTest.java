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
}
