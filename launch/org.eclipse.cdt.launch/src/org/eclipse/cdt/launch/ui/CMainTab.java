package org.eclipse.cdt.launch.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A launch configuration tab that displays and edits project and
 * main type name launch configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */

public class CMainTab extends CLaunchConfigurationTab {

	// Project UI widgets
	protected Label fProjLabel;
	protected Text fProjText;
	protected Button fProjButton;

	// Main class UI widgets
	protected Label fProgLabel;
	protected Text fProgText;
	protected Button fSearchButton;

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private String filterPlatform = EMPTY_STRING;

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);

		Composite projComp = new Composite(comp, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		projComp.setLayoutData(gd);

		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText("&Project:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);

		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fProjButton = createPushButton(projComp, "&Browse...", null);
		fProjButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		createVerticalSpacer(comp, 1);

		Composite mainComp = new Composite(comp, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 2;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);
		fProgLabel = new Label(mainComp, SWT.NONE);
		fProgLabel.setText("C/C++ Application:");
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProgLabel.setLayoutData(gd);
		fProgText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProgText.setLayoutData(gd);
		fProgText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		fSearchButton = createPushButton(mainComp, "Searc&h...", null);
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
		LaunchUIPlugin.setDialogShell(parent.getShell());
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		filterPlatform = getPlatform(config);
		updateProjectFromConfig(config);
		updateProgramFromConfig(config);

	}

	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		}
		catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		fProjText.setText(projectName);
	}

	protected void updateProgramFromConfig(ILaunchConfiguration config) {
		String programName = EMPTY_STRING;
		try {
			programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
		}
		catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		fProgText.setText(programName);
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) fProjText.getText());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String) fProgText.getText());
	}

	/**
	 * Show a dialog that lists all main types
	 */
	protected void handleSearchButtonSelected() {

		if (getCProject() == null) {
			MessageDialog.openInformation(
				getShell(),
				"Project required",
				"Project must first be entered before searching for a program");
			return;
		}

		IBinary[] executables = getBinaryFiles(getCProject());
		ILabelProvider labelProvider = new WorkbenchLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(executables);
		dialog.setMessage("Choose a &program to run");
		dialog.setTitle("Program Selection");
		if (dialog.open() == ElementListSelectionDialog.OK) {
			IBinary binary = (IBinary) dialog.getFirstResult();
			try {
				fProgText.setText(binary.getResource().getProjectRelativePath().toString());
			}
			catch (CModelException e) {
			}
		}
	}

	/**
	 * Iterate through and suck up all of the executable files that
	 * we can find.
	 */
	protected IBinary[] getBinaryFiles(ICProject cproject) {
		return cproject.getBinaryContainer().getBinaries();
	}

	/**
	 * Show a dialog that lets the user select a project.  This in turn provides
	 * context for the main type, allowing the user to key a main type name, or
	 * constraining the search for main types to the specified project.
	 */
	protected void handleProjectButtonSelected() {
		ICProject project = chooseCProject();
		if (project == null) {
			return;
		}

		String projectName = project.getElementName();
		fProjText.setText(projectName);
	}

	/**
	 * Realize a C Project selection dialog and return the first selected project,
	 * or null if there was none.
	 */
	protected ICProject chooseCProject() {
		ICProject[] projects;
		projects = getCProjects();

		ILabelProvider labelProvider = new WorkbenchLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle("Project Selection");
		dialog.setMessage("Choose a &project to constrain the search for a program");
		dialog.setElements(projects);

		ICProject cProject = getCProject();
		if (cProject != null) {
			dialog.setInitialSelections(new Object[] { cProject });
		}
		if (dialog.open() == ElementListSelectionDialog.OK) {
			return (ICProject) dialog.getFirstResult();
		}
		return null;
	}

	/**
	 * Return an array a ICProject whose platform match that of the runtime env.
	 **/

	protected ICProject[] getCProjects() {
		ICProject cproject[] = CoreModel.getDefault().getCRoot().getCProjects();
		ArrayList list = new ArrayList(cproject.length);
		boolean isNative = filterPlatform.equals(BootLoader.getOS());

		for (int i = 0; i < cproject.length; i++) {
			ICDescriptor cdesciptor = null;
			try {
				cdesciptor = CCorePlugin.getDefault().getCProjectDescription((IProject) cproject[i].getResource());
				String projectPlatform = cdesciptor.getPlatform();
				if (filterPlatform.equals("*") || projectPlatform.equals("*") || 
				    (isNative && cdesciptor.getPlatform().equalsIgnoreCase("native"))
					|| filterPlatform.equalsIgnoreCase(cdesciptor.getPlatform()) == true) {
					list.add(cproject[i]);
				}
			}
			catch (CoreException e) {
			}
		}
		return (ICProject[]) list.toArray(new ICProject[list.size()]);
	}
	/**
	 * Return the ICProject corresponding to the project name in the project name
	 * text field, or null if the text does not match a project name.
	 */
	protected ICProject getCProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return CoreModel.getDefault().getCRoot().getCProject(projectName);
	}

	/**
	 * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {

		setErrorMessage(null);
		setMessage(null);

		String name = fProjText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage("Project not specified");
			return false;
		}
		if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
			setErrorMessage("Project does not exist");
			return false;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);

		name = fProgText.getText().trim();
		if (name.length() == 0) {
			setErrorMessage("Program not specified");
			return false;
		}
		if (name.equals(".") || name.equals("..")) {
			setErrorMessage("Program does not exist");
			return false;
		}
		if (!project.isOpen()) {
			setErrorMessage("Project must be opened");
			return false;
		}
		if (!project.getFile(name).exists()) {
			setErrorMessage("Program does not exist");
			return false;
		}
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		// We set empty attributes for project & program so that when one config is
		// compared to another, the existence of empty attributes doesn't cause an
		// incorrect result (the performApply() method can result in empty values
		// for these attributes being set on a config if there is nothing in the
		// corresponding text boxes)
		// plus getContext will use this to base context from if set.
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		ICElement cElement = null;
		cElement = getContext(config, getPlatform(config));
		if (cElement != null) {
			initializeCProject(cElement, config);
		}
		initializeProgramName(cElement, config);
	}

	/**
	 * Set the program name attributes on the working copy based on the ICElement
	 */
	protected void initializeProgramName(ICElement cElement, ILaunchConfigurationWorkingCopy config) {
		IBinary binary = null;
		if (cElement instanceof ICProject) {
			IBinaryContainer bc = ((ICProject) cElement).getBinaryContainer();
			IBinary[] bins = bc.getBinaries();
			if (bins.length == 1) {
				binary = bins[0];
			}
		}

		if (binary != null) {
			String path;
			try {
				path = binary.getResource().getProjectRelativePath().toOSString();
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, path);
				String name = binary.getElementName();
				int index = name.lastIndexOf('.');
				if (index > 0) {
					name = name.substring(index + 1);
				}
				name = getLaunchConfigurationDialog().generateName(name);
				config.rename(name);
			}
			catch (CModelException e) {
			}

		}
	}
	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Main";
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_MAIN_TAB);
	}

}
