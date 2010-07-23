/*******************************************************************************
 * Copyright (c) 2007, 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.importexecutable;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
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

	private boolean shouldUpdateButtons = true;

	public ImportExecutablePageTwo(AbstractImportExecutableWizard wizard) {
		super("ImportExecutablePageTwo"); //$NON-NLS-1$
		this.wizard = wizard;
		setTitle(Messages.ImportExecutablePageTwo_ChooseProject);
		setDescription(Messages.ImportExecutablePageTwo_ChooseExisting);
		filterPlatform = "*"; //$NON-NLS-1$
	}

	private void addLaunchConfigTypes() {
		ILaunchConfigurationType[] configTypeList = DebugPlugin.getDefault()
				.getLaunchManager().getLaunchConfigurationTypes();
		int j = 0, capp = 0;
		for (int i = 0; i < configTypeList.length; i++) {
			ILaunchConfigurationType type = configTypeList[i];
			String configTypeName = type.getName();
			if (type.isPublic()
					&& type.supportsMode(ILaunchManager.DEBUG_MODE)) {
				if (wizard.supportsConfigurationType(type)) {
					configTypes.add(configTypeName);
					
					if (type.getIdentifier().equals(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP)) {
						capp = j;
					}
					j++;
				}
			}
		}
		configTypes.select(capp);
	}

	public void checkExecutableSettings() {
		shouldUpdateButtons = false;
		if (isCreateNewProjectSelected) {
			String defaultName = wizard.getDefaultProjectName();
			if (defaultName.length() > 0) {
				ICProject cProject = CoreModel.getDefault().getCModel()
						.getCProject(defaultName);
				if (cProject.exists()) {
					isCreateNewProjectSelected = false;
					existingProjectName.setText(defaultName);
					existingProjectButton.setSelection(true);
					newProjectButton.setSelection(false);
				} else {
					newProjectName.setText(defaultName);
				}
				setLaunchConfigurationName(defaultName);
			}
		}
		updateControls();
		shouldUpdateButtons = true;
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
				updateControls();
			}
		});

		newProjectName = new Text(composite, SWT.BORDER);
		final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 3;
		newProjectName.setLayoutData(gridData);
		newProjectName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateControls();
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
				updateControls();
			}
		});

		existingProjectName = new Text(composite, SWT.BORDER);
		final GridData gridData_3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData_3.horizontalSpan = 2;
		existingProjectName.setLayoutData(gridData_3);
		existingProjectName.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateControls();
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
				updateControls();
			}

		});
		addLaunchConfigTypes();
		updateControls();
		CDebugUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp( getControl(), ICDebugHelpContextIds.IMPORT_EXECUTABLE_PAGE_TWO );

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
		updateControls();
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
		if (shouldUpdateButtons && getContainer().getCurrentPage() != null)
			getContainer().updateButtons();
	}
	
    public boolean isPageComplete() {
    	setErrorMessage(null);
		if (isCreateNewProjectSelected()) {
			if (getNewProjectName().length() == 0) {

				setErrorMessage(Messages.ImportExecutablePageTwo_EnterProjectName);
				return false;
			}
			ICProject cProject = CoreModel.getDefault().getCModel().getCProject(getNewProjectName());
			if (cProject.exists()) {

				setErrorMessage(Messages.ImportExecutablePageTwo_ProjectAlreadyExists);
				return false;
			}

		} else if (!isCreateNewProjectSelected()) {

			ICProject project = getExistingCProject();
			if (project == null) {

				setErrorMessage(Messages.ImportExecutablePageTwo_BadProjectName);
				return false;
			}

		}
		if (isCreateLaunchConfigurationSelected() && getNewConfigurationName().length() == 0) {

			setErrorMessage(Messages.ImportExecutablePageTwo_EnterLaunchConfig);
			return false;
		}
		return super.isPageComplete();
	}


}
