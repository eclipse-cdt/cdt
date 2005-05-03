/**********************************************************************
 * Copyright (c) 2002,2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class ManageConfigDialog extends Dialog {
	// String constants
	private static final String CMN_PREFIX = "BuildPropertyCommon";	//$NON-NLS-1$
	private static final String CMN_LABEL = CMN_PREFIX + ".label";	//$NON-NLS-1$
	private static final String NEW = CMN_LABEL + ".new";	//$NON-NLS-1$
	private static final String REMOVE = CMN_LABEL + ".remove";	//$NON-NLS-1$
	private static final String PREFIX = "ManageConfig";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String CONVERSION_TARGET_LABEL = LABEL + ".conversionTargetLabel";	//$NON-NLS-1$
	private static final String CONVERT_TARGET = LABEL + ".convertTarget";	//$NON-NLS-1$
	private static final String RENAME = LABEL + ".rename";	//$NON-NLS-1$
	private static final String CONFIGS = LABEL + ".configs";	//$NON-NLS-1$
	private static final String CURRENT_CONFIGS = CONFIGS + ".current";	//$NON-NLS-1$
	private static final String DELETED_CONFIGS = CONFIGS + ".deleted";	//$NON-NLS-1$
	private static final String NEW_CONF_DLG = LABEL + ".new.config.dialog";	//$NON-NLS-1$
	private static final String RENAME_CONF_DLG = LABEL + ".rename.config.dialog";	//$NON-NLS-1$
	
	private static final String TIP = PREFIX + ".tip";	//$NON-NLS-1$
	private static final String CONVERSION_TARGET_TIP = TIP + ".conversionTarget";	//$NON-NLS-1$
	private static final String CONVERT_TIP = TIP + ".convert";	//$NON-NLS-1$
	
	private static final String ID_SEPARATOR = ".";	//$NON-NLS-1$

	private static final String EMPTY_STRING = new String();

	// The list of configurations to delete
	private SortedMap deletedConfigs;
	// Map of configuration names and ids
	private SortedMap existingConfigs;
	// The target the configs belong to
	private IManagedProject managedProject;
	
	// selected Configuration
	IConfiguration selectedConfiguration;

	// The title of the dialog.
	private String title = ""; //$NON-NLS-1$
	
	private Combo conversionTargetSelector;
	private Button convertTargetBtn;
	// Widgets
	protected List currentConfigList;

	protected Button newBtn;
	protected Button okBtn;
	protected Button removeBtn;
	protected Button renameBtn;
	
	
	/**
	 * @param parentShell
	 */
	protected ManageConfigDialog(Shell parentShell, String title, IManagedProject proj) {
		super(parentShell);
		this.title = title;
		this.managedProject = proj;
		
		// Get the defined configurations from the target
		getExistingConfigs().clear();
		IConfiguration [] configs = managedProject.getConfigurations();
		for (int i = 0; i < configs.length; i++) {
			IConfiguration configuration = configs[i];
			String name = configuration.getName();
			String description = configuration.getDescription();
			String nameAndDescription = new String();
			
			if ( description == null || description.equals("") ) {	//$NON-NLS-1$
				nameAndDescription = name;
			} else {
				nameAndDescription = name + "( " + description + " )"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			getExistingConfigs().put(nameAndDescription, configuration);
		}
		
		// Set the selectedConfiguration to default configuration.
		selectedConfiguration = ManagedBuildManager.getSelectedConfiguration(getProject());
		
		// clear DeletedConfig list
		getDeletedConfigs().clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okBtn = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	//	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		updateButtons();
	}

	/* (non-Javadoc)
	 * Create and lays out the group with the configuration edit controls
	 */
	private void createConfigListGroup(Composite parent) {
		// Create the config list group area
		final Group configListGroup = new Group(parent, SWT.NONE);
		configListGroup.setFont(parent.getFont());
		configListGroup.setText(ManagedBuilderUIMessages.getResourceString(CONFIGS));
		configListGroup.setLayout(new GridLayout(1, false));
		configListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		// Create the current config List
		currentConfigList = new List(configListGroup, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		currentConfigList.setFont(configListGroup.getFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = (IDialogConstants.ENTRY_FIELD_WIDTH);
		currentConfigList.setLayoutData(data);
		currentConfigList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				currentConfigList = null;
			}
		});
		currentConfigList.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleConfigSelection();
			}
		});
		
