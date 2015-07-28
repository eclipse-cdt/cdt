/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.preferences;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ArduinoPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ArduinoPreferencePage() {
		super(GRID);
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		// TODO Auto-generated method stub
		return super.getPreferenceStore();
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(ArduinoPreferences.ARDUINO_HOME, Messages.ArduinoPreferencePage_0,
				getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription(Messages.ArduinoPreferencePage_1);
		// Preferences are stored in core
		setPreferenceStore(Activator.getDefault().getCorePreferenceStore());
	}

}
