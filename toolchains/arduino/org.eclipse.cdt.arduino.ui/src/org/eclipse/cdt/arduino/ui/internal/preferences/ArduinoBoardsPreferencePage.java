/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoardManager;
import org.eclipse.cdt.arduino.core.internal.board.Board;
import org.eclipse.cdt.arduino.core.internal.board.BoardPackage;
import org.eclipse.cdt.arduino.core.internal.board.PackageIndex;
import org.eclipse.cdt.arduino.core.internal.board.Platform;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ArduinoBoardsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Table table;
	private Button installButton;
	private Set<Board> toInstall = new HashSet<>();

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getCorePreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Composite tableComp = new Composite(comp, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		table = new Table(tableComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn packageColumn = new TableColumn(table, SWT.LEAD);
		packageColumn.setText("Board");

		TableColumn platformColumn = new TableColumn(table, SWT.LEAD);
		platformColumn.setText("Platform");

		TableColumn installedColumn = new TableColumn(table, SWT.LEAD);
		installedColumn.setText("Installed");

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(packageColumn, new ColumnWeightData(5, 150, true));
		tableLayout.setColumnData(platformColumn, new ColumnWeightData(5, 150, true));
		tableLayout.setColumnData(installedColumn, new ColumnWeightData(2, 75, true));
		tableComp.setLayout(tableLayout);

		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateButtons();
			}
		});

		Composite buttonComp = new Composite(comp, SWT.NONE);
		buttonComp.setLayout(new GridLayout());
		buttonComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		installButton = new Button(buttonComp, SWT.PUSH);
		installButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		installButton.setText("Install");
		installButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				for (TableItem item : table.getSelection()) {
					Board board = (Board) item.getData();
					toInstall.add(board);
					item.setText(2, "selected");
					updateButtons();
				}
			}
		});

		updateTable();
		updateButtons();

		return comp;
	}

	private void updateTable() {
		if (table == null || table.isDisposed()) {
			return;
		}

		table.removeAll();

		try {
			PackageIndex packageIndex = ArduinoBoardManager.instance.getPackageIndex();
			List<Board> boards = new ArrayList<>();
			for (BoardPackage pkg : packageIndex.getPackages()) {
				for (Platform platform : pkg.getLatestPlatforms()) {
					try {
						for (Board board : platform.getBoards()) {
							boards.add(board);
						}
					} catch (CoreException e) {
						Activator.log(e);
					}
				}
			}

			Collections.sort(boards, new Comparator<Board>() {
				public int compare(Board o1, Board o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			for (Board board : boards) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setData(board);
				item.setText(0, board.getName());
				item.setText(1, board.getPlatform().getName());
				String msg;
				if (toInstall.contains(board)) {
					msg = "selected";
				} else {
					msg = board.getPlatform().isInstalled() ? "yes" : "no";
				}
				item.setText(2, msg);
			}
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	private void updateButtons() {
		if (table == null || table.isDisposed()) {
			return;
		}

		boolean enable = false;
		for (TableItem item : table.getSelection()) {
			Board board = (Board) item.getData();
			if (toInstall.contains(board)) {
				continue;
			}
			Platform platform = board.getPlatform();
			if (!platform.isInstalled()) {
				enable = true;
			}
		}
		installButton.setEnabled(enable);
	}

	@Override
	public boolean performOk() {
		new Job("Installing Arduino Board Platforms") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Set<Platform> platforms = new HashSet<>();
				for (Board board : toInstall) {
					platforms.add(board.getPlatform());
				}

				for (Platform platform : platforms) {
					try {
						platform.install(monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
				}

				toInstall.clear();

				if (table != null && !table.isDisposed()) {
					table.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							updateTable();
						}
					});
				}

				return Status.OK_STATUS;
			}
		}.schedule();
		return true;
	}

}
