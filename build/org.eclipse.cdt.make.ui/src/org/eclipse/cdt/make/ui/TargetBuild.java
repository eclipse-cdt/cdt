package org.eclipse.cdt.make.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002. All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dialogs.GoToBackProgressMonitorDialog;
import org.eclipse.cdt.make.internal.ui.preferences.MakeTargetsPreferencePage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;

public class TargetBuild {

	/**
	 * Causes all editors to save any modified resources depending on the user's preference.
	 */
	static void saveAllResources(IMakeTarget[] targets) {

		if (!BuildAction.isSaveAllSet())
			return;

		List projects = new ArrayList();
		for (int i = 0; i < targets.length; ++i) {
			IMakeTarget target = targets[i];
			projects.add(target.getContainer().getProject());
		}

		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				IWorkbenchPage page = pages[j];
				IEditorReference[] editorReferences = page.getEditorReferences();
				for (int k = 0; k < editorReferences.length; k++) {
					IEditorPart editor = editorReferences[k].getEditor(false);
					if (editor != null && editor.isDirty()) {
						IEditorInput input = editor.getEditorInput();
						if (input instanceof IFileEditorInput) {
							IFile inputFile = ((IFileEditorInput) input).getFile();
							if (projects.contains(inputFile.getProject())) {
								page.saveEditor(editor, false);
							}
						}
					}
				}
			}
		}
	}

	static public void runWithProgressDialog(Shell shell, IMakeTarget[] targets) {
		GoToBackProgressMonitorDialog pd = new GoToBackProgressMonitorDialog(shell,
				MakeUIPlugin.getResourceString("TargetBuild.backgroundTask.name")); //$NON-NLS-1$
		try {
			TargetBuild.run(true, pd, targets);
		} catch (InvocationTargetException e) {
			MakeUIPlugin.errorDialog(shell, MakeUIPlugin.getResourceString("TargetBuild.execption.message"), //$NON-NLS-1$
					e.getTargetException().toString(), e.getTargetException());
		}
	}

	static public void buildTargets(Shell shell, final IMakeTarget[] targets) {
		saveAllResources(targets);
		if (MakeTargetsPreferencePage.isBuildTargetInBackground()) {
			new Job(MakeUIPlugin.getResourceString("TargetBuild.backgroundTask.name")) { //$NON-NLS-1$

				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask(MakeUIPlugin.getResourceString("TargetBuild.monitor.beginTask"), targets.length); //$NON-NLS-1$
					try {
						for (int i = 0; i < targets.length; i++) {
							final IMakeTarget target = targets[i];
							IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

								public void run(IProgressMonitor monitor) throws CoreException {
									target.build(new SubProgressMonitor(monitor, 1));
								}
							};
							MakeUIPlugin.getWorkspace().run(runnable, monitor);
						}
					} catch (CoreException e) {
						return e.getStatus();
					} catch (OperationCanceledException e) {
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		} else {
			runWithProgressDialog(shell, targets);
		}
	}

	static public void run(boolean fork, IRunnableContext context, final IMakeTarget[] targets) throws InvocationTargetException {
		try {
			saveAllResources(targets);

			context.run(fork, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(MakeUIPlugin.getResourceString("TargetBuild.monitor.beginTask"), targets.length); //$NON-NLS-1$
					try {
						for (int i = 0; i < targets.length; i++) {
							final IMakeTarget target = targets[i];
							IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

								public void run(IProgressMonitor monitor) throws CoreException {
									target.build(new SubProgressMonitor(monitor, 1));
								}
							};
							MakeUIPlugin.getWorkspace().run(runnable, monitor);
						}
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} catch (OperationCanceledException e) {
						throw new InterruptedException(e.getMessage());
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			return;
		}
	}
}