/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * copied from:
 * org.eclipse.ui.externaltools.internal.launchConfigurations.GenericMainTab
 *******************************************************************************/
package org.eclipse.cdt.launch.ui.corebuild;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.internal.ui.Messages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;

/**
 * @since 9.2
 */
public class GenericMainTab extends AbstractLaunchConfigurationTab {

	public final static String FIRST_EDIT = "editedByExternalToolsMainTab"; //$NON-NLS-1$

	protected Text locationField;
	protected Text workDirectoryField;
	protected Button fileLocationButton;
	protected Button workspaceLocationButton;
	protected Button variablesLocationButton;
	protected Button fileWorkingDirectoryButton;
	protected Button workspaceWorkingDirectoryButton;
	protected Button variablesWorkingDirectoryButton;

	protected Text argumentField;
	protected Button argumentVariablesButton;

	protected SelectionAdapter selectionAdapter;

	protected boolean fInitializing = false;
	private boolean userEdited = false;

	protected WidgetListener fListener = new WidgetListener();

	/**
	 * A listener to update for text modification and widget selection.
	 */
	protected class WidgetListener extends SelectionAdapter implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			if (!fInitializing) {
				setDirty(true);
				userEdited = true;
				updateLaunchConfigurationDialog();
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			setDirty(true);
			Object source = e.getSource();
			if (source == workspaceLocationButton) {
				handleWorkspaceLocationButtonSelected();
			} else if (source == fileLocationButton) {
				handleFileLocationButtonSelected();
			} else if (source == workspaceWorkingDirectoryButton) {
				handleWorkspaceWorkingDirectoryButtonSelected();
			} else if (source == fileWorkingDirectoryButton) {
				handleFileWorkingDirectoryButtonSelected();
			} else if (source == argumentVariablesButton) {
				handleVariablesButtonSelected(argumentField);
			} else if (source == variablesLocationButton) {
				handleVariablesButtonSelected(locationField);
			} else if (source == variablesWorkingDirectoryButton) {
				handleVariablesButtonSelected(workDirectoryField);
			}
		}

	}

	@Override
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);

		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createArgumentComponent(mainComposite);
		createVerticalSpacer(mainComposite, 1);

		Dialog.applyDialogFont(parent);
	}

	/**
	 * Creates the controls needed to edit the location attribute of an external
	 * tool
	 *
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createLocationComponent(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		String locationLabel = getLocationLabel();
		group.setText(locationLabel);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayout(layout);
		group.setLayoutData(gridData);

		locationField = new Text(group, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		locationField.setLayoutData(gridData);
		locationField.addModifyListener(fListener);
		addControlAccessibleListener(locationField, group.getText());

		Composite buttonComposite = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setFont(parent.getFont());

		workspaceLocationButton = createPushButton(buttonComposite, Messages.GenericMainTab_BrowseWorkspace, null);
		workspaceLocationButton.addSelectionListener(fListener);
		addControlAccessibleListener(workspaceLocationButton,
				group.getText() + " " + workspaceLocationButton.getText()); //$NON-NLS-1$

		fileLocationButton = createPushButton(buttonComposite, Messages.GenericMainTab_BrowseFileSystem, null);
		fileLocationButton.addSelectionListener(fListener);
		addControlAccessibleListener(fileLocationButton, group.getText() + " " + fileLocationButton.getText()); //$NON-NLS-1$

		variablesLocationButton = createPushButton(buttonComposite, Messages.GenericMainTab_Variables, null);
		variablesLocationButton.addSelectionListener(fListener);
		addControlAccessibleListener(variablesLocationButton,
				group.getText() + " " + variablesLocationButton.getText()); //$NON-NLS-1$
	}

	/**
	 * Returns the label used for the location widgets. Subclasses may wish to
	 * override.
	 */
	protected String getLocationLabel() {
		return Messages.GenericMainTab_Location;
	}

	/**
	 * Creates the controls needed to edit the working directory attribute of an
	 * external tool
	 *
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		String groupName = getWorkingDirectoryLabel();
		group.setText(groupName);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayout(layout);
		group.setLayoutData(gridData);

		workDirectoryField = new Text(group, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		workDirectoryField.setLayoutData(data);
		workDirectoryField.addModifyListener(fListener);
		addControlAccessibleListener(workDirectoryField, group.getText());

		Composite buttonComposite = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 3;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(gridData);
		buttonComposite.setFont(parent.getFont());

		workspaceWorkingDirectoryButton = createPushButton(buttonComposite, Messages.GenericMainTab_BrowseWorkspace,
				null);
		workspaceWorkingDirectoryButton.addSelectionListener(fListener);
		addControlAccessibleListener(workspaceWorkingDirectoryButton,
				group.getText() + " " + workspaceWorkingDirectoryButton.getText()); //$NON-NLS-1$

		fileWorkingDirectoryButton = createPushButton(buttonComposite, Messages.GenericMainTab_BrowseFileSystem, null);
		fileWorkingDirectoryButton.addSelectionListener(fListener);
		addControlAccessibleListener(fileWorkingDirectoryButton, group.getText() + " " + fileLocationButton.getText()); //$NON-NLS-1$

		variablesWorkingDirectoryButton = createPushButton(buttonComposite, Messages.GenericMainTab_Variables, null);
		variablesWorkingDirectoryButton.addSelectionListener(fListener);
		addControlAccessibleListener(variablesWorkingDirectoryButton,
				group.getText() + " " + variablesWorkingDirectoryButton.getText()); //$NON-NLS-1$
	}

	/**
	 * Return the String to use as the label for the working directory field.
	 * Subclasses may wish to override.
	 */
	protected String getWorkingDirectoryLabel() {
		return Messages.GenericMainTab_WorkingDirectory;
	}

	/**
	 * Creates the controls needed to edit the argument and prompt for argument
	 * attributes of an external tool
	 *
	 * @param parent
	 *            the composite to create the controls in
	 */
	protected void createArgumentComponent(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		String groupName = Messages.GenericMainTab_Arguments;
		group.setText(groupName);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		group.setLayout(layout);
		group.setLayoutData(gridData);
		group.setFont(parent.getFont());

		argumentField = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		argumentField.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent event) {
				if (event.detail == SWT.TRAVERSE_RETURN && (event.stateMask & SWT.MODIFIER_MASK) != 0) {
					event.doit = true;
				}
			}
		});

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		gridData.heightHint = 30;
		argumentField.setLayoutData(gridData);
		argumentField.addModifyListener(fListener);
		addControlAccessibleListener(argumentField, group.getText());

		Composite composite = new Composite(group, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		composite.setLayout(layout);
		composite.setLayoutData(gridData);
		composite.setFont(parent.getFont());

		argumentVariablesButton = createPushButton(composite, Messages.GenericMainTab_Variables, null);
		argumentVariablesButton.addSelectionListener(fListener);
		addControlAccessibleListener(argumentVariablesButton, argumentVariablesButton.getText()); // need to strip the
																									// mnemonic from
																									// buttons

		Label instruction = new Label(group, SWT.NONE);
		instruction.setText(Messages.GenericMainTab_Quotes);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		instruction.setLayoutData(gridData);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(FIRST_EDIT, true);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		fInitializing = true;
		updateLocation(configuration);
		updateWorkingDirectory(configuration);
		updateArgument(configuration);
		fInitializing = false;
		setDirty(false);
	}

	/**
	 * Updates the working directory widgets to match the state of the given launch
	 * configuration.
	 */
	protected void updateWorkingDirectory(ILaunchConfiguration configuration) {
		String workingDir = ""; //$NON-NLS-1$
		try {
			workingDir = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce.getStatus());
		}
		workDirectoryField.setText(workingDir);
	}

	/**
	 * Updates the location widgets to match the state of the given launch
	 * configuration.
	 */
	protected void updateLocation(ILaunchConfiguration configuration) {
		String location = ""; //$NON-NLS-1$
		try {
			location = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce.getStatus());
		}
		locationField.setText(location);
	}

	/**
	 * Updates the argument widgets to match the state of the given launch
	 * configuration.
	 */
	protected void updateArgument(ILaunchConfiguration configuration) {
		String arguments = ""; //$NON-NLS-1$
		try {
			arguments = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce.getStatus());
		}
		argumentField.setText(arguments);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String location = locationField.getText().trim();
		if (location.length() == 0) {
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, (String) null);
		} else {
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_LOCATION, location);
		}

		String workingDirectory = workDirectoryField.getText().trim();
		if (workingDirectory.length() == 0) {
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
		} else {
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
		}

		String arguments = argumentField.getText().trim();
		if (arguments.length() == 0) {
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS, (String) null);
		} else {
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_TOOL_ARGUMENTS, arguments);
		}

		if (userEdited) {
			configuration.setAttribute(FIRST_EDIT, (String) null);
		}
	}

	@Override
	public String getName() {
		return Messages.GenericMainTab_Main;
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		boolean newConfig = false;
		try {
			newConfig = launchConfig.getAttribute(FIRST_EDIT, false);
		} catch (CoreException e) {
			// assume false is correct
		}
		return validateLocation(newConfig) && validateWorkDirectory();
	}

	/**
	 * Validates the content of the location field.
	 */
	protected boolean validateLocation(boolean newConfig) {
		String location = locationField.getText().trim();
		if (location.length() < 1) {
			setErrorMessage(null);
			setMessage(Messages.GenericMainTab_SpecifyLocation);
			return true;
		}

		String expandedLocation = null;
		try {
			expandedLocation = resolveValue(location);
			if (expandedLocation == null) { // a variable that needs to be resolved at runtime
				return true;
			}
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
			return false;
		}

		File file = new File(expandedLocation);
		if (!file.exists()) { // The file does not exist.
			if (!newConfig) {
				setErrorMessage(Messages.GenericMainTab_LocationNotExists);
			}
			return false;
		}
		if (!file.isFile()) {
			if (!newConfig) {
				setErrorMessage(Messages.GenericMainTab_LocationNotAFile);
			}
			return false;
		}
		return true;
	}

	/**
	 * Validates the variables of the given string to determine if all variables are
	 * valid
	 *
	 * @param expression
	 *            expression with variables
	 * @exception CoreException
	 *                if a variable is specified that does not exist
	 */
	private void validateVaribles(String expression) throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		manager.validateStringVariables(expression);
	}

	private String resolveValue(String expression) throws CoreException {
		String expanded = null;
		try {
			expanded = getValue(expression);
		} catch (CoreException e) { // possibly just a variable that needs to be resolved at runtime
			validateVaribles(expression);
			return null;
		}
		return expanded;
	}

	/**
	 * Validates the value of the given string to determine if any/all variables are
	 * valid
	 *
	 * @param expression
	 *            expression with variables
	 * @return whether the expression contained any variable values
	 * @exception CoreException
	 *                if variable resolution fails
	 */
	private String getValue(String expression) throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		return manager.performStringSubstitution(expression);
	}

	/**
	 * Validates the content of the working directory field.
	 */
	protected boolean validateWorkDirectory() {
		String dir = workDirectoryField.getText().trim();
		if (dir.length() <= 0) {
			return true;
		}

		String expandedDir = null;
		try {
			expandedDir = resolveValue(dir);
			if (expandedDir == null) { // a variable that needs to be resolved at runtime
				return true;
			}
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getMessage());
			return false;
		}

		File file = new File(expandedDir);
		if (!file.exists()) { // The directory does not exist.
			setErrorMessage(Messages.GenericMainTab_WorkingDirNotExists);
			return false;
		}
		if (!file.isDirectory()) {
			setErrorMessage(Messages.GenericMainTab_WorkingDirNotADir);
			return false;
		}
		return true;
	}

	/**
	 * Prompts the user to choose a location from the filesystem and sets the
	 * location as the full path of the selected file.
	 */
	protected void handleFileLocationButtonSelected() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(locationField.getText());
		String text = fileDialog.open();
		if (text != null) {
			locationField.setText(text);
		}
	}

	/**
	 * Prompts the user for a workspace location within the workspace and sets the
	 * location as a String containing the workspace_loc variable or
	 * <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceLocationButtonSelected() {
		ResourceSelectionDialog dialog;
		dialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(),
				Messages.GenericMainTab_SelectResource);
		dialog.open();
		Object[] results = dialog.getResult();
		if (results == null || results.length < 1) {
			return;
		}
		IResource resource = (IResource) results[0];
		locationField.setText(newVariableExpression("workspace_loc", resource.getFullPath().toString())); //$NON-NLS-1$
	}

	/**
	 * Prompts the user for a working directory location within the workspace and
	 * sets the working directory as a String containing the workspace_loc variable
	 * or <code>null</code> if no location was obtained from the user.
	 */
	protected void handleWorkspaceWorkingDirectoryButtonSelected() {
		ContainerSelectionDialog containerDialog;
		containerDialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				Messages.GenericMainTab_SelectWorkingDir);
		containerDialog.open();
		Object[] resource = containerDialog.getResult();
		String text = null;
		if (resource != null && resource.length > 0) {
			text = newVariableExpression("workspace_loc", ((IPath) resource[0]).toString()); //$NON-NLS-1$
		}
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}

	/**
	 * Returns a new variable expression with the given variable and the given
	 * argument.
	 *
	 * @see IStringVariableManager#generateVariableExpression(String, String)
	 */
	protected String newVariableExpression(String varName, String arg) {
		return VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(varName, arg);
	}

	/**
	 * Prompts the user to choose a working directory from the filesystem.
	 */
	protected void handleFileWorkingDirectoryButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		dialog.setMessage(Messages.GenericMainTab_SelectWorkingDir);
		dialog.setFilterPath(workDirectoryField.getText());
		String text = dialog.open();
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}

	/**
	 * A variable entry button has been pressed for the given text field. Prompt the
	 * user for a variable and enter the result in the given field.
	 */
	private void handleVariablesButtonSelected(Text textField) {
		String variable = getVariable();
		if (variable != null) {
			textField.insert(variable);
		}
	}

	/**
	 * Prompts the user to choose and configure a variable and returns the resulting
	 * string, suitable to be used as an attribute.
	 */
	private String getVariable() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}

	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_MAIN_TAB);
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
	}

	/*
	 * Fix for Bug 60163 Accessibility: New Builder Dialog missing object info for
	 * textInput controls
	 */
	public void addControlAccessibleListener(Control control, String controlName) {
		// strip mnemonic (&)
		String[] strs = controlName.split("&"); //$NON-NLS-1$
		StringBuffer stripped = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		control.getAccessible().addAccessibleListener(new ControlAccessibleListener(stripped.toString()));
	}

	private class ControlAccessibleListener extends AccessibleAdapter {
		private String controlName;

		ControlAccessibleListener(String name) {
			controlName = name;
		}

		@Override
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}

	}

}
