package org.eclipse.cdt.internal.ui.makeview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.resources.MakeUtil;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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


public class MakeAction extends Action  {
	static final String PREFIX = "BuildAction.";

	MakeTarget[] targets;
	Shell shell;

	public MakeAction (MakeTarget[] targets, Shell shell, String s) {
		super (s);
		this.shell = shell;
		this.targets = targets;
	
		setToolTipText(PREFIX);
		setImageDescriptor(CPluginImages.DESC_BUILD_MENU);
	}

	/**
	 * Causes all editors to save any modified resources depending on the user's
	 * preference.
	 */
	void saveAllResources() {

		if (!BuildAction.isSaveAllSet())
			return;

		List projects = new ArrayList();
		for (int i = 0; i < targets.length; ++i ) {
			MakeTarget target = targets[i];
			projects.add(target.getResource().getProject());	
		}

		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage [] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				IWorkbenchPage page = pages[j];
				IEditorReference[] editorReferences = page.getEditorReferences();
				for (int k = 0; k < editorReferences.length; k++) {
					IEditorPart editor = editorReferences[k].getEditor(false);
					if (editor != null && editor.isDirty()) {
						IEditorInput input = editor.getEditorInput();
						if (input instanceof IFileEditorInput) {
							IFile inputFile = ((IFileEditorInput)input).getFile();
							if (projects.contains(inputFile.getProject())) {
								page.saveEditor(editor, false);
							}
						}
					}
				}
			}
		}
	}

	public void run() {
		try {
			saveAllResources();
			IRunnableWithProgress op = new IRunnableWithProgress () {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
					for (int i = 0; i < targets.length; ++i ) {
						MakeTarget target = targets[i];
						IResource res = target.getResource();
						IProject project = res.getProject();

						try {
							if (! project.equals(res) || target.isLeaf()) {
								String dir = res.getLocation().toOSString();
								MakeUtil.setSessionBuildDir(project, dir);
								if (target.isLeaf()) {
									MakeUtil.setSessionTarget(project, target.toString());
								}
							}
							// Full build to force builder for targets like clean and rebuild.
							// maybe this should be a option
							project.build (IncrementalProjectBuilder.FULL_BUILD, monitor);
						} catch (CoreException e) {
						} 

						MakeUtil.removeSessionBuildDir(project);
						MakeUtil.removeSessionTarget(project);
					}
				}
			};
			new ProgressMonitorDialog(shell).run(true, true, op);
		} catch (InvocationTargetException e) {
			// handle exception
		} catch (InterruptedException e) {
			// handle cancelation
		}
	}
}
