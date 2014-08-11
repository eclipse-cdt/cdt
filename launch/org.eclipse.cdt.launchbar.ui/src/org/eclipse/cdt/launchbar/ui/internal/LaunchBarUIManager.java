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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.launchbar.core.ILaunchBarManager;
import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.ui.IHoverProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

public class LaunchBarUIManager {

	ILaunchBarManager manager;
	Map<String, ILabelProvider> descriptorLabelProviders = new HashMap<>();
	Map<String, LaunchBarTargetContribution> targetContributions = new HashMap<>();

	private final LaunchBarTargetContribution DEFAULT_CONTRIBUTION = new LaunchBarTargetContribution(null, null, null, null, null,
	        null, null);

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
						String targetName = element.getAttribute("name");

						String iconStr = element.getAttribute("icon");

						ILabelProvider labelProvider = (ILabelProvider) element.createExecutableExtension("labelProvider");

						IHoverProvider hoverProvider = null;
						if (element.getAttribute("hoverProvider") != null) {
							hoverProvider = (IHoverProvider) element.createExecutableExtension("hoverProvider");
						}
						String editCommandId = element.getAttribute("editCommandId");
						String addNewCommandId = element.getAttribute("addNewTargetCommandId");

						targetContributions.put(targetTypeId, new LaunchBarTargetContribution(targetTypeId, targetName, iconStr,
						        labelProvider, hoverProvider, editCommandId, addNewCommandId));
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

	public String getTargetTypeName(ILaunchTarget target) {
		return getContribution(target).name;
	}

	public ILabelProvider getLabelProvider(ILaunchTarget target) {
		return getContribution(target).labelProvider;
	}

	public IHoverProvider getHoverProvider(ILaunchTarget target) {
		return getContribution(target).hoverProvider;
	}

	public String getEditCommand(ILaunchTarget target) {
		return getContribution(target).editCommandId;
	}

	public String getAddTargetCommand(ILaunchTarget target) {
		return getContribution(target).addNewCommandId;
	}

	public Map<String, String> getAddTargetCommands() {
		Map<String, String> commands = new HashMap<>();
		for (LaunchBarTargetContribution contribution : targetContributions.values()) {
			if (contribution.addNewCommandId != null)
				commands.put(contribution.name, contribution.addNewCommandId);
		}
		return commands;
	}

	public Map<String, Image> getTargetIcons() {
		Map<String, Image> icons = new HashMap<>();
		for (LaunchBarTargetContribution contribution : targetContributions.values()) {
			Image icon = contribution.getIcon();
			if (icon != null) {
				icons.put(contribution.name, icon);
			}
		}
		return icons;
	}

	private LaunchBarTargetContribution getContribution(ILaunchTarget target) {
		LaunchBarTargetContribution c = targetContributions.get(target.getType().getId());
		if (c == null) {
			return DEFAULT_CONTRIBUTION;
		}
		return c;
	}

	private class LaunchBarTargetContribution {

		String id;
		String name;
		String iconStr;
		Image icon;
		ILabelProvider labelProvider;
		IHoverProvider hoverProvider;
		String editCommandId;
		String addNewCommandId;

		LaunchBarTargetContribution(String id, String name, String iconStr, ILabelProvider labelProvider,
		        IHoverProvider hoverProvider, String editCommand, String addNewCommand) {
			this.id = id;
			this.name = name;
			this.iconStr = iconStr;
			this.icon = null;
			this.labelProvider = labelProvider;
			this.hoverProvider = hoverProvider;
			this.editCommandId = editCommand;
			this.addNewCommandId = addNewCommand;
		}

		Image getIcon() {
			if (icon == null) {
				if (iconStr != null && !iconStr.isEmpty()) {
					try {
						icon = ImageDescriptor.createFromURL(new URL(iconStr)).createImage();
					} catch (MalformedURLException e) {
						Activator.log(e);
					}
				}
			}
			return icon;
		}

	}
}
