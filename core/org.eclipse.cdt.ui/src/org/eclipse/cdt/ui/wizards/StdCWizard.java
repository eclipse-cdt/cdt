package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.swt.widgets.TabFolder;

/**
 */
public class StdCWizard extends StdMakeProjectWizard {
	
	private static final String WZ_TITLE = "StdCWizard.title";
	private static final String WZ_DESC = "StdCWizard.description";
	private static final String SETTINGS_TITLE= "StdCWizardSettings.title"; //$NON-NLS-1$
	private static final String SETTINGS_DESC= "StdCWizardSettings.description"; //$NON-NLS-1$

	public StdCWizard() {
		this(CUIPlugin.getResourceString(WZ_TITLE), CUIPlugin.getResourceString(WZ_DESC));
	}

	public StdCWizard(String title, String desc) {
		super(title, desc);
	}
	
	public void addTabItems(TabFolder folder) {
		super.addTabItems(folder);
		fTabFolderPage.setTitle(CUIPlugin.getResourceString(SETTINGS_TITLE));
		fTabFolderPage.setDescription(CUIPlugin.getResourceString(SETTINGS_DESC));
	}
}
