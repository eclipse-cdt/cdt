/*******************************************************************************
 * Copyright (c) 2006 PalmSource, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Ewa Matejska (PalmSource)
 *******************************************************************************/

package org.eclipse.rse.remotecdt;

import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class RemoteCMainTab extends CMainTab {
 
	private static final String[]  SYSTEM_TYPE = {"Ssh/Gdbserver"};
	/* Labels and Error Messages */
	private static final String REMOTE_PROG_LABEL_TEXT = "Remote Path for C/C++ Application:";
	private static final String SKIP_DOWNLOAD_BUTTON_TEXT = "Skip download to target path.";
	private static final String REMOTE_PROG_TEXT_ERROR = "Remote executable path is not specified.";
	private static final String CONNECTION_TEXT_ERROR = "Remote Connection must be selected.";
	
	/* Defaults */
	private static final String REMOTE_PATH_DEFAULT = EMPTY_STRING;
	private static final boolean SKIP_DOWNLOAD_TO_REMOTE_DEFAULT = false;
	
	protected Button newRemoteConnectionButton;
	protected Label  connectionLabel;
	protected Combo  connectionCombo;
	protected Label remoteProgLabel;
	protected Text remoteProgText;
	protected Button skipDownloadButton;
	protected Button useLocalPathButton;
	
	private boolean initialized = false;
	SystemNewConnectionAction action = null;
	
	public RemoteCMainTab(boolean terminalOption) {
		super(terminalOption);
	}
	
	/*
     * createControl
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl
     */
    public void createControl(Composite parent) {
    	Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
	    setControl(comp);
		comp.setLayout(topLayout);

		/* The RSE Connection dropdown with New button. */
		createVerticalSpacer(comp, 1);
		createRemoteConnectionGroup(comp, 3);
		
		/* The Project and local binary location */
		createVerticalSpacer(comp, 1);
		createProjectGroup(comp, 1);
		createExeFileGroup(comp, 1);
		
		/* The remote binary location and skip download option */
		createVerticalSpacer(comp, 1);
		createTargetExePath(comp);
		createDownloadOption(comp);
 
		/* If the local binary path changes, modify the remote binary location */
		fProgText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				if(initialized)
					setLocalPathForRemotePath();
				else
					initialized = true;
			}
		});
		
		LaunchUIPlugin.setDialogShell(parent.getShell());
    }

	/*
	 * isValid
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid
	 */
	public boolean isValid(ILaunchConfiguration config) {
		boolean retVal =  super.isValid(config);
		if(retVal == true) {
			setErrorMessage(null);
			int currentSelection = connectionCombo.getSelectionIndex();
			String connection_name = currentSelection >= 0 ? connectionCombo.getItem(currentSelection) : "";
			if(connection_name.equals("")) {
				setErrorMessage(CONNECTION_TEXT_ERROR);
				retVal = false;
			}
			if(retVal) {
				String name = remoteProgText.getText().trim();
				if (name.length() == 0) {
					setErrorMessage(REMOTE_PROG_TEXT_ERROR);
					retVal = false;
				}
			}
		}
		return retVal;
	}
	
	protected void createRemoteConnectionGroup(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 3;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		connectionLabel = new Label(projComp, SWT.NONE);
		connectionLabel.setText("Connection:"); 
		gd = new GridData();
		gd.horizontalSpan = 1;
		connectionLabel.setLayoutData(gd);
		
		connectionCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		connectionCombo.setLayoutData(gd);
		connectionCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
		updateConnectionPulldown();

		newRemoteConnectionButton = createPushButton(projComp, "New", null);
		newRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {

		public void widgetSelected(SelectionEvent evt) {
				handleNewRemoteConnectionSelected();
				updateLaunchConfigurationDialog();
				updateConnectionPulldown();
			}
		});	

	}
	
	/*
	 * createTargetExePath
	 * This creates the remote path user-editable textfield on the Main Tab.
	 */
	protected void createTargetExePath(Composite parent) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);
		remoteProgLabel = new Label(mainComp, SWT.NONE);
		remoteProgLabel.setText(REMOTE_PROG_LABEL_TEXT);
		gd = new GridData();
		remoteProgLabel.setLayoutData(gd);
		remoteProgText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		remoteProgText.setLayoutData(gd);
		remoteProgText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	}
    
	/*
	 * createDownloadOption
	 * This creates the skip download check button.
	 */
	protected void createDownloadOption(Composite parent) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);

		skipDownloadButton = createCheckButton(mainComp, SKIP_DOWNLOAD_BUTTON_TEXT);
		skipDownloadButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		skipDownloadButton.setEnabled(true);
	}
	
	/*
	 * performApply
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		
		int currentSelection = connectionCombo.getSelectionIndex();
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION, 
				 currentSelection >= 0 ? connectionCombo.getItem(currentSelection) : null);
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH, 
				remoteProgText.getText());
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET, 
				skipDownloadButton.getSelection());
		super.performApply(config);
	}
	
	/*
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		String remoteConnection = null;
		try {
			remoteConnection = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
						 "");
		} catch (CoreException ce) {
			/* default to doing nothing */
		}
		
		String[] items = connectionCombo.getItems();
		int i = 0;
		for(i = 0; i < items.length; i++)
			if(items[i].equals(remoteConnection))
				break;
		/* Select the last used connection in the connecion pulldown if it still exists. */
		if(i < items.length)
			connectionCombo.select(i);
		else if(items.length > 0)
			connectionCombo.select(0);
		
		super.initializeFrom(config);
		
		updateTargetProgFromConfig(config);
		updateSkipDownloadFromConfig(config);
	}
	
	protected void handleNewRemoteConnectionSelected() {
    	if (action == null)
		{
		  action = new SystemNewConnectionAction(getControl().getShell(), false, false, null);
		}
		action.restrictSystemTypes(SYSTEM_TYPE);
		
		try 
		{
		  action.run();
		} catch (Exception exc)
		{
			/* Ignore for now */
		}
	}
	
	protected void updateConnectionPulldown() {
		connectionCombo.removeAll();
		IHost[] connections = RSEUIPlugin.getTheSystemRegistry().getHostsBySystemType(SYSTEM_TYPE[0]);
		for(int i = 0; i < connections.length; i++)
			connectionCombo.add(connections[i].getAliasName());
		if(connections.length > 0)
			connectionCombo.select(0);
	}
    
	protected void updateTargetProgFromConfig(ILaunchConfiguration config) {
		String targetPath = null;
		try {
			targetPath = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
						 REMOTE_PATH_DEFAULT);
		} catch (CoreException ce) {
			/* Ignore for now */
		}
		remoteProgText.setText(targetPath);
	}
	
	protected void updateSkipDownloadFromConfig(ILaunchConfiguration config) {
		boolean downloadToTarget = true;
		try {
			downloadToTarget = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET, 
							   SKIP_DOWNLOAD_TO_REMOTE_DEFAULT);
		} catch (CoreException e) {
			/* Ignore for now */
		}
		skipDownloadButton.setSelection(downloadToTarget);
	}
	
	/*
	 * setLocalPathForRemotePath
	 * This function sets the remote path text field with the value of the 
	 * local executable path.
	 */
	private void setLocalPathForRemotePath() {
		String name = fProgText.getText().trim();
		if (name.length() != 0) {
			IProject project = getCProject().getProject();
			IPath exePath = new Path(name);
			if (!exePath.isAbsolute()) {
				exePath = project.getFile(name).getLocation();
			}
			String path = exePath.toString();
			remoteProgText.setText(path);
		}
	}
}
