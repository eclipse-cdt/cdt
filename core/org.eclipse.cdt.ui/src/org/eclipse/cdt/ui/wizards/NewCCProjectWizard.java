package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * C Project wizard that creates a new project resource in
 */
public abstract class NewCCProjectWizard extends NewCProjectWizard {

	public NewCCProjectWizard() {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}

	public NewCCProjectWizard(String title, String description) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
	}


	protected void doRun(IProgressMonitor monitor) throws CoreException {
		super.doRun(monitor);
		// Add C++ Nature to the newly created project.
        if (newProject != null){
            CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, monitor);
        }
	}
}
