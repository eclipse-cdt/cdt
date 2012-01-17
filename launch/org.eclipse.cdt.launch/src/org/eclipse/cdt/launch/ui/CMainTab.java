/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ken Ryall (Nokia) - bug 178731
 *	   IBM Corporation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * A launch configuration tab that displays and edits project and main type name launch
 * configuration attributes.
 * <p>
 * This class may be instantiated. This class is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class CMainTab extends CAbstractMainTab {

    /**
     * Tab identifier used for ordering of tabs added using the 
     * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
     * extension point.
     *   
     * @since 6.0
     */
    public static final String TAB_ID = "org.eclipse.cdt.cdi.launch.mainTab"; //$NON-NLS-1$

	private final boolean fWantsTerminalOption;
	protected Button fTerminalButton;

	private final boolean dontCheckProgram;
	private final boolean fSpecifyCoreFile;

	public static final int WANTS_TERMINAL = 1;
	public static final int DONT_CHECK_PROGRAM = 2;
	/** @since 6.0 */
	public static final int SPECIFY_CORE_FILE = 4;

	public CMainTab() {
		this(WANTS_TERMINAL);
	}

	public CMainTab(boolean terminalOption) {
		this(terminalOption ? WANTS_TERMINAL : 0);
	}

	public CMainTab(int flags) {
		fWantsTerminalOption = (flags & WANTS_TERMINAL) != 0;
		dontCheckProgram = (flags & DONT_CHECK_PROGRAM) != 0;
		fSpecifyCoreFile = (flags & SPECIFY_CORE_FILE) != 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createExeFileGroup(comp, 1);
		createProjectGroup(comp, 1);
		createBuildOptionGroup(comp, 1);
		createVerticalSpacer(comp, 1);
		if (fSpecifyCoreFile) {
			createCoreFileGroup(comp, 1);
		}
		if (wantsTerminalOption() /* && ProcessFactory.supportesTerminal() */) {
			createTerminalOption(comp, 1);
		}
		LaunchUIPlugin.setDialogShell(parent.getShell());
	}

	protected boolean wantsTerminalOption() {
		return fWantsTerminalOption;
	}

	protected void createTerminalOption(Composite parent, int colSpan) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		mainComp.setLayoutData(gd);

		fTerminalButton = createCheckButton(mainComp, LaunchMessages.CMainTab_UseTerminal); 
		fTerminalButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		fTerminalButton.setEnabled(PTY.isSupported());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		filterPlatform = getPlatform(config);
		updateProjectFromConfig(config);
		updateProgramFromConfig(config);
		updateCoreFromConfig(config);
		updateBuildOptionFromConfig(config);
		updateTerminalFromConfig(config);
	}

	protected void updateTerminalFromConfig(ILaunchConfiguration config) {
		if (fTerminalButton != null) {
			boolean useTerminal = true;
			try {
				useTerminal = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
			} catch (CoreException e) {
				LaunchUIPlugin.log(e);
			}
			fTerminalButton.setSelection(useTerminal);
		}
	}

	/** @since 6.0 */
	protected void updateCoreFromConfig(ILaunchConfiguration config) {
		if (fCoreText != null) {
			String coreName = EMPTY_STRING;
			try {
				coreName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, EMPTY_STRING);
			} catch (CoreException ce) {
				LaunchUIPlugin.log(ce);
			}
			fCoreText.setText(coreName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		super.performApply(config);
		ICProject cProject = this.getCProject();
		if (cProject != null && cProject.exists()) {
			config.setMappedResources(new IResource[] { cProject.getProject() });
		} else {
			// the user typed in a non-existent project name.  Ensure that
			// won't be suppressed from the dialog.  This matches JDT behaviour
			config.setMappedResources(null);
		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		if (fProgText != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, fProgText.getText());
		}
		if (fCoreText != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, fCoreText.getText());
		}
		if (fTerminalButton != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, fTerminalButton.getSelection());
		}
	}

	/**
	 * Show a dialog that lists all main types
	 */
	@Override
	protected void handleSearchButtonSelected() {
		if (getCProject() == null) {
			MessageDialog.openInformation(getShell(), LaunchMessages.CMainTab_Project_required, 
					LaunchMessages.CMainTab_Enter_project_before_searching_for_program); 
			return;
		}

		ILabelProvider programLabelProvider = new CElementLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getPath().lastSegment());
					return name.toString();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (! (element instanceof ICElement)) {
					return super.getImage(element);
				}
				ICElement celement = (ICElement)element;

				if (celement.getElementType() == ICElement.C_BINARY) {
					IBinary belement = (IBinary)celement;
					if (belement.isExecutable()) {
						return DebugUITools.getImage(IDebugUIConstants.IMG_ACT_RUN);
					}
				}

				return super.getImage(element);
			}
		};

		ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getCPU() + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
					name.append(" - "); //$NON-NLS-1$
					name.append(bin.getPath().toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		TwoPaneElementSelector dialog = new TwoPaneElementSelector(getShell(), programLabelProvider, qualifierLabelProvider);
		dialog.setElements(getBinaryFiles(getCProject()));
		dialog.setMessage(LaunchMessages.CMainTab_Choose_program_to_run); 
		dialog.setTitle(LaunchMessages.CMainTab_Program_Selection); 
		dialog.setUpperListLabel(LaunchMessages.Launch_common_BinariesColon); 
		dialog.setLowerListLabel(LaunchMessages.Launch_common_QualifierColon); 
		dialog.setMultipleSelection(false);
		// dialog.set
		if (dialog.open() == Window.OK) {
			IBinary binary = (IBinary)dialog.getFirstResult();
			fProgText.setText(binary.getResource().getProjectRelativePath().toString());
		}
	}

	/**
	 * @since 6.0
	 */
	@Override
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
				
				updateBuildConfigCombo(""); //$NON-NLS-1$
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

	protected void createExeFileGroup(Composite parent, int colSpan) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		mainComp.setLayoutData(gd);
		fProgLabel = new Label(mainComp, SWT.NONE);
		fProgLabel.setText(LaunchMessages.CMainTab_C_Application); 
		gd = new GridData();
		gd.horizontalSpan = 3;
		fProgLabel.setLayoutData(gd);
		fProgText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProgText.setLayoutData(gd);
		fProgText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fSearchButton = createPushButton(mainComp, LaunchMessages.CMainTab_Search, null); 
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		Button fBrowseForBinaryButton;
		fBrowseForBinaryButton = createPushButton(mainComp, LaunchMessages.Launch_common_Browse_2, null); 
		fBrowseForBinaryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleBinaryBrowseButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}

	/**
	 * Show a dialog that lets the user select a project. This in turn provides context for the main
	 * type, allowing the user to key a main type name, or constraining the search for main types to
	 * the specified project.
	 */
	protected void handleBinaryBrowseButtonSelected() {
		final ICProject cproject = getCProject();
		if (cproject == null) {
			MessageDialog.openInformation(getShell(), LaunchMessages.CMainTab_Project_required, 
					LaunchMessages.CMainTab_Enter_project_before_browsing_for_program); 
			return;
		}
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(fProgText.getText());
		String text= fileDialog.open();
		if (text != null) {
			fProgText.setText(text);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		if (!dontCheckProgram) {
			String name = fProjText.getText().trim();
			if (name.length() == 0) {
				setErrorMessage(LaunchMessages.CMainTab_Project_not_specified); 
				return false;
			}
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LaunchMessages.Launch_common_Project_does_not_exist); 
				return false;
			}
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (!project.isOpen()) {
				setErrorMessage(LaunchMessages.CMainTab_Project_must_be_opened); 
				return false;
			}
	
			name = fProgText.getText().trim();
			if (name.length() == 0) {
				setErrorMessage(LaunchMessages.CMainTab_Program_not_specified); 
				return false;
			}
			if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
				setErrorMessage(LaunchMessages.CMainTab_Program_does_not_exist); 
				return false;
			}
			// Avoid constantly checking the binary if nothing relevant has
			// changed (binary or project name). See bug 277663.
			if (name.equals(fPreviouslyCheckedProgram)) {
				if (fPreviouslyCheckedProgramErrorMsg != null) {
					setErrorMessage(fPreviouslyCheckedProgramErrorMsg);
				}
				return fPreviouslyCheckedProgramIsValid;
			} else {
				fPreviouslyCheckedProgram = name;
				fPreviouslyCheckedProgramIsValid = true;	// we'll flip this below if not true
				fPreviouslyCheckedProgramErrorMsg = null;   // we'll set this below if there's an error
				IPath exePath = new Path(name);
				if (!exePath.isAbsolute()) {
					IPath location = project.getLocation();
					if (location == null) {
						setErrorMessage(fPreviouslyCheckedProgramErrorMsg = LaunchMessages.CMainTab_Program_does_not_exist); 
						return (fPreviouslyCheckedProgramIsValid = false);
					}
		
					exePath = location.append(name);
					if (!exePath.toFile().exists()) {
						// Try the old way, which is required to support linked resources.
						IFile projFile = null;					
						try {
							projFile = project.getFile(name);
						} catch (IllegalArgumentException e) {
							// thrown if relative path that resolves to a root file ("..\somefile")
						}
						if (projFile == null || !projFile.exists()) {
							setErrorMessage(fPreviouslyCheckedProgramErrorMsg = LaunchMessages.CMainTab_Program_does_not_exist); 
							return (fPreviouslyCheckedProgramIsValid = false);
						} else {
							exePath = projFile.getLocation();
						}
					}
				} 
				if (!exePath.toFile().exists()) {
					setErrorMessage(fPreviouslyCheckedProgramErrorMsg = LaunchMessages.CMainTab_Program_does_not_exist); 
					return (fPreviouslyCheckedProgramIsValid = false);
				}
				// Notice that we don't check if exePath points to a valid executable since such
				// check is too expensive to be done on the UI thread.
				// See "https://bugs.eclipse.org/bugs/show_bug.cgi?id=328012".
			}
		}
		
		if (fCoreText != null) {
			String coreName = fCoreText.getText().trim();
			// We accept an empty string.  This should trigger a prompt to the user
			// This allows to re-use the launch, with a different core file.
			if (!coreName.equals(EMPTY_STRING)) {
				if (coreName.equals(".") || coreName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
					setErrorMessage(LaunchMessages.CMainTab_Core_does_not_exist); 
					return false;
				}
				IPath corePath = new Path(coreName);
				if (!corePath.toFile().exists()) {
					setErrorMessage(LaunchMessages.CMainTab_Core_does_not_exist); 
					return false;
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		// We set empty attributes for project & program so that when one config
		// is
		// compared to another, the existence of empty attributes doesn't cause
		// an
		// incorrect result (the performApply() method can result in empty
		// values
		// for these attributes being set on a config if there is nothing in the
		// corresponding text boxes)
		// plus getContext will use this to base context from if set.
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, EMPTY_STRING);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, EMPTY_STRING);

		// Set the auto choose build configuration to true for new configurations.
		// Existing configurations created before this setting was introduced will have this disabled.
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_AUTO, true);

		ICElement cElement = null;
		cElement = getContext(config, getPlatform(config));
		if (cElement != null) {
			initializeCProject(cElement, config);
			initializeProgramName(cElement, config);
		} else {
			// don't want to remember the interim value from before
			config.setMappedResources(null);
		}
		if (wantsTerminalOption()) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, ICDTLaunchConfigurationConstants.USE_TERMINAL_DEFAULT);
		}
	}

	/**
	 * Set the program name attributes on the working copy based on the ICElement
	 */
	protected void initializeProgramName(ICElement cElement, ILaunchConfigurationWorkingCopy config) {
		boolean renamed = false;

		if (!(cElement instanceof IBinary)) {
			cElement = cElement.getCProject();
		}
		
		if (cElement instanceof ICProject) {
			IProject project = cElement.getCProject().getProject();
			String name = project.getName();
			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
			if (projDes != null) {
				String buildConfigName = projDes.getActiveConfiguration().getName();
				// Bug 234951
				name = NLS.bind(LaunchMessages.CMainTab_Configuration_name, name, buildConfigName);
			}
			name = getLaunchConfigurationDialog().generateName(name);
			config.rename(name);
			renamed = true;
		}

		IBinary binary = null;
		if (cElement instanceof ICProject) {
			IBinary[] bins = getBinaryFiles((ICProject)cElement);
			if (bins != null && bins.length == 1) {
				binary = bins[0];
			}
		} else if (cElement instanceof IBinary) {
			binary = (IBinary)cElement;
		}

		if (binary != null) {
			String path;
			path = binary.getResource().getProjectRelativePath().toOSString();
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, path);
			if (!renamed) {
				String name = binary.getElementName();
				int index = name.lastIndexOf('.');
				if (index > 0) {
					name = name.substring(0, index);
				}
				name = getLaunchConfigurationDialog().generateName(name);
				config.rename(name);
				renamed = true;				
			}
		}
		
		if (!renamed) {
			String name = getLaunchConfigurationDialog().generateName(cElement.getCProject().getElementName());
			config.rename(name);
		}
	}

	@Override
    public String getId() {
        return TAB_ID;
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return LaunchMessages.CMainTab_Main; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_MAIN_TAB);
	}
}
