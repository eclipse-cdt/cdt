/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.extensions.IExternalSearchProvider;

/**
 * Maintains a list of extensions implementing the org.eclipse.cdt.ui.externalSearchProviders
 * extension point.
 */
public class CSearchProviderManager {
	private final String SEARCH_PROVIDERS = CUIPlugin.PLUGIN_ID + ".externalSearchProviders"; //$NON-NLS-1$
	private final Object ELEMENT_PROVIDER = "provider"; //$NON-NLS-1$
	private final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	private List<IExternalSearchProvider> externalSearchProviders;

	public static CSearchProviderManager INSTANCE = new CSearchProviderManager();

	private CSearchProviderManager() {}

	public List<IExternalSearchProvider> getExternalSearchProviders() {
		if (externalSearchProviders == null) {
			externalSearchProviders = new ArrayList<IExternalSearchProvider>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint indexProviderPoint = registry.getExtensionPoint(SEARCH_PROVIDERS);
			for (IExtension extension : indexProviderPoint.getExtensions()) {
				try {
					for (IConfigurationElement element : extension.getConfigurationElements()) {
						if (ELEMENT_PROVIDER.equals(element.getName())) {
		                    Object provider = element.createExecutableExtension(ATTRIBUTE_CLASS);
		                    if (provider instanceof IExternalSearchProvider) {
		                    	externalSearchProviders.add((IExternalSearchProvider) provider);
		                    } else {
		                        CUIPlugin.logError(NLS.bind(CSearchMessages.CSearchProviderManager_InvalidSearchProvider,
		                                extension.getContributor().getName()));
		                    }
		                }
					}
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		}
		return externalSearchProviders;
	}
}
