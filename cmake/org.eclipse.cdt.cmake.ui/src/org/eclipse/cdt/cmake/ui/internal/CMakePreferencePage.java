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
package org.eclipse.cdt.cmake.ui.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.CMakeToolChainManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.envvar.CMakeBuildEnvironmentSupplier;
import org.eclipse.cdt.ui.newui.BuildVarListDialog;
import org.eclipse.cdt.utils.ui.controls.FileListControl;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * GUI page to configure workbench preferences for cmake.
 */
public class CMakePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String VALUE_DELIMITER = " || "; //$NON-NLS-1$

	private ICMakeToolChainManager manager;
	private Table filesTable;
	private Button removeButton;
	private Button variablesButton;
	private Button testButton;
	private Button browseButton;
	private Button editButton;

	private Text cmakeLocationTextBox;
	private Text generatorLocationTextBox;

	private String[] generatorLocations;
	private String cmakeLocation;
	private boolean useCmakeToolLocation;

	private Map<Path, ICMakeToolChainFile> filesToAdd = new HashMap<>();
	private Map<Path, ICMakeToolChainFile> filesToRemove = new HashMap<>();

	@Override
	public void init(IWorkbench workbench) {
		manager = Activator.getService(ICMakeToolChainManager.class);
		updateCmakeToolGroupData();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		Group filesGroup = new Group(control, SWT.NONE);
		filesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesGroup.setText(Messages.CMakePreferencePage_Files);
		filesGroup.setLayout(new GridLayout(2, false));

		Composite filesComp = new Composite(filesGroup, SWT.NONE);
		filesComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		filesTable = new Table(filesComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		filesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filesTable.setHeaderVisible(true);
		filesTable.setLinesVisible(true);
		filesTable.addListener(SWT.Selection, e -> {
			TableItem[] items = filesTable.getSelection();
			removeButton.setEnabled(items.length > 0);
		});

		TableColumn pathColumn = new TableColumn(filesTable, SWT.NONE);
		pathColumn.setText(Messages.CMakePreferencePage_Path);

		TableColumn tcColumn = new TableColumn(filesTable, SWT.NONE);
		tcColumn.setText(Messages.CMakePreferencePage_Toolchain);

		TableColumnLayout tableLayout = new TableColumnLayout();
		tableLayout.setColumnData(pathColumn, new ColumnWeightData(50, 350, true));
		tableLayout.setColumnData(tcColumn, new ColumnWeightData(50, 350, true));
		filesComp.setLayout(tableLayout);

		Composite buttonsComp = new Composite(filesGroup, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttonsComp.setLayout(new GridLayout());

		Button addButton = new Button(buttonsComp, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addButton.setText(Messages.CMakePreferencePage_Add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewCMakeToolChainFileWizard wizard = new NewCMakeToolChainFileWizard();
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				if (dialog.open() == Window.OK) {
					try {
						ICMakeToolChainFile file = wizard.getNewFile();
						IToolChain oldtc = file.getToolChain();
						ICMakeToolChainFile oldFile = manager.getToolChainFileFor(oldtc);
						if (oldFile != null) {
							filesToRemove.put(oldFile.getPath(), oldFile);
						}
						filesToAdd.put(file.getPath(), file);
						updateTable();
					} catch (CoreException ex) {
						Activator.log(ex);
					}
				}
			}
		});

		removeButton = new Button(buttonsComp, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		removeButton.setText(Messages.CMakePreferencePage_Remove);
		removeButton.setEnabled(false);
		removeButton.addListener(SWT.Selection, e -> {
			if (MessageDialog.openConfirm(getShell(), Messages.CMakePreferencePage_ConfirmRemoveTitle,
					Messages.CMakePreferencePage_ConfirmRemoveDesc)) {
				for (TableItem item : filesTable.getSelection()) {
					ICMakeToolChainFile file = (ICMakeToolChainFile) item.getData();
					if (filesToAdd.containsKey(file.getPath())) {
						filesToAdd.remove(file.getPath());
					} else {
						filesToRemove.put(file.getPath(), file);
					}
					updateTable();
				}
			}
		});

		// CMake tools section
		Group cmakeToolsGroup = new Group(control, SWT.NONE);
		cmakeToolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		cmakeToolsGroup.setText(Messages.CMakePreferencePage_CMakeTools);
		cmakeToolsGroup.setLayout(new GridLayout(1, false));

		Composite checkBoxComp = new Composite(cmakeToolsGroup, SWT.NONE);
		checkBoxComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		checkBoxComp.setLayout(new GridLayout());

		Button useCMakeToolLocCheckBox = new Button(checkBoxComp, SWT.CHECK);
		useCMakeToolLocCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		useCMakeToolLocCheckBox.setText(Messages.CMakePreferencePage_UseCMakeToolLocationsInCMakeBuilds);
		useCMakeToolLocCheckBox.setToolTipText(Messages.CMakePreferencePage_UseCMakeToolLocationsInCMakeBuildsTooltip);
		useCMakeToolLocCheckBox.setSelection(useCmakeToolLocation);
		useCMakeToolLocCheckBox.addListener(SWT.Selection, e -> {
			useCmakeToolLocation = useCMakeToolLocCheckBox.getSelection();
			updateCMakeGroup(useCmakeToolLocation);
		});

		// Set width hint to avoid Preference page opens with a horizontal scroll bar when
		// location in textBox got too long.
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = control.getSize().x;

		Composite locationComp = new Composite(cmakeToolsGroup, SWT.NONE);
		locationComp.setLayoutData(gd);
		locationComp.setLayout(new GridLayout(3, false));

		Label cmakeLocationLabel = new Label(locationComp, SWT.NONE);
		cmakeLocationLabel.setText(Messages.CMakePreferencePage_CMakeLocation);
		cmakeLocationLabel.setToolTipText(Messages.CMakePreferencePage_CMakeLocationTooltip);

		cmakeLocationTextBox = new Text(locationComp, SWT.BORDER);
		cmakeLocationTextBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		cmakeLocationTextBox.setText(cmakeLocation);
		cmakeLocationTextBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				cmakeLocation = cmakeLocationTextBox.getText().trim();
				testButton.setEnabled(useCmakeToolLocation && !cmakeLocation.isBlank());
			}
		});

		Composite cmakeLocationButtonComp = new Composite(locationComp, SWT.NONE);
		cmakeLocationButtonComp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		cmakeLocationButtonComp.setLayout(new GridLayout(3, true));

		variablesButton = new Button(cmakeLocationButtonComp, SWT.PUSH);
		variablesButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		variablesButton.setText(Messages.CMakePreferencePage_Variables);
		variablesButton.addListener(SWT.Selection, e -> {
			String variable = getVariableDialog(getShell(), null);
			if (variable != null) {
				cmakeLocationTextBox.insert(variable);
				cmakeLocation = cmakeLocationTextBox.getText().trim();
			}
		});

		testButton = new Button(cmakeLocationButtonComp, SWT.PUSH);
		testButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		testButton.setText(Messages.CMakePreferencePage_Test);
		testButton.setToolTipText(Messages.CMakePreferencePage_TestTooltip);
		testButton.addListener(SWT.Selection, e -> {
			try {
				Process p = Runtime.getRuntime().exec(new String[] {
						resolveVariableValue(cmakeLocation) + File.separatorChar + "cmake", "--version" }); //$NON-NLS-1$ //$NON-NLS-2$
				List<String> buf = new ArrayList<>();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					buf.add(line);
				}
				MessageDialog.openInformation(getShell(), Messages.CMakePreferencePage_TestCmakeLocation_Title,
						Messages.CMakePreferencePage_TestCmakeLocation_Body + String.join(System.lineSeparator(), buf));
			} catch (IOException e1) {
				MessageDialog.openError(getShell(), Messages.CMakePreferencePage_FailToTestCmakeLocation_Title,
						Messages.CMakePreferencePage_FailToTestCmakeLocation_Body + e1.getMessage());
				Activator.log(e1);
			}
		});

		browseButton = new Button(cmakeLocationButtonComp, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		browseButton.setText(Messages.CMakePreferencePage_Browse);
		browseButton.addListener(SWT.Selection, e -> {
			DirectoryDialog dirDialog = new DirectoryDialog(getShell());
			if (!cmakeLocation.isBlank()) {
				dirDialog.setFilterPath(resolveVariableValue(cmakeLocation));
			}
			String browsedDirectory = dirDialog.open();
			if (browsedDirectory != null) {
				cmakeLocationTextBox.setText(browsedDirectory);
				cmakeLocation = cmakeLocationTextBox.getText().trim();
			}
		});

		Label generatorLocationsLabel = new Label(locationComp, SWT.NONE);
		generatorLocationsLabel.setText(Messages.CMakePreferencePage_GeneratorLocation);
		generatorLocationsLabel.setToolTipText(Messages.CMakePreferencePage_GeneratorLocationTooltip);

		generatorLocationTextBox = new Text(locationComp, SWT.BORDER);
		generatorLocationTextBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		generatorLocationTextBox.setEditable(false);
		generatorLocationTextBox.setText(String.join(VALUE_DELIMITER, generatorLocations));

		Composite generatorLocationButtonComp = new Composite(locationComp, SWT.NONE);
		generatorLocationButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		generatorLocationButtonComp.setLayout(new GridLayout(3, true));

		editButton = new Button(generatorLocationButtonComp, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		editButton.setText(Messages.CMakePreferencePage_Edit);
		editButton.addListener(SWT.Selection, e -> {
			EditGeneratorLocationDialog dialog = new EditGeneratorLocationDialog(getShell(),
					Messages.CMakePreferencePage_EditGeneratorLocations_Title, generatorLocations);
			if (dialog.open() == Window.OK) {
				generatorLocations = dialog.getValues();
				generatorLocationTextBox.setText(String.join(VALUE_DELIMITER, generatorLocations));
			}
		});

		updateTable();
		updateCMakeGroup(useCmakeToolLocation);

		return control;
	}

	protected void updateCMakeGroup(boolean enable) {
		cmakeLocationTextBox.setEnabled(enable);
		generatorLocationTextBox.setEnabled(enable);
		variablesButton.setEnabled(enable);
		testButton.setEnabled(enable && !cmakeLocation.isBlank());
		browseButton.setEnabled(enable);
		editButton.setEnabled(enable);
	}

	private void updateTable() {
		List<ICMakeToolChainFile> sorted = new ArrayList<>(getFiles().values());
		Collections.sort(sorted, (o1, o2) -> o1.getPath().toString().compareToIgnoreCase(o2.getPath().toString()));

		filesTable.removeAll();
		for (ICMakeToolChainFile file : sorted) {
			TableItem item = new TableItem(filesTable, SWT.NONE);
			item.setText(0, file.getPath().toString());

			try {
				IToolChain tc = file.getToolChain();
				if (tc != null) {
					item.setText(1, tc.getName());
				}
			} catch (CoreException e) {
				Activator.log(e.getStatus());
			}

			item.setData(file);
		}
	}

	private Map<String, ICMakeToolChainFile> getFiles() {
		Map<String, ICMakeToolChainFile> files = new HashMap<>();
		try {
			for (ICMakeToolChainFile file : manager.getToolChainFiles()) {
				String id = CMakeToolChainManager.makeToolChainId(file.getToolChain());
				files.put(id, file);
			}

			for (ICMakeToolChainFile file : filesToRemove.values()) {
				String id = CMakeToolChainManager.makeToolChainId(file.getToolChain());
				files.remove(id);
			}

			for (ICMakeToolChainFile file : filesToAdd.values()) {
				String id = CMakeToolChainManager.makeToolChainId(file.getToolChain());
				files.put(id, file);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}

		return files;
	}

	@Override
	public boolean performOk() {
		for (ICMakeToolChainFile file : filesToRemove.values()) {
			manager.removeToolChainFile(file);
		}

		for (ICMakeToolChainFile file : filesToAdd.values()) {
			manager.addToolChainFile(file);
		}

		filesToAdd.clear();
		filesToRemove.clear();

		// Update Preferences for cmakeSupplier
		try {
			getPreferences().clear();
			getPreferences().node(CMakeBuildEnvironmentSupplier.CMAKE_GENERATOR_LOCATION).clear();
			getPreferences().putBoolean(CMakeBuildEnvironmentSupplier.ENABLE_USE_CMAKE_LOCATION, useCmakeToolLocation);
			if (!cmakeLocation.isEmpty()) {
				getPreferences().put(CMakeBuildEnvironmentSupplier.CMAKE_LOCATION, cmakeLocation);
			}
			int index;
			for (index = 0; index < generatorLocations.length; index++) {
				getPreferences().node(CMakeBuildEnvironmentSupplier.CMAKE_GENERATOR_LOCATION).put(
						String.format(CMakeBuildEnvironmentSupplier.LOCATION_NODE, index), generatorLocations[index]);

			}
			getPreferences().flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
		return true;
	}

	private void updateCmakeToolGroupData() {
		try {
			useCmakeToolLocation = getPreferences().getBoolean(CMakeBuildEnvironmentSupplier.ENABLE_USE_CMAKE_LOCATION,
					false);
			cmakeLocation = getPreferences().get(CMakeBuildEnvironmentSupplier.CMAKE_LOCATION,
					CMakeBuildEnvironmentSupplier.EMPTY_STRING);
			List<String> genlocs = new ArrayList<>();
			String[] keys = getPreferences().node(CMakeBuildEnvironmentSupplier.CMAKE_GENERATOR_LOCATION).keys();
			int index;
			for (index = 0; index < keys.length; index++) {
				genlocs.add(getPreferences().node(CMakeBuildEnvironmentSupplier.CMAKE_GENERATOR_LOCATION).get(
						String.format(CMakeBuildEnvironmentSupplier.LOCATION_NODE, index),
						CMakeBuildEnvironmentSupplier.EMPTY_STRING));
			}
			generatorLocations = genlocs.toArray(new String[0]);
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	private String resolveVariableValue(String value) {
		try {
			ICdtVariableManager vm = CCorePlugin.getDefault().getCdtVariableManager();
			return vm.resolveValue(value, null, CMakeBuildEnvironmentSupplier.EMPTY_STRING, null);
		} catch (CdtVariableException e) {
			Activator.log(e);
		}
		return null;
	}

	private String getVariableDialog(Shell shell, ICConfigurationDescription cfgd) {
		ICdtVariableManager vm = CCorePlugin.getDefault().getCdtVariableManager();
		BuildVarListDialog dialog = new BuildVarListDialog(shell, vm.getVariables(cfgd));
		dialog.setTitle(Messages.VariablesDialog_Title);
		if (dialog.open() == Window.OK) {
			Object[] selected = dialog.getResult();
			if (selected.length > 0) {
				String s = ((ICdtVariable) selected[0]).getName();
				return "${" + s.trim() + "}"; //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return null;
	}

	private class EditGeneratorLocationDialog extends Dialog {

		private String title;
		private FileListControl listEditor;
		private String[] genLocs;

		public EditGeneratorLocationDialog(Shell parentShell, String title, String[] genLocs) {
			super(parentShell);
			this.genLocs = genLocs;
			this.title = title;
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			if (title != null) {
				shell.setText(title);
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite comp = new Composite(parent, SWT.NULL);
			comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			comp.setLayout(new GridLayout());
			listEditor = new FileListControl(comp,
					Messages.CMakePreferencePage_EditGeneratorLocations_GeneratorLocation, FileListControl.BROWSE_DIR);
			if (genLocs != null) {
				listEditor.setList(genLocs);
			}
			return comp;
		}

		@Override
		protected void okPressed() {
			genLocs = listEditor.getItems();
			super.okPressed();
		}

		public String[] getValues() {
			List<String> values = new ArrayList<>();
			for (String loc : genLocs) {
				// Clean up return values
				values.add(loc.replace("\"", CMakeBuildEnvironmentSupplier.EMPTY_STRING).trim()); //$NON-NLS-1$
			}
			return values.toArray(new String[0]);
		}
	}

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node(CMakeBuildEnvironmentSupplier.NODENAME);
	}
}
