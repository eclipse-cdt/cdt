/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuildOptionBlock;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;


public class ResourceBuildPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage, IPreferencePageContainer, ICOptionContainer {
	/*
	 * String constants
	 */
	private static final String PREFIX = "ResourceBuildPropertyPage"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String NAME_LABEL = LABEL + ".NameText"; //$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".Configuration"; //$NON-NLS-1$
	private static final String ALL_CONFS = PREFIX	+ ".selection.configuration.all"; //$NON-NLS-1$
	private static final String ACTIVE_RESOURCE_LABEL = LABEL + ".ActiveResource"; //$NON-NLS-1$
	private static final String RESOURCE_SETTINGS_LABEL = LABEL	+ ".ResourceSettings"; //$NON-NLS-1$
	private static final String TREE_LABEL = LABEL + ".ToolTree"; //$NON-NLS-1$
	private static final String OPTIONS_LABEL = LABEL + ".ToolOptions"; //$NON-NLS-1$
	private static final String EXCLUDE_CHECKBOX = LABEL + ".ExcludeCheckBox"; //$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip"; //$NON-NLS-1$
	private static final String RESOURCE_PLAT_TIP = TIP + ".ResourcePlatform"; //$NON-NLS-1$
	private static final String CONF_TIP = TIP + ".config"; //$NON-NLS-1$
	private static final String EXCLUDE_TIP = TIP + ".excludecheck"; //$NON-NLS-1$
	private static final String MANAGE_TITLE = PREFIX + ".manage.title"; //$NON-NLS-1$
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 20, 30 };
	private static final String ID_SEPARATOR = "."; //$NON-NLS-1$

	/*
	 * Dialog widgets
	 */

	private Combo configSelector;

//	private Point lastShellSize;
	private Button excludedCheckBox;
	private boolean isExcluded = false;

	/*
	 * Bookeeping variables
	 */

	
	private IConfiguration[] configurations;
	private IConfiguration selectedConfiguration;
	private IResourceConfiguration currentResourceConfig;
	private Point lastShellSize;
	protected ManagedBuildOptionBlock fOptionBlock;
	protected boolean displayedConfig = false;
	/**
	 * Default constructor
	 */
	public ResourceBuildPropertyPage() {
	//	super();
	}

	public void setContainer(IPreferencePageContainer preferencePageContainer) {
	    super.setContainer(preferencePageContainer);
	    if (fOptionBlock == null) {
	    	fOptionBlock = new ManagedBuildOptionBlock(this);
	    }
	}	
	
	protected Control createContents(Composite parent) {
		GridData gd;
		
//		 Create the container we return to the property page editor
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 1;
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout( compositeLayout );
		
//		 Initialize the key data
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (info.getVersion() == null) {
			// Display a message page instead of the properties control
			final Label invalidInfo = new Label(composite, SWT.LEFT);
			invalidInfo.setFont(composite.getFont());
			invalidInfo.setText(ManagedBuilderUIMessages.getResourceString("ResourceBuildPropertyPage.error.version_low")); //$NON-NLS-1$
			invalidInfo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING,GridData.VERTICAL_ALIGN_CENTER, true, true));
			return composite;
		}
		
		// Add a config selection area
		Group configGroup = ControlFactory.createGroup(composite, ManagedBuilderUIMessages.getResourceString(ACTIVE_RESOURCE_LABEL), 1);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		configGroup.setLayoutData(gd);
		// Use the form layout inside the group composite
		FormLayout form = new FormLayout();
		form.marginHeight = 5;
		form.marginWidth = 5;
		configGroup.setLayout(form);

		excludedCheckBox = ControlFactory.createCheckBox(configGroup, ManagedBuilderUIMessages.getResourceString(EXCLUDE_CHECKBOX));
		excludedCheckBox.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleIsExcluded();
			}
		});
		excludedCheckBox.setToolTipText(ManagedBuilderUIMessages.getResourceString(EXCLUDE_TIP));

		FormData fd = new FormData();
		fd = new FormData();
		fd.left = new FormAttachment(excludedCheckBox, 0, SWT.CENTER);
		excludedCheckBox.setLayoutData(fd);

		Label configLabel = ControlFactory.createLabel(configGroup, ManagedBuilderUIMessages.getResourceString(CONFIG_LABEL));
		configSelector = new Combo(configGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
		configSelector.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleConfigSelection();
			}
		});
		configSelector.setToolTipText(ManagedBuilderUIMessages.getResourceString(CONF_TIP));

		// Now do the form layout for the widgets

		fd = new FormData();
		fd.top = new FormAttachment(excludedCheckBox, 15, SWT.DEFAULT);

		configLabel.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(excludedCheckBox, 15, SWT.DEFAULT);
		fd.left = new FormAttachment(configLabel, 5, SWT.DEFAULT);
		fd.right = new FormAttachment(80, -20);
		configSelector.setLayoutData(fd);

