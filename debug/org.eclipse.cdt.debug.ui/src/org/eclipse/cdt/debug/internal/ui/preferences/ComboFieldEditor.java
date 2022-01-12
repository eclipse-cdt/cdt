/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Freescale - allow setting combo values after creating FieldEditor object (Bug 427898)
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A field editor for a combo box that allows the drop-down selection of one of a list of items.
 */
public class ComboFieldEditor extends FieldEditor {

	/**
	 * The <code>Combo</code> widget.
	 */
	protected Combo fCombo;

	/**
	 * The value (not the name) of the currently selected item in the Combo widget.
	 */
	protected String fValue;

	/**
	 * The names (labels) and underlying values to populate the combo widget.  These should be
	 * arranged as: { {name1, value1}, {name2, value2}, ...}
	 */
	private String[][] fEntryNamesAndValues;

	/**
	* Create combo field editor with all choice values.
	* @param name - property name, must be the same as breakpoint attribute
	* @param labelText - text in front of field
	* @param entryNamesAndValues - The names (labels) and underlying values to populate the combo widget.
	* 		These should be arranged as: { {name1, value1}, {name2, value2}, ...}
	* @param parent the parent control
	*/
	public ComboFieldEditor(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
		init(name, labelText);
		Assert.isTrue(checkArray(entryNamesAndValues));
		fEntryNamesAndValues = entryNamesAndValues;
		createControl(parent);
	}

	/**
	 * Checks whether given <code>String[][]</code> is of "type"
	 * <code>String[][2]</code>.
	 *
	 * @return <code>true</code> if it is ok, and <code>false</code> otherwise
	 */
	private boolean checkArray(String[][] table) {
		if (table == null) {
			return false;
		}
		for (int i = 0; i < table.length; i++) {
			String[] array = table[i];
			if (array == null || array.length != 2) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		if (numColumns <= 1)
			return;
		int span = numColumns;
		Control control = getLabelControl();
		if (control != null) {
			((GridData) control.getLayoutData()).horizontalSpan = 1;
			--span;
		}
		((GridData) fCombo.getLayoutData()).horizontalSpan = span;
	}

	/**
	 * @see FieldEditor#doFillIntoGrid(Composite, int)
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);
		control = getComboBoxControl(parent);
		gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);
	}

	/**
	 * @see FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
	}

	/**
	 * @see FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
	}

	/**
	 * @see FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		if (fValue == null) {
			getPreferenceStore().setToDefault(getPreferenceName());
			return;
		}

		getPreferenceStore().setValue(getPreferenceName(), fValue);
	}

	/**
	 * @see FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 1;
	}

	/**
	 * Lazily create and return the Combo control.
	 */
	protected Combo getComboBoxControl(Composite parent) {
		if (fCombo == null) {
			fCombo = new Combo(parent, SWT.READ_ONLY);
			for (int i = 0; i < fEntryNamesAndValues.length; i++) {
				fCombo.add(fEntryNamesAndValues[i][0], i);
			}

			fCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					String oldValue = fValue;
					String name = fCombo.getText();
					fValue = getValueForName(name);
					setPresentsDefaultValue(false);
					fireValueChanged(VALUE, oldValue, fValue);
				}
			});
		}
		return fCombo;
	}

	protected Combo getComboBoxControl() {
		return fCombo;
	}

	/**
	 * Given the name (label) of an entry, return the corresponding value.
	 */
	protected String getValueForName(String name) {
		for (int i = 0; i < fEntryNamesAndValues.length; i++) {
			String[] entry = fEntryNamesAndValues[i];
			if (name.equals(entry[0])) {
				return entry[1];
			}
		}
		return fEntryNamesAndValues[0][0];
	}

	/**
	 * Set the name in the combo widget to match the specified value.
	 */
	protected void updateComboForValue(String value) {
		fValue = value;
		for (int i = 0; i < fEntryNamesAndValues.length; i++) {
			if (value.equals(fEntryNamesAndValues[i][1])) {
				fCombo.setText(fEntryNamesAndValues[i][0]);
				return;
			}
		}
		if (fEntryNamesAndValues.length > 0) {
			fValue = fEntryNamesAndValues[0][1];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#fireValueChanged(String, Object, Object)
	 */
	@Override
	protected void fireValueChanged(String property, Object oldValue, Object newValue) {
		super.fireValueChanged(property, oldValue, newValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#setPresentsDefaultValue(boolean)
	 */
	@Override
	protected void setPresentsDefaultValue(boolean b) {
		super.setPresentsDefaultValue(b);
	}

	/**
	 * Load new values in combo selection.
	 * @param namesAndValues: new values for combo widget. Cannot be null.
	 * <p> See {@link ComboFieldEditor#ComboFieldEditor(String, String, String[][], Composite)} for names and values format
	 */
	protected void setEntries(String[][] entryNamesAndValues) {
		fEntryNamesAndValues = entryNamesAndValues;
		Combo combo = getComboBoxControl();
		// dispose old items.
		if (combo.getItemCount() > 0) {
			combo.removeAll();
		}
		//load values from contribution
		for (int i = 0; i < fEntryNamesAndValues.length; ++i) {
			combo.add(fEntryNamesAndValues[i][0], i);
		}
		fCombo.select(0);
	}

}
