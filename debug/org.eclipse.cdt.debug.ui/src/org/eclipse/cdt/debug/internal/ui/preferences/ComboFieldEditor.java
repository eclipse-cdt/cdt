/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContributionUser;
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
public class ComboFieldEditor extends FieldEditor implements ICBreakpointsUIContributionUser{

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

	private ICBreakpointsUIContribution fContribution;

	private boolean isEmpty() {
		return fEntryNamesAndValues.length == 0;
	}
	
	public ComboFieldEditor(String name, String labelText, Composite parent) {
		this(name, labelText, new String[0][0], parent);
	}

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
		if ( numColumns <= 1 )
			return;
		int span = numColumns;
		Control control = getLabelControl();
		if (control != null) {
			((GridData)control.getLayoutData()).horizontalSpan = 1;
			--span;
		}
		((GridData)fCombo.getLayoutData()).horizontalSpan = span;
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
		updateComboForValue(storeToComboValue());
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
	
		comboValueToStore(fValue);
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

	protected String storeToComboValue() {
		String value = getPreferenceStore().getString(getPreferenceName());
		if (fContribution!=null) {
		    if ("integer".equals (fContribution.getType())) { //$NON-NLS-1$
		        value = Integer.toString( getPreferenceStore().getInt(getPreferenceName()) );
		    } else if ("boolean".equals (fContribution.getType()) ) {//$NON-NLS-1$
                value = Boolean.toString( getPreferenceStore().getBoolean(getPreferenceName()) );
		    } else if ("float".equals (fContribution.getType()) ) {//$NON-NLS-1$
                value = Float.toString( getPreferenceStore().getFloat(getPreferenceName()) );
            } else if ("double".equals (fContribution.getType()) ) {//$NON-NLS-1$
                value = Double.toString( getPreferenceStore().getDouble(getPreferenceName()) );
            }
		}
		
		return value;
	}
	
	protected void comboValueToStore(String val) {
		if (fContribution!=null) {
		    if ("integer".equals (fContribution.getType())) { //$NON-NLS-1$
		        getPreferenceStore().setValue(getPreferenceName(), Integer.parseInt(val)) ;
		    } else if ("boolean".equals (fContribution.getType()) ) {//$NON-NLS-1$
		        getPreferenceStore().setValue(getPreferenceName(), Boolean.parseBoolean(val)) ;
		    } else if ("float".equals (fContribution.getType()) ) {//$NON-NLS-1$
		        getPreferenceStore().setValue(getPreferenceName(), Float.parseFloat(val)) ;
            } else if ("double".equals (fContribution.getType()) ) {//$NON-NLS-1$
		        getPreferenceStore().setValue(getPreferenceName(), Double.parseDouble(val)) ;
            }			
		} else {		
			getPreferenceStore().setValue(getPreferenceName(), val);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#fireValueChanged(String, Object, Object)
	 */
	@Override
	protected void fireValueChanged( String property, Object oldValue, Object newValue )
	{
		super.fireValueChanged( property, oldValue, newValue );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#setPresentsDefaultValue(boolean)
	 */
	@Override
	protected void setPresentsDefaultValue( boolean b )
	{
		super.setPresentsDefaultValue( b );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContributionUser#setContribution(org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution)
	 */
	@Override
	public void setContribution(ICBreakpointsUIContribution contribution) {
		fContribution = contribution;
		if (isEmpty()) {
			Combo combo = getComboBoxControl();
			assert combo.getItemCount() == 0;
			//load values from contribution
			String[] possibleValues = fContribution.getPossibleValues();
			fEntryNamesAndValues = new String[possibleValues.length][2];
			for (int i=0; i<possibleValues.length; ++i) {
				fEntryNamesAndValues[i][0] = fContribution.getLabelForValue(possibleValues[i]);
				fEntryNamesAndValues[i][1] = possibleValues[i];
				combo.add(fEntryNamesAndValues[i][0], i);
			}
			fCombo.select(0); 
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContributionUser#getContribution()
	 */
	@Override
	public ICBreakpointsUIContribution getContribution() {
		return fContribution;
	}
	
	
}
