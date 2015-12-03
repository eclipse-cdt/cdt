/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.qt.ui.Messages;
import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.cdt.qt.core.IQtInstallManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class QtPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IQtInstallManager manager;
	private Table installTable;
	private Button removeButton;

	private Map<String, IQtInstall> installsToAdd = new HashMap<>();
	private Map<String, IQtInstall> installsToRemove = new HashMap<>();

	@Override
	public void init(IWorkbench workbench) {
		manager = Activator.getService(IQtInstallManager.class);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group installsGroup = new Group(control, SWT.NONE);
		installsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		installsGroup.setText(Messages.QtPreferencePage_0);
		installsGroup.setLayout(new GridLayout(2, false));

		Composite installTableComp = new Composite(installsGroup, SWT.NONE);
		installTableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		installTable = new Table(installTableComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		installTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		installTable.setHeaderVisible(true);
		installTable.setLinesVisible(true);
		installTable.addListener(SWT.Selection, e -> {
			TableItem[] items = installTable.getSelection();
			removeButton.setEnabled(items.length > 0);
		});

		TableColumn nameColumn = new TableColumn(installTable, SWT.NONE);
		nameColumn.setText(Messages.QtPreferencePage_1);

		TableColumn locationColumn = new TableColumn(installTable, SWT.NONE);
		locationColumn.setText(Messages.QtPreferencePage_2);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(nameColumn, new ColumnWeightData(25, 50, true));
		tableLayout.setColumnData(locationColumn, new ColumnWeightData(75, 150, true));
		installTableComp.setLayout(tableLayout);

		Composite buttonsComp = new Composite(installsGroup, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonsComp.setLayout(new GridLayout());

		Button addButton = new Button(buttonsComp, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addButton.setText(Messages.QtPreferencePage_3);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewQtInstallWizard wizard = new NewQtInstallWizard(getInstalls());
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					IQtInstall install = wizard.getInstall();
					installsToAdd.put(install.getName(), install);
					updateTable();
				}
			}
		});

		removeButton = new Button(buttonsComp, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		removeButton.setText(Messages.QtPreferencePage_4);
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, e -> {
			if (MessageDialog.openConfirm(getShell(), Messages.QtPreferencePage_5, Messages.QtPreferencePage_6)) {
				for (TableItem item : installTable.getSelection()) {
					IQtInstall install = (IQtInstall) item.getData();
					installsToRemove.put(install.getName(), install);
					updateTable();
				}
			}
		});

		updateTable();

		return control;
	}

	private Map<String, IQtInstall> getInstalls() {
		Map<String, IQtInstall> installs = new HashMap<>();
		for (IQtInstall install : manager.getInstalls()) {
			installs.put(install.getName(), install);
		}

		for (IQtInstall install : installsToAdd.values()) {
			installs.put(install.getName(), install);
		}

		for (IQtInstall install : installsToRemove.values()) {
			installs.remove(install.getName());
		}

		return installs;
	}

	private void updateTable() {
		List<IQtInstall> sorted = new ArrayList<>(getInstalls().values());
		Collections.sort(sorted, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

		installTable.removeAll();
		for (IQtInstall install : sorted) {
			TableItem item = new TableItem(installTable, SWT.NONE);
			item.setText(0, install.getName());
			item.setText(1, install.getQmakePath().toString());
			item.setData(install);
		}
	}

	@Override
	public boolean performOk() {
		for (IQtInstall install : installsToAdd.values()) {
			manager.addInstall(install);
		}

		for (IQtInstall install : installsToRemove.values()) {
			manager.removeInstall(install);
		}

		return true;
	}

}
