/**
 * The activator class controls the plug-in life cycle
 */
package org.eclipse.cdt.cmake.ui.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.cmake.core.CMakeProjectGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

public class NewCMakeProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public boolean performFinish() {
		if (!super.performFinish()) {
			return false;
		}

		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("Generating project", 1);
					CMakeProjectGenerator generator = new CMakeProjectGenerator(getNewProject());
					generator.generate(monitor);
					monitor.done();
				} catch (CoreException e) {
					Activator.log(e);
				}
			}
		});

		try {
			getContainer().run(false, true, op);
		} catch (InvocationTargetException | InterruptedException e) {
			return false;
		}
		return true;
	}

}
