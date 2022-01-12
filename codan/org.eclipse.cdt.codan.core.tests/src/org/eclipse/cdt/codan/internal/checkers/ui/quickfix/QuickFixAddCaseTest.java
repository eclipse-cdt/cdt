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

import org.eclipse.cdt.codan.internal.checkers.SwitchCaseChecker;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

public class QuickFixAddCaseTest extends QuickFixTestCase {

	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new QuickFixAddCaseSwitch();
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(SwitchCaseChecker.MISS_CASE_ID);
	}

	//enum FRUIT {
	// APPLE, PEAR, BANANA
	//};
	//void func() {
	//	FRUIT f = APPLE;
	//	switch(f) {
	//	case APPLE:
	//		break;
	//	}
	//}
	public void testAddCase() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("PEAR:", result); //$NON-NLS-1$
		assertContainedIn("BANANA:", result); //$NON-NLS-1$
	}

	//enum FRUIT {
	// APPLE, PEAR, BANANA
	//};
	//void func() {
	//FRUIT f = APPLE;
	//switch (f)
	//{
	//
	//}
	//}
	public void testAddCase2() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("PEAR:", result); //$NON-NLS-1$
		assertContainedIn("BANANA:", result); //$NON-NLS-1$
		assertContainedIn("APPLE:", result); //$NON-NLS-1$
	}

	//enum FRUIT {
	// APPLE, PEAR, BANANA
	//};
	//void func() {
	//FRUIT f = APPLE;
	//switch (f)
	//	;
	//}
	public void testAddCase3() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("PEAR:", result); //$NON-NLS-1$
		assertContainedIn("BANANA:", result); //$NON-NLS-1$
		assertContainedIn("APPLE:", result); //$NON-NLS-1$
	}

	//enum FRUIT {
	// APPLE, PEAR, BANANA
	//};
	//void func() {
	//FRUIT f = APPLE;
	//switch (f)
	//	case APPLE:
	//		break;
	//}
	public void testAddCase4() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("PEAR:", result); //$NON-NLS-1$
		assertContainedIn("BANANA:", result); //$NON-NLS-1$
	}
}
