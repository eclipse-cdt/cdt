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
import org.eclipse.cdt.codan.internal.checkers.VirtualMethodCallChecker;

/**
 * Test for {@link VirtualMethodCallChecker} class
 */
public class VirtualMethodCallCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = VirtualMethodCallChecker.VIRTUAL_CALL_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//	pure();
	//}
	//Foo::~Foo() {
	//}
	//Foo::bar() {
	//}
	public void testWithPureInCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(10, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//	notpure();
	//}
	//Foo::~Foo() {
	//}
	//Foo::bar() {
	//}
	public void testWithNotPureInCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(10, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//}
	//Foo::~Foo() {
	//	pure();
	//}
	//Foo::bar() {
	//}
	public void testWithPureInDtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(12, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//}
	//Foo::~Foo() {
	//	notpure();
	//}
	//Foo::bar() {
	//}
	public void testWithNotPureInDtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(12, ERR_ID);
	}

	//class Foo {
	//public:
	//Foo();
	//~Foo();
	//virtual void bar();
	//virtual void pure() = 0;
	//virtual void notpure();
	//};
	//Foo::Foo() {
	//}
	//Foo::~Foo() {
	//}
	//Foo::bar() {
	//	pure();
	//	notpure();
	//}
	public void testWithVirtualInMethod() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
