/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class GdbAbstractConsolePreferenceListener implements IPropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();

		if (property.equals(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB)) {
			String terminateStr = event.getNewValue().toString();
			boolean terminate = terminateStr.equals(Boolean.FALSE.toString()) ? false : true;
			handleAutoTerminatePref(terminate);
		} else if (property.equals(IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS)) {
			boolean enabled = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
					IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS,
					IGdbDebugPreferenceConstants.CONSOLE_INVERTED_COLORS_DEFAULT, null);
			handleInvertColorsPref(enabled);
		} else if (property.equals(IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES)) {
			int bufferLines = Platform.getPreferencesService().getInt(GdbPlugin.PLUGIN_ID,
					IGdbDebugPreferenceConstants.PREF_CONSOLE_BUFFERLINES,
					IGdbDebugPreferenceConstants.CONSOLE_BUFFERLINES_DEFAULT, null);
			handleBufferLinesPref(bufferLines);
		}
	}

	protected abstract void handleAutoTerminatePref(boolean enabled);

	protected abstract void handleInvertColorsPref(boolean enabled);

	protected abstract void handleBufferLinesPref(int bufferLines);
}
