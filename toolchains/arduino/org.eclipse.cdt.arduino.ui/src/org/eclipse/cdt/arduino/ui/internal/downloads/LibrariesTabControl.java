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

import org.eclipse.cdt.arduino.core.internal.board.ArduinoLibrary;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
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

public class LibrariesTabControl extends Composite {

	private ArduinoManager manager = Activator.getService(ArduinoManager.class);
	private Table table;
	private IWizardContainer container;
	private Collection<ArduinoLibrary> availableLibraries;

	public LibrariesTabControl(Composite parent, int style) {
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
		packageColumn.setText("Library");
		tableLayout.setColumnData(packageColumn, new ColumnWeightData(5, 150, true));

		TableColumn platformColumn = new TableColumn(table, SWT.LEAD);
		platformColumn.setText("Version");
		tableLayout.setColumnData(platformColumn, new ColumnWeightData(2, 75, true));

		TableColumn versionColumn = new TableColumn(table, SWT.LEAD);
		versionColumn.setText("Description");
		tableLayout.setColumnData(versionColumn, new ColumnWeightData(5, 150, true));

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
				addLibraries();
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
					ArduinoLibrary library = (ArduinoLibrary) item.getData();
					setInformation(library.toFormText(), item.getBounds());
				} else {
					setInformation(null, null);
				}
			}
		};
		hoverManager.install(table);

	}

	public void setContainer(IWizardContainer container) {
		this.container = container;
	}

	private void populateTable() {
		table.removeAll();
		try {
			List<ArduinoLibrary> libraries = new ArrayList<>(manager.getInstalledLibraries());
			Collections.sort(libraries, new Comparator<ArduinoLibrary>() {
				@Override
				public int compare(ArduinoLibrary o1, ArduinoLibrary o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			for (ArduinoLibrary library : libraries) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setData(library);
				item.setText(0, library.getName());
				item.setText(1, library.getVersion());
				item.setText(2, library.getSentence());
			}

		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private void uninstall() {
		List<ArduinoLibrary> selectedLibraries = new ArrayList<>(table.getSelectionCount());
		for (TableItem item : table.getSelection()) {
			selectedLibraries.add((ArduinoLibrary) item.getData());
		}
		try {
			container.run(true, true, monitor -> {
				try {
					manager.uninstallLibraries(selectedLibraries, monitor);
				} catch (CoreException e) {
					Activator.log(e);
				}
			});
		} catch (InterruptedException | InvocationTargetException e) {
			Activator.log(e);
			return;
		}
		populateTable();
	}

	private void checkForUpdates() {
		Collection<ArduinoLibrary> updates = new ArrayList<>();
		try {
			container.run(true, true, monitor -> {
				try {
					for (ArduinoLibrary available : manager.getLibraryUpdates(monitor)) {
						ArduinoLibrary installed = manager.getInstalledLibrary(available.getName());
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

			if (updates.isEmpty()) {
				MessageDialog.openInformation(getShell(), "Library Updates", "All libraries are up to date");
				return;
			}
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
			return;
		}

		if (updates.isEmpty()) {
			MessageDialog.openInformation(getShell(), "Platform Updates", "All platforms are up to date");
			return;
		} else {
			UpdateLibrariesDialog updateDialog = new UpdateLibrariesDialog(getShell(), updates);
			if (updateDialog.open() == Window.OK) {
				Collection<ArduinoLibrary> toUpdate = updateDialog.getSelectedLibraries();
				if (!toUpdate.isEmpty()) {
					try {
						container.run(true, true, monitor -> {
							try {
								manager.installLibraries(toUpdate, monitor);
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

	private void addLibraries() {
		try {
			container.run(true, true, monitor -> {
				try {
					availableLibraries = manager.getAvailableLibraries(monitor);
				} catch (CoreException e) {
					getDisplay().syncExec(() -> ErrorDialog.openError(getShell(), null, null, e.getStatus()));
					Activator.log(e);
				}
			});

			SelectLibrariesDialog selectDialog = new SelectLibrariesDialog(getShell());
			selectDialog.setLibraries(availableLibraries);
			if (selectDialog.open() == Window.OK) {
				if (ArduinoDownloadsManager.checkLicense(getShell())) {
					Collection<ArduinoLibrary> selectedLibraries = selectDialog.getChecked();
					container.run(true, true, monitor -> {
						try {
							manager.installLibraries(selectedLibraries, monitor);
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
