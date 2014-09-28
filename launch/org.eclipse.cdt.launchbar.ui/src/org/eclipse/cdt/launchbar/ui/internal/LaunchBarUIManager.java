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
import java.util.Map.Entry;

import org.eclipse.cdt.launchbar.core.ILaunchDescriptor;
import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.cdt.launchbar.core.internal.ExecutableExtension;
import org.eclipse.cdt.launchbar.core.internal.LaunchBarManager;
import org.eclipse.cdt.launchbar.ui.IHoverProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;

public class LaunchBarUIManager {

	LaunchBarManager manager;
	Map<String, ExecutableExtension<ILabelProvider>> descriptorLabelProviders = new HashMap<>();
	Map<String, LaunchBarTargetContribution> targetContributions = new HashMap<>();

	private final LaunchBarTargetContribution DEFAULT_CONTRIBUTION = new LaunchBarTargetContribution(null, null, null, null,
	        null, null);

	public LaunchBarUIManager(LaunchBarManager manager) {
		this.manager = manager;

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarUIContributions");
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				String elementName = element.getName();
				if (elementName.equals("descriptorUI")) {
					String descriptorTypeId = element.getAttribute("descriptorTypeId");
					ExecutableExtension<ILabelProvider> labelProvider = new ExecutableExtension<>(element, "labelProvider");
					descriptorLabelProviders.put(descriptorTypeId, labelProvider);
				} else if (elementName.equals("targetUI")) {
					String targetTypeId = element.getAttribute("targetTypeId");
					String targetName = element.getAttribute("name");
					String iconStr = element.getAttribute("icon");
					ExecutableExtension<ILabelProvider> labelProvider = new ExecutableExtension<ILabelProvider>(element, "labelProvider");

					ExecutableExtension<IHoverProvider> hoverProvider = null;
					if (element.getAttribute("hoverProvider") != null) {
						hoverProvider = new ExecutableExtension<IHoverProvider>(element, "hoverProvider");
					}

					String editCommandId = element.getAttribute("editCommandId");
					
					ExecutableExtension<INewWizard> newWizard = null;
					if (element.getAttribute("newWizard") != null) {
						newWizard = new ExecutableExtension<INewWizard>(element, "newWizard");
					}

					targetContributions.put(targetTypeId, new LaunchBarTargetContribution(targetName, iconStr,
					        labelProvider, hoverProvider, editCommandId, newWizard));
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

	public String getTargetTypeName(ILaunchTarget target) {
		return getTargetTypeName(target.getType());
	}

	public String getTargetTypeName(ILaunchTargetType targetType) {
		String typeId = manager.getTargetTypeId(targetType);
		String name = targetContributions.get(typeId).name;
		return name != null ? name : typeId;
	}

	public Image getTargetTypeIcon(ILaunchTargetType targetType) {
		String typeId = manager.getTargetTypeId(targetType);
		return targetContributions.get(typeId).getIcon();
	}

	public ILabelProvider getLabelProvider(ILaunchTarget target) throws CoreException {
		ExecutableExtension<ILabelProvider> provider = getContribution(target).labelProvider;
		return provider != null ? provider.get() : null;
	}

	public IHoverProvider getHoverProvider(ILaunchTarget target) throws CoreException {
		ExecutableExtension<IHoverProvider> hoverProvider = getContribution(target).hoverProvider;
		return hoverProvider != null ? hoverProvider.get() : null;
	}

	public String getEditCommand(ILaunchTarget target) {
		return getContribution(target).editCommandId;
	}

	public Map<ILaunchTargetType, ExecutableExtension<INewWizard>> getNewTargetWizards() {
		Map<ILaunchTargetType, ExecutableExtension<INewWizard>> wizards = new HashMap<>();
		for (Entry<String, LaunchBarTargetContribution> contrib : targetContributions.entrySet()) {
			if (contrib.getValue().newWizard != null) {
				ILaunchTargetType type = manager.getLaunchTargetType(contrib.getKey());
				if (type != null) {
					wizards.put(type, contrib.getValue().newWizard);
				}
			}
		}
		return wizards;
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
		LaunchBarTargetContribution c = targetContributions.get(manager.getTargetTypeId(target.getType()));
		if (c == null) {
			return DEFAULT_CONTRIBUTION;
		}
		return c;
	}

	private class LaunchBarTargetContribution {
		String name;
		String iconStr;
		Image icon;
		ExecutableExtension<ILabelProvider> labelProvider;
		ExecutableExtension<IHoverProvider> hoverProvider;
		String editCommandId;
		ExecutableExtension<INewWizard> newWizard;

		LaunchBarTargetContribution(String name, String iconStr,
				ExecutableExtension<ILabelProvider> labelProvider,
		        ExecutableExtension<IHoverProvider> hoverProvider,
		        String editCommand,
		        ExecutableExtension<INewWizard> newWizard) {
			this.name = name;
			this.iconStr = iconStr;
			this.icon = null;
			this.labelProvider = labelProvider;
			this.hoverProvider = hoverProvider;
			this.editCommandId = editCommand;
			this.newWizard = newWizard;
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
