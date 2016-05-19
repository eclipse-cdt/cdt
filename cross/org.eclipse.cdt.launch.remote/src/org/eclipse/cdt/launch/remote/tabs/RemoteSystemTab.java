/********************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 ********************************************************************************/
package org.eclipse.cdt.launch.remote.tabs;

import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.cdt.internal.launch.remote.Activator;
import org.eclipse.cdt.internal.launch.remote.Messages;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.IRemoteConnectionHostConstants;
import org.eclipse.cdt.launch.remote.RemoteHelper;
import org.eclipse.cdt.launch.remote.RemoteUIHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionPropertyService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

/**
 * This tab allows select and manage connection to a remote system.
 */
public class RemoteSystemTab extends AbstractLaunchConfigurationTab {

	public static final String TAB_NAME = Messages.RemoteSystemTab_Tab_Name;
	protected Label fConnectionLabel;
	protected Combo fConnectionCombo;
	protected Button fNewRemoteConnectionButton;
	protected Button fEditRemoteConnectionButton;
	protected Button fRemoteConnectionPropertiesButton;
	protected Label fConnectionPropType;
	protected Label fConnectionPropHost;
	protected Label fConnectionPropUsername;
	protected Button fShowSystemInfoCheck;
	protected StyledText fSystemInfoText;
	/**
	 * Do not show system information by default.
	 * It requires to open connection to remote system,
	 *  it can make UI unresponsive.
	 */
	protected final boolean SHOW_SYSTEM_INFORMATION_DEFAULT = false;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	/*
	 * createControl.
	 *
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		createVerticalSpacer(comp, 1);
		createConnectionGroup(comp, 4);
		createVerticalSpacer(comp, 1);
		createShowSystemInfoGroup(comp);
		setControl(comp);
	}
	/*
	 * getName.
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return TAB_NAME;
	}
	/*
	 * getId.
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return "org.eclipse.cdt.launch.remote.RemoteSystemTab"; //$NON-NLS-1$
	}
	/*
	 * initializeFrom.
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		/*
		 * Load current connection.
		 */
		String remoteConnection = null;
		try {
			remoteConnection = config.getAttribute(
					IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION, EMPTY_STRING);
		} catch (CoreException ce) {
			logError(ce);
			remoteConnection = EMPTY_STRING;
		}
		/*
		 * Check if connection still exists.
		 * If so, set connection combo properly.
		 * Otherwise, select the first on the list.
		 */
		String[] items = fConnectionCombo.getItems();
		int i = 0;
		for (i = 0; i < items.length; i++) {
			if (items[i].equals(remoteConnection)) {
				break;
			}
		}

		if (i < items.length) {
			fConnectionCombo.select(i);
		} else {
			setErrorMessage(MessageFormat.format(
					Messages.RemoteSystemTab_Error_ConnectionNotFound, remoteConnection));
			setDefaultConnection();
			setDirty(true);
		}

		updateConnectionButtons();

