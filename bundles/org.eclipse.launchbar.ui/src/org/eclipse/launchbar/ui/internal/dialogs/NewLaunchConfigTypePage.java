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
package org.eclipse.launchbar.ui.internal.dialogs;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class NewLaunchConfigTypePage extends WizardPage {

	private Table table;
	
	public NewLaunchConfigTypePage() {
		super(Messages.NewLaunchConfigTypePage_0);
		setTitle(Messages.NewLaunchConfigTypePage_1);
		setDescription(Messages.NewLaunchConfigTypePage_2);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		
		table = new Table(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = 500;
		table.setLayoutData(data);

		populateItems();
		
		setControl(comp);
	}

	void populateItems() {
		ILaunchGroup group = ((NewLaunchConfigWizard)getWizard()).modePage.selectedGroup;
		if (group == null)
			return;

		table.removeAll();

		boolean haveItems = false;
		for (ILaunchConfigurationType type : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes()) {
			if (!type.isPublic() || type.getCategory() != null || !type.supportsMode(group.getMode()))
				continue;

			haveItems = true;
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(type.getName());
			ImageDescriptor imageDesc = DebugUITools.getDefaultImageDescriptor(type);
			if (imageDesc != null)
				item.setImage(imageDesc.createImage());
			item.setData(type);
		}
		
		if (haveItems) {
			table.select(0);
		}
		setPageComplete(haveItems);
	}

	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	@Override
	public IWizardPage getNextPage() {
		ILaunchConfigurationType type = (ILaunchConfigurationType)table.getSelection()[0].getData();
		NewLaunchConfigEditPage editPage = ((NewLaunchConfigWizard)getWizard()).editPage;
		editPage.changeLaunchConfigType(type);
		return editPage;
	}

}
