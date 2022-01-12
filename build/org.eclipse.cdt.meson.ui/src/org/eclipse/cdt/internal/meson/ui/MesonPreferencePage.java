/*******************************************************************************
 * Copyright (c) 2016, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - modified for use in Meson build
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.meson.core.Activator;
import org.eclipse.cdt.meson.core.IMesonToolChainFile;
import org.eclipse.cdt.meson.core.IMesonToolChainManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
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

public class MesonPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IMesonToolChainManager manager;
	private Table filesTable;
	private Button removeButton;

	private Map<Path, IMesonToolChainFile> filesToAdd = new HashMap<>();
	private Map<Path, IMesonToolChainFile> filesToRemove = new HashMap<>();

	@Override
	public void init(IWorkbench workbench) {
		manager = Activator.getService(IMesonToolChainManager.class);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group filesGroup = new Group(control, SWT.NONE);
		filesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesGroup.setText(Messages.MesonPreferencePage_Files);
		filesGroup.setLayout(new GridLayout(2, false));

		Composite filesComp = new Composite(filesGroup, SWT.NONE);
		filesComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		filesTable = new Table(filesComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		filesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesTable.setHeaderVisible(true);
		filesTable.setLinesVisible(true);
		filesTable.addListener(SWT.Selection, e -> {
			TableItem[] items = filesTable.getSelection();
			removeButton.setEnabled(items.length > 0);
		});

		TableColumn pathColumn = new TableColumn(filesTable, SWT.NONE);
		pathColumn.setText(Messages.MesonPreferencePage_Path);

		TableColumn tcColumn = new TableColumn(filesTable, SWT.NONE);
		tcColumn.setText(Messages.MesonPreferencePage_Toolchain);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(pathColumn, new ColumnWeightData(50, 350, true));
		tableLayout.setColumnData(tcColumn, new ColumnWeightData(50, 350, true));
		filesComp.setLayout(tableLayout);

		Composite buttonsComp = new Composite(filesGroup, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonsComp.setLayout(new GridLayout());

		Button addButton = new Button(buttonsComp, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addButton.setText(Messages.MesonPreferencePage_Add);
		addButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			NewMesonToolChainFileWizard wizard = new NewMesonToolChainFileWizard();
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			if (dialog.open() == Window.OK) {
				IMesonToolChainFile file = wizard.getNewFile();
				IMesonToolChainFile oldFile = manager.getToolChainFile(file.getPath());
				if (oldFile != null) {
					filesToRemove.put(oldFile.getPath(), oldFile);
				}
				filesToAdd.put(file.getPath(), file);
				updateTable();
			}
		}));

		removeButton = new Button(buttonsComp, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		removeButton.setText(Messages.MesonPreferencePage_Remove);
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, e -> {
			if (MessageDialog.openConfirm(getShell(), Messages.MesonPreferencePage_ConfirmRemoveTitle,
					Messages.MesonPreferencePage_ConfirmRemoveDesc)) {
				for (TableItem item : filesTable.getSelection()) {
					IMesonToolChainFile file = (IMesonToolChainFile) item.getData();
					if (filesToAdd.containsKey(file.getPath())) {
						filesToAdd.remove(file.getPath());
					} else {
						filesToRemove.put(file.getPath(), file);
					}
					updateTable();
				}
			}
		});

		updateTable();

		return control;
	}

	private void updateTable() {
		List<IMesonToolChainFile> sorted = new ArrayList<>(getFiles().values());
		Collections.sort(sorted, (o1, o2) -> o1.getPath().toString().compareToIgnoreCase(o2.getPath().toString()));

		filesTable.removeAll();
		for (IMesonToolChainFile file : sorted) {
			TableItem item = new TableItem(filesTable, SWT.NONE);
			item.setText(0, file.getPath().toString());

			try {
				IToolChain tc = file.getToolChain();
				if (tc != null) {
					item.setText(1, tc.getName());
				}
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}

			item.setData(file);
		}
	}

	private Map<Path, IMesonToolChainFile> getFiles() {
		Map<Path, IMesonToolChainFile> files = new HashMap<>();
		for (IMesonToolChainFile file : manager.getToolChainFiles()) {
			files.put(file.getPath(), file);
		}

		for (IMesonToolChainFile file : filesToRemove.values()) {
			files.remove(file.getPath());
		}

		for (IMesonToolChainFile file : filesToAdd.values()) {
			files.put(file.getPath(), file);
		}

		return files;
	}

	@Override
	public boolean performOk() {
		for (IMesonToolChainFile file : filesToRemove.values()) {
			manager.removeToolChainFile(file);
		}

		for (IMesonToolChainFile file : filesToAdd.values()) {
			manager.addToolChainFile(file);
		}

		filesToAdd.clear();
		filesToRemove.clear();

		return true;
	}

}
