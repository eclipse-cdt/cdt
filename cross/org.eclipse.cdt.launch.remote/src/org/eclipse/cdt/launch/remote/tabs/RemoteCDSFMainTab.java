/*******************************************************************************
 * Copyright (c) 2006, 2016 PalmSource, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ewa Matejska          (PalmSource) - initial API and implementation
 * Martin Oberhuber      (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber      (Wind River) - [196934] hide disabled system types in remotecdt combo
 * Yu-Fen Kuo            (MontaVista) - [190613] Fix NPE in Remotecdt when RSEUIPlugin has not been loaded
 * Martin Oberhuber      (Wind River) - [cleanup] Avoid using SystemStartHere in production code
 * Johann Draschwandtner (Wind River) - [231827][remotecdt]Auto-compute default for Remote path
 * Johann Draschwandtner (Wind River) - [233057][remotecdt]Fix button enablement
 * Anna Dushistova       (MontaVista) - [181517][usability] Specify commands to be run before remote application launch
 * Anna Dushistova       (MontaVista) - [223728] [remotecdt] connection combo is not populated until RSE is activated
 * Anna Dushistova       (MontaVista) - [267951] [remotecdt] Support systemTypes without files subsystem
 * Anna Dushistova  (Mentor Graphics) - adapted from RemoteCMainTab 
 * Anna Dushistova  (Mentor Graphics) - moved to org.eclipse.cdt.launch.remote.tabs
 * Anna Dushistova  (Mentor Graphics) - [318052] [remote launch] Properties are not saved/used
 * Anna Dushistova       (MontaVista) - [375067] [remote] Automated remote launch does not support project-less debug
 * Dan Ungureanu          (Freescale) - [428367] [remote launch] Fix missing title for Properties dialog
 * Alvaro Sanchez-Leon     (Ericsson) - [430313] [remote] Auto Remote Debug - Unable to download to folder
 * Iulia Vasii            (Freescale) - [370768] new 'Edit...' button to access connection properties
 *******************************************************************************/
package org.eclipse.cdt.launch.remote.tabs;

import java.text.MessageFormat;

