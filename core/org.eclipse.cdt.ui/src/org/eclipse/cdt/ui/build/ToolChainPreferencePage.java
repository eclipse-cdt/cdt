/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.cdt.core.build.IUserToolChainProvider;
import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.cdt.internal.ui.build.NewToolChainWizard;
import org.eclipse.cdt.ui.CUIPlugin;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tools.templates.ui.internal.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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

	private List<IToolChain> toolChains;

	private static IToolChainManager manager = CUIPlugin.getService(IToolChainManager.class);

	private ISafeRunnable tcListener = () -> Display.getDefault().asyncExec(() -> {
		availTable.refresh();
		userTable.refresh();
	});

	public ToolChainPreferencePage() {
		super(CUIMessages.ToolChainPreferencePage_Toolchains);
	}

	@Override
	public void init(IWorkbench workbench) {
		try {
			toolChains = new ArrayList<>(manager.getAllToolChains());
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			IToolChain toolChain = (IToolChain) element;
			switch (columnIndex) {
			case 0:
				return manager.getToolChainTypeName(toolChain.getTypeId());
			case 1:
				return toolChain.getName();
			case 2:
				return toolChain.getProperty(IToolChain.ATTR_OS);
			case 3:
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

		Label label = new Label(control, SWT.NONE);
		label.setText(CUIMessages.ToolChainPreferencePage_Desc);

		Group availGroup = new Group(control, SWT.NONE);
		availGroup.setText(CUIMessages.ToolChainPreferencePage_AvailableToolchains);
		availGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		availGroup.setLayout(new GridLayout(2, false));

		availTable = createToolChainTable(availGroup);
		availTable.setLabelProvider(new TableLabelProvider());
		availTable.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return toolChains.toArray();
			}
		});
		availTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});

		Composite availButtonComp = new Composite(availGroup, SWT.NONE);
		availButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		availButtonComp.setLayout(new GridLayout());

		availUp = new Button(availButtonComp, SWT.PUSH);
		availUp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		availUp.setText(CUIMessages.ToolChainPreferencePage_Up);
		availUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = availTable.getTable().getSelectionIndex();
				if (i < 1) {
					return;
				}

				IToolChain tc = toolChains.get(i - 1);
				toolChains.set(i - 1, toolChains.get(i));
				toolChains.set(i, tc);
				availTable.refresh();
			}
		});

		availDown = new Button(availButtonComp, SWT.PUSH);
		availDown.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		availDown.setText(CUIMessages.ToolChainPreferencePage_Down);
		availDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = availTable.getTable().getSelectionIndex();
				if (i < 0 || i > toolChains.size() - 2) {
					return;
				}

				IToolChain tc = toolChains.get(i + 1);
				toolChains.set(i + 1, toolChains.get(i));
				toolChains.set(i, tc);
				availTable.refresh();
			}
		});

		Group userGroup = new Group(control, SWT.NONE);
		userGroup.setText(CUIMessages.ToolChainPreferencePage_UserDefinedToolchains);
		userGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		userGroup.setLayout(new GridLayout(2, false));

		userTable = createToolChainTable(userGroup);
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
					CUIPlugin.log(e);
				}
				return tcs.toArray();
			}
		});
		userTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});

		Composite userButtonComp = new Composite(userGroup, SWT.NONE);
		userButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		userButtonComp.setLayout(new GridLayout());

		Button userAdd = new Button(userButtonComp, SWT.PUSH);
		userAdd.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		userAdd.setText(CUIMessages.ToolChainPreferencePage_Add);
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
		userEdit.setText(CUIMessages.ToolChainPreferencePage_EditDot);
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
					MessageDialog.openInformation(getShell(), CUIMessages.ToolChainPreferencePage_Edit,
							CUIMessages.ToolChainPreferencePage_NoEditor);
				}
			}
		});

		userRemove = new Button(userButtonComp, SWT.PUSH);
		userRemove.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		userRemove.setText(CUIMessages.ToolChainPreferencePage_Remove);
		userRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (MessageDialog.openConfirm(getShell(), CUIMessages.ToolChainPreferencePage_Remove1,
						CUIMessages.ToolChainPreferencePage_AreYouSure)) {
					IToolChain tc = (IToolChain) userTable.getStructuredSelection().getFirstElement();
					IUserToolChainProvider provider = (IUserToolChainProvider) tc.getProvider();
					new Job(CUIMessages.ToolChainPreferencePage_RemoveToolchain) {
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

	@Override
	public boolean performOk() {
		if (!super.performOk()) {
			return false;
		}

		try {
			if (!toolChains.equals(manager.getAllToolChains())) {
				manager.setToolChainOrder(toolChains);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e.getStatus());
			return false;
		}

		return true;
	}

	private TableViewer createToolChainTable(Composite parent) {
		Composite tableComp = new Composite(parent, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Table table = new Table(tableComp, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumnLayout tableLayout = new TableColumnLayout();

		TableColumn tableTypeColumn = new TableColumn(table, SWT.LEAD);
		tableTypeColumn.setText(CUIMessages.ToolChainPreferencePage_Type);
		tableLayout.setColumnData(tableTypeColumn, new ColumnWeightData(2));

		TableColumn tableNameColumn = new TableColumn(table, SWT.LEAD);
		tableNameColumn.setText(CUIMessages.ToolChainPreferencePage_Name);
		tableLayout.setColumnData(tableNameColumn, new ColumnWeightData(10));

		TableColumn tableOSColumn = new TableColumn(table, SWT.LEAD);
		tableOSColumn.setText(CUIMessages.ToolChainPreferencePage_OS);
		tableLayout.setColumnData(tableOSColumn, new ColumnWeightData(2));

		TableColumn tableArchColumn = new TableColumn(table, SWT.LEAD);
		tableArchColumn.setText(CUIMessages.ToolChainPreferencePage_Arch);
		tableLayout.setColumnData(tableArchColumn, new ColumnWeightData(2));

		tableComp.setLayout(tableLayout);

		return new TableViewer(table);
	}

	private void updateButtons() {
		int i = availTable.getTable().getSelectionIndex();
		availUp.setEnabled(i > 0);
		availDown.setEnabled(i >= 0 && i < toolChains.size() - 2);

		boolean userSelected = userTable.getTable().getSelectionCount() > 0;
		userEdit.setEnabled(userSelected);
		userRemove.setEnabled(userSelected);
	}

}
