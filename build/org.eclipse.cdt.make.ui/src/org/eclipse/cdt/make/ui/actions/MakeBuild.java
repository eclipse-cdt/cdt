package org.eclipse.cdt.make.ui.actions;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.views.MakeTarget;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;

public class MakeBuild {

	/**
	 * Causes all editors to save any modified resources depending on the user's
	 * preference.
	 */
	static void saveAllResources(MakeTarget[] targets) {

		if (!BuildAction.isSaveAllSet())
			return;

		List projects = new ArrayList();
		for (int i = 0; i < targets.length; ++i) {
			MakeTarget target = targets[i];
			projects.add(target.getResource().getProject());
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
	
	static public void run(boolean fork, IRunnableContext context, final MakeTarget[] targets) {
		try {
			context.run(fork, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								saveAllResources(targets);

//								Map infoMap = new HashMap();
//								IMakeBuilderInfo info = MakeCorePlugin.create(infoMap, MakeBuilder.BUILDER_ID);
//								project.build(IncrementalProjectBuilder.FULL_BUILD, MakeBuilder.BUILDER_ID, infoMap, monitor);

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
			MakeUIPlugin.logException(e, "Build Error", "Error Building Projects");
		}
	}
}
