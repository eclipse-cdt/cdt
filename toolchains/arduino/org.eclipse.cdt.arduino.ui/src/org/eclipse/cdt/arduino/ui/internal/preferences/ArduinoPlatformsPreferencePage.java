/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.preferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.core.internal.board.PackageIndex;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ArduinoPlatformsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Table table;
	private Button installButton;
	private Button uninstallButton;
	private Button detailButton;

	private Collection<ArduinoPlatform> toInstall = new HashSet<>();
	private Collection<ArduinoPlatform> toUninstall = new HashSet<>();

	private static ArduinoManager manager = Activator.getService(ArduinoManager.class);

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Text desc = new Text(control, SWT.READ_ONLY | SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 500;
		desc.setLayoutData(layoutData);
		desc.setBackground(parent.getBackground());
		desc.setText(Messages.ArduinoPlatformsPreferencePage_0);

		Composite comp = new Composite(control, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite tableComp = new Composite(comp, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		table = new Table(tableComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn platformColumn = new TableColumn(table, SWT.LEAD);
		platformColumn.setText(Messages.ArduinoPlatformsPreferencePage_1);
		TableColumn installedColumn = new TableColumn(table, SWT.LEAD);
		installedColumn.setText(Messages.ArduinoPlatformsPreferencePage_2);
		TableColumn availableColumn = new TableColumn(table, SWT.LEAD);
		availableColumn.setText(Messages.ArduinoPlatformsPreferencePage_3);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(platformColumn, new ColumnWeightData(5, 150, true));
		tableLayout.setColumnData(installedColumn, new ColumnWeightData(2, 75, true));
		tableLayout.setColumnData(availableColumn, new ColumnWeightData(2, 75, true));
		tableComp.setLayout(tableLayout);

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = table.getSelection();
				if (selection.length > 0) {
					TableItem item = selection[0];
					detailButton.setEnabled(true);
					ArduinoPlatform aplat = (ArduinoPlatform) item.getData();
					ArduinoPlatform iplat = aplat.getPackage().getInstalledPlatforms().get(aplat.getName());
					if (iplat == null) {
						installButton.setEnabled(true);
						installButton.setText(Messages.ArduinoPlatformsPreferencePage_4);
						uninstallButton.setEnabled(false);
					} else {
						installButton.setText(Messages.ArduinoPlatformsPreferencePage_5);
						if (!aplat.getVersion().equals(iplat.getVersion())) {
							// Assuming upgrade if not equal, dangerous
							installButton.setEnabled(true);
						} else {
							installButton.setEnabled(false);
						}
						uninstallButton.setEnabled(true);
					}
				} else {
					detailButton.setEnabled(false);
					installButton.setEnabled(false);
					uninstallButton.setEnabled(false);
				}
			}
		});

		Composite buttonComp = new Composite(comp, SWT.NONE);
		buttonComp.setLayout(new GridLayout());
		buttonComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		detailButton = new Button(buttonComp, SWT.PUSH);
		detailButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		detailButton.setText(Messages.ArduinoPlatformsPreferencePage_6);
		detailButton.setEnabled(false);
		detailButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = table.getSelection();
				// We are only enabled when there is a selection
				ArduinoPlatform platform = (ArduinoPlatform) selection[0].getData();
				PlatformDetailsDialog dialog = new PlatformDetailsDialog(getShell(), platform);
				dialog.open();
			}
		});

		installButton = new Button(buttonComp, SWT.PUSH);
		installButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		installButton.setText(Messages.ArduinoPlatformsPreferencePage_7);
		installButton.setEnabled(false);
		installButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = table.getSelection();
				if (selection.length > 0) {
					TableItem item = selection[0];
					toInstall.add(((ArduinoPlatform) item.getData()));
					item.setImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_ADD));
				}
			}
		});

		uninstallButton = new Button(buttonComp, SWT.PUSH);
		uninstallButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		uninstallButton.setText(Messages.ArduinoPlatformsPreferencePage_8);
		uninstallButton.setEnabled(false);
		uninstallButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = table.getSelection();
				if (selection.length > 0) {
					TableItem item = selection[0];
					toUninstall.add(((ArduinoPlatform) item.getData()));
					item.setImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_DELETE));
				}
			}
		});

		populateTable();

		return control;
	}

	private void populateTable() {
		table.removeAll();
		for (PackageIndex packageIndex : manager.getPackageIndices()) {
			for (ArduinoPackage pkg : packageIndex.getPackages()) {
				Map<String, ArduinoPlatform> available = pkg.getAvailablePlatforms();
				Map<String, ArduinoPlatform> installed = pkg.getInstalledPlatforms();
				List<String> names = new ArrayList<>(available.keySet());
				Collections.sort(names);
				for (String name : names) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(0, name);
					ArduinoPlatform iplat = installed.get(name);
					item.setText(1, iplat != null ? iplat.getVersion() : "---"); //$NON-NLS-1$
					ArduinoPlatform aplat = available.get(name);
					item.setText(2, aplat.getVersion());
					item.setData(aplat);
				}
			}
		}
	}

	@Override
	public boolean performOk() {
		File acceptedFile = ArduinoPreferences.getArduinoHome().resolve(".accepted").toFile(); //$NON-NLS-1$
		if (!acceptedFile.exists()) {
			String message = Messages.ArduinoPlatformsPreferencePage_9 + Messages.ArduinoPlatformsPreferencePage_10;
			MessageDialog dialog = new MessageDialog(getShell(),
					Messages.ArduinoPlatformsPreferencePage_11, null, message, MessageDialog.QUESTION, new String[] {
							Messages.ArduinoPlatformsPreferencePage_12, Messages.ArduinoPlatformsPreferencePage_13 },
					0);
			int rc = dialog.open();
			if (rc == 0) {
				try {
					acceptedFile.createNewFile();
				} catch (IOException e) {
					Activator.log(e);
				}
			} else {
				return false;
			}
		}

		new Job(Messages.ArduinoPlatformsPreferencePage_14) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0, Messages.ArduinoPlatformsPreferencePage_15,
						null);

				for (ArduinoPlatform platform : toUninstall) {
					status.add(platform.uninstall(monitor));
				}
				toUninstall.clear();

				for (ArduinoPlatform platform : toInstall) {
					status.add(platform.install(monitor));
				}
				toInstall.clear();

				if (table != null && !table.isDisposed()) {
					table.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							populateTable();
						}
					});
				}

				return status;
			}
		}.schedule();
		return true;
	}

}
