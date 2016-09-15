/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.build.IToolChain;
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

public class CMakePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private ICMakeToolChainManager manager;
	private Table filesTable;
	private Button removeButton;

	private Map<Path, ICMakeToolChainFile> filesToAdd = new HashMap<>();
	private Map<Path, ICMakeToolChainFile> filesToRemove = new HashMap<>();

	@Override
	public void init(IWorkbench workbench) {
		manager = Activator.getService(ICMakeToolChainManager.class);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group filesGroup = new Group(control, SWT.NONE);
		filesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesGroup.setText("ToolChain Files");
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
		pathColumn.setText("ToolChain File");

		TableColumn osColumn = new TableColumn(filesTable, SWT.NONE);
		osColumn.setText("OS");

		TableColumn archColumn = new TableColumn(filesTable, SWT.NONE);
		archColumn.setText("CPU");

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(pathColumn, new ColumnWeightData(75, 350, true));
		tableLayout.setColumnData(osColumn, new ColumnWeightData(25, 100, true));
		tableLayout.setColumnData(archColumn, new ColumnWeightData(25, 100, true));
		filesComp.setLayout(tableLayout);

		Composite buttonsComp = new Composite(filesGroup, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonsComp.setLayout(new GridLayout());

		Button addButton = new Button(buttonsComp, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addButton.setText("Add...");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewCMakeToolChainFileWizard wizard = new NewCMakeToolChainFileWizard(getFiles());
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					ICMakeToolChainFile file = wizard.getNewFile();
					if (filesToRemove.containsKey(file.getPath())) {
						filesToRemove.remove(file.getPath());
					} else {
						filesToAdd.put(file.getPath(), file);
					}
					updateTable();
				}
			}
		});

		removeButton = new Button(buttonsComp, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		removeButton.setText("Remove");
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, e -> {
			if (MessageDialog.openConfirm(getShell(), "Deregister CMake ToolChain File",
					"Do you wish to deregister the selected files?")) {
				for (TableItem item : filesTable.getSelection()) {
					ICMakeToolChainFile file = (ICMakeToolChainFile) item.getData();
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
		List<ICMakeToolChainFile> sorted = new ArrayList<>(getFiles().values());
		Collections.sort(sorted, (o1, o2) -> o1.getPath().toString().compareToIgnoreCase(o2.getPath().toString()));

		filesTable.removeAll();
		for (ICMakeToolChainFile file : sorted) {
			TableItem item = new TableItem(filesTable, SWT.NONE);
			item.setText(0, file.getPath().toString());
			String os = file.getProperty(IToolChain.ATTR_OS);
			if (os != null) {
				item.setText(1, os);
			}
			String arch = file.getProperty(IToolChain.ATTR_ARCH);
			if (arch != null) {
				item.setText(2, arch);
			}
			item.setData(file);
		}
	}

	private Map<Path, ICMakeToolChainFile> getFiles() {
		Map<Path, ICMakeToolChainFile> files = new HashMap<>();
		for (ICMakeToolChainFile file : manager.getToolChainFiles()) {
			files.put(file.getPath(), file);
		}

		for (ICMakeToolChainFile file : filesToAdd.values()) {
			files.put(file.getPath(), file);
		}

		for (ICMakeToolChainFile file : filesToRemove.values()) {
			files.remove(file.getPath());
		}

		return files;
	}

	@Override
	public boolean performOk() {
		for (ICMakeToolChainFile file : filesToAdd.values()) {
			manager.addToolChainFile(file);
		}

		for (ICMakeToolChainFile file : filesToRemove.values()) {
			manager.removeToolChainFile(file);
		}

		filesToAdd.clear();
		filesToRemove.clear();
		
		return true;
	}

}