		if(fShowSystemInfoCheck != null && ! fShowSystemInfoCheck.isDisposed()
				&& fShowSystemInfoCheck.getSelection() == false) {
			fSystemInfoText.setText(Messages.RemoteSystemTab_ShowSystemInfo_NoInformation);
		}
	}
	/*
	 * performApply.
	 *
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		/*
		 * Save the connection selected.
		 */
		int currentSelection = fConnectionCombo.getSelectionIndex();
		config.setAttribute(
				IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION,
				currentSelection >= 0 ? fConnectionCombo
						.getItem(currentSelection) : null);
	}
	/*
	 * setDefaults.
	 *
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		if(fShowSystemInfoCheck != null &&
				! fShowSystemInfoCheck.isDisposed()) {
			fShowSystemInfoCheck.setSelection(SHOW_SYSTEM_INFORMATION_DEFAULT);
		}
		setDefaultConnection();
	}
	/*
	 * isValid.
	 *
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		boolean retVal = super.isValid(config);
		if (retVal) {
			/*
			 *  Check connection list is not empty
			 *   and the selected one exists.
			 */
			setErrorMessage(null);
			int currentSelection = fConnectionCombo.getSelectionIndex();
			String connName = currentSelection >= 0 ?
					fConnectionCombo.getItem(currentSelection) : EMPTY_STRING;
			if (connName.isEmpty()) {
				setErrorMessage(Messages.RemoteSystemTab_Error_NoConnection);
				retVal = false;
			}
		}
		return retVal;
	}
	/**
	 * Create the connection group.
	 *
	 * @param parent the parent composite.
	 * @param colSpan number of columns span.
	 */
	protected void createConnectionGroup(Composite parent, int colSpan) {
		Group grp = new Group(parent, SWT.NONE);
		grp.setText(Messages.RemoteSystemTab_ConnectionGrp_Group_Name);
		grp.setLayout(new GridLayout());

		Composite comp = new Composite(grp, SWT.NONE);
		GridLayout projLayout = new GridLayout();
		projLayout.numColumns = 5;
		projLayout.marginHeight = 0;
		projLayout.marginWidth = 0;
		comp.setLayout(projLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		comp.setLayoutData(gd);

		fConnectionLabel = new Label(comp, SWT.NONE);
		fConnectionLabel.setText(Messages.RemoteSystemTab_ConnectionGrp_ConnectionName_Label);
		gd = new GridData();
		gd.horizontalSpan = 1;
		fConnectionLabel.setLayoutData(gd);

		fConnectionCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		fConnectionCombo.setLayoutData(gd);

		fConnectionCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				/*
				 * Listener is also triggered when removing all elements.
				 * In this case, do nothing.
				 */
				if (fConnectionCombo.getText().isEmpty()) {
					return;
				}
				updateConnectionButtons();
				updateConnectionProperties();
				// Update target information if it is enabled.
				if (fShowSystemInfoCheck != null &&
						fShowSystemInfoCheck.getSelection()) {
					showSystemInfo(getCurrentConnection());
				}
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});

		/*
		 * Create the new connection button and attach selection handlers.
		 */
		fNewRemoteConnectionButton = createPushButton(comp,
				Messages.RemoteSystemTab_ConnectionGrp_New_Button, null);
		fNewRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				// Open new connection dialog
				RemoteUIHelper.newConnectionDialog(getControl().getShell());
				updateConnectionPulldown();
				updateLaunchConfigurationDialog();
			}
		});
		/*
		 * Create the edit connection button and attach selection handlers.
		 */
		fEditRemoteConnectionButton = createPushButton(comp,
				Messages.RemoteSystemTab_ConnectionGrp_Edit_Button, null);
		fEditRemoteConnectionButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				RemoteUIHelper.editConnectionDialog(getCurrentConnection(),
						getControl().getShell());
			}
		});
		/*
		 * Create the connection properties button and attach selection handlers.
		 */
		fRemoteConnectionPropertiesButton = createPushButton(comp,
				Messages.RemoteSystemTab_ConnectionGrp_Properties_Button, null);
		fRemoteConnectionPropertiesButton
				.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent evt) {
						handleRemoteConnectionPropertiesSelected();
					}
				});
		/*
		 * Create properties about the connection selected.
		 */
		// Type label
		Label lType = new Label(comp, SWT.NONE);
		lType.setText(Messages.RemoteSystemTab_ConnectionGrp_Type_Property);
		fConnectionPropType	 = new Label(comp, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 4;
		fConnectionPropType.setLayoutData(gd);
		// Host label
		Label lHost = new Label(comp, SWT.NONE);
		lHost.setText(Messages.RemoteSystemTab_ConnectionGrp_Host_Property);
		fConnectionPropHost	 = new Label(comp, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 4;
		fConnectionPropHost.setLayoutData(gd);
		// Username label
		Label lUsername = new Label(comp, SWT.None);
		lUsername.setText(Messages.RemoteSystemTab_ConnectionGrp_User_Property);
		fConnectionPropUsername	 = new Label(comp, SWT.None);
		gd = new GridData();
		gd.horizontalSpan = 4;
		fConnectionPropUsername.setLayoutData(gd);

		updateConnectionPulldown();
	}
	/**
	 * Create group to show information about remote system.
	 *
	 * @param parent the parent composite.
	 */
	protected void createShowSystemInfoGroup(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fShowSystemInfoCheck = new Button(comp, SWT.CHECK);
		createVerticalSpacer(comp, 1);
		fShowSystemInfoCheck.setText(Messages.RemoteSystemTab_ShowSystemInfo_Check);
		fShowSystemInfoCheck.setSelection(SHOW_SYSTEM_INFORMATION_DEFAULT);
		fShowSystemInfoCheck.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				/*
				 * Enable/disable show system information.
				 */
				if (fShowSystemInfoCheck.getSelection()) {
					fSystemInfoText.setEnabled(true);
					showSystemInfo(getCurrentConnection());
				} else {
					fSystemInfoText.setText(Messages.RemoteSystemTab_ShowSystemInfo_NoInformation);
					fSystemInfoText.setEnabled(false);
					fSystemInfoText.getParent().pack();
				}
			}
		});

		/*
		 *  Initialize the area to display system information.
		 */
		fSystemInfoText = new StyledText(comp, SWT.MULTI | SWT.READ_ONLY);
		fSystemInfoText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fSystemInfoText.setIndent(10);
	}
	/**
	 * Retrieve and show information about the remote system.
	 *
	 * @param connection A connection to remote system.
	 */
	protected void showSystemInfo(IRemoteConnection connection) {
		/**
		 * Retrieve and update system information.
		 * Operating might take long time, use job to post status
		 * 	and allow canceling.
		 */
		class UpdateSystemInfoJob extends UIJob {
			private IRemoteConnection conn;
			public UpdateSystemInfoJob(String name, IRemoteConnection conn) {
				super(name);
				this.conn = conn;
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!conn.isOpen()) {
					try {
						conn.open(monitor);
					} catch (RemoteConnectionException e) {
						String msg = MessageFormat.format(Messages.RemoteSystemTab_ShowSystemInfo_UpdateJob_Error,
								conn.getName());
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								msg, e);
					} finally {
						/*
						 * Notify user it is unable to show information.
						 */
						unableToShowSystemInfo();
					}
				}
				IRemoteConnectionPropertyService connProperties = conn.getService(IRemoteConnectionPropertyService.class);
				if (connProperties != null) {
					StringBuffer sb = new StringBuffer();
					String lineDelimiter = fSystemInfoText.getLineDelimiter();
					sb.append(Messages.RemoteSystemTab_ShowSystemInfo_OS_Name_Prop +
							connProperties.getProperty(IRemoteConnection.OS_NAME_PROPERTY));
					sb.append(lineDelimiter);
					sb.append(Messages.RemoteSystemTab_ShowSystemInfo_OS_Version_Prop +
							connProperties.getProperty(IRemoteConnection.OS_VERSION_PROPERTY));
					sb.append(lineDelimiter);
					sb.append(Messages.RemoteSystemTab_ShowSystemInfo_OS_Arch_Prop +
							connProperties.getProperty(IRemoteConnection.OS_ARCH_PROPERTY));
					fSystemInfoText.setText(sb.toString());
					fSystemInfoText.getParent().pack(); // refresh the text area
				}
				return Status.OK_STATUS;
			}
		}

		Job job = new UpdateSystemInfoJob(Messages.RemoteSystemTab_ShowSystemInfo_UpdateJob_Title,
				connection);
		job.schedule();
	}
	/**
	 * Handle cases when connection cannot be opened or
	 *  the system property not be read.
	 */
	protected void unableToShowSystemInfo() {
		if (fSystemInfoText != null &&
				! fSystemInfoText.isDisposed()) {
			FontDescriptor fd = FontDescriptor.createFrom(fSystemInfoText.getFont());
			fd.setStyle(SWT.BOLD | SWT.ITALIC);
			Font font = fd.createFont(fShowSystemInfoCheck.getDisplay());
			fSystemInfoText.setFont(font);
			fSystemInfoText.setText(Messages.RemoteSystemTab_ShowSystemInfo_Error_UnableToGetInfo);
			fSystemInfoText.getParent().pack();
		}
	}
	/**
	 * Reset the list of connections.
	 */
	protected void updateConnectionPulldown() {
		fConnectionCombo.removeAll();
		IRemoteConnection[] connections = RemoteHelper.getSuitableConnections();
		for (int i = 0; i < connections.length; i++) {
			fConnectionCombo.add(connections[i].getName());
		}

		if (connections.length > 0) {
			fConnectionCombo.select(connections.length - 1);
		}
		updateConnectionButtons();
	}
	/*
	 * Not all type of connection can be edited or have properties managed.
	 * Enable/disable the connection buttons properly.
	 */
	private void updateConnectionButtons() {
		if ((fRemoteConnectionPropertiesButton == null)
				|| fRemoteConnectionPropertiesButton.isDisposed()) {
			return;
		}
		if ((fEditRemoteConnectionButton == null)
				|| fEditRemoteConnectionButton.isDisposed()) {
			return;
		}
		boolean bEnable = false;
		IRemoteConnection currentConnectionSelected = getCurrentConnection();
		if (currentConnectionSelected != null &&
				currentConnectionSelected.getConnectionType().canEdit()) {
			bEnable = true;
		}
		fRemoteConnectionPropertiesButton.setEnabled(bEnable);
		fEditRemoteConnectionButton.setEnabled(bEnable);
	}
	/*
	 * Show information associated with connection selected.
	 */
	private void updateConnectionProperties() {
		IRemoteConnection conn = getCurrentConnection();
		// Display the connection type.
		if (fConnectionPropType != null) {
			fConnectionPropType.setText(conn.getConnectionType().getName());
		}

		String host = EMPTY_STRING;
		String username = EMPTY_STRING;
		if (conn.hasService(IRemoteConnectionHostService.class)) {
			IRemoteConnectionHostService hostServices = conn.getService(IRemoteConnectionHostService.class);
			host = hostServices.getHostname();
			username = hostServices.getUsername();
		}
		// Display the host name.
		if (fConnectionPropHost != null) {
			fConnectionPropHost.setText(host);
		}
		// Display the username.
		if (fConnectionPropUsername != null) {
			fConnectionPropUsername.setText(username);
		}
	}
	/**
	 * Handle remote connection properties button selected.
	 */
	protected void handleRemoteConnectionPropertiesSelected() {
		class RemoteConnectionPropertyDialog extends Dialog {
			private IRemoteConnection fHost;
			boolean fbLocalHost;
			private Button fSkipDownloadBtn;
			private Text fWSRoot;
			private String fDialogTitle;

			public RemoteConnectionPropertyDialog(Shell parentShell,
					String dialogTitle, IRemoteConnection host) {
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
				Composite composite = (Composite) super
						.createDialogArea(parent);

				Label label = new Label(composite, SWT.WRAP);
				label.setText(Messages.RemoteSystemTab_PropertiesDialog_Location_Label);
				GridData data = new GridData(GridData.GRAB_HORIZONTAL
						| GridData.GRAB_VERTICAL
						| GridData.HORIZONTAL_ALIGN_FILL
						| GridData.VERTICAL_ALIGN_CENTER);
				data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
				label.setLayoutData(data);
				label.setFont(parent.getFont());
				fWSRoot = new Text(composite, SWT.SINGLE | SWT.BORDER);
				fWSRoot.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
						| GridData.HORIZONTAL_ALIGN_FILL));

				fSkipDownloadBtn = new Button(composite, SWT.CHECK);
				fSkipDownloadBtn
						.setText(Messages.RemoteSystemTab_PropertiesDialog_SkipDownload_Check);
				if (!fbLocalHost) {
					String value = RemoteUIHelper.getConnectionProperty(fHost,
							IRemoteConnectionHostConstants.REMOTE_WS_ROOT);
					if(!value.isEmpty()) {
						fWSRoot.setText(value);
					}
					fSkipDownloadBtn
					.setSelection(Boolean
							.valueOf(
									RemoteUIHelper.getConnectionProperty(fHost,
											IRemoteConnectionHostConstants.DEFAULT_SKIP_DOWNLOAD))
							.booleanValue());
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
					RemoteUIHelper.setConnectionProperty(fHost,
							IRemoteConnectionHostConstants.REMOTE_WS_ROOT,
							fWSRoot.getText());
					RemoteUIHelper.setConnectionProperty(fHost,
							IRemoteConnectionHostConstants.DEFAULT_SKIP_DOWNLOAD,
							Boolean.toString(fSkipDownloadBtn
									.getSelection()));
				}
				super.buttonPressed(buttonId);
			}
		}
		IRemoteConnection currentConnectionSelected = getCurrentConnection();
		RemoteConnectionPropertyDialog dlg = new RemoteConnectionPropertyDialog(
				getControl().getShell(),
				Messages.RemoteSystemTab_PropertiesDialog_Title,
				currentConnectionSelected);
		dlg.setBlockOnOpen(true);
		dlg.open();
	}
	/**
	 * Get current connection.
	 *
	 * @return Current connection selected.
	 */
	protected IRemoteConnection getCurrentConnection() {
		int currentSelection = fConnectionCombo.getSelectionIndex();
		String remoteConnection = currentSelection >= 0 ? fConnectionCombo
				.getItem(currentSelection) : null;
		return RemoteHelper.getRemoteConnectionByName(remoteConnection);
	}
	/**
	 * Get connection from configuration.
	 *
	 * @param config Launch configuration.
	 * @return Remote connection.
	 */
	public static IRemoteConnection getConnection(ILaunchConfiguration config) {
		if (config != null) {
			try {
				String connName = config.getAttribute(
						IRemoteConnectionConfigurationConstants.ATTR_REMOTE_CONNECTION, EMPTY_STRING);
				return RemoteHelper.getRemoteConnectionByName(connName);
			} catch (CoreException e) {
			}
		}
		return null;
	}
	/**
	 * Set default connection on combo box.
	 */
	protected void setDefaultConnection() {
		IRemoteServicesManager sm = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnection conn = sm.getLocalConnectionType().getConnections().get(0);
		String LocalConn = conn.getName();
		if (fConnectionCombo != null &&
				! fConnectionCombo.isDisposed()) {
			int size = fConnectionCombo.getItemCount();
			for(int i = 0; i < size; i++) {
				if (fConnectionCombo.getItem(i).contentEquals(LocalConn)) {
					fConnectionCombo.select(i);
				}
			}
		}
	}
	/*
	 * getImage.
	 *
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		URL url = FileLocator.find(Activator.getDefault().getBundle(),
				new Path("icons/full/obj16/systemlocallive_obj.gif"), null); //$NON-NLS-1$
		return ImageDescriptor.createFromURL(url).createImage();
	}
	/*
	 * Log exceptions.
	 */
	private void logError(Exception e) {
		Plugin plugin = Activator.getDefault();
		ILog logger = plugin.getLog();
		logger.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				e.getMessage(), e));
	}
}
