/*******************************************************************************
 * Copyright (c) 2010, 2011  Nokia Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Ryall (Nokia) - initial API and implementation
 *     IBM Corporation
 *     Alex Collins     (Broadcom Corp.)  - choose build config automatically
 *     Anna Dushistova  (Mentor Graphics) - [333504] [remote launch] NPE after switching to "Standard Launcher" in Remote Application debug configuration 
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.LaunchUtils;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * @since 6.1
 */
public abstract class CAbstractMainTab extends CLaunchConfigurationTab {
	private static final String LAUNCHING_PREFERENCE_PAGE_ID = "org.eclipse.debug.ui.LaunchingPreferencePage"; //$NON-NLS-1$
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected String filterPlatform = EMPTY_STRING;

	/**
	 * @since 6.0
	 */
	protected Combo fBuildConfigCombo;
	/** @since 7.0*/
	protected Button fBuildConfigAuto;
	/**
	 * Indicates whether the user has clicked on the build config auto button
	 * Prevents causing a delta to the underlying launch configuration if the user hasn't touched this setting.
	 * @since 7.0
	 */
	protected boolean fBuildConfigAutoChanged;
	/** @since 6.1 */
	protected Button fDisableBuildButton;
	/** @since 6.1 */
	protected Button fEnableBuildButton;
	/** @since 6.1 */
	protected Button fWorkspaceSettingsButton;
	/** @since 6.1 */
	protected Link fWorkpsaceSettingsLink;
	protected final Map<IPath, Boolean> fBinaryExeCache = new HashMap<IPath, Boolean>();
	protected Label fProjLabel;
	protected Text fProjText;
	protected Button fProjButton;

	protected Label fProgLabel;
	protected Text fProgText;
	protected Button fSearchButton;

	// Core file UI widgets
	/** @since 2.0 */
	protected Label fCoreLabel;
	/** @since 2.0 */
	protected Text fCoreText;
	/** @since 2.0 */
	protected Button fCoreButton;

	/**
	 * Name of most recently checked program; avoid constantly checking binary.
	 * See bug 277663.
	 */
	protected String fPreviouslyCheckedProgram;
	/**
	 * Validity result of most recently checked program; avoid constantly
	 * checking binary. See bug 277663. N/A if fPreviouslyCheckedProgram = null;
	 */
	protected boolean fPreviouslyCheckedProgramIsValid;
	/**
	 * Validity error message of most recently checked program; avoid constantly
	 * checking binary. See bug 277663. N/A if fPreviouslyCheckedProgram = null.
	 */
	protected String fPreviouslyCheckedProgramErrorMsg;

	public CAbstractMainTab() {
		super();
	}

	abstract protected void handleSearchButtonSelected();

