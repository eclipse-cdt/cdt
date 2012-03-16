/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class AbstractConfigurePropertyOptionsPage extends
		FieldEditorPreferencePage {

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
		IPreferenceStore settings = getConfigurePrefStore();
		setPreferenceStore(settings);
	}

	/**
	 * Return the tool settings preference store
	 */
	protected AutotoolsConfigurePrefStore getConfigurePrefStore() {
		return AutotoolsConfigurePrefStore.getInstance();	
	}

	/**
	 * Method called when the value of a dialog field changes
	 */
	public void propertyChange(PropertyChangeEvent event) {
	    super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
		    setDirty(true);
		}
	}

	public void setDirty(boolean b) {  dirty = b; }
	public boolean isDirty() { return dirty; }
	public void storeSettings() { super.performOk(); }

	public abstract void updateFields();
	public abstract void setValues();

}
