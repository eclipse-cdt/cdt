/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.target;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

class NewLaunchTargetWizardSelectionPage extends WizardPage {

	private Table table;

	public NewLaunchTargetWizardSelectionPage() {
		super(NewLaunchTargetWizardSelectionPage.class.getName());
		setTitle(Messages.NewLaunchTargetWizardSelectionPage_Title);
		setDescription(Messages.NewLaunchTargetWizardSelectionPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		table = new Table(comp, SWT.BORDER | SWT.SINGLE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		List<IConfigurationElement> elements = new ArrayList<>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(Activator.PLUGIN_ID + ".launchTargetTypeUI"); //$NON-NLS-1$
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				String elementName = element.getName();
				if ("wizard2".equals(elementName) || "wizard".equals(elementName)) { //$NON-NLS-1$ //$NON-NLS-2$
					elements.add(element);
				}
			}
		}

		elements.sort((o1, o2) -> {
			String name1 = o1.getAttribute("name"); //$NON-NLS-1$
			String name2 = o2.getAttribute("name"); //$NON-NLS-1$
			return name1.compareTo(name2);
		});

		for (IConfigurationElement element : elements) {
			String name = element.getAttribute("name"); //$NON-NLS-1$
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(name);

			String iconFile = element.getAttribute("icon"); //$NON-NLS-1$
			if (iconFile != null) {
				ImageDescriptor desc = Activator.imageDescriptorFromPlugin(element.getNamespaceIdentifier(), iconFile);
				if (desc != null) {
					item.setImage(desc.createImage());
				}
			}

			item.setData(element);
		}

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				if (canFlipToNextPage()) {
					getContainer().showPage(getNextPage());
				}
			}
		});

		setControl(comp);
	}

	@Override
	public void dispose() {
		for (TableItem item : table.getItems()) {
			Object obj = item.getData();
			if (obj instanceof Wizard) {
				((Wizard) obj).dispose();
			}
		}
		super.dispose();
	}

	public ImageDescriptor getDescriptionImage(IConfigurationElement element) {
		String descImage = element.getAttribute("icon"); //$NON-NLS-1$
		if (descImage == null) {
			return null;
		}
		return AbstractUIPlugin.imageDescriptorFromPlugin(element.getNamespaceIdentifier(), descImage);
	}

	@Override
	public boolean canFlipToNextPage() {
		return table.getSelectionIndex() >= 0;
	}

	@Override
	public IWizardPage getNextPage() {
		int i = table.getSelectionIndex();
		if (i >= 0) {
			TableItem item = table.getItem(i);
			Object obj = item.getData();
			Wizard nextWizard;
			if (obj instanceof IConfigurationElement) {
				IConfigurationElement element = (IConfigurationElement) obj;
				try {
					nextWizard = (Wizard) element.createExecutableExtension("class"); //$NON-NLS-1$
					nextWizard.addPages();
					if (nextWizard instanceof IWorkbenchWizard) {
						((IWorkbenchWizard) nextWizard).init(PlatformUI.getWorkbench(), new StructuredSelection());
					}
					item.setData(nextWizard);
				} catch (CoreException e) {
					Activator.log(e);
					return null;
				}
			} else {
				nextWizard = (Wizard) obj;
			}

			return nextWizard.getStartingPage();
		}
		return super.getNextPage();
	}

}
