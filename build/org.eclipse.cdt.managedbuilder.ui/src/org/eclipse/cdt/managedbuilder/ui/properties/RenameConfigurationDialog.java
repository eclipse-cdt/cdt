/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.cdt.internal.ui.dialogs.StatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RenameConfigurationDialog extends StatusDialog {
	// String constants
	private static final String PREFIX = "RenameConfiguration";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String ERROR = PREFIX + ".error";	//$NON-NLS-1$	
	private static final String NAME = LABEL + ".name";	//$NON-NLS-1$
	private static final String DUPLICATE = ERROR + ".duplicateName";	//$NON-NLS-1$	
	private static final String CASE = ERROR + ".caseName";	//$NON-NLS-1$	
	private static final String INVALID = ERROR + ".invalidName";	//$NON-NLS-1$	
	private static final String DESCRIPTION = LABEL + ".description"; //$NON-NLS-1$
	
	// Widgets
	
	private Text configName;
	private Text configDescription;
		
	private IConfiguration[] definedConfigs;
	private IConfiguration renameConfig;
	private IManagedProject managedProject;
	private String newName;
	private String newDescription;
	
	private String originalName;
	/** A list containing config names that have been defined but not added to the target */
	final private ArrayList reservedNames;
	final private String title;

	
	/**
	 * @param parentShell
	 * @param managedTarget
	 * @param renameConfig   
	 * @param title
	 */
	protected RenameConfigurationDialog(Shell parentShell, IManagedProject managedProject, IConfiguration renameConfig, String title) {
		super(parentShell);
		this.title = title;
		this.renameConfig = renameConfig;
		setShellStyle(getShellStyle()|SWT.RESIZE);
		newName = renameConfig.getName();
		newDescription = renameConfig.getDescription();
		if(newDescription == null)
			newDescription = new String();
		
		// Store the original name
		setOriginalName(renameConfig.getName());
		
		this.managedProject = managedProject;
		reservedNames = new ArrayList();
		
		definedConfigs = managedProject.getConfigurations();
//		 Get the defined configuration names
		for (int i = 0; i < definedConfigs.length; i++) {
			reservedNames.add(definedConfigs[i].getName());
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog. Cache the name and base config selections.
	 * We don't have to worry that the index or name is wrong because we 
	 * enable the OK button IFF those conditions are met.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			newName = configName.getText().trim();
			newDescription = configDescription.getText().trim();
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
		super.createButtonsForButtonBar(parent);
		configName.setFocus();
		if (configName != null) {
			configName.setText(newName);
		}
		validateState();
	}

	protected Control createDialogArea(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Create a group for the name & description

		final Group group1 = new Group(composite, SWT.NONE);
		group1.setFont(composite.getFont());
		GridLayout layout1 = new GridLayout(3, false);
		group1.setLayout(layout1);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		group1.setLayoutData(gd);

		// Add a label and a text widget for Configuration's name
		final Label nameLabel = new Label(group1, SWT.LEFT);
		nameLabel.setFont(parent.getFont());
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(NAME));
				
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = false;
		nameLabel.setLayoutData(gd);

		configName = new Text(group1, SWT.SINGLE | SWT.BORDER);
		configName.setFont(group1.getFont());
		configName.setText(getNewName());
		configName.setFocus();
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		configName.setLayoutData(gd);
		configName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateState();
			}
		});
		