	/**
	 * @since 6.0
	 */
	protected void createProjectGroup(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 2;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		fProjLabel = new Label(projComp, SWT.NONE);
		fProjLabel.setText(LaunchMessages.CMainTab_ProjectColon);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProjLabel.setLayoutData(gd);

		fProjText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProjText.setLayoutData(gd);
		fProjText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				// if project changes, invalidate program name cache
				fPreviouslyCheckedProgram = null;
				updateBuildConfigCombo(EMPTY_STRING);
				updateLaunchConfigurationDialog();
			}
		});

		fProjButton = createPushButton(projComp, LaunchMessages.Launch_common_Browse_1, null); 
		fProjButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleProjectButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * Return the ICProject corresponding to the project name in the project name text field, or
	 * null if the text does not match a project name.
	 */
	protected ICProject getCProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return CoreModel.getDefault().getCModel().getCProject(projectName);
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides context for the main
	 * type, allowing the user to key a main type name, or constraining the search for main types to
	 * the specified project.
	 */
	protected void handleProjectButtonSelected() {
		String currentProjectName = fProjText.getText();
		ICProject project = chooseCProject();
		if (project == null) {
			return;
		}

		String projectName = project.getElementName();
		fProjText.setText(projectName);
		if (currentProjectName.length() == 0) {
			// New project selected for the first time, set the program name default too.
			IBinary[] bins = getBinaryFiles(project);
			if (bins != null && bins.length == 1) {				
				fProgText.setText(bins[0].getResource().getProjectRelativePath().toOSString());
			}
		}
	}

	/**
	 * Return an array a ICProject whose platform match that of the runtime env.
	 */
	protected ICProject[] getCProjects() throws CModelException {
		ICProject cproject[] = CoreModel.getDefault().getCModel().getCProjects();
		ArrayList<ICProject> list = new ArrayList<ICProject>(cproject.length);

		for (int i = 0; i < cproject.length; i++) {
			ICDescriptor cdesciptor = null;
			try {
				cdesciptor = CCorePlugin.getDefault().getCProjectDescription((IProject) cproject[i].getResource(), false);
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
		return list.toArray(new ICProject[list.size()]);
	}

	/**
	 * Realize a C Project selection dialog and return the first selected project, or null if there
	 * was none.
	 */
	protected ICProject chooseCProject() {
		try {
			ICProject[] projects = getCProjects();

			ILabelProvider labelProvider = new CElementLabelProvider();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
			dialog.setTitle(LaunchMessages.CMainTab_Project_Selection); 
			dialog.setMessage(LaunchMessages.CMainTab_Choose_project_to_constrain_search_for_program); 
			dialog.setElements(projects);

			ICProject cProject = getCProject();
			if (cProject != null) {
				dialog.setInitialSelections(new Object[] { cProject });
			}
			if (dialog.open() == Window.OK) {
				return (ICProject)dialog.getFirstResult();
			}
		} catch (CModelException e) {
			LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$			
		}
		return null;
	}

	/**
	 * @since 6.0
	 */
	protected void updateBuildConfigCombo(String selectedConfigID) {
		if (fBuildConfigCombo != null) {
			fBuildConfigCombo.setEnabled(!fBuildConfigAuto.getSelection());
			fBuildConfigCombo.removeAll();
			fBuildConfigCombo.add(LaunchMessages.CMainTab_Use_Active); 
			fBuildConfigCombo.setData("0", EMPTY_STRING); //$NON-NLS-1$
			fBuildConfigCombo.select(0);
			ICProject cproject = getCProject();
			if (cproject != null) {
				ICProjectDescription projDes = CDTPropertyManager.getProjectDescription(cproject.getProject());
				if (projDes != null) {
					// Find the config that should be automatically selected
					String autoConfigId = null;
					if (fBuildConfigAuto.getSelection()) {
						ICConfigurationDescription autoConfig = LaunchUtils.getBuildConfigByProgramPath(cproject.getProject(), fProgText.getText());
						if (autoConfig != null)
							autoConfigId = autoConfig.getId();
					}

					int selIndex = 0;
					ICConfigurationDescription[] configurations = projDes.getConfigurations();
					ICConfigurationDescription selectedConfig = projDes.getConfigurationById(selectedConfigID);
					for (int i = 0; i < configurations.length; i++) {
						String configName = configurations[i].getName();
						fBuildConfigCombo.add(configName);
						fBuildConfigCombo.setData(Integer.toString(i + 1), configurations[i].getId());
						if (selectedConfig != null && selectedConfigID.equals(configurations[i].getId()) ||
							fBuildConfigAuto.getSelection() && configurations[i].getId().equals(autoConfigId)) {
							selIndex = i + 1;
						}
					}
					fBuildConfigCombo.select(selIndex);
				}
			}
		}	
	}

	/**
	 * @since 6.0
	 */
	protected void createBuildConfigCombo(Composite parent, int colspan) {
		Composite comboComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comboComp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colspan;
		comboComp.setLayoutData(gd);
		Label dlabel = new Label(comboComp, SWT.NONE);
		dlabel.setText(LaunchMessages.CMainTab_Build_Config); 
		fBuildConfigCombo = new Combo(comboComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		fBuildConfigCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fBuildConfigCombo.addSelectionListener(new SelectionListener() {
		    @Override
			public void widgetSelected(SelectionEvent e) {
		    	updateLaunchConfigurationDialog();
		    }
	
		    @Override
			public void widgetDefaultSelected(SelectionEvent e) {
		    	updateLaunchConfigurationDialog();
		    }
		});

		new Label(comboComp, SWT.NONE).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fBuildConfigAuto = new Button(comboComp, SWT.CHECK);
		fBuildConfigAuto.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fBuildConfigAuto.setText(LaunchMessages.CMainTab_Build_Config_Auto); 
		fBuildConfigAuto.addSelectionListener(new SelectionListener() {
		    @Override
			public void widgetSelected(SelectionEvent e) {
		    	fBuildConfigAutoChanged = true;
		    	fBuildConfigCombo.setEnabled(false);
		    	updateBuildConfigCombo(EMPTY_STRING);
		    	updateLaunchConfigurationDialog();
		    }
		    @Override
			public void widgetDefaultSelected(SelectionEvent e) {
		    	fBuildConfigAutoChanged = true;
		    	fBuildConfigCombo.setEnabled(true);
				updateBuildConfigCombo(EMPTY_STRING);
		    	updateLaunchConfigurationDialog();
		    }
		});
	}

	/** @since 6.1 */
	protected void createBuildOptionGroup(final Composite parent, int colSpan) {
		Group buildGroup = new Group(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = colSpan;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 5;
		gridLayout.marginWidth = 5;
		gridLayout.makeColumnsEqualWidth= true;
		buildGroup.setLayoutData(gridData);
		buildGroup.setLayout(gridLayout);
		buildGroup.setText(LaunchMessages.CMainTab_Build_options); 
		
		createBuildConfigCombo(buildGroup, 2);
	
		fEnableBuildButton = new Button(buildGroup, SWT.RADIO);
		fEnableBuildButton.setText(LaunchMessages.CMainTab_Enable_build_button_label); 
		fEnableBuildButton.setToolTipText(LaunchMessages.CMainTab_Enable_build_button_tooltip); 
		fEnableBuildButton.addSelectionListener(new SelectionAdapter() {
	
			@Override
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	
		fDisableBuildButton = new Button(buildGroup, SWT.RADIO);
		fDisableBuildButton.setText(LaunchMessages.CMainTab_Disable_build_button_label); 
		fDisableBuildButton.setToolTipText(LaunchMessages.CMainTab_Disable_build_button_tooltip); 
		fDisableBuildButton.addSelectionListener(new SelectionAdapter() {
	
			@Override
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	
		fWorkspaceSettingsButton = new Button(buildGroup, SWT.RADIO);
		fWorkspaceSettingsButton.setText(LaunchMessages.CMainTab_Workspace_settings_button_label); 
		fWorkspaceSettingsButton.setToolTipText(LaunchMessages.CMainTab_Workspace_settings_button_tooltip); 
		fWorkspaceSettingsButton.addSelectionListener(new SelectionAdapter() {
	
			@Override
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
	
		fWorkpsaceSettingsLink = new Link(buildGroup, SWT.NONE);
		fWorkpsaceSettingsLink.setText(LaunchMessages.CMainTab_Workspace_settings_link_label); 
		fWorkpsaceSettingsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(
						parent.getShell(), 
						LAUNCHING_PREFERENCE_PAGE_ID,
						null, 
						null).open();
			}
		});
	}

	/** @since 6.1 */
	protected void updateBuildOptionFromConfig(ILaunchConfiguration config) {
		boolean configAuto = false;
		int buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING;
		try {
			buildBeforeLaunchValue = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH, buildBeforeLaunchValue);
			configAuto = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_AUTO, false);
		} catch (CoreException e) {
			LaunchUIPlugin.log(e);
		}
	
		if (fBuildConfigAuto != null) {
			fBuildConfigAuto.setSelection(configAuto);
			if (configAuto)
				updateBuildConfigCombo(EMPTY_STRING);
		}
		if (fDisableBuildButton != null)
			fDisableBuildButton.setSelection(buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED);
		if (fEnableBuildButton != null)
			fEnableBuildButton.setSelection(buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_ENABLED);
		if (fWorkspaceSettingsButton != null)
			fWorkspaceSettingsButton.setSelection(buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING);
	}

	/**
	 * Show a dialog that lets the user select a file.
	 * 
	 * @since 6.0
	 */
	protected String handleBrowseButtonSelected() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(fProgText.getText());
		return fileDialog.open();
	}

	/** @since 2.0 */
	protected void createCoreFileGroup(Composite parent, int colSpan) {
		Composite coreComp = new Composite(parent, SWT.NONE);
		GridLayout coreLayout = new GridLayout();
		coreLayout.numColumns = 3;
		coreLayout.marginHeight = 0;
		coreLayout.marginWidth = 0;
		coreComp.setLayout(coreLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		coreComp.setLayoutData(gd);
		fCoreLabel = new Label(coreComp, SWT.NONE);
		fCoreLabel.setText(LaunchMessages.CMainTab_CoreFile_path); 
		gd = new GridData();
		gd.horizontalSpan = 3;
		fCoreLabel.setLayoutData(gd);
		fCoreText = new Text(coreComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCoreText.setLayoutData(gd);
		fCoreText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		Button browseForCoreButton;
		browseForCoreButton = createPushButton(coreComp, LaunchMessages.Launch_common_Browse_3, null);
		browseForCoreButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				String text = handleBrowseButtonSelected();
				if (text != null) {
					fCoreText.setText(text);
				}
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * This method is deprecated since LaunchUtils#getBinary(IProject, IPath) is too slow to be
	 * called on the UI thread. See "https://bugs.eclipse.org/bugs/show_bug.cgi?id=328012".
	 * @param project
	 * @param exePath
	 * @return
	 * @throws CoreException
	 */
	@Deprecated
	protected boolean isBinary(IProject project, IPath exePath) throws CoreException {
		try {
			Boolean binValue = fBinaryExeCache.get(exePath);
			if (binValue == null) {
				IBinaryObject exe = LaunchUtils.getBinary(project, exePath);
				binValue = exe != null;
				fBinaryExeCache.put(exePath, binValue);				
			}
			return binValue;
		} catch (ClassCastException e) {
		}
		return false;
	}
	
	/**
	 * Iterate through and suck up all of the executable files that we can find.
	 */
	protected IBinary[] getBinaryFiles(final ICProject cproject) {
		final Display display;
		if (cproject == null || !cproject.exists()) {
			return null;
		}
		if (getShell() == null) {
			display = LaunchUIPlugin.getShell().getDisplay();
		} else {
			display = getShell().getDisplay();
		}
		final IBinary[][] ret = new IBinary[1][];
		BusyIndicator.showWhile(display, new Runnable() {
			@Override
			public void run() {
				try {
					ret[0] = cproject.getBinaryContainer().getBinaries();
				} catch (CModelException e) {
					LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$
				}
			}
		});

		return ret[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (fBuildConfigCombo != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, (String)fBuildConfigCombo.getData(Integer.toString(fBuildConfigCombo.getSelectionIndex())));
		}

		if (fBuildConfigAutoChanged && fBuildConfigAuto != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_AUTO, fBuildConfigAuto.getSelection());
		}

		if (fDisableBuildButton != null) {
			int buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING;
			if (fDisableBuildButton.getSelection()) {
				buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED;
			} else if (fEnableBuildButton.getSelection()) {
				buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_ENABLED;
			}
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH, buildBeforeLaunchValue);
		}
	}

	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = EMPTY_STRING;
		String configName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
			configName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, EMPTY_STRING);			
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		if (!fProjText.getText().equals(projectName))
			fProjText.setText(projectName);
		updateBuildConfigCombo(configName);		
	}

	protected void updateProgramFromConfig(ILaunchConfiguration config) {
		if (fProgText != null) {
			String programName = EMPTY_STRING;
			try {
				programName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
			} catch (CoreException ce) {
				LaunchUIPlugin.log(ce);
			}
			fProgText.setText(programName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		if (fBuildConfigAuto != null && fBuildConfigAuto.getSelection())
			updateBuildConfigCombo(EMPTY_STRING);
		super.updateLaunchConfigurationDialog();
	}
}