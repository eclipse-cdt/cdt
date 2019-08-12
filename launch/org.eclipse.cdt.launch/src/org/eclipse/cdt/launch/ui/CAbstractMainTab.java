/*******************************************************************************
 * Copyright (c) 2010, 2016  Nokia Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.dialogs.PropertyDialog;

/**
 * @since 6.1
 */
public abstract class CAbstractMainTab extends CLaunchConfigurationTab {
	private static final String LAUNCHING_PREFERENCE_PAGE_ID = "org.eclipse.debug.ui.LaunchingPreferencePage"; //$NON-NLS-1$
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected String filterPlatform = EMPTY_STRING;
	/**
	 * @since 7.2
	 */
	protected static final String AUTO_CONFIG = "AUTO"; //$NON-NLS-1$
	/**
	 * @since 6.0
	 */
	protected Combo fBuildConfigCombo;
	/** @since 6.1 */
	protected Button fDisableBuildButton;
	/** @since 6.1 */
	protected Button fEnableBuildButton;
	/** @since 6.1 */
	protected Button fWorkspaceSettingsButton;
	/** @since 6.1 */
	protected Link fWorkpsaceSettingsLink;
	protected final Map<IPath, Boolean> fBinaryExeCache = new HashMap<>();
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
	 * Name of most recently checked program; avoid constantly checking binary. See bug 277663.
	 */
	protected String fPreviouslyCheckedProgram;
	/**
	 * Validity result of most recently checked program; avoid constantly checking binary. See bug 277663. N/A if
	 * fPreviouslyCheckedProgram = null;
	 */
	protected boolean fPreviouslyCheckedProgramIsValid;
	/**
	 * Validity error message of most recently checked program; avoid constantly checking binary. See bug 277663. N/A if
	 * fPreviouslyCheckedProgram = null.
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
		fProjText.addModifyListener(evt -> {
			// if project changes, invalidate program name cache
			fPreviouslyCheckedProgram = null;
			updateBuildConfigCombo(EMPTY_STRING);
			updateLaunchConfigurationDialog();
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
	 * Return the ICProject corresponding to the project name in the project name text field, or null if the text does not match a
	 * project name.
	 */
	protected ICProject getCProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return CoreModel.getDefault().getCModel().getCProject(projectName);
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides context for the main type, allowing the user to key
	 * a main type name, or constraining the search for main types to the specified project.
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
		ArrayList<ICProject> list = new ArrayList<>(cproject.length);
		for (int i = 0; i < cproject.length; i++) {
			ICDescriptor cdesciptor = null;
			try {
				cdesciptor = CCorePlugin.getDefault().getCProjectDescription((IProject) cproject[i].getResource(),
						false);
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
	 * Realize a C Project selection dialog and return the first selected project, or null if there was none.
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
				return (ICProject) dialog.getFirstResult();
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
			fBuildConfigCombo.removeAll();
			int offset = 0;
			fBuildConfigCombo.add(LaunchMessages.CMainTab_Use_Active);
			fBuildConfigCombo.setData(String.valueOf(offset), EMPTY_STRING);
			fBuildConfigCombo.select(offset);
			offset++;
			if (isAutoConfigSupported()) {
				fBuildConfigCombo.add(LaunchMessages.CMainTab_Use_Automatic);
				fBuildConfigCombo.setData(String.valueOf(offset), AUTO_CONFIG);
				if (AUTO_CONFIG.equals(selectedConfigID)) {
					fBuildConfigCombo.select(offset);
				}
			}
			offset++;
			ICProject cproject = getCProject();
			if (cproject != null) {
				ICProjectDescription projDes = CDTPropertyManager.getProjectDescription(cproject.getProject());
				if (projDes != null) {
					// Populate and select config
					ICConfigurationDescription[] configurations = projDes.getConfigurations();
					for (int i = 0; i < configurations.length; i++) {
						String configName = configurations[i].getName();
						String id = configurations[i].getId();
						fBuildConfigCombo.add(configName);
						int comboIndex = i + offset;
						fBuildConfigCombo.setData(String.valueOf(comboIndex), id);
						if (id.equals(selectedConfigID)) {
							fBuildConfigCombo.select(comboIndex);
						}
					}
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
		Link dlabel = new Link(comboComp, SWT.NONE);
		dlabel.setText("<a>" + LaunchMessages.CMainTab_Build_Config + "</a>"); //$NON-NLS-1$//$NON-NLS-2$
		dlabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openBuildConfigProperties();
			}
		});
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
	}

