/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Intel corporation - cloned to CDT UI, to avoid discouraged access
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.TextProcessor;
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

import org.eclipse.cdt.internal.ui.newui.Messages;
/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ProjectContentsArea {
	private static final String ERROR_INVALID_PATH = Messages.ProjectContentsArea_3;
	private static final String ERROR_PATH_EMPTY = Messages.ProjectContentsArea_4;
	private static final String ERROR_NOT_ABSOLUTE = Messages.ProjectContentsArea_6;
	private static final String ERROR_NOT_VALID = Messages.ProjectContentsArea_7;
	private static final String ERROR_CANNOT_CREATE = Messages.ProjectContentsArea_8;
	private static final String ERROR_FILE_EXISTS = Messages.ProjectContentsArea_9;

	private static final String BROWSE_LABEL = Messages.ProjectContentsArea_0;
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$
	private Label locationLabel;
	private Text locationPathField;
	private Button browseButton;
	private IErrorMessageReporter errorReporter;
	private String projectName = AbstractPage.EMPTY_STR;
	private String userPath = AbstractPage.EMPTY_STR;
	private Button useDefaultsButton;
	private IProject existingProject;

	/**
	 * Create a new instance of a ProjectContentsLocationArea.
	 *
	 * @param composite
	 */
	public ProjectContentsArea(IErrorMessageReporter er, Composite composite) {
		errorReporter = er;
		createContents(composite, true);
	}

	/**
	 * Create the contents of the receiver.
	 *
	 * @param composite
	 * @param defaultEnabled
	 */
	private void createContents(Composite composite, boolean defaultEnabled) {
		// project specification group
		Composite projectGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		useDefaultsButton = new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
		useDefaultsButton.setText(Messages.ProjectContentsArea_1);
		useDefaultsButton.setSelection(defaultEnabled);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 4;
		useDefaultsButton.setLayoutData(buttonData);

		createUserEntryArea(projectGroup, defaultEnabled);

		useDefaultsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean useDefaults = useDefaultsButton.getSelection();

				if (useDefaults) {
					userPath = locationPathField.getText();
					locationPathField.setText(TextProcessor
							.process(getDefaultPathDisplayString()));
				} else {
					locationPathField.setText(TextProcessor.process(userPath));
				}
				setUserAreaEnabled(!useDefaults);
			}
		});
		setUserAreaEnabled(!defaultEnabled);
	}

	/**
	 * Return whether or not we are currently showing the default location for
	 * the project.
	 *
	 * @return boolean
	 */
	public boolean isDefault() {
		return useDefaultsButton.getSelection();
	}

	/**
	 * Create the area for user entry.
	 *
	 * @param composite
	 * @param defaultEnabled
	 */
	private void createUserEntryArea(Composite composite, boolean defaultEnabled) {
		// location label
		locationLabel = new Label(composite, SWT.NONE);
		locationLabel.setText(Messages.ProjectContentsArea_2);

		// project location entry field
		locationPathField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		data.horizontalSpan = 2;
		locationPathField.setLayoutData(data);

		// browse button
		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(BROWSE_LABEL);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});

		if (defaultEnabled) {
			locationPathField.setText(TextProcessor
					.process(getDefaultPathDisplayString()));
		} else {
			if (existingProject == null) {
				locationPathField.setText(AbstractPage.EMPTY_STR);
			} else {
				locationPathField.setText(TextProcessor.process(existingProject
						.getLocation().toString()));
			}
		}

		locationPathField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				errorReporter.reportError(checkValidLocation());
			}
		});
	}

	/**
	 * Return the path we are going to display. If it is a file URI then remove
	 * the file prefix.
	 *
	 * @return String
	 */
	private String getDefaultPathDisplayString() {

		URI defaultURI = null;
		if (existingProject != null) {
			defaultURI = existingProject.getLocationURI();
		}

		// Handle files specially. Assume a file if there is no project to query
		if (defaultURI == null || defaultURI.getScheme().equals(FILE_SCHEME)) {
			return Platform.getLocation().append(projectName).toString();
		}
		return defaultURI.toString();

	}

	/**
	 * Set the enablement state of the receiver.
	 *
	 * @param enabled
	 */
	private void setUserAreaEnabled(boolean enabled) {
		locationLabel.setEnabled(enabled);
		locationPathField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	/**
	 * Return the browse button. Usually referenced in order to set the layout
	 * data for a dialog.
	 *
	 * @return Button
	 */
	public Button getBrowseButton() {
		return browseButton;
	}

	/**
	 * Open an appropriate directory browser
	 */
	private void handleLocationBrowseButtonPressed() {

		String selectedDirectory = null;
		String dirName = getPathFromLocationField();

		if (!dirName.equals(AbstractPage.EMPTY_STR)) {
			File f = new Path(dirName).toFile();
			if (!f.exists()) dirName = AbstractPage.EMPTY_STR;
		}

		DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
		dialog.setMessage(Messages.ProjectContentsArea_5);
		dialog.setFilterPath(dirName);
		selectedDirectory = dialog.open();

		if (selectedDirectory != null)
			updateLocationField(selectedDirectory);
	}

	/**
	 * Update the location field based on the selected path.
	 *
	 * @param selectedPath
	 */
	private void updateLocationField(String selectedPath) {
		locationPathField.setText(TextProcessor.process(selectedPath));
	}

	/**
	 * Return the path on the location field.
	 *
	 * @return String
	 */
	private String getPathFromLocationField() {
		URI fieldURI;
		try {
			fieldURI = new URI(locationPathField.getText());
		} catch (URISyntaxException e) {
			return locationPathField.getText();
		}
		return fieldURI.getPath();
	}

	/**
	 * Check if the entry in the widget location is valid. If it is valid return
	 * null. Otherwise return a string that indicates the problem.
	 *
	 * @return String
	 */
	private String checkValidLocation() {

		if (isDefault()) return null;

		String locationFieldContents = locationPathField.getText();

		if (locationFieldContents.length() == 0)
			return ERROR_PATH_EMPTY;

		URI newPath = getProjectLocationURI();

		if (newPath == null)
			return ERROR_INVALID_PATH;

		if (!Path.EMPTY.isValidPath(locationFieldContents))
			return ERROR_NOT_VALID;

		Path p = new Path(locationFieldContents);

		if (!p.isAbsolute())
			return ERROR_NOT_ABSOLUTE;

		// try to create dummy file
		File f = p.toFile();
		if (!f.exists()) {
			boolean result = false;
			try {
				result = f.createNewFile();
			} catch (IOException e) {}

			if (result)
				f.delete();
			else
				return ERROR_CANNOT_CREATE;
		} else {
			if (f.isFile())
				return ERROR_FILE_EXISTS;
		}

		//create a dummy project for the purpose of validation if necessary
		IProject project = existingProject;
		if (project == null) {
			String name = new Path(locationFieldContents).lastSegment();
			if (name != null && Path.EMPTY.isValidSegment(name))
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			else
				return ERROR_INVALID_PATH;
		}
		IStatus locationStatus = project.getWorkspace().validateProjectLocationURI(project, newPath);

		if (!locationStatus.isOK()) {
			return locationStatus.getMessage();
		}
		if (existingProject != null) {
			URI projectPath = existingProject.getLocationURI();
			if (projectPath != null && URIUtil.equals(projectPath, newPath))
				return ERROR_INVALID_PATH;
		}

		return null;
	}

	/**
	 * Get the URI for the location field if possible.
	 * @return URI or <code>null</code> if it is not valid.
	 */
	public URI getProjectLocationURI() {
		return URIUtil.toURI(locationPathField.getText());
	}

	/**
	 * Set the text to the default or clear it if not using the defaults.
	 * @param newName
	 *            the name of the project to use. If <code>null</code> use the
	 *            existing project name.
	 */
	public void updateProjectName(String newName) {
		projectName = newName;
		if (isDefault())
			locationPathField.setText(TextProcessor.process(getDefaultPathDisplayString()));
	}

	/**
	 * Return the location for the project. If we are using defaults then return
	 * the workspace root so that core creates it with default values.
	 *
	 * @return String
	 */
	public String getProjectLocation() {
		return isDefault() ?
			Platform.getLocation().toString():
			locationPathField.getText();
	}

	/**
	 * IErrorMessageReporter is an interface for type that allow message
	 * reporting. Null means "clear error messages area".
	 */
	public interface IErrorMessageReporter {
		public void reportError(String errorMessage);
	}
}
