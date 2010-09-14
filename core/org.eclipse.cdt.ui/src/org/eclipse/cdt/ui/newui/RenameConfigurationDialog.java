/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RenameConfigurationDialog extends Dialog {
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
		
	private ICConfigurationDescription[] cfgds;
	private ICConfigurationDescription renameConfig;
	private String newName;
	private String newDescription;
	private Label statusLabel;
	
	private String originalName;
	final private String title;
	/**
	 */
	protected RenameConfigurationDialog(Shell parentShell, 
			ICConfigurationDescription _renameConfig,
			ICConfigurationDescription[] _cfgds,
			String _title) {
		super(parentShell);
		title = _title;
		renameConfig = _renameConfig;
		cfgds = _cfgds;
		
		setShellStyle(getShellStyle()|SWT.RESIZE);
		newName = renameConfig.getName();
		newDescription = renameConfig.getDescription();
		if(newDescription == null) newDescription = new String();
		originalName = renameConfig.getName();
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog. Cache the name and base config selections.
	 * We don't have to worry that the index or name is wrong because we 
	 * enable the OK button IFF those conditions are met.
	 */
	@Override
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
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) shell.setText(title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		configName.setFocus();
		if (configName != null) {
			configName.setText(newName);
		}
		validateState();
	}

	@Override
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

		// bug 187634: Add a label to warn user that configuration name will be used directly
		// as a directory name in the filesystem.
		Label warningLabel = new Label(group1, SWT.BEGINNING | SWT.WRAP);
		warningLabel.setFont(parent.getFont());
		warningLabel.setText(UIMessages.getString("RenameConfiguration.label.warning")); //$NON-NLS-1$
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1);
		gd.widthHint = 300;
		warningLabel.setLayoutData(gd);

		// Add a label and a text widget for Configuration's name
		final Label nameLabel = new Label(group1, SWT.LEFT);
		nameLabel.setFont(parent.getFont());
		nameLabel.setText(UIMessages.getString(NAME));
				
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
        descriptionLabel.setText(UIMessages.getString(DESCRIPTION));

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
        
        statusLabel = new Label(parent, SWT.CENTER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		statusLabel.setLayoutData(gd);
		statusLabel.setFont(composite.getFont());
		statusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));
		return composite;
	}

	protected boolean isDuplicateName(String newName) {
		if(newName.equals(originalName)) return false;
		// Return true if there is already a config of that name defined
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equals(newName)) return true;
		}
		return false;
	}
	
	protected boolean isSimilarName(String newName) {
		if(newName.equalsIgnoreCase(originalName))	return false;
		// Return true if there is already a config of that name defined on the target
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equalsIgnoreCase(newName)) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * Checks the argument for leading whitespaces and invalid directory name characters. 
	 * @param name
	 * @return <I>true</i> is the name is a valid directory name with no whitespaces
	 */
	private boolean validateName(String name) {
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
		String s = null;
		String currentName = configName.getText().trim();
		// Make sure that the name is at least one character in length
		if (currentName.length() == 0) {
			s = "";	//$NON-NLS-1$
		 	// Make sure the name is not a duplicate
		} else if (isDuplicateName(currentName)) {
			s = UIMessages.getFormattedString(DUPLICATE, currentName);
		} else if (isSimilarName(currentName)) {
			s = UIMessages.getFormattedString(CASE, currentName);
		} else if (!validateName(currentName)) {
			s = UIMessages.getFormattedString(INVALID, currentName);	
		}
		Button b = getButton(IDialogConstants.OK_ID);
		if (s != null) {
			statusLabel.setText(s);
			statusLabel.setVisible(true);
			if (b != null) b.setEnabled(false);
		} else {
			statusLabel.setVisible(false);
			if (b != null) b.setEnabled(true);
		}
		return;
	}
	
	public String getNewName() { return newName; }
	public String getNewDescription() { return newDescription; }
}
