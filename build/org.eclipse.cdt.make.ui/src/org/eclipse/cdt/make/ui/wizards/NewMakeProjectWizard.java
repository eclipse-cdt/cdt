package org.eclipse.cdt.make.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 */
public abstract class NewMakeProjectWizard extends CProjectWizard {
	
	protected MakeProjectWizardOptionPage fOptionPage;
	
	public NewMakeProjectWizard(String title, String desc) {
		super(title, desc);
	}

	protected void doRunPrologue(IProgressMonitor monitor) {
	}

	protected void doRunEpilogue(IProgressMonitor monitor) {
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeUIPlugin.getResourceString("MakeCWizard.task_name"), 10); //$NON-NLS-1$

        // super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(new SubProgressMonitor(monitor, 5));

		MakeProjectNature.addNature(getProjectHandle(), new SubProgressMonitor(monitor, 1));
		        
        // Modify the project based on what the user has selected
		if (newProject != null) {
			fOptionPage.performApply(new SubProgressMonitor(monitor, 4));
			monitor.done();
		}
	}
	
	public String getProjectID() {
		return MakeCorePlugin.getUniqueIdentifier() + ".make";
	}
}
