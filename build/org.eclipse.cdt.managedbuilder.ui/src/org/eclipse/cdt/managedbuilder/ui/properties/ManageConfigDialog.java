package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ManageConfigDialog extends Dialog {
	// String constants
	private static final String CMN_PREFIX = "BuildPropertyCommon";	//$NON-NLS-1$
	private static final String CMN_LABEL = CMN_PREFIX + ".label";	//$NON-NLS-1$
	private static final String NEW = CMN_LABEL + ".new";	//$NON-NLS-1$
	private static final String REMOVE = CMN_LABEL + ".remove";	//$NON-NLS-1$
	private static final String PREFIX = "ManageConfig";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String RESTORE = LABEL + ".restore";	//$NON-NLS-1$
	private static final String GROUP = LABEL + ".makecmdgroup";	//$NON-NLS-1$
	private static final String DEF_BTN = LABEL + ".makecmddef";	//$NON-NLS-1$
	private static final String OUTPUT_GROUP = LABEL + ".output.group";	//$NON-NLS-1$
	private static final String OUTPUT_LABEL = LABEL + ".output.label";	//$NON-NLS-1$
	private static final String CONFIGS = LABEL + ".configs";	//$NON-NLS-1$
	private static final String CURRENT_CONFIGS = CONFIGS + ".current";	//$NON-NLS-1$
	private static final String DELETED_CONFIGS = CONFIGS + ".deleted";	//$NON-NLS-1$
	private static final String CONF_DLG = LABEL + ".new.config.dialog";	//$NON-NLS-1$

	// The name of the build artifact
	private String buildArtifact;
	// The list of configurations to delete
	private SortedMap deletedConfigs;
	// Map of configuration names and ids
	private SortedMap existingConfigs;
	// The make command associated with the target	
	private String makeCommand;
	// The target the configs belong to
	private ITarget managedTarget;
	// Map of new configurations chosen by the user
	private SortedMap newConfigs;
	// The title of the dialog.
	private String title = "";
	// State of the check box on exit
	private boolean useDefaultMake;
	
	// Widgets
	protected Text buildArtifactEntry;
	protected List currentConfigList;
	protected List deletedConfigList;
	protected Button makeCommandDefault;
	protected Text makeCommandEntry;
	protected Button newBtn;
	protected Button okBtn;
	protected Button removeBtn;
	protected Button restoreBtn;
	
	/**
	 * @param parentShell
	 */
	protected ManageConfigDialog(Shell parentShell, String title, ITarget target) {
		super(parentShell);
		this.title = title;
		this.managedTarget = target;
		
		// Figure out the default make command
		makeCommand = managedTarget.getMakeCommand();
		
		// Get the name of the build artifact
		buildArtifact = managedTarget.getArtifactName();
		
		// Get the defined configurations from the target
		getExistingConfigs().clear();
		IConfiguration [] configs = managedTarget.getConfigurations();
		for (int i = 0; i < configs.length; i++) {
			IConfiguration configuration = configs[i];
			getExistingConfigs().put(configuration.getName(), configuration.getId());
		}
		
		getDeletedConfigs().clear();
		getNewConfigs().clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			useDefaultMake = makeCommandDefault.getSelection();
			makeCommand = makeCommandEntry.getText().trim();
			buildArtifact = buildArtifactEntry.getText().trim();
		} else {
			useDefaultMake = true;
			buildArtifact = managedTarget.getArtifactName();
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okBtn = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		updateButtons();
	}

	protected Control createDialogArea(Composite parent) {
		Composite comp = ControlFactory.createComposite(parent, 1);
		
		// Create a group for the build output
		Group outputGroup = ControlFactory.createGroup(comp, ManagedBuilderUIPlugin.getResourceString(OUTPUT_GROUP), 1);
		outputGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label outputLabel = ControlFactory.createLabel(outputGroup, ManagedBuilderUIPlugin.getResourceString(OUTPUT_LABEL));
		outputLabel.setLayoutData(new GridData());
		buildArtifactEntry = ControlFactory.createTextField(outputGroup);
		buildArtifactEntry.setText(buildArtifact);
		buildArtifactEntry.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				buildArtifactEntry = null;
			}
		});

		// Create the make command group area
		Group makeCommandGroup = ControlFactory.createGroup(comp, ManagedBuilderUIPlugin.getResourceString(GROUP), 1);
		GridData gd = new GridData(GridData.FILL_BOTH);
		makeCommandGroup.setLayoutData(gd);
		makeCommandDefault = ControlFactory.createCheckBox(makeCommandGroup, ManagedBuilderUIPlugin.getResourceString(DEF_BTN));
		setButtonLayoutData(makeCommandDefault);
		makeCommandDefault.setSelection(!managedTarget.hasOverridenMakeCommand());
		makeCommandDefault.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleUseDefaultPressed();
			}
		});
		makeCommandDefault.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				makeCommandDefault = null;
			}
		});
		makeCommandEntry = ControlFactory.createTextField(makeCommandGroup);
		makeCommandEntry.setEditable(!makeCommandDefault.getSelection());
		makeCommandEntry.setText(makeCommand);
		makeCommandEntry.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				makeCommandEntry = null;
			}
		});
		
		
		// Create the config list group area
		Group configListGroup = ControlFactory.createGroup(comp, ManagedBuilderUIPlugin.getResourceString(CONFIGS), 3);
		gd = new GridData(GridData.FILL_BOTH);
		configListGroup.setLayoutData(gd);

		// Create the 2 labels first to align the buttons and list controls
		Label currentConfigLabel = ControlFactory.createLabel(configListGroup, ManagedBuilderUIPlugin.getResourceString(CURRENT_CONFIGS));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		currentConfigLabel.setLayoutData(gd);
		Label deletedConfigLabel = ControlFactory.createLabel(configListGroup, ManagedBuilderUIPlugin.getResourceString(DELETED_CONFIGS));
		deletedConfigLabel.setLayoutData(new GridData());
		
		// Create the current config list
		Composite currentComp = ControlFactory.createComposite(configListGroup, 1);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		currentComp.setLayoutData(gd);
		currentConfigList = new List(currentComp, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		currentConfigList.setLayoutData(gd);
		currentConfigList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				currentConfigList = null;
			}
		});
		
		// Create a composite for the buttons		
		Composite buttonBar = ControlFactory.createComposite(configListGroup, 1);
		buttonBar.setLayoutData(new GridData());

		newBtn = ControlFactory.createPushButton(buttonBar, ManagedBuilderUIPlugin.getResourceString(NEW));
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
		removeBtn = ControlFactory.createPushButton(buttonBar, ManagedBuilderUIPlugin.getResourceString(REMOVE));
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
		restoreBtn = ControlFactory.createPushButton(buttonBar, ManagedBuilderUIPlugin.getResourceString(RESTORE));
		setButtonLayoutData(restoreBtn);
		restoreBtn.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleRestorePressed();
			}
		});
		restoreBtn.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				restoreBtn = null;				
			}
		});

		// Create the deleted config list
		Composite deletedComp = ControlFactory.createComposite(configListGroup, 1);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		deletedComp.setLayoutData(gd);
		deletedConfigList = new List(deletedComp, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 100;
		deletedConfigList.setLayoutData(gd);
		deletedConfigList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				deletedConfigList = null;
			}
		});

		// Do the final widget prep
		currentConfigList.setItems(getConfigurationNames());
		currentConfigList.select(0);
		newBtn.setFocus();
		return comp;
	}
	
	/**
	 * 
	 */
	protected void handleUseDefaultPressed() {
		// If the state of the button is unchecked, then we want to enable the edit widget
		makeCommandEntry.setEditable(!makeCommandDefault.getSelection());
	}

	/**
	 * Answers the value in the build artifact entry widget.
	 * 
	 * @return
	 */
	public String getBuildArtifactName() {
		return buildArtifact;
	}

	private String [] getConfigurationNames() {
		return (String[]) getExistingConfigs().keySet().toArray(new String[getExistingConfigs().size()]);
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
	
	/**
	 * Answers the value in the make command entry widget.
	 * 
	 * @return
	 */
	public String getMakeCommand() {
		return makeCommand;
	}
	
	/**
	 * Answers a map of configuration names to <code>IConfiguration</code>.
	 * The name is selected by the user and should be unique for the target 
	 * it will be added to. The configuration is the what the new 
	 * configuration will be based on.
	 * 
	 * @return Map   
	 */
	public SortedMap getNewConfigs() {
		if (newConfigs == null) {
			newConfigs = new TreeMap();
		}
		return newConfigs;
	}

	/*
	 * @return the <code>IProject</code> associated with the target
	 */
	private IProject getProject() {
		return managedTarget.getOwner().getProject();
	}
	
	/*
	 * Event handler for the add button
	 */
	protected void handleNewPressed() {
		// Find the defined target
		ITarget parentTarget = null;
		ITarget [] targets = ManagedBuildManager.getDefinedTargets(getProject());
		for (int i = 0; i < targets.length; i++) {
			ITarget target = targets[i];
			if (target.getId().equals(managedTarget.getParent().getId())) {
				parentTarget = target;
				break;
			}
		}
		// Get all the predefined configs
		IConfiguration [] allDefinedConfigs = null;
		if (parentTarget != null) {
			allDefinedConfigs = parentTarget.getConfigurations();
		}
		
		// There should be predefined configurations ....
		if (allDefinedConfigs != null && allDefinedConfigs.length != 0) {
			NewConfigurationDialog dialog = new NewConfigurationDialog(getShell(), 
																	   allDefinedConfigs, 
																	   managedTarget, 
																	   ManagedBuilderUIPlugin.getResourceString(CONF_DLG));
			if (dialog.open() == NewConfigurationDialog.OK) {
				// Get the new name and configuration to base the new config on
				String newConfigName = dialog.getNewName(); 
				getNewConfigs().put(newConfigName, dialog.getParentConfiguration());
				currentConfigList.add(newConfigName);
				currentConfigList.setSelection(currentConfigList.getItemCount() - 1);			
			}
		}

		// Update the buttons based on the choices		
		updateButtons();
	}

	/* (non-javadoc)
	 * Event handler for the remove button 
	 */
	protected void handleRemovePressed() {
		// Determine which configuration was selected
		int selectionIndex = currentConfigList.getSelectionIndex();
		if (selectionIndex != -1){
			String selectedConfigName = currentConfigList.getItem(selectionIndex);
			String selectedConfigId = null;
			
			// If this is a newly added config, remove it from that map
			if (getNewConfigs().containsKey(selectedConfigName)) {
				selectedConfigId = (String) getNewConfigs().get(selectedConfigName);
				getNewConfigs().remove(selectedConfigName);
			}
			
			// If it is not a new item, the ID is in the existing list
			selectedConfigId = (String) getExistingConfigs().get(selectedConfigName);
			getDeletedConfigs().put(selectedConfigName, selectedConfigId);

			// Clean up the UI lists
			currentConfigList.remove(selectionIndex);
			currentConfigList.setSelection(selectionIndex - 1);
			deletedConfigList.add(selectedConfigName);
			deletedConfigList.setSelection(deletedConfigList.getItemCount() - 1);
			updateButtons();
		}
	}

	/* (non-javadoc)
	 * Event handler for the restore button
	 */
	protected void handleRestorePressed() {
		// Determine which configuration was selected
		int selectionIndex = deletedConfigList.getSelectionIndex();
		// Move the selected element from the deleted list to the current list
		if (selectionIndex != -1){
			// Get the name of the item to delete
			String selectedConfigName = deletedConfigList.getItem(selectionIndex);
			String selectedConfigId = (String) getDeletedConfigs().get(selectedConfigName);
			
			// If this was a new config (it won't be in the existing list) then add it back there
			if (!getExistingConfigs().containsKey(selectedConfigName)) {
				getNewConfigs().put(selectedConfigName, selectedConfigId);
			}
			
			// Remove it from the deleted map
			getDeletedConfigs().remove(selectedConfigName);

			// Clean up the UI
			deletedConfigList.remove(selectionIndex);
			deletedConfigList.setSelection(selectionIndex - 1);
			currentConfigList.add(selectedConfigName);
			currentConfigList.setSelection(currentConfigList.getItemCount());
			updateButtons();
		}
	}

	private void updateButtons() {
		// Disable the remove button if there is only 1 configuration
		removeBtn.setEnabled(currentConfigList.getItemCount() > 1);
		// Enable the restore button if there is anything in the deleted list
		restoreBtn.setEnabled(deletedConfigList.getItemCount() > 0);
	}

	public boolean useDefaultMakeCommand () {
		return useDefaultMake;
	}
}
