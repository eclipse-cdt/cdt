/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.ui.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tools.templates.ui.internal.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IUserToolChainProvider;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.build.NewToolChainWizard;

/**
 * Preference page to manage Toolchains for Core Build.
 * 
 * @since 6.3
 */
public class ToolChainPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private TableViewer availTable;
	private Button availUp;
	private Button availDown;

	private TableViewer userTable;
	private Button userEdit;
	private Button userRemove;

	private IToolChainManager manager = CUIPlugin.getService(IToolChainManager.class);

	private ISafeRunnable tcListener = () -> Display.getDefault().asyncExec(() -> {
		availTable.refresh();
		userTable.refresh();
	});

	public ToolChainPreferencePage() {
		super("Toolchains");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			IToolChain toolChain = (IToolChain) element;
			switch (columnIndex) {
			case 0:
				return toolChain.getName();
			case 1:
				return toolChain.getProperty(IToolChain.ATTR_OS);
			case 2:
				return toolChain.getProperty(IToolChain.ATTR_ARCH);
			}
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		initializeDialogUnits(parent);

		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group availGroup = new Group(control, SWT.NONE);
		availGroup.setText("Available Toolchains");
		availGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		availGroup.setLayout(new GridLayout(2, false));

		availTable = createToolChainTable(availGroup);
		availTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		availTable.setLabelProvider(new TableLabelProvider());
		availTable.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				try {
					return manager.getAllToolChains().toArray();
				} catch (CoreException e) {
					Activator.log(e.getStatus());
					return new Object[0];
				}
			}
		});
		

		Composite availButtonComp = new Composite(availGroup, SWT.NONE);
		availButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		availButtonComp.setLayout(new GridLayout());

		availUp = new Button(availButtonComp, SWT.PUSH);
		availUp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		availUp.setText("Up");

		availDown = new Button(availButtonComp, SWT.PUSH);
		availDown.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		availDown.setText("Down");

		Group userGroup = new Group(control, SWT.NONE);
		userGroup.setText("User Defined Toolchains");
		userGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		userGroup.setLayout(new GridLayout(2, false));

		userTable = createToolChainTable(userGroup);
		userTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		userTable.setLabelProvider(new TableLabelProvider());
		userTable.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				List<IToolChain> tcs = new ArrayList<>();
				try {
					for (IToolChain tc : manager.getAllToolChains()) {
						if (tc.getProvider() instanceof IUserToolChainProvider) {
							tcs.add(tc);
						}
					}
				} catch (CoreException e) {
					Activator.log(e);
				}
				return tcs.toArray();
			}
		});

		Composite userButtonComp = new Composite(userGroup, SWT.NONE);
		userButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		userButtonComp.setLayout(new GridLayout());

		Button userAdd = new Button(userButtonComp, SWT.PUSH);
		userAdd.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		userAdd.setText("Add...");
		userAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Wizard wizard = new NewToolChainWizard();
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.open();
			}
		});

		userEdit = new Button(userButtonComp, SWT.PUSH);
		userEdit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		userEdit.setText("Edit...");
		userEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IToolChain tc = (IToolChain) userTable.getStructuredSelection().getFirstElement();
				String providerId = tc.getProvider().getId();

				ToolChainWizard wizard = null;
				IExtensionPoint point = Platform.getExtensionRegistry()
						.getExtensionPoint(CUIPlugin.PLUGIN_ID + ".newToolChainWizards"); //$NON-NLS-1$
				loop: for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement element : extension.getConfigurationElements()) {
						if (providerId.equals(element.getAttribute("providerId"))) { //$NON-NLS-1$
							try {
								wizard = (ToolChainWizard) element.createExecutableExtension("class"); //$NON-NLS-1$
								break loop;
							} catch (CoreException e1) {
								CUIPlugin.log(e1.getStatus());
							}
						}
					}
				}

				if (wizard != null) {
					wizard.setToolChain(tc);
					WizardDialog dialog = new WizardDialog(getShell(), wizard);
					dialog.open();
				} else {
					MessageDialog.openInformation(getShell(), "Edit", "No editor found for this toolchain");
				}
			}
		});

		userRemove = new Button(userButtonComp, SWT.PUSH);
		userRemove.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		userRemove.setText("Remove");
		userRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (MessageDialog.openConfirm(getShell(), "Remove",
						"Are you sure you'd like to remove the selected toolchain?")) {
					IToolChain tc = (IToolChain) userTable.getStructuredSelection().getFirstElement();
					IUserToolChainProvider provider = (IUserToolChainProvider) tc.getProvider();
					new Job("Remove ToolChain") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								provider.removeToolChain(tc);
								return Status.OK_STATUS;
							} catch (CoreException e) {
								return e.getStatus();
							}
						}
					}.schedule();
				}
			}
		});

		availTable.setInput(manager);
		userTable.setInput(manager);
		updateButtons();
		manager.addToolChainListener(tcListener);
		return control;
	}

	@Override
	public void dispose() {
		manager.removeToolChainListener(tcListener);
		super.dispose();
	}

	private TableViewer createToolChainTable(Composite parent) {
		Composite tableComp = new Composite(parent, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Table table = new Table(tableComp, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumnLayout tableLayout = new TableColumnLayout();

		TableColumn tableNameColumn = new TableColumn(table, SWT.LEAD);
		tableNameColumn.setText("Name");
		tableLayout.setColumnData(tableNameColumn, new ColumnWeightData(6));

		TableColumn tableOSColumn = new TableColumn(table, SWT.LEAD);
		tableOSColumn.setText("OS");
		tableLayout.setColumnData(tableOSColumn, new ColumnWeightData(2));

		TableColumn tableArchColumn = new TableColumn(table, SWT.LEAD);
		tableArchColumn.setText("Arch");
		tableLayout.setColumnData(tableArchColumn, new ColumnWeightData(2));

		tableComp.setLayout(tableLayout);
		
		return new TableViewer(table);
	}

	private void updateButtons() {
		boolean availSelected = availTable.getTable().getSelectionCount() > 0;
		availUp.setEnabled(availSelected);
		availDown.setEnabled(availSelected);

		boolean userSelected = userTable.getTable().getSelectionCount() > 0;
		userEdit.setEnabled(userSelected);
		userRemove.setEnabled(userSelected);
	}

}
