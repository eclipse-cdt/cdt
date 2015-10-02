/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LaunchBarPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public LaunchBarPreferencePage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
	    setPreferenceStore(Activator.getDefault().getPreferenceStore());
	    setDescription(Messages.LaunchBarPreferencePage_0);
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(Activator.PREF_ENABLE_LAUNCHBAR, Messages.LaunchBarPreferencePage_1, getFieldEditorParent()));
	}
	
}
