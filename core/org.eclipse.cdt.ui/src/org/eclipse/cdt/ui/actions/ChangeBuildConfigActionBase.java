/*******************************************************************************
 * Copyright (c) 2006, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;

/**
 * Base class for build configuration actions. 
 */
public class ChangeBuildConfigActionBase {
	
	/**
	 * List of selected managed-built projects
	 */
	protected HashSet fProjects = new HashSet();
	
	/**
	 * Fills the menu with build configurations which are common for all selected projects
	 * @param menu The menu to fill
	 */
	protected void fillMenu(Menu menu)	{
		// This should not happen 
		if (menu == null) return;

		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) items[i].dispose();
		
		TreeSet configNames = new TreeSet();
		Iterator projIter = fProjects.iterator();
		String sCurrentConfig = null;
		boolean bCurrentConfig = true;
		while (projIter.hasNext()) {
			ICConfigurationDescription[] cfgDescs = getCfgs((IProject)projIter.next());

			String sActiveConfig = null;
			// Store names and detect active configuration
			for (int i=0; i<cfgDescs.length; i++) {
				configNames.add(cfgDescs[i].getName());
				if (cfgDescs[i].isActive())	
					sActiveConfig = cfgDescs[i].getName();
			}

			// Check whether all projects have the same active configuration
			if (bCurrentConfig) {
				if (sCurrentConfig == null)
					sCurrentConfig = sActiveConfig;
				else {
					if (!sCurrentConfig.equals(sActiveConfig)) 
						bCurrentConfig = false;
				}
			}
		}
		
		Iterator confIter = configNames.iterator();
		int accel = 0;
		while (confIter.hasNext()) {
			String sName = (String)confIter.next();
			String sDesc = null;
			projIter = fProjects.iterator();
			boolean commonName = true;
			boolean commonDesc = true;
			boolean firstProj = true;
			while (projIter.hasNext()) {
				ICConfigurationDescription[] cfgDescs = getCfgs((IProject)projIter.next());
				int i = 0;
				for (; i < cfgDescs.length; i++) {
					if (cfgDescs[i].getName().equals(sName)) {
						String sNewDesc = cfgDescs[i].getDescription();
						if (sNewDesc != null && sNewDesc.length() == 0) {
							sNewDesc = null;
						}
						if (commonDesc) {
							if (firstProj) {
								sDesc = sNewDesc;
								firstProj = false;
							} else if (sNewDesc == null && sDesc != null || sNewDesc != null && !sNewDesc.equals(sDesc)) {
								commonDesc = false;	
							}
						}
						break;
					}
				}
				if (i == cfgDescs.length) {
					commonName = false;
					break;
				}
			}
			if (commonName) {
				StringBuffer builder = new StringBuffer(sName);
				if (commonDesc) {
					if (sDesc != null) {
						builder.append(" (");	//$NON-NLS-1$
						builder.append(sDesc);
						builder.append(")");	//$NON-NLS-1$
					}
				} else {
					builder.append(" (...)");	//$NON-NLS-1$
				}
					
				IAction action = new BuildConfigAction(fProjects, sName, builder.toString(), accel + 1);
				if (bCurrentConfig && sCurrentConfig.equals(sName)) {
					action.setChecked(true);
				}
				ActionContributionItem item = new ActionContributionItem(action);
				item.fill(menu, -1);
				accel++;
			}
		}
	}

	/**
	 * selectionChanged() event handler. Fills the list of managed-built projects 
	 * based on the selection. If some non-managed-built projects are selected,
	 * disables the action. 
	 * @param action The action
	 * @param selection The selection
	 */
	protected void onSelectionChanged(IAction action, ISelection selection) {
		fProjects.clear();

		if (!action.isEnabled()) return;

		boolean badObject = false;
		if (selection != null && selection instanceof IStructuredSelection) {
			Iterator iter = ((IStructuredSelection)selection).iterator();
			while (iter.hasNext()) {
				Object selItem = iter.next();
				IProject project = null;
				if (selItem instanceof ICElement) {
					ICProject cproject = ((ICElement)selItem).getCProject();
					if (cproject != null) project = cproject.getProject();
				}
				else if (selItem instanceof IResource) {
					project = ((IResource)selItem).getProject();
				}
				// Check whether the project is CDT project
				if (project != null) {
					if (!CoreModel.getDefault().isNewStyleProject(project))
						project = null;
					else {
						ICConfigurationDescription[] tmp = getCfgs(project);
						if (tmp == null || tmp.length == 0)	project = null;
					}
				}
				if (project != null) {
					fProjects.add(project);
				} else {
					badObject = true;
					break;
				}
			}
		}
		
		boolean enable = false;
		if (!badObject && !fProjects.isEmpty()) {
			Iterator iter = fProjects.iterator();
			ICConfigurationDescription[] firstConfigs = getCfgs((IProject)iter.next());
			for (int i = 0; i < firstConfigs.length; i++) {
				boolean common = true;
				Iterator iter2 = fProjects.iterator();
				while (iter2.hasNext()) {
					ICConfigurationDescription[] currentConfigs = getCfgs((IProject)iter2.next());
					int j = 0;
					for (; j < currentConfigs.length; j++) {
						if (firstConfigs[i].getName().equals(currentConfigs[j].getName())) 
							break;
					}
					if (j == currentConfigs.length) {
						common = false;
						break;
					}
				}
				if (common) {
					enable = true;
					break;
				}
			}
		}
		action.setEnabled(enable);
	}
	
	private ICConfigurationDescription[] getCfgs(IProject prj) {
		ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
		if (prjd == null) return null;
		ICConfigurationDescription[] tmp = prjd.getConfigurations();
		if (tmp == null) return null;
		return prjd.getConfigurations(); 
	}
	
	
}
