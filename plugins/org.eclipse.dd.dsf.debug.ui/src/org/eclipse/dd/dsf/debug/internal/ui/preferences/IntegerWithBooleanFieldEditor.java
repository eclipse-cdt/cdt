/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.ui.preferences;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * An integer field editor with an enablement check box.
 */
public class IntegerWithBooleanFieldEditor extends IntegerFieldEditor {

	private final String fEnableKey;
	private Button fCheckbox;
	private boolean fWasSelected;

	public IntegerWithBooleanFieldEditor(String enableKey, String nameKey, String labelText, Composite parent) {
		super(nameKey, labelText, parent);
		fEnableKey= enableKey;
	}

	public IntegerWithBooleanFieldEditor(String enableKey, String nameKey, String labelText, Composite parent, int textLimit) {
		super(nameKey, labelText, parent, textLimit);
		fEnableKey= enableKey;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getCheckboxControl(parent);
		super.doFillIntoGrid(parent, numColumns);
	}

	@Override
	public int getNumberOfControls() {
		return super.getNumberOfControls() + 1;
	}

    @Override
	protected void adjustForNumColumns(int numColumns) {
    	// the checkbox uses one column
    	super.adjustForNumColumns(numColumns - 1);
    }

	private Button getCheckboxControl(Composite parent) {
		if (fCheckbox == null) {
			fCheckbox= new Button(parent, SWT.CHECK);
			fCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
                    boolean isSelected = fCheckbox.getSelection();
                    valueChanged(fWasSelected, isSelected);
                    fWasSelected = isSelected;
				}
			});
		} else {
			checkParent(fCheckbox, parent);
		}
		return fCheckbox;
	}

	protected void valueChanged(boolean oldValue, boolean newValue) {
        setPresentsDefaultValue(false);
        if (oldValue != newValue) {
			fireStateChanged(VALUE, oldValue, newValue);
        	getTextControl().setEnabled(newValue);
        	getLabelControl().setEnabled(newValue);
		}
	}

	@Override
	protected boolean checkState() {
		if (fCheckbox != null && !fCheckbox.getSelection()) {
			return true;
		}
		return super.checkState();
	}

	@Override
	protected void doLoad() {
		super.doLoad();
        if (fCheckbox != null) {
            boolean value = getPreferenceStore().getBoolean(fEnableKey);
            fCheckbox.setSelection(value);
            fWasSelected = value;
        	getTextControl().setEnabled(value);
        	getLabelControl().setEnabled(value);
        }
	}

	@Override
	protected void doLoadDefault() {
		super.doLoadDefault();
        if (fCheckbox != null) {
            boolean value = getPreferenceStore().getDefaultBoolean(fEnableKey);
            fCheckbox.setSelection(value);
            fWasSelected = value;
        	getTextControl().setEnabled(value);
        	getLabelControl().setEnabled(value);
        }
	}

    @Override
	protected void doStore() {
    	super.doStore();
        getPreferenceStore().setValue(fEnableKey, fCheckbox.getSelection());
    }

    /**
     * Returns this field editor's current boolean value.
     *
     * @return the value
     */
    public boolean getBooleanValue() {
        return fCheckbox.getSelection();
    }

}
