package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.TabFolder;

/**
 */
public class StdCCWizard extends StdMakeProjectWizard {
	
	private static final String WZ_TITLE = "StdCCWizard.title";
	private static final String WZ_DESC = "StdCCWizard.description";
	private static final String SETTINGS_TITLE= "StdCCWizardSettings.title"; //$NON-NLS-1$
	private static final String SETTINGS_DESC= "StdCCWizardSettings.description"; //$NON-NLS-1$

	public StdCCWizard() {
		this(CUIPlugin.getResourceString(WZ_TITLE), CUIPlugin.getResourceString(WZ_DESC));
	}

	public StdCCWizard(String title, String desc) {
		super(title, desc);
	}

	public void addTabItems(TabFolder folder) {
		super.addTabItems(folder);
		fTabFolderPage.setTitle(CUIPlugin.getResourceString(SETTINGS_TITLE));
		fTabFolderPage.setDescription(CUIPlugin.getResourceString(SETTINGS_DESC));
	}
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		super.doRun(monitor);
		// Add C++ Nature.
		if (newProject != null) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("Standard C++ Make", 1);
            // Add C++ Nature to the newly created project.
            CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, monitor);
		}
	}
}
