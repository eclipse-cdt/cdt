/**********************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Class that implements the project type and configuration selection page in the new 
 * project wizard for managed builder projects.
 * 
 * @since 1.2
 */
public class CProjectPlatformPage extends WizardPage {
	/*
	 * Dialog variables and string constants
	 */
	private static final String PREFIX = "PlatformBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip"; //$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".configs"; //$NON-NLS-1$
	private static final String SHOWALL_LABEL = LABEL + ".showall"; //$NON-NLS-1$
	private static final String SHOWALL_CONFIG_LABEL = LABEL + ".showall.config"; //$NON-NLS-1$
	private static final String TARGET_LABEL = LABEL + ".platform"; //$NON-NLS-1$
	private static final String TARGET_TIP = TIP + ".platform"; //$NON-NLS-1$
	private static final String FORCEDCONFIG_TIP = TIP + ".forcedconfigs"; //$NON-NLS-1$
	

	protected NewManagedProjectWizard parentWizard;
	protected Combo platformSelection;
	private ArrayList selectedConfigurations;
	protected IProjectType selectedProjectType;
	protected Button showAllProjTypes;
	protected Button showAllConfigs;
	protected boolean showAllConfigsForced;
	protected CheckboxTableViewer tableViewer;
	protected String[] projectTypeNames;
	protected ArrayList projectTypes;
	protected IConfiguration configurations[];

	/**
	 * Constructor.
	 * @param pageName
	 * @param wizard
	 */
	public CProjectPlatformPage(String pageName, NewManagedProjectWizard parentWizard) {
		super(pageName);
		setPageComplete(false);
		selectedProjectType = null;
		selectedConfigurations = new ArrayList(0);
		this.parentWizard = parentWizard;
		showAllConfigsForced = false;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return validatePage();
	}

