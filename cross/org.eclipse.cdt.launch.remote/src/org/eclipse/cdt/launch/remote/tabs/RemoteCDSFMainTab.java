/*******************************************************************************
 * Copyright (c) 2006, 2016 PalmSource, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.cdt.dsf.gdb.internal.ui.launching.CMainTab;
import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.IRemoteConnectionHostConstants;
import org.eclipse.cdt.launch.remote.RemoteHelper;
import org.eclipse.cdt.launch.remote.RemoteUIHelper;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.dialogs.RemoteResourceBrowser;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class RemoteCDSFMainTab extends CMainTab {

	/* Labels and Error Messages */
	private static final String REMOTE_PROG_LABEL_TEXT = Messages.RemoteCMainTab_Program;
	private static final String SKIP_DOWNLOAD_BUTTON_TEXT = Messages.RemoteCMainTab_SkipDownload;
	private static final String REMOTE_PROG_TEXT_ERROR = Messages.RemoteCMainTab_ErrorNoProgram;
	private static final String REMOTE_PROG_NOT_ABSOLUTE = Messages.RemoteCMainTab_ErrorRemoteProgNotAbsolute;
	private static final String CONNECTION_TEXT_ERROR = Messages.RemoteCMainTab_ErrorNoConnection;
	private static final String PRE_RUN_LABEL_TEXT = Messages.RemoteCMainTab_Prerun;

	/* Defaults */
	private static final String REMOTE_PATH_DEFAULT = EMPTY_STRING;
	private static final boolean SKIP_DOWNLOAD_TO_REMOTE_DEFAULT = false;

	protected Button newRemoteConnectionButton;
	protected Button editRemoteConnectionButton;
	protected Button remoteConnectionPropertiesButton;
	protected Button remoteBrowseButton;
	protected Label connectionLabel;
	protected Combo connectionCombo;
	protected Label remoteProgLabel;
	protected Text remoteProgText;
	protected Button skipDownloadButton;
	protected Button useLocalPathButton;

	private Text preRunText;
	private Label preRunLabel;

	private boolean isInitializing = false;

	public RemoteCDSFMainTab() {
		super(CMainTab.INCLUDE_BUILD_SETTINGS);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createControl(parent);
		Composite comp = (Composite) getControl();
		/* The Connection dropdown with New button. */
		createVerticalSpacer(comp, 1);
		createRemoteConnectionGroup(comp, 4);
		/* The remote binary location and skip download option */
		createVerticalSpacer(comp, 1);
		createTargetExePathGroup(comp);
		createDownloadOption(comp);

		/* If the local binary path changes, modify the remote binary location */
		fProgText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent evt) {
				setLocalPathForRemotePath();
			}
		});

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), Activator.PLUGIN_ID + ".launchgroup"); //$NON-NLS-1$

	}

	/*
	 * isValid
	 *
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		if (!super.isValid(config)) {
			return false;
		}

		/* Clear any pre-existing message. */
		setErrorMessage(null);

		/* Verify that a remote connection is selected. */
		int currentSelection = connectionCombo.getSelectionIndex();
		if (currentSelection < 0) {
			setErrorMessage(CONNECTION_TEXT_ERROR);
			return false;
		}

		String connection_name = connectionCombo.getItem(currentSelection);
		if (connection_name.isEmpty()) {
			setErrorMessage(CONNECTION_TEXT_ERROR);
			return false;
		}

		/* Verify that the remote executable file name is specified. */
		String remoteProgName = remoteProgText.getText().trim();
		if (remoteProgName.isEmpty()) {
			setErrorMessage(REMOTE_PROG_TEXT_ERROR);
			return false;
		}

		/* Verify that the remote executable file name is absolute. */
		Path remoteProgPath = Path.forPosix(remoteProgName);
		if (!remoteProgPath.isAbsolute()) {
			setErrorMessage(REMOTE_PROG_NOT_ABSOLUTE);
			return false;
		}

		return true;
	}

	protected void createRemoteConnectionGroup(Composite parent, int colSpan) {
		Composite projComp = new Composite(parent, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 5;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		projComp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		projComp.setLayoutData(gd);

		connectionLabel = new Label(projComp, SWT.NONE);
		connectionLabel.setText(Messages.RemoteCMainTab_Connection);
		gd = new GridData();
		gd.horizontalSpan = 1;
		connectionLabel.setLayoutData(gd);

		connectionCombo = new Combo(projComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		connectionCombo.setLayoutData(gd);
		connectionCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				useDefaultsFromConnection();
				updateConnectionButtons();
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});

		newRemoteConnectionButton = createPushButton(projComp, Messages.RemoteCMainTab_New, null);
		newRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleNewRemoteConnectionSelected();
				updateLaunchConfigurationDialog();
				updateConnectionPulldown();
			}
		});

		editRemoteConnectionButton = createPushButton(projComp, Messages.RemoteCMainTab_Edit, null);
		editRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleEditRemoteConnectionSelected();
			}
		});

		remoteConnectionPropertiesButton = createPushButton(projComp, Messages.RemoteCMainTab_Properties, null);
		remoteConnectionPropertiesButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleRemoteConnectionPropertiesSelected();
			}
		});

		updateConnectionPulldown();
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

			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		remoteBrowseButton = createPushButton(mainComp, Messages.RemoteCMainTab_Remote_Path_Browse_Button, null);
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

			@Override
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

		skipDownloadButton = createCheckButton(mainComp, SKIP_DOWNLOAD_BUTTON_TEXT);
		skipDownloadButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		skipDownloadButton.setEnabled(true);
	}

	protected void handleNewRemoteConnectionSelected() {
		RemoteUIHelper.newConnectionDialog(getControl().getShell());
	}

	/**
	 * Opens the <code>SystemConnectionPropertyPage</code> page for the selected connection.
	 */
	protected void handleEditRemoteConnectionSelected() {
		RemoteUIHelper.editConnectionDialog(getCurrentConnection(), getControl().getShell());
	}

	protected IRemoteConnection getCurrentConnection() {
		int currentSelection = connectionCombo.getSelectionIndex();
		String remoteConnection = currentSelection >= 0 ? connectionCombo.getItem(currentSelection) : null;
		return RemoteHelper.getRemoteConnectionByName(remoteConnection);
	}

	protected void handleRemoteBrowseSelected() {
		IRemoteConnection currentConnectionSelected = getCurrentConnection();

		// Try to open the connection before showing RemoteResourceBrowser
		if (currentConnectionSelected != null && !currentConnectionSelected.isOpen()) {
			if (currentConnectionSelected.getConnectionType().hasService(IRemoteUIConnectionService.class)) {

				IRemoteUIConnectionService uiConnService = currentConnectionSelected.getConnectionType()
						.getService(IRemoteUIConnectionService.class);
				uiConnService.openConnectionWithProgress(getShell(), new ProgressMonitorDialog(getShell()),
						currentConnectionSelected);

				// Don't show RemoteResourceBrowser if the connection cannot be opened
				if (!currentConnectionSelected.isOpen())
					return;
			}
		}

		RemoteResourceBrowser b = new RemoteResourceBrowser(getControl().getShell(), SWT.NONE);
		b.setConnection(currentConnectionSelected);
		b.setTitle(Messages.RemoteCMainTab_Remote_Path_Browse_Button_Title);
		int returnCode = b.open();

		// User cancelled the browse dialog?
		if (returnCode == Window.CANCEL) {
			return;
		}

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

	protected void handleRemoteConnectionPropertiesSelected() {
		class RemoteConnectionPropertyDialog extends Dialog {
			private IRemoteConnection fHost;
			boolean fbLocalHost;
			private Button fSkipDownloadBtn;
			private Text fWSRoot;
			private String fDialogTitle;

			public RemoteConnectionPropertyDialog(Shell parentShell, String dialogTitle, IRemoteConnection host) {
				super(parentShell);
				fDialogTitle = dialogTitle;
				fHost = host;
				IRemoteServicesManager remoteServicesManager = Activator.getService(IRemoteServicesManager.class);
				fbLocalHost = (fHost.getConnectionType() == remoteServicesManager.getLocalConnectionType());
			}

			@Override
			protected void configureShell(Shell shell) {
				super.configureShell(shell);
				shell.setText(fDialogTitle);
			}

			@Override
			protected Control createDialogArea(Composite parent) {
				// create composite
				Composite composite = (Composite) super.createDialogArea(parent);

				Label label = new Label(composite, SWT.WRAP);
				label.setText(Messages.RemoteCMainTab_Properties_Location);
				GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
				data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
				label.setLayoutData(data);
				label.setFont(parent.getFont());
				fWSRoot = new Text(composite, SWT.SINGLE | SWT.BORDER);
				fWSRoot.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

				fSkipDownloadBtn = new Button(composite, SWT.CHECK);
				fSkipDownloadBtn.setText(Messages.RemoteCMainTab_Properties_Skip_default);
				if (!fbLocalHost) {
					String value = RemoteUIHelper.getConnectionProperty(fHost,
							IRemoteConnectionHostConstants.REMOTE_WS_ROOT);
					if (!value.isEmpty()) {
						fWSRoot.setText(value);
					}
					fSkipDownloadBtn.setSelection(Boolean.valueOf(RemoteUIHelper.getConnectionProperty(fHost,
							IRemoteConnectionHostConstants.DEFAULT_SKIP_DOWNLOAD)).booleanValue());
				} else {
					fSkipDownloadBtn.setEnabled(false);
					fWSRoot.setEnabled(false);
				}
				applyDialogFont(composite);
				return composite;
			}

			@Override
			protected void buttonPressed(int buttonId) {
				if (!fbLocalHost && (buttonId == IDialogConstants.OK_ID)) {
					RemoteUIHelper.setConnectionProperty(fHost, IRemoteConnectionHostConstants.REMOTE_WS_ROOT,
							fWSRoot.getText());
					RemoteUIHelper.setConnectionProperty(fHost, IRemoteConnectionHostConstants.DEFAULT_SKIP_DOWNLOAD,
							Boolean.toString(fSkipDownloadBtn.getSelection()));
				}
				super.buttonPressed(buttonId);
			}
		}
		IRemoteConnection currentConnectionSelected = getCurrentConnection();
		RemoteConnectionPropertyDialog dlg = new RemoteConnectionPropertyDialog(getControl().getShell(),
				Messages.RemoteCMainTab_Properties_title, currentConnectionSelected);
		dlg.setBlockOnOpen(true);
		dlg.open();
	}

	protected void updateConnectionPulldown() {
		connectionCombo.removeAll();
		IRemoteConnection[] connections = RemoteHelper.getSuitableConnections();
		for (int i = 0; i < connections.length; i++) {
			connectionCombo.add(connections[i].getName());
		}

		if (connections.length > 0) {
			connectionCombo.select(connections.length - 1);
		}
		updateConnectionButtons();
	}

	private void updateConnectionButtons() {
		if ((remoteConnectionPropertiesButton == null) || remoteConnectionPropertiesButton.isDisposed()) {
			return;
		}
		if ((editRemoteConnectionButton == null) || editRemoteConnectionButton.isDisposed()) {
			return;
		}
		boolean bEnable = false;
		IRemoteConnection currentConnectionSelected = getCurrentConnection();
		if (currentConnectionSelected != null && currentConnectionSelected.getConnectionType().canEdit()) {
			bEnable = true;
		}
		remoteConnectionPropertiesButton.setEnabled(bEnable);
		editRemoteConnectionButton.setEnabled(bEnable);
	}

	protected void updateTargetProgFromConfig(ILaunchConfiguration config) {
		String targetPath = null;
		try {
			targetPath = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
					REMOTE_PATH_DEFAULT);
		} catch (CoreException e) {
			// Ignore
		}
		remoteProgText.setText(targetPath);

		String prelaunchCmd = null;
		try {
			prelaunchCmd = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			// Ignore
		}
		preRunText.setText(prelaunchCmd);
	}

	protected void updateSkipDownloadFromConfig(ILaunchConfiguration config) {
		boolean downloadToTarget = true;
		try {
			downloadToTarget = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
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
				exePath = makeRelativeToWSRootLocation(exePath, remoteWsRoot, wsRoot);
			}
			String path = exePath.toString();
			remoteProgText.setText(path);
		}
	}

	private void useDefaultsFromConnection() {
		// During initialization, we don't want to use the default
		// values of the connection, but we want to use the ones
		// that are part of the configuration
		if (isInitializing)
			return;

		if ((remoteProgText != null) && !remoteProgText.isDisposed()) {
			String remoteName = remoteProgText.getText().trim();
			String remoteWsRoot = getRemoteWSRoot();
			if (remoteName.length() == 0) {
				remoteProgText.setText(remoteWsRoot);
			} else {
				// try to use remote path
				IPath wsRoot = Platform.getLocation();
				IPath remotePath = makeRelativeToWSRootLocation(new Path(remoteName), remoteWsRoot, wsRoot);
				remoteProgText.setText(remotePath.toString());
			}
		}
		if ((skipDownloadButton != null) && !skipDownloadButton.isDisposed()) {
			skipDownloadButton.setSelection(getDefaultSkipDownload());
			if (RemoteHelper.getFileSubsystem(getCurrentConnection()) == null) {
				skipDownloadButton.setEnabled(false);
			} else {
				skipDownloadButton.setEnabled(true);
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

	private IPath makeRelativeToWSRootLocation(IPath exePath, String remoteWsRoot, IPath wsRoot) {
		if (remoteWsRoot.length() != 0) {
			// use remoteWSRoot instead of Workspace Root
			if (wsRoot.isPrefixOf(exePath)) {
				return new Path(remoteWsRoot)
						.append(exePath.removeFirstSegments(wsRoot.segmentCount()).setDevice(null));
			}
		}
		return exePath;
	}

	private String getRemoteWSRoot() {
		IRemoteConnection host = getCurrentConnection();
		if (host != null) {
			String value = RemoteUIHelper.getConnectionProperty(host, IRemoteConnectionHostConstants.REMOTE_WS_ROOT);
			if (!value.isEmpty()) {
				return value;
			}
		}
		return ""; //$NON-NLS-1$
	}

	private boolean getDefaultSkipDownload() {
		IRemoteConnection host = getCurrentConnection();
		if (host != null) {
			if (RemoteHelper.getFileSubsystem(host) == null) {
				return true;
			}
			String value = RemoteUIHelper.getConnectionProperty(host,
					IRemoteConnectionHostConstants.DEFAULT_SKIP_DOWNLOAD);
			if (!value.isEmpty()) {
				return Boolean.valueOf(value).booleanValue();
			}
		}
		return SKIP_DOWNLOAD_TO_REMOTE_DEFAULT;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		isInitializing = true;
		String remoteConnection = null;
		try {
			remoteConnection = config.getAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION, ""); //$NON-NLS-1$
		} catch (CoreException ce) {
			// Ignore
		}

		String[] items = connectionCombo.getItems();
		int i = 0;
		for (i = 0; i < items.length; i++)
			if (items[i].equals(remoteConnection))
				break;
		/*
		 * Select the last used connection in the connecion pulldown if it still
		 * exists.
		 */
		if (i < items.length)
			connectionCombo.select(i);
		else if (items.length > 0)
			connectionCombo.select(0);

		super.initializeFrom(config);

		updateTargetProgFromConfig(config);
		updateSkipDownloadFromConfig(config);
		updateConnectionButtons();
		isInitializing = false;
	}

	/*
	 * performApply
	 *
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {

		int currentSelection = connectionCombo.getSelectionIndex();
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
				currentSelection >= 0 ? connectionCombo.getItem(currentSelection) : null);
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH, remoteProgText.getText());
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
				skipDownloadButton.getSelection());
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS, preRunText.getText());
		super.performApply(config);
	}

	@Override
	public String getId() {
		return "org.eclipse.cdt.launch.remote.dsf.mainTab"; //$NON-NLS-1$
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION, EMPTY_STRING);
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH, REMOTE_PATH_DEFAULT);
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_SKIP_DOWNLOAD_TO_TARGET,
				SKIP_DOWNLOAD_TO_REMOTE_DEFAULT);
		config.setAttribute(IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS, EMPTY_STRING);
	}

}
