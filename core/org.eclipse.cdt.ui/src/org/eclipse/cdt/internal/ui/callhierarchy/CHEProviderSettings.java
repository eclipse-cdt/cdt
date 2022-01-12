/*******************************************************************************
 * Copyright (c) 2018 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lidia Popescu - [536255] initial API and implementation. Extension point for open call hierarchy view
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.ui.ICHEContentProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;

/**
 * The Call Hierarchy Extension provider Settings
 * Responsible to load all available extensions for EXTENSION_POINT_ID
 * */
public class CHEProviderSettings {

	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CCallHierarchy"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_CONTENT = "CallHierarchyContentProvider"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_LABEL = "CallHierarchyLabelProvider"; //$NON-NLS-1$
	private static final String ATTRIB_CLASS = "class"; //$NON-NLS-1$

	IOpenListener[] openListeners = null;

	static ICHEContentProvider[] chContentProviders = null;
	static IStyledLabelProvider[] chLabelProviders = null;

	private static void loadExtensions() {
		List<ICHEContentProvider> chCProviders = new ArrayList<>();
		List<IStyledLabelProvider> chLProviders = new ArrayList<>();

		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint != null) {
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions != null) {
				for (IExtension ex : extensions) {
					for (IConfigurationElement el : ex.getConfigurationElements()) {
						if (el.getName().equals(ELEMENT_NAME_CONTENT)) {
							ICHEContentProvider provider = null;
							try {
								provider = (ICHEContentProvider) el.createExecutableExtension(ATTRIB_CLASS);
							} catch (CoreException e) {
								e.printStackTrace();
							}
							if (provider != null) {
								chCProviders.add(provider);
							}
						}
						if (el.getName().equals(ELEMENT_NAME_LABEL)) {
							IStyledLabelProvider provider = null;
							try {
								provider = (IStyledLabelProvider) el.createExecutableExtension(ATTRIB_CLASS);
							} catch (CoreException e) {
								e.printStackTrace();
							}
							if (provider != null) {
								chLProviders.add(provider);
							}
						}
					}
				}
			}
		}
		chLabelProviders = chLProviders.toArray(new IStyledLabelProvider[chLProviders.size()]);
		chContentProviders = chCProviders.toArray(new ICHEContentProvider[chCProviders.size()]);
	}

	public static IStyledLabelProvider[] getCCallHierarchyLabelProviders() {
		if (chLabelProviders == null) {
			loadExtensions();
		}
		return chLabelProviders;
	}

	public static ICHEContentProvider[] getCCallHierarchyContentProviders() {
		if (chContentProviders == null) {
			loadExtensions();
		}
		return chContentProviders;
	}
}
