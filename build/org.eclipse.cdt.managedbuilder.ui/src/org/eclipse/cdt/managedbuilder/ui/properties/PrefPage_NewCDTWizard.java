package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractPrefPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;

public class PrefPage_NewCDTWizard extends AbstractPrefPage {

	protected String getHeader() {
		return Messages.getString("PrefPage_NewCDTWizard.0") + //$NON-NLS-1$
		       Messages.getString("PrefPage_NewCDTWizard.1"); //$NON-NLS-1$
	}

	/*
	 * All affected settings are stored in preferences.
	 * Tabs are responsible for saving, after OK signal.
	 * No need to affect Project Description somehow.
	 */
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}
	public ICResourceDescription getResDesc() { return null; } 
	protected boolean isSingle() { return false; }

}
