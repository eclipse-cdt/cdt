/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.preferences;

import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContributionUser;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @since 7.0
 */
public class ReadOnlyFieldEditor extends FieldEditor implements ICBreakpointsUIContributionUser {
	protected Label textField;
	protected ICBreakpointsUIContribution contribution;

	public ReadOnlyFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData) textField.getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		// We only grab excess space if we have to
		// If another field editor has more columns then
		// we assume it is setting the width.
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	/**
	 * Fills this field editor's basic controls into the given parent.
	 * <p>
	 * The string field implementation of this <code>FieldEditor</code>
	 * framework method contributes the text field. Subclasses may override
	 * but must call <code>super.doFillIntoGrid</code>.
	 * </p>
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getLabelControl(parent);

		textField = getTextControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 1;

		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;

		textField.setLayoutData(gd);
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	@Override
	protected void doLoad() {
		if (textField != null) {
			String value = getPreferenceStore().getString(getPreferenceName());
			if (contribution!=null) {
				String tryValue = contribution.getLabelForValue(value);
				if (tryValue!=null)
					value = tryValue;
			}
			textField.setText(value);
		}
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	@Override
	protected void doLoadDefault() {
		if (textField != null) {
			String value = getPreferenceStore().getDefaultString(getPreferenceName());
			textField.setText(value);
		}
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns the field editor's value.
	 *
	 * @return the current value
	 */
	public String getStringValue() {
		if (textField != null) {
			return textField.getText();
		}

		return getPreferenceStore().getString(getPreferenceName());
	}

	/**
	 * Returns this field editor's text control.
	 *
	 * @return the text control, or <code>null</code> if no
	 * text field is created yet
	 */
	protected Label getTextControl() {
		return textField;
	}

	/**
	 * Returns this field editor's text control.
	 * <p>
	 * The control is created if it does not yet exist
	 * </p>
	 *
	 * @param parent the parent
	 * @return the text control
	 */
	public Label getTextControl(Composite parent) {
		if (textField == null) {
			textField = new Label(parent, SWT.WRAP);
			textField.setFont(parent.getFont());
			textField.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent event) {
					textField = null;
				}
			});
		} else {
			checkParent(textField, parent);
		}
		return textField;
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	@Override
	public boolean isValid() {
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	@Override
	public void setFocus() {
		if (textField != null) {
			textField.setFocus();
		}
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getTextControl(parent).setEnabled(enabled);
	}

	
	@Override
	protected void doStore() {
		// nothing
	}

	@Override
	public ICBreakpointsUIContribution getContribution() {
		return contribution;
	}

	@Override
	public void setContribution(ICBreakpointsUIContribution contribution) {
		this.contribution = contribution;
	}
}
