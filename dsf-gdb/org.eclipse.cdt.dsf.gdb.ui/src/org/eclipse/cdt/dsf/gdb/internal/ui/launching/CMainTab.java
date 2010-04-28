/*******************************************************************************
 * Copyright (c) 2008, 2010  QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ken Ryall (Nokia) - bug 178731
 *     Ericsson - Support for tracepoint post-mortem debugging
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.launch.ui.CAbstractMainTab;
import org.eclipse.cdt.ui.CElementLabelProvider;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
 */

public class CMainTab extends CAbstractMainTab {

    /**
     * Tab identifier used for ordering of tabs added using the 
     * <code>org.eclipse.debug.ui.launchConfigurationTabs</code>
     * extension point.
     *   
     * @since 2.0
     */
    public static final String TAB_ID = "org.eclipse.cdt.dsf.gdb.launch.mainTab"; //$NON-NLS-1$
    
    private static final String CORE_FILE = LaunchMessages.getString("CMainTab.CoreFile_type"); //$NON-NLS-1$
    private static final String TRACE_FILE = LaunchMessages.getString("CMainTab.TraceFile_type"); //$NON-NLS-1$
    
    /**
     * Combo box to select which type of post mortem file should be used.
     * We currently support core files and trace files.
     * 
     * @since 2.1
     */
    protected Combo fCoreTypeCombo;
    
	private final boolean fDontCheckProgram;
	private final boolean fSpecifyCoreFile;
	private final boolean fIncludeBuildSettings;

	public static final int DONT_CHECK_PROGRAM = 2;
	public static final int SPECIFY_CORE_FILE = 4;
	public static final int INCLUDE_BUILD_SETTINGS = 8;
	
	public CMainTab() {
		this(INCLUDE_BUILD_SETTINGS);
	}

	public CMainTab(int flags) {
		fDontCheckProgram = (flags & DONT_CHECK_PROGRAM) != 0;
		fSpecifyCoreFile = (flags & SPECIFY_CORE_FILE) != 0;
		fIncludeBuildSettings = (flags & INCLUDE_BUILD_SETTINGS) != 0;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GdbUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createExeFileGroup(comp, 1);
		createProjectGroup(comp, 1);
		if (fIncludeBuildSettings){
			createBuildOptionGroup(comp, 1);			
		}
		createVerticalSpacer(comp, 1);
		if (fSpecifyCoreFile) {
			createCoreFileGroup(comp, 1);
		}
		
		GdbUIPlugin.setDialogShell(parent.getShell());
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
		fProgLabel.setText(LaunchMessages.getString("CMainTab.C/C++_Application")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalSpan = 3;
		fProgLabel.setLayoutData(gd);
		fProgText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProgText.setLayoutData(gd);
		fProgText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		fSearchButton = createPushButton(mainComp, LaunchMessages.getString("CMainTab.Search..."), null); //$NON-NLS-1$
		fSearchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleSearchButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});

