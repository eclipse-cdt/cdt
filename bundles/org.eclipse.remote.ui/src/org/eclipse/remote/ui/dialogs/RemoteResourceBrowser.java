/*******************************************************************************
 * Copyright (c) 2008,2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.remote.ui.widgets.RemoteResourceBrowserWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Generic file/directory browser for remote resources.
 * 
 * @author greg
 * 
 */
public class RemoteResourceBrowser extends Dialog {
	public final static String EMPTY_STRING = ""; //$NON-NLS-1$
	public final static int FILE_BROWSER = 0x01;
	public final static int DIRECTORY_BROWSER = 0x02;
	public static final int SINGLE = 0x01;
	public static final int MULTI = 0x02;

	private final static int widthHint = 400;

	private Button okButton;
	private RemoteResourceBrowserWidget fWidget;

	private int browserType;
	private String dialogTitle;

	private boolean showConnections = false;
	private String fInitialPath;
	private final IRemoteConnection fConnection;
	private int optionFlags = SINGLE;

	public RemoteResourceBrowser(IRemoteServices services, IRemoteConnection conn, Shell parent, int flags) {
		super(parent);
		setShellStyle(SWT.RESIZE | getShellStyle());
		fConnection = conn;
		this.optionFlags = flags;
		if (conn == null) {
			showConnections = true;
		}
		setTitle(Messages.RemoteResourceBrowser_resourceTitle);
		setType(FILE_BROWSER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets
	 * .Composite, int, java.lang.String, boolean)
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID) {
			okButton = button;
		}
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(dialogTitle);
		if (!showConnections) {
			fWidget.setConnection(fConnection);
		}
		if (fInitialPath != null) {
			fWidget.setInitialPath(fInitialPath);
		}
		updateDialog();
		return contents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite main = (Composite) super.createDialogArea(parent);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, true);
		gd.widthHint = widthHint;
		main.setLayoutData(gd);
		main.setLayout(new GridLayout(1, true));

		int options = RemoteResourceBrowserWidget.SHOW_HIDDEN_CHECKBOX | RemoteResourceBrowserWidget.SHOW_NEW_FOLDER_BUTTON;
		int style = SWT.NONE;
		if (showConnections) {
			options |= RemoteResourceBrowserWidget.SHOW_CONNECTIONS;
		}
		if (browserType == DIRECTORY_BROWSER) {
			options |= RemoteResourceBrowserWidget.DIRECTORY_BROWSER;
		}
		if ((optionFlags & MULTI) == MULTI) {
			style = SWT.MULTI;
		}

		fWidget = new RemoteResourceBrowserWidget(main, style, options);
		fWidget.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateDialog();
			}
		});
		fWidget.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				getShell().setDefaultButton(null); // allow text widget to receive SWT.DefaultSelection event
			}

			@Override
			public void focusLost(FocusEvent e) {
				getShell().setDefaultButton(okButton);
			}
		});
		fWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		return main;
	}

	/**
	 * Get the connection that was selected
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection() {
		if (fWidget != null) {
			return fWidget.getConnection();
		}
		return null;
	}

	/**
	 * Get the path that was selected.
	 * 
	 * @return selected path
	 */
	public String getPath() {
		if (fWidget != null && fWidget.getPaths().size() > 0) {
			return fWidget.getPaths().get(0);
		}
		return null;
	}

	/**
	 * Get the paths that were selected.
	 * 
	 * @return selected paths
	 */
	public String[] getPaths() {
		if (fWidget != null) {
			return fWidget.getPaths().toArray(new String[0]);
		}
		return null;
	}

	/**
	 * Set the initial path to start browsing. This will be set in the browser
	 * text field, and in a future version should expand the browser to this
	 * location if it exists.
	 * 
	 * @param path
	 */
	public void setInitialPath(String path) {
		fInitialPath = path;
	}

	/**
	 * Set the dialogTitle of the dialog.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		dialogTitle = title;
		if (dialogTitle == null) {
			dialogTitle = ""; //$NON-NLS-1$
		}
		Shell shell = getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(dialogTitle);
		}
	}

	/**
	 * Set the type of browser. Can be either a file browser (allows selection
	 * of files) or a directory browser (allows selection of directories), or
	 * both.
	 */
	public void setType(int type) {
		browserType = type;
		if (type == DIRECTORY_BROWSER) {
			setTitle(Messages.RemoteResourceBrowser_directoryTitle);
		} else {
			setTitle(Messages.RemoteResourceBrowser_fileTitle);
		}
	}

	/**
	 * Show available connections on browser if possible.
	 * 
	 * @param enable
	 */
	public void showConnections(boolean enable) {
		this.showConnections = enable;
	}

	private void updateDialog() {
		if (okButton != null) {
			String path = getPath();
			okButton.setEnabled(getConnection() != null && path != null && !path.equals(EMPTY_STRING));
		}
	}
}
