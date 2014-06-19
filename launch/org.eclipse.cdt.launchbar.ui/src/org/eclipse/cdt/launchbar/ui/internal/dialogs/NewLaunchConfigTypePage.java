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
package org.eclipse.cdt.launchbar.ui.internal.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

public class NewLaunchConfigTypePage extends WizardPage {

	private Table table;
	ILaunchConfigurationType type;
	
	public NewLaunchConfigTypePage() {
		super("Select Launch Configuration Type");
		setTitle("Launch Configuration Type");
		setDescription("Select the type of launch configuration to create.");
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
		
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				type = (ILaunchConfigurationType)table.getSelection()[0].getData();
				setMessage("Initializing. Please wait...", INFORMATION);
				UIJob job = new UIJob("Updating Page") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						((NewLaunchConfigWizard)getWizard()).editPage.changeLaunchConfigType();
						setPageComplete(true);
						setMessage(null);
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
			}
		});
		
		setControl(comp);
	}

	void populateItems() {
		ILaunchGroup group = ((NewLaunchConfigWizard)getWizard()).modePage.selectedGroup;
		if (group == null)
			return;

		table.removeAll();
		
		for (ILaunchConfigurationType type : DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes()) {
			if (!type.isPublic() || type.getCategory() != null || !type.supportsMode(group.getMode()))
				continue;

			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(type.getName());
			ImageDescriptor imageDesc = DebugUITools.getDefaultImageDescriptor(type);
			if (imageDesc != null)
				item.setImage(imageDesc.createImage());
			item.setData(type);
		}
		
		type = null;
		setPageComplete(false);
	}

}
