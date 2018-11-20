/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.downloads;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.FormTextHoverManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class PlatformsTabControl extends Composite {

	private ArduinoManager manager = Activator.getService(ArduinoManager.class);
	private Table table;
	private IWizardContainer container;
	private Collection<ArduinoPlatform> availablePlatforms;

	public PlatformsTabControl(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());

		Text desc = new Text(this, SWT.READ_ONLY | SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.widthHint = 500;
		desc.setLayoutData(layoutData);
		desc.setBackground(parent.getBackground());
		desc.setText("Installed Platforms. Details available in their tooltips");

		Composite comp = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite tableComp = new Composite(comp, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		table = new Table(tableComp, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumnLayout tableLayout = new TableColumnLayout();

		TableColumn packageColumn = new TableColumn(table, SWT.LEAD);
		packageColumn.setText("Package");
		tableLayout.setColumnData(packageColumn, new ColumnWeightData(2, 75, true));

		TableColumn platformColumn = new TableColumn(table, SWT.LEAD);
		platformColumn.setText("Platform");
		tableLayout.setColumnData(platformColumn, new ColumnWeightData(5, 150, true));

		TableColumn versionColumn = new TableColumn(table, SWT.LEAD);
		versionColumn.setText("Version");
		tableLayout.setColumnData(versionColumn, new ColumnWeightData(2, 75, true));

		tableComp.setLayout(tableLayout);

		Composite buttonComp = new Composite(comp, SWT.NONE);
		buttonComp.setLayout(new GridLayout());
		buttonComp.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		final Button uninstallButton = new Button(buttonComp, SWT.PUSH);
		uninstallButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		uninstallButton.setText("Uninstall");
		uninstallButton.setEnabled(false);
		uninstallButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uninstall();
			}
		});

		Button updatesButton = new Button(buttonComp, SWT.PUSH);
		updatesButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		updatesButton.setText("Updates");
		updatesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkForUpdates();
			}
		});

		Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addPlatforms();
			}
		});

		populateTable();

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selection = table.getSelection();
				uninstallButton.setEnabled(selection.length > 0);
			}
		});

		FormTextHoverManager hoverManager = new FormTextHoverManager() {
			@Override
			protected void computeInformation() {
				TableItem item = table.getItem(getHoverEventLocation());
				if (item != null) {
					ArduinoPlatform platform = (ArduinoPlatform) item.getData();
					setInformation(platform.toFormText(), item.getBounds());
				} else {
					setInformation(null, null);
				}
			}
		};
		hoverManager.install(table);
	}

	@Override
	public boolean setFocus() {
		return table.setFocus();
	}

	public void setContainer(IWizardContainer container) {
		this.container = container;
	}

	private void populateTable() {
		table.removeAll();
		try {
			List<ArduinoPlatform> platforms = new ArrayList<>(manager.getInstalledPlatforms());
			Collections.sort(platforms, new Comparator<ArduinoPlatform>() {
				@Override
				public int compare(ArduinoPlatform o1, ArduinoPlatform o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			for (ArduinoPlatform platform : platforms) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setData(platform);
				item.setText(0, platform.getPackage().getName());
				item.setText(1, platform.getName());
				item.setText(2, platform.getVersion());
			}

		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private void uninstall() {
		List<ArduinoPlatform> selectedPlatforms = new ArrayList<>(table.getSelectionCount());
		for (TableItem item : table.getSelection()) {
			selectedPlatforms.add((ArduinoPlatform) item.getData());
		}
		try {
			container.run(true, true, monitor -> manager.uninstallPlatforms(selectedPlatforms, monitor));
		} catch (InterruptedException | InvocationTargetException e) {
			Activator.log(e);
			return;
		}
		populateTable();
	}

	private void checkForUpdates() {
		Collection<ArduinoPlatform> updates = new ArrayList<>();
		try {
			container.run(true, true, monitor -> {
				try {
					for (ArduinoPlatform available : manager.getPlatformUpdates(monitor)) {
						ArduinoPlatform installed = manager.getInstalledPlatform(available.getPackage().getName(),
								available.getArchitecture());
						if (installed != null) {
							if (ArduinoManager.compareVersions(available.getVersion(), installed.getVersion()) > 0) {
								updates.add(available);
							}
						}
					}
				} catch (CoreException e) {
					getDisplay().syncExec(() -> ErrorDialog.openError(getShell(), null, null, e.getStatus()));
					Activator.log(e);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
			return;
		}

		if (updates.isEmpty()) {
			MessageDialog.openInformation(getShell(), "Platform Updates", "All platforms are up to date");
			return;
		} else {
			UpdatePlatformsDialog updateDialog = new UpdatePlatformsDialog(getShell(), updates);
			if (updateDialog.open() == Window.OK) {
				Collection<ArduinoPlatform> toUpdate = updateDialog.getSelectedPlatforms();
				if (!toUpdate.isEmpty()) {
					try {
						container.run(true, true, monitor -> {
							try {
								manager.installPlatforms(toUpdate, monitor);
							} catch (CoreException e) {
								getDisplay()
										.syncExec(() -> ErrorDialog.openError(getShell(), null, null, e.getStatus()));
								Activator.log(e);
							}
						});
					} catch (InvocationTargetException | InterruptedException e) {
						Activator.log(e);
					}
					populateTable();
				}
			}
		}
	}

	private void addPlatforms() {
		try {
			container.run(true, true, monitor -> {
				try {
					availablePlatforms = manager.getAvailablePlatforms(monitor);
				} catch (CoreException e) {
					getDisplay().syncExec(() -> ErrorDialog.openError(getShell(), null, null, e.getStatus()));
					Activator.log(e);
				}
			});

			SelectPlatformsDialog selectDialog = new SelectPlatformsDialog(getShell());
			selectDialog.setPlatforms(availablePlatforms);
			if (selectDialog.open() == Window.OK) {
				if (ArduinoDownloadsManager.checkLicense(getShell())) {
					Collection<ArduinoPlatform> selectedPlatforms = selectDialog.getSelectedPlatforms();
					container.run(true, true, monitor -> {
						try {
							manager.installPlatforms(selectedPlatforms, monitor);
						} catch (CoreException e) {
							Activator.log(e);
						}
					});
				}
			}
			populateTable();
		} catch (InterruptedException | InvocationTargetException e) {
			Activator.log(e);
			return;
		}
	}

}
