package org.eclipse.cdt.debug.ui.memory.memorybrowser;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class MemoryBrowserPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MemoryBrowserPlugin.getDefault().getPreferenceStore();		
		
		// The following preferences should be kept in the store
		store.setDefault(MemoryBrowser.PREF_DEFAULT_RENDERING, "");
	}

}
