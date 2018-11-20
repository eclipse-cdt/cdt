/*******************************************************************************
 * Copyright (c) 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.extensions.ICallHierarchyProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

/**
 * Maintains a list of extensions implementing the org.eclipse.cdt.ui.callHierarchyProviders
 * extension point.
 */
public class CHProviderManager {
	private final String CALL_HIERARCHY_PROVIDERS = CUIPlugin.PLUGIN_ID + ".callHierarchyProviders"; //$NON-NLS-1$
	private final Object ELEMENT_PROVIDER = "provider"; //$NON-NLS-1$
	private final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	private List<ICallHierarchyProvider> callHierarchyProviders;

	public static CHProviderManager INSTANCE = new CHProviderManager();

	private CHProviderManager() {
	}

	public List<ICallHierarchyProvider> getCallHierarchyProviders() {
		if (callHierarchyProviders == null) {
			callHierarchyProviders = new ArrayList<>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint indexProviderPoint = registry.getExtensionPoint(CALL_HIERARCHY_PROVIDERS);
			for (IExtension extension : indexProviderPoint.getExtensions()) {
				try {
					for (IConfigurationElement element : extension.getConfigurationElements()) {
						if (ELEMENT_PROVIDER.equals(element.getName())) {
							Object provider = element.createExecutableExtension(ATTRIBUTE_CLASS);
							if (provider instanceof ICallHierarchyProvider) {
								callHierarchyProviders.add((ICallHierarchyProvider) provider);
							} else {
								CUIPlugin.logError(NLS.bind(CHMessages.CHProviderManager_InvalidCallHierarchyProvider,
										extension.getContributor().getName()));
							}
						}
					}
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		}
		return callHierarchyProviders;
	}
}
