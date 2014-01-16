package org.eclipse.cdt.managedbuilder.mingw.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.cdt.managedbuilder.mingw.ui.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.MINGW_LOCATION, "C:\\mingw");
		store.setDefault(PreferenceConstants.MSYS_LOCATION, "C:\\mingw\\msys\\1.0");
	}

}
