package org.eclipse.cdt.managedbuilder.ui.newui;

import org.eclipse.cdt.ui.newui.AbstractPrefPage;

public class PrefPage_NewCDTWizard extends AbstractPrefPage {

	protected String getHeader() {
		return Messages.getString("PrefPage_NewCDTWizard.0") + //$NON-NLS-1$
		       Messages.getString("PrefPage_NewCDTWizard.1"); //$NON-NLS-1$
	}

	protected boolean isSingle() {
		return false;
	}

}
