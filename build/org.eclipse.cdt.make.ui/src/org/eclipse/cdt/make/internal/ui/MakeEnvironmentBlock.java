/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.ListSelectionDialog;

public class MakeEnvironmentBlock extends AbstractCOptionPage {

	Preferences fPrefs;
	String fBuilderID;
	IMakeCommonBuildInfo fBuildInfo;
	protected TableViewer environmentTable;
	protected String[] envTableColumnHeaders = {MakeUIPlugin.getResourceString("MakeEnvironmentBlock.0"), MakeUIPlugin.getResourceString("MakeEnvironmentBlock.1")}; //$NON-NLS-1$ //$NON-NLS-2$
	protected ColumnLayoutData[] envTableColumnLayouts = {new ColumnPixelData(150), new ColumnPixelData(250)};

	private static final String NAME_LABEL = MakeUIPlugin.getResourceString("MakeEnvironmentBlock.2"); //$NON-NLS-1$
	private static final String VALUE_LABEL = MakeUIPlugin.getResourceString("MakeEnvironmentBlock.3"); //$NON-NLS-1$

	protected static final String P_VARIABLE = "variable"; //$NON-NLS-1$
	protected static final String P_VALUE = "value"; //$NON-NLS-1$
	protected static String[] envTableColumnProperties = {P_VARIABLE, P_VALUE};
	protected Button envAddButton;
	protected Button envEditButton;
	protected Button envRemoveButton;
	protected Button appendEnvironment;
	protected Button replaceEnvironment;
	protected Button envSelectButton;

	class EnvironmentVariable {

		// The name of the environment variable
		private String name;

		// The value of the environment variable
		private String value;

		EnvironmentVariable(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * Returns this variable's name, which serves as the key in the
		 * key/value pair this variable represents
		 * 
		 * @return this variable's name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns this variables value.
		 * 
		 * @return this variable's value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Sets this variable's value
		 * 
		 * @param value
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			boolean equal = false;
			if (obj instanceof EnvironmentVariable) {
				EnvironmentVariable var = (EnvironmentVariable)obj;
				equal = var.getName().equals(name);
			}
			return equal;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return name.hashCode();
		}
	}

