/**********************************************************************
 * Copyright (c) 2002,2004 IBM Corporation and others.
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

import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public class ManageConfigDialog extends Dialog {
	// String constants
	private static final String CMN_PREFIX = "BuildPropertyCommon";	//$NON-NLS-1$
	private static final String CMN_LABEL = CMN_PREFIX + ".label";	//$NON-NLS-1$
	private static final String NEW = CMN_LABEL + ".new";	//$NON-NLS-1$
	private static final String REMOVE = CMN_LABEL + ".remove";	//$NON-NLS-1$
	private static final String PREFIX = "ManageConfig";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String RESTORE = LABEL + ".restore";	//$NON-NLS-1$
	private static final String CONFIGS = LABEL + ".configs";	//$NON-NLS-1$
	private static final String CURRENT_CONFIGS = CONFIGS + ".current";	//$NON-NLS-1$
	private static final String DELETED_CONFIGS = CONFIGS + ".deleted";	//$NON-NLS-1$
	private static final String CONF_DLG = LABEL + ".new.config.dialog";	//$NON-NLS-1$

	private static final String EMPTY_STRING = new String();

	// The list of configurations to delete
	private SortedMap deletedConfigs;
	// Map of configuration names and ids
	private SortedMap existingConfigs;
	// The target the configs belong to
	private IManagedProject managedProject;
	/** All new configs added by the user but not yet part of target */
	private SortedMap newAddedConfigs;
	/** All new configs removed by the user but not yet part of target */
	private SortedMap removedNewConfigs;
	// The title of the dialog.
	private String title = ""; //$NON-NLS-1$
	
	// Widgets
	protected List currentConfigList;
	protected List deletedConfigList;
	protected Button newBtn;
	protected Button okBtn;
	protected Button removeBtn;
	protected Button restoreBtn;
	
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
			getExistingConfigs().put(configuration.getName(), configuration.getId());
		}
		
		getDeletedConfigs().clear();
		getNewConfigs().clear();
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
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

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
		configListGroup.setLayout(new GridLayout(3, false));
		configListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the 2 labels first to align the buttons and list controls
		final Label currentConfigLabel = new Label(configListGroup, SWT.LEFT);
		currentConfigLabel.setFont(configListGroup.getFont());
		currentConfigLabel.setText(ManagedBuilderUIMessages.getResourceString(CURRENT_CONFIGS));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		currentConfigLabel.setLayoutData(data);
		final Label deletedConfigLabel = new Label(configListGroup, SWT.LEFT);
		deletedConfigLabel.setFont(configListGroup.getFont());
		deletedConfigLabel.setText(ManagedBuilderUIMessages.getResourceString(DELETED_CONFIGS));
		deletedConfigLabel.setLayoutData(new GridData());
		
		// Create the current config List
		currentConfigList = new List(configListGroup, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		currentConfigList.setFont(configListGroup.getFont());
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = (IDialogConstants.ENTRY_FIELD_WIDTH / 2);
		currentConfigList.setLayoutData(data);
		currentConfigList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				currentConfigList = null;
			}
		});
		
		// Create a composite for the buttons		
		final Composite buttonBar = new Composite(configListGroup, SWT.NULL);
		buttonBar.setFont(configListGroup.getFont());
		buttonBar.setLayout(new GridLayout(1, true));
		buttonBar.setLayoutData(new GridData(GridData.FILL_VERTICAL));

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

		restoreBtn = new Button(buttonBar, SWT.PUSH);
		restoreBtn.setFont(buttonBar.getFont());
		restoreBtn.setText(ManagedBuilderUIMessages.getResourceString(RESTORE));
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

		//Create the deleted config list
		deletedConfigList = new List(configListGroup, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		deletedConfigList.setFont(configListGroup.getFont());
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = (IDialogConstants.ENTRY_FIELD_WIDTH / 2);
		deletedConfigList.setLayoutData(data);
		deletedConfigList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				deletedConfigList = null;
			}
		});
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
		currentConfigList.setItems(getConfigurationNames());
		currentConfigList.select(0);
		newBtn.setFocus();
		return comp;
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
	 * Answers a map of configuration names to <code>IConfiguration</code>.
	 * The name is selected by the user and should be unique for the target 
	 * it will be added to. The configuration is the what the new 
	 * configuration will be based on.
	 * 
	 * @return Map   
	 */
	public SortedMap getNewConfigs() {
		if (newAddedConfigs == null) {
			newAddedConfigs = new TreeMap();
		}
		return newAddedConfigs;
	}

	// Answers a list of new configuration names that have been added-- 
	// or added and removed--by the user, but that have not yet been added 
	// to the target
	private ArrayList getNewConfigNames() {
		ArrayList names = new ArrayList();
		names.addAll(getNewConfigs().keySet());
		names.addAll(getRemovedNewConfigs().keySet());
		return names;
	}
	
	
	// This data structure hangs on to a new configuration that is added
	// by the user, then removed before it is added to the target. This is 
	// a required bookeeping step because the user may change their minds and 
	// restore the deleted configuration. 
	private SortedMap getRemovedNewConfigs() {
		if (removedNewConfigs == null) {
			removedNewConfigs = new TreeMap();
		}
		return removedNewConfigs;
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
		// Pop-up a dialog to properly handle the request
		NewConfigurationDialog dialog = new NewConfigurationDialog(getShell(), 
																   managedProject, 
																   getNewConfigNames(),
																   ManagedBuilderUIMessages.getResourceString(CONF_DLG));
		if (dialog.open() == NewConfigurationDialog.OK) {
			// Get the new name and configuration to base the new config on
			String newConfigName = dialog.getNewName(); 
			getNewConfigs().put(newConfigName, dialog.getParentConfiguration());
			currentConfigList.add(newConfigName);
			currentConfigList.setSelection(currentConfigList.getItemCount() - 1);			
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
			
			// If this is a newly added config, remove it from the new map 
			// and add it to a special map to support the restore use case
			if (getNewConfigs().containsKey(selectedConfigName)) {
				IConfiguration selectedConfig = (IConfiguration) getNewConfigs().get(selectedConfigName); 
				getRemovedNewConfigs().put(selectedConfigName, selectedConfig);
				getNewConfigs().remove(selectedConfigName);
			} else {
				// If it is not a new item, the ID is in the existing list
				String selectedConfigId = (String) getExistingConfigs().get(selectedConfigName);
				getDeletedConfigs().put(selectedConfigName, selectedConfigId);
			}

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
		// Move the selected element from the correct deleted list to the current list
		if (selectionIndex != -1){
			// Get the name of the item to delete
			String selectedConfigName = deletedConfigList.getItem(selectionIndex);
			
			// The deleted config may be one of the existing configs or one of the
			// new configs that have not been added to the target yet
			if (getRemovedNewConfigs().containsKey(selectedConfigName)) {
				IConfiguration restoredConfig = managedProject.getConfiguration(selectedConfigName);
				getNewConfigs().put(selectedConfigName, restoredConfig);
				getRemovedNewConfigs().remove(selectedConfigName);
			} else {
				getDeletedConfigs().remove(selectedConfigName);
			}
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
}
