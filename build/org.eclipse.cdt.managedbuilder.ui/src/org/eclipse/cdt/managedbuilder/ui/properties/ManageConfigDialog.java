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
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public class ManageConfigDialog extends Dialog {
	// String constants
	private static final String PREFIX = "BuildPropertyCommon";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String NEW = LABEL + ".new";	//$NON-NLS-1$
	private static final String REMOVE = LABEL + ".remove";	//$NON-NLS-1$
	private static final String CONFIGS = LABEL + ".configs";	//$NON-NLS-1$
	
	// Default return values
	private static final ArrayList EMPTY_LIST = new ArrayList(0);
	private static final SortedMap EMPTY_MAP = new TreeMap();

	// The title of the dialog.
	private String title = "";
	// The target the configs belong to
	private ITarget managedTarget;
	// The list of configurations to delete
	private ArrayList deletedConfigIds;
	// Map of configuration names and ids
	private SortedMap configIds;
	// Map of new configurations chosen by the user
	private SortedMap newConfigs;
	
	// Widgets
	private List configurationList;
	private Button newBtn;
	private Button okBtn;
	private Button removeBtn;
	
	/**
	 * @param parentShell
	 */
	protected ManageConfigDialog(Shell parentShell, String title, ITarget target) {
		super(parentShell);
		this.title = title;
		this.managedTarget = target;
		
		// Get the defined configurations from the target
		IConfiguration [] configs = this.managedTarget.getConfigurations();
		configIds = new TreeMap();
		for (int i = 0; i < configs.length; i++) {
			IConfiguration configuration = configs[i];
			configIds.put(configuration.getName(), configuration.getId());
		}
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
		// Create the main composite with a 2-column grid layout
		Composite composite = ControlFactory.createComposite(parent, 3);
		
		// Create a list
		Composite listComp = ControlFactory.createComposite(composite, 1);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		listComp.setLayoutData(gd);
		Label label = ControlFactory.createLabel(listComp, CUIPlugin.getResourceString(CONFIGS));
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		configurationList = new List(listComp, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 15;
		configurationList.setLayoutData(gd);
		
		// Create a composite for the buttons		
		Composite buttonBar = ControlFactory.createComposite(composite, 1);
		newBtn = ControlFactory.createPushButton(buttonBar, CUIPlugin.getResourceString(NEW));
		setButtonLayoutData(newBtn);
		newBtn.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleNewPressed();
			}
		});
		removeBtn = ControlFactory.createPushButton(buttonBar, CUIPlugin.getResourceString(REMOVE));
		setButtonLayoutData(removeBtn);
		removeBtn.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleRemovePressed();
			}
		});
		
		// Do the final widget prep
		configurationList.setItems(getConfigurationNames());
		configurationList.select(0);
		newBtn.setFocus();
		return composite;
	}
	
	private String [] getConfigurationNames() {
		return (String[]) configIds.keySet().toArray(new String[configIds.size()]);
	}

	/**
	 * @return <code>ArrayList</code> of <code>IConfiguration</code> ids 
	 * the user has decided to remove from the target.
	 */
	public ArrayList getDeletedConfigs() {
		if (deletedConfigIds == null) {
			deletedConfigIds = EMPTY_LIST; 
		}
		return deletedConfigIds;
	}
	
	/**
	 * @return Map of configuration names to <code>IConfiguration</code>.
	 * The name is selected by the user and should be unique for the target 
	 * it will be added to. The configuration is the what the new 
	 * configuration will be based on.   
	 */
	public SortedMap getNewConfigs() {
		if (newConfigs == null) {
			newConfigs = EMPTY_MAP;
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
			if (target.getId().equals(managedTarget.getId())) {
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
			NewConfigurationDialog dialog = new NewConfigurationDialog(getShell(), allDefinedConfigs, managedTarget);
			if (dialog.open() == NewConfigurationDialog.OK) {
				// Get the new name and configuration to base the new config on
				getNewConfigs().put(dialog.getNewName(), dialog.getParentConfiguration());
			}
		}

		// Update the buttons based on the choices		
		updateButtons();
	}

	/*
	 * Event handler for the remove button 
	 */
	protected void handleRemovePressed() {
		// TODO Request a remove configuration function through the ITarget interface
		// Determine which configuration was selected
		int selectionIndex = configurationList.getSelectionIndex();
		if (selectionIndex != -1){
			String selectedConfig = configurationList.getItem(selectionIndex);
			getDeletedConfigs().add(configIds.get(selectedConfig));
			configurationList.remove(selectionIndex);
			updateButtons();
		}
	}

	private void updateButtons() {
		// Disable the remove button if there is only 1 configuration
//		removeBtn.setEnabled(configurationList.getItemCount() > 1);
		removeBtn.setEnabled(false);
		
		// Enable the OK button if there are any configs to delete or add
		okBtn.setEnabled(!(getDeletedConfigs().isEmpty() && getNewConfigs().isEmpty()));
	}


}
