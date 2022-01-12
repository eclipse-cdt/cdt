/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessService;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.remote.ui.IRemoteUIConnectionService;
import org.eclipse.remote.ui.IRemoteUIFileService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Widget to allow the user to select a remote file. Provides a "Browse"
 * button that uses the currently specified connection and a "Restore Default"
 * button to revert to the initial setting.
 *
 * If GROUP_FLAG is set, then the widget will be placed in a group.
 * If RESTORE_BUTTON_FLAG is set, then a "Restore Default" button will be added
 *
 * If defaultPath is not null, then the initial path will be set to its value.
 *
 * The browse message can be modified using {@link #setBrowseMessage(String)}
 *
 */
public class RemoteFileWidget extends Composite {
	public static int GROUP_FLAG = 0x01;
	public static int RESTORE_BUTTON_FLAG = 0x02;

	private final Label fLabel;
	private final Text fText;
	private final Button fBrowseButton;

	private Button fDefaultButton;
	private String fDefaultPath;
	private String fBrowseMessage = Messages.RemoteFileWidget_Select_File;
	private IRemoteConnection fRemoteConnection;

	private final ListenerList fModifyListeners = new ListenerList();
	private final Map<String, String> fPreviousSelections = new HashMap<String, String>();

	public RemoteFileWidget(Composite parent, int style, int flags, String title, String defaultPath) {
		super(parent, style);

		GridLayout layout = new GridLayout(4, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		Composite body = this;

		if ((flags & GROUP_FLAG) != 0) {
			Group group = new Group(this, SWT.NONE);
			group.setText(title);
			group.setLayout(new GridLayout(1, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			body = group;
		}

		fLabel = new Label(body, SWT.NONE);
		fLabel.setText(Messages.RemoteFileWidget_File);
		fLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		fText = new Text(body, SWT.BORDER);
		fText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String path = fText.getText();
				setSavedPath(path);
				notifyListeners(e);
			}
		});

		fBrowseButton = new Button(body, SWT.NONE);
		fBrowseButton.setText(Messages.RemoteFileWidget_Browse);
		GridData browseButtonData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		browseButtonData.widthHint = 110;
		fBrowseButton.setLayoutData(browseButtonData);
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
		});

		if ((flags & RESTORE_BUTTON_FLAG) != 0) {
			fDefaultButton = new Button(body, SWT.NONE);
			fDefaultButton.setText(Messages.RemoteFileWidget_Restore_Default);
			GridData defaultButtonData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			defaultButtonData.widthHint = 110;
			fDefaultButton.setLayoutData(defaultButtonData);
			fDefaultButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					restoreDefault(fDefaultPath);
				}
			});
		}
		if (defaultPath != null) {
			fDefaultPath = defaultPath;
			fText.setText(defaultPath);
		}
		updateBrowseButton();
	}

	/**
	 * Add a listener that will be notified when the file path is modified.
	 *
	 * @param listener
	 *            listener to add
	 */
	public void addModifyListener(ModifyListener listener) {
		fModifyListeners.add(listener);
	}

	private void browse() {
		IRemoteUIConnectionService connMgr = getUIConnectionManager();
		if (connMgr != null) {
			connMgr.openConnectionWithProgress(getShell(), null, fRemoteConnection);
		}
		if (fRemoteConnection.isOpen()) {
			IRemoteUIFileService fileMgr = getUIFileManager();
			if (fileMgr != null) {
				fileMgr.setConnection(fRemoteConnection);
				String path = fileMgr.browseFile(getShell(), fBrowseMessage, "", 0); //$NON-NLS-1$
				if (path != null) {
					setLocationPath(path);
				}
			}
		}
	}

	/**
	 * Get the file location path. This path will be relative to the remote
	 * machine.
	 *
	 * @return file location path
	 */
	public String getLocationPath() {
		return fText.getText();
	}

	private String getSavedPath() {
		if (fRemoteConnection != null) {
			return fPreviousSelections
					.get(fRemoteConnection.getConnectionType().getId() + "." + fRemoteConnection.getName()); //$NON-NLS-1$
		}
		return null;
	}

	private IRemoteUIConnectionService getUIConnectionManager() {
		if (fRemoteConnection != null) {
			return fRemoteConnection.getConnectionType().getService(IRemoteUIConnectionService.class);
		}
		return null;
	}

	private IRemoteUIFileService getUIFileManager() {
		if (fRemoteConnection != null) {
			return fRemoteConnection.getConnectionType().getService(IRemoteUIFileService.class);
		}
		return null;
	}

	private void notifyListeners(ModifyEvent e) {
		for (Object listener : fModifyListeners.getListeners()) {
			((ModifyListener) listener).modifyText(e);
		}
	}

	/**
	 * Remove a listener that will be notified when the file path is
	 * modified.
	 *
	 * @param listener
	 *            listener to remove
	 */
	public void removeModifyListener(ModifyListener listener) {
		fModifyListeners.remove(listener);
	}

	private void restoreDefault(String path) {
		if (path == null && fRemoteConnection != null) {
			IRemoteProcessService processService = fRemoteConnection.getService(IRemoteProcessService.class);
			if (processService != null) {
				path = processService.getWorkingDirectory().toString();
			}
		}
		if (path == null) {
			path = ""; //$NON-NLS-1$
		}
		setLocationPath(path); // modify event listener updates map
	}

	/**
	 * Set the message that will be displayed in the remote file browser
	 * dialog.
	 *
	 * @param message
	 *            message to be displayed
	 */
	public void setBrowseMessage(String message) {
		fBrowseMessage = message;
	}

	/**
	 * Set the remote connection to use for browsing for the remote file.
	 *
	 * The connection type must provide the IRemoteUIConnectionService and IRemoteUIFileService services and the connection must
	 * support the IRemoteFileService service. If any of these conditions are not met, this method will do nothing.
	 *
	 * @param conn
	 *            remote connection
	 * @since 4.0
	 */
	public void setConnection(IRemoteConnection conn) {
		if (conn == null) {
			throw new NullPointerException();
		}

		if (conn.hasService(IRemoteFileService.class)
				&& conn.getConnectionType().hasService(IRemoteUIConnectionService.class)
				&& conn.getConnectionType().hasService(IRemoteUIFileService.class) && !conn.equals(fRemoteConnection)) {
			fRemoteConnection = conn;
			String path = getSavedPath();
			restoreDefault(path);
			updateBrowseButton();
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (!fText.isDisposed()) {
			fText.setEnabled(enabled);
		}
		if (!fBrowseButton.isDisposed()) {
			fBrowseButton.setEnabled(enabled);
		}
	}

	/**
	 * Set the label to be displayed
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		if (fLabel != null && !fLabel.isDisposed()) {
			fLabel.setText(label);
		}
	}

	/**
	 * Set the initial remote location that will be displayed in the widget.
	 *
	 * @param path
	 */
	public void setLocationPath(String path) {
		if (path != null && !path.equals(getLocationPath())) {
			fText.setText(path);
		}
	}

	private void setSavedPath(String path) {
		if (fRemoteConnection != null) {
			fPreviousSelections.put(fRemoteConnection.getConnectionType().getId() + "." + fRemoteConnection.getName(), //$NON-NLS-1$
					path);
		}
	}

	private void updateBrowseButton() {
		fBrowseButton.setEnabled(getUIFileManager() != null);
	}
}
