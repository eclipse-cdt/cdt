/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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
import java.util.List;

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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.AbstractPage;

import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.actions.DeleteResConfigsHandler;

/**
 * Action which deletes resource description. (If resource description is missing
 * one from parent is normally used)
 * @deprecated as of CDT 8.0 now using {@link DeleteResConfigsHandler} 
 */
@Deprecated
public class DeleteResConfigsAction 
implements IWorkbenchWindowPulldownDelegate2, IObjectActionDelegate {

	protected ArrayList<IResource> objects = null;
	private   ArrayList<ResCfgData> outData = null;		

	public void selectionChanged(IAction action, ISelection selection) {
		objects = null;
		outData = null;
		
		if (!selection.isEmpty()) {
			// case for context menu
			if (selection instanceof IStructuredSelection) {
				Object[] obs = ((IStructuredSelection)selection).toArray();
				if (obs.length > 0) {
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
		} 
		action.setEnabled(objects != null);
	}
	
	public void run(IAction action) {
		openDialog();
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
		return new IStructuredContentProvider() {

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
