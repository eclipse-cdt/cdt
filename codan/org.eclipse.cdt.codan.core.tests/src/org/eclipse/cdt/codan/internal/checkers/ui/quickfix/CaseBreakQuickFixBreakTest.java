/*******************************************************************************
 * Copyright (c) 2010, 2013 Tomasz Wesolowski and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;

public class CaseBreakQuickFixBreakTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new CaseBreakQuickFixBreak();
	}

	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1:
	//		hello();
	//	case 2:
	//		break;
	//	}
	//}
	public void testSimpleCase() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("break;\tcase 2:", result);
	}

	//void func() {
	//	int a;
	//	switch(a) {
	//	case 1: {
	//		hello();
	//	}
	//	default:
	//	}
	//}
	public void testCompositeStatementCase() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("hello();\t\tbreak;", result);
	}

	//	int main() {
	//		int a;
	//		switch(a)
	//		{
	//			case 0:
	//			{
	//			}
	//			default:
	//				break;
	//		}
	//		return 0;
	//	}
	public void testNPE_bug363884() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("break;\t}\t\t\tdefault:", result);
	}
}
