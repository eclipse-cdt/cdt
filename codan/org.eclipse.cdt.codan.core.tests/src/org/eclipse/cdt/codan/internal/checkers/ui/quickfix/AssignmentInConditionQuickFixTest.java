/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
