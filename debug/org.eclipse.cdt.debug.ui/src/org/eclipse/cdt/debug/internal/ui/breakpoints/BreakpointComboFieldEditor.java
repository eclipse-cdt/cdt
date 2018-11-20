/*******************************************************************************
 * Copyright (c) 2014 Freescale and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Freescale - initial API and implementation (Bug 427898)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import org.eclipse.cdt.debug.internal.ui.preferences.ComboFieldEditor;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution;
import org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContributionUser;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends combo field editor to load combo values from {@extensionPoint org.eclipse.cdt.debug.ui.breakpointContribution} extension
 */
public class BreakpointComboFieldEditor extends ComboFieldEditor implements ICBreakpointsUIContributionUser {

	private ICBreakpointsUIContribution fContribution;

	/**
	 * Create combo field editor that would load choice values from {@link ICBreakpointsUIContribution}
	 * @param name - property name, must be the same as breakpoint attribute
	 * @param labelText - text in front of field
	 * @param parent
	 */
	public BreakpointComboFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, new String[0][0], parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContributionUser#setContribution(org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContribution)
	 */
	@Override
	public void setContribution(ICBreakpointsUIContribution contribution) {
		fContribution = contribution;
		//load values from contribution
		String[] possibleValues = fContribution.getPossibleValues();
		String[][] entryNamesAndValues = new String[possibleValues.length][2];
		for (int i = 0; i < possibleValues.length; ++i) {
			entryNamesAndValues[i][0] = fContribution.getLabelForValue(possibleValues[i]);
			entryNamesAndValues[i][1] = possibleValues[i];
		}
		setEntries(entryNamesAndValues);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.ui.breakpoints.ICBreakpointsUIContributionUser#getContribution()
	 */
	@Override
	public ICBreakpointsUIContribution getContribution() {
		return fContribution;
	}

	/**
	 * @see FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		updateComboForValue(storeToComboValue());
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
	 * load field value from preference store and return as combo widget value
	 * @return - "String" value of the attribute
	 */
	protected String storeToComboValue() {
		String value = getPreferenceStore().getString(getPreferenceName());
		if (fContribution != null) {
			if ("integer".equals(fContribution.getType())) { //$NON-NLS-1$
				value = Integer.toString(getPreferenceStore().getInt(getPreferenceName()));
			} else if ("boolean".equals(fContribution.getType())) {//$NON-NLS-1$
				value = Boolean.toString(getPreferenceStore().getBoolean(getPreferenceName()));
			} else if ("float".equals(fContribution.getType())) {//$NON-NLS-1$
				value = Float.toString(getPreferenceStore().getFloat(getPreferenceName()));
			} else if ("double".equals(fContribution.getType())) {//$NON-NLS-1$
				value = Double.toString(getPreferenceStore().getDouble(getPreferenceName()));
			}
		}
		return value;
	}

	/**
	 * Save to preference store the selected combo value
	 * @param val - value to be stored.
	 */
	protected void comboValueToStore(String val) {
		if (fContribution != null) {
			if ("integer".equals(fContribution.getType())) { //$NON-NLS-1$
				getPreferenceStore().setValue(getPreferenceName(), Integer.parseInt(val));
			} else if ("boolean".equals(fContribution.getType())) {//$NON-NLS-1$
				getPreferenceStore().setValue(getPreferenceName(), Boolean.parseBoolean(val));
			} else if ("float".equals(fContribution.getType())) {//$NON-NLS-1$
				getPreferenceStore().setValue(getPreferenceName(), Float.parseFloat(val));
			} else if ("double".equals(fContribution.getType())) {//$NON-NLS-1$
				getPreferenceStore().setValue(getPreferenceName(), Double.parseDouble(val));
			} else {
				// handle "String" attribute type
				getPreferenceStore().setValue(getPreferenceName(), val);
			}
		} else {
			getPreferenceStore().setValue(getPreferenceName(), val);
		}
	}
}
