package org.eclipse.cdt.make.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;

/**
 */
public class NewMakeCProjectWizard extends NewMakeProjectWizard {

	private static final String WZ_TITLE = "MakeCWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "MakeCWizard.description"; //$NON-NLS-1$'

	private static final String WZ_SETTINGS_TITLE = "MakeCWizardSettings.title"; //$NON-NLS-1$
	private static final String WZ_SETTINGS_DESC = "MakeCWizardSettings.description"; //$NON-NLS-1$'

	public NewMakeCProjectWizard() {
		super(MakeUIPlugin.getResourceString(WZ_TITLE), MakeUIPlugin.getResourceString(WZ_DESC));
	}

	public void addPages() {
		super.addPages();
		addPage(
			fOptionPage =
				new MakeProjectWizardOptionPage(
					MakeUIPlugin.getResourceString(WZ_SETTINGS_TITLE),
					MakeUIPlugin.getResourceString(WZ_SETTINGS_DESC)));
	}

}
