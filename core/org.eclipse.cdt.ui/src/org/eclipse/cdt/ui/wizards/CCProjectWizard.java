package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.internal.ui.CPlugin;


/**
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
		setDialogSettings(CPlugin.getDefault().getDialogSettings());
		wz_title = CPlugin.getResourceString(WZ_TITLE);
		wz_desc = CPlugin.getResourceString(WZ_DESC);
		op_error = CPlugin.getResourceString(OP_ERROR);
	}

	public CCProjectWizard(String title, String description) {
		super();
		setDialogSettings(CPlugin.getDefault().getDialogSettings());
		wz_title = title;
		wz_desc = description;
		op_error = CPlugin.getResourceString(OP_ERROR);
	}


	public CCProjectWizard(String title, String description, String error) {
		super();
		setDialogSettings(CPlugin.getDefault().getDialogSettings());
		wz_title = title;
		wz_desc = description;
		op_error = error;
	}


	protected void doRun(IProgressMonitor monitor) {
		super.doRun(monitor);
		// Add C++ Nature.
		if (newProject != null) {
			try {
				CCProjectNature.addCCNature(newProject, monitor);
			} catch (CoreException e) {
			}
		}
	}
}
