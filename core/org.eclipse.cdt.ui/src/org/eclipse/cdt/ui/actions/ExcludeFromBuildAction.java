/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.cdt.ui.newui.UIMessages;

import org.eclipse.cdt.internal.ui.actions.ActionMessages;

/**
 * Action which changes active build configuration of the current project to 
 * the given one.
 */
public class ExcludeFromBuildAction 
implements IWorkbenchWindowPulldownDelegate2, IObjectActionDelegate {

	protected ArrayList objects = null;
	protected ArrayList cfgNames = null;

	public void selectionChanged(IAction action, ISelection selection) {
		objects = null;
		cfgNames = null;
		boolean cfgsOK = true;
		
		if (!selection.isEmpty()) {
	    	// case for context menu
			if (selection instanceof IStructuredSelection) {
				Object[] obs = ((IStructuredSelection)selection).toArray();
				if (obs.length > 0) {
					for (int i=0; i<obs.length && cfgsOK; i++) {
						// if project selected, don't do anything
						if ((obs[i] instanceof IProject) || (obs[i] instanceof ICProject)) {
							cfgsOK=false; 
							break;
						}
						IResource res = null;
						// only folders and files may be affected by this action
						if (obs[i] instanceof ICContainer || obs[i] instanceof ITranslationUnit)
							res = ((ICElement)obs[i]).getResource();
						// project's configuration cannot be deleted
						else if (obs[i] instanceof IResource)
							res = (IResource)obs[i];
						if (res != null) {
							ICConfigurationDescription[] cfgds = getCfgsRead(res);
							if (cfgds == null || cfgds.length == 0) continue;
							
							if (objects == null) objects = new ArrayList();
							objects.add(res);
							if (cfgNames == null) {
								cfgNames = new ArrayList(cfgds.length);
								for (int j=0; j<cfgds.length; j++) { 
									if (!canExclude(res, cfgds[j])) {
										cfgNames = null;
										cfgsOK = false;
										break;
									}
									cfgNames.add(cfgds[j].getName());
								}
							} else {
								if (cfgNames.size() != cfgds.length) cfgsOK = false;
								else for (int j=0; j<cfgds.length; j++) {
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
		action.setEnabled(cfgsOK && objects != null );
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
			CCorePlugin.log(e);
		}					
	}
	
	public void run(IAction action) {
		openDialog();
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
				ActionMessages.getString("ExcludeFromBuildAction.0")); //$NON-NLS-1$
		dialog.setTitle(ActionMessages.getString("ExcludeFromBuildAction.1")); //$NON-NLS-1$
		
		boolean[] status = new boolean[cfgNames.size()];
		Iterator it = objects.iterator();
		while(it.hasNext()) {
			IResource res = (IResource)it.next();
			ICConfigurationDescription[] cfgds = getCfgsRead(res);
			IPath p = res.getFullPath();
			for (int i=0; i<cfgds.length; i++) {
				boolean b = CDataUtil.isExcluded(p, cfgds[i].getSourceEntries());
				if (b) status[i] = true;
			}
		}
		ArrayList lst = new ArrayList();
		for (int i=0; i<status.length; i++) 
			if (status[i]) lst.add(cfgNames.get(i));
		if (lst.size() > 0)
			dialog.setInitialElementSelections(lst);
		
		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult(); // may be empty
			Iterator it2 = objects.iterator();
			while(it2.hasNext()) {
				IResource res = (IResource)it2.next();
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
					CUIPlugin.getDefault().logErrorMessage(UIMessages.getString("AbstractPage.11") + e.getLocalizedMessage()); //$NON-NLS-1$
				}
				AbstractPage.updateViews(res);
			}
		}
	}

	private IStructuredContentProvider createSelectionDialogContentProvider() {
		return new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) { return cfgNames.toArray(); }
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		};
	}
	
	public void dispose() { objects = null; }
	
	// doing nothing
	public void init(IWorkbenchWindow window) { }
	public Menu getMenu(Menu parent) { return null; }
	public Menu getMenu(Control parent) { return null; }
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}
	
}
