/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A string field editor with an enablement check box.
 */
public class StringWithBooleanFieldEditor extends DecoratingStringFieldEditor {

	private final String fEnableKey;
	private Button fCheckbox;
	private boolean fWasSelected;

	public StringWithBooleanFieldEditor(String enableKey, String nameKey, String labelText, Composite parent) {
		super(nameKey, labelText, parent);
		fEnableKey= enableKey;
	}

	public StringWithBooleanFieldEditor(String enableKey, String nameKey, String labelText, int width, Composite parent) {
		super(nameKey, labelText, width, parent);
		fEnableKey= enableKey;
	}

	public StringWithBooleanFieldEditor(String enableKey, String nameKey, String labelText, int width, int strategy, Composite parent) {
		super(nameKey, labelText, width, strategy, parent);
		fEnableKey= enableKey;
	}
	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getCheckboxControl(parent);
		super.doFillIntoGrid(parent, numColumns);
	}

	private Button getCheckboxControl(Composite parent) {
		if (fCheckbox == null) {
			Composite inner= new Composite(parent, SWT.NULL);
			final GridLayout layout= new GridLayout(2, false);
			layout.marginWidth = 0;
			inner.setLayout(layout);
			fCheckbox= new Button(inner, SWT.CHECK);
			fCheckbox.setFont(parent.getFont());
			fCheckbox.setText(getLabelText());
			// create and hide label from base class
			Label label = getLabelControl(inner);
			label.setText(""); //$NON-NLS-1$
			label.setVisible(false);
			fCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
                    boolean isSelected = fCheckbox.getSelection();
                    valueChanged(fWasSelected, isSelected);
                    fWasSelected = isSelected;
				}
			});
		} else {
			checkParent(fCheckbox.getParent(), parent);
		}
		return fCheckbox;
	}

	@Override
	public Label getLabelControl(Composite parent) {
		final Label label= getLabelControl();
		if (label == null) {
			return super.getLabelControl(parent);
		} else {
			checkParent(label.getParent(), parent);
		}
		return label;
	}

	protected void valueChanged(boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
    		valueChanged();
			fireStateChanged(VALUE, oldValue, newValue);
        	getTextControl().setEnabled(newValue);
        	getLabelControl().setEnabled(newValue);
		}
	}

	@Override
	protected boolean checkState() {
		if (fCheckbox != null && !fCheckbox.getSelection()) {
			clearErrorMessage();
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
