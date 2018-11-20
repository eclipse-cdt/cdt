/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin.util;

import java.util.ArrayList;

/*
 * This test pass verifies visually the sizing of the dialog and its widgets.
 */
class SizingTestPass implements IDialogTestPass {
	private static final int CHECKLIST_SIZE = 5;

	@Override
	public String title() {
		return "Test Pass: Sizing and Display";
	}

	@Override
	public String description() {
		return "Verify the sizing and display of the dialogs and widgets.";
	}

	@Override
	public String label() {
		return "&Sizing and Display";
	}

	@Override
	public ArrayList<String> checkListTexts() {
		ArrayList<String> list = new ArrayList<>(CHECKLIST_SIZE);
		list.add("&1) the correct dialog displays.");
		list.add("&2) the dialog is an appropriate size for the required resolution (1024x768).");
		list.add("&3) the texts are correct and not cut off.");
		list.add("&4) all strings have been externalized properly.");
		list.add("&5) all the widgets are viewable and not cut off.");
		return list;
	}

	@Override
	public String[] failureTexts() {
		String[] failureText = new String[CHECKLIST_SIZE];
		failureText[0] = "The wrong dialog displayed.";
		failureText[1] = "The dialog is too large for the required resolution.";
		failureText[2] = "Text labels are wrong or cut off.";
		failureText[3] = "Some strings have not been externalized properly.";
		failureText[4] = "Some widgets are cut off.";
		return failureText;
	}

	@Override
	public String queryText() {
		return "Is the sizing and display of the dialog correct?";
	}

	@Override
	public int getID() {
		return VerifyDialog.TEST_SIZING;
	}
}