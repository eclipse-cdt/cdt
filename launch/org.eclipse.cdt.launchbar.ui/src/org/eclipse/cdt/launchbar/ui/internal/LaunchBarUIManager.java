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
package org.eclipse.cdt.launchbar.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.cdt.launchbar.ui.IHoverProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;

public class LaunchBarUIManager {

	ILaunchBarManager manager;
	Map<String, ILabelProvider> descriptorLabelProviders = new HashMap<>();
	Map<String, ILabelProvider> targetLabelProviders = new HashMap<>();
	Map<String, IHoverProvider> targetHoverProviders = new HashMap<>();
	Map<String, String> targetEditCommandIds = new HashMap<>();
	Map<String, String> targetAddNewCommandIds = new HashMap<>();
	
	public LaunchBarUIManager(ILaunchBarManager manager) {
		this.manager = manager;

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarUIContributions");
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				String elementName = element.getName();
				if (elementName.equals("descriptorUI")) {
					try {
						String descriptorTypeId = element.getAttribute("descriptorTypeId");

						ILabelProvider labelProvider = (ILabelProvider) element.createExecutableExtension("labelProvider");
						descriptorLabelProviders.put(descriptorTypeId, labelProvider);
					} catch (CoreException e) {
						Activator.log(e.getStatus());
					}
				} else if (elementName.equals("targetUI")) {
					try {
						String targetTypeId = element.getAttribute("targetTypeId");

						ILabelProvider labelProvider = (ILabelProvider) element.createExecutableExtension("labelProvider");
						targetLabelProviders.put(targetTypeId, labelProvider);
						
						IHoverProvider hoverProvider = (IHoverProvider) element.createExecutableExtension("hoverProvider");
						if (hoverProvider != null)
							targetHoverProviders.put(targetTypeId, hoverProvider);
						
						String editCommandId = element.getAttribute("editCommandId");
						if (editCommandId != null && editCommandId.length() > 0)
							targetEditCommandIds.put(targetTypeId, editCommandId);

						String addNewCommandId = element.getAttribute("addNewTargetCommandId");
						if (addNewCommandId != null && addNewCommandId.length() > 0)
							targetAddNewCommandIds.put(targetTypeId, addNewCommandId);
					} catch (CoreException e) {
						Activator.log(e.getStatus());
					}
				}
			}
		}
	}

	public ILaunchBarManager getManager() {
		return manager;
	}
	
	public ILabelProvider getLabelProvider(ILaunchDescriptor descriptor) {
		return descriptorLabelProviders.get(descriptor.getType().getId());
	}

	public ILabelProvider getLabelProvider(ILaunchTarget target) {
		return targetLabelProviders.get(target.getType().getId());
	}

	public IHoverProvider getHoverProvider(ILaunchTarget target) {
		return targetHoverProviders.get(target.getType().getId());
	}

	public String getEditCommand(ILaunchTarget target) {
		return targetEditCommandIds.get(target.getType().getId());
	}

	public String getAddTargetCommand(ILaunchTargetType targetType) {
		return targetAddNewCommandIds.get(targetType.getId());
	}
}
