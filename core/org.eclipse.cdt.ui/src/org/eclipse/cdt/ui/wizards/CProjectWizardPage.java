package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;

import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;

/**
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new CProjectWizardPage("basicCProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class CProjectWizardPage extends WizardPage {

	protected boolean useDefaults = true;

	// initial value stores
	private String initialProjectFieldValue;
	private IPath initialLocationFieldValue;
	
	// widgets
	private Text projectNameField;
	protected Text locationPathField;
	protected Label locationLabel;
	protected Button browseButton;

	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setLocationForSelection();
			setPageComplete(validatePage());
		}
	};

	private Listener locationModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private static final int SIZING_INDENTATION_WIDTH = 10;

	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
	
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	
		createProjectNameGroup(composite);
		createProjectLocationGroup(composite);
		projectNameField.setFocus();
		validatePage();
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	/**
	 * Creates the project location specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectLocationGroup(Composite parent) {

		// project specification group
		Composite projectGroup = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button useDefaultsButton = new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
		useDefaultsButton.setText(CPlugin.getResourceString("CProjectWizardPage.useDefaultLabel")); //$NON-NLS-1$
		useDefaultsButton.setSelection(this.useDefaults);

		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		useDefaultsButton.setLayoutData(buttonData);

		createUserSpecifiedProjectLocationGroup(projectGroup,!this.useDefaults);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useDefaults = useDefaultsButton.getSelection();
				browseButton.setEnabled(!useDefaults);
				locationPathField.setEnabled(!useDefaults);
				locationLabel.setEnabled(!useDefaults);
				setLocationForSelection();
				if (!useDefaults)
					locationPathField.setText(""); //$NON-NLS-1$
			}
		};
		useDefaultsButton.addSelectionListener(listener);
	}

	/**
	 * Creates the project name specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectNameGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup,SWT.NONE);
		projectLabel.setText(CPlugin.getResourceString("CProjectWizardPage.nameLabel")); //$NON-NLS-1$

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(data);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (initialProjectFieldValue != null)
			projectNameField.setText(initialProjectFieldValue);
		projectNameField.addListener(SWT.Modify, nameModifyListener);
		projectNameField.setVisible(true);
	}

	/**
	 * Creates the project location specification controls.
	 *
	 * @param projectGroup the parent composite
	 * @param boolean - the initial enabled state of the widgets created
	 */
	private void createUserSpecifiedProjectLocationGroup(Composite projectGroup, boolean enabled) {

		// location label
		locationLabel = new Label(projectGroup,SWT.NONE);
		locationLabel.setText(CPlugin.getResourceString("CProjectWizardPage.locationLabel")); //$NON-NLS-1$
		locationLabel.setEnabled(enabled);

		// project location entry field
		locationPathField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		locationPathField.setLayoutData(data);
		locationPathField.setEnabled(enabled);

		// browse button
		browseButton = new Button(projectGroup, SWT.PUSH);
		browseButton.setText(CPlugin.getResourceString("CProjectWizardPage.browseLabel")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});

		browseButton.setEnabled(enabled);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (initialLocationFieldValue != null)
			locationPathField.setText(initialLocationFieldValue.toOSString());
		locationPathField.addListener(SWT.Modify, locationModifyListener);
	}

	/**
	 * Returns the current project location path as entered by 
	 * the user, or its anticipated initial value.
	 *
	 * @return the project location path, its anticipated initial value, or <code>null</code>
	 *   if no project location path is known
	 */
	public IPath getLocationPath() {
		if (useDefaults)
			return initialLocationFieldValue;
		
		return new Path(locationPathField.getText());
	}

	/**
	 * Creates a project resource handle for the current project name field value.
	 * <p>
	 * This method does not create the project resource; this is the responsibility
	 * of <code>IProject::create</code> invoked by the new project resource wizard.
	 * </p>
	 *
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}

	/**
	 * Returns the current project name as entered by the user, or its anticipated
	 * initial value.
	 *
	 * @return the project name, its anticipated initial value, or <code>null</code>
	 *   if no project name is known
	 */
	public String getProjectName() {
		if (projectNameField == null)
			return initialProjectFieldValue;
		
		return projectNameField.getText();
	}

	/**
	 *	Open an appropriate directory browser
	 */
	protected void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
		dialog.setMessage(CPlugin.getResourceString("CProjectWizardPage.directoryLabel")); //$NON-NLS-1$
	
		String dirName = locationPathField.getText();
		if (!dirName.equals("")) {//$NON-NLS-1$
			File path = new File(dirName);
			if (path.exists())
				dialog.setFilterPath(dirName);
		}
	
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null)
			locationPathField.setText(selectedDirectory);
	}

	/**
	 * Creates a new project creation wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public CProjectWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		this.initialLocationFieldValue = Platform.getLocation();
	}

	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	protected void setLocationForSelection() {
		if (useDefaults) {
			IPath defaultPath = Platform.getLocation().append(projectNameField.getText());
			locationPathField.setText(defaultPath.toOSString());
		}
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		String projectFieldContents = projectNameField.getText();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(CPlugin.getResourceString("CProjectWizardPage.projectNameEmpty")); //$NON-NLS-1$
			return false;
		}

		if (projectFieldContents.indexOf(' ') != -1) {
			setErrorMessage(CPlugin.getResourceString("CProjectWizardPage.projectContainsSpace")); //$NON-NLS-1$
			return false;
		}

		IStatus nameStatus =
			workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		String locationFieldContents = locationPathField.getText();
	
		if (!locationFieldContents.equals("")) {//$NON-NLS-1$
			IPath path = new Path("");//$NON-NLS-1$
			if (!path.isValidPath(locationFieldContents)) {
				setErrorMessage(CPlugin.getResourceString("CProjectWizardPage.locationError")); //$NON-NLS-1$
				return false;
			}
			if (!useDefaults && Platform.getLocation().isPrefixOf(new Path(locationFieldContents))) {
				setErrorMessage(CPlugin.getResourceString("CProjectWizardPage.defaultLocationError")); //$NON-NLS-1$
				return false;
			}
		}

		if (getProjectHandle().exists()) {
			setErrorMessage(CPlugin.getResourceString("CProjectWizardPage.projectExistsMessage")); //$NON-NLS-1$
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}
}
