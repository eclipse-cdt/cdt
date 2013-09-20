/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.internal.remote.ui.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.RemoteUIServices;
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
 * If title is supplied then the widget will be placed in a group.
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

		// Composite textComp = new Composite(body, SWT.NONE);
		// textComp.setLayout(new GridLayout(2, false));
		// textComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fLabel = new Label(body, SWT.NONE);
		fLabel.setText(Messages.RemoteFileWidget_File);
		fLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		fText = new Text(body, SWT.BORDER);
		fText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String path = fText.getText();
				setSavedPath(path);
				notifyListeners(e);
			}
		});

		// Composite buttonComp = new Composite(body, SWT.NONE);
		// buttonComp.setLayout(new GridLayout(2, true));
		// GridData buttonCompData = new GridData(SWT.FILL, SWT.FILL, false, false);
		// buttonCompData.horizontalAlignment = SWT.END;
		// buttonComp.setLayoutData(buttonCompData);

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
		IRemoteUIConnectionManager connMgr = getUIConnectionManager();
		if (connMgr != null) {
			connMgr.openConnectionWithProgress(getShell(), null, fRemoteConnection);
			if (fRemoteConnection.isOpen()) {
				IRemoteUIFileManager fileMgr = getUIFileManager();
				if (fileMgr != null) {
					fileMgr.setConnection(fRemoteConnection);
					String path = fileMgr.browseFile(getShell(), fBrowseMessage, "", 0); //$NON-NLS-1$
					if (path != null) {
						setLocationPath(path);
					}
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
			return fPreviousSelections.get(fRemoteConnection.getRemoteServices().getId() + "." + fRemoteConnection.getName()); //$NON-NLS-1$
		}
		return null;
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fRemoteConnection != null) {
			return RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices()).getUIConnectionManager();
		}
		return null;
	}

	private IRemoteUIFileManager getUIFileManager() {
		if (fRemoteConnection != null) {
			return RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices()).getUIFileManager();
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
			path = fRemoteConnection.getWorkingDirectory().toString();
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
	 * @param conn
	 *            remote connection
	 * @since 4.0
	 */
	public void setConnection(IRemoteConnection conn) {
		if (conn == null) {
			throw new NullPointerException();
		}

		if (!conn.equals(fRemoteConnection)) {
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
			fPreviousSelections.put(fRemoteConnection.getRemoteServices().getId() + "." + fRemoteConnection.getName(), path); //$NON-NLS-1$
		}
	}

	private void updateBrowseButton() {
		fBrowseButton.setEnabled(getUIFileManager() != null);
	}
}
