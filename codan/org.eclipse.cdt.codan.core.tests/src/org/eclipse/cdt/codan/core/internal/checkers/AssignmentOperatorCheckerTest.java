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
import org.eclipse.cdt.codan.internal.checkers.AssignmentOperatorChecker;

/**
 * Test for {@link AssignmentOperatorChecker} class
 */
public class AssignmentOperatorCheckerTest extends CheckerTestCase {

	public static final String MISS_REF_ID = AssignmentOperatorChecker.MISS_REF_ID;
	public static final String MISS_SELF_ID = AssignmentOperatorChecker.MISS_SELF_CHECK_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(MISS_REF_ID, MISS_SELF_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//class Foo {
	//public:
	//Foo& operator=(const Foo& f);
	//};
	//Foo& Foo::operator=(const Foo& f) {
	//    if (this != &f) {
	//        return *this;
	//    }
	//}
	public void testWithNoError() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_REF_ID);
		checkNoErrorsOfKind(MISS_SELF_ID);
	}

	//class Foo {
	//public:
	//Foo operator=(const Foo& f);
	//};
	//Foo Foo::operator=(const Foo& f) {
	//    if (this != &f) {
	//        return *this;
	//    }
	//    return *this;
	//}
	public void testWithReturnByCopyPassByRef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, MISS_REF_ID);
		checkNoErrorsOfKind(MISS_SELF_ID);
	}

	//class Foo {
	//public:
	//Foo operator=(Foo f);
	//};
	//Foo Foo::operator=(Foo f) {
	//    if (this != &f) {
	//        return *this;
	//    }
	//    return *this;
	//}
	public void testWithReturnByCopyPassByValue() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, MISS_REF_ID);
		checkNoErrorsOfKind(MISS_SELF_ID);
	}

	//class Foo {
	//public:
	//Foo& operator=(const Foo& f);
	//};
	//Foo& Foo::operator=(const Foo& f) {
	//  return *this;
	//}
	public void testWithNoSelfCheckPassByRef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_REF_ID);
		checkErrorLine(5, MISS_SELF_ID);
	}

	//class Foo {
	//public:
	//Foo& operator=(Foo f);
	//};
	//Foo& Foo::operator=(Foo f) {
	//  return *this;
	//}
	public void testWithNoSelfCheckPassByValue() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_REF_ID);
		checkErrorLine(5, MISS_SELF_ID);
	}

	//class Foo {
	//private:
	//int p;
	//public:
	//Foo& operator=(const Foo& f);
	//};
	//Foo& Foo::operator=(const Foo& f) {
	//    if (this == &f) {
	//        return *this;
	//    }
	//    p = f.p;
	//    return *this;
	//}
	public void testWithEqSelfCheck() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_REF_ID);
		checkNoErrorsOfKind(MISS_SELF_ID);
	}

	//class Foo {
	//private:
	//int p;
	//public:
	//void operator=(const int& f);
	//};
	//void Foo::operator=(const int& f) {
	//    p = f.p;
	//}
	public void testWithOpEqNoAssignment() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(7, MISS_REF_ID);
		checkNoErrorsOfKind(MISS_SELF_ID);
	}

}
