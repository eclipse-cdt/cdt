/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin.util;

import java.util.ArrayList;

public class AccessibilityTestPass implements IDialogTestPass {
	private static final int CHECKLIST_SIZE = 5;

	@Override
	public String title() {
		return "Test Pass: Accessibility";
	}

	@Override
	public String description() {
		return "Verify the accessibility of the dialogs.";
	}

	@Override
	public String label() {
		return "&Accessibility";
	}

	@Override
	public ArrayList<String> checkListTexts() {
		ArrayList<String> list = new ArrayList<>(CHECKLIST_SIZE);
		list.add("&1) all widgets are accessible by tabbing.");
		list.add("&2) forwards and backwards tabbing is in a logical order");
		list.add("&3) all the widgets with labels have an appropriate mnemonic.");
		list.add("&4) there are no duplicate mnemonics.");
		list.add("&5) selectable widgets can be selected using the spacebar.");
		return list;
	}

	@Override
	public String[] failureTexts() {
		String[] failureText = new String[CHECKLIST_SIZE];
		failureText[0] = "Some widgets aren't accessible by tabbing.";
		failureText[1] = "Tabbing order is illogical.";
		failureText[2] = "Missing or inappropriate mnemonics.";
		failureText[3] = "Duplicate mnemonics.";
		failureText[4] = "Some widgets cannot be selected using the spacebar.";
		return failureText;
	}

	@Override
	public String queryText() {
		return "Is the accessibility of the dialog acceptable?";
	}

	@Override
	public int getID() {
		return VerifyDialog.TEST_ACCESS;
	}
}