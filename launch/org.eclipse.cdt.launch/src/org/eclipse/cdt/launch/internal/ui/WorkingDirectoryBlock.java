/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.internal.ui;
import java.io.File;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * A control for setting the working directory associated with a launch
 * configuration.
 */
public class WorkingDirectoryBlock extends AbstractLaunchConfigurationTab {
			
	// Working directory UI widgets
	protected Label fWorkingDirLabel;
	
	// Local directory
	protected Button fLocalDirButton;
	protected Text fWorkingDirText;
	protected Button fWorkingDirBrowseButton;
	
	
	// Workspace directory
	protected Button fWorkspaceDirButton;
	protected Text fWorkspaceDirText;
	protected Button fWorkspaceDirBrowseButton;
		
	// use default button
	protected Button fUseDefaultWorkingDirButton;

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * The last launch config this tab was initialized from
	 */
	protected ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
						
		Composite workingDirComp = new Composite(parent, SWT.NONE);
//		WorkbenchHelp.setHelp(workingDirComp, IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK);;		
		GridLayout workingDirLayout = new GridLayout();
		workingDirLayout.numColumns = 3;
		workingDirLayout.marginHeight = 0;
		workingDirLayout.marginWidth = 0;
		workingDirComp.setLayout(workingDirLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		workingDirComp.setLayoutData(gd);
		setControl(workingDirComp);
		
		fWorkingDirLabel = new Label(workingDirComp, SWT.NONE);
		fWorkingDirLabel.setText(LaunchMessages.getString("WorkingDirectoryBlock.Wor&king_directory")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fWorkingDirLabel.setLayoutData(gd);

		fUseDefaultWorkingDirButton = new Button(workingDirComp,SWT.CHECK);
		fUseDefaultWorkingDirButton.setText(LaunchMessages.getString("WorkingDirectoryBlock.Use_de&fault_working_directory")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fUseDefaultWorkingDirButton.setLayoutData(gd);
		fUseDefaultWorkingDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleUseDefaultWorkingDirButtonSelected();
			}
		});
		
