/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.build;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.build.ToolChainWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class NewToolChainWizardSelectionPage extends WizardPage {

	private Table table;

	public NewToolChainWizardSelectionPage() {
		super(NewToolChainWizardSelectionPage.class.getName());

		setTitle(CUIMessages.NewToolChainWizardSelectionPage_Title);
		setDescription(CUIMessages.NewToolChainWizardSelectionPage_Description);
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		table = new Table(comp, SWT.BORDER | SWT.SINGLE);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(CUIPlugin.PLUGIN_ID + ".newToolChainWizards"); //$NON-NLS-1$
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				String name = element.getAttribute("name"); //$NON-NLS-1$
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(name);

				String iconFile = element.getAttribute("icon"); //$NON-NLS-1$
				if (iconFile != null) {
					ImageDescriptor desc = CUIPlugin.imageDescriptorFromPlugin(element.getNamespaceIdentifier(),
							iconFile);
					if (desc != null) {
						item.setImage(desc.createImage());
					}
				}

				item.setData(element);
			}
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
			ToolChainWizard nextWizard;
			if (obj instanceof IConfigurationElement) {
				IConfigurationElement element = (IConfigurationElement) obj;
				try {
					nextWizard = (ToolChainWizard) element.createExecutableExtension("class"); //$NON-NLS-1$
					nextWizard.addPages();
					item.setData(nextWizard);
				} catch (CoreException e) {
					CUIPlugin.log(e);
					return null;
				}
			} else {
				nextWizard = (ToolChainWizard) obj;
			}

			return nextWizard.getStartingPage();
		}
		return super.getNextPage();
	}

}
