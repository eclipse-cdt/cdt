package org.eclipse.cdt.arduino.ui.internal;

import org.eclipse.cdt.arduino.core.ArduinoHome;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ArduinoPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(ArduinoHome.preferenceName, ArduinoHome.getDefault());
	}

}
