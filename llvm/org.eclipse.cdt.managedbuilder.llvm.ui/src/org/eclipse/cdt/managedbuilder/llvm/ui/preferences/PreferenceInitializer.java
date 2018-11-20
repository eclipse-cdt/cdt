/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Leo Hippelainen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui.preferences;

import org.eclipse.cdt.managedbuilder.llvm.ui.LlvmUIPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Class used to initialize the default preference values.
 *
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences store = DefaultScope.INSTANCE.getNode(LlvmUIPlugin.PLUGIN_ID);
		store.put(PreferenceConstants.P_LLVM_PATH, ""); //$NON-NLS-1$
		store.put(PreferenceConstants.P_LLVM_INCLUDE_PATH, ""); //$NON-NLS-1$
		store.put(PreferenceConstants.P_LLVM_LIBRARY_PATH, ""); //$NON-NLS-1$
		store.put(PreferenceConstants.P_LLVM_LIBRARIES, ""); //$NON-NLS-1$
	}

}
