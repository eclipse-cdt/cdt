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
import org.eclipse.cdt.codan.internal.checkers.CopyrightChecker;

/**
 * Test for {@link CopyrightChecker} class
 */
public class CopyrightCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = CopyrightChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//int main() {return 0;}
	public void testWithoutCopyright() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	////Copyright 2019
	//int main() {return 0;}
	public void testWithCopyright() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	////============================================================================
	//// Name : test.cpp
	//// Author : Blah
	//// Version : 1.0
	//// Copyright : Your copyright notice
	//// Description : Hello World in C++, Ansi-style
	////============================================================================
	//
	//int main() {return 0;}
	public void testWithCopyrightMultiline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	////============================================================================
	//// Name : test.cpp
	//// Author : Blah
	//// Version : 1.0
	//// Description : Hello World in C++, Ansi-style
	////============================================================================
	//
	//int main() {return 0;}
	public void testWithoutCopyrightMultiline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//// Just another comment here
	//
	////============================================================================
	//// Name : test.cpp
	//// Author : Blah
	//// Version : 1.0
	//// Copyright : Your copyright notice
	//// Description : Hello World in C++, Ansi-style
	////============================================================================
	//
	//int main() {return 0;}
	public void testWithCopyrightMultilineNoHeader() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	////============================================================================
	//// Name : test.cpp
	//// Author : Blah
	//// Version : 1.0
	//// Copyright : Your copyright notice
	//// Description : Hello World in C++, Ansi-style
	////============================================================================
	//
	//// Just another comment here
	//int main() {return 0;}
	public void testWithCopyrightMultilinePostComment() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	///****************************************************************************
	// * Name : test.cpp
	// * Author : Blah
	// * Version : 1.0
	// * Copyright : Your copyright notice
	// * Description : Hello World in C++, Ansi-style
	// ****************************************************************************/
	//
	//int main() {return 0;}
	public void testWithCopyrightBlock() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//// Just another comment here
	///****************************************************************************
	// * Name : test.cpp
	// * Author : Blah
	// * Version : 1.0
	// * Copyright : Your copyright notice
	// * Description : Hello World in C++, Ansi-style
	// ****************************************************************************/
	//
	//int main() {return 0;}
	public void testWithCopyrightBlockNoHeader() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}
}
