package org.eclipse.tm.terminal.internal.view;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class TerminalPreferenceInitializer extends AbstractPreferenceInitializer {

	public TerminalPreferenceInitializer() {
	}

	public void initializeDefaultPreferences() {
		Preferences store = TerminalViewPlugin.getDefault().getPluginPreferences();
		store.setDefault(TerminalPreferencePage.PREF_LIMITOUTPUT, TerminalPreferencePage.DEFAULT_LIMITOUTPUT);
		store.setDefault(TerminalPreferencePage.PREF_BUFFERLINES, TerminalPreferencePage.DEFAULT_BUFFERLINES);
		store.setDefault(TerminalPreferencePage.PREF_TIMEOUT_SERIAL, TerminalPreferencePage.DEFAULT_TIMEOUT_SERIAL);
		store.setDefault(TerminalPreferencePage.PREF_TIMEOUT_NETWORK, TerminalPreferencePage.DEFAULT_TIMEOUT_NETWORK);
	}

}
