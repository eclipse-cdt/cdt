package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

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

public class NewConfigurationDialog extends Dialog {
	// String constants
	private static final String PREFIX = "NewConfiguration";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String ERROR = PREFIX + ".error";	//$NON-NLS-1$	
	private static final String NAME = LABEL + ".name";	//$NON-NLS-1$
	private static final String COPY = LABEL + ".copy";	//$NON-NLS-1$
	private static final String TITLE = ERROR + ".title";	//$NON-NLS-1$	
	private static final String DUPLICATE = ERROR + ".duplicateName";	//$NON-NLS-1$	

	// Widgets
	private Combo configSelector;
	private Button btnOk;
	private Text configName;
		
	// Bookeeping
	private IConfiguration[] definedConfigurations;
	private IConfiguration parentConfig;
	private ITarget target;
	private String newName;
	private String [] allNames;
	private String title = "";

	
	/**
	 * @param parentShell
	 */
	protected NewConfigurationDialog(Shell parentShell, IConfiguration[] configs, ITarget managedTarget, String title) {
		super(parentShell);
		this.title = title;
		setShellStyle(getShellStyle()|SWT.RESIZE);
		newName = new String();
		parentConfig = null;
		definedConfigurations = configs == null ? new IConfiguration[0] : configs;
		allNames = getConfigurationNames();
		this.target = managedTarget;
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog. Cache the name and base config selections.
	 * We don't have to worry that the index or name is wrong because we 
	 * enable the OK button IFF those conditions are met.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			newName = configName.getText().trim();
			String baseConfigName = configSelector.getItem(configSelector.getSelectionIndex());
			for (int i = 0; i < definedConfigurations.length; i++) {
				IConfiguration config = definedConfigurations[i];
				if (config.getName().equals(baseConfigName)) {
					parentConfig = config;
					break;				
				}
			}
		} else {
			newName = null;
			parentConfig = null;
		}
		super.buttonPressed(buttonId);
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
		btnOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		configName.setFocus();
		if (configName != null) {
			configName.setText(newName);
		}
		updateButtonState();
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 3);
		GridData gd;
		
		// Add a label and a text widget
		Label nameLabel = ControlFactory.createLabel(composite, ManagedBuilderUIPlugin.getResourceString(NAME));
		gd = new GridData();
		gd.horizontalSpan = 1;
		nameLabel.setLayoutData(gd);
		configName = ControlFactory.createTextField(composite);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		configName.setLayoutData(gd);
		configName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonState();
			}
		});
		
		// Add a label and combo box to select the base config
		Label configLabel = ControlFactory.createLabel(composite, ManagedBuilderUIPlugin.getResourceString(COPY));
		gd = new GridData();
		gd.horizontalSpan = 1;
		configLabel.setLayoutData(gd);
		configSelector = ControlFactory.createSelectCombo(composite, allNames, newName);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		configSelector.setLayoutData(gd);
		configSelector.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtonState();		
			}
		});	
		
		return composite;
	}

	/**
	 * @return the <code>IConfiguration</code> the user selected as 
	 * the parent of the new configuration.
	 */
	public IConfiguration getParentConfiguration() {
		return parentConfig;
	}

	/*
	 * Returns an array of configuration names
	 */
	private String [] getConfigurationNames() {
		String [] names = new String[definedConfigurations.length];
		for (int index = 0; index < definedConfigurations.length; ++index) {
			IConfiguration config = definedConfigurations[index];
			names[index] = config.getName();
		}
		return names; 
	}
	
	/**
	 * @return <code>String</code> containing the name chosen by the user for the
	 * new configuration.
	 */
	public String getNewName() {
		return newName;
	}

	protected boolean isDuplicateName(String newName) {
		// Return true if there is already a config of that name defined on the target
		IConfiguration [] configs = target.getConfigurations();
		for (int index = 0; index < configs.length; index++) {
			IConfiguration configuration = configs[index];
			if (configuration.getName() == newName) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Enable the OK button if there is a valid name in the text widget
	 * and there is a valid selection in the base configuration combo 
	 */
	private void updateButtonState() {
		if (btnOk != null) {
			int selectionIndex = configSelector.getSelectionIndex();			
			btnOk.setEnabled(validateName() && selectionIndex != -1);
		}
	}
	
	private boolean validateName() {
		String currentName = configName.getText().trim(); 
		int nameLength = currentName.length();
		// Make sure the name is not a duplicate
		if (isDuplicateName(currentName)) {
			MessageDialog.openError(getShell(), 
			ManagedBuilderUIPlugin.getResourceString(TITLE), 
			ManagedBuilderUIPlugin.getFormattedString(DUPLICATE, currentName)); //$NON-NLS-1$
			return false;
		}
		// TODO make sure there are no invalid chars in name
		return (nameLength > 0);
	}
}
