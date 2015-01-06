package org.eclipse.launchbar.ui.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class LaunchBarPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(Activator.PREF_ENABLE_LAUNCHBAR, true);
		store.setDefault(Activator.PREF_LAUNCH_HISTORY_SIZE, 3);
	}

}
