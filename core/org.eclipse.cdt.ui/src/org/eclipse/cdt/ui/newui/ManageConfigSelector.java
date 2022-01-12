/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.internal.ui.cview.IncludeReferenceProxy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * This class provides static methods to work with multiple
 * implementors of "ConfigManager" extension point.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
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
		for (int i = 0; i < mgrs.length; i++) {
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
		ArrayList<IProject> lst = new ArrayList<>();
		if (obs != null) {
			for (Object ob : obs) {
				IProject prj = null;
				// Extract project from selection
				if (ob instanceof ICElement) { // for C/C++ view
					prj = ((ICElement) ob).getCProject().getProject();
				} else if (ob instanceof IResource) { // for other views
					prj = ((IResource) ob).getProject();
					/* get project from Include folder elements */
				} else if (ob instanceof IncludeRefContainer) {
					ICProject fCProject = ((IncludeRefContainer) ob).getCProject();
					if (fCProject != null)
						prj = fCProject.getProject();
				} else if (ob instanceof IncludeReferenceProxy) {
					IncludeRefContainer irc = ((IncludeReferenceProxy) ob).getIncludeRefContainer();
					if (irc != null) {
						ICProject fCProject = irc.getCProject();
						if (fCProject != null)
							prj = fCProject.getProject();
					}
				}

				if (prj == null || lst.contains(prj) || !CoreModel.getDefault().isNewStyleProject(prj))
					continue;
				lst.add(prj);
			}
		}
		return lst.toArray(new IProject[lst.size()]);
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

		ArrayList<IConfigManager> list = new ArrayList<>();
		for (int i = 0; i < extensions.length; ++i) {
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
		mgrs = list.toArray(new IConfigManager[list.size()]);
	}
}
