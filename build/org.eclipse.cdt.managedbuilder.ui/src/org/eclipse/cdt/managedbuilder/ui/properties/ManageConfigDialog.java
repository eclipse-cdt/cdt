package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2002,2004 Rational Software Corporation and others.
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
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
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
	private static final String OUTPUT_EXT = LABEL + ".output.extension";	//$NON-NLS-1$
	private static final String OUTPUT_NAME = LABEL + ".output.name";	//$NON-NLS-1$
	private static final String CONFIGS = LABEL + ".configs";	//$NON-NLS-1$
	private static final String CURRENT_CONFIGS = CONFIGS + ".current";	//$NON-NLS-1$
	private static final String DELETED_CONFIGS = CONFIGS + ".deleted";	//$NON-NLS-1$
	private static final String CONF_DLG = LABEL + ".new.config.dialog";	//$NON-NLS-1$

	// The name of the build artifact
	private String artifactExt;
	private String artifactName;
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
	private String title = ""; //$NON-NLS-1$
	// State of the check box on exit
	private boolean useDefaultMake;
	
	// Widgets
	protected Text buildArtifactExt;
	protected Text buildArtifactName;
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
		
		setMakeCommand();
		
		// Get the name of the build artifact
		artifactExt = managedTarget.getArtifactExtension();
		artifactName = managedTarget.getArtifactName();
		
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
			artifactName = buildArtifactName.getText().trim();
			artifactExt = buildArtifactExt.getText().trim();
		} else {
			useDefaultMake = true;
			artifactName = managedTarget.getArtifactName();
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
	 * Creates the group that contains the build artifact name controls.
	 */
	private void createBuildArtifactGroup(Composite parent) {
		final Group outputGroup = new Group(parent, SWT.NONE);
		outputGroup.setFont(parent.getFont());
		outputGroup.setText(ManagedBuilderUIMessages.getResourceString(OUTPUT_GROUP));
		outputGroup.setLayout(new GridLayout(3, false));
		outputGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 

		// Three labels
		final Label nameLabel = new Label(outputGroup, SWT.LEFT);
		nameLabel.setFont(outputGroup.getFont());
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(OUTPUT_NAME));
		nameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label placeHolder = new Label(outputGroup, SWT.CENTER);
		placeHolder.setText(new String());
		placeHolder.setLayoutData(new GridData());
		
		final Label extLabel = new Label(outputGroup, SWT.LEFT);
		extLabel.setFont(outputGroup.getFont());
		extLabel.setText(ManagedBuilderUIMessages.getResourceString(OUTPUT_EXT));
		extLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Now we need two text widgets separated by a label
		buildArtifactName = new Text(outputGroup, SWT.SINGLE | SWT.BORDER);
		buildArtifactName.setFont(outputGroup.getFont());
		buildArtifactName.setText(artifactName);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		buildArtifactName.setLayoutData(data);
		buildArtifactName.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				buildArtifactName = null;
			}
		});
		
		final Label dotLabel = new Label(outputGroup, SWT.CENTER);
		dotLabel.setFont(outputGroup.getFont());
		dotLabel.setText(new String(".")); //$NON-NLS-1$
		dotLabel.setLayoutData(new GridData());

		buildArtifactExt = new Text(outputGroup, SWT.SINGLE | SWT.BORDER);
		buildArtifactExt.setFont(outputGroup.getFont());
		buildArtifactExt.setText(artifactExt);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = (IDialogConstants.ENTRY_FIELD_WIDTH / 2);
		buildArtifactExt.setLayoutData(data);
		buildArtifactExt.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				buildArtifactExt = null;
			}
		});
		buildArtifactExt.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = ManagedBuilderUIMessages.getResourceString(OUTPUT_EXT);
			}
		});
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
		
		// Create the current config list
		final Composite currentComp = new Composite(configListGroup, SWT.NULL);
		currentComp.setFont(configListGroup.getFont());
		currentComp.setLayout(new GridLayout(1, true));
		currentComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		currentConfigList = new List(currentComp, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		currentConfigList.setFont(currentComp.getFont());
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

		// Create the deleted config list
		final Composite deletedComp = new Composite(configListGroup, SWT.NULL);
		deletedComp.setFont(configListGroup.getFont());
		deletedComp.setLayout(new GridLayout(1, true));
		deletedComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		deletedConfigList = new List(deletedComp, SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);
		deletedConfigList.setFont(deletedComp.getFont());
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
		
		// Create a group for the build output
		createBuildArtifactGroup(comp);
	
		// Create the make command group area
		createMakeCommandGroup(comp);
		
		// Make the configuration management area
		createConfigListGroup(comp);
		
		// Do the final widget prep
		currentConfigList.setItems(getConfigurationNames());
		currentConfigList.select(0);
		newBtn.setFocus();
		return comp;
	}

	/* (non-Javadoc)
	 * Creates the group control for the make command
	 * @param parent
	 */
	private void createMakeCommandGroup(Composite parent) {
		final Group makeCommandGroup = new Group(parent, SWT.NONE);
		makeCommandGroup.setFont(parent.getFont());
		makeCommandGroup.setText(ManagedBuilderUIMessages.getResourceString(GROUP));
		makeCommandGroup.setLayout(new GridLayout(1, true));
		makeCommandGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		makeCommandDefault = new Button(makeCommandGroup, SWT.CHECK | SWT.LEFT);
		makeCommandDefault.setFont(makeCommandGroup.getFont());
		makeCommandDefault.setText(ManagedBuilderUIMessages.getResourceString(DEF_BTN));
		setButtonLayoutData(makeCommandDefault);
		makeCommandDefault.setBackground(makeCommandGroup.getBackground());
		makeCommandDefault.setForeground(makeCommandGroup.getForeground());
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
		
		makeCommandEntry = new Text(makeCommandGroup, SWT.SINGLE | SWT.BORDER);
		makeCommandEntry.setFont(makeCommandGroup.getFont());
		makeCommandEntry.setEditable(!makeCommandDefault.getSelection());
		makeCommandEntry.setText(makeCommand);
		makeCommandEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		makeCommandEntry.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				makeCommandEntry = null;
			}
		});
	}
	
	/**
	 * Answers the extension for the build artifact.
	 * @return
	 */
	public String getBuildArtifaceExtension() {
		return artifactExt;
	}
	
	/**
	 * Answers the value in the build artifact entry widget.
	 * 
	 * @return
	 */
	public String getBuildArtifactName() {
		return artifactName;
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
		NewConfigurationDialog dialog = new NewConfigurationDialog(getShell(), 
																   managedTarget, 
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
			String selectedConfigId = null;
			
			// If this is a newly added config, remove it from that map
			if (getNewConfigs().containsKey(selectedConfigName)) {
				IConfiguration selectedConfig = (IConfiguration) getNewConfigs().get(selectedConfigName); 
				selectedConfigId = selectedConfig.getId();
				getNewConfigs().remove(selectedConfigName);
			} else {
				// If it is not a new item, the ID is in the existing list
				selectedConfigId = (String) getExistingConfigs().get(selectedConfigName);
			}
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
				IConfiguration restoredConfig = managedTarget.getConfiguration(selectedConfigId);
				getNewConfigs().put(selectedConfigName, restoredConfig);
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

	/* (non-Javadoc)
	 * Event handler for the use default check box in the make command group
	 */
	protected void handleUseDefaultPressed() {
		// If the state of the button is unchecked, then we want to enable the edit widget
		boolean checked = makeCommandDefault.getSelection();
		if (checked == true) {
			managedTarget.resetMakeCommand();
			setMakeCommand();
			makeCommandEntry.setText(makeCommand);
			makeCommandEntry.setEditable(false);
		} else {
			makeCommandEntry.setEditable(true);
		}
	}

	/*
	 * 
	 */
	private void setMakeCommand() {
		// Figure out the make command
		makeCommand = managedTarget.getMakeCommand();
		String makeArgs = managedTarget.getMakeArguments();
		if (makeArgs.length() > 0) {
			makeCommand += " " + makeArgs; //$NON-NLS-1$
		}
	}

	private void updateButtons() {
		// Disable the remove button if there is only 1 configuration
		removeBtn.setEnabled(currentConfigList.getItemCount() > 1);
		// Enable the restore button if there is anything in the deleted list
		restoreBtn.setEnabled(deletedConfigList.getItemCount() > 0);
	}

	/**
	 * Answers <code>true</code> if the user has left the use default check box selected.
	 * @return
	 */
	public boolean useDefaultMakeCommand () {
		return useDefaultMake;
	}
}
