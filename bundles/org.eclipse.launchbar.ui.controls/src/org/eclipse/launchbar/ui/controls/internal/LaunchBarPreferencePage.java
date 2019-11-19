/*******************************************************************************
 * Copyright (c) 2014, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *     Torkild U. Resheim - add preference to control target selector
 *     Vincent Guignot - Ingenico - add preference to control Build button
 *******************************************************************************/
package org.eclipse.launchbar.ui.controls.internal;

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
		addField(new BooleanFieldEditor(Activator.PREF_ENABLE_BUILDBUTTON,
				Messages.LaunchBarPreferencePage_EnableBuildButton, getFieldEditorParent()));
		addField(new BooleanFieldEditor(Activator.PREF_ALWAYS_TARGETSELECTOR,
				Messages.LaunchBarPreferencePage_AlwaysTargetSelector, getFieldEditorParent()));
	}

}
