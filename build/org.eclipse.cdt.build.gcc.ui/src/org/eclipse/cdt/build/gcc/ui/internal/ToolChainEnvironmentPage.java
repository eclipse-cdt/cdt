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
package org.eclipse.cdt.build.gcc.ui.internal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ToolChainEnvironmentPage extends WizardPage {

	private TableViewer tableViewer;
	private Button editButton;
	private Button removeButton;

	private List<IEnvironmentVariable> envvars;

	public ToolChainEnvironmentPage(IToolChain toolChain) {
		super(ToolChainEnvironmentPage.class.getName());
		setTitle(Messages.ToolChainEnvironmentPage_Title);
		setDescription(Messages.ToolChainEnvironmentPage_Description);

		if (toolChain != null && toolChain.getVariables() != null) {
			this.envvars = new LinkedList<>(Arrays.asList(toolChain.getVariables()));
		} else {
			this.envvars = new LinkedList<>();
		}
	}

	private static abstract class TableLabelProvider extends BaseLabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Table table = new Table(comp, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.ToolChainEnvironmentPage_Name);
		column.setWidth(150);

		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.ToolChainEnvironmentPage_Value);
		column.setWidth(150);

		column = new TableColumn(table, SWT.LEFT);
		column.setText(Messages.ToolChainEnvironmentPage_Operation);
		column.setWidth(75);

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return envvars.toArray();
			}
		});
		tableViewer.setLabelProvider(new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				IEnvironmentVariable var = (IEnvironmentVariable) element;
				switch (columnIndex) {
				case 0:
					return var.getName();
				case 1:
					return var.getValue();
				case 2:
					switch (var.getOperation()) {
					case IEnvironmentVariable.ENVVAR_REPLACE:
						return Messages.ToolChainEnvironmentPage_Replace;
					case IEnvironmentVariable.ENVVAR_PREPEND:
						return Messages.ToolChainEnvironmentPage_Prepend;
					case IEnvironmentVariable.ENVVAR_APPEND:
						return Messages.ToolChainEnvironmentPage_Append;
					case IEnvironmentVariable.ENVVAR_REMOVE:
						return Messages.ToolChainEnvironmentPage_Unset;
					}
				}
				return null;
			}
		});

		Composite buttonComp = new Composite(comp, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonComp.setLayout(new GridLayout());

		Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText(Messages.ToolChainEnvironmentPage_Add);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewEnvVarDialog dialog = new NewEnvVarDialog(getShell());
				if (dialog.open() == Window.OK) {
					envvars.add(dialog.getEnvVar());
					tableViewer.refresh();
				}
			}
		});

		editButton = new Button(buttonComp, SWT.PUSH);
		editButton.setText(Messages.ToolChainEnvironmentPage_Edit);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEdit();
			}
		});

		removeButton = new Button(buttonComp, SWT.PUSH);
		removeButton.setText(Messages.ToolChainEnvironmentPage_Remove);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (MessageDialog.openConfirm(getShell(), Messages.ToolChainEnvironmentPage_RemoveTitle,
						Messages.ToolChainEnvironmentPage_RemoveMessage)) {
					@SuppressWarnings("rawtypes")
					Iterator i = tableViewer.getStructuredSelection().iterator();
					while (i.hasNext()) {
						IEnvironmentVariable var = (IEnvironmentVariable) i.next();
						envvars.remove(var);
					}
					tableViewer.refresh();
				}
			}
		});

		table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateButtons();
				if (table.getSelectionCount() == 1) {
					handleEdit();
				}
			}
		});

		tableViewer.setInput(envvars);
		setControl(comp);
	}

	private void updateButtons() {
		int n = tableViewer.getTable().getSelectionCount();
		editButton.setEnabled(n == 1);
		removeButton.setEnabled(n > 0);
	}

	private void handleEdit() {
		IEnvironmentVariable var = (IEnvironmentVariable) tableViewer.getStructuredSelection().getFirstElement();
		NewEnvVarDialog dialog = new NewEnvVarDialog(getShell(), var);
		if (dialog.open() == Window.OK) {
			envvars.remove(var);
			envvars.add(dialog.getEnvVar());
			tableViewer.refresh();
			tableViewer.setSelection(new StructuredSelection(dialog.getEnvVar()));
		}
	}

	public IEnvironmentVariable[] getEnvVars() {
		return !envvars.isEmpty() ? envvars.toArray(new IEnvironmentVariable[0]) : null;
	}

}
