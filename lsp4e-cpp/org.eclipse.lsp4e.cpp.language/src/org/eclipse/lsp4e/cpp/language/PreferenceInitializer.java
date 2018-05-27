package org.eclipse.lsp4e.cpp.language;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.lsp4e.cpp.language.CPPLanguageServerPreferencePage;

//import pluginClass;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	// CPPLanguageServerPreferencePage page = new CPPLanguageServerPreferencePage();
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PATH, "");
		store.setDefault(PreferenceConstants.P_CHOICE, "clangd");
		store.setDefault(PreferenceConstants.P_FLAGS, "");
	}

}
