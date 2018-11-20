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
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin.util;

import java.util.ArrayList;

/*
 * This test pass verifies the initial focus of a dialog when it is given focus.
 */
public class FocusTestPass implements IDialogTestPass {
	private static final int CHECKLIST_SIZE = 1;

	/**
	 * @see IDialogTestPass#title()
	 */
	@Override
	public String title() {
		return "Test Pass: Initial Focus";
	}

	/**
	 * @see IDialogTestPass#description()
	 */
	@Override
	public String description() {
		return "Verify the initial focus of the dialogs.";
	}

	/**
	 * @see IDialogTestPass#label()
	 */
	@Override
	public String label() {
		return "&Initial Focus";
	}

	/**
	 * @see IDialogTestPass#checkListTexts()
	 */
	@Override
	public ArrayList<String> checkListTexts() {
		ArrayList<String> list = new ArrayList<>(CHECKLIST_SIZE);
		list.add("&1) the initial focus is appropriate.");
		return list;
	}

	/**
	 * @see IDialogTestPass#failureTexts()
	 * Size of the return array must be the same size as the checkListTexts'
	 * ArrayList.
	 */
	@Override
	public String[] failureTexts() {
		String[] failureText = new String[CHECKLIST_SIZE];
		failureText[0] = "The initial focus is inappropriate.";
		return failureText;
	}

	/**
	 * @see IDialogTestPass#queryText()
	 */
	@Override
	public String queryText() {
		return "Is the initial focus of the dialog correct?";
	}

	/**
	 * @see IDialogTestPass#getID()
	 */
	@Override
	public int getID() {
		return VerifyDialog.TEST_FOCUS;
	}
}