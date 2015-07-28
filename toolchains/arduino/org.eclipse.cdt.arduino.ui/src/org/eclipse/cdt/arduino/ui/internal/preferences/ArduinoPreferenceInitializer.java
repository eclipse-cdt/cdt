package org.eclipse.cdt.arduino.ui.internal.preferences;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ArduinoPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getCorePreferenceStore();
		store.setDefault(ArduinoPreferences.ARDUINO_HOME, ArduinoPreferences.getDefaultArduinoHome().toString());
	}

}