	/**
	 * @since 9.0
	 */
	protected void openBuildConfigProperties() {
		IStructuredSelection sel;
		ICProject cProject = getCProject();
		if (cProject != null)
			sel = new StructuredSelection(cProject.getProject());
		else
			sel = new StructuredSelection();
		Shell shell = getShell();
		String propertyPageId = "org.eclipse.cdt.managedbuilder.ui.properties.Page_head_build";//$NON-NLS-1$
		PropertyDialog dialog = PropertyDialog.createDialogOn(shell, propertyPageId, sel);
		if (dialog != null)
			dialog.open();
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
		gridLayout.makeColumnsEqualWidth = true;
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
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), LAUNCHING_PREFERENCE_PAGE_ID, null, null)
						.open();
			}
		});
	}

	/** @since 6.1 */
	protected void updateBuildOptionFromConfig(ILaunchConfiguration config) {
		boolean configAuto = false;
		int buildBeforeLaunchValue = ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING;
		try {
			buildBeforeLaunchValue = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
					buildBeforeLaunchValue);
			configAuto = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_AUTO, false);
		} catch (CoreException e) {
			LaunchUIPlugin.log(e);
		}
		if (configAuto) {
			updateBuildConfigCombo(AUTO_CONFIG);
		} else {
			String configName = EMPTY_STRING;
			try {
				configName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID,
						configName);
			} catch (CoreException ce) {
				LaunchUIPlugin.log(ce);
			}
			updateBuildConfigCombo(configName);
		}
		updateComboTooltip();
		if (fDisableBuildButton != null)
			fDisableBuildButton.setSelection(
					buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED);
		if (fEnableBuildButton != null)
			fEnableBuildButton.setSelection(
					buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_ENABLED);
		if (fWorkspaceSettingsButton != null)
			fWorkspaceSettingsButton.setSelection(
					buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING);
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
		fCoreText.addModifyListener(evt -> updateLaunchConfigurationDialog());
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
	 * This method is deprecated since LaunchUtils#getBinary(IProject, IPath) is too slow to be called on the UI thread. See
	 * "https://bugs.eclipse.org/bugs/show_bug.cgi?id=328012".
	 *
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
		BusyIndicator.showWhile(display, () -> {
			try {
				ret[0] = cproject.getBinaryContainer().getBinaries();
			} catch (CModelException e) {
				LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$
			}
		});
		return ret[0];
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		if (fBuildConfigCombo != null) {
			String configId = (String) fBuildConfigCombo
					.getData(Integer.toString(fBuildConfigCombo.getSelectionIndex()));
			boolean auto = false;
			if (configId.equals(AUTO_CONFIG)) {
				auto = true;
				configId = getAutoConfigId();
			}
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, configId);
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_AUTO, auto);
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

	/**
	 * Calculate build config id based on selection of the binary. Subclasses may override.
	 *
	 * @return
	 * @since 7.2
	 */
	protected String getAutoConfigId() {
		String data = null;
		ICProject cproject = getCProject();
		if (cproject != null) {
			ICConfigurationDescription autoConfig = LaunchUtils.getBuildConfigByProgramPath(cproject.getProject(),
					fProgText.getText());
			if (autoConfig != null)
				data = autoConfig.getId();
		}
		if (data == null)
			data = EMPTY_STRING;
		return data;
	}

	/**
	 * Either page wants Automatic selection in combo or not. Subclass should override
	 *
	 * @return true if panel support AUTO_CONFIG
	 * @since 7.2
	 */
	protected boolean isAutoConfigSupported() {
		// original behavior was if this button is null it won't be shown and "supported"
		return true;
	}

	protected void updateProjectFromConfig(ILaunchConfiguration config) {
		String projectName = EMPTY_STRING;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		} catch (CoreException ce) {
			LaunchUIPlugin.log(ce);
		}
		if (!fProjText.getText().equals(projectName))
			fProjText.setText(projectName);
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

	@Override
	protected void updateLaunchConfigurationDialog() {
		updateComboTooltip();
		super.updateLaunchConfigurationDialog();
	}

	/**
	 * @since 7.2
	 */
	protected void updateComboTooltip() {
		if (fBuildConfigCombo != null) {
			// fBuildConfigCombo might not be loaded when controls are created
			String configId = (String) fBuildConfigCombo
					.getData(Integer.toString(fBuildConfigCombo.getSelectionIndex()));
			String tooltip = EMPTY_STRING;
			if (configId != null) {
				if (configId.equals(AUTO_CONFIG)) {
					tooltip = LaunchMessages.CMainTab_Build_Config_Auto_tooltip;
				} else if (configId.equals(EMPTY_STRING)) {
					tooltip = LaunchMessages.CMainTab_Build_Config_Active_tooltip;
				}
			}
			fBuildConfigCombo.setToolTipText(tooltip);
		}
	}
}