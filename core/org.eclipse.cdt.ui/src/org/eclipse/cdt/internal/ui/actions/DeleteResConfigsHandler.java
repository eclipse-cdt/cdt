/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     Nokia - converted from action to handler
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractPage;



/**
 * Handler for command that deletes resource description. (If resource description is missing
 * one from parent is normally used)
 */
public class DeleteResConfigsHandler extends AbstractHandler {

	protected ArrayList<IResource> objects;
	private   ArrayList<ResCfgData> outData;

	@Override
	public void setEnabled(Object context) {
		ISelection selection = getSelection(context);
		setEnabledFromSelection(selection);
	}

	protected ISelection getSelection(Object context) {
		Object s = HandlerUtil.getVariable(context, ISources.ACTIVE_MENU_SELECTION_NAME);
        if (s instanceof ISelection) {
        	return (ISelection) s;
        }
	    return null;
	}

	public void setEnabledFromSelection(ISelection selection) {
		objects = null;

		if ((selection != null) && !selection.isEmpty()) {
			// case for context menu
			Object[] obs = null;
			if (selection instanceof IStructuredSelection) {
				obs = ((IStructuredSelection)selection).toArray();
			}
			else if (selection instanceof ITextSelection) {
				IFile file = getFileFromActiveEditor();
				if (file != null)
					obs = Collections.singletonList(file).toArray();
			}
			if (obs != null && obs.length > 0) {
				for (int i=0; i<obs.length; i++) {
					IResource res = null;
					// only folders and files may be affected by this action
					if (obs[i] instanceof ICContainer || obs[i] instanceof ITranslationUnit)
						res = ((ICElement)obs[i]).getResource();
					// project's configuration cannot be deleted
					else if (obs[i] instanceof IResource && !(obs[i] instanceof IProject))
						res = (IResource)obs[i];
					if (res != null) {
						IProject p = res.getProject();
						if (!p.isOpen()) continue;

						if (!CoreModel.getDefault().isNewStyleProject(p))
							continue;

						IPath path = res.getProjectRelativePath();
						// getting description in read-only mode
						ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, false);
						if (prjd == null) continue;
						ICConfigurationDescription[] cfgds = prjd.getConfigurations();
						if (cfgds == null || cfgds.length == 0) continue;
						for (ICConfigurationDescription cfgd : cfgds) {
							ICResourceDescription rd = cfgd.getResourceDescription(path, true);
							if (rd != null) {
								if (objects == null) objects = new ArrayList<IResource>();
								objects.add(res);
								break; // stop configurations scanning
							}
						}
					}
				}
			}
		}
		setBaseEnabled(objects != null);
	}

	private IFile getFileFromActiveEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				if (editor != null) {
					IEditorInput input = editor.getEditorInput();
					if (input != null)
						return (IFile) input.getAdapter(IFile.class);
				}
			}
		}
		return null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		openDialog();
		return null;
	}

	private void openDialog() {
		if (objects == null || objects.size() == 0) return;
		// create list of configurations to delete

		ListSelectionDialog dialog = new ListSelectionDialog(
				CUIPlugin.getActiveWorkbenchShell(),
				objects,
				createSelectionDialogContentProvider(),
				new LabelProvider() {}, ActionMessages.DeleteResConfigsAction_0);
		dialog.setTitle(ActionMessages.DeleteResConfigsAction_1);
		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult();
			if (selected != null && selected.length > 0) {
				for (Object element : selected) {
					((ResCfgData)element).delete();
					AbstractPage.updateViews(((ResCfgData)element).res);
				}
			}
		}
	}

	// Stores data for resource description with its "parents".
	class ResCfgData {
		IResource res;
		ICProjectDescription prjd;
		ICConfigurationDescription cfgd;
		ICResourceDescription rdesc;

		public ResCfgData(IResource res2, ICProjectDescription prjd2,
				ICConfigurationDescription cfgd2, ICResourceDescription rdesc2) {
			res = res2; prjd = prjd2; cfgd = cfgd2; rdesc = rdesc2;
		}

		// performs deletion
		public void delete() {
			try {
				cfgd.removeResourceDescription(rdesc);
				CoreModel.getDefault().setProjectDescription(res.getProject(), prjd);
			} catch (CoreException e) {}
		}
		@Override
		public String toString() {
			return "[" + cfgd.getName() + "] for " + res.getName();   //$NON-NLS-1$ //$NON-NLS-2$
		}
	}


	private IStructuredContentProvider createSelectionDialogContentProvider() {
		outData = null;

		return new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				if (outData != null) return outData.toArray();

				outData = new ArrayList<ResCfgData>();
				List<?> ls = (List<?>)inputElement;
				Iterator<?> it = ls.iterator();
				IProject proj = null;
				ICProjectDescription prjd = null;
				ICConfigurationDescription[] cfgds = null;

				// creating list of all res descs for all objects
				while (it.hasNext()) {
					IResource res = (IResource)it.next();
					IPath path = res.getProjectRelativePath();
					if (res.getProject() != proj) {
						proj = res.getProject();
						prjd = CoreModel.getDefault().getProjectDescription(proj);
						cfgds = prjd.getConfigurations();
					}
					if (cfgds != null) {
						for (ICConfigurationDescription cfgd : cfgds) {
							ICResourceDescription rd = cfgd.getResourceDescription(path, true);
							if (rd != null)
								outData.add(new ResCfgData(res, prjd, cfgd, rd));
						}
					}
				}
				return outData.toArray();
			}
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
	}

}