		fLocalDirButton = createRadioButton(workingDirComp, LaunchMessages.getString("WorkingDirectoryBlock.&Local_directory")); //$NON-NLS-1$
		fLocalDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleLocationButtonSelected();
			}
		});
		
		fWorkingDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWorkingDirText.setLayoutData(gd);
		fWorkingDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fWorkingDirBrowseButton = createPushButton(workingDirComp, LaunchMessages.getString("Launch.common.Browse_1"), null); //$NON-NLS-1$
		fWorkingDirBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkingDirBrowseButtonSelected();
			}
		});
		
		fWorkspaceDirButton = createRadioButton(workingDirComp, LaunchMessages.getString("WorkingDirectoryBlock.Works&pace")); //$NON-NLS-1$
		fWorkspaceDirButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleLocationButtonSelected();
			}
		});		
		
		fWorkspaceDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWorkspaceDirText.setLayoutData(gd);
		fWorkspaceDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		fWorkspaceDirBrowseButton = createPushButton(workingDirComp, LaunchMessages.getString("Launch.common.Browse_2"), null); //$NON-NLS-1$
		fWorkspaceDirBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkspaceDirBrowseButtonSelected();
			}
		});		
								
	}
					
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}
		
	/**
	 * Show a dialog that lets the user select a working directory
	 */
	protected void handleWorkingDirBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(LaunchMessages.getString("WorkingDirectoryBlock.Select_&working_directory_for_launch_configuration")); //$NON-NLS-1$
		String currentWorkingDir = fWorkingDirText.getText();
		if (!currentWorkingDir.trim().equals(EMPTY_STRING)) {
			File path = new File(currentWorkingDir);
			if (path.exists()) {
				dialog.setFilterPath(currentWorkingDir);
			}			
		}
		
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			fWorkingDirText.setText(selectedDirectory);
		}		
	}

	/**
	 * Show a dialog that lets the user select a working directory from 
	 * the workspace
	 */
	protected void handleWorkspaceDirBrowseButtonSelected() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
																	   ResourcesPlugin.getWorkspace().getRoot(),
																	   false,
																	   LaunchMessages.getString("WorkingDirectoryBlock.Select_&workspace_relative_working_directory")); //$NON-NLS-1$
		
		IContainer currentContainer = getContainer();
		if (currentContainer != null) {
			IPath path = currentContainer.getFullPath();
			dialog.setInitialSelections(new Object[] {path});
		}
		
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] results = dialog.getResult();		
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath)results[0];
			String containerName = path.makeRelative().toString();
			fWorkspaceDirText.setText(containerName);
		}			
	}
	
	/**
	 * Returns the selected workspace container,or <code>null</code>
	 */
	protected IContainer getContainer() {
		IResource res = getResource();
		if (res instanceof IContainer) {
			return (IContainer)res;
		}
		return null;
	}
	
	/**
	 * Returns the selected workspace resource, or <code>null</code>
	 */
	protected IResource getResource() {
		IPath path = new Path(fWorkspaceDirText.getText());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.findMember(path);
	}	
	
	/**
	 * The "local directory" or "workspace directory" button has been selected.
	 */
	protected void handleLocationButtonSelected() {
		if (!isDefaultWorkingDirectory()) {
			boolean local = isLocalWorkingDirectory();
			fWorkingDirText.setEnabled(local);
			fWorkingDirBrowseButton.setEnabled(local);
			fWorkspaceDirText.setEnabled(!local);
			fWorkspaceDirBrowseButton.setEnabled(!local);
		}
		updateLaunchConfigurationDialog();
	}
		
	/**
	 * The default working dir check box has been toggled.
	 */
	protected void handleUseDefaultWorkingDirButtonSelected() {
		if (isDefaultWorkingDirectory()) {
			setDefaultWorkingDir();
			fLocalDirButton.setEnabled(false);
			fWorkingDirText.setEnabled(false);
			fWorkingDirBrowseButton.setEnabled(false);
			fWorkspaceDirButton.setEnabled(false);
			fWorkspaceDirText.setEnabled(false);
			fWorkspaceDirBrowseButton.setEnabled(false);
		} else {
			fLocalDirButton.setEnabled(true);
			fWorkspaceDirButton.setEnabled(true);
			handleLocationButtonSelected();
		}
	}
	
	/**
	 * Sets the default working directory
	 */
	protected void setDefaultWorkingDir() {
		ILaunchConfiguration config = getLaunchConfiguration();
		if (config != null) {
			ICProject cProject = null;
			try {
				cProject = AbstractCLaunchDelegate.getCProject(config);
			} catch (CoreException e) {
			}
			if (cProject != null) {
				fWorkspaceDirText.setText(cProject.getPath().makeRelative().toOSString());
				fLocalDirButton.setSelection(false);
				fWorkspaceDirButton.setSelection(true);
				return;
			}
		}
		
		fWorkingDirText.setText(System.getProperty("user.dir")); //$NON-NLS-1$
		fLocalDirButton.setSelection(true);
		fWorkspaceDirButton.setSelection(false);		
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		
		setErrorMessage(null);
		setMessage(null);
		
		if (isLocalWorkingDirectory()) {
			String workingDirPath = fWorkingDirText.getText().trim();
			if (workingDirPath.length() > 0) {
				File dir = new File(workingDirPath);
				if (!dir.exists()) {
					setErrorMessage(LaunchMessages.getString("WorkingDirectoryBlock.Working_directory_does_not_exist")); //$NON-NLS-1$
					return false;
				}
				if (!dir.isDirectory()) {
					setErrorMessage(LaunchMessages.getString("WorkingDirectoryBlock.Working_directory_is_not_a_directory")); //$NON-NLS-1$
					return false;
				}
			}
		} else {
			if (getContainer() == null) {
				setErrorMessage(LaunchMessages.getString("WorkingDirectoryBlock.Project_or_folder_does_not_exist")); //$NON-NLS-1$
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		setLaunchConfiguration(configuration);
		try {			
			String wd = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
			fWorkspaceDirText.setText(EMPTY_STRING);
			fWorkingDirText.setText(EMPTY_STRING);
			if (wd == null) {
				fUseDefaultWorkingDirButton.setSelection(true);
			} else {
				IPath path = new Path(wd);
				if (path.isAbsolute()) {
					fWorkingDirText.setText(wd);
					fLocalDirButton.setSelection(true);
					fWorkspaceDirButton.setSelection(false);
				} else {
					fWorkspaceDirText.setText(wd);
					fWorkspaceDirButton.setSelection(true);
					fLocalDirButton.setSelection(false);
				}
				fUseDefaultWorkingDirButton.setSelection(false);
			}
			handleUseDefaultWorkingDirButtonSelected();
		} catch (CoreException e) {
			setErrorMessage(LaunchMessages.getFormattedString("Launch.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage())); //$NON-NLS-1$
			LaunchUIPlugin.log(e);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String wd = null;
		if (!isDefaultWorkingDirectory()) {
			if (isLocalWorkingDirectory()) {
				wd = getAttributeValueFrom(fWorkingDirText);
			} else {
				IPath path = new Path(fWorkspaceDirText.getText());
				path = path.makeRelative();
				wd = path.toString();
			}
		} 
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, wd);
	}

	/**
	 * Retuns the string in the text widget, or <code>null</code> if empty.
	 * 
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}
	
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchMessages.getString("WorkingDirectoryBlock.Working_Directory"); //$NON-NLS-1$
	}	
	
	/**
	 * Returns whether the default working directory is to be used
	 */
	protected boolean isDefaultWorkingDirectory() {
		return fUseDefaultWorkingDirButton.getSelection();
	}
	
	/**
	 * Returns whether the working directory is local
	 */
	protected boolean isLocalWorkingDirectory() {
		return fLocalDirButton.getSelection();
	}

	/**
	 * Sets the java project currently specified by the
	 * given launch config, if any.
	 */
	protected void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}	
	
	/**
	 * Returns the current java project context
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

}

