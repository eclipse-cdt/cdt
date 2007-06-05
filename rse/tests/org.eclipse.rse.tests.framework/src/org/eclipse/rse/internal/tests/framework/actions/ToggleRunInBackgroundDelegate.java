/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rse.internal.tests.framework.TestFrameworkPlugin;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ToggleRunInBackgroundDelegate extends Object implements IViewActionDelegate {
	
	public void init(IViewPart view) {
	}

	public void run(IAction action) {
		boolean runInBackground = action.isChecked();
		setPreference(runInBackground);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	private void setPreference(boolean runInBackground) {
		IPreferenceStore store = TestFrameworkPlugin.getDefault().getPreferenceStore();
		store.setValue(TestFrameworkPlugin.PREF_RUN_IN_BACKGROUND, runInBackground);
	}
	
}
