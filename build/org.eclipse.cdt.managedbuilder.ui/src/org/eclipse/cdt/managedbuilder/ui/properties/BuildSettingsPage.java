/*******************************************************************************
 * Copyright (c) 2004, 2005 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class BuildSettingsPage extends FieldEditorPreferencePage {
	protected IConfiguration clonedConfig;
	protected IResourceConfiguration clonedResConfig;

	private boolean dirty = false; 

	/**
	 * @param style
	 */
	protected BuildSettingsPage(IConfiguration clonedConfig) {
 		// fix for PR 63973
 		// If we use a grid layout then widgets that should be layed out horizontally,
 		// e.g. StringButtonFieldEditor, will have their component widgets
 		// arranged vertically.  This looks terrible when you have for instance
 		// a StringButtonFieldEditor, which has a label, an edit box, and a "modify" button
 		// to the right because all three will be stacked vertically.
 		super(FLAT);
 		// end fix for 63973
		noDefaultAndApplyButton();
		this.clonedConfig = clonedConfig;
	}

	protected BuildSettingsPage(IResourceConfiguration clonedResConfig) {
 		// fix for PR 63973
 		// If we use a grid layout then widgets that should be layed out horizontally,
 		// e.g. StringButtonFieldEditor, will have their component widgets
 		// arranged vertically.  This looks terrible when you have for instance
 		// a StringButtonFieldEditor, which has a label, an edit box, and a "modify" button
 		// to the right because all three will be stacked vertically.
 		super(FLAT);
 		// end fix for 63973
		noDefaultAndApplyButton();
		
		this.clonedResConfig = clonedResConfig;
		this.clonedConfig = clonedResConfig.getParent();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		// Get the preference store for the build settings
		IPreferenceStore settings = getToolSettingsPreferenceStore();
		setPreferenceStore(settings);
	}

	/**
	 * Return the tool settings preference store
	 */
	protected BuildToolSettingsPreferenceStore getToolSettingsPreferenceStore() {
		IPreferencePageContainer container = getContainer();
		if (container instanceof BuildPropertyPage) {
			return ((BuildPropertyPage)container).getToolSettingsPreferenceStore();
		} else if ( container instanceof ResourceBuildPropertyPage) {
			return ((ResourceBuildPropertyPage)container).getToolSettingsPreferenceStore();
		}
		return null;
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

	/**
	 * Sets the "dirty" state
	 */
	public void setDirty(boolean b) {
	    dirty = b;
	}

	/**
	 * Returns the "dirty" state
	 */
	public boolean isDirty() {
	    return dirty;
	}

}
