/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.cdt.ui.CUIPlugin;

public class RefactoringPreferences {

	public static final String PREF_ERROR_PAGE_SEVERITY_THRESHOLD= PreferenceConstants.REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD;
	public static final String PREF_SAVE_ALL_EDITORS= PreferenceConstants.REFACTOR_SAVE_ALL_EDITORS;
	
	public static int getCheckPassedSeverity() {
		String value= CUIPlugin.getDefault().getPreferenceStore().getString(PREF_ERROR_PAGE_SEVERITY_THRESHOLD);
		try {
			return Integer.valueOf(value).intValue() - 1;
		} catch (NumberFormatException e) {
			return RefactoringStatus.ERROR;
		}
	}
	
	public static int getStopSeverity() {
		switch (getCheckPassedSeverity()) {
			case RefactoringStatus.OK:
				return RefactoringStatus.INFO;
			case RefactoringStatus.INFO:
				return RefactoringStatus.WARNING;
			case RefactoringStatus.WARNING:
				return RefactoringStatus.ERROR;
		}
		return RefactoringStatus.FATAL;
	}
	
	public static boolean getSaveAllEditors() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(PREF_SAVE_ALL_EDITORS);
	}
	
	public static void setSaveAllEditors(boolean save) {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(RefactoringPreferences.PREF_SAVE_ALL_EDITORS, save);
	}	
}
