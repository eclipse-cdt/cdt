/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.CatchByReference;

/**
 * Test for {@see CatchByReference} class
 */
public class CatchByReferenceTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(CatchByReference.ER_ID);
	}

	//	void main() {
	//		try {
	//			foo();
	//		} catch (int e) {
	//		}
	//	}
	public void test_int() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {};
	// void main() {
	//		try {
	//			foo();
	//		} catch (C e) {
	//		}
	//	}
	public void test_class() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	// class C {};
	// void main() {
	//		try {
	//			foo();
	//		} catch (C & e) {
	//		}
	//	}
	public void test_class_ref() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// class C {};
	// void main() {
	//		try {
	//			foo();
	//		} catch (C * e) {
	//		}
	//	}
	public void test_class_point() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// typedef int A;
	// void main() {
	//		try {
	//			foo();
	//		} catch (A e) {
	//		}
	//	}
	public void test_int_typedef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// typedef int A;
	// typedef A B;
	//	void main() {
	//		try {
	//			foo();
	//		} catch (B e) {
	//		}
	//	}
	public void test_int_typedef2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	//		try {
	//			foo();
	//		} catch (C e) {
	//		}
	//	}
	public void test_class_unknown() throws Exception {
		setPreferenceValue(CatchByReference.ER_ID, CatchByReference.PARAM_UNKNOWN_TYPE, false);
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void main() {
	//		try {
	//			foo();
	//		} catch (C e) {
	//		}
	//	}
	public void test_class_unknown_on() throws Exception {
		setPreferenceValue(CatchByReference.ER_ID, CatchByReference.PARAM_UNKNOWN_TYPE, true);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	// class C {};
	// typedef C B;
	// void main() {
	//		try {
	//			foo();
	//		} catch (B e) {
	//		}
	//	}
	public void test_class_typedef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6);
	}
}
