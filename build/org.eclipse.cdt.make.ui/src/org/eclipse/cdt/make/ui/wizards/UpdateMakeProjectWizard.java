/*
 * Created on 28-Jul-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.ui.wizards;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.actions.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

public class UpdateMakeProjectWizard extends Wizard {
	private static final String MAKE_UPDATE_WINDOW_TITLE = "MakeWizardUpdate.window_title";  //$NON-NLS-1$

	private UpdateMakeProjectWizardPage page1;
	private IProject[] selected;

	public UpdateMakeProjectWizard(IProject[] selected) {
		setDefaultPageImageDescriptor(null);
		setWindowTitle(MakeUIPlugin.getResourceString(MAKE_UPDATE_WINDOW_TITLE));
		setNeedsProgressMonitor(true);
		this.selected = selected;
	}

	public boolean performFinish() {
		Object[] finalSelected = page1.getSelected();

		IProject[] projectArray = new IProject[finalSelected.length];
		System.arraycopy(finalSelected, 0, projectArray, 0, finalSelected.length);
		UpdateMakeProjectAction.run(true, getContainer(), projectArray);
		return true;
	}

	public void addPages() {
		page1 = new UpdateMakeProjectWizardPage(selected);
		addPage(page1);
	}
}
