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
 * @deprecated
 * C Project wizard that creates a new project resource in
 */
public abstract class CCProjectWizard extends CProjectWizard {

	private static final String OP_ERROR= "CCProjectWizard.op_error"; //$NON-NLS-1$
	private static final String OP_DESC= "CCProjectWizard.op_description"; //$NON-NLS-1$

	private static final String PREFIX= "CCProjectWizard"; //$NON-NLS-1$
	private static final String WZ_TITLE= "CCProjectWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC= "CCProjectWizard.description"; //$NON-NLS-1$

	private static final String WINDOW_TITLE = "CCProjectWizard.windowTitle"; //$NON-NLS-1$

	private String wz_title;
	private String wz_desc;
	private String op_error;

	public CCProjectWizard() {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		wz_title = CUIPlugin.getResourceString(WZ_TITLE);
		wz_desc = CUIPlugin.getResourceString(WZ_DESC);
		op_error = CUIPlugin.getResourceString(OP_ERROR);
	}

	public CCProjectWizard(String title, String description) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		wz_title = title;
		wz_desc = description;
		op_error = CUIPlugin.getResourceString(OP_ERROR);
	}


	public CCProjectWizard(String title, String description, String error) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		wz_title = title;
		wz_desc = description;
		op_error = error;
	}


	protected void doRun(IProgressMonitor monitor) throws CoreException {
		super.doRun(monitor);
		// Add C++ Nature to the newly created project.
        if (newProject != null){
            CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, monitor);
        }
	}
}
