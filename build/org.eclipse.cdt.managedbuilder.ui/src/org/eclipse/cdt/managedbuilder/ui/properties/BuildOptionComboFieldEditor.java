/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
		optionSelector.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				String oldValue = selected;
				String name = optionSelector.getText();
				int index = optionSelector.getSelectionIndex();
				selected = index == -1 ? new String() : optionSelector.getItem(index);
				setPresentsDefaultValue(false);
				fireValueChanged(VALUE, oldValue, selected);					
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		// set all the options to option selector
		optionSelector.removeAll();
		optionSelector.setItems(options);

		// get the selected option from preference store
		selected = getPreferenceStore().getString(getPreferenceName());
		
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
		selected = index == -1 ? new String() : optionSelector.getItem(index);
		getPreferenceStore().setValue(getPreferenceName(), selected);
	}
	
	public String getSelection(){
		return selected;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		// There is just the label from the parent and the combo
		return 2;
	}

    /**
     * Set whether or not the controls in the field editor
     * are enabled.
     * @param enabled The enabled state.
     * @param parent The parent of the controls in the group.
     *  Used to create the controls if required.
     */
    public void setEnabled(boolean enabled, Composite parent) {
        getLabelControl(parent).setEnabled(enabled);
        optionSelector.setEnabled(enabled);
    }
}
