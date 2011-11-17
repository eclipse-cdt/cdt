/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.preferences.MakePreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.ui.progress.IProgressService;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class TargetBuild {
	/** @since 7.1 */
	public static final String LAST_TARGET = "lastTarget"; //$NON-NLS-1$
	/** @since 7.1 */
	public static final String LAST_TARGET_CONTAINER = "lastTargetContainer"; //$NON-NLS-1$
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
			// Ensure we correctly save files in all referenced projects before build
			try {
				projects.addAll(Arrays.asList(target.getProject().getReferencedProjects()));
			} catch (CoreException e) {
				// Project not accessible or not open
			}
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

	static private void runWithProgressDialog(Shell shell, Job job) {
		IProgressService service = PlatformUI.getWorkbench().getProgressService();
		service.showInDialog(shell,job);
	}

	static public void buildTargets(Shell shell, final IMakeTarget[] targets) {
		// Setup the global build console
		CUIPlugin.getDefault().startGlobalConsole();

		saveAllResources(targets);
		Job targetJob = new Job(MakeUIPlugin.getResourceString("TargetBuild.backgroundTask.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(MakeUIPlugin.getResourceString("TargetBuild.monitor.beginTask"), targets.length); //$NON-NLS-1$
				try {
					for (int i = 0; i < targets.length; i++) {
						final IMakeTarget target = targets[i];
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

							@Override
							public void run(IProgressMonitor monitor) throws CoreException {
								target.build(new SubProgressMonitor(monitor, 1));
							}
						};
						MakeUIPlugin.getWorkspace().run(runnable, null, IResource.NONE, monitor);
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

		if (!MakePreferencePage.isBuildTargetInBackground()) {
			runWithProgressDialog(shell, targetJob);
		}
	}
}
