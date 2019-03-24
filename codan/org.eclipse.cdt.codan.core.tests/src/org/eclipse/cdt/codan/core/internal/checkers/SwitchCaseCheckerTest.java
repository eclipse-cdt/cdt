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
import org.eclipse.cdt.codan.internal.checkers.SwitchCaseChecker;

/**
 * Test for {@link SwitchCaseChecker} class
 */
public class SwitchCaseCheckerTest extends CheckerTestCase {

	public static final String ERR_ID = SwitchCaseChecker.ERR_ID;

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

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//FRUIT getFruit() {
	//	return APPLE;
	//}
	//int main() {
	//	switch (FRUIT p = getFruit(); p) {
	//	case APPLE:
	//	case PEAR:
	//	case BANANA:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchWithInitClause() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//	case BANANA:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchCompleteEnum() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchMissEnum() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//	case 2:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchWithMixedValues() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//	default:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchDefaultClausePresent() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
