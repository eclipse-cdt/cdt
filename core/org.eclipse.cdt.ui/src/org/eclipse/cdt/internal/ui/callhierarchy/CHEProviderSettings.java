/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lidia Popescu - [536255] initial API and implementation. Extension point for open call hierarchy view
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.cdt.ui.ICHEProvider;

/**
 * The Call Hierarchy Extension provider Settings
 * Responsible to load all available extensions for EXTENSION_POINT_ID
 * */
public class CHEProviderSettings {

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CCallHierarchy"; //$NON-NLS-1$
	private static final String ELEMENT_NAME  = "CallHierarchyProvider"; //$NON-NLS-1$
	private static final String ATTRIB_CLASS   = "class"; //$NON-NLS-1$
	
	private static boolean Done = false;
	IOpenListener[]  openListeners =null;

	static ICHEProvider[] chProviders = null;
	static Map<String, ICHEProvider> fProvidersMap = null;
	
	/**
	 * @return  value is never null, it is empty in case no providers
	 * */	
	private static ICHEProvider[] loadExtensions() {
		List<ICHEProvider> chProviders = new ArrayList<ICHEProvider>();
		try {		
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
			if (extensionPoint != null) {
				IExtension[] extensions = extensionPoint.getExtensions();
				if (extensions != null) {
					fProvidersMap =  new HashMap<String, ICHEProvider>();
					
					for (IExtension ex: extensions)	{
						String pluginId = ex.getNamespaceIdentifier();
						
						for (IConfigurationElement el : ex.getConfigurationElements()) {
							if (el.getName().equals(ELEMENT_NAME)) {
								ICHEProvider provider = null;
								try {
									provider = (ICHEProvider)el.createExecutableExtension(ATTRIB_CLASS);
								} catch (CoreException e) {
									e.printStackTrace();
								}
								if (provider != null) {
									fProvidersMap.put(pluginId, provider);
									chProviders.add(provider);
								}
							}
						}
					}
				}
			}
		} finally {
			Done = true;
		}
		return chProviders.toArray(new ICHEProvider[chProviders.size()]);
	}


	public static ICHEProvider[] getCCallHierarchyProviders() {
		if ( chProviders == null) {
			chProviders = loadExtensions();	
		}
		waitForDone();
		return chProviders;
	}
	
	private static void waitForDone() {
		if ( Done )
			return;
		try {
			while (! Done ) Thread.sleep(10);
		} catch (InterruptedException e) {}
	}
}
