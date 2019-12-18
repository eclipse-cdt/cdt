/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.target;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.target.ILaunchTargetUIManager;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class LaunchTargetUIManager implements ILaunchTargetUIManager {
	private Map<String, IConfigurationElement> typeElements;
	private Map<String, ILabelProvider> labelProviders = new HashMap<>();
	private Map<String, IConfigurationElement> editElements;

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
					if ("launchTargetTypeUI".equals(element.getName())) { //$NON-NLS-1$
						String id = element.getAttribute("id"); //$NON-NLS-1$
						if (id != null) {
							typeElements.put(id, element);
						}
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
							return ((ILaunchTarget) element).getId();
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
	public IWizardDescriptor[] getLaunchTargetWizards() {
		// No one one should be using this. The new target wizard is internal.
		return null;
	}

	@Override
	public void editLaunchTarget(ILaunchTarget target) {
		if (editElements == null) {
			// Load them up
			editElements = new HashMap<>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry
					.getExtensionPoint(Activator.getDefault().getBundle().getSymbolicName() + ".launchTargetTypeUI"); //$NON-NLS-1$
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if ("wizard2".equals(element.getName())) { //$NON-NLS-1$
						String id = element.getAttribute("id"); //$NON-NLS-1$
						if (id != null) {
							editElements.put(id, element);
						}
					}
				}
			}
		}

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		IConfigurationElement element = editElements.get(target.getTypeId());
		if (element != null) {
			try {
				LaunchTargetWizard wizard = (LaunchTargetWizard) element.createExecutableExtension("class"); //$NON-NLS-1$
				wizard.setLaunchTarget(target);
				WizardDialog dialog = wizard.canDelete() ? new LaunchTargetWizardDialog(shell, wizard)
						: new WizardDialog(shell, wizard);
				dialog.open();
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}
		} else {
			new PropertyDialogAction(new SameShellProvider(shell), new ISelectionProvider() {
				@Override
				public void setSelection(ISelection selection) {
					// ignore
				}

				@Override
				public void removeSelectionChangedListener(ISelectionChangedListener listener) {
					// ignore
				}

				@Override
				public ISelection getSelection() {
					return new StructuredSelection(target);
				}

				@Override
				public void addSelectionChangedListener(ISelectionChangedListener listener) {
					// ignore
				}
			}).run();
		}
	}

}
