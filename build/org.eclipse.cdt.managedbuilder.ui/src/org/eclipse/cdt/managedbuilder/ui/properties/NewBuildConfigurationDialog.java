/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

/**
 * Build-system specific version 
 * for "add new configuration" dialog
 * in "Manage configurations" feature
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class NewBuildConfigurationDialog extends Dialog {
	// String constants
	private static final String PREFIX = "NewConfiguration";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String ERROR = PREFIX + ".error";	//$NON-NLS-1$	
	private static final String NAME = LABEL + ".name";	//$NON-NLS-1$
	private static final String GROUP = LABEL + ".group";	//$NON-NLS-1$
	private static final String COPY = LABEL + ".copy";	//$NON-NLS-1$
	private static final String CLONE = LABEL + ".clone";	//$NON-NLS-1$
	private static final String DUPLICATE = ERROR + ".duplicateName";	//$NON-NLS-1$	
	private static final String CASE = ERROR + ".caseName";	//$NON-NLS-1$	
	private static final String INVALID = ERROR + ".invalidName";	//$NON-NLS-1$	
	private static final String DESCRIPTION = LABEL + ".description";	//$NON-NLS-1$
	
	// Widgets
	private Button btnClone;
	private Button btnCopy;
	private Text configName;
	private Text configDescription;
	private Combo copyConfigSelector;
	private Combo cloneConfigSelector;
	private Label statusLabel;

	// Bookeeping
	private boolean clone;
	/** Default configurations defined in the toolchain description */
	private IConfiguration[] defaultCfgds;
	/** Configurations defined in the target */
	private IConfiguration[] definedCfgds;
	private IConfiguration parentConfig;
	private ICProjectDescription des;
	private String newName;
	private String newDescription;
	final private String title;

	
	/**
	 * @param parentShell
	 * @param managedTarget
	 * @param nameList A list of names (Strings) that have been added by the user but have not yet been added to the target 
	 * @param title The title of the dialog
	 */
	protected NewBuildConfigurationDialog(Shell parentShell,
			ICProjectDescription prjd,
			ICConfigurationDescription[] _cfgds,
			IConfiguration[] _defs,
			String title) {
		super(parentShell);
		this.title = title;
		des = prjd;
		setShellStyle(getShellStyle()|SWT.RESIZE);
		
		newName = new String();
		newDescription = new String();
		
		parentConfig = null;
		// The default behaviour is to clone the settings
		clone = true;
		
		// Populate the list of default and defined configurations
		definedCfgds = new IConfiguration[_cfgds.length];
		for (int i=0; i<_cfgds.length; i++) 
			definedCfgds[i] = ManagedBuildManager.getConfigurationForDescription(_cfgds[i]);
		defaultCfgds = _defs; 
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog. Cache the name and base config selections.
	 * We don't have to worry that the index or name is wrong because we 
	 * enable the OK button IFF those conditions are met.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			String description = new String();
			String nameAndDescription = new String();
			String baseConfigNameAndDescription = new String();
			
			newName = configName.getText().trim();
			newDescription = configDescription.getText().trim();
			
			if (clone) {
				baseConfigNameAndDescription = cloneConfigSelector.getItem(cloneConfigSelector.getSelectionIndex());				
				for (int i = 0; i < definedCfgds.length; i++) {
					description = definedCfgds[i].getDescription();
					
					if( (description == null) || (description.equals("")) ){	//$NON-NLS-1$
						nameAndDescription = definedCfgds[i].getName();
					} else {
						nameAndDescription = definedCfgds[i].getName() + "( " + description + " )";	//$NON-NLS-1$	//$NON-NLS-2$
					}
					if (nameAndDescription.equals(baseConfigNameAndDescription)) {
						parentConfig = definedCfgds[i];
						break;				
					}
				}
			} else {
				// Get the parent config out of the default config list
				baseConfigNameAndDescription = copyConfigSelector.getItem(copyConfigSelector.getSelectionIndex());
				for (int i = 0; i < defaultCfgds.length; i++) {
					description = defaultCfgds[i].getDescription();
	
					if( (description == null) || (description.equals("")) ) {	//$NON-NLS-1$
						nameAndDescription = defaultCfgds[i].getName();
					} else {
						nameAndDescription = defaultCfgds[i].getName() + "( " + description + " )";	//$NON-NLS-1$	//$NON-NLS-2$
					}
					if (nameAndDescription.equals(baseConfigNameAndDescription)) {
						parentConfig = defaultCfgds[i];
						break;				
					}
				}
			}
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
		
		// Create a group for the radio buttons

		final Group group = new Group(composite, SWT.NONE);
		group.setFont(composite.getFont());
		group.setText(UIMessages.getString(GROUP));
		GridLayout layout = new GridLayout(3, false);
		group.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		group.setLayoutData(gd);

		SelectionListener radioListener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				clone = btnClone.getSelection();
				updateComboState();
			}
		};
		// Add a radio button and combo box to copy from default config
		btnCopy = new Button(group, SWT.RADIO);
		btnCopy.setFont(group.getFont());
		btnCopy.setText(UIMessages.getString(COPY));
		setButtonLayoutData(btnCopy);
		btnCopy.addSelectionListener(radioListener);
		
		copyConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		copyConfigSelector.setFont(group.getFont());
		int index = copyConfigSelector.indexOf(newName);
		copyConfigSelector.select(index < 0 ? 0 : index);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		copyConfigSelector.setLayoutData(gd);
		copyConfigSelector.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				validateState();		
			}
		});	
		copyConfigSelector.setEnabled(false);
		
		// Create a radio button and combo for clonable configs
		btnClone = new Button(group, SWT.RADIO);
		btnClone.setFont(group.getFont());
		btnClone.setText(UIMessages.getString(CLONE));
		setButtonLayoutData(btnClone);
		btnClone.addSelectionListener(radioListener);
		btnClone.setSelection(true);
		
		cloneConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cloneConfigSelector.setFont(group.getFont());
		cloneConfigSelector.setItems(getDefinedConfigNamesAndDescriptions());
		index = cloneConfigSelector.indexOf(newName);
		cloneConfigSelector.select(index < 0 ? 0 : index);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		cloneConfigSelector.setLayoutData(gd);
		cloneConfigSelector.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				validateState();		
			}
		});	
		
		updateComboState();
		updateDefaultConfigs();
		
		statusLabel = new Label(composite, SWT.CENTER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		statusLabel.setLayoutData(gd);
		statusLabel.setFont(composite.getFont());
		statusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));

		return composite;
	}


	/**
	 * updates the list of default configurations
	 */
	
	private void updateDefaultConfigs(){
//		IConfiguration cfgs[] = managedProject.getProjectType().getConfigurations();
		if(defaultCfgds.length != 0){
			String namesAndDescriptions[] = new String[defaultCfgds.length];
			for (int i = 0; i < defaultCfgds.length; ++i) {
				if ( (defaultCfgds[i].getDescription() == null) || defaultCfgds[i].getDescription().equals(""))	//$NON-NLS-1$
					namesAndDescriptions[i] = defaultCfgds[i].getName();
				else
					namesAndDescriptions[i] = defaultCfgds[i].getName() + "( " + defaultCfgds[i].getDescription() + " )";	//$NON-NLS-1$	//$NON-NLS-2$
			}
				
			int selectionIndex = copyConfigSelector.getSelectionIndex();
			String oldSelection = null;
			if(selectionIndex != -1)
				oldSelection = copyConfigSelector.getItem(selectionIndex);
			
			copyConfigSelector.setItems(namesAndDescriptions);
			if(oldSelection != null)
				selectionIndex = copyConfigSelector.indexOf(oldSelection);
			if(selectionIndex == -1)
				selectionIndex = 0;
			copyConfigSelector.select(selectionIndex);
		}
		else{
			copyConfigSelector.removeAll();
		}
		validateState();
	}
	
	/*
	 * Returns the array of configuration names defined for this managed project.
	 * This list will be used to populate the list of configurations to 
	 * clone.
	 */
	private String [] getDefinedConfigNamesAndDescriptions() {
		String [] namesAndDescriptions = new String[definedCfgds.length];
		for (int i = 0; i < definedCfgds.length; ++i) {
			if ( (definedCfgds[i].getDescription() == null) || definedCfgds[i].getDescription().equals(""))	//$NON-NLS-1$
				namesAndDescriptions[i] = definedCfgds[i].getName();
			else
				namesAndDescriptions[i] = definedCfgds[i].getName() + "( " + definedCfgds[i].getDescription() +" )";	//$NON-NLS-1$	//$NON-NLS-2$
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
		for (int i = 0; i < definedCfgds.length; i++) {
			if (definedCfgds[i].getName().equals(newName)) 
				return true;
		}
		return false;
	}

	protected boolean isSimilarName(String newName) {
		for (int i = 0; i < definedCfgds.length; i++) {
			if (definedCfgds[i].getName().equalsIgnoreCase(newName))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * Radio button selection event handler calls this helper method to 
	 * enable or disable the radio buttons.
	 */
	protected void updateComboState() {
		cloneConfigSelector.setEnabled(clone);
		copyConfigSelector.setEnabled(!clone);
//		btnShowAll.setVisible(!clone);
		validateState();
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
		} else if(clone ? definedCfgds.length == 0 : defaultCfgds.length == 0) {
			s = "";	//$NON-NLS-1$
			// Make sure the name is not a duplicate
		} else if (isDuplicateName(currentName)) {
			s = UIMessages.getFormattedString(DUPLICATE, currentName);
		} else if (isSimilarName(currentName)) {
			s = UIMessages.getFormattedString(CASE, currentName);
		} else if (!validateName(currentName)) {
			// TODO Create a decent I18N string to describe this problem
			s = UIMessages.getFormattedString(INVALID, currentName);
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
	 * 
	 * Always returns null - in fact, return value
	 * is kept for compatibility only.
	 */
	public ICConfigurationDescription newConfiguration() {
		Configuration cfg = (Configuration)parentConfig;
		String id = ManagedBuildManager.calculateChildId(cfg.getId(), null);
		ManagedProject mp = (ManagedProject)ManagedBuildManager.getBuildInfo(des.getProject()).getManagedProject();
		Configuration config = new Configuration(mp, cfg, id, true, false);
		config.setName(getNewName());
		config.setDescription(getNewDescription());
		
		String target = config.getArtifactName();
		if (target == null || target.length() == 0)
			config.setArtifactName(mp.getDefaultArtifactName());

		CConfigurationData data = config.getConfigurationData();
		try {
			des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
		} catch (CoreException e) {
			System.out.println(Messages.getString("NewBuildConfigurationDialog.0")); //$NON-NLS-1$
			System.out.println(Messages.getString("NewBuildConfigurationDialog.1") + e.getLocalizedMessage()); //$NON-NLS-1$
		}
		return null;
	}
}
