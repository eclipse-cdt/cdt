package org.eclipse.cdt.internal.qt.ui;

import org.eclipse.cdt.internal.qt.core.project.QtProjectGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewQtProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public boolean performFinish() {
		if (!super.performFinish()) {
			return false;
		}

		new Job("Creating Qt Project") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					QtProjectGenerator generator = new QtProjectGenerator(getNewProject());
					generator.generate(monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		}.schedule();

		return true;
	}

}
