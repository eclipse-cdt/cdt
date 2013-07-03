/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * This class is near copy-paste of TargetBuild from cdt.make.ui.
 * Original class can't be reused without moving buildconsole package to
 * separate project which has dependencies on cdt.make.ui and cdt.ui.
 */
class TargetBuild {

	/**
	 * Causes all editors to save any modified resources depending on the user's preference.
	 */
	static void saveAllResources(IMakeTarget[] targets) {

		if (!BuildAction.isSaveAllSet())
			return;

		List<IProject> projects = new ArrayList<IProject>();
		for (int i = 0; i < targets.length; ++i) {
			IMakeTarget target = targets[i];
			projects.add(target.getProject());
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

	static public Job buildTargets(Shell shell, final IMakeTarget[] targets) {
		saveAllResources(targets);
		Job targetJob = new Job("Building Targets.") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Building Targets...", targets.length); //$NON-NLS-1$
				try {
					for (int i = 0; i < targets.length; i++) {
						final IMakeTarget target = targets[i];
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

							public void run(IProgressMonitor monitor) throws CoreException {
								target.build(new SubProgressMonitor(monitor, 1));
							}
						};
						CUIPlugin.getWorkspace().run(runnable, monitor);
					}
				} catch (CoreException e) {
					return e.getStatus();
				} catch (OperationCanceledException e) {
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
			
			@Override
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
			}
		};
		targetJob.schedule();
		
		return targetJob;
		
	}
}
