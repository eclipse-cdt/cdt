/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class AbstractConfigurePropertyOptionsPage extends FieldEditorPreferencePage {

	private boolean dirty = false;
	private String name;

	protected AbstractConfigurePropertyOptionsPage(String name) {
		this(GRID);
		this.name = name;
	}

	protected String getName() {
		return name;
	}

	protected AbstractConfigurePropertyOptionsPage(int style) {
		super(style);
		noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		// Get the preference store for the build settings
		IPreferenceStore settings = AutotoolsConfigurePrefStore.getInstance();
		setPreferenceStore(settings);
	}

	/**
	 * Method called when the value of a dialog field changes
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			setDirty(true);
		}
	}

	public void setDirty(boolean b) {
		dirty = b;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void storeSettings() {
		super.performOk();
	}

	public abstract void updateFields();

	public abstract void setValues();

}
