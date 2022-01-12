/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import org.eclipse.cdt.debug.ui.memory.floatingpoint.FPutilities.FPDataType;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class FPPreferenceConstants {
	private FPPreferenceConstants() {
		// Prevent subclassing or instantiation
	}

	/**
	 * Initialize preference default values.
	 *
	 * @param store
	 */
	public static void initializeDefaults(IPreferenceStore store) {
		// Set default values

		store.setDefault(IFPRConstants.ENDIAN_KEY, -1); // -1 = No default set
		store.setDefault(IFPRConstants.DATATYPE_KEY, FPDataType.FLOAT.getValue());
		store.setDefault(IFPRConstants.FLOAT_DISP_KEY, 8);
		store.setDefault(IFPRConstants.DOUBLE_DISP_KEY, 8);
		store.setDefault(IFPRConstants.COLUMN_COUNT_KEY, Rendering.COLUMNS_AUTO_SIZE_TO_FIT);
		store.setDefault(IFPRConstants.UPDATEMODE_KEY, Rendering.UPDATE_ALWAYS);
	}

	public static class Initializer extends AbstractPreferenceInitializer {
		@Override
		public void initializeDefaultPreferences() {
			IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();
			initializeDefaults(store);
		}
	}
}