//		 Create a composite for the conversion target combo		
		final Composite conversionGroup = new Composite(configListGroup, SWT.NULL);
		conversionGroup.setFont(configListGroup.getFont());
		conversionGroup.setLayout(new GridLayout(2, true));
		conversionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the Tool chain conversion target list
		Label conversionTargetLabel = ControlFactory.createLabel(conversionGroup, ManagedBuilderUIMessages.getResourceString(CONVERSION_TARGET_LABEL));
		conversionTargetSelector = new Combo(conversionGroup, SWT.READ_ONLY|SWT.DROP_DOWN);
		conversionTargetSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleConversionTargetSelection();
			}
		});
		conversionTargetSelector.setToolTipText(ManagedBuilderUIMessages.getResourceString(CONVERSION_TARGET_TIP));
			
		//		 Create a composite for the buttons		
		final Composite buttonBar = new Composite(configListGroup, SWT.NULL);
		buttonBar.setFont(configListGroup.getFont());
		buttonBar.setLayout(new GridLayout(4, true));
		buttonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		convertTargetBtn = ControlFactory.createPushButton(buttonBar, ManagedBuilderUIMessages.getResourceString(CONVERT_TARGET));
		convertTargetBtn.setToolTipText(ManagedBuilderUIMessages.getResourceString(CONVERT_TIP));
		convertTargetBtn.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleConversionTargetSelection();
			}
		});
		
		newBtn = new Button(buttonBar, SWT.PUSH);
		newBtn.setFont(buttonBar.getFont());
		newBtn.setText(ManagedBuilderUIMessages.getResourceString(NEW));
		setButtonLayoutData(newBtn);
		newBtn.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleNewPressed();
			}
		});
		newBtn.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				newBtn = null;				
			}
		});
		
		removeBtn = new Button(buttonBar, SWT.PUSH);
		removeBtn.setFont(buttonBar.getFont());
		removeBtn.setText(ManagedBuilderUIMessages.getResourceString(REMOVE));
		setButtonLayoutData(removeBtn);
		removeBtn.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleRemovePressed();
			}
		});
		removeBtn.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				removeBtn = null;				
			}
		});

		renameBtn = new Button(buttonBar, SWT.PUSH);
		renameBtn.setFont(buttonBar.getFont());
		renameBtn.setText(ManagedBuilderUIMessages.getResourceString(RENAME));
		setButtonLayoutData(renameBtn);
		renameBtn.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleRenamePressed();
			}
		});
		renameBtn.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				renameBtn = null;				
			}
		});

	}
	
	private void handleConversionTargetSelection() {
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Make the configuration management area
		createConfigListGroup(comp);
		
		// Do the final widget prep
		// Set the configuration items
		currentConfigList.setItems(getConfigurationNamesAndDescriptions());
		
		
		//	Set the selection to selectedConfiguration.
		String name = getSelectedConfiguration().getName();
		String description = getSelectedConfiguration().getDescription();
		String nameAndDescription = new String();
		
		if ( description == null || description.equals("") ) {	//$NON-NLS-1$
			nameAndDescription = name;
		} else {
			nameAndDescription = name + "( " + description + " )";	//$NON-NLS-1$ //$NON-NLS-2$
		}	
		currentConfigList.select( currentConfigList.indexOf(nameAndDescription));	
		
		// Set the conversion target list.
		updateConversionTargets(getSelectedConfiguration());
		newBtn.setFocus();
		return comp;
	}

	private String [] getConversionTargetList(IConfiguration config) {
		// This is temporary, once I implement backend & converter extension point,
		// this will be replaced with original code.
		String [] conversionTargetNames = { "GNU Toolchain version 4.0"};	//$NON-NLS-1$ 
		return conversionTargetNames;
	}
	
	private void updateConversionTargets(IConfiguration config) {
		conversionTargetSelector.setItems( getConversionTargetList(config));
		conversionTargetSelector.select(0);
		conversionTargetSelector.setEnabled(conversionTargetSelector.getItemCount() > 1);
		convertTargetBtn.setEnabled( conversionTargetSelector.getItemCount() > 1);
	}
	
	private String [] getConfigurationNamesAndDescriptions() {
		String [] namesAndDescriptions = (String[]) getExistingConfigs().keySet().toArray(new String[getExistingConfigs().size()]);
		
		return namesAndDescriptions;
	}

	/* (non-javadoc)
	 * Answers a <code>SortedMap</code> of <code>IConfiguration</code> names to unique IDs. 
	 * 
	 * @return 
	 */
	protected SortedMap getDeletedConfigs() {
		if (deletedConfigs == null) {
			deletedConfigs = new TreeMap(); 
		}
		return deletedConfigs;
	}

	/**
	 * Answers a <code>List</code> of unique IDs corresponding to the <code>IConfigurations</code> 
	 * the user wishes to remove from the <code>ITarget</code>
	 * @return
	 */
	public ArrayList getDeletedConfigIds() {
		return new ArrayList(getDeletedConfigs().values());
	}

	protected SortedMap getExistingConfigs() {
		if (existingConfigs == null) {
			existingConfigs = new TreeMap(); 
		}
		return existingConfigs;
	}
	
	
	/*
	 * @return the <code>IProject</code> associated with the managed project
	 */
	private IProject getProject() {
		return (IProject)managedProject.getOwner();
	}
	
	/*
	 * Event handler for the add button
	 */
	protected void handleNewPressed() {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		// Pop-up a dialog to properly handle the request
		NewConfigurationDialog dialog = new NewConfigurationDialog(getShell(), 
																   managedProject, 
																   ManagedBuilderUIMessages.getResourceString(NEW_CONF_DLG));
		if (dialog.open() == NewConfigurationDialog.OK) {
			// Get the new name & description and configuration to base the new config on
			String newConfigName = dialog.getNewName(); 
			String newConfigDescription = dialog.getNewDescription();
			IConfiguration parentConfig = dialog.getParentConfiguration();
			
			if (parentConfig != null) {
				int id = ManagedBuildManager.getRandomNumber();
				
				// Create ID for the new component based on the parent ID and random component
				String newId = parentConfig.getId();
				int index = newId.lastIndexOf(ID_SEPARATOR);
				if (index > 0) {
					String lastComponent = newId.substring(index + 1, newId.length());
					if (Character.isDigit(lastComponent.charAt(0))) {
						// Strip the last component
						newId = newId.substring(0, index);
					}
				}
				newId += ID_SEPARATOR + id;
				IConfiguration newConfig;
				if (parentConfig.isExtensionElement()) {
					newConfig = info.getManagedProject().createConfiguration(parentConfig, newId);
				} else {
					newConfig = info.getManagedProject().createConfigurationClone(parentConfig, newId);
				}
				
				newConfig.setName(newConfigName);
				newConfig.setDescription(newConfigDescription);
				newConfig.setArtifactName(info.getManagedProject().getDefaultArtifactName());
				
				// Add this new configuration to the existing list.
				String nameAndDescription = new String();
				
				if ( newConfigDescription == null || newConfigDescription.equals("") ) {	//$NON-NLS-1$
					nameAndDescription = newConfigName;
				} else {
					nameAndDescription = newConfigName + "( " + newConfigDescription + " )";	//$NON-NLS-1$	//$NON-NLS-2$
				}
				
				// Add the newConfig to the existing configurations
				getExistingConfigs().put(nameAndDescription, newConfig);
				
				// Set the selected Configuration to the newConfig
				setSelectedConfiguration(newConfig);
				
				// Update the Configuration combo list that is displayed to the user.
				currentConfigList.setItems(getConfigurationNamesAndDescriptions());
				
				// Get the index of selected configuration & set selection in config list.
				int configIndex = currentConfigList.indexOf(nameAndDescription);
				currentConfigList.setSelection(configIndex);
			}
						
		}

		// Update the buttons based on the choices		
		updateButtons();
	}

	
	protected void handleRenamePressed() {
		IConfiguration selectedConfig = null;
		String selectedConfigNameAndDescription = null;

		// Determine which configuration was selected
		int selectionIndex = currentConfigList.getSelectionIndex();
		if (selectionIndex != -1) {
			selectedConfigNameAndDescription = currentConfigList
					.getItem(selectionIndex);
			selectedConfig = (IConfiguration) getExistingConfigs().get(
					selectedConfigNameAndDescription);

			// Pop-up a dialog to properly handle the request
			RenameConfigurationDialog dialog = new RenameConfigurationDialog(
					getShell(), managedProject, selectedConfig,
					ManagedBuilderUIMessages.getResourceString(RENAME_CONF_DLG));
			if (dialog.open() == RenameConfigurationDialog.OK) {
				// Get the new name & description for the selected configuration
				String newConfigName = dialog.getNewName();

				String newConfigDescription = dialog.getNewDescription();

				selectedConfig.setName(newConfigName);
				selectedConfig.setDescription(newConfigDescription);

				// Remove the old configuration from the list and add renamed
				// configuration to the list.
				getExistingConfigs().remove(selectedConfigNameAndDescription);

				String nameAndDescription = new String();

				if (newConfigDescription == null
						|| newConfigDescription.equals("")) {	//$NON-NLS-1$
					nameAndDescription = newConfigName;
				} else {
					nameAndDescription = newConfigName + "( "	//$NON-NLS-1$
							+ newConfigDescription + " )";	//$NON-NLS-1$
				}
				getExistingConfigs().put(nameAndDescription, selectedConfig);
			
				// Set the selected Configuration to the newConfig
				setSelectedConfiguration(selectedConfig);
				
				// Update the Configuration combo list that is displayed to the user.
				currentConfigList.setItems(getConfigurationNamesAndDescriptions());
				
				// Get the index of selected configuration & set selection in config list.
				int configIndex = currentConfigList.indexOf(nameAndDescription);
				currentConfigList.setSelection(configIndex);
				
				//	Update the buttons based on the choices
				updateButtons();
			}
		}
	}

	/*
	 * (non-javadoc) Event handler for the remove button
	 */
	protected void handleRemovePressed() {
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		
		// Determine which configuration was selected
		int selectionIndex = currentConfigList.getSelectionIndex();
		if (selectionIndex != -1){
			String selectedConfigNameAndDescription = currentConfigList.getItem(selectionIndex);
			
			// Get the confirmation from user before deleting the configuration
			Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
			boolean shouldDelete = MessageDialog.openQuestion(shell,
			        ManagedBuilderUIMessages.getResourceString("ManageConfig.deletedialog.title"), //$NON-NLS-1$
			        ManagedBuilderUIMessages.getFormattedString("ManageConfig.deletedialog.message",  //$NON-NLS-1$
			                new String[] {selectedConfigNameAndDescription}));
			if (shouldDelete) {
				IConfiguration selectedConfig = (IConfiguration) getExistingConfigs()
						.get(selectedConfigNameAndDescription);
				String selectedConfigId = (String) selectedConfig.getId();
				getDeletedConfigs().put(selectedConfigNameAndDescription,
						selectedConfigId);

				// Remove the configurations from the project & from list
				// configuration list
				info.getManagedProject().removeConfiguration(selectedConfigId);
				getExistingConfigs().remove(selectedConfigNameAndDescription);

				// Update the Configuration combo list that is displayed to the
				// user.
				currentConfigList
						.setItems(getConfigurationNamesAndDescriptions());
				currentConfigList
						.setSelection(currentConfigList.getItemCount() - 1);

				// Update selected configuration variable
				selectionIndex = currentConfigList.getSelectionIndex();
				if (selectionIndex != -1) {
					selectedConfigNameAndDescription = currentConfigList
							.getItem(selectionIndex);
					selectedConfig = (IConfiguration) getExistingConfigs().get(
							selectedConfigNameAndDescription);
					setSelectedConfiguration(selectedConfig);
				}
				// Clean up the UI lists
				updateButtons();
			}
		}
	}

	private void updateButtons() {
		// Disable the remove button if there is only 1 configuration
		removeBtn.setEnabled(currentConfigList.getItemCount() > 1);
		convertTargetBtn.setEnabled( conversionTargetSelector.getItemCount() > 1);
	}

	private void handleConfigSelection() {
		// Determine which configuration was selected
		int selectionIndex = currentConfigList.getSelectionIndex();

		String selectedConfigNameAndDescription = currentConfigList
				.getItem(selectionIndex);

		IConfiguration selectedConfig = (IConfiguration) getExistingConfigs()
				.get(selectedConfigNameAndDescription);
		setSelectedConfiguration(selectedConfig);

		updateConversionTargets(selectedConfig);
		return;
	}
	
	public IConfiguration getSelectedConfiguration() {
		return selectedConfiguration;
	}

	public void setSelectedConfiguration(IConfiguration selectedConfiguration) {
		this.selectedConfiguration = selectedConfiguration;
	}
}
