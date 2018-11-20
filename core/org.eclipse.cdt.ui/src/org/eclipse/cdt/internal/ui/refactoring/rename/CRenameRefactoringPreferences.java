/*******************************************************************************
 * Copyright (c) 2010, 2011 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;

public class CRenameRefactoringPreferences {
	private static final String DIALOG_SETTINGS_KEY = "CRenameRefactoringInputPage"; //$NON-NLS-1$

	public static final String KEY_IGNORE_VIRTUAL = "ignoreVirtual"; //$NON-NLS-1$
	public static final String KEY_REFERENCES_INV = "references_inv"; //$NON-NLS-1$
	public static final String KEY_COMMENT = "comment"; //$NON-NLS-1$
	public static final String KEY_STRING = "string"; //$NON-NLS-1$
	public static final String KEY_INACTIVE = "inactive"; //$NON-NLS-1$
	public static final String KEY_SCOPE = "scope"; //$NON-NLS-1$
	public static final String KEY_WORKING_SET_NAME = "workingset"; //$NON-NLS-1$

	public static final String KEY_INCLUDE = "include"; //$NON-NLS-1$
	public static final String KEY_MACRO_DEFINITION = "macroDefinition"; //$NON-NLS-1$
	public static final String KEY_PREPROCESSOR = "preprocessor"; //$NON-NLS-1$
	public static final String KEY_EXHAUSTIVE_FILE_SEARCH = "exhausiveFileSearch"; //$NON-NLS-1$

	private IDialogSettings fDialogSettings;

	public CRenameRefactoringPreferences() {
		super();
		IDialogSettings ds = CUIPlugin.getDefault().getDialogSettings();
		fDialogSettings = ds.getSection(DIALOG_SETTINGS_KEY);
		if (fDialogSettings == null) {
			fDialogSettings = ds.addNewSection(DIALOG_SETTINGS_KEY);
		}
	}

	public boolean getBoolean(String key) {
		return fDialogSettings.getBoolean(key);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String value = fDialogSettings.get(key);
		return value != null ? Boolean.parseBoolean(value) : defaultValue;
	}

	public void put(String key, int value) {
		fDialogSettings.put(key, value);
	}

	public void put(String key, String value) {
		fDialogSettings.put(key, value);
	}

	public void put(String key, boolean value) {
		fDialogSettings.put(key, value);
	}

	public int getScope() {
		try {
			return fDialogSettings.getInt(KEY_SCOPE);
		} catch (Exception e) {
			return TextSearchWrapper.SCOPE_RELATED_PROJECTS;
		}
	}

	public String getWorkingSet() {
		return fDialogSettings.get(KEY_WORKING_SET_NAME);
	}

	public int getOptions() {
		int options = 0;
		if (!getBoolean(KEY_IGNORE_VIRTUAL))
			options |= CRefactory.OPTION_DO_VIRTUAL;
		if (!getBoolean(KEY_REFERENCES_INV))
			options |= CRefactory.OPTION_IN_CODE_REFERENCES;
		if (getBoolean(KEY_INACTIVE))
			options |= CRefactory.OPTION_IN_INACTIVE_CODE;
		if (getBoolean(KEY_COMMENT))
			options |= CRefactory.OPTION_IN_COMMENT;
		if (getBoolean(KEY_STRING))
			options |= CRefactory.OPTION_IN_STRING_LITERAL;
		if (getBoolean(KEY_INCLUDE))
			options |= CRefactory.OPTION_IN_INCLUDE_DIRECTIVE;
		if (getBoolean(KEY_MACRO_DEFINITION))
			options |= CRefactory.OPTION_IN_MACRO_DEFINITION;
		if (getBoolean(KEY_PREPROCESSOR))
			options |= CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE;
		if (getBoolean(KEY_EXHAUSTIVE_FILE_SEARCH))
			options |= CRefactory.OPTION_EXHAUSTIVE_FILE_SEARCH;
		return options;
	}
}
