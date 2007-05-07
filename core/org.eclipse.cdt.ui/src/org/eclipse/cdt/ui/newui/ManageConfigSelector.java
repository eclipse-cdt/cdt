/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * This class provides static methods to work with 
 * 
 *
 */
public class ManageConfigSelector {
	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.ConfigManager"; //$NON-NLS-1$
	public static final String ELEMENT_NAME = "manager"; //$NON-NLS-1$
	public static final String CLASS_NAME = "class"; //$NON-NLS-1$
	private static IConfigManager[] mgrs = null;
	
	public static IConfigManager getManager(Object[] obs) {
		readMgrs();
		if (mgrs == null)
			return null;
		for (int i=0; i<mgrs.length; i++) {
			if (mgrs[i].canManage(obs))
				return mgrs[i];
		}
		return null;
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
