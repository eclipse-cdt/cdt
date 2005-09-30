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
	
	// Has the page been changed?
	private boolean dirty = false;

	private ModifyListener widgetModified = new ModifyListener() {
	    public void modifyText(ModifyEvent e) {
	    	IConfiguration config = parent.getSelectedConfigurationClone();
	    	if(e.widget == preBuildCmd){
	    		String val = preBuildCmd.getText().trim();
	    		if(!val.equals(config.getPrebuildStep())){
	    			config.setPrebuildStep(val);
	    			setValues();
	    			setDirty(true);
	    		}
	    	} else if(e.widget == preBuildAnnc){
	    		String val = preBuildAnnc.getText().trim();
	    		if(!val.equals(config.getPreannouncebuildStep())){
	    			config.setPreannouncebuildStep(val);
	    			setValues();
	    			setDirty(true);
	    		}
	    	} else if(e.widget == postBuildCmd){
	    		String val = postBuildCmd.getText().trim();
	    		if(!val.equals(config.getPostbuildStep())){
	    			config.setPostbuildStep(val);
	    			setValues();
	    			setDirty(true);
	    		}
	    	} else if(e.widget == postBuildAnnc){
	    		String val = postBuildAnnc.getText().trim();
	    		if(!val.equals(config.getPostannouncebuildStep())){
	    			config.setPostannouncebuildStep(val);
	    			setValues();
	    			setDirty(true);
	    		}
	    	}
	        
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
		setDirty(false);
	}

	public void updateValues() {
		setValues();	
	}

	protected void setValues() {
		// Fetch values from the current configuration and set in the UI
		IConfiguration config = parent.getSelectedConfigurationClone();
		if(!config.getPrebuildStep().equals(preBuildCmd.getText()))
			preBuildCmd.setText(config.getPrebuildStep());
		
		if(!config.getPreannouncebuildStep().equals(preBuildAnnc.getText()))
			preBuildAnnc.setText(config.getPreannouncebuildStep());
		
		if(!config.getPostbuildStep().equals(postBuildCmd.getText()))
			postBuildCmd.setText(config.getPostbuildStep());
		
		if(!config.getPostannouncebuildStep().equals(postBuildAnnc.getText()))
			postBuildAnnc.setText(config.getPostannouncebuildStep());					
	}

	public void removeValues(String id) {
		// Nothing to do...
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		IConfiguration cloneConfig = parent.getSelectedConfigurationClone();

		cloneConfig.setPrebuildStep(null);
		cloneConfig.setPreannouncebuildStep(null);
		cloneConfig.setPostbuildStep(null);
		cloneConfig.setPostannouncebuildStep(null);

		// Fetch and set the default values to be displayed in the UI
		setValues();
		setDirty(true);
			
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		
		IConfiguration selectedConfiguration = parent.getSelectedConfiguration();
		IConfiguration cloneConfig = parent.getSelectedConfigurationClone();
		
		if (!selectedConfiguration.getPrebuildStep().equals(
				cloneConfig.getPrebuildStep())) {
			selectedConfiguration.setPrebuildStep(cloneConfig.getPrebuildStep());
		}		
		if (!selectedConfiguration.getPreannouncebuildStep().equals(
				cloneConfig.getPreannouncebuildStep())) {
			selectedConfiguration.setPreannouncebuildStep(cloneConfig.getPreannouncebuildStep());
		}
		if (!selectedConfiguration.getPostbuildStep().equals(
				cloneConfig.getPostbuildStep())) {
			selectedConfiguration.setPostbuildStep(cloneConfig.getPostbuildStep());
		}		
		if (!selectedConfiguration.getPostannouncebuildStep().equals(
				cloneConfig.getPostannouncebuildStep())) {
			selectedConfiguration.setPostannouncebuildStep(cloneConfig.getPostannouncebuildStep());
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
	
	public void setVisible(boolean visible){
		if(visible)
			setValues();
		super.setVisible(visible);
	}
}
