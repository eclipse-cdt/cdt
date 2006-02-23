/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.actions;

import java.util.*;

import org.eclipse.cdt.core.model.*;
import org.eclipse.cdt.managedbuilder.core.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;

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
		if (menu == null) {
			// This should not happen 
			return;
		}
		
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
		}
		
		TreeSet configNames = new TreeSet();
		Iterator projIter = fProjects.iterator();
		String sCurrentConfig = null;
		boolean bCurrentConfig = true;
		while (projIter.hasNext()) {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo((IProject)projIter.next());
			if (info != null && info.isValid()) {
				if (bCurrentConfig) {
					String sNewConfig = info.getDefaultConfiguration().getName();
					if (sCurrentConfig == null) {
						sCurrentConfig = sNewConfig;
					}
					else {
						if (!sCurrentConfig.equals(sNewConfig)) {
							bCurrentConfig = false;
						}
					}
				}
				IConfiguration[] configs = info.getManagedProject().getConfigurations();
				for (int i = 0; i < configs.length; i++) {
					configNames.add(configs[i].getName());
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
				IManagedBuildInfo info = ManagedBuildManager.getBuildInfo((IProject)projIter.next());
				if (info != null && info.isValid()) {
					IConfiguration[] configs = info.getManagedProject().getConfigurations();
					int i = 0;
					for (; i < configs.length; i++) {
						if (configs[i].getName().equals(sName)) {
							String sNewDesc = configs[i].getDescription();
							if (sNewDesc.equals("")) { 	//$NON-NLS-1$
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
					if (i == configs.length) {
						commonName = false;
						break;
					}
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
		
		if (!action.isEnabled()) {
			return;
		}

		boolean found = false;
		if (selection != null && selection instanceof IStructuredSelection) {
			Iterator iter = ((IStructuredSelection)selection).iterator();
			while (iter.hasNext()) {
				Object selItem = iter.next();
				IProject project = null;
				if (selItem instanceof ICElement) {
					ICProject cproject = ((ICElement)selItem).getCProject();
					if (cproject != null) {
						project = cproject.getProject();
					}
				}
				else if (selItem instanceof IResource) {
					project = ((IResource)selItem).getProject();
				}
				if (project != null) {
					try	{
						if (project != null && !project.hasNature(ManagedCProjectNature.MNG_NATURE_ID)) {
							project = null;
						}
					}
					catch (CoreException xE) {
						// do nothing
					}
				}
				if (project != null) {
					IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
					if (info != null && info.isValid()) {
						fProjects.add(project);
					}
				} else {
					found = true;
					break;
				}
			}
		}
		
		boolean enable = false;
		if (!found && !fProjects.isEmpty()) {
			Iterator iter = fProjects.iterator();
			IProject first = (IProject)iter.next();
			IConfiguration[] firstConfigs = ManagedBuildManager.getBuildInfo(first).getManagedProject().getConfigurations();
			for (int i = 0; i < firstConfigs.length; i++)
			{
				boolean common = true;
				iter = fProjects.iterator();
				while (iter.hasNext()) {
					IProject current = (IProject)iter.next();
					IConfiguration[] currentConfigs = ManagedBuildManager.getBuildInfo(current).getManagedProject().getConfigurations();
					int j = 0;
					for (; j < currentConfigs.length; j++) {
						if (firstConfigs[i].getName().equals(currentConfigs[j].getName())) {
							break;
						}
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
}
