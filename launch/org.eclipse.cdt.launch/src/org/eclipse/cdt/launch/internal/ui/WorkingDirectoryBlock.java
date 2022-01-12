/*******************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import java.io.File;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * A control for setting the working directory associated with a launch
 * configuration.
 */
public class WorkingDirectoryBlock extends CLaunchConfigurationTab {

	// Local directory
	protected Text fWorkingDirText;
	protected Button fWorkspaceButton;
	protected Button fFileSystemButton;
	protected Button fVariablesButton;

	// use default button
	protected Button fUseDefaultWorkingDirButton;

	/**
	 * The last launch config this tab was initialized from
	 */
	protected ILaunchConfiguration fLaunchConfiguration;

	/**
	 * A listener to update for text changes and widget selection
	 */
	private class WidgetListener extends SelectionAdapter implements ModifyListener {

		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == fWorkspaceButton) {
				handleWorkspaceDirBrowseButtonSelected();
			} else if (source == fFileSystemButton) {
				handleWorkingDirBrowseButtonSelected();
			} else if (source == fUseDefaultWorkingDirButton) {
				handleUseDefaultWorkingDirButtonSelected();
			}
		}
	}

	private WidgetListener fListener = new WidgetListener();

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Group group = new Group(parent, SWT.NONE);
		//		WorkbenchHelp.setHelp(group,
		// IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK);
		GridLayout workingDirLayout = new GridLayout();
		workingDirLayout.makeColumnsEqualWidth = false;
		group.setLayout(workingDirLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setFont(font);
		setControl(group);

		group.setText(LaunchMessages.WorkingDirectoryBlock_Working_directory);

		fWorkingDirText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fWorkingDirText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = LaunchMessages.WorkingDirectoryBlock_Working_directory;
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fWorkingDirText.setLayoutData(gd);
		fWorkingDirText.setFont(font);
		fWorkingDirText.addModifyListener(fListener);

		fUseDefaultWorkingDirButton = new Button(group, SWT.CHECK);
		fUseDefaultWorkingDirButton.setText(LaunchMessages.WorkingDirectoryBlock_Use_default);
		gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		fUseDefaultWorkingDirButton.setLayoutData(gd);
		fUseDefaultWorkingDirButton.setFont(font);
		fUseDefaultWorkingDirButton.addSelectionListener(fListener);

		Composite buttonComp = new Composite(group, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonComp.setLayout(layout);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComp.setLayoutData(gd);
		buttonComp.setFont(font);
		fWorkspaceButton = createPushButton(buttonComp, LaunchMessages.WorkingDirectoryBlock_0, null);
		fWorkspaceButton.addSelectionListener(fListener);

		fFileSystemButton = createPushButton(buttonComp, LaunchMessages.WorkingDirectoryBlock_1, null);
		fFileSystemButton.addSelectionListener(fListener);

		fVariablesButton = createVariablesButton(buttonComp, LaunchMessages.WorkingDirectoryBlock_17, null);
	}

	@Override
	public void dispose() {
	}

	/**
	 * Show a dialog that lets the user select a working directory
	 */
	protected void handleWorkingDirBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(LaunchMessages.WorkingDirectoryBlock_7);
		String currentWorkingDir = fWorkingDirText.getText();
		if (!currentWorkingDir.trim().isEmpty()) {
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
	 * Show a dialog that lets the user select a working directory from the
	 * workspace
	 */
	protected void handleWorkspaceDirBrowseButtonSelected() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
				ResourcesPlugin.getWorkspace().getRoot(), false, LaunchMessages.WorkingDirectoryBlock_4);

		IContainer currentContainer = getContainer();
		if (currentContainer != null) {
			IPath path = currentContainer.getFullPath();
			dialog.setInitialSelections(new Object[] { path });
		}

		dialog.showClosedProjects(false);
		dialog.open();
		Object[] results = dialog.getResult();
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath) results[0];
			String containerName = path.makeRelative().toString();
			fWorkingDirText.setText("${workspace_loc:" + containerName + "}"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Returns the selected workspace container,or <code>null</code>
	 */
	protected IContainer getContainer() {
		String path = fWorkingDirText.getText().trim();
		if (path.length() > 0) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource res = root.findMember(path);
			if (res instanceof IContainer) {
				return (IContainer) res;
			}
		}
		return null;
	}

	/**
	 * The default working dir check box has been toggled.
	 */
	protected void handleUseDefaultWorkingDirButtonSelected() {
		boolean def = isDefaultWorkingDirectory();
		if (def) {
			setDefaultWorkingDir();
		}
		fWorkingDirText.setEnabled(!def);
		fWorkspaceButton.setEnabled(!def);
		fVariablesButton.setEnabled(!def);
		fFileSystemButton.setEnabled(!def);
	}

	/**
	 * Sets the default working directory
	 */
	protected void setDefaultWorkingDir() {
		try {
			ILaunchConfiguration config = getLaunchConfiguration();
			if (config != null) {
				ICProject cProject = CDebugUtils.getCProject(config);
				if (cProject != null) {
					fWorkingDirText.setText("${workspace_loc:" + cProject.getPath().makeRelative().toOSString() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
			}
		} catch (CoreException ce) {
		}
		fWorkingDirText.setText(System.getProperty("user.dir")); //$NON-NLS-1$
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {

		setErrorMessage(null);
		setMessage(null);

		// if variables are present, we cannot resolve the directory
		String workingDirPath = fWorkingDirText.getText().trim();
		if (workingDirPath.indexOf("${") >= 0) { //$NON-NLS-1$
			IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
			try {
				manager.validateStringVariables(workingDirPath);
			} catch (CoreException e) {
				setErrorMessage(e.getMessage());
				return false;
			}
		} else if (workingDirPath.length() > 0) {
			IContainer container = getContainer();
			if (container == null) {
				File dir = new File(workingDirPath);
				if (dir.isDirectory()) {
					return true;
				}
				setErrorMessage(LaunchMessages.WorkingDirectoryBlock_10);
				return false;
			}
		}
		return true;
	}

	/**
	 * Defaults are empty.
	 *
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		//		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
		// (String)null);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setLaunchConfiguration(configuration);
		try {
			String wd = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
			fWorkingDirText.setText(""); //$NON-NLS-1$
			if (wd == null) {
				fUseDefaultWorkingDirButton.setSelection(true);
			} else {
				fWorkingDirText.setText(wd);
				fUseDefaultWorkingDirButton.setSelection(false);
			}
			handleUseDefaultWorkingDirButtonSelected();
		} catch (CoreException e) {
			setErrorMessage(LaunchMessages.WorkingDirectoryBlock_Exception_occurred_reading_configuration_15
					+ e.getStatus().getMessage());
			LaunchUIPlugin.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String wd = null;
		if (!isDefaultWorkingDirectory()) {
			wd = getAttributeValueFrom(fWorkingDirText);
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

	@Override
	public String getName() {
		return LaunchMessages.WorkingDirectoryBlock_Working_Directory_8;
	}

	/**
	 * Returns whether the default working directory is to be used
	 */
	protected boolean isDefaultWorkingDirectory() {
		return fUseDefaultWorkingDirButton.getSelection();
	}

	/**
	 * Sets the c project currently specified by the given launch config, if
	 * any.
	 */
	protected void setLaunchConfiguration(ILaunchConfiguration config) {
		fLaunchConfiguration = config;
	}

	/**
	 * Returns the current c project context
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

}
