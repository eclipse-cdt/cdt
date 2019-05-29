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

public class QuickFixAddDefaultTest extends QuickFixTestCase {

	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new QuickFixAddDefaultSwitch();
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(SwitchCaseChecker.MISS_DEFAULT_ID);
	}

	//void func() {
	//	int a = 0;
	//	switch(a) {
	//	case 0:
	//		break;
	//	}
	//}
	public void testAddDefault() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("default:", result); //$NON-NLS-1$
	}

	//void func() {
	//int f = 0;
	//switch (f)
	//{
	//
	//}
	//}
	public void testAddCase2() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("default:", result); //$NON-NLS-1$
	}

	//void func() {
	//int f = 0;
	//switch (f)
	//	;
	//}
	public void testAddCase3() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("default:", result); //$NON-NLS-1$
	}

	//void func() {
	//int f = 0;
	//switch (f)
	//	case 0:
	//		break;
	//}
	public void testAddCase4() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("default:", result); //$NON-NLS-1$
	}
}
