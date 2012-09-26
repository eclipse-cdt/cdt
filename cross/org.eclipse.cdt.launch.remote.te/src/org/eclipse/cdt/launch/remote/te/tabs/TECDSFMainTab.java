/*******************************************************************************
 * Copyright (c) 2006, 2012 PalmSource, Inc. and others.
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
 * Anna Dushistova       (MontaVista) - adapted from RemoteCDSFMainTab
 *******************************************************************************/
package org.eclipse.cdt.launch.remote.te.tabs;

import org.eclipse.cdt.dsf.gdb.internal.ui.launching.CMainTab;
import org.eclipse.cdt.launch.internal.remote.te.Messages;
import org.eclipse.cdt.launch.remote.te.IRemoteTEConfigurationConstants;
import org.eclipse.cdt.launch.remote.te.controls.TCFPeerSelector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.dialogs.FSOpenFileDialog;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

public class TECDSFMainTab extends CMainTab {

	/* Labels and Error Messages */
	private static final String REMOTE_PROG_LABEL_TEXT = Messages.RemoteCMainTab_Program;
	private static final String SKIP_DOWNLOAD_BUTTON_TEXT = Messages.RemoteCMainTab_SkipDownload;
	private static final String REMOTE_PROG_TEXT_ERROR = Messages.RemoteCMainTab_ErrorNoProgram;
	private static final String CONNECTION_TEXT_ERROR = Messages.RemoteCMainTab_ErrorNoConnection;
	private static final String PRE_RUN_LABEL_TEXT = Messages.RemoteCMainTab_Prerun;

	/* Defaults */
	private static final String REMOTE_PATH_DEFAULT = EMPTY_STRING;
	private static final boolean SKIP_DOWNLOAD_TO_REMOTE_DEFAULT = false;

	protected Button remoteBrowseButton;
	protected TCFPeerSelector peerSelector;
	protected Label remoteProgLabel;
	protected Text remoteProgText;
	protected Button skipDownloadButton;

	private Text preRunText;
	private Label preRunLabel;

	public TECDSFMainTab() {
		super(CMainTab.INCLUDE_BUILD_SETTINGS);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite comp = (Composite) getControl();
		/* The TE Connection dropdown */
		createVerticalSpacer(comp, 1);
		createRemoteConnectionGroup(comp);
		/* The remote binary location and skip download option */
		createVerticalSpacer(comp, 1);
		createTargetExePathGroup(comp);
		createDownloadOption(comp);

		/* If the local binary path changes, modify the remote binary location */
		fProgText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				setLocalPathForRemotePath();
			}
		});
	}

	/*
	 * isValid
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		boolean retVal = super.isValid(config);
		if (retVal == true) {
			setErrorMessage(null);
			if (peerSelector.getPeerId() == null) {
				setErrorMessage(CONNECTION_TEXT_ERROR);
				retVal = false;
			}
			if (retVal) {
				String name = remoteProgText.getText().trim();
				if (name.length() == 0) {
					setErrorMessage(REMOTE_PROG_TEXT_ERROR);
					retVal = false;
				}
			}
		}
		return retVal;
	}

	protected void createRemoteConnectionGroup(Composite parent) {
		peerSelector = new TCFPeerSelector(parent, SWT.NONE, 2);
		peerSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		peerSelector.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
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
				Messages.RemoteCMainTab_Remote_Path_Browse_Button, null);
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

	/*
	 * createDownloadOption This creates the skip download check button.
	 */
	protected void createDownloadOption(Composite parent) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComp.setLayoutData(gd);

		skipDownloadButton = createCheckButton(mainComp,
				SKIP_DOWNLOAD_BUTTON_TEXT);
		skipDownloadButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		skipDownloadButton.setEnabled(true);
	}

	protected void handleRemoteBrowseSelected() {
		IPeerModel connection = peerSelector.getPeer();
		if (connection != null) {
			FSOpenFileDialog dialog = new FSOpenFileDialog(getShell());
			dialog.setInput(connection);
			if (dialog.open() == Window.OK) {
				Object candidate = dialog.getFirstResult();
				if (candidate instanceof FSTreeNode) {
					String absPath = ((FSTreeNode) candidate).getLocation();
					if (absPath != null) {
						remoteProgText.setText(absPath);
					}
				}
			}
		}
	}

	protected void updateTargetProgFromConfig(ILaunchConfiguration config) {
		String targetPath = null;
		try {
			targetPath = config.getAttribute(
					IRemoteTEConfigurationConstants.ATTR_REMOTE_PATH,
					REMOTE_PATH_DEFAULT);
		} catch (CoreException e) {
			// Ignore
		}
		remoteProgText.setText(targetPath);

		String prelaunchCmd = null;
		try {
			prelaunchCmd = config.getAttribute(
					IRemoteTEConfigurationConstants.ATTR_PRERUN_COMMANDS, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			// Ignore
		}
		preRunText.setText(prelaunchCmd);
	}

	protected void updateSkipDownloadFromConfig(ILaunchConfiguration config) {
		boolean downloadToTarget = true;
		try {
			downloadToTarget = config
					.getAttribute(
							IRemoteTEConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
							getDefaultSkipDownload());
		} catch (CoreException e) {
			// Ignore for now
		}
		skipDownloadButton.setSelection(downloadToTarget);
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
		// FIXME
		return ""; //$NON-NLS-1$
	}

	private boolean getDefaultSkipDownload() {
		// FIXME
		return SKIP_DOWNLOAD_TO_REMOTE_DEFAULT;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		String remoteConnection = null;
		try {
			remoteConnection = config.getAttribute(
					IRemoteTEConfigurationConstants.ATTR_REMOTE_CONNECTION, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			// Ignore
		}

		peerSelector.updateSelectionFrom(remoteConnection);
		super.initializeFrom(config);

		updateTargetProgFromConfig(config);
		updateSkipDownloadFromConfig(config);
	}

	/*
	 * performApply
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {

		String currentSelection = peerSelector.getPeerId();
		config.setAttribute(
				IRemoteTEConfigurationConstants.ATTR_REMOTE_CONNECTION,
				currentSelection != null ? currentSelection : null);
		config.setAttribute(IRemoteTEConfigurationConstants.ATTR_REMOTE_PATH,
				remoteProgText.getText());
		config.setAttribute(
				IRemoteTEConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
				skipDownloadButton.getSelection());
		config.setAttribute(
				IRemoteTEConfigurationConstants.ATTR_PRERUN_COMMANDS,
				preRunText.getText());
		super.performApply(config);
	}

	@Override
	public String getId() {
		return "org.eclipse.tcf.te.remotecdt.dsf.gdb.mainTab"; //$NON-NLS-1$
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		config.setAttribute(
				IRemoteTEConfigurationConstants.ATTR_REMOTE_CONNECTION,
				EMPTY_STRING);
		config.setAttribute(IRemoteTEConfigurationConstants.ATTR_REMOTE_PATH,
				REMOTE_PATH_DEFAULT);
		config.setAttribute(
				IRemoteTEConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
				SKIP_DOWNLOAD_TO_REMOTE_DEFAULT);
		config.setAttribute(
				IRemoteTEConfigurationConstants.ATTR_PRERUN_COMMANDS,
				EMPTY_STRING);
	}

}