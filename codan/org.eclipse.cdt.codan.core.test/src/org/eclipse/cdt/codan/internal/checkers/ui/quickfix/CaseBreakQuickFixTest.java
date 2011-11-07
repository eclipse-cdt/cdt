/*******************************************************************************
 * Copyright (c) 2010, 2011 Tomasz Wesolowski and others
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

@SuppressWarnings("nls")
public class CaseBreakQuickFixTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new CaseBreakQuickFixBreak();
	}

	// void func() {
	//	 int a;
	//   switch(a) {
	//	   case 1:
	//	     hello();
	//     case 2:
	//	 }
	// }
	public void testMiddleCase() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("break;     case 2:", result);
	}

	// void func() {
	//	 int a;
	//   switch(a) {
	//	   case 1:
	//	     hello();
	//	 }
	// }
	public void testLastCase() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("break;	 }", result);
	}

	// void func() {
	//	 int a;
	//   switch(a) {
	//	   case 1: {
	//	     hello();
	//     }
	//	 }
	// }
	public void testLastCaseComp() throws Exception {
		loadcode(getAboveComment());
		String result = runQuickFixOneFile();
		assertContainedIn("hello();\t\tbreak;", result);
	}
}
