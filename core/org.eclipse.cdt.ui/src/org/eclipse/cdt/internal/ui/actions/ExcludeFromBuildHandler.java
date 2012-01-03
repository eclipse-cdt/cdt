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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractPage;

import org.eclipse.cdt.internal.ui.newui.Messages;


/**
 * Handler for command that excludes resources from build.
 */
public class ExcludeFromBuildHandler extends AbstractHandler {

	protected ArrayList<IResource> objects;
	protected ArrayList<String> cfgNames;

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
		cfgNames = null;
		boolean cfgsOK = true;

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
				for (int i=0; i<obs.length && cfgsOK; i++) {
					// if project selected, don't do anything
					if ((obs[i] instanceof IProject) || (obs[i] instanceof ICProject)) {
						cfgsOK=false;
						break;
					}
					IResource res = null;
					// only folders and files may be affected by this action
					if (obs[i] instanceof ICContainer || obs[i] instanceof ITranslationUnit) {
						res = ((ICElement) obs[i]).getResource();
					} else if (obs[i] instanceof IResource) {
						// project's configuration cannot be deleted
						res = (IResource) obs[i];
					}
					if (res != null) {
						ICConfigurationDescription[] cfgds = getCfgsRead(res);
						if (cfgds == null || cfgds.length == 0) continue;

						if (objects == null) objects = new ArrayList<IResource>();
						objects.add(res);
						if (cfgNames == null) {
							cfgNames = new ArrayList<String>(cfgds.length);
							for (int j=0; j<cfgds.length; j++) {
								if (!canExclude(res, cfgds[j])) {
									cfgNames = null;
									cfgsOK = false;
									break;
								}
								cfgNames.add(cfgds[j].getName());
							}
						} else {
							if (cfgNames.size() != cfgds.length) {
								cfgsOK = false;
							} else {
								for (int j=0; j<cfgds.length; j++) {
									if (! canExclude(res, cfgds[j]) ||
										! cfgNames.contains(cfgds[j].getName())) {
										cfgsOK = false;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		setBaseEnabled(cfgsOK && (objects != null));
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

	private boolean canExclude(IResource res, ICConfigurationDescription cfg) {
		IPath p = res.getFullPath();
		ICSourceEntry[] ent = cfg.getSourceEntries();
		boolean state = CDataUtil.isExcluded(p, ent);
		return CDataUtil.canExclude(p, (res instanceof IFolder), !state, ent);
	}

	private void setExclude(IResource res, ICConfigurationDescription cfg, boolean exclude) {
		try {
			ICSourceEntry[] newEntries = CDataUtil.setExcluded(res.getFullPath(), (res instanceof IFolder), exclude, cfg.getSourceEntries());
			cfg.setSourceEntries(newEntries);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		openDialog();
		return null;
	}

	private ICConfigurationDescription[] getCfgsRead(IResource res) {
		IProject p = res.getProject();
		if (!p.isOpen()) return null;
		if (!CoreModel.getDefault().isNewStyleProject(p)) return null;
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, false);
		if (prjd == null) return null;
		return prjd.getConfigurations();
	}

	private void openDialog() {
		if (objects == null || objects.size() == 0) return;
		// create list of configurations to delete

		ListSelectionDialog dialog = new ListSelectionDialog(
				CUIPlugin.getActiveWorkbenchShell(),
				cfgNames,
				createSelectionDialogContentProvider(),
				new LabelProvider() {},
				ActionMessages.ExcludeFromBuildAction_0);
		dialog.setTitle(ActionMessages.ExcludeFromBuildAction_1);

		boolean[] status = new boolean[cfgNames.size()];
		Iterator<IResource> it = objects.iterator();
		while (it.hasNext()) {
			IResource res = it.next();
			ICConfigurationDescription[] cfgds = getCfgsRead(res);
			IPath p = res.getFullPath();
			for (int i=0; i<cfgds.length; i++) {
				boolean b = CDataUtil.isExcluded(p, cfgds[i].getSourceEntries());
				if (b) status[i] = true;
			}
		}
		ArrayList<String> lst = new ArrayList<String>();
		for (int i=0; i<status.length; i++)
			if (status[i]) lst.add(cfgNames.get(i));
		if (lst.size() > 0)
			dialog.setInitialElementSelections(lst);

		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult(); // may be empty
			Iterator<IResource> it2 = objects.iterator();
			while (it2.hasNext()) {
				IResource res = it2.next();
				IProject p = res.getProject();
				if (!p.isOpen()) continue;
				// get writable description
				ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, true);
				if (prjd == null) continue;
				ICConfigurationDescription[] cfgds = prjd.getConfigurations();
				for (int i=0; i<cfgds.length; i++) {
					boolean exclude = false;
					for (int j=0; j<selected.length; j++) {
						if (cfgds[i].getName().equals(selected[j])) {
							exclude = true;
							break;
						}
					}
					setExclude(res, cfgds[i], exclude);
				}
				try {
					CoreModel.getDefault().setProjectDescription(p, prjd);
				} catch (CoreException e) {
					CUIPlugin.logError(Messages.AbstractPage_11 + e.getLocalizedMessage());
				}
				AbstractPage.updateViews(res);
			}
		}
	}

	private IStructuredContentProvider createSelectionDialogContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) { return cfgNames.toArray(); }
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
	}

}
