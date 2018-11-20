/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

public class AssignmentInConditionQuickFixTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new QuickFixAssignmentInCondition();
	}

	//	void func() {
	//		int a, b;
	//		if (a == 1 && b = 2) {
	//		}
	//	}
	public void testComparisonAndAssignment_bug352267() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("b == 2", result);
	}
}
