/*******************************************************************************
 * Copyright (c) 2017 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

public class CaseBreakQuickFixCommentTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new CaseBreakQuickFixComment();
	}

	@Override
	public boolean isCpp() {
		return true;
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
	public void testSimpleCase_515814() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("/* no break */\tcase 2:", result);
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
	public void testCompositeCase_515814() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("/* no break */\tcase 2:", result);
	}
}
