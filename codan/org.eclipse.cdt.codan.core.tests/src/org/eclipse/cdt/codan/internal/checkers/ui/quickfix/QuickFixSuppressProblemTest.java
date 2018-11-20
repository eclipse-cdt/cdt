/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Felix Morgner <fmorgner@hsr.ch> - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.ui.PreferenceConstants;

public class QuickFixSuppressProblemTest extends QuickFixTestCase {
	@SuppressWarnings("restriction")
	@Override
	protected AbstractCodanCMarkerResolution createQuickFix() {
		return new QuickFixSuppressProblem();
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//struct s {};
	//void func() {
	//	try {
	//	} catch (s e) {
	//	}
	//}
	public void testCPPMarkerOnNode_495842() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("} catch (s e) { // @suppress(\"Catching by reference is recommended\")", result);
	}

	//void func() {
	//	int n = 42;
	//
	//	switch (n) {
	//	case 1:
	//		n = 32;
	//	default:
	//		break;
	//	}
	//}
	public void testCPPMarkerNotOnNode_495842() throws Exception {
		loadcode(getAboveComment(), true);
		String result = runQuickFixOneFile();
		assertContainedIn("n = 32; // @suppress(\"No break at end of case\")", result);
	}

	//int func() { }
	public void testCMarker_495842() throws Exception {
		loadcode(getAboveComment(), false);
		String result = runQuickFixOneFile();
		assertContainedIn("int func() { } // @suppress(\"No return\")", result);
	}

	//int func() { }
	public void testMarkerOnLastLineNoNewline_495842() throws Exception {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(loadcode("", false)))) {
			writer.write(getAboveComment().trim());
		}
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.ENSURE_NEWLINE_AT_EOF, false);
		indexFiles();
		String result = runQuickFixOneFile();
		PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.ENSURE_NEWLINE_AT_EOF, true);
		assertContainedIn("int func() { } // @suppress(\"No return\")", result);
	}
}