//		 Create the Tools Settings, Build Settings, ... Tabbed pane
		Group tabGroup = ControlFactory.createGroup(composite, ManagedBuilderUIMessages.getResourceString(RESOURCE_SETTINGS_LABEL), 1);
		gd = new GridData(GridData.FILL_BOTH);
		tabGroup.setLayoutData(gd);
		fOptionBlock.createContents(tabGroup, getElement());
		
//		 Update the contents of the configuration widget
		populateConfigurations();
		WorkbenchHelp.setHelp(composite,ManagedBuilderHelpContextIds.MAN_PROJ_BUILD_PROP);
		return composite;
	}

	private void handleIsExcluded() {

		// Check whether the check box is selected or not.
		boolean isSelected = excludedCheckBox.getSelection();
		setExcluded(isSelected);
	}

	/*
	 * (non-Javadoc) @return an array of names for the configurations defined
	 * for the chosen
	 */
	private String[] getConfigurationNames() {
		String[] names = new String[configurations.length];
		for (int index = 0; index < configurations.length; ++index) {
			names[index] = configurations[index].getName();
		}
		return names;
	}

	protected Point getLastShellSize() {
		if (lastShellSize == null) {
			Shell shell = getShell();
			if (shell != null)
				lastShellSize = shell.getSize();
		}
		return lastShellSize;
	}

	public IProject getProject() {
		Object element = getElement();
		if (element != null && element instanceof IFile) {
			IFile file = (IFile) element;
			return (IProject) file.getProject();
		}
		return null;
	}

	/*
	 * (non-Javadoc) @return
	 */
	public IConfiguration getSelectedConfiguration() {
		return selectedConfiguration;
	}

		/*
	 * Event Handlers
	 */
	private void handleConfigSelection() {
		// If there is nothing in config selection widget just bail
		if (configSelector.getItemCount() == 0)
			return;

		// Check if the user has selected the "all" configuration
		int selectionIndex = configSelector.getSelectionIndex();
		if (selectionIndex == -1)
			return;
		String configName = configSelector.getItem(selectionIndex);
		if (configName.equals(ManagedBuilderUIMessages
				.getResourceString(ALL_CONFS))) {
			// This is the all config
			return;
		} else {
			IConfiguration newConfig = configurations[selectionIndex];
			if (newConfig != selectedConfiguration) {
				// If the user has changed values, and is now switching configurations, prompt for saving
			    if (selectedConfiguration != null) {
			        if (fOptionBlock.isDirty()) {
						Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
						boolean shouldApply = MessageDialog.openQuestion(shell,
						        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
						        ManagedBuilderUIMessages.getFormattedString("BuildPropertyPage.changes.save.question",  //$NON-NLS-1$
						                new String[] {selectedConfiguration.getName(), newConfig.getName()}));
						if (shouldApply) {
						    if (performOk()) {
			        			fOptionBlock.setDirty(false);    
			        		} else {
						        MessageDialog.openWarning(shell,
								        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
								        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.error")); //$NON-NLS-1$ 
						    }
						}
			        }
			    }
			    // Set the new selected configuration
				selectedConfiguration = newConfig;
				ManagedBuildManager.setSelectedConfiguration(getProject(), selectedConfiguration);
				//	Set the current Resource Configuration
				setCurrentResourceConfig(findCurrentResourceConfig());
				
				isExcluded = getCurrentResourceConfig().isExcluded();
				fOptionBlock.updateValues();
				excludedCheckBox.setSelection(isExcluded);
			}
		}
		return;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	
	
	protected void performDefaults() {
		fOptionBlock.performDefaults();
		excludedCheckBox.setSelection(getCurrentResourceConfig().isExcluded());
		super.performDefaults();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		//	If the user did not visit this page, then there is nothing to do.
		if (!displayedConfig) return true;
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				fOptionBlock.performApply(monitor);
			}
		};
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(false, true, op);
		} catch (InvocationTargetException e) {
			Throwable e1 = e.getTargetException();
			ManagedBuilderUIPlugin.errorDialog(getShell(), ManagedBuilderUIMessages.getResourceString("ManagedProjectPropertyPage.internalError"),e1.toString(), e1); //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}

		// Write out the build model info
		ManagedBuildManager.setDefaultConfiguration(getProject(), getSelectedConfiguration());
		if ( getCurrentResourceConfig().isExcluded() != isExcluded() ) {
			getCurrentResourceConfig().setExclude(isExcluded());
			selectedConfiguration.setRebuildState(true);
		}
		
		ManagedBuildManager.saveBuildInfo(getProject(), false);
		return true;
	}

	private void populateConfigurations() {
		
		ManagedBuildManager.setSelectedConfiguration(getProject(), selectedConfiguration);
		// If the config select widget is not there yet, just stop
		if (configSelector == null)
			return;

		// Find the configurations defined for the platform
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		configurations = info.getManagedProject().getConfigurations();
		if (configurations.length == 0)
			return;

		// Clear and replace the contents of the selector widget
		configSelector.removeAll();
		configSelector.setItems(getConfigurationNames());

		// Make sure the active configuration is selected
		IConfiguration defaultConfig = info.getDefaultConfiguration();
		int index = configSelector.indexOf(defaultConfig.getName());
		configSelector.select(index == -1 ? 0 : index);
		handleConfigSelection();

	}

	
	/**
	 * @return Returns the currentResourceConfig.
	 */
	public IResourceConfiguration getCurrentResourceConfig() {
		return currentResourceConfig;
	}

	/**
	 * @param currentResourceConfig
	 *            The currentResourceConfig to set.
	 */
	public void setCurrentResourceConfig(
			IResourceConfiguration currentResourceConfig) {
		if (currentResourceConfig != null)
			this.currentResourceConfig = currentResourceConfig;
		else {
			IFile file = (IFile) getElement();
					
			// create a new resource configuration for this resource.
			this.currentResourceConfig = selectedConfiguration.createResourceConfiguration(file);
		}
	}
 
	// Check whether a resource configuration already exists for the current
	// resource in selectedConfiguration.
	// if so, return the resource configuration, otherwise return null.

	public IResourceConfiguration findCurrentResourceConfig() {

		IResourceConfiguration resConfigElement = null;

		//	Check if the selected configuration has any resourceConfigurations.
		if (selectedConfiguration.getResourceConfigurations().length == 0)
			return null;

		IResourceConfiguration[] resourceConfigurations = selectedConfiguration
				.getResourceConfigurations();
		IFile file = (IFile) getElement();

		// Check whether a resource configuration is already exists for the
		// selected file.
		for (int i = 0; i < resourceConfigurations.length; i++) {
			resConfigElement = resourceConfigurations[i];
			if (file.getFullPath().toString().equals(
					resConfigElement.getResourcePath())) {
				return resConfigElement;
			}
		}

		return null;
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
	}
	
	public void updateContainer() {
		fOptionBlock.update();
		setValid(fOptionBlock.isValid());
		setErrorMessage(fOptionBlock.getErrorMessage());
	}

	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
		if (visible) {
			fOptionBlock.updateValues();
			displayedConfig = true;
		}
	}

	public IPreferenceStore getPreferenceStore()
	{
		return fOptionBlock.getPreferenceStore();
	}

	/* (non-Javadoc)
	 * Return the IPreferenceStore of the Tool Settings block
	 */
	public IPreferenceStore getToolSettingsPreferenceStore()
	{
		return fOptionBlock.getToolSettingsPreferenceStore();
	}
	public Preferences getPreferences()
	{
		return null;
	}
	public void enableConfigSelection (boolean enable) {
		configSelector.setEnabled(enable);
	}
	/**
	 * @return Returns the isExcluded.
	 */
	public boolean isExcluded() {
		return isExcluded;
	}
	/**
	 * @param isExcluded The isExcluded to set.
	 */
	public void setExcluded(boolean isExcluded) {
		this.isExcluded = isExcluded;
	}
}
