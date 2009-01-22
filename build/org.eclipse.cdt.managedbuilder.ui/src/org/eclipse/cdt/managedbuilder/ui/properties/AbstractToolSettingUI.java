/*******************************************************************************
 * Copyright (c) 2004, 2007 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Intel corp. 2007 - modification for new CDT model. 
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class AbstractToolSettingUI extends FieldEditorPreferencePage {

	protected AbstractCBuildPropertyTab buildPropPage;
	protected IResourceInfo fInfo;
	private boolean dirty = false; 

	/**
	 * @param style
	 */
	protected AbstractToolSettingUI(IResourceInfo info) {
		this(info, GRID);
	}
	
	protected AbstractToolSettingUI(IResourceInfo info, int style) {
 		super(style);
 		noDefaultAndApplyButton();
		fInfo = info;	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		// Get the preference store for the build settings
		IPreferenceStore settings = getToolSettingsPrefStore();
		setPreferenceStore(settings);
	}

	/**
	 * Return the tool settings preference store
	 */
	protected ToolSettingsPrefStore getToolSettingsPrefStore() {
		return ToolSettingsPrefStore.getDefault();	
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
	
	public abstract boolean isFor(Object obj1, Object obj2);
	public abstract void updateFields();
	public abstract void setValues();
	
}
