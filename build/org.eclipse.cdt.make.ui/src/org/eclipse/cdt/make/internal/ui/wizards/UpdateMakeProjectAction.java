/*
 * Created on 25-Jul-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class UpdateMakeProjectAction implements IWorkbenchWindowActionDelegate {

	private ISelection fSelection;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		if (fSelection instanceof IStructuredSelection) {
			Object[] elems = ((IStructuredSelection) fSelection).toArray();
			ArrayList projects = new ArrayList(elems.length);

			for (int i = 0; i < elems.length; i++) {
				Object elem = elems[i];
				IProject project = null;

				if (elem instanceof IFile) {
					IFile file = (IFile) elem;
					project = file.getProject();
				} else if (elem instanceof IProject) {
					project = (IProject) elem;
				} else if (elem instanceof ICProject) {
					project = ((ICProject) elem).getProject();
				}
				if (project != null) {
				}
			}

			final IProject[] projectArray = (IProject[]) projects.toArray(new IProject[projects.size()]);

			ProgressMonitorDialog pd = new ProgressMonitorDialog(MakeUIPlugin.getActiveWorkbenchShell());
			run(true, pd, projectArray);
			UpdateMakeProjectWizard wizard = new UpdateMakeProjectWizard(projectArray);
			WizardDialog dialog = new WizardDialog(MakeUIPlugin.getActiveWorkbenchShell(), wizard);
			dialog.open();
		}

	}

	static public void run(boolean fork, IRunnableContext context, final IProject[] projects) {
		try {
			context.run(fork, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								doMakeProjectUpdate(monitor, projects);
							}
						};
						MakeUIPlugin.getWorkspace().run(runnable, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} catch (OperationCanceledException e) {
						throw new InterruptedException(e.getMessage());
					}
				}
			});
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			MakeUIPlugin.logException(e, "Error", "Error updateing Make Projects");
		}
	}

	public static void doMakeProjectUpdate(IProgressMonitor monitor, IProject[] projects) throws CoreException {
		// dinglis-TODO : Implement Make project updating. 
		monitor.beginTask("Updating make Projects...", projects.length);
		try {
			for (int i = 0; i < projects.length; i++) {
//				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);

				if (monitor.isCanceled())
					break;
			}
		} finally {
			monitor.done();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

}
