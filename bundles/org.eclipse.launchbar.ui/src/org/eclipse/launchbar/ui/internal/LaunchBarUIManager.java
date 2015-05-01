/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.ExecutableExtension;
import org.eclipse.launchbar.core.internal.LaunchBarManager;

public class LaunchBarUIManager {

	private LaunchBarManager manager;
	private Map<String, ExecutableExtension<ILabelProvider>> descriptorLabelProviders = new HashMap<>();

	public LaunchBarUIManager(LaunchBarManager manager) {
		this.manager = manager;

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarUIContributions"); //$NON-NLS-1$
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				String elementName = element.getName();
				if (elementName.equals("descriptorUI")) { //$NON-NLS-1$
					String descriptorTypeId = element.getAttribute("descriptorTypeId"); //$NON-NLS-1$
					ExecutableExtension<ILabelProvider> labelProvider = new ExecutableExtension<>(element, "labelProvider"); //$NON-NLS-1$
					descriptorLabelProviders.put(descriptorTypeId, labelProvider);
				}
			}
		}
	}

	public LaunchBarManager getManager() {
		return manager;
	}

	public ILabelProvider getLabelProvider(ILaunchDescriptor descriptor) throws CoreException {
		ExecutableExtension<ILabelProvider> provider = descriptorLabelProviders.get(manager.getDescriptorTypeId(descriptor.getType()));
		return provider != null ? provider.get() : null;
	}

}
