/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.CaseBreakChecker;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

@SuppressWarnings("restriction")
public class CaseBreakQuickFixFallthroughAttributeTest extends QuickFixTestCase {

	private boolean wasEnabled;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		wasEnabled = (boolean) getPreference(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_ENABLE_FALLTHROUGH_QUICKFIX)
				.getValue();
		setPreferenceValue(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_ENABLE_FALLTHROUGH_QUICKFIX, true);
	}

	@Override
	public void tearDown() throws Exception {
		setPreferenceValue(CaseBreakChecker.ER_ID, CaseBreakChecker.PARAM_ENABLE_FALLTHROUGH_QUICKFIX, wasEnabled);
		super.tearDown();
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new CaseBreakQuickFixFallthroughAttribute();
	}

	//void hello() {}
	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1:
	//		hello();
	//	case 2:
	//		break;
	//	}
	//}
	public void testSimpleCase_514685() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("[[fallthrough]];\tcase 2:", result);
	}

	//void hello() {}
	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1:
	//		hello();
	//		hello();
	//	case 2:
	//		break;
	//	}
	//}
	public void testMultipleStatementsCase_514685() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("[[fallthrough]];\tcase 2:", result);
	}

	//void hello() {}
	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1: {
	//		hello();
	//	}
	//	case 2:
	//		break;
	//	}
	//}
	public void testCompositeCase_514685() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("[[fallthrough]];\t}\tcase 2:", result);
	}

	//void hello() {}
	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1: {
	//		{
	//			hello();
	//			{
	//				hello();
	//			}
	//		}
	//	}
	//	case 2:
	//		break;
	//	}
	//}
	public void testNestedCompositeCase_514685() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("[[fallthrough]];\t}\tcase 2:", result);
	}
}
