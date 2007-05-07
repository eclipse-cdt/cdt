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

package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;

/**
 * This class provides static methods to work with multiple
 * implementors of "ConfigManager" extension point.
 */
public class ManageConfigSelector {
	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.ConfigManager"; //$NON-NLS-1$
	public static final String ELEMENT_NAME = "manager"; //$NON-NLS-1$
	public static final String CLASS_NAME = "class"; //$NON-NLS-1$
	private static IConfigManager[] mgrs = null;

	/**
	 * Searches for IConfigManager which
	 * can process given projects.
	 * 
	 * @param obs - list of projects to handle
	 * @return first matching ConfigManager
	 */
	public static IConfigManager getManager(IProject[] obs) {
		readMgrs();
		if (mgrs == null)
			return null;
		for (int i=0; i<mgrs.length; i++) {
			if (mgrs[i].canManage(obs))
				return mgrs[i];
		}
		return null;
	}
	
	/**
	 * Searches for IConfigManager which
	 * can process given objects.
	 * 
	 * @param obs - "raw" array of objects
	 * @return first matching ConfigManager
	 */
	public static IConfigManager getManagerFor(Object[] obs) {
		return getManager(getProjects(obs));
	}		
	
	/**
	 * Filters "raw" objects array
	 * 
	 * @param obs - objects to filter
	 * @return array with only new-style projects included
	 */
	public static IProject[] getProjects(Object[] obs) {
		ArrayList lst = new ArrayList();
		if (obs != null) {
			for (int i=0; i<obs.length; i++) {
				IProject prj = null;
				// Extract project from selection 
				if (obs[i] instanceof ICElement) { // for C/C++ view
					prj = ((ICElement)obs[i]).getCProject().getProject();
				} else if (obs[i] instanceof IResource) { // for other views
					prj = ((IResource)obs[i]).getProject();
				}

				if (prj == null || lst.contains(prj) ||
					!CoreModel.getDefault().isNewStyleProject(prj))
						continue;
				lst.add(prj);
			}
		}
		return (IProject[])lst.toArray(new IProject[lst.size()]);
	}

	private static void readMgrs() {
		if (mgrs != null)
			return;

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint == null) 
			return;

		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions == null) 
			return;

		ArrayList list = new ArrayList();
		for (int i = 0; i < extensions.length; ++i)	{
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int k = 0; k < elements.length; k++) {
				if (elements[k].getName().equals(ELEMENT_NAME)) {
					IConfigManager cm = null;
					try {
						cm = (IConfigManager) elements[k].createExecutableExtension(CLASS_NAME);
					} catch (CoreException e) {
					}
					if (cm != null)
						list.add(cm);
				}
			}
		}
		list.add(ManageConfigRunner.getDefault()); // Default manager
		mgrs = (IConfigManager[]) list.toArray(new IConfigManager[list.size()]);
	}
}