		Button browseForBinaryButton;
		browseForBinaryButton = createPushButton(mainComp, LaunchMessages.getString("Launch.common.Browse_2"), null); //$NON-NLS-1$
		browseForBinaryButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				String text = handleBrowseButtonSelected(LaunchMessages.getString("CMaintab.Application_Selection")); //$NON-NLS-1$
				if (text != null) {
					fProgText.setText(text);
				}
				updateLaunchConfigurationDialog();
			}
		});
	}	

	/*
	 * Overridden to add the possibility to choose a trace file as a post mortem debug file.
	 */
	/** @since 2.1 */
	@Override
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
		
		Label comboLabel = new Label(coreComp, SWT.NONE);
		comboLabel.setText(LaunchMessages.getString("CMainTab.Post_mortem_file_type")); //$NON-NLS-1$
		comboLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		fCoreTypeCombo = new Combo(coreComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		fCoreTypeCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		fCoreTypeCombo.add(CORE_FILE);
		fCoreTypeCombo.add(TRACE_FILE);

		fCoreLabel = new Label(coreComp, SWT.NONE);
		fCoreLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		fCoreText = new Text(coreComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fCoreText.setLayoutData(gd);
		fCoreText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		Button browseForCoreButton;
		browseForCoreButton = createPushButton(coreComp, LaunchMessages.getString("Launch.common.Browse_2"), null); //$NON-NLS-1$
		browseForCoreButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				String text;
				String coreType = getSelectedCoreType();
				if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE)) {
					text = handleBrowseButtonSelected(LaunchMessages.getString("CMaintab.Core_Selection")); //$NON-NLS-1$
				} else if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE)) {
					text = handleBrowseButtonSelected(LaunchMessages.getString("CMaintab.Trace_Selection")); //$NON-NLS-1$
				} else {
					assert false : "Unknown core file type"; //$NON-NLS-1$
					text = handleBrowseButtonSelected(LaunchMessages.getString("CMaintab.Core_Selection")); //$NON-NLS-1$
				}

				if (text != null) {
					fCoreText.setText(text);
				}
				updateLaunchConfigurationDialog();
			}
		});
		
		fCoreTypeCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateCoreFileLabel();
				updateLaunchConfigurationDialog();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		fCoreTypeCombo.select(0);
	}

	/**
	 * Show a dialog that lets the user select a file.
	 * This method allows to set the title of the dialog.
	 * 
	 * @param title The title the dialog should show.
	 * 
	 * @since 2.1
	 */
	protected String handleBrowseButtonSelected(String title) {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setText(title);
		fileDialog.setFileName(fProgText.getText());
		return fileDialog.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration config) {
		filterPlatform = getPlatform(config);
		updateProjectFromConfig(config);
		updateProgramFromConfig(config);
		updateCoreFromConfig(config);
		updateBuildOptionFromConfig(config);
	}
	
	/** @since 2.0 */
	protected void updateCoreFromConfig(ILaunchConfiguration config) {
		if (fCoreText != null) {
			String coreName = EMPTY_STRING;
			String coreType = IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TYPE_DEFAULT;
			try {
				coreName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, EMPTY_STRING);
				coreType = config.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE,
						                       IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TYPE_DEFAULT);
			} catch (CoreException ce) {
				GdbUIPlugin.log(ce);
			}
			fCoreText.setText(coreName);
			if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE)) {
				fCoreTypeCombo.setText(CORE_FILE);
			} else if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE)) {
				fCoreTypeCombo.setText(TRACE_FILE);
			} else {
				assert false : "Unknown core file type"; //$NON-NLS-1$
			fCoreTypeCombo.setText(CORE_FILE);
			}
			updateCoreFileLabel();
		}
	}
	
	/** @since 2.1 */
	protected String getSelectedCoreType() {
		int selectedIndex = fCoreTypeCombo.getSelectionIndex();
		if (fCoreTypeCombo.getItem(selectedIndex).equals(CORE_FILE)) {
			return IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE;
		} else if (fCoreTypeCombo.getItem(selectedIndex).equals(TRACE_FILE)) {
			return IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE;
		} else {
			assert false : "Unknown post mortem file type"; //$NON-NLS-1$
			return IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE;
		}
	}

	/** @since 2.1 */
	protected void updateCoreFileLabel() {
		String coreType = getSelectedCoreType();
		if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_CORE_FILE)) {
			fCoreLabel.setText(LaunchMessages.getString("CMainTab.CoreFile_path")); //$NON-NLS-1$
		} else if (coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE)) {
			fCoreLabel.setText(LaunchMessages.getString("CMainTab.TraceFile_path")); //$NON-NLS-1$
		} else {
			assert false : "Unknown post mortem file type"; //$NON-NLS-1$
			fCoreLabel.setText(LaunchMessages.getString("CMainTab.CoreFile_path")); //$NON-NLS-1$
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
		if (cProject != null && cProject.exists())
		{
			config.setMappedResources(new IResource[] { cProject.getProject() });
		} else {
			config.setMappedResources(null);
		}

		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProjText.getText());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, fProgText.getText());
		if (fCoreText != null) {
			config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, fCoreText.getText());
			config.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE, getSelectedCoreType());
		}
	}

	/**
	 * Show a dialog that lists all main types
	 */
	@Override
	protected void handleSearchButtonSelected() {

		if (getCProject() == null) {
			MessageDialog.openInformation(getShell(), LaunchMessages.getString("CMainTab.Project_required"), //$NON-NLS-1$
					LaunchMessages.getString("CMainTab.Enter_project_before_searching_for_program")); //$NON-NLS-1$
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
		dialog.setMessage(LaunchMessages.getString("CMainTab.Choose_program_to_run")); //$NON-NLS-1$
		dialog.setTitle(LaunchMessages.getString("CMainTab.Program_Selection")); //$NON-NLS-1$
		dialog.setUpperListLabel(LaunchMessages.getString("Launch.common.BinariesColon")); //$NON-NLS-1$
		dialog.setLowerListLabel(LaunchMessages.getString("Launch.common.QualifierColon")); //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		// dialog.set
		if (dialog.open() == Window.OK) {
			IBinary binary = (IBinary)dialog.getFirstResult();
			fProgText.setText(binary.getResource().getProjectRelativePath().toString());
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

		if (!fDontCheckProgram) {
			String name = fProjText.getText().trim();
			if (name.length() == 0) {
				setErrorMessage(LaunchMessages.getString("CMainTab.Project_not_specified")); //$NON-NLS-1$
				return false;
			}
			if (!ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
				setErrorMessage(LaunchMessages.getString("Launch.common.Project_does_not_exist")); //$NON-NLS-1$
				return false;
			}
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (!project.isOpen()) {
				setErrorMessage(LaunchMessages.getString("CMainTab.Project_must_be_opened")); //$NON-NLS-1$
				return false;
			}

			name = fProgText.getText().trim();
			if (name.length() == 0) {
				setErrorMessage(LaunchMessages.getString("CMainTab.Program_not_specified")); //$NON-NLS-1$
				return false;
			}
			if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
				setErrorMessage(LaunchMessages.getString("CMainTab.Program_does_not_exist")); //$NON-NLS-1$
				return false;
			}
			IPath exePath = new Path(name);
			if (!exePath.isAbsolute()) {
				if (!project.getFile(name).exists()) {
					setErrorMessage(LaunchMessages.getString("CMainTab.Program_does_not_exist")); //$NON-NLS-1$
					return false;
				}
				exePath = project.getFile(name).getLocation();
			} else {
				if (!exePath.toFile().exists()) {
					setErrorMessage(LaunchMessages.getString("CMainTab.Program_does_not_exist")); //$NON-NLS-1$
					return false;
				}
			}
			try {
				if (!isBinary(project, exePath)) {
					setErrorMessage(LaunchMessages.getString("CMainTab.Program_is_not_a_recongnized_executable")); //$NON-NLS-1$
					return false;
				}
			} catch (CoreException e) {
				GdbUIPlugin.log(e);
				setErrorMessage(e.getLocalizedMessage());
				return false;
			}
		}
		
		if (fCoreText != null) {
			String coreName = fCoreText.getText().trim();
			// We accept an empty string.  This should trigger a prompt to the user
			// This allows to re-use the launch, with a different core file.
			if (!coreName.equals(EMPTY_STRING)) {
				if (coreName.equals(".") || coreName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
					setErrorMessage(LaunchMessages.getString("CMainTab.File_does_not_exist")); //$NON-NLS-1$
					return false;
				}
				IPath corePath = new Path(coreName);
				if (!corePath.toFile().exists()) {
					setErrorMessage(LaunchMessages.getString("CMainTab.File_does_not_exist")); //$NON-NLS-1$
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
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		// We set empty attributes for project & program so that when one config is
		// compared to another, the existence of empty attributes doesn't cause and
		// incorrect result (the performApply() method can result in empty values
		// for these attributes being set on a config if there is nothing in the
		// corresponding text boxes)
		// plus getContext will use this to base context from if set.
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, EMPTY_STRING);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, EMPTY_STRING);

		ICElement cElement = null;
		cElement = getContext(config, getPlatform(config));
		if (cElement != null) {
			initializeCProject(cElement, config);
			initializeProgramName(cElement, config);
		} else {
			// don't want to remember the interim value from before
			config.setMappedResources(null);
		}
	}

	/**
	 * Set the program name attributes on the working copy based on the ICElement
	 */
	protected void initializeProgramName(ICElement cElement, ILaunchConfigurationWorkingCopy config) {

		boolean renamed = false;

		if (!(cElement instanceof IBinary))
		{
			cElement = cElement.getCProject();
		}
		
		if (cElement instanceof ICProject) {

			IProject project = cElement.getCProject().getProject();
			String name = project.getName();
			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
			if (projDes != null) {
				String buildConfigName = projDes.getActiveConfiguration().getName();
				name = name + " " + buildConfigName; //$NON-NLS-1$
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
			if (!renamed)
			{
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
		
		if (!renamed)
		{
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
	public String getName() {
		return LaunchMessages.getString("CMainTab.Main"); //$NON-NLS-1$
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