//		 Add a label and a text widget for Configuration's description
        final Label descriptionLabel = new Label(group1, SWT.LEFT);
        descriptionLabel.setFont(parent.getFont());
        descriptionLabel.setText(ManagedBuilderUIMessages.getResourceString(DESCRIPTION));

        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = false;
        descriptionLabel.setLayoutData(gd);
        configDescription = new Text(group1, SWT.SINGLE | SWT.BORDER);
        configDescription.setFont(group1.getFont());
		configDescription.setText(getNewDescription());
		configDescription.setFocus();
		
        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        configDescription.setLayoutData(gd);
		return composite;
		
		
	}

	/* (non-Javadoc)
	 * Answers <code>true</code> if the name entered by the user clashes
	 * with an existing configuration name.
	 * 
	 * @param newName
	 * @return
	 */
	protected boolean isDuplicateName(String newName) {
		// First check whether the 'newName' is same as original name, if so return false.
		// This is needed in case user wants to keep same name but change the description of configuration
		
		if(newName.equals(getOriginalName()))
			return false;
		
		// Return true if there is already a config of that name defined
		for (int index = 0; index < definedConfigs.length; index++) {
			IConfiguration configuration = definedConfigs[index];
			if (configuration.getName().equals(newName)) {
				return true;
			}
		}
		if (reservedNames.contains(newName)) {
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * Answers <code>true</code> if the name entered by the user differs 
	 * only in case from an existing name.
	 * 
	 * @param newName
	 * @return
	 */
	protected boolean isSimilarName(String newName) {
		// First check whether the 'newName' is similar to original name, if so return false.
		// This is needed in case user wants to keep similar name
		
		if(newName.equalsIgnoreCase(getOriginalName()))
			return false;
		
		// Return true if there is already a config of that name defined on the target
		for (int index = 0; index < definedConfigs.length; index++) {
			IConfiguration configuration = definedConfigs[index];
			if (configuration.getName().equalsIgnoreCase(newName)) {
				return true;
			}
		}
		Iterator iter = reservedNames.listIterator();
		while (iter.hasNext()) {
			if (((String)iter.next()).equalsIgnoreCase(newName)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * Checks the argument for leading whitespaces and invalid directory name characters. 
	 * @param name
	 * @return <I>true</i> is the name is a valid directory name with no whitespaces
	 */
	private boolean validateName(String name) {
		// Names must be at least one character in length
		if (name.trim().length() == 0)
			return false;
		
		// Iterate over the name checking for bad characters
		char[] chars = name.toCharArray();
		// No whitespaces at the start of a name
		if (Character.isWhitespace(chars[0])) {
			return false;
		}
		for (int index = 0; index < chars.length; ++index) {
			// Config name must be a valid dir name too, so we ban "\ / : * ? " < >" in the names
			if (!Character.isLetterOrDigit(chars[index])) {
				switch (chars[index]) {
				case '/':
				case '\\':
				case ':':
				case '*':
				case '?':
				case '\"':
				case '<':
				case '>':
					return false;
				default:
					break;
				}
			}
		}
		return true;
	}
	/* (non-Javadoc)
	 * Update the status message and button state based on the input selected
	 * by the user
	 * 
	 */
	private void validateState() {
		StatusInfo status= new StatusInfo();
		String currentName = configName.getText(); 
		// Trim trailing whitespace
		while (currentName.length() > 0 && Character.isWhitespace(currentName.charAt(currentName.length()-1))) {
			currentName = currentName.substring(0, currentName.length()-1);
		}
		// Make sure that the name is at least one character in length
		if (currentName.length() == 0) {
			// No error message, but cannot select OK
			status.setError("");	//$NON-NLS-1$
		 	// Make sure the name is not a duplicate
		} else if (isDuplicateName(currentName)) {
			status.setError(ManagedBuilderUIMessages.getFormattedString(DUPLICATE, currentName));
		} else if (isSimilarName(currentName)) {
			status.setError(ManagedBuilderUIMessages.getFormattedString(CASE, currentName));
		} else if (!validateName(currentName)) {
			// TODO Create a decent I18N string to describe this problem
			status.setError(ManagedBuilderUIMessages.getFormattedString(INVALID, currentName));	
		} 
		updateStatus(status);
		return;
	}
	
	public String getNewName() {
        return newName;
    }
	
	public String getNewDescription() {
        return newDescription;
    }

	public void setNewDescription(String newDescription) {
		this.newDescription = newDescription;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
}
