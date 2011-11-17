/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class NewConfigurationDialog extends Dialog implements INewCfgDialog {
	// Widgets
	private Text configName;
	private Text configDescription;
	private Combo cloneConfigSelector;
	private Label statusLabel;

	/** Default configurations defined in the toolchain description */
	private ICProjectDescription des;
	private ICConfigurationDescription[] cfgds;
	private ICConfigurationDescription parentConfig;
	private String newName;
	private String newDescription;
	private String title;


	/**
	 */
	protected NewConfigurationDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle()|SWT.RESIZE);
		newName = new String();
		newDescription = new String();
	}

	@Override
	public void setProject(ICProjectDescription prj) {
		des = prj;
		cfgds = des.getConfigurations();
	}

	@Override
	public void setTitle(String _title) {
		title = _title;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog. Cache the name and base config selections.
	 * We don't have to worry that the index or name is wrong because we
	 * enable the OK button IFF those conditions are met.
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			String description = new String();
			String nameAndDescription = new String();
			String baseConfigNameAndDescription = new String();

			newName = configName.getText().trim();
			newDescription = configDescription.getText().trim();

			baseConfigNameAndDescription = cloneConfigSelector.getItem(cloneConfigSelector.getSelectionIndex());
			for (int i = 0; i < cfgds.length; i++) {
				description = cfgds[i].getDescription();

				if( (description == null) || (description.equals("")) ){	//$NON-NLS-1$
					nameAndDescription = cfgds[i].getName();
				} else {
					nameAndDescription = cfgds[i].getName() + "( " + description + " )";	//$NON-NLS-1$	//$NON-NLS-2$
				}
				if (nameAndDescription.equals(baseConfigNameAndDescription)) {
					parentConfig = cfgds[i];
					break;
				}
			}
			newConfiguration();
		} else {
			newName = null;
			newDescription = null;
			parentConfig = null;
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
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
		warningLabel.setText(Messages.NewConfiguration_label_warning);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1);
		gd.widthHint = 300;
		warningLabel.setLayoutData(gd);

		// Add a label and a text widget for Configuration's name
		final Label nameLabel = new Label(group1, SWT.LEFT);
		nameLabel.setFont(parent.getFont());
		nameLabel.setText(Messages.NewConfiguration_label_name);

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
			@Override
			public void modifyText(ModifyEvent e) {
				validateState();
			}
		});

//		 Add a label and a text widget for Configuration's description
        final Label descriptionLabel = new Label(group1, SWT.LEFT);
        descriptionLabel.setFont(parent.getFont());
        descriptionLabel.setText(Messages.NewConfiguration_label_description);

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

		final Group group = new Group(composite, SWT.NONE);
		group.setFont(composite.getFont());
		group.setText(Messages.NewConfiguration_label_group);
		GridLayout layout = new GridLayout(1, false);
		group.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		group.setLayoutData(gd);

		cloneConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cloneConfigSelector.setFont(group.getFont());
		cloneConfigSelector.setItems(getDefinedConfigNamesAndDescriptions());
		int index = cloneConfigSelector.indexOf(newName);
		cloneConfigSelector.select(index < 0 ? 0 : index);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		cloneConfigSelector.setLayoutData(gd);
		cloneConfigSelector.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateState();
			}
		});

		statusLabel = new Label(composite, SWT.CENTER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		statusLabel.setLayoutData(gd);
		statusLabel.setFont(composite.getFont());
		statusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));

		return composite;
	}


	/*
	 * Returns the array of configuration names defined for this managed project.
	 * This list will be used to populate the list of configurations to
	 * clone.
	 */
	private String [] getDefinedConfigNamesAndDescriptions() {
		String [] namesAndDescriptions = new String[cfgds.length];
		for (int i = 0; i < cfgds.length; ++i) {
			if ( (cfgds[i].getDescription() == null) || cfgds[i].getDescription().equals(""))	//$NON-NLS-1$
				namesAndDescriptions[i] = cfgds[i].getName();
			else
				namesAndDescriptions[i] = cfgds[i].getName() + "( " + cfgds[i].getDescription() +" )";	//$NON-NLS-1$	//$NON-NLS-2$
		}
		return namesAndDescriptions;
	}

	/**
	 * @return <code>String</code> containing the name chosen by the user for the
	 * new configuration.
	 */
	public String getNewName() {
		return newName;
	}

	protected boolean isDuplicateName(String newName) {
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equals(newName))
				return true;
		}
		return false;
	}

	protected boolean isSimilarName(String newName) {
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equalsIgnoreCase(newName))
				return true;
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
		String s = null;
		String currentName = configName.getText();
		// Trim trailing whitespace
		while (currentName.length() > 0 && Character.isWhitespace(currentName.charAt(currentName.length()-1))) {
			currentName = currentName.substring(0, currentName.length()-1);
		}
		// Make sure that the name is at least one character in length
		if (currentName.length() == 0) {
			// No error message, but cannot select OK
			s = "";	//$NON-NLS-1$
		} else if (cfgds.length == 0) {
			s = "";	//$NON-NLS-1$
			// Make sure the name is not a duplicate
		} else if (isDuplicateName(currentName)) {
			s = NLS.bind(Messages.NewConfiguration_error_duplicateName, currentName);
		} else if (isSimilarName(currentName)) {
			s = NLS.bind(Messages.NewConfiguration_error_caseName, currentName);
		} else if (!validateName(currentName)) {
			// TODO Create a decent I18N string to describe this problem
			s = NLS.bind(Messages.NewConfiguration_error_invalidName, currentName);
		}
		if (statusLabel == null) return;
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
	 public String getNewDescription() {
        return newDescription;
    }

	/**
	 * Create a new configuration, using the values currently set in
	 * the dialog.
	 */
	private void newConfiguration() {
		String id = CDataUtil.genId(parentConfig.getId());
		try {
			ICConfigurationDescription newcfg = des.createConfiguration(id, newName, parentConfig);
			newcfg.setDescription(newDescription);
		} catch (CoreException e) {
			System.out.println("Cannot create config\n"+ e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	// useless in our case
	@Override
	public void setShell(Shell shell) {}
}
