/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BuildStepSettingsBlock extends AbstractCOptionPage {

	/*
	 * String constants
	 */
	private static final String PREFIX = "BuildStepSettingsBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String SETTINGS_LABEL = LABEL + ".Settings";	//$NON-NLS-1$
	private static final String PREBUILD_GROUP = LABEL + ".prebuildstep.group";	//$NON-NLS-1$	
	private static final String PREBUILD_CMD = LABEL + ".prebuildstep.cmd";	//$NON-NLS-1$
	private static final String PREBUILD_DESC = LABEL + ".prebuildstep.desc";	//$NON-NLS-1$
	private static final String POSTBUILD_GROUP = LABEL + ".postbuildstep.group";	//$NON-NLS-1$
	private static final String POSTBUILD_CMD = LABEL + ".postbuildstep.cmd";	//$NON-NLS-1$
	private static final String POSTBUILD_DESC = LABEL + ".postbuildstep.desc";	//$NON-NLS-1$
	private static final String EMPTY_STRING = new String();
	
	/*
	 * Dialog widgets
	 */
	protected Text preBuildCmd;
	protected Text preBuildAnnc;
	protected Text postBuildCmd;
	protected Text postBuildAnnc;

	/*
	 * Bookeeping variables
	 */
	private BuildPropertyPage parent;
	private String preBuildCommand;
	private String preBuildAnnounce;
	private String postBuildCommand;
	private String postBuildAnnounce;
	
	// Has the page been changed?
	private boolean dirty = false;

	private ModifyListener widgetModified = new ModifyListener() {
	    public void modifyText(ModifyEvent e) {
	        setDirty(true);
	    }
	};
	
	/*
	 *  Constructor
	 */
	public BuildStepSettingsBlock(BuildPropertyPage parent)
	{
		super(ManagedBuilderUIMessages.getResourceString(SETTINGS_LABEL));
		super.setContainer(parent);
		this.parent = parent;
	}

	public void createControl(Composite parent)  {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(comp);
		
		// Create a group for the prebuild step
		createPreBuildStepGroup(comp);
	
		// Create a group for the postbuild step
		createPostBuildStepGroup(comp);
	}

	/* (non-Javadoc)
	 * Creates the group that contains the pre-build step controls.
	 */
	private void createPreBuildStepGroup(Composite parent) {
		final Group preBuildStepGroup = new Group(parent, SWT.NONE);
		preBuildStepGroup.setFont(parent.getFont());
		preBuildStepGroup.setText(ManagedBuilderUIMessages.getResourceString(PREBUILD_GROUP));
		preBuildStepGroup.setLayout(new GridLayout(1, true));
		preBuildStepGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 

		// Pre-build command label
		final Label cmdLabel = new Label(preBuildStepGroup, SWT.LEFT);
		cmdLabel.setFont(preBuildStepGroup.getFont());
		cmdLabel.setText(ManagedBuilderUIMessages.getResourceString(PREBUILD_CMD));
		cmdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Text widget for pre-build command
		preBuildCmd = new Text(preBuildStepGroup, SWT.SINGLE | SWT.BORDER);
		preBuildCmd.setFont(preBuildStepGroup.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		preBuildCmd.setLayoutData(data);
		preBuildCmd.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				preBuildCmd = null;
			}
		});
		preBuildCmd.getAccessible().addAccessibleListener(new AccessibleAdapter(){
			public void getName(AccessibleEvent e) {
				e.result = ManagedBuilderUIMessages.getResourceString(PREBUILD_CMD);
			}
		});
		preBuildCmd.addModifyListener(widgetModified);
		
		final Label descLabel = new Label(preBuildStepGroup, SWT.LEFT);
		descLabel.setFont(preBuildStepGroup.getFont());
		descLabel.setText(ManagedBuilderUIMessages.getResourceString(PREBUILD_DESC));
		descLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Text widget for the pre-build description		
		preBuildAnnc = new Text(preBuildStepGroup, SWT.SINGLE | SWT.BORDER);
		preBuildAnnc.setFont(preBuildStepGroup.getFont());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = (IDialogConstants.ENTRY_FIELD_WIDTH);
		preBuildAnnc.setLayoutData(data);
		preBuildAnnc.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				preBuildAnnc = null;
			}
		});
		preBuildAnnc.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = ManagedBuilderUIMessages.getResourceString(PREBUILD_DESC);
			}
		});
		preBuildAnnc.addModifyListener(widgetModified);
	}

	/* (non-Javadoc)
	 * Creates the group that contains the post-build step controls.
	 */
	private void createPostBuildStepGroup(Composite parent) {
		final Group postBuildStepGroup = new Group(parent, SWT.NONE);
		postBuildStepGroup.setFont(parent.getFont());
		postBuildStepGroup.setText(ManagedBuilderUIMessages.getResourceString(POSTBUILD_GROUP));
		postBuildStepGroup.setLayout(new GridLayout(1, true));
		postBuildStepGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 

		// Post-build command label
		final Label cmdLabel = new Label(postBuildStepGroup, SWT.LEFT);
		cmdLabel.setFont(postBuildStepGroup.getFont());
		cmdLabel.setText(ManagedBuilderUIMessages.getResourceString(POSTBUILD_CMD));
		cmdLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Text widget for the post-build command
		postBuildCmd = new Text(postBuildStepGroup, SWT.SINGLE | SWT.BORDER);
		postBuildCmd.setFont(postBuildStepGroup.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		postBuildCmd.setLayoutData(data);
		postBuildCmd.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				postBuildCmd = null;
			}
		});
		postBuildCmd.getAccessible().addAccessibleListener(new AccessibleAdapter(){
			public void getName(AccessibleEvent e) {
				e.result = ManagedBuilderUIMessages.getResourceString(POSTBUILD_CMD);
			}
		});
		postBuildCmd.addModifyListener(widgetModified);
		
		// Post-build description label
		final Label descLabel = new Label(postBuildStepGroup, SWT.LEFT);
		descLabel.setFont(postBuildStepGroup.getFont());
		descLabel.setText(ManagedBuilderUIMessages.getResourceString(POSTBUILD_DESC));
		descLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Text widget for the post-build description		
		postBuildAnnc = new Text(postBuildStepGroup, SWT.SINGLE | SWT.BORDER);
		postBuildAnnc.setFont(postBuildStepGroup.getFont());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = (IDialogConstants.ENTRY_FIELD_WIDTH);
		postBuildAnnc.setLayoutData(data);
		postBuildAnnc.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				postBuildAnnc = null;
			}
		});
		postBuildAnnc.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = ManagedBuilderUIMessages.getResourceString(POSTBUILD_DESC);
			}
		});
		postBuildAnnc.addModifyListener(widgetModified);
	}
		
	protected void initializeValues() {
		setValues();
	}

	public void updateValues() {
		setValues();	
	}

	protected void setValues() {
		// Fetch values from the current configuration and set in the UI
		preBuildCommand = parent.getSelectedConfiguration().getPrebuildStep();
		preBuildCmd.setText(preBuildCommand);
		preBuildAnnounce = parent.getSelectedConfiguration().getPreannouncebuildStep();
		preBuildAnnc.setText(preBuildAnnounce);
		postBuildCommand = parent.getSelectedConfiguration().getPostbuildStep();
		postBuildCmd.setText(postBuildCommand);
		postBuildAnnounce = parent.getSelectedConfiguration().getPostannouncebuildStep();
		postBuildAnnc.setText(postBuildAnnounce);					
		setDirty(false);	//Indicate that the UI state is consistent with internal state
	}

	public void removeValues(String id) {
		// Nothing to do...
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		IConfiguration config = parent.getSelectedConfiguration();
		boolean mustSetValue = false;
		
		// Display a "Confirm" dialog box, since:
		//   1.  The defaults are immediately applied
		//   2.  The action cannot be undone
		Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
		boolean shouldDefault = MessageDialog.openConfirm(shell,
					ManagedBuilderUIMessages.getResourceString("BuildStepsSettingsBlock.defaults.title"), //$NON-NLS-1$
					ManagedBuilderUIMessages.getResourceString("BuildStepsSettingsBlock.defaults.message")); //$NON-NLS-1$
		if (!shouldDefault) return;

		// Set the build step entries to null; this will force the next fetch of the entries to get the
		// values from the parent of this configuration, which should be the values from the .xml manifest
		// file 
		config.setPrebuildStep(null);
		config.setPreannouncebuildStep(null);
		config.setPostbuildStep(null);
		config.setPostannouncebuildStep(null);

		// Save the information that was reset
		ManagedBuildManager.setDefaultConfiguration(parent.getProject(), parent.getSelectedConfiguration());
		ManagedBuildManager.saveBuildInfo(parent.getProject(), false);
		
		// Fetch and set the default values to be displayed in the UI
		setValues();
			
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		
		// Fetch the build step values from the UI and store
		preBuildCommand = preBuildCmd.getText().trim();
		preBuildAnnounce = preBuildAnnc.getText().trim();
		postBuildCommand = postBuildCmd.getText().trim();
		postBuildAnnounce = postBuildAnnc.getText().trim();
		
		IConfiguration selectedConfiguration = parent.getSelectedConfiguration();
		boolean mustSetValue = false;
		
		if (!selectedConfiguration.getPrebuildStep().equals(preBuildCommand)) {
			mustSetValue = true;
		}		
		else if (!selectedConfiguration.getPreannouncebuildStep().equals(preBuildAnnounce)) {
			mustSetValue = true;
		}
		else if (!selectedConfiguration.getPostbuildStep().equals(postBuildCommand)) {
			mustSetValue = true;
		}		
		else if (!selectedConfiguration.getPostannouncebuildStep().equals(postBuildAnnounce)) {
			mustSetValue = true;
		}

		if (mustSetValue) {			
			// Set all the build step values in the current configuration
			selectedConfiguration.setPrebuildStep(preBuildCommand);
			selectedConfiguration.setPreannouncebuildStep(preBuildAnnounce);
			selectedConfiguration.setPostbuildStep(postBuildCommand);
			selectedConfiguration.setPostannouncebuildStep(postBuildAnnounce);
		}
		
		setDirty(false);	//Indicate that the UI state is consistent with internal state
	}

	public IPreferenceStore getPreferenceStore() {
		return null;
	}

	/**
	 * Sets the "dirty" state, which indicates whether or not the state of the build step UI is consistent
	 * with its corresponding internal state
	 */
	public void setDirty(boolean b) {
	    dirty = b;
	}

	/**
	 * Returns the "dirty" state, which indicates whether or not the state of the build step UI is consistent
	 * with its corresponding internal state
	 */
	public boolean isDirty() {
	    return dirty;
	}
}
