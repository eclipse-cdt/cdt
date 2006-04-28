/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.importexecutable;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class ImportExecutablePageTwo extends WizardPage {

	private Combo configTypes;

	private Text configurationName;

	private Label configurationNameLabel;

	private Button createLaunch;

	private Button existingProjectButton;

	private Text existingProjectName;

	private String filterPlatform;

	private boolean isCreateLaunchConfigurationSelected = true;

	private boolean isCreateNewProjectSelected = true;

	private Button newProjectButton;

	private Label newProjectLabel;

	private Text newProjectName;

	private Button searchButton;

	private AbstractImportExecutableWizard wizard;

	public ImportExecutablePageTwo(AbstractImportExecutableWizard wizard) {
		super("ImportExecutablePageTwo");
		this.wizard = wizard;
		setTitle(Messages.ImportExecutablePageTwo_ChooseProject);
		setDescription(Messages.ImportExecutablePageTwo_ChooseExisting);
		filterPlatform = "*";
	}

	private void addLaunchConfigTypes() {
		ILaunchConfigurationType[] configTypeList = DebugPlugin.getDefault()
				.getLaunchManager().getLaunchConfigurationTypes();
		for (int i = 0; i < configTypeList.length; i++) {
			String configTypeName = configTypeList[i].getName();
			if (configTypeList[i].isPublic()
					&& configTypeList[i]
							.supportsMode(ILaunchManager.DEBUG_MODE)) {
				if (wizard.supportsConfigurationType(configTypeList[i])) {
					configTypes.add(configTypeName);
				}
			}
		}
		configTypes.select(0);
	}

	public void checkExecutableSettings() {
		if (isCreateNewProjectSelected) {
			String defaultName = getDefaultProjectName();
			if (defaultName.length() > 0) {
				ICProject cProject = CoreModel.getDefault().getCModel()
						.getCProject(defaultName);
				if (cProject.exists()) {
					isCreateNewProjectSelected = false;
					existingProjectName.setText(defaultName);
					existingProjectButton.setSelection(true);
					newProjectButton.setSelection(false);
					checkExistingProjectName();
				} else {
					newProjectName.setText(defaultName);
					checkNewProjectName();
				}
				setLaunchConfigurationName(defaultName);
			}
		}
	}

	protected void checkExistingProjectName() {
		ICProject project = getExistingCProject();
		setErrorMessage(null);
		setPageComplete(project != null);
		if (project == null) {
			setErrorMessage(Messages.ImportExecutablePageTwo_BadProjectName);
		}
	}

	protected void checkLaunchConfigurationName() {
		String newName = configurationName.getText();
		setErrorMessage(null);
		if (isCreateLaunchConfigurationSelected) {
			setPageComplete(newName.length() > 0);
			if (newName.length() == 0) {
				setErrorMessage(Messages.ImportExecutablePageTwo_EnterLaunchConfig);
			}
		}
	}

	protected void checkNewProjectName() {
		String newName = newProjectName.getText().trim();
		setErrorMessage(null);
		setPageComplete(newName.length() > 0);
		if (newName.length() == 0) {
			setErrorMessage(Messages.ImportExecutablePageTwo_EnterProjectName);
		}
		ICProject cProject = CoreModel.getDefault().getCModel().getCProject(
				newName);
		if (cProject.exists()) {
			setErrorMessage(Messages.ImportExecutablePageTwo_ProjectAlreadyExists);
		}
	}

	protected ICProject chooseCProject() {
		try {
			ICProject[] projects = getCProjects();

			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					getShell(), labelProvider);
			dialog.setTitle("Select a Project"); //$NON-NLS-1$
			dialog.setMessage("Choose a project for the executable."); //$NON-NLS-1$
			dialog.setElements(projects);

			ICProject cProject = getExistingCProject();
			if (cProject != null) {
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == Window.OK) {
				return (ICProject) dialog.getFirstResult();
			}
		} catch (CModelException e) {
		}
		return null;
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		//
		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		composite.setLayout(gridLayout);

		newProjectButton = new Button(composite, SWT.RADIO);
		newProjectButton.setText(Messages.ImportExecutablePageTwo_NewProjectName);
		newProjectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isCreateNewProjectSelected = newProjectButton.getSelection();
				checkNewProjectName();
				updateControls();
			}
		});

		newProjectName = new Text(composite, SWT.BORDER);
		final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 3;
		newProjectName.setLayoutData(gridData);
		newProjectName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				checkNewProjectName();
			}

		});

		new Label(composite, SWT.NONE);

		newProjectLabel = new Label(composite, SWT.NONE);
		final GridData gridData_1 = new GridData();
		gridData_1.horizontalSpan = 3;
		newProjectLabel.setLayoutData(gridData_1);
		newProjectLabel
				.setText(Messages.ImportExecutablePageTwo_ProjectLabel);

		final Label dummy2 = new Label(composite, SWT.NONE);
		final GridData gridData_2 = new GridData();
		gridData_2.horizontalSpan = 4;
		dummy2.setLayoutData(gridData_2);

		existingProjectButton = new Button(composite, SWT.RADIO);
		existingProjectButton.setText(Messages.ImportExecutablePageTwo_ExistingProject);
		existingProjectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isCreateNewProjectSelected = !newProjectButton.getSelection();
				checkExistingProjectName();
				updateControls();
			}
		});

		existingProjectName = new Text(composite, SWT.BORDER);
		final GridData gridData_3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData_3.horizontalSpan = 2;
		existingProjectName.setLayoutData(gridData_3);
		existingProjectName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				checkExistingProjectName();
			}

		});

		searchButton = new Button(composite, SWT.NONE);
		searchButton.setLayoutData(new GridData());
		searchButton.setText(Messages.ImportExecutablePageTwo_Search);
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ICProject project = chooseCProject();
				if (project == null) {
					return;
				}
				String projectName = project.getElementName();
				existingProjectName.setText(projectName);
				updateControls();
			}
		});

		newProjectButton.setSelection(true);

		final Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gridData_4 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData_4.horizontalSpan = 4;
		label.setLayoutData(gridData_4);

		final Composite composite_1 = new Composite(composite, SWT.NONE);
		final GridData gridData_5 = new GridData();
		gridData_5.horizontalSpan = 4;
		composite_1.setLayoutData(gridData_5);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 3;
		composite_1.setLayout(gridLayout_1);

		createLaunch = new Button(composite_1, SWT.CHECK);
		createLaunch.setText(Messages.ImportExecutablePageTwo_CreateLaunch);
		createLaunch.setSelection(true);
		createLaunch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isCreateLaunchConfigurationSelected = createLaunch
						.getSelection();
				setLaunchConfigurationName(configurationName.getText().trim());
				checkLaunchConfigurationName();
				updateControls();
			}
		});

		configTypes = new Combo(composite_1, SWT.READ_ONLY);
		final GridData gridData_6 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData_6.horizontalSpan = 2;
		configTypes.setLayoutData(gridData_6);
		configTypes.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setLaunchConfigurationName(configurationName.getText().trim());
			}

		});

		configurationNameLabel = new Label(composite, SWT.NONE);
		configurationNameLabel.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		configurationNameLabel.setText(Messages.ImportExecutablePageTwo_Name);

		configurationName = new Text(composite, SWT.BORDER);
		final GridData gridData_7 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData_7.horizontalSpan = 3;
		configurationName.setLayoutData(gridData_7);
		configurationName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				checkLaunchConfigurationName();
			}

		});
		addLaunchConfigTypes();
		updateControls();

	}

	private ICProject[] getCProjects() throws CModelException {
		ICProject cproject[] = CoreModel.getDefault().getCModel()
				.getCProjects();
		ArrayList list = new ArrayList(cproject.length);

		for (int i = 0; i < cproject.length; i++) {
			ICDescriptor cdesciptor = null;
			try {
				cdesciptor = CCorePlugin.getDefault().getCProjectDescription(
						(IProject) cproject[i].getResource(), false);
				if (cdesciptor != null) {
					String projectPlatform = cdesciptor.getPlatform();
					if (filterPlatform.equals("*") //$NON-NLS-1$
							|| projectPlatform.equals("*") //$NON-NLS-1$
							|| filterPlatform.equalsIgnoreCase(projectPlatform) == true) {
						list.add(cproject[i]);
					}
				} else {
					list.add(cproject[i]);
				}
			} catch (CoreException e) {
				list.add(cproject[i]);
			}
		}
		return (ICProject[]) list.toArray(new ICProject[list.size()]);
	}

	protected String getDefaultProjectName() {
		String defaultName = new String();
		String[] executables = wizard.getImportExecutablePage()
				.getSelectedExecutables();
		if (executables.length > 0) {
			String fileName = new File(executables[0]).getName();
			defaultName = new String(Messages.ImportExecutablePageTwo_DefaultProjectPrefix + fileName);
		}
		return defaultName;
	}

	protected ICProject getExistingCProject() {
		String projectName = existingProjectName.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		ICProject cProject = CoreModel.getDefault().getCModel().getCProject(
				projectName);
		if (!cProject.exists())
			return null;
		return cProject;
	}

	public String getExistingProjectName() {
		return existingProjectName.getText().trim();
	}

	public String getNewConfigurationName() {
		return configurationName.getText().trim();
	}

	public String getNewProjectName() {
		return newProjectName.getText().trim();
	}

	protected ILaunchConfigurationType getSelectedLaunchConfigurationType() {
		ILaunchConfigurationType result = null;
		String selectedTypeName = configTypes.getText();
		ILaunchConfigurationType[] configTypeList = DebugPlugin.getDefault()
				.getLaunchManager().getLaunchConfigurationTypes();
		for (int i = 0; i < configTypeList.length; i++) {
			if (selectedTypeName.equals(configTypeList[i].getName())) {
				result = configTypeList[i];
				break;
			}
		}
		return result;
	}

	public boolean isCreateLaunchConfigurationSelected() {
		return isCreateLaunchConfigurationSelected;
	}

	public boolean isCreateNewProjectSelected() {
		return isCreateNewProjectSelected;
	}

	private void setLaunchConfigurationName(String defaultName) {
		configurationName.setText(DebugPlugin.getDefault().getLaunchManager()
				.generateUniqueLaunchConfigurationNameFrom(defaultName));
		checkLaunchConfigurationName();
	}

	protected void updateControls() {
		isCreateNewProjectSelected = newProjectButton.getSelection();
		searchButton.setEnabled(!isCreateNewProjectSelected);
		newProjectName.setEnabled(isCreateNewProjectSelected);
		existingProjectName.setEnabled(!isCreateNewProjectSelected);
		newProjectLabel.setEnabled(isCreateNewProjectSelected);

		configTypes.setEnabled(isCreateLaunchConfigurationSelected);
		configurationName.setEnabled(isCreateLaunchConfigurationSelected);
		configurationNameLabel.setEnabled(isCreateLaunchConfigurationSelected);
	}
}
