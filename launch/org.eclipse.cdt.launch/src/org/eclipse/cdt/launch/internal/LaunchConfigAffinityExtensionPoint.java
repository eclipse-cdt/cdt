/*******************************************************************************
 * Copyright (c) 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.internal;

import java.util.Collection;

import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

/**
 * Encapsulates logic to get the launchConfigAffinity extensions information.
 * The extension point is very simple. Basically, it allows an extension to
 * provide a collection of strings (launch configuration type IDs).
 */
public class LaunchConfigAffinityExtensionPoint {
	
	static private final String EXTENSION_POINT_NAME = "launchConfigAffinity"; //$NON-NLS-1$
	static private final String EXTENSION_ELEMENT_NAME = "launchConfigTypeId"; //$NON-NLS-1$
	static private final String EXTENSION_ELEMENT_ATTR = "id"; //$NON-NLS-1$

	/**
	 * Returns all launch configuration type IDs registered via the extension
	 * point.
	 * 
	 * @param ids
	 *            Caller provides the collection. We just add to it. We do not
	 *            clear it. Caller can provide any type of collection.
	 */
	static public <T extends Collection<String>> void getLaunchConfigTypeIds(T ids) {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(LaunchUIPlugin.PLUGIN_ID, EXTENSION_POINT_NAME).getExtensions();
	    for (IExtension extension : extensions) {
	        IConfigurationElement[] elements = extension.getConfigurationElements();
	        for (IConfigurationElement element : elements) {
	        	if (element.getName().equals(EXTENSION_ELEMENT_NAME)) {
	        		String id = element.getAttribute(EXTENSION_ELEMENT_ATTR);
	        		if (id != null) {
	        			ids.add(id);
	        		}
	        	}
	        }
	    }
	}
}
