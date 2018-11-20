/*
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A field editor that combines an integer value preference with a boolean enablement
 * preference.
 *
 * @since 5.8
 */
public class ScalabilityIntegerFieldEditor extends IntegerFieldEditor {
	private ControlDecoration fDecoration;
	private final String fEnableKey;
	private Button fCheckbox;
	private boolean fWasSelected;

	public ScalabilityIntegerFieldEditor(String enableKey, String nameKey, String labelText, Composite parent) {
		super(nameKey, labelText, parent);
		fEnableKey = enableKey;
	}

	@Override
	public Text getTextControl(Composite parent) {
		Text control = super.getTextControl(parent);
		if (fDecoration == null) {
			fDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
			FieldDecoration errorDecoration = FieldDecorationRegistry.getDefault()
					.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
			fDecoration.setImage(errorDecoration.getImage());
			fDecoration.setDescriptionText(getErrorMessage());

			// validate on focus gain
			control.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					refreshValidState();
				}
			});
		}
		return control;
	}

	@Override
	protected void showErrorMessage(String msg) {
		super.showErrorMessage(msg);
		if (fDecoration != null) {
			fDecoration.setDescriptionText(msg);
			fDecoration.show();
		}
	}

	@Override
	protected void clearErrorMessage() {
		super.clearErrorMessage();
		if (fDecoration != null) {
			fDecoration.hide();
		}
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getCheckboxControl(parent);
		super.doFillIntoGrid(parent, numColumns);
	}

	private Button getCheckboxControl(Composite parent) {
		if (fCheckbox == null) {
			Composite inner = new Composite(parent, SWT.NULL);
			final GridLayout layout = new GridLayout(2, false);
			layout.marginWidth = 0;
			inner.setLayout(layout);
			fCheckbox = new Button(inner, SWT.CHECK);
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
		final Label label = getLabelControl();
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

	/**
	 * Set the checkbox selection and enablement of the other controls as specified.
	 */
	public void setBooleanValue(boolean value) {
		// The checkbox selection will normally be propagated to the label and text controls in the
		// checkbox selection listener.  However, the callback is only invoked when the selection changes,
		// which means that an initial value of false will not be properly propagated.  The state is
		// directly updated here.
		if (fCheckbox != null) {
			fWasSelected = value;
			getLabelControl().setEnabled(value);
			getTextControl().setEnabled(value);
			fCheckbox.setSelection(value);
		}
	}
}
