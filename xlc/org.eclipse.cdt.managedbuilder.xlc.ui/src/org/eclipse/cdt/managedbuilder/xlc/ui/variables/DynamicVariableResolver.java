/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlc.ui.variables;

import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author crecoskie
 *
 */
public class DynamicVariableResolver implements IDynamicVariableResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	@Override
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {

		if (variable.getName().equals((PreferenceConstants.P_XL_COMPILER_ROOT))) {
			IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
			return prefStore.getString(PreferenceConstants.P_XL_COMPILER_ROOT);
		}

		return null;
	}

}
