package org.eclipse.lsp4e.cpp.language;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class ServerChoiceListener implements IPropertyChangeListener {

	private static final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// TODO Auto-generated method stub
		if(event.getProperty() == "org.eclipse.cdt.lsp.serverChoicePreference") {
			store.setValue(PreferenceConstants.P_SERVER_PATH,
					CPPStreamConnectionProvider.getDefaultLSLocation((String) event.getNewValue()).getAbsolutePath());
		}
	}

}
