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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class NewLaunchConfigModePage extends WizardPage {

	private Table table;
	ILaunchGroup selectedGroup;
	
	public NewLaunchConfigModePage() {
		super(Messages.NewLaunchConfigModePage_0);
		setTitle(Messages.NewLaunchConfigModePage_1);
		setDescription(Messages.NewLaunchConfigModePage_2);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		
		table = new Table(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(data);
		
		Set<ILaunchGroup> done = new HashSet<>();
		
		for (ILaunchGroup group : DebugUITools.getLaunchGroups()) {
			if (group.getMode().equals("run")) { //$NON-NLS-1$
				createModeItem(group);
				done.add(group);
			}
		}
		
		for (ILaunchGroup group : DebugUITools.getLaunchGroups()) {
			if (group.getMode().equals("debug")) { //$NON-NLS-1$
				createModeItem(group);
				done.add(group);
			}
		}
		
		for (ILaunchGroup group : DebugUITools.getLaunchGroups()) {
			if (!done.contains(group)) {
				createModeItem(group);
			}
		}

		if (table.getItemCount() > 0) {
			// Select debug as default
			int i = 0;
			boolean hasDebug = false;
			for (TableItem item : table.getItems()) {
				if ("debug".equals(((ILaunchGroup) item.getData()).getMode())) { //$NON-NLS-1$
					hasDebug = true;
					table.select(i);
					break;
				}
				i++;
			}

			if (!hasDebug) {
				table.select(0);
			}
			selectedGroup = (ILaunchGroup) table.getSelection()[0].getData();
		}
		
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedGroup = (ILaunchGroup)table.getSelection()[0].getData();
				((NewLaunchConfigWizard)getWizard()).typePage.populateItems();
			}
		});

		setControl(comp);
	}

	private void createModeItem(ILaunchGroup group) {
		if (group.getCategory() != null || !group.isPublic())
			return;
		
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(group.getLabel().replace("&", "")); //$NON-NLS-1$ //$NON-NLS-2$
		ImageDescriptor imageDesc = group.getImageDescriptor();
		if (imageDesc != null) {
			item.setImage(imageDesc.createImage());
		}
		item.setData(group);
	}

}