	private void createConfigSelectionGroup (Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create a check box table of valid configurations
		final Label configLabel = new Label(composite, SWT.LEFT);
		configLabel.setFont(composite.getFont());
		configLabel.setText(ManagedBuilderUIMessages.getResourceString(CONFIG_LABEL));

		Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.MULTI
								| SWT.SINGLE | SWT.H_SCROLL	| SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		// Add a table layout to the table
		TableLayout tableLayout = new TableLayout();
		table.setHeaderVisible(false);
		table.setLayout(tableLayout);

		// Add the viewer
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setLabelProvider(new ConfigurationLabelProvider());
		tableViewer.setContentProvider(new ConfigurationContentProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				// will default to false until a selection is made
				handleConfigurationSelectionChange();
			}
		});

	}
	
	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Create the composite control for the tab
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Setup the help information
		WorkbenchHelp.setHelp(composite, ManagedBuilderHelpContextIds.MAN_PROJ_PLATFORM_HELP);

		// Create the widgets
		createTypeSelectGroup(composite);
		createConfigSelectionGroup(composite);
		createShowAllGroup(composite);

		// Select the first project type in the list
		populateTypes();
		platformSelection.select(0);
		handleTypeSelection();
		
		// Do the nasty
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void createShowAllGroup(Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		showAllProjTypes = new Button(composite, SWT.CHECK | SWT.LEFT);
		showAllProjTypes.setFont(composite.getFont());
		showAllProjTypes.setText(ManagedBuilderUIMessages.getResourceString(SHOWALL_LABEL));
		showAllProjTypes.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				populateTypes();
				platformSelection.select(0);
				handleTypeSelection();
			}
		});
		showAllProjTypes.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				showAllProjTypes = null;
			}
		});

		showAllConfigs = new Button(composite, SWT.CHECK | SWT.LEFT);
		showAllConfigs.setFont(composite.getFont());
		showAllConfigs.setText(ManagedBuilderUIMessages.getResourceString(SHOWALL_CONFIG_LABEL));
		showAllConfigs.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				populateConfigurations(true);
			}
		});
		showAllConfigs.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				showAllConfigs = null;
			}
		});
	}
	
	private void createTypeSelectGroup(Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the platform selection label and combo widgets
		final Label platformLabel = new Label(composite, SWT.LEFT);
		platformLabel.setFont(composite.getFont());
		platformLabel.setText(ManagedBuilderUIMessages.getResourceString(TARGET_LABEL));

		platformSelection = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);	
		platformSelection.setFont(composite.getFont());
		platformSelection.setToolTipText(ManagedBuilderUIMessages.getResourceString(TARGET_TIP));
		platformSelection.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleTypeSelection();
			}
		});
		platformSelection.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				platformSelection = null;
			}
		});
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		// Make this the same as NewCProjectWizardPage.SIZING_TEXT_FIELD_WIDTH
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 50;
		platformSelection.setLayoutData(gd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}
	
	/**
	 * @return
	 */
	public IConfiguration[] getSelectedConfigurations() {
		return (IConfiguration[]) selectedConfigurations.toArray(new IConfiguration[selectedConfigurations.size()]);
	}

	/**
	 * Returns the selected project type.
	 * 
	 * @return IProjectType Selected type or <code>null</code> if an invalid selection
	 * has been made.
	 */
	public IProjectType getSelectedProjectType() {
		return selectedProjectType;
	}

	private void handleConfigurationSelectionChange() {
		// Get the selections from the table viewer
		selectedConfigurations.clear();
		selectedConfigurations.addAll(Arrays.asList(tableViewer.getCheckedElements()));
		setPageComplete(validatePage());
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected void handleTypeSelection() {
		/*
		 * The index in the combo is the offset into the project type list
		 */
		int index;
		if (platformSelection != null
			&& (index = platformSelection.getSelectionIndex()) != -1) {
			if (selectedProjectType != (IProjectType) projectTypes.get(index)) {
				selectedProjectType = (IProjectType) projectTypes.get(index);
				parentWizard.updateProjectTypeProperties();
			}
		}
		populateConfigurations(false);
		setPageComplete(validatePage());
	}

	/**
	 * Populate the table viewer with either all known configurations
	 * or only with the supported configurations depending on whether a user
	 * has chosen to display unsupported configurations or not 
	 * By default, only supported configurations are selected.
	 */
	private void populateConfigurations(boolean showallconfigsevent) {
		if (selectedProjectType == null)
			return;
		boolean showAll = showAllConfigs != null ? showAllConfigs.getSelection() : false;
		IConfiguration selected[] = null;
		
		if (showAll) {
			configurations = selectedProjectType.getConfigurations();
			selected = filterSupportedConfigurations(configurations);
		}
		else {
			configurations = filterSupportedConfigurations(selectedProjectType.getConfigurations());
			selected = configurations;
		}
	
		// Check for buildable configs on this platform
		if (selected.length == 0) {
			// No buildable configs on this platform
			if (showallconfigsevent) {
				// "Show All Configurations" button pressed by user
				if (showAll) {		
					// "Show All Configurations" check box "checked" by user
					// For a project with no buildable configs, all available 
					// configs should be displayed and checked
					configurations = selectedProjectType.getConfigurations();
					selected = configurations;
				}
				if (showAllConfigsForced) {
				    // The previous setting of this check box was done automatically when a project type
				    // with no buildable configs was encountered; undo this state now and honor the 
					// user's button click
					setMessage(null, NONE);
					showAllConfigsForced = false;
				}
			}
			else {
				configurations = selectedProjectType.getConfigurations();
				selected = configurations;
				if (!showAll) {			    
				    showAllConfigsForced = true;
					showAllConfigs.setSelection(true);					  												
				}
			}
			// Indicate that there are no buildable configurations on this platform for this project
			// type and that all configurations will be selected
			setMessage(ManagedBuilderUIMessages.getResourceString(FORCEDCONFIG_TIP), WARNING);
		}
		else { 
			setMessage(null, NONE);
			if (showAllConfigsForced) {
			    showAllConfigsForced = false;
			    showAllConfigs.setSelection(false);
				// Redo filtering in light of reset of "show all configs" to false
				configurations = filterSupportedConfigurations(selectedProjectType.getConfigurations());
				selected = configurations;
			}
		}
		
		tableViewer.setInput(configurations);
		tableViewer.setCheckedElements(selected);
		handleConfigurationSelectionChange();
	}

	/**
	 * Returns the array of supported configurations found in the configurations
	 * passed to this method
	 */
	IConfiguration[] filterSupportedConfigurations(IConfiguration cfgs[]){
		ArrayList supported = new ArrayList();
		String os = Platform.getOS();
		String arch = Platform.getOSArch();

		for (int i = 0; i < cfgs.length; i++) {
			// First, filter on supported state
			if (cfgs[i].isSupported()) {				
				// Now, apply the OS and ARCH filters to determine if the configuration should be shown
				// Determine if the configuration's tool-chain supports this OS & Architecture.
				IToolChain tc = cfgs[i].getToolChain();
				List osList = Arrays.asList(tc.getOSList());
				if (osList.contains("all") || osList.contains(os)) {	//$NON-NLS-1$
					List archList = Arrays.asList(tc.getArchList());
					if (archList.contains("all") || archList.contains(arch)) { //$NON-NLS-1$
						supported.add(cfgs[i]);						
					}
				}		
			}
		}
		return (IConfiguration[])supported.toArray(new IConfiguration[supported.size()]);
	}
	
	/* (non-Javadoc)
	 * Extracts the names from the project types that are valid for the wizard
	 * session and populates the combo widget with them.
	 */
	private void populateTypeNames() {
		projectTypeNames = new String[projectTypes.size()];
		ListIterator iter = projectTypes.listIterator();
		int index = 0;
		while (iter.hasNext()) {
			projectTypeNames[index++] = ((IProjectType) iter.next()).getName();
		}
		
		// Now setup the combo
		platformSelection.removeAll();
		platformSelection.setItems(projectTypeNames);
	}

	/* (non-Javadoc)
	 * Collects all the valid project types for the platform Eclipse is running on
	 */
	private void populateTypes() {
		// Get a list of platforms defined by plugins
		IProjectType[] allProjectTypes = ManagedBuildManager.getDefinedProjectTypes();
		projectTypes = new ArrayList();
		String os = Platform.getOS();
		String arch = Platform.getOSArch();
		// Add all of the concrete project types to the list
		for (int index = 0; index < allProjectTypes.length; ++index) {
			IProjectType type = allProjectTypes[index];
			if (!type.isAbstract() && !type.isTestProjectType()) {
				// If the check box is selected show all the targets
				if (showAllProjTypes != null && showAllProjTypes.getSelection() == true) {
					projectTypes.add(type);
				} else if (type.isSupported()) {
					// Apply the OS and ARCH filters to determine if the target should be shown
					// Determine if the project type has any configuration with a tool-chain
					// that supports this OS & Architecture.
					IConfiguration[] configs = type.getConfigurations();
					for (int j = 0; j < configs.length; ++j) {
						IToolChain tc = configs[j].getToolChain();
						List osList = Arrays.asList(tc.getOSList());
						if (osList.contains("all") || osList.contains(os)) {	//$NON-NLS-1$
							List archList = Arrays.asList(tc.getArchList());
							if (archList.contains("all") || archList.contains(arch)) { //$NON-NLS-1$
								projectTypes.add(type);
								break;
							}
						}
					}
				}
			}
		}
		projectTypes.trimToSize();
		populateTypeNames();
	}

	/**
	 * @return
	 */
	private boolean validatePage() {
		// TODO some validation ... maybe
		if ((tableViewer.getCheckedElements()).length > 0) {
			setErrorMessage(null);
			return true;
		} else {
			setErrorMessage(ManagedBuilderUIMessages.getResourceString("PlatformBlock.message.error.noconfigs"));	//$NON-NLS-1$
			return false;
		}
	}
}
