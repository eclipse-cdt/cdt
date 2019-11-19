/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.dialogs;

import java.util.Arrays;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class NewLaunchConfigTypePage2 extends WizardPage {

	private final NewLaunchConfigEditPage editPage;

	private Table modeTable;
	private Table typeTable;

	public NewLaunchConfigTypePage2(NewLaunchConfigEditPage editPage) {
		super(NewLaunchConfigTypePage2.class.getName());
		this.editPage = editPage;
		setTitle(Messages.NewLaunchConfigTypePage_1);
		setDescription(Messages.NewLaunchConfigTypePage_2);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		Group modeGroup = new Group(comp, SWT.NONE);
		modeGroup.setText(Messages.NewLaunchConfigTypePage2_Mode);
		modeGroup.setLayout(new GridLayout());
		modeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		modeTable = new Table(modeGroup, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = 100;
		modeTable.setLayoutData(data);
		modeTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modeChanged();
			}
		});

		Group typeGroup = new Group(comp, SWT.NONE);
		typeGroup.setText(Messages.NewLaunchConfigTypePage2_Type);
		typeGroup.setLayout(new GridLayout());
		typeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		typeTable = new Table(typeGroup, SWT.SINGLE | SWT.BORDER);
		typeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		typeTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				typeChanged();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				getContainer().showPage(getNextPage());
			}
		});

		populateMode();

		setControl(comp);
	}

	private void populateMode() {
		int select = -1;

		for (ILaunchGroup group : DebugUITools.getLaunchGroups()) {
			if (group.getMode().equals("run")) { //$NON-NLS-1$
				if (createModeItem(modeTable, group)) {
					select++;
				}
			}
		}

		for (ILaunchGroup group : DebugUITools.getLaunchGroups()) {
			if (group.getMode().equals("debug")) { //$NON-NLS-1$
				if (createModeItem(modeTable, group)) {
					select++;
				}
			}
		}

		for (ILaunchGroup group : DebugUITools.getLaunchGroups()) {
			if (!group.getMode().equals("run") && !group.getMode().equals("debug")) { //$NON-NLS-1$ //$NON-NLS-2$
				createModeItem(modeTable, group);
			}
		}

		if (select >= 0) {
			modeTable.select(select);
			modeChanged();
		} else if (modeTable.getItemCount() > 0) {
			modeTable.select(0);
			modeChanged();
		} else {
			setPageComplete(false);
		}
	}

	private boolean createModeItem(Table table, ILaunchGroup group) {
		if (group.getCategory() != null || !group.isPublic())
			return false;

		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(group.getLabel().replace("&", "")); //$NON-NLS-1$ //$NON-NLS-2$
		ImageDescriptor imageDesc = group.getImageDescriptor();
		if (imageDesc != null) {
			item.setImage(imageDesc.createImage());
		}
		item.setData(group);

		return true;
	}

	private void modeChanged() {
		TableItem[] selection = modeTable.getSelection();
		if (selection.length == 0) {
			editPage.setLaunchGroup(null);
			setPageComplete(false);
			return;
		}

		ILaunchGroup group = (ILaunchGroup) selection[0].getData();
		editPage.setLaunchGroup(group);

		ILaunchConfigurationType[] types = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		Arrays.sort(types, (type0, type1) -> {
			int comp = type0.getPluginIdentifier().compareTo(type1.getPluginIdentifier());
			if (comp != 0) {
				return comp;
			} else {
				return type0.getName().compareTo(type1.getName());
			}
		});

		typeTable.removeAll();
		for (ILaunchConfigurationType type : types) {
			if (!type.isPublic() || type.getCategory() != null || !type.supportsMode(group.getMode()))
				continue;

			TableItem item = new TableItem(typeTable, SWT.NONE);
			item.setText(type.getName());
			ImageDescriptor imageDesc = DebugUITools.getDefaultImageDescriptor(type);
			if (imageDesc != null)
				item.setImage(imageDesc.createImage());
			item.setData(type);
		}

		if (typeTable.getItemCount() > 0) {
			typeTable.select(0);
			typeChanged();
		} else {
			setPageComplete(false);
		}
	}

	private void typeChanged() {
		TableItem[] selection = typeTable.getSelection();
		if (selection.length == 0) {
			editPage.setLaunchConfigType(null);
			setPageComplete(false);
			return;
		}
		
		ILaunchConfigurationType type = (ILaunchConfigurationType) selection[0].getData();
		editPage.setLaunchConfigType(type);
		setPageComplete(true);
	}

}
