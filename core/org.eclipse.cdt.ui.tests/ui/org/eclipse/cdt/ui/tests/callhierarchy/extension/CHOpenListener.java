/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lidia Popescu - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.callhierarchy.extension;

import java.util.HashMap;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.UIJob;

public class CHOpenListener implements IOpenListener {

	/** On Node click open corresponding file */
	@Override
	public void open(OpenEvent event) {
		if (event != null) {
			ISelection selection = event.getSelection();
			if (selection instanceof TreeSelection) {
				TreeSelection treeSelection = (TreeSelection) selection;
				Object element = treeSelection.getFirstElement();
				if (element instanceof DslNode) {
					DslNode node = (DslNode) element;
					ICProject project = node.getProject();
					/**
					 * Based on a custom algorithm the corresponding file and line should be found and open. Suppose that the file
					 * 'CallHierarchy_test.java' has been found, and the line number '3' where the function 'function_dsl' is defined.
					 */
					IFile file = project.getProject().getFile("CallHierarchy_test.java");
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.getActivePart().getSite().getPage();
					IEditorInput input = new FileEditorInput(file);
					IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
							.getDefaultEditor(input.getName());

					UIJob ui = new UIJob("Open File") {

						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							try {
								String editorId = null;
								if (desc == null || desc.isOpenExternal()) {
									editorId = "org.eclipse.ui.DefaultTextEditor";
								} else {
									editorId = desc.getId();
								}
								IEditorPart editor = page.openEditor(input, editorId);
								IMarker fMarker = file.createMarker(IMarker.TEXT);
								HashMap<String, Object> map = new HashMap<>();
								map.put(IMarker.LINE_NUMBER, 3);
								fMarker.setAttributes(map);
								IDE.gotoMarker(editor, fMarker);

							} catch (PartInitException e) {
								e.printStackTrace();
							} catch (CoreException e) {
								e.printStackTrace();
							}
							return new Status(IStatus.OK, CTestPlugin.PLUGIN_ID, "");
						}
					};
					ui.schedule();
				}
			}
		}
	}
}