	/**
	 * Content provider for the environment table
	 */
	protected class EnvironmentVariableContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			EnvironmentVariable[] elements = new EnvironmentVariable[0];
			IMakeCommonBuildInfo info = (IMakeCommonBuildInfo)inputElement;
			Map m = info.getEnvironment();
			if (m != null && !m.isEmpty()) {
				elements = new EnvironmentVariable[m.size()];
				String[] varNames = new String[m.size()];
				m.keySet().toArray(varNames);
				for (int i = 0; i < m.size(); i++) {
					elements[i] = new EnvironmentVariable(varNames[i], (String)m.get(varNames[i]));
				}
			}
			return elements;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				return;
			}
			if (viewer instanceof TableViewer) {
				TableViewer tableViewer = (TableViewer)viewer;
				if (tableViewer.getTable().isDisposed()) {
					return;
				}
				tableViewer.setSorter(new ViewerSorter() {

					public int compare(Viewer iviewer, Object e1, Object e2) {
						if (e1 == null) {
							return -1;
						} else if (e2 == null) {
							return 1;
						} else {
							return ((EnvironmentVariable)e1).getName().compareToIgnoreCase( ((EnvironmentVariable)e2).getName());
						}
					}
				});
			}
		}
	}

	/**
	 * Label provider for the environment table
	 */
	public class EnvironmentVariableLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object element, int columnIndex) {
			String result = null;
			if (element != null) {
				EnvironmentVariable var = (EnvironmentVariable)element;
				switch (columnIndex) {
					case 0 : // variable
						result = var.getName();
						break;
					case 1 : // value
						result = var.getValue();
						break;
				}
			}
			return result;
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_ENV_VAR);
			}
			return null;
		}
	}

	public MakeEnvironmentBlock(Preferences prefs, String builderID) {
		super(MakeUIPlugin.getResourceString("MakeEnvironmentBlock.4")); //$NON-NLS-1$
		setDescription(MakeUIPlugin.getResourceString("MakeEnvironmentBlock.5")); //$NON-NLS-1$
		fPrefs = prefs;
		fBuilderID = builderID;
	}

	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		if (getContainer().getProject() != null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), fBuilderID);
			} catch (CoreException e) {
			}
		} else {
			fBuildInfo = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, false);
		}
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		// Missing builder info
		if (fBuildInfo == null) {
			return;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IWorkspace workspace = MakeUIPlugin.getWorkspace();
		// To avoid multi-build
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(MakeUIPlugin.getResourceString("SettingsBlock.monitor.applyingSettings"), 1); //$NON-NLS-1$
				IMakeCommonBuildInfo info = null;
				if (getContainer().getProject() != null) {
					try {
						info = MakeCorePlugin.createBuildInfo(getContainer().getProject(), fBuilderID);
					} catch (CoreException e) {
						// disabled builder... just log it
						MakeCorePlugin.log(e);
						return;
					}
				} else {
					info = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, false);
				}
				// Convert the table's items into a Map so that this can be saved in the
				// configuration's attributes.
				TableItem[] items = environmentTable.getTable().getItems();
				Map map = new HashMap(items.length);
				for (int i = 0; i < items.length; i++)
				{
					EnvironmentVariable var = (EnvironmentVariable) items[i].getData();
					map.put(var.getName(), var.getValue());
				} 
				info.setEnvironment(map);
				info.setAppendEnvironment(appendEnvironment.getSelection());
			}
		};
		if (getContainer().getProject() != null) {
			workspace.run(operation, monitor);
		} else {
			operation.run(monitor);
		}
	}

	/**
	 * Updates the environment table for the given launch configuration
	 * 
	 * @param configuration
	 */
	protected void updateEnvironment(IMakeCommonBuildInfo info) {
		environmentTable.setInput(info);
	}

	public void performDefaults() {
		// Missing builder info
		if (fBuildInfo == null) {
			return;
		}

		IMakeCommonBuildInfo info;
		if (getContainer().getProject() != null) {
			info = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, false);
		} else {
			info = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, true);
		}
		boolean append = info.appendEnvironment();
		if (append) {
			appendEnvironment.setSelection(true);
			replaceEnvironment.setSelection(false);
		} else {
			replaceEnvironment.setSelection(true);
			appendEnvironment.setSelection(false);
		}
		updateEnvironment(info);
		updateAppendReplace();
	}

	public void createControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 1);
		setControl(composite);

		MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), IMakeHelpContextIds.MAKE_BUILDER_SETTINGS);

		if (fBuildInfo == null) {
			ControlFactory.createEmptySpace(composite);
			ControlFactory.createLabel(composite, MakeUIPlugin.getResourceString("SettingsBlock.label.missingBuilderInformation")); //$NON-NLS-1$
			return;
		}

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayout(layout);
		composite.setLayoutData(gridData);
		composite.setFont(parent.getFont());

		createBuildEnvironmentControls(composite);
		createTableButtons(composite);
		createAppendReplace(composite);
		
		boolean append = fBuildInfo.appendEnvironment();
		if (append) {
			appendEnvironment.setSelection(true);
			replaceEnvironment.setSelection(false);
		} else {
			replaceEnvironment.setSelection(true);
			appendEnvironment.setSelection(false);
		}
		updateEnvironment(fBuildInfo);
		updateAppendReplace();

	}

	private void createBuildEnvironmentControls(Composite parent) {
		Font font = parent.getFont();
		// Create table composite
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 150;
		tableComposite.setLayout(layout);
		tableComposite.setLayoutData(gridData);
		tableComposite.setFont(font);
		// Create label
		Label label = new Label(tableComposite, SWT.NONE);
		label.setFont(font);
		label.setText(MakeUIPlugin.getResourceString("MakeEnvironmentBlock.6")); //$NON-NLS-1$
		// Create table
		environmentTable = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		Table table = environmentTable.getTable();
		TableLayout tableLayout = new TableLayout();
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setFont(font);
		gridData = new GridData(GridData.FILL_BOTH);
		environmentTable.getControl().setLayoutData(gridData);
		environmentTable.setContentProvider(new EnvironmentVariableContentProvider());
		environmentTable.setLabelProvider(new EnvironmentVariableLabelProvider());
		environmentTable.setColumnProperties(envTableColumnProperties);
		environmentTable.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				handleTableSelectionChanged(event);
			}
		});
		environmentTable.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				if (!environmentTable.getSelection().isEmpty()) {
					handleEnvEditButtonSelected();
				}
			}
		});
		// Create columns
		for (int i = 0; i < envTableColumnHeaders.length; i++) {
			tableLayout.addColumnData(envTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(envTableColumnLayouts[i].resizable);
			tc.setText(envTableColumnHeaders[i]);
		}
	}

	/**
	 * Responds to a selection changed event in the environment table
	 * 
	 * @param event
	 *            the selection change event
	 */
	protected void handleTableSelectionChanged(SelectionChangedEvent event) {
		int size = ((IStructuredSelection)event.getSelection()).size();
		envEditButton.setEnabled(size == 1);
		envRemoveButton.setEnabled(size > 0);
	}

	/**
	 * Create some empty space.
	 */
	protected void createVerticalSpacer(Composite comp, int colSpan) {
		Label label = new Label(comp, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = colSpan;
		label.setLayoutData(gd);
		label.setFont(comp.getFont());
	}

	/**
	 * Creates the add/edit/remove buttons for the environment table
	 * 
	 * @param parent
	 *            the composite in which the buttons should be created
	 */
	protected void createTableButtons(Composite parent) {
		// Create button composite
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginHeight = 0;
		glayout.marginWidth = 0;
		glayout.numColumns = 1;
		GridData gdata = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(glayout);
		buttonComposite.setLayoutData(gdata);
		buttonComposite.setFont(parent.getFont());

		createVerticalSpacer(buttonComposite, 1);
		// Create buttons
		envAddButton = createPushButton(buttonComposite, MakeUIPlugin.getResourceString("MakeEnvironmentBlock.7"), null); //$NON-NLS-1$
		envAddButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				handleEnvAddButtonSelected();
			}
		});
		envSelectButton = createPushButton(buttonComposite, MakeUIPlugin.getResourceString("MakeEnvironmentBlock.8"), null); //$NON-NLS-1$
		envSelectButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				handleEnvSelectButtonSelected();
			}
		});
		envEditButton = createPushButton(buttonComposite, MakeUIPlugin.getResourceString("MakeEnvironmentBlock.9"), null); //$NON-NLS-1$
		envEditButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				handleEnvEditButtonSelected();
			}
		});
		envEditButton.setEnabled(false);
		envRemoveButton = createPushButton(buttonComposite, MakeUIPlugin.getResourceString("MakeEnvironmentBlock.10"), null); //$NON-NLS-1$
		envRemoveButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				handleEnvRemoveButtonSelected();
			}
		});
		envRemoveButton.setEnabled(false);
	}

	/**
	 * Adds a new environment variable to the table.
	 */
	protected void handleEnvAddButtonSelected() {
		MultipleInputDialog dialog = new MultipleInputDialog(getShell(), MakeUIPlugin.getResourceString("MakeEnvironmentBlock.11")); //$NON-NLS-1$
		dialog.addTextField(NAME_LABEL, null, false);
		dialog.addVariablesField(VALUE_LABEL, null, true);

		if (dialog.open() != Window.OK) {
			return;
		}

		String name = dialog.getStringValue(NAME_LABEL);
		String value = dialog.getStringValue(VALUE_LABEL);

		if (name != null && value != null && name.length() > 0 && value.length() > 0) {
			addVariable(new EnvironmentVariable(name.trim(), value.trim()));
			updateAppendReplace();
		}
	}

	/**
	 * Updates the enablement of the append/replace widgets. The widgets should
	 * disable when there are no environment variables specified.
	 */
	protected void updateAppendReplace() {
		boolean enable = environmentTable.getTable().getItemCount() > 0;
		appendEnvironment.setEnabled(enable);
		replaceEnvironment.setEnabled(enable);
	}

	/**
	 * Attempts to add the given variable. Returns whether the variable was
	 * added or not (as when the user answers not to overwrite an existing
	 * variable).
	 * 
	 * @param variable
	 *            the variable to add
	 * @return whether the variable was added
	 */
	protected boolean addVariable(EnvironmentVariable variable) {
		String name = variable.getName();
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable existingVariable = (EnvironmentVariable)items[i].getData();
			if (existingVariable.getName().equals(name)) {
				boolean overWrite = MessageDialog.openQuestion(getShell(), MakeUIPlugin.getResourceString("MakeEnvironmentBlock.12"), MessageFormat.format( //$NON-NLS-1$
						MakeUIPlugin.getResourceString("MakeEnvironmentBlock.13"), new String[]{name})); //$NON-NLS-1$
				if (!overWrite) {
					return false;
				}
				environmentTable.remove(existingVariable);
				break;
			}
		}
		environmentTable.add(variable);
		getContainer().updateContainer();
		return true;
	}

	/**
	 * Gets native environment variable. Creates EnvironmentVariable objects.
	 * 
	 * @return Map of name - EnvironmentVariable pairs based on native
	 *         environment.
	 */
	private Map getNativeEnvironment() {
		Map stringVars = EnvironmentReader.getEnvVars();
		HashMap vars = new HashMap();
		for (Iterator i = stringVars.keySet().iterator(); i.hasNext();) {
			String key = (String)i.next();
			String value = (String)stringVars.get(key);
			vars.put(key, new EnvironmentVariable(key, value));
		}
		return vars;
	}

	/**
	 * Displays a dialog that allows user to select native environment variables
	 * to add to the table.
	 */
	protected void handleEnvSelectButtonSelected() {
		// get Environment Variables from the OS
		Map envVariables = getNativeEnvironment();

		// get Environment Variables from the table
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable var = (EnvironmentVariable)items[i].getData();
			envVariables.remove(var.getName());
		}

		ListSelectionDialog dialog = new NativeEnvironmentDialog(getShell(), envVariables, createSelectionDialogContentProvider(),
				createSelectionDialogLabelProvider(), MakeUIPlugin.getResourceString("MakeEnvironmentBlock.14")); //$NON-NLS-1$
		dialog.setTitle(MakeUIPlugin.getResourceString("MakeEnvironmentBlock.15")); //$NON-NLS-1$

		int button = dialog.open();
		if (button == Window.OK) {
			Object[] selected = dialog.getResult();
			for (int i = 0; i < selected.length; i++) {
				environmentTable.add(selected[i]);
			}
		}

		updateAppendReplace();
		getContainer().updateContainer();
	}

	/**
	 * Creates a label provider for the native native environment variable
	 * selection dialog.
	 * 
	 * @return A label provider for the native native environment variable
	 *         selection dialog.
	 */
	private ILabelProvider createSelectionDialogLabelProvider() {
		return new ILabelProvider() {

			public Image getImage(Object element) {
				return MakeUIImages.getImage(MakeUIImages.IMG_OBJS_ENVIRONMNET);
			}
			public String getText(Object element) {
				EnvironmentVariable var = (EnvironmentVariable)element;
				return var.getName() + " [" + var.getValue() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public void dispose() {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void removeListener(ILabelProviderListener listener) {
			}
		};
	}

	/**
	 * Creates a content provider for the native native environment variable
	 * selection dialog.
	 * 
	 * @return A content provider for the native native environment variable
	 *         selection dialog.
	 */
	private IStructuredContentProvider createSelectionDialogContentProvider() {
		return new IStructuredContentProvider() {

			public Object[] getElements(Object inputElement) {
				EnvironmentVariable[] elements = null;
				if (inputElement instanceof Map) {
					Comparator comparator = new Comparator() {

						public int compare(Object o1, Object o2) {
							String s1 = (String)o1;
							String s2 = (String)o2;
							return s1.compareTo(s2);
						}

					};
					TreeMap envVars = new TreeMap(comparator);
					envVars.putAll((Map)inputElement);
					elements = new EnvironmentVariable[envVars.size()];
					int index = 0;
					for (Iterator iterator = envVars.keySet().iterator(); iterator.hasNext(); index++) {
						Object key = iterator.next();
						elements[index] = (EnvironmentVariable)envVars.get(key);
					}
				}
				return elements;
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		};
	}
	/**
	 * Creates an editor for the value of the selected environment variable.
	 */
	protected void handleEnvEditButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection)environmentTable.getSelection();
		EnvironmentVariable var = (EnvironmentVariable)sel.getFirstElement();
		if (var == null) {
			return;
		}
		String originalName = var.getName();
		String value = var.getValue();
		MultipleInputDialog dialog = new MultipleInputDialog(getShell(), MakeUIPlugin.getResourceString("MakeEnvironmentBlock.16")); //$NON-NLS-1$
		dialog.addTextField(NAME_LABEL, originalName, false);
		dialog.addVariablesField(VALUE_LABEL, value, true);

		if (dialog.open() != Window.OK) {
			return;
		}
		String name = dialog.getStringValue(NAME_LABEL);
		value = dialog.getStringValue(VALUE_LABEL);
		if (!originalName.equals(name)) {
			if (addVariable(new EnvironmentVariable(name, value))) {
				environmentTable.remove(var);
			}
		} else {
			var.setValue(value);
			environmentTable.update(var, null);
			getContainer().updateContainer();
		}
	}

	/**
	 * Removes the selected environment variable from the table.
	 */
	protected void handleEnvRemoveButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection)environmentTable.getSelection();
		environmentTable.getControl().setRedraw(false);
		for (Iterator i = sel.iterator(); i.hasNext();) {
			EnvironmentVariable var = (EnvironmentVariable)i.next();
			environmentTable.remove(var);
		}
		environmentTable.getControl().setRedraw(true);
		updateAppendReplace();
		getContainer().updateContainer();
	}

	private class NativeEnvironmentDialog extends ListSelectionDialog {

		public NativeEnvironmentDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider,
				ILabelProvider labelProvider, String message) {
			super(parentShell, input, contentProvider, labelProvider, message);
			setShellStyle(getShellStyle() | SWT.RESIZE);
		}

		protected IDialogSettings getDialogSettings() {
			IDialogSettings settings = MakeUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
			if (section == null) {
				section = settings.addNewSection(getDialogSettingsSectionName());
			}
			return section;
		}

		/**
		 * Returns the name of the section that this dialog stores its settings
		 * in
		 * 
		 * @return String
		 */
		protected String getDialogSettingsSectionName() {
			return MakeUIPlugin.getPluginId() + ".ENVIRONMENT_TAB.NATIVE_ENVIROMENT_DIALOG"; //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
		 */
		protected Point getInitialLocation(Point initialSize) {
			Point initialLocation = DialogSettingsHelper.getInitialLocation(getDialogSettingsSectionName());
			if (initialLocation != null) {
				return initialLocation;
			}
			return super.getInitialLocation(initialSize);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#getInitialSize()
		 */
		protected Point getInitialSize() {
			Point size = super.getInitialSize();
			return DialogSettingsHelper.getInitialSize(getDialogSettingsSectionName(), size);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.window.Window#close()
		 */
		public boolean close() {
			DialogSettingsHelper.persistShellGeometry(getShell(), getDialogSettingsSectionName());
			return super.close();
		}
	}

	/**
	 * Creates and configures the widgets which allow the user to choose whether
	 * the specified environment should be appended to the native environment or
	 * if it should completely replace it.
	 * 
	 * @param parent
	 *            the composite in which the widgets should be created
	 */
	protected void createAppendReplace(Composite parent) {
		Composite appendReplaceComposite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		GridLayout layout = new GridLayout();
		appendReplaceComposite.setLayoutData(gridData);
		appendReplaceComposite.setLayout(layout);
		appendReplaceComposite.setFont(parent.getFont());

		appendEnvironment = createRadioButton(appendReplaceComposite, MakeUIPlugin.getResourceString("MakeEnvironmentBlock.17")); //$NON-NLS-1$
		appendEnvironment.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				getContainer().updateContainer();
			}
		});
		replaceEnvironment = createRadioButton(appendReplaceComposite, MakeUIPlugin.getResourceString("MakeEnvironmentBlock.18")); //$NON-NLS-1$
	}

}
