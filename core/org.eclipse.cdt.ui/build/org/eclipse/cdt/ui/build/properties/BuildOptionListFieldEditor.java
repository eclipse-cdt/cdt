package org.eclipse.cdt.ui.build.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Composite;

public class BuildOptionListFieldEditor extends ListEditor {
	private static final String TITLE = "BuildPropertyCommon.label.title";	//$NON-NLS-1$

	private boolean browse;
	private String fieldName;
		
	/**
 	* @param name the name of the preference this field editor works on
 	* @param labelText the label text of the field editor
 	* @param parent the parent of the field editor's control
 	*/
	public BuildOptionListFieldEditor (String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		this.fieldName = labelText;
		createControl(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#createList(java.lang.String[])
	 */
	protected String createList(String[] items) {
		return BuildToolsSettingsStore.createList(items);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#getNewInputObject()
	 */
	protected String getNewInputObject() {
		// Create a dialog to prompt for a new symbol or path
		InputDialog dialog = new InputDialog(getShell(), CUIPlugin.getResourceString(TITLE), fieldName, new String(), null);
		String input = null;
		if (dialog.open() == InputDialog.OK) {
			input = dialog.getValue();
		}
		return input.length() == 0 ? null : input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.ListEditor#parseString(java.lang.String)
	 */
	protected String[] parseString(String stringList) {
		return BuildToolsSettingsStore.parseString(stringList);
	}
}
