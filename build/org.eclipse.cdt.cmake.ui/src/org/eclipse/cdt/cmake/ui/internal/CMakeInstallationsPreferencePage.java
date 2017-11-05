/*******************************************************************************
 * Copyright (c) 2017 Institute for Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.cmake.ui.internal;

import org.eclipse.cdt.cmake.core.ICMakeInstallation;
import org.eclipse.cdt.cmake.core.ICMakeInstallationManager;
import org.eclipse.cdt.cmake.core.ICMakeInstallation.Type;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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

public class CMakeInstallationsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, ICheckStateListener, ISelectionChangedListener {
	
	private ICMakeInstallationManager manager;
	private CheckboxTableViewer tableViewer;
	private Button removeInstallationButton;
	
	private class CMakeInstallationsTableLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ICMakeInstallation installation = (ICMakeInstallation) element;
			
			switch (columnIndex) {
			case 0:
				return installation.getCMakeCommand().toString();
			case 1:
				return installation.getVersion();
			case 2:
				return installation.getType().name();
			}
			
			return null;
		}

	}

	private class CMakeInstallationsTableContentProvider implements IStructuredContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return manager.getInstallations().toArray();
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		manager = Activator.getService(ICMakeInstallationManager.class);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group installationsGroup = new Group(control, SWT.NONE);
		installationsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		installationsGroup.setText(Messages.CMakeInstallationsPreferencePage_Installations);
		installationsGroup.setLayout(new GridLayout(2, false));
		
		Composite installationsComposite = new Composite(installationsGroup, SWT.NONE);
		installationsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tableViewer = createInstallationsTableViewer(installationsComposite);
		tableViewer.addCheckStateListener(this);
		tableViewer.addSelectionChangedListener(this);

		createModifierButtons(installationsGroup);

		updateTableViewer();
		return control;
	}

	private void createModifierButtons(Group installationsGroup) {
		Composite modifiersComposite = new Composite(installationsGroup, SWT.NONE);
		modifiersComposite.setLayout(new GridLayout());
		modifiersComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		
		Button addInstallationButton = addButton(modifiersComposite, Messages.CMakeInstallationsPreferencePage_Add);
		addInstallationButton.addListener(SWT.Selection, event -> {
			NewCMakeInstallationWizard newCMakeInstallationWizard = new NewCMakeInstallationWizard(manager);
			WizardDialog wizardDialog = new WizardDialog(getShell(), newCMakeInstallationWizard);
			if(wizardDialog.open() == Window.OK) {
				updateTableViewer();
			}
		});
		
		removeInstallationButton = addButton(modifiersComposite, Messages.CMakeInstallationsPreferencePage_Remove);
		removeInstallationButton.setEnabled(!tableViewer.getSelection().isEmpty());
		removeInstallationButton.addListener(SWT.Selection, event -> {
			TableItem[] selection = tableViewer.getTable().getSelection();
			if(selection.length != 1) {
				return;
			}
			
			ICMakeInstallation installation = (ICMakeInstallation) selection[0].getData();
			manager.remove(installation);
			updateTableViewer();
		});
	}

	private void updateTableViewer() {
		tableViewer.refresh();
		ICMakeInstallation active = manager.getActive();
		if(active != null) {
			tableViewer.setCheckedElements(new Object[] { active });
		}
	}

	private Button addButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.BORDER);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		button.setText(text);
		return button;
	}

	private CheckboxTableViewer createInstallationsTableViewer(Composite parent) {
		Table installationTable = createInstallationsTable(parent);
		
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(installationTable);
		tableViewer.setContentProvider(new CMakeInstallationsTableContentProvider());
		tableViewer.setLabelProvider(new CMakeInstallationsTableLabelProvider());
		tableViewer.setInput(manager);

		ICMakeInstallation activeInstallation = manager.getActive();
		if(activeInstallation != null) {
			tableViewer.setCheckedElements(new ICMakeInstallation[] {activeInstallation});
		}
		
		return tableViewer;
	}

	private Table createInstallationsTable(Composite parent) {
		Table installationTable = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.CHECK);
		installationTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		installationTable.setHeaderVisible(true);
		installationTable.setLinesVisible(true);
		
		TableColumn pathColumn = new TableColumn(installationTable, SWT.NONE);
		pathColumn.setText(Messages.CMakeInstallationsPreferencePage_Path);

		TableColumn versionColumn = new TableColumn(installationTable, SWT.NONE);
		versionColumn.setText(Messages.CMakeInstallationsPreferencePage_Version);

		TableColumn typeColumn = new TableColumn(installationTable, SWT.NONE);
		typeColumn.setText(Messages.CMakeInstallationsPreferencePage_Type);
		
		TableColumnLayout columnLayout = new TableColumnLayout();
		columnLayout.setColumnData(pathColumn, new ColumnWeightData(40, 100, true));
		columnLayout.setColumnData(versionColumn, new ColumnWeightData(40, 80, true));
		columnLayout.setColumnData(typeColumn, new ColumnWeightData(20, 80, true));
		parent.setLayout(columnLayout);
		return installationTable;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		CheckboxTableViewer source = (CheckboxTableViewer) event.getSource();
		source.setCheckedElements(new ICMakeInstallation[] {(ICMakeInstallation) event.getElement()});
	}

	@Override
	public boolean performOk() {
		Object[] checked = tableViewer.getCheckedElements();
		if(checked.length > 0) {
			manager.setActive((ICMakeInstallation) checked[0]);
		}
		return super.performOk();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if(!event.getSelection().isEmpty()) {
			ICMakeInstallation installation = (ICMakeInstallation) event.getStructuredSelection().getFirstElement();
			removeInstallationButton.setEnabled(installation.getType() == Type.CUSTOM);
		} else {
			removeInstallationButton.setEnabled(false);
		}
	}
}
