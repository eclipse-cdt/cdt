/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class TerminalPreferenceInitializer extends AbstractPreferenceInitializer {

	public TerminalPreferenceInitializer() {
	}

	public void initializeDefaultPreferences() {
		Preferences store = TerminalViewPlugin.getDefault().getPluginPreferences();
		store.setDefault(TerminalPreferencePage.PREF_LIMITOUTPUT, TerminalPreferencePage.DEFAULT_LIMITOUTPUT);
		store.setDefault(TerminalPreferencePage.PREF_INVERT_COLORS, TerminalPreferencePage.DEFAULT_INVERT_COLORS);
		store.setDefault(TerminalPreferencePage.PREF_BUFFERLINES, TerminalPreferencePage.DEFAULT_BUFFERLINES);
		store.setDefault(TerminalPreferencePage.PREF_TIMEOUT_SERIAL, TerminalPreferencePage.DEFAULT_TIMEOUT_SERIAL);
		store.setDefault(TerminalPreferencePage.PREF_TIMEOUT_NETWORK, TerminalPreferencePage.DEFAULT_TIMEOUT_NETWORK);
	}

}