import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.IRemoteConnectionHostConstants;
import org.eclipse.cdt.launch.remote.RemoteHelper;
import org.eclipse.cdt.launch.remote.RemoteUIHelper;
import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class RemoteCDSFMainTab extends CMainTab {

	/* Labels and Error Messages */
	private static final String REMOTE_PROG_LABEL_TEXT = Messages.RemoteCMainTab_Program;
	private static final String USE_LOCAL_PROG_BUTTON_TEXT = Messages.RemoteCMainTab_LocalProgramGrp_UseLocal_Check;
	private static final String REMOTE_PROG_TEXT_ERROR = Messages.RemoteCMainTab_ErrorNoProgram;
	private static final String PRE_RUN_LABEL_TEXT = Messages.RemoteCMainTab_Prerun;

	/* Defaults */
	private static final String REMOTE_PATH_DEFAULT = EMPTY_STRING;
	private static final boolean SKIP_DOWNLOAD_TO_REMOTE_DEFAULT = false;

	protected Button remoteBrowseButton;
	protected Label remoteProgLabel;
	protected Text remoteProgText;
	protected Button useLocalPathButton;
	protected Button fUseLocalProg;
	protected Text fWorkingDir;
	protected Label fConnectionInfoLabel;
	protected Link fConnectionInfoChangeLink;
	private Text preRunText;
	private Label preRunLabel;

	private boolean isInitializing = false;

	private ILaunchConfiguration launchConfig;

	public RemoteCDSFMainTab() {
		super();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		/*
		 * Project configuration
		 */
		createProjectGroup(comp, 4);
		/*
		 * Local application configuration
		 */
		createVerticalSpacer(comp, 1);
		createExeFileGroup(comp, 4);
		/*
		 * Remote Configuration
		 */
		createVerticalSpacer(comp, 1);
		createRemoteConfigurationGroup(comp);
		createVerticalSpacer(comp, 1);

		setControl(comp);

		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(getControl(),
						Activator.PLUGIN_ID + ".launchgroup"); //$NON-NLS-1$

	}

	/*
	 * isValid
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		boolean retVal = true;
		/*
		 * If using local binary then heritage validation,
		 * 	otherwise skip it.
		 */
		if (fUseLocalProg.getSelection()) {
			retVal = super.isValid(config);
		}
		/*
		 * Path for remote binary should be absolute and mandatory.
		 */
		if (retVal == true) {
			setErrorMessage(null);
			String name = remoteProgText.getText().trim();
			if (name.length() == 0) {
				setErrorMessage(REMOTE_PROG_TEXT_ERROR);
				retVal = false;
			} else {
				IPath path = new Path(name);
				if (!path.isAbsolute()) {
					setErrorMessage(Messages.RemoteCMainTab_Remote_Program_Error_Not_Absolute);
					retVal = false;
				}
			}
		}
		return retVal;
	}
	/**
	 * createExeFileGroup.
	 *
	 * @see org.eclipse.cdt.launch.ui.CMainTab2#createExeFileGroup(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void createExeFileGroup(Composite parent, int colSpan) {
		Group localGrp = new Group(parent, SWT.SHADOW_ETCHED_OUT);
		localGrp.setLayout(new GridLayout());
		localGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		localGrp.setText(Messages.RemoteCMainTab_LocalProgramGrp_Title);
		fUseLocalProg = new Button(localGrp, SWT.CHECK);
		fUseLocalProg.setText(USE_LOCAL_PROG_BUTTON_TEXT);
		fUseLocalProg.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fUseLocalProg.getSelection()) {
					fProgText.setEnabled(true);
				} else {
					fProgText.setEnabled(false);
				}
			}
		});
		super.createExeFileGroup(localGrp, colSpan);
		super.createBuildOptionGroup(localGrp, colSpan);

		fProgLabel.setText(Messages.RemoteCMainTab_LocalProgramGrp_Program_Label);

		/* If the local binary path changes,
		 * 	modify the remote binary location.
		 */
		fProgText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				setLocalPathForRemotePath();
			}
		});
	}
	/**
	 * Creates group to remote launch configuration.
	 *
	 * @param parent Parent composite
	 */
	protected void createRemoteConfigurationGroup(Composite parent) {
		Group remoteGrp = new Group(parent, SWT.NONE);
		remoteGrp.setLayout(new GridLayout());
		remoteGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		remoteGrp.setText(Messages.RemoteCMainTab_RemoteSetupGrp_Title);
		createConnectionInfoGroup(remoteGrp);
		createTargetExePathGroup(remoteGrp);
	}
	/**
	 * Creates group with current connection information.
	 *
	 * @param parent Parent composite.
	 */
	protected void createConnectionInfoGroup(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new RowLayout());
		fConnectionInfoLabel = new Label(comp, SWT.NONE);
		Link changeConnection = new Link(comp, SWT.NONE);
		changeConnection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				/*
				 * Switch to remote system tab.
				 */
				ILaunchConfigurationTab tabs[] = getLaunchConfigurationDialog().getTabs();
				for (int i = 0; i < tabs.length; i++) {
					if (tabs[i].getName() == RemoteSystemTab.TAB_NAME) {
						getLaunchConfigurationDialog().setActiveTab(tabs[i]);
						break;
					}
				}
			}
		});
		changeConnection.setText("(<a>" + //$NON-NLS-1$
				Messages.RemoteCMainTab_ConnectionInfo_Link + "</a>)"); //$NON-NLS-1$
	}
	/*
	 * createTargetExePath This creates the remote path user-editable textfield
	 * on the Main Tab.
	 */
	protected void createTargetExePathGroup(Composite parent) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 2;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);

		remoteProgLabel = new Label(mainComp, SWT.NONE);
		remoteProgLabel.setText(REMOTE_PROG_LABEL_TEXT);
		gd = new GridData();
		gd.horizontalSpan = 2;
		remoteProgLabel.setLayoutData(gd);

		remoteProgText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		remoteProgText.setLayoutData(gd);
		remoteProgText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		remoteBrowseButton = createPushButton(mainComp,
				Messages.RemoteCMainTab_RemoteSetupGrp_PathBrowse_Button, null);
		remoteBrowseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleRemoteBrowseSelected();
				updateLaunchConfigurationDialog();
			}
		});

		// Commands to run before execution
		preRunLabel = new Label(mainComp, SWT.NONE);
		preRunLabel.setText(PRE_RUN_LABEL_TEXT);
		gd = new GridData();
		gd.horizontalSpan = 2;
		preRunLabel.setLayoutData(gd);

		preRunText = new Text(mainComp, SWT.MULTI | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		preRunText.setLayoutData(gd);
		preRunText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

	}

	protected IRemoteConnection getCurrentConnection() {
		IRemoteConnection conn = RemoteSystemTab.getConnection(launchConfig);
		return conn;
	}

	protected void handleRemoteBrowseSelected() {
		IRemoteConnection currentConnectionSelected = getCurrentConnection();
		RemoteResourceBrowser b = new RemoteResourceBrowser(getControl().getShell(),
				SWT.NONE);
		b.setConnection(currentConnectionSelected);
		b.setTitle(Messages.RemoteCMainTab_Remote_Path_Browse_Button_Title);
		b.open();
		IFileStore selectedFile = b.getResource();
		if (selectedFile != null) {
			String absPath = selectedFile.toURI().getPath();
			if (selectedFile.fetchInfo().isDirectory()) {
				// The user selected a destination folder to upload the binary
				// Append the Program name as the default file destination
				IPath appPath = new Path(fProgText.getText().trim());
				String lastSegment = appPath.lastSegment();
				if (lastSegment != null && lastSegment.trim().length() > 0) {
					IPath remotePath = new Path(absPath).append(lastSegment.trim());
					absPath = remotePath.toPortableString();
				}
			}
			remoteProgText.setText(absPath);
		}
	}
	protected void updateTargetProgFromConfig(ILaunchConfiguration config) {
		String targetPath = null;
		try {
			targetPath = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
					REMOTE_PATH_DEFAULT);
		} catch (CoreException e) {
			// Ignore
		}
		remoteProgText.setText(targetPath);

		String prelaunchCmd = null;
		try {
			prelaunchCmd = config
					.getAttribute(
							IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS,
							""); //$NON-NLS-1$
		} catch (CoreException e) {
			// Ignore
		}
		preRunText.setText(prelaunchCmd);
	}

	protected void updateSkipDownloadFromConfig(ILaunchConfiguration config) {
		boolean downloadToTarget = true;
		try {
			downloadToTarget = ! config
					.getAttribute(
							IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
							getDefaultSkipDownload());
		} catch (CoreException e) {
			// Ignore for now
		}
		/*
		 * To maintain compatibility, use skip download field to determine whether
		 * 	use local program to run on remote system or not.
		 */
		fUseLocalProg.setSelection(downloadToTarget);
	}

	/*
	 * setLocalPathForRemotePath This function sets the remote path text field
	 * with the value of the local executable path.
	 */
	private void setLocalPathForRemotePath() {
		String programName = fProgText.getText().trim();
		boolean bUpdateRemote = false;

		String remoteName = remoteProgText.getText().trim();
		String remoteWsRoot = getRemoteWSRoot();
		if (remoteName.length() == 0) {
			bUpdateRemote = true;
		} else if (remoteWsRoot.length() != 0) {
			bUpdateRemote = remoteName.equals(remoteWsRoot);
		}

		if (programName.length() != 0 && bUpdateRemote) {
			IPath exePath = new Path(programName);
			if (!exePath.isAbsolute()) {
				IProject project = getCProject().getProject();
				exePath = project.getFile(programName).getLocation();

				IPath wsRoot = project.getWorkspace().getRoot().getLocation();
				exePath = makeRelativeToWSRootLocation(exePath, remoteWsRoot,
						wsRoot);
			}
			String path = exePath.toString();
			remoteProgText.setText(path);
		}
	}

	private void useDefaultsFromConnection() {
		// During initialization, we don't want to use the default
		// values of the connection, but we want to use the ones
		// that are part of the configuration
		if (isInitializing) return;
		
		if ((remoteProgText != null) && !remoteProgText.isDisposed()) {
			String remoteName = remoteProgText.getText().trim();
			String remoteWsRoot = getRemoteWSRoot();
			if (remoteName.length() == 0) {
				remoteProgText.setText(remoteWsRoot);
			} else {
				// try to use remote path
				IPath wsRoot = Platform.getLocation();
				IPath remotePath = makeRelativeToWSRootLocation(new Path(
						remoteName), remoteWsRoot, wsRoot);
				remoteProgText.setText(remotePath.toString());
			}
		}
		if ((remoteBrowseButton != null) && !remoteBrowseButton.isDisposed()) {
			if (RemoteHelper.getFileSubsystem(getCurrentConnection()) == null) {
				remoteBrowseButton.setEnabled(false);
			} else {
				remoteBrowseButton.setEnabled(true);
			}
		}
	}

	private IPath makeRelativeToWSRootLocation(IPath exePath,
			String remoteWsRoot, IPath wsRoot) {
		if (remoteWsRoot.length() != 0) {
			// use remoteWSRoot instead of Workspace Root
			if (wsRoot.isPrefixOf(exePath)) {
				return new Path(remoteWsRoot).append(exePath
						.removeFirstSegments(wsRoot.segmentCount()).setDevice(
								null));
			}
		}
		return exePath;
	}

	private String getRemoteWSRoot() {
		IRemoteConnection host = getCurrentConnection();
		if(host != null) {
			String value = RemoteUIHelper.getConnectionProperty(host,
					IRemoteConnectionHostConstants.REMOTE_WS_ROOT);
			if(!value.isEmpty()) {
				return value;
			}
		}
		return ""; //$NON-NLS-1$
	}

	private boolean getDefaultSkipDownload() {
		IRemoteConnection host = getCurrentConnection();
		if (host != null) {
			if(RemoteHelper.getFileSubsystem(host) == null){
				return true;
			}
			String value = RemoteUIHelper.getConnectionProperty(host,
					IRemoteConnectionHostConstants.DEFAULT_SKIP_DOWNLOAD);
			if(!value.isEmpty()) {
				return Boolean.valueOf(value).booleanValue();
			}
		}
		return SKIP_DOWNLOAD_TO_REMOTE_DEFAULT;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		isInitializing = true;
		super.initializeFrom(config);
		launchConfig = config;
		updateTargetProgFromConfig(config);
		updateSkipDownloadFromConfig(config);
		useDefaultsFromConnection();
		IRemoteConnection conn = getCurrentConnection();
		if (conn != null) {
			fConnectionInfoLabel.setText(MessageFormat.format(
					Messages.RemoteCMainTab_ConnectionInfo_Label, conn.getName()));
		}

		isInitializing = false;
	}

	/*
	 * performApply
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {

		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
				remoteProgText.getText());
		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS,
				preRunText.getText());
		boolean skipDownload = false;
		if(! fUseLocalProg.getSelection()) {
			skipDownload = true;
		}
		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
				skipDownload);
		super.performApply(config);
	}

	@Override
	public String getId() {
		return "org.eclipse.cdt.launch.remote.dsf.mainTab"; //$NON-NLS-1$
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
				EMPTY_STRING);
		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
				REMOTE_PATH_DEFAULT);
		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
				SKIP_DOWNLOAD_TO_REMOTE_DEFAULT);
		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS,
				EMPTY_STRING);
	}

}
