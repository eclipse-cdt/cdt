package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2004 IBM Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.graphics.Point;

public class BuildToolSettingsPage extends BuildSettingsPage {
	// Field editor label
	private static final String COMMAND = "FieldEditors.tool.command";	//$NON-NLS-1$

	// Tool the settings belong to
	private ITool tool;
	
	BuildToolSettingsPage(IConfiguration configuration, ITool tool) {
		// Cache the configuration and tool this page is for
		super(configuration);
		this.tool = tool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#computeSize()
	 */
	public Point computeSize() {
		return super.computeSize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		// Load up the preference store
		super.createFieldEditors();
		
		// Add a string editor to edit the tool command
		StringFieldEditor stringField = new StringFieldEditor(tool.getId(), ManagedBuilderUIPlugin.getResourceString(COMMAND), getFieldEditorParent());
		stringField.setEmptyStringAllowed(false);
		addField(stringField);		
	}

	/**
	 * Answers <code>true</code> if the receiver manages settings for the argument
	 * 
	 * @param tool
	 * @return
	 */
	public boolean isForTool(ITool tool) {
		if (tool != null) {
			return tool.equals(this.tool);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Do the super-class thang
		boolean result =  super.performOk();
		
		// Get the actual value out of the field editor
		String command = getPreferenceStore().getString(tool.getId());
		if (command.length() == 0) {
			return result;
		}
		
		// Ask the build system manager to change the tool command
		ManagedBuildManager.setToolCommand(configuration, tool, command);
		
		return result;
	}
}
