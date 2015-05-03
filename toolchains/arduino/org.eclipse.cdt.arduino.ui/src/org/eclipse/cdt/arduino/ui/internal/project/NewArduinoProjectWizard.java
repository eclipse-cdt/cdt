package org.eclipse.cdt.arduino.ui.internal.project;

import org.eclipse.cdt.arduino.core.ArduinoProjectGenerator;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewArduinoProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public void addPages() {
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		if (!super.performFinish())
			return false;

		new Job("Creating Aurdino Project") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final ArduinoProjectGenerator generator = new ArduinoProjectGenerator(getNewProject());
					generator.setupArduinoProject(monitor);
					getWorkbench().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow().getActivePage();
								IDE.openEditor(activePage, generator.getSourceFile());
							} catch (PartInitException e) {
								Activator.getDefault().getLog().log(e.getStatus());
							}
						}
					});
					return Status.OK_STATUS;
				} catch (CoreException e) {
					return e.getStatus();
				}
			}
		}.schedule();

		return true;
	}

}
