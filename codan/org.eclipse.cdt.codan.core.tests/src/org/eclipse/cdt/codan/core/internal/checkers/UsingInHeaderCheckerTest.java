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
import org.eclipse.cdt.codan.internal.checkers.UsingInHeaderChecker;

/**
 * Test for {@link UsingInHeaderChecker} class
 */
public class UsingInHeaderCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = UsingInHeaderChecker.ERR_ID;

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

	//using namespace std;
	//class Foo {
	//public:
	//void bar() {
	//}
	//};
	public void testUsingInGlobalNamespace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//namespace GLOBAL {
	//using namespace std;
	//}
	//class Foo {
	//public:
	//void bar() {
	//}
	//};
	public void testUsingInNamespace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//class Foo {
	//public:
	//void bar() {
	//}
	//};
	public void testWithoutUsing() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//void bar() {
	//using namespace std;
	//}
	//};
	public void testUsingInFunctionDeclaration() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
