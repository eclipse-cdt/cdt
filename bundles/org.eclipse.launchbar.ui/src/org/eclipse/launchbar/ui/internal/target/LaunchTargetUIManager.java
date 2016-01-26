/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class LaunchTargetUIManager implements ILaunchTargetUIManager {
	private Map<String, IConfigurationElement> typeElements;
	private Map<String, ILabelProvider> labelProviders = new HashMap<>();
	private IWizardDescriptor[] wizards;

	@Override
	public synchronized ILabelProvider getLabelProvider(ILaunchTarget target) {
		if (typeElements == null) {
			// Load them up
			typeElements = new HashMap<>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry
					.getExtensionPoint(Activator.getDefault().getBundle().getSymbolicName() + ".launchTargetTypeUI"); //$NON-NLS-1$
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (id != null) {
						typeElements.put(id, element);
					}
				}
			}
		}
		String typeId = target.getTypeId();
		ILabelProvider labelProvider = labelProviders.get(typeId);
		if (labelProvider == null) {
			IConfigurationElement element = typeElements.get(typeId);
			if (element != null) {
				try {
					labelProvider = (ILabelProvider) element.createExecutableExtension("labelProvider"); //$NON-NLS-1$
				} catch (CoreException e) {
					Activator.log(e);
				}
			}
			if (labelProvider == null) {
				labelProvider = new LabelProvider() {
					@Override
					public String getText(Object element) {
						if (element instanceof ILaunchTarget) {
							return ((ILaunchTarget) element).getName();
						}
						return super.getText(element);
					}

					@Override
					public Image getImage(Object element) {
						if (element instanceof ILaunchTarget) {
							return Activator.getDefault().getImage(Activator.IMG_LOCAL_TARGET);
						}
						return super.getImage(element);
					}
				};
			}
		}
		return labelProvider;
	}

	@Override
	public synchronized IWizardDescriptor[] getLaunchTargetWizards() {
		if (wizards != null)
			return wizards;
		WizardsRegistryReader reader = new WizardsRegistryReader(Activator.PLUGIN_ID, "launchTargetTypeUI"); //$NON-NLS-1$
		WizardCollectionElement wizardElements = reader.getWizardElements();
		List<IWizardDescriptor> result = collectWizards(wizardElements, new ArrayList<>());
		wizards = result.toArray(new IWizardDescriptor[result.size()]);
		return wizards;
	}

	/* we don't show categories we have to flatten the wizards */
	private List<IWizardDescriptor> collectWizards(WizardCollectionElement element, List<IWizardDescriptor> result) {
		Object[] children = element.getChildren(null); // children are categories
		IWizardDescriptor[] wizards = element.getWizards();
		for (IWizardDescriptor desc : wizards) {
			result.add(desc);
		}
		for (Object cat : children) {
			WizardCollectionElement category = (WizardCollectionElement) cat;
			collectWizards(category, result);
		}
		return result;
	}
}
