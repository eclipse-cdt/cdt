/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.ui;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;


public class CEnvironmentTab extends CLaunchConfigurationTab {

	protected Properties fElements;

	protected TableViewer fVariableList;
	protected Button fBtnNew;
	protected Button fBtnEdit;
	protected Button fBtnRemove;
	protected Button fBtnImport;

	class SimpleSorter extends ViewerSorter {
		public boolean isSorterProperty(Object element, Object property) {
			return true;
		}
	}

	class ElementsContentProvider implements IStructuredContentProvider {
		Object input = null;

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return fElements.entrySet().toArray();
		}
	}

	class ElementsLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element != null && element instanceof Map.Entry) {
				return (columnIndex == 0) ? ((Map.Entry) element).getKey().toString() : ((Map.Entry) element).getValue().toString();
			}
			return null;
		}
	}

	class EntryDialog extends Dialog {
		private String fName;
		private String fValue;
		private boolean fEdit = false;

		private Button fBtnOk = null;
		private Text fTextName = null;
		private Text fTextValue = null;

		public EntryDialog(String name, String value, boolean edit) {
			super(CEnvironmentTab.this.getControl().getShell());
			fName = name;
			fValue = value;
			fEdit = edit;
		}

		protected Control createContents(Composite parent) {
			Control result = super.createContents(parent);
			updateButtonsState();
			return result;
		}

		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			String title = (fEdit) ? LaunchUIPlugin.getResourceString("CEnvironmentTab.Edit_Variable") : LaunchUIPlugin.getResourceString("CEnvironmentTab.New_Variable"); //$NON-NLS-1$ //$NON-NLS-2$
			shell.setText(title);
		}

		protected Control createDialogArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout(2, false);
			layout.marginWidth = 5;
			layout.numColumns = 2;
			composite.setLayout(layout);

			GC gc = new GC(composite);
			gc.setFont(composite.getFont());
			FontMetrics metrics = gc.getFontMetrics();
			gc.dispose();
			int fieldWidthHint = convertWidthInCharsToPixels(metrics, 50);

			Label label = new Label(composite, SWT.NONE);
			label.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.NameColon")); //$NON-NLS-1$
			fTextName = new Text(composite, SWT.SINGLE | SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.grabExcessHorizontalSpace = true;
			gd.widthHint = fieldWidthHint;
			fTextName.setLayoutData(gd);
			label = new Label(composite, SWT.NONE);
			label.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.ValueColon")); //$NON-NLS-1$
			fTextValue = new Text(composite, SWT.SINGLE | SWT.BORDER);
			gd = new GridData(GridData.FILL_BOTH);
			gd.grabExcessHorizontalSpace = true;
			gd.widthHint = fieldWidthHint;
			fTextValue.setLayoutData(gd);
			fTextName.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateButtonsState();
				}
			});
			fTextValue.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateButtonsState();
				}
			});
			fTextName.setText(fName);
			fTextValue.setText(fValue);

			return composite;
		}

		protected void createButtonsForButtonBar(Composite parent) {
			fBtnOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}

		protected void updateButtonsState() {
			if (fBtnOk != null)
				fBtnOk.setEnabled(fTextName.getText().trim().length() > 0);
		}

		protected String getName() {
			return fName;
		}

		protected String getValue() {
			return fValue;
		}

		protected void okPressed() {
			fName = fTextName.getText().trim();
			fValue = fTextValue.getText().trim();
			setReturnCode(OK);
			close();
		}
	}

	public void createControl(Composite parent) {
		fElements = new Properties();
		Composite control = new Composite(parent, SWT.NONE);
		setControl(control);

		WorkbenchHelp.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB);

		GridLayout gl = new GridLayout(2, false);

		createVerticalSpacer(control, 2);

		control.setLayout(gl);
		createVariableList(control);
		createButtons(control);
		fVariableList.setInput(fElements);
		fVariableList.getTable().setFocus();
	}

	public void set(String env) {
		fElements.clear();
		ByteArrayInputStream input = new ByteArrayInputStream(env.getBytes());
		try {
			fElements.load(input);
		} catch (IOException e) {
		}

		fVariableList.refresh();
		fVariableList.getTable().setFocus();
		if (fVariableList.getTable().getItemCount() > 0)
			fVariableList.getTable().setSelection(0);
	}

	public String get() {
		String result = new String();
		Object[] entries = fElements.entrySet().toArray();
		for (int i = 0; i < entries.length; ++i)
			result += entries[i].toString() + '\n';
		return result;
	}

	public Properties getProperties() {
		return fElements;
	}

	public Object[] toArray() {
		return fElements.entrySet().toArray();
	}

	private void createVariableList(Composite parent) {
		fVariableList = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		fVariableList.setContentProvider(new ElementsContentProvider());
		fVariableList.setLabelProvider(new ElementsLabelProvider());
		fVariableList.setSorter(new SimpleSorter());

		Table table = fVariableList.getTable();

		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);

		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		gd.grabExcessVerticalSpace = true;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn column1 = new TableColumn(table, SWT.NULL);
		column1.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.Name")); //$NON-NLS-1$
		tableLayout.addColumnData(new ColumnWeightData(30));

		TableColumn column2 = new TableColumn(table, SWT.NULL);
		column2.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.Value")); //$NON-NLS-1$
		tableLayout.addColumnData(new ColumnWeightData(30));

		fVariableList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				elementDoubleClicked((IStructuredSelection) e.getSelection());
			}
		});
		fVariableList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				updateButtons();
			}
		});
	}

	private void createButtons(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		composite.setLayout(new GridLayout(1, true));
		fBtnNew = new Button(composite, SWT.NONE);
		fBtnNew.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.New...")); //$NON-NLS-1$
		fBtnNew.setLayoutData(new GridData(GridData.FILL_BOTH));
		fBtnNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newEntry();
			}
		});
		fBtnImport = new Button(composite, SWT.NONE);
		fBtnImport.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.Import...")); //$NON-NLS-1$
		fBtnImport.setLayoutData(new GridData(GridData.FILL_BOTH));
		fBtnImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				importEntries();
			}
		});
		fBtnEdit = new Button(composite, SWT.NONE);
		fBtnEdit.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.Edit...")); //$NON-NLS-1$
		fBtnEdit.setLayoutData(new GridData(GridData.FILL_BOTH));
		fBtnEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edit();
			}
		});
		fBtnRemove = new Button(composite, SWT.NONE);
		fBtnRemove.setText(LaunchUIPlugin.getResourceString("CEnvironmentTab.Remove")); //$NON-NLS-1$
		fBtnRemove.setLayoutData(new GridData(GridData.FILL_BOTH));
		fBtnRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				remove();
			}
		});
	}

	protected void updateButtons() {
		IStructuredSelection selection = (IStructuredSelection) fVariableList.getSelection();
		fBtnEdit.setEnabled(selection.size() == 1);
		fBtnRemove.setEnabled(selection.size() > 0);
	}

	protected void elementDoubleClicked(IStructuredSelection selection) {
		if (selection.size() != 1)
			return;
		doEdit((Map.Entry) selection.getFirstElement());
	}

	protected void newEntry() {
		EntryDialog dialog = new EntryDialog(new String(), new String(), false);
		if (dialog.open() == Window.OK) {
			fElements.setProperty(dialog.getName(), dialog.getValue());
			fVariableList.refresh();
		}
		updateButtons();
		updateLaunchConfigurationDialog();
	}

	protected void importEntries() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
		final String filename = fileDialog.open();
		if(filename == null) {
			return;
		}

		parseImportFile(filename);
		
		updateButtons();
		updateLaunchConfigurationDialog();
	}
	
	protected void parseImportFile(String filename) {
		File file = new File(filename);
		if(!file.exists()) {
			return;
		}

		//Iterate through each key/value property we discover
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));	

			String line, key, value;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) {
					continue;
				}
				
				int demarcation = line.indexOf("="); //$NON-NLS-1$
				if(demarcation == -1) {
					key = line;
					value = ""; //$NON-NLS-1$
				} else {
					key = line.substring(0, demarcation);
					value = line.substring(demarcation + 1, line.length());
				}

				if(fElements.getProperty(key) != null) {
					boolean overwrite;
					overwrite = MessageDialog.openQuestion(getShell(), LaunchUIPlugin.getResourceString("CEnvironmentTab.Existing_Environment_Variable"), LaunchUIPlugin.getFormattedResourceString("CEnvironmentTab.Environment_variable_NAME_exists", key)); //$NON-NLS-1$ //$NON-NLS-2$
					if(!overwrite) {
						continue;
					}
				}

				fElements.setProperty(key, value);
			}
		} catch(Exception ex) {
			
		}

		fVariableList.refresh();		
	}

	protected void edit() {
		IStructuredSelection selection = (IStructuredSelection) fVariableList.getSelection();
		doEdit((Map.Entry) selection.getFirstElement());
	}

	protected void doEdit(Map.Entry entry) {
		EntryDialog dialog = new EntryDialog(entry.getKey().toString(), entry.getValue().toString(), true);
		if (dialog.open() == Window.OK) {
			fElements.remove(entry.getKey());
			fElements.setProperty(dialog.getName(), dialog.getValue());
			fVariableList.refresh();
		}
		updateButtons();
		updateLaunchConfigurationDialog();
	}

	protected void remove() {
		IStructuredSelection selection = (IStructuredSelection) fVariableList.getSelection();
		Object[] elements = selection.toArray();
		for (int i = 0; i < elements.length; ++i)
			fElements.remove(((Map.Entry) elements[i]).getKey());
		fVariableList.refresh();
		updateButtons();
		updateLaunchConfigurationDialog();
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map) null);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_INHERIT, true);
	}

	public void initializeFrom(ILaunchConfiguration config) {
		try {
			Map env = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map) null);
			if (env != null) {
				fElements.clear();
				fElements.putAll(env);
				fVariableList.refresh();
				updateButtons();
			}
			//			config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_INHERIT, true);
		} catch (CoreException e) {
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map) fElements.clone());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_INHERIT, true);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchUIPlugin.getResourceString("CEnvironmentTab.Environment"); //$NON-NLS-1$
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_ENVIRONMENT_TAB);
	}

}
