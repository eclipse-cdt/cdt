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
 * Widget to allow the user to select a remote directory. Provides a "Browse"
 * button that uses the currently specified connection and a "Restore Default"
 * button to revert to the initial setting.
 *
 * If title is supplied then the widget will be placed in a group.
 *
 * The browse message can be modified using {@link #setBrowseMessage(String)}
 *
 */
public class RemoteDirectoryWidget extends Composite {
	private final Text text;
	private final Button browseButton;
	private final Button defaultButton;

	private final String fDefaultPath = null;
	private String fBrowseMessage = Messages.RemoteDirectoryWidget_0;
	private IRemoteConnection fRemoteConnection;
	private final ListenerList modifyListeners = new ListenerList();

	private final Map<String, String> previousSelections = new HashMap<String, String>();

	public RemoteDirectoryWidget(Composite parent, int style, String title, String defaultPath) {
		super(parent, style);

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite body = this;

		if (title != null) {
			Group group = new Group(this, SWT.NONE);
			group.setText(title);
			group.setLayout(new GridLayout(1, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			body = group;
		}

		Composite textComp = new Composite(body, SWT.NONE);
		textComp.setLayout(new GridLayout(2, false));
		textComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(textComp, SWT.NONE);
		label.setText(Messages.RemoteDirectoryWidget_1);

		text = new Text(textComp, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		text.setLayoutData(data);
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String path = text.getText();
				setSavedPath(path);
				notifyListeners(e);
			}
		});

		Composite buttonComp = new Composite(body, SWT.NONE);
		buttonComp.setLayout(new GridLayout(2, true));
		GridData buttonCompData = new GridData(SWT.FILL, SWT.FILL, false, false);
		buttonCompData.horizontalAlignment = SWT.END;
		buttonComp.setLayoutData(buttonCompData);

		browseButton = new Button(buttonComp, SWT.NONE);
		browseButton.setText(Messages.RemoteDirectoryWidget_2);
		GridData browseButtonData = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		browseButtonData.widthHint = 110;
		browseButton.setLayoutData(browseButtonData);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
		});

		defaultButton = new Button(buttonComp, SWT.NONE);
		defaultButton.setText(Messages.RemoteDirectoryWidget_3);
		GridData defaultButtonData = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		defaultButtonData.widthHint = 110;
		defaultButton.setLayoutData(defaultButtonData);
		defaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				restoreDefault(fDefaultPath);
			}
		});

		if (defaultPath != null) {
			text.setText(defaultPath);
		}
		updateBrowseButton();
	}

	/**
	 * Add a listener that will be notified when the directory path is modified.
	 *
	 * @param listener
	 *            listener to add
	 */
	public void addModifyListener(ModifyListener listener) {
		modifyListeners.add(listener);
	}

	/**
	 * Get the directory location path. This path will be relative to the remote
	 * machine.
	 *
	 * @return directory location path
	 */
	public String getLocationPath() {
		return text.getText();
	}

	/**
	 * Remove a listener that will be notified when the directory path is
	 * modified.
	 *
	 * @param listener
	 *            listener to remove
	 */
	public void removeModifyListener(ModifyListener listener) {
		modifyListeners.remove(listener);
	}

	/**
	 * Set the message that will be displayed in the remote directory browser
	 * dialog.
	 *
	 * @param message
	 *            message to be displayed
	 */
	public void setBrowseMessage(String message) {
		fBrowseMessage = message;
	}

	/**
	 * Set the remote connection to use for browsing for the remote directory.
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

	/**
	 * Set the initial remote location that will be displayed in the widget.
	 *
	 * @param path
	 */
	public void setLocationPath(String path) {
		if (path != null && !path.equals(getLocationPath())) {
			text.setText(path);
		}
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
				String path = fileMgr.browseDirectory(getShell(), fBrowseMessage, "", 0); //$NON-NLS-1$
				if (path != null) {
					setLocationPath(path);
				}
			}
		}
	}

	private String getSavedPath() {
		if (fRemoteConnection != null) {
			return previousSelections
					.get(fRemoteConnection.getConnectionType().getId() + "." + fRemoteConnection.getName()); //$NON-NLS-1$
		}
		return null;
	}

	private IRemoteUIFileService getUIFileManager() {
		if (fRemoteConnection != null) {
			return fRemoteConnection.getConnectionType().getService(IRemoteUIFileService.class);
		}
		return null;
	}

	private IRemoteUIConnectionService getUIConnectionManager() {
		if (fRemoteConnection != null) {
			return fRemoteConnection.getConnectionType().getService(IRemoteUIConnectionService.class);
		}
		return null;
	}

	private void notifyListeners(ModifyEvent e) {
		for (Object listener : modifyListeners.getListeners()) {
			((ModifyListener) listener).modifyText(e);
		}
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

	private void setSavedPath(String path) {
		if (fRemoteConnection != null) {
			previousSelections.put(fRemoteConnection.getConnectionType().getId() + "." + fRemoteConnection.getName(), //$NON-NLS-1$
					path);
		}
	}

	private void updateBrowseButton() {
		browseButton.setEnabled(getUIFileManager() != null);
	}
}
