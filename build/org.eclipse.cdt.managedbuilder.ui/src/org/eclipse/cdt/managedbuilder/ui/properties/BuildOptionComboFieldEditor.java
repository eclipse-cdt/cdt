package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BuildOptionComboFieldEditor extends FieldEditor {

	// Widgets and bookeeping variables
	private Combo optionSelector;
	private String [] options = new String[0];
	private String selected; 
	
	/**
	 * @param name
	 * @param label
	 * @param opts
	 * @param sel
	 * @param parent
	 */
	public BuildOptionComboFieldEditor (String name, String label, String [] opts, String sel, Composite parent) {
		init(name, label);
		options = opts;
		selected = sel;
		createControl(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		// For now grab the excess space
		GridData gd = (GridData)optionSelector.getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		parent.setLayoutData(gd);
		
		// Add the label
		Label label = getLabelControl(parent);
		GridData labelData = new GridData();
		labelData.horizontalSpan = 1;
		labelData.grabExcessHorizontalSpace = false;
		label.setLayoutData(labelData);
		
		// Now add the combo selector
		optionSelector = ControlFactory.createSelectCombo(parent, options, selected);
		GridData selectorData = (GridData) optionSelector.getLayoutData();
		selectorData.horizontalSpan = numColumns - 1;
		selectorData.grabExcessHorizontalSpace = true;
		optionSelector.setLayoutData(selectorData);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		// Retrieve the option string from the store
		String values = getPreferenceStore().getString(getPreferenceName());
		
		// Convert it to a string array
		options = BuildToolsSettingsStore.parseString(values);
		optionSelector.removeAll();
		optionSelector.setItems(options);
		
		// Set the index of selection in the combo box
		int index = optionSelector.indexOf(selected);
		optionSelector.select(index >= 0 ? index : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		doLoad();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {
		// Save the selected item in the store
		int index = optionSelector.getSelectionIndex();
		String selected = index == -1 ? new String() : optionSelector.getItem(index);
		getPreferenceStore().setValue(getPreferenceName(), selected);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		// There is just the label from the parent and the combo
		return 2;
	}
}
